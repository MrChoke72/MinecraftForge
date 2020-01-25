package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.server.ServerWorld;

public class StayNearPointTask extends Task<VillagerEntity> {
   private final MemoryModuleType<GlobalPos> memModulePos;
   private final float speed;
   private final int reachDist;
   private final int distance;
   private final int reachTgtMaxWait;

   public StayNearPointTask(MemoryModuleType<GlobalPos> memModulePos, float speedIn, int reachDist, int distance, int reachTgtMaxWait) {
      super(ImmutableMap.of(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleStatus.REGISTERED, MemoryModuleType.WALK_TARGET, MemoryModuleStatus.VALUE_ABSENT, memModulePos, MemoryModuleStatus.VALUE_PRESENT));
      this.memModulePos = memModulePos;
      this.speed = speedIn;
      this.reachDist = reachDist;
      this.distance = distance;
      this.reachTgtMaxWait = reachTgtMaxWait;
   }

   private void setCantReachTgtMem(VillagerEntity villagerEntity, long gameTime) {
      Brain<?> brain = villagerEntity.getBrain();
      villagerEntity.removeMemoryPos(this.memModulePos);
      brain.removeMemory(this.memModulePos);
      brain.setMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, gameTime);
   }

   protected void startExecuting(ServerWorld worldIn, VillagerEntity entityIn, long gameTimeIn) {
      Brain<?> brain = entityIn.getBrain();
      brain.getMemory(this.memModulePos).ifPresent((gPos) -> {
         if (this.checkReachTgtWaitTime(worldIn, entityIn)) {
            this.setCantReachTgtMem(entityIn, gameTimeIn);
         } else if (this.isDistanceTooFar(worldIn, entityIn, gPos)) {
            Vec3d vec3d = null;
            int i = 0;

            for(int j = 1000; i < 1000 && (vec3d == null || this.isDistanceTooFar(worldIn, entityIn, GlobalPos.of(entityIn.dimension, new BlockPos(vec3d)))); ++i) {
               vec3d = RandomPositionGenerator.findRandomTargetToward(entityIn, 15, 7, new Vec3d(gPos.getPos()));
            }

            if (i == 1000) {
               this.setCantReachTgtMem(entityIn, gameTimeIn);
               return;
            }

            brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(vec3d, this.speed, this.reachDist));
         } else if (!this.isTargetReached(worldIn, entityIn, gPos)) {
            brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(gPos.getPos(), this.speed, this.reachDist));
         }

      });
   }

   private boolean checkReachTgtWaitTime(ServerWorld world, VillagerEntity villagerEntity) {
      Optional<Long> optional = villagerEntity.getBrain().getMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
      if (optional.isPresent()) {
         return world.getGameTime() - optional.get() > (long)this.reachTgtMaxWait;
      } else {
         return false;
      }
   }

   private boolean isDistanceTooFar(ServerWorld world, VillagerEntity villagerEntity, GlobalPos gPos) {
      return gPos.getDimension() != world.getDimension().getType() || gPos.getPos().manhattanDistance(new BlockPos(villagerEntity)) > this.distance;
   }

   private boolean isTargetReached(ServerWorld world, VillagerEntity villagerEntity, GlobalPos gPos) {
      return gPos.getDimension() == world.getDimension().getType() && gPos.getPos().manhattanDistance(new BlockPos(villagerEntity)) <= this.reachDist;
   }
}
