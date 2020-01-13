package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.server.ServerWorld;

public class UpdateActivityTask extends Task<LivingEntity> {
   public UpdateActivityTask() {
      super(ImmutableMap.of());
   }

   protected void startExecuting(ServerWorld worldIn, LivingEntity entityIn, long gameTimeIn) {

      //AH CHANGE DEBUG OFF
      /*
      if(entityIn.getCustomName() != null) // && this.entity.getCustomName().getString().equals("Chuck"))
      {
         System.out.println("UpdateActivityTask::startExecuting, dayTime=" + (worldIn.getDayTime() % 24000L) + ", activity BEFORE=" + entityIn.getBrain().getActivities());
      }
       */

      entityIn.getBrain().updateActivity(worldIn.getDayTime(), worldIn.getGameTime());

      //AH CHANGE DEBUG OFF
      /*
      if(entityIn.getCustomName() != null) // && this.entity.getCustomName().getString().equals("Chuck"))
      {
         System.out.println("UpdateActivityTask::startExecuting, activity AFTER=" + entityIn.getBrain().getActivities());
      }
       */

   }
}
