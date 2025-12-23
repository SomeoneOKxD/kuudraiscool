package someoneok.kic.utils.kuudra;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import someoneok.kic.config.pages.KuudraProfitCalculatorOptions;
import someoneok.kic.models.crimson.*;
import someoneok.kic.models.kuudra.CrimsonFaction;
import someoneok.kic.models.kuudra.chest.KuudraChest;
import someoneok.kic.models.kuudra.chest.KuudraKey;
import someoneok.kic.models.request.Request;
import someoneok.kic.modules.kuudra.KuudraProfitTracker;
import someoneok.kic.modules.kuudra.TapTracker;
import someoneok.kic.utils.data.DataManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static someoneok.kic.utils.ItemUtils.*;

public class KuudraValueCache {
    private static final long EXPIRES_MS = 15L * 60L * 1000L;

    private static final ConcurrentHashMap<String, BazaarItemValue> bazaar  = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, AuctionItemValue> auction = new ConcurrentHashMap<>();

    private static String K(KuudraKey key, String faction) { return key.name() + "|" + faction; }
    private static final class KeyEntry { long buy, sell, ts; volatile boolean fetching, cached; }
    private static final ConcurrentHashMap<String, KeyEntry> keys = new ConcurrentHashMap<>();

    public static BazaarItemValue getBazaar(String itemId) { return bazaar.get(itemId); }
    public static AuctionItemValue getAuction(String itemId) { return auction.get(itemId); }
    public static BazaarItemValue essence() { return ensureBazaar("ESSENCE_CRIMSON", "§dCrimson Essence"); }
    public static BazaarItemValue kismet() { return ensureBazaar("KISMET_FEATHER", "§9Kismet Feather"); }
    public static BazaarItemValue heavyPearl() { return ensureBazaar("HEAVY_PEARL", "§6Heavy Pearl"); }
    public static BazaarItemValue tap() { return ensureBazaar("TOXIC_ARROW_POISON", "§aToxic Arrow Poison"); }
    public static BazaarItemValue twap() { return ensureBazaar("TWILIGHT_ARROW_POISON", "§aTwilight Arrow Poison"); }
    public static AuctionItemValue wheelOfFate(){ return ensureAuction("WHEEL_OF_FATE", "§9Wheel of Fate"); }
    public static void invalidateBazaarCache() { bazaar.values().forEach(v -> v.setCached(false)); }
    public static void invalidateAuctionCache() { auction.values().forEach(v -> v.setCached(false)); }
    public static void invalidateKeyCache() { keys.values().forEach(v -> v.cached = false); }

    public static BazaarItemValue ensureBazaar(String itemId, String displayName) {
        final String name = (displayName == null) ? "" : displayName;
        return bazaar.computeIfAbsent(itemId, id -> new BazaarItemValue(new BazaarItem(id, name)));
    }

    public static AuctionItemValue ensureAuction(String itemId, String displayName) {
        final String name = (displayName == null) ? "" : displayName;
        return auction.computeIfAbsent(itemId, id -> new AuctionItemValue(new AuctionItem(id, name, id, 0)));
    }

    private static boolean isFresh(Value v) {
        if (!v.isCached()) return false;
        long age = System.currentTimeMillis() - v.getTimestamp();
        return age >= 0 && age <= EXPIRES_MS;
    }

    private static boolean isFreshKey(KeyEntry e) {
        if (e == null || !e.cached) return false;
        long age = System.currentTimeMillis() - e.ts;
        return age >= 0 && age <= EXPIRES_MS;
    }

    public static void pruneExpired() {
        long now = System.currentTimeMillis();
        bazaar.entrySet().removeIf(e -> e.getValue().isCached() && (now - e.getValue().getTimestamp()) > EXPIRES_MS);
        auction.entrySet().removeIf(e -> e.getValue().isCached() && (now - e.getValue().getTimestamp()) > EXPIRES_MS);
        keys.entrySet().removeIf(en -> en.getValue().cached && (now - en.getValue().ts) > EXPIRES_MS);
    }

