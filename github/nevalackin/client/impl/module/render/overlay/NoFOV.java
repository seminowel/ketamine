package io.github.nevalackin.client.impl.module.render.overlay;

import io.github.nevalackin.client.api.module.Category;
import io.github.nevalackin.client.api.module.Module;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;

public final class NoFOV extends Module {
   @EventLink
   private final Listener onGetFOV = (event) -> {
      event.setUseModifier(false);
   };

   public NoFOV() {
      super("No FOV", Category.RENDER, Category.SubCategory.RENDER_OVERLAY);
   }

   public void onEnable() {
   }

   public void onDisable() {
   }
}
