package org.heartattack.heartattacklibs.command.typed;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.heartattack.heartattacklibs.command.CommandContext;
import org.heartattack.heartattacklibs.util.MaterialResolver;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class ArgumentRegistry {
    private ArgumentRegistry() {
    }

    public static ArgumentParser<String> word() {
        return new ArgumentParser<>() {
            @Override
            public String parse(CommandContext context, String input) {
                return input;
            }

            @Override
            public String typeName() {
                return "word";
            }
        };
    }

    public static ArgumentParser<Integer> integer() {
        return new ArgumentParser<>() {
            @Override
            public Integer parse(CommandContext context, String input) {
                try {
                    return Integer.parseInt(input);
                } catch (NumberFormatException exception) {
                    throw new IllegalArgumentException("expected integer");
                }
            }

            @Override
            public String typeName() {
                return "int";
            }
        };
    }

    public static ArgumentParser<Double> decimal() {
        return new ArgumentParser<>() {
            @Override
            public Double parse(CommandContext context, String input) {
                try {
                    return Double.parseDouble(input);
                } catch (NumberFormatException exception) {
                    throw new IllegalArgumentException("expected decimal");
                }
            }

            @Override
            public String typeName() {
                return "double";
            }
        };
    }

    public static ArgumentParser<Boolean> bool() {
        return new ArgumentParser<>() {
            @Override
            public Boolean parse(CommandContext context, String input) {
                if (input.equalsIgnoreCase("true") || input.equalsIgnoreCase("yes") || input.equals("1")) {
                    return true;
                }
                if (input.equalsIgnoreCase("false") || input.equalsIgnoreCase("no") || input.equals("0")) {
                    return false;
                }
                throw new IllegalArgumentException("expected true/false");
            }

            @Override
            public List<String> suggest(CommandContext context, String input) {
                return List.of("true", "false");
            }

            @Override
            public String typeName() {
                return "boolean";
            }
        };
    }

    public static ArgumentParser<Player> player() {
        return new ArgumentParser<>() {
            @Override
            public Player parse(CommandContext context, String input) {
                Player player = Bukkit.getPlayerExact(input);
                if (player == null) {
                    throw new IllegalArgumentException("player not found");
                }
                return player;
            }

            @Override
            public List<String> suggest(CommandContext context, String input) {
                return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
            }

            @Override
            public String typeName() {
                return "player";
            }
        };
    }

    public static ArgumentParser<Material> material() {
        return new ArgumentParser<>() {
            @Override
            public Material parse(CommandContext context, String input) {
                Material material = MaterialResolver.parseModern(input);
                if (material == null) {
                    throw new IllegalArgumentException("material not found");
                }
                return material;
            }

            @Override
            public List<String> suggest(CommandContext context, String input) {
                return Arrays.stream(Material.values()).map(m -> m.name().toLowerCase(Locale.ROOT)).toList();
            }

            @Override
            public String typeName() {
                return "material";
            }
        };
    }

    public static <E extends Enum<E>> ArgumentParser<E> enumValue(Class<E> enumClass) {
        return new ArgumentParser<>() {
            @Override
            public E parse(CommandContext context, String input) {
                for (E value : enumClass.getEnumConstants()) {
                    if (value.name().equalsIgnoreCase(input)) {
                        return value;
                    }
                }
                throw new IllegalArgumentException("expected one of " + Arrays.toString(enumClass.getEnumConstants()));
            }

            @Override
            public List<String> suggest(CommandContext context, String input) {
                return Arrays.stream(enumClass.getEnumConstants()).map(e -> e.name().toLowerCase(Locale.ROOT)).toList();
            }

            @Override
            public String typeName() {
                return enumClass.getSimpleName().toLowerCase(Locale.ROOT);
            }
        };
    }
}
