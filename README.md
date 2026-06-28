# HeartAttackLibs Usage Guide

HeartAttackLibs is a public Paper library plugin for Minecraft servers. It provides reusable framework services for plugin authors who want shared helpers for commands, configuration, databases, GUI menus, messages, modules, dependencies, items, holograms, text formatting, and general utilities.

## Requirements

- Java `21`
- Paper API `1.21+` or compatible
- Maven `3.9+` for building from source
- Add `HeartAttackLibs` as a `depend` or `softdepend` in plugins that consume the API

Optional runtime integrations:

- `PlaceholderAPI` for placeholder parsing
- `Vault` for economy, permissions, and chat providers

Example consumer `plugin.yml`:

```yml
name: MyPlugin
main: com.example.myplugin.MyPlugin
version: 1.0.0
api-version: '1.21'
softdepend:
  - HeartAttackLibs
```

Use `depend` instead of `softdepend` if your plugin cannot run without HeartAttackLibs.

## Installation

1. Download the latest jar from the GitHub Releases page.
2. Place it in your server's `plugins/` folder.
3. Restart the server.
4. Confirm `HeartAttackLibs` appears in the server plugin list.

## Getting The API

HeartAttackLibs registers `HeartAttackLibsApi` in Bukkit's `ServicesManager` when the plugin enables.

```java
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.heartattack.heartattacklibs.api.HeartAttackLibsApi;

public HeartAttackLibsApi heartAttackLibs() {
    RegisteredServiceProvider<HeartAttackLibsApi> registration =
            Bukkit.getServicesManager().getRegistration(HeartAttackLibsApi.class);
    return registration == null ? null : registration.getProvider();
}
```

A practical enable-time guard:

```java
private HeartAttackLibsApi libs;

@Override
public void onEnable() {
    libs = heartAttackLibs();
    if (libs == null) {
        getLogger().warning("HeartAttackLibs is not available; disabling plugin.");
        getServer().getPluginManager().disablePlugin(this);
        return;
    }

    getLogger().info("Hooked into HeartAttackLibs.");
}
```

Main API entry points:

- `command()` / `commandFramework()` / `commands()`
- `configFramework()` / `configs()`
- `databaseFramework()` / `database()` / `createDatabase(plugin)`
- `gui()` / `guiFramework()`
- `messageFramework()` / `messages()` / `pathMessages()`
- `moduleFramework()` / `modules()`
- `dependencyFramework()` / `dependencies()`
- `hologramFramework()` / `holograms()`
- `itemFramework()`
- `textFramework()` / `miniMessage()`
- `formatFramework()`
- `debugFramework()` / `debug()`
- `utilityFramework()`
- `playerSettings()`

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

You can also run the **Stable Release** workflow manually from GitHub Actions and enter a release tag such as `v1.0.0`. Manual runs build from the selected branch, create the tag/release, and upload the plugin jar automatically.

Release tags must use a semantic version style such as `v1.0.0` or `v1.0.0-beta.1`.

## Commands

HeartAttackLibs exposes command helpers so consuming plugins can create commands without repetitive Bukkit boilerplate.

### Simple command

```java
libs.command().register(
    libs.command().command("hello")
        .description("Say hello")
        .usage("/hello")
        .permission("myplugin.hello")
        .executor(ctx -> ctx.sender().sendRichMessage("<green>Hello from MyPlugin."))
        .build()
);
```

### Player-only command

```java
libs.command().register(
    libs.command().command("whereami")
        .playerOnly(true)
        .playerOnlyMessage("<red>Only players can use this command.")
        .executor(ctx -> {
            Player player = ctx.asPlayer();
            player.sendRichMessage("<gray>You are in <white>" + player.getWorld().getName());
        })
        .build()
);
```

### Router command with subcommands

```java
libs.command().register(
    libs.command().router("myadmin")
        .permission("myplugin.admin")
        .defaultExecutor(ctx -> ctx.sender().sendRichMessage("<red>Use /myadmin <reload|status>"))
        .subcommand("reload", sub -> sub
            .description("Reload plugin files")
            .executor(ctx -> {
                reloadConfig();
                ctx.sender().sendRichMessage("<green>Reloaded.");
            }))
        .subcommand("status", sub -> sub
            .description("Show plugin status")
            .executor(ctx -> ctx.sender().sendRichMessage("<gray>Status: <green>OK")))
        .build()
);
```

