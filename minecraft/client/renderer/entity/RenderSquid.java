package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class RenderSquid extends RenderLiving {
   private static final ResourceLocation squidTextures = new ResourceLocation("textures/entity/squid.png");

   public RenderSquid(RenderManager renderManagerIn, ModelBase modelBaseIn, float shadowSizeIn) {
      super(renderManagerIn, modelBaseIn, shadowSizeIn);
   }

   protected ResourceLocation getEntityTexture(EntitySquid entity) {
      return squidTextures;
   }

   protected void rotateCorpse(EntitySquid bat, float p_77043_2_, float p_77043_3_, float partialTicks) {
      float f = bat.prevSquidPitch + (bat.squidPitch - bat.prevSquidPitch) * partialTicks;
      float f1 = bat.prevSquidYaw + (bat.squidYaw - bat.prevSquidYaw) * partialTicks;
      GL11.glTranslatef(0.0F, 0.5F, 0.0F);
      GL11.glRotatef(180.0F - p_77043_3_, 0.0F, 1.0F, 0.0F);
      GL11.glRotatef(f, 1.0F, 0.0F, 0.0F);
      GL11.glRotatef(f1, 0.0F, 1.0F, 0.0F);
      GL11.glTranslatef(0.0F, -1.2F, 0.0F);
   }

   protected float handleRotationFloat(EntitySquid livingBase, float partialTicks) {
      return livingBase.lastTentacleAngle + (livingBase.tentacleAngle - livingBase.lastTentacleAngle) * partialTicks;
   }
}
