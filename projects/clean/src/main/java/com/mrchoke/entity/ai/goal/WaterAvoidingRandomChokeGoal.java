package com.mrchoke.entity.ai.goal;

import com.mrchoke.entity.monster.BaseChokeZombie;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;
import java.util.List;

public class WaterAvoidingRandomChokeGoal extends WaterAvoidingRandomWalkingGoal {

    private BaseChokeZombie chokeZombie;

    public WaterAvoidingRandomChokeGoal(BaseChokeZombie chokeZombie, double speedIn)
    {
        super(chokeZombie, speedIn);
        this.chokeZombie = chokeZombie;
    }

    @Override
    public boolean shouldExecute() {
        if (chokeZombie.getTargetMode() != BaseChokeZombie.TargetModeEnum.NORMAL) {
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
                    System.out.println("In WaterAvoidingRandomChokeGoal, shouldExecute.  Will start. entPos=" + creature.getPosition() + ", tgtPos=" + x + "," + y + "," + z);
                }
                 */

                return true;
            }
        }
    }

    @Nullable
    protected Vec3d getPosition() {
        if(chokeZombie.getPlayerToFollow() == null) {
            List<ServerPlayerEntity> pList = chokeZombie.getServer().getPlayerList().getPlayers();
            if(pList.size() > 0) {
                chokeZombie.setPlayerToFollow(pList.get(chokeZombie.getRNG().nextInt(pList.size())));
            }
        }

        PlayerEntity player = chokeZombie.getPlayerToFollow();
        if(player != null) {
            if (this.creature.isInWaterOrBubbleColumn()) {
                Vec3d vec3d  = RandomPositionGenerator.getLandPosTargetTowardMaxAngle(chokeZombie, 15, 9, 0, new Vec3d(player.getPosition()), Math.PI / 3F);
                return vec3d == null ? RandomPositionGenerator.findRandomTargetToward(this.creature, 15, 9, new Vec3d(player.getPosition())) : vec3d;
            } else {
                return this.creature.getRNG().nextFloat() >=
                        this.probability ? RandomPositionGenerator.getLandPosTargetTowardMaxAngle(chokeZombie, 20, 13, 0, new Vec3d(player.getPosition()), Math.PI / 3F)
                            : RandomPositionGenerator.findRandomTargetToward(this.creature, 20, 13, new Vec3d(player.getPosition()));
            }
        }
        else {
            if (this.creature.isInWaterOrBubbleColumn()) {
                Vec3d vec3d = RandomPositionGenerator.findLandPos(this.creature, 15, 9);
                return vec3d == null ? RandomPositionGenerator.findRandomPos(this.creature, 15, 9) : vec3d;
            } else {
                return this.creature.getRNG().nextFloat() >= this.probability ? RandomPositionGenerator.findLandPos(this.creature, 20, 13)
                        : RandomPositionGenerator.findRandomPos(this.creature, 20, 13);   //was 10, 7
            }
        }
    }

}
