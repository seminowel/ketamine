package io.github.nevalackin.client.impl.module.render.self;

import io.github.nevalackin.client.api.module.Category;
import io.github.nevalackin.client.api.module.Module;
import io.github.nevalackin.client.api.property.Property;
import io.github.nevalackin.client.impl.property.DoubleProperty;
import io.github.nevalackin.client.impl.property.EnumProperty;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import org.lwjgl.opengl.GL11;

public final class SwingModifier extends Module {
   private final DoubleProperty xTranslationProperty = new DoubleProperty("X Translate", 0.0, -1.0, 1.0, 0.1);
   private final DoubleProperty yTranslationProperty = new DoubleProperty("Y Translate", 0.0, -1.0, 1.0, 0.1);
   private final DoubleProperty zTranslationProperty = new DoubleProperty("Z Translate", 0.0, -1.0, 1.0, 0.1);
   private final EnumProperty blockTransformProperty;
   private final DoubleProperty swingSpeedModProperty;
   private float spinDeltaTime;
   @EventLink
   private final Listener onGetArmSwingMod;
   @EventLink
   private final Listener onBlockTransform;
   @EventLink
   private final Listener onPreItemRender;

   public SwingModifier() {
      super("Swing Modifier", Category.RENDER, Category.SubCategory.RENDER_SELF);
      this.blockTransformProperty = new EnumProperty("Block Transform", SwingModifier.BlockTransformationType._1_7);
      this.swingSpeedModProperty = new DoubleProperty("Swing Speed Mod", 1.0, 0.0, 5.0, 0.1);
      this.onGetArmSwingMod = (event) -> {
         event.setModifier(((Double)this.swingSpeedModProperty.getValue()).floatValue());
      };
      this.onBlockTransform = (event) -> {
         switch ((BlockTransformationType)this.blockTransformProperty.getValue()) {
            case _1_8:
               event.setReplacementTransform((equipProgress, swingProgress) -> {
                  event.getOriginalTransform().transform(equipProgress, 0.0F);
               });
               break;
            case OLD:
               event.setReplacementTransform((equipProgress, swingProgress) -> {
                  double swingProg = Math.sin(Math.sqrt((double)swingProgress) * Math.PI);
                  GL11.glTranslatef(0.0F, 0.2F, 0.0F);
                  event.getOriginalTransform().transform(equipProgress / 3.0F, 0.0F);
                  GL11.glRotatef((float)swingProg * 30.0F, -1.0F, 0.0F, 0.0F);
               });
               break;
            case ANTH:
               event.setReplacementTransform((equipProgress, swingProgress) -> {
                  double swingProg = Math.sin(Math.sqrt((double)swingProgress) * Math.PI);
                  GL11.glTranslatef(0.1F, 0.1F, 0.0F);
                  GL11.glRotatef((float)swingProg * 20.0F, 0.0F, 1.0F, 0.0F);
                  event.getOriginalTransform().transform(equipProgress / 2.0F, swingProgress);
               });
               break;
            case THOMAZ:
               event.setReplacementTransform((equipProgress, swingProgress) -> {
                  event.getOriginalTransform().transform(equipProgress, swingProgress);
               });
            case NEVALACK:
            default:
               break;
            case POSK:
               event.setReplacementTransform((equipProgress, swingProgress) -> {
                  double swingProg = Math.sin(Math.sqrt((double)swingProgress) * Math.PI);
                  event.getOriginalTransform().transform(equipProgress / 2.0F, 0.0F);
                  GL11.glTranslatef(0.0F, 0.25F, 0.0F);
                  GL11.glRotated(-swingProg * 25.0, swingProg / 4.9, 0.0, 2.0);
                  GL11.glRotated(-swingProg * 30.0, 4.0, swingProg * 1.7, 0.0);
               });
               break;
            case DRAKE:
               event.setReplacementTransform((equipProgress, swingProgress) -> {
                  GL11.glTranslatef(-0.15F, 0.0F, 0.0F);
                  event.getOriginalTransform().transform(equipProgress, 0.0F);
                  GL11.glRotatef(this.spinDeltaTime * 360.0F, 0.0F, 1.0F, 0.0F);
                  GL11.glRotatef(45.0F, 1.0F, -1.0F, 0.0F);
                  this.spinDeltaTime += 0.01F;
                  if (this.spinDeltaTime > 1.0F) {
                     this.spinDeltaTime = 0.0F;
                  }

               });
         }

      };
      this.onPreItemRender = (event) -> {
         GL11.glTranslated((Double)this.xTranslationProperty.getValue(), (Double)this.yTranslationProperty.getValue(), (Double)this.zTranslationProperty.getValue());
      };
      this.register(new Property[]{this.blockTransformProperty, this.xTranslationProperty, this.yTranslationProperty, this.zTranslationProperty, this.swingSpeedModProperty});
   }

   public void onEnable() {
   }

   public void onDisable() {
   }

   private static enum BlockTransformationType {
      _1_8("1.8"),
      _1_7("1.7"),
      ANTH("Anth"),
      THOMAZ("Thomaz"),
      NEVALACK("NevaLack"),
      POSK("Posk"),
      DRAKE("Drake"),
      OLD("Old");

      private final String name;

      private BlockTransformationType(String name) {
         this.name = name;
      }

      public String toString() {
         return this.name;
      }
   }
}
