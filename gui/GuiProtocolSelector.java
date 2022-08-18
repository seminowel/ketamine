package viamcp.gui;

import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.opengl.GL11;
import viamcp.ViaMCP;
import viamcp.protocols.ProtocolCollection;

public class GuiProtocolSelector extends GuiScreen {
   public static float sliderDragValue = 0.075F;
   private GuiScreen parent;
   public SlotList list;

   public GuiProtocolSelector(GuiScreen parent) {
      this.parent = parent;
   }

   public void initGui() {
      super.initGui();
      this.buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height - 25, 200, 20, "Back"));
      this.buttonList.add(new GuiButton(2, this.width / 2 - 180, this.height - 25, 75, 20, "Credits"));
      this.list = new SlotList(this.mc, this.width, this.height, 32, this.height - 32, 10);
   }

   protected void actionPerformed(GuiButton guiButton) throws IOException {
      this.list.actionPerformed(guiButton);
      if (guiButton.id == 1) {
         this.mc.displayGuiScreen(this.parent);
      }

      if (guiButton.id == 2) {
         this.mc.displayGuiScreen(new GuiCredits(this));
      }

   }

   public void handleMouseInput() throws IOException {
      this.list.handleMouseInput();
      super.handleMouseInput();
   }

   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      this.list.drawScreen(mouseX, mouseY, partialTicks);
      GL11.glPushMatrix();
      GL11.glScaled(2.0, 2.0, 2.0);
      String title = EnumChatFormatting.BOLD + "ViaMCP Reborn";
      this.drawString(this.fontRendererObj, title, (this.width - this.fontRendererObj.getStringWidth(title) * 2) / 4, 5, -1);
      GL11.glPopMatrix();
      String versionName = ProtocolCollection.getProtocolById(ViaMCP.getInstance().getVersion()).getName();
      String versionCodeName = ProtocolCollection.getProtocolInfoById(ViaMCP.getInstance().getVersion()).getName();
      String versionReleaseDate = ProtocolCollection.getProtocolInfoById(ViaMCP.getInstance().getVersion()).getReleaseDate();
      String versionTitle = "Version: " + versionName + " - " + versionCodeName;
      String versionReleased = "Released: " + versionReleaseDate;
      int fixedHeight = (5 + this.fontRendererObj.FONT_HEIGHT) * 2 + 2;
      this.drawString(this.fontRendererObj, "" + EnumChatFormatting.GRAY + EnumChatFormatting.BOLD + "Version Information", (this.width - this.fontRendererObj.getStringWidth("Version Information")) / 2, fixedHeight, -1);
      this.drawString(this.fontRendererObj, versionTitle, (this.width - this.fontRendererObj.getStringWidth(versionTitle)) / 2, fixedHeight + this.fontRendererObj.FONT_HEIGHT, -1);
      this.drawString(this.fontRendererObj, versionReleased, (this.width - this.fontRendererObj.getStringWidth(versionReleased)) / 2, fixedHeight + this.fontRendererObj.FONT_HEIGHT * 2, -1);
      super.drawScreen(mouseX, mouseY, partialTicks);
   }

   class SlotList extends GuiSlot {
      public SlotList(Minecraft mc, int width, int height, int top, int bottom, int slotHeight) {
         super(mc, width, height, top + 30, bottom, 18);
      }

      protected int getSize() {
         return ProtocolCollection.values().length;
      }

      protected void elementClicked(int i, boolean b, int i1, int i2) {
         int protocolVersion = ProtocolCollection.values()[i].getVersion().getVersion();
         ViaMCP.getInstance().setVersion(protocolVersion);
         ViaMCP.getInstance().asyncSlider.setVersion(protocolVersion);
      }

      protected boolean isSelected(int i) {
         return false;
      }

      protected void drawBackground() {
         GuiProtocolSelector.this.drawDefaultBackground();
      }

      protected void drawSlot(int i, int i1, int i2, int i3, int i4, int i5) {
         GuiProtocolSelector.this.drawCenteredString(this.mc.fontRendererObj, (ViaMCP.getInstance().getVersion() == ProtocolCollection.values()[i].getVersion().getVersion() ? EnumChatFormatting.GREEN.toString() + EnumChatFormatting.BOLD : EnumChatFormatting.GRAY.toString()) + ProtocolCollection.getProtocolById(ProtocolCollection.values()[i].getVersion().getVersion()).getName(), this.width / 2, i2 + 2, -1);
         GL11.glPushMatrix();
         GL11.glScaled(0.5, 0.5, 0.5);
         GuiProtocolSelector.this.drawCenteredString(this.mc.fontRendererObj, "PVN: " + ProtocolCollection.getProtocolById(ProtocolCollection.values()[i].getVersion().getVersion()).getVersion(), this.width, (i2 + 2) * 2 + 20, -1);
         GL11.glPopMatrix();
      }
   }
}