    public static FetchPlan buildRequestsFor(KuudraChest chest) {
        List<Request> reqs = new ArrayList<>();
        List<Value> touched = new ArrayList<>();

        for (Map.Entry<String, BazaarItem> e : chest.getBazaarItems().entrySet()) {
            BazaarItemValue v = ensureBazaar(e.getKey(), e.getValue().getName());
            if (!v.isFetching() && !isFresh(v)) {
                reqs.add(v.mapToRequest());
                v.setFetching(true);
                touched.add(v);
            }
        }

        for (Map.Entry<String, AuctionItem> e : chest.getAuctionItems().entrySet()) {
            AuctionItemValue v = ensureAuction(e.getKey(), e.getValue().getName());
            if (!v.isFetching() && !isFresh(v)) {
                reqs.add(v.mapToRequest());
                v.setFetching(true);
                touched.add(v);
            }
        }

        if (chest.getEssenceRaw() > 0 && !essence().isFetching() && !isFresh(essence())) {
            reqs.add(essence().mapToRequest());
            essence().setFetching(true);
            touched.add(essence());
        }
        if (!kismet().isFetching() && !isFresh(kismet())) {
            reqs.add(kismet().mapToRequest());
            kismet().setFetching(true);
            touched.add(kismet());
        }
        if (!heavyPearl().isFetching() && !isFresh(heavyPearl())) {
            reqs.add(heavyPearl().mapToRequest());
            heavyPearl().setFetching(true);
            touched.add(heavyPearl());
        }
        if (TapTracker.getTotalUsedTAP() > 0 && !tap().isFetching() && !isFresh(tap())) {
            reqs.add(tap().mapToRequest());
            tap().setFetching(true);
            touched.add(tap());
        }
        if (TapTracker.getTotalUsedTWAP() > 0 && !twap().isFetching() && !isFresh(twap())) {
            reqs.add(twap().mapToRequest());
            twap().setFetching(true);
            touched.add(twap());
        }
        if (!wheelOfFate().isFetching() && !isFresh(wheelOfFate())) {
            reqs.add(wheelOfFate().mapToRequest());
            wheelOfFate().setFetching(true);
            touched.add(wheelOfFate());
        }

        if (chest.getKeyNeeded() != null) {
            Request kr = queueKeyIfNeeded(chest.getKeyNeeded());
            if (kr != null) reqs.add(kr);
        }

        return new FetchPlan(reqs, touched);
    }

    public static Request queueKeyIfNeeded(KuudraKey key) {
        CrimsonFaction faction = DataManager.getUserData().getFaction();
        if (faction == null || faction == CrimsonFaction.NONE) faction = CrimsonFaction.MAGE;
        String k = K(key, faction.name());
        KeyEntry e = keys.computeIfAbsent(k, __ -> new KeyEntry());
        if (!e.fetching && !isFreshKey(e)) {
            e.fetching = true;
            return key.getRequest(faction);
        }
        return null;
    }

    public static long getKeyPrice(KuudraKey key) {
        CrimsonFaction faction = DataManager.getUserData().getFaction();
        if (faction == null || faction == CrimsonFaction.NONE) faction = CrimsonFaction.MAGE;
        KeyEntry e = keys.get(K(key, faction.name()));
        long buy = (e != null) ? e.buy : key.getPrice();
        long sell= (e != null) ? e.sell: key.getPrice();
        return (KuudraProfitCalculatorOptions.keyPriceType == 1 ? buy : sell);
    }

    public static void applyKeyObject(JsonObject obj) {
        if (!"KEY".equals(obj.get("type").getAsString())) return;
        KuudraKey key = KuudraKey.valueOf(obj.get("tier").getAsString());
        String faction = obj.get("faction").getAsString();
        long buy = obj.get("buyPrice").getAsLong();
        long sell= obj.get("sellPrice").getAsLong();

        KeyEntry e = keys.computeIfAbsent(K(key, faction), __ -> new KeyEntry());
        e.buy = buy; e.sell = sell; e.ts = System.currentTimeMillis();
        e.fetching = false; e.cached = true;

        key.setPrice(buy, sell);
        KuudraProfitTracker.updateKeyPrice(key);
    }

