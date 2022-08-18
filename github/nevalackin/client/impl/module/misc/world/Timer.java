package io.github.nevalackin.client.impl.module.misc.world;

import io.github.nevalackin.client.api.module.Category;
import io.github.nevalackin.client.api.module.Module;
import io.github.nevalackin.client.api.property.Property;
import io.github.nevalackin.client.impl.property.DoubleProperty;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;

public final class Timer extends Module {
   private final DoubleProperty timerSpeedProperty = new DoubleProperty("Timer Speed", 1.3, 0.1, 3.0, 0.1);
   @EventLink
   private final Listener onGetTimerSpeed = (event) -> {
      event.setTimerSpeed((Double)this.timerSpeedProperty.getValue());
   };

   public Timer() {
      super("Timer", Category.MISC, Category.SubCategory.MISC_WORLD);
      DoubleProperty var10001 = this.timerSpeedProperty;
      this.setSuffix(var10001::getDisplayString);
      this.register(new Property[]{this.timerSpeedProperty});
   }

   public void onEnable() {
   }

   public void onDisable() {
   }
}
