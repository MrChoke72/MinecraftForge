package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.server.ServerWorld;

public class WalkToTargetTask extends Task<MobEntity> {
   @Nullable

   //AH REFACTOR
   private Path path;
   //private Path field_220488_a;

   @Nullable
   //AH REFACTOR
   private BlockPos targetPos;
   //private BlockPos field_220489_b;

   //AH REFACTOR
   private float targetSpeed;
   //private float field_220490_c;

   //AH REFACTOR
   private int tickCount;
   //private int field_220491_d;

   public WalkToTargetTask(int duration) {
      super(ImmutableMap.of(
              MemoryModuleType.PATH, MemoryModuleStatus.VALUE_ABSENT,
              MemoryModuleType.WALK_TARGET, MemoryModuleStatus.VALUE_PRESENT),
              duration);
   }

   protected boolean shouldExecute(ServerWorld worldIn, MobEntity owner) {
      Brain<?> brain = owner.getBrain();
      WalkTarget walktarget = brain.getMemory(MemoryModuleType.WALK_TARGET).get();
      if (!this.hasReachedTarget(owner, walktarget) && this.setPath(owner, walktarget, worldIn.getGameTime())) {
         this.targetPos = walktarget.getTarget().getBlockPos();


         //AH CHANGE DEBUG
         if(owner.getCustomName() != null && owner.getCustomName().getString().equals("Chuck"))
         {
            System.out.println("WalkToTargetTask  targetPos=" + targetPos.getX() + "," + targetPos.getY() + "," + targetPos.getZ());
         }


         return true;
      } else {
         brain.removeMemory(MemoryModuleType.WALK_TARGET);
         return false;
      }
   }

   protected boolean shouldContinueExecuting(ServerWorld worldIn, MobEntity entityIn, long gameTimeIn) {
      if (this.path != null && this.targetPos != null) {
         Optional<WalkTarget> optional = entityIn.getBrain().getMemory(MemoryModuleType.WALK_TARGET);
         PathNavigator pathnavigator = entityIn.getNavigator();
         return !pathnavigator.noPath() && optional.isPresent() && !this.hasReachedTarget(entityIn, optional.get());
      } else {
         return false;
      }
   }

   protected void resetTask(ServerWorld worldIn, MobEntity entityIn, long gameTimeIn) {
      entityIn.getNavigator().clearPath();
      entityIn.getBrain().removeMemory(MemoryModuleType.WALK_TARGET);
      entityIn.getBrain().removeMemory(MemoryModuleType.PATH);
      this.path = null;
   }

   protected void startExecuting(ServerWorld worldIn, MobEntity entityIn, long gameTimeIn) {
      entityIn.getBrain().setMemory(MemoryModuleType.PATH, this.path);
      entityIn.getNavigator().setPath(this.path, (double)this.targetSpeed);
      this.tickCount = worldIn.getRandom().nextInt(10);
   }

   protected void updateTask(ServerWorld worldIn, MobEntity owner, long gameTime) {
      --this.tickCount;
      if (this.tickCount <= 0) {
         Path path = owner.getNavigator().getPath();
         Brain<?> brain = owner.getBrain();
         if (this.path != path) {
            this.path = path;
            brain.setMemory(MemoryModuleType.PATH, path);
         }

         if (path != null && this.targetPos != null) {
            WalkTarget walktarget = brain.getMemory(MemoryModuleType.WALK_TARGET).get();
            if (walktarget.getTarget().getBlockPos().distanceSq(this.targetPos) > 4.0D && this.setPath(owner, walktarget, worldIn.getGameTime())) {
               this.targetPos = walktarget.getTarget().getBlockPos();
               this.startExecuting(worldIn, owner, gameTime);
            }

         }
      }
   }

   //AH REFACTOR
   private boolean setPath(MobEntity entity, WalkTarget target, long gameTime) {
   //private boolean func_220487_a(MobEntity p_220487_1_, WalkTarget p_220487_2_, long p_220487_3_) {
      BlockPos blockpos = target.getTarget().getBlockPos();
      this.path = entity.getNavigator().getPathToPos(blockpos, 0);
      this.targetSpeed = target.getSpeed();
      if (!this.hasReachedTarget(entity, target)) {
         Brain<?> brain = entity.getBrain();
         boolean flag = this.path != null && this.path.isCompletePath();
         if (flag) {
            brain.setMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, Optional.empty());
         } else if (!brain.hasMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)) {
            brain.setMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, gameTime);
         }

         if (this.path != null) {
            return true;
         }

         Vec3d vec3d = RandomPositionGenerator.findRandomTargetBlockTowards((CreatureEntity)entity, 10, 7, new Vec3d(blockpos));
         if (vec3d != null) {
            this.path = entity.getNavigator().getPathToPos(vec3d.x, vec3d.y, vec3d.z, 0);
            return this.path != null;
         }
      }

      return false;
   }

   private boolean hasReachedTarget(MobEntity p_220486_1_, WalkTarget walkTarget) {
      return walkTarget.getTarget().getBlockPos().manhattanDistance(new BlockPos(p_220486_1_)) <= walkTarget.getReachDist();
   }
}
