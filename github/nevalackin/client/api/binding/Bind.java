package io.github.nevalackin.client.api.binding;

import io.github.nevalackin.client.impl.event.game.input.InputType;
import java.util.Objects;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class Bind {
   private final InputType inputType;
   private final int code;
   private final BindType bindType;

   public Bind(InputType inputType, int code, BindType bindType) {
      this.inputType = inputType;
      this.code = code;
      this.bindType = bindType;
   }

   public InputType getInputType() {
      return this.inputType;
   }

   public BindType getBindType() {
      return this.bindType;
   }

   public int getCode() {
      return this.code;
   }

   public String getCodeName() {
      switch (this.inputType) {
         case KEYBOARD:
            return Keyboard.getKeyName(this.code);
         case MOUSE:
            return Mouse.getButtonName(this.code);
         default:
            return null;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.inputType, this.code, this.bindType});
   }
}
