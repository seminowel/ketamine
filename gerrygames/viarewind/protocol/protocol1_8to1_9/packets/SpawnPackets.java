package de.gerrygames.viarewind.protocol.protocol1_8to1_9.packets;

import com.viaversion.viaversion.api.minecraft.entities.Entity1_10Types;
import com.viaversion.viaversion.api.minecraft.entities.Entity1_10Types.EntityType;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.version.Types1_8;
import com.viaversion.viaversion.api.type.types.version.Types1_9;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ClientboundPackets1_9;
import de.gerrygames.viarewind.ViaRewind;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.Protocol1_8TO1_9;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.entityreplacement.ShulkerBulletReplacement;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.entityreplacement.ShulkerReplacement;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.items.ReplacementRegistry1_8to1_9;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.metadata.MetadataRewriter;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.storage.EntityTracker;
import de.gerrygames.viarewind.replacement.EntityReplacement;
import de.gerrygames.viarewind.replacement.Replacement;
import de.gerrygames.viarewind.utils.PacketUtil;
import io.netty.buffer.ByteBuf;
import java.util.List;

public class SpawnPackets {
   public static void register(Protocol protocol) {
      protocol.registerClientbound(ClientboundPackets1_9.SPAWN_ENTITY, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.VAR_INT);
            this.map(Type.UUID, Type.NOTHING);
            this.map(Type.BYTE);
            this.map(Type.DOUBLE, Protocol1_8TO1_9.TO_OLD_INT);
            this.map(Type.DOUBLE, Protocol1_8TO1_9.TO_OLD_INT);
            this.map(Type.DOUBLE, Protocol1_8TO1_9.TO_OLD_INT);
            this.map(Type.BYTE);
            this.map(Type.BYTE);
            this.map(Type.INT);
            this.handler((packetWrapper) -> {
               int entityId = (Integer)packetWrapper.get(Type.VAR_INT, 0);
               int typeId = (Byte)packetWrapper.get(Type.BYTE, 0);
               EntityTracker tracker = (EntityTracker)packetWrapper.user().get(EntityTracker.class);
               Entity1_10Types.EntityType type = Entity1_10Types.getTypeFromId(typeId, true);
               if (typeId != 3 && typeId != 91 && typeId != 92 && typeId != 93) {
                  if (type == null) {
                     ViaRewind.getPlatform().getLogger().warning("[ViaRewind] Unhandled Spawn Object Type: " + typeId);
                     packetWrapper.cancel();
                  } else {
                     int x = (Integer)packetWrapper.get(Type.INT, 0);
                     int y = (Integer)packetWrapper.get(Type.INT, 1);
                     int z = (Integer)packetWrapper.get(Type.INT, 2);
                     int data;
                     if (type.is(EntityType.BOAT)) {
                        data = (Byte)packetWrapper.get(Type.BYTE, 1);
                        data = (byte)(data - 64);
                        packetWrapper.set(Type.BYTE, 1, Byte.valueOf((byte)data));
                        y += 10;
                        packetWrapper.set(Type.INT, 1, y);
                     } else if (type.is(EntityType.SHULKER_BULLET)) {
                        packetWrapper.cancel();
                        ShulkerBulletReplacement shulkerBulletReplacement = new ShulkerBulletReplacement(entityId, packetWrapper.user());
                        shulkerBulletReplacement.setLocation((double)x / 32.0, (double)y / 32.0, (double)z / 32.0);
                        tracker.addEntityReplacement(shulkerBulletReplacement);
                        return;
                     }

                     data = (Integer)packetWrapper.get(Type.INT, 3);
                     if (type.isOrHasParent(EntityType.ARROW) && data != 0) {
                        --data;
                        packetWrapper.set(Type.INT, 3, data);
                     }

                     if (type.is(EntityType.FALLING_BLOCK)) {
                        int blockId = data & 4095;
                        int blockData = data >> 12 & 15;
                        Replacement replace = ReplacementRegistry1_8to1_9.getReplacement(blockId, blockData);
                        if (replace != null) {
                           packetWrapper.set(Type.INT, 3, replace.getId() | replace.replaceData(data) << 12);
                        }
                     }

                     if (data > 0) {
                        packetWrapper.passthrough(Type.SHORT);
                        packetWrapper.passthrough(Type.SHORT);
                        packetWrapper.passthrough(Type.SHORT);
                     } else {
                        short vX = (Short)packetWrapper.read(Type.SHORT);
                        short vY = (Short)packetWrapper.read(Type.SHORT);
                        short vZ = (Short)packetWrapper.read(Type.SHORT);
                        PacketWrapper velocityPacket = PacketWrapper.create(18, (ByteBuf)null, packetWrapper.user());
                        velocityPacket.write(Type.VAR_INT, entityId);
                        velocityPacket.write(Type.SHORT, vX);
                        velocityPacket.write(Type.SHORT, vY);
                        velocityPacket.write(Type.SHORT, vZ);
                        PacketUtil.sendPacket(velocityPacket, Protocol1_8TO1_9.class);
                     }

                     tracker.getClientEntityTypes().put(entityId, type);
                     tracker.sendMetadataBuffer(entityId);
                  }
               } else {
                  packetWrapper.cancel();
               }
            });
         }
      });
      protocol.registerClientbound(ClientboundPackets1_9.SPAWN_EXPERIENCE_ORB, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.VAR_INT);
            this.map(Type.DOUBLE, Protocol1_8TO1_9.TO_OLD_INT);
            this.map(Type.DOUBLE, Protocol1_8TO1_9.TO_OLD_INT);
            this.map(Type.DOUBLE, Protocol1_8TO1_9.TO_OLD_INT);
            this.map(Type.SHORT);
            this.handler((packetWrapper) -> {
               int entityId = (Integer)packetWrapper.get(Type.VAR_INT, 0);
               EntityTracker tracker = (EntityTracker)packetWrapper.user().get(EntityTracker.class);
               tracker.getClientEntityTypes().put(entityId, EntityType.EXPERIENCE_ORB);
               tracker.sendMetadataBuffer(entityId);
            });
         }
      });
      protocol.registerClientbound(ClientboundPackets1_9.SPAWN_GLOBAL_ENTITY, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.VAR_INT);
            this.map(Type.BYTE);
            this.map(Type.DOUBLE, Protocol1_8TO1_9.TO_OLD_INT);
            this.map(Type.DOUBLE, Protocol1_8TO1_9.TO_OLD_INT);
            this.map(Type.DOUBLE, Protocol1_8TO1_9.TO_OLD_INT);
            this.handler((packetWrapper) -> {
               int entityId = (Integer)packetWrapper.get(Type.VAR_INT, 0);
               EntityTracker tracker = (EntityTracker)packetWrapper.user().get(EntityTracker.class);
               tracker.getClientEntityTypes().put(entityId, EntityType.LIGHTNING);
               tracker.sendMetadataBuffer(entityId);
            });
         }
      });
      protocol.registerClientbound(ClientboundPackets1_9.SPAWN_MOB, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.VAR_INT);
            this.map(Type.UUID, Type.NOTHING);
            this.map(Type.UNSIGNED_BYTE);
            this.map(Type.DOUBLE, Protocol1_8TO1_9.TO_OLD_INT);
            this.map(Type.DOUBLE, Protocol1_8TO1_9.TO_OLD_INT);
            this.map(Type.DOUBLE, Protocol1_8TO1_9.TO_OLD_INT);
            this.map(Type.BYTE);
            this.map(Type.BYTE);
            this.map(Type.BYTE);
            this.map(Type.SHORT);
            this.map(Type.SHORT);
            this.map(Type.SHORT);
            this.map(Types1_9.METADATA_LIST, Types1_8.METADATA_LIST);
            this.handler((packetWrapper) -> {
               int entityId = (Integer)packetWrapper.get(Type.VAR_INT, 0);
               int typeId = (Short)packetWrapper.get(Type.UNSIGNED_BYTE, 0);
               int x = (Integer)packetWrapper.get(Type.INT, 0);
               int y = (Integer)packetWrapper.get(Type.INT, 1);
               int z = (Integer)packetWrapper.get(Type.INT, 2);
               byte pitch = (Byte)packetWrapper.get(Type.BYTE, 1);
               byte yaw = (Byte)packetWrapper.get(Type.BYTE, 0);
               byte headYaw = (Byte)packetWrapper.get(Type.BYTE, 2);
               if (typeId == 69) {
                  packetWrapper.cancel();
                  EntityTracker tracker = (EntityTracker)packetWrapper.user().get(EntityTracker.class);
                  ShulkerReplacement shulkerReplacement = new ShulkerReplacement(entityId, packetWrapper.user());
                  shulkerReplacement.setLocation((double)x / 32.0, (double)y / 32.0, (double)z / 32.0);
                  shulkerReplacement.setYawPitch((float)yaw * 360.0F / 256.0F, (float)pitch * 360.0F / 256.0F);
                  shulkerReplacement.setHeadYaw((float)headYaw * 360.0F / 256.0F);
                  tracker.addEntityReplacement(shulkerReplacement);
               } else if (typeId == -1 || typeId == 255) {
                  packetWrapper.cancel();
               }

            });
            this.handler((packetWrapper) -> {
               int entityId = (Integer)packetWrapper.get(Type.VAR_INT, 0);
               int typeId = (Short)packetWrapper.get(Type.UNSIGNED_BYTE, 0);
               EntityTracker tracker = (EntityTracker)packetWrapper.user().get(EntityTracker.class);
               tracker.getClientEntityTypes().put(entityId, Entity1_10Types.getTypeFromId(typeId, false));
               tracker.sendMetadataBuffer(entityId);
            });
            this.handler((wrapper) -> {
               List metadataList = (List)wrapper.get(Types1_8.METADATA_LIST, 0);
               int entityId = (Integer)wrapper.get(Type.VAR_INT, 0);
               EntityTracker tracker = (EntityTracker)wrapper.user().get(EntityTracker.class);
               EntityReplacement replacement;
               if ((replacement = tracker.getEntityReplacement(entityId)) != null) {
                  replacement.updateMetadata(metadataList);
               } else if (tracker.getClientEntityTypes().containsKey(entityId)) {
                  MetadataRewriter.transform((Entity1_10Types.EntityType)tracker.getClientEntityTypes().get(entityId), metadataList);
               } else {
                  wrapper.cancel();
               }

            });
         }
      });
      protocol.registerClientbound(ClientboundPackets1_9.SPAWN_PAINTING, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.VAR_INT);
            this.map(Type.UUID, Type.NOTHING);
            this.map(Type.STRING);
            this.map(Type.POSITION);
            this.map(Type.BYTE, Type.UNSIGNED_BYTE);
            this.handler((packetWrapper) -> {
               int entityId = (Integer)packetWrapper.get(Type.VAR_INT, 0);
               EntityTracker tracker = (EntityTracker)packetWrapper.user().get(EntityTracker.class);
               tracker.getClientEntityTypes().put(entityId, EntityType.PAINTING);
               tracker.sendMetadataBuffer(entityId);
            });
         }
      });
      protocol.registerClientbound(ClientboundPackets1_9.SPAWN_PLAYER, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.VAR_INT);
            this.map(Type.UUID);
            this.map(Type.DOUBLE, Protocol1_8TO1_9.TO_OLD_INT);
            this.map(Type.DOUBLE, Protocol1_8TO1_9.TO_OLD_INT);
            this.map(Type.DOUBLE, Protocol1_8TO1_9.TO_OLD_INT);
            this.map(Type.BYTE);
            this.map(Type.BYTE);
            this.handler((packetWrapper) -> {
               packetWrapper.write(Type.SHORT, Short.valueOf((short)0));
            });
            this.map(Types1_9.METADATA_LIST, Types1_8.METADATA_LIST);
            this.handler((wrapper) -> {
               List metadataList = (List)wrapper.get(Types1_8.METADATA_LIST, 0);
               MetadataRewriter.transform(EntityType.PLAYER, metadataList);
            });
            this.handler((packetWrapper) -> {
               int entityId = (Integer)packetWrapper.get(Type.VAR_INT, 0);
               EntityTracker tracker = (EntityTracker)packetWrapper.user().get(EntityTracker.class);
               tracker.getClientEntityTypes().put(entityId, EntityType.PLAYER);
               tracker.sendMetadataBuffer(entityId);
            });
         }
      });
   }
}
