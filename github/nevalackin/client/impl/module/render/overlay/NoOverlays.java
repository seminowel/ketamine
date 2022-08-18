package io.github.nevalackin.client.impl.module.render.overlay;

import io.github.nevalackin.client.api.module.Category;
import io.github.nevalackin.client.api.module.Module;
import io.github.nevalackin.client.api.property.Property;
import io.github.nevalackin.client.impl.property.BooleanProperty;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;

public final class NoOverlays extends Module {
   private final BooleanProperty bossHealthBarProperty = new BooleanProperty("Boss Health Bar", false);
   @EventLink
   private final Listener onRender = (event) -> {
      event.setRenderBossHealth((Boolean)this.bossHealthBarProperty.getValue());
   };

   public NoOverlays() {
      super("No Overlays", Category.RENDER, Category.SubCategory.RENDER_OVERLAY);
      this.register(new Property[]{this.bossHealthBarProperty});
   }

   public void onEnable() {
   }

   public void onDisable() {
   }
}
