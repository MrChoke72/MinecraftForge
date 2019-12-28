package net.minecraft.entity.ai.goal;

import net.minecraft.block.Block;
import net.minecraft.entity.MobEntity;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;

import java.util.function.Predicate;

//AH CHANGE NEW CLASS ******
public class BreakTrapDoorGoal extends InteractTrapDoorGoal {

    private final Predicate<Difficulty> difficultyPredicate;

    protected int breakingTime;
    protected int previousBreakProgress = -1;
    protected int maxTicksToBreak = -1;

    public BreakTrapDoorGoal(MobEntity entity, Predicate<Difficulty> difficultyPredicate) {
        super(entity);
        this.difficultyPredicate = difficultyPredicate;
    }

    public BreakTrapDoorGoal(MobEntity entity, int p_i50333_2_, Predicate<Difficulty> difficultyPredicate) {
        this(entity, difficultyPredicate);
        this.maxTicksToBreak = p_i50333_2_;
    }

    protected int getMaxTicksToBreak() {
        return Math.max(240, this.maxTicksToBreak);
    }

    public boolean shouldExecute() {
        if (!super.shouldExecute()) {
            return false;
        } else if (!this.entity.world.getGameRules().getBoolean(GameRules.MOB_GRIEFING)) {
            return false;
        } else {
            return this.checkDifficulty(this.entity.world.getDifficulty()) && !this.canDestroy();
        }
    }

    public void startExecuting() {
        super.startExecuting();
        this.breakingTime = 0;
    }

    public boolean shouldContinueExecuting() {
        return this.breakingTime <= this.getMaxTicksToBreak() && !this.canDestroy() && this.doorPosition.withinDistance(this.entity.getPositionVec(), 2.0D)
                && this.checkDifficulty(this.entity.world.getDifficulty());
    }

    public void resetTask() {
        super.resetTask();
        this.entity.world.sendBlockBreakProgress(this.entity.getEntityId(), this.doorPosition, -1);
    }

    public void tick() {
        super.tick();
        if (this.entity.getRNG().nextInt(20) == 0) {
            this.entity.world.playEvent(1019, this.doorPosition, 0);

            //No arm swinging
            /*
            if (!this.entity.isSwingInProgress) {
                this.entity.swingArm(this.entity.getActiveHand());
            }
             */
        }

        ++this.breakingTime;
        int i = (int)((float)this.breakingTime / (float)this.getMaxTicksToBreak() * 10.0F);
        if (i != this.previousBreakProgress) {
            this.entity.world.sendBlockBreakProgress(this.entity.getEntityId(), this.doorPosition, i);
            this.previousBreakProgress = i;
        }

        if (this.breakingTime == this.getMaxTicksToBreak() && this.checkDifficulty(this.entity.world.getDifficulty())) {
            this.entity.world.removeBlock(this.doorPosition, false);
            this.entity.world.playEvent(1021, this.doorPosition, 0);
            this.entity.world.playEvent(2001, this.doorPosition, Block.getStateId(this.entity.world.getBlockState(this.doorPosition)));
        }

    }

    private boolean checkDifficulty(Difficulty difficulty) {
        return this.difficultyPredicate.test(difficulty);
    }

}
