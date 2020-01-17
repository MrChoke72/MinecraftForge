package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.schedule.Activity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.world.server.ServerWorld;

public class PanicTask extends Task<VillagerEntity> {
   public PanicTask() {
      super(ImmutableMap.of());
   }

   protected boolean shouldContinueExecuting(ServerWorld worldIn, VillagerEntity entityIn, long gameTimeIn) {
      return hasHurtByMem(entityIn) || hasNearestHostileMem(entityIn);
   }

   protected void startExecuting(ServerWorld worldIn, VillagerEntity entityIn, long gameTimeIn) {
      if (hasHurtByMem(entityIn) || hasNearestHostileMem(entityIn)) {
         Brain<?> brain = entityIn.getBrain();
         if (!brain.hasActivity(Activity.PANIC)) {
            brain.removeMemory(MemoryModuleType.PATH);
            brain.removeMemory(MemoryModuleType.WALK_TARGET);
            brain.removeMemory(MemoryModuleType.LOOK_TARGET);
            brain.removeMemory(MemoryModuleType.BREED_TARGET);
            brain.removeMemory(MemoryModuleType.INTERACTION_TARGET);
         }

         //AH CHANGE DEBUG OFF
         /*
         if(entityIn.getCustomName() != null) // && this.entity.getCustomName().getString().equals("Chuck"))
         {
            System.out.println("PanicTask::startExecuting, switching to panic activity");
         }
         */

         brain.switchTo(Activity.PANIC);
      }

   }

   protected void updateTask(ServerWorld worldIn, VillagerEntity owner, long gameTime) {
      if (gameTime % 100L == 0L) {
         owner.checkCreateIronGolem(gameTime, 3);
      }

   }

   public static boolean hasNearestHostileMem(LivingEntity entity) {
      return entity.getBrain().hasMemory(MemoryModuleType.NEAREST_HOSTILE);
   }

   public static boolean hasHurtByMem(LivingEntity entity) {
      return entity.getBrain().hasMemory(MemoryModuleType.HURT_BY);
   }
}
