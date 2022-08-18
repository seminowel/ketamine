package de.gerrygames.viarewind.utils;

import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;

public interface ServerSender {
   void sendToServer(PacketWrapper var1, Class var2, boolean var3, boolean var4) throws Exception;
}
