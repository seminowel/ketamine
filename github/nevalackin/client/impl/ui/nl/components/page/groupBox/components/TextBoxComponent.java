package io.github.nevalackin.client.impl.ui.nl.components.page.groupBox.components;

import io.github.nevalackin.client.api.ui.cfont.CustomFontRenderer;
import io.github.nevalackin.client.api.ui.framework.Animated;
import io.github.nevalackin.client.api.ui.framework.Component;
import io.github.nevalackin.client.impl.core.KetamineClient;
import io.github.nevalackin.client.util.render.ColourUtil;
import io.github.nevalackin.client.util.render.DrawUtil;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.client.gui.ScaledResolution;

public final class TextBoxComponent extends Component implements Animated {
   private final Function setter;
   private final Supplier getter;
   private boolean active;
   private double progress;
   private String text;

   public TextBoxComponent(Component parent, Function setter, Supplier getter, double y, double width, double height) {
      super(parent, 0.0, y, width, height);
      this.setter = setter;
      this.getter = getter;
      this.text = (String)getter.get();
   }

   public double getX() {
      return super.getX() + this.getParent().getWidth() - this.getWidth();
   }

   public void onDraw(ScaledResolution scaledResolution, int mouseX, int mouseY) {
      double x = this.getX();
      double y = this.getY();
      double width = this.getWidth();
      double height = this.getHeight();
      if (!this.active) {
         this.text = (String)this.getter.get();
      }

      this.progress = DrawUtil.animateProgress(this.progress, this.active ? 1.0 : 0.0, 3.0);
      DrawUtil.glDrawFilledQuad(x, y, width, height, this.getTheme().getComponentBackgroundColour());
      DrawUtil.glDrawOutlinedQuad(x, y, width, height, 1.0F, ColourUtil.fadeBetween(this.getTheme().getComponentOutlineColour(), this.getTheme().getMainColour(), DrawUtil.bezierBlendAnimation(this.progress)));
      double textScale = 0.22;
      CustomFontRenderer fontRenderer = KetamineClient.getInstance().getFontRenderer();
      fontRenderer.draw(this.text, x + width / 2.0 - fontRenderer.getWidth(this.text, 0.22) / 2.0, y + height / 2.0 - fontRenderer.getHeight(this.text, 0.22) / 2.0, 0.22, this.getTheme().getTextColour());
   }

   public void onMouseClick(int mouseX, int mouseY, int button) {
      if (button == 0) {
         this.active = true;
      }

   }

   public void onMouseRelease(int button) {
   }

   public void onKeyPress(int keyCode) {
      if (this.active) {
         switch (keyCode) {
            case 1:
               this.active = false;
               break;
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 52:
               this.text = this.text + keyCode;
               break;
            case 14:
               if (!this.text.isEmpty()) {
                  this.text = this.text.substring(0, this.text.length() - 1);
               }
               break;
            case 28:
               this.setter.apply(this.text);
               break;
            case 211:
               this.text = "";
         }
      }

   }

   public void resetAnimationState() {
      this.progress = 0.0;
   }
}
