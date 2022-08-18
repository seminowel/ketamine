package io.github.nevalackin.client.impl.module.render.self;

import io.github.nevalackin.client.api.module.Category;
import io.github.nevalackin.client.api.module.Module;
import io.github.nevalackin.client.api.property.Property;
import io.github.nevalackin.client.impl.property.DoubleProperty;
import io.github.nevalackin.client.util.render.ColourUtil;
import io.github.nevalackin.client.util.render.DrawUtil;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.client.entity.EntityPlayerSP;
import org.lwjgl.opengl.GL11;

public final class ChinaHat extends Module {
   private final DoubleProperty pointsProperty = new DoubleProperty("Points", 180.0, 3.0, 180.0, 1.0);
   private final DoubleProperty sizeProperty = new DoubleProperty("Size", 0.6, 0.0, 2.0, 0.1);
   private final double[][] positions;
   private final int[] segmentColours;
   @EventLink
   private final Listener onRender3D;

   public ChinaHat() {
      super("China Hat", Category.RENDER, Category.SubCategory.RENDER_SELF);
      this.positions = new double[(int)this.pointsProperty.getMax() + 1][2];
      this.segmentColours = new int[(int)this.pointsProperty.getMax() + 1];
      this.onRender3D = (event) -> {
         if (this.mc.gameSettings.getThirdPersonView() != 0) {
            GL11.glDisable(3553);
            GL11.glDisable(2884);
            GL11.glDepthMask(false);
            GL11.glDisable(2929);
            GL11.glShadeModel(7425);
            boolean restore = DrawUtil.glEnableBlend();
            EntityPlayerSP player = this.mc.thePlayer;
            float partialTicks = event.getPartialTicks();
            double x = DrawUtil.interpolate(player.prevPosX, player.posX, (double)partialTicks);
            double y = DrawUtil.interpolate(player.prevPosY, player.posY, (double)partialTicks);
            double z = DrawUtil.interpolate(player.prevPosZ, player.posZ, (double)partialTicks);
            int points = ((Double)this.pointsProperty.getValue()).intValue();
            double radius = (Double)this.sizeProperty.getValue();
            long totalOffset = 2000L;
            long pointOffset = 2000L / (long)points;

            for(int i = 0; i < this.segmentColours.length; ++i) {
               this.segmentColours[i] = ColourUtil.blendRainbowColours((long)i * pointOffset);
            }

            GL11.glPushMatrix();
            GL11.glTranslated(x, y + 1.9, z);
            if (player.isSneaking()) {
               GL11.glTranslated(0.0, -0.2, 0.0);
            }

            GL11.glRotatef(DrawUtil.interpolate(player.getLastTickYaw(), player.getYaw(), partialTicks), 0.0F, -1.0F, 0.0F);
            float pitch = DrawUtil.interpolate(player.getLastTickPitch(), player.getPitch(), partialTicks);
            GL11.glRotatef(pitch / 3.0F, 1.0F, 0.0F, 0.0F);
            GL11.glTranslated(0.0, 0.0, (double)(pitch / 270.0F));
            GL11.glEnable(2848);
            GL11.glHint(3154, 4354);
            GL11.glLineWidth(2.0F);
            GL11.glBegin(2);
            this.addCircleVertices(points - 1, 255);
            GL11.glEnd();
            GL11.glDisable(2848);
            GL11.glHint(3154, 4352);
            GL11.glBegin(6);
            GL11.glVertex3d(0.0, radius / 2.0, 0.0);
            this.addCircleVertices(points, 128);
            GL11.glEnd();
            GL11.glPopMatrix();
            DrawUtil.glRestoreBlend(restore);
            GL11.glDepthMask(true);
            GL11.glShadeModel(7424);
            GL11.glEnable(2929);
            GL11.glEnable(2884);
            GL11.glEnable(3553);
         }
      };
      this.register(new Property[]{this.pointsProperty, this.sizeProperty});
      this.pointsProperty.addChangeListener((now) -> {
         this.computeChineseHatPoints(now.intValue(), (Double)this.sizeProperty.getValue());
      });
      this.sizeProperty.addChangeListener((now) -> {
         this.computeChineseHatPoints(((Double)this.pointsProperty.getValue()).intValue(), now);
      });
   }

   private void computeChineseHatPoints(int points, double radius) {
      for(int i = 0; i <= points; ++i) {
         double circleX = radius * StrictMath.cos((double)i * Math.PI * 2.0 / (double)points);
         double circleZ = radius * StrictMath.sin((double)i * Math.PI * 2.0 / (double)points);
         this.positions[i][0] = circleX;
         this.positions[i][1] = circleZ;
      }

   }

   private void addCircleVertices(int points, int alpha) {
      for(int i = 0; i <= points; ++i) {
         double[] pos = this.positions[i];
         DrawUtil.glColour(this.segmentColours[i] + (alpha << 24));
         GL11.glVertex3d(pos[0], 0.0, pos[1]);
      }

   }

   public void onEnable() {
   }

   public void onDisable() {
   }
}
