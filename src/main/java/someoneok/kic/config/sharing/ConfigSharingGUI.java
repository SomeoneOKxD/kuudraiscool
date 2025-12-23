package someoneok.kic.config.sharing;

import cc.polyfrost.oneconfig.utils.IOUtils;
import cc.polyfrost.oneconfig.utils.JsonUtils;
import cc.polyfrost.oneconfig.utils.Multithreading;
import cc.polyfrost.oneconfig.utils.gui.GuiUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import someoneok.kic.config.KICConfig;
import someoneok.kic.models.APIException;
import someoneok.kic.models.KICCustomGUI;
import someoneok.kic.models.config.ConfigResponse;
import someoneok.kic.utils.ApiUtils;
import someoneok.kic.utils.NetworkUtils;
import someoneok.kic.utils.PlayerUtils;
import someoneok.kic.utils.dev.KICLogger;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static someoneok.kic.KIC.*;
import static someoneok.kic.utils.ChatUtils.sendMessageToPlayer;
import static someoneok.kic.utils.ItemUtils.*;

public class ConfigSharingGUI {
    private static final int INVENTORY_SIZE = KICCustomGUI.getCOLUMNS() * KICCustomGUI.getROWS();
    private static final List<Integer> SLOTS = Arrays.asList(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43);
    private static final List<Integer> SLOTS_MY = Arrays.asList(12, 13, 14);
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private static final ItemStack PANE = createItemWithDamage(new ItemStack(Blocks.stained_glass_pane), 5);
    private static final ItemStack WHITE_PANE = createItemWithDamage(new ItemStack(Blocks.stained_glass_pane), 0);
    private static final ItemStack BACK = createItem(new ItemStack(Items.arrow), "§c§lBack");
    private static final ItemStack CLOSE = createItem(new ItemStack(Blocks.barrier), "§c§lClose");
    private static final ItemStack REFRESH = createItem(new ItemStack(Blocks.redstone_block), "§a§lRefresh");
    private static final ItemStack FILTER = createItem(new ItemStack(Items.comparator), "§b§lVersion Filter");

    private static final ItemStack PREV = createItem(new ItemStack(Items.arrow), "§a§lPrevious Page");
    private static final ItemStack NEXT = createItem(new ItemStack(Items.arrow), "§a§lNext Page");

    private static final ItemStack SHARE = createItem(new ItemStack(Items.writable_book), "§a§lShare Current Config");
    private static final ItemStack DELETE = createItem(new ItemStack(Items.flint_and_steel), "§c§lDelete");
    private static final ItemStack VISIBILITY = getVisibilityItem();

    private static final Map<String, ConfigResponse> configs = new HashMap<>();
    private static final Map<String, ConfigResponse> myConfigs = new HashMap<>();
    private static InventoryBasic inventory;
    private static KICCustomGUI sharingGui;
    private static int currentPage;
    private static ConfigSharingPage activePage;
    private static ConfigResponse currentConfig;
    private static boolean changingVisibility = false;
    private static boolean deleting = false;
    private static boolean sharing = false;
    private static boolean updating = false;
    private static int currentConfigNum = -1;
    private static VersionFilter versionFilter = VersionFilter.ALL;
    private static final long REFRESH_COOLDOWN_MS = 5_000;
    private static long lastRefreshTime = 0;

    private static volatile ItemStack mySkull = createMyConfigsSkullPlaceholder();
    private static volatile boolean skullLoading = false;

    static { updateFilterLore(); }

