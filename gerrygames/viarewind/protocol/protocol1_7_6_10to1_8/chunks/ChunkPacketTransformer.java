package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.chunks;

import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.CustomByteType;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.items.ReplacementRegistry1_7_6_10to1_8;
import de.gerrygames.viarewind.replacement.Replacement;
import de.gerrygames.viarewind.storage.BlockState;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.zip.Deflater;

public class ChunkPacketTransformer {
   private static byte[] transformChunkData(byte[] data, int primaryBitMask, boolean skyLight, boolean groundUp) {
      int dataSize = 0;
      ByteBuf buf = Unpooled.buffer();
      ByteBuf blockDataBuf = Unpooled.buffer();

      int columnCount;
      for(columnCount = 0; columnCount < 16; ++columnCount) {
         if ((primaryBitMask & 1 << columnCount) != 0) {
            byte tmp = 0;

            for(int j = 0; j < 4096; ++j) {
               short blockData = (short)((data[dataSize + 1] & 255) << 8 | data[dataSize] & 255);
               dataSize += 2;
               int id = BlockState.extractId(blockData);
               int meta = BlockState.extractData(blockData);
               Replacement replace = ReplacementRegistry1_7_6_10to1_8.getReplacement(id, meta);
               if (replace != null) {
                  id = replace.getId();
                  meta = replace.replaceData(meta);
               }

               buf.writeByte(id);
               if (j % 2 == 0) {
                  tmp = (byte)(meta & 15);
               } else {
                  blockDataBuf.writeByte(tmp | (meta & 15) << 4);
               }
            }
         }
      }

      buf.writeBytes(blockDataBuf);
      blockDataBuf.release();
      columnCount = Integer.bitCount(primaryBitMask);
      buf.writeBytes(data, dataSize, 2048 * columnCount);
      dataSize += 2048 * columnCount;
      if (skyLight) {
         buf.writeBytes(data, dataSize, 2048 * columnCount);
         dataSize += 2048 * columnCount;
      }

      if (groundUp && dataSize + 256 <= data.length) {
         buf.writeBytes(data, dataSize, 256);
         dataSize += 256;
      }

      data = new byte[buf.readableBytes()];
      buf.readBytes(data);
      buf.release();
      return data;
   }

   private static int calcSize(int i, boolean flag, boolean flag1) {
      int j = i * 2 * 16 * 16 * 16;
      int k = i * 16 * 16 * 16 / 2;
      int l = flag ? i * 16 * 16 * 16 / 2 : 0;
      int i1 = flag1 ? 256 : 0;
      return j + k + l + i1;
   }

   public static void transformChunkBulk(PacketWrapper packetWrapper) throws Exception {
      boolean skyLightSent = (Boolean)packetWrapper.read(Type.BOOLEAN);
      int columnCount = (Integer)packetWrapper.read(Type.VAR_INT);
      int[] chunkX = new int[columnCount];
      int[] chunkZ = new int[columnCount];
      int[] primaryBitMask = new int[columnCount];
      byte[][] data = new byte[columnCount][];

      int totalSize;
      for(totalSize = 0; totalSize < columnCount; ++totalSize) {
         chunkX[totalSize] = (Integer)packetWrapper.read(Type.INT);
         chunkZ[totalSize] = (Integer)packetWrapper.read(Type.INT);
         primaryBitMask[totalSize] = (Integer)packetWrapper.read(Type.UNSIGNED_SHORT);
      }

      totalSize = 0;

      int bufferLocation;
      for(int i = 0; i < columnCount; ++i) {
         bufferLocation = calcSize(Integer.bitCount(primaryBitMask[i]), skyLightSent, true);
         CustomByteType customByteType = new CustomByteType(bufferLocation);
         data[i] = transformChunkData((byte[])packetWrapper.read(customByteType), primaryBitMask[i], skyLightSent, true);
         totalSize += data[i].length;
      }

      packetWrapper.write(Type.SHORT, (short)columnCount);
      byte[] buildBuffer = new byte[totalSize];
      bufferLocation = 0;

      for(int i = 0; i < columnCount; ++i) {
         System.arraycopy(data[i], 0, buildBuffer, bufferLocation, data[i].length);
         bufferLocation += data[i].length;
      }

      Deflater deflater = new Deflater(4);
      deflater.reset();
      deflater.setInput(buildBuffer);
      deflater.finish();
      byte[] buffer = new byte[buildBuffer.length + 100];
      int compressedSize = deflater.deflate(buffer);
      byte[] finalBuffer = new byte[compressedSize];
      System.arraycopy(buffer, 0, finalBuffer, 0, compressedSize);
      packetWrapper.write(Type.INT, compressedSize);
      packetWrapper.write(Type.BOOLEAN, skyLightSent);
      CustomByteType customByteType = new CustomByteType(compressedSize);
      packetWrapper.write(customByteType, finalBuffer);

      for(int i = 0; i < columnCount; ++i) {
         packetWrapper.write(Type.INT, chunkX[i]);
         packetWrapper.write(Type.INT, chunkZ[i]);
         packetWrapper.write(Type.SHORT, (short)primaryBitMask[i]);
         packetWrapper.write(Type.SHORT, Short.valueOf((short)0));
      }

   }
}
