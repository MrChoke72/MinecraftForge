package net.minecraft.entity.merchant.villager;

import com.google.common.collect.ImmutableSet;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.registry.Registry;
import net.minecraft.village.PointOfInterestType;

public class VillagerProfession {
   public static final VillagerProfession NONE = registerProfession("none", PointOfInterestType.UNEMPLOYED, (SoundEvent)null);
   public static final VillagerProfession ARMORER = registerProfession("armorer", PointOfInterestType.ARMORER, SoundEvents.ENTITY_VILLAGER_WORK_ARMORER);
   public static final VillagerProfession BUTCHER = registerProfession("butcher", PointOfInterestType.BUTCHER, SoundEvents.ENTITY_VILLAGER_WORK_BUTCHER);
   public static final VillagerProfession CARTOGRAPHER = registerProfession("cartographer", PointOfInterestType.CARTOGRAPHER, SoundEvents.ENTITY_VILLAGER_WORK_CARTOGRAPHER);
   public static final VillagerProfession CLERIC = registerProfession("cleric", PointOfInterestType.CLERIC, SoundEvents.ENTITY_VILLAGER_WORK_CLERIC);
   public static final VillagerProfession FARMER = registerProfession("farmer", PointOfInterestType.FARMER, ImmutableSet.of(Items.WHEAT, Items.WHEAT_SEEDS, Items.BEETROOT_SEEDS), ImmutableSet.of(Blocks.FARMLAND), SoundEvents.ENTITY_VILLAGER_WORK_FARMER);
   public static final VillagerProfession FISHERMAN = registerProfession("fisherman", PointOfInterestType.FISHERMAN, SoundEvents.ENTITY_VILLAGER_WORK_FISHERMAN);
   public static final VillagerProfession FLETCHER = registerProfession("fletcher", PointOfInterestType.FLETCHER, SoundEvents.ENTITY_VILLAGER_WORK_FLETCHER);
   public static final VillagerProfession LEATHERWORKER = registerProfession("leatherworker", PointOfInterestType.LEATHERWORKER, SoundEvents.ENTITY_VILLAGER_WORK_LEATHERWORKER);
   public static final VillagerProfession LIBRARIAN = registerProfession("librarian", PointOfInterestType.LIBRARIAN, SoundEvents.ENTITY_VILLAGER_WORK_LIBRARIAN);
   public static final VillagerProfession MASON = registerProfession("mason", PointOfInterestType.MASON, SoundEvents.ENTITY_VILLAGER_WORK_MASON);
   public static final VillagerProfession NITWIT = registerProfession("nitwit", PointOfInterestType.NITWIT, (SoundEvent)null);
   public static final VillagerProfession SHEPHERD = registerProfession("shepherd", PointOfInterestType.SHEPHERD, SoundEvents.ENTITY_VILLAGER_WORK_SHEPHERD);
   public static final VillagerProfession TOOLSMITH = registerProfession("toolsmith", PointOfInterestType.TOOLSMITH, SoundEvents.ENTITY_VILLAGER_WORK_TOOLSMITH);
   public static final VillagerProfession WEAPONSMITH = registerProfession("weaponsmith", PointOfInterestType.WEAPONSMITH, SoundEvents.ENTITY_VILLAGER_WORK_WEAPONSMITH);
   private final String name;
   private final PointOfInterestType pointOfInterest;
   private final ImmutableSet<Item> itemSet;
   private final ImmutableSet<Block> workBlockSet;
   @Nullable
   private final SoundEvent soundEvent;

   private VillagerProfession(String name, PointOfInterestType poiType, ImmutableSet<Item> itemSet, ImmutableSet<Block> workBlockSet, @Nullable SoundEvent soundEvent) {
      this.name = name;
      this.pointOfInterest = poiType;
      this.itemSet = itemSet;
      this.workBlockSet = workBlockSet;
      this.soundEvent = soundEvent;
   }

   public PointOfInterestType getPointOfInterest() {
      return this.pointOfInterest;
   }

   public ImmutableSet<Item> getItemSet() {
      return this.itemSet;
   }

   public ImmutableSet<Block> getWorkBlockSet() {
      return this.workBlockSet;
   }

   @Nullable
   public SoundEvent getSoundEvent() {
      return this.soundEvent;
   }

   public String toString() {
      return this.name;
   }

   static VillagerProfession registerProfession(String name, PointOfInterestType poiType, @Nullable SoundEvent soundEvent) {
      return registerProfession(name, poiType, ImmutableSet.of(), ImmutableSet.of(), soundEvent);
   }

   static VillagerProfession registerProfession(String name, PointOfInterestType poiType, ImmutableSet<Item> itemList, ImmutableSet<Block> workBlockSet, @Nullable SoundEvent soundEvent) {
      return Registry.register(Registry.VILLAGER_PROFESSION, new ResourceLocation(name), new VillagerProfession(name, poiType, itemList, workBlockSet, soundEvent));
   }
}
