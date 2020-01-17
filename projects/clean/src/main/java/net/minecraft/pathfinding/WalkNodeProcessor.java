package net.minecraft.pathfinding;

import com.google.common.collect.Sets;
import java.util.EnumSet;
import java.util.Set;
import javax.annotation.Nullable;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.merchant.villager.AbstractVillagerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.Region;

public class WalkNodeProcessor extends NodeProcessor {
   protected float avoidsWater;

   //AH CHANGE ADD:  Fix entityWidth > 1 pathfing bug (Golems and other things failed to path down from North or West)
   private Direction dirFacing;   //Set only for some getPathNodeType calls in getSafePoint

   public void init(Region region, MobEntity entity) {
      super.init(region, entity);
      this.avoidsWater = entity.getPathPriority(PathNodeType.WATER);
   }

   public void postProcess() {
      this.entity.setPathPriority(PathNodeType.WATER, this.avoidsWater);
      super.postProcess();
   }

   public PathPoint getStart() {
      int i;
      if (this.getCanSwim() && this.entity.isInWater()) {
         i = MathHelper.floor(this.entity.getPosY());
         BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable(this.entity.getPosX(), (double)i, this.entity.getPosZ());

         for(BlockState blockstate = this.blockaccess.getBlockState(blockpos$mutable); blockstate.getBlock() == Blocks.WATER || blockstate.getFluidState() == Fluids.WATER.getStillFluidState(false); blockstate = this.blockaccess.getBlockState(blockpos$mutable)) {
            ++i;
            blockpos$mutable.setPos(this.entity.getPosX(), (double)i, this.entity.getPosZ());
         }

         --i;
      } else if (this.entity.onGround) {
         i = MathHelper.floor(this.entity.getPosY() + 0.5D);
      } else {

         //AH CHANGE ******
         BlockPos blockpos;
         boolean bOverride = false;
         i = 0;
         for(blockpos = new BlockPos(this.entity); (this.blockaccess.getBlockState(blockpos).isAir() ||
                 this.blockaccess.getBlockState(blockpos).allowsMovement(this.blockaccess, blockpos, PathType.LAND)) && blockpos.getY() > 0; blockpos = blockpos.down()) {

            BlockState state = this.blockaccess.getBlockState(blockpos);
            Block block = state.getBlock();
            if (block.isLadder(state, this.blockaccess, blockpos)) {
               i = blockpos.getY();
               bOverride = true;
               break;
            }

         }

         if(!bOverride) {
            i = blockpos.up().getY();
         }
         /*
         BlockPos blockpos;
         for(blockpos = new BlockPos(this.entity); (this.blockaccess.getBlockState(blockpos).isAir() || this.blockaccess.getBlockState(blockpos).allowsMovement(this.blockaccess, blockpos, PathType.LAND)) && blockpos.getY() > 0; blockpos = blockpos.down()) {
            ;
         }

         i = blockpos.up().getY();
          */
         //AH END ******
      }

      BlockPos blockpos2 = new BlockPos(this.entity);
      PathNodeType pathnodetype1 = this.getPathNodeType(this.entity, blockpos2.getX(), i, blockpos2.getZ());
      if (this.entity.getPathPriority(pathnodetype1) < 0.0F) {
         Set<BlockPos> set = Sets.newHashSet();
         set.add(new BlockPos(this.entity.getBoundingBox().minX, (double)i, this.entity.getBoundingBox().minZ));
         set.add(new BlockPos(this.entity.getBoundingBox().minX, (double)i, this.entity.getBoundingBox().maxZ));
         set.add(new BlockPos(this.entity.getBoundingBox().maxX, (double)i, this.entity.getBoundingBox().minZ));
         set.add(new BlockPos(this.entity.getBoundingBox().maxX, (double)i, this.entity.getBoundingBox().maxZ));

         for(BlockPos blockpos1 : set) {
            PathNodeType pathnodetype = this.getPathNodeType(this.entity, blockpos1);
            if (this.entity.getPathPriority(pathnodetype) >= 0.0F) {
               return this.openPoint(blockpos1.getX(), blockpos1.getY(), blockpos1.getZ());
            }
         }
      }

      return this.openPoint(blockpos2.getX(), i, blockpos2.getZ());
   }

