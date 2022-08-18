package io.github.nevalackin.client.impl.module.render.esp.esp;

import io.github.nevalackin.client.api.ui.cfont.CustomFontRenderer;
import io.github.nevalackin.client.impl.module.render.esp.esp.components.Bar;
import io.github.nevalackin.client.impl.module.render.esp.esp.components.Box;
import io.github.nevalackin.client.impl.module.render.esp.esp.components.Tag;
import io.github.nevalackin.client.util.render.BlurUtil;
import io.github.nevalackin.client.util.render.ColourUtil;
import io.github.nevalackin.client.util.render.DrawUtil;
import java.util.Iterator;
import java.util.List;
import org.lwjgl.opengl.GL11;

public final class Drawable {
   private final double[] boundingBox;
   private final Box box;
   private final List bars;
   private final List tags;
   private boolean blur;
   private boolean coloured;

   public Drawable(double[] boundingBox, Box box, List bars, List tags, boolean blur, boolean coloured) {
      this.boundingBox = boundingBox;
      this.box = box;
      this.bars = bars;
      this.tags = tags;
      this.blur = blur;
      this.coloured = coloured;
   }

   private double[] getTextOffsetForSide(EnumPosition side) {
      double x = side.getXOffset();
      double y = side.getYOffset();
      if (this.bars != null) {
         Iterator var6 = this.bars.iterator();

         while(var6.hasNext()) {
            Bar bar = (Bar)var6.next();
            if (side == bar.getPosition()) {
               double totalThickness = bar.getThickness() + 1.0;
               x += side.isUseRightAsLeft() ? totalThickness : -totalThickness;
               y += side.isUseBottomAsTop() ? totalThickness : -totalThickness;
            }
         }
      }

      return new double[]{x, y};
   }

   void draw(CustomFontRenderer renderer) {
      if (this.tags != null) {
         Iterator var2 = this.tags.iterator();

         while(var2.hasNext()) {
            Tag tag = (Tag)var2.next();
            tag.draw(renderer, this.boundingBox, this.getTextOffsetForSide(tag.getPosition()));
         }
      }

      if (this.box != null) {
         double left = this.boundingBox[0];
         double top = this.boundingBox[1];
         double right = this.boundingBox[2];
         double bottom = this.boundingBox[3];
         double width = right - left;
         double height = bottom - top;
         GL11.glEnable(3042);
         GL11.glDisable(3553);
         GL11.glTranslated(left, top, 0.0);
         DrawUtil.glColour(-1778384896);
         double bThickness = this.box.getWidth();
         double oThickness = 0.5;
         double total = bThickness + 1.0;
         GL11.glBegin(7);
         GL11.glVertex2d(0.0, 0.0);
         GL11.glVertex2d(0.0, total);
         GL11.glVertex2d(width, total);
         GL11.glVertex2d(width, 0.0);
         GL11.glVertex2d(0.0, height - total);
         GL11.glVertex2d(0.0, height);
         GL11.glVertex2d(width, height);
         GL11.glVertex2d(width, height - total);
         GL11.glVertex2d(0.0, total);
         GL11.glVertex2d(0.0, height - total);
         GL11.glVertex2d(total, height - total);
         GL11.glVertex2d(total, total);
         GL11.glVertex2d(width - total, total);
         GL11.glVertex2d(width - total, height - total);
         GL11.glVertex2d(width, height - total);
         GL11.glVertex2d(width, total);
         DrawUtil.glColour(this.box.getColour());
         GL11.glVertex2d(0.5, 0.5);
         GL11.glVertex2d(0.5, 0.5 + bThickness);
         GL11.glVertex2d(width - 0.5, 0.5 + bThickness);
         GL11.glVertex2d(width - 0.5, 0.5);
         GL11.glVertex2d(0.5, height - total + 0.5);
         GL11.glVertex2d(0.5, height - 0.5);
         GL11.glVertex2d(width - 0.5, height - 0.5);
         GL11.glVertex2d(width - 0.5, height - total + 0.5);
         GL11.glVertex2d(0.5, total - 0.5);
         GL11.glVertex2d(0.5, height - total + 0.5);
         GL11.glVertex2d(total - 0.5, height - total + 0.5);
         GL11.glVertex2d(total - 0.5, total - 0.5);
         GL11.glVertex2d(width - total + 0.5, total - 0.5);
         GL11.glVertex2d(width - total + 0.5, height - total + 0.5);
         GL11.glVertex2d(width - 0.5, height - total + 0.5);
         GL11.glVertex2d(width - 0.5, total - 0.5);
         GL11.glEnd();
         if (this.blur) {
            BlurUtil.blurArea(left + 1.0, top + 1.0, width - 2.0, height - 2.0);
            if (this.coloured) {
               DrawUtil.glDrawSidewaysGradientRect(1.0, 1.0, width - 2.0, height - 2.0, ColourUtil.overwriteAlphaComponent(ColourUtil.fadeBetween(ColourUtil.getClientColour(), ColourUtil.getSecondaryColour()), 80), ColourUtil.overwriteAlphaComponent(ColourUtil.fadeBetween(ColourUtil.getSecondaryColour(), ColourUtil.getClientColour()), 80));
            }
         }

         GL11.glTranslated(-left, -top, 0.0);
         GL11.glEnable(3553);
      }

      if (this.bars != null) {
         double[] offsets = new double[]{0.5, 0.5, 0.5, 0.5};
         Iterator var22 = this.bars.iterator();

         while(var22.hasNext()) {
            Bar bar = (Bar)var22.next();
            bar.draw(renderer, this.boundingBox, offsets);
         }
      }

   }

   public double[] getBoundingBox() {
      return this.boundingBox;
   }
}
