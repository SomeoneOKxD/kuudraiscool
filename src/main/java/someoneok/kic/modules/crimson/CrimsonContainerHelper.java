package someoneok.kic.modules.crimson;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;
import someoneok.kic.KIC;
import someoneok.kic.config.KICConfig;
import someoneok.kic.events.GuiContainerEvent;
import someoneok.kic.models.NEUCompatibility;
import someoneok.kic.models.crimson.AttributeItem;
import someoneok.kic.models.crimson.AttributeItemValue;
import someoneok.kic.models.crimson.Attributes;
import someoneok.kic.modules.misc.ButtonManager;
import someoneok.kic.utils.*;
import someoneok.kic.utils.dev.KICLogger;
import someoneok.kic.utils.overlay.InteractiveOverlay;
import someoneok.kic.utils.overlay.OverlayManager;
import someoneok.kic.utils.overlay.OverlaySegment;

import java.awt.*;
import java.lang.invoke.MethodHandle;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

import static someoneok.kic.KIC.ATTRIBUTES;
import static someoneok.kic.KIC.mc;
import static someoneok.kic.utils.GeneralUtils.clickSlot;
import static someoneok.kic.utils.ItemUtils.getItemId;
import static someoneok.kic.utils.ItemUtils.mapToAttributeItem;
import static someoneok.kic.utils.LocationUtils.inDungeons;
import static someoneok.kic.utils.LocationUtils.onSkyblock;
import static someoneok.kic.utils.RenderUtils.xSizeField;
import static someoneok.kic.utils.RenderUtils.ySizeField;
import static someoneok.kic.utils.StringUtils.*;

public class CrimsonContainerHelper {
    private static final int ITEMS_PER_PAGE = 18;
    private static final Map<String, Color> COLORS = new HashMap<>();
    private static final List<String> enchants = Arrays.asList("strong_mana", "ferocious_mana", "hardened_mana", "mana_vampire", "fatal_tempo", "inferno");
    private static final List<String> miscIds = Arrays.asList("BURNING_KUUDRA_CORE", "WHEEL_OF_FATE", "MANDRAA", "KUUDRA_MANDIBLE", "RUNIC_STAFF", "HOLLOW_WAND");

    private final Map<AttributeItem, Slot> items = new HashMap<>();
    private final Map<Slot, String> miscItems = new HashMap<>();
    private final Set<String> selectedAttributes = new HashSet<>();
    private final Set<String> selectedMisc = new HashSet<>();
    private final Map<Slot, Color[]> slotHighlightColors = new HashMap<>();
    private Slot activeSlot = null;
    private int ticks = 0;
    private boolean shouldRender = false;
    private int currentView = 0;
    private boolean useLb = true;
    private int sorting = 0;
    private boolean saveSelectedItems = false;
    private String hoveredAttribute = null;
    private String hoveredMisc = null;
    private boolean showMisc = false;
    private boolean includeInvItems = false;
    private int attributeTier = 0; // 0 = ALL, 1 = T4, 2 = T5, 3 = T5+
    private int scrollIndex = 0;
    private boolean helperOverlayModified = false;

    static {
        generateColors();
    }

    private static void generateColors() {
        Random random = new Random(42);

        Consumer<String> generateColor = key -> COLORS.put(key, new Color(
                90 + random.nextInt(100),
                90 + random.nextInt(100),
                90 + random.nextInt(100),
                200
        ));

        ATTRIBUTES.forEach(generateColor);
        enchants.forEach(generateColor);
        miscIds.forEach(generateColor);
    }

    private boolean isChestNameValid(String chestName) {
        return (!inDungeons && (chestName.equals("Chest") || chestName.equals("Large Chest")))
                || chestName.contains("Backpack")
                || chestName.startsWith("Ender Chest (")
                || chestName.equals("Personal Vault");
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (!shouldProcess(event)) return;
        ticks = 0;

        if (!(mc.currentScreen instanceof GuiChest)) {
            resetState();
            return;
        }

        GuiChest guiChest = (GuiChest) mc.currentScreen;
        if (!(guiChest.inventorySlots instanceof ContainerChest)) {
            resetState();
            return;
        }

        ContainerChest container = (ContainerChest) guiChest.inventorySlots;
        if (isChestNameValid(container.getLowerChestInventory().getName())) {
            shouldRender = true;
            processInventory(container);
            return;
        }

        resetState();
    }

