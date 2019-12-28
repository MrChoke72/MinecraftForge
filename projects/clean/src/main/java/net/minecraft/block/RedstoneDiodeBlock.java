package net.minecraft.block;

import java.util.Random;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.TickPriority;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public abstract class RedstoneDiodeBlock extends HorizontalBlock {
   protected static final VoxelShape SHAPE = Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);
   public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

   protected RedstoneDiodeBlock(Block.Properties builder) {
      super(builder);
   }

   public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
      return SHAPE;
   }

   public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
      return func_220064_c(worldIn, pos.down());
   }

   public void func_225534_a_(BlockState p_225534_1_, ServerWorld p_225534_2_, BlockPos p_225534_3_, Random p_225534_4_) {
      if (!this.isLocked(p_225534_2_, p_225534_3_, p_225534_1_)) {
         boolean flag = p_225534_1_.get(POWERED);
         boolean flag1 = this.shouldBePowered(p_225534_2_, p_225534_3_, p_225534_1_);
         if (flag && !flag1) {
            p_225534_2_.setBlockState(p_225534_3_, p_225534_1_.with(POWERED, Boolean.valueOf(false)), 2);
         } else if (!flag) {
            p_225534_2_.setBlockState(p_225534_3_, p_225534_1_.with(POWERED, Boolean.valueOf(true)), 2);
            if (!flag1) {
               p_225534_2_.getPendingBlockTicks().scheduleTick(p_225534_3_, this, this.getDelay(p_225534_1_), TickPriority.VERY_HIGH);
            }
         }

      }
   }

   public int getStrongPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
      return blockState.getWeakPower(blockAccess, pos, side);
   }

   public int getWeakPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
      if (!blockState.get(POWERED)) {
         return 0;
      } else {
         return blockState.get(HORIZONTAL_FACING) == side ? this.getActiveSignal(blockAccess, pos, blockState) : 0;
      }
   }

   public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
      if (state.isValidPosition(worldIn, pos)) {
         this.updateState(worldIn, pos, state);
      } else {
         TileEntity tileentity = this.hasTileEntity() ? worldIn.getTileEntity(pos) : null;
         spawnDrops(state, worldIn, pos, tileentity);
         worldIn.removeBlock(pos, false);

         for(Direction direction : Direction.values()) {
            worldIn.notifyNeighborsOfStateChange(pos.offset(direction), this);
         }

      }
   }

   protected void updateState(World worldIn, BlockPos pos, BlockState state) {
      if (!this.isLocked(worldIn, pos, state)) {
         boolean flag = state.get(POWERED);
         boolean flag1 = this.shouldBePowered(worldIn, pos, state);
         if (flag != flag1 && !worldIn.getPendingBlockTicks().isTickPending(pos, this)) {
            TickPriority tickpriority = TickPriority.HIGH;
            if (this.isFacingTowardsRepeater(worldIn, pos, state)) {
               tickpriority = TickPriority.EXTREMELY_HIGH;
            } else if (flag) {
               tickpriority = TickPriority.VERY_HIGH;
            }

            worldIn.getPendingBlockTicks().scheduleTick(pos, this, this.getDelay(state), tickpriority);
         }

      }
   }

   public boolean isLocked(IWorldReader worldIn, BlockPos pos, BlockState state) {
      return false;
   }

   protected boolean shouldBePowered(World worldIn, BlockPos pos, BlockState state) {
      return this.calculateInputStrength(worldIn, pos, state) > 0;
   }

   protected int calculateInputStrength(World worldIn, BlockPos pos, BlockState state) {
      Direction direction = state.get(HORIZONTAL_FACING);
      BlockPos blockpos = pos.offset(direction);
      int i = worldIn.getRedstonePower(blockpos, direction);
      if (i >= 15) {
         return i;
      } else {
         BlockState blockstate = worldIn.getBlockState(blockpos);
         return Math.max(i, blockstate.getBlock() == Blocks.REDSTONE_WIRE ? blockstate.get(RedstoneWireBlock.POWER) : 0);
      }
   }

   protected int getPowerOnSides(IWorldReader worldIn, BlockPos pos, BlockState state) {
      Direction direction = state.get(HORIZONTAL_FACING);
      Direction direction1 = direction.rotateY();
      Direction direction2 = direction.rotateYCCW();
      return Math.max(this.getPowerOnSide(worldIn, pos.offset(direction1), direction1), this.getPowerOnSide(worldIn, pos.offset(direction2), direction2));
   }

   protected int getPowerOnSide(IWorldReader worldIn, BlockPos pos, Direction side) {
      BlockState blockstate = worldIn.getBlockState(pos);
      Block block = blockstate.getBlock();
      if (this.isAlternateInput(blockstate)) {
         if (block == Blocks.REDSTONE_BLOCK) {
            return 15;
         } else {
            return block == Blocks.REDSTONE_WIRE ? blockstate.get(RedstoneWireBlock.POWER) : worldIn.getStrongPower(pos, side);
         }
      } else {
         return 0;
      }
   }

   public boolean canProvidePower(BlockState state) {
      return true;
   }

   public BlockState getStateForPlacement(BlockItemUseContext context) {
      return this.getDefaultState().with(HORIZONTAL_FACING, context.getPlacementHorizontalFacing().getOpposite());
   }

   public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
      if (this.shouldBePowered(worldIn, pos, state)) {
         worldIn.getPendingBlockTicks().scheduleTick(pos, this, 1);
      }

   }

   public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
      this.notifyNeighbors(worldIn, pos, state);
   }

   public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
      if (!isMoving && state.getBlock() != newState.getBlock()) {
         super.onReplaced(state, worldIn, pos, newState, isMoving);
         this.notifyNeighbors(worldIn, pos, state);
      }
   }

   protected void notifyNeighbors(World worldIn, BlockPos pos, BlockState state) {
      Direction direction = state.get(HORIZONTAL_FACING);
      BlockPos blockpos = pos.offset(direction.getOpposite());
      worldIn.neighborChanged(blockpos, this, pos);
      worldIn.notifyNeighborsOfStateExcept(blockpos, this, direction);
   }

   protected boolean isAlternateInput(BlockState state) {
      return state.canProvidePower();
   }

   protected int getActiveSignal(IBlockReader worldIn, BlockPos pos, BlockState state) {
      return 15;
   }

   public static boolean isDiode(BlockState state) {
      return state.getBlock() instanceof RedstoneDiodeBlock;
   }

   public boolean isFacingTowardsRepeater(IBlockReader worldIn, BlockPos pos, BlockState state) {
      Direction direction = state.get(HORIZONTAL_FACING).getOpposite();
      BlockState blockstate = worldIn.getBlockState(pos.offset(direction));
      return isDiode(blockstate) && blockstate.get(HORIZONTAL_FACING) != direction;
   }

   protected abstract int getDelay(BlockState p_196346_1_);
}
