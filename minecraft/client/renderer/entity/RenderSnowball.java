package net.minecraft.client.renderer.entity;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class RenderSnowball extends Render {
   protected final Item field_177084_a;
   private final RenderItem field_177083_e;

   public RenderSnowball(RenderManager renderManagerIn, Item p_i46137_2_, RenderItem p_i46137_3_) {
      super(renderManagerIn);
      this.field_177084_a = p_i46137_2_;
      this.field_177083_e = p_i46137_3_;
   }

   public void doRender(Entity entity, double x, double y, double z, float entityYaw, float partialTicks) {
      GL11.glPushMatrix();
      GL11.glTranslatef((float)x, (float)y, (float)z);
      GlStateManager.enableRescaleNormal();
      GL11.glScalef(0.5F, 0.5F, 0.5F);
      GL11.glRotatef(-this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
      GL11.glRotatef(this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
      this.bindTexture(TextureMap.locationBlocksTexture);
      this.field_177083_e.func_181564_a(this.func_177082_d(entity), ItemCameraTransforms.TransformType.GROUND);
      GlStateManager.disableRescaleNormal();
      GL11.glPopMatrix();
      super.doRender(entity, x, y, z, entityYaw, partialTicks);
   }

   public ItemStack func_177082_d(Entity entityIn) {
      return new ItemStack(this.field_177084_a, 1, 0);
   }

   protected ResourceLocation getEntityTexture(Entity entity) {
      return TextureMap.locationBlocksTexture;
   }
}
