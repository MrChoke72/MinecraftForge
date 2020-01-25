package net.minecraft.entity.ai.goal;

import java.util.EnumSet;
import java.util.List;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;

public class DefendVillageTargetGoal extends TargetGoal {
   private final IronGolemEntity golem;
   private LivingEntity targetEntity;
   private final EntityPredicate field_223190_c = (new EntityPredicate()).setDistance(64.0D);

   public DefendVillageTargetGoal(IronGolemEntity ironGolemIn) {
      super(ironGolemIn, false, true);
      this.golem = ironGolemIn;
      this.setMutexFlags(EnumSet.of(Goal.Flag.TARGET));
   }

   public boolean shouldExecute() {
      AxisAlignedBB axisalignedbb = this.golem.getBoundingBox().grow(10.0D, 8.0D, 10.0D);
      List<LivingEntity> list = this.golem.world.getTargettableEntitiesWithinAABB(VillagerEntity.class, this.field_223190_c, this.golem, axisalignedbb);
      List<PlayerEntity> list1 = this.golem.world.getTargettablePlayersWithinAABB(this.field_223190_c, this.golem, axisalignedbb);

      for(LivingEntity livingentity : list) {
         VillagerEntity villagerentity = (VillagerEntity)livingentity;

         for(PlayerEntity playerentity : list1) {
            int i = villagerentity.getGossipForPlayer(playerentity);
            if (i <= -100) {
               this.targetEntity = playerentity;
            }
         }
      }

      if (this.targetEntity == null) {
         return false;
      } else {
         return !(this.targetEntity instanceof PlayerEntity) || !this.targetEntity.isSpectator() && !((PlayerEntity)this.targetEntity).isCreative();
      }
   }

   public void startExecuting() {
      this.golem.setAttackTarget(this.targetEntity);
      super.startExecuting();
   }
}
