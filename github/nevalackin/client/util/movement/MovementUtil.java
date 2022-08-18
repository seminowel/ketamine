package io.github.nevalackin.client.util.movement;

import io.github.nevalackin.client.impl.event.player.MoveEvent;
import io.github.nevalackin.client.impl.event.player.moveflying.MoveFlyingInputEvent;
import io.github.nevalackin.client.impl.module.combat.rage.TargetStrafe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

public final class MovementUtil {
   public static final double SPRINTING_MOD = 0.7692307974459868;
   public static final double SNEAK_MOD = 0.30000001192092896;
   public static final double ICE_MOD = 2.5;
   public static final double WALK_SPEED = 0.221;
   private static final double SWIM_MOD = 0.5203620003898759;
   private static final double[] DEPTH_STRIDER_VALUES = new double[]{1.0, 1.4304347400741908, 1.7347825295420372, 1.9217390955733897};
   public static final double MIN_DIST = 0.001;

   private MovementUtil() {
   }

   public static boolean isBlockUnder(Minecraft mc) {
      return mc.theWorld.checkBlockCollision(mc.thePlayer.getEntityBoundingBox().addCoord(0.0, -1.0, 0.0));
   }

   public static boolean isOverVoid(Minecraft mc) {
      AxisAlignedBB bb = mc.thePlayer.getEntityBoundingBox();
      double height = bb.maxY - bb.minY;

      AxisAlignedBB bbPos;
      for(double offset = height; !mc.theWorld.checkBlockCollision(bbPos = bb.addCoord(0.0, -offset, 0.0)); offset += height) {
         if (bbPos.minY <= 0.0) {
            return true;
         }
      }

      return false;
   }

   public static boolean canSprint(EntityPlayerSP player, boolean omni) {
      return (player.movementInput.moveForward >= 0.8F || omni && isMoving(player)) && ((float)player.getFoodStats().getFoodLevel() > 6.0F || player.capabilities.allowFlying) && !player.isPotionActive(Potion.blindness) && !player.isCollidedHorizontally && !player.isSneaking();
   }

   public static boolean canSprint(EntityPlayerSP player) {
      return canSprint(player, true);
   }

   public static void setSpeedMoveFlying(EntityPlayerSP player, MoveFlyingInputEvent event, TargetStrafe targetStrafe, double speed) {
      if (targetStrafe.shouldStrafe()) {
         if (targetStrafe.shouldAdaptSpeed()) {
            speed = Math.min(speed, targetStrafe.getAdaptedSpeed());
         }

         targetStrafe.setSpeed(event, speed);
      } else {
         setSpeedMoveFlying(player, event, speed, player.moveForward, player.moveStrafing, player.rotationYaw);
      }
   }

   public static void setSpeedMoveFlying(EntityPlayerSP player, MoveFlyingInputEvent moveEvent, double speed, float forward, float strafing, float yaw) {
      if (forward != 0.0F || strafing != 0.0F) {
         player.motionX = player.motionZ = 0.0;
         moveEvent.setFriction((float)speed);
      }
   }

   public static void setSpeedPartialStrafe(EntityPlayerSP player, MoveFlyingInputEvent event, TargetStrafe targetStrafe, float moveSpeed, float strafeMotion) {
      float remainder = 1.0F - strafeMotion;
      if (player.onGround) {
         setSpeedMoveFlying(player, event, targetStrafe, (double)moveSpeed);
      } else {
         player.motionX *= (double)strafeMotion;
         player.motionZ *= (double)strafeMotion;
         event.setFriction(moveSpeed * remainder);
      }

   }

   public static float getMovementDirection(float forward, float strafing, float yaw) {
      if (forward == 0.0F && strafing == 0.0F) {
         return yaw;
      } else {
         boolean reversed = forward < 0.0F;
         float strafingYaw = 90.0F * (forward > 0.0F ? 0.5F : (reversed ? -0.5F : 1.0F));
         if (reversed) {
            yaw += 180.0F;
         }

         if (strafing > 0.0F) {
            yaw -= strafingYaw;
         } else if (strafing < 0.0F) {
            yaw += strafingYaw;
         }

         return yaw;
      }
   }

