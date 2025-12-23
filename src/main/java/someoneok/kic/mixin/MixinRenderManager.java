package someoneok.kic.mixin;

import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import someoneok.kic.config.KICConfig;
import someoneok.kic.utils.LocationUtils;

import static someoneok.kic.utils.ItemUtils.isNonEmpty;
import static someoneok.kic.utils.StringUtils.removeFormatting;

@Mixin(RenderManager.class)
public class MixinRenderManager {
    @Inject(method = "doRenderEntity", at = @At("HEAD"), cancellable = true)
    private void onDoRenderEntity(Entity entity, double x, double y, double z, float yaw, float partialTicks, boolean debug, CallbackInfoReturnable<Boolean> cir) {
        if (!LocationUtils.inKuudra()) return;
        if (!(entity instanceof EntityArmorStand)) return;
        if (!entity.isInvisible()) return;

        EntityArmorStand stand = (EntityArmorStand) entity;
        if (kIC$hasHeadOrHand(stand)) return;

        String name = removeFormatting(entity.getName());
        name = name.trim();
        if ((KICConfig.hideMobNametags && name.startsWith("[Lv")) ||
                (KICConfig.hideUselessArmorStands && name.equalsIgnoreCase("Armor Stand"))) {
            cir.setReturnValue(false);
        }
    }

    @Unique
    private boolean kIC$hasHeadOrHand(EntityArmorStand stand) {
        ItemStack[] inv = stand.getInventory();
        if (inv == null || inv.length == 0) return false;
        return isNonEmpty(inv[0]) || isNonEmpty(inv[inv.length - 1]);
    }
}
