package someoneok.kic.modules.crimson.auction;

import cc.polyfrost.oneconfig.utils.JsonUtils;
import cc.polyfrost.oneconfig.utils.Multithreading;
import cc.polyfrost.oneconfig.utils.gui.GuiUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import someoneok.kic.KIC;
import someoneok.kic.config.KICConfig;
import someoneok.kic.models.APIException;
import someoneok.kic.models.Color;
import someoneok.kic.models.KICCustomGUI;
import someoneok.kic.models.kicauction.*;
import someoneok.kic.models.request.AuctionDataRequest;
import someoneok.kic.utils.ApiUtils;
import someoneok.kic.utils.NetworkUtils;
import someoneok.kic.utils.dev.KICLogger;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static someoneok.kic.KIC.KICPrefix;
import static someoneok.kic.utils.GeneralUtils.sendCommand;
import static someoneok.kic.utils.GeneralUtils.sendMessageToPlayer;
import static someoneok.kic.utils.ItemUtils.*;
import static someoneok.kic.utils.StringUtils.*;

public class KICAuction {
    private static final int INVENTORY_SIZE = KICCustomGUI.getCOLUMNS() * KICCustomGUI.getROWS();
    private static final Map<EquipmentType, String> EQUIPMENT_SKULL_TEXTURES = new HashMap<>();
    private static final Map<String, String> SKULL_TEXTURES = new HashMap<>();
    private static final Map<ArmorType, Map<ArmorCategory, Integer>> COLOR_MAP = new HashMap<>();
    private static final Map<FishingType, Map<ArmorCategory, Integer>> FISHING_COLOR_MAP = new HashMap<>();
    private static final List<Integer> SLOTS = Arrays.asList(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43);
    private static final Pattern BOUGHT_REGEX = Pattern.compile("You purchased (.+) for ([0-9,]+) coins!");

    private static final ItemStack KIC_ARMOR = createItem(new ItemStack(Items.diamond_chestplate), "§b§lArmor");
    private static final ItemStack KIC_EQUIPMENT = createItem(new ItemStack(Items.emerald), "§a§lEquipment");
    private static final ItemStack KIC_FISHING = createItem(new ItemStack(Items.fishing_rod), "§9§lFishing");
    private static final ItemStack KIC_SHARDS = createItem(new ItemStack(Items.prismarine_shard), "§3§lShards");

    private static final ItemStack PANE = createItemWithDamage(new ItemStack(Blocks.stained_glass_pane), 14);
    private static final ItemStack WHITE_PANE = createItemWithDamage(new ItemStack(Blocks.stained_glass_pane), 0);
    private static final ItemStack BACK = createItem(new ItemStack(Items.arrow), "§c§lBack");
    private static final ItemStack CLOSE = createItem(new ItemStack(Blocks.barrier), "§c§lClose");
    private static final ItemStack ITEM_BOUGHT = createItem(new ItemStack(Blocks.barrier), "§c§lBOUGHT!");
    private static final ItemStack ITEM_ERROR = createItem(new ItemStack(Blocks.command_block), "§c§lERROR!");
    private static final ItemStack ITEM_NOT_FOUND = createItem(new ItemStack(Blocks.command_block), "§c§lNOT FOUND!");

    public static final ItemStack PRICE_FILTER = createItem(new ItemStack(Items.gold_ingot), "§6§lPrice filter");
    public static final ItemStack CATEGORY_FILTER = createItem(new ItemStack(Items.nether_star), "§6§lCategory");
    public static final ItemStack CATEGORY_FILTER_2 = createItem(new ItemStack(Items.blaze_powder), "§6§lCategory");
    public static final ItemStack TYPE_FILTER = createItem(new ItemStack(Items.comparator), "§6§lType");
    public static final ItemStack CHEAPEST_TOGGLE = createItem(new ItemStack(Blocks.lever), "§6§lToggle cheapest");

    private static final Map<Long, List<String>> purchaseHistory = new HashMap<>();
    private static final Map<Long, List<String>> errorHistory = new HashMap<>();
    private static final Map<Long, List<String>> notFoundHistory = new HashMap<>();
    private static AuctionData auctionData;
    private static InventoryBasic inventory;
    private static KICCustomGUI auctionGUI;
    private static String[] currentItemData = null;

    private static KICAuctionPage activeKICPage = KICAuctionPage.MAIN;
    private static boolean filterCheapest = true;
    private static boolean armorCheapest = false;
    private static ArmorCategory activeArmorCategory = ArmorCategory.HELMETS;
    private static ArmorType activeArmorType = ArmorType.CRIMSON;
    private static EquipmentCategory activeEquipmentCategory = EquipmentCategory.NECKLACES;
    private static final Map<EquipmentCategory, EquipmentType> activeEquipmentType = new HashMap<>();
    private static FishingCategory activeFishingCategory = FishingCategory.RODS;
    private static ArmorCategory activeFishingArmorCategory = ArmorCategory.HELMETS;
    private static FishingType activeFishingArmorType = FishingType.MAGMA_LORD;

