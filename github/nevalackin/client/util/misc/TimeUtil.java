package io.github.nevalackin.client.util.misc;

public final class TimeUtil {
   private long lastMS = this.getCurrentMS();
   private long time;

   public boolean reach(long time) {
      return this.time() >= time;
   }

   public long time() {
      return System.nanoTime() / 1000000L - this.time;
   }

   public long getCurrentMS() {
      return System.nanoTime() / 1000000L;
   }

   public boolean passed(long millisecond) {
      return this.getCurrentMS() - this.lastMS >= millisecond;
   }

   public boolean passed(double millisecond) {
      return (double)(this.getCurrentMS() - this.lastMS) >= millisecond;
   }

   public void reset() {
      this.lastMS = this.getCurrentMS();
   }

   public boolean hasTimeElapsed(long time, boolean reset) {
      if (this.lastMS > System.currentTimeMillis()) {
         this.lastMS = System.currentTimeMillis();
      }

      if (System.currentTimeMillis() - this.lastMS > time) {
         if (reset) {
            this.reset();
         }

         return true;
      } else {
         return false;
      }
   }

   public boolean randDelay(double min, double max) {
      double random = Math.random() * max + min;
      return (double)(this.getCurrentMS() - this.lastMS) >= random;
   }
}