   //AH CHANGE REFACTOR
   public FlaggedPathPoint createFlaggedPathPoint(double x, double y, double z) {
   //public FlaggedPathPoint func_224768_a(double p_224768_1_, double p_224768_3_, double p_224768_5_) {
      return new FlaggedPathPoint(this.openPoint(MathHelper.floor(x), MathHelper.floor(y), MathHelper.floor(z)));
   }

   //AH CHANGE REFACTOR
   public int findPathOptions(PathPoint[] pathOptions, PathPoint targetPoint) {
   //public int func_222859_a(PathPoint[] p_222859_1_, PathPoint p_222859_2_) {
      int i = 0;
      int j = 0;

      PathNodeType pathnodetype = this.getPathNodeType(this.entity, targetPoint.x, targetPoint.y + 1, targetPoint.z);
      if (this.entity.getPathPriority(pathnodetype) >= 0.0F) {
         PathNodeType pathnodetype1 = this.getPathNodeType(this.entity, targetPoint.x, targetPoint.y, targetPoint.z);
         if (pathnodetype1 == PathNodeType.STICKY_HONEY) {
            j = 0;
         } else {
            j = MathHelper.floor(Math.max(1.0F, this.entity.stepHeight));
         }
      }

      //AH CHANGE NEW ******
      boolean isPassenger = this.entity.isPassenger();

      //AH CHANGE DEBUG OFF
      /*
      if(this.entity.getCustomName() != null) // && this.entity.getCustomName().getString().equals("Chuck"))
      {
         System.out.println("in findPathOptions, targetPos=" + targetPoint.toString());
      }
       */

      boolean canBreakTrapDoors = this.canEnterDoors && this.canOpenDoors;

      boolean bOnLadderAbove = false;
      boolean bLadderBelow = false;
      boolean bOnLadder = false;
      if(!isPassenger && this.entitySizeX <= 1.0D) {
         BlockPos blockposC = new BlockPos(targetPoint.x, targetPoint.y, targetPoint.z);
         BlockState iblockstateC = this.blockaccess.getBlockState(blockposC);
         bOnLadder = isLadderPathable(this.entity.world, iblockstateC, blockposC, true, canBreakTrapDoors);
         if (!bOnLadder) {
            //Also check block up one for ladder.  Handles mob in the water
            blockposC = blockposC.up();
            iblockstateC = this.blockaccess.getBlockState(blockposC);
            bOnLadderAbove = isLadderPathable(this.entity.world, iblockstateC, blockposC, true, canBreakTrapDoors);

            BlockPos blockposD = (new BlockPos(targetPoint.x, targetPoint.y, targetPoint.z)).down();
            BlockState iblockstateD = this.blockaccess.getBlockState(blockposD);
            bLadderBelow = isLadderPathable(this.entity.world, iblockstateD, blockposD, true, canBreakTrapDoors);
         }
      }
      //AH END ******

      double d0 = getGroundY(this.blockaccess, new BlockPos(targetPoint.x, targetPoint.y, targetPoint.z));
      PathPoint pathpoint = this.getSafePoint(targetPoint.x, targetPoint.y, targetPoint.z + 1, j, d0, Direction.SOUTH, bLadderBelow);
      if (pathpoint != null && !pathpoint.visited && pathpoint.costMalus >= 0.0F) {
         pathOptions[i++] = pathpoint;
      }

      PathPoint pathpoint1 = this.getSafePoint(targetPoint.x - 1, targetPoint.y, targetPoint.z, j, d0, Direction.WEST, bLadderBelow);
      if (pathpoint1 != null && !pathpoint1.visited && pathpoint1.costMalus >= 0.0F) {
         pathOptions[i++] = pathpoint1;
      }

      PathPoint pathpoint2 = this.getSafePoint(targetPoint.x + 1, targetPoint.y, targetPoint.z, j, d0, Direction.EAST, bLadderBelow);
      if (pathpoint2 != null && !pathpoint2.visited && pathpoint2.costMalus >= 0.0F) {
         pathOptions[i++] = pathpoint2;
      }

      PathPoint pathpoint3 = this.getSafePoint(targetPoint.x, targetPoint.y, targetPoint.z - 1, j, d0, Direction.NORTH, bLadderBelow);
      if (pathpoint3 != null && !pathpoint3.visited && pathpoint3.costMalus >= 0.0F) {
         pathOptions[i++] = pathpoint3;
      }

      //AH CHANGE NEW ******
      PathPoint pathpointU;
      if (bOnLadder || bOnLadderAbove) {
         pathpointU = this.getSafePointVert(targetPoint.x, targetPoint.y + 1, targetPoint.z);
         if (pathpointU != null && !pathpointU.visited && pathpointU.costMalus >= 0.0F) {
            pathOptions[i++] = pathpointU;
         }
      }

      PathPoint pathpointD;
      if (bOnLadder|| bLadderBelow) {
         pathpointD = this.getSafePointVert(targetPoint.x, targetPoint.y - 1, targetPoint.z);
         if (pathpointD != null && !pathpointD.visited && pathpointD.costMalus >= 0.0F) {
            pathOptions[i++] = pathpointD;
         }
      }
      //AH CHANGE END ******


      PathPoint pathpoint4 = this.getSafePoint(targetPoint.x - 1, targetPoint.y, targetPoint.z - 1, j, d0, Direction.NORTH, bLadderBelow);
      if (this.pathPointDiagCheck(targetPoint, pathpoint1, pathpoint3, pathpoint4)) {
         pathOptions[i++] = pathpoint4;
      }

      PathPoint pathpoint5 = this.getSafePoint(targetPoint.x + 1, targetPoint.y, targetPoint.z - 1, j, d0, Direction.NORTH, bLadderBelow);
      if (this.pathPointDiagCheck(targetPoint, pathpoint2, pathpoint3, pathpoint5)) {
         pathOptions[i++] = pathpoint5;
      }

      PathPoint pathpoint6 = this.getSafePoint(targetPoint.x - 1, targetPoint.y, targetPoint.z + 1, j, d0, Direction.SOUTH, bLadderBelow);
      if (this.pathPointDiagCheck(targetPoint, pathpoint1, pathpoint, pathpoint6)) {
         pathOptions[i++] = pathpoint6;
      }

      PathPoint pathpoint7 = this.getSafePoint(targetPoint.x + 1, targetPoint.y, targetPoint.z + 1, j, d0, Direction.SOUTH, bLadderBelow);
      if (this.pathPointDiagCheck(targetPoint, pathpoint2, pathpoint, pathpoint7)) {
         pathOptions[i++] = pathpoint7;
      }

      return i;
   }

