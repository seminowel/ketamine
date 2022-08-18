package io.github.nevalackin.client.impl.ui.hud.rendered;

import io.github.nevalackin.client.api.module.Category;
import io.github.nevalackin.client.api.module.Module;
import io.github.nevalackin.client.api.property.Property;
import io.github.nevalackin.client.api.ui.cfont.CustomFontRenderer;
import io.github.nevalackin.client.impl.core.KetamineClient;
import io.github.nevalackin.client.impl.event.render.overlay.RenderGameOverlayEvent;
import io.github.nevalackin.client.impl.module.combat.rage.Aura;
import io.github.nevalackin.client.impl.property.EnumProperty;
import io.github.nevalackin.client.impl.ui.hud.components.HudComponent;
import io.github.nevalackin.client.util.render.ColourUtil;
import io.github.nevalackin.client.util.render.DrawUtil;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.awt.Color;
import java.text.DecimalFormat;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

public class TargetHudModule extends Module implements HudComponent {
   private Aura aura;
   private double xPos;
   private double yPos;
   private double width;
   private double height;
   private double healthBarWidth;
   private double animatedHealth;
   private double animatedArmor;
   private double animatedDistance;
   private boolean dragon;
   private DecimalFormat format = new DecimalFormat("0.0");
   private DecimalFormat format0 = new DecimalFormat("0");
   private final EnumProperty targetHudProperty;
   private final EnumProperty modeProperty;
   private final EnumProperty shapeProperty;
   private final EnumProperty renderModeProperty;
   private Frustum frustum;
   @EventLink
   private final Listener onRenderOverlay;
   @EventLink
   private final Listener onFrustumCull;

   public TargetHudModule() {
      super("TargetHud", Category.RENDER, Category.SubCategory.RENDER_OVERLAY);
      this.targetHudProperty = new EnumProperty("Target Hud", TargetHudModule.targetHudMode.KETAMINE);
      this.modeProperty = new EnumProperty("Mode", TargetHudModule.Mode.DRAWFACE, () -> {
         return this.targetHudProperty.getValue() == TargetHudModule.targetHudMode.KETAMINE;
      });
      this.shapeProperty = new EnumProperty("Shape", TargetHudModule.Shape.DRAWRECT, () -> {
         return this.targetHudProperty.getValue() == TargetHudModule.targetHudMode.KETAMINE;
      });
      this.renderModeProperty = new EnumProperty("Render Mode", TargetHudModule.RenderMode.NORMAL);
      this.onRenderOverlay = (event) -> {
         this.render(event.getScaledResolution().getScaledWidth(), event.getScaledResolution().getScaledHeight(), (double)event.getPartialTicks(), event);
      };
      this.onFrustumCull = (event) -> {
         this.frustum = event.getFrustum();
      };
      this.register(new Property[]{this.targetHudProperty, this.shapeProperty, this.modeProperty, this.renderModeProperty});
      this.setX(405.0);
      this.setY(390.0);
   }

   public boolean isDragging() {
      return this.dragon;
   }

   public void setDragging(boolean dragging) {
      this.dragon = dragging;
   }

   public void render(int scaledWidth, int scaledHeight, double tickDelta) {
   }

