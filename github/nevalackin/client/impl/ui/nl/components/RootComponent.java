package io.github.nevalackin.client.impl.ui.nl.components;

import io.github.nevalackin.client.api.binding.Bind;
import io.github.nevalackin.client.api.module.Category;
import io.github.nevalackin.client.api.property.Property;
import io.github.nevalackin.client.api.ui.cfont.CustomFontRenderer;
import io.github.nevalackin.client.api.ui.framework.Component;
import io.github.nevalackin.client.impl.core.KetamineClient;
import io.github.nevalackin.client.impl.property.BooleanProperty;
import io.github.nevalackin.client.impl.property.DoubleProperty;
import io.github.nevalackin.client.impl.property.EnumProperty;
import io.github.nevalackin.client.impl.property.MultiSelectionEnumProperty;
import io.github.nevalackin.client.impl.ui.nl.components.page.PageComponent;
import io.github.nevalackin.client.impl.ui.nl.components.page.groupBox.GroupBoxComponent;
import io.github.nevalackin.client.impl.ui.nl.components.page.groupBox.components.CheckBoxComponent;
import io.github.nevalackin.client.impl.ui.nl.components.page.groupBox.components.DropDownComponent;
import io.github.nevalackin.client.impl.ui.nl.components.page.groupBox.components.GroupBoxHeaderComponent;
import io.github.nevalackin.client.impl.ui.nl.components.page.groupBox.components.SliderComponent;
import io.github.nevalackin.client.impl.ui.nl.components.selector.PageSelector;
import io.github.nevalackin.client.impl.ui.nl.components.selector.PageSelectorComponent;
import io.github.nevalackin.client.util.render.BlurUtil;
import io.github.nevalackin.client.util.render.DrawUtil;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;

public final class RootComponent extends WindowComponent implements PageSelector {
   private final PageSelectorComponent pageSelector;
   private final HeaderComponent header;
   private PageComponent currentPage;
   private int currentIdx = -1;
   private final List pageComponents = new ArrayList();
   private final Theme theme;
   private static final double WIDTH = 840.0;
   private static final double HEIGHT = 740.0;

