package io.github.nevalackin.client.impl.ui.click.components.sub;

import io.github.nevalackin.client.api.ui.framework.Component;
import io.github.nevalackin.client.api.ui.framework.ExpandState;
import io.github.nevalackin.client.api.ui.framework.Expandable;
import io.github.nevalackin.client.api.ui.framework.Predicated;
import io.github.nevalackin.client.impl.property.EnumProperty;
import io.github.nevalackin.client.impl.property.MultiSelectionEnumProperty;
import io.github.nevalackin.client.util.render.DrawUtil;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;

public final class DropDownComponent extends Component implements Predicated, Expandable {
   private EnumProperty property;
   private MultiSelectionEnumProperty multiSelectionEnumProperty;
   private ExpandState state;
   private double progress;
   private final boolean last;
   private final boolean isMultiSelect;

   public DropDownComponent(Component parent, EnumProperty property, double x, double y, double width, double height, boolean last) {
      super(parent, x, y, width, height);
      this.state = ExpandState.CLOSED;
      this.property = property;
      this.last = last;
      this.isMultiSelect = false;
   }

   public DropDownComponent(Component parent, MultiSelectionEnumProperty property, double x, double y, double width, double height, boolean last) {
      super(parent, x, y, width, height);
      this.state = ExpandState.CLOSED;
      this.multiSelectionEnumProperty = property;
      this.last = last;
      this.isMultiSelect = true;
   }

   public void onDraw(ScaledResolution scaledResolution, int mouseX, int mouseY) {
      double x = this.getX();
      double y = this.getY();
      double w = this.getWidth();
      double h = this.getHeight();
      double textDrawPos = y + h / 2.0 - 4.0;
      FONT_RENDERER.draw(this.getPropertyName(), x + 4.0, textDrawPos, -1315861);
      FONT_RENDERER.draw(this.getPropertyValueString(), x + w - FONT_RENDERER.getWidth(this.getPropertyValueString()) - 4.0, textDrawPos, -5921371);
      double expandedHeight = this.calculateExpandedHeight();
      switch (this.getState()) {
         case CONTRACTING:
            if (this.progress <= 0.0) {
               this.setState(ExpandState.CLOSED);
            } else {
               this.progress -= 1.0 / (double)Minecraft.getDebugFPS() * 5.0;
            }
            break;
         case EXPANDING:
            if (this.progress >= 1.0) {
               this.setState(ExpandState.EXPANDED);
            } else {
               this.progress += 1.0 / (double)Minecraft.getDebugFPS() * 5.0;
            }
      }

      if (this.isExpanded()) {
         double ex = this.getExpandedX();
         double ey = this.getExpandedY();
         double ew = this.getExpandedWidth();
         double eh = expandedHeight * this.progress;
         double triangleW = 7.0;
         double triangleH = 3.0 * this.progress;
         double xPosTriangle = x + 4.0 + 12.0;
         GL11.glTranslated(xPosTriangle, ey, 0.0);
         DrawUtil.glDrawTriangle(0.0, -triangleH, -7.0, 0.0, 7.0, 0.0, 1612323099);
         GL11.glTranslated(-xPosTriangle, -ey, 0.0);
         boolean needScissor = this.progress != 1.0;
         if (this.last) {
            DrawUtil.glDrawRoundedQuad(ex, ey, (float)ew, (float)eh, 3.0F, 1612323099);
         } else {
            DrawUtil.glDrawFilledQuad(ex, ey, ew, eh + 2.0, 1612323099);
         }

         if (needScissor) {
            this.glScissorBox(ex, ey, ew, eh, scaledResolution);
         }

         double yPos = ey + 2.0;
         if (this.isMultiSelect) {
            for(int i = 0; i < this.multiSelectionEnumProperty.getValues().length; ++i) {
               String enumName = this.multiSelectionEnumProperty.getValues()[i].toString();
               FONT_RENDERER.draw(enumName, ex + ew / 2.0 - FONT_RENDERER.getWidth(enumName) / 2.0, yPos, ((List)this.multiSelectionEnumProperty.getValue()).contains(this.multiSelectionEnumProperty.getValues()[i]) ? -5921371 : -11184811);
               yPos += 14.0;
            }
         } else {
            Enum[] var27 = this.property.getValues();
            int var33 = var27.length;

            for(int var29 = 0; var29 < var33; ++var29) {
               Enum value = var27[var29];
               FONT_RENDERER.draw(value.toString(), ex + ew / 2.0 - FONT_RENDERER.getWidth(value.toString()) / 2.0, yPos, value == this.property.getValue() ? -5921371 : -11184811);
               yPos += 14.0;
            }
         }

         if (needScissor) {
            DrawUtil.glRestoreScissor();
         }
      }

   }

   public void onMouseClick(int mouseX, int mouseY, int button) {
      if (this.isHovered(mouseX, mouseY) && button == 1) {
         if ((this.isMultiSelect ? this.multiSelectionEnumProperty.getValues().length : this.property.getValues().length) > 0) {
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

      } else {
         if (button == 0 && this.isExpanded() && this.isHoveredExpand(mouseX, mouseY)) {
            double yPos = this.getExpandedY() + 2.0;
            int i;
            if (this.isMultiSelect) {
               for(i = 0; i < this.multiSelectionEnumProperty.getValues().length; ++i) {
                  if ((double)mouseY >= this.getExpandedY() + 2.0 + (double)(i * 14) && (double)mouseY <= this.getExpandedY() + 2.0 + (double)(i * 14) + 14.0) {
                     if (this.multiSelectionEnumProperty.isSelected(i)) {
                        this.multiSelectionEnumProperty.unselect(i);
                     } else {
                        this.multiSelectionEnumProperty.select(i);
                     }
                  }
               }
            } else {
               for(i = 0; i < this.property.getValues().length; ++i) {
                  if ((double)mouseY > yPos && (double)mouseY < yPos + 14.0) {
                     this.property.setValue(i);
                     this.setState(ExpandState.CONTRACTING);
                     return;
                  }

                  yPos += 14.0;
               }
            }
         }

      }
   }

   public boolean isVisible() {
      return this.isMultiSelect ? this.multiSelectionEnumProperty.check() : this.property.check();
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

   public double calculateExpandedHeight() {
      return this.isMultiSelect ? (double)this.multiSelectionEnumProperty.getValues().length * 14.0 : (double)this.property.getValues().length * 14.0;
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

   public double getExpandProgress() {
      return this.progress;
   }

   private String getPropertyName() {
      return this.isMultiSelect ? this.multiSelectionEnumProperty.getName() : this.property.getName();
   }

   private String getPropertyValueString() {
      return this.isMultiSelect ? String.format("%d Selected", ((List)this.multiSelectionEnumProperty.getValue()).size()) : ((Enum)this.property.getValue()).toString();
   }
}
