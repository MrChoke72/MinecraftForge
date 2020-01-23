package com.mrchoke.entity.monster;

import com.mrchoke.entity.ai.goal.ChokeBreakDoorGoal;
import com.mrchoke.entity.ai.goal.ChokeTrapDoorGoal;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

import java.util.function.Predicate;

public abstract class BaseChokeZombie extends ZombieEntity {

    protected ChokeBreakDoorGoal breakDoorGoal;
    protected ChokeTrapDoorGoal trapDoorGoal;
    protected boolean isBreakDoorsTaskSet;
    protected boolean breakIronAndFences;

    protected static final Predicate<Difficulty> checkDifficulty = (difficulty) -> {
        return true;
    };

    private BlockPos playerPathTarget;
    private TargetModeEnum targetMode = ZombieNasty.TargetModeEnum.NORMAL;
    private PlayerEntity playerToFollow;


    protected BaseChokeZombie(EntityType<? extends ZombieEntity> type, World worldIn) {
        super(type, worldIn);
    }

    /*
    protected BaseChokeZombie(World worldIn) {
        this(EntityType.ZOMBIE_NASTY, worldIn);
    }
     */

    protected boolean shouldDrown() {
        return false;
    }

    /*
    public void tick() {
        super.tick();
    }

    public void livingTick() {
        super.livingTick();
    }
     */

    protected boolean shouldBurnInDay() {
        return false;
    }

    /*
    public boolean attackEntityFrom(DamageSource source, float amount) {
        return super.attackEntityFrom(source, amount);
    }

    public boolean attackEntityAsMob(Entity entityIn) {
        return super.attackEntityAsMob(entityIn);
    }
     */

       /*
    public CreatureAttribute getCreatureAttribute() {
        return CreatureAttribute.UNDEAD;
    }
     */

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

     /*
    public void onDeath(DamageSource cause) {
        super.onDeath(cause);
    }
     */

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
        //force nothing
    }

      /*
    protected void dropSpecialItems(DamageSource source, int looting, boolean recentlyHitIn) {
        super.dropSpecialItems(source, looting, recentlyHitIn);
    }
     */

    @Override
    public void remove() {
        super.remove();

        //AH CHANGE DEBUG
        if(!this.world.isRemote && this.getCustomName() != null && this.getCustomName().getString().equals("Chuck")) {
            System.out.println(this.getClass().getSimpleName() + " removed");
        }
    }

    protected ItemStack getSkullDrop() {
        return new ItemStack(Items.DRAGON_HEAD);
    }

    protected void applyAttributeBonuses(float difficulty) {
        this.getAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).applyModifier(new AttributeModifier("Random spawn bonus", this.rand.nextDouble() * (double)0.05F, AttributeModifier.Operation.ADDITION));

        //No beefed up follow range, its already big
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

    public boolean isBreakDoorsTaskSet() {
        return this.isBreakDoorsTaskSet;
    }

    public void setBreakDoorsAItask(boolean enabled) {
        if (this.canBreakDoors()) {
            if (this.isBreakDoorsTaskSet != enabled) {
                this.isBreakDoorsTaskSet = enabled;
                ((GroundPathNavigator) this.getNavigator()).setBreakDoors(enabled);
                if (enabled) {
                    this.goalSelector.addGoal(1, this.breakDoorGoal);

                    //AH CHANGE ADD
                    this.goalSelector.addGoal(1, this.trapDoorGoal);

                } else {
                    this.goalSelector.removeGoal(this.breakDoorGoal);

                    //AH CHANGE ADD
                    this.goalSelector.removeGoal(this.trapDoorGoal);
                }
            }
        } else if (this.isBreakDoorsTaskSet) {
            this.goalSelector.removeGoal(this.breakDoorGoal);

            //AH CHANGE ADD
            this.goalSelector.removeGoal(this.trapDoorGoal);

            this.isBreakDoorsTaskSet = false;
        }
    }

    //AH NEW STUFF FOR MY MOB ****
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

    public boolean isBreakIronAndFences() {
        return breakIronAndFences;
    }

    public void setBreakIronAndFences(boolean breakIronAndFences) {
        this.breakIronAndFences = breakIronAndFences;
    }

    public enum TargetModeEnum {
        NORMAL, //Random walk or attack
        PLAYER_PATH,    //follow player path
        RESET_PATH,     //Clear blocks processed before selecting path blocks
        RESET_FOLLOW,   //Right after follow path is done, do not let random start until select had a chance to find a new square
    }

    //AH END NEW STUFF ****

}