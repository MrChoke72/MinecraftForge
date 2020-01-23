package com.mrchoke.entity.ai.goal;

import com.mrchoke.entity.monster.BaseChokeZombie;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.player.PlayerEntity;

public class NearestAttTargetChokeGoal<T extends LivingEntity> extends NearestAttackableTargetGoal {

    public NearestAttTargetChokeGoal(MobEntity entity, Class<T> targetClass, boolean checkSight) {
        super(entity, targetClass, checkSight, false);
    }

    @Override
    public void startExecuting() {
        super.startExecuting();

        if(goalOwner.getAttackTarget() instanceof PlayerEntity) {
            ((BaseChokeZombie)goalOwner).setPlayerToFollow((PlayerEntity)goalOwner.getAttackTarget());
        }
    }

}
