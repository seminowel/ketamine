package io.github.nevalackin.client.impl.event.player;

import io.github.nevalackin.client.api.event.CancellableEvent;

public final class JumpEvent extends CancellableEvent {
   private float yaw;
   private double motion;

   public JumpEvent(double motion, float yaw) {
      this.yaw = yaw;
      this.motion = motion;
   }

   public double getMotion() {
      return this.motion;
   }

   public void setMotion(double motion) {
      this.motion = motion;
   }

   public float getYaw() {
      return this.yaw;
   }

   public void setYaw(float yaw) {
      this.yaw = yaw;
   }
}
