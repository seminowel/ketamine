package io.github.nevalackin.client.impl.ui.hud.components;

public interface ResizeableHudComponent extends HudComponent {
   void setWidthUnchecked(double var1);

   void setHeightUnchecked(double var1);

   double getMinWidth();

   double getMinHeight();
}
