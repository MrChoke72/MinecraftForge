package com.mrchoke.entity.ai.goal;

import com.mrchoke.entity.monster.BaseChokeZombie;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.*;

public class SelectPlayerPathGoal extends Goal {

    public static final int MAX_PATH_DIST_SQ = (64*64) + (24*24) + (64*64);    //8768  (sqrt: 93)
    public static final int MAX_CHECK_SIGHT_DIST_SQ = (24*24) + (8*8) + (24*24);    //1216 (sqrt: 34)

    private long waitBeforeExecTime;
    private BaseChokeZombie chokeZombie;
    private int delayCounter;

    private Set<BlockPos> blockPosProcessed = new HashSet<>();

    public SelectPlayerPathGoal(BaseChokeZombie chokeZombie) {
        this.chokeZombie = chokeZombie;
        this.setMutexFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    public boolean shouldExecute() {
        long i = this.chokeZombie.world.getGameTime();
        if (i - this.waitBeforeExecTime < 40) { //wait 2 secs
            return false;
        } else {
            this.waitBeforeExecTime = i;

            if (chokeZombie.getTargetMode() == BaseChokeZombie.TargetModeEnum.RESET_PATH)
            {
                chokeZombie.setTargetMode(BaseChokeZombie.TargetModeEnum.NORMAL);
                blockPosProcessed.clear();
            }
            else if(chokeZombie.getTargetMode() == BaseChokeZombie.TargetModeEnum.RESET_FOLLOW) {
                chokeZombie.setTargetMode(BaseChokeZombie.TargetModeEnum.NORMAL);
            }

            if(chokeZombie.getPlayerToFollow() == null) {
                List<ServerPlayerEntity> pList = chokeZombie.getServer().getPlayerList().getPlayers();
                if(pList.size() > 0) {
                    chokeZombie.setPlayerToFollow(pList.get(chokeZombie.getRNG().nextInt(pList.size())));
                }
            }

            PlayerEntity player = chokeZombie.getPlayerToFollow();
            if(player != null) {
                if (chokeZombie.getAttackTarget() != null || chokeZombie.getTargetMode() == BaseChokeZombie.TargetModeEnum.PLAYER_PATH || !player.isAlive() || player.isSpectator()) {
                    return false;
                } else {
                    double distToPosSq = chokeZombie.getDistanceSq(player);
                    if (distToPosSq > MAX_PATH_DIST_SQ) {

                        //AH DEBUG OFF
                        /*
                        if(chokeZombie.getCustomName() != null) {
                            System.out.println("SelectPlayerPathGoal: shouldExecute.  distToPlayer too far.  dist=" + Math.sqrt(distToPosSq) + ", No exec.  entPos=" + chokeZombie.getPosition() + ", playPos="
                                    + player.getPosition());
                        }
                         */

                        return false;
                    } else {
                        //AH CHANGE DEBUG OFF
                        /*
                        if (chokeZombie.getCustomName() != null) {
                            System.out.println("In SelectPlayerPathGoal, shouldExecute.  Will start.  entPos=" + chokeZombie.getPosition() + ", distEntToPlayer=" + Math.sqrt(distToPosSq));
                        }
                         */

                        return true;
                    }
                }
            }
            else
            {
                return false;
            }

        }
    }

    @Override
    public void startExecuting() {
        delayCounter = 0;
    }

    @Override
    public boolean shouldContinueExecuting() {
        if(chokeZombie.getAttackTarget() != null || chokeZombie.getTargetMode() != BaseChokeZombie.TargetModeEnum.NORMAL || !chokeZombie.getPlayerToFollow().isAlive()
                || chokeZombie.getPlayerToFollow().isSpectator()) {

            //AH CHANGE DEBUG OFF
            /*
            if(chokeZombie.getCustomName() != null)
            {
                System.out.println("In SelectPlayerPathGoal, stopping execute.  entPos=" + chokeZombie.getPosition() + ", chokeZombie.getAttackTarget()=" + chokeZombie.getAttackTarget() +
                        ", getTargetMode=" + chokeZombie.getTargetMode() + ", player isAlive: " + chokeZombie.getPlayerToFollow().isAlive());
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
        if(this.chokeZombie.getCustomName() != null && this.chokeZombie.getCustomName().getString().equals("Chuck"))
        {
            System.out.println("In SelectPlayerPathGoal reset");
        }
         */
    }

    @Override
    public void tick() {
        --this.delayCounter;
        if(delayCounter <= 0) {
            List<BlockPos> playerPath = chokeZombie.getPlayerToFollow().getPlayerPath();
            boolean bSetPathTarget = false;
            Map<BlockPos, Double> posDistMap = new HashMap<>();
            List<BlockPos> posList = new ArrayList<>();

            //canSeePos for-loop, check cloest to player first
            for (int posIdx = 0; posIdx < playerPath.size(); posIdx++) {
                BlockPos pos = playerPath.get(posIdx);
                if (blockPosProcessed.contains(pos)) {
                    continue;
                }

                double distToPosSq = chokeZombie.getDistanceSq(pos.getX(), pos.getY(), pos.getZ());
                if (distToPosSq > MAX_PATH_DIST_SQ) {
                    blockPosProcessed.add(pos);
                    continue;
                }

                if (distToPosSq <= MAX_CHECK_SIGHT_DIST_SQ) {
                    if (chokeZombie.canSeePos(pos)) {

                        //AH DEBUG OFF
                        /*
                        if(chokeZombie.getCustomName() != null) {
                            System.out.println("SelectPlayerPathGoal: In tick - chokeZombie can see path pos.  entPos=" + chokeZombie.getPosition() + ", posIdx=" + posIdx + ", Will path find to: " + pos);
                        }
                         */

                        blockPosProcessed.add(pos);
                        chokeZombie.setPlayerPathTarget(pos);
                        bSetPathTarget = true;
                        break;
                    }
                }

                posDistMap.put(pos, distToPosSq);
                posList.add(pos);
            }

            if (!bSetPathTarget && posList.size() > 0) {
                //Closest to chokeZombie iterate
                Collections.sort(posList, (pos1, pos2) -> {
                    double dist1 = posDistMap.get(pos1);
                    double dist2 = posDistMap.get(pos2);
                    return (dist1 == dist2) ? 0 : (dist1 < dist2) ? -1 : 1;
                });

                BlockPos pos = posList.get(0);

                //AH DEBUG OFF
                /*
                if(chokeZombie.getCustomName() != null) {
                    System.out.println("SelectPlayerPathGoal: In tick - Will path find to closest pos to entity.  distToPos=" + Math.sqrt(posDistMap.get(pos)) + ", entPos= "
                            + chokeZombie.getPosition() + ", pos=" + pos);
                }
                 */

                blockPosProcessed.add(pos);
                chokeZombie.setPlayerPathTarget(pos);
                bSetPathTarget = true;
            }

            if (bSetPathTarget)
            {
                chokeZombie.setTargetMode(BaseChokeZombie.TargetModeEnum.PLAYER_PATH);
                delayCounter = 100;
            }
            else {
                //All current path points have been tried already, reset it and try again
                chokeZombie.setTargetMode(BaseChokeZombie.TargetModeEnum.RESET_PATH);

                //AH DEBUG OFF
                /*
                if(chokeZombie.getCustomName() != null) {
                    System.out.println("SelectPlayerPathGoal: In tick - All paths tried and failed, clearing processed.  entPos=" + chokeZombie.getPosition());
                }
                 */

                delayCounter = 100; //5 secs
                /*
                Set<BlockPos> pathSet = new HashSet<>(playerPath);
                if(blockPosProcessed.size() >= pathSet.size()) {
                    //blockPosProcessed.clear();
                    chokeZombie.setTargetMode(BaseChokeZombie.TargetModeEnum.RESET_PATH);

                    //AH DEBUG
                    if(chokeZombie.getCustomName() != null) {
                        System.out.println("SelectPlayerPathGoal: In tick - All paths tried and failed, clearing processed.  entPos=" + chokeZombie.getPosition());
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
