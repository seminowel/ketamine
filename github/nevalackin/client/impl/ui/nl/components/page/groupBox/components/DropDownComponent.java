package io.github.nevalackin.client.impl.ui.nl.components.page.groupBox.components;

import io.github.nevalackin.client.api.ui.cfont.CustomFontRenderer;
import io.github.nevalackin.client.api.ui.framework.Animated;
import io.github.nevalackin.client.api.ui.framework.Component;
import io.github.nevalackin.client.api.ui.framework.ExpandState;
import io.github.nevalackin.client.api.ui.framework.Expandable;
import io.github.nevalackin.client.api.ui.framework.Predicated;
import io.github.nevalackin.client.impl.core.KetamineClient;
import io.github.nevalackin.client.util.render.ColourUtil;
import io.github.nevalackin.client.util.render.DrawUtil;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.gui.ScaledResolution;

public final class DropDownComponent extends Component implements Animated, Expandable, Predicated {
   private final String name;
   private final Consumer setValueFunc;
   private final Consumer unsetValueFunc;
   private final Supplier getSelectedFunc;
   private final Supplier getValueFunc;
   private final Supplier getValuesFunc;
   private final Supplier dependency;
   private final boolean multiSelection;
   private ExpandState state;
   private double progress;
   private double hoveredFadeInProgress;
   private int[] cachedSelectedStringValues;
   private String selectedValuesString;

   public DropDownComponent(Component parent, String name, Consumer setValueFunc, Consumer unsetValueFunc, Supplier getSelectedFunc, Supplier getValueFunc, Supplier getValuesFunc, Supplier dependency, boolean multiSelection, double height) {
      super(parent, 0.0, 0.0, 0.0, height);
      this.name = name;
      this.setValueFunc = setValueFunc;
      this.unsetValueFunc = unsetValueFunc;
      this.getSelectedFunc = getSelectedFunc;
      this.getValueFunc = getValueFunc;
      this.getValuesFunc = getValuesFunc;
      this.dependency = dependency;
      this.multiSelection = multiSelection;
   }

   private void updateSelected() {
      if (!Arrays.equals((int[])this.getSelectedFunc.get(), this.cachedSelectedStringValues)) {
         this.selectedValuesString = this.getSelected();
         this.cachedSelectedStringValues = (int[])this.getSelectedFunc.get();
      }

   }

   private String getSelected() {
      String[] values = (String[])this.getValuesFunc.get();
      if (values.length == 0) {
         return "";
      } else {
         String array = Arrays.toString(Arrays.stream((int[])this.getSelectedFunc.get()).mapToObj((i) -> {
            return values[i];
         }).toArray((x$0) -> {
            return new String[x$0];
         }));
         return array.substring(1, array.length() - 1);
      }
   }

   public void onDraw(ScaledResolution scaledResolution, int mouseX, int mouseY) {
      double x = this.getX();
      double y = this.getY();
      double width = this.getWidth();
      double height = this.getHeight();
      this.hoveredFadeInProgress = DrawUtil.animateProgress(this.hoveredFadeInProgress, this.isHovered(mouseX, mouseY) ? 1.0 : 0.0, 5.0);
      double smoothHovered = DrawUtil.bezierBlendAnimation(this.hoveredFadeInProgress);
      CustomFontRenderer fontRenderer = KetamineClient.getInstance().getFontRenderer();
      fontRenderer.draw(this.name, x, y + height / 2.0 - fontRenderer.getHeight(this.name) / 2.0, 0.22, ColourUtil.fadeBetween(this.getTheme().getTextColour(), this.getTheme().getHighlightedTextColour(), smoothHovered));
      if (this.multiSelection) {
         this.updateSelected();
      } else {
         this.selectedValuesString = ((String[])this.getValuesFunc.get())[(Integer)this.getValueFunc.get()];
      }

      fontRenderer.draw(this.selectedValuesString, x + width - fontRenderer.getWidth(this.selectedValuesString, 0.22), y + height / 2.0 - fontRenderer.getHeight(this.selectedValuesString) / 2.0, 0.22, this.getTheme().getTextColour());
   }

   public void onMouseClick(int mouseX, int mouseY, int button) {
      if (!this.multiSelection) {
         if (button == 0) {
            this.setValueFunc.accept((Integer)this.getValueFunc.get() + 1);
         } else if (button == 1) {
            this.setValueFunc.accept((Integer)this.getValueFunc.get() - 1);
         }
      }

   }

   public void onMouseRelease(int button) {
   }

   public void onKeyPress(int keyCode) {
   }

   public void resetAnimationState() {
      this.setState(ExpandState.CLOSED);
      this.progress = 0.0;
   }

   public void setState(ExpandState state) {
      this.state = state;
   }

   public ExpandState getState() {
      return this.state;
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

   public double calculateExpandedHeight() {
      double height = 0.0;
      CustomFontRenderer fontRenderer = KetamineClient.getInstance().getFontRenderer();
      String[] var4 = (String[])this.getValuesFunc.get();
      int var5 = var4.length;

      for(int var6 = 0; var6 < var5; ++var6) {
         String value = var4[var6];
         height += fontRenderer.getWidth(value, 0.22, 700) + 2.0;
      }

      return height;
   }

   public double getExpandProgress() {
      return this.progress;
   }

   public boolean isVisible() {
      return this.dependency != null ? (Boolean)this.dependency.get() : true;
   }
}
