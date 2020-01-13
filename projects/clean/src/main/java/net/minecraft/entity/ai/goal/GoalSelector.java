package net.minecraft.entity.ai.goal;

import com.google.common.collect.Sets;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.profiler.IProfiler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GoalSelector {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final PrioritizedGoal DUMMY = new PrioritizedGoal(Integer.MAX_VALUE, new Goal() {
      public boolean shouldExecute() {
         return false;
      }
   }) {
      public boolean isRunning() {
         return false;
      }
   };
   private final Map<Goal.Flag, PrioritizedGoal> runningFlagGoals = new EnumMap<>(Goal.Flag.class);    //Holds running goals only
   private final Set<PrioritizedGoal> goals = Sets.newLinkedHashSet();
   private final IProfiler profiler;
   private final EnumSet<Goal.Flag> disabledFlags = EnumSet.noneOf(Goal.Flag.class);
   private int tickRate = 3;

   public GoalSelector(IProfiler p_i50327_1_) {
      this.profiler = p_i50327_1_;
   }

   public void addGoal(int priority, Goal task) {
      this.goals.add(new PrioritizedGoal(priority, task));
   }

   public void removeGoal(Goal task) {
      this.goals.stream().filter((prioritizedGoal) -> {
         return prioritizedGoal.getGoal() == task;
      }).filter(PrioritizedGoal::isRunning).forEach(PrioritizedGoal::resetTask);
      this.goals.removeIf((prioritizedGoal) -> {
         return prioritizedGoal.getGoal() == task;
      });
   }

   public void tick() {
      this.profiler.startSection("goalCleanup");
      this.getRunningGoals().filter((pGoal) -> {
         return !pGoal.isRunning() || pGoal.getMutexFlags().stream().anyMatch(this.disabledFlags::contains) || !pGoal.shouldContinueExecuting();
      }).forEach(Goal::resetTask);
      this.runningFlagGoals.forEach((flag, pGoal) -> {
         if (!pGoal.isRunning()) {
            this.runningFlagGoals.remove(flag);
         }

      });
      this.profiler.endSection();
      this.profiler.startSection("goalUpdate");
      this.goals.stream().filter((pGoal) -> {
         return !pGoal.isRunning();
      }).filter((pGoal) -> {
         return pGoal.getMutexFlags().stream().noneMatch(this.disabledFlags::contains);
      }).filter((pGoal) -> {
         return pGoal.getMutexFlags().stream().allMatch((flag) -> {
            return this.runningFlagGoals.getOrDefault(flag, DUMMY).isPreemptedBy(pGoal);
         });
      }).filter(PrioritizedGoal::shouldExecute).forEach((pGoal) -> {
         pGoal.getMutexFlags().forEach((flag) -> {
            PrioritizedGoal prioritizedgoal = this.runningFlagGoals.getOrDefault(flag, DUMMY);
            prioritizedgoal.resetTask();
            this.runningFlagGoals.put(flag, pGoal);
         });
         pGoal.startExecuting();
      });
      this.profiler.endSection();
      this.profiler.startSection("goalTick");
      this.getRunningGoals().forEach(PrioritizedGoal::tick);
      this.profiler.endSection();
   }

   public Stream<PrioritizedGoal> getRunningGoals() {
      return this.goals.stream().filter(PrioritizedGoal::isRunning);
   }

   public void disableFlag(Goal.Flag flag) {
      this.disabledFlags.add(flag);
   }

   public void enableFlag(Goal.Flag flag) {
      this.disabledFlags.remove(flag);
   }

   public void setFlag(Goal.Flag flag, boolean b) {
      if (b) {
         this.enableFlag(flag);
      } else {
         this.disableFlag(flag);
      }

   }
}
