package io.github.nevalackin.client.impl.module.render.world;

import io.github.nevalackin.client.api.module.Category;
import io.github.nevalackin.client.api.module.Module;
import io.github.nevalackin.client.api.property.Property;
import io.github.nevalackin.client.impl.property.EnumProperty;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;

public final class WorldTime extends Module {
   private final EnumProperty timeProperty;
   @EventLink
   private final Listener onUpdateWorldTime;

   public WorldTime() {
      super("World Time", Category.RENDER, Category.SubCategory.RENDER_WORLD);
      this.timeProperty = new EnumProperty("Time", WorldTime.Time.NIGHT);
      this.onUpdateWorldTime = (event) -> {
         event.setTime(((Time)this.timeProperty.getValue()).time);
      };
      this.register(new Property[]{this.timeProperty});
   }

   public void onEnable() {
   }

   public void onDisable() {
   }

   private static enum Time {
      MORNING("Morning", 1000L),
      DAY("Day", 6000L),
      SUNSET("Sunset", 13000L),
      NIGHT("Night", 16000L);

      private final String name;
      private final long time;

      private Time(String name, long time) {
         this.name = name;
         this.time = time;
      }

      public String toString() {
         return this.name;
      }
   }
}
