package io.github.nevalackin.client.impl.ui.nl.components.selector;

import io.github.nevalackin.client.api.module.Category;
import io.github.nevalackin.client.api.ui.cfont.StaticallySizedImage;
import io.github.nevalackin.client.api.ui.framework.Component;
import io.github.nevalackin.client.util.misc.ResourceUtil;
import java.io.IOException;
import javax.imageio.ImageIO;

public final class PageSelectorComponent extends Component implements PageSelector {
   private double pageButtonOffset;
   private final double margin = 4.0;
   private final double pageButtonWidth = this.getWidth() - 8.0;

   public PageSelectorComponent(Component parent) {
      super(parent, 0.0, 0.0, 0.0, 0.0);
      Category[] var2 = Category.values();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         Category category = var2[var4];
         this.addPageLabel(category.toString());
         Category.SubCategory[] var6 = category.getSubCategories();
         int var7 = var6.length;

         for(int var8 = 0; var8 < var7; ++var8) {
            Category.SubCategory subCategory = var6[var8];
            this.addPageButton(subCategory.toString(), String.format("icons/ui/%s/%s.png", category.name().toLowerCase(), subCategory.toString().toLowerCase()), subCategory.ordinal());
         }
      }

   }

   public double getWidth() {
      return this.getParent().getWidth() / 5.0;
   }

   private void addPageLabel(String label) {
      this.getClass();
      PageSelectorLabelComponent labelComponent = new PageSelectorLabelComponent(this, label, 4.0, this.pageButtonOffset, this.pageButtonWidth, 16.0);
      this.addChild(labelComponent);
      this.pageButtonOffset += labelComponent.getHeight();
   }

   private void addPageButton(String label, String iconPath, int idx) {
      try {
         StaticallySizedImage icon = new StaticallySizedImage(ImageIO.read(ResourceUtil.getResourceStream(iconPath)), true, 3);
         this.getClass();
         PageButtonComponent pageButton = new PageButtonComponent(this, label, icon, idx, 4.0, this.pageButtonOffset, this.pageButtonWidth, 18.0);
         this.addChild(pageButton);
         this.pageButtonOffset += pageButton.getHeight();
      } catch (IOException var6) {
      }

   }
}
