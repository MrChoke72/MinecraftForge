package net.minecraft.world;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;

public interface IEntityReader {
   List<Entity> getEntitiesInAABBexcluding(@Nullable Entity entityIn, AxisAlignedBB boundingBox, @Nullable Predicate<? super Entity> predicate);

   <T extends Entity> List<T> getEntitiesWithinAABB(Class<? extends T> clazz, AxisAlignedBB aabb, @Nullable Predicate<? super T> filter);

   default <T extends Entity> List<T> getEntitiesWithinAABBDef(Class<? extends T> entityClass, AxisAlignedBB boundingBox, @Nullable Predicate<? super T> entityPred) {
      return this.getEntitiesWithinAABB(entityClass, boundingBox, entityPred);
   }

   List<? extends PlayerEntity> getPlayers();

   default List<Entity> getEntitiesWithinAABBExcludingEntity(@Nullable Entity entityIn, AxisAlignedBB bb) {
      return this.getEntitiesInAABBexcluding(entityIn, bb, EntityPredicates.NOT_SPECTATING);
   }

   default boolean checkNoEntityCollision(@Nullable Entity entityIn, VoxelShape shape) {
      return shape.isEmpty() ? true : this.getEntitiesWithinAABBExcludingEntity(entityIn, shape.getBoundingBox()).stream().filter((p_217364_1_) -> {
         return !p_217364_1_.removed && p_217364_1_.preventEntitySpawning && (entityIn == null || !p_217364_1_.isRidingSameEntity(entityIn));
      }).noneMatch((p_217356_1_) -> {
         return VoxelShapes.compare(shape, VoxelShapes.create(p_217356_1_.getBoundingBox()), IBooleanFunction.AND);
      });
   }

   default <T extends Entity> List<T> getEntitiesWithinAABB(Class<? extends T> entityClass, AxisAlignedBB boundingBox) {
      return this.getEntitiesWithinAABB(entityClass, boundingBox, EntityPredicates.NOT_SPECTATING);
   }

   default <T extends Entity> List<T> getEntitiesWithinAABBNoPred(Class<? extends T> entityClass, AxisAlignedBB boundingBox) {
      return this.getEntitiesWithinAABBDef(entityClass, boundingBox, EntityPredicates.NOT_SPECTATING);
   }

   default Stream<VoxelShape> getEmptyCollisionShapes(@Nullable Entity entityIn, AxisAlignedBB aabb, Set<Entity> entitiesToIgnore) {
      if (aabb.getAverageEdgeLength() < 1.0E-7D) {
         return Stream.empty();
      } else {
         AxisAlignedBB axisalignedbb = aabb.grow(1.0E-7D);
         return this.getEntitiesWithinAABBExcludingEntity(entityIn, axisalignedbb).stream().filter((p_217367_1_) -> {
            return !entitiesToIgnore.contains(p_217367_1_);
         }).filter((p_223442_1_) -> {
            return entityIn == null || !entityIn.isRidingSameEntity(p_223442_1_);
         }).flatMap((p_217368_1_) -> {
            return Stream.of(p_217368_1_.getCollisionBoundingBox(), entityIn == null ? null : entityIn.getCollisionBox(p_217368_1_));
         }).filter(Objects::nonNull).filter(axisalignedbb::intersects).map(VoxelShapes::create);
      }
   }

   @Nullable
   default PlayerEntity getClosestPlayer(double x, double y, double z, double distance, @Nullable Predicate<Entity> predicate) {
      double d0 = -1.0D;
      PlayerEntity playerentity = null;

      for(PlayerEntity playerentity1 : this.getPlayers()) {
         if (predicate == null || predicate.test(playerentity1)) {
            double d1 = playerentity1.getDistanceSq(x, y, z);
            if ((distance < 0.0D || d1 < distance * distance) && (d0 == -1.0D || d1 < d0)) {
               d0 = d1;
               playerentity = playerentity1;
            }
         }
      }

      return playerentity;
   }

   @Nullable
   default PlayerEntity getClosestPlayer(Entity p_217362_1_, double distance) {
      return this.getClosestPlayer(p_217362_1_.getPosX(), p_217362_1_.getPosY(), p_217362_1_.getPosZ(), distance, false);
   }

   @Nullable
   default PlayerEntity getClosestPlayer(double x, double y, double z, double distance, boolean creativePlayers) {
      Predicate<Entity> predicate = creativePlayers ? EntityPredicates.CAN_AI_TARGET : EntityPredicates.NOT_SPECTATING;
      return this.getClosestPlayer(x, y, z, distance, predicate);
   }

