package net.minecraft.client.renderer.entity;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class RenderArrow extends Render {
   private static final ResourceLocation arrowTextures = new ResourceLocation("textures/entity/arrow.png");

   public RenderArrow(RenderManager renderManagerIn) {
      super(renderManagerIn);
   }

   public void doRender(EntityArrow entity, double x, double y, double z, float entityYaw, float partialTicks) {
      this.bindEntityTexture(entity);
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      GL11.glPushMatrix();
      GL11.glTranslatef((float)x, (float)y, (float)z);
      GL11.glRotatef(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks - 90.0F, 0.0F, 1.0F, 0.0F);
      GL11.glRotatef(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks, 0.0F, 0.0F, 1.0F);
      Tessellator tessellator = Tessellator.getInstance();
      WorldRenderer worldrenderer = tessellator.getWorldRenderer();
      int i = 0;
      float f = 0.0F;
      float f1 = 0.5F;
      float f2 = (float)(0 + i * 10) / 32.0F;
      float f3 = (float)(5 + i * 10) / 32.0F;
      float f4 = 0.0F;
      float f5 = 0.15625F;
      float f6 = (float)(5 + i * 10) / 32.0F;
      float f7 = (float)(10 + i * 10) / 32.0F;
      float f8 = 0.05625F;
      GlStateManager.enableRescaleNormal();
      float f9 = (float)entity.arrowShake - partialTicks;
      if (f9 > 0.0F) {
         float f10 = -MathHelper.sin(f9 * 3.0F) * f9;
         GL11.glRotatef(f10, 0.0F, 0.0F, 1.0F);
      }

      GL11.glRotatef(45.0F, 1.0F, 0.0F, 0.0F);
      GL11.glScalef(f8, f8, f8);
      GL11.glTranslatef(-4.0F, 0.0F, 0.0F);
      GL11.glNormal3f(f8, 0.0F, 0.0F);
      worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
      worldrenderer.pos(-7.0, -2.0, -2.0).tex((double)f4, (double)f6).endVertex();
      worldrenderer.pos(-7.0, -2.0, 2.0).tex((double)f5, (double)f6).endVertex();
      worldrenderer.pos(-7.0, 2.0, 2.0).tex((double)f5, (double)f7).endVertex();
      worldrenderer.pos(-7.0, 2.0, -2.0).tex((double)f4, (double)f7).endVertex();
      tessellator.draw();
      GL11.glNormal3f(-f8, 0.0F, 0.0F);
      worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
      worldrenderer.pos(-7.0, 2.0, -2.0).tex((double)f4, (double)f6).endVertex();
      worldrenderer.pos(-7.0, 2.0, 2.0).tex((double)f5, (double)f6).endVertex();
      worldrenderer.pos(-7.0, -2.0, 2.0).tex((double)f5, (double)f7).endVertex();
      worldrenderer.pos(-7.0, -2.0, -2.0).tex((double)f4, (double)f7).endVertex();
      tessellator.draw();

      for(int j = 0; j < 4; ++j) {
         GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
         GL11.glNormal3f(0.0F, 0.0F, f8);
         worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
         worldrenderer.pos(-8.0, -2.0, 0.0).tex((double)f, (double)f2).endVertex();
         worldrenderer.pos(8.0, -2.0, 0.0).tex((double)f1, (double)f2).endVertex();
         worldrenderer.pos(8.0, 2.0, 0.0).tex((double)f1, (double)f3).endVertex();
         worldrenderer.pos(-8.0, 2.0, 0.0).tex((double)f, (double)f3).endVertex();
         tessellator.draw();
      }

      GlStateManager.disableRescaleNormal();
      GL11.glPopMatrix();
      super.doRender(entity, x, y, z, entityYaw, partialTicks);
   }

   protected ResourceLocation getEntityTexture(EntityArrow entity) {
      return arrowTextures;
   }
}
