package io.github.nevalackin.client.impl.module.render.overlay;

import io.github.nevalackin.client.api.module.Category;
import io.github.nevalackin.client.api.module.Module;
import io.github.nevalackin.client.api.property.Property;
import io.github.nevalackin.client.impl.property.BooleanProperty;
import io.github.nevalackin.client.impl.property.ColourProperty;
import io.github.nevalackin.client.impl.property.DoubleProperty;
import io.github.nevalackin.client.util.render.ColourUtil;
import io.github.nevalackin.client.util.render.DrawUtil;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;

public final class Crosshair extends Module {
   private final BooleanProperty tShapeProperty = new BooleanProperty("T Shape", false);
   private final BooleanProperty dotProperty = new BooleanProperty("Dot", true);
   private final BooleanProperty dynamicProperty = new BooleanProperty("Dynamic", false);
   private final DoubleProperty lengthProperty = new DoubleProperty("Length", 3.0, 0.0, 20.0, 0.5);
   private final BooleanProperty fadeOutProperty = new BooleanProperty("Fade Out", true);
   private final DoubleProperty gapProperty = new DoubleProperty("Gap", 2.0, 0.0, 10.0, 0.5);
   private final DoubleProperty widthProperty = new DoubleProperty("Width", 1.0, 0.0, 5.0, 0.5);
   private final DoubleProperty outlineWidthProperty = new DoubleProperty("Outline Width", 0.5, 0.0, 1.0, 0.5);
   private final ColourProperty colourProperty = new ColourProperty("Colour", ColourUtil.getClientColour());
   private double lastDist;
   private double prevLastDist;
   @EventLink
   private final Listener onUpdate = (event) -> {
      if (event.isPre()) {
         double xDist = event.getLastTickPosX() - event.getPosX();
         double zDist = event.getLastTickPosZ() - event.getPosZ();
         this.prevLastDist = this.lastDist;
         this.lastDist = Math.sqrt(xDist * xDist + zDist * zDist);
      }

   };
   @EventLink
   private final Listener onRenderGameOverlay = (event) -> {
      event.setRenderCrossHair(false);
      ScaledResolution scaledResolution = event.getScaledResolution();
      double width = (Double)this.widthProperty.getValue();
      double halfWidth = width / 2.0;
      double gap = (Double)this.gapProperty.getValue();
      if ((Boolean)this.dynamicProperty.getValue()) {
         gap *= Math.max(this.mc.thePlayer.isSneaking() ? 0.5 : 1.0, DrawUtil.interpolate(this.prevLastDist, this.lastDist, (double)event.getPartialTicks()) * 10.0);
      }

      double length = (Double)this.lengthProperty.getValue();
      int color = (Integer)this.colourProperty.getValue();
      double outlineWidth = (Double)this.outlineWidthProperty.getValue();
      boolean outline = outlineWidth > 0.0;
      boolean tShape = (Boolean)this.tShapeProperty.getValue();
      double middleX = (double)scaledResolution.getScaledWidth() / 2.0;
      double middleY = (double)scaledResolution.getScaledHeight() / 2.0;
      if (outline) {
         DrawUtil.glDrawFilledRect(middleX - gap - length - outlineWidth, middleY - halfWidth - outlineWidth, middleX - gap + outlineWidth, middleY + halfWidth + outlineWidth, -1778384896);
         DrawUtil.glDrawFilledRect(middleX + gap - outlineWidth, middleY - halfWidth - outlineWidth, middleX + gap + length + outlineWidth, middleY + halfWidth + outlineWidth, -1778384896);
         DrawUtil.glDrawFilledRect(middleX - halfWidth - outlineWidth, middleY + gap - outlineWidth, middleX + halfWidth + outlineWidth, middleY + gap + length + outlineWidth, -1778384896);
         if (!tShape) {
            DrawUtil.glDrawFilledRect(middleX - halfWidth - outlineWidth, middleY - gap - length - outlineWidth, middleX + halfWidth + outlineWidth, middleY - gap + outlineWidth, -1778384896);
         }
      }

      int clear = ColourUtil.removeAlphaComponent(color);
      GL11.glDisable(3008);
      DrawLine drawLine = (x, y, x1, y1) -> {
         if ((Boolean)this.fadeOutProperty.getValue()) {
            boolean horizontal = y1 - y <= width;
            boolean inverted = x > middleX || y > middleY;
            int startColour = inverted ? color : clear;
            int endColour = inverted ? clear : color;
            if (horizontal) {
               DrawUtil.glDrawSidewaysGradientRect(x, y, x1 - x, y1 - y, startColour, endColour);
            } else {
               DrawUtil.glDrawFilledRect(x, y, x1, y1, startColour, endColour);
            }
         } else {
            DrawUtil.glDrawFilledRect(x, y, x1, y1, color);
         }

      };
      drawLine.draw(middleX - gap - length, middleY - halfWidth, middleX - gap, middleY + halfWidth);
      drawLine.draw(middleX + gap, middleY - halfWidth, middleX + gap + length, middleY + halfWidth);
      drawLine.draw(middleX - halfWidth, middleY + gap, middleX + halfWidth, middleY + gap + length);
      if (!tShape) {
         drawLine.draw(middleX - halfWidth, middleY - gap - length, middleX + halfWidth, middleY - gap);
      }

      GL11.glEnable(3008);
      if ((Boolean)this.dotProperty.getValue()) {
         if (outline) {
            DrawUtil.glDrawFilledRect(middleX - halfWidth - outlineWidth, middleY - halfWidth - outlineWidth, middleX + halfWidth + outlineWidth, middleY + halfWidth + outlineWidth, -1778384896);
         }

         DrawUtil.glDrawFilledRect(middleX - halfWidth, middleY - halfWidth, middleX + halfWidth, middleY + halfWidth, color);
      }

   };

   public Crosshair() {
      super("Crosshair", Category.RENDER, Category.SubCategory.RENDER_OVERLAY);
      this.register(new Property[]{this.tShapeProperty, this.dotProperty, this.dynamicProperty, this.lengthProperty, this.gapProperty, this.widthProperty, this.outlineWidthProperty, this.colourProperty, this.fadeOutProperty});
   }

   public void onEnable() {
   }

   public void onDisable() {
   }

   @FunctionalInterface
   private interface DrawLine {
      void draw(double var1, double var3, double var5, double var7);
   }
}
