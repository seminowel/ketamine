package net.minecraft.client.renderer.tileentity;

import java.nio.FloatBuffer;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntityEndPortal;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class TileEntityEndPortalRenderer extends TileEntitySpecialRenderer {
   private static final ResourceLocation END_SKY_TEXTURE = new ResourceLocation("textures/environment/end_sky.png");
   private static final ResourceLocation END_PORTAL_TEXTURE = new ResourceLocation("textures/entity/end_portal.png");
   private static final Random field_147527_e = new Random(31100L);
   FloatBuffer field_147528_b = GLAllocation.createDirectFloatBuffer(16);

   public void renderTileEntityAt(TileEntityEndPortal te, double x, double y, double z, float partialTicks, int destroyStage) {
      float f = (float)this.rendererDispatcher.entityX;
      float f1 = (float)this.rendererDispatcher.entityY;
      float f2 = (float)this.rendererDispatcher.entityZ;
      GL11.glDisable(2896);
      field_147527_e.setSeed(31100L);
      float f3 = 0.75F;

      for(int i = 0; i < 16; ++i) {
         GL11.glPushMatrix();
         float f4 = (float)(16 - i);
         float f5 = 0.0625F;
         float f6 = 1.0F / (f4 + 1.0F);
         if (i == 0) {
            this.bindTexture(END_SKY_TEXTURE);
            f6 = 0.1F;
            f4 = 65.0F;
            f5 = 0.125F;
            GL11.glEnable(3042);
            GL11.glBlendFunc(770, 771);
         }

         if (i >= 1) {
            this.bindTexture(END_PORTAL_TEXTURE);
         }

         if (i == 1) {
            GL11.glEnable(3042);
            GL11.glBlendFunc(1, 1);
            f5 = 0.5F;
         }

         float f7 = (float)(-(y + (double)f3));
         float f8 = f7 + (float)ActiveRenderInfo.getPosition().yCoord;
         float f9 = f7 + f4 + (float)ActiveRenderInfo.getPosition().yCoord;
         float f10 = f8 / f9;
         f10 += (float)(y + (double)f3);
         GL11.glTranslatef(f, f10, f2);
         GlStateManager.texGen(GlStateManager.TexGen.S, 9217);
         GlStateManager.texGen(GlStateManager.TexGen.T, 9217);
         GlStateManager.texGen(GlStateManager.TexGen.R, 9217);
         GlStateManager.texGen(GlStateManager.TexGen.Q, 9216);
         GlStateManager.func_179105_a(GlStateManager.TexGen.S, 9473, this.func_147525_a(1.0F, 0.0F, 0.0F, 0.0F));
         GlStateManager.func_179105_a(GlStateManager.TexGen.T, 9473, this.func_147525_a(0.0F, 0.0F, 1.0F, 0.0F));
         GlStateManager.func_179105_a(GlStateManager.TexGen.R, 9473, this.func_147525_a(0.0F, 0.0F, 0.0F, 1.0F));
         GlStateManager.func_179105_a(GlStateManager.TexGen.Q, 9474, this.func_147525_a(0.0F, 1.0F, 0.0F, 0.0F));
         GlStateManager.enableTexGenCoord(GlStateManager.TexGen.S);
         GlStateManager.enableTexGenCoord(GlStateManager.TexGen.T);
         GlStateManager.enableTexGenCoord(GlStateManager.TexGen.R);
         GlStateManager.enableTexGenCoord(GlStateManager.TexGen.Q);
         GL11.glPopMatrix();
         GL11.glMatrixMode(5890);
         GL11.glPushMatrix();
         GL11.glLoadIdentity();
         GL11.glTranslatef(0.0F, (float)(Minecraft.getSystemTime() % 700000L) / 700000.0F, 0.0F);
         GL11.glScalef(f5, f5, f5);
         GL11.glTranslatef(0.5F, 0.5F, 0.0F);
         GL11.glRotatef((float)(i * i * 4321 + i * 9) * 2.0F, 0.0F, 0.0F, 1.0F);
         GL11.glTranslatef(-0.5F, -0.5F, 0.0F);
         GL11.glTranslatef(-f, -f2, -f1);
         f8 = f7 + (float)ActiveRenderInfo.getPosition().yCoord;
         GL11.glTranslatef((float)ActiveRenderInfo.getPosition().xCoord * f4 / f8, (float)ActiveRenderInfo.getPosition().zCoord * f4 / f8, -f1);
         Tessellator tessellator = Tessellator.getInstance();
         WorldRenderer worldrenderer = tessellator.getWorldRenderer();
         worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
         float f11 = (field_147527_e.nextFloat() * 0.5F + 0.1F) * f6;
         float f12 = (field_147527_e.nextFloat() * 0.5F + 0.4F) * f6;
         float f13 = (field_147527_e.nextFloat() * 0.5F + 0.5F) * f6;
         if (i == 0) {
            f11 = f12 = f13 = 1.0F * f6;
         }

         worldrenderer.pos(x, y + (double)f3, z).color(f11, f12, f13, 1.0F).endVertex();
         worldrenderer.pos(x, y + (double)f3, z + 1.0).color(f11, f12, f13, 1.0F).endVertex();
         worldrenderer.pos(x + 1.0, y + (double)f3, z + 1.0).color(f11, f12, f13, 1.0F).endVertex();
         worldrenderer.pos(x + 1.0, y + (double)f3, z).color(f11, f12, f13, 1.0F).endVertex();
         tessellator.draw();
         GL11.glPopMatrix();
         GL11.glMatrixMode(5888);
         this.bindTexture(END_SKY_TEXTURE);
      }

      GL11.glDisable(3042);
      GlStateManager.disableTexGenCoord(GlStateManager.TexGen.S);
      GlStateManager.disableTexGenCoord(GlStateManager.TexGen.T);
      GlStateManager.disableTexGenCoord(GlStateManager.TexGen.R);
      GlStateManager.disableTexGenCoord(GlStateManager.TexGen.Q);
      GL11.glEnable(2896);
   }

   private FloatBuffer func_147525_a(float p_147525_1_, float p_147525_2_, float p_147525_3_, float p_147525_4_) {
      this.field_147528_b.clear();
      this.field_147528_b.put(p_147525_1_).put(p_147525_2_).put(p_147525_3_).put(p_147525_4_);
      this.field_147528_b.flip();
      return this.field_147528_b;
   }
}