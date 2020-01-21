package net.minecraft.entity.ai.brain;

import java.util.Comparator;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.EntityPosWrapper;
import net.minecraft.util.math.SectionPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.server.ServerWorld;

public class BrainUtil {
   public static void func_220618_a(LivingEntity p_220618_0_, LivingEntity p_220618_1_) {
      lookAtEachOther(p_220618_0_, p_220618_1_);
      approachEachOther(p_220618_0_, p_220618_1_);
   }

   public static boolean canSee(Brain<?> brain, LivingEntity entity) {
      return brain.getMemory(MemoryModuleType.VISIBLE_MOBS).filter((p_220614_1_) -> {
         return p_220614_1_.contains(entity);
      }).isPresent();
   }

   public static boolean isCorrectVisibleType(Brain<?> p_220623_0_, MemoryModuleType<? extends LivingEntity> p_220623_1_, EntityType<?> p_220623_2_) {
      return p_220623_0_.getMemory(p_220623_1_).filter((p_220622_1_) -> {
         return p_220622_1_.getType() == p_220623_2_;
      }).filter(LivingEntity::isAlive).filter((p_220615_1_) -> {
         return canSee(p_220623_0_, p_220615_1_);
      }).isPresent();
   }

   public static void lookAtEachOther(LivingEntity ent1, LivingEntity ent2) {
      lookAt(ent1, ent2);
      lookAt(ent2, ent1);
   }

   public static void lookAt(LivingEntity owner, LivingEntity lookTgt) {
      owner.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityPosWrapper(lookTgt));
   }

   public static void approachEachOther(LivingEntity ent1, LivingEntity ent2) {
      int i = 2;
      approach(ent1, ent2, 2);
      approach(ent2, ent1, 2);
   }

   public static void approach(LivingEntity living, LivingEntity target, int targetDistance) {
      float f = (float)living.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getValue();
      EntityPosWrapper entityposwrapper = new EntityPosWrapper(target);
      WalkTarget walktarget = new WalkTarget(entityposwrapper, f, targetDistance);
      living.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, entityposwrapper);
      living.getBrain().setMemory(MemoryModuleType.WALK_TARGET, walktarget);
   }

   public static void throwItemAt(LivingEntity from, ItemStack stack, LivingEntity to) {
      double d0 = from.getPosYPlusEyeHeight() - (double)0.3F;
      ItemEntity itementity = new ItemEntity(from.world, from.getPosX(), d0, from.getPosZ(), stack);
      BlockPos blockpos = new BlockPos(to);
      BlockPos blockpos1 = new BlockPos(from);
      float f = 0.3F;
      Vec3d vec3d = new Vec3d(blockpos.subtract(blockpos1));
      vec3d = vec3d.normalize().scale((double)0.3F);
      itementity.setMotion(vec3d);
      itementity.setDefaultPickupDelay();
      from.world.addEntity(itementity);
   }

   //AH REFACTOR
   public static SectionPos getSecPosLowerInRadius(ServerWorld world, SectionPos secPosIn, int secRadius) {
   //public static SectionPos func_220617_a(ServerWorld p_220617_0_, SectionPos p_220617_1_, int p_220617_2_) {
      int i = world.getPoiSecPosLevel(secPosIn);
      return SectionPos.getAllInBox(secPosIn, secRadius).filter((secPos) -> {
         return world.getPoiSecPosLevel(secPos) < i;
      }).min(Comparator.comparingInt(world::getPoiSecPosLevel)).orElse(secPosIn);
   }
}
