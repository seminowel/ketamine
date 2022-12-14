package net.minecraft.client.renderer.entity;

import java.util.Iterator;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

public abstract class Render {
   private static final ResourceLocation shadowTextures = new ResourceLocation("textures/misc/shadow.png");
   protected final RenderManager renderManager;
   protected float shadowSize;
   protected float shadowOpaque = 1.0F;

   protected Render(RenderManager renderManager) {
      this.renderManager = renderManager;
   }

   public boolean shouldRender(Entity livingEntity, ICamera camera, double camX, double camY, double camZ) {
      AxisAlignedBB axisalignedbb = livingEntity.getEntityBoundingBox();
      if (axisalignedbb.func_181656_b() || axisalignedbb.getAverageEdgeLength() == 0.0) {
         axisalignedbb = new AxisAlignedBB(livingEntity.posX - 2.0, livingEntity.posY - 2.0, livingEntity.posZ - 2.0, livingEntity.posX + 2.0, livingEntity.posY + 2.0, livingEntity.posZ + 2.0);
      }

      return livingEntity.isInRangeToRender3d(camX, camY, camZ) && (livingEntity.ignoreFrustumCheck || camera.isBoundingBoxInFrustum(axisalignedbb));
   }

   public void doRender(Entity entity, double x, double y, double z, float entityYaw, float partialTicks) {
      if (RendererLivingEntity.isNotRenderInsideGUI) {
         this.renderName(entity, x, y, z);
      }

   }

   protected void renderName(Entity entity, double x, double y, double z) {
      if (this.canRenderName(entity)) {
         this.renderLivingLabel(entity, entity.getDisplayName().getFormattedText(), x, y, z, 64);
      }

   }

   protected boolean canRenderName(Entity entity) {
      return entity.getAlwaysRenderNameTagForRender() && entity.hasCustomName();
   }

   protected void renderOffsetLivingLabel(Entity entityIn, double x, double y, double z, String str, float p_177069_9_, double p_177069_10_) {
      this.renderLivingLabel(entityIn, str, x, y, z, 64);
   }

   protected abstract ResourceLocation getEntityTexture(Entity var1);

   protected boolean bindEntityTexture(Entity entity) {
      ResourceLocation resourcelocation = this.getEntityTexture(entity);
      if (resourcelocation == null) {
         return false;
      } else {
         this.bindTexture(resourcelocation);
         return true;
      }
   }

   public void bindTexture(ResourceLocation location) {
      this.renderManager.renderEngine.bindTexture(location);
   }

   private void renderEntityOnFire(Entity entity, double x, double y, double z, float partialTicks) {
      GL11.glDisable(2896);
      TextureMap texturemap = Minecraft.getMinecraft().getTextureMapBlocks();
      TextureAtlasSprite textureatlassprite = texturemap.getAtlasSprite("minecraft:blocks/fire_layer_0");
      TextureAtlasSprite textureatlassprite1 = texturemap.getAtlasSprite("minecraft:blocks/fire_layer_1");
      GL11.glPushMatrix();
      GL11.glTranslatef((float)x, (float)y, (float)z);
      float f = entity.width * 1.4F;
      GL11.glScalef(f, f, f);
      Tessellator tessellator = Tessellator.getInstance();
      WorldRenderer worldrenderer = tessellator.getWorldRenderer();
      float f1 = 0.5F;
      float f2 = 0.0F;
      float f3 = entity.height / f;
      float f4 = (float)(entity.posY - entity.getEntityBoundingBox().minY);
      GL11.glRotatef(-this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
      GL11.glTranslatef(0.0F, 0.0F, -0.3F + (float)((int)f3) * 0.02F);
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      float f5 = 0.0F;
      int i = 0;
      worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);

      while(f3 > 0.0F) {
         TextureAtlasSprite textureatlassprite2 = i % 2 == 0 ? textureatlassprite : textureatlassprite1;
         this.bindTexture(TextureMap.locationBlocksTexture);
         float f6 = textureatlassprite2.getMinU();
         float f7 = textureatlassprite2.getMinV();
         float f8 = textureatlassprite2.getMaxU();
         float f9 = textureatlassprite2.getMaxV();
         if (i / 2 % 2 == 0) {
            float f10 = f8;
            f8 = f6;
            f6 = f10;
         }

         worldrenderer.pos((double)(f1 - f2), (double)(0.0F - f4), (double)f5).tex((double)f8, (double)f9).endVertex();
         worldrenderer.pos((double)(-f1 - f2), (double)(0.0F - f4), (double)f5).tex((double)f6, (double)f9).endVertex();
         worldrenderer.pos((double)(-f1 - f2), (double)(1.4F - f4), (double)f5).tex((double)f6, (double)f7).endVertex();
         worldrenderer.pos((double)(f1 - f2), (double)(1.4F - f4), (double)f5).tex((double)f8, (double)f7).endVertex();
         f3 -= 0.45F;
         f4 -= 0.45F;
         f1 *= 0.9F;
         f5 += 0.03F;
         ++i;
      }

      tessellator.draw();
      GL11.glPopMatrix();
      GL11.glEnable(2896);
   }