    private static void update(Runnable callback) {
        if (!ApiUtils.isVerified() || updating) return;
        updating = true;
        configs.clear();
        myConfigs.clear();
        currentPage = 1;
        activePage = ConfigSharingPage.MAIN;
        currentConfig = null;
        Multithreading.runAsync(() -> {
            JsonArray responseAll;
            JsonArray responseMy;
            try {
                responseAll = JsonUtils.parseString(NetworkUtils.sendGetRequest(ApiUtils.apiHost() + "/config/all", true)).getAsJsonArray();
                responseMy = JsonUtils.parseString(NetworkUtils.sendGetRequest(ApiUtils.apiHost() + "/config/my", true)).getAsJsonArray();
            } catch (APIException e) {
                sendMessageToPlayer(String.format("%s §c%s", KICPrefix, e.getMessage()));
                return;
            }
            if (responseAll == null || responseAll.isJsonNull() || responseMy == null || responseMy.isJsonNull()) return;
            try {
                for (JsonElement configElement : responseAll) {
                    if (!configElement.isJsonObject()) continue;

                    JsonObject configObject = configElement.getAsJsonObject();
                    String configId = configObject.get("configId").getAsString();
                    String username = configObject.get("username").getAsString();
                    String config = configObject.get("config").getAsString();
                    String modVersion = configObject.get("modVersion").getAsString();
                    boolean hidden = configObject.get("hidden").getAsBoolean();
                    long timestamp = configObject.get("timestamp").getAsLong();

                    configs.put(configId, new ConfigResponse(configId, username, config, modVersion, hidden, timestamp));
                }
                for (JsonElement configElement : responseMy) {
                    if (!configElement.isJsonObject()) continue;

                    JsonObject configObject = configElement.getAsJsonObject();
                    String configId = configObject.get("configId").getAsString();
                    String username = configObject.get("username").getAsString();
                    String config = configObject.get("config").getAsString();
                    String modVersion = configObject.get("modVersion").getAsString();
                    boolean hidden = configObject.get("hidden").getAsBoolean();
                    long timestamp = configObject.get("timestamp").getAsLong();

                    ConfigResponse response = new ConfigResponse(configId, username, config, modVersion, hidden, timestamp);
                    myConfigs.put(configId, response);
                    if (!configs.containsKey(configId)) configs.put(configId, response);
                }
                callback.run();
            } catch (Exception e) {
                KICLogger.error("Error parsing config data: " + e.getMessage());
            }
        });
    }

    public static void open() {
        update(ConfigSharingGUI::show);
    }

    private static void show() {
        inventory = new InventoryBasic("§a§lConfig Sharing", true, INVENTORY_SIZE);

        ensureMySkullLoadedAsync();
        setupMainInv();

        sharingGui = new KICCustomGUI(inventory, () -> KICConfig.KICSharingGuiStyle,
                ConfigSharingGUI::onSlotClicked, ConfigSharingGUI::onGuiClose);

        GuiUtils.displayScreen(sharingGui);
    }

    private static void setupMainInv() {
        currentConfig = null;
        updating = false;
        activePage = ConfigSharingPage.MAIN;
        inventory.setCustomName("§a§lConfig Sharing");

        ItemStack[] items = new ItemStack[INVENTORY_SIZE];
        Arrays.fill(items, PANE);
        items[49] = CLOSE;
        items[0] = REFRESH;
        items[4] = FILTER;
        items[8] = mySkull;

        List<ConfigResponse> sortedConfigs = configs.values().stream()
                .filter(config -> !config.isHidden()) // Hide my hidden configs from main page
                .filter(config -> {
                    if (versionFilter == VersionFilter.ALL) return true;
                    return config.getModVersion().equals(VERSION);
                })
                .sorted(Comparator.comparingLong(ConfigResponse::getTimestamp).reversed())
                .collect(Collectors.toList());

        int startIndex = (currentPage - 1) * SLOTS.size();
        int endIndex = Math.min(startIndex + SLOTS.size(), sortedConfigs.size());

        List<ConfigResponse> pagedConfigs = sortedConfigs.subList(startIndex, endIndex);

        for (int index = 0; index < SLOTS.size(); index++) {
            int slot = SLOTS.get(index);

            if (index < pagedConfigs.size()) {
                ConfigResponse config = pagedConfigs.get(index);
                String formattedDate = DATE_FORMAT.format(new Date(config.getTimestamp()));

                items[slot] = createItem(
                        createGlowingBook(getDefaultConfigLore(config.getModVersion(), formattedDate)),
                        String.format("§2§l%s§r§a's Config §7(ID: %s)", config.getUsername(), config.getConfigId())
                );
            } else {
                items[slot] = WHITE_PANE;
            }
        }

        if (currentPage > 1) items[45] = PREV;
        if (endIndex < sortedConfigs.size()) items[53] = NEXT;
        for (int i = 0; i < INVENTORY_SIZE; i++) inventory.setInventorySlotContents(i, items[i]);
    }

