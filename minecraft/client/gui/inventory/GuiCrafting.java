package net.minecraft.client.gui.inventory;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

public class GuiCrafting extends GuiContainer {
   private static final ResourceLocation craftingTableGuiTextures = new ResourceLocation("textures/gui/container/crafting_table.png");

   public GuiCrafting(InventoryPlayer playerInv, World worldIn) {
      this(playerInv, worldIn, BlockPos.ORIGIN);
   }

   public GuiCrafting(InventoryPlayer playerInv, World worldIn, BlockPos blockPosition) {
      super(new ContainerWorkbench(playerInv, worldIn, blockPosition));
   }

   protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
      this.fontRendererObj.drawString(I18n.format("container.crafting"), 28, 6, 4210752);
      this.fontRendererObj.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
   }

   protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      this.mc.getTextureManager().bindTexture(craftingTableGuiTextures);
      int i = (this.width - this.xSize) / 2;
      int j = (this.height - this.ySize) / 2;
      drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
   }
}
