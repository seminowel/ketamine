package us.myles.ViaVersion.api;

import io.netty.buffer.ByteBuf;
import java.util.SortedSet;
import java.util.UUID;
import us.myles.ViaVersion.api.boss.BossBar;
import us.myles.ViaVersion.api.boss.BossColor;
import us.myles.ViaVersion.api.boss.BossStyle;

/** @deprecated */
@Deprecated
public interface ViaAPI {
   int getPlayerVersion(Object var1);

   int getPlayerVersion(UUID var1);

   default boolean isPorted(UUID playerUUID) {
      return this.isInjected(playerUUID);
   }

   boolean isInjected(UUID var1);

   String getVersion();

   void sendRawPacket(Object var1, ByteBuf var2);

   void sendRawPacket(UUID var1, ByteBuf var2);

   BossBar createBossBar(String var1, BossColor var2, BossStyle var3);

   BossBar createBossBar(String var1, float var2, BossColor var3, BossStyle var4);

   SortedSet getSupportedVersions();

   SortedSet getFullSupportedVersions();
}
