package io.github.nevalackin.client.impl.module.render.overlay;

import io.github.nevalackin.client.api.module.Category;
import io.github.nevalackin.client.api.module.Module;
import io.github.nevalackin.client.api.property.Property;
import io.github.nevalackin.client.impl.property.BooleanProperty;
import io.github.nevalackin.client.impl.property.DoubleProperty;
import io.github.nevalackin.client.impl.property.EnumProperty;

public class Gui extends Module {
   public static final EnumProperty guiModeProperty;
   public static final BooleanProperty oldGuiProperty;
   public static final BooleanProperty circleProperty;
   public static final BooleanProperty transparentProperty;
   public static final BooleanProperty blurProperty;
   public static final EnumProperty imageModeProperty;
   public static final DoubleProperty imageScaleProperty;

   public Gui() {
      super("Gui", Category.RENDER, Category.SubCategory.RENDER_OVERLAY);
      this.register(new Property[]{guiModeProperty, oldGuiProperty, circleProperty, transparentProperty, blurProperty, imageModeProperty, imageScaleProperty});
   }

   private static boolean isDropdown() {
      return guiModeProperty.getValue() == Gui.GuiMode.DROPDOWN;
   }

   private static boolean isntTransparent() {
      return !(Boolean)transparentProperty.getValue();
   }

   public void onEnable() {
   }

   public void onDisable() {
   }

   static {
      guiModeProperty = new EnumProperty("Mode", Gui.GuiMode.DROPDOWN);
      oldGuiProperty = new BooleanProperty("Old Gui", false, () -> {
         return isDropdown() && isntTransparent();
      });
      circleProperty = new BooleanProperty("Circles", false, () -> {
         return isDropdown() && (Boolean)oldGuiProperty.getValue();
      });
      transparentProperty = new BooleanProperty("Transparent", false, () -> {
         return isDropdown() && !(Boolean)oldGuiProperty.getValue();
      });
      blurProperty = new BooleanProperty("Blur", false, () -> {
         return isDropdown() && !(Boolean)oldGuiProperty.getValue() && (Boolean)transparentProperty.getValue();
      });
      imageModeProperty = new EnumProperty("Image", Gui.ImageMode.NONE);
      imageScaleProperty = new DoubleProperty("Image Scale", 0.4, 0.1, 1.0, 0.1);
   }

   public static enum ImageMode {
      NONE("None"),
      NEZUKO("Nezuko"),
      KANCOLLE("Kancolle"),
      AKAME("Akame");

      private final String name;

      private ImageMode(String name) {
         this.name = name;
      }

      public String toString() {
         return this.name;
      }
   }

   public static enum GuiMode {
      DROPDOWN("Dropdown"),
      NEVERLOSE("Neverlose");

      private final String name;

      private GuiMode(String name) {
         this.name = name;
      }

      public String toString() {
         return this.name;
      }
   }
}
