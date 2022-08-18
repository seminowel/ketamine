package io.github.nevalackin.client.impl.module.misc.inventory;

import io.github.nevalackin.client.api.module.Category;
import io.github.nevalackin.client.api.module.Module;
import io.github.nevalackin.client.api.property.Property;
import io.github.nevalackin.client.impl.core.KetamineClient;
import io.github.nevalackin.client.impl.module.combat.rage.Aura;
import io.github.nevalackin.client.impl.property.BooleanProperty;
import io.github.nevalackin.client.impl.property.DoubleProperty;
import io.github.nevalackin.client.impl.property.EnumProperty;
import io.github.nevalackin.client.util.player.InventoryUtil;
import io.github.nevalackin.client.util.player.WindowClickRequest;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.item.ItemAppleGold;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.network.play.server.S2DPacketOpenWindow;

public final class InventoryManager extends Module {
   private final EnumProperty modeProperty;
   private final DoubleProperty delayProperty;
   private final BooleanProperty dropItemsProperty;
   private final BooleanProperty sortItemsProperty;
   private final BooleanProperty autoArmorProperty;
   private final BooleanProperty ignoreItemsWithCustomName;
   private final int[] bestArmorPieces;
   private final List trash;
   private final int[] bestToolSlots;
   private final List gappleStackSlots;
   private int bestSwordSlot;
   private int bestBowSlot;
   private final List clickRequests;
   private boolean serverOpen;
   private boolean clientOpen;
   private int ticksSinceLastClick;
   private boolean nextTickCloseInventory;
   private Aura aura;
   @EventLink
   private final Listener onSendPacket;
   @EventLink
   private final Listener onWindowClick;
   @EventLink
   private final Listener onReceivePacket;
   @EventLink
   private final Listener onUpdate;