    static {
        EQUIPMENT_SKULL_TEXTURES.put(EquipmentType.MOLTEN_NECKLACE, "ewogICJ0aW1lc3RhbXAiIDogMTY0NzAxNjQxODA4MiwKICAicHJvZmlsZUlkIiA6ICI2NmI0ZDRlMTFlNmE0YjhjYTFkN2Q5YzliZTBhNjQ5OSIsCiAgInByb2ZpbGVOYW1lIiA6ICJBcmFzdG9vWXNmIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzZiYzBjYTQ0YzY3OGM0ZmI0ZWJlNzNmMTI3MTdmOGFiNmRmM2I1YWI4NjhkMjE3ZTY4YzM2YWFmZTJhZDgwZTQiCiAgICB9CiAgfQp9");
        EQUIPMENT_SKULL_TEXTURES.put(EquipmentType.LAVA_SHELL_NECKLACE, "ewogICJ0aW1lc3RhbXAiIDogMTY0ODE0NzY3NTk4NSwKICAicHJvZmlsZUlkIiA6ICJjNzQ1Mzc4MDY5MzY0ODg2ODkwNzRkOTQ3ZjBlOTlmNCIsCiAgInByb2ZpbGVOYW1lIiA6ICJjdWN1bWkwNyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS82MGQzYjFiZWM2ZTFjYzRiODhkZmY1NWU0Y2I3Yjc2ZjA2NGMyMGFmMjk3MjQ5MGQxNzEzNzJlZWY5MzcyNDQ3IiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0");
        EQUIPMENT_SKULL_TEXTURES.put(EquipmentType.DELIRIUM_NECKLACE, "ewogICJ0aW1lc3RhbXAiIDogMTY0MzU5OTM1NjU3OCwKICAicHJvZmlsZUlkIiA6ICI0NmY3N2NjNmQ2MjU0NjEzYjc2NmYyZDRmMDM2MzZhNiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNaXNzV29sZiIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9kNGNiZTg5M2UwZjgyMzAxYzVjZGNlMWI4MTgxODAzYjRmYjgzY2JmMzAzMDRkYjEzOWEzM2Q1NjlkNGVkOGMiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ");
        EQUIPMENT_SKULL_TEXTURES.put(EquipmentType.MAGMA_NECKLACE, "ewogICJ0aW1lc3RhbXAiIDogMTY0ODQ5NDE3MzMyMCwKICAicHJvZmlsZUlkIiA6ICIwNTVhOTk2NTk2M2E0YjRmOGMwMjRmMTJmNDFkMmNmMiIsCiAgInByb2ZpbGVOYW1lIiA6ICJUaGVWb3hlbGxlIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzMzZTIyMzdiZWY0ZjRmN2U5NWJiNmVhMjliNGZmNmY1ZGU4NmMwZDVmMmQ4Yzk5YzE2NGEwYTk4NDhlNzg3MDQiCiAgICB9CiAgfQp9");
        EQUIPMENT_SKULL_TEXTURES.put(EquipmentType.VANQUISHED_MAGMA_NECKLACE, "ewogICJ0aW1lc3RhbXAiIDogMTY0ODQ5NDM1MjU3OSwKICAicHJvZmlsZUlkIiA6ICJmMTkyZGU3MDUzMTQ0ODcxOTAwMjQ1MmIzZWE3MzA3NCIsCiAgInByb2ZpbGVOYW1lIiA6ICJOZVhvU2V0IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2NiZDg2N2IxOTZhZTViN2QwYTE4OGIwYzFmODMxZTA1MmMxZGI0NzhiYTlkYmE0YjFjODUwNjcwMGM0MTFmNWEiCiAgICB9CiAgfQp9");
        EQUIPMENT_SKULL_TEXTURES.put(EquipmentType.MOLTEN_CLOAK, "ewogICJ0aW1lc3RhbXAiIDogMTY0NzAxNjQzNDExNiwKICAicHJvZmlsZUlkIiA6ICJlZThjNWMzMGY3NWU0N2QxOTBmOTllNjI5NDgyOGZjMSIsCiAgInByb2ZpbGVOYW1lIiA6ICJTcGFya19QaGFudG9tIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzZjMThiYjBmNTJmMjQ2ZWM5YmZmNWYwNTc2MTQ1NmJlMjczZTczMGNkMjc2NTU1ZmQ3NzJiYThjM2ZhMGYxNDQiCiAgICB9CiAgfQp9");
        EQUIPMENT_SKULL_TEXTURES.put(EquipmentType.SCOURGE_CLOAK, "ewogICJ0aW1lc3RhbXAiIDogMTY0NDU4OTE4NTM1MSwKICAicHJvZmlsZUlkIiA6ICI4ODBiZWMwYTE0MmM0YzRlYTJlZjliMTFiMTBkNWNiNyIsCiAgInByb2ZpbGVOYW1lIiA6ICJmZ2FiIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzNjODg3NDMyMDliODgxZWE2ZjcxYTBlNWQ2ZDdiNGUyOTA0NTU0Mjk5ZDVlNzAxYmY5MTZiZTA5YTkzZWZlNjAiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ");
        EQUIPMENT_SKULL_TEXTURES.put(EquipmentType.GHAST_CLOAK, "ewogICJ0aW1lc3RhbXAiIDogMTY0ODQ5NTQwOTEyNiwKICAicHJvZmlsZUlkIiA6ICIzYTNmNzhkZmExZjQ0OTllYjE5NjlmYzlkOTEwZGYwYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJOb19jcmVyYXIiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzA4NGY2NDgyN2RiNTlhYTA3ZTg4ZTM2YzIyZjIyMmJmZmU2NmI5Zjg3YTFmYWFlMmVhZDE5NGFiMmI2ODRhMCIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9");
        EQUIPMENT_SKULL_TEXTURES.put(EquipmentType.VANQUISHED_GHAST_CLOAK, "ewogICJ0aW1lc3RhbXAiIDogMTY0ODQ5NTQ0Njc4OCwKICAicHJvZmlsZUlkIiA6ICJmZTYxY2RiMjUyMTA0ODYzYTljY2E2ODAwZDRiMzgzZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJNeVNoYWRvd3MiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTc3NDQ4OWM5NGIyMmJmMzE2OGFkZDEzOGM1NjMyOGU3OGRjZWM5MTM1NDIyYTYyMWE5NWQwZmY1YWRmMmY3MyIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9");
        EQUIPMENT_SKULL_TEXTURES.put(EquipmentType.MOLTEN_BELT, "ewogICJ0aW1lc3RhbXAiIDogMTY0NzAxNjQ4ODIwMSwKICAicHJvZmlsZUlkIiA6ICI5ZDQyNWFiOGFmZjg0MGU1OWM3NzUzZjc5Mjg5YjMyZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJUb21wa2luNDIiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTdmN2Y1NzIzNGI5OTg2ZTg1ZGE4NzhmYjNiNjY3MGYxYzcwYTM1NzBkZGRiZTlmYjUzMDkxMzA4MmJhNDRiNSIKICAgIH0KICB9Cn0");
        EQUIPMENT_SKULL_TEXTURES.put(EquipmentType.IMPLOSION_BELT, "ewogICJ0aW1lc3RhbXAiIDogMTY0MzYwMjI5OTA2MSwKICAicHJvZmlsZUlkIiA6ICI0ZTMwZjUwZTdiYWU0M2YzYWZkMmE3NDUyY2ViZTI5YyIsCiAgInByb2ZpbGVOYW1lIiA6ICJfdG9tYXRvel8iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjFkMmIwMzZkZDY2NGJiOTBjOWQ0NDNjMTk5OGZiNTI2Mzk4YWI0ZGRkZWI3OWI4NDAxYjE2YjlhNGQxMGJhMyIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9");
        EQUIPMENT_SKULL_TEXTURES.put(EquipmentType.SCOVILLE_BELT, "ewogICJ0aW1lc3RhbXAiIDogMTY0MzYwODAzMDY4MCwKICAicHJvZmlsZUlkIiA6ICI0ZjU2ZTg2ODk2OGU0ZWEwYmNjM2M2NzRlNzQ3ODdjOCIsCiAgInByb2ZpbGVOYW1lIiA6ICJDVUNGTDE1IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2Q0M2IyODRiNDQ0M2YwZmQ1OGI3OWUxZWEwOTliNzYyYWFlOTVkZjU0YTYzNmQ5NWM3NTlmOTEwMWQwMWRlZTIiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ");
        EQUIPMENT_SKULL_TEXTURES.put(EquipmentType.BLAZE_BELT, "ewogICJ0aW1lc3RhbXAiIDogMTY0ODQ5NTQ3Mzg4OCwKICAicHJvZmlsZUlkIiA6ICIzYjA1NTdlYmVmYjc0MDdmYmFmMjVhN2IyNzYwZTZlMCIsCiAgInByb2ZpbGVOYW1lIiA6ICJNcl9JY2VTcGlrZXMiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjA0ZTU4NTdiY2I0NTNjZTlkZThmYWQxYWQ4YzAwMjJhZDU2MjJlMjcyZTQxY2Q1YWM4OTBlMWZiOTg5N2RmYSIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9");
        EQUIPMENT_SKULL_TEXTURES.put(EquipmentType.VANQUISHED_BLAZE_BELT, "ewogICJ0aW1lc3RhbXAiIDogMTY0ODQ5NTU0ODQ5OSwKICAicHJvZmlsZUlkIiA6ICI5ZWEyMTQ0NGFiNjI0MWZkYjg5YjE2NDFhNDg2MGZiZiIsCiAgInByb2ZpbGVOYW1lIiA6ICI3QUJDSE9VTiIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS80YzIwYTg2YzFmNjhjN2I5OTlhZDA1MmM3MmQ4YTg2YzBiNmQwOGQyNzg0NDg3NmIwY2Q2NGQ0Y2EzZTQwNTA5IiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0");
        EQUIPMENT_SKULL_TEXTURES.put(EquipmentType.MOLTEN_BRACELET, "ewogICJ0aW1lc3RhbXAiIDogMTY0NzAxNjUwNTAzMSwKICAicHJvZmlsZUlkIiA6ICJiYzRlZGZiNWYzNmM0OGE3YWM5ZjFhMzlkYzIzZjRmOCIsCiAgInByb2ZpbGVOYW1lIiA6ICI4YWNhNjgwYjIyNDYxMzQwIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzIwNTI5NGY4OTE1YzhkYTliNmUzODI5ZjZkYmExMzVmODhhYjQ3NjFiOTQzMGUxOGZmMTI1ZWYzZTk1MGNjZGYiCiAgICB9CiAgfQp9");
        EQUIPMENT_SKULL_TEXTURES.put(EquipmentType.GAUNTLET_OF_CONTAGION, "ewogICJ0aW1lc3RhbXAiIDogMTY0NDUwMDQ1MTI0MCwKICAicHJvZmlsZUlkIiA6ICJjMDNlZTUxNjIzZTU0ZThhODc1NGM1NmVhZmJjZDA4ZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJsYXltYW51ZWwiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzdkNzViOGNmNzY1N2IwZGI3YzhmMTJjOGM3MzgzN2FhYTQxMDQ4ZTIwZTMzZmQwNTJlODU1YTQ3YmMwOTc3NiIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9");
        EQUIPMENT_SKULL_TEXTURES.put(EquipmentType.FLAMING_FIST, "ewogICJ0aW1lc3RhbXAiIDogMTY0NDUwMDUyMTI2NiwKICAicHJvZmlsZUlkIiA6ICI0ZDEzZWUyZjViOWI0N2I2OGU2NzhhMjAxN2VmZTc1MyIsCiAgInByb2ZpbGVOYW1lIiA6ICJCcmF5ZGVyZWsiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTg3MTk4NDljZWY0MjY0ZDY3MGM2Yjg4ZTZiNWViZDQwYjMyYjUzNWE2OGEyYzIyODkyZGQwOGQ4YmNjMTNjNCIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9");
        EQUIPMENT_SKULL_TEXTURES.put(EquipmentType.GLOWSTONE_GAUNTLET, "ewogICJ0aW1lc3RhbXAiIDogMTY0ODQ5NTM1Mjg0MCwKICAicHJvZmlsZUlkIiA6ICI1MTY4ZjZlMjIyM2E0Y2FjYjdiN2QyZjYyZWMxZGFhOSIsCiAgInByb2ZpbGVOYW1lIiA6ICJkZWZfbm90X2FzaCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8yNTE2OTE1MmEzNjI2YTE2NjliZmMzODhiZGU3NTIwYTNlYzVkZGYxMDY1ZmNlYTMzZTAzYjA2NjIzMjIwYjFhIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0");
        EQUIPMENT_SKULL_TEXTURES.put(EquipmentType.VANQUISHED_GLOWSTONE_GAUNTLET, "ewogICJ0aW1lc3RhbXAiIDogMTY0ODQ5NTM4Mzg4OCwKICAicHJvZmlsZUlkIiA6ICI0NDAzZGM1NDc1YmM0YjE1YTU0OGNmZGE2YjBlYjdkOSIsCiAgInByb2ZpbGVOYW1lIiA6ICJDaGFvc0NvbXB1dHJDbHViIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2U1ZmJjNDcwY2FmNGI0MzA5NGZkZWFkODIxZWMzOGQ1NzI3OGRjYTVmZjQ3NjM4YWYyN2JjZTVkNDIzYmJhOWMiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ");
        EQUIPMENT_SKULL_TEXTURES.put(EquipmentType.MAGMA_LORD_GAUNTLET, "ewogICJ0aW1lc3RhbXAiIDogMTY1MDM3OTExMjUxMywKICAicHJvZmlsZUlkIiA6ICJkOGNkMTNjZGRmNGU0Y2IzODJmYWZiYWIwOGIyNzQ4OSIsCiAgInByb2ZpbGVOYW1lIiA6ICJaYWNoeVphY2giLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTY4YTJiZmU2ZTU2ZGRlYTEwM2RlOGNlYzAwMGVmYTgzZWJiOWE5NTljNjhkODI0MDI1NDNjMDFkNDEzN2QyNiIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9");
        EQUIPMENT_SKULL_TEXTURES.put(EquipmentType.THUNDERBOLT_NECKLACE, "ewogICJ0aW1lc3RhbXAiIDogMTY0MTA5ODY5MjYyMywKICAicHJvZmlsZUlkIiA6ICIzNmMxODk4ZjlhZGE0NjZlYjk0ZDFmZWFmMjQ0MTkxMyIsCiAgInByb2ZpbGVOYW1lIiA6ICJMdW5haWFuIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzZjZWVkM2IwOGZhMzQ5ODJiYzNmYWFhOTkzNjk0MWI2Y2QwMGFiZDM3MTczZWU2N2IzY2UzYjllMThiZmNhNmMiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ");

        SKULL_TEXTURES.put("CRIMSON_HELMET", "ewogICJ0aW1lc3RhbXAiIDogMTY0NTUwNDQxNDE3MywKICAicHJvZmlsZUlkIiA6ICI5MThhMDI5NTU5ZGQ0Y2U2YjE2ZjdhNWQ1M2VmYjQxMiIsCiAgInByb2ZpbGVOYW1lIiA6ICJCZWV2ZWxvcGVyIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzUwNTFjODNkOWViZjY5MDEzZjFlYzhjOWVmYzk3OWVjMmQ5MjVhOTIxY2M4NzdmZjY0YWJlMDlhYWRkMmY2Y2MiCiAgICB9CiAgfQp9");
        SKULL_TEXTURES.put("HOT_CRIMSON_HELMET", "ewogICJ0aW1lc3RhbXAiIDogMTY0NTUwNDM5ODUzNiwKICAicHJvZmlsZUlkIiA6ICIwNjNhMTc2Y2RkMTU0ODRiYjU1MjRhNjQyMGM1YjdhNCIsCiAgInByb2ZpbGVOYW1lIiA6ICJkYXZpcGF0dXJ5IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2JiZTZkNjY3NzBhNjFiZjU2ZTZkNGI0NzY5MjJiMWMzYjNkYzlmNzhhMjZlNTZiMzZjZDk2NWI3YWIyMGI0MTciCiAgICB9CiAgfQp9");
        SKULL_TEXTURES.put("BURNING_CRIMSON_HELMET", "ewogICJ0aW1lc3RhbXAiIDogMTY0NTUwNDM4MTkxMiwKICAicHJvZmlsZUlkIiA6ICJmZDQ3Y2I4YjgzNjQ0YmY3YWIyYmUxODZkYjI1ZmMwZCIsCiAgInByb2ZpbGVOYW1lIiA6ICJDVUNGTDEyIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2ViMDM0YTVkOTdjMjRmZTBlYzkwMmJkMDJmZWM1MmEwOWQ5MTczNjZiZTA2MGM1MWY5YTFhMjc2YjI4NGE5ZDciCiAgICB9CiAgfQp9");
        SKULL_TEXTURES.put("FIERY_CRIMSON_HELMET", "ewogICJ0aW1lc3RhbXAiIDogMTY0NTUwNDM2NTM3OSwKICAicHJvZmlsZUlkIiA6ICI2OTBkMDM2OGM2NTE0OGM5ODZjMzEwN2FjMmRjNjFlYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJ5emZyXzciLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTFmMGM3YWZmZjE3ODI0NjVkOGNkYjVlYmEyNjFiNjU0MjNhN2EwNzEyZWUzYTRjNTcyYzMzZjk0YzY4YzU1IgogICAgfQogIH0KfQ==");
        SKULL_TEXTURES.put("INFERNAL_CRIMSON_HELMET", "ewogICJ0aW1lc3RhbXAiIDogMTY0NTUwNDM1MTEzMSwKICAicHJvZmlsZUlkIiA6ICI4ODBiZWMwYTE0MmM0YzRlYTJlZjliMTFiMTBkNWNiNyIsCiAgInByb2ZpbGVOYW1lIiA6ICJmZ2FiIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzUwN2Q2YmY3NjExMTkwZWQ5YzU4MGQ4Yzg3YzI5NjA1OTIxNjIzMGM5NTAxZWVjNjM1OWUwZDYwZWM4NDc1OGUiCiAgICB9CiAgfQp9");

        SKULL_TEXTURES.put("AURORA_HELMET", "ewogICJ0aW1lc3RhbXAiIDogMTY0NTUwMzc3ODA5MSwKICAicHJvZmlsZUlkIiA6ICJjNjc3MGJjZWMzZjE0ODA3ODc4MTU0NWRhMGFmMDI1NCIsCiAgInByb2ZpbGVOYW1lIiA6ICJDVUNGTDE2IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzc2M2QyZmU5Mzg4MWI0ZjI2Y2JlMWRkM2IwOWRhN2NjNDhkYmNkYzU2OGQxOTg1MmFkNjM1ZDVkMTY4NTk2MTEiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==");
        SKULL_TEXTURES.put("HOT_AURORA_HELMET", "ewogICJ0aW1lc3RhbXAiIDogMTY0NTUwNDUwMjg4NywKICAicHJvZmlsZUlkIiA6ICIzNmMxODk4ZjlhZGE0NjZlYjk0ZDFmZWFmMjQ0MTkxMyIsCiAgInByb2ZpbGVOYW1lIiA6ICJMdW5haWFuIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzMwYjZlMmQ0M2VlMjRjOWEyNDZhODM1MTVmOWI3NDE0ODQ2ZjMxNWFkOTU0NDAwYzM4Y2E2NWNkZjA4ZTkxOWUiCiAgICB9CiAgfQp9");
        SKULL_TEXTURES.put("BURNING_AURORA_HELMET", "ewogICJ0aW1lc3RhbXAiIDogMTY0NTUwNDQ4OTI0OSwKICAicHJvZmlsZUlkIiA6ICJiNzVjZDRmMThkZjg0MmNlYjJhY2MxNTU5MTNiMjA0YiIsCiAgInByb2ZpbGVOYW1lIiA6ICJLcmlzdGlqb25hczEzIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2Y4ODZiNDg0Yjc1MGMzNjcxMDc4OTI1ODY5MzBhNWU1MGNkNTdlOWJlODQzYmQzZGI3ZWIyMTdjYjc4MmYwYTEiCiAgICB9CiAgfQp9");
        SKULL_TEXTURES.put("FIERY_AURORA_HELMET", "ewogICJ0aW1lc3RhbXAiIDogMTY0NTUwNDQ2NzQzMCwKICAicHJvZmlsZUlkIiA6ICJmNWQwYjFhZTQxNmU0YTE5ODEyMTRmZGQzMWU3MzA1YiIsCiAgInByb2ZpbGVOYW1lIiA6ICJDYXRjaFRoZVdhdmUxMCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8xODU1YWFiYWZhNDYxZDM1Yjg2NDI5MWQ0OTRjYTM2YTJmNDYzMDdjZWFkYWQzYTI4Nzc0MDU0Zjg2Nzg4ZTgiCiAgICB9CiAgfQp9");
        SKULL_TEXTURES.put("INFERNAL_AURORA_HELMET", "ewogICJ0aW1lc3RhbXAiIDogMTY0NTUwNDQ1MDU0MCwKICAicHJvZmlsZUlkIiA6ICJjNTZlMjI0MmNiZWY0MWE2ODdlMzI2MGRjMGNmOTM2MSIsCiAgInByb2ZpbGVOYW1lIiA6ICJMSlI3MzEwMCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8yY2MzZWE1MzQ1YzI4OGMyNGYxYWE3ZGQ4ZmM2MzI5NTU5N2QzZmIzNzRlMzE0ZjYwNTY0MGI4ZGVhMDZmZTNmIgogICAgfQogIH0KfQ==");

        SKULL_TEXTURES.put("TERROR_HELMET", "ewogICJ0aW1lc3RhbXAiIDogMTY0NTUwNDE2MDk4NSwKICAicHJvZmlsZUlkIiA6ICI4ZGU4ZWU3MTMyMTY0NGNhYTllZjJlNTVjODRjNGU4ZCIsCiAgInByb2ZpbGVOYW1lIiA6ICJIdW5kZXNjaG9uVE0iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTJhZjg4MzM2OTdjODFiNDZlODNjOGYxODk1MjY2ZTYwNmVmYmIzYTU5ZjFjM2I0Y2EyODE2ZGEyYmNmYTlkNiIKICAgIH0KICB9Cn0=");
        SKULL_TEXTURES.put("HOT_TERROR_HELMET", "ewogICJ0aW1lc3RhbXAiIDogMTY0NTUwNDEyNzczOCwKICAicHJvZmlsZUlkIiA6ICJmYzUwMjkzYTVkMGI0NzViYWYwNDJhNzIwMWJhMzBkMiIsCiAgInByb2ZpbGVOYW1lIiA6ICJDVUNGTDE3IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2JkZWEzMGUzMDU0ODM3MTNhYzlhNTI5NWFlNjk4ZDQxMDk3NjZjOWFlMmJjNzQ0YWM1OGY2YmI0Y2Y5M2E5ZjEiCiAgICB9CiAgfQp9");
        SKULL_TEXTURES.put("BURNING_TERROR_HELMET", "ewogICJ0aW1lc3RhbXAiIDogMTY0NTUwNDExMzIwNywKICAicHJvZmlsZUlkIiA6ICIzNmMxODk4ZjlhZGE0NjZlYjk0ZDFmZWFmMjQ0MTkxMyIsCiAgInByb2ZpbGVOYW1lIiA6ICJMdW5haWFuIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzQ0ODJlYzM1MWYzNTM0NDU3NzViNDM5ZWJjOTA5ZDU3MDI3MzJmOGJhZTlkMzJiMGIwODg2MGIzZDY0MzkwNjEiCiAgICB9CiAgfQp9");
        SKULL_TEXTURES.put("FIERY_TERROR_HELMET", "ewogICJ0aW1lc3RhbXAiIDogMTY0NTUwNDEwMDgwOSwKICAicHJvZmlsZUlkIiA6ICIyMWUzNjdkNzI1Y2Y0ZTNiYjI2OTJjNGEzMDBhNGRlYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJHZXlzZXJNQyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9iNDY5MDJlMTc1NmEyNDJmNDAxZDBjMjU2N2VjNjQ4MWM2NTA4NGFmOWIxYWFhYmI5NzMyZjU2Y2FkZTU0MmYzIgogICAgfQogIH0KfQ==");
        SKULL_TEXTURES.put("INFERNAL_TERROR_HELMET", "ewogICJ0aW1lc3RhbXAiIDogMTY0NTUwNDA2Njk0OCwKICAicHJvZmlsZUlkIiA6ICJkYmQ4MDQ2M2EwMzY0Y2FjYjI3OGNhODBhMDBkZGIxMyIsCiAgInByb2ZpbGVOYW1lIiA6ICJ4bG9nMjEiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGVhZGYyOTU0ZjE2MjlkNzkzZDdmNDY3MTgxYzQ0OWUxOGQ4OWFhNDk0MWJlYzNlYTUyMTFlNTkwOWJiNTY3IgogICAgfQogIH0KfQ==");

        SKULL_TEXTURES.put("HOLLOW_HELMET", "ewogICJ0aW1lc3RhbXAiIDogMTY0NTUwNDE3MjcwNSwKICAicHJvZmlsZUlkIiA6ICI4YjgyM2E1YmU0Njk0YjhiOTE0NmE5MWRhMjk4ZTViNSIsCiAgInByb2ZpbGVOYW1lIiA6ICJTZXBoaXRpcyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS84NGVlZTlmMjM5NTJmOTc3N2QwNTE5YzExMTY1NTA2Njg1MWZiNGMxNzkzNjI5ZDM3NjdhMDBkYWNlZTZmMjUwIgogICAgfQogIH0KfQ==");
        SKULL_TEXTURES.put("HOT_HOLLOW_HELMET", "ewogICJ0aW1lc3RhbXAiIDogMTY0NTUwNDIyMjk5MSwKICAicHJvZmlsZUlkIiA6ICIwZWQ2MDFlMDhjZTM0YjRkYWUxZmI4MDljZmEwNTM5NiIsCiAgInByb2ZpbGVOYW1lIiA6ICJOZWVkTW9yZUFjY291bnRzIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2NjMzQ0ZDY1YWU2YmMyMjMwZWUyMjBkNzZkZGExZTA4MGY5MDI4MmNhMGY5ZjBkOWJjOTY2N2QyMjA0OGNlOWYiCiAgICB9CiAgfQp9");
        SKULL_TEXTURES.put("BURNING_HOLLOW_HELMET", "ewogICJ0aW1lc3RhbXAiIDogMTY0NTUwNDIwNjg0MCwKICAicHJvZmlsZUlkIiA6ICIzYTNmNzhkZmExZjQ0OTllYjE5NjlmYzlkOTEwZGYwYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJOb19jcmVyYXIiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTJhYTE3YzMyZDI3YjkyODE2YjAwMzYyYzJmNjlkODY3Y2Q5OWJhMzc2ZDEwYjA5Nzc5NjM2ODI4NzgzNGVhMSIKICAgIH0KICB9Cn0=");
        SKULL_TEXTURES.put("FIERY_HOLLOW_HELMET", "ewogICJ0aW1lc3RhbXAiIDogMTY0NTUwNDE4OTk2NywKICAicHJvZmlsZUlkIiA6ICIyNzZlMDQ2YjI0MDM0M2VkOTk2NmU0OTRlN2U2Y2IzNCIsCiAgInByb2ZpbGVOYW1lIiA6ICJBRFJBTlM3MTAiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjhjNmJkYmZmNmYwMzNjNjFiYzMzNWRkOWE3ODk1NmU2Y2RmZGQ2MTNlYmI1YTc3ZWMzOGQyNzFhODMzMjYxMiIKICAgIH0KICB9Cn0=");
        SKULL_TEXTURES.put("INFERNAL_HOLLOW_HELMET", "ewogICJ0aW1lc3RhbXAiIDogMTY0NTUwNDE3MjcwNSwKICAicHJvZmlsZUlkIiA6ICI4YjgyM2E1YmU0Njk0YjhiOTE0NmE5MWRhMjk4ZTViNSIsCiAgInByb2ZpbGVOYW1lIiA6ICJTZXBoaXRpcyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS84NGVlZTlmMjM5NTJmOTc3N2QwNTE5YzExMTY1NTA2Njg1MWZiNGMxNzkzNjI5ZDM3NjdhMDBkYWNlZTZmMjUwIgogICAgfQogIH0KfQ==");

        SKULL_TEXTURES.put("FERVOR_HELMET", "ewogICJ0aW1lc3RhbXAiIDogMTY0NTUwNDMxOTYxMiwKICAicHJvZmlsZUlkIiA6ICIwNDNkZWIzOGIyNjE0MTg1YTIzYzU4ZmI2YTc5ZWZkYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJWaXRhbFNpZ256MiIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9iYzZlMTM3ZTJhNmE1MDFkZmM4NTRjYTkwMGI1YjY4MTIyN2RlYTRkNWE5NjE2ODQ5YTNhNjhmMDljNmRjMzI3IgogICAgfQogIH0KfQ==");
        SKULL_TEXTURES.put("HOT_FERVOR_HELMET", "ewogICJ0aW1lc3RhbXAiIDogMTY0NTUwNDMwMzExNSwKICAicHJvZmlsZUlkIiA6ICI0OTVlYTMyM2E0N2U0ODk0OWRjZmJmNDQxZmFhODM4YSIsCiAgInByb2ZpbGVOYW1lIiA6ICJDb29sZVZlbnQxNjgiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzAzMGMzMjE3YTJhYzA3MGU0MDRjZjhjMjFlYWZiYTNhMGY2YjU3OWRiZjRlYzlmZGMxNjMzYWQyMTgwY2YyOCIKICAgIH0KICB9Cn0=");
        SKULL_TEXTURES.put("BURNING_FERVOR_HELMET", "ewogICJ0aW1lc3RhbXAiIDogMTY0NTUwNDI4OTE5MSwKICAicHJvZmlsZUlkIiA6ICJhYTZhNDA5NjU4YTk0MDIwYmU3OGQwN2JkMzVlNTg5MyIsCiAgInByb2ZpbGVOYW1lIiA6ICJiejE0IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzk3ZTJkMTc3ZmFhYzg2N2M5ZTQ5NTQyYWE0ZWM4MzFiNDU4ZDg1MzdmMjA2NzIwMTRjOTE0OWU0ZGRiYTQzZDYiCiAgICB9CiAgfQp9");
        SKULL_TEXTURES.put("FIERY_FERVOR_HELMET", "ewogICJ0aW1lc3RhbXAiIDogMTY0NTUwNDI3NDQ3MCwKICAicHJvZmlsZUlkIiA6ICIxYTc1ZTNiYmI1NTk0MTc2OTVjMmY4NTY1YzNlMDAzZCIsCiAgInByb2ZpbGVOYW1lIiA6ICJUZXJvZmFyIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzczZjY1ZmZmZThmYjRlNzBlZTFjMWVkYjhkOGY1ZWY3YmI2NmQwZmQxODA3ZmIyNTY4YjA5ZjFhYjk3NmQ2NGYiCiAgICB9CiAgfQp9");
        SKULL_TEXTURES.put("INFERNAL_FERVOR_HELMET", "ewogICJ0aW1lc3RhbXAiIDogMTY0NTUwNDI1ODUyOCwKICAicHJvZmlsZUlkIiA6ICI0NWY3YTJlNjE3ODE0YjJjODAwODM5MmRmN2IzNWY0ZiIsCiAgInByb2ZpbGVOYW1lIiA6ICJfSnVzdERvSXQiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWQ0N2NmNmUxMmQxMmE1ZWIxNjE3NTliYTk1Njg5YTkyMzdlYzExMTQ3M2U5MmExNDBlY2FiMDE1OGRmZDI1OCIKICAgIH0KICB9Cn0=");

        SKULL_TEXTURES.put("MAGMA_LORD_HELMET", "ewogICJ0aW1lc3RhbXAiIDogMTY0MzYwNTU1MjE0NywKICAicHJvZmlsZUlkIiA6ICJjNTZlMjI0MmNiZWY0MWE2ODdlMzI2MGRjMGNmOTM2MSIsCiAgInByb2ZpbGVOYW1lIiA6ICJMSlI3MzEwMCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS83N2Q5MWJhYzQ3NTVkYmM5NjhhZGQ3NWU2ZWMzOTc5YTc0ZGEzMGJjYjRkMjU1NmUzNWE0NjM1NzE4YWU3MzkwIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=");
        SKULL_TEXTURES.put("THUNDER_HELMET", "ewogICJ0aW1lc3RhbXAiIDogMTY0MTA5ODc5NDUzNSwKICAicHJvZmlsZUlkIiA6ICI5ZDIyZGRhOTVmZGI0MjFmOGZhNjAzNTI1YThkZmE4ZCIsCiAgInByb2ZpbGVOYW1lIiA6ICJTYWZlRHJpZnQ0OCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS80YTkzZDJiZjk5OWE3ZmUwNTJiZjY3OTVlMGEwYmM3NTU1Y2MxZTAxNGM2NjdmYTg2ZGM0MjFjYjE4NmQ1YjY3IiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=");

        // CRIMSON
        Map<ArmorCategory, Integer> crimsonColors = new HashMap<>();
        crimsonColors.put(ArmorCategory.CHESTPLATES, 0xFF6F0C);
        crimsonColors.put(ArmorCategory.LEGGINGS, 0xE66105);
        crimsonColors.put(ArmorCategory.BOOTS, 0xE65300);
        COLOR_MAP.put(ArmorType.CRIMSON, crimsonColors);

        // AURORA
        Map<ArmorCategory, Integer> auroraColors = new HashMap<>();
        auroraColors.put(ArmorCategory.CHESTPLATES, 0x2841F1);
        auroraColors.put(ArmorCategory.LEGGINGS, 0x3F56FB);
        auroraColors.put(ArmorCategory.BOOTS, 0x6184FC);
        COLOR_MAP.put(ArmorType.AURORA, auroraColors);

        // TERROR
        Map<ArmorCategory, Integer> terrorColors = new HashMap<>();
        terrorColors.put(ArmorCategory.CHESTPLATES, 0x3E05AF);
        terrorColors.put(ArmorCategory.LEGGINGS, 0x5D23D1);
        terrorColors.put(ArmorCategory.BOOTS, 0x7C44EC);
        COLOR_MAP.put(ArmorType.TERROR, terrorColors);

        // FERVOR
        Map<ArmorCategory, Integer> fervorColors = new HashMap<>();
        fervorColors.put(ArmorCategory.CHESTPLATES, 0xF04729);
        fervorColors.put(ArmorCategory.LEGGINGS, 0x17BF89);
        fervorColors.put(ArmorCategory.BOOTS, 0x07A674);
        COLOR_MAP.put(ArmorType.FERVOR, fervorColors);

        // HOLLOW
        Map<ArmorCategory, Integer> hollowColors = new HashMap<>();
        hollowColors.put(ArmorCategory.CHESTPLATES, 0xFFCB0D);
        hollowColors.put(ArmorCategory.LEGGINGS, 0xFFF6A3);
        hollowColors.put(ArmorCategory.BOOTS, 0xE3FFFA);
        COLOR_MAP.put(ArmorType.HOLLOW, hollowColors);

        // MAGMA_LORD (same color for all pieces)
        Map<ArmorCategory, Integer> magmaLordColors = new HashMap<>();
        magmaLordColors.put(ArmorCategory.CHESTPLATES, 0x6F0F08);
        magmaLordColors.put(ArmorCategory.LEGGINGS, 0x6F0F08);
        magmaLordColors.put(ArmorCategory.BOOTS, 0x6F0F08);
        FISHING_COLOR_MAP.put(FishingType.MAGMA_LORD, magmaLordColors);

        // THUNDER (same color for all pieces)
        Map<ArmorCategory, Integer> thunderColors = new HashMap<>();
        thunderColors.put(ArmorCategory.CHESTPLATES, 0x24DDE5);
        thunderColors.put(ArmorCategory.LEGGINGS, 0x24DDE5);
        thunderColors.put(ArmorCategory.BOOTS, 0x24DDE5);
        FISHING_COLOR_MAP.put(FishingType.THUNDER, thunderColors);

        activeEquipmentType.put(EquipmentCategory.NECKLACES, EquipmentType.MOLTEN_NECKLACE);
        activeEquipmentType.put(EquipmentCategory.CLOAKS, EquipmentType.MOLTEN_CLOAK);
        activeEquipmentType.put(EquipmentCategory.BELTS, EquipmentType.MOLTEN_BELT);
        activeEquipmentType.put(EquipmentCategory.BRACELETS, EquipmentType.MOLTEN_BRACELET);
    }