    private static void setupMyInv() {
        currentConfig = null;
        currentConfigNum = -1;
        sharing = false;
        updating = false;
        activePage = ConfigSharingPage.MY;
        inventory.setCustomName("§a§lConfig Sharing - My Configs");

        ItemStack[] items = new ItemStack[INVENTORY_SIZE];
        Arrays.fill(items, PANE);
        items[49] = BACK;
        items[31] = SHARE;

        List<ConfigResponse> sortedConfigs = myConfigs.values().stream()
                .sorted(Comparator.comparingLong(ConfigResponse::getTimestamp).reversed())
                .collect(Collectors.toList());

        for (int index = 0; index < SLOTS_MY.size(); index++) {
            int slot = SLOTS_MY.get(index);

            if (index < sortedConfigs.size()) {
                ConfigResponse config = sortedConfigs.get(index);
                String formattedDate = DATE_FORMAT.format(new Date(config.getTimestamp()));

                items[slot] = createItem(
                        createGlowingBook(Arrays.asList("§7Version: " + config.getModVersion(), "§7Shared: " + formattedDate)),
                        String.format("§aConfig §2#%d §r§7(ID: %s) §7(%s§7)", index + 1, config.getConfigId(), (config.isHidden() ? "§cPRIVATE" : "§aPUBLIC"))
                );
            } else {
                items[slot] = WHITE_PANE;
            }
        }

        for (int i = 0; i < INVENTORY_SIZE; i++) inventory.setInventorySlotContents(i, items[i]);
    }

    private static void setupSelectedInv() {
        activePage = ConfigSharingPage.SELECTED;
        changingVisibility = false;
        deleting = false;
        inventory.setCustomName("§a§lConfig Sharing - Config #" + currentConfigNum);

        ItemStack[] items = new ItemStack[INVENTORY_SIZE];
        Arrays.fill(items, PANE);
        items[49] = BACK;

        items[30] = VISIBILITY;
        items[32] = DELETE;

        String formattedDate = DATE_FORMAT.format(new Date(currentConfig.getTimestamp()));

        items[13] = createItem(
                createGlowingBook(getDefaultConfigLore(currentConfig.getModVersion(), formattedDate)),
                String.format("§aConfig §2#%d §r§7(ID: %s) §7(%s§7)", currentConfigNum, currentConfig.getConfigId(), (currentConfig.isHidden() ? "§cPRIVATE" : "§aPUBLIC"))
        );

        for (int i = 0; i < INVENTORY_SIZE; i++) inventory.setInventorySlotContents(i, items[i]);
    }

    private static void onSlotClicked(int slot, ItemStack itemStack, int mouseButton) {
        if (sharingGui == null) return;

        if (activePage == ConfigSharingPage.MAIN && SLOTS.contains(slot)) {
            int indexInPage = SLOTS.indexOf(slot);
            List<ConfigResponse> sortedConfigs = configs.values().stream()
                    .filter(config -> !config.isHidden()) // Hide my hidden configs from main page
                    .filter(config -> {
                        if (versionFilter == VersionFilter.ALL) return true;
                        return config.getModVersion().equals(VERSION);
                    })
                    .sorted(Comparator.comparingLong(ConfigResponse::getTimestamp).reversed())
                    .collect(Collectors.toList());

            int startIndex = (currentPage - 1) * SLOTS.size();
            int endIndex = Math.min(startIndex + SLOTS.size(), sortedConfigs.size());
            List<ConfigResponse> pagedConfigs = sortedConfigs.subList(startIndex, endIndex);

            if (indexInPage < pagedConfigs.size()) {
                ConfigResponse config = pagedConfigs.get(indexInPage);
                if (mouseButton == 0) {
                    IOUtils.copyStringToClipboard("/kic importconfig " + config.getConfigId());
                    sendMessageToPlayer(String.format(
                            "%s §aCopied import command for §2§l%s§r§a's config to clipboard. Use §eCtrl+V§a to paste it!",
                            KICPrefix, config.getUsername()));
                } else if (mouseButton == 1) {
                    importConfig(pagedConfigs.get(indexInPage));
                }
            }
            return;
        }

        if (activePage == ConfigSharingPage.MY && SLOTS_MY.contains(slot)) {
            int indexInPage = SLOTS_MY.indexOf(slot);
            List<ConfigResponse> sortedConfigs = myConfigs.values().stream()
                    .sorted(Comparator.comparingLong(ConfigResponse::getTimestamp).reversed())
                    .collect(Collectors.toList());

            if (indexInPage < sortedConfigs.size()) {
                currentConfig = sortedConfigs.get(indexInPage);
                currentConfigNum = indexInPage + 1;
                setupSelectedInv();
            }
            return;
        }

        switch (activePage) {
            case MAIN: handleMainMenuClick(slot); break;
            case MY: handleMyMenuClick(slot); break;
            case SELECTED: handleSelectedMenuClick(slot, mouseButton); break;
        }
    }

