package io.github.nevalackin.client.util.player;

import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

public final class RotationUtil {
   private static final double RAD_TO_DEG = 57.29577951308232;
   private static final double DEG_TO_RAD = 0.017453292519943295;

   private RotationUtil() {
   }

   public static MovingObjectPosition calculateIntercept(AxisAlignedBB boundingBox, Vec3 src, float yaw, float pitch, double reach) {
      return boundingBox.calculateIntercept(src, getDstVec(src, yaw, pitch, reach));
   }

   public static Vec3 getAttackHitVec(Minecraft mc, Vec3 src, AxisAlignedBB boundingBox, Vec3 desiredHitVec, boolean ignoreBlocks, int maxRayTraces) {
      if (validateHitVec(mc, src, desiredHitVec, ignoreBlocks)) {
         return desiredHitVec;
      } else {
         double closestDist = Double.MAX_VALUE;
         Vec3 bone = null;
         double xWidth = boundingBox.maxX - boundingBox.minX;
         double zWidth = boundingBox.maxZ - boundingBox.minZ;
         double height = boundingBox.maxY - boundingBox.minY;
         int passes = 0;

         for(double x = 0.0; x < 1.0; x += 0.2) {
            for(double y = 0.0; y < 1.0; y += 0.2) {
               for(double z = 0.0; z < 1.0; z += 0.2) {
                  if (maxRayTraces != -1 && passes > maxRayTraces) {
                     return null;
                  }

                  Vec3 hitVec = new Vec3(boundingBox.minX + xWidth * x, boundingBox.minY + height * y, boundingBox.minZ + zWidth * z);
                  double dist;
                  if (validateHitVec(mc, src, hitVec, ignoreBlocks) && (dist = src.distanceTo(hitVec)) < closestDist) {
                     closestDist = dist;
                     bone = hitVec;
                  }

                  ++passes;
               }
            }
         }

         return bone;
      }
   }

   public static float[] calculateAverageRotations(List rotations) {
      int n = rotations.size();
      float[] tRots = new float[2];

      float[] rotation;
      for(Iterator var3 = rotations.iterator(); var3.hasNext(); tRots[1] += rotation[1]) {
         rotation = (float[])var3.next();
         tRots[0] += rotation[0];
      }

      tRots[0] /= (float)n;
      tRots[1] /= (float)n;
      return tRots;
   }

   public static boolean validateHitVec(Minecraft mc, Vec3 src, Vec3 dst, boolean ignoreBlocks, double penetrationDist) {
      Vec3 blockHitVec = rayTraceHitVec(mc, src, dst);
      if (blockHitVec == null) {
         return true;
      } else {
         double distance = src.distanceTo(dst);
         return ignoreBlocks && distance < penetrationDist;
      }
   }

   public static boolean validateHitVec(Minecraft mc, Vec3 src, Vec3 dst, boolean ignoreBlocks) {
      return validateHitVec(mc, src, dst, ignoreBlocks, 2.8);
   }

   public static Vec3 getHitOrigin(Entity entity) {
      return new Vec3(entity.posX, entity.posY + (double)entity.getEyeHeight(), entity.posZ);
   }

   public static Vec3 getClosestPoint(Vec3 start, AxisAlignedBB boundingBox) {
      double closestX = start.xCoord >= boundingBox.maxX ? boundingBox.maxX : (start.xCoord <= boundingBox.minX ? boundingBox.minX : boundingBox.minX + (start.xCoord - boundingBox.minX));
      double closestY = start.yCoord >= boundingBox.maxY ? boundingBox.maxY : (start.yCoord <= boundingBox.minY ? boundingBox.minY : boundingBox.minY + (start.yCoord - boundingBox.minY));
      double closestZ = start.zCoord >= boundingBox.maxZ ? boundingBox.maxZ : (start.zCoord <= boundingBox.minZ ? boundingBox.minZ : boundingBox.minZ + (start.zCoord - boundingBox.minZ));
      return new Vec3(closestX, closestY, closestZ);
   }

   public static Vec3 getCenterPointOnBB(AxisAlignedBB hitBox, double progressToTop) {
      double xWidth = hitBox.maxX - hitBox.minX;
      double zWidth = hitBox.maxZ - hitBox.minZ;
      double height = hitBox.maxY - hitBox.minY;
      return new Vec3(hitBox.minX + xWidth / 2.0, hitBox.minY + height * progressToTop, hitBox.minZ + zWidth / 2.0);
   }

   public static AxisAlignedBB getHittableBoundingBox(Entity entity, double boundingBoxScale) {
      return entity.getEntityBoundingBox().expand(boundingBoxScale, boundingBoxScale, boundingBoxScale);
   }

   public static AxisAlignedBB getHittableBoundingBox(Entity entity) {
      return getHittableBoundingBox(entity, (double)entity.getCollisionBorderSize());
   }

   public static Vec3 getAverageChange(List positions) {
      int nPositions = positions.size();
      if (nPositions <= 1) {
         return new Vec3(0.0, 0.0, 0.0);
      } else {
         Vec3 changeAccumulator = new Vec3(0.0, 0.0, 0.0);
         Vec3 previous = (Vec3)positions.get(0);

         for(int i = 1; i < nPositions; ++i) {
            Vec3 position = (Vec3)positions.get(i);
            changeAccumulator = changeAccumulator.addVector(previous.xCoord - position.xCoord, previous.yCoord - position.yCoord, previous.zCoord - position.zCoord);
            previous = position;
         }

         return new Vec3(changeAccumulator.xCoord / (double)nPositions, changeAccumulator.yCoord / (double)nPositions, changeAccumulator.zCoord / (double)nPositions);
      }
   }

