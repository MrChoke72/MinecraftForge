package net.minecraft.pathfinding;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.Region;

public abstract class NodeProcessor {
   protected Region blockaccess;
   protected MobEntity entity;

   //AH INFO:  Key is pathpoint Intger "hash"
   protected final Int2ObjectMap<PathPoint> pointMap = new Int2ObjectOpenHashMap<>();

   protected int entitySizeX;
   protected int entitySizeY;
   protected int entitySizeZ;
   protected boolean canEnterDoors;
   protected boolean canOpenDoors;
   protected boolean canSwim;

   //AH CHANGE REFACTOR
   public void init(Region region, MobEntity entity) {
   //public void func_225578_a_(Region p_225578_1_, MobEntity p_225578_2_) {
      this.blockaccess = region;
      this.entity = entity;
      this.pointMap.clear();
      this.entitySizeX = MathHelper.floor(entity.getWidth() + 1.0F);
      this.entitySizeY = MathHelper.floor(entity.getHeight() + 1.0F);
      this.entitySizeZ = MathHelper.floor(entity.getWidth() + 1.0F);
   }

   public void postProcess() {
      this.blockaccess = null;
      this.entity = null;
   }

   protected PathPoint openPoint(int x, int y, int z) {
      return this.pointMap.computeIfAbsent(PathPoint.makeHash(x, y, z), (point) -> {
         return new PathPoint(x, y, z);
      });
   }

   public abstract PathPoint getStart();

   public abstract FlaggedPathPoint createFlaggedPathPoint(double p_224768_1_, double p_224768_3_, double p_224768_5_);

   public abstract int findPathOptions(PathPoint[] p_222859_1_, PathPoint p_222859_2_);

   public abstract PathNodeType getPathNodeType(IBlockReader blockaccessIn, int x, int y, int z, MobEntity entitylivingIn, int xSize, int ySize, int zSize, boolean canBreakDoorsIn, boolean canEnterDoorsIn);

   public abstract PathNodeType getPathNodeType(IBlockReader blockaccessIn, int x, int y, int z);

   public void setCanEnterDoors(boolean canEnterDoorsIn) {
      this.canEnterDoors = canEnterDoorsIn;
   }

   public void setCanOpenDoors(boolean canOpenDoorsIn) {
      this.canOpenDoors = canOpenDoorsIn;
   }

   public void setCanSwim(boolean canSwimIn) {
      this.canSwim = canSwimIn;
   }

   public boolean getCanEnterDoors() {
      return this.canEnterDoors;
   }

   public boolean getCanOpenDoors() {
      return this.canOpenDoors;
   }

   public boolean getCanSwim() {
      return this.canSwim;
   }
}
