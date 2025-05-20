package someoneok.kic.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import someoneok.kic.KIC;
import someoneok.kic.models.request.AttributeUpgradeRequest;
import someoneok.kic.modules.crimson.AttributeUpgrade;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isNumeric;
import static someoneok.kic.KIC.ATTRIBUTES;
import static someoneok.kic.utils.GeneralUtils.sendMessageToPlayer;

public class AttributeUpgradeCommand  extends CommandBase {
    private static final List<String> KUUDRA_ITEMS = Arrays.asList(
            "attribute_shard",
            "hollow_helmet",
            "fervor_helmet",
            "terror_helmet",
            "crimson_helmet",
            "aurora_helmet",
            "hollow_chestplate",
            "fervor_chestplate",
            "terror_chestplate",
            "crimson_chestplate",
            "aurora_chestplate",
            "hollow_leggings",
            "fervor_leggings",
            "terror_leggings",
            "crimson_leggings",
            "aurora_leggings",
            "hollow_boots",
            "fervor_boots",
            "terror_boots",
            "crimson_boots",
            "aurora_boots",
            "molten_necklace",
            "lava_shell_necklace",
            "delirium_necklace",
            "magma_necklace",
            "vanquished_magma_necklace",
            "molten_cloak",
            "scourge_cloak",
            "ghast_cloak",
            "vanquished_ghast_cloak",
            "molten_belt",
            "implosion_belt",
            "scoville_belt",
            "blaze_belt",
            "vanquished_blaze_belt",
            "molten_bracelet",
            "gauntlet_of_contagion",
            "flaming_fist",
            "glowstone_gauntlet",
            "vanquished_glowstone_gauntlet",
            "magma_lord_helmet",
            "thunder_helmet",
            "taurus_helmet",
            "magma_lord_chestplate",
            "thunder_chestplate",
            "taurus_chestplate",
            "magma_lord_leggings",
            "thunder_leggings",
            "taurus_leggings",
            "magma_lord_boots",
            "thunder_boots",
            "taurus_boots",
            "magma_rod",
            "inferno_rod",
            "hellfire_rod",
            "magma_lord_gauntlet",
            "thunderbolt_necklace",
            "wand_of_strength",
            "blade_of_the_volcano",
            "sword_of_bad_health",
            "rekindled_ember_helmet",
            "rekindled_ember_chestplate",
            "rekindled_ember_leggings",
            "rekindled_ember_boots",
            "fire_veil_wand",
            "ragnarock_axe",
            "fire_fury_staff"
    );

    @Override
    public String getCommandName() {
        return "au";
    }

    @Override
    public List<String> getCommandAliases() {
        return Arrays.asList("attributeupgrade", "kicau");
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/au <item> <attribute> <start level> <end level>";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length != 4) {
            sendMessageToPlayer(KIC.KICPrefix + " §cUsage: /au <item> <attribute> <start level> <end level>");
            return;
        }

        String item = args[0];
        String attribute = args[1];
        int startLvl;
        int endLvl;

        if (isNumeric(args[2])) {
            startLvl = Integer.parseInt(args[2]);
            if (isInvalidLevel(startLvl)) return;
            if (startLvl == 10) {
                sendMessageToPlayer(KIC.KICPrefix + " §cThe starting level cannot be 10.");
                return;
            }
        } else {
            sendMessageToPlayer(KIC.KICPrefix + " §cInvalid input: Start level must be a number.");
            return;
        }

        if (isNumeric(args[3])) {
            endLvl = Integer.parseInt(args[3]);
            if (isInvalidLevel(endLvl)) return;
            if (endLvl < startLvl) {
                sendMessageToPlayer(KIC.KICPrefix + " §cThe ending level must be higher than the starting level.");
                return;
            }
        } else {
            sendMessageToPlayer(KIC.KICPrefix + " §cInvalid input: End level must be a number.");
            return;
        }

        AttributeUpgrade.show(new AttributeUpgradeRequest(attribute, item, startLvl, endLvl));
    }

    private boolean isInvalidLevel(int level) {
        if (level < 1 || level > 10) {
            sendMessageToPlayer(KIC.KICPrefix + " §cInvalid level: Level must be between 1 and 10.");
            return true;
        }
        return false;
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        if (args.length == 1) {
            String lastArg = args[0].toLowerCase();
            return KUUDRA_ITEMS.stream()
                    .filter(item -> item.toLowerCase().startsWith(lastArg))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            String lastArg = args[1].toLowerCase();
            return ATTRIBUTES.stream()
                    .filter(attr -> attr.toLowerCase().startsWith(lastArg))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