    private boolean shouldProcess(TickEvent.ClientTickEvent event) {
        return ApiUtils.isVerified()
                && KICConfig.crimsonContainerHelper
                && ButtonManager.isChecked("containerValue")
                && onSkyblock
                && mc.theWorld != null
                && mc.thePlayer != null
                && event.phase == TickEvent.Phase.END
                && (++ticks % 10 == 0);
    }

    private void resetState() {
        shouldRender = false;
    }

    private void processInventory(ContainerChest container) {
        Map<AttributeItem, Slot> newItems = new HashMap<>();
        Map<Slot, String> newMiscItems = new HashMap<>();
        boolean cacheUpdateNeeded = false;
        boolean itemsChanged = false;
        boolean miscChanged = false;

        for (Slot slot : container.inventorySlots) {
            if (!slot.getHasStack()) continue;

            ItemStack item = slot.getStack();
            if (!ItemUtils.hasUuidAndItemId(item)) continue;

            boolean hasAttributes = ItemUtils.hasAttributes(item);
            boolean hasItemId = ItemUtils.hasItemId(item);
            boolean inUserInv = slot.inventory == mc.thePlayer.inventory;

            if (hasAttributes && hasItemId && !"HOLLOW_WAND".equals(getItemId(item))) {
                AttributeItem attributeItem = mapToAttributeItem(item);
                if (attributeItem == null) continue;

                if ((currentView != 0 && includeInvItems) || !inUserInv) newItems.put(attributeItem, slot);

                boolean cacheCheck = CacheManager.addItem(new AttributeItemValue(attributeItem));
                if (cacheCheck && !cacheUpdateNeeded) cacheUpdateNeeded = true;
            } else if (((currentView != 0 && includeInvItems) || !inUserInv) && hasItemId) {
                String itemId = ItemUtils.getItemId(item);

                if ("ENCHANTED_BOOK".equals(itemId) && ItemUtils.hasEnchants(item)) {
                    String[] enchantInfo = ItemUtils.getFirstEnchant(item);
                    if (enchantInfo != null) {
                        String enchant = enchantInfo[2].replace("ultimate_", "");
                        if (enchants.contains(enchant)) newMiscItems.put(slot, enchant);
                    }
                } else if (miscIds.contains(itemId)) {
                    newMiscItems.put(slot, itemId);
                }
            }
        }

        if (!items.equals(newItems)) {
            items.clear();
            items.putAll(newItems);
            itemsChanged = true;
        }

        if (!miscItems.equals(newMiscItems)) {
            miscItems.clear();
            miscItems.putAll(newMiscItems);
            miscChanged = true;
        }

        if (itemsChanged || miscChanged || cacheUpdateNeeded) {
            if (cacheUpdateNeeded) {
                CacheManager.updateAttributeItemsCache(this::updateOverlay);
            } else {
                updateOverlay();
            }
        }
    }

    @SubscribeEvent
    public void onGuiClose(GuiContainerEvent.CloseWindowEvent event) {
        items.clear();
        activeSlot = null;
        scrollIndex = 0;
        hoveredAttribute = null;
        slotHighlightColors.clear();
        if (!saveSelectedItems) selectedAttributes.clear();
        resetState();
        if (helperOverlayModified) {
            OverlayManager.getOverlay("ContainerHelper").updateText("");
            helperOverlayModified = false;
        }
    }

