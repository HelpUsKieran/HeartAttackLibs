# HeartAttackLibs

HeartAttackLibs is a public-ready Paper library plugin. It keeps the reusable framework pieces from the original internal project: commands, config, database helpers, GUI menus, messages, modules, dependency providers, items, holograms, text formatting, and utilities.

## Build

```bash
mvn clean package
```

The built plugin jar is created in `target/`.

## Public API

Other plugins can consume the Bukkit service:

```java
RegisteredServiceProvider<HeartAttackLibsApi> provider = Bukkit.getServicesManager().getRegistration(HeartAttackLibsApi.class);
HeartAttackLibsApi api = provider == null ? null : provider.getProvider();
```

Main entry points include:

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

## Notes

This copy intentionally excludes internal hub APIs, private plugin bridges, generated build output, IDE files, and bundled private jars so it can serve as a clean public-library starting point.
