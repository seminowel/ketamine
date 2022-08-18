package io.github.nevalackin.client.impl.ui.cfont;

import io.github.nevalackin.client.api.ui.cfont.CustomFontRenderer;
import io.github.nevalackin.client.api.ui.cfont.StaticallySizedImage;
import io.github.nevalackin.client.util.render.DrawUtil;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class MipMappedFontRenderer implements CustomFontRenderer {
   static final int MARGIN = 2;
   private static final int[] COLOR_CODE_COLORS = new int[32];
   private final Map glyphs = new HashMap();

   public MipMappedFontRenderer(FontGlyphs... fontGlyphsArray) {
      Arrays.stream(fontGlyphsArray).forEach((fontGlyphs) -> {
         FontGlyphs var10000 = (FontGlyphs)this.glyphs.put(fontGlyphs.getWeight(), fontGlyphs);
      });
   }

   private StaticallySizedImage[] getGlyphs(int weight) {
      return ((FontGlyphs)this.glyphs.get(weight)).getGlyphs();
   }

   public void draw(CharSequence text, double x, double y, double scale, int weight, int colour) {
      int length;
      if (text != null && (length = text.length()) != 0) {
         StaticallySizedImage[] characters = this.getGlyphs(weight);
         double characterOffset = 0.0;
         int alphaMask = (colour >> 24 & 255) << 24;
         char prev = 0;
         int currentColour = colour;
         DrawUtil.glColour(colour);

         for(int i = 0; i < length; ++i) {
            char character = text.charAt(i);
            if (character > '\n' && character < 256 && character != 127) {
               if (prev == 167) {
                  int index = "0123456789ABCDEFKLMNOR".indexOf(character);
                  if (index != -1 && index < 16) {
                     int textColor = COLOR_CODE_COLORS[index] | alphaMask;
                     if (currentColour != textColor) {
                        DrawUtil.glColour(textColor);
                        currentColour = textColor;
                     }
                  }

                  prev = character;
               } else {
                  if (character != 167) {
                     if (character != '\r') {
                        StaticallySizedImage image = characters[character];
                        double scaledWidth = (double)image.getWidth() * scale;
                        image.draw(x + characterOffset - 2.0 * scale, y - 2.0 * scale, scaledWidth, (double)image.getHeight() * scale);
                        characterOffset += scaledWidth - 4.0 * scale;
                     } else {
                        characterOffset += 32.0 * scale;
                     }
                  }

                  prev = character;
               }
            }
         }

      }
   }

   public double getWidth(CharSequence text, double scale, int weight) {
      int length;
      if (text != null && (length = text.length()) != 0) {
         double width = 0.0;
         StaticallySizedImage[] characters = this.getGlyphs(weight);
         char prev = 0;

         for(int i = 0; i < length; ++i) {
            char character = text.charAt(i);
            if (prev != 167 && character != 167 && character > '\n' && character != 127 && character < 256) {
               width += (double)(character != '\r' ? (float)(characters[character].getWidth() - 4) : 32.0F) * scale;
            }

            prev = character;
         }

         return width;
      } else {
         return 0.0;
      }
   }

   public double getHeight(CharSequence text, double scale, int weight) {
      int length;
      if (text != null && (length = text.length()) != 0) {
         double height = 0.0;
         StaticallySizedImage[] characters = this.getGlyphs(weight);
         char prev = 0;

         for(int i = 0; i < length; ++i) {
            char character = text.charAt(i);
            if (prev != 167 && character != 167 && character != '\r' && character > '\n' && character != 127 && character < 256) {
               height = Math.max(height, (double)(characters[character].getHeight() - 4) * scale);
            }

            prev = character;
         }

         return height;
      } else {
         return 0.0;
      }
   }

   static {
      for(int i = 0; i < 32; ++i) {
         int thingy = (i >> 3 & 1) * 85;
         int red = (i >> 2 & 1) * 170 + thingy;
         int green = (i >> 1 & 1) * 170 + thingy;
         int blue = (i & 1) * 170 + thingy;
         if (i == 6) {
            red += 85;
         }

         if (i >= 16) {
            red /= 4;
            green /= 4;
            blue /= 4;
         }

         COLOR_CODE_COLORS[i] = (red & 255) << 16 | (green & 255) << 8 | blue & 255;
      }

   }
}
