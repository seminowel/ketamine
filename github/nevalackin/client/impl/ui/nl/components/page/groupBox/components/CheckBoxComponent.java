package io.github.nevalackin.client.impl.ui.nl.components.page.groupBox.components;

import io.github.nevalackin.client.api.ui.cfont.CustomFontRenderer;
import io.github.nevalackin.client.api.ui.framework.Animated;
import io.github.nevalackin.client.api.ui.framework.Component;
import io.github.nevalackin.client.api.ui.framework.Predicated;
import io.github.nevalackin.client.impl.core.KetamineClient;
import io.github.nevalackin.client.util.render.ColourUtil;
import io.github.nevalackin.client.util.render.DrawUtil;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.gui.ScaledResolution;

public class CheckBoxComponent extends Component implements Predicated, Animated {
   private final String label;
   private final Consumer setter;
   private final Supplier getter;
   private final Supplier dependency;
   private double progress;

   public CheckBoxComponent(Component parent, String label, Supplier getter, Consumer setter, Supplier dependency, double height) {
      super(parent, 0.0, 0.0, 0.0, height);
      this.label = label;
      this.getter = getter;
      this.setter = setter;
      this.dependency = dependency;
   }

   public void onDraw(ScaledResolution scaledResolution, int mouseX, int mouseY) {
      double x = this.getX();
      double y = this.getY();
      double width = this.getWidth();
      double height = this.getHeight();
      if (this.shouldPlayAnimation()) {
         this.progress = DrawUtil.animateProgress(this.progress, (Boolean)this.getter.get() ? 1.0 : 0.0, 5.0);
      }

      double smooth = DrawUtil.bezierBlendAnimation(this.progress);
      if (this.label != null) {
         CustomFontRenderer fontRenderer = KetamineClient.getInstance().getFontRenderer();
         fontRenderer.draw(this.label, x, y + height / 2.0 - fontRenderer.getHeight(this.label) / 2.0, 0.22, ColourUtil.fadeBetween(this.getTheme().getTextColour(), this.getTheme().getHighlightedTextColour(), smooth));
      }

      int backgroundColour = ColourUtil.fadeBetween(this.getTheme().getCheckBoxBackgroundDisabledColour(), this.getTheme().getCheckBoxBackgroundEnabledColour(), smooth);
      int knobColour = ColourUtil.fadeBetween(this.getTheme().getCheckBoxDisabledColour(), this.getTheme().getCheckBoxEnabledColour(), smooth);
      float smallRadius = 5.0F;
      float largeRadius = 8.0F;
      double right = x + width - 8.0;
      double left = right - 10.0;
      double middle = y + height / 2.0;
      DrawUtil.glDrawPoint(right, middle, 5.0F, scaledResolution, backgroundColour);
      DrawUtil.glDrawFilledQuad(left, middle - 2.5, right - left, 5.0, backgroundColour);
      DrawUtil.glDrawPoint(left, middle, 5.0F, scaledResolution, backgroundColour);
      DrawUtil.glDrawPoint(DrawUtil.interpolate(left, right, smooth), y + height / 2.0, 8.0F, scaledResolution, knobColour);
   }

   public void onMouseClick(int mouseX, int mouseY, int button) {
      if (button == 0) {
         this.setter.accept(!(Boolean)this.getter.get());
      }

   }

   public void onMouseRelease(int button) {
   }

   public void onKeyPress(int keyCode) {
   }

   public boolean isVisible() {
      return this.dependency != null ? (Boolean)this.dependency.get() : true;
   }

   public void resetAnimationState() {
      this.progress = 0.0;
   }
}
