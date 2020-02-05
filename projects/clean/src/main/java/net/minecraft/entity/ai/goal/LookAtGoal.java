package net.minecraft.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.EntityPredicates;

public class LookAtGoal extends Goal {
   protected final MobEntity entity;
   protected Entity closestEntity;
   protected final float maxDistance;
   private int lookTime;
   protected final float chance;
   protected final Class<? extends LivingEntity> watchedClass;
   protected final EntityPredicate field_220716_e;

   public LookAtGoal(MobEntity entityIn, Class<? extends LivingEntity> watchTargetClass, float maxDistance) {
      this(entityIn, watchTargetClass, maxDistance, 0.02F);
   }

   public LookAtGoal(MobEntity entityIn, Class<? extends LivingEntity> watchTargetClass, float maxDistance, float chanceIn) {
      this.entity = entityIn;
      this.watchedClass = watchTargetClass;
      this.maxDistance = maxDistance;
      this.chance = chanceIn;
      this.setMutexFlags(EnumSet.of(Goal.Flag.LOOK));
      if (watchTargetClass == PlayerEntity.class) {
         this.field_220716_e = (new EntityPredicate()).setDistance((double)maxDistance).allowFriendlyFire().allowInvulnerable().setSkipAttackChecks().setCustomPredicate((p_220715_1_) -> {
            return EntityPredicates.notRiding(entityIn).test(p_220715_1_);
         });
      } else {
         this.field_220716_e = (new EntityPredicate()).setDistance((double)maxDistance).allowFriendlyFire().allowInvulnerable().setSkipAttackChecks();
      }

   }

   public boolean shouldExecute() {
      if (this.entity.getRNG().nextFloat() >= this.chance) {
         return false;
      } else {
         if (this.entity.getAttackTarget() != null) {
            this.closestEntity = this.entity.getAttackTarget();
         }

         if (this.watchedClass == PlayerEntity.class) {
            this.closestEntity = this.entity.world.getClosestPlayer(this.field_220716_e, this.entity, this.entity.getPosX(), this.entity.getPosYEye(), this.entity.getPosZ());
         } else {
            this.closestEntity = this.entity.world.getClosestEntity(this.watchedClass, this.field_220716_e, this.entity, this.entity.getPosX(), this.entity.getPosYEye(), this.entity.getPosZ(), this.entity.getBoundingBox().grow((double)this.maxDistance, 3.0D, (double)this.maxDistance));
         }

         return this.closestEntity != null;
      }
   }

   public boolean shouldContinueExecuting() {
      if (!this.closestEntity.isAlive()) {
         return false;
      } else if (this.entity.getDistanceSq(this.closestEntity) > (double)(this.maxDistance * this.maxDistance)) {
         return false;
      } else {
         return this.lookTime > 0;
      }
   }

   public void startExecuting() {
      this.lookTime = 40 + this.entity.getRNG().nextInt(40);
   }

   public void resetTask() {
      this.closestEntity = null;
   }

   public void tick() {

      //AH CHANGE DEBUG OFF
      /*
      if(this.entity instanceof HuskEntity && this.entity.getCustomName() != null) // && this.entity.getCustomName().getString().equals("Chuck"))
      {
         System.out.println("in LookAtGoal tick.  lookTime=" + lookTime);
      }
       */


      this.entity.getLookController().setLookPosition(this.closestEntity.getPosX(), this.closestEntity.getPosYEye(), this.closestEntity.getPosZ());
      --this.lookTime;
   }
}
