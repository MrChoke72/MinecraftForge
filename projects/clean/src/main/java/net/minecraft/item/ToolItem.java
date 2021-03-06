package net.minecraft.item;

import com.google.common.collect.Multimap;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ToolItem extends TieredItem {
   private final Set<Block> effectiveBlocks;
   protected final float efficiency;
   protected final float attackDamage;
   protected final float attackSpeed;

   protected ToolItem(float attackDamageIn, float attackSpeedIn, IItemTier tier, Set<Block> effectiveBlocksIn, Item.Properties builder) {
      super(tier, builder);
      this.effectiveBlocks = effectiveBlocksIn;
      this.efficiency = tier.getEfficiency();
      this.attackDamage = attackDamageIn + tier.getAttackDamage();
      this.attackSpeed = attackSpeedIn;
   }

   public float getDestroySpeed(ItemStack stack, BlockState state) {
      return this.effectiveBlocks.contains(state.getBlock()) ? this.efficiency : 1.0F;
   }

   public boolean hitEntity(ItemStack stack, LivingEntity target, LivingEntity attacker) {
      stack.damageItem(2, attacker, (p_220039_0_) -> {
         p_220039_0_.sendBreakAnimation(EquipmentSlotType.MAINHAND);
      });
      return true;
   }

   public boolean onBlockDestroyed(ItemStack stack, World worldIn, BlockState state, BlockPos pos, LivingEntity entityLiving) {
      if (!worldIn.isRemote && state.getBlockHardness(worldIn, pos) != 0.0F) {
         stack.damageItem(1, entityLiving, (p_220038_0_) -> {
            p_220038_0_.sendBreakAnimation(EquipmentSlotType.MAINHAND);
         });
      }

      return true;
   }

   public Multimap<String, AttributeModifier> getAttributeModifiers(EquipmentSlotType equipmentSlot) {
      Multimap<String, AttributeModifier> multimap = super.getAttributeModifiers(equipmentSlot);
      if (equipmentSlot == EquipmentSlotType.MAINHAND) {
         multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Tool modifier", (double)this.attackDamage, AttributeModifier.Operation.ADDITION));
         multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Tool modifier", (double)this.attackSpeed, AttributeModifier.Operation.ADDITION));
      }

      return multimap;
   }
}
