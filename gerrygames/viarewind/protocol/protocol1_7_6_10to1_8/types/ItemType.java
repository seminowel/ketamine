package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.types;

import com.viaversion.viaversion.api.minecraft.item.DataItem;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import io.netty.buffer.ByteBuf;

public class ItemType extends Type {
   private final boolean compressed;

   public ItemType(boolean compressed) {
      super(Item.class);
      this.compressed = compressed;
   }

   public Item read(ByteBuf buffer) throws Exception {
      int readerIndex = buffer.readerIndex();
      short id = buffer.readShort();
      if (id < 0) {
         return null;
      } else {
         Item item = new DataItem();
         item.setIdentifier(id);
         item.setAmount(buffer.readByte());
         item.setData(buffer.readShort());
         item.setTag((CompoundTag)(this.compressed ? Types1_7_6_10.COMPRESSED_NBT : Types1_7_6_10.NBT).read(buffer));
         return item;
      }
   }

   public void write(ByteBuf buffer, Item item) throws Exception {
      if (item == null) {
         buffer.writeShort(-1);
      } else {
         buffer.writeShort(item.identifier());
         buffer.writeByte(item.amount());
         buffer.writeShort(item.data());
         (this.compressed ? Types1_7_6_10.COMPRESSED_NBT : Types1_7_6_10.NBT).write(buffer, item.tag());
      }

   }
}
