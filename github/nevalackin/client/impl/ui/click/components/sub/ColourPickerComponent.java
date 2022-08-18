package io.github.nevalackin.client.impl.ui.click.components.sub;

import io.github.nevalackin.client.api.ui.framework.Component;
import io.github.nevalackin.client.api.ui.framework.ExpandState;
import io.github.nevalackin.client.api.ui.framework.Expandable;
import io.github.nevalackin.client.api.ui.framework.Predicated;
import io.github.nevalackin.client.impl.property.ColourProperty;
import io.github.nevalackin.client.util.render.ColourUtil;
import io.github.nevalackin.client.util.render.DrawUtil;
import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;

public final class ColourPickerComponent extends Component implements Expandable, Predicated {
   private final ColourProperty property;
   private ExpandState state;
   private double progress;
   private boolean colorSelectorDragging;
   private boolean alphaBarDragging;
   private boolean hueBarDragging;
   private final boolean last;

   public ColourPickerComponent(Component parent, ColourProperty property, double x, double y, double width, double height, boolean last) {
      super(parent, x, y, width, height);
      this.state = ExpandState.CLOSED;
      this.property = property;
      this.last = last;
   }

   public void onDraw(ScaledResolution scaledResolution, int mouseX, int mouseY) {
      double x = this.getX();
      double y = this.getY();
      double w = this.getWidth();
      double h = this.getHeight();
      int colour = (Integer)this.property.getValue();
      double expandedHeight = this.calculateExpandedHeight();
      switch (this.getState()) {
         case CONTRACTING:
            if (this.progress <= 0.0) {
               this.setState(ExpandState.CLOSED);
            } else {
               this.progress -= 1.0 / (double)Minecraft.getDebugFPS() * 5.0;
            }
            break;
         case EXPANDING:
            if (this.progress >= 1.0) {
               this.setState(ExpandState.EXPANDED);
            } else {
               this.progress += 1.0 / (double)Minecraft.getDebugFPS() * 5.0;
            }
      }

      FONT_RENDERER.draw(this.property.getName(), x + 4.0, y + h / 2.0 - 4.0, -1315861);
      DrawUtil.glDrawFilledQuad(x + w - 4.0 - 12.0 - 0.5, y + h / 2.0 - 2.5 - 0.5, 13.0, 7.0, -16777216);
      drawCheckeredBackground(x + w - 4.0 - 12.0, y + h / 2.0 - 2.5, 2.0, 6, 3);
      DrawUtil.glDrawFilledQuad(x + w - 4.0 - 12.0, y + h / 2.0 - 2.5, 12.0, 6.0, colour);
      if (this.isExpanded()) {
         double ex = this.getExpandedX();
         double ey = this.getExpandedY();
         double ew = this.getExpandedWidth();
         double eh = expandedHeight * this.progress;
         if (this.last) {
            DrawUtil.glDrawRoundedQuad(ex, ey, (float)ew, (float)eh, 3.0F, -1726342885);
         } else {
            DrawUtil.glDrawFilledQuad(ex, ey, ew, eh, -1726342885);
         }

         boolean needScissor = this.progress != 1.0;
         if (needScissor) {
            this.glScissorBox(ex, ey, ew, eh, scaledResolution);
         }

         double cpX = ex + 4.0;
         double cpY = ey + 4.0;
         double alphaBarHeight = 12.0;
         double hueSliderWidth = 10.0;
         double cpWidth = this.getWidth() - 12.0 - 10.0;
         double cpHeight = expandedHeight - 12.0 - 12.0;
         double alphaBarY = cpY + cpHeight + 4.0;
         double hueBarX = cpX + cpWidth + 4.0;
         Color color = this.property.getColour();
         float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), (float[])null);
         float hue;
         if (this.colorSelectorDragging) {
            hue = (float)(((double)mouseX - cpX) / cpWidth);
            float brightness = 1.0F - (float)(((double)mouseY - cpY) / cpHeight);
            this.property.setValue(ColourUtil.overwriteAlphaComponent(Color.HSBtoRGB(hsb[0], Math.max(0.0F, Math.min(1.0F, hue)), Math.max(0.0F, Math.min(1.0F, brightness))), colour >> 24 & 255));
            this.colorSelectorDragging = (double)mouseX > cpX && (double)mouseY > cpY && (double)mouseX < cpX + cpWidth && (double)mouseY < cpY + cpHeight;
         }