    public static void open() {
        if (!ApiUtils.isVerified()) {
            sendMessageToPlayer(KICPrefix + " §cMod disabled: not verified.");
            return;
        }

        if (auctionData == null) return;
        currentItemData = null;
        inventory = new InventoryBasic(Color.getColorCode(KICConfig.KICAuctionColor) + "§lKIC Auction", true, INVENTORY_SIZE);

        switch (activeKICPage) {
            case ARMOR: setupArmorInv(); break;
            case EQUIPMENT: setupEquipmentInv(); break;
            case FISHING: setupFishingInv(); break;
            case SHARDS: setupShardInv(); break;
            default: setupMainInv(); break;
        }

        auctionGUI = new KICCustomGUI(inventory, () -> KICConfig.KICAuctionStyle, KICAuction::onSlotClicked);

        GuiUtils.displayScreen(auctionGUI);
    }

    public static void open(AuctionDataRequest auctionDataRequest) {
        if (!ApiUtils.isVerified()) {
            sendMessageToPlayer(KICPrefix + " §cMod disabled: not verified.");
            return;
        }

        auctionData = null;
        currentItemData = null;
        updatePaneColor(PANE, KICConfig.KICAuctionColor);
        Multithreading.runAsync(() -> {
            String requestBody = KIC.GSON.toJson(auctionDataRequest);
            JsonObject response;
            try {
                response = JsonUtils.parseString(NetworkUtils.sendPostRequest("https://api.sm0kez.com/crimson/attribute/prices?limit=28&extra=true", true, requestBody)).getAsJsonObject();
            } catch (APIException e) {
                sendMessageToPlayer(String.format("%s §c%s", KICPrefix, e.getMessage()));
                return;
            }
            if (response == null) return;
            try {
                String attribute1 = (response.has("attribute1") && !response.get("attribute1").isJsonNull()) ? response.get("attribute1").getAsString() : null;
                int attributeLvl1 = (response.has("attributeLvl1") && !response.get("attributeLvl1").isJsonNull()) ? response.get("attributeLvl1").getAsInt() : 0;
                String attribute2 = (response.has("attribute2") && !response.get("attribute2").isJsonNull()) ? response.get("attribute2").getAsString() : null;
                int attributeLvl2 = (response.has("attributeLvl2") && !response.get("attributeLvl2").isJsonNull()) ? response.get("attributeLvl2").getAsInt() : 0;
                long timestamp = (response.has("timestamp") && !response.get("timestamp").isJsonNull()) ? response.get("timestamp").getAsLong() : 0;

                JsonObject armor = (response.has("armor") && !response.get("armor").isJsonNull()) ? response.get("armor").getAsJsonObject() : null;
                JsonObject equipment = (response.has("equipment") && !response.get("equipment").isJsonNull()) ? response.get("equipment").getAsJsonObject() : null;
                JsonArray shards = (response.has("shards") && !response.get("shards").isJsonNull()) ? response.get("shards").getAsJsonArray() : null;
                JsonArray rods = (response.has("rods") && !response.get("rods").isJsonNull()) ? response.get("rods").getAsJsonArray() : null;

                auctionData = new AuctionData(attribute1, attributeLvl1, attribute2, attributeLvl2, timestamp, armor, equipment, shards, rods);
                open();
            } catch (Exception e) {
                KICLogger.error("Error parsing auction data: " + e.getMessage());
            }
        });
    }

