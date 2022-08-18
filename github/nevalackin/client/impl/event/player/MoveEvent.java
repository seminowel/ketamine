package io.github.nevalackin.client.impl.event.player;

import io.github.nevalackin.client.api.event.CancellableEvent;

public final class MoveEvent extends CancellableEvent {
   private double x;
   public double y;
   private double z;

   public MoveEvent(double x, double y, double z) {
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public double getX() {
      return this.x;
   }

   public void setX(double x) {
      this.x = x;
   }

   public double getY() {
      return this.y;
   }

   public void setY(double y) {
      this.y = y;
   }

   public double getZ() {
      return this.z;
   }

   public void setZ(double z) {
      this.z = z;
   }
}