   private void renderShadow(Entity entityIn, double x, double y, double z, float shadowAlpha, float partialTicks) {
      GL11.glEnable(3042);
      GL11.glBlendFunc(770, 771);
      this.renderManager.renderEngine.bindTexture(shadowTextures);
      World world = this.getWorldFromRenderManager();
      GL11.glDepthMask(false);
      float f = this.shadowSize;
      if (entityIn instanceof EntityLiving) {
         EntityLiving entityliving = (EntityLiving)entityIn;
         f *= entityliving.getRenderSizeModifier();
         if (entityliving.isChild()) {
            f *= 0.5F;
         }
      }

      double d5 = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * (double)partialTicks;
      double d0 = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * (double)partialTicks;
      double d1 = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * (double)partialTicks;
      int i = MathHelper.floor_double(d5 - (double)f);
      int j = MathHelper.floor_double(d5 + (double)f);
      int k = MathHelper.floor_double(d0 - (double)f);
      int l = MathHelper.floor_double(d0);
      int i1 = MathHelper.floor_double(d1 - (double)f);
      int j1 = MathHelper.floor_double(d1 + (double)f);
      double d2 = x - d5;
      double d3 = y - d0;
      double d4 = z - d1;
      Tessellator tessellator = Tessellator.getInstance();
      WorldRenderer worldrenderer = tessellator.getWorldRenderer();
      worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
      Iterator var32 = BlockPos.getAllInBoxMutable(new BlockPos(i, k, i1), new BlockPos(j, l, j1)).iterator();

      while(var32.hasNext()) {
         BlockPos blockpos = (BlockPos)var32.next();
         Block block = world.getBlockState(blockpos.down()).getBlock();
         if (block.getRenderType() != -1 && world.getLightFromNeighbors(blockpos) > 3) {
            this.func_180549_a(block, x, y, z, blockpos, shadowAlpha, f, d2, d3, d4);
         }
      }

      tessellator.draw();
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      GL11.glDisable(3042);
      GL11.glDepthMask(true);
   }

   private World getWorldFromRenderManager() {
      return this.renderManager.worldObj;
   }

   private void func_180549_a(Block blockIn, double p_180549_2_, double p_180549_4_, double p_180549_6_, BlockPos pos, float p_180549_9_, float p_180549_10_, double p_180549_11_, double p_180549_13_, double p_180549_15_) {
      if (blockIn.isFullCube()) {
         Tessellator tessellator = Tessellator.getInstance();
         WorldRenderer worldrenderer = tessellator.getWorldRenderer();
         double d0 = ((double)p_180549_9_ - (p_180549_4_ - ((double)pos.getY() + p_180549_13_)) / 2.0) * 0.5 * (double)this.getWorldFromRenderManager().getLightBrightness(pos);
         if (d0 >= 0.0) {
            if (d0 > 1.0) {
               d0 = 1.0;
            }

            double d1 = (double)pos.getX() + blockIn.getBlockBoundsMinX() + p_180549_11_;
            double d2 = (double)pos.getX() + blockIn.getBlockBoundsMaxX() + p_180549_11_;
            double d3 = (double)pos.getY() + blockIn.getBlockBoundsMinY() + p_180549_13_ + 0.015625;
            double d4 = (double)pos.getZ() + blockIn.getBlockBoundsMinZ() + p_180549_15_;
            double d5 = (double)pos.getZ() + blockIn.getBlockBoundsMaxZ() + p_180549_15_;
            float f = (float)((p_180549_2_ - d1) / 2.0 / (double)p_180549_10_ + 0.5);
            float f1 = (float)((p_180549_2_ - d2) / 2.0 / (double)p_180549_10_ + 0.5);
            float f2 = (float)((p_180549_6_ - d4) / 2.0 / (double)p_180549_10_ + 0.5);
            float f3 = (float)((p_180549_6_ - d5) / 2.0 / (double)p_180549_10_ + 0.5);
            worldrenderer.pos(d1, d3, d4).tex((double)f, (double)f2).color(1.0F, 1.0F, 1.0F, (float)d0).endVertex();
            worldrenderer.pos(d1, d3, d5).tex((double)f, (double)f3).color(1.0F, 1.0F, 1.0F, (float)d0).endVertex();
            worldrenderer.pos(d2, d3, d5).tex((double)f1, (double)f3).color(1.0F, 1.0F, 1.0F, (float)d0).endVertex();
            worldrenderer.pos(d2, d3, d4).tex((double)f1, (double)f2).color(1.0F, 1.0F, 1.0F, (float)d0).endVertex();
         }
      }

   }

