package com.mrchoke.entity.monster;

import com.mrchoke.entity.ai.goal.*;
import net.minecraft.block.BlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.List;

public class ZombieNasty extends BaseChokeZombie {

    private static final Logger LOGGER = LogManager.getLogger();

    public ZombieNasty(EntityType<? extends ZombieEntity> type, World worldIn) {
        super(type, worldIn);

        breakIronAndFences = false;

        if(breakNotOpen) {
            breakDoorGoal =  new ChokeBreakDoorGoal(this, checkDifficulty, breakIronAndFences);
        }
        else {
            openDoorGoal = new ChokeOpenDoorGoal(this, true, breakIronAndFences);
        }

        trapDoorGoal = new ChokeTrapDoorGoal(this, false, breakIronAndFences);
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
        this.goalSelector.addGoal(2, new ZombieChokeAttackGoal(this, 1.0D));
        this.goalSelector.addGoal(4, new FollowPlayerPathGoal(this, 1.0D));     //Follow a player path
        this.goalSelector.addGoal(7, new WaterAvoidingRandomChokeGoal(this, 1.25D));  //Beef up speed a little when random walking

        this.targetSelector.addGoal(1, (new HurtByTargetGoal(this, ZombieNasty.class)));    //Ignore hurt by other nasties
        this.targetSelector.addGoal(2, new NearestAttTargetChokeGoal<>(this, PlayerEntity.class, false)); //He must see player to start target, but he doesn't need to see to keep it
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

    public class GroupData implements ILivingEntityData {
        public final boolean isChild;

        private GroupData(boolean b) {
            this.isChild = b;
        }
    }

}
