package someoneok.kic.modules.crimson;

import cc.polyfrost.oneconfig.utils.IOUtils;
import cc.polyfrost.oneconfig.utils.JsonUtils;
import cc.polyfrost.oneconfig.utils.Multithreading;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import someoneok.kic.KIC;
import someoneok.kic.config.KICConfig;
import someoneok.kic.events.GuiContainerEvent;
import someoneok.kic.models.APIException;
import someoneok.kic.models.NEUCompatibility;
import someoneok.kic.models.crimson.BazaarItem;
import someoneok.kic.models.crimson.BazaarItemValue;
import someoneok.kic.models.request.Request;
import someoneok.kic.modules.misc.ButtonManager;
import someoneok.kic.utils.ApiUtils;
import someoneok.kic.utils.NetworkUtils;
import someoneok.kic.utils.RenderUtils;
import someoneok.kic.utils.dev.KICLogger;
import someoneok.kic.utils.overlay.InteractiveOverlay;
import someoneok.kic.utils.overlay.OverlayManager;
import someoneok.kic.utils.overlay.OverlaySegment;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static someoneok.kic.KIC.KICPrefix;
import static someoneok.kic.KIC.mc;
import static someoneok.kic.utils.ApiUtils.apiHost;
import static someoneok.kic.utils.ChatUtils.sendMessageToPlayer;
import static someoneok.kic.utils.ItemUtils.getItemLore;
import static someoneok.kic.utils.LocationUtils.onSkyblock;
import static someoneok.kic.utils.StringUtils.parseToShorthandNumber;
import static someoneok.kic.utils.StringUtils.removeFormatting;

