package net.minecraft.client.renderer.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class RenderFireball extends Render {
   private float scale;

   public RenderFireball(RenderManager renderManagerIn, float scaleIn) {
      super(renderManagerIn);
      this.scale = scaleIn;
   }

   public void doRender(EntityFireball entity, double x, double y, double z, float entityYaw, float partialTicks) {
      GL11.glPushMatrix();
      this.bindEntityTexture(entity);
      GL11.glTranslatef((float)x, (float)y, (float)z);
      GlStateManager.enableRescaleNormal();
      GL11.glScalef(this.scale, this.scale, this.scale);
      TextureAtlasSprite textureatlassprite = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getParticleIcon(Items.fire_charge);
      Tessellator tessellator = Tessellator.getInstance();
      WorldRenderer worldrenderer = tessellator.getWorldRenderer();
      float f = textureatlassprite.getMinU();
      float f1 = textureatlassprite.getMaxU();
      float f2 = textureatlassprite.getMinV();
      float f3 = textureatlassprite.getMaxV();
      float f4 = 1.0F;
      float f5 = 0.5F;
      float f6 = 0.25F;
      GL11.glRotatef(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
      GL11.glRotatef(-this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
      worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);
      worldrenderer.pos(-0.5, -0.25, 0.0).tex((double)f, (double)f3).normal(0.0F, 1.0F, 0.0F).endVertex();
      worldrenderer.pos(0.5, -0.25, 0.0).tex((double)f1, (double)f3).normal(0.0F, 1.0F, 0.0F).endVertex();
      worldrenderer.pos(0.5, 0.75, 0.0).tex((double)f1, (double)f2).normal(0.0F, 1.0F, 0.0F).endVertex();
      worldrenderer.pos(-0.5, 0.75, 0.0).tex((double)f, (double)f2).normal(0.0F, 1.0F, 0.0F).endVertex();
      tessellator.draw();
      GlStateManager.disableRescaleNormal();
      GL11.glPopMatrix();
      super.doRender(entity, x, y, z, entityYaw, partialTicks);
   }

   protected ResourceLocation getEntityTexture(EntityFireball entity) {
      return TextureMap.locationBlocksTexture;
   }
}
