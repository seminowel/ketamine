package io.github.nevalackin.client.impl.module.movement.extras;

import io.github.nevalackin.client.api.module.Category;
import io.github.nevalackin.client.api.module.Module;
import io.github.nevalackin.client.api.property.Property;
import io.github.nevalackin.client.impl.core.KetamineClient;
import io.github.nevalackin.client.impl.module.combat.rage.Aura;
import io.github.nevalackin.client.impl.property.BooleanProperty;
import io.github.nevalackin.client.impl.property.EnumProperty;
import io.github.nevalackin.client.util.movement.MovementUtil;
import io.github.nevalackin.client.util.player.InventoryUtil;
import io.github.nevalackin.client.util.player.RotationUtil;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.text.DecimalFormat;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C00PacketKeepAlive;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import org.apache.commons.lang3.RandomUtils;

public class Flight extends Module {
   private int slimeBlockStack;
   private int stage;
   private boolean hasRotated;
   private boolean placed;
   private double distXZ;
   private Vec3 startPos;
   private Aura aura;
   private DecimalFormat format = new DecimalFormat("0.0");
   private final EnumProperty modeProperty;
   private final BooleanProperty distProperty;
   @EventLink
   private final Listener onGetCurrentItem;
   @EventLink
   private final Listener onReceivePacket;
   @EventLink
   private final Listener onUpdatePosition;
   @EventLink
   private final Listener onRenderOverlay;
   @EventLink
   private final Listener onMoveEvent;
   @EventLink
   private final Listener onMoveFlyingInputEvent;

   public Flight() {
      super("Flight", Category.MOVEMENT, Category.SubCategory.MOVEMENT_EXTRAS);
      this.modeProperty = new EnumProperty("Mode", Flight.Mode.WATCHDOG);
      this.distProperty = new BooleanProperty("Show Distance", true, () -> {
         return ((Mode)this.modeProperty.getValue()).equals(Flight.Mode.WATCHDOG);
      });
      this.onGetCurrentItem = (event) -> {
         if (this.slimeBlockStack != -1 && this.slimeBlockStack >= 36) {
            event.setCurrentItem(this.slimeBlockStack - 36);
         }

      };
      this.onReceivePacket = (event) -> {
         S08PacketPlayerPosLook s08;
         if (((Mode)this.modeProperty.getValue()).equals(Flight.Mode.GHOSTLY) && event.getPacket() instanceof S08PacketPlayerPosLook) {
            s08 = (S08PacketPlayerPosLook)event.getPacket();
            if (this.mc.thePlayer.getDistance(s08.getX(), s08.getY(), s08.getZ()) < 9.0) {
               event.setCancelled();
               this.mc.getNetHandler().sendPacketDirect(new C03PacketPlayer.C06PacketPlayerPosLook(s08.getX(), s08.getY(), s08.getZ(), s08.getYaw(), s08.getPitch(), false));
            }
         }

         if (((Mode)this.modeProperty.getValue()).equals(Flight.Mode.GHOSTLY) && event.getPacket() instanceof S08PacketPlayerPosLook) {
            s08 = (S08PacketPlayerPosLook)event.getPacket();
            if (this.mc.thePlayer.getDistance(s08.getX(), s08.getY(), s08.getZ()) < 9.0) {
               event.setCancelled();
               this.mc.getNetHandler().sendPacketDirect(new C03PacketPlayer.C06PacketPlayerPosLook(s08.getX(), s08.getY(), s08.getZ(), s08.getYaw(), s08.getPitch(), false));
            }
         }

      };
      this.onUpdatePosition = (event) -> {
         try {
            double dffX = this.getDifference(this.startPos.xCoord, this.mc.thePlayer.posX);
            double dffZ = this.getDifference(this.startPos.zCoord, this.mc.thePlayer.posZ);
            this.distXZ = Math.sqrt(dffX * dffX + dffZ * dffZ);
         } catch (Exception var6) {
         }

         if (((Mode)this.modeProperty.getValue()).equals(Flight.Mode.WATCHDOG)) {
            if (event.isPre()) {
               this.slimeBlockStack = InventoryUtil.getSlimeBlockStack(this.mc, 36, 45);
               event.setPitch(90.0F);
               if (this.mc.thePlayer.onGround) {
                  this.mc.thePlayer.motionY = 0.41999998688697815;
               } else if (this.mc.thePlayer.fallDistance > 0.0F) {
                  this.mc.thePlayer.motionY = RandomUtils.nextDouble(1.0E-7, 1.2E-7);
               }
            } else if (!this.placed) {
               this.doPlace();
            } else {
               this.mc.thePlayer.sendQueue.sendPacket(new C09PacketHeldItemChange(this.mc.thePlayer.inventory.currentItem));
            }
         }

         int i;
         if (((Mode)this.modeProperty.getValue()).equals(Flight.Mode.GHOSTLY) && event.isPre()) {
            if (this.stage++ < 4) {
               event.posY += 51.099998474121094;
            }

            if (this.stage > 7) {
               this.stage = 0;
            }

            for(i = 0; i < 20; ++i) {
               this.mc.getNetHandler().sendPacketDirect(new C00PacketKeepAlive(Integer.MIN_VALUE));
               this.mc.getNetHandler().sendPacketDirect(new C0FPacketConfirmTransaction(Integer.MIN_VALUE, (short)Short.MIN_VALUE, false));
            }
         }

         if (((Mode)this.modeProperty.getValue()).equals(Flight.Mode.GHOSTLY) && event.isPre()) {
            if (this.stage++ < 4) {
               event.posY += 51.099998474121094;
            }

            if (this.stage > 7) {
               this.stage = 0;
            }

            for(i = 0; i < 20; ++i) {
               this.mc.getNetHandler().sendPacketDirect(new C00PacketKeepAlive(Integer.MIN_VALUE));
               this.mc.getNetHandler().sendPacketDirect(new C0FPacketConfirmTransaction(Integer.MIN_VALUE, (short)Short.MIN_VALUE, false));
            }
         }

      };
      this.onRenderOverlay = (event) -> {
         if (((Mode)this.modeProperty.getValue()).equals(Flight.Mode.WATCHDOG) && (Boolean)this.distProperty.getValue()) {
            this.mc.fontRendererObj.drawStringWithShadow("Dist Traveled " + this.format.format(this.distXZ) + " Blocks", 418.0F, 440.0F, -1);
         }

      };
      this.onMoveEvent = (event) -> {
         if (((Mode)this.modeProperty.getValue()).equals(Flight.Mode.WATCHDOG) && !this.placed) {
            event.setX(0.0);
            event.setZ(0.0);
            MovementUtil.setSpeed(this.mc.thePlayer, event, targetStrafeInstance, 0.0);
         }

         if (((Mode)this.modeProperty.getValue()).equals(Flight.Mode.MOTION)) {
            this.mc.thePlayer.motionX = this.mc.thePlayer.motionZ = 0.0;
            event.setY(this.mc.thePlayer.motionY = 0.0);
            if (MovementUtil.isMoving(this.mc.thePlayer)) {
               MovementUtil.setSpeed(this.mc.thePlayer, event, targetStrafeInstance, 1.0);
            }

            if (this.mc.gameSettings.keyBindJump.pressed) {
               event.setY(event.getY() + 1.0);
            }

            if (this.mc.gameSettings.keyBindSneak.pressed) {
               event.setY(event.getY() - 1.0);
            }
         }

         if (((Mode)this.modeProperty.getValue()).equals(Flight.Mode.GHOSTLY)) {
            this.mc.thePlayer.motionX = this.mc.thePlayer.motionZ = 0.0;
            event.setY(this.mc.thePlayer.motionY = 0.0);
            if (MovementUtil.isMoving(this.mc.thePlayer)) {
               MovementUtil.setSpeed(this.mc.thePlayer, event, targetStrafeInstance, 1.0);
            }

            if (this.mc.gameSettings.keyBindJump.pressed) {
               event.setY(event.getY() + 1.0);
            }

            if (this.mc.gameSettings.keyBindSneak.pressed) {
               event.setY(event.getY() - 1.0);
            }
         }

      };
      this.onMoveFlyingInputEvent = (event) -> {
         if (((Mode)this.modeProperty.getValue()).equals(Flight.Mode.WATCHDOG)) {
            float baseMoveSpeed = (float)MovementUtil.getBaseMoveSpeed(this.mc.thePlayer);
            if (MovementUtil.isMoving(this.mc.thePlayer) && this.mc.thePlayer.fallDistance > 0.0F) {
               if (targetStrafeInstance.shouldStrafe()) {
                  event.setYaw(RotationUtil.calculateYawFromSrcToDst(this.mc.thePlayer.rotationYaw, this.mc.thePlayer.posX, this.mc.thePlayer.posZ, targetStrafeInstance.currentPoint.point.xCoord, targetStrafeInstance.currentPoint.point.zCoord));
               }

               MovementUtil.setSpeedPartialStrafe(this.mc.thePlayer, event, targetStrafeInstance, baseMoveSpeed, 0.0F);
            }
         }

      };
      this.register(new Property[]{this.modeProperty, this.distProperty});
   }

