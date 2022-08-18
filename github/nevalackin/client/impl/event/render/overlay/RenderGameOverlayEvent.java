package io.github.nevalackin.client.impl.event.render.overlay;

import io.github.nevalackin.client.api.event.Event;
import net.minecraft.client.gui.ScaledResolution;

public final class RenderGameOverlayEvent implements Event {
   private final float partialTicks;
   private final ScaledResolution scaledResolution;
   private boolean renderBossHealth = true;
   private boolean renderCrossHair = true;

   public RenderGameOverlayEvent(float partialTicks, ScaledResolution scaledResolution) {
      this.partialTicks = partialTicks;
      this.scaledResolution = scaledResolution;
   }

   public boolean isRenderBossHealth() {
      return this.renderBossHealth;
   }

   public void setRenderBossHealth(boolean renderBossHealth) {
      this.renderBossHealth = renderBossHealth;
   }

   public boolean isRenderCrossHair() {
      return this.renderCrossHair;
   }

   public void setRenderCrossHair(boolean renderCrossHair) {
      this.renderCrossHair = renderCrossHair;
   }

   public float getPartialTicks() {
      return this.partialTicks;
   }

   public ScaledResolution getScaledResolution() {
      return this.scaledResolution;
   }
}
