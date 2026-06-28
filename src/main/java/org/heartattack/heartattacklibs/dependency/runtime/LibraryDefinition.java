package org.heartattack.heartattacklibs.dependency.runtime;

import java.util.ArrayList;
import java.util.List;

public record LibraryDefinition(
        String groupId,
        String artifactId,
        String version,
        String repository,
        boolean required,
        List<RelocationRule> relocations
) {
    public LibraryDefinition {
        relocations = relocations == null ? new ArrayList<>() : relocations;
    }

    public String fileName() {
        return groupId.replace('.', '-') + "-" + artifactId.replace('.', '-') + "-" + version.replace('.', '-') + ".jar";
    }

    public String downloadUrl() {
        String base = repository.endsWith("/") ? repository : repository + "/";
        return base + groupId.replace('.', '/') + "/" + artifactId + "/" + version + "/" + artifactId + "-" + version + ".jar";
    }
}