public class HuntingBoxValue {
    private static final Set<Integer> VALID_SLOTS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    )));
    private static final Pattern OWNED_SHARDS_PATTERN =
            Pattern.compile("Owned:.*?(\\d{1,3}(?:,\\d{3})*|\\d+)\\s+Shards?");
    private final int SHARDS_PER_PAGE = 18;
    private static final long CACHE_EXPIRY = 600000;
    public static boolean shouldRender = false;

    private static final Map<String, BazaarItemValue> shardCache = new HashMap<>();
    private final Map<BazaarItem, Slot> shards = new HashMap<>();

    private Slot activeSlot = null;
    private int ticks = 0;
    private boolean useBuyPrice = true;
    private int sortField; // 0 = Price, 1 = Owned, 2 = Slot
    private boolean sortAscending; // true = Ascending, false = Descending
    private int scrollIndex = 0;
    private boolean overlayModified = false;

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (!shouldProcess(event)) return;
        ticks = 0;

        if (!(mc.currentScreen instanceof GuiChest)) {
            resetState();
            return;
        }

        GuiChest guiChest = (GuiChest) mc.currentScreen;
        ContainerChest container = (guiChest.inventorySlots instanceof ContainerChest)
                ? (ContainerChest) guiChest.inventorySlots
                : null;

        if (container == null || !"Hunting Box".equals(container.getLowerChestInventory().getName())) {
            resetState();
            return;
        }

        shouldRender = true;
        processInventory(container);
    }

    private void processInventory(ContainerChest container) {
        if (!ButtonManager.isChecked("huntingBoxValue")) return;
        Map<BazaarItem, Slot> newShards = new HashMap<>();
        boolean needsCacheUpdate = false;

        for (Slot slot : container.inventorySlots) {
            if (!slot.getHasStack() || !VALID_SLOTS.contains(slot.slotNumber)) continue;
            ItemStack item = slot.getStack();
            if (!item.hasTagCompound()) continue;

            List<String> lore = getItemLore(item);
            if (lore.isEmpty()) continue;

            int count = extractOwnedShards(lore);
            if (count == 0) continue;

            String displayName = item.getDisplayName();
            String itemId = "SHARD_" + removeFormatting(displayName).toUpperCase().replace(" ", "_");
            BazaarItem bzItem = new BazaarItem(itemId, displayName, count);

            newShards.put(bzItem, slot);
            needsCacheUpdate |= addShard(new BazaarItemValue(bzItem));
        }

        if (!shards.equals(newShards)) {
            shards.clear();
            shards.putAll(newShards);
            updateCacheOrOverlay(needsCacheUpdate);
        } else if (needsCacheUpdate) {
            updateShardCache(this::updateOverlay);
        }
    }

    private boolean addShard(BazaarItemValue item) {
        BazaarItemValue existing = shardCache.get(item.getItemId());
        if (existing == null) {
            shardCache.put(item.getItemId(), item);
            return true;
        }

        long now = System.currentTimeMillis();
        boolean isFresh = now - existing.getTimestamp() < CACHE_EXPIRY;

        if (!existing.isCached() && !existing.isFetching()) return true;
        if (!isFresh && !existing.isFetching()) {
            existing.setTimestamp(now);
            existing.setCached(false);
            existing.setFetching(false);
            return true;
        }

        return false;
    }

    private int extractOwnedShards(List<String> lore) {
        for (String line : lore) {
            if (!line.contains("Owned")) continue;

            Matcher matcher = OWNED_SHARDS_PATTERN.matcher(line);
            if (matcher.find()) {
                String number = matcher.group(1).replace(",", "");
                try {
                    return Integer.parseInt(number);
                } catch (Exception ignored) {
                    return 0;
                }
            }
        }
        return 0;
    }

    private void updateCacheOrOverlay(boolean cacheUpdateNeeded) {
        if (cacheUpdateNeeded) {
            updateShardCache(this::updateOverlay);
        } else {
            updateOverlay();
        }
    }

    private void updateOverlay() {
        InteractiveOverlay overlay = (InteractiveOverlay) OverlayManager.getOverlay("HuntingBoxValue");
        overlay.setRenderCondition(() -> shouldRender && !shards.isEmpty() && ButtonManager.isChecked("huntingBoxValue"));

        List<Map.Entry<BazaarItem, Slot>> entries  = getSortedEntries();
        List<OverlaySegment> segments = new ArrayList<>();

        int total = shards.size();
        int below = Math.max(0, total - (scrollIndex + SHARDS_PER_PAGE));
        int above = scrollIndex;

        segments.add(new OverlaySegment(
                String.format("%s%s §a§lHunting Box Value",above > 0 ? "§e↑ " + above + " " : "", KICPrefix),
                true, this::scrollUp, this::scrollDown
        ));

        for (Map.Entry<BazaarItem, Slot> entry : entries) {
            BazaarItem item = entry.getKey();
            Slot slot = entry.getValue();
            BazaarItemValue val = shardCache.get(item.getItemId());
            if (val == null) continue;

            long totalPrice = val.getPrice(useBuyPrice) * item.getCount();
            String text = String.format("\n§r§6%s §7| §r%s §cx%d",
                    parseToShorthandNumber(totalPrice), val.getName(), item.getCount());

            if (ApiUtils.isPrivilegedUser()) {
                segments.add(new OverlaySegment(text, true,
                        () -> IOUtils.copyStringToClipboard(val.toString()),
                        true,
                        () -> activeSlot = slot,
                        () -> activeSlot = null,
                        true,
                        this::scrollUp,
                        this::scrollDown
                ));
            } else {
                segments.add(new OverlaySegment(text, true,
                        () -> activeSlot = slot,
                        () -> activeSlot = null,
                        true,
                        this::scrollUp,
                        this::scrollDown
                ));
            }
        }

        double totalValue = shards.keySet().stream()
                .mapToDouble(item -> {
                    BazaarItemValue val = shardCache.get(item.getItemId());
                    return (val != null) ? val.getPrice(useBuyPrice) * item.getCount() : 0;
                }).sum();

        segments.add(new OverlaySegment(
                String.format("\n%s§7Total: §6%s", below > 0 ? "§e↓ " + below + " " : "", parseToShorthandNumber(totalValue)),
                true, this::scrollUp, this::scrollDown
        ));

        segments.add(new OverlaySegment("\n§7-=-=-=-=-=-=-=-=-=-=-"));
        segments.add(new OverlaySegment(
                String.format("\n§7- §aPrice Type §7[%sSell Offer§7/%sInsta Sell§7]", useBuyPrice ? "§d" : "§8", useBuyPrice ? "§8" : "§d"),
                this::changeValueType, true
        ));

        segments.add(new OverlaySegment(
                String.format("\n§7- §aSort By §7[%sPrice§7/%sOwned§7/%sSlot§7]",
                        sortField == 0 ? "§d" : "§8",
                        sortField == 1 ? "§d" : "§8",
                        sortField == 2 ? "§d" : "§8"),
                this::changeSorting, true
        ));

        segments.add(new OverlaySegment(
                String.format("\n§7- §aSort Order §7[%sAscending ▲§7/%sDescending ▼§7]",
                        sortAscending ? "§d" : "§8",
                        sortAscending ? "§8" : "§d"),
                this::changeOrder, true
        ));

        overlay.setSegments(segments);
        overlayModified = true;
    }

    private List<Map.Entry<BazaarItem, Slot>> getSortedEntries() {
        return shards.entrySet().stream()
                .sorted((e1, e2) -> {
                    BazaarItem i1 = e1.getKey(), i2 = e2.getKey();
                    double p1 = Optional.ofNullable(shardCache.get(i1.getItemId()))
                            .map(v -> v.getPrice(useBuyPrice) * i1.getCount())
                            .orElse(0L);
                    double p2 = Optional.ofNullable(shardCache.get(i2.getItemId()))
                            .map(v -> v.getPrice(useBuyPrice) * i2.getCount())
                            .orElse(0L);
                    int c1 = i1.getCount(), c2 = i2.getCount();
                    int s1 = e1.getValue().slotNumber, s2 = e2.getValue().slotNumber;

                    int cmp;
                    switch (sortField) {
                        case 0: cmp = Double.compare(p1, p2); break;
                        case 1: cmp = Integer.compare(c1, c2); break;
                        case 2: cmp = Integer.compare(s1, s2); break;
                        default: cmp = 0; break;
                    }

                    return sortAscending ? cmp : -cmp;
                })
                .skip(scrollIndex)
                .limit(SHARDS_PER_PAGE)
                .collect(Collectors.toList());
    }

    private void scrollUp() {
        if (scrollIndex > 0) {
            scrollIndex--;
            updateOverlay();
        }
    }

    private void scrollDown() {
        if (scrollIndex + SHARDS_PER_PAGE < shards.size()) {
            scrollIndex++;
            updateOverlay();
        }
    }

    private void changeValueType() {
        useBuyPrice = !useBuyPrice;
        scrollIndex = 0;
        updateOverlay();
    }

    private void changeSorting() {
        sortField = (sortField + 1) % 3;
        scrollIndex = 0;
        updateOverlay();
    }

    private void changeOrder() {
        sortAscending = !sortAscending;
        scrollIndex = 0;
        updateOverlay();
    }

    private void resetState() {
        shouldRender = false;
    }

    private boolean shouldProcess(TickEvent.ClientTickEvent event) {
        return ApiUtils.isVerified()
                && KICConfig.crimsonHuntingBoxValue
                && onSkyblock
                && mc.theWorld != null && mc.thePlayer != null
                && event.phase == TickEvent.Phase.END
                && (++ticks % 10 == 0);
    }

    private boolean shouldNotRender() {
        return !ApiUtils.isVerified()
                || !KICConfig.crimsonHuntingBoxValue
                || !ButtonManager.isChecked("huntingBoxValue")
                || !onSkyblock
                || !shouldRender
                || NEUCompatibility.isStorageMenuActive();
    }

    @SubscribeEvent
    public void onGuiClose(GuiContainerEvent.CloseWindowEvent event) {
        shards.clear();
        activeSlot = null;
        scrollIndex = 0;
        resetState();
        if (overlayModified) {
            OverlayManager.getOverlay("HuntingBoxValue").updateText("");
            overlayModified = false;
        }
    }

    @SubscribeEvent
    public void onBackgroundDrawn(GuiScreenEvent.BackgroundDrawnEvent e) {
        if (!shouldNotRender() && e.gui instanceof GuiChest && activeSlot != null) {
            RenderUtils.highlight(new Color(0, 255, 13, 175), (GuiChest) e.gui, activeSlot);
        }
    }

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Unload event) {
        cleanupCaches();
    }

    private void cleanupCaches() {
        long now = System.currentTimeMillis();
        shardCache.entrySet().removeIf(e -> now - e.getValue().getTimestamp() > CACHE_EXPIRY);
    }

    public static void forceCleanupCaches(long timestamp) {
        shardCache.entrySet().removeIf(e -> e.getValue().getTimestamp() < timestamp);
    }

    private void updateShardCache(Runnable callback) {
        if (!ApiUtils.isVerified()) return;

        List<BazaarItemValue> toFetch = shardCache.values().stream()
                .filter(v -> !v.isFetching() && !v.isCached())
                .collect(Collectors.toList());

        if (toFetch.isEmpty()) {
            if (callback != null) callback.run();
            return;
        }

        KICLogger.info("Updating shard cache from api");
        List<Request> requestItems = toFetch.stream()
                .map(BazaarItemValue::mapToRequest)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        toFetch.forEach(item -> item.setFetching(true));
        String requestBody = KIC.GSON.toJson(requestItems);

        Multithreading.runAsync(() -> {
            try {
                JsonArray response = JsonUtils.parseString(NetworkUtils.sendPostRequest(apiHost() + "/crimson/prices", true, requestBody)).getAsJsonArray();

                long time = System.currentTimeMillis();
                if (response != null) {
                    for (JsonElement el : response) {
                        JsonObject obj = el.getAsJsonObject();
                        if (!"BAZAAR".equals(obj.get("type").getAsString())) continue;

                        String itemId = obj.get("itemId").getAsString();
                        BazaarItemValue val = shardCache.get(itemId);
                        if (val != null) {
                            val.setPrice(obj.get("buyPrice").getAsLong(), obj.get("sellPrice").getAsLong());
                            val.setCached(true);
                            val.setFetching(false);
                            val.setTimestamp(time);
                        }
                    }
                }
            } catch (APIException e) {
                sendMessageToPlayer(String.format("%s §c%s", KICPrefix, e.getMessage()));
                toFetch.forEach(item -> {
                    item.setFetching(false);
                    item.setCached(false);
                });
            }
            if (callback != null) callback.run();
        });
    }
}