   //AH CHNAGE NEW ******
   @Nullable
   protected PathPoint getSafePointVert(int x, int y, int z) {
      PathPoint pathpoint = null;
      PathNodeType pathnodetype = this.getPathNodeType(this.entity, x, y, z);
      float f = this.entity.getPathPriority(pathnodetype);
      double d1 = (double)this.entity.getWidth() / 2.0D;
      if (f >= 0.0F) {
         pathpoint = this.openPoint(x, y, z);
         pathpoint.nodeType = pathnodetype;
         pathpoint.costMalus = Math.max(pathpoint.costMalus, f);
      }

      if (pathnodetype == PathNodeType.WALKABLE) {
         return pathpoint;
      } else {
         if (pathnodetype == PathNodeType.OPEN) {
            AxisAlignedBB axisalignedbb1 = new AxisAlignedBB((double)x - d1 + 0.5D, (double)y + 0.001D, (double)z - d1 + 0.5D, (double)x + d1 + 0.5D,
                    (double)((float)y + this.entity.getHeight()), (double)z + d1 + 0.5D);
            if (!this.entity.world.isCollisionBoxesEmpty((Entity)null, axisalignedbb1)) {
               return null;
            }

            if (this.entity.getWidth() >= 1.0F) {
               PathNodeType pathnodetype1 = this.getPathNodeType(this.entity, x, y - 1, z);
               if (pathnodetype1 == PathNodeType.BLOCKED) {
                  pathpoint = this.openPoint(x, y, z);
                  pathpoint.nodeType = PathNodeType.WALKABLE;
                  pathpoint.costMalus = Math.max(pathpoint.costMalus, f);
                  return pathpoint;
               }
            }
         }

         return pathpoint;
      }
   }
   //AH CHANGE END ******

