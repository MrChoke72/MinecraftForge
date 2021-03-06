package net.minecraft.entity.ai.brain;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import com.mojang.datafixers.util.Pair;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.schedule.Activity;
import net.minecraft.entity.ai.brain.schedule.Schedule;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.util.IDynamicSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.server.ServerWorld;

public class Brain<E extends LivingEntity> implements IDynamicSerializable {
   private final Map<MemoryModuleType<?>, Optional<?>> memories = Maps.newHashMap();
   private final Map<SensorType<? extends Sensor<? super E>>, Sensor<? super E>> sensors = Maps.newLinkedHashMap();

   //AH REFACTOR
   private final Map<Integer, Map<Activity, Set<Task<? super E>>>> activityTasksMap = Maps.newTreeMap(); //Key is Integer: task execution order.  Must match Integer on tasklist pairs
   //private final Map<Integer, Map<Activity, Set<Task<? super E>>>> field_218232_c = Maps.newTreeMap();

   private Schedule schedule = Schedule.EMPTY;
   private final Map<Activity, Set<Pair<MemoryModuleType<?>, MemoryModuleStatus>>> requiredMemoryStates = Maps.newHashMap();
   private Set<Activity> defaultActivities = Sets.newHashSet();
   private final Set<Activity> activities = Sets.newHashSet();
   private Activity fallbackActivity = Activity.IDLE;
   private long lastGameTime = -9999L;

   //AH REFACTOR
   public <T> Brain(Collection<MemoryModuleType<?>> memModules, Collection<SensorType<? extends Sensor<? super E>>> sensorTypes, Dynamic<T> memoriesIn) {
   //public <T> Brain(Collection<MemoryModuleType<?>> p_i50378_1_, Collection<SensorType<? extends Sensor<? super E>>> p_i50378_2_, Dynamic<T> p_i50378_3_) {
      memModules.forEach((memModuleType) -> {
         Optional optional = this.memories.put(memModuleType, Optional.empty());
      });
      sensorTypes.forEach((sensorType) -> {
         Sensor sensor = this.sensors.put(sensorType, sensorType.getSensor());
      });
      this.sensors.values().forEach((sensor) -> {
         for(MemoryModuleType<?> memorymoduletype : sensor.getUsedMemories()) {
            this.memories.put(memorymoduletype, Optional.empty());
         }

      });

      for(Entry<Dynamic<T>, Dynamic<T>> entry : memoriesIn.get("memories").asMap(Function.identity(), Function.identity()).entrySet()) {
         this.setMemory(Registry.MEMORY_MODULE_TYPE.getOrDefault(new ResourceLocation(entry.getKey().asString(""))), entry.getValue());
      }

   }

   //AH CHANGE TEMP FOR DEBUG ONLY ****
   public Set<Activity> getActivities()
   {
      return activities;
   }
   //AH CHANGE END ****

   public boolean hasMemory(MemoryModuleType<?> memModuleType) {
      return this.hasMemory(memModuleType, MemoryModuleStatus.VALUE_PRESENT);
   }

   private <T, U> void setMemory(MemoryModuleType<U> memModuleType, Dynamic<T> p_218216_2_) {
      this.setMemory(memModuleType, memModuleType.getDeserializer().orElseThrow(RuntimeException::new).apply(p_218216_2_));
   }

   public <U> void removeMemory(MemoryModuleType<U> memModuleType) {
      this.setMemory(memModuleType, Optional.empty());
   }

   public <U> void setMemory(MemoryModuleType<U> memModuleType, @Nullable U u) {
   //public <U> void setMemory(MemoryModuleType<U> p_218205_1_, @Nullable U p_218205_2_) {
      this.setMemory(memModuleType, Optional.ofNullable(u));
   }

   public <U> void setMemory(MemoryModuleType<U> memModuleType, Optional<U> optional) {
      if (this.memories.containsKey(memModuleType)) {
         if (optional.isPresent() && this.isEmptyCollection(optional.get())) {
            this.removeMemory(memModuleType);
         } else {
            this.memories.put(memModuleType, optional);
         }
      }

   }

   public <U> Optional<U> getMemory(MemoryModuleType<U> memModuleType) {
      return (Optional<U>) this.memories.get(memModuleType);
   }

   public boolean hasMemory(MemoryModuleType<?> memoryTypeIn, MemoryModuleStatus memoryStatusIn) {
      Optional<?> optional = this.memories.get(memoryTypeIn);
      if (optional == null) {
         return false;
      } else {
         return memoryStatusIn == MemoryModuleStatus.REGISTERED || memoryStatusIn == MemoryModuleStatus.VALUE_PRESENT && optional.isPresent() ||
                 memoryStatusIn == MemoryModuleStatus.VALUE_ABSENT && !optional.isPresent();
      }
   }

   public Schedule getSchedule() {
      return this.schedule;
   }

   public void setSchedule(Schedule newSchedule) {
      this.schedule = newSchedule;
   }

   public void setDefaultActivities(Set<Activity> newActivities) {
      this.defaultActivities = newActivities;
   }

   @Deprecated
   public Stream<Task<? super E>> getRunningTasks() {
      return this.activityTasksMap.values().stream().flatMap((setMap) -> {
         return setMap.values().stream();
      }).flatMap(Collection::stream).filter((task) -> {
         return task.getStatus() == Task.Status.RUNNING;
      });
   }

