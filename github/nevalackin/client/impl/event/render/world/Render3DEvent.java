package io.github.nevalackin.client.impl.event.render.world;

import io.github.nevalackin.client.api.event.Event;
import net.minecraft.client.gui.ScaledResolution;

public final class Render3DEvent implements Event {
   private final float partialTicks;
   private final ScaledResolution scaledResolution;

   public Render3DEvent(float partialTicks, ScaledResolution scaledResolution) {
      this.partialTicks = partialTicks;
      this.scaledResolution = scaledResolution;
   }

   public ScaledResolution getScaledResolution() {
      return this.scaledResolution;
   }

   public float getPartialTicks() {
      return this.partialTicks;
   }
}
