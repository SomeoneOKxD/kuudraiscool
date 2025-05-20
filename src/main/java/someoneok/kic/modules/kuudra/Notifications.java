package someoneok.kic.modules.kuudra;

import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import someoneok.kic.config.KICConfig;
import someoneok.kic.utils.LocationUtils;
import someoneok.kic.utils.TitleUtils;
import someoneok.kic.utils.overlay.OverlayManager;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static someoneok.kic.KIC.mc;
import static someoneok.kic.utils.StringUtils.removeFormatting;

public class Notifications {
    private final List<NotificationRule> rules = new ArrayList<>();
    private static int totalTicks = 0;
    private static int ticks = 0;

    public Notifications() {
        rules.add(new NotificationRule(Kuudra.NO_EQUALS, "§aNo Equals", KICConfig.kuudraNotiNoSupplyTime, () -> KICConfig.kuudraNotiNoEquals));
        rules.add(new NotificationRule(Kuudra.NO_SHOP, "§6No Shop", KICConfig.kuudraNotiNoSupplyTime, () -> KICConfig.kuudraNotiNoShop));
        rules.add(new NotificationRule(Kuudra.NO_X_CANNON, "§6§eNo XC", KICConfig.kuudraNotiNoSupplyTime, () -> KICConfig.kuudraNotiNoXCannon));
        rules.add(new NotificationRule(Kuudra.NO_SLASH, "§2No Slash", KICConfig.kuudraNotiNoSupplyTime, () -> KICConfig.kuudraNotiNoSlash));
        rules.add(new NotificationRule(Kuudra.NO_SQUARE, "§eNo Square", KICConfig.kuudraNotiNoSupplyTime, () -> KICConfig.kuudraNotiNoSquare));
        rules.add(new NotificationRule(Kuudra.NO_TRIANGLE, "§aNo Tri", KICConfig.kuudraNotiNoSupplyTime, () -> KICConfig.kuudraNotiNoTriangle));
        rules.add(new NotificationRule(Kuudra.NO_X, "§2No X", KICConfig.kuudraNotiNoSupplyTime, () -> KICConfig.kuudraNotiNoX));
        rules.add(new NotificationRule("You moved and the Chest slipped out of your hands!", "§4DROPPED", KICConfig.kuudraNotiDroppedTime, () -> KICConfig.kuudraNotiDropped));
        rules.add(new NotificationRule("Sending to server", "CD", KICConfig.kuudraNotiCooldownTime, () -> KICConfig.kuudraNotiCooldown));
        rules.add(new NotificationRule("You are already currently picking up some supplies!", "§aGrabbing", KICConfig.kuudraNotiGrabbingTime, () -> KICConfig.kuudraNotiGrabbing));
        rules.add(new NotificationRule("Someone else is currently trying to pick up these supplies!", "§cGrabbed", KICConfig.kuudraNotiGrabbedTime, () -> KICConfig.kuudraNotiGrabbed));
        rules.add(new NotificationRule("([a-zA-Z0-9_]+) recovered", "§cPLACED", KICConfig.kuudraNotiPlacedTime, () -> KICConfig.kuudraNotiPlaced, true));
    }

    @SubscribeEvent(receiveCanceled = true)
    public void onChat(ClientChatReceivedEvent event) {
        if (!KICConfig.kuudraNotifications || !LocationUtils.inKuudra) return;
        String msg = removeFormatting(event.message.getUnformattedText());

        for (NotificationRule rule : rules) {
            if (!rule.enabled.get()) continue;

            if (rule.requiresNameMatch) {
                Matcher matcher = rule.pattern.matcher(msg);
                if (matcher.find() && matcher.groupCount() >= 1) {
                    String matchedName = matcher.group(1);
                    if (matchedName.equalsIgnoreCase(mc.thePlayer.getName())) {
                        showMessage(rule.text, rule.ticks);
                        break;
                    }
                }
            } else if (msg.contains(rule.matchText)) {
                showMessage(rule.text, rule.ticks);
                break;
            }
        }
    }

    public static void showMessage(String text, int ticks) {
        if (KICConfig.kuudraNotificationsMoveable) {
            OverlayManager.getOverlay("Notifications").updateText(text);
            Notifications.totalTicks = ticks;
            Notifications.ticks = 0;
        } else {
            TitleUtils.showTitle(text, ticks);
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START || !KICConfig.kuudraNotificationsMoveable || totalTicks == 0) return;

        ticks++;
        if (ticks > totalTicks) {
            totalTicks = 0;
            ticks = 0;
            OverlayManager.getOverlay("Notifications").updateText("");
        }
    }

    private static class NotificationRule {
        final String matchText;
        final String text;
        final int ticks;
        final Supplier<Boolean> enabled;
        final boolean requiresNameMatch;
        final Pattern pattern;

        NotificationRule(String matchText, String text, int ticks, Supplier<Boolean> enabled) {
            this(matchText, text, ticks, enabled, false);
        }

        NotificationRule(String matchText, String text, int ticks, Supplier<Boolean> enabled, boolean requiresNameMatch) {
            this.matchText = matchText;
            this.text = text;
            this.ticks = ticks;
            this.enabled = enabled;
            this.requiresNameMatch = requiresNameMatch;
            this.pattern = requiresNameMatch ? Pattern.compile(matchText) : null;
        }
    }
}
