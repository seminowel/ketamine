package io.github.nevalackin.client.impl.ui.cfont;

import io.github.nevalackin.client.api.ui.cfont.StaticallySizedImage;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public final class FontGlyphs {
   private final Font font;
   private final int weight;
   private final StaticallySizedImage[] glyphs = new StaticallySizedImage[256];

   public FontGlyphs(Font font, int weight) {
      this.font = font.deriveFont(32.0F);
      this.weight = weight;
      this.createGlyphs();
   }

   private void createGlyphs() {
      BufferedImage tempImage = new BufferedImage(1, 1, 2);
      Graphics2D graphics2D = (Graphics2D)tempImage.getGraphics();
      FontMetrics fontMetrics = graphics2D.getFontMetrics(this.font);

      for(int i = 11; i < 256; ++i) {
         if (i != 127) {
            char character = (char)i;
            Rectangle2D characterBounds = fontMetrics.getStringBounds(String.valueOf(character), graphics2D);
            int characterWidth = (int)StrictMath.ceil(characterBounds.getWidth()) + 4;
            int characterHeight = (int)StrictMath.ceil(characterBounds.getHeight()) + 4;
            if (characterWidth > 0 && characterHeight > 0) {
               BufferedImage characterImage = new BufferedImage(characterWidth, characterHeight, 2);
               Graphics2D graphics = (Graphics2D)characterImage.getGraphics();
               graphics.setFont(this.font);
               graphics.setColor(new Color(0, 0, 0, 0));
               graphics.fillRect(0, 0, characterImage.getWidth(), characterImage.getHeight());
               graphics.setColor(Color.WHITE);
               graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
               graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
               graphics.drawString(String.valueOf(character), 2, 2 + fontMetrics.getAscent());
               this.glyphs[i] = new StaticallySizedImage(characterImage, true, 3);
            }
         }
      }

   }

   public Font getFont() {
      return this.font;
   }

   public int getWeight() {
      return this.weight;
   }

   public StaticallySizedImage[] getGlyphs() {
      return this.glyphs;
   }
}