    public static boolean validAuctionData() {
        return auctionData != null && (System.currentTimeMillis() - auctionData.getTimestamp() < 900000);
    }

    private static void setupMainInv() {
        activeKICPage = KICAuctionPage.MAIN;
        inventory.setCustomName(Color.getColorCode(KICConfig.KICAuctionColor) + "§lKIC Auction");
        ItemStack[] items = new ItemStack[INVENTORY_SIZE];
        Arrays.fill(items, PANE);

        items[8] = makeInfoItem();
        items[19] = KIC_ARMOR;
        items[21] = KIC_EQUIPMENT;
        items[23] = KIC_FISHING;
        items[25] = KIC_SHARDS;
        items[49] = CLOSE;

        for (int i = 0; i < INVENTORY_SIZE; i++) {
            inventory.setInventorySlotContents(i, items[i]);
        }
    }

    private static void setupArmorInv() {
        activeKICPage = KICAuctionPage.ARMOR;
        inventory.setCustomName(Color.getColorCode(KICConfig.KICAuctionColor) + "§lKIC Auction - Armor");

        ItemStack[] items = new ItemStack[INVENTORY_SIZE];
        Arrays.fill(items, PANE);
        items[8] = makeInfoItem();
        items[49] = BACK;

        List<String> cheapestToggleLore = createLore(armorCheapest ? "ON" : "OFF", Arrays.asList("ON", "OFF"));
        setItemLore(CHEAPEST_TOGGLE, cheapestToggleLore);
        items[51] = CHEAPEST_TOGGLE;

        List<String> categoryLore = createLore(
                activeArmorCategory.getDisplayText(),
                Arrays.stream(ArmorCategory.values()).map(ArmorCategory::getDisplayText).collect(Collectors.toList())
        );
        setItemLore(CATEGORY_FILTER, categoryLore);
        items[3] = CATEGORY_FILTER;

        List<KICAuctionItem> armor = getSortedArmorItems(items);

        processArmorItems(armor, items);

        for (int i = 0; i < INVENTORY_SIZE; i++) {
            inventory.setInventorySlotContents(i, items[i]);
        }
    }

