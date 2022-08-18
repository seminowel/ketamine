package viamcp.gui;

import java.util.Arrays;
import java.util.Collections;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;
import viamcp.ViaMCP;
import viamcp.protocols.ProtocolCollection;

public class VersionSlider extends GuiButton {
   private final ProtocolCollection[] values = ProtocolCollection.values();
   private float sliderValue;
   public boolean dragging;

   public VersionSlider(int buttonId, int x, int y, int widthIn, int heightIn) {
      super(buttonId, x, y, MathHelper.clamp_int(widthIn, 110, Integer.MAX_VALUE), heightIn, "");
      Collections.reverse(Arrays.asList(this.values));
      this.sliderValue = GuiProtocolSelector.sliderDragValue != -1.0F ? GuiProtocolSelector.sliderDragValue : 0.0F;
      this.displayString = GuiProtocolSelector.sliderDragValue != -1.0F ? "Version: " + this.values[(int)(this.sliderValue * (float)(this.values.length - 1))].getVersion().getName() : "Drag for Version";
   }

   protected int getHoverState(boolean mouseOver) {
      return 0;
   }

   protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
      if (this.visible) {
         if (this.dragging) {
            this.sliderValue = (float)(mouseX - (this.xPosition + 4)) / (float)(this.width - 8);
            this.sliderValue = MathHelper.clamp_float(this.sliderValue, 0.0F, 1.0F);
            GuiProtocolSelector.sliderDragValue = this.sliderValue;
            this.displayString = "Version: " + this.values[(int)(this.sliderValue * (float)(this.values.length - 1))].getVersion().getName();
            ViaMCP.getInstance().setVersion(this.values[(int)(this.sliderValue * (float)(this.values.length - 1))].getVersion().getVersion());
         }

         mc.getTextureManager().bindTexture(buttonTextures);
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         drawTexturedModalRect(this.xPosition + (int)(this.sliderValue * (float)(this.width - 8)), this.yPosition, 0, 66, 4, 20);
         drawTexturedModalRect(this.xPosition + (int)(this.sliderValue * (float)(this.width - 8)) + 4, this.yPosition, 196, 66, 4, 20);
      }

   }

   public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
      if (super.mousePressed(mc, mouseX, mouseY)) {
         this.sliderValue = (float)(mouseX - (this.xPosition + 4)) / (float)(this.width - 8);
         this.sliderValue = MathHelper.clamp_float(this.sliderValue, 0.0F, 1.0F);
         GuiProtocolSelector.sliderDragValue = this.sliderValue;
         this.displayString = "Version: " + this.values[(int)(this.sliderValue * (float)(this.values.length - 1))].getVersion().getName();
         ViaMCP.getInstance().setVersion(this.values[(int)(this.sliderValue * (float)(this.values.length - 1))].getVersion().getVersion());
         this.dragging = true;
         return true;
      } else {
         return false;
      }
   }

   public void mouseReleased(int mouseX, int mouseY) {
      this.dragging = false;
   }
}
