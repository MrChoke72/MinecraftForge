package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.MobEntity;
import net.minecraft.world.server.ServerWorld;

public class SwimTask extends Task<MobEntity> {
   private final float width;
   private final float height;

   public SwimTask(float width, float height) {
      super(ImmutableMap.of());
      this.width = width;
      this.height = height;
   }

   protected boolean shouldExecute(ServerWorld worldIn, MobEntity owner) {
      return owner.isInWater() && owner.getSubmergedHeight() > (double)this.width || owner.isInLava();
   }

   protected boolean shouldContinueExecuting(ServerWorld worldIn, MobEntity entityIn, long gameTimeIn) {
      return this.shouldExecute(worldIn, entityIn);
   }

   protected void updateTask(ServerWorld worldIn, MobEntity owner, long gameTime) {
      if (owner.getRNG().nextFloat() < this.height) {
         owner.getJumpController().setJumping();
      }

   }
}