    @SubscribeEvent
    public void onSlotClick(GuiContainerEvent.SlotClickEvent event) {
        if (!ApiUtils.isVerified() ||
                !KICConfig.crimsonContainerBlockClicks ||
                shouldNotRender() ||
                event.getSlot() == null ||
                slotHighlightColors.isEmpty()) {
            return;
        }

        int slotNumber = event.getSlot().slotNumber;

        boolean holdingItem = mc.thePlayer != null && mc.thePlayer.inventory.getItemStack() != null;
        boolean isTargetedSlot = slotHighlightColors.keySet().stream().anyMatch(slot -> slot.slotNumber == slotNumber);

        if (!isTargetedSlot && !holdingItem) {
            event.setCanceled(true);
        }
    }

    private void updateOverlay() {
        if (currentView == 0) {
            slotHighlightColors.clear();
            valueOverlay();
        } else {
            activeSlot = null;
            helperOverlay();
            updateSlotHighlights();
        }
    }

    private void changeView() {
        currentView ^= 1;
        scrollIndex = 0;
        updateOverlay();
    }

    private void valueOverlay() {
        InteractiveOverlay overlay = (InteractiveOverlay) OverlayManager.getOverlay("ContainerHelper");
        overlay.setRenderCondition(() -> shouldRender && (!items.isEmpty() || !miscItems.isEmpty()) && ButtonManager.isChecked("containerValue"));

        List<OverlaySegment> segments = new ArrayList<>();
        List<Map.Entry<AttributeItem, Slot>> sortedEntries = getSortedEntries();
        int totalItems = items.size();
        int itemsAbove = scrollIndex;
        int itemsBelow = Math.max(0, totalItems - (scrollIndex + ITEMS_PER_PAGE));

        segments.add(new OverlaySegment(
                String.format("%s%s §a§lContainer Value §7- ",  (!sortedEntries.isEmpty() && scrollIndex > 0) ? ("§e↑ " + itemsAbove + " ") : "", KIC.KICPrefix),
                true, this::scrollUp, this::scrollDown
        ));

        segments.add(new OverlaySegment(
                String.format("§7[%sValue§7/%sHelper§7]", currentView == 0 ? "§d" : "§8", currentView == 0 ? "§8" : "§d"),
                this::changeView, true
        ));

        if (sortedEntries.isEmpty()) {
            segments.add(new OverlaySegment("\n§cNo items!"));
        } else {
            for (Map.Entry<AttributeItem, Slot> entry : sortedEntries) {
                Slot slot = entry.getValue();
                AttributeItemValue value = CacheManager.getAttributeItem(entry.getKey().getUuid());
                if (value == null) continue;

                StringBuilder sb = new StringBuilder()
                        .append("\n§r§6").append(parseToShorthandNumber(value.getPrice(useLb)))
                        .append(" §7| §r").append(value.getName());

                String attributeText = getFormattedAttributes(value.getAttributes());
                if (!attributeText.isEmpty()) {
                    sb.append(" §r§7- ").append(attributeText);
                }

                segments.add(new OverlaySegment(sb.toString(), true,
                        () -> clickSlot(slot, 0, 1),
                        true,
                        () -> activeSlot = slot,
                        () -> activeSlot = null,
                        true,
                        this::scrollUp,
                        this::scrollDown
                ));
            }

            double totalValue = 0;
            for (AttributeItem item : items.keySet()) {
                AttributeItemValue value = CacheManager.getAttributeItem(item.getUuid());
                if (value != null) {
                    totalValue += value.getPrice(useLb);
                }
            }

            segments.add(new OverlaySegment(
                    String.format("\n%s§7Total: §6%s", (scrollIndex + ITEMS_PER_PAGE < items.size()) ? ("§e↓ " + itemsBelow + " ") : "", parseToShorthandNumber(totalValue)),
                    true, this::scrollUp, this::scrollDown
            ));
        }

        segments.add(new OverlaySegment("\n§7-=-=-=-=-=-=-=-=-=-=-"));

        segments.add(new OverlaySegment(
                String.format("\n§7- §aPrice Type §7[%sLB§7/%sAVG§7]", useLb ? "§d" : "§8", useLb ? "§8" : "§d"),
                this::changeValueType, true
        ));

        segments.add(new OverlaySegment(
                String.format("\n§7- §aSort By §7[%sPrice▼§7/%sPrice▲§7/%sSlot]",
                        sorting == 0 ? "§d" : "§8",
                        sorting == 1 ? "§d" : "§8",
                        sorting == 2 ? "§d" : "§8"),
                this::changeSorting, true
        ));

        overlay.setSegments(segments);
        helperOverlayModified = true;
    }

