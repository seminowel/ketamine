package io.github.nevalackin.client.impl.ui.click.components.sub;

import io.github.nevalackin.client.api.ui.framework.Component;
import io.github.nevalackin.client.api.ui.framework.Predicated;
import io.github.nevalackin.client.impl.config.Config;
import io.github.nevalackin.client.impl.core.KetamineClient;
import io.github.nevalackin.client.util.render.DrawUtil;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.ScaledResolution;

public final class ConfigComponent extends Component implements Predicated {
   private final Config config;

   public ConfigComponent(Component parent, Config config, double x, double y, double width, double height) {
      super(parent, x, y, width, height);
      this.config = config;
   }

   public void onDraw(ScaledResolution scaledResolution, int mouseX, int mouseY) {
      double x = this.getX();
      double y = this.getY();
      double w = this.getWidth();
      double h = this.getHeight();
      boolean isSelected = KetamineClient.getInstance().getDropdownGUI().getSelectedConfig() == this.config;
      FONT_RENDERER.draw(this.config.getName().toLowerCase(), x + (w / 2.0 - FONT_RENDERER.getWidth(this.config.getName().toLowerCase()) / 2.0), y + h / 2.0 - 4.0, isSelected ? -1315861 : -5921371);
      if (this.isLastConfigComponent()) {
         DrawUtil.glDrawFilledQuad(x, y + h, w, 1.0, -13552349);
      }

      super.onDraw(scaledResolution, mouseX, mouseY);
   }

   public void onMouseClick(int mouseX, int mouseY, int button) {
      if (button == 0 && this.isHovered(mouseX, mouseY)) {
         KetamineClient.getInstance().getDropdownGUI().setSelectedConfig(this.config);
      }

      super.onMouseClick(mouseX, mouseY, button);
   }

   public void onMouseRelease(int button) {
      super.onMouseRelease(button);
   }

   public boolean isVisible() {
      return true;
   }

   private boolean isLastConfigComponent() {
      List cacheConfigComponents = new ArrayList(this.getParent().getChildren());
      cacheConfigComponents.removeIf((component) -> {
         return !(component instanceof ConfigComponent);
      });
      return cacheConfigComponents.indexOf(this) == cacheConfigComponents.size() - 1;
   }
}
