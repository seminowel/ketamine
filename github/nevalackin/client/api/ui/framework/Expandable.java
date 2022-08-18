package io.github.nevalackin.client.api.ui.framework;

import io.github.nevalackin.client.util.render.DrawUtil;

public interface Expandable {
   void setState(ExpandState var1);

   ExpandState getState();

   default boolean isExpanded() {
      return this.getState() != ExpandState.CLOSED && this.getExpandProgress() > 0.0;
   }

   double getX();

   double getY();

   double getExpandedX();

   double getExpandedY();

   double getExpandedWidth();

   double calculateExpandedHeight();

   double getExpandProgress();

   default double getExpandedHeight() {
      return this.calculateExpandedHeight() * DrawUtil.bezierBlendAnimation(this.getExpandProgress());
   }

   default boolean isHoveredExpanded(int mouseX, int mouseY) {
      double ex;
      double ey;
      return this.isExpanded() && (double)mouseX > (ex = this.getExpandedX()) && (double)mouseX < ex + this.getExpandedWidth() && (double)mouseY > (ey = this.getExpandedY()) && (double)mouseY < ey + this.getExpandedHeight();
   }

   default boolean isHoveredExpand(int mouseX, int mouseY) {
      double ex = this.getExpandedX();
      double ey = this.getExpandedY();
      double ew = this.getExpandedWidth();
      double eh = this.getExpandedHeight();
      return (double)mouseX > ex && (double)mouseY > ey && (double)mouseX < ex + ew && (double)mouseY < ey + eh;
   }
}
