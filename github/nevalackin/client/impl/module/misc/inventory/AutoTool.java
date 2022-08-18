package io.github.nevalackin.client.impl.module.misc.inventory;

import io.github.nevalackin.client.api.module.Category;
import io.github.nevalackin.client.api.module.Module;
import io.github.nevalackin.client.api.property.Property;
import io.github.nevalackin.client.impl.core.KetamineClient;
import io.github.nevalackin.client.impl.module.combat.rage.Aura;
import io.github.nevalackin.client.impl.property.BooleanProperty;
import io.github.nevalackin.client.util.player.InventoryUtil;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;

public final class AutoTool extends Module {
   private Aura aura;
   private boolean switched;
   private int previousSlot;
   private final BooleanProperty autoWeaponProperty = new BooleanProperty("Auto Weapon", true);
   private final BooleanProperty switchBackProperty = new BooleanProperty("Switch back", true);
   @EventLink(3)
   private final Listener onUpdatePosition = (event) -> {
      if (event.isPre()) {
         if ((Boolean)this.switchBackProperty.getValue() && this.switched && this.previousSlot != -1) {
            this.mc.thePlayer.inventory.currentItem = this.previousSlot;
            this.previousSlot = -1;
            this.switched = false;
         }

         if ((!(Boolean)this.autoWeaponProperty.getValue() || this.aura.getTarget() == null) && (!this.isPointedEntity() || !this.mc.gameSettings.keyBindAttack.isKeyDown())) {
            if (this.isPointedBlock() && this.mc.gameSettings.keyBindAttack.isKeyDown()) {
               BlockPos blockPos = this.mc.objectMouseOver.getBlockPos();
               Block block = this.mc.theWorld.getBlockState(blockPos).getBlock();
               double bestToolEfficiency = 1.0;
               int bestToolSlot = -1;

               for(int i = 36; i < 45; ++i) {
                  ItemStack stackx = this.mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                  if (stackx != null && stackx.getItem() instanceof ItemTool) {
                     ItemTool tool = (ItemTool)stackx.getItem();
                     double eff = (double)tool.getStrVsBlock(stackx, block);
                     if (eff > bestToolEfficiency) {
                        bestToolEfficiency = eff;
                        bestToolSlot = i;
                     }
                  }
               }

               if (bestToolSlot != -1) {
                  this.previousSlot = this.mc.thePlayer.inventory.currentItem;
                  this.mc.thePlayer.inventory.currentItem = bestToolSlot - 36;
                  this.switched = true;
               }
            }
         } else {
            double bestDamage = 1.0;
            int bestWeaponSlot = -1;

            for(int ix = 36; ix < 45; ++ix) {
               ItemStack stack = this.mc.thePlayer.inventoryContainer.getSlot(ix).getStack();
               if (stack != null) {
                  double damage = InventoryUtil.getItemDamage(stack);
                  if (damage > bestDamage) {
                     bestDamage = damage;
                     bestWeaponSlot = ix;
                  }
               }
            }

            if (bestWeaponSlot != -1) {
               this.mc.thePlayer.inventory.currentItem = bestWeaponSlot - 36;
               this.previousSlot = this.mc.thePlayer.inventory.currentItem;
               this.switched = false;
            }
         }
      }

   };

   public AutoTool() {
      super("Auto Tool", Category.MISC, Category.SubCategory.MISC_INVENTORY);
      this.register(new Property[]{this.autoWeaponProperty, this.switchBackProperty});
   }

   private boolean isPointedEntity() {
      return this.mc.objectMouseOver != null && this.mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY && this.mc.objectMouseOver.entityHit != null;
   }

   private boolean isPointedBlock() {
      return this.mc.objectMouseOver != null && this.mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK;
   }

   public void onEnable() {
      if (this.aura == null) {
         this.aura = (Aura)KetamineClient.getInstance().getModuleManager().getModule(Aura.class);
      }

      this.switched = false;
      this.previousSlot = -1;
   }

   public void onDisable() {
      if (this.switched && this.previousSlot != -1) {
         this.mc.thePlayer.inventory.currentItem = this.previousSlot;
         this.previousSlot = -1;
         this.switched = false;
      }

   }
}
