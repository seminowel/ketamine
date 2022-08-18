package io.github.nevalackin.client.impl.ui.nl.components.selector;

import io.github.nevalackin.client.api.ui.cfont.CustomFontRenderer;
import io.github.nevalackin.client.api.ui.cfont.StaticallySizedImage;
import io.github.nevalackin.client.api.ui.framework.Component;
import io.github.nevalackin.client.impl.core.KetamineClient;
import io.github.nevalackin.client.util.render.ColourUtil;
import io.github.nevalackin.client.util.render.DrawUtil;
import net.minecraft.client.gui.ScaledResolution;

public final class PageButtonComponent extends Component implements PageSelector {
   private final String label;
   private final StaticallySizedImage icon;
   private double selectedFadeInProgress;
   private final int idx;

   public PageButtonComponent(Component parent, String label, StaticallySizedImage icon, int idx, double x, double y, double width, double height) {
      super(parent, x, y, width, height);
      this.label = label;
      this.icon = icon;
      this.idx = idx;
   }

   public void onDraw(ScaledResolution scaledResolution, int mouseX, int mouseY) {
      double x = this.getX();
      double y = this.getY();
      double width = this.getWidth();
      double height = this.getHeight();
      this.selectedFadeInProgress = DrawUtil.animateProgress(this.selectedFadeInProgress, this.idx == this.getSelectedIdx() ? 1.0 : 0.0, 4.0);
      DrawUtil.glDrawRoundedRect(x, y, width, height, DrawUtil.RoundingMode.FULL, 4.0F, (float)scaledResolution.getScaleFactor(), ColourUtil.overwriteAlphaComponent(this.getTheme().getPageSelectorSelectedPageColour(), (int)(255.0 * DrawUtil.bezierBlendAnimation(this.selectedFadeInProgress))));
      int iconSize = true;
      this.icon.draw(x + 4.0, y + height / 2.0 - 6.0, 12.0, 12.0, this.getTheme().getMainColour());
      CustomFontRenderer cFontRenderer = KetamineClient.getInstance().getFontRenderer();
      cFontRenderer.draw(this.label, x + 4.0 + 12.0 + 4.0, y + height / 2.0 - cFontRenderer.getHeight(this.label) / 2.0, this.getTheme().getHighlightedTextColour());
   }

   public void onMouseClick(int mouseX, int mouseY, int button) {
      if (button == 0 && this.isHovered(mouseX, mouseY)) {
         this.onPageSelect(this.idx, this.getY() + this.getHeight() / 2.0);
      }

   }
}
