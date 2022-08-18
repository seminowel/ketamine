package net.minecraft.client.renderer.tileentity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureCompass;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.MapData;
import org.lwjgl.opengl.GL11;

public class RenderItemFrame extends Render {
   private static final ResourceLocation mapBackgroundTextures = new ResourceLocation("textures/map/map_background.png");
   private final Minecraft mc = Minecraft.getMinecraft();
   private final ModelResourceLocation itemFrameModel = new ModelResourceLocation("item_frame", "normal");
   private final ModelResourceLocation mapModel = new ModelResourceLocation("item_frame", "map");
   private RenderItem itemRenderer;

   public RenderItemFrame(RenderManager renderManagerIn, RenderItem itemRendererIn) {
      super(renderManagerIn);
      this.itemRenderer = itemRendererIn;
   }

   public void doRender(EntityItemFrame entity, double x, double y, double z, float entityYaw, float partialTicks) {
      GL11.glPushMatrix();
      BlockPos blockpos = entity.getHangingPosition();
      double d0 = (double)blockpos.getX() - entity.posX + x;
      double d1 = (double)blockpos.getY() - entity.posY + y;
      double d2 = (double)blockpos.getZ() - entity.posZ + z;
      GL11.glTranslated(d0 + 0.5, d1 + 0.5, d2 + 0.5);
      GL11.glRotatef(180.0F - entity.rotationYaw, 0.0F, 1.0F, 0.0F);
      this.renderManager.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
      BlockRendererDispatcher blockrendererdispatcher = this.mc.getBlockRendererDispatcher();
      ModelManager modelmanager = blockrendererdispatcher.getBlockModelShapes().getModelManager();
      IBakedModel ibakedmodel;
      if (entity.getDisplayedItem() != null && entity.getDisplayedItem().getItem() == Items.filled_map) {
         ibakedmodel = modelmanager.getModel(this.mapModel);
      } else {
         ibakedmodel = modelmanager.getModel(this.itemFrameModel);
      }

      GL11.glPushMatrix();
      GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
      blockrendererdispatcher.getBlockModelRenderer().renderModelBrightnessColor(ibakedmodel, 1.0F, 1.0F, 1.0F, 1.0F);
      GL11.glPopMatrix();
      GL11.glTranslatef(0.0F, 0.0F, 0.4375F);
      this.renderItem(entity);
      GL11.glPopMatrix();
      this.renderName(entity, x + (double)((float)entity.facingDirection.getFrontOffsetX() * 0.3F), y - 0.25, z + (double)((float)entity.facingDirection.getFrontOffsetZ() * 0.3F));
   }

   protected ResourceLocation getEntityTexture(EntityItemFrame entity) {
      return null;
   }

