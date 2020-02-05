package net.minecraft.entity.ai;

import java.util.Random;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import javax.annotation.Nullable;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class RandomPositionGenerator {
   @Nullable
   public static Vec3d findRandomTarget(CreatureEntity entitycreatureIn, int xz, int y) {
      return getRandomPosition(entitycreatureIn, xz, y, 0, (Vec3d)null, true, (double)((float)Math.PI / 2F), entitycreatureIn::getBlockPathWeight, false, 0, 0, true);
   }

   @Nullable
   public static Vec3d findRandomTowardMaxAngle(CreatureEntity entity, int xz, int y, int yOffset, @Nullable Vec3d dirTowardVec, double maxTowardAngle) {
      return getRandomPosition(entity, xz, y, yOffset, dirTowardVec, true, maxTowardAngle, entity::getBlockPathWeight, true, 0, 0, false);
   }

   @Nullable
   public static Vec3d getLandPos(CreatureEntity creature, int maxXZ, int maxY) {
      return findLandPosWeight(creature, maxXZ, maxY, creature::getBlockPathWeight);
   }

   @Nullable
   public static Vec3d findLandPosWeight(CreatureEntity entity, int maxXZ, int maxY, ToDoubleFunction<BlockPos> blockPathWeight) {
      return getRandomPosition(entity, maxXZ, maxY, 0, (Vec3d)null, false, 0.0D, blockPathWeight, true, 0, 0, true);
   }

   @Nullable
   public static Vec3d getLandPosTowardMaxAngle(CreatureEntity entity, int maxXZ, int maxY, Vec3d dirTowardVec, float maxTowardAngle, int p_226340_5_, int p_226340_6_) {
      return getRandomPosition(entity, maxXZ, maxY, 0, dirTowardVec, false, (double)maxTowardAngle, entity::getBlockPathWeight, true, p_226340_5_, p_226340_6_, true);
   }

   @Nullable
   public static Vec3d findRandomTargetBlockTowards(CreatureEntity entitycreatureIn, int xz, int y, Vec3d targetVec3) {
      Vec3d vec3d = targetVec3.subtract(entitycreatureIn.getPosX(), entitycreatureIn.getPosY(), entitycreatureIn.getPosZ());
      return getRandomPosition(entitycreatureIn, xz, y, 0, vec3d, true, (double)((float)Math.PI / 2F), entitycreatureIn::getBlockPathWeight, false, 0, 0, true);
   }

   @Nullable
   public static Vec3d findRandomTargetTowardsScaled(CreatureEntity entitycreatureIn, int maxXZ, int maxY, Vec3d targetVec3, double maxTowardAngle) {
      Vec3d dirTowardVec = targetVec3.subtract(entitycreatureIn.getPosX(), entitycreatureIn.getPosY(), entitycreatureIn.getPosZ());
      return getRandomPosition(entitycreatureIn, maxXZ, maxY, 0, dirTowardVec, true, maxTowardAngle, entitycreatureIn::getBlockPathWeight, false, 0, 0, true);
   }

   @Nullable
   public static Vec3d getLandPosTargetTowardMaxAngle(CreatureEntity entitycreatureIn, int maxXZ, int maxY, int yOffset, Vec3d targetVec3, double maxTowardAngle) {
      Vec3d dirTowardVec = targetVec3.subtract(entitycreatureIn.getPosX(), entitycreatureIn.getPosY(), entitycreatureIn.getPosZ());
      return getRandomPosition(entitycreatureIn, maxXZ, maxY, yOffset, dirTowardVec, false, maxTowardAngle, entitycreatureIn::getBlockPathWeight, true, 0, 0, false);
   }

   @Nullable
   public static Vec3d findRandomTargetBlockAwayFrom(CreatureEntity entitycreatureIn, int xz, int y, Vec3d targetVec3) {
      Vec3d vec3d = entitycreatureIn.getPositionVec().subtract(targetVec3);
      return getRandomPosition(entitycreatureIn, xz, y, 0, vec3d, true, (double)((float)Math.PI / 2F), entitycreatureIn::getBlockPathWeight, false, 0, 0, true);
   }

   @Nullable
   public static Vec3d findLandTargetPosAwayFrom(CreatureEntity p_223548_0_, int p_223548_1_, int p_223548_2_, Vec3d p_223548_3_) {
      Vec3d vec3d = p_223548_0_.getPositionVec().subtract(p_223548_3_);
      return getRandomPosition(p_223548_0_, p_223548_1_, p_223548_2_, 0, vec3d, false, (double)((float)Math.PI / 2F), p_223548_0_::getBlockPathWeight, true, 0, 0, true);
   }

   @Nullable
   private static Vec3d getRandomPosition(CreatureEntity entity, int xz, int y, int yOffset, @Nullable Vec3d dirTowardVec, boolean noAvoidWater,
                                          double maxTowardAngle, ToDoubleFunction<BlockPos> blockWeightFunc, boolean p_226339_9_, int aboveSolidRange, int p_226339_11_, boolean p_226339_12_) {
      PathNavigator pathnavigator = entity.getNavigator();
      Random random = entity.getRNG();
      boolean flag;
      if (entity.detachHome()) {
         flag = entity.getHomePosition().withinDistance(entity.getPositionVec(), (double)(entity.getMaximumHomeDistance() + (float)xz) + 1.0D);
      } else {
         flag = false;
      }

      boolean flag1 = false;
      double d0 = Double.NEGATIVE_INFINITY;
      BlockPos blockpos = new BlockPos(entity);

      for(int i = 0; i < 10; ++i) {
         BlockPos blockpos1 = getRandPos(random, xz, y, yOffset, dirTowardVec, maxTowardAngle);
         if (blockpos1 != null) {
            int j = blockpos1.getX();
            int k = blockpos1.getY();
            int l = blockpos1.getZ();
            if (entity.detachHome() && xz > 1) {
               BlockPos blockpos2 = entity.getHomePosition();
               if (entity.getPosX() > (double)blockpos2.getX()) {
                  j -= random.nextInt(xz / 2);
               } else {
                  j += random.nextInt(xz / 2);
               }

               if (entity.getPosZ() > (double)blockpos2.getZ()) {
                  l -= random.nextInt(xz / 2);
               } else {
                  l += random.nextInt(xz / 2);
               }
            }

            BlockPos blockpos3 = new BlockPos((double)j + entity.getPosX(), (double)k + entity.getPosY(), (double)l + entity.getPosZ());
            if (blockpos3.getY() >= 0 && blockpos3.getY() <= entity.world.getHeight() && (!flag || entity.isWithinHomeDistanceFromPosition(blockpos3)) && (!p_226339_12_ || pathnavigator.canEntityStandOnPos(blockpos3))) {
               if (p_226339_9_) {
                  blockpos3 = getBlockAboveSold(blockpos3, random.nextInt(aboveSolidRange + 1) + p_226339_11_, entity.world.getHeight(), (pos) -> {
                     return entity.world.getBlockState(pos).getMaterial().isSolid();
                  });
               }

               if (noAvoidWater || !entity.world.getFluidState(blockpos3).isTagged(FluidTags.WATER)) {
                  PathNodeType pathnodetype = WalkNodeProcessor.getPathNodeTypeDamage(entity.world, blockpos3.getX(), blockpos3.getY(), blockpos3.getZ());
                  if (entity.getPathPriority(pathnodetype) == 0.0F) {
                     double d1 = blockWeightFunc.applyAsDouble(blockpos3);
                     if (d1 > d0) {
                        d0 = d1;
                        blockpos = blockpos3;
                        flag1 = true;
                     }
                  }
               }
            }
         }
      }

      return flag1 ? new Vec3d(blockpos) : null;
   }

   @Nullable
   private static BlockPos getRandPos(Random random, int xz, int y, int yOffset, @Nullable Vec3d dirTowardVec, double maxTowardAngle) {
      if (dirTowardVec != null && !(maxTowardAngle >= Math.PI)) {
         double d3 = MathHelper.atan2(dirTowardVec.z, dirTowardVec.x) - (double)((float)Math.PI / 2F);
         double d4 = d3 + (double)(2.0F * random.nextFloat() - 1.0F) * maxTowardAngle;
         double d0 = Math.sqrt(random.nextDouble()) * (double)MathHelper.SQRT_2 * (double)xz;
         double d1 = -d0 * Math.sin(d4);
         double d2 = d0 * Math.cos(d4);
         if (!(Math.abs(d1) > (double)xz) && !(Math.abs(d2) > (double)xz)) {
            int l = random.nextInt(2 * y + 1) - y + yOffset;
            return new BlockPos(d1, (double)l, d2);
         } else {
            return null;
         }
      } else {
         int i = random.nextInt(2 * xz + 1) - xz;
         int j = random.nextInt(2 * y + 1) - y + yOffset;
         int k = random.nextInt(2 * xz + 1) - xz;
         return new BlockPos(i, j, k);
      }
   }

   static BlockPos getBlockAboveSold(BlockPos pos, int aboveSolidAmt, int worldHeight, Predicate<BlockPos> posPred) {
      if (aboveSolidAmt < 0) {
         throw new IllegalArgumentException("aboveSolidAmount was " + aboveSolidAmt + ", expected >= 0");
      } else if (!posPred.test(pos)) {
         return pos;
      } else {
         BlockPos blockpos;
         for(blockpos = pos.up(); blockpos.getY() < worldHeight && posPred.test(blockpos); blockpos = blockpos.up()) {
            ;
         }

         BlockPos blockpos1;
         BlockPos blockpos2;
         for(blockpos1 = blockpos; blockpos1.getY() < worldHeight && blockpos1.getY() - blockpos.getY() < aboveSolidAmt; blockpos1 = blockpos2) {
            blockpos2 = blockpos1.up();
            if (posPred.test(blockpos2)) {
               break;
            }
         }

         return blockpos1;
      }
   }
}
