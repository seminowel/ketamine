package io.github.nevalackin.client.impl.event.player;

import io.github.nevalackin.client.api.event.Event;

public final class WindowClickEvent implements Event {
   private final int windowId;
   private final int slot;
   private final int hotBarSlot;
   private final int mode;

   public WindowClickEvent(int windowId, int slot, int hotBarSlot, int mode) {
      this.windowId = windowId;
      this.slot = slot;
      this.hotBarSlot = hotBarSlot;
      this.mode = mode;
   }

   public int getWindowId() {
      return this.windowId;
   }

   public int getSlot() {
      return this.slot;
   }

   public int getHotBarSlot() {
      return this.hotBarSlot;
   }

   public int getMode() {
      return this.mode;
   }
}
