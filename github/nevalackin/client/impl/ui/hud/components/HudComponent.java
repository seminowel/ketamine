package io.github.nevalackin.client.impl.ui.hud.components;

import io.github.nevalackin.client.impl.ui.hud.Quadrant;

public interface HudComponent {
   boolean isDragging();

   void setDragging(boolean var1);

   default void fitInScreen(int scaledWidth, int scaledHeight) {
      double x = this.getX();
      double y = this.getY();
      double width = this.getWidth();
      double height = this.getHeight();
      if (x < 0.0) {
         this.setX(0.0);
      } else if (x + width > (double)scaledWidth) {
         this.setX((double)scaledWidth - width);
      }

      if (y < 0.0) {
         this.setY(0.0);
      } else if (y + height > (double)scaledHeight) {
         this.setY((double)scaledHeight - height);
      }

   }

   default boolean isHovered(double mouseX, double mouseY) {
      double x = this.getX();
      double y = this.getY();
      return mouseX > x && mouseY > y && mouseX < x + this.getWidth() && mouseY < y + this.getHeight();
   }

   default Quadrant getQuadrant(int scaledWidth, int scaledHeight) {
      double x = this.getX();
      double y = this.getY();
      double hw = this.getWidth() * 0.5;
      double hh = this.getHeight() * 0.5;
      if (x > (double)((float)scaledWidth * 0.5F) - hw) {
         return y > (double)((float)scaledHeight * 0.5F) - hh ? Quadrant.BOTTOM_RIGHT : Quadrant.TOP_RIGHT;
      } else {
         return y > (double)((float)scaledHeight * 0.5F) - hh ? Quadrant.BOTTOM_LEFT : Quadrant.TOP_LEFT;
      }
   }

   void render(int var1, int var2, double var3);

   double getX();

   double getY();

   void setX(double var1);

   void setY(double var1);

   double setWidth(double var1);

   double setHeight(double var1);

   double getWidth();

   double getHeight();

   boolean isVisible();

   void setVisible(boolean var1);
}
