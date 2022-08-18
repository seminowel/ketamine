package io.github.nevalackin.client.util.render;

import java.awt.Color;

public final class ColourUtil {
   private static final int[] HEALTH_COLOURS = new int[]{-16711847, -256, -32768, -65536, -8388608};
   private static final int[] RAINBOW_COLOURS = new int[]{-234868, -234795, -2462980, -7247108, -9794308, -9775620, -2462980, -234868};
   private static final int[] CZECHIA_COLOURS = new int[]{-15645314, -15645314, -2681830, -2681830, -1, -15645314};
   private static final int[] GERMAN_COLOURS = new int[]{-16777216, -131072, -12544, -16777216};
   public static int clientColour = -3278336;
   public static int secondaryColour = -16718593;

   private ColourUtil() {
   }

   public static int getClientColour() {
      return clientColour;
   }

   public static void setClientColour(int colour) {
      clientColour = colour;
   }

   public static int getSecondaryColour() {
      return secondaryColour;
   }

   public static void setSecondaryColour(int secondColour) {
      secondaryColour = secondColour;
   }

   public static int blendHealthColours(double progress) {
      return blendColours(HEALTH_COLOURS, progress);
   }

   public static int blendRainbowColours(double progress) {
      return blendColours(RAINBOW_COLOURS, progress);
   }

   public static int blendRainbowColours(long offset) {
      return blendRainbowColours(getFadingFromSysTime(offset));
   }

   public static int blendCzechiaColours(double progress) {
      return blendColours(CZECHIA_COLOURS, progress);
   }

   public static int blendCzechiaColours(long offset) {
      return blendCzechiaColours(getFadingFromSysTime(offset));
   }

   public static int blendGermanColours(double progress) {
      return blendColours(GERMAN_COLOURS, progress);
   }

   public static int blendGermanColours(long offset) {
      return blendGermanColours(getFadingFromSysTime(offset));
   }

   public static int blendSpecialRainbow(long offset) {
      float fading = (float)getFadingFromSysTime(offset);
      return Color.HSBtoRGB(1.0F - fading, 0.8F, 1.0F);
   }

   public static double getFadingFromSysTime(long offset) {
      return (double)((System.currentTimeMillis() + offset) % 2000L) / 2000.0;
   }

   public static float getBreathingProgress() {
      float progress = (float)(System.currentTimeMillis() % 2000L) / 1000.0F;
      return progress > 1.0F ? 1.0F - progress % 1.0F : progress;
   }

   public static int darker(int colour, double factor) {
      int r = (int)((double)(colour >> 16 & 255) * factor);
      int g = (int)((double)(colour >> 8 & 255) * factor);
      int b = (int)((double)(colour & 255) * factor);
      int a = colour >> 24 & 255;
      return (r & 255) << 16 | (g & 255) << 8 | b & 255 | (a & 255) << 24;
   }

   public static float calculateAverageChannel(int rgb) {
      int red = rgb >> 16 & 255;
      int green = rgb >> 8 & 255;
      int blue = rgb & 255;
      return (float)Math.max(red, Math.max(green, blue)) / 255.0F;
   }

   public static int removeAlphaComponent(int colour) {
      int red = colour >> 16 & 255;
      int green = colour >> 8 & 255;
      int blue = colour & 255;
      return (red & 255) << 16 | (green & 255) << 8 | blue & 255;
   }

   public static int overwriteAlphaComponent(int colour, int alphaComponent) {
      int red = colour >> 16 & 255;
      int green = colour >> 8 & 255;
      int blue = colour & 255;
      return (alphaComponent & 255) << 24 | (red & 255) << 16 | (green & 255) << 8 | blue & 255;
   }

   public static Color getGradientOffset(Color color1, Color color2, double offset) {
      double inverse_percent;
      int redPart;
      if (offset > 1.0) {
         inverse_percent = offset % 1.0;
         redPart = (int)offset;
         offset = redPart % 2 == 0 ? inverse_percent : 1.0 - inverse_percent;
      }

      inverse_percent = 1.0 - offset;
      redPart = (int)((double)color1.getRed() * inverse_percent + (double)color2.getRed() * offset);
      int greenPart = (int)((double)color1.getGreen() * inverse_percent + (double)color2.getGreen() * offset);
      int bluePart = (int)((double)color1.getBlue() * inverse_percent + (double)color2.getBlue() * offset);
      return new Color(redPart, greenPart, bluePart);
   }

   public static int blendOpacityRainbowColours(long offset, int alphaComponent) {
      return overwriteAlphaComponent(blendRainbowColours(getFadingFromSysTime(offset)), alphaComponent);
   }

   public static int darker(int color) {
      return darker(color, 0.6);
   }

   public static int blendColours(int[] colours, double progress) {
      int size = colours.length;
      if (progress == 1.0) {
         return colours[0];
      } else if (progress == 0.0) {
         return colours[size - 1];
      } else {
         double mulProgress = Math.max(0.0, (1.0 - progress) * (double)(size - 1));
         int index = (int)mulProgress;
         return fadeBetween(colours[index], colours[index + 1], mulProgress - (double)index);
      }
   }

   public static int fadeBetween(int startColour, int endColour, double progress) {
      if (progress > 1.0) {
         progress = 1.0 - progress % 1.0;
      }

      return fadeTo(startColour, endColour, progress);
   }

   public static int fadeBetween(int startColour, int endColour, long offset) {
      return fadeBetween(startColour, endColour, (double)((System.currentTimeMillis() + offset) % 2000L) / 1000.0);
   }

   public static int fadeBetween(int startColour, int endColour) {
      return fadeBetween(startColour, endColour, 0L);
   }

   public static int fadeTo(int startColour, int endColour, double progress) {
      double invert = 1.0 - progress;
      int r = (int)((double)(startColour >> 16 & 255) * invert + (double)(endColour >> 16 & 255) * progress);
      int g = (int)((double)(startColour >> 8 & 255) * invert + (double)(endColour >> 8 & 255) * progress);
      int b = (int)((double)(startColour & 255) * invert + (double)(endColour & 255) * progress);
      int a = (int)((double)(startColour >> 24 & 255) * invert + (double)(endColour >> 24 & 255) * progress);
      return (a & 255) << 24 | (r & 255) << 16 | (g & 255) << 8 | b & 255;
   }
}
