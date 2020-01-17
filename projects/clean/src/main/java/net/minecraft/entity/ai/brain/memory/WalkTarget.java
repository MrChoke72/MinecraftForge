package net.minecraft.entity.ai.brain.memory;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPosWrapper;
import net.minecraft.util.math.IPosWrapper;
import net.minecraft.util.math.Vec3d;

public class WalkTarget {
   private final IPosWrapper target;
   private final float speed;

   //AH REFACTOR
   private final int reachDist;
   //private final int distance;

   public WalkTarget(BlockPos targetIn, float speedIn, int reachDist) {
      this(new BlockPosWrapper(targetIn), speedIn, reachDist);
   }

   public WalkTarget(Vec3d targetIn, float speedIn, int reachDist) {
      this(new BlockPosWrapper(new BlockPos(targetIn)), speedIn, reachDist);
   }

   public WalkTarget(IPosWrapper targetIn, float speedIn, int reachDist) {
      this.target = targetIn;
      this.speed = speedIn;
      this.reachDist = reachDist;
   }

   public IPosWrapper getTarget() {
      return this.target;
   }

   public float getSpeed() {
      return this.speed;
   }

   public int getReachDist() {
      return this.reachDist;
   }
}
