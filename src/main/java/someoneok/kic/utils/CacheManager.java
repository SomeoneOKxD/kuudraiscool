package someoneok.kic.utils;

import cc.polyfrost.oneconfig.utils.JsonUtils;
import cc.polyfrost.oneconfig.utils.Multithreading;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import someoneok.kic.KIC;
import someoneok.kic.models.APIException;
import someoneok.kic.models.crimson.AttributeItemValue;
import someoneok.kic.models.crimson.Attributes;
import someoneok.kic.models.crimson.AuctionItemValue;
import someoneok.kic.models.crimson.BazaarItemValue;
import someoneok.kic.models.request.Request;
import someoneok.kic.utils.dev.KICLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static someoneok.kic.KIC.KICPrefix;
import static someoneok.kic.utils.GeneralUtils.sendMessageToPlayer;

public class CacheManager {
    private static long auctionUpdate = 0;
    private static final long EXPIRED = 600000;
    private static final Map<String, AttributeItemValue> attributeItems = new ConcurrentHashMap<>();
    private static final Map<String, BazaarItemValue> bazaarItems = new ConcurrentHashMap<>();
    private static final Map<String, AuctionItemValue> auctionItems = new ConcurrentHashMap<>();

    /**
     * Adds an {@link AttributeItemValue} to the cache or refreshes it if expired.
     * <p>
     * If the item already exists and is expired (older than {@code EXPIRED}),
     * its timestamp is updated, and it is marked as uncached and not fetching,
     * signaling that it needs to be updated.
     * </p>
     *
     * @param item the {@link AttributeItemValue} to add or refresh in the cache
     * @return {@code true} if the cache should be updated (new or expired entry),
     *         {@code false} if the item is still fresh and no update is needed
     */
    public static boolean addItem(AttributeItemValue item) {
        return addItemInternal(item, EXPIRED, false);
    }

    public static boolean addItemAuctionHelper(AttributeItemValue item) {
        return addItemInternal(item, auctionUpdate, true);
    }

    public static boolean addBazaarItem(BazaarItemValue item) {
        return addBazaarItemInternal(item);
    }

    public static boolean addAuctionItem(AuctionItemValue item) {
        return addAuctionItemInternal(item);
    }

    private static boolean addItemInternal(AttributeItemValue item, long freshnessRef, boolean isAuctionTimestamp) {
        AttributeItemValue value = getAttributeItem(item.getUuid());

        if (value == null || !itemsMatch(item, value)) {
            attributeItems.put(item.getUuid(), item);
            return true;
        }

        if (!value.isCached() && !value.isFetching()) {
            return true;
        }

        long now = System.currentTimeMillis();
        boolean isFresh = isAuctionTimestamp
                ? value.getTimestamp() >= freshnessRef
                : (now - value.getTimestamp() < freshnessRef);

        if (!isFresh && !value.isFetching()) {
            value.setTimestamp(now);
            value.setCached(false);
            value.setFetching(false);
            return true;
        }

        return false;
    }

    private static boolean addBazaarItemInternal(BazaarItemValue item) {
        BazaarItemValue value = getBazaarItem(item.getItemId());

        if (value == null || !bazaarItemsMatch(item, value)) {
            bazaarItems.put(item.getItemId(), item);
            return true;
        }

        if (!value.isCached() && !value.isFetching()) {
            return true;
        }

        long now = System.currentTimeMillis();
        boolean isFresh = (now - value.getTimestamp() < EXPIRED);

        if (!isFresh && !value.isFetching()) {
            value.setTimestamp(now);
            value.setCached(false);
            value.setFetching(false);
            return true;
        }

        return false;
    }

    private static boolean addAuctionItemInternal(AuctionItemValue item) {
        AuctionItemValue value = getAuctionItem(item.getUuid());

        if (value == null) {
            auctionItems.put(item.getUuid(), item);
            return true;
        }

        if (!value.isCached() && !value.isFetching()) {
            return true;
        }

        long now = System.currentTimeMillis();
        boolean isFresh = (now - value.getTimestamp() < EXPIRED);

        if (!isFresh && !value.isFetching()) {
            value.setTimestamp(now);
            value.setCached(false);
            value.setFetching(false);
            return true;
        }

        return false;
    }

    private static boolean itemsMatch(AttributeItemValue i1, AttributeItemValue i2) {
        if (!Objects.equals(i1.getUuid(), i2.getUuid()) ||
                !Objects.equals(i1.getItemId(), i2.getItemId())) {
            return false;
        }

        Attributes a1 = i1.getAttributes();
        Attributes a2 = i2.getAttributes();

        boolean directMatch =
                Objects.equals(a1.getAttribute1(), a2.getAttribute1()) &&
                        Objects.equals(a1.getAttribute2(), a2.getAttribute2()) &&
                        a1.getLevel1() == a2.getLevel1() &&
                        a1.getLevel2() == a2.getLevel2();

        boolean swappedMatch =
                Objects.equals(a1.getAttribute1(), a2.getAttribute2()) &&
                        Objects.equals(a1.getAttribute2(), a2.getAttribute1()) &&
                        a1.getLevel1() == a2.getLevel2() &&
                        a1.getLevel2() == a2.getLevel1();

        return directMatch || swappedMatch;
    }

    private static boolean bazaarItemsMatch(BazaarItemValue i1, BazaarItemValue i2) {
        return Objects.equals(i1.getItemId(), i2.getItemId()) && Objects.equals(i1.getItemCount(), i2.getItemCount());
    }

    public static AttributeItemValue getAttributeItem(String uuid) {
        return attributeItems.get(uuid);
    }

    public static BazaarItemValue getBazaarItem(String itemId) {
        return bazaarItems.get(itemId);
    }

