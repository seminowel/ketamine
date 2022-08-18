package de.gerrygames.viarewind.api;

import com.viaversion.viaversion.util.Config;
import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ViaRewindConfigImpl extends Config implements ViaRewindConfig {
   public ViaRewindConfigImpl(File configFile) {
      super(configFile);
      this.reloadConfig();
   }

   public ViaRewindConfig.CooldownIndicator getCooldownIndicator() {
      return ViaRewindConfig.CooldownIndicator.valueOf(this.getString("cooldown-indicator", "TITLE").toUpperCase());
   }

   public boolean isReplaceAdventureMode() {
      return this.getBoolean("replace-adventure", false);
   }

   public boolean isReplaceParticles() {
      return this.getBoolean("replace-particles", false);
   }

   public int getMaxBookPages() {
      return this.getInt("max-book-pages", 100);
   }

   public int getMaxBookPageSize() {
      return this.getInt("max-book-page-length", 5000);
   }

   public URL getDefaultConfigURL() {
      return this.getClass().getClassLoader().getResource("assets/viarewind/config.yml");
   }

   protected void handleConfig(Map map) {
   }

   public List getUnsupportedOptions() {
      return Collections.emptyList();
   }
}
