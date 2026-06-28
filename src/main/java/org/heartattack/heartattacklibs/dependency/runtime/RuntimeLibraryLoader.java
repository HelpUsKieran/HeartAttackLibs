package org.heartattack.heartattacklibs.dependency.runtime;

import me.lucko.jarrelocator.JarRelocator;
import me.lucko.jarrelocator.Relocation;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class RuntimeLibraryLoader {
    private final JavaPlugin plugin;
    private final Path libraryFolder;
    private final URLClassLoaderAccess loaderAccess;

    public RuntimeLibraryLoader(JavaPlugin plugin) {
        this.plugin = plugin;
        this.libraryFolder = plugin.getDataFolder().toPath().resolve("libraries");
        this.loaderAccess = URLClassLoaderAccess.create(plugin.getClass().getClassLoader());
    }

    public boolean loadConfigured(FileConfiguration config) {
        boolean enabled = config.getBoolean("runtime-libraries.enabled", false);
        if (!enabled) {
            return true;
        }

        if (loaderAccess == null) {
            plugin.getLogger().warning("Runtime loader unavailable on this classloader implementation.");
            return false;
        }

        try {
            Files.createDirectories(libraryFolder);
        } catch (IOException exception) {
            plugin.getLogger().warning("Could not create runtime library directory: " + exception.getMessage());
            return false;
        }

        ConfigurationSection libsSection = config.getConfigurationSection("runtime-libraries.libraries");
        if (libsSection == null) {
            return true;
        }

        for (String key : libsSection.getKeys(false)) {
            ConfigurationSection section = libsSection.getConfigurationSection(key);
            if (section == null) {
                continue;
            }

            LibraryDefinition definition = parseDefinition(section);
            boolean loaded = loadLibrary(definition);
            if (!loaded && definition.required()) {
                plugin.getLogger().severe("Required runtime library failed to load: " + key);
                return false;
            }
        }
        return true;
    }

    private LibraryDefinition parseDefinition(ConfigurationSection section) {
        String group = section.getString("group", "");
        String artifact = section.getString("artifact", "");
        String version = section.getString("version", "");
        String repo = section.getString("repository", "https://repo1.maven.org/maven2");
        boolean required = section.getBoolean("required", false);

        List<RelocationRule> relocations = new ArrayList<>();
        ConfigurationSection relocationSection = section.getConfigurationSection("relocations");
        if (relocationSection != null) {
            for (String from : relocationSection.getKeys(false)) {
                String to = relocationSection.getString(from);
                if (to != null && !to.isBlank()) {
                    relocations.add(new RelocationRule(from, to));
                }
            }
        }
        return new LibraryDefinition(group, artifact, version, repo, required, relocations);
    }

    private boolean loadLibrary(LibraryDefinition definition) {
        if (definition.groupId().isBlank() || definition.artifactId().isBlank() || definition.version().isBlank()) {
            plugin.getLogger().warning("Skipping invalid runtime library definition.");
            return false;
        }

        Path targetJar = libraryFolder.resolve(definition.fileName());
        if (!Files.exists(targetJar)) {
            if (!downloadLibrary(definition, targetJar)) {
                return false;
            }
        }

        try {
            loaderAccess.addUrl(targetJar.toUri().toURL());
            return true;
        } catch (Exception exception) {
            plugin.getLogger().warning("Could not load library " + definition.fileName() + ": " + exception.getMessage());
            return false;
        }
    }

    private boolean downloadLibrary(LibraryDefinition definition, Path targetJar) {
        try {
            URL url = new URL(definition.downloadUrl());
            Path tempJar = libraryFolder.resolve(definition.fileName() + ".tmp");

            try (InputStream inputStream = url.openStream()) {
                Files.copy(inputStream, tempJar);
            }

            if (definition.relocations().isEmpty()) {
                Files.move(tempJar, targetJar);
                return true;
            }

            List<Relocation> relocationList = new ArrayList<>();
            for (RelocationRule rule : definition.relocations()) {
                relocationList.add(new Relocation(rule.from(), rule.to()));
            }
            new JarRelocator(tempJar.toFile(), targetJar.toFile(), relocationList).run();
            Files.deleteIfExists(tempJar);
            return true;
        } catch (Exception exception) {
            plugin.getLogger().warning("Failed downloading runtime library " + definition.fileName() + ": " + exception.getMessage());
            return false;
        }
    }
}
