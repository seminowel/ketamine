package io.github.nevalackin.client.util.render.anims;

public class DAnimation {
   private long animationStart;
   private long animationEnd;
   private double animationFromValue;
   private double animationToValue;

   public double getValue() {
      double path = (double)(System.currentTimeMillis() - this.animationStart) / (double)(this.animationEnd - this.animationStart);
      if (path >= 1.0) {
         return this.animationToValue;
      } else {
         double value = (this.animationToValue - this.animationFromValue) * path + this.animationFromValue;
         return value;
      }
   }

   public void setValue(int value) {
      this.animationFromValue = (double)value;
      this.animationToValue = (double)value;
   }

   public void animate(double duration, double valueTo) {
      double path = (double)(System.currentTimeMillis() - this.animationStart) / (double)(this.animationEnd - this.animationStart);
      this.animationStart = System.currentTimeMillis();
      this.animationEnd = (long)((double)System.currentTimeMillis() + duration);
      if (!(path >= 1.0)) {
         this.animationFromValue += (this.animationToValue - this.animationFromValue) * path;
      } else {
         this.animationFromValue = this.animationToValue;
      }

      this.animationToValue = valueTo;
   }

   public boolean isDone() {
      double path = (double)(System.currentTimeMillis() - this.animationStart) / (double)(this.animationEnd - this.animationStart);
      return path >= 1.0;
   }

   public boolean isAlive() {
      double path = (double)(System.currentTimeMillis() - this.animationStart) / (double)(this.animationEnd - this.animationStart);
      return path < 1.0;
   }
}
