package io.github.nevalackin.client.util.render.anims;

public class Animation {
   private long animationStart;
   private double duration;
   private double animationFromValue;
   private double animationToValue;
   private Easing easing;
   private double lastValue;

   public Animation() {
      this.easing = Easings.NONE;
   }

   public void setLastValue(double lastValue) {
      this.lastValue = lastValue;
   }

   public double getValue() {
      return this.lastValue;
   }

   public void setValue(double value) {
      this.animationFromValue = value;
      this.animationToValue = value;
   }

   public void animate(double value, double duration, Easing easing) {
      this.animationFromValue = this.lastValue;
      this.animationToValue = value;
      this.animationStart = System.currentTimeMillis();
      this.duration = duration;
      this.easing = easing;
   }

   public boolean updateAnimation() {
      double part = (double)(System.currentTimeMillis() - this.animationStart) / this.duration;
      double value;
      if (this.isAlive()) {
         part = this.easing.ease(part);
         value = this.animationFromValue + (this.animationToValue - this.animationFromValue) * part;
      } else {
         this.animationStart = 0L;
         value = this.animationToValue;
      }

      this.lastValue = value;
      return this.isAlive();
   }

   public boolean isDone() {
      double part = (double)(System.currentTimeMillis() - this.animationStart) / this.duration;
      return part >= 1.0;
   }

   public boolean isAlive() {
      double part = (double)(System.currentTimeMillis() - this.animationStart) / this.duration;
      return part < 1.0;
   }
}
