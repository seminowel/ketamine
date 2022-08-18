package de.gerrygames.viarewind.protocol.protocol1_8to1_9.storage;

import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.util.Pair;
import de.gerrygames.viarewind.ViaRewind;
import de.gerrygames.viarewind.api.ViaRewindConfig;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.Protocol1_8TO1_9;
import de.gerrygames.viarewind.utils.PacketUtil;
import de.gerrygames.viarewind.utils.Tickable;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.UUID;

public class Cooldown extends StoredObject implements Tickable {
   private double attackSpeed = 4.0;
   private long lastHit = 0L;
   private final ViaRewindConfig.CooldownIndicator cooldownIndicator;
   private UUID bossUUID;
   private boolean lastSend;
   private static final int max = 10;

   public Cooldown(UserConnection user) {
      super(user);

      ViaRewindConfig.CooldownIndicator indicator;
      try {
         indicator = ViaRewind.getConfig().getCooldownIndicator();
      } catch (IllegalArgumentException var4) {
         ViaRewind.getPlatform().getLogger().warning("Invalid cooldown-indicator setting");
         indicator = ViaRewindConfig.CooldownIndicator.DISABLED;
      }

      this.cooldownIndicator = indicator;
   }

   public void tick() {
      if (!this.hasCooldown()) {
         if (this.lastSend) {
            this.hide();
            this.lastSend = false;
         }

      } else {
         BlockPlaceDestroyTracker tracker = (BlockPlaceDestroyTracker)this.getUser().get(BlockPlaceDestroyTracker.class);
         if (tracker.isMining()) {
            this.lastHit = 0L;
            if (this.lastSend) {
               this.hide();
               this.lastSend = false;
            }

         } else {
            this.showCooldown();
            this.lastSend = true;
         }
      }
   }

   private void showCooldown() {
      if (this.cooldownIndicator == ViaRewindConfig.CooldownIndicator.TITLE) {
         this.sendTitle("", this.getTitle(), 0, 2, 5);
      } else if (this.cooldownIndicator == ViaRewindConfig.CooldownIndicator.ACTION_BAR) {
         this.sendActionBar(this.getTitle());
      } else if (this.cooldownIndicator == ViaRewindConfig.CooldownIndicator.BOSS_BAR) {
         this.sendBossBar((float)this.getCooldown());
      }

   }

   private void hide() {
      if (this.cooldownIndicator == ViaRewindConfig.CooldownIndicator.ACTION_BAR) {
         this.sendActionBar("§r");
      } else if (this.cooldownIndicator == ViaRewindConfig.CooldownIndicator.TITLE) {
         this.hideTitle();
      } else if (this.cooldownIndicator == ViaRewindConfig.CooldownIndicator.BOSS_BAR) {
         this.hideBossBar();
      }

   }

   private void hideBossBar() {
      if (this.bossUUID != null) {
         PacketWrapper wrapper = PacketWrapper.create(12, (ByteBuf)null, this.getUser());
         wrapper.write(Type.UUID, this.bossUUID);
         wrapper.write(Type.VAR_INT, 1);
         PacketUtil.sendPacket(wrapper, Protocol1_8TO1_9.class, false, true);
         this.bossUUID = null;
      }
   }

   private void sendBossBar(float cooldown) {
      PacketWrapper wrapper = PacketWrapper.create(12, (ByteBuf)null, this.getUser());
      if (this.bossUUID == null) {
         this.bossUUID = UUID.randomUUID();
         wrapper.write(Type.UUID, this.bossUUID);
         wrapper.write(Type.VAR_INT, 0);
         wrapper.write(Type.STRING, "{\"text\":\"  \"}");
         wrapper.write(Type.FLOAT, cooldown);
         wrapper.write(Type.VAR_INT, 0);
         wrapper.write(Type.VAR_INT, 0);
         wrapper.write(Type.UNSIGNED_BYTE, Short.valueOf((short)0));
      } else {
         wrapper.write(Type.UUID, this.bossUUID);
         wrapper.write(Type.VAR_INT, 2);
         wrapper.write(Type.FLOAT, cooldown);
      }

      PacketUtil.sendPacket(wrapper, Protocol1_8TO1_9.class, false, true);
   }

