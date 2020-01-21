package com.mrchoke.entity.ai.goal;

import com.mrchoke.entity.monster.ZombieNasty;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.player.PlayerEntity;

public class NearestAttTargetNastyGoal<T extends LivingEntity> extends NearestAttackableTargetGoal {

    public NearestAttTargetNastyGoal(MobEntity entity, Class<T> targetClass, boolean checkSight) {
        super(entity, targetClass, checkSight, false);
    }

    @Override
    public void startExecuting() {
        super.startExecuting();

        if(goalOwner.getAttackTarget() instanceof PlayerEntity) {
            ((ZombieNasty)goalOwner).setPlayerToFollow((PlayerEntity)goalOwner.getAttackTarget());
        }
    }

}
