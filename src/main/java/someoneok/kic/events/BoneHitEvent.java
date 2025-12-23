package someoneok.kic.events;

import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.common.eventhandler.Event;

public class BoneHitEvent extends Event {
    private final EntityLivingBase target;
    private final Hit type;

    public BoneHitEvent(EntityLivingBase target, Hit type) {
        this.target = target;
        this.type = type;
    }

    public EntityLivingBase getTarget() { return target; }

    public Hit getType() { return type; }

    public enum Hit {
        FRONT, BACK
    }
}