### Typed command

Typed commands parse arguments before your executor runs.

```java
public final class GiveCoinsCommand extends TypedCommand {
    @Override
    public String name() {
        return "givecoins";
    }

    @Override
    public String permission() {
        return "myplugin.givecoins";
    }

    @Override
    protected List<CommandArgument<?>> arguments() {
        return List.of(
            CommandArgument.required("player", ArgumentRegistry.player()),
            CommandArgument.required("amount", ArgumentRegistry.integer())
        );
    }

    @Override
    protected void executeTyped(CommandContext context, ParsedArguments args) {
        Player target = args.get("player");
        int amount = args.get("amount");
        context.sender().sendRichMessage("<green>Gave " + amount + " coins to " + target.getName());
    }
}
```

Register it:

```java
libs.command().register(new GiveCoinsCommand());
```

Built-in typed parsers:

- `ArgumentRegistry.word()`
- `ArgumentRegistry.integer()`
- `ArgumentRegistry.decimal()`
- `ArgumentRegistry.bool()`
- `ArgumentRegistry.player()`
- `ArgumentRegistry.material()`
- `ArgumentRegistry.enumValue(MyEnum.class)`

## Config Framework

Use the config framework for plugin-owned YAML files.

```java
ConfigFileHandle settings = libs.configFramework().getOrRegister(this, "settings.yml", true);

String mode = settings.string("mode", "default");
int limit = settings.intValue("limit", 10);
boolean enabled = settings.booleanValue("enabled", true);

settings.set("last-loaded", System.currentTimeMillis());
settings.save();
```

Useful operations:

- `reload()`
- `save()`
- `onReload(...)`
- `requireString(...)`
- `optionalInt(...)`
- `stringList(...)`
- `section(...)`
- `keys(...)`

Use the shared `ConfigManager` for files owned by HeartAttackLibs itself:

```java
String prefix = libs.configs().getString("messages.prefix");
```

Most consuming plugins should prefer `configFramework().getOrRegister(this, ...)` so files are stored in the consuming plugin's data folder.

## Message Framework

Message files are parsed from `messages.<key>` entries.

Example `messages.yml` in your plugin:

```yml
messages:
  welcome:
    line: "<green>Welcome, {player}!"
  reward:
    lines:
      - "<gold>Reward claimed"
      - "<gray>+{amount} coins"
    actionbar: "<yellow>+{amount}"
    title: "<green>Success"
    subtitle: "<gray>You got rewarded"
    times:
      fadeIn: 10
      stay: 40
      fadeOut: 10
    sound:
      name: "ENTITY_EXPERIENCE_ORB_PICKUP"
      volume: 1.0
      pitch: 1.2
```

Register and send messages:

```java
MessageRegistry registry = libs.messageFramework().getOrRegister(this, "messages.yml");

registry.send("welcome", player, MessagePlaceholder.of("player", player.getName()));
registry.broadcast("reward", MessagePlaceholder.of("amount", 250));
```

Use `MessageContainer` when you want to keep a message reference:

```java
MessageContainer reward = registry.container("reward");
reward.send(player, MessagePlaceholder.of("amount", 250));
```

## Path Messages

`PathMessageService` is useful when you already have a Bukkit `FileConfiguration` and want to read a message by path.

```java
Map<String, String> placeholders = Map.of("player", player.getName());
libs.pathMessages().send(
    player,
    getConfig(),
    "messages.welcome",
    "<green>Welcome, {player}!",
    placeholders
);
```

It can return raw strings, legacy-colored strings, Adventure components, and component lists.

## Text Framework

Text helpers support MiniMessage and legacy `&` color input.

```java
libs.textFramework().send(player, "<green>Saved successfully.");
libs.textFramework().sendWithPrefix(player, "<gray>Your balance is <gold>100");
```

Deserialize to an Adventure component:

