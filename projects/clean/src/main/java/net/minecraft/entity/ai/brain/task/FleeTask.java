package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.server.ServerWorld;

public class FleeTask extends Task<CreatureEntity> {
   private final MemoryModuleType<? extends Entity> avoidEntityMem;
   private final float speed;

   public FleeTask(MemoryModuleType<? extends Entity> memModuleType, float speed) {
      super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryModuleStatus.VALUE_ABSENT, memModuleType, MemoryModuleStatus.VALUE_PRESENT));
      this.avoidEntityMem = memModuleType;
      this.speed = speed;
   }

   protected boolean shouldExecute(ServerWorld worldIn, CreatureEntity owner) {
      Entity entity = owner.getBrain().getMemory(this.avoidEntityMem).get();
      return owner.getDistanceSq(entity) < 36.0D;
   }

   protected void startExecuting(ServerWorld worldIn, CreatureEntity owner, long gameTimeIn) {
      Entity entity = owner.getBrain().getMemory(this.avoidEntityMem).get();
      setWalkTarget(owner, entity, this.speed);
   }

   public static void setWalkTarget(CreatureEntity owner, Entity entityAvoid, float speed) {
      for(int i = 0; i < 10; ++i) {
         Vec3d vec3d = RandomPositionGenerator.findLandTargetPosAwayFrom(owner, 16, 7, entityAvoid.getPositionVec());
         if (vec3d != null) {
            owner.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(vec3d, speed, 0));
            return;
         }
      }

   }
}
