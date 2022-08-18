package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.entityreplacements;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.entities.Entity1_10Types.EntityType;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.minecraft.metadata.types.MetaType1_8;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.ClientboundPackets1_7;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.Protocol1_7_6_10TO1_8;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.metadata.MetadataRewriter;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.types.MetaType1_7_6_10;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.types.Types1_7_6_10;
import de.gerrygames.viarewind.replacement.EntityReplacement;
import de.gerrygames.viarewind.utils.PacketUtil;
import de.gerrygames.viarewind.utils.math.AABB;
import de.gerrygames.viarewind.utils.math.Vector3d;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ArmorStandReplacement implements EntityReplacement {
   private int entityId;
   private List datawatcher = new ArrayList();
   private int[] entityIds = null;
   private double locX;
   private double locY;
   private double locZ;
   private State currentState = null;
   private boolean invisible = false;
   private boolean nameTagVisible = false;
   private String name = null;
   private UserConnection user;
   private float yaw;
   private float pitch;
   private float headYaw;
   private boolean small = false;
   private boolean marker = false;

   public int getEntityId() {
      return this.entityId;
   }

   public ArmorStandReplacement(int entityId, UserConnection user) {
      this.entityId = entityId;
      this.user = user;
   }

   public void setLocation(double x, double y, double z) {
      if (x != this.locX || y != this.locY || z != this.locZ) {
         this.locX = x;
         this.locY = y;
         this.locZ = z;
         this.updateLocation();
      }

   }

   public void relMove(double x, double y, double z) {
      if (x != 0.0 || y != 0.0 || z != 0.0) {
         this.locX += x;
         this.locY += y;
         this.locZ += z;
         this.updateLocation();
      }
   }

   public void setYawPitch(float yaw, float pitch) {
      if (this.yaw != yaw && this.pitch != pitch || this.headYaw != yaw) {
         this.yaw = yaw;
         this.headYaw = yaw;
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

      this.updateState();
   }

   public void updateState() {
      byte flags = 0;
      byte armorStandFlags = 0;
      Iterator var3 = this.datawatcher.iterator();

      while(true) {
         while(var3.hasNext()) {
            Metadata metadata = (Metadata)var3.next();
            if (metadata.id() == 0 && metadata.metaType() == MetaType1_8.Byte) {
               flags = ((Number)metadata.getValue()).byteValue();
            } else if (metadata.id() == 2 && metadata.metaType() == MetaType1_8.String) {
               this.name = metadata.getValue().toString();
               if (this.name != null && this.name.equals("")) {
                  this.name = null;
               }
            } else if (metadata.id() == 10 && metadata.metaType() == MetaType1_8.Byte) {
               armorStandFlags = ((Number)metadata.getValue()).byteValue();
            } else if (metadata.id() == 3 && metadata.metaType() == MetaType1_8.Byte) {
               this.nameTagVisible = ((Number)metadata.getValue()).byteValue() != 0;
            }
         }

         this.invisible = (flags & 32) != 0;
         this.small = (armorStandFlags & 1) != 0;
         this.marker = (armorStandFlags & 16) != 0;
         State prevState = this.currentState;
         if (this.invisible && this.name != null) {
            this.currentState = ArmorStandReplacement.State.HOLOGRAM;
         } else {
            this.currentState = ArmorStandReplacement.State.ZOMBIE;
         }

         if (this.currentState != prevState) {
            this.despawn();
            this.spawn();
         } else {
            this.updateMetadata();
            this.updateLocation();
         }

         return;
      }
   }

   public void updateLocation() {
      if (this.entityIds != null) {
         if (this.currentState == ArmorStandReplacement.State.ZOMBIE) {
            this.updateZombieLocation();
         } else if (this.currentState == ArmorStandReplacement.State.HOLOGRAM) {
            this.updateHologramLocation();
         }

      }
   }

   private void updateZombieLocation() {
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

   private void updateHologramLocation() {
      PacketWrapper detach = PacketWrapper.create(ClientboundPackets1_7.ATTACH_ENTITY, (ByteBuf)null, this.user);
      detach.write(Type.INT, this.entityIds[1]);
      detach.write(Type.INT, -1);
      detach.write(Type.BOOLEAN, false);
      PacketWrapper teleportSkull = PacketWrapper.create(ClientboundPackets1_7.ENTITY_TELEPORT, (ByteBuf)null, this.user);
      teleportSkull.write(Type.INT, this.entityIds[0]);
      teleportSkull.write(Type.INT, (int)(this.locX * 32.0));
      teleportSkull.write(Type.INT, (int)((this.locY + (this.marker ? 54.85 : (this.small ? 56.0 : 57.0))) * 32.0));
      teleportSkull.write(Type.INT, (int)(this.locZ * 32.0));
      teleportSkull.write(Type.BYTE, (byte)0);
      teleportSkull.write(Type.BYTE, (byte)0);
      PacketWrapper teleportHorse = PacketWrapper.create(ClientboundPackets1_7.ENTITY_TELEPORT, (ByteBuf)null, this.user);
      teleportHorse.write(Type.INT, this.entityIds[1]);
      teleportHorse.write(Type.INT, (int)(this.locX * 32.0));
      teleportHorse.write(Type.INT, (int)((this.locY + 56.75) * 32.0));
      teleportHorse.write(Type.INT, (int)(this.locZ * 32.0));
      teleportHorse.write(Type.BYTE, (byte)0);
      teleportHorse.write(Type.BYTE, (byte)0);
      PacketWrapper attach = PacketWrapper.create(ClientboundPackets1_7.ATTACH_ENTITY, (ByteBuf)null, this.user);
      attach.write(Type.INT, this.entityIds[1]);
      attach.write(Type.INT, this.entityIds[0]);
      attach.write(Type.BOOLEAN, false);
      PacketUtil.sendPacket(detach, Protocol1_7_6_10TO1_8.class, true, true);
      PacketUtil.sendPacket(teleportSkull, Protocol1_7_6_10TO1_8.class, true, true);
      PacketUtil.sendPacket(teleportHorse, Protocol1_7_6_10TO1_8.class, true, true);
      PacketUtil.sendPacket(attach, Protocol1_7_6_10TO1_8.class, true, true);
   }

   public void updateMetadata() {
      if (this.entityIds != null) {
         PacketWrapper metadataPacket = PacketWrapper.create(ClientboundPackets1_7.ENTITY_METADATA, (ByteBuf)null, this.user);
         if (this.currentState == ArmorStandReplacement.State.ZOMBIE) {
            this.writeZombieMeta(metadataPacket);
         } else {
            if (this.currentState != ArmorStandReplacement.State.HOLOGRAM) {
               return;
            }

            this.writeHologramMeta(metadataPacket);
         }

         PacketUtil.sendPacket(metadataPacket, Protocol1_7_6_10TO1_8.class, true, true);
      }
   }

   private void writeZombieMeta(PacketWrapper metadataPacket) {
      metadataPacket.write(Type.INT, this.entityIds[0]);
      List metadataList = new ArrayList();
      Iterator var3 = this.datawatcher.iterator();

      while(var3.hasNext()) {
         Metadata metadata = (Metadata)var3.next();
         if (metadata.id() >= 0 && metadata.id() <= 9) {
            metadataList.add(new Metadata(metadata.id(), metadata.metaType(), metadata.getValue()));
         }
      }

      if (this.small) {
         metadataList.add(new Metadata(12, MetaType1_8.Byte, (byte)1));
      }

      MetadataRewriter.transform(EntityType.ZOMBIE, metadataList);
      metadataPacket.write(Types1_7_6_10.METADATA_LIST, metadataList);
   }

   private void writeHologramMeta(PacketWrapper metadataPacket) {
      metadataPacket.write(Type.INT, this.entityIds[1]);
      List metadataList = new ArrayList();
      metadataList.add(new Metadata(12, MetaType1_7_6_10.Int, -1700000));
      metadataList.add(new Metadata(10, MetaType1_7_6_10.String, this.name));
      metadataList.add(new Metadata(11, MetaType1_7_6_10.Byte, (byte)1));
      metadataPacket.write(Types1_7_6_10.METADATA_LIST, metadataList);
   }

   public void spawn() {
      if (this.entityIds != null) {
         this.despawn();
      }

      if (this.currentState == ArmorStandReplacement.State.ZOMBIE) {
         this.spawnZombie();
      } else if (this.currentState == ArmorStandReplacement.State.HOLOGRAM) {
         this.spawnHologram();
      }

      this.updateMetadata();
      this.updateLocation();
   }

   private void spawnZombie() {
      PacketWrapper spawn = PacketWrapper.create(ClientboundPackets1_7.SPAWN_MOB, (ByteBuf)null, this.user);
      spawn.write(Type.VAR_INT, this.entityId);
      spawn.write(Type.UNSIGNED_BYTE, Short.valueOf((short)54));
      spawn.write(Type.INT, (int)(this.locX * 32.0));
      spawn.write(Type.INT, (int)(this.locY * 32.0));
      spawn.write(Type.INT, (int)(this.locZ * 32.0));
      spawn.write(Type.BYTE, (byte)0);
      spawn.write(Type.BYTE, (byte)0);
      spawn.write(Type.BYTE, (byte)0);
      spawn.write(Type.SHORT, Short.valueOf((short)0));
      spawn.write(Type.SHORT, Short.valueOf((short)0));
      spawn.write(Type.SHORT, Short.valueOf((short)0));
      spawn.write(Types1_7_6_10.METADATA_LIST, new ArrayList());
      PacketUtil.sendPacket(spawn, Protocol1_7_6_10TO1_8.class, true, true);
      this.entityIds = new int[]{this.entityId};
   }

   private void spawnHologram() {
      int[] entityIds = new int[]{this.entityId, this.additionalEntityId()};
      PacketWrapper spawnSkull = PacketWrapper.create(ClientboundPackets1_7.SPAWN_ENTITY, (ByteBuf)null, this.user);
      spawnSkull.write(Type.VAR_INT, entityIds[0]);
      spawnSkull.write(Type.BYTE, (byte)66);
      spawnSkull.write(Type.INT, (int)(this.locX * 32.0));
      spawnSkull.write(Type.INT, (int)(this.locY * 32.0));
      spawnSkull.write(Type.INT, (int)(this.locZ * 32.0));
      spawnSkull.write(Type.BYTE, (byte)0);
      spawnSkull.write(Type.BYTE, (byte)0);
      spawnSkull.write(Type.INT, 0);
      PacketWrapper spawnHorse = PacketWrapper.create(ClientboundPackets1_7.SPAWN_MOB, (ByteBuf)null, this.user);
      spawnHorse.write(Type.VAR_INT, entityIds[1]);
      spawnHorse.write(Type.UNSIGNED_BYTE, Short.valueOf((short)100));
      spawnHorse.write(Type.INT, (int)(this.locX * 32.0));
      spawnHorse.write(Type.INT, (int)(this.locY * 32.0));
      spawnHorse.write(Type.INT, (int)(this.locZ * 32.0));
      spawnHorse.write(Type.BYTE, (byte)0);
      spawnHorse.write(Type.BYTE, (byte)0);
      spawnHorse.write(Type.BYTE, (byte)0);
      spawnHorse.write(Type.SHORT, Short.valueOf((short)0));
      spawnHorse.write(Type.SHORT, Short.valueOf((short)0));
      spawnHorse.write(Type.SHORT, Short.valueOf((short)0));
      spawnHorse.write(Types1_7_6_10.METADATA_LIST, new ArrayList());
      PacketUtil.sendPacket(spawnSkull, Protocol1_7_6_10TO1_8.class, true, true);
      PacketUtil.sendPacket(spawnHorse, Protocol1_7_6_10TO1_8.class, true, true);
      this.entityIds = entityIds;
   }

   private int additionalEntityId() {
      return 2147467647 - this.entityId;
   }

   public AABB getBoundingBox() {
      double w = this.small ? 0.25 : 0.5;
      double h = this.small ? 0.9875 : 1.975;
      Vector3d min = new Vector3d(this.locX - w / 2.0, this.locY, this.locZ - w / 2.0);
      Vector3d max = new Vector3d(this.locX + w / 2.0, this.locY + h, this.locZ + w / 2.0);
      return new AABB(min, max);
   }

   public void despawn() {
      if (this.entityIds != null) {
         PacketWrapper despawn = PacketWrapper.create(ClientboundPackets1_7.DESTROY_ENTITIES, (ByteBuf)null, this.user);
         despawn.write(Type.BYTE, (byte)this.entityIds.length);
         int[] var2 = this.entityIds;
         int var3 = var2.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            int id = var2[var4];
            despawn.write(Type.INT, id);
         }

         this.entityIds = null;
         PacketUtil.sendPacket(despawn, Protocol1_7_6_10TO1_8.class, true, true);
      }
   }

   private static enum State {
      HOLOGRAM,
      ZOMBIE;
   }
}
