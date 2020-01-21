package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.brain.BrainUtil;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.SectionPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.server.ServerWorld;

public class FindWalkTargetTask extends Task<CreatureEntity> {

   //AH REFACTOR
   private final float speed;
   //private final float field_220597_a;

   //AH REFACTOR
   private final int randXZ;
   //private final int field_223525_b;

   //AH REFACTOR
   private final int randY;
   //private final int field_223526_c;

   public FindWalkTargetTask(float moveSpeed) {
      this(moveSpeed, 10, 7);
   }

   public FindWalkTargetTask(float speedIn, int xz, int y) {
      super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryModuleStatus.VALUE_ABSENT));
      this.speed = speedIn;
      this.randXZ = xz;
      this.randY = y;
   }

   protected void startExecuting(ServerWorld worldIn, CreatureEntity entityIn, long gameTimeIn) {
      BlockPos blockpos = new BlockPos(entityIn);
      if (worldIn.isPosBelowEQSecLevel1(blockpos)) {
         this.findLandPosition(entityIn);
      } else {
         SectionPos sectionpos = SectionPos.from(blockpos);
         SectionPos sectionpos1 = BrainUtil.getSecPosLowerInRadius(worldIn, sectionpos, 2);
         if (sectionpos1 != sectionpos) {
            this.findBlockTowardPos(entityIn, sectionpos1);
         } else {
            this.findLandPosition(entityIn);
         }
      }

   }

   //AH REFACTOR
   private void findBlockTowardPos(CreatureEntity entity, SectionPos secPos) {
   //private void func_220594_a(CreatureEntity p_220594_1_, SectionPos p_220594_2_) {
      Optional<Vec3d> optional = Optional.ofNullable(RandomPositionGenerator.findRandomTargetBlockTowards(entity, this.randXZ, this.randY, new Vec3d(secPos.getCenter())));
      entity.getBrain().setMemory(MemoryModuleType.WALK_TARGET, optional.map((vec3d) -> {
         return new WalkTarget(vec3d, this.speed, 0);
      }));
   }

   private void findLandPosition(CreatureEntity entity) {
   //private void func_220593_a(CreatureEntity p_220593_1_) {
      Optional<Vec3d> optional = Optional.ofNullable(RandomPositionGenerator.getLandPos(entity, this.randXZ, this.randY));
      entity.getBrain().setMemory(MemoryModuleType.WALK_TARGET, optional.map((vec3d) -> {
         return new WalkTarget(vec3d, this.speed, 0);
      }));
   }
}
