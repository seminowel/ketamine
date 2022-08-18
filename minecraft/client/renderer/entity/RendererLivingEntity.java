package net.minecraft.client.renderer.entity;

import com.google.common.collect.Lists;
import io.github.nevalackin.client.impl.core.KetamineClient;
import io.github.nevalackin.client.impl.event.player.UpdatePositionEvent;
import io.github.nevalackin.client.impl.event.render.model.ApplyHurtEffectEvent;
import io.github.nevalackin.client.impl.event.render.model.ModelRenderEvent;
import io.github.nevalackin.client.impl.event.render.model.RenderLivingEntityEvent;
import io.github.nevalackin.client.impl.event.render.model.RenderNameEvent;
import io.github.nevalackin.client.util.render.DrawUtil;
import java.nio.FloatBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

public abstract class RendererLivingEntity extends Render {
   private static final Logger logger = LogManager.getLogger();
   private static final DynamicTexture field_177096_e = new DynamicTexture(16, 16);
   public static boolean isNotRenderInsideGUI = true;
   protected ModelBase mainModel;
   protected FloatBuffer brightnessBuffer = GLAllocation.createDirectFloatBuffer(4);
   protected List layerRenderers = Lists.newArrayList();

   public RendererLivingEntity(RenderManager renderManagerIn, ModelBase modelBaseIn, float shadowSizeIn) {
      super(renderManagerIn);
      this.mainModel = modelBaseIn;
      this.shadowSize = shadowSizeIn;
   }

   public void doRender(EntityLivingBase entity, double x, double y, double z, float entityYaw, float partialTicks) {
      GL11.glPushMatrix();
      GL11.glDisable(2884);
      this.mainModel.swingProgress = this.getSwingProgress(entity, partialTicks);
      this.mainModel.isRiding = entity.isRiding();
      this.mainModel.isChild = entity.isChild();

      try {
         float pitch;
         float bodyYaw;
         float headYaw;
         UpdatePositionEvent event;
         float yawDif;
         float ticksPT;
         if (entity instanceof EntityPlayerSP && (event = ((EntityPlayerSP)entity).lastEvent) != null && event.isRotating()) {
            yawDif = DrawUtil.interpolate(event.getLastTickYaw(), event.getYaw(), partialTicks);
            ticksPT = DrawUtil.interpolate(event.getLastTickPitch(), event.getPitch(), partialTicks);
            pitch = ticksPT;
            bodyYaw = yawDif;
            headYaw = yawDif;
         } else {
            bodyYaw = this.interpolateRotation(entity.prevRenderYawOffset, entity.renderYawOffset, partialTicks);
            headYaw = this.interpolateRotation(entity.prevRotationYawHead, entity.rotationYawHead, partialTicks);
            pitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
         }

         yawDif = headYaw - bodyYaw;
         float interpLimbSwing;
         if (entity.isRiding() && entity.ridingEntity instanceof EntityLivingBase) {
            EntityLivingBase entitylivingbase = (EntityLivingBase)entity.ridingEntity;
            bodyYaw = this.interpolateRotation(entitylivingbase.prevRenderYawOffset, entitylivingbase.renderYawOffset, partialTicks);
            yawDif = headYaw - bodyYaw;
            interpLimbSwing = MathHelper.wrapAngleTo180_float(yawDif);
            if (interpLimbSwing < -85.0F) {
               interpLimbSwing = -85.0F;
            }

            if (interpLimbSwing >= 85.0F) {
               interpLimbSwing = 85.0F;
            }

            bodyYaw = headYaw - interpLimbSwing;
            if (interpLimbSwing * interpLimbSwing > 2500.0F) {
               bodyYaw += interpLimbSwing * 0.2F;
            }

            yawDif = headYaw - bodyYaw;
         }

         this.renderLivingAt(entity, x, y, z);
         ticksPT = this.handleRotationFloat(entity, partialTicks);
         this.rotateCorpse(entity, ticksPT, bodyYaw, partialTicks);
         GlStateManager.enableRescaleNormal();
         GL11.glScalef(-1.0F, -1.0F, 1.0F);
         this.preRenderCallback(entity, partialTicks);
         GL11.glTranslatef(0.0F, -1.5078125F, 0.0F);
         interpLimbSwing = entity.prevLimbSwingAmount + (entity.limbSwingAmount - entity.prevLimbSwingAmount) * partialTicks;
         float invLimbSwing = entity.limbSwing - entity.limbSwingAmount * (1.0F - partialTicks);
         if (entity.isChild()) {
            invLimbSwing *= 3.0F;
         }

         if (interpLimbSwing > 1.0F) {
            interpLimbSwing = 1.0F;
         }

         GlStateManager.enableAlpha();
         this.mainModel.setLivingAnimations(entity, invLimbSwing, interpLimbSwing, partialTicks);
         this.mainModel.setRotationAngles(invLimbSwing, interpLimbSwing, ticksPT, yawDif, pitch, 0.0625F, entity);
         RenderLivingEntityEvent renderLivingEntityEvent = new RenderLivingEntityEvent(entity, partialTicks);
         KetamineClient.getInstance().getEventBus().post(renderLivingEntityEvent);
         if (!renderLivingEntityEvent.isCancelled()) {
            renderLivingEntityEvent.onPreRender();
            ApplyHurtEffectEvent applyHurtEffectEvent = new ApplyHurtEffectEvent(ApplyHurtEffectEvent.RenderCallbackFunc.SET, ApplyHurtEffectEvent.RenderCallbackFunc.UNSET, 1308557312, entity);
            KetamineClient.getInstance().getEventBus().post(applyHurtEffectEvent);
            Supplier setBrightnessFunc = () -> {
               return this.setDoRenderBrightness(entity, partialTicks, applyHurtEffectEvent.getHurtColour());
            };
            boolean hasSetBrightness = false;
            if (applyHurtEffectEvent.getPreRenderModelCallback() == ApplyHurtEffectEvent.RenderCallbackFunc.SET) {
               hasSetBrightness = (Boolean)setBrightnessFunc.get();
            }

            this.renderModel(entity, bodyYaw, invLimbSwing, interpLimbSwing, ticksPT, yawDif, pitch, 0.0625F, partialTicks);
            switch (applyHurtEffectEvent.getPreRenderLayersCallback()) {
               case UNSET:
                  if (hasSetBrightness) {
                     this.unsetBrightness();
                     hasSetBrightness = false;
                  }
                  break;
               case SET:
                  if (!hasSetBrightness) {
                     hasSetBrightness = (Boolean)setBrightnessFunc.get();
                  }
            }

            if (!(entity instanceof EntityPlayer) || !((EntityPlayer)entity).isSpectator()) {
               this.renderLayers(entity, invLimbSwing, interpLimbSwing, partialTicks, ticksPT, yawDif, pitch, 0.0625F);
            }

            if (hasSetBrightness) {
               this.unsetBrightness();
            }

            renderLivingEntityEvent.onPostRender();
         }

         GlStateManager.disableRescaleNormal();
      } catch (Exception var22) {
         logger.error("Couldn't render entity", var22);
      }

      GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
      GL11.glEnable(3553);
      GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
      GL11.glEnable(2884);
      GL11.glPopMatrix();
      super.doRender(entity, x, y, z, entityYaw, partialTicks);
   }

