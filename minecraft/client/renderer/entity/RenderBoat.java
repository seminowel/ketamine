package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBoat;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class RenderBoat extends Render {
   private static final ResourceLocation boatTextures = new ResourceLocation("textures/entity/boat.png");
   protected ModelBase modelBoat = new ModelBoat();

   public RenderBoat(RenderManager renderManagerIn) {
      super(renderManagerIn);
      this.shadowSize = 0.5F;
   }

   public void doRender(EntityBoat entity, double x, double y, double z, float entityYaw, float partialTicks) {
      GL11.glPushMatrix();
      GL11.glTranslatef((float)x, (float)y + 0.25F, (float)z);
      GL11.glRotatef(180.0F - entityYaw, 0.0F, 1.0F, 0.0F);
      float f = (float)entity.getTimeSinceHit() - partialTicks;
      float f1 = entity.getDamageTaken() - partialTicks;
      if (f1 < 0.0F) {
         f1 = 0.0F;
      }

      if (f > 0.0F) {
         GL11.glRotatef(MathHelper.sin(f) * f * f1 / 10.0F * (float)entity.getForwardDirection(), 1.0F, 0.0F, 0.0F);
      }

      float f2 = 0.75F;
      GL11.glScalef(f2, f2, f2);
      GL11.glScalef(1.0F / f2, 1.0F / f2, 1.0F / f2);
      this.bindEntityTexture(entity);
      GL11.glScalef(-1.0F, -1.0F, 1.0F);
      this.modelBoat.render(entity, 0.0F, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
      GL11.glPopMatrix();
      super.doRender(entity, x, y, z, entityYaw, partialTicks);
   }

   protected ResourceLocation getEntityTexture(EntityBoat entity) {
      return boatTextures;
   }
}
