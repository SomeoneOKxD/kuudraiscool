package someoneok.kic.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import someoneok.kic.KIC;
import someoneok.kic.config.KICConfig;
import someoneok.kic.models.request.AuctionDataRequest;
import someoneok.kic.modules.crimson.auction.KICAuction;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isNumeric;
import static someoneok.kic.KIC.ATTRIBUTES;
import static someoneok.kic.utils.GeneralUtils.sendMessageToPlayer;

public class KICAuctionCommand extends CommandBase {
    @Override
    public String getCommandName() {
        return "ka";
    }

    @Override
    public List<String> getCommandAliases() {
        return Arrays.asList("kicaction", "kicah");
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/ka <attribute> [level] [attribute] [level]";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 0) {
            if (KICAuction.validAuctionData()) {
                KICAuction.open();
            } else {
                sendMessageToPlayer(KIC.KICPrefix + " §cNo previous price data available!");
            }
            return;
        }

        AuctionDataRequest request = new AuctionDataRequest();
        request.setAttribute1(args[0]);

        if (args.length > 1) {
            if (isNumeric(args[1])) {
                int level1 = Integer.parseInt(args[1]);
                if (isInvalidLevel(level1)) return;

                request.setAttributeLvl1(level1);
                if (args.length > 2) {
                    request.setAttribute2(args[2]);

                    if (args.length > 3 && isNumeric(args[3])) {
                        int level2 = Integer.parseInt(args[3]);
                        if (isInvalidLevel(level2)) return;

                        request.setAttributeLvl2(level2);
                    }
                }
            } else {
                request.setAttribute2(args[1]);
                if (args.length > 2 && isNumeric(args[2])) {
                    int level2 = Integer.parseInt(args[2]);
                    if (isInvalidLevel(level2)) return;

                    request.setAttributeLvl2(level2);
                }
            }
        } else if (KICConfig.kaUseDefaultAttributeLvl) {
            request.setAttributeLvl1(KICConfig.kaDefaultAttributeLvl);
        }

        KICAuction.open(request);
    }

    private boolean isInvalidLevel(int level) {
        if (level < 1 || level > 10) {
            sendMessageToPlayer(KIC.KICPrefix + " §cAttribute level must be between 1-10.");
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
        if (args.length > 4) return Collections.emptyList();
        String lastArg = args[args.length - 1].toLowerCase();
        return ATTRIBUTES.stream()
                .filter(attr -> attr.toLowerCase().startsWith(lastArg))
                .collect(Collectors.toList());
    }
}
