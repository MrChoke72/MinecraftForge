package net.minecraft.block;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class FlowerPotBlock extends Block {
   private static final Map<Block, Block> field_196451_b = Maps.newHashMap();
   protected static final VoxelShape SHAPE = Block.makeCuboidShape(5.0D, 0.0D, 5.0D, 11.0D, 6.0D, 11.0D);
   private final Block flower;

   public FlowerPotBlock(Block p_i48395_1_, Block.Properties p_i48395_2_) {
      super(p_i48395_2_);
      this.flower = p_i48395_1_;
      field_196451_b.put(p_i48395_1_, this);
   }

   public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
      return SHAPE;
   }

   public BlockRenderType getRenderType(BlockState state) {
      return BlockRenderType.MODEL;
   }

   public ActionResultType func_225533_a_(BlockState p_225533_1_, World p_225533_2_, BlockPos p_225533_3_, PlayerEntity p_225533_4_, Hand p_225533_5_, BlockRayTraceResult p_225533_6_) {
      ItemStack itemstack = p_225533_4_.getHeldItem(p_225533_5_);
      Item item = itemstack.getItem();
      Block block = item instanceof BlockItem ? field_196451_b.getOrDefault(((BlockItem)item).getBlock(), Blocks.AIR) : Blocks.AIR;
      boolean flag = block == Blocks.AIR;
      boolean flag1 = this.flower == Blocks.AIR;
      if (flag != flag1) {
         if (flag1) {
            p_225533_2_.setBlockState(p_225533_3_, block.getDefaultState(), 3);
            p_225533_4_.addStat(Stats.POT_FLOWER);
            if (!p_225533_4_.abilities.isCreativeMode) {
               itemstack.shrink(1);
            }
         } else {
            ItemStack itemstack1 = new ItemStack(this.flower);
            if (itemstack.isEmpty()) {
               p_225533_4_.setHeldItem(p_225533_5_, itemstack1);
            } else if (!p_225533_4_.addItemStackToInventory(itemstack1)) {
               p_225533_4_.dropItem(itemstack1, false);
            }

            p_225533_2_.setBlockState(p_225533_3_, Blocks.FLOWER_POT.getDefaultState(), 3);
         }

         return ActionResultType.SUCCESS;
      } else {
         return ActionResultType.CONSUME;
      }
   }

   public ItemStack getItem(IBlockReader worldIn, BlockPos pos, BlockState state) {
      return this.flower == Blocks.AIR ? super.getItem(worldIn, pos, state) : new ItemStack(this.flower);
   }

   public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
      return facing == Direction.DOWN && !stateIn.isValidPosition(worldIn, currentPos) ? Blocks.AIR.getDefaultState() : super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
   }

   public Block func_220276_d() {
      return this.flower;
   }
}
