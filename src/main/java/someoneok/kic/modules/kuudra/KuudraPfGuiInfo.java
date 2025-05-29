package someoneok.kic.modules.kuudra;

import cc.polyfrost.oneconfig.utils.JsonUtils;
import cc.polyfrost.oneconfig.utils.Multithreading;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import someoneok.kic.KIC;
import someoneok.kic.config.KICConfig;
import someoneok.kic.events.GuiContainerEvent;
import someoneok.kic.models.APIException;
import someoneok.kic.models.Island;
import someoneok.kic.models.overlay.OverlayExamples;
import someoneok.kic.modules.misc.ButtonManager;
import someoneok.kic.utils.ApiUtils;
import someoneok.kic.utils.LocationUtils;
import someoneok.kic.utils.NetworkUtils;
import someoneok.kic.utils.dev.KICLogger;
import someoneok.kic.utils.overlay.OverlayManager;

import java.util.*;
import java.util.stream.Collectors;

import static someoneok.kic.modules.kuudra.KuudraUserInfo.makeMessage;
import static someoneok.kic.utils.ApiUtils.hasPremium;
import static someoneok.kic.utils.ItemUtils.getItemLore;
import static someoneok.kic.utils.PlayerUtils.getPlayerName;
import static someoneok.kic.utils.StringUtils.*;

