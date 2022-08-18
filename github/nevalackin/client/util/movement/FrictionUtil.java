package io.github.nevalackin.client.util.movement;

import java.util.Arrays;
import net.minecraft.client.entity.EntityPlayerSP;

public final class FrictionUtil {
   private static final double AIR_FRICTION = 0.9800000190734863;
   private static final double WATER_FRICTION = 0.8899999856948853;
   private static final double LAVA_FRICTION = 0.5350000262260437;
   public static final double BUNNY_DIV_FRICTION = 159.999;
   private static final double[] SPEEDS = new double[3];

   private FrictionUtil() {
   }

   public static double applyNCPFriction(EntityPlayerSP player, double moveSpeed, double lastDist, double baseMoveSpeedRef) {
      SPEEDS[0] = lastDist - lastDist / 159.999;
      SPEEDS[1] = lastDist - (moveSpeed - lastDist) / 33.3;
      double materialFriction = player.isInWater() ? 0.8899999856948853 : (player.isInLava() ? 0.5350000262260437 : 0.9800000190734863);
      SPEEDS[2] = lastDist - baseMoveSpeedRef * (1.0 - materialFriction);
      Arrays.sort(SPEEDS);
      return SPEEDS[0];
   }

   public static double applyVanillaFriction(EntityPlayerSP player, double moveSpeed, double lastDist, double baseMoveSpeedRef) {
      return moveSpeed * 0.9100000262260437;
   }

   @FunctionalInterface
   public interface Friction {
      double applyFriction(EntityPlayerSP var1, double var2, double var4, double var6);
   }
}
