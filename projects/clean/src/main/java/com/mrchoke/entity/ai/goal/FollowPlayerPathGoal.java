package com.mrchoke.entity.ai.goal;

import com.mrchoke.entity.monster.ZombieNasty;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.math.BlockPos;

import java.util.EnumSet;

public class FollowPlayerPathGoal extends Goal {
    private final ZombieNasty nasty;
    private final double speed;
    private int delayCounter;
    private Path path;
    private long waitBeforeExecTime;

    public FollowPlayerPathGoal(ZombieNasty nasty, double speedIn) {
        this.nasty = nasty;
        this.speed = speedIn;
        this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    public boolean shouldExecute() {
        long i = this.nasty.world.getGameTime();
        if (i - this.waitBeforeExecTime < 10) {
            return false;
        }
        else {
            this.waitBeforeExecTime = i;
            if (nasty.getAttackTarget() != null || nasty.getTargetMode() != ZombieNasty.TargetModeEnum.PLAYER_PATH || nasty.getPlayerToFollow().isSpectator() || !nasty.getPlayerToFollow().isAlive()) {
                return false;
            }
            else {

                //AH CHANGE DEBUG OFF
                /*
                if (this.nasty.getCustomName() != null && this.nasty.getCustomName().getString().equals("Chuck")) {
                    System.out.println("FollowPlayerPathGoal:  In shouldExecute.  Will execute.  entPos=" + nasty.getPosition() + ", tgtPos=" + nasty.getPlayerPathTarget());
                }
                 */

                return true;
            }
        }
    }

    public boolean shouldContinueExecuting() {
        PlayerEntity player = nasty.getPlayerToFollow();
        boolean bCont = !nasty.getNavigator().noPath() && nasty.getAttackTarget() == null && nasty.getTargetMode() == ZombieNasty.TargetModeEnum.PLAYER_PATH
                && !player.isSpectator() && player.isAlive();

        //AH CHANGE DEBUG OFF
        /*
        if(!bCont && this.nasty.getCustomName() != null && this.nasty.getCustomName().getString().equals("Chuck"))
        {
            String s;
            if(this.nasty.getNavigator().getPath() == null)
            {
                s = "no-path";
            }
            else
            {
                s = String.valueOf(this.nasty.getNavigator().getPath().isFinished());
            }

            System.out.println("FollowPlayerPathGoal:  stopping execute.  entPos=" + this.nasty.getPosition() + ", targetMode=" + nasty.getTargetMode()
                    + ", noPath=" + s + ", attackTarget=" + nasty.getAttackTarget() + ", playerSpactate=" + player.isSpectator()
                    + ", playerAlive=" + player.isAlive());
        }
         */

        return bCont;
    }

    public void startExecuting() {
        //nasty.getNavigator().setPath(this.path, this.speed);

        BlockPos targetPos = nasty.getPlayerPathTarget();
        this.nasty.getNavigator().tryMoveToXYZ(targetPos.getX(), targetPos.getY(), targetPos.getZ(), this.speed);
        delayCounter = 10;
    }

    public void resetTask() {
        nasty.setTargetMode(ZombieNasty.TargetModeEnum.RESET_FOLLOW);

        //AH CHANGE DEBUG OFF
        /*
        if(this.nasty.getCustomName() != null && this.nasty.getCustomName().getString().equals("Chuck"))
        {
            System.out.println("In FollowPlayerPathGoal reset");
        }
         */
    }

    @Override
    public void tick() {
        --this.delayCounter;
        if(delayCounter <= 0) {
            nasty.setIdleTime(0);    //stop despawn if he is following path

            if(this.path != null) {
                nasty.getNavigator().setPath(this.path, this.speed);
                this.path = null;
            }

            double distToPosSq = nasty.getDistanceSq(nasty.getPlayerToFollow());
            if (distToPosSq <= SelectPlayerPathGoal.MAX_CHECK_SIGHT_DIST_SQ && this.nasty.getEntitySenses().canSee(nasty.getPlayerToFollow())) {
                nasty.setTargetMode(ZombieNasty.TargetModeEnum.RESET_PATH);

                //AH CHANGE DEBUG OFF
                /*
                if(this.nasty.getCustomName() != null && this.nasty.getCustomName().getString().equals("Chuck"))
                {
                    System.out.println("In FollowPlayerPathGoal, in tick.  can see player, stopping follow path.  entityPos=" + nasty.getPosition());
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
