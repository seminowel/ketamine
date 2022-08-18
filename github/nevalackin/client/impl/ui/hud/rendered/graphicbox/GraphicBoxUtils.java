package io.github.nevalackin.client.impl.ui.hud.rendered.graphicbox;

import io.github.nevalackin.client.api.ui.cfont.CustomFontRenderer;
import io.github.nevalackin.client.impl.core.KetamineClient;
import io.github.nevalackin.client.util.render.BloomUtil;
import io.github.nevalackin.client.util.render.ColourUtil;
import io.github.nevalackin.client.util.render.DrawUtil;
import java.util.List;

public class GraphicBoxUtils {
   private static final float OUTLINE_WIDTH = 0.5F;
   private static final float SEPARATOR_WIDTH = 1.0F;
   private static final float HEADER = 10.0F;

   private GraphicBoxUtils() {
   }

   public static float[] drawGraphicBox(List fields, List objects, float x, float y) {
      CustomFontRenderer fontRenderer = KetamineClient.getInstance().getFontRenderer();
      float[] dimensions = new float[2];
      float width = 0.0F;
      float margin = 2.0F;
      float spacing = margin * 3.0F;
      int fs = fields.size();
      int n = objects.size();
      String[][] values = new String[fs][n];
      double[] fieldWidths = new double[fs];

      int j;
      for(j = 0; j < fs; ++j) {
         fieldWidths[j] = fontRenderer.getWidth(((GraphicBoxField)fields.get(j)).title) + (double)spacing;
      }

      for(j = 0; j < n; ++j) {
         Object object = objects.get(j);

         for(int i = 0; i < fs; ++i) {
            values[i][j] = (String)((GraphicBoxField)fields.get(i)).valueFunc.apply(object);
            fieldWidths[i] = Math.max(fieldWidths[i], fontRenderer.getWidth(values[i][j]) + (double)spacing);
         }
      }

      float fieldWidthsAccumulator = 0.0F;

      for(int i = 0; i < fs; ++i) {
         fieldWidthsAccumulator = (float)((double)fieldWidthsAccumulator + fieldWidths[i]);
      }

      width = Math.max(width, fieldWidthsAccumulator);
      dimensions[0] = width;
      drawGraphicBox((String)null, x, y, width, (outlineWidth, separatorWidth, header) -> {
         return (float)(n + 1) * header + separatorWidth;
      }, (gbx, gby, gbwidth, gbheight, color, outlineWidth, separatorWidth, header) -> {
         float xAccumulator = x + margin;

         int i;
         for(i = 0; i < fs; ++i) {
            fontRenderer.drawWithShadow(((GraphicBoxField)fields.get(i)).title, (double)xAccumulator, (double)y, 0.5, -1);
            xAccumulator = (float)((double)xAccumulator + fieldWidths[i]);
         }

         for(i = 0; i < n; ++i) {
            float elementY = y + header + separatorWidth + (float)i * header;
            xAccumulator = x + margin;

            for(int j = 0; j < fs; ++j) {
               fontRenderer.drawWithShadow(values[j][i], (double)xAccumulator, (double)elementY, 0.5, (Integer)((GraphicBoxField)fields.get(j)).valueColorFunc.apply(objects.get(i)));
               xAccumulator = (float)((double)xAccumulator + fieldWidths[j]);
            }
         }

         dimensions[1] = gbheight;
      });
      return dimensions;
   }

   public static void drawGraphicBox(String title, float x, float y, float width, float height, GraphicBoxContent content) {
      CustomFontRenderer fontRenderer = KetamineClient.getInstance().getFontRenderer();
      int startColour = ColourUtil.fadeBetween(ColourUtil.getClientColour(), ColourUtil.getSecondaryColour(), 0L);
      int endColour = ColourUtil.fadeBetween(ColourUtil.getSecondaryColour(), ColourUtil.getClientColour(), 250L);
      int color = 4505979;
      DrawUtil.glDrawFilledQuad((double)x, (double)y, (double)width, (double)height, Integer.MIN_VALUE);
      DrawUtil.glDrawFilledQuad((double)x, (double)y, (double)width, 10.0, Integer.MIN_VALUE);
      BloomUtil.drawAndBloom(() -> {
         DrawUtil.glDrawSidewaysGradientRect((double)x, (double)(y + 10.0F), (double)width, 1.0, startColour, endColour);
      });
      if (title != null) {
         fontRenderer.drawWithShadow(title, (double)(x + 2.0F), (double)(y + 2.0F), 0.5, -1);
      }

      if (content != null) {
         content.drawContent((double)x, (double)y, width, height, color, 0.5F, 1.0F, 10.0F);
      }

   }

   public static void drawGraphicBox(String title, float x, float y, float width, GraphicBoxHeightSupplier heightSupplier, GraphicBoxContent content) {
      float height = heightSupplier.get(0.5F, 1.0F, 10.0F);
      drawGraphicBox(title, x, y, width, height, content);
   }
}
