package io.github.nevalackin.client.impl.module.render.esp;

import io.github.nevalackin.client.api.module.Category;
import io.github.nevalackin.client.api.module.Module;
import io.github.nevalackin.client.api.property.Property;
import io.github.nevalackin.client.api.ui.cfont.CustomFontRenderer;
import io.github.nevalackin.client.impl.core.KetamineClient;
import io.github.nevalackin.client.impl.property.BooleanProperty;
import io.github.nevalackin.client.impl.property.ColourProperty;
import io.github.nevalackin.client.impl.property.DoubleProperty;
import io.github.nevalackin.client.impl.property.EnumProperty;
import io.github.nevalackin.client.util.player.RotationUtil;
import io.github.nevalackin.client.util.player.TeamsUtil;
import io.github.nevalackin.client.util.render.ColourUtil;
import io.github.nevalackin.client.util.render.DrawUtil;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.GL11;

public final class OffScreenESP extends Module {
   private final EnumProperty colourProperty;
   private final ColourProperty arrowColourProperty;
   private final BooleanProperty pulsingColourProperty;
   private final BooleanProperty outlineProperty;
   private final DoubleProperty radiusProperty;
   private final DoubleProperty arrowSizeProperty;
   private final BooleanProperty infoProperty;
   private Frustum frustum;
   @EventLink
   private final Listener onRenderGameOverlay;
   @EventLink
   private final Listener onUpdateFrustum;