    public static AuctionItemValue getAuctionItem(String uuid) {
        return auctionItems.get(uuid);
    }

    public static void updateItemsCache(Runnable callback) {
        if (!ApiUtils.isVerified()) return;

        List<AttributeItemValue> itemsToFetch = attributeItems.values().stream()
                .filter(item -> !item.isFetching() && !item.isCached())
                .collect(Collectors.toList());

        List<BazaarItemValue> bazaarItemsToFetch = bazaarItems.values().stream()
                .filter(item -> !item.isFetching() && !item.isCached())
                .collect(Collectors.toList());

        List<AuctionItemValue> auctionItemsToFetch = auctionItems.values().stream()
                .filter(item -> !item.isFetching() && !item.isCached())
                .collect(Collectors.toList());

        if (itemsToFetch.isEmpty() && bazaarItemsToFetch.isEmpty() && auctionItemsToFetch.isEmpty()) {
            if (callback != null) {
                callback.run();
            }
            return;
        }

        KICLogger.info("Updating cache from api");

        List<Request> requestItems = new ArrayList<>();

        requestItems.addAll(
                itemsToFetch.stream()
                        .map(AttributeItemValue::mapToRequest)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList())
        );

        requestItems.addAll(
                bazaarItemsToFetch.stream()
                        .map(BazaarItemValue::mapToRequest)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList())
        );

        requestItems.addAll(
                auctionItemsToFetch.stream()
                        .map(AuctionItemValue::mapToRequest)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList())
        );

        itemsToFetch.forEach(item -> item.setFetching(true));

        String requestBody = KIC.GSON.toJson(requestItems);

        KICLogger.info("Cache update request: " + requestBody);

        Multithreading.runAsync(() -> {
            try {
                JsonArray response = JsonUtils.parseString(NetworkUtils.sendPostRequest("https://api.sm0kez.com/crimson/prices", true, requestBody)).getAsJsonArray();
                long time = System.currentTimeMillis();

                if (response != null) {
                    for (JsonElement element : response) {
                        JsonObject obj = element.getAsJsonObject();
                        String type = obj.get("type").getAsString();
                        switch (type) {
                            case "ATTRIBUTES":
                                String uuid = obj.get("uuid").getAsString();
                                AttributeItemValue attributeItemValue = getAttributeItem(uuid);
                                if (attributeItemValue == null) continue;

                                attributeItemValue.setFetching(false);
                                attributeItemValue.setCached(true);
                                attributeItemValue.setTimestamp(time);

                                Attributes attributes = attributeItemValue.getAttributes();
                                attributes.setLbPrice1(obj.get("priceAttribute1").getAsLong());
                                attributes.setAvgPrice1(obj.get("averagePriceAttribute1").getAsLong());
                                if (obj.has("priceAttribute2") && !obj.get("priceAttribute2").isJsonNull()) {
                                    attributes.setLbPrice2(obj.get("priceAttribute2").getAsLong());
                                    attributes.setAvgPrice2(obj.get("averagePriceAttribute2").getAsLong());
                                }
                                attributes.setGodroll(obj.get("godRoll").getAsBoolean());
                                if (obj.has("godRollPrice") && !obj.get("godRollPrice").isJsonNull()) {
                                    attributes.setGodrollLbPrice(obj.get("godRollPrice").getAsLong());
                                    attributes.setGodrollAvgPrice(obj.get("averageGodRollPrice").getAsLong());
                                }
                                break;
                            case "BAZAAR":
                                String itemId = obj.get("itemId").getAsString();
                                BazaarItemValue bazaarItemValue = getBazaarItem(itemId);
                                if (bazaarItemValue == null) continue;

                                long buyPriceBazaar = obj.get("buyPrice").getAsLong();
                                long sellPriceBazaar = obj.get("sellPrice").getAsLong();
                                bazaarItemValue.setPrice(buyPriceBazaar, sellPriceBazaar);
                                bazaarItemValue.setFetching(false);
                                bazaarItemValue.setCached(true);
                                bazaarItemValue.setTimestamp(time);
                            case "AUCTION":
                                String uuid2 = obj.get("uuid").getAsString();
                                AuctionItemValue auctionItemValue = getAuctionItem(uuid2);
                                if (auctionItemValue == null) continue;

                                long priceAuction = obj.get("price").getAsLong();
                                long avgAuction = obj.get("averagePrice").getAsLong();
                                auctionItemValue.setPrice(priceAuction, avgAuction);
                                auctionItemValue.setFetching(false);
                                auctionItemValue.setCached(true);
                                auctionItemValue.setTimestamp(time);
                                break;
                        }
                    }
                }
            } catch (APIException e) {
                sendMessageToPlayer(String.format("%s §c%s", KICPrefix, e.getMessage()));
                itemsToFetch.forEach(item -> {
                    item.setFetching(false);
                    item.setCached(false);
                });
                bazaarItemsToFetch.forEach(item -> {
                    item.setFetching(false);
                    item.setCached(false);
                });
                auctionItemsToFetch.forEach(item -> {
                    item.setFetching(false);
                    item.setCached(false);
                });
            }

            if (callback != null) {
                callback.run();
            }
        });
    }

    private static void cleanupCaches() {
        long currentTime = System.currentTimeMillis();
        attributeItems.entrySet().removeIf(entry -> currentTime - entry.getValue().getTimestamp() > EXPIRED);
        bazaarItems.entrySet().removeIf(entry -> currentTime - entry.getValue().getTimestamp() > EXPIRED);
        auctionItems.entrySet().removeIf(entry -> currentTime - entry.getValue().getTimestamp() > EXPIRED);
    }

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Unload event) {
        CacheManager.cleanupCaches();
    }

    public static void setAuctionUpdate(long timestamp) {
        auctionUpdate = timestamp;
    }
}
