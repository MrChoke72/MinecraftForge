package net.minecraft.entity.ai.brain.schedule;

public class DutyTime {
   private final int startTime;
   private final float durationForActivity; //1=duty duration for activity duration associated with this object.   0, another activity at another duration.

   public DutyTime(int p_i50139_1_, float p_i50139_2_) {
      this.startTime = p_i50139_1_;
      this.durationForActivity = p_i50139_2_;
   }

   public int getStartTime() {
      return this.startTime;
   }

   public float isDurationForActivity() {
      return this.durationForActivity;
   }
}