   public OffScreenESP() {
      super("Off Screen ESP", Category.RENDER, Category.SubCategory.RENDER_ESP);
      this.colourProperty = new EnumProperty("Colour Mode", OffScreenESP.ColourMode.TEAM);
      this.arrowColourProperty = new ColourProperty("Arrow Colour", -15490049, () -> {
         return this.colourProperty.getValue() == OffScreenESP.ColourMode.NORMAL;
      });
      this.pulsingColourProperty = new BooleanProperty("Pulsing", true);
      this.outlineProperty = new BooleanProperty("Outline", false);
      this.radiusProperty = new DoubleProperty("Radius", 30.0, 1.0, 200.0, 0.5);
      this.arrowSizeProperty = new DoubleProperty("Size", 10.0, 1.0, 30.0, 0.5);
      this.infoProperty = new BooleanProperty("Draw Info", false);
      this.onRenderGameOverlay = (event) -> {
         if (this.frustum != null) {
            ScaledResolution scaledResolution = event.getScaledResolution();
            CustomFontRenderer fontRenderer = KetamineClient.getInstance().getFontRenderer();
            float hWidth = (float)scaledResolution.getScaledWidth() / 2.0F;
            float hHeight = (float)scaledResolution.getScaledHeight() / 2.0F;
            float partialTicks = event.getPartialTicks();
            GL11.glTranslatef(hWidth, hHeight, 0.0F);
            if ((Boolean)this.outlineProperty.getValue()) {
               GL11.glEnable(2848);
               GL11.glHint(3154, 4354);
               GL11.glLineWidth(1.0F);
            }

            double arrowSize = (Double)this.arrowSizeProperty.getValue();
            double radius = (Double)this.radiusProperty.getValue();
            this.mc.theWorld.playerEntities.stream().filter(this::validatePlayer).forEach((player) -> {
               if (!this.frustum.isBoundingBoxInFrustum(player.getEntityBoundingBox())) {
                  Entity local = this.mc.thePlayer;
                  float currentRotation = DrawUtil.interpolate(local.prevRotationYaw, local.rotationYaw, partialTicks);
                  double currentPosX = DrawUtil.interpolate(local.prevPosX, local.posX, (double)partialTicks);
                  double currentPosZ = DrawUtil.interpolate(local.prevPosZ, local.posZ, (double)partialTicks);
                  double playerPosX = DrawUtil.interpolate(player.prevPosX, player.posX, (double)partialTicks);
                  double playerPosZ = DrawUtil.interpolate(player.prevPosZ, player.posZ, (double)partialTicks);
                  float yawToPlayer = RotationUtil.calculateYawFromSrcToDst(currentRotation, currentPosX, currentPosZ, playerPosX, playerPosZ) - currentRotation;
                  GL11.glPushMatrix();
                  double rads = Math.toRadians((double)yawToPlayer);
                  double aspectRatio = scaledResolution.getScaledWidth_double() / scaledResolution.getScaledHeight_double();
                  GL11.glTranslated(radius * Math.sin(rads) * aspectRatio, radius * -Math.cos(rads), 0.0);
                  float health = player.getHealth() / player.getMaxHealth();
                  int healthColour = ColourUtil.blendHealthColours((double)health);
                  double barSize = arrowSize + 4.0;
                  if ((Boolean)this.infoProperty.getValue()) {
                     String name = player.getGameProfile().getName();
                     fontRenderer.draw(name, -fontRenderer.getWidth(name) / 2.0, -arrowSize / 2.0 - fontRenderer.getHeight(name) - 4.0, -1);
                  }

                  GL11.glDisable(3553);
                  boolean restore = DrawUtil.glEnableBlend();
                  if ((Boolean)this.infoProperty.getValue()) {
                     GL11.glBegin(7);
                     DrawUtil.glColour(-1778384896);
                     addQuadVertices(-barSize / 2.0, arrowSize / 2.0 + 2.0, barSize, 2.0);
                     DrawUtil.glColour(healthColour);
                     double filled = (barSize - 1.0) * (double)health;
                     addQuadVertices(-barSize / 2.0 + 0.5, arrowSize / 2.0 + 2.0 + 0.5, filled, 1.0);
                     GL11.glEnd();
                  }

                  int colour;
                  switch ((ColourMode)this.colourProperty.getValue()) {
                     case TEAM:
                        colour = TeamsUtil.TeamsMode.NAME.getColourSupplier().getTeamColour(player);
                        break;
                     case HEALTH:
                        colour = healthColour;
                        break;
                     default:
                        colour = (Integer)this.arrowColourProperty.getValue();
                  }

                  if ((Boolean)this.pulsingColourProperty.getValue()) {
                     colour = ColourUtil.fadeBetween(colour, colour & 1358954495);
                  }

                  GL11.glRotatef(yawToPlayer, 0.0F, 0.0F, 1.0F);
                  if ((Boolean)this.outlineProperty.getValue()) {
                     DrawUtil.glColour(colour | -16777216);
                     GL11.glBegin(2);
                     addTriangleVertices(arrowSize);
                     GL11.glEnd();
                  }

                  GL11.glEnable(2881);
                  GL11.glHint(3155, 4354);
                  DrawUtil.glColour(colour);
                  GL11.glBegin(4);
                  addTriangleVertices(arrowSize);
                  GL11.glEnd();
                  GL11.glDisable(2881);
                  GL11.glHint(3155, 4352);
                  DrawUtil.glRestoreBlend(restore);
                  GL11.glEnable(3553);
                  GL11.glPopMatrix();
               }

            });
            if ((Boolean)this.outlineProperty.getValue()) {
               GL11.glDisable(2848);
               GL11.glHint(3154, 4352);
            }

            GL11.glTranslatef(-hWidth, -hHeight, 0.0F);
         }
      };
      this.onUpdateFrustum = (event) -> {
         this.frustum = event.getFrustum();
      };
      this.register(new Property[]{this.colourProperty, this.arrowColourProperty, this.pulsingColourProperty, this.outlineProperty, this.radiusProperty, this.arrowSizeProperty, this.infoProperty});
   }

   private static void addQuadVertices(double x, double y, double width, double height) {
      GL11.glVertex2d(x, y);
      GL11.glVertex2d(x, y + height);
      GL11.glVertex2d(x + width, y + height);
      GL11.glVertex2d(x + width, y);
   }

   private static void addTriangleVertices(double size) {
      GL11.glVertex2d(0.0, -size / 2.0);
      GL11.glVertex2d(-size / 2.0, size / 2.0);
      GL11.glVertex2d(size / 2.0, size / 2.0);
   }

   private boolean validatePlayer(EntityPlayer player) {
      return player instanceof EntityOtherPlayerMP && player.isEntityAlive() && !player.isInvisible();
   }

   public void onEnable() {
      this.frustum = null;
   }

   public void onDisable() {
   }

   private static enum ColourMode {
      HEALTH("Health"),
      TEAM("Team"),
      NORMAL("Normal");

      private final String name;

      private ColourMode(String name) {
         this.name = name;
      }

      public String toString() {
         return this.name;
      }
   }
}
