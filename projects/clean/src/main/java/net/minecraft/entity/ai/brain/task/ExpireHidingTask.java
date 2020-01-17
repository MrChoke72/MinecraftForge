package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

public class ExpireHidingTask extends Task<LivingEntity> {
   private final int distNearHidingPlace;
   private final int numTicksToHide;
   private int tickCountHiding;

   public ExpireHidingTask(int secsToHide, int distNearHidingPlace) {
      super(ImmutableMap.of(MemoryModuleType.HIDING_PLACE, MemoryModuleStatus.VALUE_PRESENT, MemoryModuleType.HEARD_BELL_TIME, MemoryModuleStatus.VALUE_PRESENT));
      this.numTicksToHide = secsToHide * 20;
      this.tickCountHiding = 0;
      this.distNearHidingPlace = distNearHidingPlace;
   }

   protected void startExecuting(ServerWorld worldIn, LivingEntity entityIn, long gameTimeIn) {
      Brain<?> brain = entityIn.getBrain();
      Optional<Long> optional = brain.getMemory(MemoryModuleType.HEARD_BELL_TIME);
      boolean flag = optional.get() + 300L <= gameTimeIn;
      if (this.tickCountHiding <= this.numTicksToHide && !flag) {
         BlockPos blockpos = brain.getMemory(MemoryModuleType.HIDING_PLACE).get().getPos();
         if (blockpos.withinDistance(new BlockPos(entityIn), (double)(this.distNearHidingPlace + 1))) {
            ++this.tickCountHiding;
         }

      } else {
         brain.removeMemory(MemoryModuleType.HEARD_BELL_TIME);
         brain.removeMemory(MemoryModuleType.HIDING_PLACE);
         brain.updateActivity(worldIn.getDayTime(), worldIn.getGameTime());
         this.tickCountHiding = 0;
      }
   }
}
