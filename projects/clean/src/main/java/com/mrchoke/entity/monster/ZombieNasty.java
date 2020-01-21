package com.mrchoke.entity.monster;

import com.mrchoke.entity.ai.goal.*;
import net.minecraft.block.BlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public class ZombieNasty extends ZombieEntity {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final UUID BABY_SPEED_BOOST_ID = UUID.fromString("B9766B59-9566-4402-BC1F-2EE2A276D836");
    private static final AttributeModifier BABY_SPEED_BOOST = new AttributeModifier(BABY_SPEED_BOOST_ID, "Baby speed boost", 0.5D, AttributeModifier.Operation.MULTIPLY_BASE);
    private static final DataParameter<Boolean> IS_CHILD = EntityDataManager.createKey(ZombieNasty.class, DataSerializers.BOOLEAN);
    //private static final DataParameter<Integer> VILLAGER_TYPE = EntityDataManager.createKey(ZombieNasty.class, DataSerializers.VARINT);
    //private static final DataParameter<Boolean> DROWNING = EntityDataManager.createKey(ZombieNasty.class, DataSerializers.BOOLEAN);

    private static final Predicate<Difficulty> checkDifficulty = (difficulty) -> {
        return true;
    };
    private final BreakDoorGoal breakDoor = new BreakDoorGoal(this, checkDifficulty);
    private final OpenTrapDoorGoal openTrapDoor = new OpenTrapDoorGoal(this, false);

    //private boolean isBreakDoorsTaskSet;
    //private int inWaterTime;
    //private int drownedConversionTime;

    private BlockPos playerPathTarget;
    private TargetModeEnum targetMode = TargetModeEnum.NORMAL;
    private PlayerEntity playerToFollow;

    public ZombieNasty(EntityType<? extends ZombieEntity> type, World worldIn) {
        super(type, worldIn);
    }

    public ZombieNasty(World worldIn) {
        this(EntityType.ZOMBIE_NASTY, worldIn);
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(8, new LookAtGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.addGoal(8, new LookRandomlyGoal(this));
        this.applyEntityAI();
    }

    protected void applyEntityAI() {
        this.goalSelector.addGoal(1, new SwimGoal(this));   //he don't drowned, so he can swim

        //AH DEBUG - Fix dude not attacking all the time
        //this.goalSelector.addGoal(2, new ZombieNastyAttackGoal(this, 1.0D));
        this.goalSelector.addGoal(2, new ZombieAttackGoal(this, 1.0D, true));


        this.goalSelector.addGoal(4, new FollowPlayerPathGoal(this, 1.0D));     //Follow a player path
        this.goalSelector.addGoal(7, new WaterAvoidingRandomNastyGoal(this, 1.25D));  //Beef up speed a little when random walking

        this.targetSelector.addGoal(1, (new HurtByTargetGoal(this, ZombieNasty.class)));    //Ignore hurt by other nasties
        this.targetSelector.addGoal(2, new NearestAttTargetNastyGoal<>(this, PlayerEntity.class, false)); //He must see player to start target, but he doesn't need to see to keep it
        this.targetSelector.addGoal(4, new SelectPlayerPathGoal(this)); //Try to sniff out a player's recent path
    }

    protected void registerAttributes() {
        super.registerAttributes();

        this.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(64.0D);        //BIG
        this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue((double)0.23F);  //default is 0.23
        this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(3.0D);    //same as zombie
        this.getAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(2.5D);    //beefed up from 2.0
        this.getAttribute(SPAWN_REINFORCEMENTS_CHANCE).setBaseValue(0.0D);
    }

    protected boolean shouldDrown() {
        return false;
    }

    public void tick() {
        super.tick();
    }

    public void livingTick() {
        super.livingTick();
    }

    public boolean processInteract(PlayerEntity player, Hand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        Item item = itemstack.getItem();
        if (item instanceof SpawnEggItem && ((SpawnEggItem)item).hasType(itemstack.getTag(), this.getType())) {
            if (!this.world.isRemote) {
                ZombieNasty nasty = (ZombieNasty)this.getType().create(this.world);
                if (nasty != null) {
                    nasty.setChild(true);
                    nasty.setLocationAndAngles(this.getPosX(), this.getPosY(), this.getPosZ(), 0.0F, 0.0F);
                    this.world.addEntity(nasty);
                    if (itemstack.hasDisplayName()) {
                        nasty.setCustomName(itemstack.getDisplayName());
                    }

                    if (!player.abilities.isCreativeMode) {
                        itemstack.shrink(1);
                    }
                }
            }

            return true;
        } else {
            return false;
        }
    }

    protected boolean shouldBurnInDay() {
        return false;
    }

    public boolean attackEntityFrom(DamageSource source, float amount) {
        return super.attackEntityFrom(source, amount);
    }

    public boolean attackEntityAsMob(Entity entityIn) {
        return super.attackEntityAsMob(entityIn);
    }

    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_ZOMBIENASTY_AMBIENT;
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundEvents.ENTITY_ZOMBIENASTY_HURT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_ZOMBIENASTY_DEATH;
    }

    protected SoundEvent getStepSound() {
        return SoundEvents.ENTITY_ZOMBIENASTY_STEP;
    }

    protected void playStepSound(BlockPos pos, BlockState blockIn) {
        this.playSound(this.getStepSound(), 0.15F, 1.0F);
    }

    public CreatureAttribute getCreatureAttribute() {
        return CreatureAttribute.UNDEAD;
    }

    protected void setEquipmentBasedOnDifficulty(DifficultyInstance difficulty) {
        super.setEquipmentBasedOnDifficulty(difficulty);
        if (this.rand.nextFloat() < (this.world.getDifficulty() == Difficulty.HARD ? 0.05F : 0.01F)) {
            int i = this.rand.nextInt(3);
            if (i == 0) {
                this.setItemStackToSlot(EquipmentSlotType.MAINHAND, new ItemStack(Items.DIAMOND_SWORD));
            } else {
                this.setItemStackToSlot(EquipmentSlotType.MAINHAND, new ItemStack(Items.DIAMOND_SHOVEL));
            }
        }

    }

    @Override
    public void remove() {
        super.remove();

        //AH CHANGE DEBUG
        if(!this.world.isRemote && this.getCustomName() != null && this.getCustomName().getString().equals("Chuck")) {
            System.out.println("ZombineNasty removed");
        }
    }

    public void onDeath(DamageSource cause) {
        super.onDeath(cause);
    }

    /*
    public void writeAdditional(CompoundNBT compound) {
        super.writeAdditional(compound);
        if (this.isChild()) {
            compound.putBoolean("IsBaby", true);
        }

        compound.putBoolean("CanBreakDoors", this.isBreakDoorsTaskSet());
        compound.putInt("InWaterTime", this.isInWater() ? this.inWaterTime : -1);
        compound.putInt("DrownedConversionTime", this.isDrowning() ? this.drownedConversionTime : -1);
    }

    public void readAdditional(CompoundNBT compound) {
        super.readAdditional(compound);
        if (compound.getBoolean("IsBaby")) {
            this.setChild(true);
        }

        this.setBreakDoorsAItask(compound.getBoolean("CanBreakDoors"));
        this.inWaterTime = compound.getInt("InWaterTime");
        if (compound.contains("DrownedConversionTime", 99) && compound.getInt("DrownedConversionTime") > -1) {
            this.startDrowning(compound.getInt("DrownedConversionTime"));
        }
    }
     */

    public void onKillEntity(LivingEntity entityLivingIn) {
        //nothing
    }

    /*
    protected float getStandingEyeHeight(Pose poseIn, EntitySize sizeIn) {
        return this.isChild() ? 0.93F : 1.74F;
    }

    protected boolean canEquipItem(ItemStack stack) {
        return stack.getItem() == Items.EGG && this.isChild() && this.isPassenger() ? false : super.canEquipItem(stack);
    }
     */

    @Nullable
    public ILivingEntityData onInitialSpawn(IWorld worldIn, DifficultyInstance difficultyIn, SpawnReason reason, @Nullable ILivingEntityData spawnDataIn, @Nullable CompoundNBT dataTag) {
        spawnDataIn = super.onInitialSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
        float f = difficultyIn.getClampedAdditionalDifficulty();
        this.setCanPickUpLoot(this.rand.nextFloat() < 0.55F * f);
        if (spawnDataIn == null) {
            spawnDataIn = new ZombieNasty.GroupData(worldIn.getRandom().nextFloat() < 0.05F);  //This is the chance to be a baby
        }

        if (spawnDataIn instanceof ZombieNasty.GroupData) {
            ZombieNasty.GroupData zombieentity$groupdata = (ZombieNasty.GroupData)spawnDataIn;
            if (zombieentity$groupdata.isChild) {
                this.setChild(true);
                if ((double)worldIn.getRandom().nextFloat() < 0.05D) {
                    List<ChickenEntity> list = worldIn.getEntitiesWithinAABB(ChickenEntity.class, this.getBoundingBox().grow(5.0D, 3.0D, 5.0D), EntityPredicates.IS_STANDALONE);
                    if (!list.isEmpty()) {
                        ChickenEntity chickenentity = list.get(0);
                        chickenentity.setChickenJockey(true);
                        this.startRiding(chickenentity);
                    }
                } else if ((double)worldIn.getRandom().nextFloat() < 0.05D) {
                    ChickenEntity chickenentity1 = EntityType.CHICKEN.create(this.world);
                    chickenentity1.setLocationAndAngles(this.getPosX(), this.getPosY(), this.getPosZ(), this.rotationYaw, 0.0F);
                    chickenentity1.onInitialSpawn(worldIn, difficultyIn, SpawnReason.JOCKEY, (ILivingEntityData)null, (CompoundNBT)null);
                    chickenentity1.setChickenJockey(true);
                    worldIn.addEntity(chickenentity1);
                    this.startRiding(chickenentity1);
                }
            }

            this.setEquipmentBasedOnDifficulty(difficultyIn);
            this.setEnchantmentBasedOnDifficulty(difficultyIn);
        }

        this.applyAttributeBonuses(f);
        this.setBreakDoorsAItask(true);

        //AH DEBUG OFF - Test out follow path and 128 block auto-depswawn issue.  For now, no despawn
        //enablePersistence();

        return spawnDataIn;
    }

    protected void applyAttributeBonuses(float difficulty) {
        this.getAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).applyModifier(new AttributeModifier("Random spawn bonus", this.rand.nextDouble() * (double)0.05F, AttributeModifier.Operation.ADDITION));

        //No beefed up follow trange, its already big
        /*
        double d0 = this.rand.nextDouble() * 1.5D * (double)difficulty;
        if (d0 > 1.0D) {
            this.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).applyModifier(new AttributeModifier("Random zombie-spawn bonus", d0, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
         */

        if (this.rand.nextFloat() < difficulty * 0.05F) {
            //this.getAttribute(SPAWN_REINFORCEMENTS_CHANCE).applyModifier(new AttributeModifier("Leader zombie bonus", this.rand.nextDouble() * 0.25D + 0.5D, AttributeModifier.Operation.ADDITION));
            this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).applyModifier(new AttributeModifier("Leader zombie bonus", this.rand.nextDouble() * 3.0D + 1.0D, AttributeModifier.Operation.MULTIPLY_TOTAL));
            //this.setBreakDoorsAItask(this.canBreakDoors());
        }

    }

    protected void dropSpecialItems(DamageSource source, int looting, boolean recentlyHitIn) {
        super.dropSpecialItems(source, looting, recentlyHitIn);
    }

    protected ItemStack getSkullDrop() {
        return new ItemStack(Items.DRAGON_HEAD);
    }

    public BlockPos getPlayerPathTarget() {
        return playerPathTarget;
    }

    public void setPlayerPathTarget(BlockPos playerPathTarget) {
        this.playerPathTarget = playerPathTarget;
    }

    public TargetModeEnum getTargetMode() {
        return targetMode;
    }

    public void setTargetMode(TargetModeEnum targetMode) {
        this.targetMode = targetMode;
    }

    public PlayerEntity getPlayerToFollow() {
        return playerToFollow;
    }

    public void setPlayerToFollow(PlayerEntity playerToFollow) {
        this.playerToFollow = playerToFollow;
    }

    public class GroupData implements ILivingEntityData {
        public final boolean isChild;

        private GroupData(boolean p_i47328_2_) {
            this.isChild = p_i47328_2_;
        }
    }

    public enum TargetModeEnum {
        NORMAL, //Random walk or attack
        PLAYER_PATH,    //follow player path
        RESET_PATH,     //Clear blocks processed before selecting path blocks
        RESET_FOLLOW,   //Right after follow path is done, do not let random start until select had a chance to find a new square
    }

}
