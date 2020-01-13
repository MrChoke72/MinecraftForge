package net.minecraft.pathfinding;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Region;

public class PathFinder {
   private final PathHeap path = new PathHeap();
   private final Set<PathPoint> closedSet = Sets.newHashSet();
   private final PathPoint[] pathOptions = new PathPoint[32];

   //AH CHANGE REFACTOR
   private final int followRangeMult16;
   //private final int field_215751_d;

   private final NodeProcessor nodeProcessor;

   public PathFinder(NodeProcessor p_i51280_1_, int p_i51280_2_) {
      this.nodeProcessor = p_i51280_1_;
      this.followRangeMult16 = p_i51280_2_;
   }

   @Nullable
   //AH CHANGE REFACTOR
   public Path findPath(Region region, MobEntity entity, Set<BlockPos> finalTgtSet, float followRange, int keepDist, float iterMaxMult) {
   //public Path func_227478_a_(Region p_227478_1_, MobEntity p_227478_2_, Set<BlockPos> p_227478_3_, float p_227478_4_, int p_227478_5_, float p_227478_6_) {
      this.path.clearPath();
      this.nodeProcessor.init(region, entity);

      //AH CHANGE DEBUG OFF
      if(this.nodeProcessor.entity.getCustomName() != null) // && this.entity.getCustomName().getString().equals("Chuck"))
      {
         System.out.println("findPath before getStart");
      }


      PathPoint pathpoint = this.nodeProcessor.getStart();
      Map<FlaggedPathPoint, BlockPos> map = finalTgtSet.stream().collect(Collectors.toMap((pos) -> {
         return this.nodeProcessor.createFlaggedPathPoint((double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
      }, Function.identity()));
      Path path = this.findPath(pathpoint, map, followRange, keepDist, iterMaxMult);
      this.nodeProcessor.postProcess();
      return path;
   }

   @Nullable
   //AH CHANGE REFACTOR
   private Path findPath(PathPoint startPoint, Map<FlaggedPathPoint, BlockPos> tgtPointMap, float followRange, int keepDist, float iterMaxMult) {
   //private Path func_227479_a_(PathPoint p_227479_1_, Map<FlaggedPathPoint, BlockPos> p_227479_2_, float p_227479_3_, int p_227479_4_, float p_227479_5_) {

      //AH CHANGE DEBUG OFF
      /*
      if(this.nodeProcessor.entity.getCustomName() != null && this.nodeProcessor.entity.getCustomName().getString().equals("Chuck"))
      {
         System.out.println("findPath before loop.  finalTgtPoint=" + startPoint.x + "," + startPoint.y + "," + startPoint.z);
      }
       */

      Set<FlaggedPathPoint> flagPointSet = tgtPointMap.keySet();
      startPoint.distFromStartPlusMalus = 0.0F;
      startPoint.closestDistToAnyTgt = this.getClosestDistInSet(startPoint, flagPointSet);
      startPoint.distCloestPlusStart = startPoint.closestDistToAnyTgt;
      this.path.clearPath();
      this.closedSet.clear();
      this.path.addPoint(startPoint);
      int i = 0;
      int j = (int)((float)this.followRangeMult16 * iterMaxMult);

      while(!this.path.isPathEmpty()) {
         ++i;
         if (i >= j) {
            break;
         }

         PathPoint pathpoint = this.path.dequeue();
         pathpoint.visited = true;
         flagPointSet.stream().filter((flagPoint) -> {
            return pathpoint.pointDist(flagPoint) <= (float)keepDist;
         }).forEach(FlaggedPathPoint::setKeepPoint);
         if (flagPointSet.stream().anyMatch(FlaggedPathPoint::isKeepPoint)) {
            break;
         }

         if (!(pathpoint.distanceTo(startPoint) >= followRange)) {
            int k = this.nodeProcessor.findPathOptions(this.pathOptions, pathpoint);

            for(int l = 0; l < k; ++l) {
               PathPoint pathpoint1 = this.pathOptions[l];
               float f = pathpoint.distanceTo(pathpoint1);
               pathpoint1.accumDistance = pathpoint.accumDistance + f;
               float f1 = pathpoint.distFromStartPlusMalus + f + pathpoint1.costMalus;
               if (pathpoint1.accumDistance < followRange && (!pathpoint1.isAssigned() || f1 < pathpoint1.distFromStartPlusMalus)) {
                  pathpoint1.previous = pathpoint;
                  pathpoint1.distFromStartPlusMalus = f1;
                  pathpoint1.closestDistToAnyTgt = this.getClosestDistInSet(pathpoint1, flagPointSet) * 1.5F;
                  if (pathpoint1.isAssigned()) {
                     this.path.changeDistance(pathpoint1, pathpoint1.distFromStartPlusMalus + pathpoint1.closestDistToAnyTgt);
                  } else {
                     pathpoint1.distCloestPlusStart = pathpoint1.distFromStartPlusMalus + pathpoint1.closestDistToAnyTgt;
                     this.path.addPoint(pathpoint1);
                  }
               }
            }
         }
      }

      Stream<Path> pathStream;
      if (flagPointSet.stream().anyMatch(FlaggedPathPoint::isKeepPoint)) {
         pathStream = flagPointSet.stream().filter(FlaggedPathPoint::isKeepPoint).map((flagPoint) -> {
            return this.createPath(flagPoint.getClosestPathPoint(), tgtPointMap.get(flagPoint), true);
         }).sorted(Comparator.comparingInt(Path::getCurrentPathLength));
      } else {
         pathStream = flagPointSet.stream().map((flagPoint) -> {
            return this.createPath(flagPoint.getClosestPathPoint(), tgtPointMap.get(flagPoint), false);
         }).sorted(Comparator.comparingDouble(Path::getFinalPointTgtDist).thenComparingInt(Path::getCurrentPathLength));
      }

      Optional<Path> optional = pathStream.findFirst();
      if (!optional.isPresent()) {
         return null;
      } else {
         Path path = optional.get();
         return path;
      }
   }

   //AH CHANGE REFACTOR
   private float getClosestDistInSet(PathPoint point, Set<FlaggedPathPoint> flagPointSet) {
   //private float func_224776_a(PathPoint p_224776_1_, Set<FlaggedPathPoint> p_224776_2_) {
      float f = Float.MAX_VALUE;

      for(FlaggedPathPoint flaggedpathpoint : flagPointSet) {
         float f1 = point.distanceTo(flaggedpathpoint);
         flaggedpathpoint.setClosestPoint(f1, point);
         f = Math.min(f1, f);
      }

      return f;
   }

   //AH CHANGE REFACTOR
   private Path createPath(PathPoint closestPoint, BlockPos tgtPos, boolean completePath) {
   //private Path func_224780_a(PathPoint p_224780_1_, BlockPos p_224780_2_, boolean p_224780_3_) {
      List<PathPoint> list = Lists.newArrayList();
      PathPoint pathpoint = closestPoint;
      list.add(0, closestPoint);

      while(pathpoint.previous != null) {
         pathpoint = pathpoint.previous;
         list.add(0, pathpoint);
      }

      return new Path(list, tgtPos, completePath);
   }
}
