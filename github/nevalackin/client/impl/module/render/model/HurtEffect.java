package io.github.nevalackin.client.impl.module.render.model;

import io.github.nevalackin.client.api.module.Category;
import io.github.nevalackin.client.api.module.Module;
import io.github.nevalackin.client.api.property.Property;
import io.github.nevalackin.client.impl.event.render.model.ApplyHurtEffectEvent;
import io.github.nevalackin.client.impl.property.BooleanProperty;
import io.github.nevalackin.client.impl.property.ColourProperty;
import io.github.nevalackin.client.impl.property.EnumProperty;
import io.github.nevalackin.client.util.render.ColourUtil;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;

public final class HurtEffect extends Module {
   private final EnumProperty modeProperty;
   private final BooleanProperty healthBasedColourProperty;
   private final ColourProperty colourProperty;
   @EventLink
   private final Listener onApplyHurtEvent;

   public HurtEffect() {
      super("Hurt Effect", Category.RENDER, Category.SubCategory.RENDER_MODEL);
      this.modeProperty = new EnumProperty("Mode", HurtEffect.Mode.BOTH);
      this.healthBasedColourProperty = new BooleanProperty("Health Based Colour", true);
      this.colourProperty = new ColourProperty("Colour", 1308557312, () -> {
         return this.modeProperty.getValue() != HurtEffect.Mode.NONE && !(Boolean)this.healthBasedColourProperty.getValue();
      });
      this.onApplyHurtEvent = (event) -> {
         event.setHurtColour((Boolean)this.healthBasedColourProperty.getValue() ? ColourUtil.overwriteAlphaComponent(ColourUtil.blendHealthColours((double)(event.getEntity().getHealth() / event.getEntity().getMaxHealth())), 77) : (Integer)this.colourProperty.getValue());
         switch ((Mode)this.modeProperty.getValue()) {
            case NONE:
               event.setPreRenderModelCallback(ApplyHurtEffectEvent.RenderCallbackFunc.NONE);
               break;
            case BOTH:
               event.setPreRenderLayersCallback(ApplyHurtEffectEvent.RenderCallbackFunc.NONE);
               break;
            case ARMOR_ONLY:
               event.setPreRenderModelCallback(ApplyHurtEffectEvent.RenderCallbackFunc.NONE);
               event.setPreRenderLayersCallback(ApplyHurtEffectEvent.RenderCallbackFunc.SET);
         }

      };
      this.register(new Property[]{this.modeProperty, this.healthBasedColourProperty, this.colourProperty});
   }

   public void onEnable() {
   }

   public void onDisable() {
   }

   private static enum Mode {
      ARMOR_ONLY("Armour Only"),
      MODEL_ONLY("Model Only"),
      BOTH("Both"),
      NONE("None");

      private final String name;

      private Mode(String name) {
         this.name = name;
      }

      public String toString() {
         return this.name;
      }
   }
}
