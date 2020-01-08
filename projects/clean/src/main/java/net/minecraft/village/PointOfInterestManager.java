package net.minecraft.village;

import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.block.BlockState;
import net.minecraft.util.SectionDistanceGraph;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.DefaultTypeReferences;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.SectionPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.storage.RegionSectionCache;

public class PointOfInterestManager extends RegionSectionCache<PointOfInterestData> {
   private final PointOfInterestManager.DistanceGraph field_219164_a;
   private final LongSet field_226345_b_ = new LongOpenHashSet();

   public PointOfInterestManager(File p_i50298_1_, DataFixer p_i50298_2_) {
      super(p_i50298_1_, PointOfInterestData::new, PointOfInterestData::new, p_i50298_2_, DefaultTypeReferences.POI_CHUNK);
      this.field_219164_a = new PointOfInterestManager.DistanceGraph();
   }

   public void func_219135_a(BlockPos p_219135_1_, PointOfInterestType p_219135_2_) {
      this.func_219110_e(SectionPos.from(p_219135_1_).asLong()).func_218243_a(p_219135_1_, p_219135_2_);
   }

   public void func_219140_a(BlockPos p_219140_1_) {
      this.func_219110_e(SectionPos.from(p_219140_1_).asLong()).remove(p_219140_1_);
   }

   //AH CHANGE REFACTOR
   public long func_219145_a(Predicate<PointOfInterestType> poiTypePred, BlockPos pos, int p_219145_3_, PointOfInterestManager.Status p_219145_4_) {
   //public long func_219145_a(Predicate<PointOfInterestType> p_219145_1_, BlockPos p_219145_2_, int p_219145_3_, PointOfInterestManager.Status p_219145_4_) {
      return this.poiStreamByDistFiltPos(poiTypePred, pos, p_219145_3_, p_219145_4_).count();
   }

   //AH CHANGE REFACTOR
   public Stream<PointOfInterest> poiStreamByRadius(Predicate<PointOfInterestType> poiTypePred, BlockPos centerPos, int blockRadius, PointOfInterestManager.Status status) {
   //public Stream<PointOfInterest> func_226353_b_(Predicate<PointOfInterestType> p_226353_1_, BlockPos p_226353_2_, int p_226353_3_, PointOfInterestManager.Status p_226353_4_) {
      return ChunkPos.getAllInBox(new ChunkPos(centerPos), Math.floorDiv(blockRadius, 16)).flatMap((chunkPos) -> {
         return this.func_219137_a(poiTypePred, chunkPos, status);
      });
   }

   //AH CHANGE REFACTOR
   public Stream<PointOfInterest> poiStreamByDistFiltPos(Predicate<PointOfInterestType> poiTypePred, BlockPos pos, int distance, PointOfInterestManager.Status status) {
   //public Stream<PointOfInterest> func_219146_b(Predicate<PointOfInterestType> p_219146_1_, BlockPos p_219146_2_, int distance, PointOfInterestManager.Status p_219146_4_) {
      int i = distance * distance;
      return this.poiStreamByRadius(poiTypePred, pos, distance, status).filter((poi) -> {
         return poi.getPos().distanceSq(pos) <= (double)i;
      });
   }

   //AH CHANGE REFACTOR
   public Stream<PointOfInterest> func_219137_a(Predicate<PointOfInterestType> poiTypePred, ChunkPos chunkPos, PointOfInterestManager.Status status) {
   //public Stream<PointOfInterest> func_219137_a(Predicate<PointOfInterestType> p_219137_1_, ChunkPos p_219137_2_, PointOfInterestManager.Status p_219137_3_) {
      return IntStream.range(0, 16).boxed().flatMap((i) -> {
         return this.func_219136_a(poiTypePred, SectionPos.from(chunkPos, i).asLong(), status);
      });
   }

   //AH REFACTOR
   private Stream<PointOfInterest> func_219136_a(Predicate<PointOfInterestType> poiPred, long p_219136_2_, PointOfInterestManager.Status status) {
   //private Stream<PointOfInterest> func_219136_a(Predicate<PointOfInterestType> p_219136_1_, long p_219136_2_, PointOfInterestManager.Status p_219136_4_) {
      return this.func_219113_d(p_219136_2_).map((p_219159_2_) -> {
         return p_219159_2_.func_218247_a(poiPred, status);
      }).orElseGet(Stream::empty);
   }

   //AH CHANGE REFACTOR
   public Stream<BlockPos> poiStreamByDistFiltPos(Predicate<PointOfInterestType> poiPred, Predicate<BlockPos> posPred, BlockPos pos, int distance, PointOfInterestManager.Status status) {
   //public Stream<BlockPos> func_225399_a(Predicate<PointOfInterestType> p_225399_1_, Predicate<BlockPos> p_225399_2_, BlockPos p_225399_3_, int p_225399_4_, PointOfInterestManager.Status p_225399_5_) {
      return this.poiStreamByDistFiltPos(poiPred, pos, distance, status).map(PointOfInterest::getPos).filter(posPred);
   }

