package net.minecraft.entity.ai.goal;

import java.util.EnumSet;
import java.util.function.Predicate;
import javax.annotation.Nullable;

import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;

public class NearestAttackableTargetGoal<T extends LivingEntity> extends TargetGoal {
   protected final Class<T> targetClass;
   protected final int targetChance;
   protected LivingEntity nearestTarget;
   protected EntityPredicate targetEntitySelector;

   public NearestAttackableTargetGoal(MobEntity entity, Class<T> targetClass, boolean checkSight) {
      this(entity, targetClass, checkSight, false);
   }

   public NearestAttackableTargetGoal(MobEntity entity, Class<T> targetClass, boolean checkSight, boolean nearbyOnly) {
      this(entity, targetClass, 10, checkSight, nearbyOnly, (Predicate<LivingEntity>)null);
   }

   //AH REFACTOR
   public NearestAttackableTargetGoal(MobEntity entity, Class<T> targetClass, int targetChance, boolean checkSight, boolean nearbyOnly, @Nullable Predicate<LivingEntity> entityPred) {
   //public NearestAttackableTargetGoal(MobEntity p_i50315_1_, Class<T> p_i50315_2_, int p_i50315_3_, boolean p_i50315_4_, boolean p_i50315_5_, @Nullable Predicate<LivingEntity> p_i50315_6_) {
      super(entity, checkSight, nearbyOnly);
      this.targetClass = targetClass;
      this.targetChance = targetChance;
      this.setMutexFlags(EnumSet.of(Goal.Flag.TARGET));
      this.targetEntitySelector = (new EntityPredicate()).setDistance(this.getTargetDistance()).setCustomPredicate(entityPred);
   }

   public boolean shouldExecute() {
      if (this.targetChance > 0 && this.goalOwner.getRNG().nextInt(this.targetChance) != 0) {
         return false;
      } else {
         this.findNearestTarget();

         //AH CHANGE DEBUG OFF
         /*
         if(goalOwner.getCustomName() != null)
         {
            if(this.nearestTarget != null)
            {
               System.out.println("In NearestAttackableTargetGoal, shouldExecute.  Entity=" + goalOwner.getClass().getSimpleName() + " will target: " + targetClass.getSimpleName());
            }
         }
          */

         return this.nearestTarget != null;
      }
   }

   protected AxisAlignedBB getTargetableArea(double targetDistance) {
      return this.goalOwner.getBoundingBox().grow(targetDistance, 4.0D, targetDistance);
   }

   protected void findNearestTarget() {
      if (this.targetClass != PlayerEntity.class && this.targetClass != ServerPlayerEntity.class) {
         this.nearestTarget = this.goalOwner.world.<T>getClosestEntity(this.targetClass, this.targetEntitySelector, this.goalOwner, this.goalOwner.getPosX(),
                 this.goalOwner.getPosYEye(), this.goalOwner.getPosZ(), this.getTargetableArea(this.getTargetDistance()));
      } else {
         this.nearestTarget = this.goalOwner.world.getClosestPlayer(this.targetEntitySelector, this.goalOwner, this.goalOwner.getPosX(),
                 this.goalOwner.getPosYEye(), this.goalOwner.getPosZ());
      }

   }

   public void startExecuting() {
      this.goalOwner.setAttackTarget(this.nearestTarget);
      super.startExecuting();
   }
}
