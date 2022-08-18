package io.github.nevalackin.client.impl.module.render.esp.esp.components;

import io.github.nevalackin.client.api.ui.cfont.CustomFontRenderer;
import io.github.nevalackin.client.impl.module.render.esp.esp.EnumPosition;
import io.github.nevalackin.client.util.render.ColourUtil;
import io.github.nevalackin.client.util.render.DrawUtil;
import java.util.function.Supplier;
import org.lwjgl.opengl.GL11;

public final class Bar {
   private final double thickness;
   private final EnumPosition position;
   private final boolean drawBackground;
   private final boolean drawPercentageText;
   private final int colour;
   private final int secondaryColour;
   private final boolean gradient;
   private final Supplier percentageSupplier;

   public Bar(double thickness, EnumPosition position, boolean drawBackground, boolean drawPercentageText, int colour, int secondaryColour, boolean gradient, Supplier percentageSupplier) {
      this.thickness = thickness;
      this.position = position;
      this.drawBackground = drawBackground;
      this.drawPercentageText = drawPercentageText;
      this.colour = colour;
      this.secondaryColour = secondaryColour;
      this.gradient = gradient;
      this.percentageSupplier = percentageSupplier;
   }

   public void draw(CustomFontRenderer renderer, double[] boundingBox, double[] offsets) {
      double left = boundingBox[0];
      double top = boundingBox[1];
      double right = boundingBox[2];
      double bottom = boundingBox[3];
      double lowerRight = boundingBox[4];
      double width = right - left;
      double height = bottom - top;
      double percentage = this.getPercentage();
      EnumPosition side = this.getPosition();
      double thickness = this.getThickness();
      double totalThickness = 1.0 + thickness;
      double bLeft = side.isUseRightAsLeft() ? right : left;
      double bTop = side.isUseBottomAsTop() ? bottom : top;
      if (side.isDrawHorizontalBar()) {
         bTop += side.isUseBottomAsTop() ? offsets[side.ordinal()] : -(offsets[side.ordinal()] + totalThickness);
      } else {
         bLeft += side.isUseRightAsLeft() ? offsets[side.ordinal()] + totalThickness : -(offsets[side.ordinal()] + totalThickness);
      }

      int var10001 = side.ordinal();
      offsets[var10001] += totalThickness + 0.5;
      boolean drawText = this.drawPercentageText && percentage < 1.0;
      double xText = 0.0;
      double yText = 0.0;
      double filledWidth;
      if (side.isDrawHorizontalBar()) {
         filledWidth = (width - 1.0) * percentage;
         if (drawText) {
            xText = filledWidth;
            yText = bTop + thickness / 2.0;
         }

         if (this.drawBackground) {
            DrawUtil.glDrawFilledQuad(bLeft, bTop, width, totalThickness, -1778384896);
         }

         this.drawBar(bLeft + 0.5, bTop + 0.5, filledWidth, thickness, percentage);
      } else {
         filledWidth = (height - 1.0) * percentage;
         double healthHeight = bTop + height - 0.5 - filledWidth;
         if (drawText) {
            xText = bLeft + thickness / 2.0;
            yText = healthHeight - 9.0;
         }

         if (this.drawBackground) {
            DrawUtil.glDrawFilledQuad(bLeft, bTop, totalThickness, height, -1778384896);
         }

         this.drawBar(bLeft + 0.5, healthHeight, thickness, filledWidth, percentage);
      }

      if (drawText) {
         String text = String.valueOf((int)(percentage * 100.0));
         if (!side.isDrawHorizontalBar()) {
            xText -= renderer.getWidth(text) / 2.0;
         }

         GL11.glTranslated(0.0, 0.0, 3.0);
         renderer.draw(text, xText, yText, 0.2, -1);
         GL11.glTranslated(0.0, 0.0, -3.0);
      }

   }

   private void drawBar(double x, double y, double width, double height, double percentage) {
      if (this.gradient) {
         boolean restore = DrawUtil.glEnableBlend();
         GL11.glDisable(3553);
         int topColour = ColourUtil.fadeBetween(this.secondaryColour, this.colour, percentage);
         GL11.glShadeModel(7425);
         GL11.glBegin(7);
         if (this.position.isDrawHorizontalBar()) {
            DrawUtil.glColour(topColour);
            GL11.glVertex2d(x, y);
            GL11.glVertex2d(x, y + height);
            DrawUtil.glColour(this.secondaryColour);
            GL11.glVertex2d(x + width, y + height);
            GL11.glVertex2d(x + width, y);
         } else {
            DrawUtil.glColour(topColour);
            GL11.glVertex2d(x, y);
            DrawUtil.glColour(this.secondaryColour);
            GL11.glVertex2d(x, y + height);
            GL11.glVertex2d(x + width, y + height);
            DrawUtil.glColour(topColour);
            GL11.glVertex2d(x + width, y);
         }

         GL11.glEnd();
         GL11.glShadeModel(7424);
         DrawUtil.glRestoreBlend(restore);
         GL11.glEnable(3553);
      } else {
         DrawUtil.glDrawFilledQuad(x, y, width, height, this.colour);
      }

   }

   public double getThickness() {
      return this.thickness;
   }

   public EnumPosition getPosition() {
      return this.position;
   }

   public double getPercentage() {
      return (Double)this.percentageSupplier.get();
   }
}
