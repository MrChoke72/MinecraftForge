package net.minecraft.entity.ai.goal;

import javax.annotation.Nullable;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public abstract class TargetGoal extends Goal {
   protected final MobEntity goalOwner;
   protected final boolean shouldCheckSight;
   private final boolean nearbyOnly;
   private int targetSearchStatus;
   private int targetSearchDelay;
   private int targetUnseenTicks;
   protected LivingEntity target;
   protected int unseenMemoryTicks = 60;

   public TargetGoal(MobEntity mobIn, boolean checkSight) {
      this(mobIn, checkSight, false);
   }

   public TargetGoal(MobEntity mobIn, boolean checkSight, boolean nearbyOnlyIn) {
      this.goalOwner = mobIn;
      this.shouldCheckSight = checkSight;
      this.nearbyOnly = nearbyOnlyIn;
   }

   public boolean shouldContinueExecuting() {
      LivingEntity livingentity = this.goalOwner.getAttackTarget();
      if (livingentity == null) {
         livingentity = this.target;
      }

      if (livingentity == null) {
         return false;
      } else if (!livingentity.isAlive()) {
         return false;
      } else {
         Team team = this.goalOwner.getTeam();
         Team team1 = livingentity.getTeam();
         if (team != null && team1 == team) {
            return false;
         } else {
            double d0 = this.getTargetDistance();
            if (this.goalOwner.getDistanceSq(livingentity) > d0 * d0) {

               //AH CHANGE DEBUG OFF
               /*
               if(goalOwner.getCustomName() != null)
               {
                  System.out.println("TargetGoal:  shouldContinueExecuting, distance too far.  dist=" + this.goalOwner.getDistanceSq(livingentity));
               }
                */

               return false;
            } else {
               if (this.shouldCheckSight) {
                  if (this.goalOwner.getEntitySenses().canSee(livingentity)) {
                     this.targetUnseenTicks = 0;
                  } else if (++this.targetUnseenTicks > this.unseenMemoryTicks) {

                     //AH CHANGE DEBUG OFF
                     /*
                     if(this.goalOwner.getCustomName() != null)
                     {
                        System.out.println("TargetGoal, targetUnseenTicks=" + targetUnseenTicks + ", unseenMemoryTicks=" + unseenMemoryTicks);
                     }
                      */

                     return false;
                  }
               }

               if (livingentity instanceof PlayerEntity && ((PlayerEntity)livingentity).abilities.disableDamage) {
                  return false;
               } else {
                  this.goalOwner.setAttackTarget(livingentity);
                  return true;
               }
            }
         }
      }
   }

   protected double getTargetDistance() {
      IAttributeInstance iattributeinstance = this.goalOwner.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE);
      return iattributeinstance == null ? 16.0D : iattributeinstance.getValue();
   }

   public void startExecuting() {
      this.targetSearchStatus = 0;
      this.targetSearchDelay = 0;
      this.targetUnseenTicks = 0;
   }

   public void resetTask() {

      //AH CHANGE DEBUG OFF
      /*
      if(goalOwner.getCustomName() != null)
      {
            System.out.println("TargetGoal reset for " + goalOwner.getClass().getSimpleName() + ".  goal=" + getClass().getSimpleName());
      }
       */

      this.goalOwner.setAttackTarget((LivingEntity)null);
      this.target = null;
   }

   protected boolean isSuitableTarget(@Nullable LivingEntity potentialTarget, EntityPredicate targetPredicate) {
      if (potentialTarget == null) {
         return false;
      } else if (!targetPredicate.canTarget(this.goalOwner, potentialTarget)) {
         return false;
      } else if (!this.goalOwner.isWithinHomeDistanceFromPosition(new BlockPos(potentialTarget))) {
         return false;
      } else {
         if (this.nearbyOnly) {
            if (--this.targetSearchDelay <= 0) {
               this.targetSearchStatus = 0;
            }

            if (this.targetSearchStatus == 0) {
               this.targetSearchStatus = this.canEasilyReach(potentialTarget) ? 1 : 2;
            }

            if (this.targetSearchStatus == 2) {
               return false;
            }
         }

         return true;
      }
   }

   private boolean canEasilyReach(LivingEntity target) {
      this.targetSearchDelay = 10 + this.goalOwner.getRNG().nextInt(5);
      Path path = this.goalOwner.getNavigator().getPathToEntity(target, 0);
      if (path == null) {
         return false;
      } else {
         PathPoint pathpoint = path.getFinalPathPoint();
         if (pathpoint == null) {
            return false;
         } else {
            int i = pathpoint.x - MathHelper.floor(target.getPosX());
            int j = pathpoint.z - MathHelper.floor(target.getPosZ());
            return (double)(i * i + j * j) <= 2.25D;
         }
      }
   }

   public TargetGoal setUnseenMemoryTicks(int unseenMemoryTicksIn) {
      this.unseenMemoryTicks = unseenMemoryTicksIn;
      return this;
   }
}
