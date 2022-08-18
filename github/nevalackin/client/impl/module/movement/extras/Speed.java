package io.github.nevalackin.client.impl.module.movement.extras;

import io.github.nevalackin.client.api.module.Category;
import io.github.nevalackin.client.api.module.Module;
import io.github.nevalackin.client.api.notification.NotificationType;
import io.github.nevalackin.client.api.property.Property;
import io.github.nevalackin.client.impl.core.KetamineClient;
import io.github.nevalackin.client.impl.module.combat.healing.AutoPot;
import io.github.nevalackin.client.impl.module.combat.rage.Aura;
import io.github.nevalackin.client.impl.module.misc.world.Scaffold;
import io.github.nevalackin.client.impl.property.BooleanProperty;
import io.github.nevalackin.client.impl.property.DoubleProperty;
import io.github.nevalackin.client.impl.property.EnumProperty;
import io.github.nevalackin.client.util.math.MathUtil;
import io.github.nevalackin.client.util.movement.JumpUtil;
import io.github.nevalackin.client.util.movement.MovementUtil;
import io.github.nevalackin.client.util.player.RotationUtil;
import io.github.nevalackin.client.util.render.BlurUtil;
import io.github.nevalackin.client.util.render.ColourUtil;
import io.github.nevalackin.client.util.render.DrawUtil;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.util.Timer;
import java.util.TimerTask;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import org.lwjgl.input.Keyboard;

public final class Speed extends Module {
   private static final double[] LOW_HOP_Y_POSITIONS = new double[]{MathUtil.round(0.4, 0.001), MathUtil.round(0.71, 0.001), MathUtil.round(0.75, 0.001), MathUtil.round(0.55, 0.001), MathUtil.round(0.41, 0.001)};
   public static final EnumProperty modeProperty;
   private final DoubleProperty vanillaSpeedProperty = new DoubleProperty("Vanilla Speed", 1.0, this::isVanilla, 0.1, 2.0, 0.1);
   private final EnumProperty jumpTypeProperty;
   private final BooleanProperty velocityBoostProperty;
   private final DoubleProperty boostSpeedProperty;
   private final DoubleProperty reduceProperty;
   private final DoubleProperty strafeMotionProperty;
   private final BooleanProperty timerProperty;
   private final BooleanProperty onSpaceProperty;
   private final DoubleProperty timerSpeedProperty;
   private final BooleanProperty disableOnFlagProperty;
   private double moveSpeed;
   private boolean wasInitialLowHop;
   private boolean wasOnGround;
   private boolean disable;
   private long timeOfFlag;
   private final Timer turnBackOnTimer;
   private final long turnBackOnDelay;
   private Aura aura;
   private AutoPot autopot;
   private Scaffold scaffold;
   @EventLink
   private final Listener onTimerSpeed;
   @EventLink
   private final Listener onUpdatePosition;
   @EventLink
   private final Listener onMoveFlying;
   @EventLink
   private final Listener onRenderOverlay;
   @EventLink
   private final Listener onReceivePacket;

