package io.github.nevalackin.client.util.player;

import com.google.common.collect.Multimap;
import io.github.nevalackin.client.impl.core.KetamineClient;
import io.github.nevalackin.client.impl.module.misc.inventory.InventoryManager;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAppleGold;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

public final class InventoryUtil {
   public static final int INCLUDE_ARMOR_BEGIN = 5;
   public static final int EXCLUDE_ARMOR_BEGIN = 9;
   public static final int ONLY_HOT_BAR_BEGIN = 36;
   public static final int END = 45;
   private static InventoryManager inventoryManager;

   private InventoryUtil() {
   }

   public static int findSlotMatching(EntityPlayerSP player, Predicate cond) {
      for(int i = 44; i >= 9; --i) {
         ItemStack stack = player.inventoryContainer.getSlot(i).getStack();
         if (cond.test(stack)) {
            return i;
         }
      }

      return -1;
   }

   public static int getBestBlockStack(Minecraft mc, int start, int end) {
      int bestSlot = -1;
      int bestSlotStackSize = 0;

      for(int i = start; i < end; ++i) {
         ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
         if (stack != null && stack.stackSize > bestSlotStackSize && stack.getItem() instanceof ItemBlock && isStackValidToPlace(stack)) {
            bestSlot = i;
            bestSlotStackSize = stack.stackSize;
         }
      }

      return bestSlot;
   }

   public static int getSlimeBlockStack(Minecraft mc, int start, int end) {
      int bestSlot = -1;
      int bestSlotStackSize = 0;

      for(int i = start; i < end; ++i) {
         ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
         if (stack != null && stack.stackSize > bestSlotStackSize && stack.getItem() instanceof ItemBlock && ((ItemBlock)stack.getItem()).getBlock() == Blocks.slime_block && stack.stackSize >= 1) {
            bestSlot = i;
            bestSlotStackSize = stack.stackSize;
         }
      }

      return bestSlot;
   }

   public static boolean hasFreeSlots(EntityPlayerSP player) {
      for(int i = 9; i < 45; ++i) {
         if (!player.inventoryContainer.getSlot(i).getHasStack()) {
            return true;
         }
      }

      return false;
   }

   public static boolean isValidStack(EntityPlayerSP player, ItemStack stack) {
      if (stack == null) {
         return false;
      } else {
         Item item = stack.getItem();
         if (item instanceof ItemSword) {
            return isBestSword(player, stack);
         } else if (item instanceof ItemArmor) {
            return isBestArmor(player, stack);
         } else if (item instanceof ItemTool) {
            return isBestTool(player, stack);
         } else if (item instanceof ItemBow) {
            return isBestBow(player, stack);
         } else if (item instanceof ItemFood) {
            return isGoodFood(stack);
         } else if (item instanceof ItemBlock) {
            return isStackValidToPlace(stack);
         } else {
            return item instanceof ItemPotion ? isBuffPotion(stack) : isGoodItem(item);
         }
      }
   }

   public static boolean isGoodItem(Item item) {
      return item instanceof ItemEnderPearl || item == Items.arrow || item instanceof ItemBlock && ((ItemBlock)item).getBlock() == Blocks.slime_block;
   }

   public static boolean isBestSword(EntityPlayerSP player, ItemStack itemStack) {
      double damage = 0.0;
      ItemStack bestStack = null;

      for(int i = 9; i < 45; ++i) {
         ItemStack stack = player.inventoryContainer.getSlot(i).getStack();
         if (stack != null && stack.getItem() instanceof ItemSword) {
            double newDamage = getItemDamage(stack);
            if (newDamage > damage) {
               damage = newDamage;
               bestStack = stack;
            }
         }
      }

      return bestStack == itemStack || getItemDamage(itemStack) > damage;
   }

   public static boolean isBestArmor(EntityPlayerSP player, ItemStack itemStack) {
      ItemArmor itemArmor = (ItemArmor)itemStack.getItem();
      double reduction = 0.0;
      ItemStack bestStack = null;

      for(int i = 5; i < 45; ++i) {
         ItemStack stack = player.inventoryContainer.getSlot(i).getStack();
         if (stack != null && stack.getItem() instanceof ItemArmor) {
            ItemArmor stackArmor = (ItemArmor)stack.getItem();
            if (stackArmor.armorType == itemArmor.armorType) {
               double newReduction = getDamageReduction(stack);
               if (newReduction > reduction) {
                  reduction = newReduction;
                  bestStack = stack;
               }
            }
         }
      }

      return bestStack == itemStack || getDamageReduction(itemStack) > reduction;
   }

   public static int getToolType(ItemStack stack) {
      ItemTool tool = (ItemTool)stack.getItem();
      if (tool instanceof ItemPickaxe) {
         return 0;
      } else if (tool instanceof ItemAxe) {
         return 1;
      } else {
         return tool instanceof ItemSpade ? 2 : -1;
      }
   }

   public static boolean isBestTool(EntityPlayerSP player, ItemStack itemStack) {
      int type = getToolType(itemStack);
      Tool bestTool = new Tool(-1, -1.0, (ItemStack)null);

      for(int i = 9; i < 45; ++i) {
         ItemStack stack = player.inventoryContainer.getSlot(i).getStack();
         if (stack != null && stack.getItem() instanceof ItemTool && type == getToolType(stack)) {
            double efficiency = (double)getToolEfficiency(stack);
            if (efficiency > bestTool.getEfficiency()) {
               bestTool = new Tool(i, efficiency, stack);
            }
         }
      }

      return bestTool.getStack() == itemStack || (double)getToolEfficiency(itemStack) > bestTool.getEfficiency();
   }

