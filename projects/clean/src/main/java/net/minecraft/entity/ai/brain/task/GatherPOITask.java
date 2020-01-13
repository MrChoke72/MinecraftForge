package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.network.DebugPacketSender;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.village.PointOfInterestManager;
import net.minecraft.village.PointOfInterestType;
import net.minecraft.world.server.ServerWorld;

public class GatherPOITask extends Task<CreatureEntity> {

   //AH REFACTOR
   private final PointOfInterestType poiType;
   //private final PointOfInterestType field_220604_a;

   //AH REFACTOR
   private final MemoryModuleType<GlobalPos> memModuleType;
   //private final MemoryModuleType<GlobalPos> field_220605_b;


   //AH REFACTOR
   private final boolean mustBeAdult;
   //private final boolean field_220606_c;


   //AH REFACTOR
   private long taskEndTime;
   //private long field_220607_d;

   //AH REFACTOR
   private final Long2LongMap posByEndTimeMap = new Long2LongOpenHashMap();    //Key is BlockPos packed
                                                                              //Value is taskEndTime for it
   //private final Long2LongMap field_223013_e = new Long2LongOpenHashMap();

   //AH REFACTOR
   private int maxPoiPosToCheck;
   //private int field_223014_f;

   //AH REFACTOR
   public GatherPOITask(PointOfInterestType poiType, MemoryModuleType<GlobalPos> memModuleType, boolean mustBeAdult) {
   //public GatherPOITask(PointOfInterestType p_i50374_1_, MemoryModuleType<GlobalPos> p_i50374_2_, boolean p_i50374_3_) {
      super(ImmutableMap.of(memModuleType, MemoryModuleStatus.VALUE_ABSENT));
      this.poiType = poiType;
      this.memModuleType = memModuleType;
      this.mustBeAdult = mustBeAdult;
   }

   protected boolean shouldExecute(ServerWorld worldIn, CreatureEntity owner) {
      if (this.mustBeAdult && owner.isChild()) {
         return false;
      } else {
         return worldIn.getGameTime() - this.taskEndTime >= 20L;
      }
   }

   protected void startExecuting(ServerWorld worldIn, CreatureEntity entityIn, long gameTimeIn) {
      this.maxPoiPosToCheck = 0;
      this.taskEndTime = worldIn.getGameTime() + (long)worldIn.getRandom().nextInt(20);
      PointOfInterestManager pointofinterestmanager = worldIn.getPoiMgr();
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
      Stream<BlockPos> stream = pointofinterestmanager.poiStreamByDistFiltPos(this.poiType.getPoiTypePred(), predicate, new BlockPos(entityIn), 48, PointOfInterestManager.Status.HAS_SPACE);
      Path path = entityIn.getNavigator().findPath(stream, this.poiType.getKeepDist());
      if (path != null && path.isCompletePath()) {
         BlockPos blockpos = path.getTargetPos();
         pointofinterestmanager.getPoiTypeForPos(blockpos).ifPresent((poiType) -> {
            pointofinterestmanager.claimPoiPos(this.poiType.getPoiTypePred(), (pos) -> {
               return pos.equals(blockpos);
            }, blockpos, 1);
            entityIn.getBrain().setMemory(this.memModuleType, GlobalPos.of(worldIn.getDimension().getType(), blockpos));

            //AH CHANGE DEBUG
           if(entityIn.getCustomName() != null && entityIn.getCustomName().getString().equals("Chuck"))
           {
               System.out.println("GatherPOITask, claimed: " + blockpos.toString() + ", type=" + this.poiType);
           }

            DebugPacketSender.func_218801_c(worldIn, blockpos);
         });
      } else if (this.maxPoiPosToCheck < 5) {
         this.posByEndTimeMap.long2LongEntrySet().removeIf((p_223011_1_) -> {
            return p_223011_1_.getLongValue() < this.taskEndTime;
         });
      }

   }
}
