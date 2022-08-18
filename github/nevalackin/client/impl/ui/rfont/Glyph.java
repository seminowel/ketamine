package io.github.nevalackin.client.impl.ui.rfont;

final class Glyph {
   static int CODEPOINTS = 95;
   final int x;
   final int y;
   final int advance;

   static boolean validateCodepoint(int code) {
      return code > 31 && code < 127;
   }

   static int codepointToGlyphIndex(int code) {
      return code - 32;
   }

   public Glyph(int x, int y, int advance) {
      this.x = x;
      this.y = y;
      this.advance = advance;
   }
}
