package io.github.nevalackin.client.impl.ui.hud;

public enum Quadrant {
   TOP_LEFT(false, false),
   TOP_RIGHT(false, true),
   BOTTOM_LEFT(true, false),
   BOTTOM_RIGHT(true, true);

   private final boolean bottom;
   private final boolean right;

   private Quadrant(boolean bottom, boolean right) {
      this.bottom = bottom;
      this.right = right;
   }

   public boolean isBottom() {
      return this.bottom;
   }

   public boolean isRight() {
      return this.right;
   }
}
