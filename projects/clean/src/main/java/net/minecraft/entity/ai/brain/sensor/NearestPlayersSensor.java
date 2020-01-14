package net.minecraft.entity.ai.brain.sensor;

import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.EntityPredicates;
import net.minecraft.world.server.ServerWorld;

public class NearestPlayersSensor extends Sensor<LivingEntity> {
   protected void update(ServerWorld world, LivingEntity entity) {
      List<PlayerEntity> list = world.getPlayers().stream().filter(EntityPredicates.NOT_SPECTATING).filter((p_220979_1_) -> {
         return entity.getDistanceSq(p_220979_1_) < 256.0D;
      }).sorted(Comparator.comparingDouble(entity::getDistanceSq)).collect(Collectors.toList());
      Brain<?> brain = entity.getBrain();
      brain.setMemory(MemoryModuleType.NEAREST_PLAYERS, list);
      brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER, list.stream().filter(entity::canEntityBeSeen).findFirst());
   }

   public Set<MemoryModuleType<?>> getUsedMemories() {
      return ImmutableSet.of(MemoryModuleType.NEAREST_PLAYERS, MemoryModuleType.NEAREST_VISIBLE_PLAYER);
   }
}
