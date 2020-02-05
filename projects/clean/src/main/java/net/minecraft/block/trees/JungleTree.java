package net.minecraft.block.trees;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.world.biome.DefaultBiomeFeatures;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.HugeTreeFeatureConfig;
import net.minecraft.world.gen.feature.TreeFeature;
import net.minecraft.world.gen.feature.TreeFeatureConfig;

public class JungleTree extends BigTree {
   @Nullable
   protected ConfiguredFeature<TreeFeatureConfig, ?> func_225546_b_(Random p_225546_1_, boolean p_225546_2_) {
      return (new TreeFeature(TreeFeatureConfig::func_227338_a_)).withConfiguration(DefaultBiomeFeatures.field_226808_c_);
   }

   @Nullable
   protected ConfiguredFeature<HugeTreeFeatureConfig, ?> func_225547_a_(Random p_225547_1_) {
      return Feature.MEGA_JUNGLE_TREE.withConfiguration(DefaultBiomeFeatures.field_226825_t_);
   }
}
