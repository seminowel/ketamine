package de.gerrygames.viarewind;

import com.google.inject.Inject;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyReloadEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.viaversion.viaversion.velocity.util.LoggerWrapper;
import de.gerrygames.viarewind.api.ViaRewindConfigImpl;
import de.gerrygames.viarewind.api.ViaRewindPlatform;
import java.nio.file.Path;
import java.util.logging.Logger;

@Plugin(
   id = "viarewind",
   name = "ViaRewind",
   version = "2.0.3-SNAPSHOT",
   authors = {"Gerrygames"},
   dependencies = {@Dependency(
   id = "viaversion"
), @Dependency(
   id = "viabackwards",
   optional = true
)},
   url = "https://viaversion.com/rewind"
)
public class VelocityPlugin implements ViaRewindPlatform {
   private Logger logger;
   @Inject
   private org.slf4j.Logger loggerSlf4j;
   @Inject
   @DataDirectory
   private Path configDir;
   private ViaRewindConfigImpl conf;

   @Subscribe(
      order = PostOrder.LATE
   )
   public void onProxyStart(ProxyInitializeEvent e) {
      this.logger = new LoggerWrapper(this.loggerSlf4j);
      this.conf = new ViaRewindConfigImpl(this.configDir.resolve("config.yml").toFile());
      this.conf.reloadConfig();
      this.init(this.conf);
   }

   @Subscribe
   public void onReload(ProxyReloadEvent e) {
      this.conf.reloadConfig();
   }

   public Logger getLogger() {
      return this.logger;
   }
}
