package io.github.nevalackin.client.impl.ui.hud.rendered;

import com.google.common.collect.Lists;
import io.github.nevalackin.client.api.module.Category;
import io.github.nevalackin.client.api.module.Module;
import io.github.nevalackin.client.api.property.Property;
import io.github.nevalackin.client.api.ui.cfont.CustomFontRenderer;
import io.github.nevalackin.client.impl.core.KetamineClient;
import io.github.nevalackin.client.impl.property.BooleanProperty;
import io.github.nevalackin.client.impl.property.ColourProperty;
import io.github.nevalackin.client.impl.property.DoubleProperty;
import io.github.nevalackin.client.impl.property.EnumProperty;
import io.github.nevalackin.client.impl.property.MultiSelectionEnumProperty;
import io.github.nevalackin.client.impl.ui.hud.Quadrant;
import io.github.nevalackin.client.impl.ui.hud.components.HudComponent;
import io.github.nevalackin.client.util.render.BloomUtil;
import io.github.nevalackin.client.util.render.BlurUtil;
import io.github.nevalackin.client.util.render.ColourUtil;
import io.github.nevalackin.client.util.render.DrawUtil;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public final class ArraylistModule extends Module implements HudComponent {
   private double xPos;
   private double yPos;
   private double width;
   private double height;
   private boolean dragon;
   private final BooleanProperty backgroundProperty = new BooleanProperty("Background", true);
   private final BooleanProperty backgroundBlurProperty = new BooleanProperty("Blur Background", true);
   private final DoubleProperty backgroundOpacityProperty;
   private final MultiSelectionEnumProperty lineSelectionProperty;
   private final BooleanProperty hideRenderProperty;
   private final EnumProperty colourProperty;
   public final ColourProperty startColourProperty;
   public final ColourProperty endColourProperty;
   @EventLink
   private final Listener onRenderOverlay;

   public ArraylistModule() {
      super("Arraylist", Category.RENDER, Category.SubCategory.RENDER_OVERLAY);
      this.backgroundOpacityProperty = new DoubleProperty("Background Opacity", 50.0, this.backgroundProperty::getValue, 0.0, 100.0, 1.0);
      this.lineSelectionProperty = new MultiSelectionEnumProperty("Lines", Lists.newArrayList(new LineSelection[]{ArraylistModule.LineSelection.TOP, ArraylistModule.LineSelection.SIDE}), ArraylistModule.LineSelection.values());
      this.hideRenderProperty = new BooleanProperty("Hide Render Modules", true);
      this.colourProperty = new EnumProperty("Colour", ArraylistModule.Colour.CLIENT);
      this.startColourProperty = new ColourProperty("Start Colour", -393028, this::isBlend);
      this.endColourProperty = new ColourProperty("End Colour", -16718593, this::isBlend);
      this.onRenderOverlay = (event) -> {
         this.render(event.getScaledResolution().getScaledWidth(), event.getScaledResolution().getScaledHeight(), (double)event.getPartialTicks());
      };
      this.register(new Property[]{this.backgroundProperty, this.backgroundBlurProperty, this.backgroundOpacityProperty, this.lineSelectionProperty, this.hideRenderProperty, this.colourProperty, this.startColourProperty, this.endColourProperty});
      this.setX(882.0);
      this.setY(4.0);
      this.setEnabled(true);
   }

   public boolean isDragging() {
      return this.dragon;
   }

   public void setDragging(boolean dragging) {
      this.dragon = dragging;
   }

   public void render(int scaledWidth, int scaledHeight, double tickDelta) {
      this.fitInScreen(scaledWidth, scaledHeight);
      double x = this.getX();
      double y = this.getY();
      CustomFontRenderer fontRenderer = KetamineClient.getInstance().getFontRenderer();
      Comparator lengthComparator = (a, b) -> {
         double ab = fontRenderer.getWidth(a);
         double bb = fontRenderer.getWidth(b);
         return Double.compare(bb, ab);
      };
      int startColour = ((Colour)this.colourProperty.getValue()).getModColour((Integer)this.startColourProperty.getValue(), (Integer)this.endColourProperty.getValue(), 0);
      Quadrant quad = this.getQuadrant(scaledWidth, scaledHeight);
      List features = (List)KetamineClient.getInstance().getModuleManager().getModules().stream().filter(Module::isDisplayed).filter((m) -> {
         return !m.getCategory().equals(Category.RENDER) || !(Boolean)this.hideRenderProperty.getValue();
      }).map(Module::getName).sorted(quad.isBottom() ? lengthComparator.reversed() : lengthComparator).collect(Collectors.toList());
      double margin = 2.0;
      double lineWidth = 2.0;
      double elementHeight = 11.0;
      int i = 0;

      for(int size = features.size(); i < size; ++i) {
         String feature = (String)features.get(i);
         double bounds = fontRenderer.getWidth(feature);
         double elementY = y + (double)i * elementHeight;
         double elementWidth = bounds + margin * 2.0;
         this.width = Math.max(elementWidth + lineWidth, this.width);
         int colour = ((Colour)this.colourProperty.getValue()).getModColour((Integer)this.startColourProperty.getValue(), (Integer)this.endColourProperty.getValue(), i);
         if ((Boolean)this.backgroundProperty.getValue()) {
            DrawUtil.glDrawFilledQuad(quad.isRight() ? x - lineWidth + this.width - bounds - 2.0 : x + lineWidth, elementY, elementWidth, elementHeight, (int)(255.0 * ((Double)this.backgroundOpacityProperty.getValue() / 100.0)) << 24);
         }

         if ((Boolean)this.backgroundBlurProperty.getValue()) {
            BlurUtil.blurArea(quad.isRight() ? x - lineWidth + this.width - bounds - 2.0 : x + lineWidth, elementY, elementWidth, elementHeight);
         }

         if (this.lineSelectionProperty.isSelected(ArraylistModule.LineSelection.TOP) && i == 0) {
            BloomUtil.drawAndBloom(() -> {
               DrawUtil.glDrawSidewaysGradientRect(quad.isRight() ? x - lineWidth + this.width - bounds - 2.0 : x + lineWidth, elementY - 1.0, elementWidth, 1.0, startColour, colour);
            });
         }

         if (this.lineSelectionProperty.isSelected(ArraylistModule.LineSelection.SIDE)) {
            BloomUtil.drawAndBloom(() -> {
               DrawUtil.glDrawSidewaysGradientRect(quad.isRight() ? x - lineWidth + this.width + 2.0 : x + lineWidth - 1.0, elementY - 1.0, 1.0, elementHeight + 2.0, startColour, colour);
            });
         }

         if (this.lineSelectionProperty.isSelected(ArraylistModule.LineSelection.BOTTOM) && i == size - 1) {
            BloomUtil.drawAndBloom(() -> {
               DrawUtil.glDrawSidewaysGradientRect(quad.isRight() ? x - lineWidth + this.width - bounds - 2.0 : x + lineWidth, elementY + elementHeight, elementWidth, 1.0, colour, startColour);
            });
         }

         fontRenderer.drawWithShadow(feature, quad.isRight() ? x - margin - lineWidth + this.width - bounds + 1.5 : x + margin + lineWidth, elementY + margin, 0.5, colour);
         this.height = (double)i * elementHeight;
         this.setHeight(this.height + (double)i);
      }

   }

   private boolean isBlend() {
      return this.colourProperty.getValue() == ArraylistModule.Colour.BLEND;
   }

   public void onEnable() {
   }

   public void onDisable() {
   }

   public void setX(double x) {
      this.xPos = x;
   }

   public void setY(double y) {
      this.yPos = y;
   }

   public double getX() {
      return this.xPos;
   }

   public double getY() {
      return this.yPos;
   }

   public double setWidth(double width) {
      return this.width = width;
   }

   public double setHeight(double height) {
      return this.height = height;
   }

   public double getWidth() {
      return this.width;
   }

   public double getHeight() {
      return this.height;
   }

   public boolean isVisible() {
      return true;
   }

   public void setVisible(boolean visible) {
   }

   private static enum Colour {
      BLEND("Blend", (startColour, endColour, index) -> {
         return ColourUtil.fadeBetween(startColour, endColour, (long)index * 150L);
      }),
      CLIENT("Client Colour", (startColour, endColour, index) -> {
         return ColourUtil.fadeBetween(ColourUtil.getClientColour(), ColourUtil.getSecondaryColour(), (long)index * 150L);
      });

      private final String name;
      private final ModColourFunc modColourFunc;

      private Colour(String name, ModColourFunc modColourFunc) {
         this.name = name;
         this.modColourFunc = modColourFunc;
      }

      public int getModColour(int startColour, int endColour, int index) {
         return this.modColourFunc.getColour(startColour, endColour, index);
      }

      public String toString() {
         return this.name;
      }
   }

   @FunctionalInterface
   private interface ModColourFunc {
      int getColour(int var1, int var2, int var3);
   }

   private static enum LineSelection {
      TOP("Top"),
      SIDE("Side"),
      BOTTOM("Bottom");

      private final String name;

      private LineSelection(String name) {
         this.name = name;
      }

      public String toString() {
         return this.name;
      }
   }
}
