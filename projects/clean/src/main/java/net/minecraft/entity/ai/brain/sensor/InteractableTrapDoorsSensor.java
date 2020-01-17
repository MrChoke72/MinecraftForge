package net.minecraft.entity.ai.brain.sensor;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;

import java.util.List;
import java.util.Set;

//AH ADD NEW CLASS
public class InteractableTrapDoorsSensor extends Sensor<LivingEntity> {

    protected void update(ServerWorld world, LivingEntity entity) {
        DimensionType dimensiontype = world.getDimension().getType();
        BlockPos blockpos = new BlockPos(entity);
        List<GlobalPos> list = Lists.newArrayList();

        int yHighVal;
        if(entity.isChild())
        {
            yHighVal = 1;
        }
        else
        {
            yHighVal = 2;
        }

        for(int i = -1; i <= 1; ++i) {
            for(int j = -1; j <= yHighVal; ++j) {
                for(int k = -1; k <= 1; ++k) {
                    BlockPos blockpos1 = blockpos.add(i, j, k);
                    if (world.getBlockState(blockpos1).isIn(BlockTags.WOODEN_TRAPDOORS)) {
                        list.add(GlobalPos.of(dimensiontype, blockpos1));
                    }
                }
            }
        }

        Brain<?> brain = entity.getBrain();
        if (!list.isEmpty()) {
            brain.setMemory(MemoryModuleType.INTERACTABLE_TRAPDOORS, list);
        } else {
            brain.removeMemory(MemoryModuleType.INTERACTABLE_TRAPDOORS);
        }

    }

    public Set<MemoryModuleType<?>> getUsedMemories() {
        return ImmutableSet.of(MemoryModuleType.INTERACTABLE_TRAPDOORS);
    }

}
