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
import someoneok.kic.models.kuudra.AutoKickProfile;
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
import static someoneok.kic.utils.ApiUtils.apiHost;
import static someoneok.kic.utils.ApiUtils.hasPremium;
import static someoneok.kic.utils.ChatUtils.*;
import static someoneok.kic.utils.PlayerUtils.getPlayerName;
import static someoneok.kic.utils.StringUtils.*;

public class KuudraUserInfo {
    private static final Pattern PARTY_JOIN_PATTERN = Pattern.compile("^Party Finder > (.+) joined the group! (.+)$");
    private static final String[] TALISMANS = {"§4No Talisman", "§cKuudra's Kidney", "§aKuudra's Lung", "§2Kuudra's Heart"};

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
            playerInfo = JsonUtils.parseString(NetworkUtils.sendGetRequest(apiHost() + "/hypixel/info/" + formattedPlayer + "?type=KUUDRA", true)).getAsJsonObject();
        } catch (APIException e) {
            sendMessageToPlayer(String.format("%s §c%s", KICPrefix, e.getMessage()));
            return;
        }

        if (playerInfo == null || !playerInfo.isJsonObject()) {
            sendMessageToPlayer(KICPrefix + " §cError while retrieving data!");
            return;
        }

        ChatComponentText message = makeMessage(playerInfo, manual, false);

        if (message == null) {
            sendMessageToPlayer(KICPrefix + " §cError while retrieving data!");
            return;
        }

        sendMessageToPlayer(message);
    }

    public static ChatComponentText makeMessage(JsonObject infoObject, boolean manual, boolean gui) {
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
            boolean term_smold5 = extra.get("term_smold5").getAsBoolean();
            String term_lore = extra.get("term_lore").getAsString();
            boolean deployable = extra.get("deployable").getAsBoolean();
            String deployable_name = extra.get("deployable_name").getAsString();
            boolean fire_veil = extra.get("fire_veil").getAsBoolean();
            String fire_veil_name = extra.get("fire_veil_name").getAsString();
            boolean radio = extra.get("radio").getAsBoolean();
            String radio_name = extra.get("radio_name").getAsString();
            boolean hollow = extra.get("hollow").getAsBoolean();

            // Rend
            boolean rend_term = rend.get("term").getAsBoolean();
            String rend_term_name = rend.get("term_name").getAsString();
            int rend_term_rend_lvl = rend.get("term_rend_lvl").getAsInt();
            boolean rend_term_power7 = rend.get("term_power7").getAsBoolean();
            boolean rend_term_cubism6 = rend.get("term_cubism6").getAsBoolean();
            boolean rend_term_smold5 = rend.get("term_smold5").getAsBoolean();
            boolean rend_bone = rend.get("bone").getAsBoolean();
            String rend_bone_name = rend.get("bone_name").getAsString();
            int rend_bone_rend_lvl = rend.get("bone_rend_lvl").getAsInt();
            boolean rend_bone_power7 = rend.get("bone_power7").getAsBoolean();
            boolean rend_bone_cubism6 = rend.get("bone_cubism6").getAsBoolean();
            boolean rend_bone_smold5 = rend.get("bone_smold5").getAsBoolean();
            boolean rend_tux = rend.get("tux").getAsBoolean();
            int rend_tux_hab = rend.get("tux_hab_lvl").getAsInt();
            boolean rend_blade = rend.get("blade").getAsBoolean();
            String rend_blade_name = rend.get("blade_name").getAsString();
            boolean rend_blade_gk7 = rend.get("blade_gk7").getAsBoolean();
            boolean rend_blade_pros6 = rend.get("blade_pros6").getAsBoolean();

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
            double foraging_level = stats.get("foraging_level").getAsDouble();
            int talismanTier = stats.get("talisman_tier").getAsInt();
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
            boolean duplex_smold5 = items.get("duplex_term_smold5").getAsBoolean();
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
            boolean chestplate = armor.get("chestplate").getAsBoolean();
            String chestplate_name = armor.get("chestplate_name").getAsString();
            String chestplate_lore = armor.get("chestplate_lore").getAsString();
            JsonArray chestplate_gemstones = armor.getAsJsonArray("chestplate_gemstones");
            String chestplate_gem_1 = chestplate_gemstones.size() > 0 ? chestplate_gemstones.get(0).getAsString() : "NONE";
            String chestplate_gem_2 = chestplate_gemstones.size() > 1 ? chestplate_gemstones.get(1).getAsString() : "NONE";
            boolean leggings = armor.get("leggings").getAsBoolean();
            String leggings_name = armor.get("leggings_name").getAsString();
            String leggings_lore = armor.get("leggings_lore").getAsString();
            JsonArray leggings_gemstones = armor.getAsJsonArray("leggings_gemstones");
            String leggings_gem_1 = leggings_gemstones.size() > 0 ? leggings_gemstones.get(0).getAsString() : "NONE";
            String leggings_gem_2 = leggings_gemstones.size() > 1 ? leggings_gemstones.get(1).getAsString() : "NONE";
            boolean boots = armor.get("boots").getAsBoolean();
            String boots_name = armor.get("boots_name").getAsString();
            String boots_lore = armor.get("boots_lore").getAsString();
            JsonArray boots_gemstones = armor.getAsJsonArray("boots_gemstones");
            String boots_gem_1 = boots_gemstones.size() > 0 ? boots_gemstones.get(0).getAsString() : "NONE";
            String boots_gem_2 = boots_gemstones.size() > 1 ? boots_gemstones.get(1).getAsString() : "NONE";

            // Equipment
            boolean necklace = equipment.get("necklace").getAsBoolean();
            String necklace_name = equipment.get("necklace_name").getAsString();
            String necklace_lore = equipment.get("necklace_lore").getAsString();
            boolean cloak = equipment.get("cloak").getAsBoolean();
            String cloak_name = equipment.get("cloak_name").getAsString();
            String cloak_lore = equipment.get("cloak_lore").getAsString();
            boolean belt = equipment.get("belt").getAsBoolean();
            String belt_name = equipment.get("belt_name").getAsString();
            String belt_lore = equipment.get("belt_lore").getAsString();
            boolean bracelet = equipment.get("bracelet").getAsBoolean();
            String bracelet_name = equipment.get("bracelet_name").getAsString();
            String bracelet_lore = equipment.get("bracelet_lore").getAsString();

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

            // Setup
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
            extra_list.add("§7* §aHollow: " + (hollow ? "§a✔" : "§4X"));
            extra_list.add("§7* §aDeployable: " + (deployable ? deployable_name : "§4X"));
            extra_list.add("§7* §aFire Veil: " + (fire_veil ? fire_veil_name : "§4X"));
            extra_list.add("§7* §aRadio: " + (radio ? radio_name : "§4X"));
            extra_list.add("§7* §aLegion:§b " + totalLegion);
            extra_list.add("§7* §aRep:§b " + Math.max(mageRep, barbRep));
            extra_list.add("§7* §aForaging Level: " + getColor("skill", (int) foraging_level) + foraging_level);
            extra_list.add("\n§a§lMana Enchants§r§b (" + (totalStrongMana + totalFeroMana)+ ")");
            extra_list.add(String.format("§7* §aStrong:§b %d (%.2f%% of Mana)", totalStrongMana, totalStrongMana * 0.1));
            extra_list.add(String.format("§7* §aFero:§b %d (%.2f%% of Mana)", totalFeroMana, totalFeroMana * 0.1));

            boolean hephaestusRemedies = false;
            boolean remedies = false;
            boolean hephaestusRelic = false;
            boolean relic = false;
            boolean biggerTeeth = false;

            List<String> pets_list = new ArrayList<>();
            pets_list.add("§7* §aBank: §f" + (banking ? parseToShorthandNumber(bankBal) : api_off));
            pets_list.add("§7* §aGold: §f" + (collections ? parseToShorthandNumber(goldCol) : api_off));

            for (JsonElement elem : petsArray) {
                JsonObject pet = elem.getAsJsonObject();

                int level = pet.has("level") ? pet.get("level").getAsInt() : 0;
                String name = pet.has("name") ? pet.get("name").getAsString() : "Unknown";
                String petItem = pet.has("pet_item") && !pet.get("pet_item").isJsonNull()
                        ? pet.get("pet_item").getAsString()
                        : "No pet item";
                String petItemId = pet.has("pet_item_id") && !pet.get("pet_item_id").isJsonNull()
                        ? pet.get("pet_item_id").getAsString()
                        : "NONE";

                if (level == 200) {
                    switch (petItemId) {
                        case "HEPHAESTUS_REMEDIES":
                            hephaestusRemedies = true;
                            break;
                        case "ANTIQUE_REMEDIES":
                            remedies = true;
                            break;
                        case "HEPHAESTUS_RELIC":
                            hephaestusRelic = true;
                            break;
                        case "MINOS_RELIC":
                            relic = true;
                            break;
                        case "BIGGER_TEETH":
                            biggerTeeth = true;
                        default:
                            break;
                    }
                }

                pets_list.add(String.format("\n§7[Lvl %d] §6%s", level, name));
                pets_list.add("§7* §aPet item: §f" + petItem);
            }

            boolean hasRend = rend_bone && rend_blade;
            List<String> rend_list = new ArrayList<>();
            if (rend_term) {
                rend_list.add(String.format(
                        "§7* §aTerm: %s §7(%sRend %d§7) §7(%s§7) (%s§7) (%s§7)",
                        rend_term_name,
                        getColor("chimduplexrend", rend_term_rend_lvl), rend_term_rend_lvl,
                        rend_term_power7 ? "§aP7: ✔" : "§4P7: X",
                        rend_term_cubism6 ? "§aC6: ✔" : "§4C6: X",
                        rend_term_smold5 ? "§aSMOLD5: ✔" : "§4SMOLD5: X"
                ));
            } else {
                rend_list.add("§7* §aTerm: §4X");
            }
            if (rend_bone) {
                rend_list.add(String.format(
                        "§7* §aBone: %s §7(%sRend %d§7) §7(%s§7) (%s§7) (%s§7)",
                        rend_bone_name,
                        getColor("chimduplexrend", rend_bone_rend_lvl), rend_bone_rend_lvl,
                        rend_bone_power7 ? "§aP7: ✔" : "§4P7: X",
                        rend_bone_cubism6 ? "§aC6: ✔" : "§4C6: X",
                        rend_bone_smold5 ? "§aSMOLD5: ✔" : "§4SMOLD5: X"
                ));
            } else {
                rend_list.add("§7* §aBone: §4X");
            }
            if (rend_blade) {
                rend_list.add(String.format(
                        "§7* §aBlade: %s §7(%s§7) (%s§7)",
                        rend_blade_name,
                        rend_blade_gk7 ? "§aGK7: ✔" : "§4GK7: X",
                        rend_blade_pros6 ? "§aPROS6: ✔" : "§4PROS6: X"
                ));
            } else {
                rend_list.add("§7* §aBlade: §4X");
            }
            if (rend_tux) {
                rend_list.add(String.format(
                        "§7* §aTux: §a✔ §7(%sHab %d§7)",
                        getColor("chimduplexrend", rend_tux_hab), rend_tux_hab
                ));
            } else {
                rend_list.add("§7* §aTux: §4X");
            }
            if (biggerTeeth) {
                rend_list.add("§7* §aBigger Teeth: §a✔");
            } else {
                rend_list.add("§7* §aBigger Teeth: §4X");
            }

            String runs_lore = "§a§lRuns Breakdown:\n\n" + String.join("\n", runs_list);
            String extra_lore = "§a§lExtra Info:\n\n" + String.join("\n", extra_list);
            String rend_lore = "§a§lRend Info:\n\n" + String.join("\n", rend_list);
            String pets_lore = "§a§lPets Info:\n\n" + String.join("\n", pets_list);
            String magicalPower_lore = "§a§lMP Breakdown:\n\n" + String.join("\n", magicalPower_list);
            String user_lore = "§a§lUser Info:\n\n" + String.join("\n", user_list);

            int ancientCount = 0;
            if (helmet_name.contains("Ancient")) ancientCount++;
            if (chestplate_name.contains("Ancient")) ancientCount++;
            if (leggings_name.contains("Ancient")) ancientCount++;
            if (boots_name.contains("Ancient")) ancientCount++;

            boolean withered = rag_lore.contains("Withered");

            // Message creation
            ChatComponentText mainMessage = new ChatComponentText("");
            String startString = String.format("§2§m-----§f[- §7[%s%d§7] §2%s §f-]§2§m-----\n", getColor("sblvl", (int) skyblock_level), (int) skyblock_level, username);
            String endString = generateDashString(startString, "§2§m");
            mainMessage.appendSibling(createHoverAndClickComponent(true, startString, user_lore, ClickEvent.Action.RUN_COMMAND, "/pv " + username));
            mainMessage.appendSibling(createHoverComponent(true, String.format("§aRuns: %s%d §7(%s%d§7)\n", getColor("runs", infernalComps), infernalComps, getColor("runs", totalComps), totalComps), runs_lore));
            mainMessage.appendSibling(createHoverComponent(true, String.format("§aMagical Power: %s%d\n", getColor("magicalPower", magicalPower), magicalPower), magicalPower_lore));
            int effectiveCataLevel = (int) Math.min(cata_level, 50);
            mainMessage.appendSibling(new ChatComponentText(String.format(
                    "§aCata: %s%.2f §7(§f+§9%.2f☠§7) §7(§f+§c%.2f❁§7)\n",
                    getColor("skill", (int) cata_level),
                    cata_level,
                    (double) effectiveCataLevel * ancientCount,
                    withered ? (effectiveCataLevel * 1.5) : 0
            )));

            if (inventory) {
                mainMessage.appendSibling(createHoverComponent(hyperion, "\n§aHyperion: " + (hyperion ? "§a✔" : "§4X"), hyperion_lore));
                if (duplex) {
                    mainMessage.appendSibling(createHoverComponent(
                            true,
                            String.format(
                                    "\n§aTerminator: §a✔ §7(%sDuplex %d§7) §7(%s§7) (%s§7) (%s§7)",
                                    getColor("chimduplexrend", duplex_lvl), duplex_lvl,
                                    duplex_power7 ? "§aP7: ✔" : "§4P7: X",
                                    duplex_cubism6 ? "§aC6: ✔" : "§4C6: X",
                                    duplex_smold5 ? "§aSMOLD5: ✔" : "§4SMOLD5: X"
                            ),
                            duplex_lore
                    ));
                } else if (term) {
                    mainMessage.appendSibling(createHoverComponent(
                            true,
                            String.format(
                                    "\n§aTerminator: §a✔ §7(%s§7) (%s§7) (%s§7)",
                                    term_power7 ? "§aP7: ✔" : "§4P7: X",
                                    term_cubism6 ? "§aC6: ✔" : "§4C6: X",
                                    term_smold5 ? "§aSMOLD5: ✔" : "§4SMOLD5: X"
                            ),
                            term_lore
                    ));
                } else {
                    mainMessage.appendSibling(new ChatComponentText("\n§aTerminator: §4X"));
                }

                mainMessage.appendSibling(createHoverComponent(rag, "\n§aRagnarock Axe: " + (rag ? String.format("§a✔ §7(%sChim %d§7) §7(%s§7)", getColor("chimduplexrend1", rag_chim), rag_chim, getGemColor(rag_gem) + "❁") : "§4X"), rag_lore));
                mainMessage.appendSibling(new ChatComponentText("\n§aTalisman: " + TALISMANS[talismanTier]));

                if (!gui) {
                    if (hasRend) {
                        mainMessage.appendSibling(createHoverComponent(true, "\n§aRend Info §7[HOVER]", rend_lore));
                    }
                    mainMessage.appendSibling(createHoverComponent(true, "\n§aExtra Info §7[HOVER]\n", extra_lore));
                }

                mainMessage.appendSibling(createHoverComponent(helmet, String.format("\n§a%s* %s* %s", getGemColor(helmet_gem_1), getGemColor(helmet_gem_2), (helmet ? helmet_name.replace("✿ ", "") : "§cNo helmet")), helmet_lore));
                mainMessage.appendSibling(createHoverComponent(chestplate, String.format("\n§a%s* %s* %s", getGemColor(chestplate_gem_1), getGemColor(chestplate_gem_2), (chestplate ? chestplate_name.replace("✿ ", "") : "§cNo chestplate")), chestplate_lore));
                mainMessage.appendSibling(createHoverComponent(leggings, String.format("\n§a%s* %s* %s", getGemColor(leggings_gem_1), getGemColor(leggings_gem_2), (leggings ? leggings_name.replace("✿ ", "") : "§cNo leggings")), leggings_lore));
                mainMessage.appendSibling(createHoverComponent(boots, String.format("\n§a%s* %s* %s", getGemColor(boots_gem_1), getGemColor(boots_gem_2), (boots ? boots_name.replace("✿ ", "") : "§cNo boots")), boots_lore));

                if (!gui) {
                    mainMessage.appendSibling(createHoverComponent(necklace, String.format("\n\n%s", (necklace ? necklace_name : "§cNo necklace")), necklace_lore));
                    mainMessage.appendSibling(createHoverComponent(cloak, String.format("\n%s", (cloak ? cloak_name : "§cNo cloak")), cloak_lore));
                    mainMessage.appendSibling(createHoverComponent(belt, String.format("\n%s", (belt ? belt_name : "§cNo belt")), belt_lore));
                    mainMessage.appendSibling(createHoverComponent(bracelet, String.format("\n%s", (bracelet ? bracelet_name : "§cNo bracelet")), bracelet_lore));
                }
            } else {
                mainMessage.appendSibling(new ChatComponentText("\n§aHyperion: " + api_off));
                mainMessage.appendSibling(new ChatComponentText("\n§aTerminator: " + api_off));
                mainMessage.appendSibling(new ChatComponentText("\n§aRagnarock Axe: " + api_off));

                mainMessage.appendSibling(new ChatComponentText("\n\n§7* * " + api_off));
                mainMessage.appendSibling(new ChatComponentText("\n§7* * " + api_off));
                mainMessage.appendSibling(new ChatComponentText("\n§7* * " + api_off));
                mainMessage.appendSibling(new ChatComponentText("\n§7* * " + api_off));
            }
            String petItemMsg;
            if (hephaestusRemedies) petItemMsg = "§aHephaestus Remedies: ✔";
            else if (remedies) petItemMsg = "§aRemedies: ✔";
            else if (hephaestusRelic) petItemMsg = "§aHephaestus Relic: ✔";
            else if (relic) petItemMsg = "§aRelic: ✔";
            else petItemMsg = "§4Remedies: X";
            mainMessage.appendSibling(createHoverComponent(true, hasPet ? String.format("\n%s* %s §7(%s§7)", oneBilBank ? "§a" : "§c", firstPetName, petItemMsg) : "\n§c* No golden dragon", pets_lore));

            mainMessage.appendSibling(new ChatComponentText(endString));

            if (!manual && (!PartyUtils.inParty() || PartyUtils.amILeader())) {
                mainMessage.appendSibling(createClickComponent(true, String.format("\n§cRemove §4%s §cfrom the party", username), ClickEvent.Action.RUN_COMMAND, "/party kick " + username));
                AutoKickProfile profile = AutoKickProfile.builder()
                        .user(username)
                        .uuid(uuid)
                        .cataLevel((int) cata_level)
                        .foragingLevel((int) foraging_level)
                        .t5Comps(infernalComps)
                        .magicalPower(magicalPower)
                        .ragChimLevel(rag_chim)
                        .termDuplexLevel(duplex_lvl)
                        .talismanTier(talismanTier)
                        .ragGemstone(rag_gem)
                        .chestplate(chestplate_name)
                        .leggings(leggings_name)
                        .boots(boots_name)
                        .termP7(duplex_power7)
                        .termC6(duplex_cubism6)
                        .termSmold5(duplex_smold5)
                        .witherImpact(hyperion)
                        .legionLevel(totalLegion)
                        .strongFeroMana((totalStrongMana + totalFeroMana))
                        .rend(hasRend)
                        .rendTerm(rend_term)
                        .boneP7(rend_bone_power7)
                        .boneC6(rend_bone_cubism6)
                        .boneSmold5(rend_bone_smold5)
                        .bladeGK7(rend_blade_gk7)
                        .bladePros6(rend_blade_pros6)
                        .habLevel(rend_tux_hab)
                        .biggerTeeth(biggerTeeth)
                        .bankBalance(bankBal)
                        .goldCollection(goldCol)
                        .goldenDragonLevel(firstPetLevel)
                        .hephaestusRemedies(hephaestusRemedies)
                        .inventoryApi(inventory)
                        .bankingApi(banking)
                        .collectionsApi(collections)
                        .build();

                autoKick(profile);
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

            case "chimduplexrend":
                if (value == 1) return "§a";
                if (value == 2) return "§9";
                if (value == 3) return "§5";
                if (value == 4) return "§6";
                if (value == 5) return "§d";
                return "§f";

            case "skill":
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
        if (!message.contains("Party Finder >")) return;

        Matcher matcher = PARTY_JOIN_PATTERN.matcher(message);
        if (matcher.find()) {
            String player = matcher.group(1);
            if (player.equalsIgnoreCase(getPlayerName())) return;
            Multithreading.runAsync(() -> KuudraUserInfo.showKuudraInfo(player, false));
        }
    }
}
