package net.minecraft.entity.monster;

import java.util.Random;
import java.util.function.Predicate;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ShootableItem;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

public abstract class MonsterEntity extends CreatureEntity implements IMob {

   protected MonsterEntity(EntityType<? extends MonsterEntity> type, World p_i48553_2_) {
      super(type, p_i48553_2_);
      this.experienceValue = 5;
   }

   public SoundCategory getSoundCategory() {
      return SoundCategory.HOSTILE;
   }

   public void livingTick() {
      this.updateArmSwingProgress();
      this.incrementIdleTime();
      super.livingTick();
   }

   protected void incrementIdleTime() {
      float f = this.getBrightness();
      if (f > 0.5F) {
         this.idleTime += 2;
      }

   }

   protected boolean func_225511_J_() {
      return true;
   }

   protected SoundEvent getSwimSound() {
      return SoundEvents.ENTITY_HOSTILE_SWIM;
   }

   protected SoundEvent getSplashSound() {
      return SoundEvents.ENTITY_HOSTILE_SPLASH;
   }

   public boolean attackEntityFrom(DamageSource source, float amount) {
      return this.isInvulnerableTo(source) ? false : super.attackEntityFrom(source, amount);
   }

   protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
      return SoundEvents.ENTITY_HOSTILE_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_HOSTILE_DEATH;
   }

   protected SoundEvent getFallSound(int heightIn) {
      return heightIn > 4 ? SoundEvents.ENTITY_HOSTILE_BIG_FALL : SoundEvents.ENTITY_HOSTILE_SMALL_FALL;
   }

   public float getBlockPathWeight(BlockPos pos, IWorldReader worldIn) {
      return 0.5F - worldIn.getBrightness(pos);
   }

   public static boolean isLightSpawnable(IWorld world, BlockPos pos, Random rand) {
      if (world.getLightLevel(LightType.SKY, pos) > rand.nextInt(32)) {
         return false;
      } else {
         int i = world.getWorld().isThundering() ? world.getNeighborAwareLightSubtracted(pos, 10) : world.getLight(pos);
         return i <= rand.nextInt(8);
      }
   }

   public static boolean spawnPred(EntityType<? extends MonsterEntity> entityType, IWorld world, SpawnReason spawnReason, BlockPos pos, Random rand) {
      return world.getDifficulty() != Difficulty.PEACEFUL && isLightSpawnable(world, pos, rand) && canEntitySpawn(entityType, world, spawnReason, pos, rand);
   }

   public static boolean func_223324_d(EntityType<? extends MonsterEntity> p_223324_0_, IWorld p_223324_1_, SpawnReason p_223324_2_, BlockPos p_223324_3_, Random p_223324_4_) {
      return p_223324_1_.getDifficulty() != Difficulty.PEACEFUL && canEntitySpawn(p_223324_0_, p_223324_1_, p_223324_2_, p_223324_3_, p_223324_4_);
   }

   //AH CHANGE
   protected void registerAttributes() {
      super.registerAttributes();

      //AH CHANGE - ADD, default is 16
      this.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(32.0D);

      this.getAttributes().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
   }
   /*
   protected void registerAttributes() {
      super.registerAttributes();
      this.getAttributes().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
   }
    */

   protected boolean canDropLoot() {
      return true;
   }

   public boolean isPreventingPlayerRest(PlayerEntity playerIn) {
      return true;
   }

   public ItemStack findAmmo(ItemStack shootable) {
      if (shootable.getItem() instanceof ShootableItem) {
         Predicate<ItemStack> predicate = ((ShootableItem)shootable.getItem()).getAmmoPredicate();
         ItemStack itemstack = ShootableItem.getHeldAmmo(this, predicate);
         return itemstack.isEmpty() ? new ItemStack(Items.ARROW) : itemstack;
      } else {
         return ItemStack.EMPTY;
      }
   }

}
