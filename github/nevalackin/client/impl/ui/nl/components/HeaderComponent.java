package io.github.nevalackin.client.impl.ui.nl.components;

import io.github.nevalackin.client.api.ui.framework.Component;
import io.github.nevalackin.client.impl.ui.nl.components.buttons.SaveConfigButtonComponent;
import io.github.nevalackin.client.util.render.DrawUtil;
import java.util.function.Supplier;
import net.minecraft.client.gui.ScaledResolution;

public final class HeaderComponent extends Component {
   public HeaderComponent(Component parent) {
      super(parent, 0.0, 0.0, 0.0, 40.0);
      double margin = this.getHeight() / 2.0 - 8.0;
      this.addChild(new SaveConfigButtonComponent(this, (Supplier)null, margin, margin, 75.0, 16.0));
   }

   public void onDraw(ScaledResolution scaledResolution, int mouseX, int mouseY) {
      double x = this.getX();
      double y = this.getY();
      double width = this.getWidth();
      double height = this.getHeight();
      double lineThickness = 1.0;
      DrawUtil.glDrawFilledQuad(x, y + height - 1.0, width, 1.0, this.getTheme().getHeaderPageSeparatorColour());
      super.onDraw(scaledResolution, mouseX, mouseY);
   }
}
