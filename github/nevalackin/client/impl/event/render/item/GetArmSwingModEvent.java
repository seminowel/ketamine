package io.github.nevalackin.client.impl.event.render.item;

import io.github.nevalackin.client.api.event.Event;

public final class GetArmSwingModEvent implements Event {
   private float modifier;

   public GetArmSwingModEvent(float modifier) {
      this.modifier = modifier;
   }

   public float getModifier() {
      return this.modifier;
   }

   public void setModifier(float modifier) {
      this.modifier = modifier;
   }
}
