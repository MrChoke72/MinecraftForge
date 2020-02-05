package net.minecraft.village;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.util.IDynamicSerializable;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.SectionPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PointOfInterestData implements IDynamicSerializable {

   //AH REFACTOR
   private static final Logger LOGGER = LogManager.getLogger();
   //private static final Logger field_218255_a = LogManager.getLogger();

   //AH REFACTOR
   private final Short2ObjectMap<PointOfInterest> poiLocationMap = new Short2ObjectOpenHashMap<>();   //Key is SectionPos packed
   //private final Short2ObjectMap<PointOfInterest> field_218256_b = new Short2ObjectOpenHashMap<>();

   //AH REFACTOR
   private final Map<PointOfInterestType, Set<PointOfInterest>> poiSetsByTypeMap = Maps.newHashMap();
   //private final Map<PointOfInterestType, Set<PointOfInterest>> field_218257_c = Maps.newHashMap();

   private final Runnable onChange;
   private boolean valid;

   public PointOfInterestData(Runnable p_i50293_1_) {
      this.onChange = p_i50293_1_;
      this.valid = true;
   }

   public <T> PointOfInterestData(Runnable p_i50294_1_, Dynamic<T> p_i50294_2_) {
      this.onChange = p_i50294_1_;

      try {
         this.valid = p_i50294_2_.get("Valid").asBoolean(false);
         p_i50294_2_.get("Records").asStream().forEach((p_218249_2_) -> {
            this.addToMap(new PointOfInterest(p_218249_2_, p_i50294_1_));
         });
      } catch (Exception exception) {
         LOGGER.error("Failed to load POI chunk", (Throwable)exception);
         this.clear();
         this.valid = false;
      }

   }

   //AH REFACTOR
   public Stream<PointOfInterest> poiStreamByPoiTypePredStatus(Predicate<PointOfInterestType> poiTypePred, PointOfInterestManager.Status status) {
   //public Stream<PointOfInterest> func_218247_a(Predicate<PointOfInterestType> p_218247_1_, PointOfInterestManager.Status p_218247_2_) {
      return this.poiSetsByTypeMap.entrySet().stream().filter((entry) -> {
         return poiTypePred.test(entry.getKey());
      }).flatMap((entry) -> {
         return entry.getValue().stream();
      }).filter(status.getPoiPred());
   }

   //AH REFACTOR
   public void addPoiLocation(BlockPos pos, PointOfInterestType poiType) {
   //public void func_218243_a(BlockPos p_218243_1_, PointOfInterestType p_218243_2_) {
      if (this.addToMap(new PointOfInterest(pos, poiType, this.onChange))) {
         LOGGER.debug("Added POI of type {} @ {}", () -> {
            return poiType;
         }, () -> {
            return pos;
         });
         this.onChange.run();
      }

   }

   //AH REFACTOR
   private boolean addToMap(PointOfInterest poi) {
   //private boolean func_218254_a(PointOfInterest p_218254_1_) {
      BlockPos blockpos = poi.getPos();
      PointOfInterestType pointofinteresttype = poi.getType();
      short short1 = SectionPos.toRelativeOffset(blockpos);
      PointOfInterest pointofinterest = this.poiLocationMap.get(short1);
      if (pointofinterest != null) {
         if (pointofinteresttype.equals(pointofinterest.getType())) {
            return false;
         } else {
            throw (IllegalStateException)Util.spinlockIfDevMode(new IllegalStateException("POI data mismatch: already registered at " + blockpos));
         }
      } else {
         this.poiLocationMap.put(short1, poi);
         this.poiSetsByTypeMap.computeIfAbsent(pointofinteresttype, (p_218252_0_) -> {
            return Sets.newHashSet();
         }).add(poi);
         return true;
      }
   }

   //AHJ REFACTOR
   public void remove(BlockPos pos) {
   //public void remove(BlockPos p_218248_1_) {
      PointOfInterest pointofinterest = this.poiLocationMap.remove(SectionPos.toRelativeOffset(pos));
      if (pointofinterest == null) {
         LOGGER.error("POI data mismatch: never registered at " + pos);
      } else {
         this.poiSetsByTypeMap.get(pointofinterest.getType()).remove(pointofinterest);
         LOGGER.debug("Removed POI of type {} @ {}", pointofinterest::getType, pointofinterest::getPos);
         this.onChange.run();
      }
   }

   //AH REFACTOR
   public boolean removePoiLocation(BlockPos pos) {
   //public boolean func_218251_c(BlockPos p_218251_1_) {
      PointOfInterest pointofinterest = this.poiLocationMap.get(SectionPos.toRelativeOffset(pos));
      if (pointofinterest == null) {
         throw (IllegalStateException)Util.spinlockIfDevMode(new IllegalStateException("POI never registered at " + pos));
      } else {
         boolean flag = pointofinterest.release();
         this.onChange.run();
         return flag;
      }
   }

   //AH REFACTOR
   public boolean isPoiValid(BlockPos pos, Predicate<PointOfInterestType> poiPred) {
   //public boolean func_218245_a(BlockPos p_218245_1_, Predicate<PointOfInterestType> p_218245_2_) {
      short short1 = SectionPos.toRelativeOffset(pos);
      PointOfInterest pointofinterest = this.poiLocationMap.get(short1);
      return pointofinterest != null && poiPred.test(pointofinterest.getType());
   }

   //AH REFACTOR
   public Optional<PointOfInterestType> getPoiTypeForPos(BlockPos pos) {
   //public Optional<PointOfInterestType> func_218244_d(BlockPos p_218244_1_) {
      short short1 = SectionPos.toRelativeOffset(pos);
      PointOfInterest pointofinterest = this.poiLocationMap.get(short1);
      return pointofinterest != null ? Optional.of(pointofinterest.getType()) : Optional.empty();
   }

   public <T> T serialize(DynamicOps<T> p_218175_1_) {
      T t = p_218175_1_.createList(this.poiLocationMap.values().stream().map((p_218242_1_) -> {
         return p_218242_1_.serialize(p_218175_1_);
      }));
      return p_218175_1_.createMap(ImmutableMap.of(p_218175_1_.createString("Records"), t, p_218175_1_.createString("Valid"), p_218175_1_.createBoolean(this.valid)));
   }

   public void func_218240_a(Consumer<BiConsumer<BlockPos, PointOfInterestType>> p_218240_1_) {
      if (!this.valid) {
         Short2ObjectMap<PointOfInterest> short2objectmap = new Short2ObjectOpenHashMap<>(this.poiLocationMap);
         this.clear();
         p_218240_1_.accept((p_218250_2_, p_218250_3_) -> {
            short short1 = SectionPos.toRelativeOffset(p_218250_2_);
            PointOfInterest pointofinterest = short2objectmap.computeIfAbsent(short1, (p_218241_3_) -> {
               return new PointOfInterest(p_218250_2_, p_218250_3_, this.onChange);
            });
            this.addToMap(pointofinterest);
         });
         this.valid = true;
         this.onChange.run();
      }

   }

   private void clear() {
      this.poiLocationMap.clear();
      this.poiSetsByTypeMap.clear();
   }

   boolean func_226355_a_() {
      return this.valid;
   }
}