```java
Component component = libs.textFramework().deserialize("<aqua>Hello <white>world");
player.sendMessage(component);
```

Use placeholders:

```java
Component component = libs.textFramework().deserialize(
    "<green>Hello <name>",
    libs.miniMessage().placeholder("name", player.getName())
);
```

## GUI Framework

The GUI framework loads YAML menus and supports static items, dynamic rendered sections, requirements, actions, pagination, and placeholders.

Register your menu folder:

```java
libs.gui().registerMenus(this, "menus");
```

Open a menu:

```java
libs.gui().open(player, "shop", GuiOpenContext.of(Map.of("source", "command")));
```

Reload only your plugin's menus:

```java
libs.gui().reloadPluginMenus(this);
```

### Register custom GUI behavior

```java
libs.gui().bindAction(this, "my_action", context -> {
    context.player().sendRichMessage("<green>Clicked custom action.");
});

libs.gui().bindRequirement(this, "has_even_level", context -> context.player().getLevel() % 2 == 0);

libs.gui().bindPlaceholder(this, "rank", context -> "Knight");

libs.gui().bindRenderer(this, "shop_items", context -> List.of(
    new GuiRenderedItem(
        libs.itemFramework().builder(Material.DIAMOND).name("<aqua>Diamond").build(),
        Map.of("price", "100")
    )
));
```

### Placeholder formats

Menu titles, item names, lore, actions, requirement values, and deny messages can use:

- `{key}`
- `<key>`
- `%key%`

Built-in values include:

- `player_name`
- `player_uuid`
- `menu_id`
- values from `GuiOpenContext`
- values returned by `bindPlaceholder(...)`
- values attached to a `GuiRenderedItem`

### Built-in action types

String form:

```yml
click-actions:
  - "[close]"
  - "[open_menu] other_menu"
  - "[message] <green>Hello <player_name>"
  - "[command] spawn"
  - "[console_command] say <player_name> clicked"
  - "[sound] ENTITY_EXPERIENCE_ORB_PICKUP;1.0;1.2"
  - "[refresh]"
```

Object form:

```yml
click-actions:
  - type: command
    value: "spawn"
```

Click action keys:

- `click-actions`
- `left-click-actions`
- `right-click-actions`
- `shift-left-click-actions`
- `shift-right-click-actions`

If no actions exist for the exact click type, the GUI falls back to left-click actions.

### Requirement types

Built-ins:

```yml
view-requirements:
  - "permission:myplugin.shop"
  - type: placeholder_equals
    key: source
    value: command
```

Requirement locations:

- `open-requirements` gates opening the menu
- `view-requirements` gates item visibility
- `click-requirements` gates action execution

Map-form requirements can include `deny-message`.

### Menu YAML reference

Top-level keys:

- `menu.id` defaults to the filename
- `menu.title` defaults to `GUI`
- `menu.size` is clamped to valid chest sizes from 9 to 54
- `menu.settings.cancel-clicks` defaults to `true`
- `menu.settings.cancel-drag` defaults to `true`
- `menu.settings.allow-player-inventory-click` defaults to `false`
- `menu.placeholders`
- `menu.open-requirements`
- `menu.items`
- `menu.dynamic`

Static item keys:

- `slot` or `slots`
- `material`
- `amount`
- `name`
- `lore`
- `glow`
- `flags`
- `custom-model-data`
- `texture`
- `texture.type` and `texture.value`
- `view-requirements`
- `click-requirements`
- click action keys

Dynamic section keys:

- `renderer-id`
- `slot` or `slots`
- `pagination.enabled`
- `pagination.page-size`
- `pagination.previous-slot`
- `pagination.next-slot`
- `view-requirements`
- `click-requirements`
- click action keys

### Complete GUI example

