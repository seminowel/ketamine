package net.minecraft.client.renderer.entity;

import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class RenderEntity extends Render {
   public RenderEntity(RenderManager renderManagerIn) {
      super(renderManagerIn);
   }

   public void doRender(Entity entity, double x, double y, double z, float entityYaw, float partialTicks) {
      GL11.glPushMatrix();
      renderOffsetAABB(entity.getEntityBoundingBox(), x - entity.lastTickPosX, y - entity.lastTickPosY, z - entity.lastTickPosZ);
      GL11.glPopMatrix();
      super.doRender(entity, x, y, z, entityYaw, partialTicks);
   }

   protected ResourceLocation getEntityTexture(Entity entity) {
      return null;
   }
}
