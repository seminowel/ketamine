package io.github.nevalackin.client.impl.event.render.model;

import io.github.nevalackin.client.api.event.Event;
import net.minecraft.entity.EntityLivingBase;

public final class ApplyHurtEffectEvent implements Event {
   private RenderCallbackFunc preRenderModelCallback;
   private RenderCallbackFunc preRenderLayersCallback;
   private int hurtColour;
   private final EntityLivingBase entity;

   public ApplyHurtEffectEvent(RenderCallbackFunc preRenderModelCallback, RenderCallbackFunc preRenderLayersCallback, int hurtColour, EntityLivingBase entity) {
      this.preRenderLayersCallback = preRenderLayersCallback;
      this.preRenderModelCallback = preRenderModelCallback;
      this.hurtColour = hurtColour;
      this.entity = entity;
   }

   public EntityLivingBase getEntity() {
      return this.entity;
   }

   public int getHurtColour() {
      return this.hurtColour;
   }

   public void setHurtColour(int hurtColour) {
      this.hurtColour = hurtColour;
   }

   public RenderCallbackFunc getPreRenderModelCallback() {
      return this.preRenderModelCallback;
   }

   public void setPreRenderModelCallback(RenderCallbackFunc preRenderModelCallback) {
      this.preRenderModelCallback = preRenderModelCallback;
   }

   public RenderCallbackFunc getPreRenderLayersCallback() {
      return this.preRenderLayersCallback;
   }

   public void setPreRenderLayersCallback(RenderCallbackFunc preRenderLayersCallback) {
      this.preRenderLayersCallback = preRenderLayersCallback;
   }

   public static enum RenderCallbackFunc {
      SET,
      UNSET,
      NONE;
   }
}
