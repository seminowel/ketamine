package io.github.nevalackin.client.impl.ui.click.components.sub;

import io.github.nevalackin.client.api.ui.framework.Component;
import io.github.nevalackin.client.api.ui.framework.Predicated;
import io.github.nevalackin.client.impl.property.BooleanProperty;
import io.github.nevalackin.client.util.render.ColourUtil;
import io.github.nevalackin.client.util.render.DrawUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

public final class CheckBoxComponent extends Component implements Predicated {
   private final BooleanProperty property;
   private double progress;

   public CheckBoxComponent(Component parent, BooleanProperty property, double x, double y, double width, double height) {
      super(parent, x, y, width, height);
      this.property = property;
   }

   public void onDraw(ScaledResolution scaledResolution, int mouseX, int mouseY) {
      double x = this.getX();
      double y = this.getY();
      double w = this.getWidth();
      double h = this.getHeight();
      double width = 12.0;
      double left = x + w - 4.0 - 12.0;
      if ((Boolean)this.property.getValue()) {
         if (this.progress < 1.0) {
            this.progress += 1.0 / (double)Minecraft.getDebugFPS() * 6.0;
         } else {
            this.progress = 1.0;
         }
      } else if (this.progress > 0.0) {
         this.progress -= 1.0 / (double)Minecraft.getDebugFPS() * 6.0;
      } else {
         this.progress = 0.0;
      }

      double size = 5.0;
      int colour = this.getColour(this);
      int backgroundColour = ColourUtil.overwriteAlphaComponent(ColourUtil.fadeBetween(-12237499, colour, this.progress), 255);
      DrawUtil.glDrawFilledEllipse(left + 2.5, y + h / 2.0, 12.0F, backgroundColour);
      DrawUtil.glDrawFilledEllipse(left + 12.0 - 2.5, y + h / 2.0, 12.0F, backgroundColour);
      DrawUtil.glDrawFilledQuad(left + 2.5, y + h / 2.0 - 3.0, 7.0, 6.0, backgroundColour);
      DrawUtil.glDrawFilledEllipse(left + 2.5 + 0.5 + this.progress * 6.0, y + h / 2.0, 10.0F, -15066853);
      FONT_RENDERER.draw(this.property.getName(), x + 4.0, y + h / 2.0 - 4.0, (Boolean)this.property.getValue() ? -1315861 : -5921371);
   }

   public void onMouseClick(int mouseX, int mouseY, int button) {
      if (this.isHovered(mouseX, mouseY) && button == 0) {
         this.property.setValue(!(Boolean)this.property.getValue());
      }

   }

   public boolean isVisible() {
      return this.property.check();
   }
}