   public InventoryManager() {
      super("Inventory Manager", Category.MISC, Category.SubCategory.MISC_INVENTORY);
      this.modeProperty = new EnumProperty("Mode", InventoryManager.Mode.WHILE_OPEN);
      this.delayProperty = new DoubleProperty("Delay", 150.0, 0.0, 500.0, 50.0);
      this.dropItemsProperty = new BooleanProperty("Drop Items", true);
      this.sortItemsProperty = new BooleanProperty("Sort Items", true);
      this.autoArmorProperty = new BooleanProperty("Auto Armor", true);
      this.ignoreItemsWithCustomName = new BooleanProperty("Ignore Custom Name", true);
      this.bestArmorPieces = new int[4];
      this.trash = new ArrayList();
      this.bestToolSlots = new int[3];
      this.gappleStackSlots = new ArrayList();
      this.clickRequests = new ArrayList();
      this.onSendPacket = (event) -> {
         Packet packet = event.getPacket();
         if (packet instanceof C16PacketClientStatus) {
            C16PacketClientStatus clientStatus = (C16PacketClientStatus)packet;
            if (clientStatus.getStatus() == C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT) {
               this.clientOpen = true;
               this.serverOpen = true;
            }
         } else if (packet instanceof C0DPacketCloseWindow) {
            C0DPacketCloseWindow packetCloseWindow = (C0DPacketCloseWindow)packet;
            if (packetCloseWindow.getWindowId() == this.mc.thePlayer.inventoryContainer.windowId) {
               this.clientOpen = false;
               this.serverOpen = false;
            }
         }

      };
      this.onWindowClick = (event) -> {
         this.ticksSinceLastClick = 0;
      };
      this.onReceivePacket = (event) -> {
         Packet packet = event.getPacket();
         if (packet instanceof S2DPacketOpenWindow) {
            this.clientOpen = false;
            this.serverOpen = false;
         }

      };
      this.onUpdate = (event) -> {
         if (event.isPre()) {
            ++this.ticksSinceLastClick;
            if (!((double)this.ticksSinceLastClick < Math.floor((Double)this.delayProperty.getValue() / 50.0))) {
               if (this.aura.getTarget() == null) {
                  Aura var10000 = this.aura;
                  if (!Aura.canAutoBlock()) {
                     if (this.clientOpen || this.mc.currentScreen == null && this.modeProperty.getValue() != InventoryManager.Mode.WHILE_OPEN) {
                        this.clear();

                        for(int slot = 5; slot < 45; ++slot) {
                           ItemStack stack = this.mc.thePlayer.inventoryContainer.getSlot(slot).getStack();
                           if (stack != null && (!(Boolean)this.ignoreItemsWithCustomName.getValue() || !stack.hasDisplayName())) {
                              if (stack.getItem() instanceof ItemSword && InventoryUtil.isBestSword(this.mc.thePlayer, stack)) {
                                 this.bestSwordSlot = slot;
                              } else if (stack.getItem() instanceof ItemTool && InventoryUtil.isBestTool(this.mc.thePlayer, stack)) {
                                 int toolType = InventoryUtil.getToolType(stack);
                                 if (toolType != -1 && slot != this.bestToolSlots[toolType]) {
                                    this.bestToolSlots[toolType] = slot;
                                 }
                              } else if (stack.getItem() instanceof ItemArmor && InventoryUtil.isBestArmor(this.mc.thePlayer, stack)) {
                                 ItemArmor armor = (ItemArmor)stack.getItem();
                                 int pieceSlot = this.bestArmorPieces[armor.armorType];
                                 if (pieceSlot == -1 || slot != pieceSlot) {
                                    this.bestArmorPieces[armor.armorType] = slot;
                                 }
                              } else if (stack.getItem() instanceof ItemBow && InventoryUtil.isBestBow(this.mc.thePlayer, stack)) {
                                 if (slot != this.bestBowSlot) {
                                    this.bestBowSlot = slot;
                                 }
                              } else if (stack.getItem() instanceof ItemAppleGold) {
                                 this.gappleStackSlots.add(slot);
                              } else if (!this.trash.contains(slot) && !isValidStack(stack)) {
                                 this.trash.add(slot);
                              }
                           }
                        }

                        boolean busy = !this.trash.isEmpty() && (Boolean)this.dropItemsProperty.getValue() || this.equipArmor(false) || this.sortItems(false) || !this.clickRequests.isEmpty();
                        if (!busy) {
                           if (this.nextTickCloseInventory) {
                              this.close();
                              this.nextTickCloseInventory = false;
                           } else {
                              this.nextTickCloseInventory = true;
                           }

                           return;
                        }

                        boolean waitUntilNextTick = !this.serverOpen;
                        this.open();
                        if (this.nextTickCloseInventory) {
                           this.nextTickCloseInventory = false;
                        }

                        if (waitUntilNextTick) {
                           return;
                        }

                        if (!this.clickRequests.isEmpty()) {
                           WindowClickRequest request = (WindowClickRequest)this.clickRequests.remove(0);
                           request.performRequest();
                           request.onCompleted();
                           return;
                        }

                        if (this.equipArmor(true)) {
                           return;
                        }

                        if (this.dropItem(this.trash)) {
                           return;
                        }

                        this.sortItems(true);
                     }

                     return;
                  }
               }

               if (this.nextTickCloseInventory) {
                  this.nextTickCloseInventory = false;
               }

               this.close();
            }
         }
      };
      this.register(new Property[]{this.modeProperty, this.delayProperty, this.dropItemsProperty, this.sortItemsProperty, this.autoArmorProperty, this.ignoreItemsWithCustomName});
   }

   private boolean isSpoof() {
      return this.modeProperty.getValue() == InventoryManager.Mode.SPOOF;
   }

   private boolean dropItem(List listOfSlots) {
      if ((Boolean)this.dropItemsProperty.getValue() && !listOfSlots.isEmpty()) {
         int slot = (Integer)listOfSlots.remove(0);
         InventoryUtil.windowClick(this.mc, slot, 1, InventoryUtil.ClickType.DROP_ITEM);
         return true;
      } else {
         return false;
      }
   }

   public List getClickRequests() {
      return this.clickRequests;
   }

