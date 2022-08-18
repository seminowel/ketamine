package io.github.nevalackin.client.util.render;

import io.github.nevalackin.client.util.render.anims.Animation;
import io.github.nevalackin.client.util.render.anims.Easing;

public class Position2D {
   private double x;
   private double y;
   private final Animation animX = new Animation();
   private final Animation animY = new Animation();
   private boolean wasAnimated = false;

   public Position2D(double x, double y) {
      this.x = x;
      this.y = y;
   }

   public void animate(double newX, double newY, double duration, Easing easing) {
      if (!this.animX.isAlive() && !this.animY.isAlive() && this.wasAnimated) {
         this.wasAnimated = false;
      }

      if (!this.wasAnimated) {
         this.animX.setLastValue(this.x);
         this.animY.setLastValue(this.y);
         this.animX.animate(newX, duration, easing);
         this.animY.animate(newY, duration, easing);
         this.wasAnimated = true;
      }

      this.animX.updateAnimation();
      this.animY.updateAnimation();
      this.x = this.animX.getValue();
      this.y = this.animY.getValue();
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
}
