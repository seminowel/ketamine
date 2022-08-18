package io.github.nevalackin.client.util.math;

import java.math.BigDecimal;
import java.util.Random;

public final class MathUtil {
   private MathUtil() {
   }

   public static double round(double value, double inc) {
      if (inc == 0.0) {
         return value;
      } else if (inc == 1.0) {
         return (double)Math.round(value);
      } else {
         double halfOfInc = inc / 2.0;
         double floored = Math.floor(value / inc) * inc;
         return value >= floored + halfOfInc ? (new BigDecimal(Math.ceil(value / inc) * inc)).doubleValue() : (new BigDecimal(floored)).doubleValue();
      }
   }

   public static float getRandomInRange(float min, float max) {
      Random random = new Random();
      double range = (double)(max - min);
      double scaled = random.nextDouble() * range;
      if (scaled > (double)max) {
         scaled = (double)max;
      }

      double shifted = scaled + (double)min;
      if (shifted > (double)max) {
         shifted = (double)max;
      }

      return (float)shifted;
   }

   public static byte[] getRandomBytes(int minSize, int maxSize, byte min, byte max) {
      int size = getRandom_int(minSize, maxSize);
      byte[] out = new byte[size];

      for(int i = 0; i < size; ++i) {
         out[i] = getRandomByte(min, max);
      }

      return out;
   }

   public static int getRandom_int(int min, int max) {
      if (min > max) {
         return min;
      } else {
         Random RANDOM = new Random();
         return RANDOM.nextInt(max) + min;
      }
   }

   private static byte getRandomByte(byte min, byte max) {
      if (min > max) {
         return min;
      } else {
         Random RANDOM = new Random();
         return (byte)(RANDOM.nextInt(max) + min);
      }
   }

   public static float[][] getArcVertices(float radius, float angleStart, float angleEnd, int segments) {
      float range = Math.max(angleStart, angleEnd) - Math.min(angleStart, angleEnd);
      int nSegments = Math.max(2, Math.round(360.0F / range * (float)segments));
      float segDeg = range / (float)nSegments;
      float[][] vertices = new float[nSegments + 1][2];

      for(int i = 0; i <= nSegments; ++i) {
         float angleOfVert = (angleStart + (float)i * segDeg) / 180.0F * 3.1415927F;
         vertices[i][0] = (float)Math.sin((double)angleOfVert) * radius;
         vertices[i][1] = (float)(-Math.cos((double)angleOfVert)) * radius;
      }

      return vertices;
   }

   public static double distance(double srcX, double srcY, double srcZ, double dstX, double dstY, double dstZ) {
      double xDist = dstX - srcX;
      double yDist = dstY - srcY;
      double zDist = dstZ - srcZ;
      return Math.sqrt(xDist * xDist + yDist * yDist + zDist * zDist);
   }

   public static double distance(double srcX, double srcZ, double dstX, double dstZ) {
      double xDist = dstX - srcX;
      double zDist = dstZ - srcZ;
      return Math.sqrt(xDist * xDist + zDist * zDist);
   }
}
