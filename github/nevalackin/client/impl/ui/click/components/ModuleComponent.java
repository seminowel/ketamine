package io.github.nevalackin.client.impl.ui.click.components;

import io.github.nevalackin.client.api.binding.Bind;
import io.github.nevalackin.client.api.binding.BindType;
import io.github.nevalackin.client.api.module.Module;
import io.github.nevalackin.client.api.notification.NotificationType;
import io.github.nevalackin.client.api.property.Property;
import io.github.nevalackin.client.api.ui.framework.Component;
import io.github.nevalackin.client.api.ui.framework.ExpandState;
import io.github.nevalackin.client.api.ui.framework.Expandable;
import io.github.nevalackin.client.api.ui.framework.Predicated;
import io.github.nevalackin.client.impl.core.KetamineClient;
import io.github.nevalackin.client.impl.event.game.input.InputType;
import io.github.nevalackin.client.impl.module.render.overlay.Gui;
import io.github.nevalackin.client.impl.property.BooleanProperty;
import io.github.nevalackin.client.impl.property.ColourProperty;
import io.github.nevalackin.client.impl.property.DoubleProperty;
import io.github.nevalackin.client.impl.property.EnumProperty;
import io.github.nevalackin.client.impl.property.MultiSelectionEnumProperty;
import io.github.nevalackin.client.impl.ui.click.components.sub.CheckBoxComponent;
import io.github.nevalackin.client.impl.ui.click.components.sub.ColourPickerComponent;
import io.github.nevalackin.client.impl.ui.click.components.sub.DropDownComponent;
import io.github.nevalackin.client.impl.ui.click.components.sub.SliderComponent;
import io.github.nevalackin.client.util.render.ColourUtil;
import io.github.nevalackin.client.util.render.DrawUtil;
import java.util.Iterator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public final class ModuleComponent extends Component implements Expandable {
   private final Module module;
   private boolean binding;
   private boolean expandingCircle;
   private double circleProgress;
   public double enableProgress;
   private int mouseX;
   private int mouseY;
   private ExpandState state;
   private double progress;
   private final boolean last;
   private final ModuleComponent previousModuleComponent;

   public ModuleComponent(Component parent, Module module, double x, double y, double width, double height, boolean last, ModuleComponent previousModuleComponent) {
      super(parent, x, y, width, height);
      this.state = ExpandState.CLOSED;
      this.module = module;
      this.last = last;
      this.previousModuleComponent = previousModuleComponent;
      Iterator properties = module.getProperties().iterator();

      while(true) {
         while(properties.hasNext()) {
            Property property = (Property)properties.next();
            if (property instanceof BooleanProperty) {
               this.addChild(new CheckBoxComponent(this, (BooleanProperty)property, 0.0, 0.0, this.getExpandedWidth(), 16.0));
            } else if (property instanceof EnumProperty) {
               this.addChild(new DropDownComponent(this, (EnumProperty)property, 0.0, 0.0, this.getExpandedWidth(), 16.0, last && !properties.hasNext()));
            } else if (property instanceof DoubleProperty) {
               this.addChild(new SliderComponent(this, (DoubleProperty)property, 0.0, 0.0, this.getExpandedWidth(), 20.0));
            } else if (property instanceof ColourProperty) {
               this.addChild(new ColourPickerComponent(this, (ColourProperty)property, 0.0, 0.0, this.getExpandedWidth(), 16.0, last && !properties.hasNext()));
            } else if (property instanceof MultiSelectionEnumProperty) {
               this.addChild(new DropDownComponent(this, (MultiSelectionEnumProperty)property, 0.0, 0.0, this.getExpandedWidth(), 16.0, last && !properties.hasNext()));
            }
         }

         return;
      }
   }

   public void onDraw(ScaledResolution scaledResolution, int mouseX, int mouseY) {
      double x = this.getX();
      double y = this.getY();
      double w = this.getWidth();
      double h = this.getHeight();
      boolean enabled = this.module.isEnabled();
      double moduleBackgroundOffset = x + 26.0 + 2.0;
      String text = this.binding ? "Press a key..." : this.module.getName();
      boolean needScissor = this.progress != 1.0;
      double expandedHeight = this.calculateExpandedHeight();
      double ex = this.getExpandedX();
      double ey = this.getExpandedY();
      double ew = this.getExpandedWidth();
      double eh = expandedHeight * DrawUtil.bezierBlendAnimation(this.progress);
      int colour = this.getColour(this);
      int offset = this.getTextOffset(this);
      double speed = 6.0 - Math.min(4.0, Math.sqrt(expandedHeight) / 5.0);
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

      int currentColour = ColourUtil.fadeBetween(1973790, colour, this.enableProgress);
      int previousColour = currentColour;
      if (this.enableProgress > 0.0 && this.previousModuleComponent != null) {
         previousColour = ColourUtil.fadeBetween(1973790, this.getColour(this.previousModuleComponent), this.enableProgress);
      }

      float buttonSize = 8.0F;
      if ((Boolean)Gui.oldGuiProperty.getValue()) {
         if ((Boolean)Gui.circleProperty.getValue()) {
            DrawUtil.glDrawArcOutline(x + 13.0 - 4.0 + 3.5, y + h / 2.0 - 4.0 + 3.75, 4.5F, 0.0F, 360.0F, 1.25F, colour);
         } else {
            DrawUtil.glDrawFilledQuad(x + 13.0 - 4.0 - 0.5, y + h / 2.0 - 4.0 - 0.5, 8.0, 8.0, colour);
            DrawUtil.glDrawFilledQuad(x + 13.0 - 4.0, y + h / 2.0 - 4.0, 7.0, 7.0, -15461356);
         }
      }

      if (enabled) {
         if (this.enableProgress >= 1.0) {
            this.enableProgress = 1.0;
         } else {
            this.enableProgress += 1.0 / (double)Minecraft.getDebugFPS() * 5.0;
         }
      } else if (this.enableProgress <= 0.0) {
         this.enableProgress = 0.0;
      } else {
         this.enableProgress -= 1.0 / (double)Minecraft.getDebugFPS() * 5.0;
      }

      double innerButton = 5.0;
      if ((Boolean)Gui.oldGuiProperty.getValue()) {
         if ((Boolean)Gui.circleProperty.getValue()) {
            DrawUtil.glDrawPoint(x + 13.0 - 4.0 + 3.5, y + h / 2.0 - 4.0 + 3.75, 6.5F, scaledResolution, ColourUtil.fadeBetween(previousColour, currentColour));
         } else {
            DrawUtil.glDrawFilledQuad(x + 13.0 - 4.0 + 1.0, y + h / 2.0 - 4.0 + 1.0, 5.0, 5.0, ColourUtil.fadeBetween(previousColour, currentColour));
         }
      } else if (this.last) {
         DrawUtil.glDrawRoundedRectEllipse(x + 13.0 - 14.0, y + h / 2.0 - 8.0, this.getWidth() + 2.0, this.getHeight() - 1.0, DrawUtil.RoundingMode.BOTTOM, 12, 4.0, previousColour);
      } else {
         DrawUtil.glDrawFilledQuad(x + 13.0 - 14.0, y + h / 2.0 - 8.0, this.getWidth() + 1.0, this.getHeight(), previousColour, currentColour);
      }

      FONT_RENDERER.draw(text, moduleBackgroundOffset + (double)offset, y + h / 2.0 - 4.0, (Boolean)Gui.transparentProperty.getValue() ? (enabled ? -1 : 1979711487) : (enabled ? -5921371 : -11184811));
      if (this.hasChildren()) {
         DrawUtil.glDrawPlusSign(x + w - 12.0, y + h / 2.0, 6.0, this.progress * 180.0, -1315861);
      }

      double yPos;
      if (this.expandingCircle && (Boolean)Gui.oldGuiProperty.getValue() && !this.isExpanded()) {
         yPos = w - 26.0 - 2.0;
         if (this.circleProgress >= 1.0) {
            this.expandingCircle = false;
         } else {
            this.circleProgress += 1.0 / (double)Minecraft.getDebugFPS() * 2.0;
         }

         this.glScissorBox(moduleBackgroundOffset, y, yPos, h, scaledResolution);
         DrawUtil.glDrawFilledEllipse((double)this.mouseX, (double)this.mouseY, (float)(this.circleProgress * yPos), 16777215 + (128 - (int)(this.circleProgress * 118.0) << 24));
         DrawUtil.glEndScissor();
      }

      if (this.isExpanded()) {
         yPos = 7.0;
         double triangleH = 3.0 * this.progress;
         double xPosTriangle = moduleBackgroundOffset + (double)offset + 8.0;
         GL11.glTranslated(xPosTriangle, ey, 0.0);
         DrawUtil.glDrawTriangle(0.0, -triangleH, -7.0, 0.0, 7.0, 0.0, 1612323099);
         GL11.glTranslated(-xPosTriangle, -ey, 0.0);
         if ((Boolean)Gui.oldGuiProperty.getValue()) {
            if (this.last) {
               DrawUtil.glDrawRoundedRectEllipse(ex, ey, ew, eh, DrawUtil.RoundingMode.BOTTOM, 12, 3.0, -15066853);
            } else {
               DrawUtil.glDrawFilledQuad(ex, ey, ew, eh, -15066853);
            }
         } else if (this.last) {
            DrawUtil.glDrawRoundedQuad(ex, ey, (float)ew, (float)eh, 3.0F, 1612323099);
         } else {
            DrawUtil.glDrawFilledQuad(ex, ey, ew, eh + 2.0, 1612323099);
         }

         yPos = this.getHeight();
         Iterator var40 = this.getChildren().iterator();

         while(true) {
            Component component;
            Predicated Predicated;
            do {
               if (!var40.hasNext()) {
                  return;
               }

               component = (Component)var40.next();
               if (!(component instanceof Predicated)) {
                  break;
               }

               Predicated = (Predicated)component;
            } while(!Predicated.isVisible());

            component.setY(yPos);
            if (needScissor) {
               this.glScissorBox(ex, ey, ew, eh, scaledResolution);
            }

            component.onDraw(scaledResolution, mouseX, mouseY);
            if (component instanceof Expandable) {
               Expandable Expandable = (Expandable)component;
               if (Expandable.isExpanded()) {
                  yPos += Expandable.getExpandedHeight();
               }
            }

            if (needScissor) {
               DrawUtil.glRestoreScissor();
            }

            yPos += component.getHeight();
         }
      }
   }

   public void onMouseClick(int mouseX, int mouseY, int mouseButton) {
      double yPos;
      if (this.isHovered(mouseX, mouseY)) {
         switch (mouseButton) {
            case 0:
               yPos = (double)mouseX - this.getX();
               if (yPos >= 26.0 && !this.isExpanded()) {
                  this.mouseX = mouseX;
                  this.mouseY = mouseY;
                  this.circleProgress = 0.0;
                  this.expandingCircle = true;
               }

               this.module.toggle();
               return;
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

                  return;
               }
               break;
            case 2:
               this.binding = true;
               return;
         }
      }

      if (this.isExpanded() && this.isHoveredExpand(mouseX, mouseY)) {
         yPos = this.getHeight();
         Iterator var6 = this.getChildren().iterator();

         while(true) {
            Component component;
            Predicated predicateComponent;
            do {
               if (!var6.hasNext()) {
                  return;
               }

               component = (Component)var6.next();
               if (!(component instanceof Predicated)) {
                  break;
               }

               predicateComponent = (Predicated)component;
            } while(!predicateComponent.isVisible());

            component.setY(yPos);
            component.onMouseClick(mouseX, mouseY, mouseButton);
            if (component instanceof Expandable) {
               Expandable expandableComponent = (Expandable)component;
               if (expandableComponent.isExpanded()) {
                  yPos += expandableComponent.getExpandedHeight();
               }
            }

            yPos += component.getHeight();
         }
      }
   }

   public void onKeyPress(int keyCode) {
      if (!this.binding) {
         super.onKeyPress(keyCode);
      } else {
         if (keyCode != 54 && keyCode != 1) {
            if (keyCode != 211 && keyCode != 14) {
               this.module.setBind(new Bind(InputType.KEYBOARD, keyCode, BindType.TOGGLE));
               KetamineClient.getInstance().getNotificationManager().add(NotificationType.INFO, String.format("Bound %s", this.module.getName()), String.format("Set %s bind to %s", this.module.getName(), Keyboard.getKeyName(keyCode)), 2000L);
            } else {
               this.module.setBind((Bind)null);
               KetamineClient.getInstance().getNotificationManager().add(NotificationType.INFO, "Unbound Module", String.format("Unbound %s", this.module.getName()), 2000L);
            }

            this.binding = false;
         }

      }
   }

   public double calculateExpandedHeight() {
      double height = 0.0;
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

         if (component instanceof Expandable) {
            Expandable Expandable = (Expandable)component;
            if (Expandable.isExpanded()) {
               height += Expandable.getExpandedHeight();
            }
         }

         height += component.getHeight();
      }
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

   public ExpandState getState() {
      return this.state;
   }

   public double getExpandProgress() {
      return this.progress;
   }
}