```yml
menu:
  id: shop
  title: "<dark_gray>Shop <gray>- <white><player_name>"
  size: 54
  settings:
    cancel-clicks: true
    cancel-drag: true
    allow-player-inventory-click: false
  placeholders:
    source: command
  open-requirements:
    - type: permission
      permission: myplugin.shop
      deny-message: "<red>No permission."

  items:
    close:
      slot: 49
      material: BARRIER
      name: "<red>Close"
      lore:
        - "<gray>Click to close."
      click-actions:
        - "[close]"

    info:
      slot: 4
      material: BOOK
      name: "<gold>Shop Info"
      lore:
        - "<gray>Opened by <player_name>."
        - "<gray>Source: <source>"
      click-actions:
        - "[message] <green>This is a static item."

  dynamic:
    listing:
      renderer-id: shop_items
      slots: [10, 11, 12, 13, 14, 15, 16]
      pagination:
        enabled: true
        page-size: 7
        previous-slot: 45
        next-slot: 53
      click-requirements:
        - type: has_even_level
          deny-message: "<red>You need an even level."
      click-actions:
        - "[message] <green>You clicked an item priced <price>."
        - "[sound] ENTITY_EXPERIENCE_ORB_PICKUP;1.0;1.2"
```

## LiveMenu Builder

`LiveMenu` is useful when you want to build an inventory directly in Java instead of YAML.

```java
LiveMenu menu = LiveMenu.builder(this, "<dark_gray>Confirm", 3)
    .item(11, libs.itemFramework().builder(Material.LIME_DYE).name("<green>Confirm").build())
    .button(15, libs.itemFramework().builder(Material.RED_DYE).name("<red>Cancel").build(), click -> {
        click.player().closeInventory();
    })
    .onClose(player -> getLogger().info(player.getName() + " closed the menu"))
    .build();

menu.open(player);
```

Input regions can collect items from specific slots:

```java
LiveMenu menu = LiveMenu.builder(this, "<dark_gray>Deposit", 3)
    .inputRegion("deposit", new int[] {10, 11, 12, 13, 14, 15, 16})
    .onInputChange((player, region) -> player.sendRichMessage("<gray>Updated " + region))
    .build();
```

## Item Framework

Create items fluently:

```java
ItemStack blade = libs.itemFramework().builder(Material.DIAMOND_SWORD)
    .name("<gold><bold>Blade")
    .lore("<gray>Line one", "<gray>Line two")
    .glow()
    .unbreakable(true)
    .customModelData(1001)
    .build();
```

Builder features:

- `.amount(int)`
- `.name(String)`
- `.lore(List<String>)` and `.lore(String...)`
- `.addLoreLine(String)`
- `.enchant(...)`
- `.flags(ItemFlag...)`
- `.unbreakable(boolean)`
- `.glow()`
- `.customModelData(int)`
- `.texture(String url)`
- `.texture(String type, String value)` with `url`, `base64`, or `player`
- `.textureBase64(...)`
- `.texturePlayer("Notch")`

Build from config:

```yml
reward-item:
  material: PLAYER_HEAD
  amount: 1
  name: "<gold>Reward Head"
  lore:
    - "<gray>Claimed by <player_name>"
  glow: true
  texture:
    type: player
    value: Notch
```

```java
ItemStack reward = libs.itemFramework().build(getConfig().getConfigurationSection("reward-item"), Material.STONE);
```

## Hologram Framework

```java
Hologram hologram = libs.hologramFramework().create(
    HologramBuilder.at(player.getLocation().add(0, 2.2, 0))
        .lines("<gold>Reward", "<gray>Claimed by " + player.getName())
        .lineSpacing(0.3)
        .viewRange(48f)
);

hologram.spawn();
hologram.updateLine(0, "<green>Updated reward");
libs.hologramFramework().remove(hologram);
```

Call `libs.hologramFramework().despawnAll()` or `libs.holograms().despawnAll()` from your shutdown path if you manage long-lived holograms yourself.

Owner tagging can prevent duplicate ghost holograms after chunk reloads or lost references:

```java
NamespacedKey ownerKey = new NamespacedKey(this, "hologram-owner");
String ownerId = "spawn-info";

Hologram hologram = libs.hologramFramework().create(
    HologramBuilder.at(location)
        .owner(ownerKey, ownerId)
        .lines("<green>Spawn", "<gray>Welcome")
);

// Optional manual cleanup for matching orphaned TextDisplay/ItemDisplay entities in the chunk.
int removed = Hologram.purgeOrphans(location, ownerKey, ownerId);
```