    private static List<KICAuctionItem> getSortedArmorItems(ItemStack[] items) {
        Map<String, List<KICAuctionItem>> armorMap = auctionData.getArmor(activeArmorCategory);

        if (armorCheapest) {
            return armorMap.entrySet().stream()
                    .filter(entry -> Arrays.stream(ArmorType.values())
                            .anyMatch(type -> entry.getKey().toLowerCase().contains(type.getDisplayText().toLowerCase())))
                    .flatMap(entry -> entry.getValue().stream())
                    .sorted(Comparator.comparingLong(KICAuctionItem::getPrice))
                    .limit(28)
                    .collect(Collectors.toList());
        } else {
            List<String> typeLore = createLore(
                    activeArmorType.getDisplayText(),
                    Arrays.stream(ArmorType.values()).map(ArmorType::getDisplayText).collect(Collectors.toList())
            );
            setItemLore(TYPE_FILTER, typeLore);
            items[5] = TYPE_FILTER;

            List<String> priceFilterLore = createLore(
                    filterCheapest ? "Cheapest to expensive" : "Expensive to cheapest",
                    Arrays.asList("Cheapest to expensive", "Expensive to cheapest")
            );
            setItemLore(PRICE_FILTER, priceFilterLore);
            items[47] = PRICE_FILTER;

            return armorMap.entrySet().stream()
                    .filter(entry -> entry.getKey().toLowerCase().contains(activeArmorType.getDisplayText().toLowerCase()))
                    .flatMap(entry -> entry.getValue().stream())
                    .sorted(filterCheapest
                            ? Comparator.comparingLong(KICAuctionItem::getPrice)
                            : Comparator.comparingLong(KICAuctionItem::getPrice).reversed())
                    .collect(Collectors.toList());
        }
    }