         DrawUtil.glDrawFilledQuad(cpX - 0.5, cpY - 0.5, cpWidth + 1.0, cpHeight + 1.0, -16777216);
         drawColourPicker(cpX, cpY, cpWidth, cpHeight, hsb[0]);
         GL11.glPushMatrix();
         GL11.glTranslated(cpX + (double)hsb[1] * cpWidth - 1.0, cpY + (double)(1.0F - hsb[2]) * cpHeight - 1.0, 0.0);
         DrawUtil.glDrawFilledQuad(0.0, 0.0, 2.0, 2.0, -2130706433);
         DrawUtil.glDrawOutlinedQuad(0.0, 0.0, 2.0, 2.0, 1.0F, -16777216);
         GL11.glPopMatrix();
         if (this.alphaBarDragging) {
            int alpha = (int)(((double)mouseX - cpX) / cpWidth * 255.0);
            this.property.setValue(new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.min(255, Math.max(0, alpha))));
            this.alphaBarDragging = (double)mouseX > cpX && (double)mouseY > alphaBarY && (double)mouseX < cpX + cpWidth && (double)mouseY < alphaBarY + 12.0;
         }

         DrawUtil.glDrawFilledQuad(cpX - 0.5, cpY + cpHeight + 4.0 - 0.5, cpWidth + 1.0, 13.0, -16777216);
         drawCheckeredBackground(cpX, cpY + cpHeight + 4.0, 4.0, (int)(cpWidth / 4.0), 3);
         DrawUtil.glDrawSidewaysGradientRect(cpX, cpY + cpHeight + 4.0, cpWidth, 12.0, 0, colour | -16777216);
         DrawUtil.glDrawOutlinedQuad(cpX + (double)((float)(colour >> 24 & 255) / 255.0F) * cpWidth - 1.0, cpY + cpHeight + 4.0 - 0.5, 1.0, 12.5, 1.0F, -16777216);
         if (this.hueBarDragging) {
            hue = (float)(((double)mouseY - cpY) / cpHeight);
            this.property.setValue(ColourUtil.overwriteAlphaComponent(Color.HSBtoRGB(hue, hsb[1], hsb[2]), colour >> 24 & 255));
            this.hueBarDragging = (double)mouseX > hueBarX && (double)mouseY > cpY && (double)mouseX < hueBarX + 10.0 && (double)mouseY < cpY + cpHeight;
         }

         DrawUtil.glDrawFilledQuad(cpX + cpWidth + 4.0 - 0.5, cpY - 0.5, 11.0, cpHeight + 1.0, -16777216);
         drawHueSlider(cpX + cpWidth + 4.0, cpY, 10.0, cpHeight);
         DrawUtil.glDrawOutlinedQuad(cpX + cpWidth + 4.0, cpY + (double)hsb[0] * cpHeight - 1.0, 10.5, 1.0, 0.5F, -16777216);
         if (needScissor) {
            DrawUtil.glRestoreScissor();
         }
      }

   }

   private static void drawCheckeredBackground(double x, double y, double sqWidth, int sqsWide, int sqsHigh) {
      DrawUtil.glDrawFilledQuad(x, y, sqWidth * (double)sqsWide, sqWidth * (double)sqsHigh, -1);
      boolean restore = DrawUtil.glEnableBlend();
      GL11.glDisable(3553);
      GL11.glTranslated(x, y, 0.0);
      DrawUtil.glColour(-4210753);
      GL11.glBegin(7);
      double sqX = 0.0;
      double sqY = 0.0;

      for(int j = 0; j < sqsHigh; ++j) {
         for(int i = 0; i < sqsWide / 2; ++i) {
            GL11.glVertex2d(sqX, sqY);
            GL11.glVertex2d(sqX, sqY + sqWidth);
            GL11.glVertex2d(sqX + sqWidth, sqY + sqWidth);
            GL11.glVertex2d(sqX + sqWidth, sqY);
            sqX += sqWidth * 2.0;
         }

         sqX = j % 2 == 0 ? sqWidth : 0.0;
         sqY += sqWidth;
      }

      GL11.glEnd();
      GL11.glTranslated(-x, -y, 0.0);
      DrawUtil.glRestoreBlend(restore);
      GL11.glEnable(3553);
   }

   private static void drawColourPicker(double x, double y, double width, double height, float hue) {
      DrawUtil.glDrawSidewaysGradientRect(x, y, width, height, -1, Color.HSBtoRGB(hue, 1.0F, 1.0F));
      DrawUtil.glDrawFilledQuad(x, y, width, height, 0, -16777216);
   }

   private static void drawHueSlider(double x, double y, double width, double height) {
      boolean restore = DrawUtil.glEnableBlend();
      GL11.glDisable(3553);
      GL11.glTranslated(x, y, 0.0);
      GL11.glShadeModel(7425);
      int[] colours = new int[]{-65536, -256, -16711936, -16711681, -16776961, -65281, -65536};
      double segment = height / (double)colours.length;
      GL11.glBegin(7);

      for(int i = 0; i < colours.length; ++i) {
         int colour = colours[i];
         int top = i != 0 ? ColourUtil.fadeBetween(colours[i - 1], colour, 0.5) : colour;
         int bottom = i + 1 < colours.length ? ColourUtil.fadeBetween(colour, colours[i + 1], 0.5) : colour;
         double start = segment * (double)i;
         DrawUtil.glColour(top);
         GL11.glVertex2d(0.0, start);
         DrawUtil.glColour(bottom);
         GL11.glVertex2d(0.0, start + segment);
         GL11.glVertex2d(width, start + segment);
         DrawUtil.glColour(top);
         GL11.glVertex2d(width, start);
      }

      GL11.glEnd();
      GL11.glShadeModel(7424);
      GL11.glTranslated(-x, -y, 0.0);
      DrawUtil.glRestoreBlend(restore);
      GL11.glEnable(3553);
   }

   public void onMouseRelease(int button) {
      this.colorSelectorDragging = false;
      this.alphaBarDragging = false;
      this.hueBarDragging = false;
   }

   public void onMouseClick(int mouseX, int mouseY, int button) {
      switch (button) {
         case 0:
            boolean expanded = this.isExpanded();
            boolean expandedHovered = this.isHoveredExpand(mouseX, mouseY);
            double ex = this.getExpandedX();
            double ey = this.getExpandedY();
            double expandedHeight = this.getExpandedHeight();
            double cpX = ex + 4.0;
            double cpY = ey + 4.0;
            double alphaBarHeight = 12.0;
            double hueSliderWidth = 10.0;
            double cpWidth = this.getWidth() - 12.0 - 10.0;
            double cpHeight = expandedHeight - 12.0 - 12.0;
            double alphaBarY = cpY + cpHeight + 4.0;
            double hueBarX = cpX + cpWidth + 4.0;
            this.colorSelectorDragging = expanded && expandedHovered && (double)mouseX > cpX && (double)mouseY > cpY && (double)mouseX < cpX + cpWidth && (double)mouseY < cpY + cpHeight;
            this.alphaBarDragging = expanded && expandedHovered && (double)mouseX > cpX && (double)mouseY > alphaBarY && (double)mouseX < cpX + cpWidth && (double)mouseY < alphaBarY + 12.0;
            this.hueBarDragging = expanded && expandedHovered && (double)mouseX > hueBarX && (double)mouseY > cpY && (double)mouseX < hueBarX + 10.0 && (double)mouseY < cpY + cpHeight;
            break;
         case 1:
            if (this.isHovered(mouseX, mouseY)) {
               switch (this.getState()) {
                  case CONTRACTING:
                  case CLOSED:
                     this.setState(ExpandState.EXPANDING);
                     break;
                  case EXPANDING:
                  case EXPANDED:
                     this.setState(ExpandState.CONTRACTING);
               }
            }
      }

   }

   public void setState(ExpandState state) {
      this.state = state;
      switch (state) {
         case CLOSED:
            this.progress = 0.0;
            break;
         case EXPANDED:
            this.progress = 1.0;
      }

   }

   public ExpandState getState() {
      return this.state;
   }

   public double calculateExpandedHeight() {
      return 80.0;
   }

   public double getExpandedX() {
      return this.getX();
   }

   public double getExpandedY() {
      return this.getY() + this.getHeight();
   }

   public double getExpandedWidth() {
      return this.getWidth();
   }

   public double getExpandProgress() {
      return this.progress;
   }

   public boolean isVisible() {
      return this.property.check();
   }
}
