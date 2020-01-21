package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

public class WalkRandomlyTask extends Task<CreatureEntity> {
   private final float speed;

   public WalkRandomlyTask(float speed) {
      super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryModuleStatus.VALUE_ABSENT));
      this.speed = speed;
   }

   protected boolean shouldExecute(ServerWorld worldIn, CreatureEntity owner) {
      return !worldIn.isMaxLightLevel(new BlockPos(owner));
   }

   protected void startExecuting(ServerWorld worldIn, CreatureEntity entityIn, long gameTimeIn) {
      BlockPos blockpos = new BlockPos(entityIn);
      List<BlockPos> list = BlockPos.getAllInBox(blockpos.add(-1, -1, -1), blockpos.add(1, 1, 1)).map(BlockPos::toImmutable).collect(Collectors.toList());
      Collections.shuffle(list);
      Optional<BlockPos> optional = list.stream().filter((pos) -> {
         return !worldIn.isMaxLightLevel(pos);
      }).filter((pos) -> {
         return worldIn.func_217400_a(pos, entityIn);
      }).filter((pos) -> {
         return worldIn.isEntityNoCollide(entityIn);
      }).findFirst();
      optional.ifPresent((pos) -> {
         entityIn.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(pos, this.speed, 0));
      });
   }
}
