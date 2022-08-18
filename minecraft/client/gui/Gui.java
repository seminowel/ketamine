package net.minecraft.client.gui;

import io.github.nevalackin.client.util.render.DrawUtil;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class Gui {
   protected float zLevel;
   public static final ResourceLocation optionsBackground = new ResourceLocation("textures/gui/options_background.png");
   public static final ResourceLocation statIcons = new ResourceLocation("textures/gui/container/stats_icons.png");
   public static final ResourceLocation icons = new ResourceLocation("textures/gui/icons.png");

   public static void drawModalRectWithCustomSizedTexture(int x, int y, float u, float v, int width, int height, float textureWidth, float textureHeight) {
      float f = 1.0F / textureWidth;
      float f1 = 1.0F / textureHeight;
      Tessellator tessellator = Tessellator.getInstance();
      WorldRenderer worldrenderer = tessellator.getWorldRenderer();
      worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
      worldrenderer.pos((double)x, (double)(y + height), 0.0).tex((double)(u * f), (double)((v + (float)height) * f1)).endVertex();
      worldrenderer.pos((double)(x + width), (double)(y + height), 0.0).tex((double)((u + (float)width) * f), (double)((v + (float)height) * f1)).endVertex();
      worldrenderer.pos((double)(x + width), (double)y, 0.0).tex((double)((u + (float)width) * f), (double)(v * f1)).endVertex();
      worldrenderer.pos((double)x, (double)y, 0.0).tex((double)(u * f), (double)(v * f1)).endVertex();
      tessellator.draw();
   }

   public static void drawScaledCustomSizeModalRect(double x, double y, float u, float v, int uWidth, int vHeight, double width, double height, float tileWidth, float tileHeight) {
      float f = 1.0F / tileWidth;
      float f1 = 1.0F / tileHeight;
      Tessellator tessellator = Tessellator.getInstance();
      WorldRenderer worldrenderer = tessellator.getWorldRenderer();
      worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
      worldrenderer.pos(x, y + height, 0.0).tex((double)(u * f), (double)((v + (float)vHeight) * f1)).endVertex();
      worldrenderer.pos(x + width, y + height, 0.0).tex((double)((u + (float)uWidth) * f), (double)((v + (float)vHeight) * f1)).endVertex();
      worldrenderer.pos(x + width, y, 0.0).tex((double)((u + (float)uWidth) * f), (double)(v * f1)).endVertex();
      worldrenderer.pos(x, y, 0.0).tex((double)(u * f), (double)(v * f1)).endVertex();
      tessellator.draw();
   }

   protected void drawHorizontalLine(int startX, int endX, int y, int color) {
      if (endX < startX) {
         int i = startX;
         startX = endX;
         endX = i;
      }

      DrawUtil.glDrawLine((double)startX, (double)y, (double)(endX + 1), (double)(y + 1), 1.0F, false, color);
   }

   protected void drawVerticalLine(int x, int startY, int endY, int color) {
      if (endY < startY) {
         int i = startY;
         startY = endY;
         endY = i;
      }

      DrawUtil.glDrawLine((double)x, (double)(startY + 1), (double)(x + 1), (double)endY, 1.0F, false, color);
   }

   public void drawCenteredString(FontRenderer fontRendererIn, String text, int x, int y, int color) {
      fontRendererIn.drawStringWithShadow(text, (float)(x - fontRendererIn.getStringWidth(text) / 2), (float)y, color);
   }

   public void drawString(FontRenderer fontRendererIn, String text, int x, int y, int color) {
      fontRendererIn.drawStringWithShadow(text, (float)x, (float)y, color);
   }

   public static void drawTexturedModalRect(int x, int y, int textureX, int textureY, int width, int height) {
      float f = 0.00390625F;
      GL11.glTranslated((double)x, (double)y, 0.0);
      GL11.glBegin(7);
      GL11.glTexCoord2f((float)textureX * 0.00390625F, (float)(textureY + height) * 0.00390625F);
      GL11.glVertex2f(0.0F, (float)height);
      GL11.glTexCoord2f((float)(textureX + width) * 0.00390625F, (float)(textureY + height) * 0.00390625F);
      GL11.glVertex2f((float)width, (float)height);
      GL11.glTexCoord2f((float)(textureX + width) * 0.00390625F, (float)textureY * 0.00390625F);
      GL11.glVertex2f((float)width, 0.0F);
      GL11.glTexCoord2f((float)textureX * 0.00390625F, (float)textureY * 0.00390625F);
      GL11.glVertex2f(0.0F, 0.0F);
      GL11.glEnd();
      GL11.glTranslated((double)(-x), (double)(-y), 0.0);
   }

   public void drawTexturedModalRect(float xCoord, float yCoord, int minU, int minV, int maxU, int maxV) {
      float f = 0.00390625F;
      float f1 = 0.00390625F;
      Tessellator tessellator = Tessellator.getInstance();
      WorldRenderer worldrenderer = tessellator.getWorldRenderer();
      worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
      worldrenderer.pos((double)(xCoord + 0.0F), (double)(yCoord + (float)maxV), (double)this.zLevel).tex((double)((float)(minU + 0) * f), (double)((float)(minV + maxV) * f1)).endVertex();
      worldrenderer.pos((double)(xCoord + (float)maxU), (double)(yCoord + (float)maxV), (double)this.zLevel).tex((double)((float)(minU + maxU) * f), (double)((float)(minV + maxV) * f1)).endVertex();
      worldrenderer.pos((double)(xCoord + (float)maxU), (double)(yCoord + 0.0F), (double)this.zLevel).tex((double)((float)(minU + maxU) * f), (double)((float)(minV + 0) * f1)).endVertex();
      worldrenderer.pos((double)(xCoord + 0.0F), (double)(yCoord + 0.0F), (double)this.zLevel).tex((double)((float)(minU + 0) * f), (double)((float)(minV + 0) * f1)).endVertex();
      tessellator.draw();
   }

   public void drawTexturedModalRect(int xCoord, int yCoord, TextureAtlasSprite textureSprite, int widthIn, int heightIn) {
      Tessellator tessellator = Tessellator.getInstance();
      WorldRenderer worldrenderer = tessellator.getWorldRenderer();
      worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
      worldrenderer.pos((double)(xCoord + 0), (double)(yCoord + heightIn), (double)this.zLevel).tex((double)textureSprite.getMinU(), (double)textureSprite.getMaxV()).endVertex();
      worldrenderer.pos((double)(xCoord + widthIn), (double)(yCoord + heightIn), (double)this.zLevel).tex((double)textureSprite.getMaxU(), (double)textureSprite.getMaxV()).endVertex();
      worldrenderer.pos((double)(xCoord + widthIn), (double)(yCoord + 0), (double)this.zLevel).tex((double)textureSprite.getMaxU(), (double)textureSprite.getMinV()).endVertex();
      worldrenderer.pos((double)(xCoord + 0), (double)(yCoord + 0), (double)this.zLevel).tex((double)textureSprite.getMinU(), (double)textureSprite.getMinV()).endVertex();
      tessellator.draw();
   }
}
