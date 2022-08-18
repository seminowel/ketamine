package io.github.nevalackin.client.impl.module.render.world;

import io.github.nevalackin.client.api.module.Category;
import io.github.nevalackin.client.api.module.Module;
import io.github.nevalackin.client.api.property.Property;
import io.github.nevalackin.client.impl.property.ColourProperty;

public final class WorldColour extends Module {
   private final ColourProperty colourProperty = new ColourProperty("Colour", -1);

   public WorldColour() {
      super("World Colour", Category.RENDER, Category.SubCategory.RENDER_WORLD);
      this.register(new Property[]{this.colourProperty});
   }

   public void onEnable() {
   }

   public void onDisable() {
   }
}
