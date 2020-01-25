package com.mrchoke.entity.ai.goal;

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
import net.minecraft.world.World;

public class ChokeOpenDoorGoal extends InteractDoorGoal {

    private final boolean closeDoor;
    private int closeDoorTemporisation;

    protected boolean breakIronAndFences;
    protected boolean woodBlock;
    protected boolean gateBlock;

    public ChokeOpenDoorGoal(MobEntity entitylivingIn, boolean shouldClose, boolean breakIronAndFences) {
        super(entitylivingIn);
        this.entity = entitylivingIn;
        this.closeDoor = shouldClose;
        this.breakIronAndFences = breakIronAndFences;
    }

    public boolean shouldExecute()
    {
        //From super
        if (!this.entity.collidedHorizontally) {
            return false;
        } else {

            //AH CHANGE DEBUG OFF
            /*
            if(this.entity.getCustomName() != null) // && this.entity.getCustomName().getString().equals("Chuck"))
            {
                System.out.println("ChokeOpenDoorGoal shouldExecute");
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
        if(!gateBlock) {
            if (entity.isChild()) {
                this.closeDoorTemporisation = 20;
            } else {
                this.closeDoorTemporisation = 30;   //was 20
            }
        }
        else
        {
            this.closeDoorTemporisation = 16;
        }

        this.toggleDoor(true);
    }

    public void resetTask() {

        //AH CHANGE - THIS LOOKS BUGGED..  WANT TO CHECK for closeDoor before it closes
        if(closeDoor)
        {
            this.toggleDoor(false);
        }
        //Vanilla
        //this.toggleDoor(false);
    }

    public void tick() {
        --this.closeDoorTemporisation;
        super.tick();
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
