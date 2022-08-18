package io.github.nevalackin.client.impl.ui.rfont;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import org.lwjgl.opengl.GL11;

public final class TextRenderer {
   final StaticImage bitmap;
   private final int bitmapSize;
   private final Glyph[] glyphs;
   private Metrics glyphMetrics;

   public TextRenderer(Font font, boolean antialiasing, boolean fractionalMetrics) {
      this.glyphs = new Glyph[Glyph.CODEPOINTS];
      int bitmapSize = 0;

      StaticImage bitmap;
      do {
         bitmapSize += 256;
         bitmap = this.generateBitmap(font, antialiasing, fractionalMetrics, bitmapSize);
      } while(bitmap == null && bitmapSize <= 2048);

      this.bitmapSize = bitmapSize;
      this.bitmap = bitmap;
   }

   private static void addTexturedVertices(int x, int y, int w, int h, int texX, int texY, int texW, int texH, int texSize) {
      float s = (float)texX / (float)texSize;
      float t = (float)texY / (float)texSize;
      float s1 = (float)(texX + texW) / (float)texSize;
      float t1 = (float)(texY + texH) / (float)texSize;
      GL11.glTexCoord2f(s, t);
      GL11.glVertex2i(x, y);
      GL11.glTexCoord2f(s, t1);
      GL11.glVertex2i(x, y + h);
      GL11.glTexCoord2f(s1, t1);
      GL11.glVertex2i(x + w, y + h);
      GL11.glTexCoord2f(s1, t);
      GL11.glVertex2i(x + w, y);
   }

   private static int countLineFeeds(CharSequence text) {
      int occurrences = 0;

      for(int i = 0; i < text.length(); ++i) {
         if (text.charAt(i) == '\n') {
            ++occurrences;
         }
      }

      return occurrences;
   }

   private StaticImage generateBitmap(Font font, boolean antialiasing, boolean fractionalMetrics, int bitmapSize) {
      BufferedImage image = new BufferedImage(bitmapSize, bitmapSize, 2);
      Graphics2D ctx = image.createGraphics();
      ctx.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, antialiasing ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
      ctx.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, fractionalMetrics ? RenderingHints.VALUE_FRACTIONALMETRICS_ON : RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
      ctx.setFont(font);
      FontMetrics metrics = ctx.getFontMetrics();
      this.glyphMetrics = new Metrics(metrics.getMaxAdvance(), metrics.getHeight());
      int x = 0;
      int y = 0;

      for(int i = 32; i < 127; ++i) {
         char[] chars = Character.toChars(i);
         if (x + this.glyphMetrics.w > bitmapSize) {
            x = 0;
            y += this.glyphMetrics.h;
         }

         if (y + this.glyphMetrics.h > bitmapSize) {
            System.err.println("ran out of space on bitmap!");
            return null;
         }

         int glyphIndex = Glyph.codepointToGlyphIndex(i);
         Glyph glyph = new Glyph(x, y, metrics.charWidth(i));
         this.glyphs[glyphIndex] = glyph;
         ctx.setColor(Color.WHITE);
         ctx.drawString(new String(chars, 0, 1), glyph.x + 1, glyph.y + metrics.getAscent());
         x += this.glyphMetrics.w;
      }

      return new StaticImage(image, true);
   }

   public void render(CharSequence text, float x, float y, int wrapWidth, boolean shadow, int color) {
      if (shadow) {
         GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.5F);
         this.render(text, x, y, wrapWidth);
      }

      GL11.glColor4f((float)(color >> 16 & 255) / 255.0F, (float)(color >> 8 & 255) / 255.0F, (float)(color >> 0 & 255) / 255.0F, (float)(color >> 24 & 255) / 255.0F);
      this.render(text, x, y, wrapWidth);
   }

   private void render(CharSequence text, float x, float y, int wrapWidth) {
      int x1 = (int)(x * 2.0F);
      int x2 = x1;
      int y2 = (int)(y * 2.0F);
      int gw = this.glyphMetrics.w;
      int gh = this.glyphMetrics.h;
      wrapWidth *= 2;
      this.bitmap.bind();
      GL11.glScalef(0.5F, 0.5F, 1.0F);
      GL11.glBegin(7);

      for(int i = 0; i < text.length(); ++i) {
         char c = text.charAt(i);
         if (Glyph.validateCodepoint(c)) {
            if (x2 != x1 || c != ' ') {
               int cp = Glyph.codepointToGlyphIndex(c);
               Glyph glyph = this.glyphs[cp];
               if (wrapWidth != 0 && x2 + glyph.advance - x1 > wrapWidth) {
                  x2 = x1;
                  y2 += gh;
                  if (c == ' ') {
                     continue;
                  }
               }

               if (c != ' ') {
                  addTexturedVertices(x2, y2, gw, gh, glyph.x, glyph.y, gw, gh, this.bitmapSize);
               }

               x2 += glyph.advance;
            }
         } else if (c == '\n') {
            x2 = x1;
            y2 += gh;
         } else if (c == '\t') {
            x2 += this.glyphs[Glyph.codepointToGlyphIndex(32)].advance * 4;
         }
      }

      GL11.glEnd();
      GL11.glScalef(2.0F, 2.0F, 1.0F);
   }

   public float getWidth(CharSequence text) {
      int max = 0;
      int w = 0;

      for(int i = 0; i < text.length(); ++i) {
         char c = text.charAt(i);
         if (c == '\n') {
            max = Math.max(max, w);
            w = 0;
         } else if (Glyph.validateCodepoint(c)) {
            w += this.glyphs[Glyph.codepointToGlyphIndex(c)].advance;
         }
      }

      return (float)Math.max(w, max) * 0.5F;
   }

   public float getHeight(String text) {
      return (float)(this.glyphMetrics.h * (countLineFeeds(text) + 1)) * 0.5F;
   }

   void delete() {
      this.bitmap.delete();
   }
}
