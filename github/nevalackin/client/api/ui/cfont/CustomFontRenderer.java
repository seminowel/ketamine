package io.github.nevalackin.client.api.ui.cfont;

public interface CustomFontRenderer {
   double DEFAULT_SCALE = 0.23;
   int DEFAULT_WEIGHT = 900;
   float BASE_FONT = 32.0F;

   void draw(CharSequence var1, double var2, double var4, double var6, int var8, int var9);

   default void draw(CharSequence text, double x, double y, double scale, int colour) {
      this.draw(text, x, y, scale, 900, colour);
   }

   default void draw(CharSequence text, double x, double y, int weight, int colour) {
      this.draw(text, x, y, 0.23, weight, colour);
   }

   default void draw(CharSequence text, double x, double y, int colour) {
      this.draw(text, x, y, 0.23, 900, colour);
   }

   double getWidth(CharSequence var1, double var2, int var4);

   default double getWidth(CharSequence text, double scale) {
      return this.getWidth(text, scale, 900);
   }

   default void drawWithShadow(String text, double x, double y, double scale, int weight, double shadowLength, int color) {
      this.draw(text, x + shadowLength, y + shadowLength, scale, weight, -16777216);
      this.draw(text, x, y, scale, weight, color);
   }

   default void drawWithShadow(String text, double x, double y, double shadowLength, int color) {
      this.draw(text, x + shadowLength, y + shadowLength, 0.23, -16777216);
      this.draw(text, x, y, 0.23, color);
   }

   default double getWidth(CharSequence text, int weight) {
      return this.getWidth(text, 0.23, weight);
   }

   default double getWidth(CharSequence text) {
      return this.getWidth(text, 0.23, 900);
   }

   double getHeight(CharSequence var1, double var2, int var4);

   default double getHeight(CharSequence text, double scale) {
      return this.getHeight(text, scale, 900);
   }

   default double getHeight(CharSequence text, int weight) {
      return this.getHeight(text, 0.23, weight);
   }

   default double getHeight(CharSequence text) {
      return this.getHeight(text, 0.23, 900);
   }
}