   public static void renderOffsetAABB(AxisAlignedBB boundingBox, double x, double y, double z) {
      GL11.glDisable(3553);
      Tessellator tessellator = Tessellator.getInstance();
      WorldRenderer worldrenderer = tessellator.getWorldRenderer();
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      worldrenderer.setTranslation(x, y, z);
      worldrenderer.begin(7, DefaultVertexFormats.POSITION_NORMAL);
      worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).normal(0.0F, 0.0F, -1.0F).endVertex();
      worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).normal(0.0F, 0.0F, -1.0F).endVertex();
      worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).normal(0.0F, 0.0F, -1.0F).endVertex();
      worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).normal(0.0F, 0.0F, -1.0F).endVertex();
      worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).normal(0.0F, 0.0F, 1.0F).endVertex();
      worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).normal(0.0F, 0.0F, 1.0F).endVertex();
      worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).normal(0.0F, 0.0F, 1.0F).endVertex();
      worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).normal(0.0F, 0.0F, 1.0F).endVertex();
      worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).normal(0.0F, -1.0F, 0.0F).endVertex();
      worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).normal(0.0F, -1.0F, 0.0F).endVertex();
      worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).normal(0.0F, -1.0F, 0.0F).endVertex();
      worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).normal(0.0F, -1.0F, 0.0F).endVertex();
      worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).normal(0.0F, 1.0F, 0.0F).endVertex();
      worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).normal(0.0F, 1.0F, 0.0F).endVertex();
      worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).normal(0.0F, 1.0F, 0.0F).endVertex();
      worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).normal(0.0F, 1.0F, 0.0F).endVertex();
      worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).normal(-1.0F, 0.0F, 0.0F).endVertex();
      worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).normal(-1.0F, 0.0F, 0.0F).endVertex();
      worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).normal(-1.0F, 0.0F, 0.0F).endVertex();
      worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).normal(-1.0F, 0.0F, 0.0F).endVertex();
      worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).normal(1.0F, 0.0F, 0.0F).endVertex();
      worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).normal(1.0F, 0.0F, 0.0F).endVertex();
      worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).normal(1.0F, 0.0F, 0.0F).endVertex();
      worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).normal(1.0F, 0.0F, 0.0F).endVertex();
      tessellator.draw();
      worldrenderer.setTranslation(0.0, 0.0, 0.0);
      GL11.glEnable(3553);
   }

   public void doRenderShadowAndFire(Entity entityIn, double x, double y, double z, float yaw, float partialTicks) {
      if (this.renderManager.options != null) {
         if (this.renderManager.options.field_181151_V && this.shadowSize > 0.0F && !entityIn.isInvisible() && this.renderManager.isRenderShadow()) {
            double d0 = this.renderManager.getDistanceToCamera(entityIn.posX, entityIn.posY, entityIn.posZ);
            float f = (float)((1.0 - d0 / 256.0) * (double)this.shadowOpaque);
            if (f > 0.0F) {
               this.renderShadow(entityIn, x, y, z, f, partialTicks);
            }
         }

         if (entityIn.canRenderOnFire() && (!(entityIn instanceof EntityPlayer) || !((EntityPlayer)entityIn).isSpectator())) {
            this.renderEntityOnFire(entityIn, x, y, z, partialTicks);
         }
      }

   }

   public FontRenderer getFontRendererFromRenderManager() {
      return this.renderManager.getFontRenderer();
   }

   protected void renderLivingLabel(Entity entityIn, String str, double x, double y, double z, int maxDistance) {
      double d0 = entityIn.getDistanceSqToEntity(this.renderManager.livingPlayer);
      if (d0 <= (double)(maxDistance * maxDistance)) {
         FontRenderer fontrenderer = this.getFontRendererFromRenderManager();
         float f = 1.6F;
         float f1 = 0.016666668F * f;
         GL11.glPushMatrix();
         GL11.glTranslatef((float)x + 0.0F, (float)y + entityIn.height + 0.5F, (float)z);
         GL11.glNormal3f(0.0F, 1.0F, 0.0F);
         GL11.glRotatef(-this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
         GL11.glRotatef(this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
         GL11.glScalef(-f1, -f1, f1);
         GL11.glDisable(2896);
         GL11.glDepthMask(false);
         GL11.glDisable(2929);
         GL11.glEnable(3042);
         GL14.glBlendFuncSeparate(770, 771, 1, 0);
         Tessellator tessellator = Tessellator.getInstance();
         WorldRenderer worldrenderer = tessellator.getWorldRenderer();
         int i = 0;
         int j = fontrenderer.getStringWidth(str) / 2;
         GL11.glDisable(3553);
         worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
         worldrenderer.pos((double)(-j - 1), (double)(-1 + i), 0.0).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
         worldrenderer.pos((double)(-j - 1), (double)(8 + i), 0.0).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
         worldrenderer.pos((double)(j + 1), (double)(8 + i), 0.0).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
         worldrenderer.pos((double)(j + 1), (double)(-1 + i), 0.0).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
         tessellator.draw();
         GL11.glEnable(3553);
         fontrenderer.drawString(str, -fontrenderer.getStringWidth(str) / 2, i, 553648127);
         GL11.glEnable(2929);
         GL11.glDepthMask(true);
         fontrenderer.drawString(str, -fontrenderer.getStringWidth(str) / 2, i, -1);
         GL11.glEnable(2896);
         GL11.glDisable(3042);
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         GL11.glPopMatrix();
      }

   }

   public RenderManager getRenderManager() {
      return this.renderManager;
   }
}
