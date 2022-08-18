package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.types;

import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;

public class IntArrayType extends Type {
   public IntArrayType() {
      super(int[].class);
   }

   public int[] read(ByteBuf byteBuf) throws Exception {
      byte size = byteBuf.readByte();
      int[] array = new int[size];

      for(byte i = 0; i < size; ++i) {
         array[i] = byteBuf.readInt();
      }

      return array;
   }

   public void write(ByteBuf byteBuf, int[] array) throws Exception {
      byteBuf.writeByte(array.length);
      int[] var3 = array;
      int var4 = array.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         int i = var3[var5];
         byteBuf.writeInt(i);
      }

   }
}