    private static void processArmorItems(List<KICAuctionItem> armor, ItemStack[] inventorySlots) {
        for (int index = 0; index < SLOTS.size(); index++) {
            int slot = SLOTS.get(index);

            if (index < armor.size()) {
                KICAuctionItem item = armor.get(index);
                List<String> lore = createCustomItemLore(item, false);
                String name = createName(item);

                inventorySlots[slot] = isItemBought(item.getUuid())
                        ? ITEM_BOUGHT
                        : isItemErrored(item.getUuid())
                        ? ITEM_ERROR
                        : isItemNotFound(item.getUuid())
                        ? ITEM_NOT_FOUND
                        : (activeArmorCategory == ArmorCategory.HELMETS
                        ? skullItem(SKULL_TEXTURES.get(item.getItemId()), name, lore, item)
                        : applyArmorColor(createCustomItem(item, name, lore, new ItemStack(activeArmorCategory.getItemArmor())), item));
            } else {
                inventorySlots[slot] = WHITE_PANE;
            }
        }
    }

    private static boolean isItemNotFound(String uuid) {
        return notFoundHistory.containsKey(auctionData.getTimestamp()) && notFoundHistory.get(auctionData.getTimestamp()).contains(uuid);
    }

    private static boolean isItemBought(String uuid) {
        return purchaseHistory.containsKey(auctionData.getTimestamp()) && purchaseHistory.get(auctionData.getTimestamp()).contains(uuid);
    }

    private static boolean isItemErrored(String uuid) {
        return errorHistory.containsKey(auctionData.getTimestamp()) && errorHistory.get(auctionData.getTimestamp()).contains(uuid);
    }

    private static ItemStack applyArmorColor(ItemStack stack, KICAuctionItem item) {
        if (stack.getItem() instanceof ItemArmor && ((ItemArmor) stack.getItem()).getArmorMaterial() == ItemArmor.ArmorMaterial.LEATHER) {
            int color = determineColor(item.getItemId(), activeArmorCategory);
            NBTTagCompound tag = stack.getTagCompound();
            if (tag == null) tag = new NBTTagCompound();
            NBTTagCompound display = tag.getCompoundTag("display");
            display.setInteger("color", color);
            tag.setTag("display", display);
            stack.setTagCompound(tag);
        }
        return stack;
    }

    private static void setupEquipmentInv() {
        activeKICPage = KICAuctionPage.EQUIPMENT;
        inventory.setCustomName(Color.getColorCode(KICConfig.KICAuctionColor) + "§lKIC Auction - Equipment");

        ItemStack[] items = new ItemStack[INVENTORY_SIZE];
        Arrays.fill(items, PANE);
        items[8] = makeInfoItem();

        List<String> priceFilterLore = createLore(
                filterCheapest ? "Cheapest to expensive" : "Expensive to cheapest",
                Arrays.asList("Cheapest to expensive", "Expensive to cheapest")
        );
        setItemLore(PRICE_FILTER, priceFilterLore);
        items[47] = PRICE_FILTER;
        items[49] = BACK;

        List<String> categoryLore = createLore(
                activeEquipmentCategory.getDisplayText(),
                Arrays.stream(EquipmentCategory.values()).map(EquipmentCategory::getDisplayText).collect(Collectors.toList())
        );
        setItemLore(CATEGORY_FILTER, categoryLore);
        items[3] = CATEGORY_FILTER;

        EquipmentType activeType = activeEquipmentType.get(activeEquipmentCategory);
        List<String> typeLore = createLore(
                activeType.getDisplayText(),
                EquipmentType.getTypes(activeEquipmentCategory).stream().map(EquipmentType::getDisplayText).collect(Collectors.toList())
        );
        setItemLore(TYPE_FILTER, typeLore);
        items[5] = TYPE_FILTER;

        List<KICAuctionItem> equipment = auctionData.getEquipment(activeEquipmentCategory, activeType);
        equipment.sort(filterCheapest
                ? Comparator.comparingLong(KICAuctionItem::getPrice)
                : Comparator.comparingLong(KICAuctionItem::getPrice).reversed());

        processEquipmentItems(equipment, items, activeType);

        for (int i = 0; i < INVENTORY_SIZE; i++) {
            inventory.setInventorySlotContents(i, items[i]);
        }
    }

    private static void processEquipmentItems(List<KICAuctionItem> equipment, ItemStack[] inventorySlots, EquipmentType activeType) {
        for (int index = 0; index < SLOTS.size(); index++) {
            int slot = SLOTS.get(index);

            if (index < equipment.size()) {
                KICAuctionItem item = equipment.get(index);
                List<String> lore = createCustomItemLore(item, false);
                String name = createName(item);

                inventorySlots[slot] = isItemBought(item.getUuid())
                        ? ITEM_BOUGHT
                        : isItemErrored(item.getUuid())
                        ? ITEM_ERROR
                        : isItemNotFound(item.getUuid())
                        ? ITEM_NOT_FOUND
                        : skullItem(EQUIPMENT_SKULL_TEXTURES.get(activeType), name, lore, item);
            } else {
                inventorySlots[slot] = WHITE_PANE;
            }
        }
    }

    private static void setupFishingInv() {
        activeKICPage = KICAuctionPage.FISHING;
        inventory.setCustomName(Color.getColorCode(KICConfig.KICAuctionColor) + "§lKIC Auction - Fishing");

        ItemStack[] items = new ItemStack[INVENTORY_SIZE];
        Arrays.fill(items, PANE);
        items[8] = makeInfoItem();

        List<String> priceFilterLore = createLore(
                filterCheapest ? "Cheapest to expensive" : "Expensive to cheapest",
                Arrays.asList("Cheapest to expensive", "Expensive to cheapest")
        );
        setItemLore(PRICE_FILTER, priceFilterLore);
        items[47] = PRICE_FILTER;
        items[49] = BACK;

        List<String> categoryLore = createLore(
                activeFishingCategory.getDisplayText(),
                Arrays.stream(FishingCategory.values()).map(FishingCategory::getDisplayText).collect(Collectors.toList())
        );
        setItemLore(CATEGORY_FILTER, categoryLore);
        items[3] = CATEGORY_FILTER;

        if (activeFishingCategory == FishingCategory.RODS) {
            List<KICAuctionItem> rods = auctionData.getRods();
            rods.sort(filterCheapest
                    ? Comparator.comparingLong(KICAuctionItem::getPrice)
                    : Comparator.comparingLong(KICAuctionItem::getPrice).reversed());

            processAuctionItems(rods, items, new ItemStack(Items.fishing_rod));
        } else {
            List<String> typeLore = createLore(
                    activeFishingArmorCategory.getDisplayText(),
                    Arrays.stream(ArmorCategory.values()).map(ArmorCategory::getDisplayText).collect(Collectors.toList())
            );
            setItemLore(CATEGORY_FILTER_2, typeLore);
            items[4] = CATEGORY_FILTER_2;

            List<String> fishingTypeLore = createLore(
                    activeFishingArmorType.getDisplayText(),
                    Arrays.stream(FishingType.values()).map(FishingType::getDisplayText).collect(Collectors.toList())
            );
            setItemLore(TYPE_FILTER, fishingTypeLore);
            items[5] = TYPE_FILTER;

            List<KICAuctionItem> armor = auctionData.getArmor(activeFishingArmorCategory)
                    .entrySet().stream()
                    .filter(entry -> entry.getKey().toLowerCase().contains(activeFishingArmorType.name().toLowerCase()))
                    .flatMap(entry -> entry.getValue().stream())
                    .sorted(filterCheapest
                            ? Comparator.comparingLong(KICAuctionItem::getPrice)
                            : Comparator.comparingLong(KICAuctionItem::getPrice).reversed())
                    .collect(Collectors.toList());

            processFishingArmorItems(armor, items);
        }

        for (int i = 0; i < INVENTORY_SIZE; i++) {
            inventory.setInventorySlotContents(i, items[i]);
        }
    }

