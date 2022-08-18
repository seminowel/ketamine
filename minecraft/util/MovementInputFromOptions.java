package net.minecraft.util;

import io.github.nevalackin.client.impl.core.KetamineClient;
import io.github.nevalackin.client.impl.module.misc.inventory.Inventory;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.settings.GameSettings;
import org.lwjgl.input.Keyboard;

public class MovementInputFromOptions extends MovementInput {
   private final GameSettings gameSettings;

   public MovementInputFromOptions(GameSettings gameSettingsIn) {
      this.gameSettings = gameSettingsIn;
   }

   public void updatePlayerMoveState() {
      this.moveStrafe = 0.0F;
      this.moveForward = 0.0F;
      Predicate isPressed = (keyBinding) -> {
         return ((Inventory)KetamineClient.getInstance().getModuleManager().getModule(Inventory.class)).isEnabled() ? Keyboard.isKeyDown(keyBinding.getKeyCode()) && !(Minecraft.getMinecraft().currentScreen instanceof GuiChat) : keyBinding.isKeyDown();
      };
      if (isPressed.test(this.gameSettings.keyBindForward)) {
         ++this.moveForward;
      }

      if (isPressed.test(this.gameSettings.keyBindBack)) {
         --this.moveForward;
      }

      if (isPressed.test(this.gameSettings.keyBindLeft)) {
         ++this.moveStrafe;
      }

      if (isPressed.test(this.gameSettings.keyBindRight)) {
         --this.moveStrafe;
      }

      this.jump = isPressed.test(this.gameSettings.keyBindJump);
      this.sneak = this.gameSettings.keyBindSneak.isKeyDown();
      if (this.sneak) {
         this.moveStrafe = (float)((double)this.moveStrafe * 0.3);
         this.moveForward = (float)((double)this.moveForward * 0.3);
      }

   }
}
