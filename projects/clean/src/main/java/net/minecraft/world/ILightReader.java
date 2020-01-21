package net.minecraft.world;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.lighting.WorldLightManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface ILightReader extends IBlockReader {
   WorldLightManager getLightMgr();

   @OnlyIn(Dist.CLIENT)
   int func_225525_a_(BlockPos p_225525_1_, ColorResolver p_225525_2_);

   default int getLightLevel(LightType lightType, BlockPos pos) {
      return this.getLightMgr().getLightEngine(lightType).getLightFor(pos);
   }

   default int func_226659_b_(BlockPos pos, int p_226659_2_) {
      return this.getLightMgr().func_227470_b_(pos, p_226659_2_);
   }

   default boolean isMaxLightLevel(BlockPos pos) {
      return this.getLightLevel(LightType.SKY, pos) >= this.getMaxLightLevel();
   }
}
