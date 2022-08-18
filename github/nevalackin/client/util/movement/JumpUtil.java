package io.github.nevalackin.client.util.movement;

import io.github.nevalackin.client.util.math.MathUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;

public final class JumpUtil {
   private JumpUtil() {
   }

   public static double getJumpHeight(EntityPlayerSP player) {
      double base = 0.41999998688697815;
      PotionEffect effect = player.getActivePotionEffect(Potion.jump);
      return effect == null ? 0.41999998688697815 : 0.41999998688697815 + (double)((float)(effect.getAmplifier() + 1) * 0.1F);
   }

   public static float getMinFallDist(EntityPlayerSP player) {
      float baseFallDist = 3.0F;
      PotionEffect effect = player.getActivePotionEffect(Potion.jump);
      int amp = effect != null ? effect.getAmplifier() + 1 : 0;
      return 3.0F + (float)amp;
   }

   public static double getGroundPosition(EntityPlayerSP player) {
      for(BlockPos check = player.getPosition(); check.getY() >= 0; check = check.down()) {
         if (!canMoveThrough(check)) {
            return getCollisionBoundingBox(check).maxY;
         }
      }

      return Double.POSITIVE_INFINITY;
   }

   private static IBlockState getBlockState(BlockPos pos) {
      return Minecraft.getMinecraft().theWorld.getBlockState(pos);
   }

   public static Block getBlock(BlockPos pos) {
      IBlockState state = getBlockState(pos);
      return state == null ? null : state.getBlock();
   }

   public static boolean canMoveThrough(BlockPos pos) {
      Block block = getBlock(pos);
      if (block == null) {
         return false;
      } else {
         return !block.getMaterial().blocksMovement();
      }
   }

   public static AxisAlignedBB getCollisionBoundingBox(BlockPos pos) {
      Block block = getBlock(pos);
      return block != null && !(block instanceof BlockAir) ? block.getCollisionBoundingBox(Minecraft.getMinecraft().thePlayer.worldObj, pos, getBlockState(pos)) : null;
   }

   public static double calculateJumpDistance(double baseMoveSpeedRef, double[] velocity, double lastDist, MotionModificationFunc motionModificationFunc) {
      double posY = 0.0;
      double totalDistance = 0.0;
      int tick = 0;

      do {
         if (Math.abs(velocity[0]) < 0.005) {
            velocity[0] = 0.0;
         }

         if (Math.abs(velocity[1]) < 0.005) {
            velocity[1] = 0.0;
         }

         if (Math.abs(velocity[2]) < 0.005) {
            velocity[2] = 0.0;
         }

         motionModificationFunc.runSimulation(velocity, baseMoveSpeedRef, lastDist, MathUtil.round(posY - (double)((int)posY), 0.001), tick);
         posY += velocity[1];
         double dist = Math.sqrt(velocity[0] * velocity[0] + velocity[2] * velocity[2]);
         lastDist = dist;
         totalDistance += dist;
         velocity[1] -= 0.08;
         velocity[1] *= 0.9800000190734863;
         ++tick;
      } while(posY > 0.0);

      return totalDistance;
   }
}