public class KuudraPfGuiInfo {
    private static final Set<Integer> VALID_SLOTS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
    )));
    private static final long CACHE_EXPIRATION_MS = 30 * 60 * 1000;
    private static final long CACHED_TIME_MS = 15 * 60 * 1000;
    private static final long ERROR_RETRY_DELAY_MS = 30 * 1000;
    private static final String[] OVERLAY_NAMES = {"PFStatsPlayer1", "PFStatsPlayer2", "PFStatsPlayer3"};

    private final List<String> hoveredLore = new ArrayList<>();
    private final List<String> hoveredParty = new ArrayList<>();
    private final Map<String, PlayerDataCache> playerCache = new HashMap<>();

    public static boolean shouldRender = false;
    private boolean renderedExample = false;

    @SubscribeEvent
    public void onDrawGui(GuiContainerEvent.ForegroundDrawnEvent event) {
        if (!(event.getContainer() instanceof ContainerChest)) return;

        shouldRender = KICConfig.partyFinderGuiStats && hasPremium() &&
                LocationUtils.currentIsland == Island.CRIMSON_ISLE &&
                "Forgotten Skull".equals(LocationUtils.subArea) &&
                "Party Finder".equals(event.getChestName()) &&
                ApiUtils.isVerified();
        if (!shouldRender) return;

        if (ButtonManager.isChecked("partyFinderGuiStatsExample")) {
            if (!renderedExample) {
                reset();
                renderExample();
                renderedExample = true;
            }
            return;
        }

        Slot hoveredSlot = event.getGui().getSlotUnderMouse();
        if (hoveredSlot == null || !hoveredSlot.getHasStack() || !VALID_SLOTS.contains(hoveredSlot.slotNumber)) {
            reset();
            return;
        }

        if (ButtonManager.isChecked("partyFinderGuiStats")) {
            ItemStack stack = hoveredSlot.getStack();
            List<String> lore = getItemLore(stack);
            if (!hoveredLore.equals(lore)) {
                reset();
                hoveredLore.addAll(lore);
                processLore();
                processPlayers();
            }
        }
    }

    @SubscribeEvent
    public void onGuiClose(GuiContainerEvent.CloseWindowEvent event) {
        reset();
        shouldRender = false;
        long cutoff = System.currentTimeMillis() - CACHE_EXPIRATION_MS;
        playerCache.entrySet().removeIf(entry -> entry.getValue().getTimestamp() < cutoff);
    }

    private void reset() {
        renderedExample = false;
        hoveredLore.clear();
        hoveredParty.clear();
        for (String name : OVERLAY_NAMES) {
            OverlayManager.getOverlay(name).updateText("");
        }
    }

    private void processLore() {
        int memberStartIndex = hoveredLore.indexOf("§f§7Members: ");
        if (memberStartIndex == -1) return;

        String myName = getPlayerName().toLowerCase();

        for (int i = memberStartIndex + 1; i <= memberStartIndex + 3 && i < hoveredLore.size(); i++) {
            String raw = hoveredLore.get(i);
            if (raw == null) continue;

            String player = removeFormatting(raw).trim();
            if (isNullOrEmpty(player) || player.equalsIgnoreCase("Empty")) continue;

            player = player.toLowerCase();
            if (player.contains(myName)) continue;

            int parenIndex = player.indexOf(" (");
            if (parenIndex != -1) player = player.substring(0, parenIndex).trim();

            hoveredParty.add(formatPlayerName(player));
        }
    }

    private void processPlayers() {
        Map<String, PlayerDataCache> playersToFetch = new HashMap<>();
        List<PlayerDataCache> cachedPlayers = new ArrayList<>();

        for (String player : hoveredParty) {
            String lower = player.toLowerCase();
            PlayerDataCache cache = playerCache.get(lower);

            if (cache != null) {
                if (cache.isCached()) {
                    cachedPlayers.add(cache);
                } else if (!cache.isFetching() && cache.canRetry()) {
                    cache.setFetching();
                    playersToFetch.put(lower, cache);
                } else {
                    cachedPlayers.add(cache);
                }
            } else {
                PlayerDataCache newCache = new PlayerDataCache(lower);
                newCache.setFetching();
                playerCache.put(lower, newCache);
                playersToFetch.put(lower, newCache);
            }
        }

        if (!playersToFetch.isEmpty()) {
            final List<String> currentHoverSnapshot = new ArrayList<>(hoveredParty);
            Set<String> fetching = playersToFetch.keySet();
            render(fetching.stream()
                    .sorted()
                    .map(f -> "§a" + f + ": Fetching...")
                    .collect(Collectors.toList()));

            Multithreading.runAsync(() -> {
                String requestBody = KIC.GSON.toJson(fetching);
                JsonArray playerInfos;
                try {
                    playerInfos = JsonUtils.parseString(NetworkUtils.sendPostRequest(
                            "https://api.sm0kez.com/premium/pf?type=KUUDRA", true, requestBody)).getAsJsonArray();
                } catch (APIException e) {
                    setErrored(playersToFetch);
                    KICLogger.info(e.getMessage());
                    return;
                }

                if (playerInfos == null || !playerInfos.isJsonArray() || playerInfos.size() == 0) {
                    setErrored(playersToFetch);
                    return;
                }

                List<PlayerDataCache> updated = new ArrayList<>();
                for (JsonElement element : playerInfos) {
                    if (!element.isJsonObject()) continue;

                    JsonObject info = element.getAsJsonObject();
                    ChatComponentText stats = makeMessage(info, true);
                    if (stats == null) continue;

                    String player = extractPlayer(info).toLowerCase();
                    PlayerDataCache cache = playersToFetch.get(player);
                    if (cache != null) {
                        cache.updateStats(stats.getUnformattedText());
                        updated.add(cache);
                    }
                }

                if (hoveredParty.equals(currentHoverSnapshot)) {
                    List<PlayerDataCache> fullList = new ArrayList<>(updated);
                    for (String name : hoveredParty) {
                        PlayerDataCache cached = playerCache.get(name.toLowerCase());
                        if (cached != null && !fullList.contains(cached)) {
                            fullList.add(cached);
                        }
                    }
                    render(fullList.stream()
                            .sorted(Comparator.comparing(PlayerDataCache::getUsername))
                            .map(PlayerDataCache::getStats)
                            .collect(Collectors.toList()));
                }
            });
        } else if (!cachedPlayers.isEmpty()) {
            render(cachedPlayers.stream()
                    .sorted(Comparator.comparing(PlayerDataCache::getUsername))
                    .map(PlayerDataCache::getStats)
                    .collect(Collectors.toList()));
        }
    }

    private void setErrored(Map<String, PlayerDataCache> playersToFetch) {
        playersToFetch.values().forEach(PlayerDataCache::errored);

        List<String> renderLines = hoveredParty.stream().map(name -> {
            String lower = name.toLowerCase();
            PlayerDataCache cache = playerCache.get(lower);
            if (cache == null) return "";
            if (cache.isErrored()) return "§c" + name + ": Errored...";
            if (cache.isFetching()) return "§a" + name + ": Fetching...";
            return cache.getStats();
        }).collect(Collectors.toList());

        render(renderLines);
    }

    private String extractPlayer(JsonObject info) {
        JsonObject playerInfo = info.has("playerInfo") && info.get("playerInfo").isJsonObject()
                ? info.getAsJsonObject("playerInfo")
                : null;
        return (playerInfo != null && playerInfo.has("username"))
                ? playerInfo.get("username").getAsString()
                : "unknown";
    }

    private void render(List<String> strings) {
        for (int i = 0; i < OVERLAY_NAMES.length; i++) {
            OverlayManager.getOverlay(OVERLAY_NAMES[i]).updateText(i < strings.size() ? strings.get(i) : "");
        }
    }

    private void renderExample() {
        OverlayManager.getOverlay("PFStatsPlayer1").updateText(OverlayExamples.EXAMPLE_PLAYER_1);
        OverlayManager.getOverlay("PFStatsPlayer2").updateText(OverlayExamples.EXAMPLE_PLAYER_2);
        OverlayManager.getOverlay("PFStatsPlayer3").updateText(OverlayExamples.EXAMPLE_PLAYER_3);
    }

    private static class PlayerDataCache {
        private String stats = "";
        private String username;
        private boolean cached = false;
        private boolean fetching = false;
        private boolean errored = false;
        private long timestamp = 0;
        private long lastError = 0;

        public PlayerDataCache(String username) {
            this.username = username;
        }

        public void errored() {
            this.errored = true;
            this.fetching = false;
            this.cached = false;
            this.lastError = System.currentTimeMillis();
        }

        public void updateStats(String stats) {
            this.stats = stats;
            this.cached = true;
            this.fetching = false;
            this.errored = false;
            this.timestamp = System.currentTimeMillis();
        }

        public boolean isCached() {
            return cached && (System.currentTimeMillis() - timestamp) < CACHED_TIME_MS;
        }

        public String getUsername() {
            return username;
        }

        public boolean isErrored() {
            return errored;
        }

        public boolean isFetching() {
            return fetching;
        }

        public boolean canRetry() {
            return errored && (System.currentTimeMillis() - lastError) > ERROR_RETRY_DELAY_MS;
        }

        public void setFetching() {
            this.fetching = true;
            this.errored = false;
        }

        public String getStats() {
            return stats;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}
