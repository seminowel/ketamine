package io.github.nevalackin.client.impl.module.render.world;

import io.github.nevalackin.client.api.module.Category;
import io.github.nevalackin.client.api.module.Module;
import io.github.nevalackin.client.api.property.Property;
import io.github.nevalackin.client.impl.property.BooleanProperty;
import io.github.nevalackin.client.impl.property.ColourProperty;
import io.github.nevalackin.client.util.render.ColourUtil;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;

public final class BlockOverlay extends Module {
   private final ColourProperty colourProperty = new ColourProperty("Colour", ColourUtil.overwriteAlphaComponent(ColourUtil.getClientColour(), 80));
   private final BooleanProperty outlineProperty = new BooleanProperty("Outline", true);
   @EventLink
   private final Listener onDrawSelectedBB = (event) -> {
      event.setFilled(true);
      event.setOutlineWidth((Boolean)this.outlineProperty.getValue() ? 1.0F : 0.0F);
      event.setColour((Integer)this.colourProperty.getValue());
   };

   public BlockOverlay() {
      super("Block Overlay", Category.RENDER, Category.SubCategory.RENDER_WORLD);
      this.register(new Property[]{this.colourProperty, this.outlineProperty});
   }

   public void onEnable() {
   }

   public void onDisable() {
   }
}
