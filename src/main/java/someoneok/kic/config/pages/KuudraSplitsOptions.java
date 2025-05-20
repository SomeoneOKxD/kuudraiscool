package someoneok.kic.config.pages;

import cc.polyfrost.oneconfig.config.annotations.Button;
import cc.polyfrost.oneconfig.config.annotations.Dropdown;
import cc.polyfrost.oneconfig.config.annotations.Info;
import cc.polyfrost.oneconfig.config.annotations.Switch;
import cc.polyfrost.oneconfig.config.data.InfoType;
import someoneok.kic.models.kuudra.KuudraPhase;
import someoneok.kic.modules.kuudra.KuudraSplits;

import static someoneok.kic.models.Color.getColorCode;

public class KuudraSplitsOptions {
    private transient static final String TOGGLE = "Toggles";
    private transient static final String COLOR = "Colors";
    private transient static final String UPDATE = "Update";

    @Switch(
            name = "Show Pace",
            subcategory = TOGGLE
    )
    public static boolean showPace = true;

    @Switch(
            name = "Show Estimated Pace",
            subcategory = TOGGLE
    )
    public static boolean showEstimatedPace = true;

    @Switch(
            name = "Show Lag",
            subcategory = TOGGLE
    )
    public static boolean showLag = true;

    @Switch(
            name = "Show Lag In Seconds",
            subcategory = TOGGLE
    )
    public static boolean showLagInSeconds = true;

    @Switch(
            name = "Show Detailed Overview In Chat",
            subcategory = TOGGLE
    )
    public static boolean showDetailedOverview = false;

    @Switch(
            name = "Show Misc In Detailed Overview",
            subcategory = TOGGLE
    )
    public static boolean showMiscInDetailed = true;

    @Dropdown(
            name = "Supplies Color",
            options = {
                    "Black", "Dark Blue", "Dark Green", "Dark Aqua", "Dark Red", "Dark Purple",
                    "Gold", "Gray", "Dark Gray", "Blue", "Green", "Aqua", "Red", "Light Purple",
                    "Yellow", "White"
            },
            subcategory = COLOR
    )
    public static int suppliesColor = 4;

    @Dropdown(
            name = "Build Color",
            options = {
                    "Black", "Dark Blue", "Dark Green", "Dark Aqua", "Dark Red", "Dark Purple",
                    "Gold", "Gray", "Dark Gray", "Blue", "Green", "Aqua", "Red", "Light Purple",
                    "Yellow", "White"
            },
            subcategory = COLOR
    )
    public static int buildColor = 12;

    @Dropdown(
            name = "Eaten Color",
            options = {
                    "Black", "Dark Blue", "Dark Green", "Dark Aqua", "Dark Red", "Dark Purple",
                    "Gold", "Gray", "Dark Gray", "Blue", "Green", "Aqua", "Red", "Light Purple",
                    "Yellow", "White"
            },
            subcategory = COLOR
    )
    public static int eatenColor = 6;

    @Dropdown(
            name = "Stun Color",
            options = {
                    "Black", "Dark Blue", "Dark Green", "Dark Aqua", "Dark Red", "Dark Purple",
                    "Gold", "Gray", "Dark Gray", "Blue", "Green", "Aqua", "Red", "Light Purple",
                    "Yellow", "White"
            },
            subcategory = COLOR
    )
    public static int stunColor = 14;

    @Dropdown(
            name = "DPS Color",
            options = {
                    "Black", "Dark Blue", "Dark Green", "Dark Aqua", "Dark Red", "Dark Purple",
                    "Gold", "Gray", "Dark Gray", "Blue", "Green", "Aqua", "Red", "Light Purple",
                    "Yellow", "White"
            },
            subcategory = COLOR
    )
    public static int dpsColor = 15;

    @Dropdown(
            name = "Skip Color",
            options = {
                    "Black", "Dark Blue", "Dark Green", "Dark Aqua", "Dark Red", "Dark Purple",
                    "Gold", "Gray", "Dark Gray", "Blue", "Green", "Aqua", "Red", "Light Purple",
                    "Yellow", "White"
            },
            subcategory = COLOR
    )
    public static int skipColor = 13;

