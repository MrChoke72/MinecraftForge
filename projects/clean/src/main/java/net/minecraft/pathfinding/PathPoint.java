package net.minecraft.pathfinding;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PathPoint {
   public final int x;
   public final int y;
   public final int z;
   private final int hash;
   public int index = -1;

   //AH REFACTOR
   public float distFromStartPlusMalus;
   //public float totalPathDistance;

   //AH REFACTOR
   public float closestDistToAnyTgt;
   //public float distanceToNext;

   //AH REFACTOR
   public float distCloestPlusStart;   //This is var used for path sorting
   //public float distanceToTarget;

   public PathPoint previous;
   public boolean visited;

   //AH CHANGE REFACTOR
   public float accumDistance;
   //public float field_222861_j;

   public float costMalus;
   public PathNodeType nodeType = PathNodeType.BLOCKED;

   public PathPoint(int x, int y, int z) {
      this.x = x;
      this.y = y;
      this.z = z;
      this.hash = makeHash(x, y, z);
   }

   public PathPoint cloneMove(int x, int y, int z) {
      PathPoint pathpoint = new PathPoint(x, y, z);
      pathpoint.index = this.index;
      pathpoint.distFromStartPlusMalus = this.distFromStartPlusMalus;
      pathpoint.closestDistToAnyTgt = this.closestDistToAnyTgt;
      pathpoint.distCloestPlusStart = this.distCloestPlusStart;
      pathpoint.previous = this.previous;
      pathpoint.visited = this.visited;
      pathpoint.accumDistance = this.accumDistance;
      pathpoint.costMalus = this.costMalus;
      pathpoint.nodeType = this.nodeType;
      return pathpoint;
   }

   public static int makeHash(int x, int y, int z) {
      return y & 255 | (x & 32767) << 8 | (z & 32767) << 24 | (x < 0 ? Integer.MIN_VALUE : 0) | (z < 0 ? '\u8000' : 0);
   }

   public float distanceTo(PathPoint pathpointIn) {
      float f = (float)(pathpointIn.x - this.x);
      float f1 = (float)(pathpointIn.y - this.y);
      float f2 = (float)(pathpointIn.z - this.z);
      return MathHelper.sqrt(f * f + f1 * f1 + f2 * f2);
   }

   public float distanceToSquared(PathPoint pathpointIn) {
      float f = (float)(pathpointIn.x - this.x);
      float f1 = (float)(pathpointIn.y - this.y);
      float f2 = (float)(pathpointIn.z - this.z);
      return f * f + f1 * f1 + f2 * f2;
   }

   //AH CHANGE REFACTOR
   public float pointDist(PathPoint point) {
   //public float func_224757_c(PathPoint p_224757_1_) {
      float f = (float)Math.abs(point.x - this.x);
      float f1 = (float)Math.abs(point.y - this.y);
      float f2 = (float)Math.abs(point.z - this.z);
      return f + f1 + f2;
   }

   //AH REFACTOR
   public float pointDist(BlockPos pos) {
   //public float func_224758_c(BlockPos p_224758_1_) {
      float f = (float)Math.abs(pos.getX() - this.x);
      float f1 = (float)Math.abs(pos.getY() - this.y);
      float f2 = (float)Math.abs(pos.getZ() - this.z);
      return f + f1 + f2;
   }

   @OnlyIn(Dist.CLIENT)
   public BlockPos func_224759_a() {
      return new BlockPos(this.x, this.y, this.z);
   }

   public boolean equals(Object p_equals_1_) {
      if (!(p_equals_1_ instanceof PathPoint)) {
         return false;
      } else {
         PathPoint pathpoint = (PathPoint)p_equals_1_;
         return this.hash == pathpoint.hash && this.x == pathpoint.x && this.y == pathpoint.y && this.z == pathpoint.z;
      }
   }

   public int hashCode() {
      return this.hash;
   }

   public boolean isAssigned() {
      return this.index >= 0;
   }

   public String toString() {
      return "Node{x=" + this.x + ", y=" + this.y + ", z=" + this.z + '}';
   }

   @OnlyIn(Dist.CLIENT)
   public static PathPoint createFromBuffer(PacketBuffer buf) {
      PathPoint pathpoint = new PathPoint(buf.readInt(), buf.readInt(), buf.readInt());
      pathpoint.accumDistance = buf.readFloat();
      pathpoint.costMalus = buf.readFloat();
      pathpoint.visited = buf.readBoolean();
      pathpoint.nodeType = PathNodeType.values()[buf.readInt()];
      pathpoint.distCloestPlusStart = buf.readFloat();
      return pathpoint;
   }
}
