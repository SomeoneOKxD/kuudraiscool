package someoneok.kic.modules.admin.websocket;

import cc.polyfrost.oneconfig.utils.JsonUtils;
import cc.polyfrost.oneconfig.utils.Multithreading;
import cc.polyfrost.oneconfig.utils.gui.GuiUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import someoneok.kic.config.KICConfig;
import someoneok.kic.models.APIException;
import someoneok.kic.models.Color;
import someoneok.kic.models.KICCustomGUI;
import someoneok.kic.utils.NetworkUtils;
import someoneok.kic.utils.StringUtils;
import someoneok.kic.utils.dev.KICLogger;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static someoneok.kic.KIC.KICPrefix;
import static someoneok.kic.utils.ApiUtils.isAdmin;
import static someoneok.kic.utils.GeneralUtils.sendCommand;
import static someoneok.kic.utils.GeneralUtils.sendMessageToPlayer;
import static someoneok.kic.utils.ItemUtils.*;
import static someoneok.kic.utils.StringUtils.*;

public class KICAdminUI {
    private static final int INVENTORY_SIZE = KICCustomGUI.getCOLUMNS() * KICCustomGUI.getROWS();
    private static final List<Integer> SLOTS = Arrays.asList(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43);

    private static final ItemStack PANE = createItemWithDamage(new ItemStack(Blocks.stained_glass_pane), 14);
    private static final ItemStack WHITE_PANE = createItemWithDamage(new ItemStack(Blocks.stained_glass_pane), 0);
    private static final ItemStack BACK = createItem(new ItemStack(Items.arrow), "§c§lBack");
    private static final ItemStack CLOSE = createItem(new ItemStack(Blocks.barrier), "§c§lClose");

    private static final ItemStack TITLE = createItem(new ItemStack(Items.sign), "§b§lTitle");
    private static final ItemStack VISIT = createItem(new ItemStack(Items.oak_door), "§b§lVisit");
    private static final ItemStack PARTY = createItem(new ItemStack(Items.paper), "§b§lParty");

    private static final ItemStack PREV = createItem(new ItemStack(Items.arrow), "§a§lPrevious Page");
    private static final ItemStack NEXT = createItem(new ItemStack(Items.arrow), "§a§lNext Page");

    private static final Map<String, Client> clients = new HashMap<>();
    private static InventoryBasic inventory;
    private static KICCustomGUI adminGUI;
    private static int currentPage;
    private static KICAdminPage activePage;
    private static Client currentClient;

    public static void open() {
        if (!isAdmin()) {
            sendMessageToPlayer(KICPrefix + " §cUNAUTHORIZED!");
            return;
        }
        updatePaneColor(PANE, KICConfig.KICAdminGuiColor);
        clients.clear();
        currentPage = 1;
        activePage = KICAdminPage.MAIN;
        currentClient = null;
        Multithreading.runAsync(() -> {
            JsonArray response;
            try {
                response = JsonUtils.parseString(NetworkUtils.sendGetRequest("https://api.sm0kez.com/admin/ws-clients", true)).getAsJsonArray();
            } catch (APIException e) {
                sendMessageToPlayer(String.format("%s §c%s", KICPrefix, e.getMessage()));
                return;
            }
            if (response == null) return;
            try {
                for (JsonElement clientElement : response) {
                    if (!clientElement.isJsonObject()) continue;

                    JsonObject clientObject = clientElement.getAsJsonObject();
                    JsonObject apiKeyObject = getJsonObject(clientObject, "apiKey");
                    if (apiKeyObject == null) continue;

                    String ign = getString(clientObject, "ign");
                    String owner = getString(apiKeyObject, "owner");
                    String discord = getString(apiKeyObject, "discord");
                    if (ign == null || owner == null || discord == null) continue;

                    String connectedIgn = getString(clientObject, "connectedIgn");
                    String modVersion = getString(clientObject, "modVersion");

                    List<String> roles = getStringList(apiKeyObject, "roles");
                    if (roles.isEmpty()) continue;

                    int requests = getInt(apiKeyObject, "monthlyRequests", 0);
                    long playtime = getLong(apiKeyObject, "monthlyPlaytime", 0);
                    long connectionTime = getLong(clientObject, "connectionTime", 0);
                    if (connectionTime == 0) continue;

                    Client client = new Client(ign, owner, discord, connectedIgn, modVersion, roles, requests, playtime, connectionTime);
                    client.setSkull(skullItem(client));
                    clients.put(owner, client);
                }
                show();
            } catch (Exception e) {
                KICLogger.error("Error parsing client data: " + e.getMessage());
            }
        });
    }

