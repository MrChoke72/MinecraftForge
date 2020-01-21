package net.minecraft.entity.ai.goal;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.util.math.AxisAlignedBB;

public class HurtByTargetGoal extends TargetGoal {
   private static final EntityPredicate ENTITY_PRED = (new EntityPredicate()).setLineOfSiteRequired().setUseInvisibilityCheck();
   private boolean entityCallsForHelp;
   private int revengeTimerOld;
   private final Class<?>[] excludedReinforcementTypes;
   private Class<?>[] reinforceClasses;

   public HurtByTargetGoal(CreatureEntity creatureIn, Class<?>... excludeTypes) {
      super(creatureIn, true);
      this.excludedReinforcementTypes = excludeTypes;
      this.setMutexFlags(EnumSet.of(Goal.Flag.TARGET));
   }

   public boolean shouldExecute() {
      int i = this.goalOwner.getRevengeTimer();
      LivingEntity livingentity = this.goalOwner.getRevengeTarget();
      if (i != this.revengeTimerOld && livingentity != null) {
         for(Class<?> oclass : this.excludedReinforcementTypes) {
            if (oclass.isAssignableFrom(livingentity.getClass())) {
               return false;
            }
         }

         return this.isSuitableTarget(livingentity, ENTITY_PRED);
      } else {
         return false;
      }
   }

   public HurtByTargetGoal setCallsForHelp(Class<?>... reinforceClasses) {
      this.entityCallsForHelp = true;
      this.reinforceClasses = reinforceClasses;
      return this;
   }

   public void startExecuting() {
      this.goalOwner.setAttackTarget(this.goalOwner.getRevengeTarget());
      this.target = this.goalOwner.getAttackTarget();
      this.revengeTimerOld = this.goalOwner.getRevengeTimer();
      this.unseenMemoryTicks = 300;
      if (this.entityCallsForHelp) {
         this.alertOthers();
      }

      super.startExecuting();

      //AH CHANGE DEBUG OFF
      /*
      if(this.goalOwner.getCustomName() != null && this.goalOwner.getCustomName().getString().equals("Chuck"))
      {
         System.out.println("In HurtByTargetGoal, startExecute.  entPos=" + goalOwner.getPosition());
      }
       */

   }

   protected void alertOthers() {
      double d0 = this.getTargetDistance();
      List<MobEntity> list = this.goalOwner.world.getEntitiesWithinAABBNoPred(this.goalOwner.getClass(), (new AxisAlignedBB(this.goalOwner.getPosX(), this.goalOwner.getPosY(), this.goalOwner.getPosZ(), this.goalOwner.getPosX() + 1.0D, this.goalOwner.getPosY() + 1.0D, this.goalOwner.getPosZ() + 1.0D)).grow(d0, 10.0D, d0));
      Iterator iterator = list.iterator();

      while(true) {
         MobEntity mobentity;
         while(true) {
            if (!iterator.hasNext()) {
               return;
            }

            mobentity = (MobEntity)iterator.next();
            if (this.goalOwner != mobentity && mobentity.getAttackTarget() == null && (!(this.goalOwner instanceof TameableEntity) || ((TameableEntity)this.goalOwner).getOwner() == ((TameableEntity)mobentity).getOwner()) && !mobentity.isOnSameTeam(this.goalOwner.getRevengeTarget())) {
               if (this.reinforceClasses == null) {
                  break;
               }

               boolean flag = false;

               for(Class<?> oclass : this.reinforceClasses) {
                  if (mobentity.getClass() == oclass) {
                     flag = true;
                     break;
                  }
               }

               if (!flag) {
                  break;
               }
            }
         }

         this.setAttackTarget(mobentity, this.goalOwner.getRevengeTarget());
      }
   }

   protected void setAttackTarget(MobEntity mobIn, LivingEntity targetIn) {
      mobIn.setAttackTarget(targetIn);
   }
}