   public void render(int scaledWidth, int scaledHeight, double tickDelta, RenderGameOverlayEvent event) {
      EntityLivingBase target = this.aura.getTarget();
      if (target == null && this.mc.currentScreen == KetamineClient.getInstance().getDropdownGUI()) {
         target = this.mc.thePlayer;
      }

      if (target != null) {
         if (this.frustum != null) {
            this.fitInScreen(scaledWidth, scaledHeight);
            double hpClamped = (double)(((EntityLivingBase)target).getHealth() / ((EntityLivingBase)target).getMaxHealth());
            hpClamped = MathHelper.clamp_double(hpClamped, 0.0, 1.0);
            double armorClamped = (double)((EntityLivingBase)target).getTotalArmorValue() / 20.0;
            armorClamped = MathHelper.clamp_double(armorClamped, 0.0, 1.0);
            double distanceClamped = (double)this.mc.thePlayer.getDistanceToEntity((Entity)target) / 6.0;
            distanceClamped = MathHelper.clamp_double(distanceClamped, 0.0, 1.0);
            CustomFontRenderer fontRenderer = KetamineClient.getInstance().getFontRenderer();
            int startColour = ColourUtil.fadeBetween(ColourUtil.getClientColour(), ColourUtil.getSecondaryColour(), 0L);
            int endColour = ColourUtil.fadeBetween(ColourUtil.getSecondaryColour(), ColourUtil.getClientColour(), 250L);
            int distanceGradientColour = ColourUtil.getGradientOffset(new Color(0, 255, 0), new Color(255, 0, 0), (double)Math.abs(this.mc.thePlayer.getDistanceToEntity((Entity)target) / 6.0F) + 3.0 / (fontRenderer.getHeight(this.getName()) + 135.0)).getRGB();
            double margin = 2.0;
            double x;
            double y;
            if (!((RenderMode)this.renderModeProperty.getValue()).equals(TargetHudModule.RenderMode.PROJECT)) {
               x = this.getX();
               y = this.getY();
            } else {
               if (this.frustum == null || !this.frustum.isBoundingBoxInFrustum(((EntityLivingBase)target).getEntityBoundingBox()) || target == this.mc.thePlayer && this.mc.gameSettings.thirdPersonView == 0) {
                  return;
               }

               AxisAlignedBB interpolatedBB = this.interpolateRenderBB((Entity)target, event);
               double[] coords = new double[4];
               this.projectAABB(coords, interpolatedBB, (Entity)target, event);
               x = coords[0];
               y = coords[1] - 60.0;
               double eWidth = coords[2];
               double eHeight = coords[3];
               x -= (x - eWidth) / 2.0;
            }

            String text = "Distance: " + this.format0.format((double)((EntityLivingBase)target).getDistanceToEntity(this.mc.thePlayer)) + " | Hurt: " + this.format.format((long)((EntityLivingBase)target).hurtTime);
            double heightBounds = fontRenderer.getHeight(text);
            double width = 85.0 + margin * 2.0;
            double height = heightBounds + margin * 2.0;
            this.width = width + 70.0;
            this.height = height + 30.0;
            this.setWidth(this.width);
            this.setHeight(this.height);
            double hpWidth = 80.0 * hpClamped;
            this.healthBarWidth = DrawUtil.animateProgress(this.healthBarWidth, hpWidth, 75.0);
            this.animatedHealth = DrawUtil.animateProgress(this.animatedHealth, (double)(((EntityLivingBase)target).getHealth() * 20.0F), 120.0);
            this.animatedArmor = DrawUtil.animateProgress(this.animatedArmor, (double)((float)((EntityLivingBase)target).getTotalArmorValue() * 20.0F), 120.0);
            this.animatedDistance = DrawUtil.animateProgress(this.animatedDistance, (double)(this.mc.thePlayer.getDistanceToEntity((Entity)target) * 20.0F), 120.0);
            double animatedClampedHealth = this.animatedHealth / 20.0 / (double)((EntityLivingBase)target).getMaxHealth();
            animatedClampedHealth = MathHelper.clamp_double(animatedClampedHealth, 0.0, 1.0);
            double animatedClampedArmor = this.animatedArmor / 20.0 / 20.0;
            animatedClampedArmor = MathHelper.clamp_double(animatedClampedArmor, 0.0, 1.0);
            double animatedClampedDistance = this.animatedDistance / 20.0 / 6.0;
            animatedClampedDistance = MathHelper.clamp_double(animatedClampedDistance, 0.0, 1.0);
            double healthAnimatedPercent = 20.0 * (this.healthBarWidth / 80.0) * 5.0;
            switch ((targetHudMode)this.targetHudProperty.getValue()) {
               case KETAMINE:
                  switch ((Shape)this.shapeProperty.getValue()) {
                     case DRAWRECT:
                        GL11.glPushMatrix();
                        GL11.glTranslated(-(width + 70.0) / 2.0, 0.0, 0.0);
                        DrawUtil.glDrawFilledQuad(x, y + 1.0, (double)((float)width + 70.0F), (double)((float)(height + 28.0)), 1610612736);
                        DrawUtil.glDrawOutlinedQuadGradient(x, y + 1.0, width + 70.0, (double)((float)(height + 28.0)), 2.0F, startColour, endColour);
                        DrawUtil.glDrawSidewaysGradientRect(x + 39.0, y + 30.0, (double)((float)this.healthBarWidth), 6.75, startColour, endColour);
                        fontRenderer.drawWithShadow(((EntityLivingBase)target).getName(), x + 39.0, y + 6.0, 0.28, 900, 0.5, -1);
                        fontRenderer.drawWithShadow(text, x + 39.0, y + 17.0, 0.5, -8355712);
                        fontRenderer.drawWithShadow(this.format.format(healthAnimatedPercent) + "%", x + this.healthBarWidth + hpClamped + 41.0, y + 28.5, 0.5, -1);
                        GL11.glPopMatrix();
                        break;
                     case DRAWROUNDED:
                        GL11.glPushMatrix();
                        GL11.glTranslated(-(width + 70.0) / 2.0, 0.0, 0.0);
                        DrawUtil.glDrawRoundedQuad(x, y + 1.0, (float)width + 70.0F, (float)(height + 28.0), 4.0F, 1610612736);
                        DrawUtil.glDrawRoundedQuadRainbow(x + 39.0, y + 30.0, (float)this.healthBarWidth, 6.75F, 4.0F);
                        fontRenderer.drawWithShadow(((EntityLivingBase)target).getName(), x + 39.0, y + 6.0, 0.5, -1);
                        fontRenderer.drawWithShadow(text, x + 39.0, y + 17.0, 0.5, -8355712);
                        fontRenderer.drawWithShadow(this.format.format(healthAnimatedPercent) + "%", x + this.healthBarWidth + hpClamped + 41.0, y + 28.5, 0.5, -1);
                        GL11.glPopMatrix();
                  }

                  switch ((Mode)this.modeProperty.getValue()) {
                     case DRAWFACE:
                        GL11.glPushMatrix();
                        GL11.glTranslated(-(width + 70.0) / 2.0, 0.0, 0.0);
                        if (target instanceof EntityPlayer) {
                           DrawUtil.drawFace(x + 3.0, y + 5.0, 33.0, 33.0, (AbstractClientPlayer)target);
                        }

                        GL11.glPopMatrix();
                        return;
                     case DRAWMODEL:
                        GL11.glPushMatrix();
                        GL11.glTranslated(-(width + 70.0) / 2.0, 0.0, 0.0);
                        GL11.glColor3f(255.0F, 255.0F, 255.0F);
                        GuiInventory.drawEntityOnScreen((int)(x + 19.0), (int)(y + 40.0), 18, ((EntityLivingBase)target).rotationYaw, ((EntityLivingBase)target).rotationPitch, (EntityLivingBase)target);
                        GL11.glPopMatrix();
                        return;
                     default:
                        return;
                  }
               case NEW:
                  GL11.glPushMatrix();
                  GL11.glTranslated(-(width + 70.0) / 2.0, 0.0, 0.0);
                  DrawUtil.glDrawFilledQuad(x, y + 1.0, (double)((float)width + 70.0F), (double)((float)(height + 28.0)), Integer.MIN_VALUE);
                  fontRenderer.drawWithShadow(((EntityLivingBase)target).getName(), x + 3.0, y + 3.0, 1.0, -1);
                  DrawUtil.glDrawArcOutline(x + 33.0, y + 38.0, 18.0F, -90.0F, 90.0F, 2.0F, ColourUtil.blendHealthColours(hpClamped));
                  DrawUtil.glDrawFilledQuad(x + 15.0, y + 37.0, 36.0, 1.0, ColourUtil.blendHealthColours(hpClamped));
                  DrawUtil.glDrawPoint(x + 33.0, y + 37.5, 4.0F, event.getScaledResolution(), -16777216);
                  fontRenderer.drawWithShadow(this.format.format((double)((EntityLivingBase)target).getHealth()), x + 32.0 - fontRenderer.getWidth(this.format.format((double)((EntityLivingBase)target).getHealth())) / 2.0, y + 26.5, 1.0, -1);
                  DrawUtil.glDrawLine(x + 33.0, y + 37.5, x + 33.0 - Math.sin(animatedClampedHealth * Math.PI + Math.toRadians(90.0)) * 18.0, y + 37.5 + Math.cos(animatedClampedHealth * Math.PI + Math.toRadians(90.0)) * 18.0, 2.0F, true, -16777216);
                  DrawUtil.glDrawArcOutline(x + 78.0, y + 38.0, 18.0F, -90.0F, 90.0F, 2.0F, -9720370);
                  DrawUtil.glDrawFilledQuad(x + 60.0, y + 37.0, 36.0, 1.0, -9720370);
                  DrawUtil.glDrawPoint(x + 78.0, y + 37.5, 4.0F, event.getScaledResolution(), -16777216);
                  fontRenderer.drawWithShadow(this.format.format((long)((EntityLivingBase)target).getTotalArmorValue()), x + 77.0 - fontRenderer.getWidth(this.format.format((long)((EntityLivingBase)target).getTotalArmorValue())) / 2.0, y + 26.5, 1.0, -1);
                  DrawUtil.glDrawLine(x + 78.0, y + 37.5, x + 78.0 - Math.sin(animatedClampedArmor * Math.PI + Math.toRadians(90.0)) * 18.0, y + 37.5 + Math.cos(animatedClampedArmor * Math.PI + Math.toRadians(90.0)) * 18.0, 2.0F, true, -16777216);
                  DrawUtil.glDrawArcOutline(x + 123.0, y + 38.0, 18.0F, -90.0F, 90.0F, 2.0F, distanceGradientColour);
                  DrawUtil.glDrawFilledQuad(x + 105.0, y + 37.0, 36.0, 1.0, distanceGradientColour);
                  DrawUtil.glDrawPoint(x + 123.0, y + 37.5, 4.0F, event.getScaledResolution(), -16777216);
                  fontRenderer.drawWithShadow(this.format.format((double)this.mc.thePlayer.getDistanceToEntity((Entity)target)), x + 122.0 - fontRenderer.getWidth(this.format.format((double)this.mc.thePlayer.getDistanceToEntity((Entity)target))) / 2.0, y + 26.5, 1.0, -1);
                  DrawUtil.glDrawLine(x + 123.0, y + 37.5, x + 123.0 - Math.sin(animatedClampedDistance * Math.PI + Math.toRadians(90.0)) * 18.0, y + 37.5 + Math.cos(animatedClampedDistance * Math.PI + Math.toRadians(90.0)) * 18.0, 2.0F, true, -16777216);
                  GL11.glPopMatrix();
            }

         }
      }
   }

