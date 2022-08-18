package io.github.nevalackin.client.impl.ui.nl.components.selector;

import io.github.nevalackin.client.api.ui.cfont.CustomFontRenderer;
import io.github.nevalackin.client.api.ui.framework.Component;
import io.github.nevalackin.client.impl.core.KetamineClient;
import net.minecraft.client.gui.ScaledResolution;

public final class PageSelectorLabelComponent extends Component implements PageSelector {
   private final String label;

   public PageSelectorLabelComponent(Component parent, String label, double x, double y, double width, double height) {
      super(parent, x, y, width, height);
      this.label = label;
   }

   public void onDraw(ScaledResolution scaledResolution, int mouseX, int mouseY) {
      double x = this.getX();
      double y = this.getY();
      double width = this.getWidth();
      double height = this.getHeight();
      CustomFontRenderer fontRenderer = KetamineClient.getInstance().getFontRenderer();
      fontRenderer.draw(this.label, x + 4.0, y + height / 2.0 - fontRenderer.getHeight(this.label) / 2.0, 0.225, this.getTheme().getPageSelectorLabelColour());
      super.onDraw(scaledResolution, mouseX, mouseY);
   }

   public void onMouseClick(int mouseX, int mouseY, int button) {
   }

   public void onMouseRelease(int button) {
   }

   public void onKeyPress(int keyCode) {
   }

   public boolean isHovered(int mouseX, int mouseY) {
      return false;
   }
}
