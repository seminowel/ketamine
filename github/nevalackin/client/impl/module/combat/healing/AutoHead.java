package io.github.nevalackin.client.impl.module.combat.healing;

import io.github.nevalackin.client.api.module.Category;
import io.github.nevalackin.client.api.module.Module;
import io.github.nevalackin.client.api.property.Property;
import io.github.nevalackin.client.impl.core.KetamineClient;
import io.github.nevalackin.client.impl.module.misc.player.FastUse;
import io.github.nevalackin.client.impl.property.DoubleProperty;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.potion.Potion;

public class AutoHead extends Module {
   private int ticksSinceLastEat;
   private final DoubleProperty delayProperty = new DoubleProperty("Delay", 1000.0, 50.0, 1000.0, 50.0);
   private final DoubleProperty healthProperty = new DoubleProperty("Health", 6.0, 1.0, 10.0, 0.5);
   @EventLink
   private final Listener onUpdate = (event) -> {
      if (event.isPre()) {
         ++this.ticksSinceLastEat;
         if ((double)this.ticksSinceLastEat <= (Double)this.delayProperty.getValue() / 50.0) {
            return;
         }

         if ((double)this.mc.thePlayer.getHealth() < (Double)this.healthProperty.getValue() * 2.0) {
            for(int slot = 36; slot < 45; ++slot) {
               ItemStack stack = this.mc.thePlayer.inventoryContainer.getSlot(slot).getStack();
               if (stack != null) {
                  int itemID = Item.getIdFromItem(stack.getItem());
                  boolean shouldEatID = itemID == 258 && ((FastUse)KetamineClient.getInstance().getModuleManager().getModule(FastUse.class)).isEnabled() || itemID == 282 || itemID == Item.getIdFromItem(Items.skull) || itemID == Item.getIdFromItem(Items.baked_potato) || itemID == Item.getIdFromItem(Items.magma_cream) || itemID == Item.getIdFromItem(Items.mutton);
                  if (shouldEatID && !this.mc.thePlayer.isPotionActive(Potion.regeneration)) {
                     slot -= 36;
                     this.mc.thePlayer.sendQueue.sendPacket(new C09PacketHeldItemChange(slot));
                     this.mc.thePlayer.sendQueue.sendPacket(new C08PacketPlayerBlockPlacement((ItemStack)null));
                     this.mc.thePlayer.sendQueue.sendPacket(new C09PacketHeldItemChange(this.mc.thePlayer.inventory.currentItem));
                     this.ticksSinceLastEat = 0;
                     return;
                  }
               }
            }
         }
      }

   };

   public AutoHead() {
      super("Auto Head", Category.COMBAT, Category.SubCategory.COMBAT_HEALING);
      this.register(new Property[]{this.delayProperty, this.healthProperty});
   }

   public void onEnable() {
      this.ticksSinceLastEat = 0;
   }

   public void onDisable() {
   }
}
