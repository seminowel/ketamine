package io.github.nevalackin.client.impl.module.render.self;

import io.github.nevalackin.client.api.module.Category;
import io.github.nevalackin.client.api.module.Module;
import io.github.nevalackin.client.api.property.Property;
import io.github.nevalackin.client.impl.property.BooleanProperty;
import io.github.nevalackin.client.impl.property.DoubleProperty;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import org.lwjgl.input.Mouse;

public final class ThirdPerson extends Module {
   private final BooleanProperty onKeyProperty = new BooleanProperty("On Middle Click", true);
   private final BooleanProperty viewClipProperty = new BooleanProperty("View Clip", true);
   private final DoubleProperty distanceProperty = new DoubleProperty("Distance", 7.0, 1.0, 20.0, 0.5);
   @EventLink
   private final Listener onOrientCamera = (event) -> {
      if ((Boolean)this.onKeyProperty.getValue() && Mouse.isButtonDown(2)) {
         event.setThirdPersonView(1);
      }

   };
   @EventLink
   private final Listener onRayTrace = (event) -> {
      event.setDistance((Double)this.distanceProperty.getValue());
      if ((Boolean)this.viewClipProperty.getValue()) {
         event.setCancelled();
      }

   };

   public ThirdPerson() {
      super("Thirdperson", Category.RENDER, Category.SubCategory.RENDER_SELF);
      this.register(new Property[]{this.viewClipProperty, this.onKeyProperty, this.distanceProperty});
   }

   public void onEnable() {
   }

   public void onDisable() {
   }

   private static enum DrawServerRotations {
      OFF("Off"),
      MODEL("Model"),
      GHOST("Ghost");

      private final String name;

      private DrawServerRotations(String name) {
         this.name = name;
      }

      public String toString() {
         return this.name;
      }
   }
}
