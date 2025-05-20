package someoneok.kic.utils.overlay;

import static someoneok.kic.KIC.mc;
import static someoneok.kic.utils.StringUtils.removeFormatting;

public class OverlayUtils {
    public static void drawString(int x, int y, String str, TextStyle textStyle, Alignment alignment) {
        String text = "§r" + str;
        int startX;

        switch (alignment) {
            case Center:
                startX = x - mc.fontRendererObj.getStringWidth(str) / 2;
                break;
            case Right:
                startX = x - mc.fontRendererObj.getStringWidth(str);
                break;
            default:
                startX = x;
                break;
        }

        switch (textStyle) {
            case Default:
                mc.fontRendererObj.drawString(text, startX, y, -1, false);
                break;
            case Shadow:
                mc.fontRendererObj.drawString(text, startX, y, -1, true);
                break;
            case Outline:
                String rawString = removeFormatting(text);

                mc.fontRendererObj.drawString(rawString, startX - 1, y, -16777216);
                mc.fontRendererObj.drawString(rawString, startX + 1, y, -16777216);
                mc.fontRendererObj.drawString(rawString, startX, y - 1, -16777216);
                mc.fontRendererObj.drawString(rawString, startX, y + 1, -16777216);
                mc.fontRendererObj.drawString(text, startX, y, -1);
                break;
        }
    }

    public enum TextStyle {
        Default,
        Shadow,
        Outline;

        public static TextStyle fromInt(int number) {
            switch (number) {
                case 0:
                    return Default;
                case 1:
                    return Shadow;
                case 2:
                    return Outline;
                default:
                    return null;
            }
        }
    }

    public enum Alignment {
        Left,
        Center,
        Right
    }
}
