package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.types;

import com.viaversion.viaversion.api.minecraft.Environment;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.type.PartialType;
import com.viaversion.viaversion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;
import io.netty.buffer.ByteBuf;
import java.util.zip.Deflater;

public class Chunk1_7_10Type extends PartialType {
   public Chunk1_7_10Type(ClientWorld param) {
      super(param, Chunk.class);
   }

   public Chunk read(ByteBuf byteBuf, ClientWorld clientWorld) throws Exception {
      throw new UnsupportedOperationException();
   }

   public void write(ByteBuf output, ClientWorld clientWorld, Chunk chunk) throws Exception {
      output.writeInt(chunk.getX());
      output.writeInt(chunk.getZ());
      output.writeBoolean(chunk.isFullChunk());
      output.writeShort(chunk.getBitmask());
      output.writeShort(0);
      ByteBuf dataToCompress = output.alloc().buffer();
      ByteBuf blockData = output.alloc().buffer();

      int i;
      int y;
      int z;
      int previousData;
      for(i = 0; i < chunk.getSections().length; ++i) {
         if ((chunk.getBitmask() & 1 << i) != 0) {
            ChunkSection section = chunk.getSections()[i];

            for(y = 0; y < 16; ++y) {
               for(z = 0; z < 16; ++z) {
                  previousData = 0;

                  for(int x = 0; x < 16; ++x) {
                     int block = section.getFlatBlock(x, y, z);
                     dataToCompress.writeByte(block >> 4);
                     int data = block & 15;
                     if (x % 2 == 0) {
                        previousData = data;
                     } else {
                        blockData.writeByte(data << 4 | previousData);
                     }
                  }
               }
            }
         }
      }

      dataToCompress.writeBytes(blockData);
      blockData.release();

      for(i = 0; i < chunk.getSections().length; ++i) {
         if ((chunk.getBitmask() & 1 << i) != 0) {
            chunk.getSections()[i].getLight().writeBlockLight(dataToCompress);
         }
      }

      boolean skyLight = clientWorld != null && clientWorld.getEnvironment() == Environment.NORMAL;
      if (skyLight) {
         for(int i = 0; i < chunk.getSections().length; ++i) {
            if ((chunk.getBitmask() & 1 << i) != 0) {
               chunk.getSections()[i].getLight().writeSkyLight(dataToCompress);
            }
         }
      }

      if (chunk.isFullChunk() && chunk.isBiomeData()) {
         int[] var19 = chunk.getBiomeData();
         y = var19.length;

         for(z = 0; z < y; ++z) {
            previousData = var19[z];
            dataToCompress.writeByte((byte)previousData);
         }
      }

      dataToCompress.readerIndex(0);
      byte[] data = new byte[dataToCompress.readableBytes()];
      dataToCompress.readBytes(data);
      dataToCompress.release();
      Deflater deflater = new Deflater(4);

      byte[] compressedData;
      try {
         deflater.setInput(data, 0, data.length);
         deflater.finish();
         compressedData = new byte[data.length];
         previousData = deflater.deflate(compressedData);
      } finally {
         deflater.end();
      }

      output.writeInt(previousData);
      output.writeBytes(compressedData, 0, previousData);
   }
}
