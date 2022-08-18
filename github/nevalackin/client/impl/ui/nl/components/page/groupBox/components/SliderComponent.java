package io.github.nevalackin.client.impl.ui.nl.components.page.groupBox.components;

import io.github.nevalackin.client.api.ui.cfont.CustomFontRenderer;
import io.github.nevalackin.client.api.ui.framework.Animated;
import io.github.nevalackin.client.api.ui.framework.Component;
import io.github.nevalackin.client.api.ui.framework.Predicated;
import io.github.nevalackin.client.impl.core.KetamineClient;
import io.github.nevalackin.client.impl.property.DoubleProperty;
import io.github.nevalackin.client.util.render.ColourUtil;
import io.github.nevalackin.client.util.render.DrawUtil;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.gui.ScaledResolution;

public final class SliderComponent extends Component implements Predicated, Animated {
   private final String label;
   private final Supplier displayStringGetter;
   private final Consumer setter;
   private final Supplier getter;
   private final Supplier minGetter;
   private final Supplier maxGetter;
   private final Supplier incGetter;
   private final Supplier dependency;
   private double progress;
   private double hoveredFadeInProgress;
   private boolean sliding;

   public SliderComponent(Component parent, String label, Supplier displayStringGetter, Consumer setter, Supplier getter, Supplier minGetter, Supplier maxGetter, Supplier incGetter, Supplier dependency, double height) {
      super(parent, 0.0, 0.0, 0.0, height);
      this.label = label;
      if (displayStringGetter == null) {
         displayStringGetter = () -> {
            return DoubleProperty.DECIMAL_FORMAT.format(getter.get());
         };
      }

      this.displayStringGetter = displayStringGetter;
      this.setter = setter;
      this.getter = getter;
      this.minGetter = minGetter;
      this.maxGetter = maxGetter;
      this.incGetter = incGetter;
      this.dependency = dependency;
   }

   public void onDraw(ScaledResolution scaledResolution, int mouseX, int mouseY) {
      double x = this.getX();
      double y = this.getY();
      double width = this.getWidth();
      double height = this.getHeight();
      this.hoveredFadeInProgress = DrawUtil.animateProgress(this.hoveredFadeInProgress, this.isHovered(mouseX, mouseY) ? 1.0 : 0.0, 5.0);
      double smoothHovered = DrawUtil.bezierBlendAnimation(this.hoveredFadeInProgress);
      CustomFontRenderer fontRenderer = KetamineClient.getInstance().getFontRenderer();
      fontRenderer.draw(this.label, x, y + height / 2.0 - fontRenderer.getHeight(this.label) / 2.0, 0.23, ColourUtil.fadeBetween(this.getTheme().getTextColour(), this.getTheme().getHighlightedTextColour(), smoothHovered));
      double min = (Double)this.minGetter.get();
      double max = (Double)this.maxGetter.get();
      double range = max - min;
      String sValue = (String)this.displayStringGetter.get();
      double sValueWidth = fontRenderer.getWidth(sValue, 0.2, 700);
      double calculatedSliderWidth = Math.min(width - (sValueWidth + 4.0 + 2.0 + 2.0) - (fontRenderer.getWidth(this.label, 0.23) + 4.0 + 2.0), width / 2.5);
      double sliderThickness = 1.0;
      double sliderPos = y + height / 2.0 - 0.5;
      double right = x + width - 4.0;
      double value;
      double percentage;
      if (this.sliding) {
         value = right - calculatedSliderWidth;
         percentage = (double)mouseX - value;
         double sliderPercentage = Math.min(1.0, Math.max(0.0, percentage / calculatedSliderWidth));
         this.setter.accept(min + range * sliderPercentage);
      }

      value = (Double)this.getter.get();
      percentage = (value - min) / range;
      if (this.shouldPlayAnimation()) {
         this.progress = DrawUtil.animateProgress(this.progress, percentage, 5.0);
      }

      int mainColour = this.getTheme().getMainColour();
      DrawUtil.glDrawFilledQuad(right - calculatedSliderWidth, sliderPos, calculatedSliderWidth, 1.0, this.getTheme().getSliderBackgroundColour());
      DrawUtil.glDrawFilledQuad(right - calculatedSliderWidth, sliderPos, calculatedSliderWidth * this.progress, 1.0, mainColour);
      DrawUtil.glDrawPoint(right - calculatedSliderWidth * (1.0 - this.progress), sliderPos, 8.0F, scaledResolution, mainColour);
      fontRenderer.draw(sValue, right - calculatedSliderWidth - sValueWidth - 4.0 - 2.0, y + height / 2.0 - fontRenderer.getHeight(sValue, 700) / 2.0, 0.2, 700, this.getTheme().getTextColour());
   }

   public void onMouseClick(int mouseX, int mouseY, int button) {
      if (!this.sliding) {
         double x = this.getX();
         double y = this.getY();
         double width = this.getWidth();
         double height = this.getHeight();
         CustomFontRenderer fontRenderer = KetamineClient.getInstance().getFontRenderer();
         String sValue = (String)this.displayStringGetter.get();
         double sValueWidth = fontRenderer.getWidth(sValue, 0.2, 700);
         double calculatedSliderWidth = Math.min(width - (sValueWidth + 4.0 + 2.0 + 2.0) - (fontRenderer.getWidth(this.label, 0.23) + 4.0 + 2.0), width / 2.5);
         double right = x + width - 4.0;
         if ((double)mouseX > right - calculatedSliderWidth && (double)mouseY > y && (double)mouseX < right && (double)mouseY < y + height) {
            this.sliding = true;
         }
      }

   }

   public void onMouseRelease(int button) {
      if (this.sliding && button == 0) {
         this.sliding = false;
      }

   }

   public void onKeyPress(int keyCode) {
   }

   public boolean isVisible() {
      return this.dependency != null ? (Boolean)this.dependency.get() : true;
   }

   public void resetAnimationState() {
      this.progress = 0.0;
      this.hoveredFadeInProgress = 0.0;
      this.sliding = false;
   }
}
