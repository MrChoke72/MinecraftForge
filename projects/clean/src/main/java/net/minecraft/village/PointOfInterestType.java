package net.minecraft.village;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.state.properties.BedPart;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;

public class PointOfInterestType {
   private static final Predicate<PointOfInterestType> ANY_VILLAGER_WORKSTATION = (poiType) -> {
      return Registry.VILLAGER_PROFESSION.stream().map(VillagerProfession::getPointOfInterest).collect(Collectors.toSet()).contains(poiType);
   };

   //AH REFACTOR
   public static final Predicate<PointOfInterestType> POI_TYPE_PRED_TRUE = (p_221049_0_) -> {
      return true;
   };
   private static final Set<BlockState> BED_HEADS = ImmutableList.of(Blocks.RED_BED, Blocks.BLACK_BED, Blocks.BLUE_BED, Blocks.BROWN_BED, Blocks.CYAN_BED, Blocks.GRAY_BED,
           Blocks.GREEN_BED, Blocks.LIGHT_BLUE_BED, Blocks.LIGHT_GRAY_BED, Blocks.LIME_BED, Blocks.MAGENTA_BED, Blocks.ORANGE_BED, Blocks.PINK_BED, Blocks.PURPLE_BED, Blocks.WHITE_BED,
           Blocks.YELLOW_BED).stream().flatMap((block) -> {
      return block.getStateContainer().getValidStates().stream();
   }).filter((state) -> {
      return state.get(BedBlock.PART) == BedPart.HEAD;
   }).collect(ImmutableSet.toImmutableSet());

   //AH REFACTOR
   private static final Map<BlockState, PointOfInterestType> blStatePoiTypeMap = Maps.newHashMap();
   //private static final Map<BlockState, PointOfInterestType> field_221073_u = Maps.newHashMap();

   public static final PointOfInterestType UNEMPLOYED = createPoiTypeWPred("unemployed", ImmutableSet.of(), 1, ANY_VILLAGER_WORKSTATION, 1);
   public static final PointOfInterestType ARMORER = createPoiType("armorer", getAllStates(Blocks.BLAST_FURNACE), 1, 1);
   public static final PointOfInterestType BUTCHER = createPoiType("butcher", getAllStates(Blocks.SMOKER), 1, 1);
   public static final PointOfInterestType CARTOGRAPHER = createPoiType("cartographer", getAllStates(Blocks.CARTOGRAPHY_TABLE), 1, 1);
   public static final PointOfInterestType CLERIC = createPoiType("cleric", getAllStates(Blocks.BREWING_STAND), 1, 1);
   public static final PointOfInterestType FARMER = createPoiType("farmer", getAllStates(Blocks.COMPOSTER), 1, 1);
   public static final PointOfInterestType FISHERMAN = createPoiType("fisherman", getAllStates(Blocks.BARREL), 1, 1);
   public static final PointOfInterestType FLETCHER = createPoiType("fletcher", getAllStates(Blocks.FLETCHING_TABLE), 1, 1);
   public static final PointOfInterestType LEATHERWORKER = createPoiType("leatherworker", getAllStates(Blocks.CAULDRON), 1, 1);
   public static final PointOfInterestType LIBRARIAN = createPoiType("librarian", getAllStates(Blocks.LECTERN), 1, 1);
   public static final PointOfInterestType MASON = createPoiType("mason", getAllStates(Blocks.STONECUTTER), 1, 1);
   public static final PointOfInterestType NITWIT = createPoiType("nitwit", ImmutableSet.of(), 1, 1);
   public static final PointOfInterestType SHEPHERD = createPoiType("shepherd", getAllStates(Blocks.LOOM), 1, 1);
   public static final PointOfInterestType TOOLSMITH = createPoiType("toolsmith", getAllStates(Blocks.SMITHING_TABLE), 1, 1);
   public static final PointOfInterestType WEAPONSMITH = createPoiType("weaponsmith", getAllStates(Blocks.GRINDSTONE), 1, 1);
   public static final PointOfInterestType HOME = createPoiType("home", BED_HEADS, 1, 1);
   public static final PointOfInterestType MEETING = createPoiType("meeting", getAllStates(Blocks.BELL), 32, 6);
   public static final PointOfInterestType BEEHIVE = createPoiType("beehive", getAllStates(Blocks.BEEHIVE), 0, 1);
   public static final PointOfInterestType BEE_NEST = createPoiType("bee_nest", getAllStates(Blocks.BEE_NEST), 0, 1);
   public static final PointOfInterestType NETHER_PORTAL = createPoiType("nether_portal", getAllStates(Blocks.NETHER_PORTAL), 0, 1);
   private final String name;

