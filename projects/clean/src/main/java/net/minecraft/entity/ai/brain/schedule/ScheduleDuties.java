package net.minecraft.entity.ai.brain.schedule;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;
import java.util.List;

//AH REFACTOR Class refactor
public class ScheduleDuties {
   private final List<DutyTime> dutyList = Lists.newArrayList();  //Sorted by start time
   private int dutyListIdx;

   public ScheduleDuties addDutyTime(int startTime, float durationForActivity) {
      this.dutyList.add(new DutyTime(startTime, durationForActivity));
      this.sortDuties();
      return this;
   }

   private void sortDuties() {
      Int2ObjectSortedMap<DutyTime> int2objectsortedmap = new Int2ObjectAVLTreeMap<>();
      this.dutyList.forEach((dutyTime) -> {
         DutyTime dutytime = int2objectsortedmap.put(dutyTime.getStartTime(), dutyTime);
      });
      this.dutyList.clear();
      this.dutyList.addAll(int2objectsortedmap.values());
      this.dutyListIdx = 0;
   }

   public float isDurationForActivity(int dayTime) {
      if (this.dutyList.size() <= 0) {
         return 0.0F;
      } else {
         DutyTime dutytime = this.dutyList.get(this.dutyListIdx);
         DutyTime dutytime1 = this.dutyList.get(this.dutyList.size() - 1);
         boolean flag = dayTime < dutytime.getStartTime();
         int i = flag ? 0 : this.dutyListIdx;
         float f = flag ? dutytime1.isDurationForActivity() : dutytime.isDurationForActivity();

         for(int j = i; j < this.dutyList.size(); ++j) {
            DutyTime dutytime2 = this.dutyList.get(j);
            if (dutytime2.getStartTime() > dayTime) {
               break;
            }

            this.dutyListIdx = j;
            f = dutytime2.isDurationForActivity();
         }

         return f;
      }
   }
}