    private static void handleMainMenuClick(int slot) {
        switch (slot) {
            case 49: GuiUtils.closeScreen(); break;
            case 8: setupMyInv(); break;
            case 45: if (currentPage > 1) { currentPage--; setupMainInv(); } break;
            case 53: if ((currentPage * SLOTS.size()) < configs.size()) { currentPage++; setupMainInv(); } break;
            case 4:
                versionFilter = (versionFilter == VersionFilter.ALL)
                        ? VersionFilter.CURRENT
                        : VersionFilter.ALL;
                updateFilterLore();
                setupMainInv();
                break;
            case 0:
                long now = System.currentTimeMillis();

                if (now - lastRefreshTime < REFRESH_COOLDOWN_MS) {
                    long remaining = (REFRESH_COOLDOWN_MS - (now - lastRefreshTime)) / 1000;
                    sendMessageToPlayer(String.format(
                            "%s §cPlease wait §e%d§c second%s before refreshing again.",
                            KICPrefix,
                            Math.max(1, remaining),
                            remaining == 1 ? "" : "s"
                    ));
                    return;
                }

                lastRefreshTime = now;
                update(() -> {
                    setupMainInv();
                    sendMessageToPlayer(KICPrefix + " §aSuccessfully refreshed all configs.");
                });
                break;
        }
    }

    private static void handleMyMenuClick(int slot) {
        switch (slot) {
            case 49: setupMainInv(); break;
            case 31: share(); break;
        }
    }

    private static void handleSelectedMenuClick(int slot, int mouseButton) {
        switch (slot) {
            case 49: setupMyInv(); break;
            case 30: changeVisibility(); break;
            case 32: delete(); break;
            case 13: handleSelectedImport(mouseButton); break;
        }
    }

    private static void handleSelectedImport(int mouseButton) {
        if (mouseButton == 0) {
            IOUtils.copyStringToClipboard("/kic importconfig " + currentConfig.getConfigId());
            sendMessageToPlayer(String.format(
                    "%s §aCopied import command for Config §2#%s§r§a to clipboard. Use §eCtrl+V§a to paste it!",
                    KICPrefix, currentConfigNum));
        } else if (mouseButton == 1) {
            importConfig(currentConfig);
        }
    }

    private static void changeVisibility() {
        if (changingVisibility || currentConfig == null || deleting) return;
        changingVisibility = true;
        Multithreading.runAsync(() -> {
            try {
                String id = currentConfig.getConfigId();
                boolean newHidden = !currentConfig.isHidden();
                NetworkUtils.sendPostRequest(ApiUtils.apiHost() + "/config/visibility/" + id + "?hidden=" + newHidden, true, null);
                currentConfig.setHidden(newHidden);
                configs.get(id).setHidden(newHidden);
                myConfigs.get(id).setHidden(newHidden);
                sendMessageToPlayer(String.format(
                        "%s §aSuccessfully changed your config from %s §ato %s. §7(ID: §2§l%s§r§7)",
                        KICPrefix, (!newHidden ? "§cPRIVATE" : "§aPUBLIC"), (newHidden ? "§cPRIVATE" : "§aPUBLIC"), id));
                setupMyInv();
            } catch (APIException e) {
                sendMessageToPlayer(String.format("%s §c%s", KICPrefix, e.getMessage()));
            }
        });
    }

