package net.minecraft.entity.ai.brain.schedule;

import com.google.common.collect.Maps;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import net.minecraft.util.registry.Registry;

public class Schedule {
   public static final Schedule EMPTY = register("empty").add(0, Activity.IDLE).build();
   public static final Schedule SIMPLE = register("simple").add(5000, Activity.WORK).add(11000, Activity.REST).build();
   public static final Schedule VILLAGER_BABY = register("villager_baby").add(10, Activity.IDLE).add(3000, Activity.PLAY).add(6000, Activity.IDLE).add(10000, Activity.PLAY).add(12000, Activity.REST).build();
   public static final Schedule VILLAGER_DEFAULT = register("villager_default")
           .add(10, Activity.IDLE)
           .add(2000, Activity.WORK)
           .add(9000, Activity.MEET)
           .add(11000, Activity.IDLE)
           .add(12000, Activity.REST)
           .build();

   private final Map<Activity, ScheduleDuties> activityDutyMap = Maps.newHashMap();

   protected static ScheduleBuilder register(String key) {
      Schedule schedule = Registry.register(Registry.SCHEDULE, key, new Schedule());
      return new ScheduleBuilder(schedule);
   }

   protected void createDutiesFor(Activity activity) {
      if (!this.activityDutyMap.containsKey(activity)) {
         this.activityDutyMap.put(activity, new ScheduleDuties());
      }

   }

   protected ScheduleDuties getDutiesFor(Activity activity) {
      return this.activityDutyMap.get(activity);
   }

   protected List<ScheduleDuties> getAllDutiesExcept(Activity activity) {
      return this.activityDutyMap.entrySet().stream().filter((entry) -> {
         return entry.getKey() != activity;
      }).map(Entry::getValue).collect(Collectors.toList());
   }

   public Activity getScheduledActivity(int dayTime) {
      return this.activityDutyMap.entrySet().stream().max(Comparator.comparingDouble((dutiesEntry) -> {
         return (double)dutiesEntry.getValue().isDurationForActivity(dayTime);
      })).map(Entry::getKey).orElse(Activity.IDLE);
   }
}
