package io.github.nevalackin.client.impl.event.player;

import io.github.nevalackin.client.api.event.Event;

public class PlayerNameUpdateEvent implements Event {
   String oldName;
   String newName;

   public PlayerNameUpdateEvent(String oldName, String newName) {
      this.oldName = oldName;
      this.newName = newName;
   }

   public String getOldName() {
      return this.oldName;
   }

   public String getNewName() {
      return this.newName;
   }
}
