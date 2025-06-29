package someoneok.kic.modules.kuudra;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;
import someoneok.kic.config.KICConfig;
import someoneok.kic.models.crimson.Value;
import someoneok.kic.utils.LocationUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static someoneok.kic.KIC.mc;
import static someoneok.kic.utils.StringUtils.isNullOrEmpty;
import static someoneok.kic.utils.StringUtils.parseToShorthandNumber;

public class Hologram {
    private static final Vec3 chestPos1 = new Vec3(-98.9, 80.35, -119.2); // t1-4
    private static final Vec3 chestPos2 = new Vec3(-99.7, 7.35, -105.8); // t5
    private static final String VALUABLE_TEXT = "§7-=[§c§lV§6§lA§e§lL§a§lU§b§lA§9§lB§d§lL§c§lE§r§7]=-";
    private static final double HALF_SIZE = 45.0;
    private static final Map<String, String> TEXTURES = new HashMap<>();

    private static boolean show = false;
    private static String label = null;
    private static String price = null;
    private static ItemStack item = null;
    private static Vec3 pos = null;

    static {
        TEXTURES.put("WHEEL_OF_FATE", "ewogICJ0aW1lc3RhbXAiIDogMTYxNzE0MDQ2MTMyOSwKICAicHJvZmlsZUlkIiA6ICIyMWUzNjdkNzI1Y2Y0ZTNiYjI2OTJjNGEzMDBhNGRlYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJHZXlzZXJNQyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS81Yzg3ZjU5YjAyODg2ZmVmNTk0ZWFlYmI0NmRjMzM3YWFiYjg0NWIyYzQxYmI0ZDIzOGMxMjYwN2Q4YTA5NTQ0IiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=");
        TEXTURES.put("BURNING_KUUDRA_CORE", "ewogICJ0aW1lc3RhbXAiIDogMTY0NzA0MDIzMTE0NCwKICAicHJvZmlsZUlkIiA6ICIzYzE0YmVkNDFiOGE0MDIzOGM3MDgzMTA1NzEwMTZmYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJOb2Jpa28iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzY4NzQzNDk3ODIwYzA4NjI3ZDJjYzVlODkxYzBmOWZjMzc5M2Y3NWI2ZTQxZTE0MGFjOWIwMDdkM2I1MDVhNSIKICAgIH0KICB9Cn0=");
        TEXTURES.put("TENTACLE_DYE", "ewogICJ0aW1lc3RhbXAiIDogMTc0MTE2MTM3OTg3NywKICAicHJvZmlsZUlkIiA6ICIwNDg0N2ZjNWM5YjY0NTQ1YjI1ZWJkYmJiNzdjNjg2NSIsCiAgInByb2ZpbGVOYW1lIiA6ICJuYXFsdWEiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjk4YmJhODA2ODA5MWJmYjcwZDdmOTc5ODM5MzRiMGRlYTZjZjkxY2M5OWVhOWQxNGFmN2U4MTE4MTVjNGJmNyIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9");
    }

    @SuppressWarnings("deprecation")
    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (!show || label == null || price == null || item == null || pos == null) return;
        if (!KICConfig.showValuableHolo || !LocationUtils.inKuudra) return;

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

        renderFacingText(0.0, 0.0, -HALF_SIZE, 0.0F, fr, VALUABLE_TEXT);
        renderFacingText(HALF_SIZE, 0.0, 0.0, 90.0F, fr, VALUABLE_TEXT);
        renderFacingText(0.0, 0.0, HALF_SIZE, 180.0F, fr, VALUABLE_TEXT);
        renderFacingText(-HALF_SIZE, 0.0, 0.0, 270.0F, fr, VALUABLE_TEXT);

        // Isolate item rendering
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0, -45.0, 0.0);
        GlStateManager.scale(-35F, -35F, 35F);
        GlStateManager.rotate(-45.0F, 0.0F, 1.0F, 0.0F);
        renderItem.renderItem(item, ItemCameraTransforms.TransformType.GROUND);
        GlStateManager.popMatrix();

        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1f, 1f, 1f, 1f);

        renderFacingText(0.0, -85.0, 0.0, 45.0F, fr, label);
        renderFacingText(0.0, -75.0, 0.0, 45.0F, fr, price);

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

        if (KICConfig.showValuableHoloBothSides) {
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
        label = null;
        price = null;
        item = null;
        pos = null;
    }

    public static void show(Value droppedItem) {
        if (!KICConfig.showValuableHolo || !LocationUtils.inKuudra) return;

        String itemId = droppedItem.getItemId().toUpperCase();
        if (itemId.equals("WHEEL_OF_FATE") || itemId.equals("BURNING_KUUDRA_CORE") || itemId.equals("TENTACLE_DYE")) {
            if (!TEXTURES.containsKey(itemId)) return;
            item = skullItem(TEXTURES.get(itemId));
        } else {
            switch (itemId) {
                case "TORMENTOR":
                    item = new ItemStack(Items.iron_sword);
                    break;
                case "HELLSTORM_STAFF":
                    item = new ItemStack(Blocks.redstone_torch);
                    break;
                case "ENCHANTMENT_ULTIMATE_FATAL_TEMPO_1":
                case "ENCHANTMENT_ULTIMATE_INFERNO_1":
                    item = new ItemStack(Items.enchanted_book);
                    break;
                case "ANANKE_FEATHER":
                    ItemStack anankeFeather = new ItemStack(Items.feather);
                    NBTTagCompound tag = new NBTTagCompound();
                    tag.setTag("ench", new NBTTagList());
                    anankeFeather.setTagCompound(tag);
                    item = anankeFeather;
                    break;
                default:
                    return;
            }
        }

        show = true;
        pos = LocationUtils.kuudraTier == 5 ? chestPos2 : chestPos1;
        label = droppedItem.getName();
        price = "§6" + parseToShorthandNumber(droppedItem.getValue());
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
}
