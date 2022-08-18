package io.github.nevalackin.client.impl.event.packet;

import io.github.nevalackin.client.api.event.CancellableEvent;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;

public final class ReceivePacketEvent extends CancellableEvent {
   private final INetHandler packetListener;
   private final Packet packet;

   public ReceivePacketEvent(Packet packet, INetHandler packetListener) {
      this.packet = packet;
      this.packetListener = packetListener;
   }

   public INetHandler getPacketListener() {
      return this.packetListener;
   }

   public Packet getPacket() {
      return this.packet;
   }
}
