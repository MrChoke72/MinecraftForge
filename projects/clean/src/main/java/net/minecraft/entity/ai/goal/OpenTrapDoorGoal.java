package net.minecraft.entity.ai.goal;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.monster.HuskEntity;

//AH CHANGE NEW
public class OpenTrapDoorGoal extends InteractTrapDoorGoal {
    private final boolean closeDoor;
    private int closeDoorTemporisation;

    public OpenTrapDoorGoal(MobEntity entitylivingIn, boolean shouldClose) {
        super(entitylivingIn);
        this.entity = entitylivingIn;
        this.closeDoor = shouldClose;
    }

    public boolean shouldContinueExecuting() {
        return this.closeDoor && this.closeDoorTemporisation > 0 && super.shouldContinueExecuting();
    }

    public void startExecuting() {
        if (this.entity.isChild()) {
            this.closeDoorTemporisation = 15;
        } else {
            this.closeDoorTemporisation = 30;
        }

        this.toggleDoor(true);
    }

    public void resetTask() {
        if(closeDoor) {
            this.toggleDoor(false);
        }
    }

    public void tick() {
        --this.closeDoorTemporisation;

        //AH CHANGE DEBUG OFF
        /*
        if(this.entity instanceof HuskEntity && this.entity.getCustomName() != null) // && this.entity.getCustomName().getString().equals("Chuck"))
        {
            System.out.println("OpenTrapDoorGoal tick.  closeDoorTemporisation=" + closeDoorTemporisation);
        }
         */

        super.tick();
    }
}
