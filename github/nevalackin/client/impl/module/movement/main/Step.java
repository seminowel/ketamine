package io.github.nevalackin.client.impl.module.movement.main;

import io.github.nevalackin.client.api.module.Category;
import io.github.nevalackin.client.api.module.Module;
import io.github.nevalackin.client.api.property.Property;
import io.github.nevalackin.client.impl.property.BooleanProperty;
import io.github.nevalackin.client.impl.property.EnumProperty;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.network.play.client.C03PacketPlayer;

public final class Step extends Module {
   private final EnumProperty modeProperty;
   private final BooleanProperty higherStep;
   private final BooleanProperty lessPacketsProperty;
   private final double[] offsets;
   private double stepTimer;
   private boolean lessPackets;
   private int cancelledPackets;
   @EventLink
   private final Listener onStep;
   @EventLink
   private final Listener onGetTimer;
   @EventLink
   public final Listener onUpdatePosition;

   public Step() {
      super("Step", Category.MOVEMENT, Category.SubCategory.MOVEMENT_MAIN);
      this.modeProperty = new EnumProperty("Mode", Step.Mode.NCP);
      this.higherStep = new BooleanProperty("1.5 Block", true, () -> {
         return this.modeProperty.getValue() == Step.Mode.NCP;
      });
      this.lessPacketsProperty = new BooleanProperty("Less Packets", true, () -> {
         return this.modeProperty.getValue() == Step.Mode.NCP;
      });
      this.offsets = new double[]{0.42, 0.7532, 1.0};
      this.onStep = (event) -> {
         if (!this.mc.thePlayer.isInWater() && !this.mc.thePlayer.isInLava() && this.mc.thePlayer.onGround && this.mc.thePlayer.isCollidedVertically && !this.mc.thePlayer.isOnLadder()) {
            if (event.isPre()) {
               event.setStepHeight(1.0F);
            } else if (this.modeProperty.getValue() == Step.Mode.NCP) {
               double steppedHeight = event.getHeightStepped();
               if (steppedHeight > (double)this.mc.thePlayer.stepHeight) {
                  double[] var4 = this.offsets;
                  int var5 = var4.length;

                  for(int var6 = 0; var6 < var5; ++var6) {
                     double offset = var4[var6];
                     this.mc.thePlayer.sendQueue.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(this.mc.thePlayer.posX, this.mc.thePlayer.posY + offset * Math.min(steppedHeight, 1.0), this.mc.thePlayer.posZ, false));
                  }

                  this.stepTimer = 1.0 / (double)(this.offsets.length + 1);
                  this.lessPackets = (Boolean)this.lessPacketsProperty.getValue();
               }
            }
         }

      };
      this.onGetTimer = (event) -> {
         if (this.lessPackets) {
            event.setTimerSpeed(this.stepTimer);
         }

      };
      this.onUpdatePosition = (event) -> {
         if (this.cancelledPackets > 1) {
            this.lessPackets = false;
            this.cancelledPackets = 0;
         }

         if (this.lessPackets) {
            ++this.cancelledPackets;
         }

      };
      this.setSuffix(() -> {
         return ((Mode)this.modeProperty.getValue()).toString();
      });
      this.register(new Property[]{this.modeProperty, this.lessPacketsProperty});
   }

   public void onEnable() {
      this.cancelledPackets = 0;
      this.lessPackets = false;
   }

   public void onDisable() {
   }

   private static enum Mode {
      VANILLA("Vanilla"),
      NCP("NCP");

      private final String name;

      private Mode(String name) {
         this.name = name;
      }

      public String toString() {
         return this.name;
      }
   }
}