   public Speed() {
      super("Speed", Category.MOVEMENT, Category.SubCategory.MOVEMENT_EXTRAS);
      this.jumpTypeProperty = new EnumProperty("Jump Type", Speed.Jump.NORMAL, () -> {
         return !this.isVanilla();
      });
      this.velocityBoostProperty = new BooleanProperty("Damage Boost", false);
      this.boostSpeedProperty = new DoubleProperty("Boost", 2.1, () -> {
         return !this.isVanilla();
      }, 1.0, 2.149, 0.01);
      this.reduceProperty = new DoubleProperty("Reduce", 0.66, () -> {
         return !this.isVanilla();
      }, 0.01, 1.0, 0.01);
      this.strafeMotionProperty = new DoubleProperty("Strafe Motion", 0.6, () -> {
         return !this.isVanilla();
      }, 0.0, 1.0, 0.01);
      this.timerProperty = new BooleanProperty("Timer", false);
      BooleanProperty var10005 = this.timerProperty;
      var10005.getClass();
      this.onSpaceProperty = new BooleanProperty("On Space", false, var10005::getValue);
      this.timerSpeedProperty = new DoubleProperty("Timer Speed", 1.0, this.timerProperty::getValue, 1.0, 10.0, 0.1);
      this.disableOnFlagProperty = new BooleanProperty("Disable On Flag", true);
      this.turnBackOnTimer = new Timer();
      this.turnBackOnDelay = 2500L;
      this.onTimerSpeed = (event) -> {
         if ((Boolean)this.timerProperty.getValue() && !this.disable && (!(Boolean)this.onSpaceProperty.getValue() || Keyboard.isKeyDown(57))) {
            event.setTimerSpeed((Double)this.timerSpeedProperty.getValue());
         }

      };
      this.onUpdatePosition = (event) -> {
         if (this.jumpTypeProperty.getValue() == Speed.Jump.FASTFALL && this.mc.thePlayer.motionY < 0.1 && this.mc.thePlayer.motionY > -0.25 && (double)this.mc.thePlayer.fallDistance < 0.1) {
            EntityPlayerSP var10000 = this.mc.thePlayer;
            var10000.motionY -= 0.055;
         }

      };
      this.onMoveFlying = (event) -> {
         if (!this.disable && MovementUtil.isMoving(this.mc.thePlayer)) {
            double baseMoveSpeed = MovementUtil.getBaseMoveSpeed(this.mc.thePlayer);
            boolean doInitialLowHop = this.jumpTypeProperty.getValue() == Speed.Jump.LOWHOP && !this.mc.gameSettings.keyBindJump.isKeyDown() && !this.mc.thePlayer.isPotionActive(Potion.jump) && !this.scaffold.isEnabled() && !this.mc.thePlayer.isCollidedHorizontally && this.simJumpShouldDoLowHop(baseMoveSpeed);
            switch ((Mode)modeProperty.getValue()) {
               case WATCHDOG:
                  if (!this.mc.thePlayer.onGround && this.wasInitialLowHop && (double)this.mc.thePlayer.fallDistance < 0.54) {
                     this.mc.thePlayer.motionY = this.lowHopYModification(this.mc.thePlayer.motionY, MathUtil.round(this.mc.thePlayer.posY - (double)((int)this.mc.thePlayer.posY), 0.001));
                  }

                  if (this.mc.thePlayer.onGround && !this.wasOnGround) {
                     this.moveSpeed = baseMoveSpeed * (Double)this.boostSpeedProperty.getValue();
                     this.wasInitialLowHop = doInitialLowHop;
                     this.mc.thePlayer.motionY = doInitialLowHop ? 0.4000000059604645 : JumpUtil.getJumpHeight(this.mc.thePlayer);
                     this.wasOnGround = true;
                  } else if (this.wasOnGround) {
                     this.wasOnGround = false;
                     this.moveSpeed *= (double)((Double)this.reduceProperty.getValue()).floatValue();
                  } else {
                     this.moveSpeed = this.moveSpeed / 100.0 * 98.5;
                  }

                  this.moveSpeed = Math.max(this.moveSpeed, baseMoveSpeed);
                  if (targetStrafeInstance.shouldStrafe()) {
                     event.setYaw(RotationUtil.calculateYawFromSrcToDst(this.mc.thePlayer.rotationYaw, this.mc.thePlayer.posX, this.mc.thePlayer.posZ, targetStrafeInstance.currentPoint.point.xCoord, targetStrafeInstance.currentPoint.point.zCoord));
                  }

                  MovementUtil.setSpeedPartialStrafe(this.mc.thePlayer, event, targetStrafeInstance, (float)this.moveSpeed, ((Double)this.strafeMotionProperty.getValue()).floatValue());
                  break;
               case VANILLA:
                  MovementUtil.setSpeedPartialStrafe(this.mc.thePlayer, event, targetStrafeInstance, ((Double)this.vanillaSpeedProperty.getValue()).floatValue(), 1.0F);
            }
         }

         if (!MovementUtil.isMoving(this.mc.thePlayer)) {
            MovementUtil.setSpeedPartialStrafe(this.mc.thePlayer, event, targetStrafeInstance, 0.0F, 0.0F);
         }

      };
      this.onRenderOverlay = (event) -> {
         ScaledResolution scaledResolution = event.getScaledResolution();
         if (this.disable) {
            long currentMillis = System.currentTimeMillis();
            long timeSinceFlagged = currentMillis - this.timeOfFlag;
            if (timeSinceFlagged < 2500L) {
               double xRegionBuffer = 4.0;
               double yRegionBuffer = 2.0;
               String text = String.format("Re-enabling in %.1f seconds", (float)(2500L - timeSinceFlagged) / 1000.0F);
               double width = (double)this.mc.fontRendererObj.getStringWidth(text) + 8.0;
               double height = 24.0;
               double barHeight = 6.0;
               double centrePosX = (double)scaledResolution.getScaledWidth() / 2.0;
               double centrePosY = (double)scaledResolution.getScaledHeight() / 2.0 + 100.0;
               double progress = (double)timeSinceFlagged / 2500.0;
               double leftBackground = centrePosX - width / 2.0;
               double rightBackground = centrePosX + width / 2.0;
               BlurUtil.blurArea(leftBackground, centrePosY, width, 24.0);
               DrawUtil.glDrawFilledQuad(leftBackground, centrePosY, width, 24.0, Integer.MIN_VALUE);
               DrawUtil.glDrawGradientLine(leftBackground, centrePosY, rightBackground, centrePosY, 1.0F, ColourUtil.getClientColour());
               double left = centrePosX - (width - 8.0) / 2.0;
               double top = centrePosY + 24.0 - 6.0 - 2.0;
               DrawUtil.glDrawFilledQuad(left, top, width - 8.0, 6.0, Integer.MIN_VALUE);
               DrawUtil.glDrawSidewaysGradientRect(left, top, (width - 8.0) * progress, 6.0, ColourUtil.fadeBetween(ColourUtil.getClientColour(), ColourUtil.getSecondaryColour()), ColourUtil.fadeBetween(ColourUtil.getSecondaryColour(), ColourUtil.getClientColour()));
               this.mc.fontRendererObj.drawStringWithShadow(text, (float)left, (float)centrePosY + 4.0F, -1);
            }
         }

      };
      this.onReceivePacket = (event) -> {
         if (event.getPacket() instanceof S08PacketPlayerPosLook && !this.disable && (Boolean)this.disableOnFlagProperty.getValue()) {
            this.timeOfFlag = System.currentTimeMillis();
            this.disable = true;
            KetamineClient.getInstance().getNotificationManager().add(NotificationType.ERROR, "Flagged", String.format("Flag detected re-enabling Speed in %.0fs", 2.5F), 2500L);
            this.turnBackOnTimer.purge();
            this.turnBackOnTimer.schedule(new TimerTask() {
               public void run() {
                  Speed.this.reset();
               }
            }, 2500L);
         }

         if (event.getPacket() instanceof S12PacketEntityVelocity && (Boolean)this.velocityBoostProperty.getValue()) {
            S12PacketEntityVelocity packetVelocity = (S12PacketEntityVelocity)event.getPacket();
            double x = (double)packetVelocity.getMotionX() / 8000.0;
            double z = (double)packetVelocity.getMotionZ() / 8000.0;
            if (packetVelocity.getEntityID() != this.mc.thePlayer.getEntityId()) {
               return;
            }

            double motion = Math.abs(x) + Math.abs(z);
            if (motion >= 0.2) {
               event.setCancelled(true);
               double speed = Math.sqrt(x * x + z * z);
               this.moveSpeed = MovementUtil.getBaseMoveSpeed(this.mc.thePlayer) + speed * 0.8;
            }
         }

      };
      this.setSuffix(() -> {
         return ((Mode)modeProperty.getValue()).toString();
      });
      this.register(new Property[]{modeProperty, this.jumpTypeProperty, this.velocityBoostProperty, this.boostSpeedProperty, this.reduceProperty, this.disableOnFlagProperty, this.strafeMotionProperty, this.timerProperty, this.timerSpeedProperty, this.onSpaceProperty, this.vanillaSpeedProperty});
   }

