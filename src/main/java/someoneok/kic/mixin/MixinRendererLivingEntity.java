package someoneok.kic.mixin;

import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import someoneok.kic.config.KICConfig;
import someoneok.kic.utils.LocationUtils;

import static someoneok.kic.utils.ItemUtils.isNonEmpty;

@Mixin(RendererLivingEntity.class)
public abstract class MixinRendererLivingEntity {
    @Inject(method = "preRenderCallback(Lnet/minecraft/entity/EntityLivingBase;F)V", at = @At("HEAD"))
    private void onPreRenderCallback(EntityLivingBase entity, float partialTickTime, CallbackInfo ci) {
        if (!KICConfig.tinyKuudraMobs) return;
        if (!LocationUtils.inKuudra()) return;
        if (entity.isInvisible()) return;

        if (entity instanceof EntityPigZombie) {
            GL11.glScaled(KICConfig.kuudraMobSize, KICConfig.kuudraMobSize, KICConfig.kuudraMobSize);
            return;
        }

        if (entity instanceof EntityZombie) {
            EntityZombie zombie = (EntityZombie) entity;
            if (kIC$hasArmor(zombie)) GL11.glScaled(KICConfig.kuudraMobSize, KICConfig.kuudraMobSize, KICConfig.kuudraMobSize);
        }
    }

    @Unique
    private boolean kIC$hasArmor(EntityZombie zombie) {
        for (int slot = 1; slot <= 3; slot++) {
            ItemStack stack = zombie.getEquipmentInSlot(slot);
            if (isNonEmpty(stack)) return true;
        }
        return false;
    }
}
