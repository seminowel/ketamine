package io.github.nevalackin.client.impl.module.render.esp.esp;

public enum EnumPosition {
   LEFT("Left", -0.5, 0.0, false, true, false, false, false, false),
   TOP("Top", 0.0, -0.5, true, false, true, true, false, false),
   RIGHT("Right", 0.5, 0.0, false, false, false, false, true, false),
   BOTTOM("Bottom", 0.0, 0.5, false, false, true, true, false, true),
   LOWER_RIGHT("Lower Right", 0.5, 12.5, false, false, false, false, true, false);

   private final String name;
   private final double xOffset;
   private final double yOffset;
   private final boolean needSubtractTextHeight;
   private final boolean needSubtractTextWidth;
   private final boolean centreText;
   private final boolean drawHorizontalBar;
   private final boolean useRightAsLeft;
   private final boolean useBottomAsTop;

   public String getName() {
      return this.name;
   }

   private EnumPosition(String name, double xOffset, double yOffset, boolean needSubtractTextHeight, boolean needSubtractTextWidth, boolean centreText, boolean drawHorizontalBar, boolean useRightAsLeft, boolean useBottomAsTop) {
      this.name = name;
      this.xOffset = xOffset;
      this.yOffset = yOffset;
      this.needSubtractTextHeight = needSubtractTextHeight;
      this.needSubtractTextWidth = needSubtractTextWidth;
      this.centreText = centreText;
      this.drawHorizontalBar = drawHorizontalBar;
      this.useRightAsLeft = useRightAsLeft;
      this.useBottomAsTop = useBottomAsTop;
   }

   public String toString() {
      return this.name;
   }

   public boolean isUseRightAsLeft() {
      return this.useRightAsLeft;
   }

   public boolean isUseBottomAsTop() {
      return this.useBottomAsTop;
   }

   public boolean isNeedSubtractTextHeight() {
      return this.needSubtractTextHeight;
   }

   public boolean isNeedSubtractTextWidth() {
      return this.needSubtractTextWidth;
   }

   public boolean isCentreText() {
      return this.centreText;
   }

   public boolean isDrawHorizontalBar() {
      return this.drawHorizontalBar;
   }

   public double getXOffset() {
      return this.xOffset;
   }

   public double getYOffset() {
      return this.yOffset;
   }
}
