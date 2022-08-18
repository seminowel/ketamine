package io.github.nevalackin.client.impl.module.misc.player;

import io.github.nevalackin.client.api.module.Category;
import io.github.nevalackin.client.api.module.Module;
import io.github.nevalackin.client.util.misc.ServerUtil;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.util.ArrayList;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;

public final class PingSpoof extends Module {
   private long timeOfFlag;
   private final ArrayList packets = new ArrayList();
   @EventLink
   private final Listener onLoadWorld = (event) -> {
      this.timeOfFlag = 0L;
      this.packets.clear();
   };
   @EventLink
   private final Listener onSendPacket = (event) -> {
      Packet packet = event.getPacket();
      if (ServerUtil.isHypixel()) {
         if (packet instanceof C03PacketPlayer) {
            if (this.mc.thePlayer.ticksExisted < 120) {
               event.setCancelled();
            } else if (!event.isCancelled() && System.currentTimeMillis() - this.timeOfFlag < 10000L) {
               event.setCancelled();
               this.packets.add(event.getPacket());

               while(this.packets.size() > 100) {
                  this.mc.thePlayer.sendQueue.sendPacket((Packet)this.packets.remove(this.packets.size() - 1));
               }
            }
         }

      }
   };

   public PingSpoof() {
      super("Ping Spoof", Category.MISC, Category.SubCategory.MISC_PLAYER);
   }

   public void onEnable() {
      this.timeOfFlag = 0L;
   }

   public void onDisable() {
   }
}
