package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.network.DebugPacketSender;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.PointOfInterestManager;
import net.minecraft.village.PointOfInterestType;
import net.minecraft.world.server.ServerWorld;

public class WalkToHouseTask extends Task<LivingEntity> {

   //AH REFACTOR
   private final float moveSpeed;
   //private final float field_220524_a;

   //AH REFACTOR
   //Key: Long: BlockPos packed
   //Value:
   private final Long2LongMap posEndTimeMap = new Long2LongOpenHashMap();
   //private final Long2LongMap field_225455_b = new Long2LongOpenHashMap();

   //AH REFACTOR
   private int findPoiLimit;
   //private int field_225456_c;

   private long startTime;

   //AH REFACTOR
   public WalkToHouseTask(float moveSpeed) {
      //public WalkToHouseTask(float p_i50353_1_) {
      super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryModuleStatus.VALUE_ABSENT, MemoryModuleType.HOME, MemoryModuleStatus.VALUE_ABSENT));
      this.moveSpeed = moveSpeed;
   }

   protected boolean shouldExecute(ServerWorld worldIn, LivingEntity owner) {
      if (worldIn.getGameTime() - this.startTime < 20L) {
         return false;
      } else {
         CreatureEntity creatureentity = (CreatureEntity)owner;
         PointOfInterestManager pointofinterestmanager = worldIn.getPointOfInterestManager();

         //AH DEBUG
         if(owner.getCustomName() != null)
         {
            System.out.println("In WalkToHouseTask, should execute");
         }
         
         //AH CHANGE REFACTOR
         Optional<BlockPos> optional = pointofinterestmanager.getPoiPosInRange(PointOfInterestType.HOME.getPoiTypePred(), (posPred) -> {
            //Optional<BlockPos> optional = pointofinterestmanager.getPoiPosInRange(PointOfInterestType.HOME.getPoiTypePred(), (p_220522_0_) -> {
            return true;

            //AH CHANGE - increase range form 48 to 64
         }, new BlockPos(owner), 64, PointOfInterestManager.Status.ANY);
         //}, new BlockPos(owner), 48, PointOfInterestManager.Status.ANY);


         return optional.isPresent() && !(optional.get().distanceSq(new BlockPos(creatureentity)) <= 4.0D);
      }
   }

   protected void startExecuting(ServerWorld worldIn, LivingEntity entityIn, long gameTimeIn) {
      this.findPoiLimit = 0;
      this.startTime = worldIn.getGameTime() + (long)worldIn.getRandom().nextInt(20);
      CreatureEntity creatureentity = (CreatureEntity)entityIn;
      PointOfInterestManager pointofinterestmanager = worldIn.getPointOfInterestManager();
      Predicate<BlockPos> predicate = (pos) -> {
         long i = pos.toLong();
         if (this.posEndTimeMap.containsKey(i)) {
            return false;
         } else if (++this.findPoiLimit >= 5) {
            return false;
         } else {
            this.posEndTimeMap.put(i, this.startTime + 40L);
            return true;
         }
      };

      //AH CHANGE DEBUG
      if(entityIn.getCustomName() != null)
      {
         System.out.println("WalkToHouseTask, in startExecuting");
      }

      //AH CHANGE - increase home search from 48 to 64
      Stream<BlockPos> stream = pointofinterestmanager.poiStreamByDistFiltPos(PointOfInterestType.HOME.getPoiTypePred(), predicate, new BlockPos(entityIn), 64, PointOfInterestManager.Status.ANY);
      //Stream<BlockPos> stream = pointofinterestmanager.poiStreamByDistFiltPos(PointOfInterestType.HOME.getPoiTypePred(), predicate, new BlockPos(entityIn), 48, PointOfInterestManager.Status.ANY);

      Path path = creatureentity.getNavigator().findPath(stream, PointOfInterestType.HOME.getKeepDist());
      if (path != null && path.isCompletePath()) {
         BlockPos blockpos = path.getTargetPos();
         Optional<PointOfInterestType> optional = pointofinterestmanager.getPoiTypeForPos(blockpos);
         if (optional.isPresent()) {
            entityIn.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(blockpos, this.moveSpeed, 1));
            DebugPacketSender.func_218801_c(worldIn, blockpos);
         }
      } else if (this.findPoiLimit < 5) {
         this.posEndTimeMap.long2LongEntrySet().removeIf((entry) -> {
            return entry.getLongValue() < this.startTime;
         });
      }

   }
}
