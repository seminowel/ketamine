package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.entityreplacements;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.entities.Entity1_10Types.EntityType;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.ClientboundPackets1_7;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.Protocol1_7_6_10TO1_8;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.metadata.MetadataRewriter;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.types.Types1_7_6_10;
import de.gerrygames.viarewind.replacement.EntityReplacement;
import de.gerrygames.viarewind.utils.PacketUtil;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RabbitReplacement implements EntityReplacement {
   private int entityId;
   private List datawatcher = new ArrayList();
   private double locX;
   private double locY;
   private double locZ;
   private float yaw;
   private float pitch;
   private float headYaw;
   private UserConnection user;

   public RabbitReplacement(int entityId, UserConnection user) {
      this.entityId = entityId;
      this.user = user;
      this.spawn();
   }

   public void setLocation(double x, double y, double z) {
      this.locX = x;
      this.locY = y;
      this.locZ = z;
      this.updateLocation();
   }

   public void relMove(double x, double y, double z) {
      this.locX += x;
      this.locY += y;
      this.locZ += z;
      this.updateLocation();
   }

   public void setYawPitch(float yaw, float pitch) {
      if (this.yaw != yaw || this.pitch != pitch) {
         this.yaw = yaw;
         this.pitch = pitch;
         this.updateLocation();
      }

   }

   public void setHeadYaw(float yaw) {
      if (this.headYaw != yaw) {
         this.headYaw = yaw;
         this.updateLocation();
      }

   }

   public void updateMetadata(List metadataList) {
      Iterator var2 = metadataList.iterator();

      while(var2.hasNext()) {
         Metadata metadata = (Metadata)var2.next();
         this.datawatcher.removeIf((m) -> {
            return m.id() == metadata.id();
         });
         this.datawatcher.add(metadata);
      }

      this.updateMetadata();
   }

   public void updateLocation() {
      PacketWrapper teleport = PacketWrapper.create(ClientboundPackets1_7.ENTITY_TELEPORT, (ByteBuf)null, this.user);
      teleport.write(Type.INT, this.entityId);
      teleport.write(Type.INT, (int)(this.locX * 32.0));
      teleport.write(Type.INT, (int)(this.locY * 32.0));
      teleport.write(Type.INT, (int)(this.locZ * 32.0));
      teleport.write(Type.BYTE, (byte)((int)(this.yaw / 360.0F * 256.0F)));
      teleport.write(Type.BYTE, (byte)((int)(this.pitch / 360.0F * 256.0F)));
      PacketWrapper head = PacketWrapper.create(ClientboundPackets1_7.ENTITY_HEAD_LOOK, (ByteBuf)null, this.user);
      head.write(Type.INT, this.entityId);
      head.write(Type.BYTE, (byte)((int)(this.headYaw / 360.0F * 256.0F)));
      PacketUtil.sendPacket(teleport, Protocol1_7_6_10TO1_8.class, true, true);
      PacketUtil.sendPacket(head, Protocol1_7_6_10TO1_8.class, true, true);
   }

   public void updateMetadata() {
      PacketWrapper metadataPacket = PacketWrapper.create(ClientboundPackets1_7.ENTITY_METADATA, (ByteBuf)null, this.user);
      metadataPacket.write(Type.INT, this.entityId);
      List metadataList = new ArrayList();
      Iterator var3 = this.datawatcher.iterator();

      while(var3.hasNext()) {
         Metadata metadata = (Metadata)var3.next();
         metadataList.add(new Metadata(metadata.id(), metadata.metaType(), metadata.getValue()));
      }

      MetadataRewriter.transform(EntityType.CHICKEN, metadataList);
      metadataPacket.write(Types1_7_6_10.METADATA_LIST, metadataList);
      PacketUtil.sendPacket(metadataPacket, Protocol1_7_6_10TO1_8.class, true, true);
   }

   public void spawn() {
      PacketWrapper spawn = PacketWrapper.create(ClientboundPackets1_7.SPAWN_MOB, (ByteBuf)null, this.user);
      spawn.write(Type.VAR_INT, this.entityId);
      spawn.write(Type.UNSIGNED_BYTE, Short.valueOf((short)93));
      spawn.write(Type.INT, 0);
      spawn.write(Type.INT, 0);
      spawn.write(Type.INT, 0);
      spawn.write(Type.BYTE, (byte)0);
      spawn.write(Type.BYTE, (byte)0);
      spawn.write(Type.BYTE, (byte)0);
      spawn.write(Type.SHORT, Short.valueOf((short)0));
      spawn.write(Type.SHORT, Short.valueOf((short)0));
      spawn.write(Type.SHORT, Short.valueOf((short)0));
      spawn.write(Types1_7_6_10.METADATA_LIST, new ArrayList());
      PacketUtil.sendPacket(spawn, Protocol1_7_6_10TO1_8.class, true, true);
   }

   public void despawn() {
      PacketWrapper despawn = PacketWrapper.create(ClientboundPackets1_7.DESTROY_ENTITIES, (ByteBuf)null, this.user);
      despawn.write(Types1_7_6_10.INT_ARRAY, new int[]{this.entityId});
      PacketUtil.sendPacket(despawn, Protocol1_7_6_10TO1_8.class, true, true);
   }

   public int getEntityId() {
      return this.entityId;
   }
}
