package net.minecraft.data.loot;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.advancements.criterion.DamageSourcePredicate;
import net.minecraft.advancements.criterion.EntityFlagsPredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.storage.loot.ConstantRange;
import net.minecraft.world.storage.loot.EmptyLootEntry;
import net.minecraft.world.storage.loot.ItemLootEntry;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.LootTables;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.TableLootEntry;
import net.minecraft.world.storage.loot.TagLootEntry;
import net.minecraft.world.storage.loot.conditions.DamageSourceProperties;
import net.minecraft.world.storage.loot.conditions.EntityHasProperty;
import net.minecraft.world.storage.loot.conditions.KilledByPlayer;
import net.minecraft.world.storage.loot.conditions.RandomChance;
import net.minecraft.world.storage.loot.conditions.RandomChanceWithLooting;
import net.minecraft.world.storage.loot.functions.LootingEnchantBonus;
import net.minecraft.world.storage.loot.functions.SetCount;
import net.minecraft.world.storage.loot.functions.SetNBT;
import net.minecraft.world.storage.loot.functions.Smelt;

public class EntityLootTables implements Consumer<BiConsumer<ResourceLocation, LootTable.Builder>> {
   protected static final EntityPredicate.Builder field_218586_a = EntityPredicate.Builder.create().func_217987_a(EntityFlagsPredicate.Builder.create().onFire(true).build());
   private static final Set<EntityType<?>> entitySet = ImmutableSet.of(
           EntityType.PLAYER, EntityType.ARMOR_STAND, EntityType.IRON_GOLEM, EntityType.SNOW_GOLEM, EntityType.VILLAGER);
   private final Map<ResourceLocation, LootTable.Builder> lootBuilderMap = Maps.newHashMap();

