package io.github.nevalackin.client.impl.module.combat.healing;

import io.github.nevalackin.client.api.module.Category;
import io.github.nevalackin.client.api.module.Module;
import io.github.nevalackin.client.api.property.Property;
import io.github.nevalackin.client.impl.property.DoubleProperty;
import io.github.nevalackin.client.util.player.InventoryUtil;
import io.github.nevalackin.client.util.player.WindowClickRequest;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

public final class AutoPot extends Module {
   private int ticksSinceLastPot;
   private final Map potionRequirementMap = new HashMap();
   private final DoubleProperty delayProperty = new DoubleProperty("Delay", 1000.0, 50.0, 1000.0, 50.0);
   private final DoubleProperty healthProperty = new DoubleProperty("Health", 6.0, 1.0, 10.0, 0.5);
   public boolean potting;
   public boolean setPitch;
   private WindowClickRequest lastRequest;
   @EventLink(1)
   private final Listener onUpdate = (event) -> {
      if (event.isPre()) {
         ++this.ticksSinceLastPot;
         if (this.mc.currentScreen != null && !(this.mc.currentScreen instanceof GuiInventory)) {
            return;
         }

         if (this.mc.thePlayer.fallDistance > 2.0F) {
            return;
         }

         if ((double)this.ticksSinceLastPot <= (Double)this.delayProperty.getValue() / 50.0) {
            return;
         }

         if (this.mc.thePlayer.isEating()) {
            return;
         }

         if (!this.mc.thePlayer.onGround) {
            return;
         }

         final int mostImportantSlot = this.getMostImportantPotion();
         if (!event.isRotating() && mostImportantSlot != -1) {
            if (mostImportantSlot < 36) {
               if (this.lastRequest == null || this.lastRequest.isCompleted()) {
                  InventoryUtil.queueClickRequest(this.lastRequest = new WindowClickRequest() {
                     public void performRequest() {
                        InventoryUtil.windowClick(AutoPot.this.mc, mostImportantSlot, 7, InventoryUtil.ClickType.SWAP_WITH_HOT_BAR_SLOT);
                     }
                  });
               }

               return;
            }

            mostImportantSlot -= 36;
            if (!this.setPitch) {
               event.setPitch(90.0F);
               this.setPitch = true;
            }

            this.mc.thePlayer.sendQueue.sendPacket(new C09PacketHeldItemChange(mostImportantSlot));
            this.potting = true;
         }
      } else if (this.setPitch && this.potting) {
         this.mc.thePlayer.sendQueue.sendPacket(new C08PacketPlayerBlockPlacement((ItemStack)null));
         this.mc.thePlayer.sendQueue.sendPacket(new C09PacketHeldItemChange(this.mc.thePlayer.inventory.currentItem));
         this.ticksSinceLastPot = 0;
         this.potting = false;
         this.setPitch = false;
      }

   };

   public AutoPot() {
      super("Auto Potion", Category.COMBAT, Category.SubCategory.COMBAT_HEALING);
      this.register(new Property[]{this.delayProperty, this.healthProperty});
      Requirement healthBelowRequirement = new HealthBelowRequirement();
      Requirement betterThanCurrentRequirement = new BetterThanCurrentRequirement();
      Requirement notCurrentlyActiveRequirement = new NotCurrentlyActiveRequirement();
      this.potionRequirementMap.put(Potion.moveSpeed.getId(), new Requirement[]{betterThanCurrentRequirement});
      this.potionRequirementMap.put(Potion.regeneration.getId(), new Requirement[]{betterThanCurrentRequirement, notCurrentlyActiveRequirement, healthBelowRequirement});
      this.potionRequirementMap.put(Potion.heal.getId(), new Requirement[]{healthBelowRequirement});
   }

   public void onEnable() {
      this.potting = false;
      this.setPitch = false;
   }

   public void onDisable() {
   }

