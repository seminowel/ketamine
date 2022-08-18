package io.github.nevalackin.client.impl.config;

import com.google.gson.JsonObject;
import io.github.nevalackin.client.api.config.ConfigManager;
import io.github.nevalackin.client.api.notification.NotificationType;
import io.github.nevalackin.client.impl.core.KetamineClient;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.FilenameUtils;

public final class ConfigManagerImpl implements ConfigManager {
   private final Map configsMap = new HashMap();
   private Config currentlyLoadedConfig;

   public ConfigManagerImpl() {
      this.refresh();
   }

   public void refresh() {
      File configsDir = KetamineClient.getInstance().getFileManager().getFile("configs");
      File[] files = configsDir.listFiles();
      if (files != null) {
         int loadedConfigs = 0;
         File[] var4 = files;
         int var5 = files.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            File file = var4[var6];
            if (FilenameUtils.getExtension(file.getName()).equals(".keta".substring(1))) {
               String config = FilenameUtils.removeExtension(file.getName());
               this.configsMap.put(config, new Config(config));
               ++loadedConfigs;
            }
         }

         KetamineClient.getInstance().getNotificationManager().add(NotificationType.SUCCESS, "Loaded Configs", String.format("Successfully loaded %s config(s).", loadedConfigs), 2000L);
      }

      if (KetamineClient.getInstance().getDropdownGUI() != null) {
         KetamineClient.getInstance().getDropdownGUI().updateConfigs(this.getConfigs());
      }

   }

   public boolean load(String config) {
      Config configObj = this.find(config);
      return this.load(configObj);
   }

   public boolean load(Config config) {
      if (config == null) {
         KetamineClient.getInstance().getNotificationManager().add(NotificationType.ERROR, "Non-Existent Config", "Attempted to load config which does not exist.", 2000L);
         return false;
      } else {
         JsonObject object = KetamineClient.getInstance().getFileManager().parse(config.getFile()).getAsJsonObject();
         if (object == null) {
            KetamineClient.getInstance().getNotificationManager().add(NotificationType.ERROR, "Config Format Error", "Config file format is incorrect, unable to load.", 2000L);
            return false;
         } else {
            config.load(object);
            this.currentlyLoadedConfig = config;
            return true;
         }
      }
   }

   public void save(String config) {
      Config saved;
      if ((saved = this.find(config)) == null) {
         saved = new Config(config);
         this.configsMap.put(config, saved);
      }

      this.save(saved);
   }

   public boolean save(Config config) {
      if (config == null) {
         KetamineClient.getInstance().getNotificationManager().add(NotificationType.ERROR, "Non-Existent Config", "Attempted to save config which does not exist.", 2000L);
         return false;
      } else {
         JsonObject base = new JsonObject();
         config.save(base);
         KetamineClient.getInstance().getFileManager().writeJson(config.getFile(), base);
         KetamineClient.getInstance().getNotificationManager().add(NotificationType.SUCCESS, "Saved Config", String.format("Successfully saved %s", config.getName()), 1000L);
         return true;
      }
   }

   public Config find(String config) {
      if (this.configsMap.containsKey(config)) {
         return (Config)this.configsMap.get(config);
      } else {
         File configsDir = KetamineClient.getInstance().getFileManager().getFile("configs");
         return (new File(configsDir, config + ".keta")).exists() ? new Config(config) : null;
      }
   }

   public boolean delete(String config) {
      Config configObj;
      return (configObj = this.find(config)) != null && this.delete(configObj);
   }

   public boolean delete(Config config) {
      if (config == null) {
         KetamineClient.getInstance().getNotificationManager().add(NotificationType.WARNING, "Non-Existent Config", "Config has either already been deleted or didn't exist.", 3000L);
         return false;
      } else {
         File f = config.getFile();
         this.configsMap.remove(config.getName());
         boolean success = f.exists() && f.delete();
         if (success) {
            KetamineClient.getInstance().getNotificationManager().add(NotificationType.SUCCESS, "Deleted Config", "Successfully deleted config and cleaned up file.", 1000L);
         } else {
            KetamineClient.getInstance().getNotificationManager().add(NotificationType.SUCCESS, "Delete Config Error", "Config file does not exist or insufficient permissions to delete file.", 3000L);
         }

         return success;
      }
   }

   public boolean saveCurrent() {
      return this.save(this.currentlyLoadedConfig);
   }

   public boolean reloadCurrent() {
      return this.load(this.currentlyLoadedConfig);
   }

   public Collection getConfigs() {
      return this.configsMap.values();
   }
}
