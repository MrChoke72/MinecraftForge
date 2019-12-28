package net.minecraft.entity.merchant.villager;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.schedule.Activity;
import net.minecraft.entity.ai.brain.schedule.Schedule;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.ai.brain.task.VillagerTasks;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.merchant.IReputationTracking;
import net.minecraft.entity.merchant.IReputationType;
import net.minecraft.entity.monster.WitchEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.villager.IVillagerDataHolder;
import net.minecraft.entity.villager.IVillagerType;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MerchantOffer;
import net.minecraft.item.MerchantOffers;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.network.DebugPacketSender;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stats.Stats;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.LongSerializable;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.village.GossipManager;
import net.minecraft.village.GossipType;
import net.minecraft.village.PointOfInterestManager;
import net.minecraft.village.PointOfInterestType;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.raid.Raid;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class VillagerEntity extends AbstractVillagerEntity implements IReputationTracking, IVillagerDataHolder {
   private static final DataParameter<VillagerData> VILLAGER_DATA = EntityDataManager.createKey(VillagerEntity.class, DataSerializers.VILLAGER_DATA);
   public static final Map<Item, Integer> field_213788_bA = ImmutableMap.of(Items.BREAD, 4, Items.POTATO, 1, Items.CARROT, 1, Items.BEETROOT, 1);
   private static final Set<Item> field_213776_bD = ImmutableSet.of(Items.BREAD, Items.POTATO, Items.CARROT, Items.WHEAT, Items.WHEAT_SEEDS, Items.BEETROOT, Items.BEETROOT_SEEDS);
   private int timeUntilReset;
   private boolean customer;
   @Nullable
   private PlayerEntity field_213778_bG;
   private byte foodLevel;
   private final GossipManager gossip = new GossipManager();
   private long field_213783_bN;
   private long lastGossipDecay;
   private int xp;
   private long lastRestock;
   private int field_223725_bO;
   private long field_223726_bP;
   private static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(MemoryModuleType.HOME, MemoryModuleType.JOB_SITE, MemoryModuleType.MEETING_POINT, MemoryModuleType.MOBS, MemoryModuleType.VISIBLE_MOBS, MemoryModuleType.VISIBLE_VILLAGER_BABIES, MemoryModuleType.NEAREST_PLAYERS, MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryModuleType.WALK_TARGET, MemoryModuleType.LOOK_TARGET, MemoryModuleType.INTERACTION_TARGET, MemoryModuleType.BREED_TARGET, MemoryModuleType.PATH, MemoryModuleType.INTERACTABLE_DOORS, MemoryModuleType.OPENED_DOORS, MemoryModuleType.NEAREST_BED, MemoryModuleType.HURT_BY, MemoryModuleType.HURT_BY_ENTITY, MemoryModuleType.NEAREST_HOSTILE, MemoryModuleType.SECONDARY_JOB_SITE, MemoryModuleType.HIDING_PLACE, MemoryModuleType.HEARD_BELL_TIME, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.LAST_SLEPT, MemoryModuleType.field_226332_A_, MemoryModuleType.LAST_WORKED_AT_POI, MemoryModuleType.GOLEM_LAST_SEEN_TIME);
   private static final ImmutableList<SensorType<? extends Sensor<? super VillagerEntity>>> SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS, SensorType.INTERACTABLE_DOORS, SensorType.NEAREST_BED, SensorType.HURT_BY, SensorType.VILLAGER_HOSTILES, SensorType.VILLAGER_BABIES, SensorType.SECONDARY_POIS, SensorType.GOLEM_LAST_SEEN);
   public static final Map<MemoryModuleType<GlobalPos>, BiPredicate<VillagerEntity, PointOfInterestType>> field_213774_bB = ImmutableMap.of(MemoryModuleType.HOME, (p_213769_0_, p_213769_1_) -> {
      return p_213769_1_ == PointOfInterestType.HOME;
   }, MemoryModuleType.JOB_SITE, (p_213771_0_, p_213771_1_) -> {
      return p_213771_0_.getVillagerData().getProfession().getPointOfInterest() == p_213771_1_;
   }, MemoryModuleType.MEETING_POINT, (p_213772_0_, p_213772_1_) -> {
      return p_213772_1_ == PointOfInterestType.MEETING;
   });

   public VillagerEntity(EntityType<? extends VillagerEntity> p_i50182_1_, World p_i50182_2_) {
      this(p_i50182_1_, p_i50182_2_, IVillagerType.PLAINS);
   }

   public VillagerEntity(EntityType<? extends VillagerEntity> p_i50183_1_, World p_i50183_2_, IVillagerType p_i50183_3_) {
      super(p_i50183_1_, p_i50183_2_);
      ((GroundPathNavigator)this.getNavigator()).setBreakDoors(true);
      this.getNavigator().setCanSwim(true);
      this.setCanPickUpLoot(true);
      this.setVillagerData(this.getVillagerData().withType(p_i50183_3_).withProfession(VillagerProfession.NONE));
      this.brain = this.createBrain(new Dynamic<>(NBTDynamicOps.INSTANCE, new CompoundNBT()));
   }

   public Brain<VillagerEntity> getBrain() {
      return (Brain<VillagerEntity>) super.getBrain();
   }

   protected Brain<?> createBrain(Dynamic<?> p_213364_1_) {
      Brain<VillagerEntity> brain = new Brain<>(MEMORY_TYPES, SENSOR_TYPES, p_213364_1_);
      this.initBrain(brain);
      return brain;
   }

   public void resetBrain(ServerWorld p_213770_1_) {
      Brain<VillagerEntity> brain = this.getBrain();
      brain.stopAllTasks(p_213770_1_, this);
      this.brain = brain.copy();
      this.initBrain(this.getBrain());
   }

   private void initBrain(Brain<VillagerEntity> p_213744_1_) {
      VillagerProfession villagerprofession = this.getVillagerData().getProfession();
      float f = (float)this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getValue();
      if (this.isChild()) {
         p_213744_1_.setSchedule(Schedule.VILLAGER_BABY);
         p_213744_1_.registerActivity(Activity.PLAY, VillagerTasks.play(f));
      } else {
         p_213744_1_.setSchedule(Schedule.VILLAGER_DEFAULT);
         p_213744_1_.registerActivity(Activity.WORK, VillagerTasks.work(villagerprofession, f), ImmutableSet.of(Pair.of(MemoryModuleType.JOB_SITE, MemoryModuleStatus.VALUE_PRESENT)));
      }

      p_213744_1_.registerActivity(Activity.CORE, VillagerTasks.core(villagerprofession, f));
      p_213744_1_.registerActivity(Activity.MEET, VillagerTasks.meet(villagerprofession, f), ImmutableSet.of(Pair.of(MemoryModuleType.MEETING_POINT, MemoryModuleStatus.VALUE_PRESENT)));
      p_213744_1_.registerActivity(Activity.REST, VillagerTasks.rest(villagerprofession, f));
      p_213744_1_.registerActivity(Activity.IDLE, VillagerTasks.idle(villagerprofession, f));
      p_213744_1_.registerActivity(Activity.PANIC, VillagerTasks.panic(villagerprofession, f));
      p_213744_1_.registerActivity(Activity.PRE_RAID, VillagerTasks.preRaid(villagerprofession, f));
      p_213744_1_.registerActivity(Activity.RAID, VillagerTasks.raid(villagerprofession, f));
      p_213744_1_.registerActivity(Activity.HIDE, VillagerTasks.hide(villagerprofession, f));
      p_213744_1_.setDefaultActivities(ImmutableSet.of(Activity.CORE));
      p_213744_1_.setFallbackActivity(Activity.IDLE);
      p_213744_1_.switchTo(Activity.IDLE);
      p_213744_1_.updateActivity(this.world.getDayTime(), this.world.getGameTime());
   }

   protected void onGrowingAdult() {
      super.onGrowingAdult();
      if (this.world instanceof ServerWorld) {
         this.resetBrain((ServerWorld)this.world);
      }

   }

   protected void registerAttributes() {
      super.registerAttributes();
      this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.5D);

      //AH CHANGE CANCEL
      //this.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(64.0D);
      this.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(48.0D);
   }


   protected void updateAITasks() {
      this.world.getProfiler().startSection("brain");
      this.getBrain().tick((ServerWorld)this.world, this);
      this.world.getProfiler().endSection();
      if (!this.func_213716_dX() && this.timeUntilReset > 0) {
         --this.timeUntilReset;
         if (this.timeUntilReset <= 0) {
            if (this.customer) {
               this.populateBuyingList();
               this.customer = false;
            }

            this.addPotionEffect(new EffectInstance(Effects.REGENERATION, 200, 0));
         }
      }

      if (this.field_213778_bG != null && this.world instanceof ServerWorld) {
         ((ServerWorld)this.world).func_217489_a(IReputationType.TRADE, this.field_213778_bG, this);
         this.world.setEntityState(this, (byte)14);
         this.field_213778_bG = null;
      }

      if (!this.isAIDisabled() && this.rand.nextInt(100) == 0) {
         Raid raid = ((ServerWorld)this.world).findRaid(new BlockPos(this));
         if (raid != null && raid.isActive() && !raid.func_221319_a()) {
            this.world.setEntityState(this, (byte)42);
         }
      }

      if (this.getVillagerData().getProfession() == VillagerProfession.NONE && this.func_213716_dX()) {
         this.func_213750_eg();
      }

      super.updateAITasks();
   }

   public void tick() {
      super.tick();
      if (this.getShakeHeadTicks() > 0) {
         this.setShakeHeadTicks(this.getShakeHeadTicks() - 1);
      }

      this.func_223343_eC();
   }

   public boolean processInteract(PlayerEntity player, Hand hand) {
      ItemStack itemstack = player.getHeldItem(hand);
      boolean flag = itemstack.getItem() == Items.NAME_TAG;
      if (flag) {
         itemstack.interactWithEntity(player, this, hand);
         return true;
      } else if (itemstack.getItem() != Items.VILLAGER_SPAWN_EGG && this.isAlive() && !this.func_213716_dX() && !this.isSleeping()) {
         if (this.isChild()) {
            this.shakeHead();
            return super.processInteract(player, hand);
         } else {
            boolean flag1 = this.getOffers().isEmpty();
            if (hand == Hand.MAIN_HAND) {
               if (flag1 && !this.world.isRemote) {
                  this.shakeHead();
               }

               player.addStat(Stats.TALKED_TO_VILLAGER);
            }

            if (flag1) {
               return super.processInteract(player, hand);
            } else {
               if (!this.world.isRemote && !this.offers.isEmpty()) {
                  this.func_213740_f(player);
               }

               return true;
            }
         }
      } else {
         return super.processInteract(player, hand);
      }
   }

   private void shakeHead() {
      this.setShakeHeadTicks(40);
      if (!this.world.isRemote()) {
         this.playSound(SoundEvents.ENTITY_VILLAGER_NO, this.getSoundVolume(), this.getSoundPitch());
      }

   }

   private void func_213740_f(PlayerEntity p_213740_1_) {
      this.func_213762_g(p_213740_1_);
      this.setCustomer(p_213740_1_);
      this.func_213707_a(p_213740_1_, this.getDisplayName(), this.getVillagerData().getLevel());
   }

   public void setCustomer(@Nullable PlayerEntity player) {
      boolean flag = this.getCustomer() != null && player == null;
      super.setCustomer(player);
      if (flag) {
         this.func_213750_eg();
      }

   }

   protected void func_213750_eg() {
      super.func_213750_eg();
      this.func_213748_et();
   }

   private void func_213748_et() {
      for(MerchantOffer merchantoffer : this.getOffers()) {
         merchantoffer.func_222220_k();
      }

   }

   public boolean func_223340_ej() {
      return true;
   }

   public void func_213766_ei() {
      this.func_223715_ey();

      for(MerchantOffer merchantoffer : this.getOffers()) {
         merchantoffer.func_222203_h();
      }

      if (this.getVillagerData().getProfession() == VillagerProfession.FARMER) {
         this.func_223359_eB();
      }

      this.lastRestock = this.world.getGameTime();
      ++this.field_223725_bO;
   }

   private boolean func_223723_ev() {
      for(MerchantOffer merchantoffer : this.getOffers()) {
         if (merchantoffer.func_226654_r_()) {
            return true;
         }
      }

      return false;
   }

   private boolean func_223720_ew() {
      return this.field_223725_bO == 0 || this.field_223725_bO < 2 && this.world.getGameTime() > this.lastRestock + 2400L;
   }

   public boolean func_223721_ek() {
      long i = this.lastRestock + 12000L;
      long j = this.world.getGameTime();
      boolean flag = j > i;
      long k = this.world.getDayTime();
      if (this.field_223726_bP > 0L) {
         long l = this.field_223726_bP / 24000L;
         long i1 = k / 24000L;
         flag |= i1 > l;
      }

      this.field_223726_bP = k;
      if (flag) {
         this.lastRestock = j;
         this.func_223718_eH();
      }

      return this.func_223720_ew() && this.func_223723_ev();
   }

   private void func_223719_ex() {
      int i = 2 - this.field_223725_bO;
      if (i > 0) {
         for(MerchantOffer merchantoffer : this.getOffers()) {
            merchantoffer.func_222203_h();
         }
      }

      for(int j = 0; j < i; ++j) {
         this.func_223715_ey();
      }

   }

   private void func_223715_ey() {
      for(MerchantOffer merchantoffer : this.getOffers()) {
         merchantoffer.func_222222_e();
      }

   }

   private void func_213762_g(PlayerEntity p_213762_1_) {
      int i = this.func_223107_f(p_213762_1_);
      if (i != 0) {
         for(MerchantOffer merchantoffer : this.getOffers()) {
            merchantoffer.func_222207_a(-MathHelper.floor((float)i * merchantoffer.func_222211_m()));
         }
      }

      if (p_213762_1_.isPotionActive(Effects.HERO_OF_THE_VILLAGE)) {
         EffectInstance effectinstance = p_213762_1_.getActivePotionEffect(Effects.HERO_OF_THE_VILLAGE);
         int k = effectinstance.getAmplifier();

         for(MerchantOffer merchantoffer1 : this.getOffers()) {
            double d0 = 0.3D + 0.0625D * (double)k;
            int j = (int)Math.floor(d0 * (double)merchantoffer1.func_222218_a().getCount());
            merchantoffer1.func_222207_a(-Math.max(j, 1));
         }
      }

   }

   protected void registerData() {
      super.registerData();
      this.dataManager.register(VILLAGER_DATA, new VillagerData(IVillagerType.PLAINS, VillagerProfession.NONE, 1));
   }

   public void writeAdditional(CompoundNBT compound) {
      super.writeAdditional(compound);
      compound.put("VillagerData", this.getVillagerData().serialize(NBTDynamicOps.INSTANCE));
      compound.putByte("FoodLevel", this.foodLevel);
      compound.put("Gossips", this.gossip.func_220914_a(NBTDynamicOps.INSTANCE).getValue());
      compound.putInt("Xp", this.xp);
      compound.putLong("LastRestock", this.lastRestock);
      compound.putLong("LastGossipDecay", this.lastGossipDecay);
      compound.putInt("RestocksToday", this.field_223725_bO);
   }

   public void readAdditional(CompoundNBT compound) {
      super.readAdditional(compound);
      if (compound.contains("VillagerData", 10)) {
         this.setVillagerData(new VillagerData(new Dynamic<>(NBTDynamicOps.INSTANCE, compound.get("VillagerData"))));
      }

      if (compound.contains("Offers", 10)) {
         this.offers = new MerchantOffers(compound.getCompound("Offers"));
      }

      if (compound.contains("FoodLevel", 1)) {
         this.foodLevel = compound.getByte("FoodLevel");
      }

      ListNBT listnbt = compound.getList("Gossips", 10);
      this.gossip.func_220918_a(new Dynamic<>(NBTDynamicOps.INSTANCE, listnbt));
      if (compound.contains("Xp", 3)) {
         this.xp = compound.getInt("Xp");
      }

      this.lastRestock = compound.getLong("LastRestock");
      this.lastGossipDecay = compound.getLong("LastGossipDecay");
      this.setCanPickUpLoot(true);
      if (this.world instanceof ServerWorld) {
         this.resetBrain((ServerWorld)this.world);
      }

      this.field_223725_bO = compound.getInt("RestocksToday");
   }

   public boolean canDespawn(double distanceToClosestPlayer) {
      return false;
   }

   @Nullable
   protected SoundEvent getAmbientSound() {
      if (this.isSleeping()) {
         return null;
      } else {
         return this.func_213716_dX() ? SoundEvents.ENTITY_VILLAGER_TRADE : SoundEvents.ENTITY_VILLAGER_AMBIENT;
      }
   }

   protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
      return SoundEvents.ENTITY_VILLAGER_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_VILLAGER_DEATH;
   }

   public void playWorkstationSound() {
      SoundEvent soundevent = this.getVillagerData().getProfession().func_226558_e_();
      if (soundevent != null) {
         this.playSound(soundevent, this.getSoundVolume(), this.getSoundPitch());
      }

   }

   public void setVillagerData(VillagerData p_213753_1_) {
      VillagerData villagerdata = this.getVillagerData();
      if (villagerdata.getProfession() != p_213753_1_.getProfession()) {
         this.offers = null;
      }

      this.dataManager.set(VILLAGER_DATA, p_213753_1_);
   }

   public VillagerData getVillagerData() {
      return this.dataManager.get(VILLAGER_DATA);
   }

   protected void func_213713_b(MerchantOffer p_213713_1_) {
      int i = 3 + this.rand.nextInt(4);
      this.xp += p_213713_1_.func_222210_n();
      this.field_213778_bG = this.getCustomer();
      if (this.func_213741_eu()) {
         this.timeUntilReset = 40;
         this.customer = true;
         i += 5;
      }

      if (p_213713_1_.func_222221_q()) {
         this.world.addEntity(new ExperienceOrbEntity(this.world, this.getPosX(), this.getPosY() + 0.5D, this.getPosZ(), i));
      }

   }

   public void setRevengeTarget(@Nullable LivingEntity livingBase) {
      if (livingBase != null && this.world instanceof ServerWorld) {
         ((ServerWorld)this.world).func_217489_a(IReputationType.VILLAGER_HURT, livingBase, this);
         if (this.isAlive() && livingBase instanceof PlayerEntity) {
            this.world.setEntityState(this, (byte)13);
         }
      }

      super.setRevengeTarget(livingBase);
   }

   public void onDeath(DamageSource cause) {
      LOGGER.info("Villager {} died, message: '{}'", this, cause.getDeathMessage(this).getString());
      Entity entity = cause.getTrueSource();
      if (entity != null) {
         this.func_223361_a(entity);
      }

      this.func_213742_a(MemoryModuleType.HOME);
      this.func_213742_a(MemoryModuleType.JOB_SITE);
      this.func_213742_a(MemoryModuleType.MEETING_POINT);
      super.onDeath(cause);
   }

   private void func_223361_a(Entity p_223361_1_) {
      if (this.world instanceof ServerWorld) {
         Optional<List<LivingEntity>> optional = this.brain.getMemory(MemoryModuleType.VISIBLE_MOBS);
         if (optional.isPresent()) {
            ServerWorld serverworld = (ServerWorld)this.world;
            optional.get().stream().filter((p_223349_0_) -> {
               return p_223349_0_ instanceof IReputationTracking;
            }).forEach((p_223342_2_) -> {
               serverworld.func_217489_a(IReputationType.VILLAGER_KILLED, p_223361_1_, (IReputationTracking)p_223342_2_);
            });
         }
      }
   }

   public void func_213742_a(MemoryModuleType<GlobalPos> p_213742_1_) {
      if (this.world instanceof ServerWorld) {
         MinecraftServer minecraftserver = ((ServerWorld)this.world).getServer();
         this.brain.getMemory(p_213742_1_).ifPresent((p_213752_3_) -> {
            ServerWorld serverworld = minecraftserver.getWorld(p_213752_3_.getDimension());
            PointOfInterestManager pointofinterestmanager = serverworld.func_217443_B();
            Optional<PointOfInterestType> optional = pointofinterestmanager.func_219148_c(p_213752_3_.getPos());
            BiPredicate<VillagerEntity, PointOfInterestType> bipredicate = field_213774_bB.get(p_213742_1_);
            if (optional.isPresent() && bipredicate.test(this, optional.get())) {
               pointofinterestmanager.func_219142_b(p_213752_3_.getPos());
               DebugPacketSender.func_218801_c(serverworld, p_213752_3_.getPos());
            }

         });
      }
   }

   public boolean canBreed() {
      return this.foodLevel + this.func_213751_ew() >= 12 && this.getGrowingAge() == 0;
   }

   private boolean func_223344_ex() {
      return this.foodLevel < 12;
   }

   private void func_213765_en() {
      if (this.func_223344_ex() && this.func_213751_ew() != 0) {
         for(int i = 0; i < this.func_213715_ed().getSizeInventory(); ++i) {
            ItemStack itemstack = this.func_213715_ed().getStackInSlot(i);
            if (!itemstack.isEmpty()) {
               Integer integer = field_213788_bA.get(itemstack.getItem());
               if (integer != null) {
                  int j = itemstack.getCount();

                  for(int k = j; k > 0; --k) {
                     this.foodLevel = (byte)(this.foodLevel + integer);
                     this.func_213715_ed().decrStackSize(i, 1);
                     if (!this.func_223344_ex()) {
                        return;
                     }
                  }
               }
            }
         }

      }
   }

   public int func_223107_f(PlayerEntity p_223107_1_) {
      return this.gossip.func_220921_a(p_223107_1_.getUniqueID(), (p_223103_0_) -> {
         return true;
      });
   }

   private void func_213758_s(int p_213758_1_) {
      this.foodLevel = (byte)(this.foodLevel - p_213758_1_);
   }

   public void func_223346_ep() {
      this.func_213765_en();
      this.func_213758_s(12);
   }

   public void func_213768_b(MerchantOffers p_213768_1_) {
      this.offers = p_213768_1_;
   }

   private boolean func_213741_eu() {
      int i = this.getVillagerData().getLevel();
      return VillagerData.func_221128_d(i) && this.xp >= VillagerData.func_221127_c(i);
   }

   private void populateBuyingList() {
      this.setVillagerData(this.getVillagerData().withLevel(this.getVillagerData().getLevel() + 1));
      this.populateTradeData();
   }

   protected ITextComponent func_225513_by_() {
      return new TranslationTextComponent(this.getType().getTranslationKey() + '.' + Registry.VILLAGER_PROFESSION.getKey(this.getVillagerData().getProfession()).getPath());
   }

   @OnlyIn(Dist.CLIENT)
   public void handleStatusUpdate(byte id) {
      if (id == 12) {
         this.func_213718_a(ParticleTypes.HEART);
      } else if (id == 13) {
         this.func_213718_a(ParticleTypes.ANGRY_VILLAGER);
      } else if (id == 14) {
         this.func_213718_a(ParticleTypes.HAPPY_VILLAGER);
      } else if (id == 42) {
         this.func_213718_a(ParticleTypes.SPLASH);
      } else {
         super.handleStatusUpdate(id);
      }

   }

   @Nullable
   public ILivingEntityData onInitialSpawn(IWorld worldIn, DifficultyInstance difficultyIn, SpawnReason reason, @Nullable ILivingEntityData spawnDataIn, @Nullable CompoundNBT dataTag) {
      if (reason == SpawnReason.BREEDING) {
         this.setVillagerData(this.getVillagerData().withProfession(VillagerProfession.NONE));
      }

      if (reason == SpawnReason.COMMAND || reason == SpawnReason.SPAWN_EGG || reason == SpawnReason.SPAWNER || reason == SpawnReason.DISPENSER) {
         this.setVillagerData(this.getVillagerData().withType(IVillagerType.byBiome(worldIn.func_226691_t_(new BlockPos(this)))));
      }

      return super.onInitialSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
   }

   public VillagerEntity createChild(AgeableEntity ageable) {
      double d0 = this.rand.nextDouble();
      IVillagerType ivillagertype;
      if (d0 < 0.5D) {
         ivillagertype = IVillagerType.byBiome(this.world.func_226691_t_(new BlockPos(this)));
      } else if (d0 < 0.75D) {
         ivillagertype = this.getVillagerData().getType();
      } else {
         ivillagertype = ((VillagerEntity)ageable).getVillagerData().getType();
      }

      VillagerEntity villagerentity = new VillagerEntity(EntityType.VILLAGER, this.world, ivillagertype);
      villagerentity.onInitialSpawn(this.world, this.world.getDifficultyForLocation(new BlockPos(villagerentity)), SpawnReason.BREEDING, (ILivingEntityData)null, (CompoundNBT)null);
      return villagerentity;
   }

   public void onStruckByLightning(LightningBoltEntity lightningBolt) {
      WitchEntity witchentity = EntityType.WITCH.create(this.world);
      witchentity.setLocationAndAngles(this.getPosX(), this.getPosY(), this.getPosZ(), this.rotationYaw, this.rotationPitch);
      witchentity.onInitialSpawn(this.world, this.world.getDifficultyForLocation(new BlockPos(witchentity)), SpawnReason.CONVERSION, (ILivingEntityData)null, (CompoundNBT)null);
      witchentity.setNoAI(this.isAIDisabled());
      if (this.hasCustomName()) {
         witchentity.setCustomName(this.getCustomName());
         witchentity.setCustomNameVisible(this.isCustomNameVisible());
      }

      this.world.addEntity(witchentity);
      this.remove();
   }

   protected void updateEquipmentIfNeeded(ItemEntity itemEntity) {
      ItemStack itemstack = itemEntity.getItem();
      Item item = itemstack.getItem();
      if (this.func_223717_b(item)) {
         Inventory inventory = this.func_213715_ed();
         boolean flag = false;

         for(int i = 0; i < inventory.getSizeInventory(); ++i) {
            ItemStack itemstack1 = inventory.getStackInSlot(i);
            if (itemstack1.isEmpty() || itemstack1.getItem() == item && itemstack1.getCount() < itemstack1.getMaxStackSize()) {
               flag = true;
               break;
            }
         }

         if (!flag) {
            return;
         }

         int j = inventory.count(item);
         if (j == 256) {
            return;
         }

         if (j > 256) {
            inventory.func_223374_a(item, j - 256);
            return;
         }

         this.onItemPickup(itemEntity, itemstack.getCount());
         ItemStack itemstack2 = inventory.addItem(itemstack);
         if (itemstack2.isEmpty()) {
            itemEntity.remove();
         } else {
            itemstack.setCount(itemstack2.getCount());
         }
      }

   }

   public boolean func_223717_b(Item p_223717_1_) {
      return field_213776_bD.contains(p_223717_1_) || this.getVillagerData().getProfession().func_221146_c().contains(p_223717_1_);
   }

   public boolean canAbondonItems() {
      return this.func_213751_ew() >= 24;
   }

   public boolean wantsMoreFood() {
      return this.func_213751_ew() < 12;
   }

   private int func_213751_ew() {
      Inventory inventory = this.func_213715_ed();
      return field_213788_bA.entrySet().stream().mapToInt((p_226553_1_) -> {
         return inventory.count(p_226553_1_.getKey()) * p_226553_1_.getValue();
      }).sum();
   }

   private void func_223359_eB() {
      Inventory inventory = this.func_213715_ed();
      int i = inventory.count(Items.WHEAT);
      int j = i / 3;
      if (j != 0) {
         int k = j * 3;
         inventory.func_223374_a(Items.WHEAT, k);
         ItemStack itemstack = inventory.addItem(new ItemStack(Items.BREAD, j));
         if (!itemstack.isEmpty()) {
            this.entityDropItem(itemstack, 0.5F);
         }

      }
   }

   public boolean isFarmItemInInventory() {
      Inventory inventory = this.func_213715_ed();
      return inventory.hasAny(ImmutableSet.of(Items.WHEAT_SEEDS, Items.POTATO, Items.CARROT, Items.BEETROOT_SEEDS));
   }

   protected void populateTradeData() {
      VillagerData villagerdata = this.getVillagerData();
      Int2ObjectMap<VillagerTrades.ITrade[]> int2objectmap = VillagerTrades.field_221239_a.get(villagerdata.getProfession());
      if (int2objectmap != null && !int2objectmap.isEmpty()) {
         VillagerTrades.ITrade[] avillagertrades$itrade = int2objectmap.get(villagerdata.getLevel());
         if (avillagertrades$itrade != null) {
            MerchantOffers merchantoffers = this.getOffers();
            this.addTrades(merchantoffers, avillagertrades$itrade, 2);
         }
      }
   }

   public void func_213746_a(VillagerEntity p_213746_1_, long p_213746_2_) {
      if ((p_213746_2_ < this.field_213783_bN || p_213746_2_ >= this.field_213783_bN + 1200L) && (p_213746_2_ < p_213746_1_.field_213783_bN || p_213746_2_ >= p_213746_1_.field_213783_bN + 1200L)) {
         this.gossip.func_220912_a(p_213746_1_.gossip, this.rand, 10);
         this.field_213783_bN = p_213746_2_;
         p_213746_1_.field_213783_bN = p_213746_2_;
         this.func_223358_a(p_213746_2_, 5);
      }
   }

   private void func_223343_eC() {
      long i = this.world.getGameTime();
      if (this.lastGossipDecay == 0L) {
         this.lastGossipDecay = i;
      } else if (i >= this.lastGossipDecay + 24000L) {
         this.gossip.func_223538_b();
         this.lastGossipDecay = i;
      }
   }

   public void func_223358_a(long p_223358_1_, int p_223358_3_) {
      if (this.func_223350_a(p_223358_1_)) {
         AxisAlignedBB axisalignedbb = this.getBoundingBox().grow(10.0D, 10.0D, 10.0D);
         List<VillagerEntity> list = this.world.getEntitiesWithinAABB(VillagerEntity.class, axisalignedbb);
         List<VillagerEntity> list1 = list.stream().filter((p_226554_2_) -> {
            return p_226554_2_.func_223350_a(p_223358_1_);
         }).limit(5L).collect(Collectors.toList());
         if (list1.size() >= p_223358_3_) {
            IronGolemEntity irongolementity = this.func_213759_ey();
            if (irongolementity != null) {
               list.forEach((p_226552_2_) -> {
                  p_226552_2_.func_223347_b(p_223358_1_);
               });
            }
         }
      }
   }

   private void func_223347_b(long p_223347_1_) {
      this.brain.setMemory(MemoryModuleType.GOLEM_LAST_SEEN_TIME, p_223347_1_);
   }

   private boolean func_223354_c(long p_223354_1_) {
      Optional<Long> optional = this.brain.getMemory(MemoryModuleType.GOLEM_LAST_SEEN_TIME);
      if (!optional.isPresent()) {
         return false;
      } else {
         Long olong = optional.get();
         return p_223354_1_ - olong <= 600L;
      }
   }

   public boolean func_223350_a(long p_223350_1_) {
      VillagerData villagerdata = this.getVillagerData();
      if (villagerdata.getProfession() != VillagerProfession.NONE && villagerdata.getProfession() != VillagerProfession.NITWIT) {
         if (!this.func_223352_d(this.world.getGameTime())) {
            return false;
         } else {
            return !this.func_223354_c(p_223350_1_);
         }
      } else {
         return false;
      }
   }

   @Nullable
   private IronGolemEntity func_213759_ey() {
      BlockPos blockpos = new BlockPos(this);

      for(int i = 0; i < 10; ++i) {
         double d0 = (double)(this.world.rand.nextInt(16) - 8);
         double d1 = (double)(this.world.rand.nextInt(16) - 8);
         double d2 = 6.0D;

         for(int j = 0; j >= -12; --j) {
            BlockPos blockpos1 = blockpos.add(d0, d2 + (double)j, d1);
            if ((this.world.getBlockState(blockpos1).isAir() || this.world.getBlockState(blockpos1).getMaterial().isLiquid()) && this.world.getBlockState(blockpos1.down()).getMaterial().isOpaque()) {
               d2 += (double)j;
               break;
            }
         }

         BlockPos blockpos2 = blockpos.add(d0, d2, d1);
         IronGolemEntity irongolementity = EntityType.IRON_GOLEM.create(this.world, (CompoundNBT)null, (ITextComponent)null, (PlayerEntity)null, blockpos2, SpawnReason.MOB_SUMMONED, false, false);
         if (irongolementity != null) {
            if (irongolementity.canSpawn(this.world, SpawnReason.MOB_SUMMONED) && irongolementity.isNotColliding(this.world)) {
               this.world.addEntity(irongolementity);
               return irongolementity;
            }

            irongolementity.remove();
         }
      }

      return null;
   }

   public void func_213739_a(IReputationType p_213739_1_, Entity p_213739_2_) {
      if (p_213739_1_ == IReputationType.ZOMBIE_VILLAGER_CURED) {
         this.gossip.func_220916_a(p_213739_2_.getUniqueID(), GossipType.MAJOR_POSITIVE, 20);
         this.gossip.func_220916_a(p_213739_2_.getUniqueID(), GossipType.MINOR_POSITIVE, 25);
      } else if (p_213739_1_ == IReputationType.TRADE) {
         this.gossip.func_220916_a(p_213739_2_.getUniqueID(), GossipType.TRADING, 2);
      } else if (p_213739_1_ == IReputationType.VILLAGER_HURT) {
         this.gossip.func_220916_a(p_213739_2_.getUniqueID(), GossipType.MINOR_NEGATIVE, 25);
      } else if (p_213739_1_ == IReputationType.VILLAGER_KILLED) {
         this.gossip.func_220916_a(p_213739_2_.getUniqueID(), GossipType.MAJOR_NEGATIVE, 25);
      }

   }

   public int getXp() {
      return this.xp;
   }

   public void setXp(int p_213761_1_) {
      this.xp = p_213761_1_;
   }

   private void func_223718_eH() {
      this.func_223719_ex();
      this.field_223725_bO = 0;
   }

   public GossipManager func_223722_es() {
      return this.gossip;
   }

   public void func_223716_a(INBT p_223716_1_) {
      this.gossip.func_220918_a(new Dynamic<>(NBTDynamicOps.INSTANCE, p_223716_1_));
   }

   protected void func_213387_K() {
      super.func_213387_K();
      DebugPacketSender.func_218798_a(this);
   }

   public void startSleeping(BlockPos p_213342_1_) {
      super.startSleeping(p_213342_1_);
      this.brain.setMemory(MemoryModuleType.LAST_SLEPT, LongSerializable.of(this.world.getGameTime()));
   }

   public void wakeUp() {
      super.wakeUp();
      this.brain.setMemory(MemoryModuleType.field_226332_A_, LongSerializable.of(this.world.getGameTime()));
   }

   private boolean func_223352_d(long p_223352_1_) {
      Optional<LongSerializable> optional = this.brain.getMemory(MemoryModuleType.LAST_SLEPT);
      Optional<LongSerializable> optional1 = this.brain.getMemory(MemoryModuleType.LAST_WORKED_AT_POI);
      if (optional.isPresent() && optional1.isPresent()) {
         return p_223352_1_ - optional.get().func_223461_a() < 24000L && p_223352_1_ - optional1.get().func_223461_a() < 36000L;
      } else {
         return false;
      }
   }
}