   private boolean sortItems(boolean moveItems) {
      if ((Boolean)this.sortItemsProperty.getValue()) {
         if (this.bestSwordSlot != -1 && this.bestSwordSlot != 36) {
            if (moveItems) {
               this.putItemInSlot(36, this.bestSwordSlot);
               this.bestSwordSlot = 36;
            }

            return true;
         }

         if (this.bestBowSlot != -1 && this.bestBowSlot != 38) {
            if (moveItems) {
               this.putItemInSlot(38, this.bestBowSlot);
               this.bestBowSlot = 38;
            }

            return true;
         }

         if (!this.gappleStackSlots.isEmpty()) {
            this.gappleStackSlots.sort(Comparator.comparingInt((slot) -> {
               return this.mc.thePlayer.inventoryContainer.getSlot(slot).getStack().stackSize;
            }));
            int bestGappleSlot = (Integer)this.gappleStackSlots.get(0);
            if (bestGappleSlot != 37) {
               if (moveItems) {
                  this.putItemInSlot(37, bestGappleSlot);
                  this.gappleStackSlots.set(0, 37);
               }

               return true;
            }
         }

         int[] toolSlots = new int[]{39, 40, 41};
         int[] var3 = this.bestToolSlots;
         int var4 = var3.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            int toolSlot = var3[var5];
            if (toolSlot != -1) {
               int type = InventoryUtil.getToolType(this.mc.thePlayer.inventoryContainer.getSlot(toolSlot).getStack());
               if (type != -1 && toolSlot != toolSlots[type]) {
                  if (moveItems) {
                     this.putToolsInSlot(type, toolSlots);
                  }

                  return true;
               }
            }
         }
      }

      return false;
   }

   private boolean equipArmor(boolean moveItems) {
      if ((Boolean)this.autoArmorProperty.getValue()) {
         for(int i = 0; i < this.bestArmorPieces.length; ++i) {
            int piece = this.bestArmorPieces[i];
            if (piece != -1) {
               int armorPieceSlot = i + 5;
               ItemStack stack = this.mc.thePlayer.inventoryContainer.getSlot(armorPieceSlot).getStack();
               if (stack == null) {
                  if (moveItems) {
                     InventoryUtil.windowClick(this.mc, piece, 0, InventoryUtil.ClickType.SHIFT_CLICK);
                  }

                  return true;
               }
            }
         }
      }

      return false;
   }

   private void putItemInSlot(int slot, int slotIn) {
      InventoryUtil.windowClick(this.mc, slotIn, slot - 36, InventoryUtil.ClickType.SWAP_WITH_HOT_BAR_SLOT);
   }

   private void putToolsInSlot(int tool, int[] toolSlots) {
      int toolSlot = toolSlots[tool];
      InventoryUtil.windowClick(this.mc, this.bestToolSlots[tool], toolSlot - 36, InventoryUtil.ClickType.SWAP_WITH_HOT_BAR_SLOT);
      this.bestToolSlots[tool] = toolSlot;
   }

   private static boolean isValidStack(ItemStack stack) {
      if (stack.getItem() instanceof ItemBlock && InventoryUtil.isStackValidToPlace(stack)) {
         return true;
      } else if (stack.getItem() instanceof ItemPotion && InventoryUtil.isBuffPotion(stack)) {
         return true;
      } else {
         return stack.getItem() instanceof ItemFood && InventoryUtil.isGoodFood(stack) ? true : InventoryUtil.isGoodItem(stack.getItem());
      }
   }

   public void onEnable() {
      if (this.aura == null) {
         this.aura = (Aura)KetamineClient.getInstance().getModuleManager().getModule(Aura.class);
      }

      this.ticksSinceLastClick = 0;
      this.clientOpen = this.mc.currentScreen instanceof GuiInventory;
      this.serverOpen = this.clientOpen;
   }

   public void onDisable() {
      this.close();
      this.clear();
      this.clickRequests.clear();
   }

   private void open() {
      if (!this.clientOpen && !this.serverOpen) {
         this.mc.thePlayer.sendQueue.sendPacket(new C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT));
         this.serverOpen = true;
      }

   }

   private void close() {
      if (!this.clientOpen && this.serverOpen) {
         this.mc.thePlayer.sendQueue.sendPacket(new C0DPacketCloseWindow(this.mc.thePlayer.inventoryContainer.windowId));
         this.serverOpen = false;
      }

   }

   private void clear() {
      this.trash.clear();
      this.bestBowSlot = -1;
      this.bestSwordSlot = -1;
      this.gappleStackSlots.clear();
      Arrays.fill(this.bestArmorPieces, -1);
      Arrays.fill(this.bestToolSlots, -1);
   }

   private static enum Mode {
      WHILE_OPEN("In Inventory"),
      SPOOF("Spoof");

      private final String name;

      private Mode(String name) {
         this.name = name;
      }

      public String toString() {
         return this.name;
      }
   }
}
