package someoneok.kic.models.kuudra.chest;

import cc.polyfrost.oneconfig.utils.JsonUtils;
import cc.polyfrost.oneconfig.utils.Multithreading;
import com.google.gson.JsonArray;
import net.minecraft.item.ItemStack;
import someoneok.kic.KIC;
import someoneok.kic.config.pages.KuudraProfitCalculatorOptions;
import someoneok.kic.models.APIException;
import someoneok.kic.models.crimson.*;
import someoneok.kic.models.request.Request;
import someoneok.kic.utils.ApiUtils;
import someoneok.kic.utils.ItemUtils;
import someoneok.kic.utils.NetworkUtils;
import someoneok.kic.utils.dev.KICLogger;
import someoneok.kic.utils.kuudra.KuudraChestUtils;
import someoneok.kic.utils.kuudra.KuudraValueCache;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static someoneok.kic.KIC.KICPrefix;
import static someoneok.kic.utils.ApiUtils.apiHost;
import static someoneok.kic.utils.ChatUtils.sendMessageToPlayer;
import static someoneok.kic.utils.StringUtils.isNullOrEmpty;
import static someoneok.kic.utils.kuudra.KuudraChestUtils.applyPetBoostIfEnabled;

public class KuudraChest {
    private static final AtomicInteger NEXT_ID = new AtomicInteger(1);

    private final int id;

    private final KuudraChestType type;
    private KuudraKey keyNeeded;
    private int essence;
    private boolean rerolled;
    private boolean shardRerolled;
    private boolean bought;
    private final AtomicBoolean addedToTracker;
    
    private final Map<String, BazaarItem> bazaarItems;
    private final Map<String, AuctionItem> auctionItems;
    private final Set<String> shards;
    private final Set<String> valuables;

    private final AtomicBoolean fetchInFlight;
    private final AtomicReference<Runnable> latestCallback;

    private volatile boolean profitCalculated;
    private volatile long totalValue;

    public KuudraChest(KuudraChestType type) {
        this.id = NEXT_ID.getAndIncrement();
        this.type = type;
        this.keyNeeded = null;
        this.essence = 0;
        this.rerolled = false;
        this.shardRerolled = false;
        this.bought = false;
        this.addedToTracker = new AtomicBoolean(false);
        this.bazaarItems = new HashMap<>();
        this.auctionItems = new HashMap<>();
        this.shards = new HashSet<>();
        this.valuables = new HashSet<>();
        this.fetchInFlight = new AtomicBoolean(false);
        this.latestCallback = new AtomicReference<>(null);
        this.profitCalculated = false;
        this.totalValue = 0;
    }

    public void reset() {
        this.essence = 0;
        this.bazaarItems.clear();
        this.auctionItems.clear();
        this.shards.clear();
        this.valuables.clear();
        this.profitCalculated = false;
        this.totalValue = 0;
        this.rerolled = false;
        this.shardRerolled = false;
        this.bought = false;
        this.addedToTracker.set(false);
    }

    public int getId() { return id; }
    public KuudraChestType getType() { return type; }
    public KuudraKey getKeyNeeded() { return keyNeeded; }
    public void setKeyNeeded(KuudraKey key) { this.keyNeeded = key; }
    public int getEssenceRaw() { return essence; }
    public void addEssence(int amount) { this.essence += amount; }
    public Map<String, BazaarItem> getBazaarItems() { return bazaarItems; }
    public Map<String, AuctionItem> getAuctionItems() { return auctionItems; }
    public boolean isProfitCalculated() { return profitCalculated; }
    public boolean hasItems() { return !bazaarItems.isEmpty() || !auctionItems.isEmpty(); }
    public String getChestName() { return type.getDisplayText(); }
    public void setRerolled(boolean rerolled) { this.rerolled = rerolled; }
    public boolean isRerolled() { return rerolled; }
    public void setShardRerolled(boolean shardRerolled) { this.shardRerolled = shardRerolled; }
    public boolean isShardRerolled() { return shardRerolled; }
    public void setBought(boolean bought) { this.bought = bought; }
    public boolean isBought() { return bought; }
    public boolean markAddedToTracker() { return !addedToTracker.getAndSet(true); }
    public boolean isAddedToTracker() { return addedToTracker.get(); }
    public long getRawTotalValue() { return totalValue; }
    public boolean hasShards() { return !shards.isEmpty(); }
    public long getTotalValue() {
        long total = this.totalValue;
        if (rerolled) total -= KuudraValueCache.kismet().getValue();
        if (shardRerolled) total -= KuudraValueCache.wheelOfFate().getValue();
        return total;
    }

    public int getTeeth() {
        BazaarItem teeth = bazaarItems.get("KUUDRA_TEETH");
        return teeth == null ? 0 : teeth.getCount();
    }

    public int getEssence() {
        if (KuudraProfitCalculatorOptions.ignoreEssence) return 0;
        return applyPetBoostIfEnabled(this.essence);
    }

