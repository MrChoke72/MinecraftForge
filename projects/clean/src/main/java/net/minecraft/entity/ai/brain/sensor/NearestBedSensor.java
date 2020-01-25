package net.minecraft.entity.ai.brain.sensor;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.PointOfInterestManager;
import net.minecraft.village.PointOfInterestType;
import net.minecraft.world.server.ServerWorld;

public class NearestBedSensor extends Sensor<MobEntity> {

   //AH REFACTOR
   private final Long2LongMap posByEndTimeMap = new Long2LongOpenHashMap();   //Key is BlockPos packed
                                                                              //Value is taskEndTime for it

   //AH REFACTOR
   private int maxPoiPosToCheck;
   //private int field_225472_b;


   //AH REFACTOR
   private long taskEndTime;
   //private long field_225473_c;

   public NearestBedSensor() {
      super(20);
   }

   public Set<MemoryModuleType<?>> getUsedMemories() {
      return ImmutableSet.of(MemoryModuleType.NEAREST_BED);
   }

   //AH REFACTOR
   protected void update(ServerWorld world, MobEntity entity) {
   //protected void update(ServerWorld p_212872_1_, MobEntity p_212872_2_) {
      if (entity.isChild()) {
         this.maxPoiPosToCheck = 0;
         this.taskEndTime = world.getGameTime() + (long)world.getRandom().nextInt(20);
         PointOfInterestManager pointofinterestmanager = world.getPoiMgr();
         Predicate<BlockPos> predicate = (pos) -> {
            long i = pos.toLong();
            if (this.posByEndTimeMap.containsKey(i)) {
               return false;
            } else if (++this.maxPoiPosToCheck >= 5) {
               return false;
            } else {
               this.posByEndTimeMap.put(i, this.taskEndTime + 40L);
               return true;
            }
         };

         //AH CHANGE - Increase range to look for a bed.  Default is 48
         Stream<BlockPos> stream = pointofinterestmanager.poiStreamByDistFiltPos(PointOfInterestType.HOME.getPoiTypePred(), predicate, new BlockPos(entity), 64, PointOfInterestManager.Status.ANY);
         //Stream<BlockPos> stream = pointofinterestmanager.func_225399_a(PointOfInterestType.HOME.func_221045_c(), predicate, new BlockPos(p_212872_2_), 48, PointOfInterestManager.Status.ANY);

         //AH DEBUG OFF
         /*
         if(entity.getCustomName() != null)
         {
            System.out.println("In NearestBedSensor, update. beds found=" + stream.count());
         }
          */

         Path path = entity.getNavigator().findPath(stream, PointOfInterestType.HOME.getKeepDist());
         if (path != null && path.isCompletePath()) {
            BlockPos blockpos = path.getTargetPos();
            Optional<PointOfInterestType> optional = pointofinterestmanager.getPoiTypeForPos(blockpos);
            if (optional.isPresent()) {
               entity.getBrain().setMemory(MemoryModuleType.NEAREST_BED, blockpos);
            }
         } else if (this.maxPoiPosToCheck < 5) {
            this.posByEndTimeMap.long2LongEntrySet().removeIf((entry) -> {
               return entry.getLongValue() < this.taskEndTime;
            });
         }

      }
   }
}
