package de.gerrygames.viarewind.protocol.protocol1_8to1_9.packets;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.BlockChangeRecord;
import com.viaversion.viaversion.api.minecraft.Environment;
import com.viaversion.viaversion.api.minecraft.Position;
import com.viaversion.viaversion.api.minecraft.chunks.BaseChunk;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSectionImpl;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.StringTag;
import com.viaversion.viaversion.protocols.protocol1_8.ClientboundPackets1_8;
import com.viaversion.viaversion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;
import com.viaversion.viaversion.protocols.protocol1_9_3to1_9_1_2.types.Chunk1_9_1_2Type;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ClientboundPackets1_9;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.types.Chunk1_8Type;
import de.gerrygames.viarewind.ViaRewind;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.Protocol1_8TO1_9;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.items.ReplacementRegistry1_8to1_9;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.sound.Effect;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.sound.SoundRemapper;
import de.gerrygames.viarewind.utils.PacketUtil;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;

public class WorldPackets {
   public static void register(Protocol protocol) {
      protocol.registerClientbound(ClientboundPackets1_9.BLOCK_ENTITY_DATA, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.POSITION);
            this.map(Type.UNSIGNED_BYTE);
            this.map(Type.NBT);
            this.handler((packetWrapper) -> {
               CompoundTag tag = (CompoundTag)packetWrapper.get(Type.NBT, 0);
               if (tag != null && tag.contains("SpawnData")) {
                  String entity = (String)((CompoundTag)tag.get("SpawnData")).get("id").getValue();
                  tag.remove("SpawnData");
                  tag.put("entityId", new StringTag(entity));
               }

            });
         }
      });
      protocol.registerClientbound(ClientboundPackets1_9.BLOCK_ACTION, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.POSITION);
            this.map(Type.UNSIGNED_BYTE);
            this.map(Type.UNSIGNED_BYTE);
            this.map(Type.VAR_INT);
            this.handler((packetWrapper) -> {
               int block = (Integer)packetWrapper.get(Type.VAR_INT, 0);
               if (block >= 219 && block <= 234) {
                  int blockx = true;
                  packetWrapper.set(Type.VAR_INT, 0, 130);
               }

            });
         }
      });
      protocol.registerClientbound(ClientboundPackets1_9.BLOCK_CHANGE, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.POSITION);
            this.map(Type.VAR_INT);
            this.handler((packetWrapper) -> {
               int combined = (Integer)packetWrapper.get(Type.VAR_INT, 0);
               int replacedCombined = ReplacementRegistry1_8to1_9.replace(combined);
               packetWrapper.set(Type.VAR_INT, 0, replacedCombined);
            });
         }
      });
      protocol.registerClientbound(ClientboundPackets1_9.MULTI_BLOCK_CHANGE, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.INT);
            this.map(Type.INT);
            this.map(Type.BLOCK_CHANGE_RECORD_ARRAY);
            this.handler((packetWrapper) -> {
               BlockChangeRecord[] var1 = (BlockChangeRecord[])packetWrapper.get(Type.BLOCK_CHANGE_RECORD_ARRAY, 0);
               int var2 = var1.length;

               for(int var3 = 0; var3 < var2; ++var3) {
                  BlockChangeRecord record = var1[var3];
                  int replacedCombined = ReplacementRegistry1_8to1_9.replace(record.getBlockId());
                  record.setBlockId(replacedCombined);
               }

            });
         }
      });
      protocol.registerClientbound(ClientboundPackets1_9.NAMED_SOUND, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.STRING);
            this.handler((packetWrapper) -> {
               String name = (String)packetWrapper.get(Type.STRING, 0);
               name = SoundRemapper.getOldName(name);
               if (name == null) {
                  packetWrapper.cancel();
               } else {
                  packetWrapper.set(Type.STRING, 0, name);
               }

            });
            this.map(Type.VAR_INT, Type.NOTHING);
            this.map(Type.INT);
            this.map(Type.INT);
            this.map(Type.INT);
            this.map(Type.FLOAT);
            this.map(Type.UNSIGNED_BYTE);
         }
      });
      protocol.registerClientbound(ClientboundPackets1_9.EXPLOSION, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.FLOAT);
            this.map(Type.FLOAT);
            this.map(Type.FLOAT);
            this.map(Type.FLOAT);
            this.handler((packetWrapper) -> {
               int count = (Integer)packetWrapper.read(Type.INT);
               packetWrapper.write(Type.INT, count);

               for(int i = 0; i < count; ++i) {
                  packetWrapper.passthrough(Type.UNSIGNED_BYTE);
                  packetWrapper.passthrough(Type.UNSIGNED_BYTE);
                  packetWrapper.passthrough(Type.UNSIGNED_BYTE);
               }

            });
            this.map(Type.FLOAT);
            this.map(Type.FLOAT);
            this.map(Type.FLOAT);
         }
      });
      protocol.registerClientbound(ClientboundPackets1_9.UNLOAD_CHUNK, ClientboundPackets1_8.CHUNK_DATA, new PacketRemapper() {
         public void registerMap() {
            this.handler((packetWrapper) -> {
               int chunkX = (Integer)packetWrapper.read(Type.INT);
               int chunkZ = (Integer)packetWrapper.read(Type.INT);
               ClientWorld world = (ClientWorld)packetWrapper.user().get(ClientWorld.class);
               packetWrapper.write(new Chunk1_8Type(world), new BaseChunk(chunkX, chunkZ, true, false, 0, new ChunkSection[16], (int[])null, new ArrayList()));
            });
         }
      });
      protocol.registerClientbound(ClientboundPackets1_9.CHUNK_DATA, new PacketRemapper() {
         public void registerMap() {
            this.handler((packetWrapper) -> {
               ClientWorld world = (ClientWorld)packetWrapper.user().get(ClientWorld.class);
               Chunk chunk = (Chunk)packetWrapper.read(new Chunk1_9_1_2Type(world));
               ChunkSection[] var3 = ((Chunk)chunk).getSections();
               int var4 = var3.length;

               for(int var5 = 0; var5 < var4; ++var5) {
                  ChunkSection sectionx = var3[var5];
                  if (sectionx != null) {
                     for(int i = 0; i < sectionx.getPaletteSize(); ++i) {
                        int block = sectionx.getPaletteEntry(i);
                        int replacedBlock = ReplacementRegistry1_8to1_9.replace(block);
                        sectionx.setPaletteEntry(i, replacedBlock);
                     }
                  }
               }

               if (((Chunk)chunk).isFullChunk() && ((Chunk)chunk).getBitmask() == 0) {
                  boolean skylight = world.getEnvironment() == Environment.NORMAL;
                  ChunkSection[] sections = new ChunkSection[16];
                  ChunkSection section = new ChunkSectionImpl(true);
                  sections[0] = section;
                  section.addPaletteEntry(0);
                  if (skylight) {
                     section.getLight().setSkyLight(new byte[2048]);
                  }

                  chunk = new BaseChunk(((Chunk)chunk).getX(), ((Chunk)chunk).getZ(), true, false, 1, sections, ((Chunk)chunk).getBiomeData(), ((Chunk)chunk).getBlockEntities());
               }

               packetWrapper.write(new Chunk1_8Type(world), chunk);
               UserConnection user = packetWrapper.user();
               ((Chunk)chunk).getBlockEntities().forEach((nbt) -> {
                  if (nbt.contains("x") && nbt.contains("y") && nbt.contains("z") && nbt.contains("id")) {
                     Position position = new Position((Integer)nbt.get("x").getValue(), (short)(Integer)nbt.get("y").getValue(), (Integer)nbt.get("z").getValue());
                     byte action;
                     switch ((String)nbt.get("id").getValue()) {
                        case "minecraft:mob_spawner":
                           action = 1;
                           break;
                        case "minecraft:command_block":
                           action = 2;
                           break;
                        case "minecraft:beacon":
                           action = 3;
                           break;
                        case "minecraft:skull":
                           action = 4;
                           break;
                        case "minecraft:flower_pot":
                           action = 5;
                           break;
                        case "minecraft:banner":
                           action = 6;
                           break;
                        default:
                           return;
                     }

                     PacketWrapper updateTileEntity = PacketWrapper.create(9, (ByteBuf)null, user);
                     updateTileEntity.write(Type.POSITION, position);
                     updateTileEntity.write(Type.UNSIGNED_BYTE, Short.valueOf(action));
                     updateTileEntity.write(Type.NBT, nbt);
                     PacketUtil.sendPacket(updateTileEntity, Protocol1_8TO1_9.class, false, false);
                  }
               });
            });
         }
      });
      protocol.registerClientbound(ClientboundPackets1_9.EFFECT, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.INT);
            this.map(Type.POSITION);
            this.map(Type.INT);
            this.map(Type.BOOLEAN);
            this.handler((packetWrapper) -> {
               int id = (Integer)packetWrapper.get(Type.INT, 0);
               id = Effect.getOldId(id);
               if (id == -1) {
                  packetWrapper.cancel();
               } else {
                  packetWrapper.set(Type.INT, 0, id);
                  if (id == 2001) {
                     int replacedBlock = ReplacementRegistry1_8to1_9.replace((Integer)packetWrapper.get(Type.INT, 1));
                     packetWrapper.set(Type.INT, 1, replacedBlock);
                  }

               }
            });
         }
      });
      protocol.registerClientbound(ClientboundPackets1_9.SPAWN_PARTICLE, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.INT);
            this.handler((packetWrapper) -> {
               int type = (Integer)packetWrapper.get(Type.INT, 0);
               if (type > 41 && !ViaRewind.getConfig().isReplaceParticles()) {
                  packetWrapper.cancel();
               } else {
                  if (type == 42) {
                     packetWrapper.set(Type.INT, 0, 24);
                  } else if (type == 43) {
                     packetWrapper.set(Type.INT, 0, 3);
                  } else if (type == 44) {
                     packetWrapper.set(Type.INT, 0, 34);
                  } else if (type == 45) {
                     packetWrapper.set(Type.INT, 0, 1);
                  }

               }
            });
         }
      });
      protocol.registerClientbound(ClientboundPackets1_9.MAP_DATA, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.VAR_INT);
            this.map(Type.BYTE);
            this.map(Type.BOOLEAN, Type.NOTHING);
         }
      });
      protocol.registerClientbound(ClientboundPackets1_9.SOUND, ClientboundPackets1_8.NAMED_SOUND, new PacketRemapper() {
         public void registerMap() {
            this.handler((packetWrapper) -> {
               int soundId = (Integer)packetWrapper.read(Type.VAR_INT);
               String sound = SoundRemapper.oldNameFromId(soundId);
               if (sound == null) {
                  packetWrapper.cancel();
               } else {
                  packetWrapper.write(Type.STRING, sound);
               }

            });
            this.handler((packetWrapper) -> {
               packetWrapper.read(Type.VAR_INT);
            });
            this.map(Type.INT);
            this.map(Type.INT);
            this.map(Type.INT);
            this.map(Type.FLOAT);
            this.map(Type.UNSIGNED_BYTE);
         }
      });
   }
}
