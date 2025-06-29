package someoneok.kic.utils;

import net.minecraft.client.gui.FontRenderer;
import someoneok.kic.KIC;
import someoneok.kic.utils.dev.KICLogger;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StringUtils {
    private static final Pattern UUID_V4_PATTERN = Pattern.compile(
            "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", Pattern.CASE_INSENSITIVE);
    private final static Pattern COLOR_CODE_PATTERN = Pattern.compile("(§.)");

    public static String formatPlayerName(String player) {
        if (player == null || player.isEmpty()) {
            return null;
        }

        String formattedPlayer = player.replaceAll("[^a-zA-Z0-9_]", "");

        if (formattedPlayer.length() < 3) {
            return null;
        }

        if (formattedPlayer.length() > 16) {
            formattedPlayer = formattedPlayer.substring(0, 16);
        }

        return formattedPlayer;
    }

    public static String parseToShorthandNumber(double labelValue) {
        if (labelValue == 0) return "0";
        int sign = (int) Math.signum(labelValue);
        double absoluteValue = Math.abs(labelValue);

        if (absoluteValue >= 1.0e+12) {
            return String.format("%.2fT", sign * (absoluteValue / 1.0e+12));
        } else if (absoluteValue >= 1.0e+9) {
            return String.format("%.2fB", sign * (absoluteValue / 1.0e+9));
        } else if (absoluteValue >= 1.0e+6) {
            return String.format("%.2fM", sign * (absoluteValue / 1.0e+6));
        } else if (absoluteValue >= 1.0e+3) {
            return String.format("%.2fK", sign * (absoluteValue / 1.0e+3));
        } else {
            return String.valueOf(sign * absoluteValue);
        }
    }

    public static boolean isValidUUIDv4(String uuid) {
        if (uuid == null || uuid.isEmpty()) return false;
        try {
            UUID test = UUID.fromString(uuid);
            return test.version() == 4;
        } catch (IllegalArgumentException e) {
            KICLogger.info("Invalid UUID format: " + uuid);
            return false;
        }
    }

    public static boolean isValidUUIDv4RegexBased(String uuid) {
        if (uuid == null || !UUID_V4_PATTERN.matcher(uuid).matches()) return false;
        return UUID.fromString(uuid).version() == 4;
    }

    public static String formatElapsedTime(double seconds, int fixed, int units) {
        int days = (int) (seconds / 86400);
        int hours = (int) ((seconds % 86400) / 3600);
        int minutes = (int) ((seconds % 3600) / 60);
        String remainingSeconds = String.format(("%." + fixed + "f"), seconds % 60);

        StringBuilder timeParts = new StringBuilder();

        if (days > 0 && units > 0) {
            timeParts.append(days).append("d");
            units--;
        }

        if ((hours > 0 || days > 0) && units > 0) {
            timeParts.append(String.format("%02dh", hours));
            units--;
        }

        if ((minutes > 0 || hours > 0 || days > 0) && units > 0) {
            timeParts.append(String.format("%02dm", minutes));
            units--;
        }

        if (units > 0) {
            timeParts.append(String.format("%02ds", Integer.parseInt(remainingSeconds.split("\\.")[0])));
        }

        return timeParts.toString();
    }

    public static String formatElapsedTimeMs(long millis) {
        long totalSeconds = millis / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        StringBuilder sb = new StringBuilder();

        if (hours > 0) {
            sb.append(hours).append("h");
        }

        if (minutes > 0 || hours > 0) {
            sb.append(minutes).append("m");
        }

        if (hours == 0 && minutes == 0) {
            double fractionalSeconds = millis / 1000.0;
            sb.append(String.format("%.2fs", fractionalSeconds));
        } else {
            sb.append(seconds).append("s");
        }

        return sb.toString();
    }

    public static String removeFormatting(String text) {
        return text.replaceAll("§.", "");
    }

    public static String removeUnicode(String input) {
        return input.replaceAll("[^\\x00-\\x7F]", "");
    }

    public static String formatId(String id) {
        if (id == null || id.isEmpty()) return "";

        return Pattern.compile("_")
                .splitAsStream(id.toLowerCase().replace("mending", "vitality"))
                .map(word -> word.isEmpty() ? "" : Character.toUpperCase(word.charAt(0)) + word.substring(1))
                .collect(Collectors.joining(" "));
    }

    public static String applyEffect(String text, String effect) {
        StringBuilder result = new StringBuilder();
        Matcher matcher = COLOR_CODE_PATTERN.matcher(text);
        int lastEnd = 0;

        while (matcher.find()) {
            result.append(text, lastEnd, matcher.end());
            result.append(effect);
            lastEnd = matcher.end();
        }

        if (lastEnd < text.length()) {
            result.append(text.substring(lastEnd));
        }

        return result.toString();
    }

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static String timeSince(long timestamp) {
        Instant pastInstant = Instant.ofEpochMilli(timestamp);
        Instant nowInstant = Instant.now();

        Duration duration = Duration.between(pastInstant, nowInstant);

        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;

        List<String> parts = new ArrayList<>();

        if (days > 0) parts.add(days + " day" + (days > 1 ? "s" : ""));
        if (hours > 0) parts.add(hours + " hr" + (hours > 1 ? "s" : ""));
        if (minutes > 0) parts.add(minutes + " min");
        if (seconds > 0 || parts.isEmpty()) parts.add(seconds + " sec");

        if (parts.size() > 2) {
            parts = parts.subList(0, 2);
        }

        return String.join(" ", parts) + " ago";
    }

    public static String generateDashString(String startString, String color) {
        FontRenderer fontRenderer = KIC.mc.fontRendererObj;

        String visibleText = removeFormatting(startString);
        int startWidth = fontRenderer.getStringWidth(visibleText);
        int dashWidth = fontRenderer.getStringWidth("-");
        int dashCount = startWidth / dashWidth;

        return "\n" + color + new String(new char[dashCount]).replace('\0', '-');
    }

    public static String getRankColor(String message) {
        if (message.contains("YOUTUBE")) {
            return "§c";
        } else if (message.contains("[MVP++]")) {
            return "§6";
        } else if (message.contains("[MVP+]") || message.contains("[MVP]")) {
            return "§b";
        } else if (message.contains("VIP")) {
            return "§a";
        } else {
            return "§7";
        }
    }
}
