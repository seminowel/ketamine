package de.gerrygames.viarewind.utils;

import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.exception.CancelException;

public class PacketUtil {
   public static void sendToServer(PacketWrapper packet, Class packetProtocol) {
      sendToServer(packet, packetProtocol, true);
   }

   public static void sendToServer(PacketWrapper packet, Class packetProtocol, boolean skipCurrentPipeline) {
      sendToServer(packet, packetProtocol, skipCurrentPipeline, false);
   }

   public static void sendToServer(PacketWrapper packet, Class packetProtocol, boolean skipCurrentPipeline, boolean currentThread) {
      try {
         if (currentThread) {
            packet.sendToServer(packetProtocol, skipCurrentPipeline);
         } else {
            packet.scheduleSendToServer(packetProtocol, skipCurrentPipeline);
         }
      } catch (CancelException var5) {
      } catch (Exception var6) {
         var6.printStackTrace();
      }

   }

   public static boolean sendPacket(PacketWrapper packet, Class packetProtocol) {
      return sendPacket(packet, packetProtocol, true);
   }

   public static boolean sendPacket(PacketWrapper packet, Class packetProtocol, boolean skipCurrentPipeline) {
      return sendPacket(packet, packetProtocol, true, false);
   }

   public static boolean sendPacket(PacketWrapper packet, Class packetProtocol, boolean skipCurrentPipeline, boolean currentThread) {
      try {
         if (currentThread) {
            packet.send(packetProtocol, skipCurrentPipeline);
         } else {
            packet.scheduleSend(packetProtocol, skipCurrentPipeline);
         }

         return true;
      } catch (CancelException var5) {
      } catch (Exception var6) {
         var6.printStackTrace();
      }

      return false;
   }
}
