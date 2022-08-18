package viamcp.loader;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.platform.ViaPlatformLoader;
import com.viaversion.viaversion.api.protocol.version.VersionProvider;
import com.viaversion.viaversion.bungee.providers.BungeeMovementTransmitter;
import com.viaversion.viaversion.protocols.base.BaseVersionProvider;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.providers.MovementTransmitterProvider;
import viamcp.ViaMCP;

public class MCPViaLoader implements ViaPlatformLoader {
   public void load() {
      Via.getManager().getProviders().use(MovementTransmitterProvider.class, new BungeeMovementTransmitter());
      Via.getManager().getProviders().use(VersionProvider.class, new BaseVersionProvider() {
         public int getClosestServerProtocol(UserConnection connection) throws Exception {
            return connection.isClientSide() ? ViaMCP.getInstance().getVersion() : super.getClosestServerProtocol(connection);
         }
      });
   }

   public void unload() {
   }
}