   //AH CHANGE REFACTOR
   private final Set<BlockState> stateSet;
   //private final Set<BlockState> field_221075_w;

   private final int maxFreeTickets;

   //AH CHANGE REFACTOR
   private final Predicate<PointOfInterestType> poiPred;
   //private final Predicate<PointOfInterestType> field_221078_z;

   //AH CHANGE REFACTOR
   private final int keepDist;
   //private final int field_225481_A;

   private static Set<BlockState> getAllStates(Block p_221042_0_) {
      return ImmutableSet.copyOf(p_221042_0_.getStateContainer().getValidStates());
   }

   //AH CHANGE REFACTOR
   private PointOfInterestType(String name, Set<BlockState> stateSet, int maxFreeTickets, Predicate<PointOfInterestType> poiPred, int keepDist) {
   //private PointOfInterestType(String p_i225713_1_, Set<BlockState> p_i225713_2_, int p_i225713_3_, Predicate<PointOfInterestType> p_i225713_4_, int p_i225713_5_) {
      this.name = name;
      this.stateSet = ImmutableSet.copyOf(stateSet);
      this.maxFreeTickets = maxFreeTickets;
      this.poiPred = poiPred;
      this.keepDist = keepDist;
   }

   //AH CHANGE REFACTOR
   private PointOfInterestType(String name, Set<BlockState> stateSet, int maxFreeTickets, int keepDist) {
   //private PointOfInterestType(String p_i225712_1_, Set<BlockState> p_i225712_2_, int p_i225712_3_, int p_i225712_4_) {
      this.name = name;
      this.stateSet = ImmutableSet.copyOf(stateSet);
      this.maxFreeTickets = maxFreeTickets;
      this.poiPred = (p_221046_1_) -> {
         return p_221046_1_ == this;
      };
      this.keepDist = keepDist;
   }

   public int getMaxFreeTickets() {
      return this.maxFreeTickets;
   }

   //AH CHANGE REFACTOR
   public Predicate<PointOfInterestType> getPoiTypePred() {
   //public Predicate<PointOfInterestType> func_221045_c() {
      return this.poiPred;
   }

   //Ah REFACTOR
   public int getKeepDist() {
   //public int func_225478_d() {
      return this.keepDist;
   }

   public String toString() {
      return this.name;
   }

   //AH CHANGE REFACTOR
   private static PointOfInterestType createPoiType(String name, Set<BlockState> stateSet, int maxFreeTickets, int keepDist) {
   //private static PointOfInterestType func_226359_a_(String p_226359_0_, Set<BlockState> p_226359_1_, int p_226359_2_, int p_226359_3_) {
      return addPoiTypeAndStates(Registry.POINT_OF_INTEREST_TYPE.register(new ResourceLocation(name), new PointOfInterestType(name, stateSet, maxFreeTickets, keepDist)));
   }

   //AH CHANGE REFACTOR
   private static PointOfInterestType createPoiTypeWPred(String name, Set<BlockState> stateSet, int maxFreeTickets, Predicate<PointOfInterestType> posPred, int keepDist) {
   //private static PointOfInterestType func_226360_a_(String p_226360_0_, Set<BlockState> p_226360_1_, int p_226360_2_, Predicate<PointOfInterestType> p_226360_3_, int p_226360_4_) {
      return addPoiTypeAndStates(Registry.POINT_OF_INTEREST_TYPE.register(new ResourceLocation(name), new PointOfInterestType(name, stateSet, maxFreeTickets, posPred, keepDist)));
   }

   //AH CHANGE REFACTOR
   private static PointOfInterestType addPoiTypeAndStates(PointOfInterestType poiType) {
   //private static PointOfInterestType func_221052_a(PointOfInterestType p_221052_0_) {
      poiType.stateSet.forEach((blockState) -> {
         PointOfInterestType pointofinteresttype = blStatePoiTypeMap.put(blockState, poiType);
         if (pointofinteresttype != null) {
            throw (IllegalStateException)Util.func_229757_c_(new IllegalStateException(String.format("%s is defined in too many tags", blockState)));
         }
      });
      return poiType;
   }

   public static Optional<PointOfInterestType> forState(BlockState state) {
      return Optional.ofNullable(blStatePoiTypeMap.get(state));
   }

   public static Stream<BlockState> getAllStates() {
      return blStatePoiTypeMap.keySet().stream();
   }
}
