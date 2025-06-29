package someoneok.kic.config.pages;

import cc.polyfrost.oneconfig.config.annotations.Button;
import cc.polyfrost.oneconfig.config.annotations.Dropdown;
import cc.polyfrost.oneconfig.config.annotations.Info;
import cc.polyfrost.oneconfig.config.annotations.Switch;
import cc.polyfrost.oneconfig.config.data.InfoType;
import someoneok.kic.modules.kuudra.KuudraProfitTracker;

public class KuudraProfitTrackerOptions {
    private transient static final String TOGGLE = "Toggles";
    private transient static final String COLOR = "Colors";
    private transient static final String VALUE = "Value Toggles";
    private transient static final String UPDATE = "Update";

    @Switch(
            name = "Show Current Session",
            subcategory = TOGGLE
    )
    public static boolean showCurrentSession = true;

    @Switch(
            name = "Show Profit",
            subcategory = TOGGLE
    )
    public static boolean showProfit = true;

    @Switch(
            name = "Show Runs",
            subcategory = TOGGLE
    )
    public static boolean showRuns = true;

    @Switch(
            name = "Show Chests",
            subcategory = TOGGLE
    )
    public static boolean showChests = true;

    @Switch(
            name = "Show Average Per Chest",
            subcategory = TOGGLE
    )
    public static boolean showAverageProfitPerChest = true;

    @Switch(
            name = "Show Rerolls",
            subcategory = TOGGLE
    )
    public static boolean showRerolls = true;

    @Switch(
            name = "Show Time",
            subcategory = TOGGLE
    )
    public static boolean showTime = true;

    @Switch(
            name = "Show Average Time Per Run",
            subcategory = TOGGLE
    )
    public static boolean showAverageTimePerRun = false;

    @Switch(
            name = "Show Rate",
            subcategory = TOGGLE
    )
    public static boolean showRate = true;

    @Switch(
            name = "Show Valuables",
            subcategory = TOGGLE
    )
    public static boolean showValuables = false;

    @Switch(
            name = "Show Essence",
            subcategory = TOGGLE
    )
    public static boolean showEssence = true;

    @Dropdown(
            name = "KIC Color",
            options = {
                    "Black", "Dark Blue", "Dark Green", "Dark Aqua", "Dark Red", "Dark Purple",
                    "Gold", "Gray", "Dark Gray", "Blue", "Green", "Aqua", "Red", "Light Purple",
                    "Yellow", "White"
            },
            subcategory = COLOR
    )
    public static int kicColor = 4;

    @Dropdown(
            name = "Profit Tracker Color",
            options = {
                    "Black", "Dark Blue", "Dark Green", "Dark Aqua", "Dark Red", "Dark Purple",
                    "Gold", "Gray", "Dark Gray", "Blue", "Green", "Aqua", "Red", "Light Purple",
                    "Yellow", "White"
            },
            subcategory = COLOR
    )
    public static int profitTrackerColor = 4;

    @Dropdown(
            name = "Session Color",
            options = {
                    "Black", "Dark Blue", "Dark Green", "Dark Aqua", "Dark Red", "Dark Purple",
                    "Gold", "Gray", "Dark Gray", "Blue", "Green", "Aqua", "Red", "Light Purple",
                    "Yellow", "White"
            },
            subcategory = COLOR
    )
    public static int sessionColor = 4;

    @Dropdown(
            name = "Profit Color",
            options = {
                    "Black", "Dark Blue", "Dark Green", "Dark Aqua", "Dark Red", "Dark Purple",
                    "Gold", "Gray", "Dark Gray", "Blue", "Green", "Aqua", "Red", "Light Purple",
                    "Yellow", "White"
            },
            subcategory = COLOR
    )
    public static int profitColor = 4;

    @Dropdown(
            name = "Runs Color",
            options = {
                    "Black", "Dark Blue", "Dark Green", "Dark Aqua", "Dark Red", "Dark Purple",
                    "Gold", "Gray", "Dark Gray", "Blue", "Green", "Aqua", "Red", "Light Purple",
                    "Yellow", "White"
            },
            subcategory = COLOR
    )
    public static int runsColor = 12;