    private static void show() {
        if (clients.isEmpty()) {
            sendMessageToPlayer(KICPrefix + " §cNo clients connected!");
            return;
        }
        inventory = new InventoryBasic(Color.getColorCode(KICConfig.KICAdminGuiColor) + "§lKIC Admin", true, INVENTORY_SIZE);

        setupMainInv();

        adminGUI = new KICCustomGUI(inventory, () -> KICConfig.KICAdminGuiStyle, KICAdminUI::onSlotClicked);

        GuiUtils.displayScreen(adminGUI);
    }

    private static void setupMainInv() {
        currentClient = null;
        activePage = KICAdminPage.MAIN;
        inventory.setCustomName(Color.getColorCode(KICConfig.KICAdminGuiColor) + "§lKIC Admin");

        ItemStack[] items = new ItemStack[INVENTORY_SIZE];
        Arrays.fill(items, PANE);
        items[49] = CLOSE;

        List<Client> sortedClients = clients.values().stream()
                .filter(client -> !client.getOwner().equals("kuudraiscool_discord_bot"))
                .sorted(Comparator.comparingLong(Client::getConnectionTime))
                .collect(Collectors.toList());

        int startIndex = (currentPage - 1) * SLOTS.size();
        int endIndex = Math.min(startIndex + SLOTS.size(), sortedClients.size());

        for (int index = 0; index < SLOTS.size(); index++) {
            int slot = SLOTS.get(index);

            if (index < sortedClients.size()) {
                Client client = sortedClients.get(index);

                items[slot] = client.getSkull();
            } else {
                items[slot] = WHITE_PANE;
            }
        }

        if (currentPage > 1) {
            items[45] = PREV;
        }
        if (endIndex < sortedClients.size()) {
            items[53] = NEXT;
        }

        for (int i = 0; i < INVENTORY_SIZE; i++) {
            inventory.setInventorySlotContents(i, items[i]);
        }
    }

    private static void setupUserInv() {
        activePage = KICAdminPage.USER;
        inventory.setCustomName(Color.getColorCode(KICConfig.KICAdminGuiColor) + "§lKIC Admin - " + currentClient.getIgn());
        ItemStack[] items = new ItemStack[INVENTORY_SIZE];
        Arrays.fill(items, PANE);

        items[20] = VISIT;
        items[22] = TITLE;
        items[24] = PARTY;
        items[49] = BACK;

        for (int i = 0; i < INVENTORY_SIZE; i++) {
            inventory.setInventorySlotContents(i, items[i]);
        }
    }

    private static void onSlotClicked(int slot, ItemStack itemStack, int mouseButton) {
        if (adminGUI == null) return;

        if (activePage == KICAdminPage.MAIN && SLOTS.contains(slot)) {
            handleUserClick(itemStack);
            return;
        }

        switch (activePage) {
            case MAIN: handleMainMenuClick(slot); break;
            case USER: handleUserMenuClick(slot); break;
        }
    }

    private static void handleUserClick(ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasTagCompound()) return;

        NBTTagCompound tag = itemStack.getTagCompound();
        if (!tag.hasKey("client-uuid")) return;

