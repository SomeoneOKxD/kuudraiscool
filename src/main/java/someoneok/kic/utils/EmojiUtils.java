package someoneok.kic.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmojiUtils {
    private static final Map<String, String> EMOJI_LIST = new HashMap<>();
    private static final Pattern EMOJI_PATTERN;

    static {
        EMOJI_LIST.put("<3", "❤");
        EMOJI_LIST.put(":star:", "✮");
        EMOJI_LIST.put(":yes:", "✔");
        EMOJI_LIST.put(":no:", "✖");
        EMOJI_LIST.put(":java:", "☕");
        EMOJI_LIST.put(":arrow:", "➜");
        EMOJI_LIST.put(":shrug:", "¯\\_(ツ)_/¯");
        EMOJI_LIST.put(":tableflip:", "(╯°□°）╯︵ ┻━┻");
        EMOJI_LIST.put("o/", "( ﾟ◡ﾟ)/");
        EMOJI_LIST.put(":totem:", "☉_☉");
        EMOJI_LIST.put(":typing:", "✎...");
        EMOJI_LIST.put(":maths:", "√(π+x)=L");
        EMOJI_LIST.put(":snail:", "@'-'");
        EMOJI_LIST.put(":thinking:", "(0.o?)");
        EMOJI_LIST.put(":gimme:", "༼つ◕_◕༽つ");
        EMOJI_LIST.put(":wizard:", "('-')⊃━☆ﾟ.*･｡ﾟ");
        EMOJI_LIST.put(":pvp:", "⚔");
        EMOJI_LIST.put(":peace:", "✌");
        EMOJI_LIST.put(":puffer:", "<('O')>");
        EMOJI_LIST.put(":dog:", "(ᵔᴥᵔ)");
        EMOJI_LIST.put("h/", "ヽ(^◇^*)/");
        EMOJI_LIST.put(":cat:", "= ＾● ⋏ ●＾ =");
        EMOJI_LIST.put(":cute:", "(✿◠‿◠)");
        EMOJI_LIST.put(":snow:", "☃");
        EMOJI_LIST.put(":dj:", "ヽ(⌐■_■)ノ♬");
        EMOJI_LIST.put(":sloth:", "(・⊝・)");
        EMOJI_LIST.put(":yey:", "ヽ (◕◡◕) ﾉ");
        EMOJI_LIST.put(":skull:", "☠");
        EMOJI_LIST.put(":bum:", "♿");

        EMOJI_PATTERN = Pattern.compile(
                EMOJI_LIST.keySet().stream()
                        .map(Pattern::quote)
                        .reduce((a, b) -> a + "|" + b)
                        .orElse(""),
                Pattern.CASE_INSENSITIVE
        );
    }

    public static String replaceEmojis(String message) {
        Matcher matcher = EMOJI_PATTERN.matcher(message);
        StringBuffer replacedMessage = new StringBuffer();

        while (matcher.find()) {
            matcher.appendReplacement(replacedMessage, EMOJI_LIST.get(matcher.group()));
        }
        matcher.appendTail(replacedMessage);

        return replacedMessage.toString();
    }
}
