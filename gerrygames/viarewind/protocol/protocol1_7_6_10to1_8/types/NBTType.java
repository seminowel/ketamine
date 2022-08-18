package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.types;

import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.opennbt.NBTIO;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class NBTType extends Type {
   public NBTType() {
      super(CompoundTag.class);
   }

   public CompoundTag read(ByteBuf buffer) {
      short length = buffer.readShort();
      if (length < 0) {
         return null;
      } else {
         ByteBufInputStream byteBufInputStream = new ByteBufInputStream(buffer);
         DataInputStream dataInputStream = new DataInputStream(byteBufInputStream);

         try {
            CompoundTag var5 = NBTIO.readTag(dataInputStream);
            return var5;
         } catch (Throwable var15) {
            var15.printStackTrace();
         } finally {
            try {
               dataInputStream.close();
            } catch (IOException var14) {
               var14.printStackTrace();
            }

         }

         return null;
      }
   }

   public void write(ByteBuf buffer, CompoundTag nbt) throws Exception {
      if (nbt == null) {
         buffer.writeShort(-1);
      } else {
         ByteBuf buf = buffer.alloc().buffer();
         ByteBufOutputStream bytebufStream = new ByteBufOutputStream(buf);
         DataOutputStream dataOutputStream = new DataOutputStream(bytebufStream);
         NBTIO.writeTag(dataOutputStream, nbt);
         dataOutputStream.close();
         buffer.writeShort(buf.readableBytes());
         buffer.writeBytes(buf);
         buf.release();
      }

   }
}
