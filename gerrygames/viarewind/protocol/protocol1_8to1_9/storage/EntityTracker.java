package de.gerrygames.viarewind.protocol.protocol1_8to1_9.storage;

import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.entity.ClientEntityIdChangeListener;
import com.viaversion.viaversion.api.minecraft.Vector;
import com.viaversion.viaversion.api.minecraft.entities.Entity1_10Types;
import com.viaversion.viaversion.api.minecraft.entities.Entity1_10Types.EntityType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.version.Types1_8;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.Protocol1_8TO1_9;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.metadata.MetadataRewriter;
import de.gerrygames.viarewind.replacement.EntityReplacement;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EntityTracker extends StoredObject implements ClientEntityIdChangeListener {
   private final Map vehicleMap = new ConcurrentHashMap();
   private final Map clientEntityTypes = new ConcurrentHashMap();
   private final Map metadataBuffer = new ConcurrentHashMap();
   private final Map entityReplacements = new ConcurrentHashMap();
   private final Map entityOffsets = new ConcurrentHashMap();
   private int playerId;
   private int playerGamemode = 0;

   public EntityTracker(UserConnection user) {
      super(user);
   }

   public void setPlayerId(int entityId) {
      this.playerId = entityId;
   }

   public int getPlayerId() {
      return this.playerId;
   }

   public int getPlayerGamemode() {
      return this.playerGamemode;
   }

   public void setPlayerGamemode(int playerGamemode) {
      this.playerGamemode = playerGamemode;
   }

   public void removeEntity(int entityId) {
      this.vehicleMap.remove(entityId);
      this.vehicleMap.forEach((vehicle, passengers) -> {
         passengers.remove(entityId);
      });
      this.vehicleMap.entrySet().removeIf((entry) -> {
         return ((List)entry.getValue()).isEmpty();
      });
      this.clientEntityTypes.remove(entityId);
      this.entityOffsets.remove(entityId);
      if (this.entityReplacements.containsKey(entityId)) {
         ((EntityReplacement)this.entityReplacements.remove(entityId)).despawn();
      }

   }

   public void resetEntityOffset(int entityId) {
      this.entityOffsets.remove(entityId);
   }

   public Vector getEntityOffset(int entityId) {
      return (Vector)this.entityOffsets.computeIfAbsent(entityId, (key) -> {
         return new Vector(0, 0, 0);
      });
   }

   public void addToEntityOffset(int entityId, short relX, short relY, short relZ) {
      this.entityOffsets.compute(entityId, (key, offset) -> {
         if (offset == null) {
            return new Vector(relX, relY, relZ);
         } else {
            offset.setBlockX(offset.getBlockX() + relX);
            offset.setBlockY(offset.getBlockY() + relY);
            offset.setBlockZ(offset.getBlockZ() + relZ);
            return offset;
         }
      });
   }

   public void setEntityOffset(int entityId, short relX, short relY, short relZ) {
      this.entityOffsets.compute(entityId, (key, offset) -> {
         if (offset == null) {
            return new Vector(relX, relY, relZ);
         } else {
            offset.setBlockX(relX);
            offset.setBlockY(relY);
            offset.setBlockZ(relZ);
            return offset;
         }
      });
   }

   public void setEntityOffset(int entityId, Vector offset) {
      this.entityOffsets.put(entityId, offset);
   }

   public List getPassengers(int entityId) {
      return (List)this.vehicleMap.getOrDefault(entityId, new ArrayList());
   }

   public void setPassengers(int entityId, List passengers) {
      this.vehicleMap.put(entityId, passengers);
   }

   public void addEntityReplacement(EntityReplacement entityReplacement) {
      this.entityReplacements.put(entityReplacement.getEntityId(), entityReplacement);
   }

   public EntityReplacement getEntityReplacement(int entityId) {
      return (EntityReplacement)this.entityReplacements.get(entityId);
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

   public List getBufferedMetadata(int entityId) {
      return (List)this.metadataBuffer.get(entityId);
   }

   public boolean isInsideVehicle(int entityId) {
      Iterator var2 = this.vehicleMap.values().iterator();

      List vehicle;
      do {
         if (!var2.hasNext()) {
            return false;
         }

         vehicle = (List)var2.next();
      } while(!vehicle.contains(entityId));

      return true;
   }

   public int getVehicle(int passenger) {
      Iterator var2 = this.vehicleMap.entrySet().iterator();

      Map.Entry vehicle;
      do {
         if (!var2.hasNext()) {
            return -1;
         }

         vehicle = (Map.Entry)var2.next();
      } while(!((List)vehicle.getValue()).contains(passenger));

      return (Integer)vehicle.getKey();
   }

   public boolean isPassenger(int vehicle, int passenger) {
      return this.vehicleMap.containsKey(vehicle) && ((List)this.vehicleMap.get(vehicle)).contains(passenger);
   }

   public void sendMetadataBuffer(int entityId) {
      if (this.metadataBuffer.containsKey(entityId)) {
         if (this.entityReplacements.containsKey(entityId)) {
            ((EntityReplacement)this.entityReplacements.get(entityId)).updateMetadata((List)this.metadataBuffer.remove(entityId));
         } else {
            PacketWrapper wrapper = PacketWrapper.create(28, (ByteBuf)null, this.getUser());
            wrapper.write(Type.VAR_INT, entityId);
            wrapper.write(Types1_8.METADATA_LIST, (List)this.metadataBuffer.get(entityId));
            MetadataRewriter.transform((Entity1_10Types.EntityType)this.getClientEntityTypes().get(entityId), (List)this.metadataBuffer.get(entityId));
            if (!((List)this.metadataBuffer.get(entityId)).isEmpty()) {
               try {
                  wrapper.send(Protocol1_8TO1_9.class);
               } catch (Exception var4) {
                  var4.printStackTrace();
               }
            }

            this.metadataBuffer.remove(entityId);
         }

      }
   }

   public void setClientEntityId(int playerEntityId) {
      this.clientEntityTypes.remove(this.playerId);
      this.playerId = playerEntityId;
      this.clientEntityTypes.put(this.playerId, EntityType.ENTITY_HUMAN);
   }
}