## Database Framework

Use `createDatabase(plugin)` for plugin-specific persistence. This stores SQLite files under your plugin's data folder instead of HeartAttackLibs' folder.

```java
DatabaseFramework database = libs.createDatabase(this);
database.connect(DatabaseConfig.sqlite("data.db"));
```

MySQL example:

```java
database.connect(DatabaseConfig.mysql("localhost", 3306, "minecraft", "user", "password"));
```

Create/update data:

```java
database.update(
    "CREATE TABLE IF NOT EXISTS users (uuid VARCHAR(36) PRIMARY KEY, coins INT NOT NULL)"
);

database.update(
    "INSERT INTO users (uuid, coins) VALUES (?, ?) ON DUPLICATE KEY UPDATE coins = ?",
    ps -> {
        ps.setString(1, player.getUniqueId().toString());
        ps.setInt(2, 100);
        ps.setInt(3, 100);
    }
);
```

Query data:

```java
Integer coins = database.query(
    "SELECT coins FROM users WHERE uuid = ?",
    ps -> ps.setString(1, player.getUniqueId().toString()),
    rs -> rs.next() ? rs.getInt("coins") : 0
);
```

Close plugin-owned database connections during disable:

```java
@Override
public void onDisable() {
    if (database != null) {
        database.close();
    }
}
```

## Dependency Framework

The dependency framework detects optional providers and exposes safe fallbacks.

```java
DependencyStatus economyStatus = libs.dependencyFramework().status(DependencyCapability.ECONOMY);
if (economyStatus.available()) {
    double balance = libs.dependencyFramework().economy().balance(player);
    player.sendRichMessage("<gray>Balance: <gold>" + balance);
}
```

Capabilities:

- `PLACEHOLDER`
- `ECONOMY`
- `PERMISSION`
- `CHAT`
- `HOLOGRAM`
- `RUNTIME_LIBRARY_LOADER`

Register a custom placeholder provider:

```java
libs.dependencyFramework().registerPlaceholderProvider(new DPlaceholderProvider() {
    @Override
    public String key() {
        return "myplugin";
    }

    @Override
    public String onPlaceholderRequest(Player player, String[] params) {
        if (params.length > 0 && params[0].equalsIgnoreCase("rank")) {
            return "Knight";
        }
        return "";
    }
});
```

Then placeholders such as `{myplugin_rank}` can be resolved by framework systems that use the dependency placeholder bridge.

## Runtime Library Loader

Runtime libraries are configured in `dependencies.yml`.

```yml
runtime-libraries:
  enabled: true
  libraries:
    example-lib:
      group: "com.example"
      artifact: "example"
      version: "1.0.0"
      repository: "https://repo1.maven.org/maven2"
      required: false
      relocations:
        "com.example": "my.plugin.libs.example"
```

Use this carefully for libraries that are not already shaded into your plugin. For public plugins, prefer normal Maven dependencies and shading unless runtime resolution is specifically needed.

## Module Framework

Modules are lightweight feature units with dependencies and enable/disable hooks.

```java
public final class RewardsModule implements DModule {
    @Override
    public String name() {
        return "rewards";
    }

    @Override
    public Set<String> dependencies() {
        return Set.of("core");
    }

    @Override
    public void onEnable(ModuleContext context) {
        context.plugin().getLogger().info("Rewards module enabled.");
    }

    @Override
    public void onDisable(ModuleContext context) {
        context.plugin().getLogger().info("Rewards module disabled.");
    }
}
```

Register and enable:

```java
libs.moduleFramework().register(new RewardsModule());
libs.moduleFramework().loadAndEnableAll();
```

State values:

- `REGISTERED`
- `ENABLED`
- `DISABLED`
- `FAILED`

Config gate in `modules.yml`:

```yml
modules:
  rewards:
    enabled: true
```

`PluginModuleManager<C>` is also available if you want a generic module manager for your own plugin context.

## Player Settings

HeartAttackLibs includes a reusable player settings service for toggles such as messages, actionbars, titles, particles, and sounds.

```java
PlayerSettingsService settings = libs.playerSettings();
```

The default settings are controlled by `config.yml`:

```yml
settings:
  enabled: true
  defaults:
    messages: true
    actionbars: true
    titles: true
    particles: true
    sounds: true
```

Use this service when your plugin wants to respect player-level display preferences through the shared message systems.

## Debug Framework

```java
libs.debugFramework().setEnabled(true);
libs.debugFramework().debug("myplugin", "Something happened.");
```

Backed by config:

```yml
debug:
  enabled: false
  categories:
    - "*"
```

## Format Framework

```java
String compact = libs.formatFramework().durationCompact(3661); // 1h 1m 1s
String words = libs.formatFramework().durationWords(Duration.ofMinutes(90));
String money = libs.formatFramework().money(1234.5);
String shortNumber = libs.formatFramework().abbreviate(15320); // 15.32K
long millis = libs.formatFramework().parseDurationMillis("1h30m");
```

## Utility Framework

Create helper objects on demand.

### Cooldowns

```java
CooldownMap cooldowns = libs.utilityFramework().cooldowns();
cooldowns.set(player.getUniqueId(), 5000L);

if (cooldowns.onCooldown(player.getUniqueId())) {
    player.sendRichMessage("<red>Wait " + cooldowns.remainingSeconds(player.getUniqueId()) + "s.");
}
```

### Grouped cooldowns

```java
GroupedCooldownMap grouped = libs.utilityFramework().groupedCooldowns();
grouped.set("dash", player.getUniqueId(), 3000L);
```

### Task tracking

```java
TaskTracker tracker = libs.utilityFramework().taskTracker();
tracker.track(getServer().getScheduler().runTaskTimer(this, () -> {
    // repeating task
}, 1L, 20L));

@Override
public void onDisable() {
    tracker.cancelAll();
}
```

### Placeholder map

```java
String text = libs.utilityFramework().placeholders()
    .with("player", player.getName())
    .with("coins", 100)
    .apply("{player} has {coins} coins");
```

## Built-In HeartAttackLibs Commands

HeartAttackLibs registers a small built-in command set for server owners and developers.

Admin command:

- `/heartattacklibs help` shows command help
- `/heartattacklibs list` lists plugins depending on HeartAttackLibs
- `/heartattacklibs demo <...>` forwards to the demo command
- `/heartattacklibs debug <status|toggle|on|off>` controls framework debug mode
- `/heartattacklibs reload` reloads configs, messages, dependencies, modules, and menus
- `/heartattacklibs modules` shows module states
- `/heartattacklibs deps` shows dependency provider states

Admin command aliases:

- `/halibs`
- `/heartlibs`

Standalone reload command:

- `/heartattacklibsreload` reloads configs, messages, dependencies, modules, and menus

Reload command aliases:

- `/halreload`
- `/heartlibsreload`

Demo command:

- `/heartattacklibsdemo gui` opens the demo GUI
- `/heartattacklibsdemo template` opens the demo GUI with a context placeholder
- `/heartattacklibsdemo item` gives a demo item built with `ItemBuilder`
- `/heartattacklibsdemo reload` reloads the framework
- `/heartattacklibsdemo debug` toggles debug mode
- `/heartattacklibsdemo hologram` spawns a temporary demo hologram
- `/heartattacklibsdemo module` lists module states
- `/heartattacklibsdemo deps` lists dependency states
- `/heartattacklibsdemo balance` shows the player's Vault economy balance when available

Settings command:

- `/settings` opens the personal message/effect settings GUI
- `/settings toggle <plugin> <category> <location>` toggles a specific scoped setting

Permissions:

- `heartattacklibs.admin` defaults to `op`
- `heartattacklibs.demo` defaults to `op`
- `heartattacklibs.reload` defaults to `op`
- `heartattacklibs.settings` defaults to `true`

The command manager registers these dynamically at runtime, so a `commands:` block is not required in `plugin.yml`.

## Public Repository Hygiene

The repository is configured for public use:

- Build artifacts are ignored through `.gitignore`
- IDE files are ignored
- Local server runtime folders are ignored
- Private jar folders are ignored
- CI builds run on pushes and pull requests
- Stable releases are published from `v*` tags