   private void renderItem(EntityItemFrame itemFrame) {
      ItemStack itemstack = itemFrame.getDisplayedItem();
      if (itemstack != null) {
         EntityItem entityitem = new EntityItem(itemFrame.worldObj, 0.0, 0.0, 0.0, itemstack);
         Item item = entityitem.getEntityItem().getItem();
         entityitem.getEntityItem().stackSize = 1;
         entityitem.hoverStart = 0.0F;
         GL11.glPushMatrix();
         GL11.glDisable(2896);
         int i = itemFrame.getRotation();
         if (item == Items.filled_map) {
            i = i % 4 * 2;
         }

         GL11.glRotatef((float)i * 360.0F / 8.0F, 0.0F, 0.0F, 1.0F);
         if (item == Items.filled_map) {
            this.renderManager.renderEngine.bindTexture(mapBackgroundTextures);
            GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
            float f = 0.0078125F;
            GL11.glScalef(f, f, f);
            GL11.glTranslatef(-64.0F, -64.0F, 0.0F);
            MapData mapdata = Items.filled_map.getMapData(entityitem.getEntityItem(), itemFrame.worldObj);
            GL11.glTranslatef(0.0F, 0.0F, -1.0F);
            if (mapdata != null) {
               this.mc.entityRenderer.getMapItemRenderer().renderMap(mapdata, true);
            }
         } else {
            TextureAtlasSprite textureatlassprite = null;
            if (item == Items.compass) {
               textureatlassprite = this.mc.getTextureMapBlocks().getAtlasSprite(TextureCompass.field_176608_l);
               this.mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
               if (textureatlassprite instanceof TextureCompass) {
                  TextureCompass texturecompass = (TextureCompass)textureatlassprite;
                  double d0 = texturecompass.currentAngle;
                  double d1 = texturecompass.angleDelta;
                  texturecompass.currentAngle = 0.0;
                  texturecompass.angleDelta = 0.0;
                  texturecompass.updateCompass(itemFrame.worldObj, itemFrame.posX, itemFrame.posZ, (double)MathHelper.wrapAngleTo180_float((float)(180 + itemFrame.facingDirection.getHorizontalIndex() * 90)), false, true);
                  texturecompass.currentAngle = d0;
                  texturecompass.angleDelta = d1;
               } else {
                  textureatlassprite = null;
               }
            }

            GL11.glScalef(0.5F, 0.5F, 0.5F);
            if (!this.itemRenderer.shouldRenderItemIn3D(entityitem.getEntityItem()) || item instanceof ItemSkull) {
               GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
            }

            GlStateManager.pushAttrib();
            RenderHelper.enableStandardItemLighting();
            this.itemRenderer.func_181564_a(entityitem.getEntityItem(), ItemCameraTransforms.TransformType.FIXED);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.popAttrib();
            if (textureatlassprite != null && textureatlassprite.getFrameCount() > 0) {
               textureatlassprite.updateAnimation();
            }
         }

         GL11.glEnable(2896);
         GL11.glPopMatrix();
      }

   }

   protected void renderName(EntityItemFrame entity, double x, double y, double z) {
      if (Minecraft.isGuiEnabled() && entity.getDisplayedItem() != null && entity.getDisplayedItem().hasDisplayName() && this.renderManager.pointedEntity == entity) {
         float f = 1.6F;
         float f1 = 0.016666668F * f;
         double d0 = entity.getDistanceSqToEntity(this.renderManager.livingPlayer);
         float f2 = entity.isSneaking() ? 32.0F : 64.0F;
         if (d0 < (double)(f2 * f2)) {
            String s = entity.getDisplayedItem().getDisplayName();
            if (entity.isSneaking()) {
               FontRenderer fontrenderer = this.getFontRendererFromRenderManager();
               GL11.glPushMatrix();
               GL11.glTranslatef((float)x + 0.0F, (float)y + entity.height + 0.5F, (float)z);
               GL11.glNormal3f(0.0F, 1.0F, 0.0F);
               GL11.glRotatef(-this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
               GL11.glRotatef(this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
               GL11.glScalef(-f1, -f1, f1);
               GL11.glDisable(2896);
               GL11.glTranslatef(0.0F, 0.25F / f1, 0.0F);
               GL11.glDepthMask(false);
               GL11.glEnable(3042);
               GL11.glBlendFunc(770, 771);
               Tessellator tessellator = Tessellator.getInstance();
               WorldRenderer worldrenderer = tessellator.getWorldRenderer();
               int i = fontrenderer.getStringWidth(s) / 2;
               GL11.glDisable(3553);
               worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
               worldrenderer.pos((double)(-i - 1), -1.0, 0.0).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
               worldrenderer.pos((double)(-i - 1), 8.0, 0.0).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
               worldrenderer.pos((double)(i + 1), 8.0, 0.0).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
               worldrenderer.pos((double)(i + 1), -1.0, 0.0).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
               tessellator.draw();
               GL11.glEnable(3553);
               GL11.glDepthMask(true);
               fontrenderer.drawString(s, -fontrenderer.getStringWidth(s) / 2, 0, 553648127);
               GL11.glEnable(2896);
               GL11.glDisable(3042);
               GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
               GL11.glPopMatrix();
            } else {
               this.renderLivingLabel(entity, s, x, y, z, 64);
            }
         }
      }

   }
}