        String clientUUID = tag.getString("client-uuid");
        Client client = clients.get(clientUUID);
        if (client == null) return;
        currentClient = client;
        setupUserInv();
    }

    private static void handleMainMenuClick(int slot) {
        switch (slot) {
            case 49: GuiUtils.closeScreen(); break;
            case 45: if (currentPage > 1) { currentPage--; setupMainInv(); } break;
            case 53: if ((currentPage * SLOTS.size()) < clients.size()) { currentPage++; setupMainInv(); } break;
        }
    }

    private static void handleUserMenuClick(int slot) {
        switch (slot) {
            case 49: setupMainInv(); break;
            case 20: handleVisit(); break;
            case 22: handleTitle(); break;
            case 24: handleParty(); break;
        }
    }

    private static void handleTitle() {
        if (currentClient == null) return;
        KICLogger.info("Title on client: " + currentClient.getIgn());
    }

    private static void handleVisit() {
        if (currentClient == null) return;
        String cmd = "/visit " + currentClient.getName();
        GuiUtils.closeScreen();
        Multithreading.schedule(() -> sendCommand(cmd), 250, TimeUnit.MILLISECONDS);
    }

    private static void handleParty() {
        if (currentClient == null) return;
        String cmd = "/party " + currentClient.getName();
        sendCommand(cmd);
    }

    private static List<String> createCustomItemLore(Client client) {
        List<String> lore = new ArrayList<>();

        lore.add("");
        lore.add("§6Roles: §b" + String.join(", ", client.getRoles()));
        lore.add("§6Requests: §b" + parseToShorthandNumber(client.getRequests()));
        lore.add("§6Playtime: §b" + formatElapsedTime((double) client.getPlaytime() / 1000, 0, 2));
        lore.add("§6IGN: §b" + client.getIgn());
        lore.add("§6Connected IGN: §b" + (isNullOrEmpty(client.getConnectedIgn()) ? "§cNo IGN" : client.getConnectedIgn()));
        lore.add("§6Mod Version: §b" + (isNullOrEmpty(client.getModVersion()) ? "§cNo Version" : client.getModVersion()));
        lore.add("§6Connected: §b" + timeSince(client.connectionTime));

        return lore;
    }

    private static ItemStack skullItem(Client client) {
        ItemStack skull = new ItemStack(Items.skull, 1, 3);
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagCompound display = new NBTTagCompound();

        display.setString("Name", "§r§a" + client.getIgn());
        tag.setTag("display", display);
        tag.setString("client-uuid", client.getOwner());

        tag.setTag("SkullOwner", new NBTTagString(client.getIgn()));

        skull.setTagCompound(tag);
        setItemLore(skull, createCustomItemLore(client));

        return skull;
    }

    private static JsonObject getJsonObject(JsonObject obj, String key) {
        return (obj.has(key) && obj.get(key).isJsonObject()) ? obj.getAsJsonObject(key) : null;
    }

    private static String getString(JsonObject obj, String key) {
        return (obj.has(key) && !obj.get(key).isJsonNull()) ? obj.get(key).getAsString() : null;
    }

    private static int getInt(JsonObject obj, String key, int defaultValue) {
        return (obj.has(key) && !obj.get(key).isJsonNull()) ? obj.get(key).getAsInt() : defaultValue;
    }

    private static long getLong(JsonObject obj, String key, long defaultValue) {
        return (obj.has(key) && !obj.get(key).isJsonNull()) ? obj.get(key).getAsLong() : defaultValue;
    }

    private static List<String> getStringList(JsonObject obj, String key) {
        List<String> list = new ArrayList<>();
        if (obj.has(key) && obj.get(key).isJsonArray()) {
            obj.getAsJsonArray(key).forEach(element -> list.add(element.getAsString()));
        }
        return list;
    }

    private static class Client {
        private final String ign;
        private final String owner;
        private final String discord;
        private final String connectedIgn;
        private final String modVersion;
        private final List<String> roles;
        private final int requests;
        private final long playtime;
        private final long connectionTime;
        private ItemStack skull;

        public Client(String ign, String owner, String discord, String connectedIgn, String modVersion, List<String> roles, int requests, long playtime, long connectionTime) {
            this.ign = ign;
            this.owner = owner;
            this.discord = discord;
            this.connectedIgn = connectedIgn;
            this.modVersion = modVersion;
            this.roles = roles;
            this.requests = requests;
            this.playtime = playtime;
            this.connectionTime = connectionTime;
        }

        public String getIgn() {
            return ign;
        }

        public String getOwner() {
            return owner;
        }

        public String getDiscord() {
            return discord;
        }

        public String getConnectedIgn() {
            return connectedIgn;
        }

        public String getModVersion() {
            return modVersion;
        }

        public List<String> getRoles() {
            return roles;
        }

        public int getRequests() {
            return requests;
        }

        public long getPlaytime() {
            return playtime;
        }

        public long getConnectionTime() {
            return connectionTime;
        }

        public void setSkull(ItemStack skull) {
            this.skull = skull;
        }

        public ItemStack getSkull() {
            return skull;
        }

        public String getName() {
            return StringUtils.isNullOrEmpty(currentClient.getConnectedIgn()) ? currentClient.getIgn() : currentClient.getConnectedIgn();
        }
    }

    private enum KICAdminPage {
        MAIN,
        USER
    }
}