    @Dropdown(
            name = "Chests Color",
            options = {
                    "Black", "Dark Blue", "Dark Green", "Dark Aqua", "Dark Red", "Dark Purple",
                    "Gold", "Gray", "Dark Gray", "Blue", "Green", "Aqua", "Red", "Light Purple",
                    "Yellow", "White"
            },
            subcategory = COLOR
    )
    public static int chestsColor = 6;

    @Dropdown(
            name = "Average Per Chest Color",
            options = {
                    "Black", "Dark Blue", "Dark Green", "Dark Aqua", "Dark Red", "Dark Purple",
                    "Gold", "Gray", "Dark Gray", "Blue", "Green", "Aqua", "Red", "Light Purple",
                    "Yellow", "White"
            },
            subcategory = COLOR
    )
    public static int averagePerChestColor = 14;

    @Dropdown(
            name = "Rerolls Color",
            options = {
                    "Black", "Dark Blue", "Dark Green", "Dark Aqua", "Dark Red", "Dark Purple",
                    "Gold", "Gray", "Dark Gray", "Blue", "Green", "Aqua", "Red", "Light Purple",
                    "Yellow", "White"
            },
            subcategory = COLOR
    )
    public static int rerollsColor = 15;

    @Dropdown(
            name = "Time Color",
            options = {
                    "Black", "Dark Blue", "Dark Green", "Dark Aqua", "Dark Red", "Dark Purple",
                    "Gold", "Gray", "Dark Gray", "Blue", "Green", "Aqua", "Red", "Light Purple",
                    "Yellow", "White"
            },
            subcategory = COLOR
    )
    public static int timeColor = 13;

    @Dropdown(
            name = "Average Time Per Run Color",
            options = {
                    "Black", "Dark Blue", "Dark Green", "Dark Aqua", "Dark Red", "Dark Purple",
                    "Gold", "Gray", "Dark Gray", "Blue", "Green", "Aqua", "Red", "Light Purple",
                    "Yellow", "White"
            },
            subcategory = COLOR
    )
    public static int averageTimePerRunColor = 0;

    @Dropdown(
            name = "Rate Color",
            options = {
                    "Black", "Dark Blue", "Dark Green", "Dark Aqua", "Dark Red", "Dark Purple",
                    "Gold", "Gray", "Dark Gray", "Blue", "Green", "Aqua", "Red", "Light Purple",
                    "Yellow", "White"
            },
            subcategory = COLOR
    )
    public static int rateColor = 5;

    @Dropdown(
            name = "Valuable Color",
            options = {
                    "Black", "Dark Blue", "Dark Green", "Dark Aqua", "Dark Red", "Dark Purple",
                    "Gold", "Gray", "Dark Gray", "Blue", "Green", "Aqua", "Red", "Light Purple",
                    "Yellow", "White"
            },
            subcategory = COLOR
    )
    public static int valuableColor = 6;

    @Dropdown(
            name = "Essence Color",
            options = {
                    "Black", "Dark Blue", "Dark Green", "Dark Aqua", "Dark Red", "Dark Purple",
                    "Gold", "Gray", "Dark Gray", "Blue", "Green", "Aqua", "Red", "Light Purple",
                    "Yellow", "White"
            },
            subcategory = COLOR
    )
    public static int essenceColor = 5;

    @Switch(
            name = "Show Valuable Value",
            subcategory = VALUE
    )
    public static boolean showValuablesValue = false;

    @Switch(
            name = "Show Essence Value",
            subcategory = VALUE
    )
    public static boolean showEssenceValue = true;

    @Switch(
            name = "Show Key Costs",
            subcategory = VALUE
    )
    public static boolean showKeyCosts = false;

    @Switch(
            name = "Show Reroll Costs",
            subcategory = VALUE
    )
    public static boolean showRerollCosts = true;

    @Info(
            text = "Changes to the Profit Tracker won't be visible until refreshed. Click the button below to update the overlay!",
            type = InfoType.INFO,
            size = 2,
            subcategory = UPDATE
    )
    public static boolean ignored;

    @Button(
            name = "Refresh The Profit Tracker Overlay",
            text = "Click to Apply Changes",
            size = 2,
            subcategory = UPDATE
    )
    Runnable runnable = KuudraProfitTracker::updateTracker;
}
