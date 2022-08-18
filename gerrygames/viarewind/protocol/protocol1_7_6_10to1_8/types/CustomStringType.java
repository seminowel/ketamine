package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.types;

import com.viaversion.viaversion.api.type.PartialType;
import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;

public class CustomStringType extends PartialType {
   public CustomStringType(Integer param) {
      super(param, String[].class);
   }

   public String[] read(ByteBuf buffer, Integer size) throws Exception {
      if (buffer.readableBytes() < size / 4) {
         throw new RuntimeException("Readable bytes does not match expected!");
      } else {
         String[] array = new String[size];

         for(int i = 0; i < size; ++i) {
            array[i] = (String)Type.STRING.read(buffer);
         }

         return array;
      }
   }

   public void write(ByteBuf buffer, Integer size, String[] strings) throws Exception {
      String[] var4 = strings;
      int var5 = strings.length;

      for(int var6 = 0; var6 < var5; ++var6) {
         String s = var4[var6];
         Type.STRING.write(buffer, s);
      }

   }
}
