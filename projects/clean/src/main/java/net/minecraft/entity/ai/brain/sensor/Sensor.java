package net.minecraft.entity.ai.brain.sensor;

import java.util.Random;
import java.util.Set;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.world.server.ServerWorld;

public abstract class Sensor<E extends LivingEntity> {
   private static final Random RANDOM = new Random();
   private final int interval;
   private long counter;

   public Sensor(int interval) {
      this.interval = interval;
      this.counter = (long)RANDOM.nextInt(interval);
   }

   public Sensor() {
      this(20);
   }

   public final void tick(ServerWorld worldIn, E entityIn) {
      if (--this.counter <= 0L) {
         this.counter = (long)this.interval;
         this.update(worldIn, entityIn);
      }

   }

   protected abstract void update(ServerWorld worldIn, E entityIn);

   public abstract Set<MemoryModuleType<?>> getUsedMemories();
}
