package net.minecraft.world.biome;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.provider.BiomeProvider;

public class BiomeManager {
   private final BiomeManager.IBiomeReader reader;
   private final long seed;
   private final IBiomeMagnifier magnifier;

   public BiomeManager(BiomeManager.IBiomeReader readerIn, long seedIn, IBiomeMagnifier magnifierIn) {
      this.reader = readerIn;
      this.seed = seedIn;
      this.magnifier = magnifierIn;
   }

   public BiomeManager copyWithProvider(BiomeProvider newProvider) {
      return new BiomeManager(newProvider, this.seed, this.magnifier);
   }

   public Biome getBiome(BlockPos p_226836_1_) {
      return this.magnifier.getBiome(this.seed, p_226836_1_.getX(), p_226836_1_.getY(), p_226836_1_.getZ(), this.reader);
   }

   public interface IBiomeReader {
      Biome getNoiseBiome(int x, int y, int z);
   }
}