    private List<Map.Entry<AttributeItem, Slot>> getSortedEntries() {
        List<Map.Entry<AttributeItem, Slot>> sortedEntries = new ArrayList<>(items.entrySet());

        sortedEntries.sort((e1, e2) -> {
            AttributeItemValue v1 = CacheManager.getAttributeItem(e1.getKey().getUuid());
            AttributeItemValue v2 = CacheManager.getAttributeItem(e2.getKey().getUuid());

            int slotDiff = Integer.compare(e1.getValue().slotNumber, e2.getValue().slotNumber);
            double priceDiff = Double.compare(
                    v1 != null ? v1.getPrice(useLb) : 0,
                    v2 != null ? v2.getPrice(useLb) : 0
            );

            return sorting == 0 ? -(int) priceDiff : sorting == 1 ? (int) priceDiff : slotDiff;
        });

        int start = Math.min(scrollIndex, sortedEntries.size());
        int end = Math.min(start + ITEMS_PER_PAGE, sortedEntries.size());

        return sortedEntries.subList(start, end);
    }

    private void scrollUp() {
        if (scrollIndex > 0) {
            scrollIndex--;
            updateOverlay();
        }
    }

    private void scrollDown() {
        if (scrollIndex + ITEMS_PER_PAGE < items.size()) {
            scrollIndex++;
            updateOverlay();
        }
    }

    private void changeValueType() {
        useLb = !useLb;
        scrollIndex = 0;
        updateOverlay();
    }

    private void changeSorting() {
        sorting = (sorting + 1) % 3;
        scrollIndex = 0;
        updateOverlay();
    }

    private String getFormattedAttributes(Attributes attributes) {
        if (attributes == null) return "";

        List<String> parts = new ArrayList<>();
        if (attributes.hasAttribute1()) parts.add(String.format("§r§7[§b%s§7]", attributes.getAttribute1()));
        if (attributes.hasAttribute2()) parts.add(String.format("§r§7[§b%s§7]", attributes.getAttribute2()));

        return String.join(" ", parts);
    }

