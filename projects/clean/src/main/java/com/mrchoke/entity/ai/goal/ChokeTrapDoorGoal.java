package com.mrchoke.entity.ai.goal;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.MobEntity;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ChokeTrapDoorGoal extends InteractTrapDoorGoal {
    private final boolean closeDoor;
    private int closeDoorTemporisation;

    protected boolean openIron;
    protected boolean woodBlock;

    public ChokeTrapDoorGoal(MobEntity entitylivingIn, boolean shouldClose, boolean openIron) {
        super(entitylivingIn);
        this.entity = entitylivingIn;
        this.closeDoor = shouldClose;
        this.openIron = openIron;
    }

    @Override
    public boolean shouldExecute() {
        if (!this.entity.collidedVertically) {
            return false;
        } else {

            //AH CHANGE DEBUG OFF
            /*
            if(this.entity.getCustomName() != null) // && this.entity.getCustomName().getString().equals("Chuck"))
            {
                System.out.println("ChokeTrapDoorGoal shouldExecute");
            }
             */

            GroundPathNavigator groundpathnavigator = (GroundPathNavigator)this.entity.getNavigator();
            Path path = groundpathnavigator.getPath();
            if (path != null && !path.isFinished() && groundpathnavigator.getEnterDoors()) {
                for(int i = Math.max(0, path.getCurrentPathIndex()- 2); i < Math.min(path.getCurrentPathIndex() + 2, path.getCurrentPathLength()); ++i) {
                    PathPoint pathpoint = path.getPathPointFromIndex(i);
                    this.doorPosition = new BlockPos(pathpoint.x, pathpoint.y, pathpoint.z);
                    if (!(this.entity.getDistanceSq((double)this.doorPosition.getX(), this.doorPosition.getY(), (double)this.doorPosition.getZ()) > 3.0D)) {  //is 2.25D in DoorInteract
                        this.doorInteract = canInteractTrapChoke(this.entity.world, this.doorPosition);
                        if (this.doorInteract) {
                            return true;
                        }
                    }
                }

                this.doorPosition = new BlockPos(this.entity);
                this.doorInteract = canInteractTrapChoke(this.entity.world, this.doorPosition);
                return this.doorInteract;
            } else {
                return false;
            }
        }
    }

    public boolean shouldContinueExecuting() {
        return this.closeDoor && this.closeDoorTemporisation > 0 && super.shouldContinueExecuting();
    }

    public void startExecuting() {
        if (this.entity.isChild()) {
            this.closeDoorTemporisation = 15;
        } else {
            this.closeDoorTemporisation = 30;
        }

        this.toggleDoor(true);
    }

    public void resetTask() {
        if(closeDoor) {
            this.toggleDoor(false);
        }
    }

    public void tick() {
        --this.closeDoorTemporisation;
        super.tick();
    }

    public boolean canInteractTrapChoke(World world, BlockPos pos) {
        BlockState blockstate = world.getBlockState(pos);

        if(!openIron)
        {
            return BlockTags.WOODEN_TRAPDOORS.contains(blockstate.getBlock());
        }
        else
        {
            return blockstate.getBlock() == Blocks.IRON_TRAPDOOR;
        }
    }

}
