package io.github.nevalackin.client.impl.ui.nl.components.page.groupBox;

import io.github.nevalackin.client.api.ui.framework.Animated;
import io.github.nevalackin.client.api.ui.framework.Component;
import io.github.nevalackin.client.api.ui.framework.Predicated;
import io.github.nevalackin.client.util.render.ColourUtil;
import io.github.nevalackin.client.util.render.DrawUtil;
import java.util.Iterator;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public abstract class GroupBoxComponent extends Component implements Animated {
   protected final double xMargin = 3.0;
   protected final double yMargin = 2.0;
   public double maxHeight;
   public int pageColumn;
   private double scrollVelocity;
   public int scrollOffset;

   public GroupBoxComponent(Component parent) {
      super(parent, 0.0, 0.0, 0.0, 0.0);
      this.onInit();
   }

   public double getHeight() {
      this.getClass();
      double height = 2.0 * 2.0;
      Iterator var3 = this.getChildren().iterator();

      while(true) {
         Component component;
         Predicated Predicated;
         do {
            if (!var3.hasNext()) {
               return height;
            }

            component = (Component)var3.next();
            if (!(component instanceof Predicated)) {
               break;
            }

            Predicated = (Predicated)component;
         } while(!Predicated.isVisible());

         height += component.getHeight();
      }
   }

   public void onDraw(ScaledResolution scaledResolution, int mouseX, int mouseY) {
      double x = this.getX();
      double y = this.getY();
      double width = this.getWidth();
      double scrollableHeight = this.getHeight();
      double viewableHeight = Math.min(scrollableHeight, this.maxHeight);
      int groupBoxBackground = this.getTheme().getGroupBoxBackgroundColour();
      DrawUtil.glDrawRoundedRect(x, y, width, viewableHeight, DrawUtil.RoundingMode.FULL, 4.0F, (float)scaledResolution.getScaleFactor(), groupBoxBackground);
      this.getClass();
      double childOffset = 2.0;
      boolean scrollable = scrollableHeight > viewableHeight;
      if (scrollable) {
         DrawUtil.glScissorBox(x, y, width, viewableHeight, scaledResolution);
         if (this.isHovered(mouseX, mouseY)) {
            double scrollAmount = (double)(-Mouse.getDWheel()) / 50.0;
            if (scrollAmount != 0.0) {
               this.scrollVelocity += scrollAmount;
            }
         }

         this.scrollVelocity *= 0.98;
         this.scrollOffset = (int)Math.round(Math.max(0.0, Math.min(scrollableHeight - viewableHeight, (double)this.scrollOffset + this.scrollVelocity)));
         GL11.glTranslated(0.0, (double)(-this.scrollOffset), 0.0);
      } else {
         this.scrollOffset = 0;
         this.scrollVelocity = 0.0;
      }

      Iterator var21 = this.getChildren().iterator();

      while(true) {
         Component child;
         Predicated Predicated;
         do {
            if (!var21.hasNext()) {
               if (scrollable) {
                  DrawUtil.glEndScissor();
                  if ((double)this.scrollOffset < scrollableHeight - viewableHeight) {
                     DrawUtil.glDrawFilledQuad(x, y + viewableHeight - 20.0, width, 20.0, ColourUtil.removeAlphaComponent(groupBoxBackground), groupBoxBackground);
                  }

                  GL11.glTranslated(0.0, (double)this.scrollOffset, 0.0);
               }

               return;
            }

            child = (Component)var21.next();
            if (!(child instanceof Predicated)) {
               break;
            }

            Predicated = (Predicated)child;
         } while(!Predicated.isVisible());

         this.getClass();
         child.setX(3.0);
         child.setY(childOffset);
         this.getClass();
         child.setWidth(width - 3.0 * 2.0);
         child.onDraw(scaledResolution, mouseX, mouseY + this.scrollOffset);
         childOffset += child.getHeight();
      }
   }

   public void onMouseClick(int mouseX, int mouseY, int button) {
      Iterator var4 = this.getChildren().iterator();

      Component child;
      Predicated Predicated;
      do {
         do {
            if (!var4.hasNext()) {
               return;
            }

            child = (Component)var4.next();
            if (!(child instanceof Predicated)) {
               break;
            }

            Predicated = (Predicated)child;
         } while(!Predicated.isVisible());
      } while(!child.isHovered(mouseX, mouseY + this.scrollOffset));

      child.onMouseClick(mouseX, mouseY + this.scrollOffset, button);
   }

   public boolean isHovered(int mouseX, int mouseY) {
      if (super.isHovered(mouseX, mouseY + this.scrollOffset)) {
         return true;
      } else {
         Iterator var3 = this.getChildren().iterator();

         Component child;
         Predicated Predicated;
         do {
            do {
               if (!var3.hasNext()) {
                  return false;
               }

               child = (Component)var3.next();
               if (!(child instanceof Predicated)) {
                  break;
               }

               Predicated = (Predicated)child;
            } while(!Predicated.isVisible());
         } while(!child.isHovered(mouseX, mouseY + this.scrollOffset));

         return true;
      }
   }

   public void resetAnimationState() {
      this.resetChildrenAnimations();
   }

   public abstract void onInit();
}
