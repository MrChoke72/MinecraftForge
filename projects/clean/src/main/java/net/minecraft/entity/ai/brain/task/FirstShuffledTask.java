package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Map;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;

public class FirstShuffledTask<E extends LivingEntity> extends MultiTask<E> {
   public FirstShuffledTask(List<Pair<Task<? super E>, Integer>> taskList) {
      this(ImmutableMap.of(), taskList);
   }

   public FirstShuffledTask(Map<MemoryModuleType<?>, MemoryModuleStatus> memModuleMap, List<Pair<Task<? super E>, Integer>> taskList) {
      super(memModuleMap, ImmutableSet.of(), MultiTask.Ordering.SHUFFLED, MultiTask.RunType.RUN_ONE, taskList);
   }
}
