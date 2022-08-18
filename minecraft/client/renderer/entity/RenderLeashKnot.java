package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ModelLeashKnot;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLeashKnot;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class RenderLeashKnot extends Render {
   private static final ResourceLocation leashKnotTextures = new ResourceLocation("textures/entity/lead_knot.png");
   private ModelLeashKnot leashKnotModel = new ModelLeashKnot();

   public RenderLeashKnot(RenderManager renderManagerIn) {
      super(renderManagerIn);
   }

   public void doRender(EntityLeashKnot entity, double x, double y, double z, float entityYaw, float partialTicks) {
      GL11.glPushMatrix();
      GL11.glDisable(2884);
      GL11.glTranslatef((float)x, (float)y, (float)z);
      float f = 0.0625F;
      GlStateManager.enableRescaleNormal();
      GL11.glScalef(-1.0F, -1.0F, 1.0F);
      GlStateManager.enableAlpha();
      this.bindEntityTexture(entity);
      this.leashKnotModel.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, f);
      GL11.glPopMatrix();
      super.doRender(entity, x, y, z, entityYaw, partialTicks);
   }

   protected ResourceLocation getEntityTexture(EntityLeashKnot entity) {
      return leashKnotTextures;
   }
}