   private int getMostImportantPotion() {
      Map effectsInSlots;
      if ((effectsInSlots = this.doSearchPotion(36, 45)).isEmpty()) {
         effectsInSlots = this.doSearchPotion(9, 36);
         if (effectsInSlots.isEmpty()) {
            return -1;
         }
      }

      Map potionIDAmplifiers = new HashMap();
      Iterator var3 = effectsInSlots.keySet().iterator();

      Integer slot;
      PotionEffect effect;
      while(var3.hasNext()) {
         slot = (Integer)var3.next();
         effect = (PotionEffect)effectsInSlots.get(slot);
         if (potionIDAmplifiers.containsKey(effect.getPotionID())) {
            int amplifier = (Integer)potionIDAmplifiers.get(effect.getPotionID());
            if (effect.getAmplifier() > amplifier) {
               potionIDAmplifiers.put(effect.getPotionID(), effect.getAmplifier());
            }
         } else {
            potionIDAmplifiers.put(effect.getPotionID(), effect.getAmplifier());
         }
      }

      var3 = effectsInSlots.keySet().iterator();

      do {
         if (!var3.hasNext()) {
            return -1;
         }

         slot = (Integer)var3.next();
         effect = (PotionEffect)effectsInSlots.get(slot);
         if (this.getMostImportantInList(potionIDAmplifiers, effect, Potion.heal.getId())) {
            return slot;
         }

         if (this.getMostImportantInList(potionIDAmplifiers, effect, Potion.regeneration.getId())) {
            return slot;
         }
      } while(!this.getMostImportantInList(potionIDAmplifiers, effect, Potion.moveSpeed.getId()));

      return slot;
   }

   private boolean getMostImportantInList(Map potionIDAmplifiers, PotionEffect effect, int filterPotionID) {
      Iterator var4 = potionIDAmplifiers.keySet().iterator();

      Integer potionID;
      Integer amplifier;
      do {
         if (!var4.hasNext()) {
            return false;
         }

         potionID = (Integer)var4.next();
         amplifier = (Integer)potionIDAmplifiers.get(potionID);
      } while(effect.getPotionID() != potionID || effect.getAmplifier() != amplifier || potionID != filterPotionID);

      return true;
   }

   private Map doSearchPotion(int start, int end) {
      Map slotEffectMap = new HashMap();

      for(int i = start; i < end; ++i) {
         ItemStack stack = this.mc.thePlayer.inventoryContainer.getSlot(i).getStack();
         if (stack != null && stack.getItem() instanceof ItemPotion && ItemPotion.isSplash(stack.getMetadata()) && InventoryUtil.isBuffPotion(stack)) {
            ItemPotion potion = (ItemPotion)stack.getItem();
            List effects = potion.getEffects(stack.getMetadata());
            if (effects != null) {
               Iterator var8 = effects.iterator();

               label68:
               while(var8.hasNext()) {
                  PotionEffect effect = (PotionEffect)var8.next();
                  Iterator var10 = this.potionRequirementMap.keySet().iterator();

                  while(true) {
                     while(true) {
                        Integer potionID;
                        do {
                           if (!var10.hasNext()) {
                              continue label68;
                           }

                           potionID = (Integer)var10.next();
                        } while(effect.getPotionID() != potionID);

                        Requirement[] requirements = (Requirement[])this.potionRequirementMap.get(potionID);
                        if (requirements.length == 1) {
                           if (requirements[0].test(this.mc.thePlayer, ((Double)this.healthProperty.getValue()).floatValue() * 2.0F, effect.getAmplifier(), potionID)) {
                              slotEffectMap.put(i, effect);
                           }
                        } else if (requirements.length > 1) {
                           boolean pass = false;
                           Requirement[] var14 = requirements;
                           int var15 = requirements.length;

                           for(int var16 = 0; var16 < var15; ++var16) {
                              Requirement requirement = var14[var16];
                              pass = requirement.test(this.mc.thePlayer, ((Double)this.healthProperty.getValue()).floatValue() * 2.0F, effect.getAmplifier(), potionID);
                           }

                           if (pass) {
                              slotEffectMap.put(i, effect);
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      return slotEffectMap;
   }

   private static class BetterThanCurrentRequirement implements Requirement {
      private BetterThanCurrentRequirement() {
      }

      public boolean test(EntityPlayerSP player, float healthTarget, int currentAmplifier, int potionId) {
         PotionEffect effect = player.getActivePotionEffect(potionId);
         return effect == null || effect.getAmplifier() < currentAmplifier;
      }

      // $FF: synthetic method
      BetterThanCurrentRequirement(Object x0) {
         this();
      }
   }

   private static class NotCurrentlyActiveRequirement implements Requirement {
      private NotCurrentlyActiveRequirement() {
      }

      public boolean test(EntityPlayerSP player, float healthTarget, int currentAmplifier, int potionId) {
         return player.isPotionActive(potionId);
      }

      // $FF: synthetic method
      NotCurrentlyActiveRequirement(Object x0) {
         this();
      }
   }

   private static class HealthBelowRequirement implements Requirement {
      private HealthBelowRequirement() {
      }

      public boolean test(EntityPlayerSP player, float healthTarget, int currentAmplifier, int potionId) {
         return player.getHealth() < healthTarget;
      }

      // $FF: synthetic method
      HealthBelowRequirement(Object x0) {
         this();
      }
   }

   private interface Requirement {
      boolean test(EntityPlayerSP var1, float var2, int var3, int var4);
   }
}
