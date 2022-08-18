package io.github.nevalackin.client.impl.ui.nl;

import io.github.nevalackin.client.impl.ui.nl.components.RootComponent;
import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;

public final class GuiNLUIScreen extends GuiScreen {
   private final RootComponent rootComponent;
   private static boolean ignoreInput;

   public GuiNLUIScreen() {
      this.mc = Minecraft.getMinecraft();
      this.rootComponent = new RootComponent(new ScaledResolution(this.mc));
   }

   public void onGuiClosed() {
      this.rootComponent.resetAnimationState();
   }

   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      GL11.glDisable(3008);
      this.rootComponent.onDraw(new ScaledResolution(this.mc), mouseX, mouseY);
      GL11.glEnable(3008);
   }

   protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
      this.rootComponent.onMouseClick(mouseX, mouseY, mouseButton);
   }

   public void initGui() {
      ignoreInput = true;
   }

   protected void keyTyped(char typedChar, int keyCode) throws IOException {
      if (ignoreInput) {
         ignoreInput = false;
      } else {
         switch (keyCode) {
            case 1:
            case 54:
            case 210:
               this.mc.displayGuiScreen((GuiScreen)null);
               return;
            default:
               this.rootComponent.onKeyPress(keyCode);
         }
      }
   }

   protected void mouseReleased(int mouseX, int mouseY, int state) {
      super.mouseReleased(mouseX, mouseY, state);
      this.rootComponent.onMouseRelease(state);
   }

   public boolean doesGuiPauseGame() {
      return false;
   }
}
