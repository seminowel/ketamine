package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.storage;

import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.entity.ClientEntityIdChangeListener;
import com.viaversion.viaversion.api.minecraft.entities.Entity1_10Types;
import com.viaversion.viaversion.api.minecraft.entities.Entity1_10Types.EntityType;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.version.Types1_8;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.Protocol1_7_6_10TO1_8;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.metadata.MetadataRewriter;
import de.gerrygames.viarewind.replacement.EntityReplacement;
import de.gerrygames.viarewind.utils.PacketUtil;
import io.netty.buffer.ByteBuf;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EntityTracker extends StoredObject implements ClientEntityIdChangeListener {
   private final Map clientEntityTypes = new ConcurrentHashMap();
   private final Map metadataBuffer = new ConcurrentHashMap();
   private final Map vehicles = new ConcurrentHashMap();
   private final Map entityReplacements = new ConcurrentHashMap();
   private final Map playersByEntityId = new HashMap();
   private final Map playersByUniqueId = new HashMap();
   private final Map playerEquipment = new HashMap();
   private int gamemode = 0;
   private int playerId = -1;
   private int spectating = -1;
   private int dimension = 0;

   public EntityTracker(UserConnection user) {
      super(user);
   }

   public void removeEntity(int entityId) {
      this.clientEntityTypes.remove(entityId);
      if (this.entityReplacements.containsKey(entityId)) {
         ((EntityReplacement)this.entityReplacements.remove(entityId)).despawn();
      }

      if (this.playersByEntityId.containsKey(entityId)) {
         this.playersByUniqueId.remove(this.playersByEntityId.remove(entityId));
      }

   }

   public void addPlayer(Integer entityId, UUID uuid) {
      this.playersByUniqueId.put(uuid, entityId);
      this.playersByEntityId.put(entityId, uuid);
   }

   public UUID getPlayerUUID(int entityId) {
      return (UUID)this.playersByEntityId.get(entityId);
   }

   public int getPlayerEntityId(UUID uuid) {
      return (Integer)this.playersByUniqueId.getOrDefault(uuid, -1);
   }

   public Item[] getPlayerEquipment(UUID uuid) {
      return (Item[])this.playerEquipment.get(uuid);
   }

   public void setPlayerEquipment(UUID uuid, Item[] equipment) {
      this.playerEquipment.put(uuid, equipment);
   }

   public Map getClientEntityTypes() {
      return this.clientEntityTypes;
   }

   public void addMetadataToBuffer(int entityID, List metadataList) {
      if (this.metadataBuffer.containsKey(entityID)) {
         ((List)this.metadataBuffer.get(entityID)).addAll(metadataList);
      } else if (!metadataList.isEmpty()) {
         this.metadataBuffer.put(entityID, metadataList);
      }

   }

   public void addEntityReplacement(EntityReplacement entityReplacement) {
      this.entityReplacements.put(entityReplacement.getEntityId(), entityReplacement);
   }

   public EntityReplacement getEntityReplacement(int entityId) {
      return (EntityReplacement)this.entityReplacements.get(entityId);
   }

   public List getBufferedMetadata(int entityId) {
      return (List)this.metadataBuffer.get(entityId);
   }

   public void sendMetadataBuffer(int entityId) {
      if (this.metadataBuffer.containsKey(entityId)) {
         if (this.entityReplacements.containsKey(entityId)) {
            ((EntityReplacement)this.entityReplacements.get(entityId)).updateMetadata((List)this.metadataBuffer.remove(entityId));
         } else {
            Entity1_10Types.EntityType type = (Entity1_10Types.EntityType)this.getClientEntityTypes().get(entityId);
            PacketWrapper wrapper = PacketWrapper.create(28, (ByteBuf)null, this.getUser());
            wrapper.write(Type.VAR_INT, entityId);
            wrapper.write(Types1_8.METADATA_LIST, (List)this.metadataBuffer.get(entityId));
            MetadataRewriter.transform(type, (List)this.metadataBuffer.get(entityId));
            if (!((List)this.metadataBuffer.get(entityId)).isEmpty()) {
               PacketUtil.sendPacket(wrapper, Protocol1_7_6_10TO1_8.class);
            }

            this.metadataBuffer.remove(entityId);
         }

      }
   }

   public int getVehicle(int passengerId) {
      Iterator var2 = this.vehicles.entrySet().iterator();

      Map.Entry vehicle;
      do {
         if (!var2.hasNext()) {
            return -1;
         }

         vehicle = (Map.Entry)var2.next();
      } while((Integer)vehicle.getValue() != passengerId);

      return (Integer)vehicle.getValue();
   }

   public int getPassenger(int vehicleId) {
      return (Integer)this.vehicles.getOrDefault(vehicleId, -1);
   }

   public void setPassenger(int vehicleId, int passengerId) {
      if (vehicleId == this.spectating && this.spectating != this.playerId) {
         try {
            PacketWrapper sneakPacket = PacketWrapper.create(11, (ByteBuf)null, this.getUser());
            sneakPacket.write(Type.VAR_INT, this.playerId);
            sneakPacket.write(Type.VAR_INT, 0);
            sneakPacket.write(Type.VAR_INT, 0);
            PacketWrapper unsneakPacket = PacketWrapper.create(11, (ByteBuf)null, this.getUser());
            unsneakPacket.write(Type.VAR_INT, this.playerId);
            unsneakPacket.write(Type.VAR_INT, 1);
            unsneakPacket.write(Type.VAR_INT, 0);
            PacketUtil.sendToServer(sneakPacket, Protocol1_7_6_10TO1_8.class, true, true);
            this.setSpectating(this.playerId);
         } catch (Exception var5) {
            var5.printStackTrace();
         }
      }

      if (vehicleId == -1) {
         int oldVehicleId = this.getVehicle(passengerId);
         this.vehicles.remove(oldVehicleId);
      } else if (passengerId == -1) {
         this.vehicles.remove(vehicleId);
      } else {
         this.vehicles.put(vehicleId, passengerId);
      }

   }

   public int getSpectating() {
      return this.spectating;
   }

   public boolean setSpectating(int spectating) {
      PacketWrapper mount;
      if (spectating != this.playerId && this.getPassenger(spectating) != -1) {
         mount = PacketWrapper.create(11, (ByteBuf)null, this.getUser());
         mount.write(Type.VAR_INT, this.playerId);
         mount.write(Type.VAR_INT, 0);
         mount.write(Type.VAR_INT, 0);
         PacketWrapper unsneakPacket = PacketWrapper.create(11, (ByteBuf)null, this.getUser());
         unsneakPacket.write(Type.VAR_INT, this.playerId);
         unsneakPacket.write(Type.VAR_INT, 1);
         unsneakPacket.write(Type.VAR_INT, 0);
         PacketUtil.sendToServer(mount, Protocol1_7_6_10TO1_8.class, true, true);
         this.setSpectating(this.playerId);
         return false;
      } else {
         if (this.spectating != spectating && this.spectating != this.playerId) {
            mount = PacketWrapper.create(27, (ByteBuf)null, this.getUser());
            mount.write(Type.INT, this.playerId);
            mount.write(Type.INT, -1);
            mount.write(Type.BOOLEAN, false);
            PacketUtil.sendPacket(mount, Protocol1_7_6_10TO1_8.class);
         }

         this.spectating = spectating;
         if (spectating != this.playerId) {
            mount = PacketWrapper.create(27, (ByteBuf)null, this.getUser());
            mount.write(Type.INT, this.playerId);
            mount.write(Type.INT, this.spectating);
            mount.write(Type.BOOLEAN, false);
            PacketUtil.sendPacket(mount, Protocol1_7_6_10TO1_8.class);
         }

         return true;
      }
   }

   public int getGamemode() {
      return this.gamemode;
   }

   public void setGamemode(int gamemode) {
      this.gamemode = gamemode;
   }

   public int getPlayerId() {
      return this.playerId;
   }

   public void setPlayerId(int playerId) {
      this.playerId = this.spectating = playerId;
   }

   public void clearEntities() {
      this.clientEntityTypes.clear();
      this.entityReplacements.clear();
      this.vehicles.clear();
      this.metadataBuffer.clear();
   }

   public int getDimension() {
      return this.dimension;
   }

   public void setDimension(int dimension) {
      this.dimension = dimension;
   }

   public void setClientEntityId(int playerEntityId) {
      if (this.spectating == this.playerId) {
         this.spectating = playerEntityId;
      }

      this.clientEntityTypes.remove(this.playerId);
      this.playerId = playerEntityId;
      this.clientEntityTypes.put(this.playerId, EntityType.ENTITY_HUMAN);
   }
}
