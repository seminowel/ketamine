package net.minecraft.client.renderer.entity;

import net.minecraft.entity.monster.EntityCaveSpider;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class RenderCaveSpider extends RenderSpider {
   private static final ResourceLocation caveSpiderTextures = new ResourceLocation("textures/entity/spider/cave_spider.png");

   public RenderCaveSpider(RenderManager renderManagerIn) {
      super(renderManagerIn);
      this.shadowSize *= 0.7F;
   }

   protected void preRenderCallback(EntityCaveSpider entitylivingbaseIn, float partialTickTime) {
      GL11.glScalef(0.7F, 0.7F, 0.7F);
   }

   protected ResourceLocation getEntityTexture(EntityCaveSpider entity) {
      return caveSpiderTextures;
   }
}
