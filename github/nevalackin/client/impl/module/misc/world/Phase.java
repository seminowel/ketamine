package io.github.nevalackin.client.impl.module.misc.world;

import io.github.nevalackin.client.api.module.Category;
import io.github.nevalackin.client.api.module.Module;

public class Phase extends Module {
   public Phase() {
      super("Phase", Category.MISC, Category.SubCategory.MISC_WORLD);
   }

   public void onEnable() {
      this.mc.thePlayer.setPosition(this.mc.thePlayer.posX, this.mc.thePlayer.posY - 5.0 - Math.random(), this.mc.thePlayer.posZ);
      this.setEnabled(false);
   }

   public void onDisable() {
   }
}