   //AH REFACTOR
   public Optional<BlockPos> func_219127_a(Predicate<PointOfInterestType> poiTypePred, Predicate<BlockPos> posPred, BlockPos pos, int distance, PointOfInterestManager.Status status) {
   //public Optional<BlockPos> func_219127_a(Predicate<PointOfInterestType> p_219127_1_, Predicate<BlockPos> p_219127_2_, BlockPos p_219127_3_, int p_219127_4_, PointOfInterestManager.Status p_219127_5_) {
      return this.poiStreamByDistFiltPos(poiTypePred, posPred, pos, distance, status).findFirst();
   }

   //AH CHANGE REFACTOR
   public Optional<BlockPos> getPoiPosInRange(Predicate<PointOfInterestType> poiTypePred, Predicate<BlockPos> posPred, BlockPos pos, int distance, PointOfInterestManager.Status status) {
   //public Optional<BlockPos> func_219147_b(Predicate<PointOfInterestType> p_219147_1_, Predicate<BlockPos> p_219147_2_, BlockPos p_219147_3_, int p_219147_4_, PointOfInterestManager.Status p_219147_5_) {
      return this.poiStreamByDistFiltPos(poiTypePred, pos, distance, status).map(PointOfInterest::getPos).sorted(Comparator.comparingDouble((p_219160_1_) -> {
         return p_219160_1_.distanceSq(pos);
      })).filter(posPred).findFirst();
   }

   public Optional<BlockPos> func_219157_a(Predicate<PointOfInterestType> p_219157_1_, Predicate<BlockPos> p_219157_2_, BlockPos p_219157_3_, int p_219157_4_) {
      return this.poiStreamByDistFiltPos(p_219157_1_, p_219157_3_, p_219157_4_, PointOfInterestManager.Status.HAS_SPACE).filter((p_219129_1_) -> {
         return p_219157_2_.test(p_219129_1_.getPos());
      }).findFirst().map((p_219152_0_) -> {
         p_219152_0_.claim();
         return p_219152_0_.getPos();
      });
   }

   public Optional<BlockPos> func_219163_a(Predicate<PointOfInterestType> p_219163_1_, Predicate<BlockPos> p_219163_2_, PointOfInterestManager.Status p_219163_3_, BlockPos p_219163_4_, int p_219163_5_, Random p_219163_6_) {
      List<PointOfInterest> list = this.poiStreamByDistFiltPos(p_219163_1_, p_219163_4_, p_219163_5_, p_219163_3_).collect(Collectors.toList());
      Collections.shuffle(list, p_219163_6_);
      return list.stream().filter((p_219131_1_) -> {
         return p_219163_2_.test(p_219131_1_.getPos());
      }).findFirst().map(PointOfInterest::getPos);
   }

   //AH REFACTOR
   public boolean func_219142_b(BlockPos pos) {
   //public boolean func_219142_b(BlockPos p_219142_1_) {
      return this.func_219110_e(SectionPos.from(pos).asLong()).removePoiLocation(pos);
   }

   //AH REFACTOR
   public boolean isPoiValid(BlockPos pos, Predicate<PointOfInterestType> poiTypePred) {
   //public boolean func_219138_a(BlockPos p_219138_1_, Predicate<PointOfInterestType> p_219138_2_) {
      return this.func_219113_d(SectionPos.from(pos).asLong()).map((poiData) -> {
         return poiData.isPoiValid(pos, poiTypePred);
      }).orElse(false);
   }

   //Ah REFACTOR
   public Optional<PointOfInterestType> getPoiTypeForPos(BlockPos pos) {
   //public Optional<PointOfInterestType> func_219148_c(BlockPos p_219148_1_) {
      PointOfInterestData pointofinterestdata = this.func_219110_e(SectionPos.from(pos).asLong());
      return pointofinterestdata.getPoiTypeForPos(pos);
   }

   public int func_219150_a(SectionPos p_219150_1_) {
      this.field_219164_a.func_215563_a();
      return this.field_219164_a.getLevel(p_219150_1_.asLong());
   }

   private boolean func_219154_f(long p_219154_1_) {
      Optional<PointOfInterestData> optional = this.func_219106_c(p_219154_1_);
      return optional == null ? false : optional.map((p_223144_0_) -> {
         return p_223144_0_.func_218247_a(PointOfInterestType.field_221053_a, PointOfInterestManager.Status.IS_OCCUPIED).count() > 0L;
      }).orElse(false);
   }