    @Dropdown(
            name = "Kill Color",
            options = {
                    "Black", "Dark Blue", "Dark Green", "Dark Aqua", "Dark Red", "Dark Purple",
                    "Gold", "Gray", "Dark Gray", "Blue", "Green", "Aqua", "Red", "Light Purple",
                    "Yellow", "White"
            },
            subcategory = COLOR
    )
    public static int killColor = 5;

    @Dropdown(
            name = "Overall Color",
            options = {
                    "Black", "Dark Blue", "Dark Green", "Dark Aqua", "Dark Red", "Dark Purple",
                    "Gold", "Gray", "Dark Gray", "Blue", "Green", "Aqua", "Red", "Light Purple",
                    "Yellow", "White"
            },
            subcategory = COLOR
    )
    public static int overallColor = 0;

    @Dropdown(
            name = "Pace Color",
            options = {
                    "Black", "Dark Blue", "Dark Green", "Dark Aqua", "Dark Red", "Dark Purple",
                    "Gold", "Gray", "Dark Gray", "Blue", "Green", "Aqua", "Red", "Light Purple",
                    "Yellow", "White"
            },
            subcategory = COLOR
    )
    public static int paceColor = 9;

    @Dropdown(
            name = "Splits Color",
            options = {
                    "Black", "Dark Blue", "Dark Green", "Dark Aqua", "Dark Red", "Dark Purple",
                    "Gold", "Gray", "Dark Gray", "Blue", "Green", "Aqua", "Red", "Light Purple",
                    "Yellow", "White"
            },
            subcategory = COLOR
    )
    public static int splitsColor = 4;

    @Dropdown(
            name = "Supply Times Color",
            options = {
                    "Black", "Dark Blue", "Dark Green", "Dark Aqua", "Dark Red", "Dark Purple",
                    "Gold", "Gray", "Dark Gray", "Blue", "Green", "Aqua", "Red", "Light Purple",
                    "Yellow", "White"
            },
            subcategory = COLOR
    )
    public static int supplyTimesColor = 4;

    @Dropdown(
            name = "Fresh Times Color",
            options = {
                    "Black", "Dark Blue", "Dark Green", "Dark Aqua", "Dark Red", "Dark Purple",
                    "Gold", "Gray", "Dark Gray", "Blue", "Green", "Aqua", "Red", "Light Purple",
                    "Yellow", "White"
            },
            subcategory = COLOR
    )
    public static int freshTimesColor = 4;

    @Dropdown(
            name = "Misc Color",
            options = {
                    "Black", "Dark Blue", "Dark Green", "Dark Aqua", "Dark Red", "Dark Purple",
                    "Gold", "Gray", "Dark Gray", "Blue", "Green", "Aqua", "Red", "Light Purple",
                    "Yellow", "White"
            },
            subcategory = COLOR
    )
    public static int miscColor = 4;

    @Info(
            text = "Changes to the Kuudra Splits won't be visible until refreshed. Click the button below to update the overlay!",
            type = InfoType.INFO,
            size = 2,
            subcategory = UPDATE
    )
    public static boolean ignored;

    @Button(
            name = "Refresh The Kuudra Splits Overlay",
            text = "Click to Apply Changes",
            size = 2,
            subcategory = UPDATE
    )
    Runnable runnable = KuudraSplits::updateSplitColors;

    public static String getColorForPhase(KuudraPhase phase) {
        switch (phase) {
            case SUPPLIES: return getColorCode(suppliesColor);
            case BUILD:    return getColorCode(buildColor);
            case EATEN:    return getColorCode(eatenColor);
            case STUN:     return getColorCode(stunColor);
            case DPS:      return getColorCode(dpsColor);
            case SKIP:     return getColorCode(skipColor);
            case KILL:     return getColorCode(killColor);
            case OVERALL:  return getColorCode(overallColor);
            default:       return "§f";
        }
    }
}
