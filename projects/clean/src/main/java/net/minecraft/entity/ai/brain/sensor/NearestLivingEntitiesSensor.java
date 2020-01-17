package net.minecraft.entity.ai.brain.sensor;

import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.world.server.ServerWorld;

public class NearestLivingEntitiesSensor extends Sensor<LivingEntity> {
   private static final EntityPredicate TARGET_ENTITY_PRED = (new EntityPredicate()).setDistance(16.0D).allowFriendlyFire().setSkipAttackChecks().setLineOfSiteRequired();

   protected void update(ServerWorld world, LivingEntity entity) {
      List<LivingEntity> list = world.getEntitiesWithinAABB(LivingEntity.class, entity.getBoundingBox().grow(16.0D, 16.0D, 16.0D), (livingEntity) -> {
         return livingEntity != entity && livingEntity.isAlive();
      });
      list.sort(Comparator.comparingDouble(entity::getDistanceSq));
      Brain<?> brain = entity.getBrain();
      brain.setMemory(MemoryModuleType.MOBS, list);
      brain.setMemory(MemoryModuleType.VISIBLE_MOBS, list.stream().filter((livingEntity) -> {
         return TARGET_ENTITY_PRED.canTarget(entity, livingEntity);
      }).filter(entity::canEntityBeSeen).collect(Collectors.toList()));
   }

   public Set<MemoryModuleType<?>> getUsedMemories() {
      return ImmutableSet.of(MemoryModuleType.MOBS, MemoryModuleType.VISIBLE_MOBS);
   }
}