   private boolean isVanilla() {
      return modeProperty.getValue() == Speed.Mode.VANILLA;
   }

   private boolean simJumpShouldDoLowHop(double baseMoveSpeedRef) {
      float direction = RotationUtil.calculateYawFromSrcToDst(this.mc.thePlayer.rotationYaw, this.mc.thePlayer.lastReportedPosX, this.mc.thePlayer.lastReportedPosZ, this.mc.thePlayer.posX, this.mc.thePlayer.posZ);
      Vec3 start = new Vec3(this.mc.thePlayer.posX, this.mc.thePlayer.posY + LOW_HOP_Y_POSITIONS[2], this.mc.thePlayer.posZ);
      MovingObjectPosition rayTrace = this.mc.theWorld.rayTraceBlocks(start, RotationUtil.getDstVec(start, direction, 0.0F, 8.0), false, true, true);
      if (rayTrace == null) {
         return true;
      } else if (rayTrace.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) {
         return true;
      } else if (rayTrace.hitVec == null) {
         return true;
      } else {
         AxisAlignedBB bb = this.mc.thePlayer.getEntityBoundingBox();
         if (this.mc.theWorld.checkBlockCollision(bb.offset(bb.minX - rayTrace.hitVec.xCoord, bb.minY - rayTrace.hitVec.yCoord, bb.minZ - rayTrace.hitVec.zCoord))) {
            return false;
         } else {
            double dist = start.distanceTo(rayTrace.hitVec);
            double normalJumpDist = 4.0;
            return dist > 4.0;
         }
      }
   }

