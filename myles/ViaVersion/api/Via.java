package us.myles.ViaVersion.api;

import io.netty.buffer.ByteBuf;
import java.util.SortedSet;
import java.util.UUID;
import us.myles.ViaVersion.api.boss.BossBar;
import us.myles.ViaVersion.api.boss.BossColor;
import us.myles.ViaVersion.api.boss.BossStyle;

/** @deprecated */
@Deprecated
public class Via implements ViaAPI {
   private static final ViaAPI INSTANCE = new Via();

   private Via() {
   }

   /** @deprecated */
   @Deprecated
   public static ViaAPI getAPI() {
      return INSTANCE;
   }

   public int getPlayerVersion(Object player) {
      return com.viaversion.viaversion.api.Via.getAPI().getPlayerVersion(player);
   }

   public int getPlayerVersion(UUID uuid) {
      return com.viaversion.viaversion.api.Via.getAPI().getPlayerVersion(uuid);
   }

   public boolean isInjected(UUID playerUUID) {
      return com.viaversion.viaversion.api.Via.getAPI().isInjected(playerUUID);
   }

   public String getVersion() {
      return com.viaversion.viaversion.api.Via.getAPI().getVersion();
   }

   public void sendRawPacket(Object player, ByteBuf packet) {
      com.viaversion.viaversion.api.Via.getAPI().sendRawPacket(player, packet);
   }

   public void sendRawPacket(UUID uuid, ByteBuf packet) {
      com.viaversion.viaversion.api.Via.getAPI().sendRawPacket(uuid, packet);
   }

   public BossBar createBossBar(String title, BossColor color, BossStyle style) {
      return new BossBar(com.viaversion.viaversion.api.Via.getAPI().legacyAPI().createLegacyBossBar(title, com.viaversion.viaversion.api.legacy.bossbar.BossColor.values()[color.ordinal()], com.viaversion.viaversion.api.legacy.bossbar.BossStyle.values()[style.ordinal()]));
   }

   public BossBar createBossBar(String title, float health, BossColor color, BossStyle style) {
      return new BossBar(com.viaversion.viaversion.api.Via.getAPI().legacyAPI().createLegacyBossBar(title, health, com.viaversion.viaversion.api.legacy.bossbar.BossColor.values()[color.ordinal()], com.viaversion.viaversion.api.legacy.bossbar.BossStyle.values()[style.ordinal()]));
   }

   public SortedSet getSupportedVersions() {
      return com.viaversion.viaversion.api.Via.getAPI().getSupportedVersions();
   }

   public SortedSet getFullSupportedVersions() {
      return com.viaversion.viaversion.api.Via.getAPI().getFullSupportedVersions();
   }
}
