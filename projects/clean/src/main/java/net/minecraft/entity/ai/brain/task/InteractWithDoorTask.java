package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.pathfinding.Path;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.server.ServerWorld;

public class InteractWithDoorTask extends Task<LivingEntity> {
   public InteractWithDoorTask() {
      super(ImmutableMap.of(
              MemoryModuleType.PATH, MemoryModuleStatus.VALUE_PRESENT,
              MemoryModuleType.INTERACTABLE_DOORS, MemoryModuleStatus.VALUE_PRESENT,
              MemoryModuleType.OPENED_DOORS, MemoryModuleStatus.REGISTERED));
   }

   protected void startExecuting(ServerWorld worldIn, LivingEntity entityIn, long gameTimeIn) {
      Brain<?> brain = entityIn.getBrain();
      Path path = brain.getMemory(MemoryModuleType.PATH).get();
      List<GlobalPos> list = brain.getMemory(MemoryModuleType.INTERACTABLE_DOORS).get();
      List<BlockPos> list1 = path.getPoints().stream().map((point) -> {
         return new BlockPos(point.x, point.y, point.z);
      }).collect(Collectors.toList());
      Set<BlockPos> set = this.filterByDimensionAndPath(worldIn, list, list1);
      int i = path.getCurrentPathIndex() - 1;
      this.processDoors(worldIn, list1, set, i, entityIn, brain);
   }

   //AH CHANGE REFACTOR
   private Set<BlockPos> filterByDimensionAndPath(ServerWorld world, List<GlobalPos> doorPosList, List<BlockPos> posList) {
   //private Set<BlockPos> func_220436_a(ServerWorld p_220436_1_, List<GlobalPos> p_220436_2_, List<BlockPos> p_220436_3_) {
      return doorPosList.stream().filter((globalPos) -> {
         return globalPos.getDimension() == world.getDimension().getType();
      }).map(GlobalPos::getPos).filter(posList::contains).collect(Collectors.toSet());
   }

   //AH CHANGE REFACTOR
   private void processDoors(ServerWorld world, List<BlockPos> pathPointList, Set<BlockPos> doorPosSet, int pathIdxMinusOne, LivingEntity entity, Brain<?> brain) {
   //private void func_220434_a(ServerWorld p_220434_1_, List<BlockPos> p_220434_2_, Set<BlockPos> p_220434_3_, int p_220434_4_, LivingEntity p_220434_5_, Brain<?> p_220434_6_) {
      doorPosSet.forEach((doorPos) -> {
         int i = pathPointList.indexOf(doorPos);
         BlockState blockstate = world.getBlockState(doorPos);
         Block block = blockstate.getBlock();
         if (BlockTags.WOODEN_DOORS.contains(block) && block instanceof DoorBlock) {
            boolean flag = i >= pathIdxMinusOne;
            ((DoorBlock)block).toggleDoor(world, doorPos, flag);
            GlobalPos globalpos = GlobalPos.of(world.getDimension().getType(), doorPos);
            if (!brain.getMemory(MemoryModuleType.OPENED_DOORS).isPresent() && flag) {
               brain.setMemory(MemoryModuleType.OPENED_DOORS, Sets.newHashSet(globalpos));
            } else {
               brain.getMemory(MemoryModuleType.OPENED_DOORS).ifPresent((gPosSet) -> {
                  if (flag) {
                     gPosSet.add(globalpos);
                  } else {
                     gPosSet.remove(globalpos);
                  }

               });
            }
         }
      });
      closeOpenedDoors(world, pathPointList, pathIdxMinusOne, entity, brain);
   }

   //AH CHANGE REFACTOR
   public static void closeOpenedDoors(ServerWorld world, List<BlockPos> pathPointList, int pathIdxMinusOne, LivingEntity entity, Brain<?> brain) {
   //public static void func_225449_a(ServerWorld p_225449_0_, List<BlockPos> p_225449_1_, int p_225449_2_, LivingEntity p_225449_3_, Brain<?> p_225449_4_) {
      brain.getMemory(MemoryModuleType.OPENED_DOORS).ifPresent((gPosSet) -> {
         Iterator<GlobalPos> iterator = gPosSet.iterator();

         while(iterator.hasNext()) {
            GlobalPos globalpos = iterator.next();
            BlockPos blockpos = globalpos.getPos();
            int i = pathPointList.indexOf(blockpos);
            if (world.getDimension().getType() != globalpos.getDimension()) {
               iterator.remove();
            } else {
               BlockState blockstate = world.getBlockState(blockpos);
               Block block = blockstate.getBlock();
               if (BlockTags.WOODEN_DOORS.contains(block) && block instanceof DoorBlock && i < pathIdxMinusOne && blockpos.withinDistance(entity.getPositionVec(), 4.0D)) {
                  ((DoorBlock)block).toggleDoor(world, blockpos, false);
                  iterator.remove();
               }
            }
         }

      });
   }
}
