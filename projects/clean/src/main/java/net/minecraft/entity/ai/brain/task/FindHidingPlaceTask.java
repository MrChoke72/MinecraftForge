package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.village.PointOfInterestManager;
import net.minecraft.village.PointOfInterestType;
import net.minecraft.world.server.ServerWorld;

public class FindHidingPlaceTask extends Task<LivingEntity> {
   private final float moveSpeed;

   //AH REFACTOR
   private final int distLookForHome;
   //private final int field_220458_b;

   private final int distNearHome;
   private Optional<BlockPos> hidePlacePos = Optional.empty();

   //AH REFACTOR
   public FindHidingPlaceTask(int distLookForHome, float moveSpeed, int distNearHome) {
   //public FindHidingPlaceTask(int p_i50361_1_, float p_i50361_2_, int p_i50361_3_) {
      super(ImmutableMap.of(
              MemoryModuleType.WALK_TARGET, MemoryModuleStatus.VALUE_ABSENT,
              MemoryModuleType.HOME, MemoryModuleStatus.REGISTERED,
              MemoryModuleType.HIDING_PLACE, MemoryModuleStatus.REGISTERED));
      this.distLookForHome = distLookForHome;
      this.moveSpeed = moveSpeed;
      this.distNearHome = distNearHome;
   }

   protected boolean shouldExecute(ServerWorld worldIn, LivingEntity owner) {
      Optional<BlockPos> optional = worldIn.getPointOfInterestManager().poiOptByDistFiltPos((poiType) -> {
         return poiType == PointOfInterestType.HOME;
      }, (pos) -> {
         return true;
      }, new BlockPos(owner), this.distNearHome + 1, PointOfInterestManager.Status.ANY);
      if (optional.isPresent() && optional.get().withinDistance(owner.getPositionVec(), (double)this.distNearHome)) {
         this.hidePlacePos = optional;
      } else {
         this.hidePlacePos = Optional.empty();
      }

      return true;
   }

   protected void startExecuting(ServerWorld worldIn, LivingEntity entityIn, long gameTimeIn) {
      Brain<?> brain = entityIn.getBrain();
      Optional<BlockPos> optional = this.hidePlacePos;
      if (!optional.isPresent()) {
         optional = worldIn.getPointOfInterestManager().getRandomPoiPos((poiType) -> {
            return poiType == PointOfInterestType.HOME;
         }, (pos) -> {
            return true;
         }, PointOfInterestManager.Status.ANY, new BlockPos(entityIn), this.distLookForHome, entityIn.getRNG());
         if (!optional.isPresent()) {
            Optional<GlobalPos> optional1 = brain.getMemory(MemoryModuleType.HOME);
            if (optional1.isPresent()) {
               optional = Optional.of(optional1.get().getPos());
            }
         }
      }

      if (optional.isPresent()) {
         brain.removeMemory(MemoryModuleType.PATH);
         brain.removeMemory(MemoryModuleType.LOOK_TARGET);
         brain.removeMemory(MemoryModuleType.BREED_TARGET);
         brain.removeMemory(MemoryModuleType.INTERACTION_TARGET);
         brain.setMemory(MemoryModuleType.HIDING_PLACE, GlobalPos.of(worldIn.getDimension().getType(), optional.get()));
         if (!optional.get().withinDistance(entityIn.getPositionVec(), (double)this.distNearHome)) {
            brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(optional.get(), this.moveSpeed, this.distNearHome));
         }
      }

   }
}
