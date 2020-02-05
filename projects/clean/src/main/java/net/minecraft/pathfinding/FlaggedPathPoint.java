package net.minecraft.pathfinding;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class FlaggedPathPoint extends PathPoint {

   //AH CHANGE REFACTOR all 3
   private float closestDistToTgt = Float.MAX_VALUE;
   private PathPoint closestPathPoint;
   private boolean keepPoint;
   //private float field_224765_m = Float.MAX_VALUE;
   //private PathPoint field_224766_n;
   //private boolean field_224767_o;

   //AH REFACTOR
   public FlaggedPathPoint(PathPoint point) {
   //public FlaggedPathPoint(PathPoint p_i51802_1_) {
      super(point.x, point.y, point.z);
   }

   @OnlyIn(Dist.CLIENT)
   public FlaggedPathPoint(int p_i51803_1_, int p_i51803_2_, int p_i51803_3_) {
      super(p_i51803_1_, p_i51803_2_, p_i51803_3_);
   }

   //AH REFACTOR
   public void setClosestPoint(float distToTgt, PathPoint point) {
   //public void func_224761_a(float p_224761_1_, PathPoint p_224761_2_) {
      if (distToTgt < this.closestDistToTgt) {
         this.closestDistToTgt = distToTgt;
         this.closestPathPoint = point;
      }

   }

   //AH CHANGE REFACTOR
   public PathPoint getClosestPathPoint() {
   //public PathPoint func_224763_d() {
      return this.closestPathPoint;
   }

   //AH CHANGE REFACTOR
   public void setKeepPoint() {
   //public void func_224764_e() {
      this.keepPoint = true;
   }

   //AH CHANGE REFACTOR
   public boolean isKeepPoint() {
   //public boolean func_224762_f() {
      return this.keepPoint;
   }

   @OnlyIn(Dist.CLIENT)
   public static FlaggedPathPoint func_224760_c(PacketBuffer p_224760_0_) {
      FlaggedPathPoint flaggedpathpoint = new FlaggedPathPoint(p_224760_0_.readInt(), p_224760_0_.readInt(), p_224760_0_.readInt());
      flaggedpathpoint.accumDistance = p_224760_0_.readFloat();
      flaggedpathpoint.costMalus = p_224760_0_.readFloat();
      flaggedpathpoint.visited = p_224760_0_.readBoolean();
      flaggedpathpoint.nodeType = PathNodeType.values()[p_224760_0_.readInt()];
      flaggedpathpoint.distFromStartPlusMalus = p_224760_0_.readFloat();
      return flaggedpathpoint;
   }
}
