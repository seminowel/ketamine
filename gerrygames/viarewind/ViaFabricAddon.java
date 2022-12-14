package de.gerrygames.viarewind;

import de.gerrygames.viarewind.api.ViaRewindConfigImpl;
import de.gerrygames.viarewind.api.ViaRewindPlatform;
import de.gerrygames.viarewind.fabric.util.LoggerWrapper;
import java.util.logging.Logger;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;

public class ViaFabricAddon implements ViaRewindPlatform, Runnable {
   private final Logger logger = new LoggerWrapper(LogManager.getLogger("ViaRewind"));

   public void run() {
      ViaRewindConfigImpl conf = new ViaRewindConfigImpl(FabricLoader.getInstance().getConfigDirectory().toPath().resolve("ViaRewind").resolve("config.yml").toFile());
      conf.reloadConfig();
      this.init(conf);
   }

   public Logger getLogger() {
      return this.logger;
   }
}
