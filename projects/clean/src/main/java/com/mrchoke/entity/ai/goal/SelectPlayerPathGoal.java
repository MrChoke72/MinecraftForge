package com.mrchoke.entity.ai.goal;

import com.mrchoke.entity.monster.ZombieNasty;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.*;

public class SelectPlayerPathGoal extends Goal {

    public static final int MAX_PATH_DIST_SQ = (56*48) + (16*16) + (56*56);    //6528  (sqrt: 81)
    public static final int MAX_CHECK_SIGHT_DIST_SQ = (20*20) + (5*5) + (20*20);    //825 (sqrt: 29)

    private long waitBeforeExecTime;
    private ZombieNasty nasty;
    private int delayCounter;

    private Set<BlockPos> blockPosProcessed = new HashSet<>();

    public SelectPlayerPathGoal(ZombieNasty nasty) {
        this.nasty = nasty;
        this.setMutexFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    public boolean shouldExecute() {
        long i = this.nasty.world.getGameTime();
        if (i - this.waitBeforeExecTime < 40) { //wait 2 secs
            return false;
        } else {
            this.waitBeforeExecTime = i;

            if (nasty.getTargetMode() == ZombieNasty.TargetModeEnum.RESET_PATH)
            {
                nasty.setTargetMode(ZombieNasty.TargetModeEnum.NORMAL);
                blockPosProcessed.clear();
            }
            else if(nasty.getTargetMode() == ZombieNasty.TargetModeEnum.RESET_FOLLOW) {
                nasty.setTargetMode(ZombieNasty.TargetModeEnum.NORMAL);
            }

            if(nasty.getPlayerToFollow() == null) {
                List<ServerPlayerEntity> pList = nasty.getServer().getPlayerList().getPlayers();
                nasty.setPlayerToFollow(pList.get(nasty.getRNG().nextInt(pList.size())));
            }

            PlayerEntity player = nasty.getPlayerToFollow();
            if (nasty.getAttackTarget() != null || nasty.getTargetMode() == ZombieNasty.TargetModeEnum.PLAYER_PATH || !player.isAlive() || player.isSpectator()) {
                return false;
            }
            else
            {
                double distToPosSq = nasty.getDistanceSq(player);
                if (distToPosSq > MAX_PATH_DIST_SQ) {

                    //AH DEBUG OFF
                    /*
                    if(nasty.getCustomName() != null) {
                        System.out.println("SelectPlayerPathGoal: shouldExecute.  distToPlayer too far.  dist=" + Math.sqrt(distToPosSq) + ", No exec.  entPos=" + nasty.getPosition() + ", playPos="
                                + player.getPosition());
                    }
                     */

                    return false;
                }
                else
                {
                    //AH CHANGE DEBUG OFF
                    /*
                    if(nasty.getCustomName() != null)
                    {
                       System.out.println("In SelectPlayerPathGoal, shouldExecute.  Will start.  entPos=" + nasty.getPosition() + ", distEntToPlayer=" + Math.sqrt(distToPosSq));
                    }
                     */

                    return true;
                }
            }
        }
    }

    @Override
    public void startExecuting() {
        delayCounter = 0;
    }

    @Override
    public boolean shouldContinueExecuting() {
        if(nasty.getAttackTarget() != null || nasty.getTargetMode() != ZombieNasty.TargetModeEnum.NORMAL || !nasty.getPlayerToFollow().isAlive() || nasty.getPlayerToFollow().isSpectator()) {

            //AH CHANGE DEBUG OFF
            /*
            if(nasty.getCustomName() != null)
            {
                System.out.println("In SelectPlayerPathGoal, stopping execute.  entPos=" + nasty.getPosition() + ", nasty.getAttackTarget()=" + nasty.getAttackTarget() +
                        ", getTargetMode=" + nasty.getTargetMode() + ", player isAlive: " + nasty.getPlayerToFollow().isAlive());
            }
             */

            return false;
        }

        return true;
    }

    @Override
    public void resetTask() {
        //AH CHANGE DEBUG OFF
        /*
        if(this.nasty.getCustomName() != null && this.nasty.getCustomName().getString().equals("Chuck"))
        {
            System.out.println("In SelectPlayerPathGoal reset");
        }
         */
    }

    @Override
    public void tick() {
        --this.delayCounter;
        if(delayCounter <= 0) {
            List<BlockPos> playerPath = nasty.getPlayerToFollow().getPlayerPath();
            boolean bSetPathTarget = false;
            Map<BlockPos, Double> posDistMap = new HashMap<>();
            List<BlockPos> posList = new ArrayList<>();

            //canSeePos for-loop, check cloest to player first
            for (int posIdx = 0; posIdx < playerPath.size(); posIdx++) {
                BlockPos pos = playerPath.get(posIdx);
                if (blockPosProcessed.contains(pos)) {
                    continue;
                }

                double distToPosSq = nasty.getDistanceSq(pos.getX(), pos.getY(), pos.getZ());
                if (distToPosSq > MAX_PATH_DIST_SQ) {
                    blockPosProcessed.add(pos);
                    continue;
                }

                if (distToPosSq <= MAX_CHECK_SIGHT_DIST_SQ) {
                    if (nasty.canSeePos(pos)) {

                        //AH DEBUG OFF
                        /*
                        if(nasty.getCustomName() != null) {
                            System.out.println("SelectPlayerPathGoal: In tick - Nasty can see path pos.  entPos=" + nasty.getPosition() + ", posIdx=" + posIdx + ", Will path find to: " + pos);
                        }
                         */

                        blockPosProcessed.add(pos);
                        nasty.setPlayerPathTarget(pos);
                        bSetPathTarget = true;
                        break;
                    }
                }

                posDistMap.put(pos, distToPosSq);
                posList.add(pos);
            }

            if (!bSetPathTarget && posList.size() > 0) {
                //Closest to nasty iterate
                Collections.sort(posList, (pos1, pos2) -> {
                    double dist1 = posDistMap.get(pos1);
                    double dist2 = posDistMap.get(pos2);
                    return (dist1 == dist2) ? 0 : (dist1 < dist2) ? -1 : 1;
                });

                BlockPos pos = posList.get(0);

                //AH DEBUG OFF
                /*
                if(nasty.getCustomName() != null) {
                    System.out.println("SelectPlayerPathGoal: In tick - Will path find to closest pos to entity.  distToPos=" + Math.sqrt(posDistMap.get(pos)) + ", entPos= "
                            + nasty.getPosition() + ", pos=" + pos);
                }
                 */

                blockPosProcessed.add(pos);
                nasty.setPlayerPathTarget(pos);
                bSetPathTarget = true;
            }

            if (bSetPathTarget)
            {
                nasty.setTargetMode(ZombieNasty.TargetModeEnum.PLAYER_PATH);
                delayCounter = 100;
            }
            else {
                //All current path points have been tried already, reset it and try again
                nasty.setTargetMode(ZombieNasty.TargetModeEnum.RESET_PATH);

                //AH DEBUG OFF
                /*
                if(nasty.getCustomName() != null) {
                    System.out.println("SelectPlayerPathGoal: In tick - All paths tried and failed, clearing processed.  entPos=" + nasty.getPosition());
                }
                 */

                delayCounter = 100; //5 secs
                /*
                Set<BlockPos> pathSet = new HashSet<>(playerPath);
                if(blockPosProcessed.size() >= pathSet.size()) {
                    //blockPosProcessed.clear();
                    nasty.setTargetMode(ZombieNasty.TargetModeEnum.RESET_PATH);

                    //AH DEBUG
                    if(nasty.getCustomName() != null) {
                        System.out.println("SelectPlayerPathGoal: In tick - All paths tried and failed, clearing processed.  entPos=" + nasty.getPosition());
                    }

                    delayCounter = 200; //10 secs
                }
                else
                {
                    delayCounter = 5;
                }
                 */
            }
        }
    }

}
