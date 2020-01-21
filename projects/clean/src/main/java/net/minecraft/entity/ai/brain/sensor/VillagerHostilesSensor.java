package net.minecraft.entity.ai.brain.sensor;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.server.ServerWorld;

public class VillagerHostilesSensor extends Sensor<LivingEntity> {

   //AH CHANGE - Beef up villager hostile range sensor
   private static final ImmutableMap<EntityType<?>, Float> hostileDistMap = ImmutableMap.<EntityType<?>, Float>builder().put(EntityType.DROWNED, 12.0F).put(EntityType.EVOKER, 16.0F)
           .put(EntityType.HUSK, 12.0F).put(EntityType.ILLUSIONER, 16.0F).put(EntityType.PILLAGER, 20.0F).put(EntityType.RAVAGER, 16.0F).put(EntityType.VEX, 12.0F)
           .put(EntityType.VINDICATOR, 14.0F).put(EntityType.ZOMBIE, 12.0F).put(EntityType.ZOMBIE_VILLAGER, 12.0F)
           .build();
   //private static final ImmutableMap<EntityType<?>, Float> field_220991_b = ImmutableMap.<EntityType<?>, Float>builder().put(EntityType.DROWNED, 8.0F).put(EntityType.EVOKER, 12.0F).put(EntityType.HUSK, 8.0F).put(EntityType.ILLUSIONER, 12.0F).put(EntityType.PILLAGER, 15.0F).put(EntityType.RAVAGER, 12.0F).put(EntityType.VEX, 8.0F).put(EntityType.VINDICATOR, 10.0F).put(EntityType.ZOMBIE, 8.0F).put(EntityType.ZOMBIE_VILLAGER, 8.0F).build();

   public Set<MemoryModuleType<?>> getUsedMemories() {
      return ImmutableSet.of(MemoryModuleType.NEAREST_HOSTILE);
   }

   protected void update(ServerWorld world, LivingEntity entity) {
      entity.getBrain().setMemory(MemoryModuleType.NEAREST_HOSTILE, this.getNearestHostile(entity));
   }

   private Optional<LivingEntity> getNearestHostile(LivingEntity owner) {
      return this.getVisibleMobs(owner).flatMap((entityList) -> {
         return entityList.stream().filter(this::hasEntityType).filter((entity) -> {
            return this.isEntityClose(owner, entity);
         }).min((entity1, entity2) -> {
            return this.getOwnerDistDiff(owner, entity1, entity2);
         });
      });
   }

   private Optional<List<LivingEntity>> getVisibleMobs(LivingEntity entity) {
      return entity.getBrain().getMemory(MemoryModuleType.VISIBLE_MOBS);
   }

   private int getOwnerDistDiff(LivingEntity owner, LivingEntity entity1, LivingEntity entity2) {
      return MathHelper.floor(entity1.getDistanceSq(owner) - entity2.getDistanceSq(owner));
   }

   private boolean isEntityClose(LivingEntity owner, LivingEntity entity) {
      float f = hostileDistMap.get(entity.getType());
      return entity.getDistanceSq(owner) <= (double)(f * f);
   }

   private boolean hasEntityType(LivingEntity entity) {
      return hostileDistMap.containsKey(entity.getType());
   }
}
