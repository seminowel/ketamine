package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.packets;

import com.viaversion.viaversion.api.minecraft.BlockChangeRecord;
import com.viaversion.viaversion.api.minecraft.Position;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.CustomByteType;
import com.viaversion.viaversion.protocols.protocol1_8.ClientboundPackets1_8;
import com.viaversion.viaversion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.types.Chunk1_8Type;
import com.viaversion.viaversion.util.ChatColorUtil;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.Protocol1_7_6_10TO1_8;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.chunks.ChunkPacketTransformer;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.items.ReplacementRegistry1_7_6_10to1_8;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.storage.WorldBorder;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.types.Chunk1_7_10Type;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.types.Particle;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.types.Types1_7_6_10;
import de.gerrygames.viarewind.replacement.Replacement;
import de.gerrygames.viarewind.types.VarLongType;
import de.gerrygames.viarewind.utils.ChatUtil;
import de.gerrygames.viarewind.utils.PacketUtil;
import io.netty.buffer.ByteBuf;

public class WorldPackets {
   public static void register(Protocol1_7_6_10TO1_8 protocol) {
      protocol.registerClientbound(ClientboundPackets1_8.CHUNK_DATA, new PacketRemapper() {
         public void registerMap() {
            this.handler((packetWrapper) -> {
               ClientWorld world = (ClientWorld)packetWrapper.user().get(ClientWorld.class);
               Chunk chunk = (Chunk)packetWrapper.read(new Chunk1_8Type(world));
               packetWrapper.write(new Chunk1_7_10Type(world), chunk);
               ChunkSection[] var3 = chunk.getSections();
               int var4 = var3.length;

               for(int var5 = 0; var5 < var4; ++var5) {
                  ChunkSection section = var3[var5];
                  if (section != null) {
                     for(int i = 0; i < section.getPaletteSize(); ++i) {
                        int block = section.getPaletteEntry(i);
                        int replacedBlock = ReplacementRegistry1_7_6_10to1_8.replace(block);
                        section.setPaletteEntry(i, replacedBlock);
                     }
                  }
               }

            });
         }
      });
      protocol.registerClientbound(ClientboundPackets1_8.MULTI_BLOCK_CHANGE, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.INT);
            this.map(Type.INT);
            this.handler((packetWrapper) -> {
               BlockChangeRecord[] records = (BlockChangeRecord[])packetWrapper.read(Type.BLOCK_CHANGE_RECORD_ARRAY);
               packetWrapper.write(Type.SHORT, (short)records.length);
               packetWrapper.write(Type.INT, records.length * 4);
               BlockChangeRecord[] var2 = records;
               int var3 = records.length;

               for(int var4 = 0; var4 < var3; ++var4) {
                  BlockChangeRecord record = var2[var4];
                  short data = (short)(record.getSectionX() << 12 | record.getSectionZ() << 8 | record.getY());
                  packetWrapper.write(Type.SHORT, data);
                  int replacedBlock = ReplacementRegistry1_7_6_10to1_8.replace(record.getBlockId());
                  packetWrapper.write(Type.SHORT, (short)replacedBlock);
               }

            });
         }
      });
      protocol.registerClientbound(ClientboundPackets1_8.BLOCK_CHANGE, new PacketRemapper() {
         public void registerMap() {
            this.handler((packetWrapper) -> {
               Position position = (Position)packetWrapper.read(Type.POSITION);
               packetWrapper.write(Type.INT, position.getX());
               packetWrapper.write(Type.UNSIGNED_BYTE, (short)position.getY());
               packetWrapper.write(Type.INT, position.getZ());
            });
            this.handler((packetWrapper) -> {
               int data = (Integer)packetWrapper.read(Type.VAR_INT);
               int blockId = data >> 4;
               int meta = data & 15;
               Replacement replace = ReplacementRegistry1_7_6_10to1_8.getReplacement(blockId, meta);
               if (replace != null) {
                  blockId = replace.getId();
                  meta = replace.replaceData(meta);
               }

               packetWrapper.write(Type.VAR_INT, blockId);
               packetWrapper.write(Type.UNSIGNED_BYTE, (short)meta);
            });
         }
      });
      protocol.registerClientbound(ClientboundPackets1_8.BLOCK_ACTION, new PacketRemapper() {
         public void registerMap() {
            this.handler((packetWrapper) -> {
               Position position = (Position)packetWrapper.read(Type.POSITION);
               packetWrapper.write(Type.INT, position.getX());
               packetWrapper.write(Type.SHORT, (short)position.getY());
               packetWrapper.write(Type.INT, position.getZ());
            });
            this.map(Type.UNSIGNED_BYTE);
            this.map(Type.UNSIGNED_BYTE);
            this.map(Type.VAR_INT);
         }
      });
      protocol.registerClientbound(ClientboundPackets1_8.BLOCK_BREAK_ANIMATION, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.VAR_INT);
            this.handler((packetWrapper) -> {
               Position position = (Position)packetWrapper.read(Type.POSITION);
               packetWrapper.write(Type.INT, position.getX());
               packetWrapper.write(Type.INT, position.getY());
               packetWrapper.write(Type.INT, position.getZ());
            });
            this.map(Type.BYTE);
         }
      });
      protocol.registerClientbound(ClientboundPackets1_8.MAP_BULK_CHUNK, new PacketRemapper() {
         public void registerMap() {
            this.handler(ChunkPacketTransformer::transformChunkBulk);
         }
      });
      protocol.registerClientbound(ClientboundPackets1_8.EFFECT, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.INT);
            this.handler((packetWrapper) -> {
               Position position = (Position)packetWrapper.read(Type.POSITION);
               packetWrapper.write(Type.INT, position.getX());
               packetWrapper.write(Type.BYTE, (byte)position.getY());
               packetWrapper.write(Type.INT, position.getZ());
            });
            this.map(Type.INT);
            this.map(Type.BOOLEAN);
         }
      });
      protocol.registerClientbound(ClientboundPackets1_8.SPAWN_PARTICLE, new PacketRemapper() {
         public void registerMap() {
            this.handler((packetWrapper) -> {
               int particleId = (Integer)packetWrapper.read(Type.INT);
               Particle particle = Particle.find(particleId);
               if (particle == null) {
                  particle = Particle.CRIT;
               }

               packetWrapper.write(Type.STRING, particle.name);
               packetWrapper.read(Type.BOOLEAN);
            });
            this.map(Type.FLOAT);
            this.map(Type.FLOAT);
            this.map(Type.FLOAT);
            this.map(Type.FLOAT);
            this.map(Type.FLOAT);
            this.map(Type.FLOAT);
            this.map(Type.FLOAT);
            this.map(Type.INT);
            this.handler((packetWrapper) -> {
               String name = (String)packetWrapper.get(Type.STRING, 0);
               Particle particle = Particle.find(name);
               if (particle == Particle.ICON_CRACK || particle == Particle.BLOCK_CRACK || particle == Particle.BLOCK_DUST) {
                  int id = (Integer)packetWrapper.read(Type.VAR_INT);
                  int data = particle == Particle.ICON_CRACK ? (Integer)packetWrapper.read(Type.VAR_INT) : 0;
                  if (id >= 256 && id <= 422 || id >= 2256 && id <= 2267) {
                     particle = Particle.ICON_CRACK;
                  } else {
                     if ((id < 0 || id > 164) && (id < 170 || id > 175)) {
                        packetWrapper.cancel();
                        return;
                     }

                     if (particle == Particle.ICON_CRACK) {
                        particle = Particle.BLOCK_CRACK;
                     }
                  }

                  name = particle.name + "_" + id + "_" + data;
               }

               packetWrapper.set(Type.STRING, 0, name);
            });
         }
      });
      protocol.registerClientbound(ClientboundPackets1_8.UPDATE_SIGN, new PacketRemapper() {
         public void registerMap() {
            this.handler((packetWrapper) -> {
               Position position = (Position)packetWrapper.read(Type.POSITION);
               packetWrapper.write(Type.INT, position.getX());
               packetWrapper.write(Type.SHORT, (short)position.getY());
               packetWrapper.write(Type.INT, position.getZ());
            });
            this.handler((packetWrapper) -> {
               for(int i = 0; i < 4; ++i) {
                  String line = (String)packetWrapper.read(Type.STRING);
                  line = ChatUtil.jsonToLegacy(line);
                  line = ChatUtil.removeUnusedColor(line, '0');
                  if (line.length() > 15) {
                     line = ChatColorUtil.stripColor(line);
                     if (line.length() > 15) {
                        line = line.substring(0, 15);
                     }
                  }

                  packetWrapper.write(Type.STRING, line);
               }

            });
         }
      });
      protocol.registerClientbound(ClientboundPackets1_8.MAP_DATA, new PacketRemapper() {
         public void registerMap() {
            this.handler((packetWrapper) -> {
               packetWrapper.cancel();
               int id = (Integer)packetWrapper.read(Type.VAR_INT);
               byte scale = (Byte)packetWrapper.read(Type.BYTE);
               int count = (Integer)packetWrapper.read(Type.VAR_INT);
               byte[] icons = new byte[count * 4];

               short rows;
               for(int ix = 0; ix < count; ++ix) {
                  rows = (Byte)packetWrapper.read(Type.BYTE);
                  icons[ix * 4] = (byte)(rows >> 4 & 15);
                  icons[ix * 4 + 1] = (Byte)packetWrapper.read(Type.BYTE);
                  icons[ix * 4 + 2] = (Byte)packetWrapper.read(Type.BYTE);
                  icons[ix * 4 + 3] = (byte)(rows & 15);
               }

               short columns = (Short)packetWrapper.read(Type.UNSIGNED_BYTE);
               int ixx;
               if (columns > 0) {
                  rows = (Short)packetWrapper.read(Type.UNSIGNED_BYTE);
                  ixx = (Short)packetWrapper.read(Type.UNSIGNED_BYTE);
                  short z = (Short)packetWrapper.read(Type.UNSIGNED_BYTE);
                  byte[] data = (byte[])packetWrapper.read(Type.BYTE_ARRAY_PRIMITIVE);

                  for(int column = 0; column < columns; ++column) {
                     byte[] columnData = new byte[rows + 3];
                     columnData[0] = 0;
                     columnData[1] = (byte)(ixx + column);
                     columnData[2] = (byte)z;

                     for(int i = 0; i < rows; ++i) {
                        columnData[i + 3] = data[column + i * columns];
                     }

                     PacketWrapper columnUpdate = PacketWrapper.create(52, (ByteBuf)null, packetWrapper.user());
                     columnUpdate.write(Type.VAR_INT, id);
                     columnUpdate.write(Type.SHORT, (short)columnData.length);
                     columnUpdate.write(new CustomByteType(columnData.length), columnData);
                     PacketUtil.sendPacket(columnUpdate, Protocol1_7_6_10TO1_8.class, true, true);
                  }
               }

               if (count > 0) {
                  byte[] iconData = new byte[count * 3 + 1];
                  iconData[0] = 1;

                  for(ixx = 0; ixx < count; ++ixx) {
                     iconData[ixx * 3 + 1] = (byte)(icons[ixx * 4] << 4 | icons[ixx * 4 + 3] & 15);
                     iconData[ixx * 3 + 2] = icons[ixx * 4 + 1];
                     iconData[ixx * 3 + 3] = icons[ixx * 4 + 2];
                  }

                  PacketWrapper iconUpdate = PacketWrapper.create(52, (ByteBuf)null, packetWrapper.user());
                  iconUpdate.write(Type.VAR_INT, id);
                  iconUpdate.write(Type.SHORT, (short)iconData.length);
                  CustomByteType customByteType = new CustomByteType(iconData.length);
                  iconUpdate.write(customByteType, iconData);
                  PacketUtil.sendPacket(iconUpdate, Protocol1_7_6_10TO1_8.class, true, true);
               }

               PacketWrapper scaleUpdate = PacketWrapper.create(52, (ByteBuf)null, packetWrapper.user());
               scaleUpdate.write(Type.VAR_INT, id);
               scaleUpdate.write(Type.SHORT, Short.valueOf((short)2));
               scaleUpdate.write(new CustomByteType(2), new byte[]{2, scale});
               PacketUtil.sendPacket(scaleUpdate, Protocol1_7_6_10TO1_8.class, true, true);
            });
         }
      });
      protocol.registerClientbound(ClientboundPackets1_8.BLOCK_ENTITY_DATA, new PacketRemapper() {
         public void registerMap() {
            this.handler((packetWrapper) -> {
               Position position = (Position)packetWrapper.read(Type.POSITION);
               packetWrapper.write(Type.INT, position.getX());
               packetWrapper.write(Type.SHORT, (short)position.getY());
               packetWrapper.write(Type.INT, position.getZ());
            });
            this.map(Type.UNSIGNED_BYTE);
            this.map(Type.NBT, Types1_7_6_10.COMPRESSED_NBT);
         }
      });
      protocol.cancelClientbound(ClientboundPackets1_8.SERVER_DIFFICULTY);
      protocol.cancelClientbound(ClientboundPackets1_8.COMBAT_EVENT);
      protocol.registerClientbound(ClientboundPackets1_8.WORLD_BORDER, (ClientboundPacketType)null, new PacketRemapper() {
         public void registerMap() {
            this.handler((packetWrapper) -> {
               int action = (Integer)packetWrapper.read(Type.VAR_INT);
               WorldBorder worldBorder = (WorldBorder)packetWrapper.user().get(WorldBorder.class);
               if (action == 0) {
                  worldBorder.setSize((Double)packetWrapper.read(Type.DOUBLE));
               } else if (action == 1) {
                  worldBorder.lerpSize((Double)packetWrapper.read(Type.DOUBLE), (Double)packetWrapper.read(Type.DOUBLE), (Long)packetWrapper.read(VarLongType.VAR_LONG));
               } else if (action == 2) {
                  worldBorder.setCenter((Double)packetWrapper.read(Type.DOUBLE), (Double)packetWrapper.read(Type.DOUBLE));
               } else if (action == 3) {
                  worldBorder.init((Double)packetWrapper.read(Type.DOUBLE), (Double)packetWrapper.read(Type.DOUBLE), (Double)packetWrapper.read(Type.DOUBLE), (Double)packetWrapper.read(Type.DOUBLE), (Long)packetWrapper.read(VarLongType.VAR_LONG), (Integer)packetWrapper.read(Type.VAR_INT), (Integer)packetWrapper.read(Type.VAR_INT), (Integer)packetWrapper.read(Type.VAR_INT));
               } else if (action == 4) {
                  worldBorder.setWarningTime((Integer)packetWrapper.read(Type.VAR_INT));
               } else if (action == 5) {
                  worldBorder.setWarningBlocks((Integer)packetWrapper.read(Type.VAR_INT));
               }

               packetWrapper.cancel();
            });
         }
      });
   }
}
