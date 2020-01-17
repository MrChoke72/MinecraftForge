package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.TrapDoorBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.pathfinding.Path;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.server.ServerWorld;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

//AH NEW CLASS
public class InteractWithTrapDoorTask extends Task<LivingEntity> {

    public InteractWithTrapDoorTask() {
        super(ImmutableMap.of(
                MemoryModuleType.PATH, MemoryModuleStatus.VALUE_PRESENT,
                MemoryModuleType.INTERACTABLE_TRAPDOORS, MemoryModuleStatus.VALUE_PRESENT,
                MemoryModuleType.OPENED_TRAPDOORS, MemoryModuleStatus.REGISTERED));
    }

    protected void startExecuting(ServerWorld worldIn, LivingEntity entityIn, long gameTimeIn) {
        Brain<?> brain = entityIn.getBrain();
        Path path = brain.getMemory(MemoryModuleType.PATH).get();
        List<GlobalPos> list = brain.getMemory(MemoryModuleType.INTERACTABLE_TRAPDOORS).get();
        List<BlockPos> list1 = path.getPoints().stream().map((point) -> {
            return new BlockPos(point.x, point.y, point.z);
        }).collect(Collectors.toList());
        Set<BlockPos> set = this.filterByDimensionAndPath(worldIn, list, list1);
        int i = path.getCurrentPathIndex() - 1;
        this.processDoors(worldIn, list1, set, i, entityIn, brain);
    }

    private Set<BlockPos> filterByDimensionAndPath(ServerWorld world, List<GlobalPos> doorPosList, List<BlockPos> posList) {
        return doorPosList.stream().filter((globalPos) -> {
            return globalPos.getDimension() == world.getDimension().getType();
        }).map(GlobalPos::getPos).filter(posList::contains).collect(Collectors.toSet());
    }

    private void processDoors(ServerWorld world, List<BlockPos> pathPointList, Set<BlockPos> doorPosSet, int pathIdxMinusOne, LivingEntity entity, Brain<?> brain) {
        doorPosSet.forEach((doorPos) -> {
            int i = pathPointList.indexOf(doorPos);
            BlockState blockstate = world.getBlockState(doorPos);
            Block block = blockstate.getBlock();
            if (BlockTags.WOODEN_TRAPDOORS.contains(block) && block instanceof TrapDoorBlock) {
                boolean flag = i >= pathIdxMinusOne;
                ((TrapDoorBlock)block).toggleDoor(world, doorPos, flag);
                GlobalPos globalpos = GlobalPos.of(world.getDimension().getType(), doorPos);
                if (!brain.getMemory(MemoryModuleType.OPENED_TRAPDOORS).isPresent() && flag) {
                    brain.setMemory(MemoryModuleType.OPENED_TRAPDOORS, Sets.newHashSet(globalpos));
                } else {
                    brain.getMemory(MemoryModuleType.OPENED_TRAPDOORS).ifPresent((gPosSet) -> {
                        if (flag) {
                            gPosSet.add(globalpos);
                        } else {
                            gPosSet.remove(globalpos);
                        }

                    });
                }
            }
        });
        closeOpenedTrapDoors(world, pathPointList, pathIdxMinusOne, entity, brain);
    }

    public static void closeOpenedTrapDoors(ServerWorld world, List<BlockPos> pathPointList, int pathIdxMinusOne, LivingEntity entity, Brain<?> brain) {
        brain.getMemory(MemoryModuleType.OPENED_TRAPDOORS).ifPresent((gPosSet) -> {
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
                    if (BlockTags.WOODEN_TRAPDOORS.contains(block) && block instanceof TrapDoorBlock && i < pathIdxMinusOne && blockpos.withinDistance(entity.getPositionVec(), 4.0D)) {
                        ((TrapDoorBlock)block).toggleDoor(world, blockpos, false);
                        iterator.remove();
                    }
                }
            }

        });
    }

}
