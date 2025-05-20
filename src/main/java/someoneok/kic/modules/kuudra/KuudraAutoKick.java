package someoneok.kic.modules.kuudra;

import cc.polyfrost.oneconfig.utils.Multithreading;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import someoneok.kic.KIC;
import someoneok.kic.config.KICConfig;
import someoneok.kic.config.pages.KuudraAutoKickOptions;
import someoneok.kic.utils.dev.KICLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static someoneok.kic.config.pages.KuudraAutoKickOptions.*;
import static someoneok.kic.utils.GeneralUtils.sendCommand;
import static someoneok.kic.utils.PartyUtils.amILeader;
import static someoneok.kic.utils.StringUtils.parseToShorthandNumber;

public class KuudraAutoKick {
    private static final Pattern trimonuPattern = Pattern.compile("^Party > (?:\\[[^]]+]\\s*)?(\\w+).*?: .*?✯ Opened.*");
    private static final List<String> armorTiers = Arrays.asList("INFERNAL", "FIERY", "BURNING", "HOT", "BASIC", "NONE");
    private static final List<String> gemTiers = Arrays.asList("PERFECT", "FLAWLESS", "FINE", "FLAWED", "ROUGH", "NONE");

    public static void autoKick(
            String user, int lifeline, int mana_pool, int cata_level, int t5_comps, int magical_power, int rag_chim_level,
            int term_duplex_level, String rag_gemstone, String chestplate, String leggings, String boots,
            boolean term_p7, boolean term_c6, boolean wither_impact, int legion_level, int strong_fero_mana,
            long bank_balance, long gold_collection, int golden_dragon_level,
            boolean inventory_api, boolean banking_api, boolean collections_api
    ) {
        if (!KICConfig.kuudraAutoKick) return;

        KICLogger.info("[AutoKick] Evaluating player: " + user);

        String lowerUser = user.toLowerCase();

        if (useWhitelist && parsePlayerList(whitelisted).contains(lowerUser)) {
            return;
        }

        if (useBlacklist && parsePlayerList(blacklisted).contains(lowerUser)) {
            sendCommand(String.format("/pc [KIC] %s was kicked for: blacklisted", user));
            Multithreading.schedule(() -> sendCommand("/p kick " + user), 500, TimeUnit.MILLISECONDS);
            return;
        }

        int gem_tier = gemTiers.indexOf(rag_gemstone);

        int chestplate_tier = armorTiers.stream().filter(chestplate.toUpperCase()::contains).map(armorTiers::indexOf).findFirst().orElse(5);
        int leggings_tier = armorTiers.stream().filter(leggings.toUpperCase()::contains).map(armorTiers::indexOf).findFirst().orElse(5);
        int boots_tier = armorTiers.stream().filter(boots.toUpperCase()::contains).map(armorTiers::indexOf).findFirst().orElse(5);

        int armor_tier = Math.max(chestplate_tier, Math.max(leggings_tier, boots_tier));

        List<String> failedCriteria = new ArrayList<>();

        // Handle API being off first
        if (!inventory_api && autoKickInventoryApiOff) {
            failedCriteria.add("Inventory API off");
        }
        if (!banking_api && autoKickBankingApiOff) {
            failedCriteria.add("Banking API off");
        }
        if (!collections_api && autoKickCollectionsApiOff) {
            failedCriteria.add("Collections API off");
        }

        if (inventory_api) {
            failedCriteria.addAll(Stream.of(
                    new KickCriteria(lifeline < minLifelineLevel, lifeline + "/" + minLifelineLevel + " lifeline/dominance"),
                    new KickCriteria(mana_pool < minManapoolLevel, mana_pool + "/" + minManapoolLevel + " mana pool"),
                    new KickCriteria(cata_level < minCataLevel, cata_level + "/" + minCataLevel + " cata level"),
                    new KickCriteria(t5_comps < minT5Completions, t5_comps + "/" + minT5Completions + " T5 runs"),
                    new KickCriteria(magical_power < minMagicalPower, magical_power + "/" + minMagicalPower + " magical power"),
                    new KickCriteria(rag_chim_level < minChimeraLevelRagAxe, rag_chim_level + "/" + minChimeraLevelRagAxe + " chimera"),
                    new KickCriteria(term_duplex_level < minDuplexLevel, term_duplex_level + "/" + minDuplexLevel + " duplex"),
                    new KickCriteria(gem_tier > minGemstoneTierRagAxe, "Low tier rag gemstone [Has: " + gemTiers.get(gem_tier) + ", Req: " + gemTiers.get(minGemstoneTierRagAxe) + "]"),
                    new KickCriteria(armor_tier > minTerrorTier, "Low tier armor [Has: " + armorTiers.get(armor_tier) + ", Req: " + armorTiers.get(minTerrorTier) + "]"),
                    new KickCriteria(autoKickNoP7Duplex && !term_p7, "Missing P7 on term"),
                    new KickCriteria(autoKickNoC7Duplex && !term_c6, "Missing C6 on term"),
                    new KickCriteria(autoKickNoWitherImpact && !wither_impact, "Missing Wither Impact"),
                    new KickCriteria(legion_level < minLegionLevel, legion_level + "/" + minLegionLevel + " legion"),
                    new KickCriteria(strong_fero_mana < minStrongFeroLevel, strong_fero_mana + "/" + minStrongFeroLevel + " strong/fero mana")
            ).filter(c -> c.condition).map(c -> c.reason).limit(6).collect(Collectors.toList()));
        }

        if (banking_api) {
            failedCriteria.addAll(Stream.of(
                    new KickCriteria(bank_balance < minBankBalance * 10L, parseToShorthandNumber(bank_balance) + "/" + parseToShorthandNumber(minBankBalance * 10L) + " bank")
            ).filter(c -> c.condition).map(c -> c.reason).limit(6).collect(Collectors.toList()));
        }

        if (collections_api) {
            failedCriteria.addAll(Stream.of(
                    new KickCriteria(gold_collection < minGoldCollection * 10L, parseToShorthandNumber(gold_collection) + "/" + parseToShorthandNumber(minGoldCollection * 10L) + " gold"),
                    new KickCriteria(golden_dragon_level < minGregLevel, golden_dragon_level + "/" + minGregLevel + " golden dragon level")
            ).filter(c -> c.condition).map(c -> c.reason).limit(6).collect(Collectors.toList()));
        }

        if (!failedCriteria.isEmpty()) {
            boolean hasMore = failedCriteria.size() > 5;
            String reasons = String.join(", ", failedCriteria.subList(0, Math.min(5, failedCriteria.size())));
            if (hasMore) reasons += ", ...";

            KICLogger.warn(String.format("[AutoKick] %s - Kicked for: %s", user, reasons));

            sendCommand(String.format("/pc [KIC] %s kicked for: %s", user, reasons));
            Multithreading.schedule(() -> sendCommand("/p kick " + user), 500, TimeUnit.MILLISECONDS);
        } else {
            KICLogger.info(String.format("[AutoKick] %s meets all requirements, no kick necessary.", user));
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    public void onChat(ClientChatReceivedEvent event) {
        if (!KICConfig.kuudraAutoKick || !KuudraAutoKickOptions.autoKickTrimonu || !amILeader()) return;
        String message = event.message.getUnformattedText();
        Matcher matcher = trimonuPattern.matcher(message);

        if (matcher.find()) {
            String player = matcher.group(1);
            if (player.equalsIgnoreCase(KIC.mc.thePlayer.getName())) return;
            sendCommand(String.format("/pc [KIC] %s kicked for using Trimonu!", player));
            Multithreading.schedule(() -> sendCommand("/party kick " + player), 500, TimeUnit.MILLISECONDS);
        }
    }

    private static class KickCriteria {
        boolean condition;
        String reason;

        KickCriteria(boolean condition, String reason) {
            this.condition = condition;
            this.reason = reason;
        }
    }

    private static Set<String> parsePlayerList(String input) {
        return Arrays.stream(input.split(";"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }
}