   public void onEnable() {
      if (this.scaffold == null) {
         this.scaffold = (Scaffold)KetamineClient.getInstance().getModuleManager().getModule(Scaffold.class);
      }

      if (this.aura == null) {
         this.aura = (Aura)KetamineClient.getInstance().getModuleManager().getModule(Aura.class);
      }

      if (this.autopot == null) {
         this.autopot = (AutoPot)KetamineClient.getInstance().getModuleManager().getModule(AutoPot.class);
      }

      this.reset();
   }

   private void reset() {
      this.wasInitialLowHop = false;
      this.moveSpeed = 0.221;
      this.disable = false;
   }

   public void onDisable() {
   }

   private double lowHopYModification(double baseMotionY, double yDistFromGround) {
      if (yDistFromGround == LOW_HOP_Y_POSITIONS[0]) {
         return 0.31;
      } else if (yDistFromGround == LOW_HOP_Y_POSITIONS[1]) {
         return 0.04;
      } else if (yDistFromGround == LOW_HOP_Y_POSITIONS[2]) {
         return -0.2;
      } else if (yDistFromGround == LOW_HOP_Y_POSITIONS[3]) {
         return -0.14;
      } else {
         return yDistFromGround == LOW_HOP_Y_POSITIONS[4] ? -0.2 : baseMotionY;
      }
   }

   static {
      modeProperty = new EnumProperty("Mode", Speed.Mode.WATCHDOG);
   }

   public static enum Mode {
      VANILLA("Vanilla"),
      WATCHDOG("Watchdog");

      private final String name;

      private Mode(String name) {
         this.name = name;
      }

      public String toString() {
         return this.name;
      }
   }

   private static enum Jump {
      NORMAL("Normal"),
      LOWHOP("Low Hop"),
      FASTFALL("Fast Fall");

      private final String name;

      private Jump(String name) {
         this.name = name;
      }

      public String toString() {
         return this.name;
      }
   }
}