    private void helperOverlay() {
        InteractiveOverlay overlay = (InteractiveOverlay) OverlayManager.getOverlay("ContainerHelper");
        overlay.setRenderCondition(() -> shouldRender && (!items.isEmpty() || !miscItems.isEmpty()) && ButtonManager.isChecked("containerValue"));

        List<OverlaySegment> segments = new ArrayList<>();
        segments.add(new OverlaySegment(KIC.KICPrefix + " §a§lContainer Helper §7- "));

        segments.add(new OverlaySegment(
                String.format("§7[%sValue§7/%sHelper§7]", currentView == 0 ? "§d" : "§8", currentView == 0 ? "§8" : "§d"),
                this::changeView, true
        ));

        if (showMisc) {
            Map<String, Integer> miscCounts = getMiscCounts();

            if (miscCounts.isEmpty()) {
                segments.add(new OverlaySegment("\n§cNo items!"));
            } else {
                miscCounts.entrySet().stream()
                        .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                        .forEach(entry -> {
                            String misc = entry.getKey();

                            segments.add(new OverlaySegment(
                                    String.format("\n§6%sx§7 | §b%s%s", entry.getValue(), formatId(misc.replace("RUNIC_STAFF", "AURORA_STAFF")),
                                            selectedMisc.contains(misc) ? " §7[§a✔§7]" : ""),
                                    true, () -> toggleHighlightMisc(misc),
                                    true, () -> hoverHighlightMisc(misc),
                                    () -> removeHoverHighlightMisc(misc)
                            ));

                            if (selectedMisc.contains(misc)) {
                                int colorValue = Optional.ofNullable(COLORS.get(misc))
                                        .orElse(Color.WHITE)
                                        .getRGB();
                                segments.add(new OverlaySegment(" [COLOR]", colorValue));
                            }
                        });
            }
        } else {
            Map<String, Integer> attributeCounts = getAttributeCounts();

            if (attributeCounts.isEmpty()) {
                segments.add(new OverlaySegment("\n§cNo items!"));
            } else {
                attributeCounts.entrySet().stream()
                        .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                        .forEach(entry -> {
                            String attribute = entry.getKey();

                            segments.add(new OverlaySegment(
                                    String.format("\n§6%sx§7 | §b%s%s", entry.getValue(), formatId(attribute),
                                            selectedAttributes.contains(attribute) ? " §7[§a✔§7]" : ""),
                                    true, () -> toggleAttributeHighlight(attribute),
                                    true, () -> hoverHighlight(attribute),
                                    () -> removeHoverHighlight(attribute)
                            ));

                            if (selectedAttributes.contains(attribute)) {
                                int colorValue = Optional.ofNullable(COLORS.get(attribute))
                                        .orElse(Color.WHITE)
                                        .getRGB();
                                segments.add(new OverlaySegment(" [COLOR]", colorValue));
                            }
                        });
            }
        }

        segments.add(new OverlaySegment("\n§7-=-=-=-=-=-=-=-=-=-=-"));

        segments.add(new OverlaySegment(
                String.format("\n§7- §aShow Miscellaneous §7[%s§7]", showMisc ? "§a✔" : "§cX"),
                this::changeShowMisc, true
        ));

        segments.add(new OverlaySegment(
                String.format("\n§7- §aStay Selected §7[%s§7]", saveSelectedItems ? "§a✔" : "§cX"),
                this::changeStaySelected, true
        ));

        segments.add(new OverlaySegment(
                String.format("\n§7- §aInclude Inventory §7[%s§7]", includeInvItems ? "§a✔" : "§cX"),
                this::changeIncludeInvItems, true
        ));

        if (!showMisc) {
            segments.add(new OverlaySegment(
                    String.format("\n§7- §aAttribute Tier §7[%sALL§7/%sT4§7/%sT5§7/%sT5+§7]",
                            attributeTier == 0 ? "§d" : "§8",
                            attributeTier == 1 ? "§d" : "§8",
                            attributeTier == 2 ? "§d" : "§8",
                            attributeTier == 3 ? "§d" : "§8"),
                    this::changeAttributeTier, true
            ));
        }

        overlay.setSegments(segments);
        helperOverlayModified = true;
    }

    private void changeAttributeTier() {
        attributeTier = (attributeTier + 1) % 4;
        updateOverlay();
    }

    private Map<String, Integer> getAttributeCounts() {
        Map<String, Integer> attributeCounts = new HashMap<>();

        for (AttributeItem item : items.keySet()) {
            Attributes attributes = item.getAttributes();
            if (attributes == null) continue;

            if (attributes.hasAttribute1() && passesTierFilter(attributes.getLevel1())) {
                attributeCounts.merge(attributes.getName1(), 1, Integer::sum);
            }
            if (attributes.hasAttribute2() && passesTierFilter(attributes.getLevel2())) {
                attributeCounts.merge(attributes.getName2(), 1, Integer::sum);
            }
        }

        if (saveSelectedItems) {
            selectedAttributes.forEach(attr -> attributeCounts.putIfAbsent(attr, 0));
        }

        return attributeCounts;
    }

    private boolean passesTierFilter(int level) {
        if (attributeTier == 0) return true;
        if (attributeTier == 1) return level == 4;
        if (attributeTier == 2) return level == 5;
        if (attributeTier == 3) return level >= 5;
        return false;
    }

    private Map<String, Integer> getMiscCounts() {
        Map<String, Integer> miscCounts = new HashMap<>();

        for (String misc : miscItems.values()) {
            if (!isNullOrEmpty(misc)) {
                miscCounts.merge(misc, 1, Integer::sum);
            }
        }

        if (saveSelectedItems) {
            selectedMisc.forEach(misc -> miscCounts.putIfAbsent(misc, 0));
        }

        return miscCounts;
    }

