package someoneok.kic.modules.kuudra;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;
import someoneok.kic.config.KICConfig;
import someoneok.kic.config.pages.KuudraProfitCalculatorOptions;
import someoneok.kic.models.crimson.AttributeItemValue;
import someoneok.kic.models.crimson.Attributes;
import someoneok.kic.utils.LocationUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static someoneok.kic.KIC.mc;
import static someoneok.kic.utils.StringUtils.isNullOrEmpty;
import static someoneok.kic.utils.StringUtils.parseToShorthandNumber;

public class GodRoll {
    private static final Vec3 chestPos1 = new Vec3(-98.9, 80.35, -119.2); // t1-4
    private static final Vec3 chestPos2 = new Vec3(-99.7, 7.35, -105.8); // t5
    private static final String GODROLL_TEXT = "§7-=[§c§lG§6§lO§e§lD§a§lR§b§lO§9§lL§d§lL§r§7]=-";
    private static final double HALF_SIZE = 45.0;
    private static final Map<String, String> TEXTURES = new HashMap<>();
    private static final Map<String, Integer> COLOR_MAP = new HashMap<>();

    private static boolean show = false;
    private static String grLabel = null;
    private static String grAttributes = null;
    private static String grPrice = null;
    private static ItemStack grItem = null;
    private static Vec3 pos = null;

    static {
        TEXTURES.put("CRIMSON_HELMET", "ewogICJ0aW1lc3RhbXAiIDogMTY0NTUwNDQxNDE3MywKICAicHJvZmlsZUlkIiA6ICI5MThhMDI5NTU5ZGQ0Y2U2YjE2ZjdhNWQ1M2VmYjQxMiIsCiAgInByb2ZpbGVOYW1lIiA6ICJCZWV2ZWxvcGVyIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzUwNTFjODNkOWViZjY5MDEzZjFlYzhjOWVmYzk3OWVjMmQ5MjVhOTIxY2M4NzdmZjY0YWJlMDlhYWRkMmY2Y2MiCiAgICB9CiAgfQp9");
        TEXTURES.put("AURORA_HELMET", "ewogICJ0aW1lc3RhbXAiIDogMTY0NTUwMzc3ODA5MSwKICAicHJvZmlsZUlkIiA6ICJjNjc3MGJjZWMzZjE0ODA3ODc4MTU0NWRhMGFmMDI1NCIsCiAgInByb2ZpbGVOYW1lIiA6ICJDVUNGTDE2IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzc2M2QyZmU5Mzg4MWI0ZjI2Y2JlMWRkM2IwOWRhN2NjNDhkYmNkYzU2OGQxOTg1MmFkNjM1ZDVkMTY4NTk2MTEiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==");
        TEXTURES.put("TERROR_HELMET", "ewogICJ0aW1lc3RhbXAiIDogMTY0NTUwNDE2MDk4NSwKICAicHJvZmlsZUlkIiA6ICI4ZGU4ZWU3MTMyMTY0NGNhYTllZjJlNTVjODRjNGU4ZCIsCiAgInByb2ZpbGVOYW1lIiA6ICJIdW5kZXNjaG9uVE0iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTJhZjg4MzM2OTdjODFiNDZlODNjOGYxODk1MjY2ZTYwNmVmYmIzYTU5ZjFjM2I0Y2EyODE2ZGEyYmNmYTlkNiIKICAgIH0KICB9Cn0=");
        TEXTURES.put("MOLTEN_NECKLACE", "ewogICJ0aW1lc3RhbXAiIDogMTY0NzAxNjQxODA4MiwKICAicHJvZmlsZUlkIiA6ICI2NmI0ZDRlMTFlNmE0YjhjYTFkN2Q5YzliZTBhNjQ5OSIsCiAgInByb2ZpbGVOYW1lIiA6ICJBcmFzdG9vWXNmIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzZiYzBjYTQ0YzY3OGM0ZmI0ZWJlNzNmMTI3MTdmOGFiNmRmM2I1YWI4NjhkMjE3ZTY4YzM2YWFmZTJhZDgwZTQiCiAgICB9CiAgfQp9");
        TEXTURES.put("MOLTEN_CLOAK", "ewogICJ0aW1lc3RhbXAiIDogMTY0NzAxNjQzNDExNiwKICAicHJvZmlsZUlkIiA6ICJlZThjNWMzMGY3NWU0N2QxOTBmOTllNjI5NDgyOGZjMSIsCiAgInByb2ZpbGVOYW1lIiA6ICJTcGFya19QaGFudG9tIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzZjMThiYjBmNTJmMjQ2ZWM5YmZmNWYwNTc2MTQ1NmJlMjczZTczMGNkMjc2NTU1ZmQ3NzJiYThjM2ZhMGYxNDQiCiAgICB9CiAgfQp9");
        TEXTURES.put("MOLTEN_BELT", "ewogICJ0aW1lc3RhbXAiIDogMTY0NzAxNjQ4ODIwMSwKICAicHJvZmlsZUlkIiA6ICI5ZDQyNWFiOGFmZjg0MGU1OWM3NzUzZjc5Mjg5YjMyZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJUb21wa2luNDIiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTdmN2Y1NzIzNGI5OTg2ZTg1ZGE4NzhmYjNiNjY3MGYxYzcwYTM1NzBkZGRiZTlmYjUzMDkxMzA4MmJhNDRiNSIKICAgIH0KICB9Cn0");
        TEXTURES.put("MOLTEN_BRACELET", "ewogICJ0aW1lc3RhbXAiIDogMTY0NzAxNjUwNTAzMSwKICAicHJvZmlsZUlkIiA6ICJiYzRlZGZiNWYzNmM0OGE3YWM5ZjFhMzlkYzIzZjRmOCIsCiAgInByb2ZpbGVOYW1lIiA6ICI4YWNhNjgwYjIyNDYxMzQwIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzIwNTI5NGY4OTE1YzhkYTliNmUzODI5ZjZkYmExMzVmODhhYjQ3NjFiOTQzMGUxOGZmMTI1ZWYzZTk1MGNjZGYiCiAgICB9CiAgfQp9");
        COLOR_MAP.put("CRIMSON_CHESTPLATE", 0xFF6F0C);
        COLOR_MAP.put("CRIMSON_LEGGINGS", 0xE66105);
        COLOR_MAP.put("CRIMSON_BOOTS", 0xE65300);
        COLOR_MAP.put("AURORA_CHESTPLATE", 0x2841F1);
        COLOR_MAP.put("AURORA_LEGGINGS", 0x3F56FB);
        COLOR_MAP.put("AURORA_BOOTS", 0x6184FC);
        COLOR_MAP.put("TERROR_CHESTPLATE", 0x3E05AF);
        COLOR_MAP.put("TERROR_LEGGINGS", 0x5D23D1);
        COLOR_MAP.put("TERROR_BOOTS", 0x7C44EC);
    }

