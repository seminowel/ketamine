package io.github.nevalackin.client.impl.script;

import io.github.nevalackin.client.impl.core.KetamineClient;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class Script {
   private static final ScriptEngineManager MANAGER = new ScriptEngineManager();
   private final ScriptEngine engine;
   private final Invocable invocable;
   private final String name;
   private final String version;
   private final String author;
   private boolean enabled;
   private boolean invalid;
   @EventLink
   private final Listener onUpdatePosition = (event) -> {
      this.callFunc("onUpdate", event);
   };
   @EventLink
   private final Listener onRenderGameOverlay = (event) -> {
      this.callFunc("onRender", event);
   };

   public Script(String source) {
      this.engine = MANAGER.getEngineByName("nashorn");
      this.engine.put("script", this);

      try {
         this.engine.eval(source);
      } catch (ScriptException var3) {
         this.invalid = true;
      }

      this.invocable = (Invocable)this.engine;
      this.name = (String)this.engine.get("name");
      this.version = (String)this.engine.get("version");
      this.author = (String)this.engine.get("author");
      this.callFunc("onLoad");
   }

   public String getName() {
      return this.name;
   }

   public String getVersion() {
      return this.version;
   }

   public String getAuthor() {
      return this.author;
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public void setEnabled(boolean enabled) {
      if (!this.invalid) {
         this.enabled = enabled;
         if (enabled) {
            this.callFunc("onEnable");
            KetamineClient.getInstance().getEventBus().subscribe(this);
         } else {
            KetamineClient.getInstance().getEventBus().unsubscribe(this);
            this.callFunc("onDisable");
         }

      }
   }

   private void callFunc(String funcName, Object... args) {
      try {
         this.invocable.invokeFunction(funcName, args);
      } catch (NoSuchMethodException var4) {
      } catch (Exception var5) {
         this.invalid = true;
      }

   }
}