   public RootComponent(ScaledResolution scaledResolution) {
      super((Component)null, 0.0, 0.0, 840.0 / (double)scaledResolution.getScaleFactor(), 740.0 / (double)scaledResolution.getScaleFactor());
      this.theme = Theme.BLUE;
      this.setResizable(true);
      this.setDraggable(true);
      this.setX((double)scaledResolution.getScaledWidth() / 2.0 - this.getWidth() / 2.0);
      this.setY((double)scaledResolution.getScaledHeight() / 2.0 - this.getHeight() / 2.0);
      this.header = new HeaderComponent(this);
      this.addChild(this.header);
      this.pageSelector = new PageSelectorComponent(this);
      this.addChild(this.pageSelector);
      Category[] var2 = Category.values();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         Category category = var2[var4];
         Category.SubCategory[] var6 = category.getSubCategories();
         int var7 = var6.length;

         for(int var8 = 0; var8 < var7; ++var8) {
            final Category.SubCategory subCategory = var6[var8];
            this.pageComponents.add(new PageComponent(this) {
               public void onInit() {
                  KetamineClient.getInstance().getModuleManager().getModules().stream().filter((module) -> {
                     return module.getSubCategory() == subCategory;
                  }).forEach((module) -> {
                     this.addChild(new GroupBoxComponent(this) {
                        public void onInit() {
                           this.addChild(new GroupBoxHeaderComponent(this, module.getName(), module::setEnabled, module::isEnabled, module::setBind, module::getBind, () -> {
                              module.setBind((Bind)null);
                           }, 20.0));
                           Iterator var1 = module.getProperties().iterator();

                           while(var1.hasNext()) {
                              Property property = (Property)var1.next();
                              if (property instanceof BooleanProperty) {
                                 BooleanProperty booleanProperty = (BooleanProperty)property;
                                 this.addChild(new CheckBoxComponent(this, property.getName(), booleanProperty::getValue, booleanProperty::setValue, booleanProperty::check, 14.0));
                              } else if (property instanceof DoubleProperty) {
                                 DoubleProperty doubleProperty = (DoubleProperty)property;
                                 this.addChild(new SliderComponent(this, property.getName(), doubleProperty::getDisplayString, doubleProperty::setValue, doubleProperty::getValue, doubleProperty::getMin, doubleProperty::getMax, doubleProperty::getInc, doubleProperty::check, 14.0));
                              } else if (property instanceof EnumProperty) {
                                 EnumProperty enumPropertyx = (EnumProperty)property;
                                 this.addChild(new DropDownComponent(this, property.getName(), enumPropertyx::setValue, (Consumer)null, (Supplier)null, () -> {
                                    return ((Enum)enumPropertyx.getValue()).ordinal();
                                 }, enumPropertyx::getValueNames, enumPropertyx::check, false, 14.0));
                              } else if (property instanceof MultiSelectionEnumProperty) {
                                 MultiSelectionEnumProperty enumProperty = (MultiSelectionEnumProperty)property;
                                 this.addChild(new DropDownComponent(this, property.getName(), enumProperty::select, enumProperty::unselect, enumProperty::getValueIndices, (Supplier)null, enumProperty::getValueNames, enumProperty::check, true, 14.0));
                              }
                           }

                        }
                     });
                  });
               }
            });
         }
      }

   }

   public Theme getTheme() {
      return this.theme;
   }

   public void onPageSelect(int idx, double y) {
      if (!this.pageComponents.isEmpty()) {
         int currentIdx = Math.min(this.pageComponents.size() - 1, Math.max(0, idx));
         if (this.currentIdx != currentIdx) {
            this.currentIdx = currentIdx;
            this.currentPage = (PageComponent)this.pageComponents.get(currentIdx);
            this.currentPage.pageSelectorButtonY = y;
            this.currentPage.resetAnimationState();
         }

      }
   }

   public int getSelectedIdx() {
      return this.currentIdx;
   }

   public void onDraw(ScaledResolution scaledResolution, int mouseX, int mouseY) {
      double x = this.getX();
      double y = this.getY();
      double width = this.getWidth();
      double height = this.getHeight();
      this.setWidth(840.0 / (double)scaledResolution.getScaleFactor());
      this.setHeight(740.0 / (double)scaledResolution.getScaleFactor());
      this.pageSelector.setY(this.header.getHeight());
      this.pageSelector.setHeight(this.getHeight() - this.header.getHeight());
      this.header.setX(this.pageSelector.getWidth());
      this.header.setWidth(this.getWidth() - this.pageSelector.getWidth());
      this.fadeInProgress = DrawUtil.animateProgress(this.fadeInProgress, 1.0, 5.0);
      double smoothFadeIn = DrawUtil.bezierBlendAnimation(this.fadeInProgress);
      GL11.glTranslated(x + width / 2.0, y + height / 2.0, 0.0);
      GL11.glScaled(smoothFadeIn, smoothFadeIn, 1.0);
      GL11.glTranslated(-(x + width / 2.0), -(y + height / 2.0), 0.0);
      if (smoothFadeIn >= 1.0) {
         this.onHandleDragging((double)mouseX, (double)mouseY);
      }

      double pageSelectorWidth = this.pageSelector.getWidth();
      double watermarkTextX = 1.0;
      if (smoothFadeIn >= 1.0) {
         BlurUtil.blurArea(x, y, pageSelectorWidth, height);
      }

      DrawUtil.glDrawRoundedQuad(x + 2.0, y, (float)pageSelectorWidth, (float)height, 6.0F, this.getTheme().getPageSelectorBackgroundColour());
      DrawUtil.glDrawFilledQuad(x + pageSelectorWidth - 1.0, y, 1.0, height, this.getTheme().getPageSelectorPageSeparatorColour());
      DrawUtil.glDrawRoundedRect(x + pageSelectorWidth, y, width - pageSelectorWidth, height, DrawUtil.RoundingMode.RIGHT, 4.0F, (float)scaledResolution.getScaleFactor(), this.getTheme().getPageBackgroundColour());
      String watermarkText = "§L" + KetamineClient.getInstance().getName().toUpperCase();
      CustomFontRenderer fontRenderer = KetamineClient.getInstance().getFontRenderer();
      watermarkTextX = x + this.pageSelector.getWidth() / 2.0 - fontRenderer.getWidth(watermarkText, 0.5) / 2.0;
      double watermarkTextY = y + this.header.getHeight() / 2.0 - fontRenderer.getHeight(watermarkText, 0.5) / 2.0;
      fontRenderer.draw(watermarkText, watermarkTextX + 0.5, watermarkTextY, 0.475, 900, this.getTheme().getWatermarkTextColour());
      super.onDraw(scaledResolution, mouseX, mouseY);
      if (this.currentPage != null) {
         this.currentPage.setX(this.pageSelector.getWidth());
         this.currentPage.setY(this.header.getHeight());
         this.currentPage.setWidth(this.getWidth() - this.pageSelector.getWidth());
         this.currentPage.setHeight(this.getHeight() - this.header.getHeight());
         this.currentPage.onDraw(scaledResolution, mouseX, mouseY);
      }

      GL11.glScaled(-smoothFadeIn, -smoothFadeIn, 1.0);
   }

   public void onMouseClick(int mouseX, int mouseY, int button) {
      if (!(this.fadeInProgress < 1.0)) {
         Iterator var4 = this.getChildren().iterator();

         Component child;
         while(var4.hasNext()) {
            child = (Component)var4.next();
            if (child.isHovered(mouseX, mouseY)) {
               child.onMouseClick(mouseX, mouseY, button);
               return;
            }
         }

         if (this.currentPage != null) {
            var4 = this.currentPage.getChildren().iterator();

            while(var4.hasNext()) {
               child = (Component)var4.next();
               if (child.isHovered(mouseX, mouseY)) {
                  child.onMouseClick(mouseX, mouseY, button);
                  return;
               }
            }
         }

         if (button == 0 && this.isHovered(mouseX, mouseY)) {
            this.onStartDragging((double)mouseX, (double)mouseY);
         }

      }
   }

   public void onMouseRelease(int button) {
      if (button == 0) {
         this.onStopDragging();
      }

      super.onMouseRelease(button);
      if (this.currentPage != null) {
         this.currentPage.onMouseRelease(button);
      }

   }

   public void onKeyPress(int keyCode) {
      if (this.currentPage != null) {
         this.currentPage.onKeyPress(keyCode);
      }

   }
}
