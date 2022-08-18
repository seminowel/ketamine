package io.github.nevalackin.client.impl.ui.nl.components.page.groupBox.components;

import io.github.nevalackin.client.api.binding.Bind;
import io.github.nevalackin.client.api.binding.BindType;
import io.github.nevalackin.client.api.notification.NotificationType;
import io.github.nevalackin.client.api.ui.cfont.CustomFontRenderer;
import io.github.nevalackin.client.api.ui.framework.Animated;
import io.github.nevalackin.client.api.ui.framework.Component;
import io.github.nevalackin.client.api.ui.framework.ExpandState;
import io.github.nevalackin.client.api.ui.framework.Expandable;
import io.github.nevalackin.client.impl.core.KetamineClient;
import io.github.nevalackin.client.impl.event.game.input.InputType;
import io.github.nevalackin.client.util.render.DrawUtil;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public final class GroupBoxHeaderComponent extends Component implements Expandable, Animated {
   private final String name;
   private final Unbind unbindFunc;
   private final boolean bindable;
   private boolean binding;
   private final Consumer bindSetter;
   private final Supplier bindGetter;
   private double clickedX;
   private double clickedY;
   private ExpandState state;
   private double progress;
   private BindType selectedBindType;
   private double expandedWidth;
   private double lastExpandedHeight;
   private double fontScale;
   private double buttonHeight;

   public GroupBoxHeaderComponent(Component parent, String name, Consumer enabledSetter, Supplier enabledGetter, Consumer bindSetter, Supplier bindGetter, Unbind unbindFunc, double height) {
      super(parent, 0.0, 0.0, 0.0, height);
      this.name = name;
      this.bindable = bindSetter != null && bindGetter != null && unbindFunc != null;
      this.bindSetter = bindSetter;
      this.bindGetter = bindGetter;
      this.unbindFunc = unbindFunc;
      CheckBoxComponent checkBox = new CheckBoxComponent(this, (String)null, enabledGetter, enabledSetter, (Supplier)null, height - 4.0) {
         public double getWidth() {
            return this.getParent().getWidth();
         }
      };
      this.addChild(checkBox);
   }

   public void onDraw(ScaledResolution scaledResolution, int mouseX, int mouseY) {
      double x = this.getX();
      double y = this.getY();
      double width = this.getWidth();
      double height = this.getHeight();
      CustomFontRenderer fontRenderer = KetamineClient.getInstance().getFontRenderer();
      String groupBoxTitle = this.binding ? "Binding..." : this.name;
      fontRenderer.draw(groupBoxTitle, x, y + (height - 4.0) / 2.0 - fontRenderer.getHeight(groupBoxTitle, 0.275) / 2.0, 0.275, this.getTheme().getHighlightedTextColour());
      Bind bind;
      if (this.bindable && (bind = (Bind)this.bindGetter.get()) != null) {
         String buttonName = String.format("%s [%s]", bind.getCodeName(), bind.getBindType());
         fontRenderer.draw(buttonName, x + fontRenderer.getWidth(groupBoxTitle, 0.275) + 4.0, y + (height - 4.0) / 2.0 - fontRenderer.getHeight(buttonName, 0.22) / 2.0, 0.22, 700, this.getTheme().getTextColour());
      }

      double lineThickness = 1.0;
      DrawUtil.glDrawFilledQuad(x, y + (height - 2.0) - 1.0, width, 1.0, this.getTheme().getGroupBoxHeaderSeparatorColour());
      super.onDraw(scaledResolution, mouseX, mouseY);
      if (this.state != ExpandState.CLOSED) {
         GL11.glPushMatrix();
         switch (this.state) {
            case EXPANDING:
               this.progress = DrawUtil.animateProgress(this.progress, 1.0, 4.0);
               if (this.progress >= 1.0) {
                  this.state = ExpandState.EXPANDED;
                  this.progress = 1.0;
               }
               break;
            case CONTRACTING:
               this.progress = DrawUtil.animateProgress(this.progress, 0.0, 4.0);
               if (this.progress <= 0.0) {
                  this.progress = 0.0;
                  this.state = ExpandState.CLOSED;
                  return;
               }
         }

         GL11.glTranslated(x, y + (height - 4.0) / 2.0, 2.0);
         GL11.glScaled(this.progress, this.progress, 1.0);
         GL11.glTranslated(-x, -(y + (height - 4.0) / 2.0), 0.0);
         double ex = this.getExpandedX();
         double ey = this.getExpandedY();
         double ew = this.getExpandedWidth();
         double eh = this.getExpandedHeight();
         DrawUtil.glDrawFilledQuad(ex, ey, ew, eh, this.getTheme().getGroupBoxBackgroundColour());
         DrawUtil.glDrawOutlinedQuad(ex, ey, ew, eh, 1.0F, this.getTheme().getMainColour());
         BindType[] types = BindType.values();
         double yOffset = ey + 1.0;
         BindType[] var28 = types;
         int var29 = types.length;

         for(int var30 = 0; var30 < var29; ++var30) {
            BindType type = var28[var30];
            boolean hovered = (double)mouseX > ex && (double)mouseX < ex + ew && (double)mouseY > yOffset && (double)mouseY < yOffset + this.buttonHeight;
            fontRenderer.draw(type.toString(), ex + ew / 2.0 - fontRenderer.getWidth(type.toString(), this.fontScale, 700) / 2.0, yOffset + this.buttonHeight / 2.0 - fontRenderer.getHeight(type.toString(), this.fontScale, 700) / 2.0, this.fontScale, 700, hovered ? this.getTheme().getHighlightedTextColour() : this.getTheme().getTextColour());
            yOffset += this.buttonHeight;
         }

         GL11.glPopMatrix();
      }

   }

   public void onMouseClick(int mouseX, int mouseY, int button) {
      if (this.binding) {
         this.bindSetter.accept(new Bind(InputType.MOUSE, button, this.selectedBindType));
         KetamineClient.getInstance().getNotificationManager().add(NotificationType.INFO, String.format("Bound %s", this.name), String.format("Set %s to %s on %s", this.name, this.selectedBindType.toString(), Mouse.getButtonName(button)), 2000L);
         this.binding = false;
      } else {
         if (button == 0) {
            if (this.isHoveredExpanded(mouseX, mouseY)) {
               double ex = this.getExpandedX();
               double ey = this.getExpandedY();
               double ew = this.getExpandedWidth();
               BindType[] types = BindType.values();
               double yOffset = ey + 1.0;
               BindType[] var13 = types;
               int var14 = types.length;

               for(int var15 = 0; var15 < var14; ++var15) {
                  BindType type = var13[var15];
                  if ((double)mouseX > ex && (double)mouseX < ex + ew && (double)mouseY > yOffset && (double)mouseY < yOffset + this.buttonHeight) {
                     this.selectedBindType = type;
                     this.setState(ExpandState.CONTRACTING);
                     if (this.selectedBindType == BindType.NONE) {
                        this.unbindFunc.unbind();
                        return;
                     }

                     this.binding = true;
                  }

                  yOffset += this.buttonHeight;
               }

               return;
            }

            super.onMouseClick(mouseX, mouseY, button);
         } else if (button == 1 && this.bindable) {
            switch (this.state) {
               case EXPANDING:
               case EXPANDED:
                  this.setState(ExpandState.CONTRACTING);
                  break;
               case CONTRACTING:
               case CLOSED:
                  this.setState(ExpandState.EXPANDING);
                  this.clickedX = (double)mouseX;
                  this.clickedY = (double)mouseY;
            }
         }

      }
   }

   public void onKeyPress(int keyCode) {
      if (this.binding) {
         this.bindSetter.accept(new Bind(InputType.KEYBOARD, keyCode, this.selectedBindType));
         KetamineClient.getInstance().getNotificationManager().add(NotificationType.INFO, String.format("Bound %s", this.name), String.format("Set %s to %s on %s", this.name, this.selectedBindType.toString(), Keyboard.getKeyName(keyCode)), 2000L);
         this.binding = false;
      }

   }

   public void onMouseRelease(int button) {
   }

   public void setState(ExpandState state) {
      this.state = state;
   }

   public ExpandState getState() {
      return this.state;
   }

   public double getExpandedX() {
      return this.clickedX;
   }

   public double getExpandedY() {
      return this.clickedY;
   }

   public double getExpandedWidth() {
      double eh = this.calculateExpandedHeight();
      if (this.lastExpandedHeight != eh) {
         BindType[] types = BindType.values();
         this.buttonHeight = eh / (double)types.length;
         this.fontScale = (this.buttonHeight - 2.0) / 32.0;
         double longest = 0.0;
         CustomFontRenderer fontRenderer = KetamineClient.getInstance().getFontRenderer();
         BindType[] var7 = types;
         int var8 = types.length;

         for(int var9 = 0; var9 < var8; ++var9) {
            BindType type = var7[var9];
            double length = fontRenderer.getWidth(type.toString(), this.fontScale, 700) + 4.0;
            if (length > longest) {
               longest = length;
            }
         }

         this.expandedWidth = longest;
         this.lastExpandedHeight = eh;
      }

      return this.expandedWidth;
   }

   public double calculateExpandedHeight() {
      return this.getHeight() + 4.0;
   }

   public double getExpandProgress() {
      return this.progress;
   }

   public void resetAnimationState() {
      this.setState(ExpandState.CLOSED);
      this.progress = 0.0;
   }

   @FunctionalInterface
   public interface Unbind {
      void unbind();
   }
}