   public static void setSpeed(MoveEvent moveEvent, double speed, float forward, float strafing, float yaw) {
      if (forward != 0.0F || strafing != 0.0F) {
         yaw = getMovementDirection(forward, strafing, yaw);
         double movementDirectionRads = Math.toRadians((double)yaw);
         double x = -Math.sin(movementDirectionRads) * speed;
         double z = Math.cos(movementDirectionRads) * speed;
         moveEvent.setX(x);
         moveEvent.setZ(z);
      }
   }

   public static void setSpeed(EntityPlayerSP player, MoveEvent event, TargetStrafe targetStrafe, double speed) {
      if (targetStrafe.shouldStrafe()) {
         if (targetStrafe.shouldAdaptSpeed()) {
            speed = Math.min(speed, targetStrafe.getAdaptedSpeed());
         }

         targetStrafe.setSpeed(event, speed);
      } else {
         setSpeed(event, speed, player.moveForward, player.moveStrafing, player.rotationYaw);
      }
   }

   public static boolean isOnGround(World world, EntityPlayerSP player, double offset) {
      return world.checkBlockCollision(player.getEntityBoundingBox().addCoord(0.0, -offset, 0.0));
   }

   public static double getBaseMoveSpeed(EntityPlayerSP player) {
      double base = player.isSneaking() ? 0.0663000026345253 : (canSprint(player) ? 0.2872999894618988 : 0.221);
      PotionEffect speed = player.getActivePotionEffect(Potion.moveSpeed);
      int moveSpeedAmp = speed == null ? 0 : speed.getAmplifier() + 1;
      if (moveSpeedAmp > 0) {
         base *= 1.0 + 0.2 * (double)moveSpeedAmp;
      }

      if (player.isInWater()) {
         base *= 0.5203620003898759;
         int depthStriderLevel = EnchantmentHelper.getDepthStriderModifier(player);
         if (depthStriderLevel > 0) {
            base *= DEPTH_STRIDER_VALUES[depthStriderLevel];
         }

         return base * 0.5203620003898759;
      } else {
         return player.isInLava() ? base * 0.5203620003898759 : base;
      }
   }

   public static void hypixelDamage(EntityPlayerSP player) {
      if (player != null) {
         for(int i = 0; i < 101; ++i) {
            player.sendQueue.sendPacketDirect(new C03PacketPlayer.C04PacketPlayerPosition(player.posX, player.posY + 0.03, player.posZ, false));
            player.sendQueue.sendPacketDirect(new C03PacketPlayer.C04PacketPlayerPosition(player.posX, player.posY, player.posZ, false));
         }

         player.sendQueue.sendPacketDirect(new C03PacketPlayer.C04PacketPlayerPosition(player.posX, player.posY, player.posZ, true));
      }

   }

   public static void zaneDamage(EntityPlayerSP player) {
      double x = player.posX;
      double y = player.posY;
      double z = player.posZ;
      float minDmgDist = JumpUtil.getMinFallDist(player);

      double lo;
      double hi;
      for(double inc = 0.0625; minDmgDist > 0.0F; minDmgDist = (float)((double)minDmgDist - (hi - lo))) {
         lo = Math.random() * 0.0010000000474974513;
         hi = inc - lo;
         player.sendQueue.sendPacketDirect(new C03PacketPlayer.C04PacketPlayerPosition(x, y + hi, z, false));
         player.sendQueue.sendPacketDirect(new C03PacketPlayer.C04PacketPlayerPosition(x, y + lo, z, false));
      }

      player.sendQueue.sendPacketDirect(new C03PacketPlayer.C04PacketPlayerPosition(x, y, z, true));
   }

   public static boolean isMoving(EntityPlayerSP player) {
      return player.moveForward != 0.0F || player.moveStrafing != 0.0F;
   }
}
