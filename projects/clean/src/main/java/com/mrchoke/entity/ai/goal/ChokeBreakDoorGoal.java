package com.mrchoke.entity.ai.goal;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.InteractDoorGoal;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

import java.util.function.Predicate;

public class ChokeBreakDoorGoal extends InteractDoorGoal {

    private final Predicate<Difficulty> difficultyPredicate;

    protected int breakingTime;
    protected int previousBreakProgress = -1;
    protected boolean breakIronAndFences;
    protected boolean woodBlock;
    protected boolean gateBlock;

    public ChokeBreakDoorGoal(MobEntity entity, Predicate<Difficulty> diffPred, boolean breakIronAndFences) {
        super(entity);
        this.difficultyPredicate = diffPred;
        this.breakIronAndFences = breakIronAndFences;
    }

    public boolean shouldExecute() {
        //From super
        if (!this.entity.collidedHorizontally) {
            return false;
        } else {

            //AH CHANGE DEBUG OFF
            /*
            if(this.entity.getCustomName() != null) // && this.entity.getCustomName().getString().equals("Chuck"))
            {
                System.out.println("ChokeBreakDoorGoal shouldExecute");
            }
             */

            GroundPathNavigator groundpathnavigator = (GroundPathNavigator)this.entity.getNavigator();
            Path path = groundpathnavigator.getPath();
            if (path != null && !path.isFinished() && groundpathnavigator.getEnterDoors()) {

                boolean bGood = false;
                //AH CHANGE
                for(int i = Math.max(0, path.getCurrentPathIndex()- 2); i < Math.min(path.getCurrentPathIndex() + 2, path.getCurrentPathLength()); ++i) {
                    //for(int i = 0; i < Math.min(path.getCurrentPathIndex() + 2, path.getCurrentPathLength()); ++i) {

                    PathPoint pathpoint = path.getPathPointFromIndex(i);
                    this.doorPosition = new BlockPos(pathpoint.x, pathpoint.y, pathpoint.z);
                    //this.doorPosition = new BlockPos(pathpoint.x, pathpoint.y + 1, pathpoint.z);
                    if (!(this.entity.getDistanceSq((double)this.doorPosition.getX(), this.entity.getPosY(), (double)this.doorPosition.getZ()) > 2.25)) {
                        this.doorInteract = canInteractDoorGate(this.entity.world, this.doorPosition);
                        if (this.doorInteract) {
                            bGood = true;
                            break;
                        }
                    }
                }

                if(!bGood)
                {
                    this.doorPosition = new BlockPos(this.entity);
                    this.doorInteract = canInteractDoorGate(this.entity.world, this.doorPosition);
                }
                //return this.doorInteract;
            } else {
                return false;
            }
        }

        if (!doorInteract) {
            return false;
        } else if (!this.entity.world.getGameRules().getBoolean(GameRules.MOB_GRIEFING)) {
            return false;
        } else {
            if(this.checkDifficulty(this.entity.world.getDifficulty()) && !this.canDestroy()) {
                if (!gateBlock && !this.entity.isChild()) {
                    this.doorPosition = this.doorPosition.up(); //hit the top of the door
                }

                return true;
            }
            else
            {
                return false;
            }
        }
    }

    public void startExecuting() {
        super.startExecuting();
        this.breakingTime = 0;
    }

    public boolean shouldContinueExecuting() {
        return this.breakingTime <= 240 && !this.canDestroy() && this.doorPosition.withinDistance(this.entity.getPositionVec(), 2.0D)
                && this.checkDifficulty(this.entity.world.getDifficulty());
    }

    public void resetTask() {
        super.resetTask();
        this.entity.world.sendBlockBreakProgress(this.entity.getEntityId(), this.doorPosition, -1);
    }

    public void tick() {
        super.tick();
        if (this.entity.getRNG().nextInt(20) == 0) {

            if(woodBlock) {
                this.entity.world.playEvent(1019, this.doorPosition, 0);
            }
            else
            {
                this.entity.world.playEvent(1020, this.doorPosition, 0);
            }

            if (!this.entity.isSwingInProgress) {
                this.entity.swingArm(this.entity.getActiveHand());
            }
        }

        ++this.breakingTime;
        int i = (int)((float)this.breakingTime / (float)240 * 10.0F);
        if (i != this.previousBreakProgress) {
            this.entity.world.sendBlockBreakProgress(this.entity.getEntityId(), this.doorPosition, i);
            this.previousBreakProgress = i;
        }

        if (this.breakingTime == 240 && this.checkDifficulty(this.entity.world.getDifficulty())) {
            this.entity.world.removeBlock(this.doorPosition, false);

            if(woodBlock) {
                this.entity.world.playEvent(1021, this.doorPosition, 0);
            }
            else
            {
                this.entity.world.playEvent(7272, this.doorPosition, 0);
            }
            this.entity.world.playEvent(2001, this.doorPosition, Block.getStateId(this.entity.world.getBlockState(this.doorPosition)));
        }

    }

    @Override
    protected boolean canDestroy() {
        if (!this.doorInteract) {
            return false;
        } else {
            BlockState blockstate = this.entity.world.getBlockState(this.doorPosition);
            if (blockstate.getBlock() instanceof DoorBlock) {
                return blockstate.get(DoorBlock.OPEN);
            } else if (blockstate.getBlock() instanceof FenceGateBlock) {
                return blockstate.get(FenceGateBlock.OPEN);
            }
            else
            {
                this.doorInteract = false;
                return false;
            }
        }
    }

    @Override
    protected void toggleDoor(boolean open) {
        if (this.doorInteract) {
            BlockState blockstate = this.entity.world.getBlockState(this.doorPosition);
            if (blockstate.getBlock() instanceof DoorBlock) {
                ((DoorBlock)blockstate.getBlock()).toggleDoor(this.entity.world, this.doorPosition, open);
            }
            else if (blockstate.getBlock() instanceof FenceGateBlock) {
                ((FenceGateBlock)blockstate.getBlock()).toggleGate(this.entity.world, this.doorPosition, open);
            }
        }
    }

    private boolean checkDifficulty(Difficulty difficulty) {
        return this.difficultyPredicate.test(difficulty);
    }

    public boolean canInteractDoorGate(World world, BlockPos pos) {
        BlockState blockstate = world.getBlockState(pos);

        if(!breakIronAndFences)
        {
            boolean b = blockstate.getBlock() instanceof DoorBlock && blockstate.getMaterial() == Material.WOOD;
            if(b)
            {
                woodBlock = true;
                gateBlock = false;
            }
            return b;
        }
        else
        {
            if(blockstate.getBlock() instanceof DoorBlock)
            {
                boolean b = blockstate.getMaterial() == Material.IRON;
                if(b) {
                    woodBlock = false;
                    gateBlock = false;
                }

                return b;
            }
            else
            {
                boolean b = blockstate.getBlock() instanceof FenceGateBlock;
                if(b) {
                    woodBlock = true;
                    gateBlock = true;
                }

                return b;
            }
        }
    }
}
