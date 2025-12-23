package someoneok.kic.modules.kuudra;

import cc.polyfrost.oneconfig.utils.Multithreading;
import someoneok.kic.config.KICConfig;
import someoneok.kic.models.kuudra.AutoKickProfile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static someoneok.kic.config.pages.KuudraAutoKickOptions.*;
import static someoneok.kic.utils.ChatUtils.sendCommand;
import static someoneok.kic.utils.StringUtils.parseToShorthandNumber;

public class KuudraAutoKick {
    private static final List<String> ARMOR_TIERS = Arrays.asList("INFERNAL", "FIERY", "BURNING", "HOT", "BASIC", "NONE");
    private static final List<String> GEMSTONE_TIERS = Arrays.asList("PERFECT", "FLAWLESS", "FINE", "FLAWED", "ROUGH", "NONE");
    private static final List<String> TALISMAN_TIERS = Arrays.asList("No Talisman", "Kuudra's Kidney", "Kuudra's Lung", "Kuudra's Heart");
    private static final List<String> HAB_LEVELS = Arrays.asList("No Habanero Tactics", "Habanero Tactics 4", "Habanero Tactics 5");

    public static void autoKick(AutoKickProfile p) {
        if (!KICConfig.kuudraAutoKick) return;

        String lowerUser = p.getUser().toLowerCase();
        String lowerUuid = p.getUuid().toLowerCase();

        Set<String> whitelist = parsePlayerList(whitelisted);
        if (useWhitelist && (whitelist.contains(lowerUser) || whitelist.contains(lowerUuid))) return;

        Set<String> blacklist = parsePlayerList(blacklisted);
        if (useBlacklist && (blacklist.contains(lowerUser) || blacklist.contains(lowerUuid))) {
            sendCommand(String.format("/pc [KIC] %s was kicked for: blacklisted", p.getUser()));
            Multithreading.schedule(() -> sendCommand("/p kick " + p.getUser()), 500, TimeUnit.MILLISECONDS);
            return;
        }

        List<KickCriteria> criteria = new ArrayList<>();

        criteria.add(new KickCriteria(autoKickInventoryApiOff && !p.hasInventoryApi(), "Inventory API off"));
        criteria.add(new KickCriteria(autoKickBankingApiOff && !p.hasBankingApi(), "Banking API off"));
        criteria.add(new KickCriteria(autoKickCollectionsApiOff && !p.hasCollectionsApi(), "Collections API off"));

        if (p.hasInventoryApi()) {
            String chestName = String.valueOf(p.getChestplate()).toUpperCase();
            String legsName  = String.valueOf(p.getLeggings()).toUpperCase();
            String bootsName = String.valueOf(p.getBoots()).toUpperCase();

            int chestplate_tier = ARMOR_TIERS.stream()
                    .filter(chestName::contains)
                    .map(ARMOR_TIERS::indexOf)
                    .findFirst()
                    .orElse(ARMOR_TIERS.indexOf("NONE"));

            int leggings_tier = ARMOR_TIERS.stream()
                    .filter(legsName::contains)
                    .map(ARMOR_TIERS::indexOf)
                    .findFirst()
                    .orElse(ARMOR_TIERS.indexOf("NONE"));

            int boots_tier = ARMOR_TIERS.stream()
                    .filter(bootsName::contains)
                    .map(ARMOR_TIERS::indexOf)
                    .findFirst()
                    .orElse(ARMOR_TIERS.indexOf("NONE"));

            int armorTier = Math.max(chestplate_tier, Math.max(leggings_tier, boots_tier));

            String ragGem = p.getRagGemstone();
            int gemTier = -1;
            if (ragGem != null) gemTier = GEMSTONE_TIERS.indexOf(ragGem.toUpperCase());
            if (gemTier == -1) gemTier = GEMSTONE_TIERS.indexOf("NONE");

            int talTier = Math.max(0, Math.min(p.getTalismanTier(), TALISMAN_TIERS.size() - 1));
            int minTalReq = Math.max(0, Math.min(minTalismanTier, TALISMAN_TIERS.size() - 1));

            criteria.add(new KickCriteria(p.getCataLevel() < minCataLevel,
                    p.getCataLevel() + "/" + minCataLevel + " cata level"));
            criteria.add(new KickCriteria(p.getForagingLevel() < minForagingLevel,
                    p.getForagingLevel() + "/" + minForagingLevel + " foraging level"));
            criteria.add(new KickCriteria(p.getT5Comps() < minT5Completions,
                    p.getT5Comps() + "/" + minT5Completions + " T5 runs"));
            criteria.add(new KickCriteria(p.getMagicalPower() < minMagicalPower,
                    p.getMagicalPower() + "/" + minMagicalPower + " magical power"));
            criteria.add(new KickCriteria(p.getRagChimLevel() < minChimeraLevelRagAxe,
                    p.getRagChimLevel() + "/" + minChimeraLevelRagAxe + " chimera"));
            criteria.add(new KickCriteria(p.getTermDuplexLevel() < minDuplexLevel,
                    p.getTermDuplexLevel() + "/" + minDuplexLevel + " duplex"));

            criteria.add(new KickCriteria(
                    gemTier > minGemstoneTierRagAxe,
                    "Low tier rag gemstone [Has: " + GEMSTONE_TIERS.get(gemTier)
                            + ", Req: " + GEMSTONE_TIERS.get(minGemstoneTierRagAxe) + "]"
            ));
            criteria.add(new KickCriteria(
                    armorTier > minTerrorTier,
                    "Low tier armor [Has: " + ARMOR_TIERS.get(armorTier)
                            + ", Req: " + ARMOR_TIERS.get(minTerrorTier) + "]"
            ));
            criteria.add(new KickCriteria(
                    talTier < minTalReq,
                    "Low tier talisman [Has: " + TALISMAN_TIERS.get(talTier)
                            + ", Req: " + TALISMAN_TIERS.get(minTalReq) + "]"
            ));

            criteria.add(new KickCriteria(autoKickNoP7Duplex && !p.isTermP7(), "Missing P7 on term"));
            criteria.add(new KickCriteria(autoKickNoC6Duplex && !p.isTermC6(), "Missing C6 on term"));
            criteria.add(new KickCriteria(autoKickNoSmold5Duplex && !p.isTermSmold5(), "Missing SMOLD5 on term"));
            criteria.add(new KickCriteria(autoKickNoWitherImpact && !p.hasWitherImpact(), "Missing Wither Impact"));
            criteria.add(new KickCriteria(p.getLegionLevel() < minLegionLevel,
                    p.getLegionLevel() + "/" + minLegionLevel + " legion"));
            criteria.add(new KickCriteria(p.getStrongFeroMana() < minStrongFeroLevel,
                    p.getStrongFeroMana() + "/" + minStrongFeroLevel + " strong/fero mana"));

            criteria.add(new KickCriteria(autoKickNoRend && !p.hasRend(), "Missing Rend"));

            if (p.hasRend()) {
                int hl = p.getHabLevel();
                int habLevelIndex = (hl == 4) ? 1 : (hl == 5) ? 2 : 0;
                int minHabReq = Math.max(0, Math.min(minHabTactics, HAB_LEVELS.size() - 1));

                criteria.add(new KickCriteria(autoKickNoRendTerm && !p.hasRendTerm(), "Missing Rend term"));
                criteria.add(new KickCriteria(autoKickNoP7Bone && !p.isBoneP7(), "Missing P7 on bone"));
                criteria.add(new KickCriteria(autoKickNoC6Bone && !p.isBoneC6(), "Missing C6 on bone"));
                criteria.add(new KickCriteria(autoKickNoSmold5Bone && !p.isBoneSmold5(), "Missing SMOLD5 on bone"));
                criteria.add(new KickCriteria(autoKickNoGK7Blade && !p.isBladeGK7(), "Missing GK7 on blade"));
                criteria.add(new KickCriteria(autoKickNoPros6Blade && !p.isBladePros6(), "Missing Pros6 on blade"));
                criteria.add(new KickCriteria(
                        habLevelIndex < minHabReq,
                        "Low hab level [Has: " + HAB_LEVELS.get(habLevelIndex)
                                + ", Req: " + HAB_LEVELS.get(minHabReq) + "]"
                ));
                criteria.add(new KickCriteria(autoKickNoBiggerTeeth && !p.hasBiggerTeeth(),
                        "Missing Greg with bigger teeth"));
            }
        }

        criteria.add(new KickCriteria(
                p.getGoldenDragonLevel() < minGregLevel,
                p.getGoldenDragonLevel() + "/" + minGregLevel + " golden dragon level"
        ));
        criteria.add(new KickCriteria(autoKickNoHephaestusRemedies && !p.hasHephaestusRemedies(), "Missing Hephaestus Remedies"));

        if (p.hasBankingApi()) {
            long requiredBank = minBankBalance * 10L;
            criteria.add(new KickCriteria(
                    p.getBankBalance() < requiredBank,
                    parseToShorthandNumber(p.getBankBalance()) + "/"
                            + parseToShorthandNumber(requiredBank) + " bank"
            ));
        }

        if (p.hasCollectionsApi()) {
            long requiredGold = minGoldCollection * 10L;
            criteria.add(new KickCriteria(
                    p.getGoldCollection() < requiredGold,
                    parseToShorthandNumber(p.getGoldCollection()) + "/"
                            + parseToShorthandNumber(requiredGold) + " gold"
            ));
        }

        List<String> failedCriteria = criteria.stream()
                .filter(c -> c.condition)
                .map(c -> c.reason)
                .collect(Collectors.toList());

        if (!failedCriteria.isEmpty()) {
            boolean hasMore = failedCriteria.size() > 5;
            String reasons = String.join(", ",
                    failedCriteria.subList(0, Math.min(5, failedCriteria.size())));
            if (hasMore) reasons += ", ...";

            sendCommand(String.format("/pc [KIC] %s kicked for: %s", p.getUser(), reasons));
            Multithreading.schedule(() -> sendCommand("/p kick " + p.getUser()), 500, TimeUnit.MILLISECONDS);
        }
    }

    private static Set<String> parsePlayerList(String input) {
        String cleanedInput = input.replace("-", "");
        return Arrays.stream(cleanedInput.split(";"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    private static class KickCriteria {
        boolean condition;
        String reason;

        KickCriteria(boolean condition, String reason) {
            this.condition = condition;
            this.reason = reason;
        }
    }
}
