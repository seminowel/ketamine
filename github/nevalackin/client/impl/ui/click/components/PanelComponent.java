package io.github.nevalackin.client.impl.ui.click.components;

import io.github.nevalackin.client.api.ui.cfont.StaticallySizedImage;
import io.github.nevalackin.client.api.ui.framework.Component;
import io.github.nevalackin.client.api.ui.framework.ExpandState;
import io.github.nevalackin.client.api.ui.framework.Expandable;
import io.github.nevalackin.client.impl.module.render.overlay.Gui;
import io.github.nevalackin.client.util.misc.ResourceUtil;
import io.github.nevalackin.client.util.render.BlurUtil;
import io.github.nevalackin.client.util.render.DrawUtil;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public abstract class PanelComponent extends Component implements Expandable {
   private boolean dragging;
   private double prevX;
   private double prevY;
   private ExpandState state;
   private double progress;
   private final String displayName;
   private double scrollbarActiveFadeInOut;
   private boolean hasScrollbar;
   private double maxPanelHeight;
   private int scrollOffset;
   private final StaticallySizedImage icon;
   private int scrollVelocity;
   private ComponentColourFunc childColourFunc;
   private final int tabColour;
   private final boolean drawModuleBar;
   public static final double MODULE_ENABLE_BAR_W = 26.0;
   public static final double MODULE_SEPARATOR_W = 2.0;

   public PanelComponent(String displayName, String iconPath, int tabColour, double x, double y, double width, double headerHeight, boolean drawModuleBar) {
      super((Component)null, x, y, width, headerHeight);
      this.state = ExpandState.CLOSED;
      this.displayName = displayName;
      this.tabColour = tabColour;

      try {
         this.icon = new StaticallySizedImage(ImageIO.read(ResourceUtil.getResourceStream(String.format("icons/dropdown/%s.png", iconPath))), true, 3);
      } catch (IOException var14) {
         throw new IllegalArgumentException("Resource does not exist.");
      }

      this.initComponents();
      this.drawModuleBar = drawModuleBar;
      this.setState(ExpandState.EXPANDED);
   }

   public void clearChildren() {
      this.getChildren().clear();
   }

   public void onDraw(ScaledResolution scaledResolution, int mouseX, int mouseY) {
      int colour = this.getColour(this);
      if (this.dragging) {
         this.setX((double)mouseX - this.prevX);
         this.setY((double)mouseY - this.prevY);
      }

      double x = this.getX();
      double y = this.getY();
      double width = this.getWidth();
      double height = this.getHeight();
      boolean expanded = this.isExpanded();
      if ((Boolean)Gui.oldGuiProperty.getValue()) {
         DrawUtil.glDrawRoundedRectEllipse(x, y + 1.0, width, height, expanded ? DrawUtil.RoundingMode.TOP : DrawUtil.RoundingMode.FULL, 12, 3.0, -15461356);
      } else {
         if ((Boolean)Gui.transparentProperty.getValue() && (Boolean)Gui.blurProperty.getValue()) {
            BlurUtil.blurArea(x, y + 1.0, width, height);
         }

         DrawUtil.glDrawRoundedRectEllipse(x, y + 1.0, width, height, expanded ? DrawUtil.RoundingMode.TOP : DrawUtil.RoundingMode.FULL, 12, 3.0, (Boolean)Gui.transparentProperty.getValue() ? 1343493140 : -15461356);
      }

      int iconSize = true;
      FONT_RENDERER.draw(this.displayName, x + 12.0 + 6.0, y + height / 2.0 - 4.0, (Boolean)Gui.transparentProperty.getValue() ? -1 : -2829100);
      this.icon.draw(x + 2.0, y + 1.0 + height / 2.0 - 6.0, 12.0, 12.0, colour);
      double expandedHeight = this.calculateExpandedHeight();
      double eh = expandedHeight * DrawUtil.bezierBlendAnimation(this.progress);
      int heightLimit = (int)((float)scaledResolution.getScaledHeight() * 0.75F);
      this.hasScrollbar = expandedHeight > (double)heightLimit;
      this.maxPanelHeight = Math.min(eh, (double)heightLimit);
      if (!this.hasScrollbar) {
         this.scrollOffset = 0;
      }

      double speed = 6.0 - Math.min(3.0, Math.sqrt(expandedHeight) / 5.0);
      switch (this.getState()) {
         case CONTRACTING:
            if (this.progress <= 0.0) {
               this.setState(ExpandState.CLOSED);
            } else {
               this.progress -= 1.0 / (double)Minecraft.getDebugFPS() * speed;
            }
            break;
         case EXPANDING:
            if (this.progress >= 1.0) {
               this.setState(ExpandState.EXPANDED);
            } else {
               this.progress += 1.0 / (double)Minecraft.getDebugFPS() * speed;
            }
      }

      if (expanded) {
         double ex = this.getExpandedX();
         double ey = this.getExpandedY();
         double ew = this.getExpandedWidth();
         if (this.hasScrollbar) {
            if (this.isHoveredExpand(mouseX, mouseY)) {
               int scrollAmount = -Mouse.getDWheel() / 20;
               if (scrollAmount != 0) {
                  this.scrollVelocity += scrollAmount;
                  this.scrollbarActiveFadeInOut = 1.0;
               }

               this.scrollVelocity = (int)((double)this.scrollVelocity * 0.98);
               this.scrollOffset = Math.max(0, Math.min((int)(expandedHeight - this.maxPanelHeight), this.scrollOffset + this.scrollVelocity));
            }

            this.glScissorBox(ex, ey, ew, this.maxPanelHeight, scaledResolution);
            if (this.scrollOffset > 0) {
               GL11.glTranslated(0.0, (double)(-this.scrollOffset), 0.0);
               this.transformOffset -= (double)this.scrollOffset;
            }
         }

         if (this.drawModuleBar) {
            if ((Boolean)Gui.oldGuiProperty.getValue()) {
               DrawUtil.glDrawRoundedRectEllipse(ex, ey, 26.0, eh, DrawUtil.RoundingMode.BOTTOM_LEFT, 12, 3.0, -15461356);
               DrawUtil.glDrawFilledQuad(ex + 26.0, ey, 2.0, eh, -15000805);
            } else {
               if ((Boolean)Gui.transparentProperty.getValue() && (Boolean)Gui.blurProperty.getValue()) {
                  BlurUtil.blurArea(ex, ey, 26.0, this.maxPanelHeight);
               }

               DrawUtil.glDrawRoundedRectEllipse(ex, ey, 26.0, eh, DrawUtil.RoundingMode.BOTTOM_LEFT, 12, 3.0, (Boolean)Gui.transparentProperty.getValue() ? 1344151070 : -14803426);
               if ((Boolean)Gui.transparentProperty.getValue() && (Boolean)Gui.blurProperty.getValue()) {
                  BlurUtil.blurArea(ex + 26.0, ey, 2.0, this.maxPanelHeight);
               }

               DrawUtil.glDrawFilledQuad(ex + 26.0, ey, 2.0, eh, (Boolean)Gui.transparentProperty.getValue() ? 1344151070 : -14803426);
            }
         }

         if ((Boolean)Gui.oldGuiProperty.getValue()) {
            DrawUtil.glDrawRoundedRectEllipse(this.drawModuleBar ? ex + 26.0 + 2.0 : ex, ey, this.drawModuleBar ? ew - 26.0 - 2.0 : ew, eh, DrawUtil.RoundingMode.BOTTOM_RIGHT, 12, 3.0, -14803426);
         } else {
            if ((Boolean)Gui.transparentProperty.getValue() && (Boolean)Gui.blurProperty.getValue()) {
               BlurUtil.blurArea(this.drawModuleBar ? ex + 26.0 + 2.0 : ex, ey, this.drawModuleBar ? ew - 26.0 - 2.0 : ew, this.maxPanelHeight);
            }

            DrawUtil.glDrawRoundedRectEllipse(this.drawModuleBar ? ex + 26.0 + 2.0 : ex, ey, this.drawModuleBar ? ew - 26.0 - 2.0 : ew, eh, this.drawModuleBar ? DrawUtil.RoundingMode.BOTTOM_RIGHT : DrawUtil.RoundingMode.BOTTOM, 12, 3.0, (Boolean)Gui.transparentProperty.getValue() ? 1344151070 : -14803426);
         }

         double yPos = this.getHeight();
         List children = this.getChildren();

         for(int i = 0; i < children.size(); ++i) {
            Component component = (Component)children.get(i);
            component.setY(yPos);
            boolean skipScissor = this.hasScrollbar && i == 0;
            if (!skipScissor) {
               this.glScissorBox(ex, ey, ew, this.hasScrollbar ? this.maxPanelHeight : eh + 1.0, false, scaledResolution);
            }

            component.onDraw(scaledResolution, mouseX, mouseY + this.scrollOffset);
            if (component instanceof Expandable) {
               Expandable Expandable = (Expandable)component;
               if (Expandable.isExpanded()) {
                  yPos += Expandable.getExpandedHeight();
               }
            }

            yPos += component.getHeight();
         }

         DrawUtil.glEndScissor();
         if (this.hasScrollbar) {
            if (this.scrollOffset > 0) {
               GL11.glTranslated(0.0, (double)this.scrollOffset, 0.0);
               this.transformOffset += (double)this.scrollOffset;
               DrawUtil.glDrawFilledQuad(x, y + height, width, 6.0, Integer.MIN_VALUE, 0);
            }

            double visiblePercentage = this.maxPanelHeight / eh;
            double scrollbarHeight = visiblePercentage * this.maxPanelHeight;
            double scrollBarWidth = 2.0;
            GL11.glPushMatrix();
            GL11.glTranslated(ex + width - 3.0, ey + (this.maxPanelHeight - scrollbarHeight) * ((double)this.scrollOffset / (eh - this.maxPanelHeight)), 0.0);
            GL11.glPopMatrix();
         }
      } else {
         this.scrollOffset = 0;
      }

   }

   public void onMouseClick(int mouseX, int mouseY, int button) {
      if (this.isHovered(mouseX, mouseY)) {
         switch (button) {
            case 0:
               this.dragging = true;
               this.prevX = (double)mouseX - this.getX();
               this.prevY = (double)mouseY - this.getY();
               break;
            case 1:
               if (this.hasChildren()) {
                  switch (this.getState()) {
                     case CONTRACTING:
                     case CLOSED:
                        this.setState(ExpandState.EXPANDING);
                        break;
                     case EXPANDING:
                     case EXPANDED:
                        this.setState(ExpandState.CONTRACTING);
                  }
               }
         }
      }

      if (this.isExpanded() && this.isHoveredExpand(mouseX, mouseY)) {
         double yPos = this.getHeight();

         Component component;
         try {
            for(Iterator var6 = this.getChildren().iterator(); var6.hasNext(); yPos += component.getHeight()) {
               component = (Component)var6.next();
               component.setY(yPos);
               component.onMouseClick(mouseX, mouseY + this.scrollOffset, button);
               if (component instanceof Expandable) {
                  Expandable expandableComponent = (Expandable)component;
                  if (expandableComponent.isExpanded()) {
                     yPos += expandableComponent.getExpandedHeight();
                  }
               }
            }
         } catch (Exception var9) {
            var9.printStackTrace();
         }
      }

   }

   public boolean isHoveredExpand(int mouseX, int mouseY) {
      double ex = this.getExpandedX();
      double ey = this.getExpandedY();
      double ew = this.getExpandedWidth();
      double eh = Math.min(this.getExpandedHeight(), this.maxPanelHeight);
      return (double)mouseX > ex && (double)mouseY > ey && (double)mouseX < ex + ew && (double)mouseY < ey + eh;
   }

   public void onMouseRelease(int button) {
      if (button == 0) {
         this.dragging = false;
      }

      super.onMouseRelease(button);
   }

   public double calculateExpandedHeight() {
      double height = 0.0;

      Component component;
      for(Iterator var3 = this.getChildren().iterator(); var3.hasNext(); height += component.getHeight()) {
         component = (Component)var3.next();
         if (component instanceof Expandable) {
            Expandable Expandable = (Expandable)component;
            if (Expandable.isExpanded()) {
               height += Expandable.getExpandedHeight();
            }
         }
      }

      return height;
   }

   public ComponentColourFunc getChildColourFunc() {
      return this.childColourFunc;
   }

   public void setChildColourFunc(ComponentColourFunc childColourFunc) {
      this.childColourFunc = childColourFunc;
   }

   public double getExpandedX() {
      return this.getX();
   }

   public double getExpandedY() {
      return this.getY() + this.getHeight();
   }

   public double getExpandedWidth() {
      return this.getWidth();
   }

   public ExpandState getState() {
      return this.state;
   }

   public void setState(ExpandState state) {
      this.state = state;
      switch (state) {
         case CLOSED:
            this.progress = 0.0;
            break;
         case EXPANDED:
            this.progress = 1.0;
      }

   }

   public int getTabColour() {
      return this.tabColour;
   }

   public int getColour(Component child) {
      return (Integer)this.childColourFunc.apply(child);
   }

   public double getExpandProgress() {
      return this.progress;
   }

   public abstract void initComponents();
}
