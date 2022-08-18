package io.github.nevalackin.client.impl.ui.click.components.sub;

import io.github.nevalackin.client.api.ui.framework.Component;
import net.minecraft.client.gui.ScaledResolution;

public abstract class ButtonComponent extends Component {
   private final String buttonLabel;

   public ButtonComponent(Component parent, String buttonLabel, double x, double y, double width, double height) {
      super(parent, x, y, width, height);
      this.buttonLabel = buttonLabel;
   }

   public abstract void onMouseClick(int var1, int var2, int var3);

   public void onDraw(ScaledResolution scaledResolution, int mouseX, int mouseY) {
      double x = this.getX();
      double y = this.getY();
      double w = this.getWidth();
      double h = this.getHeight();
      FONT_RENDERER.draw(this.buttonLabel, x + (w / 2.0 - FONT_RENDERER.getWidth(this.buttonLabel) / 2.0), y + h / 2.0 - 4.0, this.isHovered(mouseX, mouseY) ? -5921371 : -657931);
      super.onDraw(scaledResolution, mouseX, mouseY);
   }
}