    private static void processAuctionItems(List<KICAuctionItem> itemsList, ItemStack[] inventorySlots, ItemStack baseItem) {
        for (int index = 0; index < SLOTS.size(); index++) {
            int slot = SLOTS.get(index);

            if (index < itemsList.size()) {
                KICAuctionItem item = itemsList.get(index);
                inventorySlots[slot] = isItemBought(item.getUuid())
                        ? ITEM_BOUGHT
                        : isItemErrored(item.getUuid())
                        ? ITEM_ERROR
                        : isItemNotFound(item.getUuid())
                        ? ITEM_NOT_FOUND
                        : createCustomItem(item, createName(item), createCustomItemLore(item, false), baseItem);
            } else {
                inventorySlots[slot] = WHITE_PANE;
            }
        }
    }

    private static void processFishingArmorItems(List<KICAuctionItem> armor, ItemStack[] inventorySlots) {
        for (int index = 0; index < SLOTS.size(); index++) {
            int slot = SLOTS.get(index);

            if (index < armor.size()) {
                KICAuctionItem item = armor.get(index);
                List<String> lore = createCustomItemLore(item, false);
                String name = createName(item);

                inventorySlots[slot] = (activeFishingArmorCategory == ArmorCategory.HELMETS)
                        ? skullItem(SKULL_TEXTURES.get(item.getItemId()), name, lore, item)
                        : applyFishingArmorColor(createCustomItem(item, name, lore, new ItemStack(activeFishingArmorCategory.getItemArmor())), item);
            } else {
                inventorySlots[slot] = WHITE_PANE;
            }
        }
    }

    private static ItemStack applyFishingArmorColor(ItemStack stack, KICAuctionItem item) {
        if (stack.getItem() instanceof ItemArmor && ((ItemArmor) stack.getItem()).getArmorMaterial() == ItemArmor.ArmorMaterial.LEATHER) {
            int color = determineColorFishing(item.getItemId(), activeFishingArmorCategory);
            NBTTagCompound tag = stack.getTagCompound();
            if (tag == null) tag = new NBTTagCompound();
            NBTTagCompound display = tag.getCompoundTag("display");
            display.setInteger("color", color);
            tag.setTag("display", display);
            stack.setTagCompound(tag);
        }
        return stack;
    }

    private static void setupShardInv() {
        activeKICPage = KICAuctionPage.SHARDS;
        inventory.setCustomName(Color.getColorCode(KICConfig.KICAuctionColor) + "§lKIC Auction - Shards");

        ItemStack[] items = new ItemStack[INVENTORY_SIZE];
        Arrays.fill(items, PANE);
        items[8] = makeInfoItem();

        List<String> priceFilterLore = createLore(filterCheapest ? "Cheapest to expensive" : "Expensive to cheapest",
                Arrays.asList("Cheapest to expensive", "Expensive to cheapest"));
        setItemLore(PRICE_FILTER, priceFilterLore);
        items[47] = PRICE_FILTER;
        items[49] = BACK;

        List<KICAuctionItem> shards = auctionData.getShards();
        shards.sort((a, b) -> filterCheapest ? Long.compare(a.getPrice(), b.getPrice()) : Long.compare(b.getPrice(), a.getPrice()));

        for (int index = 0; index < SLOTS.size(); index++) {
            int slot = SLOTS.get(index);

            if (index < shards.size()) {
                KICAuctionItem item = shards.get(index);

                items[slot] = isItemBought(item.getUuid())
                        ? ITEM_BOUGHT
                        : isItemErrored(item.getUuid())
                        ? ITEM_ERROR
                        : isItemNotFound(item.getUuid())
                        ? ITEM_NOT_FOUND
                        : createCustomItem(item, "§fAttribute Shard", createCustomItemLore(item, true), new ItemStack(Items.prismarine_shard));
            } else {
                items[slot] = WHITE_PANE;
            }
        }

        for (int i = 0; i < INVENTORY_SIZE; i++) {
            inventory.setInventorySlotContents(i, items[i]);
        }
    }

    private static void onSlotClicked(int slot, ItemStack itemStack, int mouseButton) {
        if (auctionGUI == null) return;

        if (activeKICPage != KICAuctionPage.MAIN && SLOTS.contains(slot)) {
            buying(itemStack);
            return;
        }

        switch (activeKICPage) {
            case MAIN: handleMainMenuClick(slot); break;
            case ARMOR: handleArmorMenuClick(slot, mouseButton); break;
            case EQUIPMENT: handleEquipmentMenuClick(slot, mouseButton); break;
            case FISHING: handleFishingMenuClick(slot, mouseButton); break;
            case SHARDS: handleShardMenuClick(slot); break;
        }
    }

    private static void handleMainMenuClick(int slot) {
        switch (slot) {
            case 49: GuiUtils.closeScreen(); break;
            case 19: setupArmorInv(); break;
            case 21: setupEquipmentInv(); break;
            case 23: setupFishingInv(); break;
            case 25: setupShardInv(); break;
        }
    }

    private static void handleArmorMenuClick(int slot, int mouseButton) {
        switch (slot) {
            case 47: toggleFilterCheapest(KICAuction::setupArmorInv); break;
            case 49: setupMainInv(); break;
            case 51: toggleArmorCheapest(); break;
            case 3: changeArmorCategory(mouseButton); break;
            case 5: changeArmorType(mouseButton); break;
        }
    }

    private static void handleEquipmentMenuClick(int slot, int mouseButton) {
        switch (slot) {
            case 47: toggleFilterCheapest(KICAuction::setupEquipmentInv); break;
            case 49: setupMainInv(); break;
            case 3: changeEquipmentCategory(mouseButton); break;
            case 5: changeEquipmentType(mouseButton); break;
        }
    }

    private static void handleFishingMenuClick(int slot, int mouseButton) {
        switch (slot) {
            case 47: toggleFilterCheapest(KICAuction::setupFishingInv); break;
            case 49: setupMainInv(); break;
            case 3: changeFishingCategory(mouseButton); break;
            case 4: changeFishingArmorCategory(mouseButton); break;
            case 5: changeFishingArmorType(mouseButton); break;
        }
    }

    private static void handleShardMenuClick(int slot) {
        switch (slot) {
            case 47: toggleFilterCheapest(KICAuction::setupShardInv); break;
            case 49: setupMainInv(); break;
        }
    }

    private static void toggleFilterCheapest(Runnable setupMethod) {
        filterCheapest = !filterCheapest;
        setupMethod.run();
    }

    private static void toggleArmorCheapest() {
        armorCheapest = !armorCheapest;
        setupArmorInv();
    }

    private static void changeArmorCategory(int mouseButton) {
        activeArmorCategory = (mouseButton == 0) ? ArmorCategory.getNext(activeArmorCategory)
                : ArmorCategory.getPrevious(activeArmorCategory);
        setupArmorInv();
    }

    private static void changeArmorType(int mouseButton) {
        activeArmorType = (mouseButton == 0) ? ArmorType.getNext(activeArmorType)
                : ArmorType.getPrevious(activeArmorType);
        setupArmorInv();
    }

    private static void changeEquipmentCategory(int mouseButton) {
        activeEquipmentCategory = (mouseButton == 0) ? EquipmentCategory.getNext(activeEquipmentCategory)
                : EquipmentCategory.getPrevious(activeEquipmentCategory);
        setupEquipmentInv();
    }

    private static void changeEquipmentType(int mouseButton) {
        activeEquipmentType.compute(activeEquipmentCategory,
                (k, activeType) -> (mouseButton == 0) ? EquipmentType.getNext(activeEquipmentCategory, activeType)
                        : EquipmentType.getPrevious(activeEquipmentCategory, activeType));
        setupEquipmentInv();
    }

    private static void changeFishingCategory(int mouseButton) {
        activeFishingCategory = (mouseButton == 0) ? FishingCategory.getNext(activeFishingCategory)
                : FishingCategory.getPrevious(activeFishingCategory);
        setupFishingInv();
    }

    private static void changeFishingArmorCategory(int mouseButton) {
        activeFishingArmorCategory = (mouseButton == 0) ? ArmorCategory.getNext(activeFishingArmorCategory)
                : ArmorCategory.getPrevious(activeFishingArmorCategory);
        setupFishingInv();
    }

    private static void changeFishingArmorType(int mouseButton) {
        activeFishingArmorType = (mouseButton == 0) ? FishingType.getNext(activeFishingArmorType)
                : FishingType.getPrevious(activeFishingArmorType);
        setupFishingInv();
    }

    public static void onGuiClosed() {
        inventory = null;
        auctionGUI = null;
    }

    private static void buying(ItemStack item) {
        String uuid = getUuid(item);
        String[] itemData = getItemData(item);

        long currentTimestamp = auctionData.getTimestamp();
        errorHistory.entrySet().removeIf(entry -> entry.getKey() != currentTimestamp);

        if (itemData == null) {
            errorHistory.computeIfAbsent(currentTimestamp, k -> new ArrayList<>()).add(uuid);
            return;
        }
        KICLogger.info(Arrays.toString(itemData));

        currentItemData = itemData;
        GuiUtils.closeScreen();
        Multithreading.schedule(() -> sendCommand("/viewauction " + uuid), 150, TimeUnit.MILLISECONDS);
    }