    // Format: type;itemId;lbPrice/buyPrice;avgPrice/sellPrice
    public Set<String> getValuables() {
        Set<String> result = new HashSet<>();
        if (valuables.isEmpty()) return result;

        for (String valuable : valuables) {
            if (bazaarItems.containsKey(valuable)) {
                BazaarItemValue value = KuudraValueCache.getBazaar(valuable);
                if (value == null) return null;

                result.add(String.format(
                        "BAZAAR;%s;%d;%d",
                        value.getItemId(),
                        value.getPrice(true),
                        value.getPrice(false)
                ));
            }

            if (auctionItems.containsKey(valuable)) {
                AuctionItemValue value = KuudraValueCache.getAuction(valuable);
                if (value == null) return null;

                result.add(String.format(
                        "AUCTION;%s;%d;%d",
                        value.getItemId(),
                        value.getPrice(true),
                        value.getPrice(false)
                ));
            }
        }

        return result;
    }

    public void addItem(ItemStack item) {
        ItemKind kind = KuudraChestUtils.classify(item);
        if (kind == ItemKind.NONE) return;

        String itemId = ItemUtils.getItemId(item);
        if (itemId != null && KuudraChestUtils.VALUABLES.contains(itemId)) {
            valuables.add(itemId);
        }
        int count = item.stackSize;
        String itemName = item.getDisplayName() == null ? "" : item.getDisplayName();
        switch (kind) {
            case BAZAAR_ENCHANT:
                String[] enc = ItemUtils.getFirstEnchant(item);
                if (enc == null) return;
                String encId = enc[0];
                String encName = enc[1];
                if (isNullOrEmpty(encId) || isNullOrEmpty(encName)) return;
                if (KuudraChestUtils.VALUABLES.contains(encId)) {
                    valuables.add(encId);
                }
                BazaarItem encItem = bazaarItems.get(encId);
                if (encItem == null) bazaarItems.put(encId, new BazaarItem(encId, encName, count));
                else encItem.addCount(count);
                KuudraValueCache.ensureBazaar(encId, encName);
                break;

            case BAZAAR:
                if (isNullOrEmpty(itemId)) return;
                BazaarItem bzItem = bazaarItems.get(itemId);
                if (bzItem == null) bazaarItems.put(itemId, new BazaarItem(itemId, itemName, count));
                else bzItem.addCount(count);
                KuudraValueCache.ensureBazaar(itemId, itemName);
                break;

            case AUCTION:
                String ahUuid = ItemUtils.getItemUuid(item);
                if (isNullOrEmpty(itemId) || isNullOrEmpty(ahUuid)) return;
                int stars     = ItemUtils.getItemStars(item);
                auctionItems.put(itemId, new AuctionItem(itemId, itemName, ahUuid, stars));
                KuudraValueCache.ensureAuction(itemId, itemName);
                break;
        }
    }

    public void addShard(String name, String itemId, int count) {
        BazaarItem existing = bazaarItems.get(itemId);
        shards.add(itemId);
        if (existing == null) bazaarItems.put(itemId, new BazaarItem(itemId, name, count));
        else existing.addCount(count);
        KuudraValueCache.ensureBazaar(itemId, name);
    }

    public void addAuctionItem(AuctionItem auctionItem) {
        String itemId = auctionItem.getItemId();
        auctionItems.put(itemId, auctionItem);
        KuudraValueCache.ensureAuction(itemId, auctionItem.getName());
    }

    public void updateValues(Runnable callback) {
        if (!ApiUtils.isVerified()) return;
        if (callback != null) latestCallback.set(callback);

        if (!fetchInFlight.compareAndSet(false, true)) {
            KICLogger.info("Fetch already in flight; coalescing call.");
            return;
        }

        KICLogger.info("Updating values (cache-based)");

        final KuudraValueCache.FetchPlan plan = KuudraValueCache.buildRequestsFor(this);
        final List<Request> reqs = plan.requests;

        if (reqs.isEmpty()) {
            recomputeTotal();
            profitCalculated = true;
            fetchInFlight.set(false);
            runAndClearLatestCallback();
            return;
        }

        final String body = KIC.GSON.toJson(reqs);
        KICLogger.info("POST /crimson/prices body: " + body);

        Multithreading.runAsync(() -> {
            try {
                JsonArray resp = JsonUtils.parseString(
                        NetworkUtils.sendPostRequest(apiHost() + "/crimson/prices", true, body)
                ).getAsJsonArray();

                if (resp != null) KuudraValueCache.applyApiResponse(resp);

                recomputeTotal();
            } catch (APIException ex) {
                KICLogger.error("API error fetching values: " + ex.getMessage());
                sendMessageToPlayer(String.format("%s §c%s", KICPrefix, ex.getMessage()));
                for (Value v : plan.touched) { v.setFetching(false); v.setCached(false); }
            } finally {
                profitCalculated = true;
                fetchInFlight.set(false);
                runAndClearLatestCallback();
            }
        });
    }

    private void recomputeTotal() {
        this.totalValue = KuudraValueCache.computeTotal(this, false);
        KICLogger.info("Total value updated (cache): " + totalValue);
    }

    private void runAndClearLatestCallback() {
        Runnable r = latestCallback.getAndSet(null);
        if (r != null) {
            try { r.run(); } catch (Throwable t) {
                KICLogger.info("Callback error: " + t.getMessage());
            }
        }
    }
}
