package net.minecraft.block.trees;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.world.biome.DefaultBiomeFeatures;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.TreeFeatureConfig;

public class AcaciaTree extends Tree {
   @Nullable
   protected ConfiguredFeature<TreeFeatureConfig, ?> func_225546_b_(Random p_225546_1_, boolean p_225546_2_) {
      return Feature.ACACIA_TREE.withConfiguration(DefaultBiomeFeatures.field_226811_f_);
   }
}
