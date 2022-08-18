package io.github.nevalackin.client.impl.binding;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.nevalackin.client.api.binding.Bind;
import io.github.nevalackin.client.api.binding.BindManager;
import io.github.nevalackin.client.api.binding.Bindable;
import io.github.nevalackin.client.api.file.FileManager;
import io.github.nevalackin.client.impl.core.KetamineClient;
import io.github.nevalackin.client.impl.event.game.input.State;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class BindManagerImpl implements BindManager {
   private final Map binds = new HashMap();
   @EventLink
   private final Listener onInput = (event) -> {
      Iterator var2 = this.binds.entrySet().iterator();

      while(var2.hasNext()) {
         Map.Entry bindsEntry = (Map.Entry)var2.next();
         Bind bind = (Bind)bindsEntry.getValue();
         if (bind != null && bind.getInputType() == event.getType() && bind.getCode() == event.getButton()) {
            Bindable bindable = (Bindable)bindsEntry.getKey();
            switch (bind.getBindType()) {
               case TOGGLE:
                  if (event.getState() == State.PRESSED) {
                     bindable.toggle();
                  }
                  break;
               case HOLD:
                  switch (event.getState()) {
                     case PRESSED:
                        bindable.setActive(true);
                        break;
                     case RELEASED:
                        bindable.setActive(false);
                  }
            }
         }
      }

   };

   public BindManagerImpl() {
      KetamineClient.getInstance().getEventBus().subscribe(this);
   }

   public void save() {
      FileManager fileManager = KetamineClient.getInstance().getFileManager();
      File bindsFile = fileManager.getFile("binds");
      JsonObject bindsObject = new JsonObject();
      Iterator var4 = this.getBinds().keySet().iterator();

      while(var4.hasNext()) {
         Bindable bindable = (Bindable)var4.next();
         bindable.saveBind(bindsObject);
      }

      fileManager.writeJson(bindsFile, bindsObject);
   }

   public void load() {
      FileManager fileManager = KetamineClient.getInstance().getFileManager();
      File bindsFile = fileManager.getFile("binds");
      JsonElement object = fileManager.parse(bindsFile);
      if (object.isJsonObject()) {
         Iterator var4 = object.getAsJsonObject().entrySet().iterator();

         while(var4.hasNext()) {
            Map.Entry entry = (Map.Entry)var4.next();
            if (((JsonElement)entry.getValue()).isJsonObject()) {
               this.getBinds().keySet().stream().filter((b) -> {
                  return b.getName().equals(entry.getKey());
               }).forEach((b) -> {
                  b.loadBind(((JsonElement)entry.getValue()).getAsJsonObject());
               });
            }
         }
      }

   }

   public Map getBinds() {
      return this.binds;
   }
}