   public void onEnable() {
      if (this.aura == null) {
         this.aura = (Aura)KetamineClient.getInstance().getModuleManager().getModule(Aura.class);
      }

   }

   public void projectAABB(double[] coordsOut, AxisAlignedBB interpolatedBB, Entity entity, RenderGameOverlayEvent event) {
      coordsOut[0] = Double.MAX_VALUE;
      coordsOut[1] = Double.MAX_VALUE;
      coordsOut[2] = Double.MIN_VALUE;
      coordsOut[3] = Double.MIN_VALUE;
      double[][] bounds = new double[][]{{interpolatedBB.minX, interpolatedBB.minY, interpolatedBB.minZ}, {interpolatedBB.minX, interpolatedBB.maxY, interpolatedBB.minZ}, {interpolatedBB.minX, interpolatedBB.maxY, interpolatedBB.maxZ}, {interpolatedBB.minX, interpolatedBB.minY, interpolatedBB.maxZ}, {interpolatedBB.maxX, interpolatedBB.minY, interpolatedBB.minZ}, {interpolatedBB.maxX, interpolatedBB.maxY, interpolatedBB.minZ}, {interpolatedBB.maxX, interpolatedBB.maxY, interpolatedBB.maxZ}, {interpolatedBB.maxX, interpolatedBB.minY, interpolatedBB.maxZ}};
      double[][] var6 = bounds;
      int var7 = bounds.length;

      for(int var8 = 0; var8 < var7; ++var8) {
         double[] position = var6[var8];
         double[] projected = this.projectPosition(position[0], position[1], position[2], event);
         coordsOut[0] = Math.min(coordsOut[0], projected[0]);
         coordsOut[1] = Math.min(coordsOut[1], event.getScaledResolution().getScaledHeight_double() - projected[1]);
         coordsOut[2] = Math.max(coordsOut[2], projected[0]);
         coordsOut[3] = Math.max(coordsOut[3], event.getScaledResolution().getScaledHeight_double() - projected[1]);
      }

   }

