package viamcp.loader;

import com.viaversion.viabackwards.api.ViaBackwardsPlatform;
import java.io.File;
import java.util.logging.Logger;
import viamcp.ViaMCP;

public class MCPBackwardsLoader implements ViaBackwardsPlatform {
   private final File file;

   public MCPBackwardsLoader(File file) {
      this.init(this.file = new File(file, "ViaBackwards"));
   }

   public Logger getLogger() {
      return ViaMCP.getInstance().getjLogger();
   }

   public void disable() {
   }

   public boolean isOutdated() {
      return false;
   }

   public File getDataFolder() {
      return new File(this.file, "config.yml");
   }
}
