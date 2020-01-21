package com.mrchoke.entity.ai.goal;

import net.minecraft.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.entity.monster.ZombieEntity;

public class ZombieNastyAttackGoal extends ZombieAttackGoal {

    public ZombieNastyAttackGoal(ZombieEntity zombieIn, double speedIn)
    {
        super(zombieIn, speedIn, true); //always long memory
    }

    @Override
    public void tick() {
        attacker.setIdleTime(0);    //stop despawn if he is chasing
        super.tick();
    }

}