   public double[] projectPosition(double x, double y, double z, RenderGameOverlayEvent event) {
      boolean success = GLU.gluProject((float)x, (float)y, (float)z, ActiveRenderInfo.getModelView(), ActiveRenderInfo.getProjection(), ActiveRenderInfo.getViewport(), ActiveRenderInfo.getObjectCoords());
      return success ? new double[]{(double)(ActiveRenderInfo.getObjectCoords().get(0) / (float)event.getScaledResolution().getScaleFactor()), (double)(ActiveRenderInfo.getObjectCoords().get(1) / (float)event.getScaledResolution().getScaleFactor())} : new double[0];
   }

   public AxisAlignedBB interpolateRenderBB(Entity entity, RenderGameOverlayEvent event) {
      double minX = entity.lastTickPosX - (double)entity.width / 2.0 + (entity.posX - entity.lastTickPosX) * (double)event.getPartialTicks() - this.mc.getRenderManager().getRenderPosX();
      double minY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double)event.getPartialTicks() - this.mc.getRenderManager().getRenderPosY();
      double minZ = entity.lastTickPosZ - (double)entity.width / 2.0 + (entity.posZ - entity.lastTickPosZ) * (double)event.getPartialTicks() - this.mc.getRenderManager().getRenderPosZ();
      double maxX = entity.lastTickPosX + (double)entity.width / 2.0 + (entity.posX - entity.lastTickPosX) * (double)event.getPartialTicks() - this.mc.getRenderManager().getRenderPosX();
      double maxY = entity.lastTickPosY + (double)entity.height + (entity.posY - entity.lastTickPosY) * (double)event.getPartialTicks() - this.mc.getRenderManager().getRenderPosY();
      double maxZ = entity.lastTickPosZ + (double)entity.width / 2.0 + (entity.posZ - entity.lastTickPosZ) * (double)event.getPartialTicks() - this.mc.getRenderManager().getRenderPosZ();
      return AxisAlignedBB.fromBounds(minX, minY, minZ, maxX, maxY, maxZ);
   }

   public void onDisable() {
   }

   public void setX(double x) {
      this.xPos = x;
   }

   public void setY(double y) {
      this.yPos = y;
   }

   public double getX() {
      return this.xPos;
   }

   public double getY() {
      return this.yPos;
   }

   public double setWidth(double width) {
      return this.width = width;
   }

   public double setHeight(double height) {
      return this.height = height;
   }

   public double getWidth() {
      return this.width;
   }

   public double getHeight() {
      return this.height;
   }

   public boolean isVisible() {
      return true;
   }

   public void setVisible(boolean visible) {
   }

   private static enum RenderMode {
      NORMAL("Normal"),
      PROJECT("Project");

      private final String name;

      private RenderMode(String name) {
         this.name = name;
      }

      public String toString() {
         return this.name;
      }
   }

   private static enum Shape {
      DRAWRECT("Draw Rectangle"),
      DRAWROUNDED("Draw Rounded");

      private final String name;

      private Shape(String name) {
         this.name = name;
      }

      public String toString() {
         return this.name;
      }
   }

   private static enum Mode {
      DRAWFACE("Draw Face"),
      DRAWMODEL("Draw Model");

      private final String name;

      private Mode(String name) {
         this.name = name;
      }

      public String toString() {
         return this.name;
      }
   }

   private static enum targetHudMode {
      KETAMINE("Ketamine"),
      NEW("New");

      private final String name;

      private targetHudMode(String name) {
         this.name = name;
      }

      public String toString() {
         return this.name;
      }
   }
}
