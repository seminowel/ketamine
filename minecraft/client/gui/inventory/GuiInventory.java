package net.minecraft.client.gui.inventory;

import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.achievement.GuiAchievements;
import net.minecraft.client.gui.achievement.GuiStats;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.GL11;

public class GuiInventory extends InventoryEffectRenderer {
   private float oldMouseX;
   private float oldMouseY;

   public GuiInventory(EntityPlayer p_i1094_1_) {
      super(p_i1094_1_.inventoryContainer);
      this.allowUserInput = true;
   }

   public void updateScreen() {
      if (this.mc.playerController.isInCreativeMode()) {
         this.mc.displayGuiScreen(new GuiContainerCreative(this.mc.thePlayer));
      }

      this.updateActivePotionEffects();
   }

   public void initGui() {
      this.buttonList.clear();
      if (this.mc.playerController.isInCreativeMode()) {
         this.mc.displayGuiScreen(new GuiContainerCreative(this.mc.thePlayer));
      } else {
         super.initGui();
      }

   }

   protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
      this.fontRendererObj.drawString(I18n.format("container.crafting"), 86, 16, 4210752);
   }

   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      super.drawScreen(mouseX, mouseY, partialTicks);
      this.oldMouseX = (float)mouseX;
      this.oldMouseY = (float)mouseY;
   }

   protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      this.mc.getTextureManager().bindTexture(inventoryBackground);
      int i = this.guiLeft;
      int j = this.guiTop;
      drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
      drawEntityOnScreen(i + 51, j + 75, 30, (float)(i + 51) - this.oldMouseX, (float)(j + 75 - 50) - this.oldMouseY, this.mc.thePlayer);
   }

   public static void drawEntityOnScreen(int posX, int posY, int scale, float mouseX, float mouseY, EntityLivingBase ent) {
      RendererLivingEntity.isNotRenderInsideGUI = false;
      GL11.glEnable(2903);
      GL11.glPushMatrix();
      GL11.glTranslatef((float)posX, (float)posY, 50.0F);
      GL11.glScalef((float)(-scale), (float)scale, (float)scale);
      GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
      float f = ent.renderYawOffset;
      float f1 = ent.rotationYaw;
      float f2 = ent.rotationPitch;
      float f3 = ent.prevRotationYawHead;
      float f4 = ent.rotationYawHead;
      GL11.glRotatef(135.0F, 0.0F, 1.0F, 0.0F);
      RenderHelper.enableStandardItemLighting();
      GL11.glRotatef(-135.0F, 0.0F, 1.0F, 0.0F);
      GL11.glRotatef(-((float)Math.atan((double)(mouseY / 40.0F))) * 20.0F, 1.0F, 0.0F, 0.0F);
      ent.renderYawOffset = (float)Math.atan((double)(mouseX / 40.0F)) * 20.0F;
      ent.rotationYaw = (float)Math.atan((double)(mouseX / 40.0F)) * 40.0F;
      ent.rotationPitch = -((float)Math.atan((double)(mouseY / 40.0F))) * 20.0F;
      ent.rotationYawHead = ent.rotationYaw;
      ent.prevRotationYawHead = ent.rotationYaw;
      GL11.glTranslatef(0.0F, 0.0F, 0.0F);
      RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
      rendermanager.setPlayerViewY(180.0F);
      rendermanager.setRenderShadow(false);
      rendermanager.renderEntityWithPosYaw(ent, 0.0, 0.0, 0.0, 0.0F, 1.0F);
      rendermanager.setRenderShadow(true);
      ent.renderYawOffset = f;
      ent.rotationYaw = f1;
      ent.rotationPitch = f2;
      ent.prevRotationYawHead = f3;
      ent.rotationYawHead = f4;
      GL11.glPopMatrix();
      RenderHelper.disableStandardItemLighting();
      GlStateManager.disableRescaleNormal();
      GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
      GL11.glDisable(3553);
      GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
      RendererLivingEntity.isNotRenderInsideGUI = true;
   }

   protected void actionPerformed(GuiButton button) throws IOException {
      if (button.id == 0) {
         this.mc.displayGuiScreen(new GuiAchievements(this, this.mc.thePlayer.getStatFileWriter()));
      }

      if (button.id == 1) {
         this.mc.displayGuiScreen(new GuiStats(this, this.mc.thePlayer.getStatFileWriter()));
      }

   }
}
