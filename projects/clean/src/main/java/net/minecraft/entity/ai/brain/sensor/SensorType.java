package net.minecraft.entity.ai.brain.sensor;

import java.util.function.Supplier;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import com.mrchoke.entity.ai.brain.sensor.InteractableTrapDoorsSensor;

public class SensorType<U extends Sensor<?>> {
   public static final SensorType<DummySensor> DUMMY = register("dummy", DummySensor::new);
   public static final SensorType<NearestLivingEntitiesSensor> NEAREST_LIVING_ENTITIES = register("nearest_living_entities", NearestLivingEntitiesSensor::new);
   public static final SensorType<NearestPlayersSensor> NEAREST_PLAYERS = register("nearest_players", NearestPlayersSensor::new);
   public static final SensorType<InteractableDoorsSensor> INTERACTABLE_DOORS = register("interactable_doors", InteractableDoorsSensor::new);
   public static final SensorType<NearestBedSensor> NEAREST_BED = register("nearest_bed", NearestBedSensor::new);
   public static final SensorType<HurtBySensor> HURT_BY = register("hurt_by", HurtBySensor::new);
   public static final SensorType<VillagerHostilesSensor> VILLAGER_HOSTILES = register("villager_hostiles", VillagerHostilesSensor::new);
   public static final SensorType<VillagerBabiesSensor> VILLAGER_BABIES = register("villager_babies", VillagerBabiesSensor::new);
   public static final SensorType<SecondaryPositionSensor> SECONDARY_POIS = register("secondary_pois", SecondaryPositionSensor::new);
   public static final SensorType<GolemLastSeenSensor> GOLEM_LAST_SEEN = register("golem_last_seen", GolemLastSeenSensor::new);

   //AH ADDED
   public static final SensorType<InteractableTrapDoorsSensor> INTERACTABLE_TRAPDOORS = register("interactable_trapdoors", InteractableTrapDoorsSensor::new);

   private final Supplier<U> supplier;

   public SensorType(Supplier<U> supplier) {
      this.supplier = supplier;
   }

   //AH REFACTOR
   public U getSensor() {
   //public U func_220995_a() {
      return (U)(this.supplier.get());
   }

   private static <U extends Sensor<?>> SensorType<U> register(String key, Supplier<U> supplier) {
      return Registry.register(Registry.SENSOR_TYPE, new ResourceLocation(key), new SensorType<>(supplier));
   }
}
