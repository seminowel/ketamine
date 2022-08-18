package io.github.nevalackin.client.impl.config;

import com.google.gson.JsonObject;
import io.github.nevalackin.client.api.config.Serializable;
import io.github.nevalackin.client.api.module.Module;
import io.github.nevalackin.client.impl.core.KetamineClient;
import io.github.nevalackin.client.util.render.ColourUtil;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public final class Config implements Serializable {
   private final String name;
   private final File file;

   public Config(String name) {
      this.name = name;
      this.file = new File(KetamineClient.getInstance().getFileManager().getFile("configs"), name + ".keta");
      if (!this.file.exists()) {
         try {
            boolean var2 = this.file.createNewFile();
         } catch (IOException var3) {
         }
      }

   }

   public void load(JsonObject object) {
      if (object.has("dateModified")) {
      }

      if (object.has("clientColour")) {
         ColourUtil.setClientColour(object.get("clientColour").getAsInt());
      }

      if (object.has("secondaryColour")) {
         ColourUtil.setSecondaryColour(object.get("secondaryColour").getAsInt());
      }

      if (object.has("modules")) {
         JsonObject modulesObject = object.getAsJsonObject("modules");
         Iterator var3 = KetamineClient.getInstance().getModuleManager().getModules().iterator();

         while(var3.hasNext()) {
            Module module = (Module)var3.next();
            if (modulesObject.has(module.getName())) {
               module.load(modulesObject.getAsJsonObject(module.getName()));
            }
         }
      }

   }

   public void save(JsonObject object) {
      object.addProperty("dateModified", System.currentTimeMillis());
      object.addProperty("clientColour", ColourUtil.getClientColour());
      object.addProperty("secondaryColour", ColourUtil.getSecondaryColour());
      JsonObject modulesObject = new JsonObject();
      Iterator var3 = KetamineClient.getInstance().getModuleManager().getModules().iterator();

      while(var3.hasNext()) {
         Module module = (Module)var3.next();
         module.save(modulesObject);
      }

      object.add("modules", modulesObject);
   }

   public String getName() {
      return this.name;
   }

   public File getFile() {
      return this.file;
   }
}