    private void toggleAttributeHighlight(String attribute) {
        if (selectedAttributes.contains(attribute)) {
            selectedAttributes.remove(attribute);
        } else {
            selectedAttributes.add(attribute);
        }
        updateOverlay();
    }

    private void toggleHighlightMisc(String misc) {
        if (selectedMisc.contains(misc)) {
            selectedMisc.remove(misc);
        } else {
            selectedMisc.add(misc);
        }
        updateOverlay();
    }

    private void hoverHighlight(String attribute) {
        hoveredAttribute = attribute;
        updateSlotHighlights();
    }

    private void hoverHighlightMisc(String misc) {
        hoveredMisc = misc;
        updateSlotHighlights();
    }

    private void removeHoverHighlight(String attribute) {
        if (hoveredAttribute != null && hoveredAttribute.equals(attribute)) {
            hoveredAttribute = null;
            updateSlotHighlights();
        }
    }

    private void removeHoverHighlightMisc(String misc) {
        if (hoveredMisc != null && hoveredMisc.equals(misc)) {
            hoveredMisc = null;
            updateSlotHighlights();
        }
    }

    private void changeStaySelected() {
        saveSelectedItems = !saveSelectedItems;
        updateOverlay();
    }

    private void changeShowMisc() {
        showMisc = !showMisc;
        updateOverlay();
    }

    private void changeIncludeInvItems() {
        includeInvItems = !includeInvItems;
        updateOverlay();
    }

    private boolean shouldNotRender() {
        return !ApiUtils.isVerified()
                || !KICConfig.crimsonContainerHelper
                || !ButtonManager.isChecked("containerValue")
                || !onSkyblock
                || !shouldRender
                || NEUCompatibility.isStorageMenuActive();
    }

    @SubscribeEvent
    public void onBackgroundDrawn(GuiScreenEvent.BackgroundDrawnEvent e) {
        if (shouldNotRender() || !(e.gui instanceof GuiChest)) return;

        GuiChest gui = (GuiChest) e.gui;

        if (activeSlot != null) {
            RenderUtils.highlight(new Color(0, 255, 13, 175), gui, activeSlot);
        }

        for (Map.Entry<Slot, Color[]> entry : slotHighlightColors.entrySet()) {
            Slot slot = entry.getKey();
            Color[] colors = entry.getValue();
            if (colors.length == 1) {
                RenderUtils.highlight(colors[0], gui, slot);
            } else if (colors.length == 2) {
                RenderUtils.highlight(colors[0], colors[1], gui, slot);
            }
        }
    }

    @SubscribeEvent
    public void onDrawGui(GuiScreenEvent.DrawScreenEvent.Post e) {
        if (shouldNotRender() || !(e.gui instanceof GuiChest)) return;

        GuiChest gui = (GuiChest) e.gui;

        if (!slotHighlightColors.isEmpty()) {
            drawDarkOverlay(gui);
            renderTooltipManually(e);
        }
    }

    private void drawDarkOverlay(GuiContainer gui) {
        try {
            int xSize = (int) xSizeField.invokeExact(gui);
            int ySize = (int) ySizeField.invokeExact(gui);

            int guiLeft = (gui.width - xSize) / 2;
            int guiTop = (gui.height - ySize) / 2;
            int guiRight = guiLeft + xSize;
            int guiBottom = guiTop + ySize;

            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
            GlStateManager.disableAlpha();
            GlStateManager.shadeModel(GL11.GL_SMOOTH);
            GlStateManager.disableDepth();
            GlStateManager.disableLighting();

            GL11.glEnable(GL11.GL_STENCIL_TEST);
            GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);

            GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
            GL11.glStencilOp(GL11.GL_REPLACE, GL11.GL_REPLACE, GL11.GL_REPLACE);

            GL11.glColorMask(false, false, false, false);
            GL11.glDepthMask(false);

            if (activeSlot != null) {
                drawSlotMask(guiLeft, guiTop, activeSlot);
            }

            for (Slot slot : slotHighlightColors.keySet()) {
                drawSlotMask(guiLeft, guiTop, slot);
            }

            GL11.glColorMask(true, true, true, true);
            GL11.glDepthMask(true);

            GL11.glStencilFunc(GL11.GL_NOTEQUAL, 1, 0xFF);
            GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);