   public static boolean isBestBow(EntityPlayerSP player, ItemStack itemStack) {
      double bestBowDmg = -1.0;
      ItemStack bestBow = null;

      for(int i = 9; i < 45; ++i) {
         ItemStack stack = player.inventoryContainer.getSlot(i).getStack();
         if (stack != null && stack.getItem() instanceof ItemBow) {
            double damage = getBowDamage(stack);
            if (damage > bestBowDmg) {
               bestBow = stack;
               bestBowDmg = damage;
            }
         }
      }

      return itemStack == bestBow || getBowDamage(itemStack) > bestBowDmg;
   }

   public static double getDamageReduction(ItemStack stack) {
      double reduction = 0.0;
      ItemArmor armor = (ItemArmor)stack.getItem();
      reduction += (double)armor.damageReduceAmount;
      if (stack.isItemEnchanted()) {
         reduction += (double)EnchantmentHelper.getEnchantmentLevel(Enchantment.protection.effectId, stack) * 0.25;
      }

      return reduction;
   }

   public static boolean isBuffPotion(ItemStack stack) {
      ItemPotion potion = (ItemPotion)stack.getItem();
      List effects = potion.getEffects(stack);
      Iterator var3 = effects.iterator();

      PotionEffect effect;
      do {
         if (!var3.hasNext()) {
            return true;
         }

         effect = (PotionEffect)var3.next();
      } while(!Potion.potionTypes[effect.getPotionID()].isBadEffect());

      return false;
   }

   public static double getBowDamage(ItemStack stack) {
      double damage = 0.0;
      if (stack.getItem() instanceof ItemBow && stack.isItemEnchanted()) {
         damage += (double)EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, stack);
      }

      return damage;
   }

   public static boolean isGoodFood(ItemStack stack) {
      ItemFood food = (ItemFood)stack.getItem();
      if (food instanceof ItemAppleGold) {
         return true;
      } else {
         return food.getHealAmount(stack) >= 4 && food.getSaturationModifier(stack) >= 0.3F;
      }
   }

   public static float getToolEfficiency(ItemStack itemStack) {
      ItemTool tool = (ItemTool)itemStack.getItem();
      float efficiency = tool.getEfficiencyOnProperMaterial();
      int lvl = EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, itemStack);
      if (efficiency > 1.0F && lvl > 0) {
         efficiency += (float)(lvl * lvl + 1);
      }

      return efficiency;
   }

   public static double getItemDamage(ItemStack stack) {
      double damage = 0.0;
      Multimap attributeModifierMap = stack.getAttributeModifiers();
      Iterator var4 = attributeModifierMap.keySet().iterator();

      while(var4.hasNext()) {
         String attributeName = (String)var4.next();
         if (attributeName.equals("generic.attackDamage")) {
            Iterator attributeModifiers = attributeModifierMap.get(attributeName).iterator();
            if (attributeModifiers.hasNext()) {
               damage += ((AttributeModifier)attributeModifiers.next()).getAmount();
            }
            break;
         }
      }

      if (stack.isItemEnchanted()) {
         damage += (double)EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, stack);
         damage += (double)EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, stack) * 1.25;
      }

      return damage;
   }

   public static void windowClick(Minecraft mc, int windowId, int slotId, int mouseButtonClicked, ClickType mode) {
      mc.playerController.windowClick(windowId, slotId, mouseButtonClicked, mode.ordinal(), mc.thePlayer);
   }

   public static void windowClick(Minecraft mc, int slotId, int mouseButtonClicked, ClickType mode) {
      mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, slotId, mouseButtonClicked, mode.ordinal(), mc.thePlayer);
   }

   public static void queueClickRequest(WindowClickRequest request) {
      if (inventoryManager == null) {
         inventoryManager = (InventoryManager)KetamineClient.getInstance().getModuleManager().getModule(InventoryManager.class);
      }

      inventoryManager.getClickRequests().add(request);
   }

   public static boolean isStackValidToPlace(ItemStack stack) {
      return stack.stackSize >= 1 && validateBlock(Block.getBlockFromItem(stack.getItem()), InventoryUtil.BlockAction.PLACE);
   }

   public static boolean validateBlock(Block block, BlockAction action) {
      if (block instanceof BlockContainer) {
         return false;
      } else {
         Material material = block.getMaterial();
         switch (action) {
            case PLACE:
               return !(block instanceof BlockFalling) && block.isFullBlock() && block.isFullCube();
            case REPLACE:
               return material.isReplaceable();
            case PLACE_ON:
               return !(block instanceof BlockAir);
            default:
               return true;
         }
      }
   }

   private static class Tool {
      private final int slot;
      private final double efficiency;
      private final ItemStack stack;

      public Tool(int slot, double efficiency, ItemStack stack) {
         this.slot = slot;
         this.efficiency = efficiency;
         this.stack = stack;
      }

      public int getSlot() {
         return this.slot;
      }

      public double getEfficiency() {
         return this.efficiency;
      }

      public ItemStack getStack() {
         return this.stack;
      }
   }

   public static enum ClickType {
      CLICK,
      SHIFT_CLICK,
      SWAP_WITH_HOT_BAR_SLOT,
      PLACEHOLDER,
      DROP_ITEM;
   }

   public static enum BlockAction {
      PLACE,
      REPLACE,
      PLACE_ON;
   }
}
