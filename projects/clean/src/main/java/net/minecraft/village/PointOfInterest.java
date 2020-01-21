package net.minecraft.village;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Objects;
import net.minecraft.util.IDynamicSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

public class PointOfInterest implements IDynamicSerializable {
   private final BlockPos pos;
   private final PointOfInterestType type;
   private int freeTickets;
   private final Runnable onChange;

   private PointOfInterest(BlockPos pos, PointOfInterestType poiType, int freeTickets, Runnable onChange) {
      this.pos = pos.toImmutable();
      this.type = poiType;
      this.freeTickets = freeTickets;
      this.onChange = onChange;
   }

   public PointOfInterest(BlockPos pos, PointOfInterestType poiType, Runnable onChange) {
      this(pos, poiType, poiType.getMaxFreeTickets(), onChange);
   }

   public <T> PointOfInterest(Dynamic<T> p_i50297_1_, Runnable p_i50297_2_) {
      this(p_i50297_1_.get("pos").map(BlockPos::deserialize).orElse(new BlockPos(0, 0, 0)), Registry.POINT_OF_INTEREST_TYPE.getOrDefault(new ResourceLocation(p_i50297_1_.get("type").asString(""))), p_i50297_1_.get("free_tickets").asInt(0), p_i50297_2_);
   }

   public <T> T serialize(DynamicOps<T> p_218175_1_) {
      return p_218175_1_.createMap(ImmutableMap.of(p_218175_1_.createString("pos"), this.pos.serialize(p_218175_1_), p_218175_1_.createString("type"), p_218175_1_.createString(Registry.POINT_OF_INTEREST_TYPE.getKey(this.type).toString()), p_218175_1_.createString("free_tickets"), p_218175_1_.createInt(this.freeTickets)));
   }

   protected boolean claim() {
      if (this.freeTickets <= 0) {
         return false;
      } else {
         --this.freeTickets;
         this.onChange.run();
         return true;
      }
   }

   protected boolean release() {
      if (this.freeTickets >= this.type.getMaxFreeTickets()) {
         return false;
      } else {
         ++this.freeTickets;
         this.onChange.run();
         return true;
      }
   }

   public boolean hasSpace() {
      return this.freeTickets > 0;
   }

   public boolean isOccupied() {
      return this.freeTickets != this.type.getMaxFreeTickets();
   }

   public BlockPos getPos() {
      return this.pos;
   }

   public PointOfInterestType getType() {
      return this.type;
   }

   public boolean equals(Object p_equals_1_) {
      if (this == p_equals_1_) {
         return true;
      } else {
         return p_equals_1_ != null && this.getClass() == p_equals_1_.getClass() ? Objects.equals(this.pos, ((PointOfInterest)p_equals_1_).pos) : false;
      }
   }

   public int hashCode() {
      return this.pos.hashCode();
   }
}
