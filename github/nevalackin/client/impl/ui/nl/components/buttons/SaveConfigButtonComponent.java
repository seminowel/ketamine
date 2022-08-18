package io.github.nevalackin.client.impl.ui.nl.components.buttons;

import io.github.nevalackin.client.api.config.ConfigManager;
import io.github.nevalackin.client.api.ui.cfont.CustomFontRenderer;
import io.github.nevalackin.client.api.ui.cfont.StaticallySizedImage;
import io.github.nevalackin.client.api.ui.framework.Component;
import io.github.nevalackin.client.impl.config.Config;
import io.github.nevalackin.client.impl.core.KetamineClient;
import io.github.nevalackin.client.util.misc.ResourceUtil;
import io.github.nevalackin.client.util.render.ColourUtil;
import io.github.nevalackin.client.util.render.DrawUtil;
import java.io.IOException;
import java.util.function.Supplier;
import javax.imageio.ImageIO;
import net.minecraft.client.gui.ScaledResolution;

public final class SaveConfigButtonComponent extends Component {
   private static StaticallySizedImage icon;
   private final Supplier configSupplier;
   private double hoveredFadeInProgress;

   public SaveConfigButtonComponent(Component parent, Supplier configSupplier, double x, double y, double width, double height) {
      super(parent, x, y, width, height);
      this.configSupplier = configSupplier;
   }

   public void onDraw(ScaledResolution scaledResolution, int mouseX, int mouseY) {
      double x = this.getX();
      double y = this.getY();
      double width = this.getWidth();
      double height = this.getHeight();
      this.hoveredFadeInProgress = DrawUtil.animateProgress(this.hoveredFadeInProgress, this.isHovered(mouseX, mouseY) ? 1.0 : 0.0, 4.0);
      DrawUtil.glDrawRoundedOutline(x, y, width, height, 2.0F, DrawUtil.RoundingMode.FULL, 4.0F, ColourUtil.fadeBetween(this.getTheme().getComponentOutlineColour(), this.getTheme().getMainColour(), DrawUtil.bezierBlendAnimation(this.hoveredFadeInProgress)));
      CustomFontRenderer cFontRenderer = KetamineClient.getInstance().getFontRenderer();
      String label = "Save";
      double iconSize = this.getHeight() - 4.0;
      double labelWidth = cFontRenderer.getWidth("Save");
      int textColour = this.getTheme().getTextColour();
      icon.draw(x + width / 2.0 - labelWidth / 2.0 - (iconSize + 4.0) / 2.0, y + height / 2.0 - iconSize / 2.0, iconSize, iconSize, textColour);
      cFontRenderer.draw("Save", x + width / 2.0 - labelWidth / 2.0 + (iconSize + 4.0) / 2.0, y + height / 2.0 - cFontRenderer.getHeight("Save") / 2.0, textColour);
   }

   public void onMouseClick(int mouseX, int mouseY, int button) {
      if (button == 0 && this.isHovered(mouseX, mouseY)) {
         ConfigManager configManager = KetamineClient.getInstance().getConfigManager();
         if (this.configSupplier == null) {
            configManager.saveCurrent();
         } else {
            configManager.save((Config)this.configSupplier.get());
         }
      }

   }

   public void onMouseRelease(int button) {
   }

   public void onKeyPress(int keyCode) {
   }

   static {
      try {
         icon = new StaticallySizedImage(ImageIO.read(ResourceUtil.getResourceStream("icons/ui/save.png")), true, 3);
      } catch (IOException var1) {
         var1.printStackTrace();
      }

   }
}