   public void func_219115_a(BooleanSupplier p_219115_1_) {
      super.func_219115_a(p_219115_1_);
      this.field_219164_a.func_215563_a();
   }

   protected void markDirty(long sectionPosIn) {
      super.markDirty(sectionPosIn);
      this.field_219164_a.updateSourceLevel(sectionPosIn, this.field_219164_a.getSourceLevel(sectionPosIn), false);
   }

   protected void func_219111_b(long p_219111_1_) {
      this.field_219164_a.updateSourceLevel(p_219111_1_, this.field_219164_a.getSourceLevel(p_219111_1_), false);
   }

   public void func_219139_a(ChunkPos p_219139_1_, ChunkSection p_219139_2_) {
      SectionPos sectionpos = SectionPos.from(p_219139_1_, p_219139_2_.getYLocation() >> 4);
      Util.acceptOrElse(this.func_219113_d(sectionpos.asLong()), (p_219130_3_) -> {
         p_219130_3_.func_218240_a((p_219141_3_) -> {
            if (hasAnyPOI(p_219139_2_)) {
               this.func_219132_a(p_219139_2_, sectionpos, p_219141_3_);
            }

         });
      }, () -> {
         if (hasAnyPOI(p_219139_2_)) {
            PointOfInterestData pointofinterestdata = this.func_219110_e(sectionpos.asLong());
            this.func_219132_a(p_219139_2_, sectionpos, pointofinterestdata::func_218243_a);
         }

      });
   }

   private static boolean hasAnyPOI(ChunkSection p_219151_0_) {
      return PointOfInterestType.getAllStates().anyMatch(p_219151_0_::contains);
   }

   private void func_219132_a(ChunkSection p_219132_1_, SectionPos p_219132_2_, BiConsumer<BlockPos, PointOfInterestType> p_219132_3_) {
      p_219132_2_.allBlocksWithin().forEach((p_219143_2_) -> {
         BlockState blockstate = p_219132_1_.getBlockState(SectionPos.mask(p_219143_2_.getX()), SectionPos.mask(p_219143_2_.getY()), SectionPos.mask(p_219143_2_.getZ()));
         PointOfInterestType.forState(blockstate).ifPresent((p_219161_2_) -> {
            p_219132_3_.accept(p_219143_2_, p_219161_2_);
         });
      });
   }

   public void func_226347_a_(IWorldReader p_226347_1_, BlockPos p_226347_2_, int p_226347_3_) {
      SectionPos.func_229421_b_(new ChunkPos(p_226347_2_), Math.floorDiv(p_226347_3_, 16)).map((p_226354_1_) -> {
         return Pair.of(p_226354_1_, this.func_219113_d(p_226354_1_.asLong()));
      }).filter((p_226352_0_) -> {
         return !p_226352_0_.getSecond().map(PointOfInterestData::func_226355_a_).orElse(false);
      }).map((p_226348_0_) -> {
         return p_226348_0_.getFirst().asChunkPos();
      }).filter((p_226351_1_) -> {
         return this.field_226345_b_.add(p_226351_1_.asLong());
      }).forEach((p_226346_1_) -> {
         p_226347_1_.getChunk(p_226346_1_.x, p_226346_1_.z, ChunkStatus.EMPTY);
      });
   }

   final class DistanceGraph extends SectionDistanceGraph {
      private final Long2ByteMap field_215565_b = new Long2ByteOpenHashMap();

      protected DistanceGraph() {
         super(7, 16, 256);
         this.field_215565_b.defaultReturnValue((byte)7);
      }

      protected int getSourceLevel(long pos) {
         return PointOfInterestManager.this.func_219154_f(pos) ? 0 : 7;
      }

      protected int getLevel(long sectionPosIn) {
         return this.field_215565_b.get(sectionPosIn);
      }

      protected void setLevel(long sectionPosIn, int level) {
         if (level > 6) {
            this.field_215565_b.remove(sectionPosIn);
         } else {
            this.field_215565_b.put(sectionPosIn, (byte)level);
         }

      }

      public void func_215563_a() {
         super.processUpdates(Integer.MAX_VALUE);
      }
   }

   public static enum Status {
      HAS_SPACE(PointOfInterest::hasSpace),
      IS_OCCUPIED(PointOfInterest::isOccupied),
      ANY((p_221036_0_) -> {
         return true;
      });

      private final Predicate<? super PointOfInterest> field_221037_d;

      private Status(Predicate<? super PointOfInterest> p_i50192_3_) {
         this.field_221037_d = p_i50192_3_;
      }

      public Predicate<? super PointOfInterest> func_221035_a() {
         return this.field_221037_d;
      }
   }
}
