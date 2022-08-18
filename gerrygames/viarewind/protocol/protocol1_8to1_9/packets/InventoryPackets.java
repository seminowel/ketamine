package de.gerrygames.viarewind.protocol.protocol1_8to1_9.packets;

import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonParser;
import com.viaversion.viaversion.protocols.protocol1_8.ServerboundPackets1_8;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ClientboundPackets1_9;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.items.ItemRewriter;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.storage.Windows;

public class InventoryPackets {
   public static void register(Protocol protocol) {
      protocol.registerClientbound(ClientboundPackets1_9.CLOSE_WINDOW, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.UNSIGNED_BYTE);
            this.handler((packetWrapper) -> {
               short windowsId = (Short)packetWrapper.get(Type.UNSIGNED_BYTE, 0);
               ((Windows)packetWrapper.user().get(Windows.class)).remove(windowsId);
            });
         }
      });
      protocol.registerClientbound(ClientboundPackets1_9.OPEN_WINDOW, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.UNSIGNED_BYTE);
            this.map(Type.STRING);
            this.map(Type.COMPONENT);
            this.map(Type.UNSIGNED_BYTE);
            this.handler((packetWrapper) -> {
               String type = (String)packetWrapper.get(Type.STRING, 0);
               if (type.equals("EntityHorse")) {
                  packetWrapper.passthrough(Type.INT);
               }

            });
            this.handler((packetWrapper) -> {
               short windowId = (Short)packetWrapper.get(Type.UNSIGNED_BYTE, 0);
               String windowType = (String)packetWrapper.get(Type.STRING, 0);
               ((Windows)packetWrapper.user().get(Windows.class)).put(windowId, windowType);
            });
            this.handler((packetWrapper) -> {
               String type = (String)packetWrapper.get(Type.STRING, 0);
               if (type.equalsIgnoreCase("minecraft:shulker_box")) {
                  type = "minecraft:container";
                  packetWrapper.set(Type.STRING, 0, "minecraft:container");
               }

               String name = ((JsonElement)packetWrapper.get(Type.COMPONENT, 0)).toString();
               if (name.equalsIgnoreCase("{\"translate\":\"container.shulkerBox\"}")) {
                  packetWrapper.set(Type.COMPONENT, 0, JsonParser.parseString("{\"text\":\"Shulker Box\"}"));
               }

            });
         }
      });
      protocol.registerClientbound(ClientboundPackets1_9.WINDOW_ITEMS, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.UNSIGNED_BYTE);
            this.handler((packetWrapper) -> {
               short windowId = (Short)packetWrapper.get(Type.UNSIGNED_BYTE, 0);
               Item[] items = (Item[])packetWrapper.read(Type.ITEM_ARRAY);

               for(int i = 0; i < items.length; ++i) {
                  items[i] = ItemRewriter.toClient(items[i]);
               }

               if (windowId == 0 && items.length == 46) {
                  Item[] old = items;
                  items = new Item[45];
                  System.arraycopy(old, 0, items, 0, 45);
               } else {
                  String type = ((Windows)packetWrapper.user().get(Windows.class)).get(windowId);
                  if (type != null && type.equalsIgnoreCase("minecraft:brewing_stand")) {
                     System.arraycopy(items, 0, ((Windows)packetWrapper.user().get(Windows.class)).getBrewingItems(windowId), 0, 4);
                     Windows.updateBrewingStand(packetWrapper.user(), items[4], windowId);
                     Item[] oldx = items;
                     items = new Item[items.length - 1];
                     System.arraycopy(oldx, 0, items, 0, 4);
                     System.arraycopy(oldx, 5, items, 4, oldx.length - 5);
                  }
               }

               packetWrapper.write(Type.ITEM_ARRAY, items);
            });
         }
      });
      protocol.registerClientbound(ClientboundPackets1_9.SET_SLOT, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.UNSIGNED_BYTE);
            this.map(Type.SHORT);
            this.map(Type.ITEM);
            this.handler((packetWrapper) -> {
               packetWrapper.set(Type.ITEM, 0, ItemRewriter.toClient((Item)packetWrapper.get(Type.ITEM, 0)));
               byte windowId = ((Short)packetWrapper.get(Type.UNSIGNED_BYTE, 0)).byteValue();
               short slot = (Short)packetWrapper.get(Type.SHORT, 0);
               if (windowId == 0 && slot == 45) {
                  packetWrapper.cancel();
               } else {
                  String type = ((Windows)packetWrapper.user().get(Windows.class)).get((short)windowId);
                  if (type != null) {
                     if (type.equalsIgnoreCase("minecraft:brewing_stand")) {
                        if (slot > 4) {
                           --slot;
                           packetWrapper.set(Type.SHORT, 0, slot);
                        } else {
                           if (slot == 4) {
                              packetWrapper.cancel();
                              Windows.updateBrewingStand(packetWrapper.user(), (Item)packetWrapper.get(Type.ITEM, 0), (short)windowId);
                              return;
                           }

                           ((Windows)packetWrapper.user().get(Windows.class)).getBrewingItems((short)windowId)[slot] = (Item)packetWrapper.get(Type.ITEM, 0);
                        }
                     }

                  }
               }
            });
         }
      });
      protocol.registerServerbound(ServerboundPackets1_8.CLOSE_WINDOW, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.UNSIGNED_BYTE);
            this.handler((packetWrapper) -> {
               short windowsId = (Short)packetWrapper.get(Type.UNSIGNED_BYTE, 0);
               ((Windows)packetWrapper.user().get(Windows.class)).remove(windowsId);
            });
         }
      });
      protocol.registerServerbound(ServerboundPackets1_8.CLICK_WINDOW, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.UNSIGNED_BYTE);
            this.map(Type.SHORT);
            this.map(Type.BYTE);
            this.map(Type.SHORT);
            this.map(Type.BYTE, Type.VAR_INT);
            this.map(Type.ITEM);
            this.handler((packetWrapper) -> {
               packetWrapper.set(Type.ITEM, 0, ItemRewriter.toServer((Item)packetWrapper.get(Type.ITEM, 0)));
            });
            this.handler((packetWrapper) -> {
               short windowId = (Short)packetWrapper.get(Type.UNSIGNED_BYTE, 0);
               Windows windows = (Windows)packetWrapper.user().get(Windows.class);
               String type = windows.get(windowId);
               if (type != null) {
                  if (type.equalsIgnoreCase("minecraft:brewing_stand")) {
                     short slot = (Short)packetWrapper.get(Type.SHORT, 0);
                     if (slot > 3) {
                        ++slot;
                        packetWrapper.set(Type.SHORT, 0, slot);
                     }
                  }

               }
            });
         }
      });
      protocol.registerServerbound(ServerboundPackets1_8.CREATIVE_INVENTORY_ACTION, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.SHORT);
            this.map(Type.ITEM);
            this.handler((packetWrapper) -> {
               packetWrapper.set(Type.ITEM, 0, ItemRewriter.toServer((Item)packetWrapper.get(Type.ITEM, 0)));
            });
         }
      });
   }
}
