# HeartAttackLibs

HeartAttackLibs is a public Paper library plugin for Minecraft servers. It provides reusable framework services for plugin authors who want shared helpers for commands, configuration, databases, GUI menus, messages, modules, dependencies, items, holograms, text formatting, and general utilities.

## Requirements

- Java 21
- Paper API 1.21 or compatible
- Maven 3.9+

Optional runtime integrations:

- PlaceholderAPI
- Vault

## Installation

1. Download the latest jar from the GitHub Releases page.
2. Place it in your server's `plugins/` folder.
3. Restart the server.

Other plugins can then access HeartAttackLibs through Bukkit services.

## Public API

```java
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.heartattack.heartattacklibs.api.HeartAttackLibsApi;

RegisteredServiceProvider<HeartAttackLibsApi> provider = Bukkit.getServicesManager().getRegistration(HeartAttackLibsApi.class);
HeartAttackLibsApi api = provider == null ? null : provider.getProvider();
```

Main entry points:

- `api.command()` / `api.commands()`
- `api.configFramework()` / `api.configs()`
- `api.databaseFramework()` / `api.createDatabase(plugin)`
- `api.gui()`
- `api.messages()` / `api.pathMessages()`
- `api.modules()`
- `api.dependencies()`
- `api.itemFramework()`
- `api.textFramework()`
- `api.utilityFramework()`

## Build From Source

```bash
git clone https://github.com/HelpUsKieran/HeartAttackLibs.git
cd HeartAttackLibs
mvn clean package
```

The built jar is created in `target/`.

## Releases

Stable public releases are created from version tags:

```bash
git tag v1.0.0
git push origin v1.0.0
```

The release workflow builds the jar with Java 21, creates a GitHub Release, and uploads the plugin jar automatically.

## Development Notes

Generated build output, IDE files, local server folders, private jars, logs, and environment files are ignored by `.gitignore`.