   public void switchTo(Activity activityIn) {
      this.activities.clear();
      this.activities.addAll(this.defaultActivities);
      boolean flag = this.requiredMemoryStates.keySet().contains(activityIn) && this.hasRequiredMemories(activityIn);
      this.activities.add(flag ? activityIn : this.fallbackActivity);
   }

   public void updateActivity(long dayTime, long gameTime) {
      if (gameTime - this.lastGameTime > 20L) {
         this.lastGameTime = gameTime;
         Activity activity = this.getSchedule().getScheduledActivity((int)(dayTime % 24000L));
         if (!this.activities.contains(activity)) {
            this.switchTo(activity);
         }
      }

   }

   public void setFallbackActivity(Activity newFallbackActivity) {
      this.fallbackActivity = newFallbackActivity;
   }

   //AH REFACTOR
   public void registerActivity(Activity activityIn, ImmutableList<Pair<Integer, ? extends Task<? super E>>> taskList) {
   //public void registerActivity(Activity activityIn, ImmutableList<Pair<Integer, ? extends Task<? super E>>> p_218208_2_) {
      this.registerActivity(activityIn, taskList, ImmutableSet.of());
   }

   //AH REFACTOR
   public void registerActivity(Activity activityIn, ImmutableList<Pair<Integer, ? extends Task<? super E>>> taskList, Set<Pair<MemoryModuleType<?>, MemoryModuleStatus>> memModuleSet) {
   //public void registerActivity(Activity activityIn, ImmutableList<Pair<Integer, ? extends Task<? super E>>> taskList, Set<Pair<MemoryModuleType<?>, MemoryModuleStatus>> p_218224_3_) {
      this.requiredMemoryStates.put(activityIn, memModuleSet);
      taskList.forEach((pair) -> {
         this.activityTasksMap.computeIfAbsent(pair.getFirst(), (map) -> {
            return Maps.newHashMap();
         }).computeIfAbsent(activityIn, (set) -> {
            return Sets.newLinkedHashSet();
         }).add(pair.getSecond());
      });
   }

   public boolean hasActivity(Activity activityIn) {
      return this.activities.contains(activityIn);
   }

   public Brain<E> copy() {
      Brain<E> brain = new Brain<>(this.memories.keySet(), this.sensors.keySet(), new Dynamic<>(NBTDynamicOps.INSTANCE, new CompoundNBT()));
      this.memories.forEach((p_218188_1_, p_218188_2_) -> {
         p_218188_2_.ifPresent((p_218209_2_) -> {
            Optional optional = brain.memories.put(p_218188_1_, Optional.of(p_218209_2_));
         });
      });
      return brain;
   }

   public void tick(ServerWorld worldIn, E entityIn) {
      this.updateSensors(worldIn, entityIn);
      this.startTasks(worldIn, entityIn);
      this.tickTasks(worldIn, entityIn);
   }

   public void stopAllTasks(ServerWorld worldIn, E owner) {
      long i = owner.world.getGameTime();
      this.getRunningTasks().forEach((p_218206_4_) -> {
         p_218206_4_.stop(worldIn, owner, i);
      });
   }

   public <T> T serialize(DynamicOps<T> p_218175_1_) {
      T t = p_218175_1_.createMap(this.memories.entrySet().stream().filter((p_218197_0_) -> {
         return p_218197_0_.getKey().getDeserializer().isPresent() && p_218197_0_.getValue().isPresent();
      }).map((p_218186_1_) -> {
         return Pair.of(p_218175_1_.createString(Registry.MEMORY_MODULE_TYPE.getKey(p_218186_1_.getKey()).toString()), ((IDynamicSerializable)p_218186_1_.getValue().get()).serialize(p_218175_1_));
      }).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)));
      return p_218175_1_.createMap(ImmutableMap.of(p_218175_1_.createString("memories"), t));
   }

   private void updateSensors(ServerWorld worldIn, E entityIn) {
      this.sensors.values().forEach((sensor) -> {
         sensor.tick(worldIn, entityIn);
      });
   }

   private void startTasks(ServerWorld worldIn, E entityIn) {
      long i = worldIn.getGameTime();
      this.activityTasksMap.values().stream().flatMap((activitySetMap) -> {
         return activitySetMap.entrySet().stream();
      }).filter((entry) -> {
         return this.activities.contains(entry.getKey());
      }).map(Entry::getValue).flatMap(Collection::stream).filter((task) -> {
         return task.getStatus() == Task.Status.STOPPED;
      }).forEach((task) -> {
         task.start(worldIn, entityIn, i);
      });
   }

   private void tickTasks(ServerWorld worldIn, E entityIn) {
      long i = worldIn.getGameTime();
      this.getRunningTasks().forEach((task) -> {
         task.tick(worldIn, entityIn, i);
      });
   }

   private boolean hasRequiredMemories(Activity activityIn) {
      return this.requiredMemoryStates.get(activityIn).stream().allMatch((pair) -> {
         MemoryModuleType<?> memorymoduletype = pair.getFirst();
         MemoryModuleStatus memorymodulestatus = pair.getSecond();
         return this.hasMemory(memorymoduletype, memorymodulestatus);
      });
   }

   private boolean isEmptyCollection(Object p_218213_1_) {
      return p_218213_1_ instanceof Collection && ((Collection)p_218213_1_).isEmpty();
   }
}
