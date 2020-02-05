package com.mrchoke.entity.ai.goal;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.TrapDoorBlock;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

//AH CHANGE NEW CLASS ******
public abstract class InteractTrapDoorGoal extends Goal {
    protected MobEntity entity;
    protected BlockPos doorPosition = BlockPos.ZERO;
    protected boolean doorInteract;
    private boolean hasStoppedDoorInteraction;
    private float entityPositionY;

    public InteractTrapDoorGoal(MobEntity entityIn) {
        this.entity = entityIn;
        if (!(entityIn.getNavigator() instanceof GroundPathNavigator)) {
            throw new IllegalArgumentException("Unsupported mob type for DoorInteractGoal");
        }
    }

    protected boolean canDestroy() {
        if (!this.doorInteract) {
            return false;
        } else {
            BlockState blockstate = this.entity.world.getBlockState(this.doorPosition);
            if (!BlockTags.TRAPDOORS.contains(blockstate.getBlock())) {
                this.doorInteract = false;
                return false;
            } else {
                return blockstate.get(TrapDoorBlock.OPEN);
            }
        }
    }

    protected void toggleDoor(boolean open) {
        if (this.doorInteract) {
            BlockState blockstate = this.entity.world.getBlockState(this.doorPosition);

            //AH CHANGE START ******
            Block block = blockstate.getBlock();
            if(block instanceof TrapDoorBlock)
            {
                ((TrapDoorBlock)blockstate.getBlock()).toggleDoor(this.entity.world, this.doorPosition, open);
            }
        }
    }

    public boolean shouldExecute() {
        if (!this.entity.collidedVertically) {
            return false;
        } else {

            //AH CHANGE DEBUG OFF
            /*
            if(this.entity.getCustomName() != null) // && this.entity.getCustomName().getString().equals("Chuck"))
            {
                System.out.println("OpenTrapDoorGoal shouldExecute");
            }
             */

            GroundPathNavigator groundpathnavigator = (GroundPathNavigator)this.entity.getNavigator();
            Path path = groundpathnavigator.getPath();
            if (path != null && !path.isFinished() && groundpathnavigator.getEnterDoors()) {
                for(int i = Math.max(0, path.getCurrentPathIndex()- 2); i < Math.min(path.getCurrentPathIndex() + 2, path.getCurrentPathLength()); ++i) {
                    PathPoint pathpoint = path.getPathPointFromIndex(i);
                    this.doorPosition = new BlockPos(pathpoint.x, pathpoint.y, pathpoint.z);
                    if (!(this.entity.getDistanceSq((double)this.doorPosition.getX(), this.doorPosition.getY(), (double)this.doorPosition.getZ()) > 3.0D)) {  //is 2.25D in DoorInteract
                        this.doorInteract = canInteractTrapDoor(this.entity.world, this.doorPosition);
                        if (this.doorInteract) {
                            return true;
                        }
                    }
                }

                this.doorPosition = new BlockPos(this.entity);
                this.doorInteract = canInteractTrapDoor(this.entity.world, this.doorPosition);
                return this.doorInteract;
            } else {
                return false;
            }
        }
    }

    public boolean shouldContinueExecuting() {
        return !this.hasStoppedDoorInteraction;
    }

    public void startExecuting() {
        this.hasStoppedDoorInteraction = false;
        this.entityPositionY = (float)((double)((float)this.doorPosition.getY() + 0.5F) - this.entity.getPosY());
    }

    public void tick() {
        float f1 = (float)((double)((float)this.doorPosition.getY() + 0.5F) - this.entity.getPosY());
        float f3 = this.entityPositionY * f1;

        //AH CHANGE DEBUG OFF
        /*
        if(this.entity instanceof HuskEntity && this.entity.getCustomName() != null) // && this.entity.getCustomName().getString().equals("Chuck"))
        {
            System.out.println("InteractTrapDoorGoal tick.  f3=" + f3);
        }
         */

        if (f3 < 0.0F) {
            this.hasStoppedDoorInteraction = true;
        }
    }

    public static boolean canInteractTrapDoor(World world, BlockPos pos) {
        BlockState blockstate = world.getBlockState(pos);
        return BlockTags.WOODEN_TRAPDOORS.contains(blockstate.getBlock());
    }

}