   @Nullable
   default PlayerEntity getClosestPlayer(double p_217365_1_, double p_217365_3_, double p_217365_5_) {
      double d0 = -1.0D;
      PlayerEntity playerentity = null;

      for(PlayerEntity playerentity1 : this.getPlayers()) {
         if (EntityPredicates.NOT_SPECTATING.test(playerentity1)) {
            double d1 = playerentity1.getDistanceSq(p_217365_1_, playerentity1.getPosY(), p_217365_3_);
            if ((p_217365_5_ < 0.0D || d1 < p_217365_5_ * p_217365_5_) && (d0 == -1.0D || d1 < d0)) {
               d0 = d1;
               playerentity = playerentity1;
            }
         }
      }

      return playerentity;
   }

   default boolean isPlayerWithin(double x, double y, double z, double distance) {
      for(PlayerEntity playerentity : this.getPlayers()) {
         if (EntityPredicates.NOT_SPECTATING.test(playerentity) && EntityPredicates.IS_LIVING_ALIVE.test(playerentity)) {
            double d0 = playerentity.getDistanceSq(x, y, z);
            if (distance < 0.0D || d0 < distance * distance) {
               return true;
            }
         }
      }

      return false;
   }

   @Nullable
   default PlayerEntity getClosestPlayer(EntityPredicate p_217370_1_, LivingEntity p_217370_2_) {
      return this.getClosestEntity(this.getPlayers(), p_217370_1_, p_217370_2_, p_217370_2_.getPosX(), p_217370_2_.getPosY(), p_217370_2_.getPosZ());
   }

   @Nullable
   default PlayerEntity getClosestPlayer(EntityPredicate entityPred, LivingEntity entityIn, double x, double y, double z) {
      return this.getClosestEntity(this.getPlayers(), entityPred, entityIn, x, y, z);
   }

   @Nullable
   default PlayerEntity getClosestPlayer(EntityPredicate p_217359_1_, double p_217359_2_, double p_217359_4_, double p_217359_6_) {
      return this.getClosestEntity(this.getPlayers(), p_217359_1_, (LivingEntity)null, p_217359_2_, p_217359_4_, p_217359_6_);
   }

   @Nullable
   default <T extends LivingEntity> T getClosestEntityWithinAABB(Class<? extends T> p_217360_1_, EntityPredicate p_217360_2_, @Nullable LivingEntity p_217360_3_, double p_217360_4_, double p_217360_6_, double p_217360_8_, AxisAlignedBB p_217360_10_) {
      return this.getClosestEntity(this.getEntitiesWithinAABB(p_217360_1_, p_217360_10_, (Predicate<T>)null), p_217360_2_, p_217360_3_, p_217360_4_, p_217360_6_, p_217360_8_);
   }

   @Nullable
   default <T extends LivingEntity> T getClosestEntity(Class<? extends T> targetClass, EntityPredicate tgtEntitySelector, @Nullable LivingEntity owner, double x, double y, double z, AxisAlignedBB targetArea) {
      return this.getClosestEntity(this.getEntitiesWithinAABBDef(targetClass, targetArea, (Predicate<T>)null), tgtEntitySelector, owner, x, y, z);
   }

   @Nullable
   default <T extends LivingEntity> T getClosestEntity(List<? extends T> tgtEntityList, EntityPredicate tgtEntitySelector, @Nullable LivingEntity owner, double x, double y, double z) {
      double d0 = -1.0D;
      T t = null;

      for(T t1 : tgtEntityList) {
         if (tgtEntitySelector.canTarget(owner, t1)) {
            double d1 = t1.getDistanceSq(x, y, z);
            if (d0 == -1.0D || d1 < d0) {
               d0 = d1;
               t = t1;
            }
         }
      }

      return t;
   }

   default List<PlayerEntity> getTargettablePlayersWithinAABB(EntityPredicate p_217373_1_, LivingEntity p_217373_2_, AxisAlignedBB p_217373_3_) {
      List<PlayerEntity> list = Lists.newArrayList();

      for(PlayerEntity playerentity : this.getPlayers()) {
         if (p_217373_3_.contains(playerentity.getPosX(), playerentity.getPosY(), playerentity.getPosZ()) && p_217373_1_.canTarget(p_217373_2_, playerentity)) {
            list.add(playerentity);
         }
      }

      return list;
   }

   default <T extends LivingEntity> List<T> getTargettableEntitiesWithinAABB(Class<? extends T> p_217374_1_, EntityPredicate p_217374_2_, LivingEntity p_217374_3_, AxisAlignedBB p_217374_4_) {
      List<T> list = this.getEntitiesWithinAABB(p_217374_1_, p_217374_4_, (Predicate<T>)null);
      List<T> list1 = Lists.newArrayList();

      for(T t : list) {
         if (p_217374_2_.canTarget(p_217374_3_, t)) {
            list1.add(t);
         }
      }

      return list1;
   }

   @Nullable
   default PlayerEntity getPlayerByUuid(UUID uniqueIdIn) {
      for(int i = 0; i < this.getPlayers().size(); ++i) {
         PlayerEntity playerentity = this.getPlayers().get(i);
         if (uniqueIdIn.equals(playerentity.getUniqueID())) {
            return playerentity;
         }
      }

      return null;
   }
}