   private void hideTitle() {
      PacketWrapper hide = PacketWrapper.create(69, (ByteBuf)null, this.getUser());
      hide.write(Type.VAR_INT, 3);
      PacketUtil.sendPacket(hide, Protocol1_8TO1_9.class);
   }

   private void sendTitle(String title, String subTitle, int fadeIn, int stay, int fadeOut) {
      PacketWrapper timePacket = PacketWrapper.create(69, (ByteBuf)null, this.getUser());
      timePacket.write(Type.VAR_INT, 2);
      timePacket.write(Type.INT, fadeIn);
      timePacket.write(Type.INT, stay);
      timePacket.write(Type.INT, fadeOut);
      PacketWrapper titlePacket = PacketWrapper.create(69, (ByteBuf)null, this.getUser());
      titlePacket.write(Type.VAR_INT, 0);
      titlePacket.write(Type.STRING, title);
      PacketWrapper subtitlePacket = PacketWrapper.create(69, (ByteBuf)null, this.getUser());
      subtitlePacket.write(Type.VAR_INT, 1);
      subtitlePacket.write(Type.STRING, subTitle);
      PacketUtil.sendPacket(titlePacket, Protocol1_8TO1_9.class);
      PacketUtil.sendPacket(subtitlePacket, Protocol1_8TO1_9.class);
      PacketUtil.sendPacket(timePacket, Protocol1_8TO1_9.class);
   }

   private void sendActionBar(String bar) {
      PacketWrapper actionBarPacket = PacketWrapper.create(2, (ByteBuf)null, this.getUser());
      actionBarPacket.write(Type.STRING, bar);
      actionBarPacket.write(Type.BYTE, (byte)2);
      PacketUtil.sendPacket(actionBarPacket, Protocol1_8TO1_9.class);
   }

   public boolean hasCooldown() {
      long time = System.currentTimeMillis() - this.lastHit;
      double cooldown = this.restrain((double)time * this.attackSpeed / 1000.0, 0.0, 1.5);
      return cooldown > 0.1 && cooldown < 1.1;
   }

   public double getCooldown() {
      long time = System.currentTimeMillis() - this.lastHit;
      return this.restrain((double)time * this.attackSpeed / 1000.0, 0.0, 1.0);
   }

   private double restrain(double x, double a, double b) {
      if (x < a) {
         return a;
      } else {
         return x > b ? b : x;
      }
   }

   private String getTitle() {
      String symbol = this.cooldownIndicator == ViaRewindConfig.CooldownIndicator.ACTION_BAR ? "■" : "˙";
      double cooldown = this.getCooldown();
      int green = (int)Math.floor(10.0 * cooldown);
      int grey = 10 - green;
      StringBuilder builder = new StringBuilder("§8");

      while(green-- > 0) {
         builder.append(symbol);
      }

      builder.append("§7");

      while(grey-- > 0) {
         builder.append(symbol);
      }

      return builder.toString();
   }

   public double getAttackSpeed() {
      return this.attackSpeed;
   }

   public void setAttackSpeed(double attackSpeed) {
      this.attackSpeed = attackSpeed;
   }

   public void setAttackSpeed(double base, ArrayList modifiers) {
      this.attackSpeed = base;

      int j;
      for(j = 0; j < modifiers.size(); ++j) {
         if ((Byte)((Pair)modifiers.get(j)).getKey() == 0) {
            this.attackSpeed += (Double)((Pair)modifiers.get(j)).getValue();
            modifiers.remove(j--);
         }
      }

      for(j = 0; j < modifiers.size(); ++j) {
         if ((Byte)((Pair)modifiers.get(j)).getKey() == 1) {
            this.attackSpeed += base * (Double)((Pair)modifiers.get(j)).getValue();
            modifiers.remove(j--);
         }
      }

      for(j = 0; j < modifiers.size(); ++j) {
         if ((Byte)((Pair)modifiers.get(j)).getKey() == 2) {
            this.attackSpeed *= 1.0 + (Double)((Pair)modifiers.get(j)).getValue();
            modifiers.remove(j--);
         }
      }

   }

   public void hit() {
      this.lastHit = System.currentTimeMillis();
   }

   public void setLastHit(long lastHit) {
      this.lastHit = lastHit;
   }
}
