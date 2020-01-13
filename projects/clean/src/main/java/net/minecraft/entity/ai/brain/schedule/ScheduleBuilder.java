package net.minecraft.entity.ai.brain.schedule;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;

public class ScheduleBuilder {
   private final Schedule schedule;
   private final List<ScheduleBuilder.ActivityEntry> entries = Lists.newArrayList();

   public ScheduleBuilder(Schedule schedule) {
      this.schedule = schedule;
   }

   public ScheduleBuilder add(int startTime, Activity activity) {
      this.entries.add(new ScheduleBuilder.ActivityEntry(startTime, activity));
      return this;
   }

   public Schedule build() {
      this.entries.stream().map(ScheduleBuilder.ActivityEntry::getActivity).collect(Collectors.toSet()).forEach(this.schedule::createDutiesFor);
      this.entries.forEach((activityEntry) -> {
         Activity activity = activityEntry.getActivity();
         this.schedule.getAllDutiesExcept(activity).forEach((schedDuties) -> {
            schedDuties.addDutyTime(activityEntry.getStartTime(), 0.0F);
         });
         this.schedule.getDutiesFor(activity).addDutyTime(activityEntry.getStartTime(), 1.0F);
      });
      return this.schedule;
   }

   static class ActivityEntry {
      private final int startTime;
      private final Activity activity;

      public ActivityEntry(int p_i51309_1_, Activity p_i51309_2_) {
         this.startTime = p_i51309_1_;
         this.activity = p_i51309_2_;
      }

      public int getStartTime() {
         return this.startTime;
      }

      public Activity getActivity() {
         return this.activity;
      }
   }
}
