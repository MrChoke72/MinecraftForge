package net.minecraft.world.server;

import java.util.concurrent.Executor;
import net.minecraft.profiler.IProfiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.border.IBorderListener;
import net.minecraft.world.chunk.listener.IChunkStatusListener;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.storage.DerivedWorldInfo;
import net.minecraft.world.storage.SaveHandler;

public class ServerMultiWorld extends ServerWorld {
   public ServerMultiWorld(ServerWorld p_i50708_1_, MinecraftServer serverIn, Executor p_i50708_3_, SaveHandler p_i50708_4_, DimensionType dimType, IProfiler p_i50708_6_, IChunkStatusListener p_i50708_7_) {
      super(serverIn, p_i50708_3_, p_i50708_4_, new DerivedWorldInfo(p_i50708_1_.getWorldInfo()), dimType, p_i50708_6_, p_i50708_7_);
      p_i50708_1_.getWorldBorder().addListener(new IBorderListener.Impl(this.getWorldBorder()));
   }

   protected void advanceTime() {
   }
}