            Gui.drawRect(guiLeft, guiTop, guiRight, guiBottom, new Color(0, 0, 0, 153).getRGB());

            GL11.glDisable(GL11.GL_STENCIL_TEST);

            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
            GlStateManager.shadeModel(GL11.GL_FLAT);
            GlStateManager.enableAlpha();
            GlStateManager.disableBlend();
        } catch (Throwable e) {
            KICLogger.info(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void drawSlotMask(int left, int top, Slot slot) {
        int slotX = left + slot.xDisplayPosition;
        int slotY = top + slot.yDisplayPosition;
        int slotSize = 16;

        Gui.drawRect(slotX, slotY, slotX + slotSize, slotY + slotSize, Color.WHITE.getRGB());
    }

    private void renderTooltipManually(GuiScreenEvent.DrawScreenEvent.Post e) {
        GuiScreen gui = e.gui;
        if (gui instanceof GuiContainer) {
            GuiContainer guiContainer = (GuiContainer) gui;
            Slot hoveredSlot = guiContainer.getSlotUnderMouse();

            if (hoveredSlot != null && hoveredSlot.getHasStack()) {
                ItemStack stack = hoveredSlot.getStack();

                try {
                    MethodHandle renderToolTip = ReflectionUtil.getMethod(GuiScreen.class, Arrays.asList("renderToolTip", "func_146285_a"), ItemStack.class, int.class, int.class);

                    renderToolTip.invoke(guiContainer, stack, e.mouseX, e.mouseY);
                } catch (Throwable ex) {
                    KICLogger.info(ex.getMessage());
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    private void updateSlotHighlights() {
        slotHighlightColors.clear();

        if (showMisc) {
            if (!selectedMisc.isEmpty() || hoveredMisc != null) {
                miscItems.forEach((slot, misc) -> {
                    if (!isNullOrEmpty(misc)) {
                        processSlotHighlight(slot, misc, selectedMisc, hoveredMisc);
                    }
                });
            }
        } else {
            if (!selectedAttributes.isEmpty() || hoveredAttribute != null) {
                items.forEach((attributeItem, slot) -> processSlotHighlight(
                        slot,
                        attributeItem.getAttributes(),
                        selectedAttributes,
                        hoveredAttribute
                ));
            }
        }
    }

    private void processSlotHighlight(Slot slot, Object attributeOrMisc, Set<String> selectedSet, String hovered) {
        if (attributeOrMisc == null) return;

        List<Color> colors = new ArrayList<>();

        if (attributeOrMisc instanceof Attributes) {
            Attributes attributes = (Attributes) attributeOrMisc;
            if (attributes.hasAttribute1() && passesTierFilter(attributes.getLevel1())) {
                addHighlightColor(colors, attributes.getName1(), selectedSet, hovered);
            }
            if (attributes.hasAttribute2() && passesTierFilter(attributes.getLevel2())) {
                addHighlightColor(colors, attributes.getName2(), selectedSet, hovered);
            }
        } else if (attributeOrMisc instanceof String) {
            addHighlightColor(colors, (String) attributeOrMisc, selectedSet, hovered);
        }

        if (!colors.isEmpty()) {
            slotHighlightColors.put(slot, colors.toArray(new Color[0]));
        }
    }

    private void addHighlightColor(List<Color> colors, String name, Set<String> selectedSet, String hovered) {
        if (name != null && (selectedSet.contains(name) || name.equals(hovered))) {
            colors.add(COLORS.getOrDefault(name, new Color(255, 255, 255, 175)));
        }
    }
}