    private static void delete() {
        if (deleting || currentConfig == null || changingVisibility) return;
        deleting = true;
        Multithreading.runAsync(() -> {
            try {
                String id = currentConfig.getConfigId();
                NetworkUtils.sendDeleteRequest(ApiUtils.apiHost() + "/config/delete/" + id, true);
                configs.remove(id);
                myConfigs.remove(id);
                sendMessageToPlayer(String.format(
                        "%s §aSuccessfully deleted your config. §7(ID: §2§l%s§r§7)",
                        KICPrefix, id));
            } catch (APIException e) {
                sendMessageToPlayer(String.format("%s §c%s", KICPrefix, e.getMessage()));
            }
            setupMyInv();
        });
    }

    private static void share() {
        if (sharing) return;
        sharing = true;
        ConfigSharing.shareConfig(() -> update(ConfigSharingGUI::setupMyInv));
    }

    private static void importConfig(ConfigResponse config) {
        if (config != null) ConfigSharing.importConfig(config);
        GuiUtils.closeScreen();
    }

    private static ItemStack createMyConfigsSkullPlaceholder() {
        ItemStack skull = new ItemStack(Items.skull, 1, 3);
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagCompound display = new NBTTagCompound();
        display.setString("Name", "§a§lMy Configs");
        tag.setTag("display", display);
        skull.setTagCompound(tag);
        return skull;
    }

    private static void ensureMySkullLoadedAsync() {
        if (skullLoading) return;
        skullLoading = true;

        final String name = PlayerUtils.getPlayerName();

        Multithreading.runAsync(() -> {
            try {
                GameProfile gp = new GameProfile(null, name);
                GameProfile filled = net.minecraft.tileentity.TileEntitySkull.updateGameprofile(gp);
                ItemStack resolved = new ItemStack(Items.skull, 1, 3);
                NBTTagCompound tag = new NBTTagCompound();
                NBTTagCompound display = new NBTTagCompound();
                display.setString("Name", "§a§lMy Configs");
                tag.setTag("display", display);
                NBTTagCompound owner = new NBTTagCompound();
                net.minecraft.nbt.NBTUtil.writeGameProfile(owner, filled);
                tag.setTag("SkullOwner", owner);
                resolved.setTagCompound(tag);
                mc.addScheduledTask(() -> {
                    mySkull = resolved;
                    skullLoading = false;
                    if (inventory != null && sharingGui != null && activePage == ConfigSharingPage.MAIN) {
                        inventory.setInventorySlotContents(8, mySkull);
                    }
                });
            } catch (Throwable t) {
                skullLoading = false;
            }
        });
    }

    private static ItemStack createGlowingBook(List<String> lore) {
        ItemStack book = new ItemStack(Items.book);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag("ench", new NBTTagList());
        book.setTagCompound(tag);
        setItemLore(book, lore);
        return book;
    }

    private static List<String> getDefaultConfigLore(String version, String formattedDate) {
        return Arrays.asList(
                "§7Version: " + version,
                "§7Shared: " + formattedDate,
                "",
                "§a§lLeft-click§a to copy the import command.",
                "§b§lRight-click§b to import this config."
        );
    }

    private static void onGuiClose() {
        updating = false;
        currentConfigNum = -1;
        currentConfig = null;
    }

    private static ItemStack getVisibilityItem() {
        ItemStack item = createItem(new ItemStack(Blocks.beacon), "§b§lVisibility");
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add("§cPRIVATE §8→ §7Only importable via ID");
        lore.add("§aPUBLIC §8→ §7Visible and importable by anyone");
        setItemLore(item, lore);
        return item;
    }

    private static void updateFilterLore() {
        List<String> lore = new ArrayList<>();
        lore.add("");

        if (versionFilter == VersionFilter.ALL) {
            lore.add("§a§l> All");
            lore.add("§7" + VERSION + " (current)");
        } else {
            lore.add("§7All");
            lore.add("§a§l> " + VERSION + " (current)");
        }

        lore.add("");
        lore.add("§eClick to toggle");

        setItemLore(FILTER, lore);
    }

    private enum ConfigSharingPage {
        MAIN,
        MY,
        SELECTED
    }

    private enum VersionFilter {
        ALL,
        CURRENT
    }
}
