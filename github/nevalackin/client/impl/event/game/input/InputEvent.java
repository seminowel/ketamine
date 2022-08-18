package io.github.nevalackin.client.impl.event.game.input;

import io.github.nevalackin.client.api.event.Event;

public final class InputEvent implements Event {
   private final InputType type;
   private final int button;
   private final State state;

   public InputEvent(InputType type, int button, State state) {
      this.type = type;
      this.button = button;
      this.state = state;
   }

   public InputType getType() {
      return this.type;
   }

   public int getButton() {
      return this.button;
   }

   public State getState() {
      return this.state;
   }
}
