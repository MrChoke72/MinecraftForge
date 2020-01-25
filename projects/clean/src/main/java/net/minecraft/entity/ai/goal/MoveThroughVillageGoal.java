package net.minecraft.entity.ai.goal;

import com.google.common.collect.Lists;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.PointOfInterestManager;
import net.minecraft.village.PointOfInterestType;
import net.minecraft.world.server.ServerWorld;

public class MoveThroughVillageGoal extends Goal {
   protected final CreatureEntity entity;
   private final double movementSpeed;
   private Path path;
   private BlockPos targetPos;
   private final boolean isNocturnal;
   private final List<BlockPos> doorList = Lists.newArrayList();
   private final int distNearTarget;
   private final BooleanSupplier isBreakDoorsTaskSet;

   public MoveThroughVillageGoal(CreatureEntity entity, double speedIn, boolean isNocturnal, int distNearTarget, BooleanSupplier isBreakDoorsTaskSet) {
      this.entity = entity;
      this.movementSpeed = speedIn;
      this.isNocturnal = isNocturnal;
      this.distNearTarget = distNearTarget;
      this.isBreakDoorsTaskSet = isBreakDoorsTaskSet;
      this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE));
      if (!(entity.getNavigator() instanceof GroundPathNavigator)) {
         throw new IllegalArgumentException("Unsupported mob for MoveThroughVillageGoal");
      }
   }

   public boolean shouldExecute() {
      this.resizeDoorList();
      if (this.isNocturnal && this.entity.world.isDaytime()) {
         return false;
      } else {
         ServerWorld serverworld = (ServerWorld)this.entity.world;
         BlockPos blockpos = new BlockPos(this.entity);
         if (!serverworld.isPosBelowEQSecLevel(blockpos, 6)) {
            return false;
         } else {
            Vec3d vec3d = RandomPositionGenerator.findLandPosWeight(this.entity, 15, 7, (pos) -> {
               if (!serverworld.isPosBelowEQSecLevel1(pos)) {
                  return Double.NEGATIVE_INFINITY;
               } else {
                  Optional<BlockPos> optional1 = serverworld.getPoiMgr().poiOptByDistFiltPos(PointOfInterestType.POI_TYPE_PRED_TRUE, this::isNotTrackedDoor, pos, 10,
                          PointOfInterestManager.Status.IS_OCCUPIED);
                  return !optional1.isPresent() ? Double.NEGATIVE_INFINITY : -optional1.get().distanceSq(blockpos);
               }
            });
            if (vec3d == null) {
               return false;
            } else {
               Optional<BlockPos> optional = serverworld.getPoiMgr().poiOptByDistFiltPos(PointOfInterestType.POI_TYPE_PRED_TRUE, this::isNotTrackedDoor, new BlockPos(vec3d), 10,
                       PointOfInterestManager.Status.IS_OCCUPIED);
               if (!optional.isPresent()) {
                  return false;
               } else {
                  this.targetPos = optional.get().toImmutable();
                  GroundPathNavigator groundpathnavigator = (GroundPathNavigator)this.entity.getNavigator();
                  boolean flag = groundpathnavigator.getEnterDoors();
                  groundpathnavigator.setBreakDoors(this.isBreakDoorsTaskSet.getAsBoolean());
                  this.path = groundpathnavigator.getPathToPos(this.targetPos, 0);
                  groundpathnavigator.setBreakDoors(flag);
                  if (this.path == null) {
                     Vec3d vec3d1 = RandomPositionGenerator.findRandomTargetToward(this.entity, 10, 7, new Vec3d(this.targetPos));
                     if (vec3d1 == null) {
                        return false;
                     }

                     groundpathnavigator.setBreakDoors(this.isBreakDoorsTaskSet.getAsBoolean());
                     this.path = this.entity.getNavigator().getPathToPos(vec3d1.x, vec3d1.y, vec3d1.z, 0);
                     groundpathnavigator.setBreakDoors(flag);
                     if (this.path == null) {
                        return false;
                     }
                  }

                  for(int i = 0; i < this.path.getCurrentPathLength(); ++i) {
                     PathPoint pathpoint = this.path.getPathPointFromIndex(i);
                     BlockPos blockpos1 = new BlockPos(pathpoint.x, pathpoint.y + 1, pathpoint.z);
                     if (InteractDoorGoal.canInteractDoor(this.entity.world, blockpos1)) {
                        this.path = this.entity.getNavigator().getPathToPos((double)pathpoint.x, (double)pathpoint.y, (double)pathpoint.z, 0);
                        break;
                     }
                  }

                  return this.path != null;
               }
            }
         }
      }
   }

   public boolean shouldContinueExecuting() {
      if (this.entity.getNavigator().noPath()) {
         return false;
      } else {
         return !this.targetPos.withinDistance(this.entity.getPositionVec(), (double)(this.entity.getWidth() + (float)this.distNearTarget));
      }
   }

   public void startExecuting() {
      this.entity.getNavigator().setPath(this.path, this.movementSpeed);
   }

   public void resetTask() {
      if (this.entity.getNavigator().noPath() || this.targetPos.withinDistance(this.entity.getPositionVec(), (double)this.distNearTarget)) {
         this.doorList.add(this.targetPos);
      }

   }

   private boolean isNotTrackedDoor(BlockPos pos) {
      for(BlockPos blockpos : this.doorList) {
         if (Objects.equals(pos, blockpos)) {
            return false;
         }
      }

      return true;
   }

   private void resizeDoorList() {
      if (this.doorList.size() > 15) {
         this.doorList.remove(0);
      }

   }
}