   public void renderName(EntityLivingBase entity, double x, double y, double z) {
      if (this.canRenderName(entity)) {
         RenderNameEvent event = new RenderNameEvent(entity);
         KetamineClient.getInstance().getEventBus().post(event);
         if (event.isCancelled()) {
            return;
         }

         double d0 = entity.getDistanceSqToEntity(this.renderManager.livingPlayer);
         float f = entity.isSneaking() ? 32.0F : 64.0F;
         if (d0 < (double)(f * f)) {
            String s = entity.getDisplayName().getFormattedText();
            float f1 = 0.02666667F;
            GL11.glAlphaFunc(516, 0.1F);
            if (entity.isSneaking()) {
               FontRenderer fontrenderer = this.getFontRendererFromRenderManager();
               GL11.glPushMatrix();
               GL11.glTranslatef((float)x, (float)y + entity.height + 0.5F - (entity.isChild() ? entity.height / 2.0F : 0.0F), (float)z);
               GL11.glNormal3f(0.0F, 1.0F, 0.0F);
               GL11.glRotatef(-this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
               GL11.glRotatef(this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
               GL11.glScalef(-0.02666667F, -0.02666667F, 0.02666667F);
               GL11.glTranslatef(0.0F, 9.374999F, 0.0F);
               GL11.glDisable(2896);
               GL11.glDepthMask(false);
               GL11.glEnable(3042);
               GL11.glDisable(3553);
               GL14.glBlendFuncSeparate(770, 771, 1, 0);
               int i = fontrenderer.getStringWidth(s) / 2;
               Tessellator tessellator = Tessellator.getInstance();
               WorldRenderer worldrenderer = tessellator.getWorldRenderer();
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
               this.renderOffsetLivingLabel(entity, x, y - (entity.isChild() ? (double)(entity.height / 2.0F) : 0.0), z, s, 0.02666667F, d0);
            }
         }
      }

   }

   protected boolean canRenderName(EntityLivingBase entity) {
      EntityPlayerSP entityplayersp = Minecraft.getMinecraft().thePlayer;
      if (entity instanceof EntityPlayer && entity != entityplayersp && isNotRenderInsideGUI) {
         Team team = entity.getTeam();
         Team team1 = entityplayersp.getTeam();
         if (team != null) {
            Team.EnumVisible team$enumvisible = team.getNameTagVisibility();
            switch (team$enumvisible) {
               case ALWAYS:
                  return true;
               case NEVER:
                  return false;
               case HIDE_FOR_OTHER_TEAMS:
                  return team1 == null || team.isSameTeam(team1);
               case HIDE_FOR_OWN_TEAM:
                  return team1 == null || !team.isSameTeam(team1);
               default:
                  return true;
            }
         }
      }

      return Minecraft.isGuiEnabled() && entity != this.renderManager.livingPlayer && !entity.isInvisibleToPlayer(entityplayersp) && entity.riddenByEntity == null;
   }

   protected boolean addLayer(LayerRenderer layer) {
      return this.layerRenderers.add(layer);
   }

   protected boolean removeLayer(LayerRenderer layer) {
      return this.layerRenderers.remove(layer);
   }

   public ModelBase getMainModel() {
      return this.mainModel;
   }

   protected float interpolateRotation(float par1, float par2, float par3) {
      float f;
      for(f = par2 - par1; f < -180.0F; f += 360.0F) {
      }

      while(f >= 180.0F) {
         f -= 360.0F;
      }

      return par1 + par3 * f;
   }

   public void transformHeldFull3DItemLayer() {
   }

   protected boolean setScoreTeamColor(EntityLivingBase entityLivingBaseIn) {
      int i = 16777215;
      if (entityLivingBaseIn instanceof EntityPlayer) {
         ScorePlayerTeam scoreplayerteam = (ScorePlayerTeam)entityLivingBaseIn.getTeam();
         if (scoreplayerteam != null) {
            String s = FontRenderer.getFormatFromString(scoreplayerteam.getColorPrefix());
            if (s.length() >= 2) {
               i = this.getFontRendererFromRenderManager().getColorCode(s.charAt(1));
            }
         }
      }

      float f1 = (float)(i >> 16 & 255) / 255.0F;
      float f2 = (float)(i >> 8 & 255) / 255.0F;
      float f = (float)(i & 255) / 255.0F;
      GL11.glDisable(2896);
      GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
      GL11.glColor4f(f1, f2, f, 1.0F);
      GL11.glDisable(3553);
      GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
      GL11.glDisable(3553);
      GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
      return true;
   }

   protected void unsetScoreTeamColor() {
      GL11.glEnable(2896);
      GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
      GL11.glEnable(3553);
      GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
      GL11.glEnable(3553);
      GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
   }

   protected void renderModel(EntityLivingBase entitylivingbaseIn, float bodyYaw, float invLimbSwing, float interpLimbSwing, float ticksPT, float yawDif, float pitch, float scale, float partialTicks) {
      boolean flag = !entitylivingbaseIn.isInvisible();
      boolean flag1 = !flag && !entitylivingbaseIn.isInvisibleToPlayer(Minecraft.getMinecraft().thePlayer);
      if (flag || flag1) {
         if (flag1) {
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.15F);
            GL11.glDepthMask(false);
            GL11.glEnable(3042);
            GL11.glBlendFunc(770, 771);
         }

         if (entitylivingbaseIn instanceof EntityPlayer && isNotRenderInsideGUI) {
            GL11.glDisable(3008);
            if (!this.bindEntityTexture(entitylivingbaseIn)) {
               return;
            }

            ModelRenderEvent renderEvent = new ModelRenderEvent((EntityPlayer)entitylivingbaseIn, () -> {
               this.mainModel.render(entitylivingbaseIn, invLimbSwing, interpLimbSwing, ticksPT, yawDif, pitch, scale);
            }, () -> {
               this.renderLayers(entitylivingbaseIn, invLimbSwing, interpLimbSwing, partialTicks, ticksPT, yawDif, pitch, scale);
            }, bodyYaw);
            KetamineClient.getInstance().getEventBus().post(renderEvent);
            renderEvent.drawModel();
            GL11.glEnable(3008);
            renderEvent.setPost();
            KetamineClient.getInstance().getEventBus().post(renderEvent);
         } else {
            if (!this.bindEntityTexture(entitylivingbaseIn)) {
               return;
            }

            this.mainModel.render(entitylivingbaseIn, invLimbSwing, interpLimbSwing, ticksPT, yawDif, pitch, scale);
         }

         if (flag1) {
            GL11.glDisable(3042);
            GL11.glDepthMask(true);
         }
      }

   }

   protected boolean setDoRenderBrightness(EntityLivingBase entityLivingBaseIn, float partialTicks, int hurtColour) {
      return this.setBrightness(entityLivingBaseIn, partialTicks, true, hurtColour);
   }

   protected boolean setBrightness(EntityLivingBase entitylivingbaseIn, float partialTicks, boolean combineTextures, int hurtColour) {
      float f = entitylivingbaseIn.getBrightness(partialTicks);
      int i = this.getColorMultiplier(entitylivingbaseIn, f, partialTicks);
      boolean flag = (i >> 24 & 255) > 0;
      boolean flag1 = entitylivingbaseIn.hurtTime > 0 || entitylivingbaseIn.deathTime > 0;
      if (!flag && !flag1) {
         return false;
      } else if (!flag && !combineTextures) {
         return false;
      } else {
         GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
         GL11.glEnable(3553);
         GL11.glTexEnvi(8960, 8704, OpenGlHelper.GL_COMBINE);
         GL11.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_RGB, 8448);
         GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_RGB, OpenGlHelper.defaultTexUnit);
         GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.GL_PRIMARY_COLOR);
         GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_RGB, 768);
         GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND1_RGB, 768);
         GL11.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_ALPHA, 7681);
         GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_ALPHA, OpenGlHelper.defaultTexUnit);
         GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_ALPHA, 770);
         GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
         GL11.glEnable(3553);
         GL11.glTexEnvi(8960, 8704, OpenGlHelper.GL_COMBINE);
         GL11.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_RGB, OpenGlHelper.GL_INTERPOLATE);
         GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_RGB, OpenGlHelper.GL_CONSTANT);
         GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.GL_PREVIOUS);
         GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE2_RGB, OpenGlHelper.GL_CONSTANT);
         GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_RGB, 768);
         GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND1_RGB, 768);
         GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND2_RGB, 770);
         GL11.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_ALPHA, 7681);
         GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_ALPHA, OpenGlHelper.GL_PREVIOUS);
         GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_ALPHA, 770);
         this.brightnessBuffer.position(0);
         float a;
         float r;
         float g;
         float b;
         if (flag1) {
            a = (float)(hurtColour >> 24 & 255) / 255.0F;
            r = (float)(hurtColour >> 16 & 255) / 255.0F;
            g = (float)(hurtColour >> 8 & 255) / 255.0F;
            b = (float)(hurtColour & 255) / 255.0F;
            this.brightnessBuffer.put(r);
            this.brightnessBuffer.put(g);
            this.brightnessBuffer.put(b);
            this.brightnessBuffer.put(a);
         } else {
            a = (float)(i >> 24 & 255) / 255.0F;
            r = (float)(i >> 16 & 255) / 255.0F;
            g = (float)(i >> 8 & 255) / 255.0F;
            b = (float)(i & 255) / 255.0F;
            this.brightnessBuffer.put(r);
            this.brightnessBuffer.put(g);
            this.brightnessBuffer.put(b);
            this.brightnessBuffer.put(1.0F - a);
         }

         this.brightnessBuffer.flip();
         GL11.glTexEnv(8960, 8705, this.brightnessBuffer);
         GlStateManager.setActiveTexture(OpenGlHelper.GL_TEXTURE2);
         GL11.glEnable(3553);
         GL11.glBindTexture(3553, field_177096_e.getGlTextureId());
         GL11.glTexEnvi(8960, 8704, OpenGlHelper.GL_COMBINE);
         GL11.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_RGB, 8448);
         GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_RGB, OpenGlHelper.GL_PREVIOUS);
         GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.lightmapTexUnit);
         GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_RGB, 768);
         GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND1_RGB, 768);
         GL11.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_ALPHA, 7681);
         GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_ALPHA, OpenGlHelper.GL_PREVIOUS);
         GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_ALPHA, 770);
         GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
         return true;
      }
   }

   protected void unsetBrightness() {
      GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
      GL11.glEnable(3553);
      GL11.glTexEnvi(8960, 8704, OpenGlHelper.GL_COMBINE);
      GL11.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_RGB, 8448);
      GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_RGB, OpenGlHelper.defaultTexUnit);
      GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.GL_PRIMARY_COLOR);
      GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_RGB, 768);
      GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND1_RGB, 768);
      GL11.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_ALPHA, 8448);
      GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_ALPHA, OpenGlHelper.defaultTexUnit);
      GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE1_ALPHA, OpenGlHelper.GL_PRIMARY_COLOR);
      GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_ALPHA, 770);
      GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND1_ALPHA, 770);
      GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
      GL11.glTexEnvi(8960, 8704, OpenGlHelper.GL_COMBINE);
      GL11.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_RGB, 8448);
      GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_RGB, 768);
      GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND1_RGB, 768);
      GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_RGB, 5890);
      GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.GL_PREVIOUS);
      GL11.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_ALPHA, 8448);
      GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_ALPHA, 770);
      GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_ALPHA, 5890);
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.setActiveTexture(OpenGlHelper.GL_TEXTURE2);
      GL11.glDisable(3553);
      GL11.glBindTexture(3553, 0);
      GL11.glTexEnvi(8960, 8704, OpenGlHelper.GL_COMBINE);
      GL11.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_RGB, 8448);
      GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_RGB, 768);
      GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND1_RGB, 768);
      GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_RGB, 5890);
      GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.GL_PREVIOUS);
      GL11.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_ALPHA, 8448);
      GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_ALPHA, 770);
      GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_ALPHA, 5890);
      GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
   }

   protected void renderLivingAt(EntityLivingBase entityLivingBaseIn, double x, double y, double z) {
      GL11.glTranslatef((float)x, (float)y, (float)z);
   }

   protected void rotateCorpse(EntityLivingBase bat, float p_77043_2_, float p_77043_3_, float partialTicks) {
      GL11.glRotatef(180.0F - p_77043_3_, 0.0F, 1.0F, 0.0F);
      if (bat.deathTime > 0) {
         float f = ((float)bat.deathTime + partialTicks - 1.0F) / 20.0F * 1.6F;
         f = MathHelper.sqrt_float(f);
         if (f > 1.0F) {
            f = 1.0F;
         }

         GL11.glRotatef(f * this.getDeathMaxRotation(bat), 0.0F, 0.0F, 1.0F);
      } else {
         String s = EnumChatFormatting.getTextWithoutFormattingCodes(bat.getName());
         if (s != null && (s.equals("Dinnerbone") || s.equals("Grumm")) && (!(bat instanceof EntityPlayer) || ((EntityPlayer)bat).isWearing(EnumPlayerModelParts.CAPE))) {
            GL11.glTranslatef(0.0F, bat.height + 0.1F, 0.0F);
            GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
         }
      }

   }

   protected float getSwingProgress(EntityLivingBase livingBase, float partialTickTime) {
      return livingBase.getSwingProgress(partialTickTime);
   }

   protected float handleRotationFloat(EntityLivingBase livingBase, float partialTicks) {
      return (float)livingBase.ticksExisted + partialTicks;
   }

   protected void renderLayers(EntityLivingBase entitylivingbaseIn, float p_177093_2_, float p_177093_3_, float partialTicks, float p_177093_5_, float p_177093_6_, float p_177093_7_, float p_177093_8_) {
      Iterator var9 = this.layerRenderers.iterator();

      while(var9.hasNext()) {
         LayerRenderer layerrenderer = (LayerRenderer)var9.next();
         layerrenderer.doRenderLayer(entitylivingbaseIn, p_177093_2_, p_177093_3_, partialTicks, p_177093_5_, p_177093_6_, p_177093_7_, p_177093_8_);
      }

   }

   protected float getDeathMaxRotation(EntityLivingBase entityLivingBaseIn) {
      return 90.0F;
   }

   protected int getColorMultiplier(EntityLivingBase entitylivingbaseIn, float lightBrightness, float partialTickTime) {
      return 0;
   }

   protected void preRenderCallback(EntityLivingBase entitylivingbaseIn, float partialTickTime) {
   }

   static {
      int[] aint = field_177096_e.getTextureData();

      for(int i = 0; i < 256; ++i) {
         aint[i] = -1;
      }

      field_177096_e.updateDynamicTexture();
   }
}
