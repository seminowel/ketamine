package de.gerrygames.viarewind.types;

import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;

public class VarLongType extends Type {
   public static final VarLongType VAR_LONG = new VarLongType();

   public VarLongType() {
      super("VarLong", Long.class);
   }

   public Long read(ByteBuf byteBuf) throws Exception {
      long i = 0L;
      int j = 0;

      byte b0;
      do {
         b0 = byteBuf.readByte();
         i |= (long)((b0 & 127) << j++ * 7);
         if (j > 10) {
            throw new RuntimeException("VarLong too big");
         }
      } while((b0 & 128) == 128);

      return i;
   }

   public void write(ByteBuf byteBuf, Long i) throws Exception {
      while((i & -128L) != 0L) {
         byteBuf.writeByte((int)(i & 127L) | 128);
         i = i >>> 7;
      }

      byteBuf.writeByte(i.intValue());
   }
}
