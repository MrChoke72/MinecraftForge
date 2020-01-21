package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.server.ServerWorld;

public class WalkTowardsRandomSecondaryPosTask extends Task<VillagerEntity> {
   private final MemoryModuleType<List<GlobalPos>> allJobSites;
   private final MemoryModuleType<GlobalPos> currJobSite;
   private final float speed;
   private final int reachDist;
   private final int distance;
   private long execAgainTime;
   @Nullable
   private GlobalPos selectedJobSite;

   public WalkTowardsRandomSecondaryPosTask(MemoryModuleType<List<GlobalPos>> allJobSites, float speed, int reachDist, int distance, MemoryModuleType<GlobalPos> currJobSite) {
      super(ImmutableMap.of(
              MemoryModuleType.WALK_TARGET, MemoryModuleStatus.REGISTERED,
              allJobSites, MemoryModuleStatus.VALUE_PRESENT,
              currJobSite, MemoryModuleStatus.VALUE_PRESENT));
      this.allJobSites = allJobSites;
      this.speed = speed;
      this.reachDist = reachDist;
      this.distance = distance;
      this.currJobSite = currJobSite;
   }

   protected boolean shouldExecute(ServerWorld worldIn, VillagerEntity owner) {
      Optional<List<GlobalPos>> optional = owner.getBrain().getMemory(this.allJobSites);
      Optional<GlobalPos> optional1 = owner.getBrain().getMemory(this.currJobSite);
      if (optional.isPresent() && optional1.isPresent()) {
         List<GlobalPos> list = optional.get();
         if (!list.isEmpty()) {
            this.selectedJobSite = list.get(worldIn.getRandom().nextInt(list.size()));
            return this.selectedJobSite != null && Objects.equals(worldIn.getDimension().getType(), this.selectedJobSite.getDimension())
                    && optional1.get().getPos().withinDistance(owner.getPositionVec(), (double)this.distance);
         }
      }

      return false;
   }

   protected void startExecuting(ServerWorld worldIn, VillagerEntity entityIn, long gameTimeIn) {
      if (gameTimeIn > this.execAgainTime && this.selectedJobSite != null) {
         entityIn.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(this.selectedJobSite.getPos(), this.speed, this.reachDist));
         this.execAgainTime = gameTimeIn + 100L;
      }

   }
}
