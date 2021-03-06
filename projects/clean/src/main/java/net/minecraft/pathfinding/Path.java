package net.minecraft.pathfinding;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class Path {

   //AH CHANGE REFACTOR
   private final List<PathPoint> points;
   //private final List<PathPoint> field_75884_a;

   private PathPoint[] openSet = new PathPoint[0];
   private PathPoint[] closedSet = new PathPoint[0];

   @OnlyIn(Dist.CLIENT)
   //AH REFACTOR
   private Set<FlaggedPathPoint> pointSet;
   //private Set<FlaggedPathPoint> field_224772_d;

   private int currentPathIndex;
   private final BlockPos target;

   //AH REFACTOR
   private final float finalPointTgtDist;
   //private final float field_224773_g;

   //AH REFACTOR
   private final boolean completePath;
   //private final boolean field_224774_h;

   //AH CHANGE REFACTOR
   public Path(List<PathPoint> points, BlockPos tgtPos, boolean completePath) {
   //public Path(List<PathPoint> p_i51804_1_, BlockPos p_i51804_2_, boolean p_i51804_3_) {
      this.points = points;
      this.target = tgtPos;
      this.finalPointTgtDist = points.isEmpty() ? Float.MAX_VALUE : this.points.get(this.points.size() - 1).pointDist(this.target);
      this.completePath = completePath;
   }

   public void incrementPathIndex() {
      ++this.currentPathIndex;
   }

   public boolean isFinished() {
      return this.currentPathIndex >= this.points.size();
   }

   @Nullable
   public PathPoint getFinalPathPoint() {
      return !this.points.isEmpty() ? this.points.get(this.points.size() - 1) : null;
   }

   public PathPoint getPathPointFromIndex(int index) {
      return this.points.get(index);
   }

   //AH CHANGE REFACTOR
   public List<PathPoint> getPoints() {
   //public List<PathPoint> func_215746_d() {
      return this.points;
   }

   public void setCurrentPathLength(int len) {
      if (this.points.size() > len) {
         this.points.subList(len, this.points.size()).clear();
      }

   }

   public void setPoint(int index, PathPoint point) {
      this.points.set(index, point);
   }

   public int getCurrentPathLength() {
      return this.points.size();
   }

   public int getCurrentPathIndex() {
      return this.currentPathIndex;
   }

   public void setCurrentPathIndex(int currentPathIndexIn) {
      this.currentPathIndex = currentPathIndexIn;
   }

   public Vec3d getVectorFromIndex(Entity entityIn, int index) {
      PathPoint pathpoint = this.points.get(index);

      //AH CHANGE ****** - Fix entityWidth > 1 pathing problem (Iron golem)
      if(entityIn.getWidth() >= 1.0F)
      {
         if(index > 0)
         {
            PathPoint prevpoint = this.points.get(index-1);
            double d0 = (double)pathpoint.x + (((double)((int)(entityIn.getWidth() + 1.0F)) * 0.5D) * Math.signum((pathpoint.x) - prevpoint.x));
            double d2 = (double)pathpoint.z + (((double)((int)(entityIn.getWidth() + 1.0F)) * 0.5D) * Math.signum((pathpoint.z) - prevpoint.z));
            return new Vec3d(d0, pathpoint.y, d2);
         }
         else
         {
            double d0 = (double)pathpoint.x + (double)((int)(entityIn.getWidth() + 1.0F)) * 0.5D;
            double d2 = (double)pathpoint.z + (double)((int)(entityIn.getWidth() + 1.0F)) * 0.5D;
            return new Vec3d(d0, pathpoint.y, d2);
         }
      }
      else {
         double d0 = (double)pathpoint.x + (double)((int)(entityIn.getWidth() + 1.0F)) * 0.5D;
         double d2 = (double)pathpoint.z + (double)((int)(entityIn.getWidth() + 1.0F)) * 0.5D;
         return new Vec3d(d0, pathpoint.y, d2);
      }
      //AH CHANGE END ******
      //Vanilla
      /*
      double d0 = (double)pathpoint.x + (double)((int)(entityIn.getWidth() + 1.0F)) * 0.5D;
      double d1 = (double)pathpoint.y;
      double d2 = (double)pathpoint.z + (double)((int)(entityIn.getWidth() + 1.0F)) * 0.5D;
      return new Vec3d(d0, d1, d2);
       */

   }

   public Vec3d getPosition(Entity entityIn) {
      return this.getVectorFromIndex(entityIn, this.currentPathIndex);
   }

   public Vec3d getCurrentPos() {
      PathPoint pathpoint = this.points.get(this.currentPathIndex);
      return new Vec3d((double)pathpoint.x, (double)pathpoint.y, (double)pathpoint.z);
   }

   public boolean isSamePath(@Nullable Path pathentityIn) {
      if (pathentityIn == null) {
         return false;
      } else if (pathentityIn.points.size() != this.points.size()) {
         return false;
      } else {
         for(int i = 0; i < this.points.size(); ++i) {
            PathPoint pathpoint = this.points.get(i);
            PathPoint pathpoint1 = pathentityIn.points.get(i);
            if (pathpoint.x != pathpoint1.x || pathpoint.y != pathpoint1.y || pathpoint.z != pathpoint1.z) {
               return false;
            }
         }

         return true;
      }
   }

   //AH REFACTOR
   public boolean isCompletePath() {
   //public boolean func_224771_h() {
      return this.completePath;
   }

   @OnlyIn(Dist.CLIENT)
   public PathPoint[] getOpenSet() {
      return this.openSet;
   }

   @OnlyIn(Dist.CLIENT)
   public PathPoint[] getClosedSet() {
      return this.closedSet;
   }

   @OnlyIn(Dist.CLIENT)
   public static Path read(PacketBuffer buf) {
      boolean flag = buf.readBoolean();
      int i = buf.readInt();
      int j = buf.readInt();
      Set<FlaggedPathPoint> set = Sets.newHashSet();

      for(int k = 0; k < j; ++k) {
         set.add(FlaggedPathPoint.func_224760_c(buf));
      }

      BlockPos blockpos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
      List<PathPoint> list = Lists.newArrayList();
      int l = buf.readInt();

      for(int i1 = 0; i1 < l; ++i1) {
         list.add(PathPoint.createFromBuffer(buf));
      }

      PathPoint[] apathpoint = new PathPoint[buf.readInt()];

      for(int j1 = 0; j1 < apathpoint.length; ++j1) {
         apathpoint[j1] = PathPoint.createFromBuffer(buf);
      }

      PathPoint[] apathpoint1 = new PathPoint[buf.readInt()];

      for(int k1 = 0; k1 < apathpoint1.length; ++k1) {
         apathpoint1[k1] = PathPoint.createFromBuffer(buf);
      }

      Path path = new Path(list, blockpos, flag);
      path.openSet = apathpoint;
      path.closedSet = apathpoint1;
      path.pointSet = set;
      path.currentPathIndex = i;
      return path;
   }

   public String toString() {
      return "Path(length=" + this.points.size() + ")";
   }

   //AH REFACTOR
   public BlockPos getTargetPos() {
   //public BlockPos func_224770_k() {
      return this.target;
   }

   //AH REFACTOR
   public float getFinalPointTgtDist() {
      return this.finalPointTgtDist;
   }
}
