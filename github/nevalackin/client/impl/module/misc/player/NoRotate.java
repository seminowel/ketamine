package io.github.nevalackin.client.impl.module.misc.player;

import io.github.nevalackin.client.api.module.Category;
import io.github.nevalackin.client.api.module.Module;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;

public final class NoRotate extends Module {
   @EventLink
   private final Listener onSetPosLook = (event) -> {
      event.setYaw(this.mc.thePlayer.rotationYaw);
      event.setPitch(this.mc.thePlayer.rotationPitch);
   };

   public NoRotate() {
      super("No Rotate", Category.MISC, Category.SubCategory.MISC_PLAYER);
   }

   public void onEnable() {
   }

   public void onDisable() {
   }
}
