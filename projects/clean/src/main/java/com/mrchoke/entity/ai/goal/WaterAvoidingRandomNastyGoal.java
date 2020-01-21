package com.mrchoke.entity.ai.goal;

import com.mrchoke.entity.monster.ZombieNasty;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;

public class WaterAvoidingRandomNastyGoal extends WaterAvoidingRandomWalkingGoal {

    private ZombieNasty nasty;

    public WaterAvoidingRandomNastyGoal(ZombieNasty nasty, double speedIn)
    {
        super(nasty, speedIn);
        this.nasty = nasty;
    }


    @Override
    public boolean shouldExecute() {
        if (nasty.getTargetMode() != ZombieNasty.TargetModeEnum.NORMAL) {
            return false;
        }

        //Copied from RandomWalkingGoal:
        if (this.creature.isBeingRidden()) {
            return false;
        } else {
            if (!this.mustUpdate) {
                if (this.creature.getIdleTime() >= 600) {   //AH CHANGE, increased from 100 to 600
                    return false;
                }

                if (this.creature.getRNG().nextInt(this.executionChance) != 0) {
                    return false;
                }
            }

            Vec3d vec3d = this.getPosition();
            if (vec3d == null) {
                return false;
            } else {
                this.x = vec3d.x;
                this.y = vec3d.y;
                this.z = vec3d.z;
                this.mustUpdate = false;

                //AH CHANGE DEBUG OFF
                /*
                if (this.creature.getCustomName() != null && this.creature.getCustomName().getString().equals("Chuck")) {
                    System.out.println("In WaterAvoidingRandomNastyGoal, shouldExecute.  Will start. entPos=" + creature.getPosition() + ", tgtPos=" + x + "," + y + "," + z);
                }
                 */

                return true;
            }
        }
    }

    @Nullable
    protected Vec3d getPosition() {
        if (this.creature.isInWaterOrBubbleColumn()) {
            Vec3d vec3d = RandomPositionGenerator.getLandPos(this.creature, 15, 7);
            return vec3d == null ? super.getPosition() : vec3d;
        } else {
            return this.creature.getRNG().nextFloat() >= this.probability ? RandomPositionGenerator.getLandPos(this.creature, 20, 10)
                    : RandomPositionGenerator.findRandomTarget(this.creature, 20, 10);   //was 10, 7
        }
    }

}
