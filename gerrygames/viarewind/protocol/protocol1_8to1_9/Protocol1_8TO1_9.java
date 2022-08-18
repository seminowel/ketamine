package de.gerrygames.viarewind.protocol.protocol1_8to1_9;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.ValueTransformer;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_8.ClientboundPackets1_8;
import com.viaversion.viaversion.protocols.protocol1_8.ServerboundPackets1_8;
import com.viaversion.viaversion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ClientboundPackets1_9;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ServerboundPackets1_9;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.packets.EntityPackets;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.packets.InventoryPackets;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.packets.PlayerPackets;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.packets.ScoreboardPackets;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.packets.SpawnPackets;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.packets.WorldPackets;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.storage.BlockPlaceDestroyTracker;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.storage.BossBarStorage;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.storage.Cooldown;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.storage.EntityTracker;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.storage.Levitation;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.storage.PlayerPosition;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.storage.Windows;
import de.gerrygames.viarewind.utils.Ticker;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;

public class Protocol1_8TO1_9 extends AbstractProtocol {
   public static final Timer TIMER = new Timer("ViaRewind-1_8TO1_9", true);
   public static final Set VALID_ATTRIBUTES = new HashSet();
   public static final ValueTransformer TO_OLD_INT;
   public static final ValueTransformer DEGREES_TO_ANGLE;

   public Protocol1_8TO1_9() {
      super(ClientboundPackets1_9.class, ClientboundPackets1_8.class, ServerboundPackets1_9.class, ServerboundPackets1_8.class);
   }

   protected void registerPackets() {
      EntityPackets.register(this);
      InventoryPackets.register(this);
      PlayerPackets.register(this);
      ScoreboardPackets.register(this);
      SpawnPackets.register(this);
      WorldPackets.register(this);
   }

   public void init(UserConnection userConnection) {
      Ticker.init();
      userConnection.put(new Windows(userConnection));
      userConnection.put(new EntityTracker(userConnection));
      userConnection.put(new Levitation(userConnection));
      userConnection.put(new PlayerPosition(userConnection));
      userConnection.put(new Cooldown(userConnection));
      userConnection.put(new BlockPlaceDestroyTracker(userConnection));
      userConnection.put(new BossBarStorage(userConnection));
      userConnection.put(new ClientWorld(userConnection));
   }

   static {
      TO_OLD_INT = new ValueTransformer(Type.INT) {
         public Integer transform(PacketWrapper wrapper, Double inputValue) {
            return (int)(inputValue * 32.0);
         }
      };
      DEGREES_TO_ANGLE = new ValueTransformer(Type.BYTE) {
         public Byte transform(PacketWrapper packetWrapper, Float degrees) throws Exception {
            return (byte)((int)(degrees / 360.0F * 256.0F));
         }
      };
      VALID_ATTRIBUTES.add("generic.maxHealth");
      VALID_ATTRIBUTES.add("generic.followRange");
      VALID_ATTRIBUTES.add("generic.knockbackResistance");
      VALID_ATTRIBUTES.add("generic.movementSpeed");
      VALID_ATTRIBUTES.add("generic.attackDamage");
      VALID_ATTRIBUTES.add("horse.jumpStrength");
      VALID_ATTRIBUTES.add("zombie.spawnReinforcements");
   }
}
