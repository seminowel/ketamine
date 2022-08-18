package io.github.nevalackin.client.impl.event.render.world;

import io.github.nevalackin.client.api.event.Event;

public final class DrawSelectedBoundingBoxEvent implements Event {
   private boolean filled = false;
   private float outlineWidth = 1.0F;
   private int colour = 1711276032;

   public boolean isFilled() {
      return this.filled;
   }

   public void setFilled(boolean filled) {
      this.filled = filled;
   }

   public float getOutlineWidth() {
      return this.outlineWidth;
   }

   public void setOutlineWidth(float outlineWidth) {
      this.outlineWidth = outlineWidth;
   }

   public int getColour() {
      return this.colour;
   }

   public void setColour(int colour) {
      this.colour = colour;
   }
}
