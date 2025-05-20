package someoneok.kic.models;

public enum Color {
    BLACK("Black", "§0"),
    DARK_BLUE("Dark Blue", "§1"),
    DARK_GREEN("Dark Green", "§2"),
    DARK_AQUA("Dark Aqua", "§3"),
    DARK_RED("Dark Red", "§4"),
    DARK_PURPLE("Dark Purple", "§5"),
    GOLD("Gold", "§6"),
    GRAY("Gray", "§7"),
    DARK_GRAY("Dark Gray", "§8"),
    BLUE("Blue", "§9"),
    GREEN("Green", "§a"),
    AQUA("Aqua", "§b"),
    RED("Red", "§c"),
    LIGHT_PURPLE("Light Purple", "§d"),
    YELLOW("Yellow", "§e"),
    WHITE("White", "§f");

    private final String name;
    private final String code;

    Color(String name, String code) {
        this.name = name;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public static Color fromCode(String code) {
        for (Color color : values()) {
            if (color.code.equals(code)) {
                return color;
            }
        }
        return null;
    }

    public static String getColorCode(int index) {
        Color[] colors = Color.values();
        if (index < 0 || index >= colors.length) {
            return Color.WHITE.getCode();
        }
        return colors[index].getCode();
    }
}
