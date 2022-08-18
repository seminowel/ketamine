package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.packets;

import com.viaversion.viaversion.api.minecraft.Position;
import com.viaversion.viaversion.api.minecraft.entities.Entity1_10Types;
import com.viaversion.viaversion.api.minecraft.entities.Entity1_10Types.EntityType;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.version.Types1_8;
import com.viaversion.viaversion.protocols.protocol1_8.ClientboundPackets1_8;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.Protocol1_7_6_10TO1_8;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.items.ItemRewriter;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.metadata.MetadataRewriter;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.storage.EntityTracker;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.storage.GameProfileStorage;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.types.Types1_7_6_10;
import de.gerrygames.viarewind.replacement.EntityReplacement;
import de.gerrygames.viarewind.utils.PacketUtil;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.UUID;

public class EntityPackets {
   public static void register(Protocol1_7_6_10TO1_8 protocol) {
      protocol.registerClientbound(ClientboundPackets1_8.ENTITY_EQUIPMENT, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.VAR_INT, Type.INT);
            this.map(Type.SHORT);
            this.map(Type.ITEM, Types1_7_6_10.COMPRESSED_NBT_ITEM);
            this.handler((packetWrapper) -> {
               Item item = (Item)packetWrapper.get(Types1_7_6_10.COMPRESSED_NBT_ITEM, 0);
               ItemRewriter.toClient(item);
               packetWrapper.set(Types1_7_6_10.COMPRESSED_NBT_ITEM, 0, item);
            });
            this.handler((packetWrapper) -> {
               if ((Short)packetWrapper.get(Type.SHORT, 0) > 4) {
                  packetWrapper.cancel();
               }

            });
            this.handler((packetWrapper) -> {
               if (!packetWrapper.isCancelled()) {
                  EntityTracker tracker = (EntityTracker)packetWrapper.user().get(EntityTracker.class);
                  UUID uuid = tracker.getPlayerUUID((Integer)packetWrapper.get(Type.INT, 0));
                  if (uuid != null) {
                     Item[] equipment = tracker.getPlayerEquipment(uuid);
                     if (equipment == null) {
                        tracker.setPlayerEquipment(uuid, equipment = new Item[5]);
                     }

                     equipment[(Short)packetWrapper.get(Type.SHORT, 0)] = (Item)packetWrapper.get(Types1_7_6_10.COMPRESSED_NBT_ITEM, 0);
                     GameProfileStorage storage = (GameProfileStorage)packetWrapper.user().get(GameProfileStorage.class);
                     GameProfileStorage.GameProfile profile = storage.get(uuid);
                     if (profile != null && profile.gamemode == 3) {
                        packetWrapper.cancel();
                     }

                  }
               }
            });
         }
      });
      protocol.registerClientbound(ClientboundPackets1_8.USE_BED, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.VAR_INT, Type.INT);
            this.handler((packetWrapper) -> {
               Position position = (Position)packetWrapper.read(Type.POSITION);
               packetWrapper.write(Type.INT, position.getX());
               packetWrapper.write(Type.UNSIGNED_BYTE, (short)position.getY());
               packetWrapper.write(Type.INT, position.getZ());
            });
         }
      });
      protocol.registerClientbound(ClientboundPackets1_8.COLLECT_ITEM, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.VAR_INT, Type.INT);
            this.map(Type.VAR_INT, Type.INT);
         }
      });
      protocol.registerClientbound(ClientboundPackets1_8.ENTITY_VELOCITY, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.VAR_INT, Type.INT);
            this.map(Type.SHORT);
            this.map(Type.SHORT);
            this.map(Type.SHORT);
         }
      });
      protocol.registerClientbound(ClientboundPackets1_8.DESTROY_ENTITIES, new PacketRemapper() {
         public void registerMap() {
            this.handler((packetWrapper) -> {
               int[] entityIds = (int[])packetWrapper.read(Type.VAR_INT_ARRAY_PRIMITIVE);
               EntityTracker tracker = (EntityTracker)packetWrapper.user().get(EntityTracker.class);
               int[] entityIds2 = entityIds;
               int var4 = entityIds.length;

               for(int var5 = 0; var5 < var4; ++var5) {
                  int entityId = entityIds2[var5];
                  tracker.removeEntity(entityId);
               }

               while(entityIds.length > 127) {
                  entityIds2 = new int[127];
                  System.arraycopy(entityIds, 0, entityIds2, 0, 127);
                  int[] temp = new int[entityIds.length - 127];
                  System.arraycopy(entityIds, 127, temp, 0, temp.length);
                  entityIds = temp;
                  PacketWrapper destroy = PacketWrapper.create(19, (ByteBuf)null, packetWrapper.user());
                  destroy.write(Types1_7_6_10.INT_ARRAY, entityIds2);
                  PacketUtil.sendPacket(destroy, Protocol1_7_6_10TO1_8.class);
               }

               packetWrapper.write(Types1_7_6_10.INT_ARRAY, entityIds);
            });
         }
      });
      protocol.registerClientbound(ClientboundPackets1_8.ENTITY_MOVEMENT, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.VAR_INT, Type.INT);
         }
      });
      protocol.registerClientbound(ClientboundPackets1_8.ENTITY_POSITION, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.VAR_INT, Type.INT);
            this.map(Type.BYTE);
            this.map(Type.BYTE);
            this.map(Type.BYTE);
            this.map(Type.BOOLEAN, Type.NOTHING);
            this.handler((packetWrapper) -> {
               int entityId = (Integer)packetWrapper.get(Type.INT, 0);
               EntityTracker tracker = (EntityTracker)packetWrapper.user().get(EntityTracker.class);
               EntityReplacement replacement = tracker.getEntityReplacement(entityId);
               if (replacement != null) {
                  packetWrapper.cancel();
                  int x = (Byte)packetWrapper.get(Type.BYTE, 0);
                  int y = (Byte)packetWrapper.get(Type.BYTE, 1);
                  int z = (Byte)packetWrapper.get(Type.BYTE, 2);
                  replacement.relMove((double)x / 32.0, (double)y / 32.0, (double)z / 32.0);
               }

            });
         }
      });
      protocol.registerClientbound(ClientboundPackets1_8.ENTITY_ROTATION, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.VAR_INT, Type.INT);
            this.map(Type.BYTE);
            this.map(Type.BYTE);
            this.map(Type.BOOLEAN, Type.NOTHING);
            this.handler((packetWrapper) -> {
               int entityId = (Integer)packetWrapper.get(Type.INT, 0);
               EntityTracker tracker = (EntityTracker)packetWrapper.user().get(EntityTracker.class);
               EntityReplacement replacement = tracker.getEntityReplacement(entityId);
               if (replacement != null) {
                  packetWrapper.cancel();
                  int yaw = (Byte)packetWrapper.get(Type.BYTE, 0);
                  int pitch = (Byte)packetWrapper.get(Type.BYTE, 1);
                  replacement.setYawPitch((float)yaw * 360.0F / 256.0F, (float)pitch * 360.0F / 256.0F);
               }

            });
         }
      });
      protocol.registerClientbound(ClientboundPackets1_8.ENTITY_POSITION_AND_ROTATION, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.VAR_INT, Type.INT);
            this.map(Type.BYTE);
            this.map(Type.BYTE);
            this.map(Type.BYTE);
            this.map(Type.BYTE);
            this.map(Type.BYTE);
            this.map(Type.BOOLEAN, Type.NOTHING);
            this.handler((packetWrapper) -> {
               int entityId = (Integer)packetWrapper.get(Type.INT, 0);
               EntityTracker tracker = (EntityTracker)packetWrapper.user().get(EntityTracker.class);
               EntityReplacement replacement = tracker.getEntityReplacement(entityId);
               if (replacement != null) {
                  packetWrapper.cancel();
                  int x = (Byte)packetWrapper.get(Type.BYTE, 0);
                  int y = (Byte)packetWrapper.get(Type.BYTE, 1);
                  int z = (Byte)packetWrapper.get(Type.BYTE, 2);
                  int yaw = (Byte)packetWrapper.get(Type.BYTE, 3);
                  int pitch = (Byte)packetWrapper.get(Type.BYTE, 4);
                  replacement.relMove((double)x / 32.0, (double)y / 32.0, (double)z / 32.0);
                  replacement.setYawPitch((float)yaw * 360.0F / 256.0F, (float)pitch * 360.0F / 256.0F);
               }

            });
         }
      });
      protocol.registerClientbound(ClientboundPackets1_8.ENTITY_TELEPORT, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.VAR_INT, Type.INT);
            this.map(Type.INT);
            this.map(Type.INT);
            this.map(Type.INT);
            this.map(Type.BYTE);
            this.map(Type.BYTE);
            this.map(Type.BOOLEAN, Type.NOTHING);
            this.handler((packetWrapper) -> {
               int entityId = (Integer)packetWrapper.get(Type.INT, 0);
               EntityTracker tracker = (EntityTracker)packetWrapper.user().get(EntityTracker.class);
               Entity1_10Types.EntityType type = (Entity1_10Types.EntityType)tracker.getClientEntityTypes().get(entityId);
               if (type == EntityType.MINECART_ABSTRACT) {
                  int y = (Integer)packetWrapper.get(Type.INT, 2);
                  y += 12;
                  packetWrapper.set(Type.INT, 2, y);
               }

            });
            this.handler((packetWrapper) -> {
               int entityId = (Integer)packetWrapper.get(Type.INT, 0);
               EntityTracker tracker = (EntityTracker)packetWrapper.user().get(EntityTracker.class);
               EntityReplacement replacement = tracker.getEntityReplacement(entityId);
               if (replacement != null) {
                  packetWrapper.cancel();
                  int x = (Integer)packetWrapper.get(Type.INT, 1);
                  int y = (Integer)packetWrapper.get(Type.INT, 2);
                  int z = (Integer)packetWrapper.get(Type.INT, 3);
                  int yaw = (Byte)packetWrapper.get(Type.BYTE, 0);
                  int pitch = (Byte)packetWrapper.get(Type.BYTE, 1);
                  replacement.setLocation((double)x / 32.0, (double)y / 32.0, (double)z / 32.0);
                  replacement.setYawPitch((float)yaw * 360.0F / 256.0F, (float)pitch * 360.0F / 256.0F);
               }

            });
         }
      });
      protocol.registerClientbound(ClientboundPackets1_8.ENTITY_HEAD_LOOK, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.VAR_INT, Type.INT);
            this.map(Type.BYTE);
            this.handler((packetWrapper) -> {
               int entityId = (Integer)packetWrapper.get(Type.INT, 0);
               EntityTracker tracker = (EntityTracker)packetWrapper.user().get(EntityTracker.class);
               EntityReplacement replacement = tracker.getEntityReplacement(entityId);
               if (replacement != null) {
                  packetWrapper.cancel();
                  int yaw = (Byte)packetWrapper.get(Type.BYTE, 0);
                  replacement.setHeadYaw((float)yaw * 360.0F / 256.0F);
               }

            });
         }
      });
      protocol.registerClientbound(ClientboundPackets1_8.ATTACH_ENTITY, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.INT);
            this.map(Type.INT);
            this.map(Type.BOOLEAN);
            this.handler((packetWrapper) -> {
               boolean leash = (Boolean)packetWrapper.get(Type.BOOLEAN, 0);
               if (!leash) {
                  int passenger = (Integer)packetWrapper.get(Type.INT, 0);
                  int vehicle = (Integer)packetWrapper.get(Type.INT, 1);
                  EntityTracker tracker = (EntityTracker)packetWrapper.user().get(EntityTracker.class);
                  tracker.setPassenger(vehicle, passenger);
               }
            });
         }
      });
      protocol.registerClientbound(ClientboundPackets1_8.ENTITY_METADATA, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.VAR_INT, Type.INT);
            this.map(Types1_8.METADATA_LIST, Types1_7_6_10.METADATA_LIST);
            this.handler((wrapper) -> {
               List metadataList = (List)wrapper.get(Types1_7_6_10.METADATA_LIST, 0);
               int entityId = (Integer)wrapper.get(Type.INT, 0);
               EntityTracker tracker = (EntityTracker)wrapper.user().get(EntityTracker.class);
               if (tracker.getClientEntityTypes().containsKey(entityId)) {
                  EntityReplacement replacement = tracker.getEntityReplacement(entityId);
                  if (replacement != null) {
                     wrapper.cancel();
                     replacement.updateMetadata(metadataList);
                  } else {
                     MetadataRewriter.transform((Entity1_10Types.EntityType)tracker.getClientEntityTypes().get(entityId), metadataList);
                     if (metadataList.isEmpty()) {
                        wrapper.cancel();
                     }
                  }
               } else {
                  tracker.addMetadataToBuffer(entityId, metadataList);
                  wrapper.cancel();
               }

            });
         }
      });
      protocol.registerClientbound(ClientboundPackets1_8.ENTITY_EFFECT, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.VAR_INT, Type.INT);
            this.map(Type.BYTE);
            this.map(Type.BYTE);
            this.map(Type.VAR_INT, Type.SHORT);
            this.map(Type.BYTE, Type.NOTHING);
         }
      });
      protocol.registerClientbound(ClientboundPackets1_8.REMOVE_ENTITY_EFFECT, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.VAR_INT, Type.INT);
            this.map(Type.BYTE);
         }
      });
      protocol.registerClientbound(ClientboundPackets1_8.ENTITY_PROPERTIES, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.VAR_INT, Type.INT);
            this.handler((packetWrapper) -> {
               int entityId = (Integer)packetWrapper.get(Type.INT, 0);
               EntityTracker tracker = (EntityTracker)packetWrapper.user().get(EntityTracker.class);
               if (tracker.getEntityReplacement(entityId) != null) {
                  packetWrapper.cancel();
               } else {
                  int amount = (Integer)packetWrapper.passthrough(Type.INT);

                  for(int i = 0; i < amount; ++i) {
                     packetWrapper.passthrough(Type.STRING);
                     packetWrapper.passthrough(Type.DOUBLE);
                     int modifierlength = (Integer)packetWrapper.read(Type.VAR_INT);
                     packetWrapper.write(Type.SHORT, (short)modifierlength);

                     for(int j = 0; j < modifierlength; ++j) {
                        packetWrapper.passthrough(Type.UUID);
                        packetWrapper.passthrough(Type.DOUBLE);
                        packetWrapper.passthrough(Type.BYTE);
                     }
                  }

               }
            });
         }
      });
      protocol.cancelClientbound(ClientboundPackets1_8.UPDATE_ENTITY_NBT);
   }
}