    @SubscribeEvent(receiveCanceled = true)
    public void onChatMessage(ClientChatReceivedEvent event) {
        if (currentItemData == null) return;

        String message = removeUnicode(removeFormatting(event.message.getUnformattedText()));
        if ("This auction wasn't found!".equalsIgnoreCase(message) && currentItemData != null) {
            long currentTimestamp = auctionData.getTimestamp();

            notFoundHistory.entrySet().removeIf(entry -> entry.getKey() != currentTimestamp);
            notFoundHistory.computeIfAbsent(currentTimestamp, k -> new ArrayList<>()).add(currentItemData[2]);

            if (KICConfig.openKAGUIAgain) {
                Multithreading.schedule(KICAuction::open, 150, TimeUnit.MILLISECONDS);
            }
            currentItemData = null;
            return;
        }

        if (!message.startsWith("You purchased")) return;

        try {
            Matcher matcher = BOUGHT_REGEX.matcher(message);
            if (matcher.matches()) {
                String item = matcher.group(1);
                String priceStr = matcher.group(2);
                long price = Long.parseLong(priceStr.replaceAll(",", ""));
                long currentItemPrice = Long.parseLong(currentItemData[1]);
                String itemId = item.replaceAll(" ", "_").toUpperCase();

                KICLogger.info(String.format("Price: %s | Current Price: %s | ItemID: %s", price, currentItemPrice, itemId));

                if (price > 0 && price == currentItemPrice && itemId.contains(currentItemData[0])) {
                    long currentTimestamp = auctionData.getTimestamp();

                    purchaseHistory.entrySet().removeIf(entry -> entry.getKey() != currentTimestamp);
                    purchaseHistory.computeIfAbsent(currentTimestamp, k -> new ArrayList<>()).add(currentItemData[2]);

                    if (KICConfig.openKAGUIAgain) {
                        Multithreading.schedule(KICAuction::open, 150, TimeUnit.MILLISECONDS);
                    }
                }
            }
        } catch (Exception ignored) {}

        currentItemData = null;
    }

    private static String getUuid(ItemStack item) {
        if (item == null || !item.hasTagCompound()) {
            return "";
        }

        NBTTagCompound nbt = item.getTagCompound();
        if (nbt == null) return "";

        return nbt.hasKey("uuid") ? nbt.getString("uuid") : "";
    }

    private static String[] getItemData(ItemStack item) {
        if (item == null || !item.hasTagCompound()) {
            return null;
        }

        NBTTagCompound nbt = item.getTagCompound();
        if (nbt == null) return null;

        String[] keys = {"item-id", "item-price", "uuid"};
        String[] values = new String[keys.length];

        for (int i = 0; i < keys.length; i++) {
            if (!nbt.hasKey(keys[i])) {
                return null;
            }
            values[i] = nbt.getString(keys[i]);
        }

        return values;
    }

    private static ItemStack makeInfoItem() {
        ItemStack itemStack = createItem(new ItemStack(Blocks.redstone_torch), "§a§lInfo");

        List<String> loreLines = new ArrayList<>();

        loreLines.add("");
        loreLines.add("§6> Search");

        String attribute1Text = "§7- §b" + auctionData.getAttribute1().replace("Mending", "Vitality");

        if (auctionData.getAttributeLvl1() != 0) {
            attribute1Text += " " + auctionData.getAttributeLvl1();
        }
        loreLines.add(attribute1Text);

        if (auctionData.getAttribute2() != null) {
            String attribute2Text = "§7- §b" + auctionData.getAttribute2().replace("Mending", "Vitality");
            if (auctionData.getAttributeLvl2() != 0) {
                attribute2Text += " " + auctionData.getAttributeLvl2();
            }
            loreLines.add(attribute2Text);
        }

        loreLines.add("");
        loreLines.add("§6> Last Updated");
        loreLines.add("§7- §b" + timeSince(auctionData.getTimestamp()));

        setItemLore(itemStack, loreLines);
        return itemStack;
    }

    private static int determineColor(String itemId, ArmorCategory category) {
        for (ArmorType key : COLOR_MAP.keySet()) {
            if (itemId.contains(key.getDisplayText().toUpperCase())) {
                return COLOR_MAP.get(key).getOrDefault(category, 0xFFFFFF);
            }
        }
        return 0xFFFFFF;
    }

    private static int determineColorFishing(String itemId, ArmorCategory category) {
        for (FishingType key : FISHING_COLOR_MAP.keySet()) {
            if (itemId.contains(key.name().toUpperCase())) {
                return FISHING_COLOR_MAP.get(key).getOrDefault(category, 0xFFFFFF);
            }
        }
        return 0xFFFFFF;
    }

    private static ItemStack createCustomItem(KICAuctionItem item, String itemName, List<String> itemLore, ItemStack mcItem) {
        NBTTagCompound tag = new NBTTagCompound();

        tag.setString("uuid", item.getUuid());
        tag.setString("item-price", String.valueOf(item.getPrice()));
        tag.setString("item-id", item.getItemId());

        mcItem.setTagCompound(tag);
        mcItem.setStackDisplayName(itemName);
        setItemLore(mcItem, itemLore);

        return mcItem;
    }

    private static ItemStack skullItem(String texture, String name, List<String> lore, KICAuctionItem item) {
        ItemStack skull = new ItemStack(Items.skull, 1, 3);
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagCompound skullOwner = new NBTTagCompound();
        NBTTagCompound properties = new NBTTagCompound();
        NBTTagList textures = new NBTTagList();
        NBTTagCompound textureString = new NBTTagCompound();
        NBTTagCompound display = new NBTTagCompound();

        String uuid = item.getUuid();

        skullOwner.setString("Id", uuid);
        skullOwner.setString("Name", uuid);

        if (!isNullOrEmpty(texture)) {
            textureString.setString("Value", texture);
            textures.appendTag(textureString);
        }

        display.setString("Name", name);
        tag.setTag("display", display);
        tag.setString("uuid", uuid);
        tag.setString("item-price", String.valueOf(item.getPrice()));
        tag.setString("item-id", item.getItemId());

        properties.setTag("textures", textures);
        skullOwner.setTag("Properties", properties);
        tag.setTag("SkullOwner", skullOwner);

        skull.setTagCompound(tag);
        setItemLore(skull, lore);

        return skull;
    }

    private static String createName(KICAuctionItem item) {
        String itemTypeName = formatId(item.getItemId());
        String rarity = "§8";
        String reforge = "";
        String stars;

        List<String> lore = item.getLore();
        if (lore != null && !lore.isEmpty()) {
            rarity = getRarity(lore.get(lore.size() - 1));
        }

        String modifier = item.getModifier();
        Integer upgradeLevel = item.getUpgrade_level();

        if (modifier != null) {
            reforge = capitalizeEachWord(modifier) + " ";
        }

        stars = calculateStars(upgradeLevel);

        return rarity + reforge + itemTypeName + stars;
    }

    private static String calculateStars(Integer upgradeLvl) {
        if (upgradeLvl == null || upgradeLvl <= 0) return "";

        int remainingLevels = upgradeLvl;
        int purpleStars = Math.min(remainingLevels / 2, 2);
        remainingLevels -= purpleStars * 2;
        int goldStars = Math.min(remainingLevels, 5 - purpleStars);

        StringBuilder stars = new StringBuilder();
        if (purpleStars > 0) stars.append("§d").append(new String(new char[purpleStars]).replace("\0", "✪"));
        if (goldStars > 0) stars.append("§6").append(new String(new char[purpleStars]).replace("\0", "✪"));

        return stars.length() > 0 ? " " + stars : "";
    }

    private static String getRarity(String lastLoreLine) {
        String rarityLine = lastLoreLine.toUpperCase();
        if (rarityLine.contains("DIVINE")) return "§b";
        if (rarityLine.contains("MYTHIC")) return "§d";
        if (rarityLine.contains("LEGENDARY")) return "§6";
        if (rarityLine.contains("EPIC")) return "§5";
        if (rarityLine.contains("RARE")) return "§9";
        if (rarityLine.contains("UNCOMMON")) return "§a";
        return "§f";
    }

    private static List<String> createCustomItemLore(KICAuctionItem item, boolean isShard) {
        List<String> lore = new ArrayList<>();

        if (item.getLore() != null) {
            for (String line : item.getLore()) {
                String updatedLine = line;
                int pricePerXLvl = isShard ? KICConfig.kaPricePerXLvlShards : KICConfig.kaPricePerXLvl;

                if (item.hasAttribute1() && (line.contains(item.getAttribute1(true)) || line.contains(item.getAttribute1(false)))) {
                    long baseLevelValue = item.getPrice() / (long) Math.pow(2, item.getAttributeLvl1() - 1);
                    long adjustedOutput = baseLevelValue * (long) Math.pow(2, pricePerXLvl - 1);
                    updatedLine += " §7(§6" + parseToShorthandNumber(adjustedOutput) + "§7/§elvl" + (pricePerXLvl > 1 ? " " + pricePerXLvl : "") + "§7)";
                } else if (item.hasAttribute2() && (line.contains(item.getAttribute2(true)) || line.contains(item.getAttribute2(false)))) {
                    long baseLevelValue = item.getPrice() / (long) Math.pow(2, item.getAttributeLvl2() - 1);
                    long adjustedOutput = baseLevelValue * (long) Math.pow(2, pricePerXLvl - 1);
                    updatedLine += " §7(§6" + parseToShorthandNumber(adjustedOutput) + "§7/§elvl" + (pricePerXLvl > 1 ? " " + pricePerXLvl : "") + "§7)";
                }

                lore.add(updatedLine);
            }
        }

        lore.add("§8——————————————————————");
        lore.add("§7Buy it now: §6" + formatNumber(item.getPrice()));
        lore.add("");
        lore.add("§eClick to view the auction!");

        return lore;
    }

    private static String formatNumber(long num) {
        return String.format("%,d", num);
    }

    private static List<String> createLore(String active, List<String> items) {
        return items.stream()
                .map(item -> item.equals(active) ? "§a> " + capitalizeEachWord(item) : "§7" + capitalizeEachWord(item))
                .collect(Collectors.toList());
    }

    private enum KICAuctionPage {
        MAIN,
        ARMOR,
        EQUIPMENT,
        FISHING,
        SHARDS
    }
}
