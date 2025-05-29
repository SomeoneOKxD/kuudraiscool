package someoneok.kic.modules.kuudra;

import cc.polyfrost.oneconfig.utils.JsonUtils;
import cc.polyfrost.oneconfig.utils.Multithreading;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import someoneok.kic.KIC;
import someoneok.kic.config.KICConfig;
import someoneok.kic.models.APIException;
import someoneok.kic.utils.ApiUtils;
import someoneok.kic.utils.NetworkUtils;
import someoneok.kic.utils.PartyUtils;
import someoneok.kic.utils.dev.KICLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static someoneok.kic.KIC.KICPrefix;
import static someoneok.kic.modules.kuudra.KuudraAutoKick.autoKick;
import static someoneok.kic.utils.ApiUtils.hasPremium;
import static someoneok.kic.utils.GeneralUtils.*;
import static someoneok.kic.utils.PlayerUtils.getPlayerName;
import static someoneok.kic.utils.StringUtils.*;

public class KuudraUserInfo {
    private static final Pattern partyJoinPattern = Pattern.compile("^Party Finder > (.+) joined the group! (.+)$");

    public static void showKuudraInfo(String player, boolean manual) {
        if (!ApiUtils.isVerified()) {
            sendMessageToPlayer(KICPrefix + " §cMod disabled: not verified.");
            return;
        }

        if (isNullOrEmpty(player) && KIC.mc.thePlayer != null) {
            player = getPlayerName();
        }
        if (player == null) return;
        String formattedPlayer = formatPlayerName(player);
        if (formattedPlayer == null) {
            sendMessageToPlayer(KICPrefix + " §cInvalid username!");
            return;
        }

        JsonObject playerInfo;
        try {
            playerInfo = JsonUtils.parseString(NetworkUtils.sendGetRequest("https://api.sm0kez.com/hypixel/info/" + formattedPlayer + "?type=KUUDRA", true)).getAsJsonObject();
        } catch (APIException e) {
            sendMessageToPlayer(String.format("%s §c%s", KICPrefix, e.getMessage()));
            return;
        }

        if (playerInfo == null || !playerInfo.isJsonObject()) {
            sendMessageToPlayer(KICPrefix + " §cError while retrieving data!");
            return;
        }

        ChatComponentText message = makeMessage(playerInfo, manual);

        if (message == null) {
            sendMessageToPlayer(KICPrefix + " §cError while retrieving data!");
            return;
        }

        sendMessageToPlayer(message);
    }

