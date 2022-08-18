package io.github.nevalackin.client.impl.ui.click.components.sub;

import io.github.nevalackin.client.api.ui.framework.Component;
import io.github.nevalackin.client.api.ui.framework.Predicated;
import io.github.nevalackin.client.impl.property.DoubleProperty;
import io.github.nevalackin.client.util.math.MathUtil;
import io.github.nevalackin.client.util.render.ColourUtil;
import io.github.nevalackin.client.util.render.DrawUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

public final class SliderComponent extends Component implements Predicated {
   private final DoubleProperty property;
   private boolean sliding;
   private double animatedPercentage;

   public SliderComponent(Component parent, DoubleProperty property, double x, double y, double width, double height) {
      super(parent, x, y, width, height);
      this.property = property;
   }

   public void onDraw(ScaledResolution scaledResolution, int mouseX, int mouseY) {
      double x = this.getX();
      double y = this.getY();
      double w = this.getWidth();
      double h = this.getHeight();
      double value = (Double)this.property.getValue();
      String valueText = this.property.getDisplayString();
      double percentage = (value - this.property.getMin()) / (this.property.getMax() - this.property.getMin());
      double sliderOffset = x + 4.0;
      double sliderWidth = w - 8.0;
      int colour = ColourUtil.overwriteAlphaComponent(this.getColour(this), 255);
      double inc;
      if (this.animatedPercentage < percentage) {
         inc = 1.0 / (double)Minecraft.getDebugFPS() * 2.0;
         if (percentage - this.animatedPercentage < inc) {
            this.animatedPercentage = percentage;
         } else {
            this.animatedPercentage += inc;
         }
      } else if (this.animatedPercentage > percentage) {
         inc = 1.0 / (double)Minecraft.getDebugFPS() * 2.0;
         if (this.animatedPercentage - percentage < inc) {
            this.animatedPercentage = percentage;
         } else {
            this.animatedPercentage -= inc;
         }
      }

      if (this.sliding) {
         this.property.setValue(Math.max(this.property.getMin(), Math.min(this.property.getMax(), MathUtil.round(((double)mouseX - x) * (this.property.getMax() - this.property.getMin()) / sliderWidth + this.property.getMin(), this.property.getInc()))));
      }

      FONT_RENDERER.draw(valueText, x + w - FONT_RENDERER.getWidth(valueText) - 4.0, y + 3.0, -5921371);
      DrawUtil.glDrawFilledQuad(sliderOffset, y + h - 3.0 - 1.0, sliderWidth, 1.0, -14145496);
      DrawUtil.glDrawFilledQuad(sliderOffset, y + h - 3.0 - 1.0, sliderWidth * this.animatedPercentage, 1.0, colour);
      DrawUtil.glDrawFilledEllipse(sliderOffset + sliderWidth * this.animatedPercentage, y + h - 3.0 - 1.0 + 0.5, 12.0F, colour);
      if (this.sliding) {
         DrawUtil.glDrawFilledEllipse(sliderOffset + sliderWidth * this.animatedPercentage, y + h - 3.0 - 1.0 + 0.5, 17.0F, ColourUtil.overwriteAlphaComponent(colour, 80));
      }

      FONT_RENDERER.draw(this.property.getName(), sliderOffset, y + 3.0, -1315861);
   }

   public void onMouseClick(int mouseX, int mouseY, int button) {
      double x = this.getX();
      double y = this.getY();
      double w = this.getWidth();
      double h = this.getHeight();
      this.sliding = button == 0 && (double)mouseX > x && (double)mouseY > y + h - 6.0 && (double)mouseX < x + w && (double)mouseY < y + h + 3.0;
   }

   public void onMouseRelease(int button) {
      this.sliding = false;
   }

   public boolean isVisible() {
      return this.property.check();
   }
}
