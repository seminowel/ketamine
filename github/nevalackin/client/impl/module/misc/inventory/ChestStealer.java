package io.github.nevalackin.client.impl.module.misc.inventory;

import io.github.nevalackin.client.api.module.Category;
import io.github.nevalackin.client.api.module.Module;
import io.github.nevalackin.client.api.property.Property;
import io.github.nevalackin.client.impl.property.BooleanProperty;
import io.github.nevalackin.client.impl.property.DoubleProperty;
import io.github.nevalackin.client.util.player.InventoryUtil;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S2DPacketOpenWindow;

public final class ChestStealer extends Module {
   private final BooleanProperty autoDelayProperty = new BooleanProperty("Auto Delay", true);
   private final DoubleProperty delayProperty = new DoubleProperty("Delay", 100.0, () -> {
      return !(Boolean)this.autoDelayProperty.getValue();
   }, 0.0, 500.0, 50.0);
   private long lastClickTime;
   private int lastColumn;
   private int lastRow;
   private long nextDelay = 100L;
   @EventLink
   private final Listener onReceivePacket = (event) -> {
      Packet packet = event.getPacket();
      if (packet instanceof S2DPacketOpenWindow) {
         S2DPacketOpenWindow openWindow = (S2DPacketOpenWindow)packet;
         if (openWindow.getGuiId().equals("minecraft:container")) {
            this.reset();
         }
      }

   };
   @EventLink
   private final Listener onWindowClick = (event) -> {
      this.lastClickTime = System.currentTimeMillis();
   };
   @EventLink
   private final Listener onUpdate = (event) -> {
      if (event.isPre()) {
         long timeSinceLastClick = System.currentTimeMillis() - this.lastClickTime;
         if (timeSinceLastClick < this.nextDelay) {
            return;
         }

         GuiScreen current = this.mc.currentScreen;
         if (current instanceof GuiChest) {
            GuiChest guiChest = (GuiChest)current;
            IInventory lowerChestInventory = guiChest.getLowerChestInventory();
            String chestName = lowerChestInventory.getDisplayName().getUnformattedText();
            if (!chestName.equals(I18n.format("container.chest")) && !chestName.equals(I18n.format("container.chestDouble"))) {
               return;
            }

            if (!InventoryUtil.hasFreeSlots(this.mc.thePlayer)) {
               if (timeSinceLastClick > 100L) {
                  this.mc.thePlayer.closeScreen();
               }

               return;
            }

            int nSlots = lowerChestInventory.getSizeInventory();

            for(int i = 0; i < nSlots; ++i) {
               ItemStack stack = lowerChestInventory.getStackInSlot(i);
               if (InventoryUtil.isValidStack(this.mc.thePlayer, stack)) {
                  int column = i % 9;
                  int row = i % (nSlots / 9);
                  int columnDif = this.lastColumn - column;
                  int rowDif = this.lastRow - row;
                  this.nextDelay = (Boolean)this.autoDelayProperty.getValue() ? (long)Math.ceil(50.0 * Math.max(1.0, Math.sqrt((double)(columnDif * columnDif + rowDif * rowDif)))) : ((Double)this.delayProperty.getValue()).longValue();
                  if (timeSinceLastClick < this.nextDelay) {
                     return;
                  }

                  InventoryUtil.windowClick(this.mc, this.mc.thePlayer.openContainer.windowId, i, 0, InventoryUtil.ClickType.SHIFT_CLICK);
                  this.lastColumn = column;
                  this.lastRow = row;
                  return;
               }
            }

            if (timeSinceLastClick > 100L) {
               this.mc.thePlayer.closeScreen();
            }
         }
      }

   };

   public ChestStealer() {
      super("Stealer", Category.MISC, Category.SubCategory.MISC_INVENTORY);
      this.register(new Property[]{this.autoDelayProperty, this.delayProperty});
   }

   private void reset() {
      this.lastClickTime = System.currentTimeMillis();
      this.nextDelay = 100L;
      this.lastColumn = 0;
      this.lastRow = 0;
   }

   public void onEnable() {
      this.reset();
   }

   public void onDisable() {
   }
}
