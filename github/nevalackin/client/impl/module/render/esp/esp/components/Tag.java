package io.github.nevalackin.client.impl.module.render.esp.esp.components;

import io.github.nevalackin.client.api.ui.cfont.CustomFontRenderer;
import io.github.nevalackin.client.impl.module.render.esp.esp.EnumPosition;
import io.github.nevalackin.client.util.render.DrawUtil;

public final class Tag {
   private final String text;
   private final boolean drawBackground;
   private final EnumPosition position;
   private final int colour;

   public Tag(String text, EnumPosition position, int colour, boolean drawBackground) {
      this.text = text;
      this.position = position;
      this.colour = colour;
      this.drawBackground = drawBackground;
   }

   public void draw(CustomFontRenderer renderer, double[] boundingBox, double[] sideOffset) {
      double left = boundingBox[0];
      double top = boundingBox[1];
      double right = boundingBox[2];
      double bottom = boundingBox[3];
      double lowerRight = boundingBox[4];
      String text = this.text;
      double textWidth = renderer.getWidth(text);
      EnumPosition side = this.getPosition();
      boolean background = this.drawBackground;
      double backgroundBufferX = 2.0;
      double backgroundBufferY = 1.0;
      double tLeft = side.isUseRightAsLeft() ? right : left;
      double tTop = side.isUseBottomAsTop() ? bottom : top;
      tLeft += sideOffset[0];
      tTop += sideOffset[1];
      if (side.isNeedSubtractTextHeight()) {
         tTop -= 9.0;
         if (background) {
            tTop -= 2.0;
         }
      }

      if (side.isNeedSubtractTextWidth()) {
         tLeft -= textWidth;
      }

      if (side.isCentreText()) {
         double width = right - left;
         double centerOfTag = tLeft + width / 2.0;
         tLeft = centerOfTag - (textWidth + 4.0) / 2.0;
         if (background) {
            if (side == EnumPosition.TOP) {
               --tTop;
            } else {
               ++tTop;
            }
         }
      } else if (background) {
         if (side == EnumPosition.LEFT) {
            tLeft -= 2.0;
         } else {
            tLeft += 2.0;
         }
      }

      if (this.drawBackground) {
         DrawUtil.glDrawFilledQuad(tLeft, tTop, textWidth + 4.0, 11.0, -1778384896);
      }

      renderer.draw(text, tLeft + 2.0, tTop + 1.0, this.colour);
   }

   public EnumPosition getPosition() {
      return this.position;
   }
}
