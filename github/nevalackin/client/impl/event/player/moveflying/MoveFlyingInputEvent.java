package io.github.nevalackin.client.impl.event.player.moveflying;

import io.github.nevalackin.client.api.event.CancellableEvent;

public class MoveFlyingInputEvent extends CancellableEvent {
   private float forward;
   private float strafe;
   private float friction;
   private float yaw;

   public MoveFlyingInputEvent(float forward, float strafe, float friction, float yaw) {
      this.forward = forward;
      this.strafe = strafe;
      this.friction = friction;
      this.yaw = yaw;
   }

   public float getForward() {
      return this.forward;
   }

   public float getYaw() {
      return this.yaw;
   }

   public void setYaw(float yaw) {
      this.yaw = yaw;
   }

   public void setForward(float forward) {
      this.forward = forward;
   }

   public float getStrafe() {
      return this.strafe;
   }

   public void setStrafe(float strafe) {
      this.strafe = strafe;
   }

   public float getFriction() {
      return this.friction;
   }

   public void setFriction(float friction) {
      this.friction = friction;
   }
}
