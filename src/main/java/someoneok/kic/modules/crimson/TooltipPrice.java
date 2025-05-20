package someoneok.kic.modules.crimson;

import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import someoneok.kic.config.KICConfig;
import someoneok.kic.models.crimson.AttributeItem;
import someoneok.kic.models.crimson.AttributeItemValue;
import someoneok.kic.models.crimson.Attributes;
import someoneok.kic.utils.CacheManager;
import someoneok.kic.utils.LocationUtils;

import java.util.List;
import java.util.regex.Pattern;

import static someoneok.kic.utils.ItemUtils.mapToAttributeItem;
import static someoneok.kic.utils.StringUtils.parseToShorthandNumber;
import static someoneok.kic.utils.StringUtils.toRoman;

public class TooltipPrice {
    private static final Pattern VALUE_SUFFIX_PATTERN = Pattern.compile(" §r§7\\(§6.*?§7\\)§r$");

    @SubscribeEvent
    public void onRenderToolTip(ItemTooltipEvent event) {
        if (!KICConfig.crimsonTooltipPrices || !LocationUtils.onSkyblock) return;
        ItemStack stack = event.itemStack;
        List<String> tooltip = event.toolTip;

        if (stack == null || !stack.hasDisplayName()) return;
        AttributeItem item = mapToAttributeItem(stack);
        if (item == null || !item.hasAttributes() || "HOLLOW_WAND".equals(item.getItemId())) return;

        AttributeItemValue value = CacheManager.getAttributeItem(item.getUuid());
        if (value == null) return;

        // Main Value
        String formattedMainValue = "§r§3§lItem Value: §r§6" + parseToShorthandNumber(value.getPrice(true));
        if (!tooltip.contains(formattedMainValue)) {
            tooltip.removeIf(line -> line.contains("§r§3§lItem Value: §r§6"));
            tooltip.add(formattedMainValue);
        }

        if (KICConfig.crimsonTooltipPerAttribute) {
            Attributes attributes = value.getAttributes();
            if (attributes.hasAttribute1()) {
                injectAttributeValueLine(tooltip,
                        attributes.getFormattedName1(),
                        attributes.getLevel1(),
                        attributes.getLbPrice1()
                );
            }

            if (attributes.hasAttribute2()) {
                injectAttributeValueLine(tooltip,
                        attributes.getFormattedName2(),
                        attributes.getLevel2(),
                        attributes.getLbPrice2()
                );
            }
        }
    }

    private void injectAttributeValueLine(List<String> tooltip, String attributeName, int level, long price) {
        String romanLevel = toRoman(level);
        String numberLevel = String.valueOf(level);
        String valueSuffix = " §r§7(§6" + parseToShorthandNumber(price) + "§7)§r";

        for (int i = 0; i < tooltip.size(); i++) {
            String line = tooltip.get(i);

            if ((line.contains(attributeName + " " + numberLevel) || line.contains(attributeName + " " + romanLevel))) {
                if (!line.endsWith(valueSuffix)) {
                    String stripped = VALUE_SUFFIX_PATTERN.matcher(line).replaceFirst("");
                    tooltip.set(i, stripped + valueSuffix);
                }
                return;
            }
        }
    }
}