    @SuppressWarnings("deprecation")
    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (!show || grLabel == null || grAttributes == null || grPrice == null || grItem == null || pos == null) return;
        if (!KICConfig.showGodRollHolo || !LocationUtils.inKuudra) return;
        Entity viewer = mc.getRenderViewEntity();
        RenderItem renderItem = mc.getRenderItem();
        FontRenderer fr = mc.fontRendererObj;

        float rotation = (System.currentTimeMillis() % 10000L) / 10000.0F * 360.0F;

        double interpX = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * event.partialTicks;
        double interpY = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * event.partialTicks;
        double interpZ = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * event.partialTicks;

        double dx = pos.xCoord - interpX;
        double dy = pos.yCoord - interpY;
        double dz = pos.zCoord - interpZ;

        GlStateManager.pushMatrix();
        GlStateManager.translate(dx, dy, dz);
        GlStateManager.scale(-0.025F, -0.025F, 0.025F);
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GlStateManager.rotate(rotation, 0.0F, 1.0F, 0.0F);

        renderFacingText(0.0, 0.0, -HALF_SIZE, 0.0F, fr, GODROLL_TEXT);
        renderFacingText(HALF_SIZE, 0.0, 0.0, 90.0F, fr, GODROLL_TEXT);
        renderFacingText(0.0, 0.0, HALF_SIZE, 180.0F, fr, GODROLL_TEXT);
        renderFacingText(-HALF_SIZE, 0.0, 0.0, 270.0F, fr, GODROLL_TEXT);

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0, -45.0, 0.0);
        GlStateManager.scale(-35F, -35F, 35F);
        GlStateManager.rotate(-45.0F, 0.0F, 1.0F, 0.0F);
        renderItem.renderItem(grItem, ItemCameraTransforms.TransformType.GROUND);
        GlStateManager.popMatrix();

        renderFacingText(0.0, -90.0, 0.0, 45.0F, fr, grLabel);
        renderFacingText(0.0, -80.0, 0.0, 45.0F, fr, grAttributes);
        renderFacingText(0.0, -70.0, 0.0, 45.0F, fr, grPrice);

        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    private void renderFacingText(double x, double y, double z, float rotationY, FontRenderer fr, String text) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(rotationY, 0.0F, 1.0F, 0.0F);

        int width = fr.getStringWidth(text);
        fr.drawString(text, -width / 2, 0, 0xFFFFFF);

        if (KICConfig.showGodRollHoloBothSides) {
            GlStateManager.pushMatrix();
            GlStateManager.rotate(180F, 0.0F, 1.0F, 0.0F);
            fr.drawString(text, -width / 2, 0, 0xFFFFFF);
            GlStateManager.popMatrix();
        }

        GlStateManager.popMatrix();
    }

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Unload event) {
        show = false;
        grLabel = null;
        grAttributes = null;
        grPrice = null;
        grItem = null;
        pos = null;
    }

    public static void show(AttributeItemValue item) {
        if (!KICConfig.showGodRollHolo || !LocationUtils.inKuudra) return;

        boolean isMolten = false;
        String itemId = item.getItemId().toUpperCase();
        if (itemId.contains("MOLTEN") || itemId.contains("HELMET")) {
            if (itemId.contains("MOLTEN")) isMolten = true;
            if (!TEXTURES.containsKey(itemId)) return;
            grItem = skullItem(TEXTURES.get(itemId));
        } else {
            if (!COLOR_MAP.containsKey(itemId)) return;
            ItemStack itemStack;
            if (itemId.contains("CHESTPLATE")) {
                itemStack = new ItemStack(Items.leather_chestplate);
            } else if (itemId.contains("LEGGINGS")) {
                itemStack = new ItemStack(Items.leather_leggings);
            } else if (itemId.contains("BOOTS")) {
                itemStack = new ItemStack(Items.leather_boots);
            } else {
                return;
            }
            grItem = applyArmorColor(itemStack, COLOR_MAP.get(itemId));
        }

        show = true;
        pos = LocationUtils.kuudraTier == 5 ? chestPos2 : chestPos1;
        grLabel = (isMolten ? "§5" : "§6") + item.getName();
        Attributes attributes = item.getAttributes();
        grAttributes = (isMolten ? "§d" : "§e") + attributes.getFormattedName1() + " & " + attributes.getFormattedName2();
        grPrice = (isMolten ? "§d" : "§e") + parseToShorthandNumber(item.getPrice(KuudraProfitCalculatorOptions.godRollPriceType == 0));
    }

    private static ItemStack skullItem(String texture) {
        ItemStack skull = new ItemStack(Items.skull, 1, 3);

        if (!isNullOrEmpty(texture)) {
            NBTTagCompound tag = new NBTTagCompound();
            NBTTagCompound skullOwner = new NBTTagCompound();
            NBTTagCompound properties = new NBTTagCompound();
            NBTTagList textures = new NBTTagList();
            NBTTagCompound textureTag = new NBTTagCompound();

            textureTag.setString("Value", texture);
            textures.appendTag(textureTag);
            properties.setTag("textures", textures);

            skullOwner.setTag("Properties", properties);
            skullOwner.setString("Id", UUID.randomUUID().toString());

            tag.setTag("SkullOwner", skullOwner);
            skull.setTagCompound(tag);
        }

        return skull;
    }

    private static ItemStack applyArmorColor(ItemStack stack, int color) {
        if (stack.getItem() instanceof ItemArmor && ((ItemArmor) stack.getItem()).getArmorMaterial() == ItemArmor.ArmorMaterial.LEATHER) {
            NBTTagCompound tag = stack.getTagCompound();
            if (tag == null) tag = new NBTTagCompound();
            NBTTagCompound display = tag.getCompoundTag("display");
            display.setInteger("color", color);
            tag.setTag("display", display);
            stack.setTagCompound(tag);
        }
        return stack;
    }
}