    public static ChatComponentText makeMessage(JsonObject infoObject, boolean manual) {
        if (!ApiUtils.isVerified()) return null;
        try {
            JsonObject playerInfo = infoObject.get("playerInfo").getAsJsonObject();
            String kic = infoObject.has("kic") && !infoObject.get("kic").isJsonNull() ? infoObject.get("kic").getAsString() : "";
            JsonObject apiStatus = infoObject.get("apiStatus").getAsJsonObject();
            JsonObject stats = infoObject.get("stats").getAsJsonObject();
            JsonObject items = infoObject.get("items").getAsJsonObject();
            JsonObject extra = infoObject.get("extra").getAsJsonObject();
            JsonObject rend = infoObject.get("rend").getAsJsonObject();
            JsonObject armor = infoObject.get("armor").getAsJsonObject();
            JsonObject equipment = infoObject.get("equipment").getAsJsonObject();
            JsonObject pets = infoObject.get("pets").getAsJsonObject();

            String username = playerInfo.get("username").getAsString();
            String uuid = playerInfo.get("uuid").getAsString();
            String api_off = "§cAPI OFF";

            // Api status
            boolean banking = apiStatus.get("banking").getAsBoolean();
            boolean collections = apiStatus.get("collections").getAsBoolean();
            boolean inventory = apiStatus.get("inventory").getAsBoolean();

            // Extra info
            boolean reaper = extra.get("reaper").getAsBoolean();
            boolean term = extra.get("term").getAsBoolean();
            boolean term_power7 = extra.get("term_power7").getAsBoolean();
            boolean term_cubism6 = extra.get("term_cubism6").getAsBoolean();
            String term_lore = extra.get("term_lore").getAsString();
            boolean deployable = extra.get("deployable").getAsBoolean();
            String deployable_name = extra.get("deployable_name").getAsString();
            boolean fire_veil = extra.get("fire_veil").getAsBoolean();
            String fire_veil_name = extra.get("fire_veil_name").getAsString();
            boolean radio = extra.get("radio").getAsBoolean();
            String radio_name = extra.get("radio_name").getAsString();

            // Rend
            boolean rend_term = rend.get("rend_term").getAsBoolean();
            String rend_term_lore = rend.get("rend_term_lore").getAsString();
            int rend_term_rend_lvl = rend.get("rend_term_rend_lvl").getAsInt();
            boolean rend_term_power7 = rend.get("rend_term_power7").getAsBoolean();
            boolean rend_term_cubism6 = rend.get("rend_term_cubism6").getAsBoolean();
            boolean rend_bone = rend.get("rend_bone").getAsBoolean();
            int rend_bone_rend_lvl = rend.get("rend_bone_rend_lvl").getAsInt();
            boolean rend_bone_power7 = rend.get("rend_bone_power7").getAsBoolean();
            boolean rend_bone_cubism6 = rend.get("rend_bone_cubism6").getAsBoolean();
            boolean rend_blade = rend.get("rend_blade").getAsBoolean();
            int rend_blade_swarm_lvl = rend.get("rend_blade_swarm_lvl").getAsInt();
            boolean rend_blade_gk7 = rend.get("rend_blade_gk7").getAsBoolean();
            boolean rend_blade_pros6 = rend.get("rend_blade_pros6").getAsBoolean();

            // Stats
            int basicComps = stats.get("basic_runs").getAsInt();
            int hotComps = stats.get("hot_runs").getAsInt();
            int burningComps = stats.get("burning_runs").getAsInt();
            int fieryComps = stats.get("fiery_runs").getAsInt();
            int infernalComps = stats.get("infernal_runs").getAsInt();
            int totalComps = basicComps + hotComps + burningComps + fieryComps + infernalComps;
            double soreComps = basicComps * 0.5 + hotComps + burningComps * 2 + fieryComps * 4 + infernalComps * 8;
            double cata_level = stats.get("cata_level").getAsDouble();
            double skyblock_level = stats.get("skyblock_level").getAsDouble();
            String power = stats.get("power").getAsString();
            String tuning = stats.get("tuning").getAsString();
            String enrichment = stats.get("enrichments").getAsString();
            int mageRep = stats.get("mage_rep").getAsInt();
            int barbRep = stats.get("barb_rep").getAsInt();
            int magicalPower = stats.get("magical_power").getAsInt();
            int totalStrongMana = armor.get("helmet_strong_mana").getAsInt() + armor.get("chestplate_strong_mana").getAsInt() + armor.get("leggings_strong_mana").getAsInt() + armor.get("boots_strong_mana").getAsInt();
            int totalFeroMana = armor.get("helmet_fero_mana").getAsInt() + armor.get("chestplate_fero_mana").getAsInt() + armor.get("leggings_fero_mana").getAsInt() + armor.get("boots_fero_mana").getAsInt();
            int totalLegion = armor.get("helmet_legion").getAsInt() + armor.get("chestplate_legion").getAsInt() + armor.get("leggings_legion").getAsInt() + armor.get("boots_legion").getAsInt();

            // Items
            boolean hyperion = items.get("hyperion").getAsBoolean();
            String hyperion_lore = items.get("hyperion_lore").getAsString();
            boolean duplex = items.get("duplex_term").getAsBoolean();
            String duplex_lore = items.get("duplex_term_lore").getAsString();
            int duplex_lvl = items.get("duplex_term_duplex_lvl").getAsInt();
            boolean duplex_power7 = items.get("duplex_term_power7").getAsBoolean();
            boolean duplex_cubism6 = items.get("duplex_term_cubism6").getAsBoolean();
            boolean rag = items.get("rag").getAsBoolean();
            String rag_lore = items.get("rag_lore").getAsString();
            String rag_gem = items.get("rag_gemstone").getAsString();
            int rag_chim = items.get("rag_chimera").getAsInt();

            // Armor
            boolean helmet = armor.get("helmet").getAsBoolean();
            String helmet_name = armor.get("helmet_name").getAsString();
            String helmet_lore = armor.get("helmet_lore").getAsString();
            JsonArray helmet_gemstones = armor.getAsJsonArray("helmet_gemstones");
            String helmet_gem_1 = helmet_gemstones.size() > 0 ? helmet_gemstones.get(0).getAsString() : "NONE";
            String helmet_gem_2 = helmet_gemstones.size() > 1 ? helmet_gemstones.get(1).getAsString() : "NONE";
            int helmet_ll = armor.get("helmet_lifeline").getAsInt();
            int helmet_dom = armor.get("helmet_dominance").getAsInt();
            int helmet_mp = armor.get("helmet_mana_pool").getAsInt();
            boolean chestplate = armor.get("chestplate").getAsBoolean();
            String chestplate_name = armor.get("chestplate_name").getAsString();
            String chestplate_lore = armor.get("chestplate_lore").getAsString();
            JsonArray chestplate_gemstones = armor.getAsJsonArray("chestplate_gemstones");
            String chestplate_gem_1 = chestplate_gemstones.size() > 0 ? chestplate_gemstones.get(0).getAsString() : "NONE";
            String chestplate_gem_2 = chestplate_gemstones.size() > 1 ? chestplate_gemstones.get(1).getAsString() : "NONE";
            int chestplate_ll = armor.get("chestplate_lifeline").getAsInt();
            int chestplate_dom = armor.get("chestplate_dominance").getAsInt();
            int chestplate_mp = armor.get("chestplate_mana_pool").getAsInt();
            boolean leggings = armor.get("leggings").getAsBoolean();
            String leggings_name = armor.get("leggings_name").getAsString();
            String leggings_lore = armor.get("leggings_lore").getAsString();
            JsonArray leggings_gemstones = armor.getAsJsonArray("leggings_gemstones");
            String leggings_gem_1 = leggings_gemstones.size() > 0 ? leggings_gemstones.get(0).getAsString() : "NONE";
            String leggings_gem_2 = leggings_gemstones.size() > 1 ? leggings_gemstones.get(1).getAsString() : "NONE";
            int leggings_ll = armor.get("leggings_lifeline").getAsInt();
            int leggings_dom = armor.get("leggings_dominance").getAsInt();
            int leggings_mp = armor.get("leggings_mana_pool").getAsInt();
            boolean boots = armor.get("boots").getAsBoolean();
            String boots_name = armor.get("boots_name").getAsString();
            String boots_lore = armor.get("boots_lore").getAsString();
            JsonArray boots_gemstones = armor.getAsJsonArray("boots_gemstones");
            String boots_gem_1 = boots_gemstones.size() > 0 ? boots_gemstones.get(0).getAsString() : "NONE";
            String boots_gem_2 = boots_gemstones.size() > 1 ? boots_gemstones.get(1).getAsString() : "NONE";
            int boots_ll = armor.get("boots_lifeline").getAsInt();
            int boots_dom = armor.get("boots_dominance").getAsInt();
            int boots_mp = armor.get("boots_mana_pool").getAsInt();

            // Equipment
            String necklace_name = equipment.get("necklace_name").getAsString();
            int necklace_ll = equipment.get("necklace_lifeline").getAsInt();
            int necklace_dom = equipment.get("necklace_dominance").getAsInt();
            int necklace_mp = equipment.get("necklace_mana_pool").getAsInt();
            String cloak_name = equipment.get("cloak_name").getAsString();
            int cloak_ll = equipment.get("cloak_lifeline").getAsInt();
            int cloak_dom = equipment.get("cloak_dominance").getAsInt();
            int cloak_mp = equipment.get("cloak_mana_pool").getAsInt();
            String belt_name = equipment.get("belt_name").getAsString();
            int belt_ll = equipment.get("belt_lifeline").getAsInt();
            int belt_dom = equipment.get("belt_dominance").getAsInt();
            int belt_mp = equipment.get("belt_mana_pool").getAsInt();
            String bracelet_name = equipment.get("bracelet_name").getAsString();
            int bracelet_ll = equipment.get("bracelet_lifeline").getAsInt();
            int bracelet_dom = equipment.get("bracelet_dominance").getAsInt();
            int bracelet_mp = equipment.get("bracelet_mana_pool").getAsInt();

            // Pets
            JsonArray petsArray = pets.getAsJsonArray("pets");
            boolean hasPet = petsArray.size() > 0;
            JsonObject firstPet = hasPet ? petsArray.get(0).getAsJsonObject() : null;
            int firstPetLevel = hasPet ? firstPet.get("level").getAsInt() : 0;
            String firstPetName = hasPet ? String.format("§7[Lvl %d] §6%s", firstPet.get("level").getAsInt(), firstPet.get("name").getAsString()) : null;
            long bankBal = pets.get("bank_balance").getAsLong();
            long goldCol = pets.get("gold_collection").getAsLong();
            long purseBal = pets.get("purse_balance").getAsLong();
            boolean oneBilBank = bankBal >= 1000000000;

            int totalLL = helmet_ll + chestplate_ll + leggings_ll + boots_ll + necklace_ll + cloak_ll + belt_ll + bracelet_ll;
            int totalDom = helmet_dom + chestplate_dom + leggings_dom + boots_dom + necklace_dom + cloak_dom + belt_dom + bracelet_dom;
            int totalMp = helmet_mp + chestplate_mp + leggings_mp + boots_mp + necklace_mp + cloak_mp + belt_mp + bracelet_mp;

            boolean useLL = (totalLL > totalDom || (totalLL == 0 && totalDom == 0));

            ChatComponentText mainMessage = new ChatComponentText("");
            switch (KICConfig.kuudraStatsStyle) {
                case 1:
                    String startStringAM = String.format("§e---------- Kuudra Stats For §c%s §e----------\n\n", username);
                    String endStringAM = generateDashString(startStringAM, "§e");
                    String scoreAM = String.format("§e- §cKuudra Score: §a%s §e(§a%s §eInfernal Completions)\n", parseToShorthandNumber(soreComps), parseToShorthandNumber(infernalComps));
                    String damageAM = String.format("§e- §cDamage: §a%d §eMP, §r%s §ewith §a%s §eBank\n", magicalPower, (firstPetName != null ? firstPetName : "§cNO PET"), parseToShorthandNumber(bankBal));
                    int effectiveCataLevelAM = (int) Math.min(cata_level, 50);
                    int effectiveSBLevelAM = (int) skyblock_level;
                    String skillsAM = String.format("§e- §cSkills: §eCata §a%d§e, SB §a%d\n", effectiveCataLevelAM, effectiveSBLevelAM);
                    String attributesAM = String.format("§e- §cAttributes: §b%s §a%d§e, §bMana Pool §a%d\n\n", (useLL ? "Lifeline" : "Dominance"), (useLL ? totalLL : totalDom), totalMp);
                    String hypeNameAM = hyperion ? hyperion_lore.split("\n")[0] : "§cNO HYPERION";
                    String hypeAM = String.format("§e- %s\n", hypeNameAM);
                    boolean hasTermAM = false;
                    String termNameAM;
                    String termLoreAM;
                    if (duplex) {
                        termNameAM = duplex_lore.split("\n")[0] + " §r§e[§d§lDUPLEX§r§e]";
                        termLoreAM = duplex_lore;
                        hasTermAM = true;
                    } else if (rend_term) {
                        termNameAM = rend_term_lore.split("\n")[0] + " §r§e[§d§lREND§r§e]";
                        termLoreAM = rend_term_lore;
                        hasTermAM = true;
                    } else if (term) {
                        termNameAM = term_lore.split("\n")[0];
                        termLoreAM = term_lore;
                        hasTermAM = true;
                    } else {
                        termNameAM = "§cNO TERMINATOR";
                        termLoreAM = "";
                    }
                    String termAM = String.format("§e- %s\n", termNameAM);
                    String ragNameAM = rag ? rag_lore.split("\n")[0] : "§cNO RAGNAROCK AXE";
                    String ragAM = String.format("§e- %s\n", ragNameAM);

                    mainMessage.appendSibling(createClickComponent(true, startStringAM, ClickEvent.Action.RUN_COMMAND, "/pv " + username));
                    mainMessage.appendSibling(new ChatComponentText(scoreAM));
                    mainMessage.appendSibling(new ChatComponentText(damageAM));
                    mainMessage.appendSibling(new ChatComponentText(skillsAM));
                    mainMessage.appendSibling(new ChatComponentText(attributesAM));
                    mainMessage.appendSibling(createHoverComponent(hyperion, hypeAM, hyperion_lore));
                    mainMessage.appendSibling(createHoverComponent(hasTermAM, termAM, termLoreAM));
                    mainMessage.appendSibling(createHoverComponent(rag, ragAM, rag_lore));
                    mainMessage.appendSibling(new ChatComponentText(endStringAM));
                    break;

                case 0:
                default:
                    // Setup
                    List<String> llOrDom_list = new ArrayList<>();
                    if ((useLL ? helmet_ll : helmet_dom) > 0) llOrDom_list.add(helmet_name + " §r§f+" + (useLL ? helmet_ll : helmet_dom));
                    if ((useLL ? chestplate_ll : chestplate_dom) > 0) llOrDom_list.add(chestplate_name + " §r§f+" + (useLL ? chestplate_ll : chestplate_dom));
                    if ((useLL ? leggings_ll : leggings_dom) > 0) llOrDom_list.add(leggings_name + " §r§f+" + (useLL ? leggings_ll : leggings_dom));
                    if ((useLL ? boots_ll : boots_dom) > 0) llOrDom_list.add(boots_name + " §r§f+" + (useLL ? boots_ll : boots_dom));

                    if ((useLL ? necklace_ll : necklace_dom) > 0) llOrDom_list.add(necklace_name + " §r§f+" + (useLL ? necklace_ll : necklace_dom));
                    if ((useLL ? cloak_ll : cloak_dom) > 0) llOrDom_list.add(cloak_name + " §r§f+" + (useLL ? cloak_ll : cloak_dom));
                    if ((useLL ? belt_ll : belt_dom) > 0) llOrDom_list.add(belt_name + " §r§f+" + (useLL ? belt_ll : belt_dom));
                    if ((useLL ? bracelet_ll : bracelet_dom) > 0) llOrDom_list.add(bracelet_name + " §r§f+" + (useLL ? bracelet_ll : bracelet_dom));

                    List<String> mp_list = new ArrayList<>();
                    if (helmet_mp > 0) mp_list.add(helmet_name + " §r§f+" + helmet_mp);
                    if (chestplate_mp > 0) mp_list.add(chestplate_name + " §r§f+" + chestplate_mp);
                    if (leggings_mp > 0) mp_list.add(leggings_name + " §r§f+" + leggings_mp);
                    if (boots_mp > 0) mp_list.add(boots_name + " §r§f+" + boots_mp);

                    if (necklace_mp > 0) mp_list.add(necklace_name + " §r§f+" + necklace_mp);
                    if (cloak_mp > 0) mp_list.add(cloak_name + " §r§f+" + cloak_mp);
                    if (belt_mp > 0) mp_list.add(belt_name + " §r§f+" + belt_mp);
                    if (bracelet_mp > 0) mp_list.add(bracelet_name + " §r§f+" + bracelet_mp);

                    List<String> runs_list = new ArrayList<>();
                    runs_list.add("§aBasic: §f" + basicComps);
                    runs_list.add("§aHot: §f" + hotComps);
                    runs_list.add("§aBurning: §f" + burningComps);
                    runs_list.add("§aFiery: §f" + fieryComps);
                    runs_list.add("§aInfernal: §f" + infernalComps);
                    runs_list.add("\n§a§lTotal: §f" + totalComps);

                    List<String> magicalPower_list = new ArrayList<>();
                    magicalPower_list.add("§7* §aPower: §f§l" + power);
                    magicalPower_list.add("§7* §aTuning: " + tuning);
                    magicalPower_list.add("§7* §aEnrich: " + enrichment);

                    List<String> rend_list = new ArrayList<>();
                    if (rend_term) {
                        rend_list.add("§7* §aRend Term: §a✔ §7(" +
                                getColor("chimduplexrendswarm", rend_term_rend_lvl) + "Rend " + rend_term_rend_lvl +
                                "§7) §7(" + (rend_term_power7 ? "§aP7: ✔" : "§4P7: X") + "§7) (" +
                                (rend_term_cubism6 ? "§aC6: ✔" : "§4C6: X") + "§7)");
                    } else {
                        rend_list.add("§7* §aRend Term: §4X");
                    }
                    if (rend_bone) {
                        rend_list.add("§7* §aRend Bone: §a✔ §7(" +
                                getColor("chimduplexrendswarm", rend_bone_rend_lvl) + "Rend " + rend_bone_rend_lvl +
                                "§7) §7(" + (rend_bone_power7 ? "§aP7: ✔" : "§4P7: X") + "§7) (" +
                                (rend_bone_cubism6 ? "§aC6: ✔" : "§4C6: X") + "§7)");
                    } else {
                        rend_list.add("§7* §aRend Bone: §4X");
                    }
                    if (rend_blade) {
                        rend_list.add("§7* §aBOTV: §a✔ §7(" +
                                getColor("chimduplexrendswarm", rend_blade_swarm_lvl) + "Swarm " + rend_blade_swarm_lvl +
                                "§7) §7(" + (rend_blade_gk7 ? "§aGK7: ✔" : "§4GK7: X") + "§7) (" +
                                (rend_blade_pros6 ? "§aPROS6: ✔" : "§4PROS6: X") + "§7)");
                    } else {
                        rend_list.add("§7* §aBOTV: §4X");
                    }

                    List<String> user_list = new ArrayList<>();
                    user_list.add("§7* §aUsername: §2" + username);
                    user_list.add(String.format("§7* §aSB level: %s%.2f", getColor("sblvl", (int) skyblock_level), skyblock_level));
                    user_list.add("§7* §aKuudra level: §3" + soreComps / 100);
                    user_list.add("§7* §aPurse: §e" + parseToShorthandNumber(purseBal));
                    if (!isNullOrEmpty(kic) && hasPremium()) {
                        boolean isKicUserOnline = !"Not a KIC user".equals(kic) && kic.contains("Online");
                        String color = isKicUserOnline ? "§a" : "§c";
                        user_list.add("§7* §aKIC: " + color + kic);
                    }

                    List<String> extra_list = new ArrayList<>();
                    extra_list.add("§7* §aReaper: " + (reaper ? "§a✔" : "§4X"));
                    extra_list.add("§7* §aDeployable: " + (deployable ? deployable_name : "§4X"));
                    extra_list.add("§7* §aFire Veil: " + (fire_veil ? fire_veil_name : "§4X"));
                    extra_list.add("§7* §aRadio: " + (radio ? radio_name : "§4X"));
                    extra_list.add("§7* §aLegion:§b " + totalLegion);
                    extra_list.add("§7* §aRep:§b " + Math.max(mageRep, barbRep));
                    extra_list.add("\n§a§lMana Enchants§r§b (" + (totalStrongMana + totalFeroMana)+ ")");
                    extra_list.add(String.format("§7* §aStrong:§b %d (%.2f%% of Mana)", totalStrongMana, totalStrongMana * 0.1));
                    extra_list.add(String.format("§7* §aFero:§b %d (%.2f%% of Mana)", totalFeroMana, totalFeroMana * 0.1));

                    boolean shelmet = false;
                    boolean remedies = false;
                    boolean relic = false;
                    List<String> pets_list = new ArrayList<>();
                    pets_list.add("§7* §aBank: §f" + (banking ? parseToShorthandNumber(bankBal) : api_off));
                    pets_list.add("§7* §aGold: §f" + (collections ? parseToShorthandNumber(goldCol) : api_off));
                    for (JsonElement pet : petsArray) {
                        JsonObject petObject = pet.getAsJsonObject();
                        String petItem = petObject.has("pet_item") && !petObject.get("pet_item").isJsonNull() ? petObject.get("pet_item").getAsString() : "No pet item";
                        int petLvl = petObject.get("level").getAsInt();

                        if ("Dwarf Turtle Shelmet".equals(petItem) && petLvl == 200) shelmet = true;
                        if ("Antique Remedies".equals(petItem) && petLvl == 200) remedies = true;
                        if ("Minos Relic".equals(petItem) && petLvl == 200) relic = true;

                        pets_list.add(String.format("\n§7[Lvl %d] §6%s", petLvl, petObject.get("name").getAsString()));
                        pets_list.add(String.format("§7* §aPet item: §f%s", petItem));
                    }

                    String llOrDom_lore = String.format("§a§l%s Breakdown:\n\n", useLL ? "Lifeline" : "Dominance") + String.join("\n", llOrDom_list);
                    String mp_lore = "§a§lMana Pool Breakdown:\n\n" + String.join("\n", mp_list);
                    String runs_lore = "§a§lRuns Breakdown:\n\n" + String.join("\n", runs_list);
                    String rend_lore = "§a§lRend Info:\n\n" + String.join("\n", rend_list);
                    String extra_lore = "§a§lExtra Info:\n\n" + String.join("\n", extra_list);
                    String pets_lore = "§a§lPets Info:\n\n" + String.join("\n", pets_list);
                    String magicalPower_lore = "§a§lMP Breakdown:\n\n" + String.join("\n", magicalPower_list);
                    String user_lore = "§a§lUser Info:\n\n" + String.join("\n", user_list);

                    boolean rendInfo = rend_bone || rend_blade;

                    int ancientCount = 0;
                    if (helmet_name.contains("Ancient")) ancientCount++;
                    if (chestplate_name.contains("Ancient")) ancientCount++;
                    if (leggings_name.contains("Ancient")) ancientCount++;
                    if (boots_name.contains("Ancient")) ancientCount++;

                    boolean withered = rag_lore.contains("Withered");

                    // Message creation
                    String startString = String.format("§2§m-----§f[- §7[%s%d§7] §2%s §f-]§2§m-----\n", getColor("sblvl", (int) skyblock_level), (int) skyblock_level, username);
                    String endString = generateDashString(startString, "§2§m");
                    mainMessage.appendSibling(createHoverAndClickComponent(true, startString, user_lore, "/pv " + username));
                    mainMessage.appendSibling(createHoverComponent(useLL ? totalLL > 0 : totalDom > 0, String.format("§a%s: %s%d §7(§f+§c%.2f%%§7)\n", useLL ? "Lifeline" : "Dominance", getColor("lldommp", useLL ? totalLL : totalDom), useLL ? totalLL : totalDom, useLL ? totalLL * 2.5 : totalDom * 1.5), llOrDom_lore));
                    mainMessage.appendSibling(createHoverComponent(inventory, String.format("§aMana Pool: %s%d §7(§f+§b%d Intel§7)\n", getColor("lldommp", totalMp), totalMp, totalMp * 20), mp_lore));
                    mainMessage.appendSibling(createHoverComponent(true, String.format("§aRuns: %s%d §7(%s%d§7)\n", getColor("runs", infernalComps), infernalComps, getColor("runs", totalComps), totalComps), runs_lore));
                    mainMessage.appendSibling(createHoverComponent(true, String.format("§aMagical Power: %s%d\n", getColor("magicalPower", magicalPower), magicalPower), magicalPower_lore));
                    int effectiveCataLevel = (int) Math.min(cata_level, 50);
                    mainMessage.appendSibling(new ChatComponentText(String.format(
                            "§aCata: %s%.2f §7(§f+§9%.2f☠§7) §7(§f+§c%.2f❁§7)\n",
                            getColor("cata", (int) cata_level),
                            cata_level,
                            (double) effectiveCataLevel * ancientCount,
                            withered ? (effectiveCataLevel * 1.5) : 0
                    )));

                    if (inventory) {
                        mainMessage.appendSibling(createHoverComponent(hyperion, "\n§aHyperion: " + (hyperion ? "§a✔" : "§4X"), hyperion_lore));
                        if (duplex) {
                            mainMessage.appendSibling(createHoverComponent(true, "\n§aTerminator: §a✔ §7(" +
                                    getColor("chimduplexrendswarm", duplex_lvl) + "Duplex " + duplex_lvl +
                                    "§7) §7(" + (duplex_power7 ? "§aP7: ✔" : "§4P7: X") + "§7) (" +
                                    (duplex_cubism6 ? "§aC6: ✔" : "§4C6: X") + "§7)", duplex_lore));
                        } else if (rend_term) {
                            mainMessage.appendSibling(createHoverComponent(true, "\n§aTerminator: §a✔ §7(" +
                                    getColor("chimduplexrendswarm", rend_term_rend_lvl) + "Rend " + rend_term_rend_lvl +
                                    "§7) §7(" + (rend_term_power7 ? "§aP7: ✔" : "§4P7: X") + "§7) (" +
                                    (rend_term_cubism6 ? "§aC6: ✔" : "§4C6: X") + "§7)", rend_term_lore));
                        } else if (term) {
                            mainMessage.appendSibling(createHoverComponent(true, "\n§aTerminator: §a✔ §7(" +
                                    (term_power7 ? "§aP7: ✔" : "§4P7: X") + "§7) (" +
                                    (term_cubism6 ? "§aC6: ✔" : "§4C6: X") + "§7)", term_lore));
                        } else {
                            mainMessage.appendSibling(new ChatComponentText("\n§aTerminator: §4X"));
                        }
                        mainMessage.appendSibling(createHoverComponent(rag, "\n§aRagnarock Axe: " + (rag ? String.format("§a✔ §7(%sChim %d§7) §7(%s§7)", getColor("chimduplexrendswarm", rag_chim), rag_chim, getGemColor(rag_gem) + "❁") : "§4X"), rag_lore));
                        if (rendInfo) {
                            mainMessage.appendSibling(createHoverComponent(true, "\n§aRend Info §7[HOVER]", rend_lore));
                        }
                        mainMessage.appendSibling(createHoverComponent(true, "\n§aExtra Info §7[HOVER]\n", extra_lore));

                        mainMessage.appendSibling(createHoverComponent(helmet, String.format("\n§a%s* %s* %s", getGemColor(helmet_gem_1), getGemColor(helmet_gem_2), (helmet ? helmet_name.replace("✿ ", "") : "§cNo helmet")), helmet_lore));
                        mainMessage.appendSibling(createHoverComponent(chestplate, String.format("\n§a%s* %s* %s", getGemColor(chestplate_gem_1), getGemColor(chestplate_gem_2), (chestplate ? chestplate_name.replace("✿ ", "") : "§cNo chestplate")), chestplate_lore));
                        mainMessage.appendSibling(createHoverComponent(leggings, String.format("\n§a%s* %s* %s", getGemColor(leggings_gem_1), getGemColor(leggings_gem_2), (leggings ? leggings_name.replace("✿ ", "") : "§cNo leggings")), leggings_lore));
                        mainMessage.appendSibling(createHoverComponent(boots, String.format("\n§a%s* %s* %s", getGemColor(boots_gem_1), getGemColor(boots_gem_2), (boots ? boots_name.replace("✿ ", "") : "§cNo boots")), boots_lore));
                    } else {
                        mainMessage.appendSibling(new ChatComponentText("\n§aHyperion: " + api_off));
                        mainMessage.appendSibling(new ChatComponentText("\n§aTerminator: " + api_off));
                        mainMessage.appendSibling(new ChatComponentText("\n§aRagnarock Axe: " + api_off));

                        mainMessage.appendSibling(new ChatComponentText("\n\n§7* * " + api_off));
                        mainMessage.appendSibling(new ChatComponentText("\n§7* * " + api_off));
                        mainMessage.appendSibling(new ChatComponentText("\n§7* * " + api_off));
                        mainMessage.appendSibling(new ChatComponentText("\n§7* * " + api_off));
                    }
                    mainMessage.appendSibling(createHoverComponent(true, hasPet ? String.format("\n%s* %s §7(%s§7) §7(%s§7)", oneBilBank ? "§a" : "§c", firstPetName, (shelmet ? "§aShelmet: ✔" : "§4Shelmet: X"), (remedies ? "§aRemedies: ✔" : (relic ? "§aMinos: ✔" : "§4Remedies: X"))) : "\n§c* No golden dragon", pets_lore));

                    mainMessage.appendSibling(new ChatComponentText(endString));

                    if (!manual && (!PartyUtils.inParty() || PartyUtils.amILeader())) {
                        mainMessage.appendSibling(createClickComponent(true, String.format("\n§cRemove §4%s §cfrom the party", username), ClickEvent.Action.RUN_COMMAND, "/party kick " + username));
                        autoKick(
                                username, uuid, (useLL ? totalLL : totalDom), totalMp, (int) cata_level, infernalComps, magicalPower,
                                rag_chim, duplex_lvl, rag_gem, chestplate_name, leggings_name, boots_name, duplex_power7,
                                duplex_cubism6, hyperion, totalLegion, (totalStrongMana + totalFeroMana), bankBal, goldCol,
                                firstPetLevel, inventory, banking, collections
                        );
                    }
                    break;
            }
            return mainMessage;
        } catch (Exception e) {
            KICLogger.error(e.getMessage());
            return null;
        }
    }