    public static void applyApiResponse(JsonArray response) {
        if (response == null) return;

        for (JsonElement el : response) {
            JsonObject obj = el.getAsJsonObject();
            String type = obj.get("type").getAsString();

            switch (type) {
                case "BAZAAR": {
                    String itemId = obj.get("itemId").getAsString();
                    BazaarItemValue v = bazaar.get(itemId);
                    if (v == null) break;
                    long buy  = obj.get("buyPrice").getAsLong();
                    long sell = obj.get("sellPrice").getAsLong();
                    v.setPrice(buy, sell);
                    v.setFetching(false); v.setCached(true); v.setTimestamp(System.currentTimeMillis());

                    if ("ESSENCE_CRIMSON".equals(itemId))      KuudraProfitTracker.updateEssencePrice(v.getSingleValue());
                    else if ("KISMET_FEATHER".equals(itemId))  KuudraProfitTracker.updateKismetPrice(v.getSingleValue());
                    else if ("KUUDRA_TEETH".equals(itemId))    KuudraProfitTracker.updateTeethPrice(v.getSingleValue());
                    else if ("TOXIC_ARROW_POISON".equals(itemId)) KuudraProfitTracker.updateTapPrice(v.getSingleValue());
                    else if ("TWILIGHT_ARROW_POISON".equals(itemId)) KuudraProfitTracker.updateTwapPrice(v.getSingleValue());
                    break;
                }
                case "AUCTION": {
                    String itemId = obj.get("itemId").getAsString();
                    AuctionItemValue v = auction.get(itemId);
                    if (v == null) v = ensureAuction(itemId, "");
                    long price = obj.get("price").getAsLong();
                    long avg   = obj.get("averagePrice").getAsLong();
                    v.setPrice(price, avg);
                    v.setFetching(false); v.setCached(true); v.setTimestamp(System.currentTimeMillis());

                    if ("WHEEL_OF_FATE".equals(itemId)) KuudraProfitTracker.updateWofPrice(v.getValue());
                    break;
                }
                case "KEY": {
                    applyKeyObject(obj);
                    break;
                }
            }
        }
    }

    public static long computeTotal(KuudraChest chest, boolean ignoreTeeth) {
        long total = 0;

        if (!KuudraProfitCalculatorOptions.ignoreEssence) {
            int ess = KuudraChestUtils.applyPetBoostIfEnabled(chest.getEssenceRaw());
            total += (long) ess * essence().getValue();
        }

        for (Map.Entry<String, BazaarItem> e : chest.getBazaarItems().entrySet()) {
            String id = e.getKey();
            BazaarItemValue v = bazaar.get(id);
            if (ignoreTeeth && "KUUDRA_TEETH".equals(id)) continue;
            if (v != null) total += (long) e.getValue().getCount() * v.getValue();
        }

        for (Map.Entry<String, AuctionItem> e : chest.getAuctionItems().entrySet()) {
            final String key = e.getKey();
            final AuctionItemValue v = auction.get(key);
            if (v == null) continue;

            long current = v.getValue();

            long add;
            if (isArmorPiece(key)) {
                long salvage = KuudraChestUtils.calculateSalvageValueArmor(e.getValue().getStars(), essence(), heavyPearl());
                v.setSalvagePrice(salvage);
                add = (KuudraProfitCalculatorOptions.forceSalvageValueArmor || salvage > current) ? salvage : current;
            } else if (isWandOrStaff(key) || isMolten(key)) {
                long salvage = KuudraChestUtils.calculateSalvageValueMoltenWandStaff(essence());
                v.setSalvagePrice(salvage);
                add = (KuudraProfitCalculatorOptions.forceSalvageValueStaffWand
                        || KuudraProfitCalculatorOptions.forceSalvageValueEquipment
                        || salvage > current) ? salvage : current;
            } else {
                add = current;
            }
            total += add;
        }

        if (chest.getKeyNeeded() != null) total -= getKeyPrice(chest.getKeyNeeded());

        return total;
    }

    public static final class FetchPlan {
        public final List<Request> requests;
        public final List<Value> touched;
        public FetchPlan(List<Request> requests, List<Value> touched) {
            this.requests = requests; this.touched = touched;
        }
    }
}
