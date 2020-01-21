package net.minecraft.entity.ai.brain.task;

import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.util.WeightedList;
import net.minecraft.world.server.ServerWorld;

public class MultiTask<E extends LivingEntity> extends Task<E> {
   private final Set<MemoryModuleType<?>> memModuleSet;
   private final MultiTask.Ordering ordering;
   private final MultiTask.RunType runType;
   private final WeightedList<Task<? super E>> taskWeightedList = new WeightedList<>();

   public MultiTask(Map<MemoryModuleType<?>, MemoryModuleStatus> requiredMemoryState, Set<MemoryModuleType<?>> memModuleSet, MultiTask.Ordering ordering, MultiTask.RunType runType,
                    List<Pair<Task<? super E>, Integer>> taskList) {
      super(requiredMemoryState);
      this.memModuleSet = memModuleSet;
      this.ordering = ordering;
      this.runType = runType;
      taskList.forEach((pair) -> {
         this.taskWeightedList.addToList(pair.getFirst(), pair.getSecond());
      });
   }

   protected boolean shouldContinueExecuting(ServerWorld worldIn, E entityIn, long gameTimeIn) {
      return this.taskWeightedList.entStream().filter((task) -> {
         return task.getStatus() == Task.Status.RUNNING;
      }).anyMatch((task) -> {
         return task.shouldContinueExecuting(worldIn, entityIn, gameTimeIn);
      });
   }

   protected boolean isTimedOut(long gameTime) {
      return false;
   }

   protected void startExecuting(ServerWorld worldIn, E entityIn, long gameTimeIn) {
      this.ordering.applyOrderingToList(this.taskWeightedList);
      this.runType.runTasks(this.taskWeightedList, worldIn, entityIn, gameTimeIn);
   }

   protected void updateTask(ServerWorld worldIn, E owner, long gameTime) {
      this.taskWeightedList.entStream().filter((task) -> {
         return task.getStatus() == Task.Status.RUNNING;
      }).forEach((task) -> {
         task.tick(worldIn, owner, gameTime);
      });
   }

   protected void resetTask(ServerWorld worldIn, E entityIn, long gameTimeIn) {
      this.taskWeightedList.entStream().filter((task) -> {
         return task.getStatus() == Task.Status.RUNNING;
      }).forEach((task) -> {
         task.stop(worldIn, entityIn, gameTimeIn);
      });
      this.memModuleSet.forEach(entityIn.getBrain()::removeMemory);
   }

   public String toString() {
      Set<? extends Task<? super E>> set = this.taskWeightedList.entStream().filter((task) -> {
         return task.getStatus() == Task.Status.RUNNING;
      }).collect(Collectors.toSet());
      return "(" + this.getClass().getSimpleName() + "): " + set;
   }

   static enum Ordering {
      ORDERED((list) -> {
      }),
      SHUFFLED(WeightedList::getWeighedList);

      private final Consumer<WeightedList<?>> consumer;

      private Ordering(Consumer<WeightedList<?>> consumer) {
         this.consumer = consumer;
      }

      public void applyOrderingToList(WeightedList<?> weightedList) {
         this.consumer.accept(weightedList);
      }
   }

   static enum RunType {
      RUN_ONE {
         public <E extends LivingEntity> void runTasks(WeightedList<Task<? super E>> taskList, ServerWorld world, E entityIn, long gameTime) {
            taskList.entStream().filter((task) -> {
               return task.getStatus() == Task.Status.STOPPED;
            }).filter((task) -> {
               return task.start(world, entityIn, gameTime);
            }).findFirst();
         }
      },
      TRY_ALL {
         public <E extends LivingEntity> void runTasks(WeightedList<Task<? super E>> taskList, ServerWorld world, E entityIn, long gameTime) {
            taskList.entStream().filter((task) -> {
               return task.getStatus() == Task.Status.STOPPED;
            }).forEach((task) -> {
               task.start(world, entityIn, gameTime);
            });
         }
      };

      private RunType() {
      }

      public abstract <E extends LivingEntity> void runTasks(WeightedList<Task<? super E>> p_220630_1_, ServerWorld world, E entityIn, long gameTime);
   }
}
