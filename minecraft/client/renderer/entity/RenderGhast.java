package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ModelGhast;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class RenderGhast extends RenderLiving {
   private static final ResourceLocation ghastTextures = new ResourceLocation("textures/entity/ghast/ghast.png");
   private static final ResourceLocation ghastShootingTextures = new ResourceLocation("textures/entity/ghast/ghast_shooting.png");

   public RenderGhast(RenderManager renderManagerIn) {
      super(renderManagerIn, new ModelGhast(), 0.5F);
   }

   protected ResourceLocation getEntityTexture(EntityGhast entity) {
      return entity.isAttacking() ? ghastShootingTextures : ghastTextures;
   }

   protected void preRenderCallback(EntityGhast entitylivingbaseIn, float partialTickTime) {
      float f = 1.0F;
      float f1 = (8.0F + f) / 2.0F;
      float f2 = (8.0F + 1.0F / f) / 2.0F;
      GL11.glScalef(f2, f1, f2);
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
   }
}