    private static String getColor(String type, int value) {
        switch (type) {
            case "lldommp":
                if (value < 42) return "§4";
                if (value < 58) return "§e";
                if (value < 70) return "§2";
                if (value == 70) return "§3";
                return "§f";

            case "runs":
                if (value < 500) return "§4";
                if (value < 1000) return "§e";
                if (value < 5000) return "§2";
                return "§3";

            case "magicalPower":
                if (value < 1350) return "§4";
                if (value < 1600) return "§e";
                if (value < 1750) return "§2";
                return "§3";

            case "chimduplexrendswarm":
                if (value == 1) return "§a";
                if (value == 2) return "§9";
                if (value == 3) return "§5";
                if (value == 4) return "§6";
                if (value == 5) return "§d";
                return "§f";

            case "cata":
                if (value <= 10) return "§4";
                if (value <= 20) return "§c";
                if (value <= 30) return "§e";
                if (value <= 40) return "§6";
                if (value < 50) return "§2";
                return "§3";

            case "sblvl":
                switch (value / 40) {
                    case 0: return "§7";  // Default (Gray)
                    case 1: return "§f";  // White
                    case 2: return "§e";  // Yellow
                    case 3: return "§a";  // Green
                    case 4: return "§2";  // Dark Green
                    case 5: return "§b";  // Aqua
                    case 6: return "§3";  // Cyan
                    case 7: return "§9";  // Blue
                    case 8: return "§d";  // Pink
                    case 9: return "§5";  // Purple
                    case 10: return "§6"; // Gold
                    case 11: return "§c"; // Red
                    case 12: return "§4"; // Dark Red
                    default: return "§4"; // Dark Red (For 480+)
                }

            default:
                return "§f";
        }
    }

    private static String getGemColor(String type) {
        switch (type) {
            case "PERFECT":
                return "§6";

            case "FLAWLESS":
                return "§d";

            case "FINE":
                return "§9";

            case "FLAWED":
                return "§a";

            case "ROUGH":
                return "§f";

            default:
                return "§7";
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    public void onChat(ClientChatReceivedEvent event) {
        if (!KICConfig.partyFinder || !ApiUtils.isVerified()) return;
        String message = event.message.getUnformattedText();
        Matcher matcher = partyJoinPattern.matcher(message);

        if (matcher.find()) {
            String player = matcher.group(1);
            String me = KIC.mc.thePlayer == null ? "" : KIC.mc.thePlayer.getName();
            if (player.equalsIgnoreCase(me)) return;
            Multithreading.runAsync(() -> KuudraUserInfo.showKuudraInfo(player, false));
        }
    }
}
