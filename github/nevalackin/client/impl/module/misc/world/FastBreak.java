package io.github.nevalackin.client.impl.module.misc.world;

import io.github.nevalackin.client.api.module.Category;
import io.github.nevalackin.client.api.module.Module;
import io.github.nevalackin.client.api.property.Property;
import io.github.nevalackin.client.impl.property.BooleanProperty;
import io.github.nevalackin.client.impl.property.DoubleProperty;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;

public final class FastBreak extends Module {
   private final DoubleProperty breakPercentageProperty = new DoubleProperty("Break Percentage", 70.0, 1.0, 100.0, 1.0);
   private final BooleanProperty noHitDelayProperty = new BooleanProperty("No Hit Delay", true);
   @EventLink
   private final Listener onPlayerDamageBlock = (event) -> {
      if (this.mc.playerController.curBlockDamageMP >= ((Double)this.breakPercentageProperty.getValue()).floatValue() / 100.0F) {
         this.mc.playerController.curBlockDamageMP = 1.0F;
      }

   };
   @EventLink
   private final Listener onUpdate = (event) -> {
      if (event.isPre() && (Boolean)this.noHitDelayProperty.getValue()) {
         this.mc.playerController.setBlockHitDelay(0);
      }

   };

   public FastBreak() {
      super("Fast Break", Category.MISC, Category.SubCategory.MISC_WORLD);
      this.register(new Property[]{this.breakPercentageProperty, this.noHitDelayProperty});
   }

   public void onEnable() {
   }

   public void onDisable() {
   }
}
