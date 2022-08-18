package io.github.nevalackin.client.impl.event.render.game;

import io.github.nevalackin.client.api.event.Event;

public final class UpdateFramebufferSizeEvent implements Event {
   private final int width;
   private final int height;

   public UpdateFramebufferSizeEvent(int width, int height) {
      this.width = width;
      this.height = height;
   }

   public int getWidth() {
      return this.width;
   }

   public int getHeight() {
      return this.height;
   }
}
