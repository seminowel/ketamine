package io.github.nevalackin.client.impl.event.render.model;

import io.github.nevalackin.client.api.event.CancellableEvent;
import net.minecraft.entity.EntityLivingBase;

public final class RenderNameEvent extends CancellableEvent {
   private final EntityLivingBase entity;

   public RenderNameEvent(EntityLivingBase entity) {
      this.entity = entity;
   }

   public EntityLivingBase getEntity() {
      return this.entity;
   }
}
