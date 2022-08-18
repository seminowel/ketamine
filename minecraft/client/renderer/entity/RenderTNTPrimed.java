package net.minecraft.client.renderer.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class RenderTNTPrimed extends Render {
   public RenderTNTPrimed(RenderManager renderManagerIn) {
      super(renderManagerIn);
      this.shadowSize = 0.5F;
   }

   public void doRender(EntityTNTPrimed entity, double x, double y, double z, float entityYaw, float partialTicks) {
      BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
      GL11.glPushMatrix();
      GL11.glTranslatef((float)x, (float)y + 0.5F, (float)z);
      float f2;
      if ((float)entity.fuse - partialTicks + 1.0F < 10.0F) {
         f2 = 1.0F - ((float)entity.fuse - partialTicks + 1.0F) / 10.0F;
         f2 = MathHelper.clamp_float(f2, 0.0F, 1.0F);
         f2 *= f2;
         f2 *= f2;
         float f1 = 1.0F + f2 * 0.3F;
         GL11.glScalef(f1, f1, f1);
      }

      f2 = (1.0F - ((float)entity.fuse - partialTicks + 1.0F) / 100.0F) * 0.8F;
      this.bindEntityTexture(entity);
      GL11.glTranslatef(-0.5F, -0.5F, 0.5F);
      blockrendererdispatcher.renderBlockBrightness(Blocks.tnt.getDefaultState(), entity.getBrightness(partialTicks));
      GL11.glTranslatef(0.0F, 0.0F, 1.0F);
      if (entity.fuse / 5 % 2 == 0) {
         GL11.glDisable(3553);
         GL11.glDisable(2896);
         GL11.glEnable(3042);
         GL11.glBlendFunc(770, 772);
         GL11.glColor4f(1.0F, 1.0F, 1.0F, f2);
         GlStateManager.doPolygonOffset(-3.0F, -3.0F);
         GlStateManager.enablePolygonOffset();
         blockrendererdispatcher.renderBlockBrightness(Blocks.tnt.getDefaultState(), 1.0F);
         GlStateManager.doPolygonOffset(0.0F, 0.0F);
         GlStateManager.disablePolygonOffset();
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         GL11.glDisable(3042);
         GL11.glEnable(2896);
         GL11.glEnable(3553);
      }

      GL11.glPopMatrix();
      super.doRender(entity, x, y, z, entityYaw, partialTicks);
   }

   protected ResourceLocation getEntityTexture(EntityTNTPrimed entity) {
      return TextureMap.locationBlocksTexture;
   }
}