   private static LootTable.Builder func_218583_a(IItemProvider p_218583_0_) {
      return LootTable.builder().addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(p_218583_0_))).addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(TableLootEntry.builder(EntityType.SHEEP.getLootTable())));
   }

   public void accept(BiConsumer<ResourceLocation, LootTable.Builder> p_accept_1_) {
      this.addToLootMap(EntityType.ARMOR_STAND, LootTable.builder());
      this.addToLootMap(EntityType.BAT, LootTable.builder());
      this.addToLootMap(EntityType.field_226289_e_, LootTable.builder());
      this.addToLootMap(EntityType.BLAZE, LootTable.builder().addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.BLAZE_ROD).acceptFunction(SetCount.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F))).acceptFunction(LootingEnchantBonus.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F)))).acceptCondition(KilledByPlayer.builder())));
      this.addToLootMap(EntityType.CAT, LootTable.builder().addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.STRING).acceptFunction(SetCount.createLootBuilder(RandomValueRange.createRandRange(0.0F, 2.0F))))));
      this.addToLootMap(EntityType.CAVE_SPIDER, LootTable.builder().addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.STRING).acceptFunction(SetCount.createLootBuilder(RandomValueRange.createRandRange(0.0F, 2.0F))).acceptFunction(LootingEnchantBonus.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F))))).addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.SPIDER_EYE).acceptFunction(SetCount.createLootBuilder(RandomValueRange.createRandRange(-1.0F, 1.0F))).acceptFunction(LootingEnchantBonus.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F)))).acceptCondition(KilledByPlayer.builder())));
      this.addToLootMap(EntityType.CHICKEN, LootTable.builder().addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.FEATHER).acceptFunction(SetCount.createLootBuilder(RandomValueRange.createRandRange(0.0F, 2.0F))).acceptFunction(LootingEnchantBonus.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F))))).addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.CHICKEN).acceptFunction(Smelt.func_215953_b().acceptCondition(EntityHasProperty.func_215999_a(LootContext.EntityTarget.THIS, field_218586_a))).acceptFunction(LootingEnchantBonus.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F))))));
      this.addToLootMap(EntityType.COD, LootTable.builder().addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.COD).acceptFunction(Smelt.func_215953_b().acceptCondition(EntityHasProperty.func_215999_a(LootContext.EntityTarget.THIS, field_218586_a))))).addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.BONE_MEAL)).acceptCondition(RandomChance.builder(0.05F))));
      this.addToLootMap(EntityType.COW, LootTable.builder().addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.LEATHER).acceptFunction(SetCount.createLootBuilder(RandomValueRange.createRandRange(0.0F, 2.0F))).acceptFunction(LootingEnchantBonus.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F))))).addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.BEEF).acceptFunction(SetCount.createLootBuilder(RandomValueRange.createRandRange(1.0F, 3.0F))).acceptFunction(Smelt.func_215953_b().acceptCondition(EntityHasProperty.func_215999_a(LootContext.EntityTarget.THIS, field_218586_a))).acceptFunction(LootingEnchantBonus.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F))))));
      this.addToLootMap(EntityType.CREEPER, LootTable.builder().addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.GUNPOWDER).acceptFunction(SetCount.createLootBuilder(RandomValueRange.createRandRange(0.0F, 2.0F))).acceptFunction(LootingEnchantBonus.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F))))).addLootPool(LootPool.builder().addEntry(TagLootEntry.func_216176_b(ItemTags.MUSIC_DISCS)).acceptCondition(EntityHasProperty.func_215999_a(LootContext.EntityTarget.KILLER, EntityPredicate.Builder.create().func_217989_a(EntityTypeTags.SKELETONS)))));
      this.addToLootMap(EntityType.DOLPHIN, LootTable.builder().addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.COD).acceptFunction(SetCount.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F))).acceptFunction(LootingEnchantBonus.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F))).acceptFunction(Smelt.func_215953_b().acceptCondition(EntityHasProperty.func_215999_a(LootContext.EntityTarget.THIS, field_218586_a))))));
      this.addToLootMap(EntityType.DONKEY, LootTable.builder().addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.LEATHER).acceptFunction(SetCount.createLootBuilder(RandomValueRange.createRandRange(0.0F, 2.0F))).acceptFunction(LootingEnchantBonus.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F))))));
      this.addToLootMap(EntityType.DROWNED, LootTable.builder().addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.ROTTEN_FLESH).acceptFunction(SetCount.createLootBuilder(RandomValueRange.createRandRange(0.0F, 2.0F))).acceptFunction(LootingEnchantBonus.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F))))).addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.GOLD_INGOT)).acceptCondition(KilledByPlayer.builder()).acceptCondition(RandomChanceWithLooting.builder(0.05F, 0.01F))));
      this.addToLootMap(EntityType.ELDER_GUARDIAN, LootTable.builder().addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.PRISMARINE_SHARD).acceptFunction(SetCount.createLootBuilder(RandomValueRange.createRandRange(0.0F, 2.0F))).acceptFunction(LootingEnchantBonus.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F))))).addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.COD).weight(3).acceptFunction(LootingEnchantBonus.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F))).acceptFunction(Smelt.func_215953_b().acceptCondition(EntityHasProperty.func_215999_a(LootContext.EntityTarget.THIS, field_218586_a)))).addEntry(ItemLootEntry.builder(Items.PRISMARINE_CRYSTALS).weight(2).acceptFunction(LootingEnchantBonus.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F)))).addEntry(EmptyLootEntry.func_216167_a())).addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Blocks.WET_SPONGE)).acceptCondition(KilledByPlayer.builder())).addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(TableLootEntry.builder(LootTables.GAMEPLAY_FISHING_FISH)).acceptCondition(KilledByPlayer.builder()).acceptCondition(RandomChanceWithLooting.builder(0.025F, 0.01F))));
      this.addToLootMap(EntityType.ENDER_DRAGON, LootTable.builder());
      this.addToLootMap(EntityType.ENDERMAN, LootTable.builder().addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.ENDER_PEARL).acceptFunction(SetCount.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F))).acceptFunction(LootingEnchantBonus.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F))))));
      this.addToLootMap(EntityType.ENDERMITE, LootTable.builder());
      this.addToLootMap(EntityType.EVOKER, LootTable.builder().addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.TOTEM_OF_UNDYING))).addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.EMERALD).acceptFunction(SetCount.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F))).acceptFunction(LootingEnchantBonus.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F)))).acceptCondition(KilledByPlayer.builder())));
      this.addToLootMap(EntityType.FOX, LootTable.builder());
      this.addToLootMap(EntityType.GHAST, LootTable.builder().addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.GHAST_TEAR).acceptFunction(SetCount.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F))).acceptFunction(LootingEnchantBonus.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F))))).addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.GUNPOWDER).acceptFunction(SetCount.createLootBuilder(RandomValueRange.createRandRange(0.0F, 2.0F))).acceptFunction(LootingEnchantBonus.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F))))));
      this.addToLootMap(EntityType.GIANT, LootTable.builder());
      this.addToLootMap(EntityType.GUARDIAN, LootTable.builder().addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.PRISMARINE_SHARD).acceptFunction(SetCount.createLootBuilder(RandomValueRange.createRandRange(0.0F, 2.0F))).acceptFunction(LootingEnchantBonus.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F))))).addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.COD).weight(2).acceptFunction(LootingEnchantBonus.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F))).acceptFunction(Smelt.func_215953_b().acceptCondition(EntityHasProperty.func_215999_a(LootContext.EntityTarget.THIS, field_218586_a)))).addEntry(ItemLootEntry.builder(Items.PRISMARINE_CRYSTALS).weight(2).acceptFunction(LootingEnchantBonus.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F)))).addEntry(EmptyLootEntry.func_216167_a())).addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(TableLootEntry.builder(LootTables.GAMEPLAY_FISHING_FISH)).acceptCondition(KilledByPlayer.builder()).acceptCondition(RandomChanceWithLooting.builder(0.025F, 0.01F))));
      this.addToLootMap(EntityType.HORSE, LootTable.builder().addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.LEATHER).acceptFunction(SetCount.createLootBuilder(RandomValueRange.createRandRange(0.0F, 2.0F))).acceptFunction(LootingEnchantBonus.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F))))));
      this.addToLootMap(EntityType.HUSK, LootTable.builder().addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.ROTTEN_FLESH).acceptFunction(SetCount.createLootBuilder(RandomValueRange.createRandRange(0.0F, 2.0F))).acceptFunction(LootingEnchantBonus.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F))))).addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.IRON_INGOT)).addEntry(ItemLootEntry.builder(Items.CARROT)).addEntry(ItemLootEntry.builder(Items.POTATO)).acceptCondition(KilledByPlayer.builder()).acceptCondition(RandomChanceWithLooting.builder(0.025F, 0.01F))));
      this.addToLootMap(EntityType.RAVAGER, LootTable.builder().addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.SADDLE).acceptFunction(SetCount.createLootBuilder(ConstantRange.of(1))))));
      this.addToLootMap(EntityType.ILLUSIONER, LootTable.builder());
      this.addToLootMap(EntityType.IRON_GOLEM, LootTable.builder().addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Blocks.POPPY).acceptFunction(SetCount.createLootBuilder(RandomValueRange.createRandRange(0.0F, 2.0F))))).addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.IRON_INGOT).acceptFunction(SetCount.createLootBuilder(RandomValueRange.createRandRange(3.0F, 5.0F))))));
      this.addToLootMap(EntityType.LLAMA, LootTable.builder().addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.LEATHER).acceptFunction(SetCount.createLootBuilder(RandomValueRange.createRandRange(0.0F, 2.0F))).acceptFunction(LootingEnchantBonus.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F))))));
      this.addToLootMap(EntityType.MAGMA_CUBE, LootTable.builder().addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.MAGMA_CREAM).acceptFunction(SetCount.createLootBuilder(RandomValueRange.createRandRange(-2.0F, 1.0F))).acceptFunction(LootingEnchantBonus.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F))))));
      this.addToLootMap(EntityType.MULE, LootTable.builder().addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.LEATHER).acceptFunction(SetCount.createLootBuilder(RandomValueRange.createRandRange(0.0F, 2.0F))).acceptFunction(LootingEnchantBonus.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F))))));
      this.addToLootMap(EntityType.MOOSHROOM, LootTable.builder().addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.LEATHER).acceptFunction(SetCount.createLootBuilder(RandomValueRange.createRandRange(0.0F, 2.0F))).acceptFunction(LootingEnchantBonus.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F))))).addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.BEEF).acceptFunction(SetCount.createLootBuilder(RandomValueRange.createRandRange(1.0F, 3.0F))).acceptFunction(Smelt.func_215953_b().acceptCondition(EntityHasProperty.func_215999_a(LootContext.EntityTarget.THIS, field_218586_a))).acceptFunction(LootingEnchantBonus.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F))))));
      this.addToLootMap(EntityType.OCELOT, LootTable.builder());
      this.addToLootMap(EntityType.PANDA, LootTable.builder().addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Blocks.BAMBOO).acceptFunction(SetCount.createLootBuilder(ConstantRange.of(1))))));
      this.addToLootMap(EntityType.PARROT, LootTable.builder().addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.FEATHER).acceptFunction(SetCount.createLootBuilder(RandomValueRange.createRandRange(1.0F, 2.0F))).acceptFunction(LootingEnchantBonus.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F))))));
      this.addToLootMap(EntityType.PHANTOM, LootTable.builder().addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.PHANTOM_MEMBRANE).acceptFunction(SetCount.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F))).acceptFunction(LootingEnchantBonus.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F)))).acceptCondition(KilledByPlayer.builder())));
      this.addToLootMap(EntityType.PIG, LootTable.builder().addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.PORKCHOP).acceptFunction(SetCount.createLootBuilder(RandomValueRange.createRandRange(1.0F, 3.0F))).acceptFunction(Smelt.func_215953_b().acceptCondition(EntityHasProperty.func_215999_a(LootContext.EntityTarget.THIS, field_218586_a))).acceptFunction(LootingEnchantBonus.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F))))));
      this.addToLootMap(EntityType.PILLAGER, LootTable.builder());
      this.addToLootMap(EntityType.PLAYER, LootTable.builder());
      this.addToLootMap(EntityType.POLAR_BEAR, LootTable.builder().addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.COD).weight(3).acceptFunction(SetCount.createLootBuilder(RandomValueRange.createRandRange(0.0F, 2.0F))).acceptFunction(LootingEnchantBonus.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F)))).addEntry(ItemLootEntry.builder(Items.SALMON).acceptFunction(SetCount.createLootBuilder(RandomValueRange.createRandRange(0.0F, 2.0F))).acceptFunction(LootingEnchantBonus.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F))))));
      this.addToLootMap(EntityType.PUFFERFISH, LootTable.builder().addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.PUFFERFISH).acceptFunction(SetCount.createLootBuilder(ConstantRange.of(1))))).addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.BONE_MEAL)).acceptCondition(RandomChance.builder(0.05F))));
      this.addToLootMap(EntityType.RABBIT, LootTable.builder().addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.RABBIT_HIDE).acceptFunction(SetCount.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F))).acceptFunction(LootingEnchantBonus.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F))))).addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.RABBIT).acceptFunction(SetCount.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F))).acceptFunction(Smelt.func_215953_b().acceptCondition(EntityHasProperty.func_215999_a(LootContext.EntityTarget.THIS, field_218586_a))).acceptFunction(LootingEnchantBonus.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F))))).addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.RABBIT_FOOT)).acceptCondition(KilledByPlayer.builder()).acceptCondition(RandomChanceWithLooting.builder(0.1F, 0.03F))));
      this.addToLootMap(EntityType.SALMON, LootTable.builder().addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.SALMON).acceptFunction(Smelt.func_215953_b().acceptCondition(EntityHasProperty.func_215999_a(LootContext.EntityTarget.THIS, field_218586_a))))).addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.BONE_MEAL)).acceptCondition(RandomChance.builder(0.05F))));
      this.addToLootMap(EntityType.SHEEP, LootTable.builder().addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.MUTTON).acceptFunction(SetCount.createLootBuilder(RandomValueRange.createRandRange(1.0F, 2.0F))).acceptFunction(Smelt.func_215953_b().acceptCondition(EntityHasProperty.func_215999_a(LootContext.EntityTarget.THIS, field_218586_a))).acceptFunction(LootingEnchantBonus.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F))))));
      this.addToLootMap(LootTables.ENTITIES_SHEEP_BLACK, func_218583_a(Blocks.BLACK_WOOL));
      this.addToLootMap(LootTables.ENTITIES_SHEEP_BLUE, func_218583_a(Blocks.BLUE_WOOL));
      this.addToLootMap(LootTables.ENTITIES_SHEEP_BROWN, func_218583_a(Blocks.BROWN_WOOL));
      this.addToLootMap(LootTables.ENTITIES_SHEEP_CYAN, func_218583_a(Blocks.CYAN_WOOL));
      this.addToLootMap(LootTables.ENTITIES_SHEEP_GRAY, func_218583_a(Blocks.GRAY_WOOL));
      this.addToLootMap(LootTables.ENTITIES_SHEEP_GREEN, func_218583_a(Blocks.GREEN_WOOL));
      this.addToLootMap(LootTables.ENTITIES_SHEEP_LIGHT_BLUE, func_218583_a(Blocks.LIGHT_BLUE_WOOL));
      this.addToLootMap(LootTables.ENTITIES_SHEEP_LIGHT_GRAY, func_218583_a(Blocks.LIGHT_GRAY_WOOL));
      this.addToLootMap(LootTables.ENTITIES_SHEEP_LIME, func_218583_a(Blocks.LIME_WOOL));
      this.addToLootMap(LootTables.ENTITIES_SHEEP_MAGENTA, func_218583_a(Blocks.MAGENTA_WOOL));
      this.addToLootMap(LootTables.ENTITIES_SHEEP_ORANGE, func_218583_a(Blocks.ORANGE_WOOL));
      this.addToLootMap(LootTables.ENTITIES_SHEEP_PINK, func_218583_a(Blocks.PINK_WOOL));
      this.addToLootMap(LootTables.ENTITIES_SHEEP_PURPLE, func_218583_a(Blocks.PURPLE_WOOL));
      this.addToLootMap(LootTables.ENTITIES_SHEEP_RED, func_218583_a(Blocks.RED_WOOL));
      this.addToLootMap(LootTables.ENTITIES_SHEEP_WHITE, func_218583_a(Blocks.WHITE_WOOL));
      this.addToLootMap(LootTables.ENTITIES_SHEEP_YELLOW, func_218583_a(Blocks.YELLOW_WOOL));
      this.addToLootMap(EntityType.SHULKER, LootTable.builder().addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.SHULKER_SHELL)).acceptCondition(RandomChanceWithLooting.builder(0.5F, 0.0625F))));
      this.addToLootMap(EntityType.SILVERFISH, LootTable.builder());
      this.addToLootMap(EntityType.SKELETON, LootTable.builder().addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.ARROW).acceptFunction(SetCount.createLootBuilder(RandomValueRange.createRandRange(0.0F, 2.0F))).acceptFunction(LootingEnchantBonus.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F))))).addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.BONE).acceptFunction(SetCount.createLootBuilder(RandomValueRange.createRandRange(0.0F, 2.0F))).acceptFunction(LootingEnchantBonus.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F))))));
      this.addToLootMap(EntityType.SKELETON_HORSE, LootTable.builder().addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.BONE).acceptFunction(SetCount.createLootBuilder(RandomValueRange.createRandRange(0.0F, 2.0F))).acceptFunction(LootingEnchantBonus.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F))))));
      this.addToLootMap(EntityType.SLIME, LootTable.builder().addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.SLIME_BALL).acceptFunction(SetCount.createLootBuilder(RandomValueRange.createRandRange(0.0F, 2.0F))).acceptFunction(LootingEnchantBonus.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F))))));
      this.addToLootMap(EntityType.SNOW_GOLEM, LootTable.builder().addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.SNOWBALL).acceptFunction(SetCount.createLootBuilder(RandomValueRange.createRandRange(0.0F, 15.0F))))));
      this.addToLootMap(EntityType.SPIDER, LootTable.builder().addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.STRING).acceptFunction(SetCount.createLootBuilder(RandomValueRange.createRandRange(0.0F, 2.0F))).acceptFunction(LootingEnchantBonus.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F))))).addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.SPIDER_EYE).acceptFunction(SetCount.createLootBuilder(RandomValueRange.createRandRange(-1.0F, 1.0F))).acceptFunction(LootingEnchantBonus.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F)))).acceptCondition(KilledByPlayer.builder())));
      this.addToLootMap(EntityType.SQUID, LootTable.builder().addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.INK_SAC).acceptFunction(SetCount.createLootBuilder(RandomValueRange.createRandRange(1.0F, 3.0F))).acceptFunction(LootingEnchantBonus.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F))))));
      this.addToLootMap(EntityType.STRAY, LootTable.builder().addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.ARROW).acceptFunction(SetCount.createLootBuilder(RandomValueRange.createRandRange(0.0F, 2.0F))).acceptFunction(LootingEnchantBonus.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F))))).addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.BONE).acceptFunction(SetCount.createLootBuilder(RandomValueRange.createRandRange(0.0F, 2.0F))).acceptFunction(LootingEnchantBonus.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F))))).addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.TIPPED_ARROW).acceptFunction(SetCount.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F))).acceptFunction(LootingEnchantBonus.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F)).func_216072_a(1)).acceptFunction(SetNBT.func_215952_a(Util.make(new CompoundNBT(), (p_218584_0_) -> {
         p_218584_0_.putString("Potion", "minecraft:slowness");
      })))).acceptCondition(KilledByPlayer.builder())));
      this.addToLootMap(EntityType.TRADER_LLAMA, LootTable.builder().addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.LEATHER).acceptFunction(SetCount.createLootBuilder(RandomValueRange.createRandRange(0.0F, 2.0F))).acceptFunction(LootingEnchantBonus.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F))))));
      this.addToLootMap(EntityType.TROPICAL_FISH, LootTable.builder().addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.TROPICAL_FISH).acceptFunction(SetCount.createLootBuilder(ConstantRange.of(1))))).addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.BONE_MEAL)).acceptCondition(RandomChance.builder(0.05F))));
      this.addToLootMap(EntityType.TURTLE, LootTable.builder().addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Blocks.SEAGRASS).weight(3).acceptFunction(SetCount.createLootBuilder(RandomValueRange.createRandRange(0.0F, 2.0F))).acceptFunction(LootingEnchantBonus.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F))))).addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.BOWL)).acceptCondition(DamageSourceProperties.builder(DamageSourcePredicate.Builder.damageType().func_217950_h(true)))));
      this.addToLootMap(EntityType.VEX, LootTable.builder());
      this.addToLootMap(EntityType.VILLAGER, LootTable.builder());
      this.addToLootMap(EntityType.WANDERING_TRADER, LootTable.builder());
      this.addToLootMap(EntityType.VINDICATOR, LootTable.builder().addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.EMERALD).acceptFunction(SetCount.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F))).acceptFunction(LootingEnchantBonus.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F)))).acceptCondition(KilledByPlayer.builder())));
      this.addToLootMap(EntityType.WITCH, LootTable.builder().addLootPool(LootPool.builder().rolls(RandomValueRange.createRandRange(1.0F, 3.0F)).addEntry(ItemLootEntry.builder(Items.GLOWSTONE_DUST).acceptFunction(SetCount.createLootBuilder(RandomValueRange.createRandRange(0.0F, 2.0F))).acceptFunction(LootingEnchantBonus.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F)))).addEntry(ItemLootEntry.builder(Items.SUGAR).acceptFunction(SetCount.createLootBuilder(RandomValueRange.createRandRange(0.0F, 2.0F))).acceptFunction(LootingEnchantBonus.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F)))).addEntry(ItemLootEntry.builder(Items.REDSTONE).acceptFunction(SetCount.createLootBuilder(RandomValueRange.createRandRange(0.0F, 2.0F))).acceptFunction(LootingEnchantBonus.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F)))).addEntry(ItemLootEntry.builder(Items.SPIDER_EYE).acceptFunction(SetCount.createLootBuilder(RandomValueRange.createRandRange(0.0F, 2.0F))).acceptFunction(LootingEnchantBonus.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F)))).addEntry(ItemLootEntry.builder(Items.GLASS_BOTTLE).acceptFunction(SetCount.createLootBuilder(RandomValueRange.createRandRange(0.0F, 2.0F))).acceptFunction(LootingEnchantBonus.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F)))).addEntry(ItemLootEntry.builder(Items.GUNPOWDER).acceptFunction(SetCount.createLootBuilder(RandomValueRange.createRandRange(0.0F, 2.0F))).acceptFunction(LootingEnchantBonus.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F)))).addEntry(ItemLootEntry.builder(Items.STICK).weight(2).acceptFunction(SetCount.createLootBuilder(RandomValueRange.createRandRange(0.0F, 2.0F))).acceptFunction(LootingEnchantBonus.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F))))));
      this.addToLootMap(EntityType.WITHER, LootTable.builder());
      this.addToLootMap(EntityType.WITHER_SKELETON, LootTable.builder().addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.COAL).acceptFunction(SetCount.createLootBuilder(RandomValueRange.createRandRange(-1.0F, 1.0F))).acceptFunction(LootingEnchantBonus.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F))))).addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.BONE).acceptFunction(SetCount.createLootBuilder(RandomValueRange.createRandRange(0.0F, 2.0F))).acceptFunction(LootingEnchantBonus.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F))))).addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Blocks.WITHER_SKELETON_SKULL)).acceptCondition(KilledByPlayer.builder()).acceptCondition(RandomChanceWithLooting.builder(0.025F, 0.01F))));
      this.addToLootMap(EntityType.WOLF, LootTable.builder());
      this.addToLootMap(EntityType.ZOMBIE, LootTable.builder().addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.ROTTEN_FLESH).acceptFunction(SetCount.createLootBuilder(RandomValueRange.createRandRange(0.0F, 2.0F))).acceptFunction(LootingEnchantBonus.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F))))).addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.IRON_INGOT)).addEntry(ItemLootEntry.builder(Items.CARROT)).addEntry(ItemLootEntry.builder(Items.POTATO)).acceptCondition(KilledByPlayer.builder()).acceptCondition(RandomChanceWithLooting.builder(0.025F, 0.01F))));
      this.addToLootMap(EntityType.ZOMBIE_HORSE, LootTable.builder().addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.ROTTEN_FLESH).acceptFunction(SetCount.createLootBuilder(RandomValueRange.createRandRange(0.0F, 2.0F))).acceptFunction(LootingEnchantBonus.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F))))));
      this.addToLootMap(EntityType.ZOMBIE_PIGMAN, LootTable.builder().addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.ROTTEN_FLESH).acceptFunction(SetCount.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F))).acceptFunction(LootingEnchantBonus.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F))))).addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.GOLD_NUGGET).acceptFunction(SetCount.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F))).acceptFunction(LootingEnchantBonus.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F))))).addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.GOLD_INGOT)).acceptCondition(KilledByPlayer.builder()).acceptCondition(RandomChanceWithLooting.builder(0.025F, 0.01F))));
      this.addToLootMap(EntityType.ZOMBIE_VILLAGER, LootTable.builder().addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.ROTTEN_FLESH).acceptFunction(SetCount.createLootBuilder(RandomValueRange.createRandRange(0.0F, 2.0F))).acceptFunction(LootingEnchantBonus.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F))))).addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.IRON_INGOT)).addEntry(ItemLootEntry.builder(Items.CARROT)).addEntry(ItemLootEntry.builder(Items.POTATO)).acceptCondition(KilledByPlayer.builder()).acceptCondition(RandomChanceWithLooting.builder(0.025F, 0.01F))));
      Set<ResourceLocation> set = Sets.newHashSet();
      Iterator iterator = Registry.ENTITY_TYPE.iterator();


      //AH ADD ****
      this.addToLootMap(EntityType.ZOMBIE_NASTY, LootTable.builder()
              .addLootPool(LootPool.builder().rolls(ConstantRange.of(1))
                           .addEntry(ItemLootEntry.builder(Items.ROTTEN_FLESH)
                                    .acceptFunction(SetCount.createLootBuilder(RandomValueRange.createRandRange(0.0F, 2.0F)))
                                    .acceptFunction(LootingEnchantBonus.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F)))))
              .addLootPool(LootPool.builder().rolls(ConstantRange.of(1))
                           .addEntry(ItemLootEntry.builder(Items.DIAMOND))
                           .addEntry(ItemLootEntry.builder(Items.CAKE))
                           .addEntry(ItemLootEntry.builder(Items.PARROT_SPAWN_EGG))
                           .acceptCondition(KilledByPlayer.builder())
                           .acceptCondition(RandomChanceWithLooting.builder(0.1F, 0.02F))));
                           //.acceptCondition(RandomChanceWithLooting.builder(0.025F, 0.01F))));

      this.addToLootMap(EntityType.ZOMBIE_MEAN, LootTable.builder()
              .addLootPool(LootPool.builder().rolls(ConstantRange.of(1))
                      .addEntry(ItemLootEntry.builder(Items.ROTTEN_FLESH)
                              .acceptFunction(SetCount.createLootBuilder(RandomValueRange.createRandRange(0.0F, 2.0F)))
                              .acceptFunction(LootingEnchantBonus.createLootBuilder(RandomValueRange.createRandRange(0.0F, 1.0F)))))
              .addLootPool(LootPool.builder().rolls(ConstantRange.of(1))
                      .addEntry(ItemLootEntry.builder(Items.DIAMOND))
                      .addEntry(ItemLootEntry.builder(Items.CAKE))
                      .addEntry(ItemLootEntry.builder(Items.PARROT_SPAWN_EGG))
                      .acceptCondition(KilledByPlayer.builder())
                      .acceptCondition(RandomChanceWithLooting.builder(0.1F, 0.02F))));
      //.acceptCondition(RandomChanceWithLooting.builder(0.025F, 0.01F))));

      //AH END ****

      EntityType<?> entitytype;
      ResourceLocation resourcelocation;
      while(true) {
         if (!iterator.hasNext()) {
            this.lootBuilderMap.forEach(p_accept_1_::accept);
            return;
         }

         entitytype = (EntityType)iterator.next();
         resourcelocation = entitytype.getLootTable();
         if (!entitySet.contains(entitytype) && entitytype.getClassification() == EntityClassification.MISC) {
            if (resourcelocation != LootTables.EMPTY && this.lootBuilderMap.remove(resourcelocation) != null) {
               break;
            }
         } else if (resourcelocation != LootTables.EMPTY && set.add(resourcelocation)) {
            LootTable.Builder loottable$builder = this.lootBuilderMap.remove(resourcelocation);
            if (loottable$builder == null) {
               throw new IllegalStateException(String.format("Missing loottable '%s' for '%s'", resourcelocation, Registry.ENTITY_TYPE.getKey(entitytype)));
            }

            p_accept_1_.accept(resourcelocation, loottable$builder);
         }
      }

      throw new IllegalStateException(String.format("Weird loottable '%s' for '%s', not a LivingEntity so should not have loot", resourcelocation, Registry.ENTITY_TYPE.getKey(entitytype)));
   }

   protected void addToLootMap(EntityType<?> entityType, LootTable.Builder lootBuilder) {
      this.addToLootMap(entityType.getLootTable(), lootBuilder);
   }

   protected void addToLootMap(ResourceLocation location, LootTable.Builder lootBuilder) {
      this.lootBuilderMap.put(location, lootBuilder);
   }
}
