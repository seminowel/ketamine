package io.github.nevalackin.client.impl.event.render.overlay;

import io.github.nevalackin.client.api.event.Event;
import net.minecraft.client.gui.ScaledResolution;

public final class DrawHUDEditorEvent implements Event {
   private final float partialTicks;
   private final ScaledResolution scaledResolution;

   public DrawHUDEditorEvent(float partialTicks, ScaledResolution scaledResolution) {
      this.partialTicks = partialTicks;
      this.scaledResolution = scaledResolution;
   }

   public float getPartialTicks() {
      return this.partialTicks;
   }

   public ScaledResolution getScaledResolution() {
      return this.scaledResolution;
   }
}
