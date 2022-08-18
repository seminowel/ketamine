package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.types;

import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;

public class ItemArrayType extends Type {
   private final boolean compressed;

   public ItemArrayType(boolean compressed) {
      super(Item[].class);
      this.compressed = compressed;
   }

   public Item[] read(ByteBuf buffer) throws Exception {
      int amount = Type.SHORT.read(buffer);
      Item[] items = new Item[amount];

      for(int i = 0; i < amount; ++i) {
         items[i] = (Item)(this.compressed ? Types1_7_6_10.COMPRESSED_NBT_ITEM : Types1_7_6_10.ITEM).read(buffer);
      }

      return items;
   }

   public void write(ByteBuf buffer, Item[] items) throws Exception {
      Type.SHORT.write(buffer, (short)items.length);
      Item[] var3 = items;
      int var4 = items.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         Item item = var3[var5];
         (this.compressed ? Types1_7_6_10.COMPRESSED_NBT_ITEM : Types1_7_6_10.ITEM).write(buffer, item);
      }

   }
}
