package net.minecraft.client;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.MinecraftError;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

public class LoadingScreenRenderer implements IProgressUpdate {
   private String message = "";
   private Minecraft mc;
   private String currentlyDisplayedText = "";
   private long systemTime = Minecraft.getSystemTime();
   private boolean field_73724_e;
   private ScaledResolution scaledResolution;
   private Framebuffer framebuffer;

   public LoadingScreenRenderer(Minecraft mcIn) {
      this.mc = mcIn;
      this.scaledResolution = new ScaledResolution(mcIn);
      this.framebuffer = new Framebuffer(mcIn.displayWidth, mcIn.displayHeight, false);
      this.framebuffer.setFramebufferFilter(9728);
   }

   public void resetProgressAndMessage(String message) {
      this.field_73724_e = false;
      this.displayString(message);
   }

   public void displaySavingString(String message) {
      this.field_73724_e = true;
      this.displayString(message);
   }

   private void displayString(String message) {
      this.currentlyDisplayedText = message;
      if (!this.mc.running) {
         if (!this.field_73724_e) {
            throw new MinecraftError();
         }
      } else {
         GL11.glClear(256);
         GL11.glMatrixMode(5889);
         GL11.glLoadIdentity();
         if (OpenGlHelper.isFramebufferEnabled()) {
            int i = this.scaledResolution.getScaleFactor();
            GL11.glOrtho(0.0, (double)(this.scaledResolution.getScaledWidth() * i), (double)(this.scaledResolution.getScaledHeight() * i), 0.0, 100.0, 300.0);
         } else {
            ScaledResolution scaledresolution = new ScaledResolution(this.mc);
            GL11.glOrtho(0.0, scaledresolution.getScaledWidth_double(), scaledresolution.getScaledHeight_double(), 0.0, 100.0, 300.0);
         }

         GL11.glMatrixMode(5888);
         GL11.glLoadIdentity();
         GL11.glTranslatef(0.0F, 0.0F, -200.0F);
      }

   }

   public void displayLoadingString(String message) {
      if (!this.mc.running) {
         if (!this.field_73724_e) {
            throw new MinecraftError();
         }
      } else {
         this.systemTime = 0L;
         this.message = message;
         this.setLoadingProgress(-1);
         this.systemTime = 0L;
      }

   }

   public void setLoadingProgress(int progress) {
      if (!this.mc.running) {
         if (!this.field_73724_e) {
            throw new MinecraftError();
         }
      } else {
         long i = Minecraft.getSystemTime();
         if (i - this.systemTime >= 100L) {
            this.systemTime = i;
            ScaledResolution scaledresolution = new ScaledResolution(this.mc);
            int j = scaledresolution.getScaleFactor();
            int k = scaledresolution.getScaledWidth();
            int l = scaledresolution.getScaledHeight();
            if (OpenGlHelper.isFramebufferEnabled()) {
               this.framebuffer.framebufferClear();
            } else {
               GL11.glClear(256);
            }

            this.framebuffer.bindFramebuffer(false);
            GL11.glMatrixMode(5889);
            GL11.glLoadIdentity();
            GL11.glOrtho(0.0, scaledresolution.getScaledWidth_double(), scaledresolution.getScaledHeight_double(), 0.0, 100.0, 300.0);
            GL11.glMatrixMode(5888);
            GL11.glLoadIdentity();
            GL11.glTranslatef(0.0F, 0.0F, -200.0F);
            if (!OpenGlHelper.isFramebufferEnabled()) {
               GL11.glClear(16640);
            }

            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();
            this.mc.getTextureManager().bindTexture(Gui.optionsBackground);
            float f = 32.0F;
            worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            worldrenderer.pos(0.0, (double)l, 0.0).tex(0.0, (double)((float)l / f)).color(64, 64, 64, 255).endVertex();
            worldrenderer.pos((double)k, (double)l, 0.0).tex((double)((float)k / f), (double)((float)l / f)).color(64, 64, 64, 255).endVertex();
            worldrenderer.pos((double)k, 0.0, 0.0).tex((double)((float)k / f), 0.0).color(64, 64, 64, 255).endVertex();
            worldrenderer.pos(0.0, 0.0, 0.0).tex(0.0, 0.0).color(64, 64, 64, 255).endVertex();
            tessellator.draw();
            if (progress >= 0) {
               int i1 = 100;
               int j1 = 2;
               int k1 = k / 2 - i1 / 2;
               int l1 = l / 2 + 16;
               GL11.glDisable(3553);
               worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
               worldrenderer.pos((double)k1, (double)l1, 0.0).color(128, 128, 128, 255).endVertex();
               worldrenderer.pos((double)k1, (double)(l1 + j1), 0.0).color(128, 128, 128, 255).endVertex();
               worldrenderer.pos((double)(k1 + i1), (double)(l1 + j1), 0.0).color(128, 128, 128, 255).endVertex();
               worldrenderer.pos((double)(k1 + i1), (double)l1, 0.0).color(128, 128, 128, 255).endVertex();
               worldrenderer.pos((double)k1, (double)l1, 0.0).color(128, 255, 128, 255).endVertex();
               worldrenderer.pos((double)k1, (double)(l1 + j1), 0.0).color(128, 255, 128, 255).endVertex();
               worldrenderer.pos((double)(k1 + progress), (double)(l1 + j1), 0.0).color(128, 255, 128, 255).endVertex();
               worldrenderer.pos((double)(k1 + progress), (double)l1, 0.0).color(128, 255, 128, 255).endVertex();
               tessellator.draw();
               GL11.glEnable(3553);
            }

            GL11.glEnable(3042);
            GL14.glBlendFuncSeparate(770, 771, 1, 0);
            this.mc.fontRendererObj.drawStringWithShadow(this.currentlyDisplayedText, (float)((k - this.mc.fontRendererObj.getStringWidth(this.currentlyDisplayedText)) / 2), (float)(l / 2 - 4 - 16), 16777215);
            this.mc.fontRendererObj.drawStringWithShadow(this.message, (float)((k - this.mc.fontRendererObj.getStringWidth(this.message)) / 2), (float)(l / 2 - 4 + 8), 16777215);
            this.framebuffer.unbindFramebuffer();
            if (OpenGlHelper.isFramebufferEnabled()) {
               this.framebuffer.framebufferRender(k * j, l * j);
            }

            this.mc.updateDisplay();

            try {
               Thread.yield();
            } catch (Exception var15) {
            }
         }
      }

   }

   public void setDoneWorking() {
   }
}
