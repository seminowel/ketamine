package io.github.nevalackin.client.impl.event.render.overlay;

import io.github.nevalackin.client.api.event.Event;
import io.github.nevalackin.client.impl.event.render.RenderCallback;

public final class RenderHandEvent implements Event {
   private RenderCallback preRenderCallback;
   private RenderCallback postRenderCallback;
   private boolean left;
   private boolean bindTexture = true;

   public void onPreRender() {
      if (this.preRenderCallback != null) {
         this.preRenderCallback.render();
      }

   }

   public void onPostRender() {
      if (this.postRenderCallback != null) {
         this.postRenderCallback.render();
      }

   }

   public void setPreRenderCallback(RenderCallback preRenderCallback) {
      this.preRenderCallback = preRenderCallback;
   }

   public void setPostRenderCallback(RenderCallback postRenderCallback) {
      this.postRenderCallback = postRenderCallback;
   }

   public boolean isBindTexture() {
      return this.bindTexture;
   }

   public void setBindTexture(boolean bindTexture) {
      this.bindTexture = bindTexture;
   }

   public boolean isLeft() {
      return this.left;
   }

   public void setLeft(boolean left) {
      this.left = left;
   }
}
