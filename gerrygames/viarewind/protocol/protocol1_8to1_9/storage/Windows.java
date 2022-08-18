package de.gerrygames.viarewind.protocol.protocol1_8to1_9.storage;

import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.item.DataItem;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.Protocol1_8TO1_9;
import de.gerrygames.viarewind.utils.PacketUtil;
import io.netty.buffer.ByteBuf;
import java.util.HashMap;

public class Windows extends StoredObject {
   private HashMap types = new HashMap();
   private HashMap brewingItems = new HashMap();

   public Windows(UserConnection user) {
      super(user);
   }

   public String get(short windowId) {
      return (String)this.types.get(windowId);
   }

   public void put(short windowId, String type) {
      this.types.put(windowId, type);
   }

   public void remove(short windowId) {
      this.types.remove(windowId);
      this.brewingItems.remove(windowId);
   }

   public Item[] getBrewingItems(short windowId) {
      return (Item[])this.brewingItems.computeIfAbsent(windowId, (key) -> {
         return new Item[]{new DataItem(), new DataItem(), new DataItem(), new DataItem()};
      });
   }

   public static void updateBrewingStand(UserConnection user, Item blazePowder, short windowId) {
      if (blazePowder == null || blazePowder.identifier() == 377) {
         int amount = blazePowder == null ? 0 : blazePowder.amount();
         PacketWrapper openWindow = PacketWrapper.create(45, (ByteBuf)null, user);
         openWindow.write(Type.UNSIGNED_BYTE, windowId);
         openWindow.write(Type.STRING, "minecraft:brewing_stand");
         openWindow.write(Type.STRING, "[{\"translate\":\"container.brewing\"},{\"text\":\": \",\"color\":\"dark_gray\"},{\"text\":\"§4" + amount + " \",\"color\":\"dark_red\"},{\"translate\":\"item.blazePowder.name\",\"color\":\"dark_red\"}]");
         openWindow.write(Type.UNSIGNED_BYTE, (short)420);
         PacketUtil.sendPacket(openWindow, Protocol1_8TO1_9.class);
         Item[] items = ((Windows)user.get(Windows.class)).getBrewingItems(windowId);

         for(int i = 0; i < items.length; ++i) {
            PacketWrapper setSlot = PacketWrapper.create(47, (ByteBuf)null, user);
            setSlot.write(Type.BYTE, (byte)windowId);
            setSlot.write(Type.SHORT, (short)i);
            setSlot.write(Type.ITEM, items[i]);
            PacketUtil.sendPacket(setSlot, Protocol1_8TO1_9.class);
         }

      }
   }
}
