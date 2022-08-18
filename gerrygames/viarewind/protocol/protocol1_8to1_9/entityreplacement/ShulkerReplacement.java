package de.gerrygames.viarewind.protocol.protocol1_8to1_9.entityreplacement;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.entities.Entity1_10Types.EntityType;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.minecraft.metadata.types.MetaType1_9;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.version.Types1_8;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.Protocol1_8TO1_9;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.metadata.MetadataRewriter;
import de.gerrygames.viarewind.replacement.EntityReplacement;
import de.gerrygames.viarewind.utils.PacketUtil;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ShulkerReplacement implements EntityReplacement {
   private int entityId;
   private List datawatcher = new ArrayList();
   private double locX;
   private double locY;
   private double locZ;
   private UserConnection user;

   public ShulkerReplacement(int entityId, UserConnection user) {
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
   }

   public void setHeadYaw(float yaw) {
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
      PacketWrapper teleport = PacketWrapper.create(24, (ByteBuf)null, this.user);
      teleport.write(Type.VAR_INT, this.entityId);
      teleport.write(Type.INT, (int)(this.locX * 32.0));
      teleport.write(Type.INT, (int)(this.locY * 32.0));
      teleport.write(Type.INT, (int)(this.locZ * 32.0));
      teleport.write(Type.BYTE, (byte)0);
      teleport.write(Type.BYTE, (byte)0);
      teleport.write(Type.BOOLEAN, true);
      PacketUtil.sendPacket(teleport, Protocol1_8TO1_9.class, true, true);
   }

   public void updateMetadata() {
      PacketWrapper metadataPacket = PacketWrapper.create(28, (ByteBuf)null, this.user);
      metadataPacket.write(Type.VAR_INT, this.entityId);
      List metadataList = new ArrayList();
      Iterator var3 = this.datawatcher.iterator();

      while(var3.hasNext()) {
         Metadata metadata = (Metadata)var3.next();
         if (metadata.id() != 11 && metadata.id() != 12 && metadata.id() != 13) {
            metadataList.add(new Metadata(metadata.id(), metadata.metaType(), metadata.getValue()));
         }
      }

      metadataList.add(new Metadata(11, MetaType1_9.VarInt, 2));
      MetadataRewriter.transform(EntityType.MAGMA_CUBE, metadataList);
      metadataPacket.write(Types1_8.METADATA_LIST, metadataList);
      PacketUtil.sendPacket(metadataPacket, Protocol1_8TO1_9.class);
   }

   public void spawn() {
      PacketWrapper spawn = PacketWrapper.create(15, (ByteBuf)null, this.user);
      spawn.write(Type.VAR_INT, this.entityId);
      spawn.write(Type.UNSIGNED_BYTE, Short.valueOf((short)62));
      spawn.write(Type.INT, 0);
      spawn.write(Type.INT, 0);
      spawn.write(Type.INT, 0);
      spawn.write(Type.BYTE, (byte)0);
      spawn.write(Type.BYTE, (byte)0);
      spawn.write(Type.BYTE, (byte)0);
      spawn.write(Type.SHORT, Short.valueOf((short)0));
      spawn.write(Type.SHORT, Short.valueOf((short)0));
      spawn.write(Type.SHORT, Short.valueOf((short)0));
      List list = new ArrayList();
      list.add(new Metadata(0, MetaType1_9.Byte, (byte)0));
      spawn.write(Types1_8.METADATA_LIST, list);
      PacketUtil.sendPacket(spawn, Protocol1_8TO1_9.class, true, true);
   }

   public void despawn() {
      PacketWrapper despawn = PacketWrapper.create(19, (ByteBuf)null, this.user);
      despawn.write(Type.VAR_INT_ARRAY_PRIMITIVE, new int[]{this.entityId});
      PacketUtil.sendPacket(despawn, Protocol1_8TO1_9.class, true, true);
   }

   public int getEntityId() {
      return this.entityId;
   }
}
