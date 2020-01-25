package com.mrchoke.entity.ai.goal;

import com.mrchoke.entity.monster.BaseChokeZombie;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.math.BlockPos;

import java.util.EnumSet;

public class FollowPlayerPathGoal extends Goal {
    private final BaseChokeZombie chokeZombie;
    private final double speed;
    private int delayCounter;
    private Path path;
    private long waitBeforeExecTime;

    public FollowPlayerPathGoal(BaseChokeZombie chokeZombie, double speedIn) {
        this.chokeZombie = chokeZombie;
        this.speed = speedIn;
        this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    public boolean shouldExecute() {
        long i = this.chokeZombie.world.getGameTime();
        if (i - this.waitBeforeExecTime < 10) {
            return false;
        }
        else {
            this.waitBeforeExecTime = i;
            if (chokeZombie.getAttackTarget() != null || chokeZombie.getTargetMode() != BaseChokeZombie.TargetModeEnum.PLAYER_PATH || chokeZombie.getPlayerToFollow().isSpectator() || !chokeZombie.getPlayerToFollow().isAlive()) {
                return false;
            }
            else {

                //AH CHANGE DEBUG
                if (this.chokeZombie.getCustomName() != null && this.chokeZombie.getCustomName().getString().equals("Chuck")) {
                    System.out.println("FollowPlayerPathGoal:  In shouldExecute.  Will execute.  entPos=" + chokeZombie.getPosition() + ", tgtPos=" + chokeZombie.getPlayerPathTarget());
                }

                return true;
            }
        }
    }

    public boolean shouldContinueExecuting() {
        PlayerEntity player = chokeZombie.getPlayerToFollow();
        boolean bCont = !chokeZombie.getNavigator().noPath() && chokeZombie.getAttackTarget() == null && chokeZombie.getTargetMode() == BaseChokeZombie.TargetModeEnum.PLAYER_PATH
                && !player.isSpectator() && player.isAlive();

        //AH CHANGE DEBUG OFF
        /*
        if(!bCont && this.chokeZombie.getCustomName() != null && this.chokeZombie.getCustomName().getString().equals("Chuck"))
        {
            String s;
            if(this.chokeZombie.getNavigator().getPath() == null)
            {
                s = "no-path";
            }
            else
            {
                s = String.valueOf(this.chokeZombie.getNavigator().getPath().isFinished());
            }

            System.out.println("FollowPlayerPathGoal:  stopping execute.  entPos=" + this.chokeZombie.getPosition() + ", targetMode=" + chokeZombie.getTargetMode()
                    + ", noPath=" + s + ", attackTarget=" + chokeZombie.getAttackTarget() + ", playerSpactate=" + player.isSpectator()
                    + ", playerAlive=" + player.isAlive());
        }
         */

        return bCont;
    }

    public void startExecuting() {
        //chokeZombie.getNavigator().setPath(this.path, this.speed);

        BlockPos targetPos = chokeZombie.getPlayerPathTarget();
        this.chokeZombie.getNavigator().tryMoveToXYZ(targetPos.getX(), targetPos.getY(), targetPos.getZ(), this.speed);
        delayCounter = 10;
    }

    public void resetTask() {
        chokeZombie.setTargetMode(BaseChokeZombie.TargetModeEnum.RESET_FOLLOW);

        //AH CHANGE DEBUG OFF
        /*
        if(this.chokeZombie.getCustomName() != null && this.chokeZombie.getCustomName().getString().equals("Chuck"))
        {
            System.out.println("In FollowPlayerPathGoal reset");
        }
         */
    }

    @Override
    public void tick() {
        --this.delayCounter;
        if(delayCounter <= 0) {
            chokeZombie.setIdleTime(0);    //stop despawn if he is following path

            if(this.path != null) {
                chokeZombie.getNavigator().setPath(this.path, this.speed);
                this.path = null;
            }

            double distToPosSq = chokeZombie.getDistanceSq(chokeZombie.getPlayerToFollow());
            if (distToPosSq <= SelectPlayerPathGoal.MAX_CHECK_SIGHT_DIST_SQ && this.chokeZombie.getEntitySenses().canSee(chokeZombie.getPlayerToFollow())) {
                chokeZombie.setTargetMode(BaseChokeZombie.TargetModeEnum.RESET_PATH);

                //AH CHANGE DEBUG OFF
                /*
                if(this.chokeZombie.getCustomName() != null && this.chokeZombie.getCustomName().getString().equals("Chuck"))
                {
                    System.out.println("In FollowPlayerPathGoal, in tick.  can see player, stopping follow path.  entityPos=" + chokeZombie.getPosition());
                }
                 */

                delayCounter = 100;
            }
            else
            {
                delayCounter = 10;
            }
        }
    }
}
