package io.github.nevalackin.client.api.ui.framework;

import io.github.nevalackin.client.api.ui.cfont.CustomFontRenderer;
import io.github.nevalackin.client.impl.core.KetamineClient;
import io.github.nevalackin.client.impl.module.render.overlay.Gui;
import io.github.nevalackin.client.impl.ui.click.components.PanelComponent;
import io.github.nevalackin.client.impl.ui.nl.components.Theme;
import io.github.nevalackin.client.util.render.DrawUtil;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.gui.ScaledResolution;

public abstract class Component {
   protected static final CustomFontRenderer FONT_RENDERER = KetamineClient.getInstance().getFontRenderer();
   private final List children = new ArrayList();
   private final Component parent;
   private double x;
   private double y;
   private double width;
   private double height;
   private double scissorBottom;
   protected double transformOffset;

   public Component(Component parent, double x, double y, double width, double height) {
      this.parent = parent;
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
   }

   public Component getParent() {
      return this.parent;
   }

   public void addChild(Component child) {
      this.children.add(child);
   }

   public void onDraw(ScaledResolution scaledResolution, int mouseX, int mouseY) {
      Iterator var4 = this.children.iterator();

      while(var4.hasNext()) {
         Component child = (Component)var4.next();
         child.onDraw(scaledResolution, mouseX, mouseY);
      }

   }

   public void onMouseClick(int mouseX, int mouseY, int button) {
      Iterator var4 = this.children.iterator();

      while(var4.hasNext()) {
         Component child = (Component)var4.next();
         child.onMouseClick(mouseX, mouseY, button);
      }

   }

   public void onMouseRelease(int button) {
      Iterator var2 = this.children.iterator();

      while(var2.hasNext()) {
         Component child = (Component)var2.next();
         child.onMouseRelease(button);
      }

   }

   public void onKeyPress(int keyCode) {
      Iterator var2 = this.children.iterator();

      while(var2.hasNext()) {
         Component child = (Component)var2.next();
         child.onKeyPress(keyCode);
      }

   }

   public double getTransformOffset() {
      Component familyMember = this.parent;

      double familyTreeTransform;
      for(familyTreeTransform = this.transformOffset; familyMember != null; familyMember = familyMember.parent) {
         familyTreeTransform += familyMember.transformOffset;
      }

      return familyTreeTransform;
   }

   protected void glScissorBox(double x, double y, double width, double height, ScaledResolution scaledResolution) {
      this.glScissorBox(x, y, width, height, true, scaledResolution);
   }

   protected void glScissorBox(double x, double y, double width, double height, boolean useTransform, ScaledResolution scaledResolution) {
      double yPos = useTransform ? y + this.getTransformOffset() : y;
      if (this.parent == null || !(yPos > this.parent.scissorBottom)) {
         double currentScissorBottom = yPos + height;
         if (this.parent != null && currentScissorBottom > this.parent.scissorBottom) {
            DrawUtil.glScissorBox(x, yPos, width, height - (currentScissorBottom - this.parent.scissorBottom), scaledResolution);
         } else {
            this.scissorBottom = currentScissorBottom;
            DrawUtil.glScissorBox(x, yPos, width, height, scaledResolution);
         }

      }
   }

   public double getX() {
      Component familyMember = this.parent;

      double familyTreeX;
      for(familyTreeX = this.x; familyMember != null; familyMember = familyMember.parent) {
         familyTreeX += familyMember.x;
      }

      return familyTreeX;
   }

   public void setX(double x) {
      this.x = x;
   }

   public boolean isHovered(int mouseX, int mouseY) {
      double x = this.getX();
      double y = this.getY();
      return (double)mouseX > x && (double)mouseY > y && (double)mouseX < x + this.getWidth() && (double)mouseY < y + this.getHeight();
   }

   public double getY() {
      Component familyMember = this.parent;

      double familyTreeY;
      for(familyTreeY = this.y; familyMember != null; familyMember = familyMember.parent) {
         familyTreeY += familyMember.y;
      }

      return familyTreeY;
   }

   public Theme getTheme() {
      return this.parent.getTheme();
   }

   public void setY(double y) {
      this.y = y;
   }

   public double getWidth() {
      return this.width;
   }

   public void setWidth(double width) {
      this.width = width;
   }

   public double getHeight() {
      return this.height;
   }

   public void setHeight(double height) {
      this.height = height;
   }

   public boolean hasChildren() {
      return !this.children.isEmpty();
   }

   public List getChildren() {
      return this.children;
   }

   public int getColour(Component child) {
      for(Component familyMember = this.parent; familyMember != null; familyMember = familyMember.parent) {
         if (familyMember instanceof PanelComponent) {
            return familyMember.getColour(child);
         }
      }

      return -1;
   }

   public int getTextOffset(Component child) {
      for(Component familyMember = this.parent; familyMember != null; familyMember = familyMember.parent) {
         if (familyMember instanceof PanelComponent) {
            return familyMember.getTextOffset(child);
         }
      }

      return (Boolean)Gui.oldGuiProperty.getValue() ? 4 : -25;
   }
}
