package io.github.nevalackin.client.impl.event.player;

import io.github.nevalackin.client.api.event.CancellableEvent;

public final class UpdatePositionEvent extends CancellableEvent {
   public double posX;
   public double posY;
   public double posZ;
   private final double lastTickPosX;
   private final double lastTickPosY;
   private final double lastTickPosZ;
   private float yaw;
   private float pitch;
   private float lastTickYaw;
   private float lastTickPitch;
   public boolean onGround;
   private final boolean wasOnGround;
   private boolean post;
   private boolean rotating;

   public UpdatePositionEvent(double posX, double posY, double posZ, double lastTickPosX, double lastTickPosY, double lastTickPosZ, float yaw, float pitch, float lastTickYaw, float lastTickPitch, boolean onGround, boolean wasOnGround) {
      this.posX = posX;
      this.posY = posY;
      this.posZ = posZ;
      this.lastTickPosX = lastTickPosX;
      this.lastTickPosY = lastTickPosY;
      this.lastTickPosZ = lastTickPosZ;
      this.yaw = yaw;
      this.pitch = pitch;
      this.lastTickYaw = lastTickYaw;
      this.lastTickPitch = lastTickPitch;
      this.onGround = onGround;
      this.wasOnGround = wasOnGround;
   }

   public void setPost() {
      this.post = true;
   }

   public boolean isPre() {
      return !this.post;
   }

   public boolean isWasOnGround() {
      return this.wasOnGround;
   }

   public double getPosX() {
      return this.posX;
   }

   public void setPosX(double posX) {
      this.posX = posX;
   }

   public double getPosY() {
      return this.posY;
   }

   public void setPosY(double posY) {
      this.posY = posY;
   }

   public double getPosZ() {
      return this.posZ;
   }

   public void setPosZ(double posZ) {
      this.posZ = posZ;
   }

   public double getLastTickPosX() {
      return this.lastTickPosX;
   }

   public double getLastTickPosY() {
      return this.lastTickPosY;
   }

   public double getLastTickPosZ() {
      return this.lastTickPosZ;
   }

   public float getYaw() {
      return this.yaw;
   }

   public void setYaw(float yaw) {
      if (this.yaw - yaw != 0.0F) {
         this.rotating = true;
      }

      this.yaw = yaw;
   }

   public float getPitch() {
      return this.pitch;
   }

   public void setPitch(float pitch) {
      if (this.pitch - pitch != 0.0F) {
         this.rotating = true;
      }

      this.pitch = pitch;
   }

   public float getLastTickYaw() {
      return this.lastTickYaw;
   }

   public float getLastTickPitch() {
      return this.lastTickPitch;
   }

   public boolean isOnGround() {
      return this.onGround;
   }

   public void setOnGround(boolean onGround) {
      this.onGround = onGround;
   }

   public boolean isRotating() {
      return this.rotating;
   }

   public void setLastTickYaw(float lastTickYaw) {
      this.lastTickYaw = lastTickYaw;
   }

   public void setLastTickPitch(float lastTickPitch) {
      this.lastTickPitch = lastTickPitch;
   }
}
