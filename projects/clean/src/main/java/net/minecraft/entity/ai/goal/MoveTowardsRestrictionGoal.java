package net.minecraft.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.math.Vec3d;

public class MoveTowardsRestrictionGoal extends Goal {
   private final CreatureEntity creature;
   private double movePosX;
   private double movePosY;
   private double movePosZ;
   private final double movementSpeed;

   public MoveTowardsRestrictionGoal(CreatureEntity creatureIn, double speedIn) {
      this.creature = creatureIn;
      this.movementSpeed = speedIn;
      this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE));
   }

   public boolean shouldExecute() {
      if (this.creature.isWithinHomeDistanceCurrentPosition()) {
         return false;
      } else {
         Vec3d vec3d = RandomPositionGenerator.findRandomTargetBlockTowards(this.creature, 16, 7, new Vec3d(this.creature.getHomePosition()));
         if (vec3d == null) {
            return false;
         } else {
            this.movePosX = vec3d.x;
            this.movePosY = vec3d.y;
            this.movePosZ = vec3d.z;
            return true;
         }
      }
   }

   public boolean shouldContinueExecuting() {
      return !this.creature.getNavigator().noPath();
   }

   public void startExecuting() {
      this.creature.getNavigator().tryMoveToXYZ(this.movePosX, this.movePosY, this.movePosZ, this.movementSpeed);
   }
}
