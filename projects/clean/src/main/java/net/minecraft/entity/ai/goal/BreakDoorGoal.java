package net.minecraft.entity.ai.goal;

import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.entity.MobEntity;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;

public class BreakDoorGoal extends InteractDoorGoal {

   //AH CHANGE REFACTOR
   private final Predicate<Difficulty> difficultyPredicate;
   //private final Predicate<Difficulty> field_220699_g;

   protected int breakingTime;
   protected int previousBreakProgress = -1;

   //AH CHANGE REFACTOR
   protected int maxTicksToBreak = -1;
   //protected int field_220698_c = -1;

   public BreakDoorGoal(MobEntity entity, Predicate<Difficulty> diffPred) {
      super(entity);
      this.difficultyPredicate = diffPred;
   }

   public BreakDoorGoal(MobEntity p_i50333_1_, int p_i50333_2_, Predicate<Difficulty> p_i50333_3_) {
      this(p_i50333_1_, p_i50333_3_);
      this.maxTicksToBreak = p_i50333_2_;
   }

   //AH CHANGE REFACTOR
   protected int getMaxTicksToBreak() {
   //protected int func_220697_f() {
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
         if (!this.entity.isSwingInProgress) {
            this.entity.swingArm(this.entity.getActiveHand());
         }
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

   //AH CHANGE REFACTOR
   private boolean checkDifficulty(Difficulty p_220696_1_) {
   //private boolean func_220696_a(Difficulty p_220696_1_) {
      return this.difficultyPredicate.test(p_220696_1_);
   }
}