   public static Vec3 getPointedVec(float yaw, float pitch) {
      double theta = -Math.cos((double)(-pitch) * 0.017453292519943295);
      return new Vec3(Math.sin((double)(-yaw) * 0.017453292519943295 - Math.PI) * theta, Math.sin((double)(-pitch) * 0.017453292519943295), Math.cos((double)(-yaw) * 0.017453292519943295 - Math.PI) * theta);
   }

   public static Vec3 getDstVec(Vec3 src, float yaw, float pitch, double reach) {
      Vec3 rotationVec = getPointedVec(yaw, pitch);
      return src.addVector(rotationVec.xCoord * reach, rotationVec.yCoord * reach, rotationVec.zCoord * reach);
   }

   public static Vec3 rayTraceHitVec(Minecraft mc, Vec3 src, Vec3 dst) {
      MovingObjectPosition rayTraceResult = mc.theWorld.rayTraceBlocks(src, dst, false, false, false);
      return rayTraceResult != null ? rayTraceResult.hitVec : null;
   }

   public static MovingObjectPosition rayTraceBlocks(Minecraft mc, Vec3 src, double reach, float yaw, float pitch) {
      return mc.theWorld.rayTraceBlocks(src, getDstVec(src, yaw, pitch, reach), false, false, true);
   }

   public static MovingObjectPosition rayTraceBlocks(Minecraft mc, float yaw, float pitch) {
      return rayTraceBlocks(mc, getHitOrigin(mc.thePlayer), (double)mc.playerController.getBlockReachDistance(), yaw, pitch);
   }

   public static float[] getRotations(Vec3 start, Vec3 dst) {
      double xDif = dst.xCoord - start.xCoord;
      double yDif = dst.yCoord - start.yCoord;
      double zDif = dst.zCoord - start.zCoord;
      double distXZ = Math.sqrt(xDif * xDif + zDif * zDif);
      return new float[]{(float)(Math.atan2(zDif, xDif) * 57.29577951308232) - 90.0F, (float)(-(Math.atan2(yDif, distXZ) * 57.29577951308232))};
   }

   public static float[] getRotations(float[] lastRotations, float smoothing, Vec3 start, Vec3 dst) {
      float[] rotations = getRotations(start, dst);
      applySmoothing(lastRotations, smoothing, rotations);
      return rotations;
   }

   public static void applySmoothing(float[] lastRotations, float smoothing, float[] dstRotation) {
      if (smoothing > 0.0F) {
         float yawChange = MathHelper.wrapAngleTo180_float(dstRotation[0] - lastRotations[0]);
         float pitchChange = MathHelper.wrapAngleTo180_float(dstRotation[1] - lastRotations[1]);
         float smoothingFactor = Math.max(1.0F, smoothing / 10.0F);
         dstRotation[0] = lastRotations[0] + yawChange / smoothingFactor;
         dstRotation[1] = Math.max(Math.min(90.0F, lastRotations[1] + pitchChange / smoothingFactor), -90.0F);
      }

   }

   public static double getMouseGCD() {
      float sens = Minecraft.getMinecraft().gameSettings.mouseSensitivity * 0.6F + 0.2F;
      float pow = sens * sens * sens * 8.0F;
      return (double)pow * 0.15;
   }

   public static void applyGCD(float[] rotations, float[] prevRots) {
      float yawDif = rotations[0] - prevRots[0];
      float pitchDif = rotations[1] - prevRots[1];
      double gcd = getMouseGCD();
      rotations[0] = (float)((double)rotations[0] - (double)yawDif % gcd);
      rotations[1] = (float)((double)rotations[1] - (double)pitchDif % gcd);
   }

   public static float calculateYawFromSrcToDst(float yaw, double srcX, double srcZ, double dstX, double dstZ) {
      double xDist = dstX - srcX;
      double zDist = dstZ - srcZ;
      float var1 = (float)(StrictMath.atan2(zDist, xDist) * 180.0 / Math.PI) - 90.0F;
      return yaw + MathHelper.wrapAngleTo180_float(var1 - yaw);
   }

   public static enum RotationsPoint {
      CLOSEST("Closest", RotationUtil::getClosestPoint),
      HEAD("Head", (start, hitBox) -> {
         return RotationUtil.getCenterPointOnBB(hitBox, 0.9);
      }),
      CHEST("Chest", (start, hitBox) -> {
         return RotationUtil.getCenterPointOnBB(hitBox, 0.7);
      }),
      PELVIS("Pelvis", (start, hitBox) -> {
         return RotationUtil.getCenterPointOnBB(hitBox, 0.5);
      }),
      LEGS("Legs", (start, hitBox) -> {
         return RotationUtil.getCenterPointOnBB(hitBox, 0.3);
      }),
      FEET("Feet", (start, hitBox) -> {
         return RotationUtil.getCenterPointOnBB(hitBox, 0.1);
      });

      private final String name;
      private final BiFunction getHitVecFunc;

      private RotationsPoint(String name, BiFunction getHitVecFunc) {
         this.name = name;
         this.getHitVecFunc = getHitVecFunc;
      }

      public Vec3 getHitVec(Vec3 start, AxisAlignedBB hitBox) {
         return (Vec3)this.getHitVecFunc.apply(start, hitBox);
      }

      public String toString() {
         return this.name;
      }
   }
}