   //AH CHANGE REFACTOR
   private boolean pathPointDiagCheck(PathPoint targetPoint, @Nullable PathPoint pathpoint1, @Nullable PathPoint pathpoint2, @Nullable PathPoint pathpoint3) {
   //private boolean func_222860_a(PathPoint p_222860_1_, @Nullable PathPoint p_222860_2_, @Nullable PathPoint p_222860_3_, @Nullable PathPoint p_222860_4_) {
      if (pathpoint3 != null && pathpoint2 != null && pathpoint1 != null) {
         if (pathpoint3.visited) {
            return false;
         } else if (pathpoint2.y <= targetPoint.y && pathpoint1.y <= targetPoint.y) {
            return pathpoint3.costMalus >= 0.0F && (pathpoint2.y < targetPoint.y || pathpoint2.costMalus >= 0.0F) && (pathpoint1.y < targetPoint.y || pathpoint1.costMalus >= 0.0F);
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   //AH CHANGE
   //public static double getGroundY(NodeProcessor nodeProcessor, IBlockReader p_197682_0_, BlockPos pos) {
   public static double getGroundY(IBlockReader blockReader, BlockPos pos) {
      //AH CHANGE ******
      BlockPos blockpos;

      BlockState state = blockReader.getBlockState(pos);
      if (isLadderPathable(blockReader, state, pos, false, false)) {
         return pos.getY() + 0.5D;
      }

      blockpos = pos.down();

      VoxelShape voxelshape = blockReader.getBlockState(blockpos).getCollisionShape(blockReader, blockpos);
      return (double)blockpos.getY() + (voxelshape.isEmpty() ? 0.0D : voxelshape.getEnd(Direction.Axis.Y));

      //Vanilla:
      /*
      BlockPos blockpos = pos.down();
      VoxelShape voxelshape = blockReader.getBlockState(blockpos).getCollisionShape(blockReader, blockpos);
      return (double)blockpos.getY() + (voxelshape.isEmpty() ? 0.0D : voxelshape.getEnd(Direction.Axis.Y));
       */
      //AH END ******
   }

   @Nullable
   //AH CHANGE
   private PathPoint getSafePoint(int x, int y, int z, int stepHeight, double groundYIn, Direction facing, boolean bOnLadderBelow) {
   //private PathPoint getSafePoint(int x, int y, int z, int stepHeight, double groundYIn, Direction facing) {
      PathPoint pathpoint = null;
      BlockPos blockpos = new BlockPos(x, y, z);
      double d0 = getGroundY(this.blockaccess, blockpos);
      if (d0 - groundYIn > 1.125D) {
         return null;
      } else {

         //AH CHANGE ADD ******
         this.dirFacing = facing;
         try {
         //AH CHANGE END ******

            PathNodeType pathnodetype = this.getPathNodeType(this.entity, x, y, z);
            float f = this.entity.getPathPriority(pathnodetype);
            double d1 = (double) this.entity.getWidth() / 2.0D;
            if (f >= 0.0F) {
               pathpoint = this.openPoint(x, y, z);
               pathpoint.nodeType = pathnodetype;
               pathpoint.costMalus = Math.max(pathpoint.costMalus, f);
            }

            if (pathnodetype == PathNodeType.WALKABLE) {
               return pathpoint;
            } else {
               //AH CHANGE
               if ((pathpoint == null || pathpoint.costMalus < 0.0F) && stepHeight > 0 && pathnodetype != PathNodeType.FENCE && pathnodetype != PathNodeType.LILYPAD && !bOnLadderBelow) {
                  pathpoint = this.getSafePoint(x, y + 1, z, stepHeight - 1, groundYIn, facing, bOnLadderBelow);
                  //if ((pathpoint == null || pathpoint.costMalus < 0.0F) && stepHeight > 0 && pathnodetype != PathNodeType.FENCE && pathnodetype != PathNodeType.TRAPDOOR) {
                  //pathpoint = this.getSafePoint(x, y + 1, z, stepHeight - 1, groundYIn, facing);

                  if (pathpoint != null && (pathpoint.nodeType == PathNodeType.OPEN || pathpoint.nodeType == PathNodeType.WALKABLE) && this.entity.getWidth() < 1.0F) {
                     double d2 = (double) (x - facing.getXOffset()) + 0.5D;
                     double d3 = (double) (z - facing.getZOffset()) + 0.5D;
                     AxisAlignedBB axisalignedbb = new AxisAlignedBB(d2 - d1, getGroundY(this.blockaccess, new BlockPos(d2, (double) (y + 1), d3)) + 0.001D, d3 - d1, d2 + d1, (double) this.entity.getHeight() + getGroundY(this.blockaccess, new BlockPos(pathpoint.x, pathpoint.y, pathpoint.z)) - 0.002D, d3 + d1);
                     if (!this.blockaccess.isCollisionBoxesEmpty(this.entity, axisalignedbb)) {
                        pathpoint = null;
                     }
                  }
               }

               if (pathnodetype == PathNodeType.WATER && !this.getCanSwim()) {
                  if (this.getPathNodeType(this.entity, x, y - 1, z) != PathNodeType.WATER) {
                     return pathpoint;
                  }

                  while (y > 0) {
                     --y;
                     pathnodetype = this.getPathNodeType(this.entity, x, y, z);
                     if (pathnodetype != PathNodeType.WATER) {
                        return pathpoint;
                     }

                     pathpoint = this.openPoint(x, y, z);
                     pathpoint.nodeType = pathnodetype;
                     pathpoint.costMalus = Math.max(pathpoint.costMalus, this.entity.getPathPriority(pathnodetype));
                  }
               }

               if (pathnodetype == PathNodeType.OPEN) {
                  AxisAlignedBB axisalignedbb1 = new AxisAlignedBB((double) x - d1 + 0.5D, (double) y + 0.001D, (double) z - d1 + 0.5D, (double) x + d1 + 0.5D, (double) ((float) y + this.entity.getHeight()), (double) z + d1 + 0.5D);
                  if (!this.blockaccess.isCollisionBoxesEmpty(this.entity, axisalignedbb1)) {
                     return null;
                  }

                  if (this.entity.getWidth() >= 1.0F) {
                     PathNodeType pathnodetype1 = this.getPathNodeType(this.entity, x, y - 1, z);
                     if (pathnodetype1 == PathNodeType.BLOCKED) {
                        pathpoint = this.openPoint(x, y, z);
                        pathpoint.nodeType = PathNodeType.WALKABLE;
                        pathpoint.costMalus = Math.max(pathpoint.costMalus, f);
                        return pathpoint;
                     }
                  }

                  int i = 0;
                  int j = y;

                  while (pathnodetype == PathNodeType.OPEN) {
                     --y;
                     if (y < 0) {
                        PathPoint pathpoint2 = this.openPoint(x, j, z);
                        pathpoint2.nodeType = PathNodeType.BLOCKED;
                        pathpoint2.costMalus = -1.0F;
                        return pathpoint2;
                     }

                     PathPoint pathpoint1 = this.openPoint(x, y, z);
                     if (i++ >= this.entity.getMaxFallHeight()) {
                        pathpoint1.nodeType = PathNodeType.BLOCKED;
                        pathpoint1.costMalus = -1.0F;
                        return pathpoint1;
                     }

                     pathnodetype = this.getPathNodeType(this.entity, x, y, z);
                     f = this.entity.getPathPriority(pathnodetype);
                     if (pathnodetype != PathNodeType.OPEN && f >= 0.0F) {
                        pathpoint = pathpoint1;
                        pathpoint1.nodeType = pathnodetype;
                        pathpoint1.costMalus = Math.max(pathpoint1.costMalus, f);
                        break;
                     }

                     if (f < 0.0F) {
                        pathpoint1.nodeType = PathNodeType.BLOCKED;
                        pathpoint1.costMalus = -1.0F;
                        return pathpoint1;
                     }
                  }
               }

               return pathpoint;
            }
         //AH CHANGE ADDED finally
         } finally {
            this.dirFacing = null;
         }
         //AH CHANGE END
      }
   }

   public PathNodeType getPathNodeType(IBlockReader blockaccessIn, int x, int y, int z, MobEntity entitylivingIn, int xSize, int ySize, int zSize, boolean canBreakDoorsIn, boolean canEnterDoorsIn) {
      EnumSet<PathNodeType> enumset = EnumSet.noneOf(PathNodeType.class);
      PathNodeType pathnodetype = PathNodeType.BLOCKED;
      double d0 = (double)entitylivingIn.getWidth() / 2.0D;
      BlockPos blockpos = new BlockPos(entitylivingIn);
      pathnodetype = this.getPathNodeType(blockaccessIn, x, y, z, xSize, ySize, zSize, canBreakDoorsIn, canEnterDoorsIn, enumset, pathnodetype, blockpos);
      if (enumset.contains(PathNodeType.FENCE)) {
         return PathNodeType.FENCE;
      } else {
         PathNodeType pathnodetype1 = PathNodeType.BLOCKED;

         for(PathNodeType pathnodetype2 : enumset) {
            if (entitylivingIn.getPathPriority(pathnodetype2) < 0.0F) {
               return pathnodetype2;
            }

            if (entitylivingIn.getPathPriority(pathnodetype2) >= entitylivingIn.getPathPriority(pathnodetype1)) {
               pathnodetype1 = pathnodetype2;
            }
         }

         return pathnodetype == PathNodeType.OPEN && entitylivingIn.getPathPriority(pathnodetype1) == 0.0F ? PathNodeType.OPEN : pathnodetype1;
      }
   }

   public PathNodeType getPathNodeType(IBlockReader p_193577_1_, int x, int y, int z, int xSize, int ySize, int zSize, boolean canOpenDoorsIn, boolean canEnterDoorsIn, EnumSet<PathNodeType> nodeTypeEnum, PathNodeType nodeType, BlockPos pos) {
      //AH CHANGE ******
      int[] xArr = new int[xSize];
      int[] zArr = new int[zSize];
      xArr[0] = x;
      zArr[0] = z;
      if(this.dirFacing != null)
      {
         for(int i = 1; i < xSize; ++i) {
            xArr[i] = xArr[i - 1] + dirFacing.getXOffset();
         }
         for(int i = 1; i < zSize; ++i) {
            zArr[i] = zArr[i - 1] + dirFacing.getZOffset();
         }
      }
      for(int i = 0; i < xSize; ++i) {
         for(int j = 0; j < ySize; ++j) {
            for(int k = 0; k < zSize; ++k) {
               int l = xArr[i];
               int i1 = j + y;
               int j1 = zArr[k];
               PathNodeType pathnodetype = this.getPathNodeType(p_193577_1_, l, i1, j1);
               pathnodetype = this.getPathNodeTypeWalkable(p_193577_1_, canOpenDoorsIn, canEnterDoorsIn, pos, pathnodetype);
               if (i == 0 && j == 0 && k == 0) {
                  nodeType = pathnodetype;
               }

               nodeTypeEnum.add(pathnodetype);
            }
         }
      }
      //AH CHANGE END ******
      //Vanilla
      /*
      for(int i = 0; i < xSize; ++i) {
         for(int j = 0; j < ySize; ++j) {
            for(int k = 0; k < zSize; ++k) {
               int l = i + x;
               int i1 = j + y;
               int j1 = k + z;
               PathNodeType pathnodetype = this.getPathNodeType(p_193577_1_, l, i1, j1);
               pathnodetype = this.getPathNodeTypeWalkable(p_193577_1_, canOpenDoorsIn, canEnterDoorsIn, pos, pathnodetype);
               if (i == 0 && j == 0 && k == 0) {
                  nodeType = pathnodetype;
               }

               nodeTypeEnum.add(pathnodetype);
            }
         }
      }
      */

      return nodeType;
   }

   //AH CHANGE REFACTOR
   protected PathNodeType getPathNodeTypeWalkable(IBlockReader blockReader, boolean canOpenDoorsIn, boolean canEnterDoorsIn, BlockPos pos, PathNodeType pathNodeType) {
   //protected PathNodeType func_215744_a(IBlockReader p_215744_1_, boolean p_215744_2_, boolean p_215744_3_, BlockPos p_215744_4_, PathNodeType p_215744_5_) {

      if (pathNodeType == PathNodeType.DOOR_WOOD_CLOSED && canOpenDoorsIn && canEnterDoorsIn) {
         pathNodeType = PathNodeType.WALKABLE;
      }

      if (pathNodeType == PathNodeType.DOOR_OPEN && !canEnterDoorsIn) {
         pathNodeType = PathNodeType.BLOCKED;
      }

      //AH CHANGE ADD ******
      if (pathNodeType == PathNodeType.TRAPDOOR_CLOSED && (canOpenDoorsIn && canEnterDoorsIn)) {
         pathNodeType = PathNodeType.WALKABLE;
      }
      //AH CHANGE END ******

      if (pathNodeType == PathNodeType.RAIL && !(blockReader.getBlockState(pos).getBlock() instanceof AbstractRailBlock) && !(blockReader.getBlockState(pos.down()).getBlock() instanceof AbstractRailBlock)) {
         pathNodeType = PathNodeType.FENCE;
      }

      if (pathNodeType == PathNodeType.LEAVES) {
         pathNodeType = PathNodeType.BLOCKED;
      }

      return pathNodeType;
   }

   private PathNodeType getPathNodeType(MobEntity entitylivingIn, BlockPos pos) {
      return this.getPathNodeType(entitylivingIn, pos.getX(), pos.getY(), pos.getZ());
   }

   private PathNodeType getPathNodeType(MobEntity entitylivingIn, int x, int y, int z) {
      return this.getPathNodeType(this.blockaccess, x, y, z, entitylivingIn, this.entitySizeX, this.entitySizeY, this.entitySizeZ, this.getCanOpenDoors(), this.getCanEnterDoors());
   }

   public PathNodeType getPathNodeType(IBlockReader blockaccessIn, int x, int y, int z) {
      return getPathNodeTypeDamage(blockaccessIn, x, y, z);
   }

   //AH CHANGE REFACTOR
   public static PathNodeType getPathNodeTypeDamage(IBlockReader blockReader, int x, int y, int z) {
   //public static PathNodeType func_227480_b_(IBlockReader p_227480_0_, int p_227480_1_, int p_227480_2_, int p_227480_3_) {
      PathNodeType pathnodetype = getPathNodeTypeRaw(blockReader, x, y, z);
      if (pathnodetype == PathNodeType.OPEN && y >= 1) {
         Block block = blockReader.getBlockState(new BlockPos(x, y - 1, z)).getBlock();
         PathNodeType pathnodetype1 = getPathNodeTypeRaw(blockReader, x, y - 1, z);

         //AH CHANGE
         pathnodetype = pathnodetype1 != PathNodeType.WALKABLE && pathnodetype1 != PathNodeType.OPEN && pathnodetype1 != PathNodeType.LADDER &&
                  pathnodetype1 != PathNodeType.WATER && pathnodetype1 != PathNodeType.LAVA ? PathNodeType.WALKABLE : PathNodeType.OPEN;
         //pathnodetype = pathnodetype1 != PathNodeType.WALKABLE && pathnodetype1 != PathNodeType.OPEN && pathnodetype1 != PathNodeType.WATER &&
           //      pathnodetype1 != PathNodeType.LAVA ? PathNodeType.WALKABLE : PathNodeType.OPEN;

         if (pathnodetype1 == PathNodeType.DAMAGE_FIRE || block == Blocks.MAGMA_BLOCK || block == Blocks.CAMPFIRE) {
            pathnodetype = PathNodeType.DAMAGE_FIRE;
         }

         if (pathnodetype1 == PathNodeType.DAMAGE_CACTUS) {
            pathnodetype = PathNodeType.DAMAGE_CACTUS;
         }

         if (pathnodetype1 == PathNodeType.DAMAGE_OTHER) {
            pathnodetype = PathNodeType.DAMAGE_OTHER;
         }

         if (pathnodetype1 == PathNodeType.STICKY_HONEY) {
            pathnodetype = PathNodeType.STICKY_HONEY;
         }
      }

      if (pathnodetype == PathNodeType.WALKABLE) {
         pathnodetype = checkNeighborBlocks(blockReader, x, y, z, pathnodetype);
      }

      return pathnodetype;
   }

   public static PathNodeType checkNeighborBlocks(IBlockReader p_193578_0_, int blockaccessIn, int x, int y, PathNodeType z) {
      try (BlockPos.PooledMutable blockpos$pooledmutable = BlockPos.PooledMutable.retain()) {
         for(int i = -1; i <= 1; ++i) {
            for(int j = -1; j <= 1; ++j) {
               for(int k = -1; k <= 1; ++k) {
                  if (i != 0 || k != 0) {
                     Block block = p_193578_0_.getBlockState(blockpos$pooledmutable.setPos(i + blockaccessIn, j + x, k + y)).getBlock();
                     if (block == Blocks.CACTUS) {
                        z = PathNodeType.DANGER_CACTUS;
                     } else if (block != Blocks.FIRE && block != Blocks.LAVA) {
                        if (block == Blocks.SWEET_BERRY_BUSH) {
                           z = PathNodeType.DANGER_OTHER;
                        }
                     } else {
                        z = PathNodeType.DANGER_FIRE;
                     }
                  }
               }
            }
         }
      }

      return z;
   }

   //AH CHANGE
   protected static PathNodeType getPathNodeTypeRaw(IBlockReader blockReader, int x, int y, int z) {
   //protected static PathNodeType getPathNodeTypeRaw(IBlockReader p_189553_0_, int blockaccessIn, int x, int y) {
      BlockPos blockpos = new BlockPos(x, y, z);
      BlockState blockstate = blockReader.getBlockState(blockpos);
      Block block = blockstate.getBlock();
      Material material = blockstate.getMaterial();
      if (blockstate.isAir()) {
         return PathNodeType.OPEN;

      //AH CHANGE ******
      } else if (block instanceof TrapDoorBlock) {
         if(blockstate.get(DoorBlock.OPEN))
         {
            return PathNodeType.TRAPDOOR_OPEN;
         }
         else
         {
            return (material == Material.WOOD) ? PathNodeType.TRAPDOOR_CLOSED : PathNodeType.BLOCKED;
         }
      } else if (block == Blocks.LILY_PAD) {
         return PathNodeType.LILYPAD;
      } else if (block == Blocks.FIRE) {
      //AH CHANGE END ******
      //Vanilla
      //} else if (!block.isIn(BlockTags.TRAPDOORS) && block != Blocks.LILY_PAD) {
         //if (block == Blocks.FIRE) {

         return PathNodeType.DAMAGE_FIRE;
      } else if (block == Blocks.CACTUS) {
         return PathNodeType.DAMAGE_CACTUS;
      } else if (block == Blocks.SWEET_BERRY_BUSH) {
         return PathNodeType.DAMAGE_OTHER;
      } else if (block == Blocks.field_226907_mc_) {
         return PathNodeType.STICKY_HONEY;
      } else if (block == Blocks.COCOA) {
         return PathNodeType.COCOA;
      } else if (block instanceof DoorBlock && material == Material.WOOD && !blockstate.get(DoorBlock.OPEN)) {
         return PathNodeType.DOOR_WOOD_CLOSED;
      } else if (block instanceof DoorBlock && material == Material.IRON && !blockstate.get(DoorBlock.OPEN)) {
         return PathNodeType.DOOR_IRON_CLOSED;
      } else if (block instanceof DoorBlock && blockstate.get(DoorBlock.OPEN)) {
         return PathNodeType.DOOR_OPEN;
      } else if (block instanceof AbstractRailBlock) {
         return PathNodeType.RAIL;

         //AH CHANGE ADD
      } else if (block instanceof ScaffoldingBlock) {
         return PathNodeType.SCAFFOLDING;

         //AH CHANGE ADD
      } else if (isLadderPathable(blockReader, blockstate, blockpos, true, false)) {
         return PathNodeType.LADDER;

      } else if (block instanceof LeavesBlock) {
         return PathNodeType.LEAVES;
      } else if (!block.isIn(BlockTags.FENCES) && !block.isIn(BlockTags.WALLS) && (!(block instanceof FenceGateBlock) || blockstate.get(FenceGateBlock.OPEN))) {
         IFluidState ifluidstate = blockReader.getFluidState(blockpos);
         if (ifluidstate.isTagged(FluidTags.WATER)) {
            return PathNodeType.WATER;
         } else if (ifluidstate.isTagged(FluidTags.LAVA)) {
            return PathNodeType.LAVA;
         } else {
            return blockstate.allowsMovement(blockReader, blockpos, PathType.LAND) ? PathNodeType.OPEN : PathNodeType.BLOCKED;
         }
      } else {
         return PathNodeType.FENCE;
      }

      //AH CHANGE REMOVE
      /*
      } else {
         return PathNodeType.TRAPDOOR;
      }
       */
   }

   public static boolean isLadderPathable(IBlockReader blockReader, BlockState state, BlockPos pos, boolean bPathFinding, boolean canOpenTrapDoors) {
      Block block = state.getBlock();
      if (BlockTags.TRAPDOORS.contains(block)) {
         BlockState down;
         if(canOpenTrapDoors && bPathFinding)
         {
            down = blockReader.getBlockState(pos.down());
            if (down.getBlock() == Blocks.LADDER || down.getBlock() == Blocks.VINE) {
               return true;
            }
         }
         else {
            if ((Boolean) state.get(TrapDoorBlock.OPEN)) {
               down = blockReader.getBlockState(pos.down());
               if (down.getBlock() == Blocks.LADDER|| down.getBlock() == Blocks.VINE) {
                  return true;
               }
            }
         }

         return false;
      }
      else {
         if(!bPathFinding || block != Blocks.SCAFFOLDING) {
            return block.isLadder(state, blockReader, pos);
         }
         else
         {
            return false;
         }
      }
   }
}