   private void doPlace() {
      if (this.slimeBlockStack >= 36) {
         MovingObjectPosition rayTrace = RotationUtil.rayTraceBlocks(this.mc, 0.0F, 90.0F);
         if (rayTrace != null && rayTrace.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && rayTrace.hitVec != null) {
            ItemStack heldItem = this.mc.thePlayer.inventoryContainer.getSlot(this.slimeBlockStack).getStack();
            if (heldItem != null) {
               BlockPos blockPos = rayTrace.getBlockPos();
               if (this.mc.playerController.onPlayerRightClick(this.mc.thePlayer, this.mc.theWorld, heldItem, blockPos, rayTrace.sideHit, rayTrace.hitVec)) {
                  this.mc.thePlayer.sendQueue.sendPacket(new C0APacketAnimation());
                  this.placed = true;
               }

            }
         }
      }
   }

   private double getDifference(double base, double yaw) {
      double bigger;
      if (base >= yaw) {
         bigger = base - yaw;
      } else {
         bigger = yaw - base;
      }

      return bigger;
   }

   public void onEnable() {
      if (this.aura == null) {
         this.aura = (Aura)KetamineClient.getInstance().getModuleManager().getModule(Aura.class);
      }

      if (this.mc.thePlayer != null) {
         this.startPos = new Vec3(this.mc.thePlayer.posX, this.mc.thePlayer.posY, this.mc.thePlayer.posZ);
      }

      this.stage = 0;
      this.hasRotated = false;
      this.placed = false;
   }

   public void onDisable() {
   }

   public static enum Mode {
      WATCHDOG("Watchdog"),
      MOTION("Motion"),
      GHOSTLY("Ghostly");

      private final String name;

      private Mode(String name) {
         this.name = name;
      }

      public String toString() {
         return this.name;
      }
   }
}
