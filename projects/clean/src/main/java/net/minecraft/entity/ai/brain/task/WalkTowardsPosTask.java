package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.server.ServerWorld;

public class WalkTowardsPosTask extends Task<CreatureEntity> {
   private final MemoryModuleType<GlobalPos> memModuleType;
   private final int reachDist;
   private final int distance;
   private long execAgainTime;

   public WalkTowardsPosTask(MemoryModuleType<GlobalPos> memModuleType, int reachDist, int distance) {
      super(ImmutableMap.of(
              MemoryModuleType.WALK_TARGET, MemoryModuleStatus.REGISTERED,
              memModuleType, MemoryModuleStatus.VALUE_PRESENT));
      this.memModuleType = memModuleType;
      this.reachDist = reachDist;
      this.distance = distance;
   }

   protected boolean shouldExecute(ServerWorld worldIn, CreatureEntity owner) {
      Optional<GlobalPos> optional = owner.getBrain().getMemory(this.memModuleType);
      return optional.isPresent() && Objects.equals(worldIn.getDimension().getType(), optional.get().getDimension())
              && optional.get().getPos().withinDistance(owner.getPositionVec(), (double)this.distance);
   }

   protected void startExecuting(ServerWorld worldIn, CreatureEntity entityIn, long gameTimeIn) {
      if (gameTimeIn > this.execAgainTime) {
         Brain<?> brain = entityIn.getBrain();
         Optional<GlobalPos> optional = brain.getMemory(this.memModuleType);
         optional.ifPresent((gPos) -> {
            brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(gPos.getPos(), 0.4F, this.reachDist));
         });
         this.execAgainTime = gameTimeIn + 80L;
      }

   }
}
