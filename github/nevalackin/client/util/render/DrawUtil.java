package io.github.nevalackin.client.util.render;

import io.github.nevalackin.client.api.ui.cfont.StaticallySizedImage;
import io.github.nevalackin.client.util.math.MathUtil;
import io.github.nevalackin.client.util.misc.ResourceUtil;
import java.awt.Color;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.glu.GLU;

public final class DrawUtil {
   private static final FloatBuffer WND_POS_BUFFER = GLAllocation.createDirectFloatBuffer(4);
   private static final IntBuffer VIEWPORT_BUFFER = GLAllocation.createDirectIntBuffer(16);
   private static final FloatBuffer MODEL_MATRIX_BUFFER = GLAllocation.createDirectFloatBuffer(16);
   private static final FloatBuffer PROJECTION_MATRIX_BUFFER = GLAllocation.createDirectFloatBuffer(16);
   private static final IntBuffer SCISSOR_BUFFER = GLAllocation.createDirectIntBuffer(16);
   private static StaticallySizedImage checkmarkImage;
   private static StaticallySizedImage warningImage;
   private static final String CIRCLE_FRAG_SHADER = "#version 120\n\nuniform float innerRadius;\nuniform vec4 colour;\n\nvoid main() {\n   vec2 pixel = gl_TexCoord[0].st;\n   vec2 centre = vec2(0.5, 0.5);\n   float d = length(pixel - centre);\n   float c = smoothstep(d+innerRadius, d+innerRadius+0.01, 0.5-innerRadius);\n   float a = smoothstep(0.0, 1.0, c) * colour.a;\n   gl_FragColor = vec4(colour.rgb, a);\n}\n";
   public static final String VERTEX_SHADER = "#version 120 \n\nvoid main() {\n    gl_TexCoord[0] = gl_MultiTexCoord0;\n    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;\n}";
   private static final GLShader CIRCLE_SHADER;
   private static final String ROUNDED_QUAD_FRAG_SHADER = "#version 120\nuniform float width;\nuniform float height;\nuniform float radius;\nuniform vec4 colour;\n\nfloat SDRoundedRect(vec2 p, vec2 b, float r) {\n    vec2 q = abs(p) - b + r;\n    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r;\n}\n\nvoid main() {\n    vec2 size = vec2(width, height);\n    vec2 pixel = gl_TexCoord[0].st * size;\n    vec2 centre = 0.5 * size;\n    float b = SDRoundedRect(pixel - centre, centre, radius);\n    float a = 1.0 - smoothstep(0, 1.0, b);\n    gl_FragColor = vec4(colour.rgb, colour.a * a);\n}";
   private static final GLShader ROUNDED_QUAD_SHADER;
   private static final String RAINBOW_FRAG_SHADER = "#version 120\nuniform float width;\nuniform float height;\nuniform float radius;\nuniform float u_time;\n\nfloat SDRoundedRect(vec2 p, vec2 b, float r) {\n    vec2 q = abs(p) - b + r;\n    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r;\n}\n\nvoid main() {\n    vec2 size = vec2(width, height);\n    vec2 pixel = gl_TexCoord[0].st * size;\n    vec2 centre = 0.5 * size;\n    float b = SDRoundedRect(pixel - centre, centre, radius);\n    float a = 1.0 - smoothstep(0, 1.0, b);\n    vec3 colour = 0.5 + 0.5*cos(u_time+gl_TexCoord[0].st.x+vec3(0,2,4));\n    gl_FragColor = vec4(colour, a);\n}";
   private static final GLShader GL_COLOUR_SHADER;

   private DrawUtil() {
   }

   public static double animateProgress(double current, double target, double speed) {
      double inc;
      if (current < target) {
         inc = 1.0 / (double)Minecraft.getDebugFPS() * speed;
         return target - current < inc ? target : current + inc;
      } else if (current > target) {
         inc = 1.0 / (double)Minecraft.getDebugFPS() * speed;
         return current - target < inc ? target : current - inc;
      } else {
         return current;
      }
   }

   public static void checkGLError(String error) {
      int code = GL11.glGetError();
      if (code != 0) {
         String msg = GLU.gluErrorString(code);
         System.err.printf("error: %s\n%d: %s\n", error, code, msg);
      }

   }

   public static double bezierBlendAnimation(double t) {
      return t * t * (3.0 - 2.0 * t);
   }

   public static void glDrawTriangle(double x, double y, double x1, double y1, double x2, double y2, int colour) {
      GL11.glDisable(3553);
      boolean restore = glEnableBlend();
      GL11.glEnable(2881);
      GL11.glHint(3155, 4354);
      glColour(colour);
      GL11.glBegin(4);
      GL11.glVertex2d(x, y);
      GL11.glVertex2d(x1, y1);
      GL11.glVertex2d(x2, y2);
      GL11.glEnd();
      GL11.glEnable(3553);
      glRestoreBlend(restore);
      GL11.glDisable(2881);
      GL11.glHint(3155, 4352);
   }

   public static void glDrawFramebuffer(int framebufferTexture, int width, int height) {
      GL11.glBindTexture(3553, framebufferTexture);
      GL11.glDisable(3008);
      boolean restore = glEnableBlend();
      GL11.glBegin(7);
      GL11.glTexCoord2f(0.0F, 1.0F);
      GL11.glVertex2f(0.0F, 0.0F);
      GL11.glTexCoord2f(0.0F, 0.0F);
      GL11.glVertex2f(0.0F, (float)height);
      GL11.glTexCoord2f(1.0F, 0.0F);
      GL11.glVertex2f((float)width, (float)height);
      GL11.glTexCoord2f(1.0F, 1.0F);
      GL11.glVertex2f((float)width, 0.0F);
      GL11.glEnd();
      glRestoreBlend(restore);
      GL11.glEnable(3008);
   }

   public static void glDrawPlusSign(double x, double y, double size, double rotation, int colour) {
      GL11.glDisable(3553);
      boolean restore = glEnableBlend();
      GL11.glEnable(2848);
      GL11.glHint(3154, 4354);
      GL11.glLineWidth(1.0F);
      GL11.glPushMatrix();
      GL11.glTranslated(x, y, 0.0);
      GL11.glRotated(rotation, 0.0, 1.0, 1.0);
      GL11.glDisable(2929);
      GL11.glDepthMask(false);
      glColour(colour);
      GL11.glBegin(1);
      GL11.glVertex2d(-(size / 2.0), 0.0);
      GL11.glVertex2d(size / 2.0, 0.0);
      GL11.glVertex2d(0.0, -(size / 2.0));
      GL11.glVertex2d(0.0, size / 2.0);
      GL11.glEnd();
      GL11.glEnable(2929);
      GL11.glDepthMask(true);
      GL11.glPopMatrix();
      GL11.glEnable(3553);
      glRestoreBlend(restore);
      GL11.glDisable(2848);
      GL11.glHint(3154, 4352);
   }

   public static void glDrawFilledEllipse(double x, double y, double radius, int startIndex, int endIndex, int polygons, boolean smooth, int colour) {
      boolean restore = glEnableBlend();
      if (smooth) {
         GL11.glEnable(2881);
         GL11.glHint(3155, 4354);
      }

      GL11.glDisable(3553);
      glColour(colour);
      GL11.glDisable(2884);
      GL11.glBegin(9);
      GL11.glVertex2d(x, y);

      for(double i = (double)startIndex; i <= (double)endIndex; ++i) {
         double theta = 6.283185307179586 * i / (double)polygons;
         GL11.glVertex2d(x + radius * Math.cos(theta), y + radius * Math.sin(theta));
      }

      GL11.glEnd();
      glRestoreBlend(restore);
      if (smooth) {
         GL11.glDisable(2881);
         GL11.glHint(3155, 4352);
      }

      GL11.glEnable(2884);
      GL11.glEnable(3553);
   }

   public static void glDrawFilledEllipse(double x, double y, float radius, int colour) {
      boolean restore = glEnableBlend();
      GL11.glEnable(2832);
      GL11.glHint(3153, 4354);
      GL11.glDisable(3553);
      glColour(colour);
      GL11.glPointSize(radius);
      GL11.glBegin(0);
      GL11.glVertex2d(x, y);
      GL11.glEnd();
      glRestoreBlend(restore);
      GL11.glDisable(2832);
      GL11.glHint(3153, 4352);
      GL11.glEnable(3553);
   }

   public static void glScissorBox(double x, double y, double width, double height, ScaledResolution scaledResolution) {
      if (!GL11.glIsEnabled(3089)) {
         GL11.glEnable(3089);
      }

      int scaling = scaledResolution.getScaleFactor();
      GL11.glScissor((int)(x * (double)scaling), (int)(((double)scaledResolution.getScaledHeight() - (y + height)) * (double)scaling), (int)(width * (double)scaling), (int)(height * (double)scaling));
   }

   public static void glRestoreScissor() {
      if (!GL11.glIsEnabled(3089)) {
         GL11.glEnable(3089);
      }

      GL11.glScissor(SCISSOR_BUFFER.get(0), SCISSOR_BUFFER.get(1), SCISSOR_BUFFER.get(2), SCISSOR_BUFFER.get(3));
   }

   public static void glEndScissor() {
      GL11.glDisable(3089);
   }

   public static double[] worldToScreen(double[] positionVector, AxisAlignedBB boundingBox, double[] projection, double[] projectionBuffer) {
      double[][] bounds = new double[][]{{boundingBox.minX, boundingBox.minY, boundingBox.minZ}, {boundingBox.minX, boundingBox.maxY, boundingBox.minZ}, {boundingBox.minX, boundingBox.maxY, boundingBox.maxZ}, {boundingBox.minX, boundingBox.minY, boundingBox.maxZ}, {boundingBox.maxX, boundingBox.minY, boundingBox.minZ}, {boundingBox.maxX, boundingBox.maxY, boundingBox.minZ}, {boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ}, {boundingBox.maxX, boundingBox.minY, boundingBox.maxZ}};
      double[] position;
      if (positionVector != null) {
         if (!worldToScreen(positionVector, projectionBuffer, projection[2])) {
            return null;
         }

         position = new double[]{projection[0], projection[1], -1.0, -1.0, projectionBuffer[0], projectionBuffer[1]};
      } else {
         position = new double[]{projection[0], projection[1], -1.0, -1.0};
      }

      double[][] var6 = bounds;
      int var7 = bounds.length;

      for(int var8 = 0; var8 < var7; ++var8) {
         double[] vector = var6[var8];
         if (worldToScreen(vector, projectionBuffer, projection[2])) {
            double projected_x = projectionBuffer[0];
            double projected_y = projectionBuffer[1];
            position[0] = Math.min(position[0], projected_x);
            position[1] = Math.min(position[1], projected_y);
            position[2] = Math.max(position[2], projected_x);
            position[3] = Math.max(position[3], projected_y);
         }
      }

      return position;
   }

   public static boolean worldToScreen(double[] in, double[] out, double scaling) {
      GL11.glGetFloat(2982, MODEL_MATRIX_BUFFER);
      GL11.glGetFloat(2983, PROJECTION_MATRIX_BUFFER);
      GL11.glGetInteger(2978, VIEWPORT_BUFFER);
      if (GLU.gluProject((float)in[0], (float)in[1], (float)in[2], MODEL_MATRIX_BUFFER, PROJECTION_MATRIX_BUFFER, VIEWPORT_BUFFER, WND_POS_BUFFER)) {
         float zCoordinate = WND_POS_BUFFER.get(2);
         if (!(zCoordinate < 0.0F) && !(zCoordinate > 1.0F)) {
            out[0] = (double)WND_POS_BUFFER.get(0) / scaling;
            out[1] = (double)((float)Display.getHeight() - WND_POS_BUFFER.get(1)) / scaling;
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static void glColour(int color) {
      GL11.glColor4ub((byte)(color >> 16 & 255), (byte)(color >> 8 & 255), (byte)(color & 255), (byte)(color >> 24 & 255));
   }

   public static void glDrawGradientLine(double x, double y, double x1, double y1, float lineWidth, int colour) {
      boolean restore = glEnableBlend();
      GL11.glDisable(3553);
      GL11.glLineWidth(lineWidth);
      GL11.glEnable(2848);
      GL11.glHint(3154, 4354);
      GL11.glShadeModel(7425);
      int noAlpha = ColourUtil.removeAlphaComponent(colour);
      GL11.glDisable(3008);
      GL11.glBegin(3);
      glColour(noAlpha);
      GL11.glVertex2d(x, y);
      double dif = x1 - x;
      glColour(colour);
      GL11.glVertex2d(x + dif * 0.4, y);
      GL11.glVertex2d(x + dif * 0.6, y);
      glColour(noAlpha);
      GL11.glVertex2d(x1, y1);
      GL11.glEnd();
      GL11.glEnable(3008);
      GL11.glShadeModel(7424);
      glRestoreBlend(restore);
      GL11.glDisable(2848);
      GL11.glHint(3154, 4352);
      GL11.glEnable(3553);
   }

   public static void glDrawLine(double x, double y, double x1, double y1, float lineWidth, boolean smoothed, int colour) {
      boolean restore = glEnableBlend();
      GL11.glDisable(3553);
      GL11.glLineWidth(lineWidth);
      if (smoothed) {
         GL11.glEnable(2848);
         GL11.glHint(3154, 4354);
      }

      glColour(colour);
      GL11.glBegin(1);
      GL11.glVertex2d(x, y);
      GL11.glVertex2d(x1, y1);
      GL11.glEnd();
      glRestoreBlend(restore);
      if (smoothed) {
         GL11.glDisable(2848);
         GL11.glHint(3154, 4352);
      }

      GL11.glEnable(3553);
   }

   public static void glDrawPlayerFace(double x, double y, double width, double height, ResourceLocation skinLocation) {
      Minecraft.getMinecraft().getTextureManager().bindTexture(skinLocation);
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      float eightPixelOff = 0.125F;
      GL11.glBegin(7);
      GL11.glTexCoord2f(0.125F, 0.125F);
      GL11.glVertex2d(x, y);
      GL11.glTexCoord2f(0.125F, 0.25F);
      GL11.glVertex2d(x, y + height);
      GL11.glTexCoord2f(0.25F, 0.25F);
      GL11.glVertex2d(x + width, y + height);
      GL11.glTexCoord2f(0.25F, 0.125F);
      GL11.glVertex2d(x + width, y);
      GL11.glEnd();
   }

   public static void drawFace(double x, double y, double width, double height, AbstractClientPlayer target) {
      ResourceLocation skin = target.getLocationSkin();
      Minecraft.getMinecraft().getTextureManager().bindTexture(skin);
      GL11.glEnable(3042);
      GL11.glColor4f(255.0F, 255.0F, 255.0F, 1.0F);
      float hurtTimePercentage = ((float)target.hurtTime - Minecraft.getMinecraft().getTimer().renderPartialTicks) / (float)target.maxHurtTime;
      if (hurtTimePercentage > 0.0F) {
         x += (double)(1.0F * hurtTimePercentage);
         y += (double)(1.0F * hurtTimePercentage);
         height -= (double)(2.0F * hurtTimePercentage);
         width -= (double)(2.0F * hurtTimePercentage);
      }

      Gui.drawScaledCustomSizeModalRect(x, y, 8.0F, 8.0F, 8, 8, width, height, 64.0F, 64.0F);
      if ((double)hurtTimePercentage > 0.0) {
         GL11.glTranslated(x, y, 0.0);
         GL11.glDisable(3553);
         boolean restore = glEnableBlend();
         GL11.glShadeModel(7425);
         GL11.glDisable(3008);
         float lineWidth = 10.0F;
         GL11.glLineWidth(10.0F);
         int fadeOutColour = ColourUtil.fadeTo(0, ColourUtil.blendHealthColours((double)(target.getHealth() / target.getMaxHealth())), (double)hurtTimePercentage);
         GL11.glBegin(7);
         glColour(fadeOutColour);
         GL11.glVertex2d(0.0, 0.0);
         GL11.glVertex2d(0.0, height);
         glColour(16711680);
         GL11.glVertex2d(10.0, height - 10.0);
         GL11.glVertex2d(10.0, 10.0);
         glColour(16711680);
         GL11.glVertex2d(width - 10.0, 10.0);
         GL11.glVertex2d(width - 10.0, height - 10.0);
         glColour(fadeOutColour);
         GL11.glVertex2d(width, height);
         GL11.glVertex2d(width, 0.0);
         glColour(fadeOutColour);
         GL11.glVertex2d(0.0, 0.0);
         glColour(16711680);
         GL11.glVertex2d(10.0, 10.0);
         GL11.glVertex2d(width - 10.0, 10.0);
         glColour(fadeOutColour);
         GL11.glVertex2d(width, 0.0);
         glColour(16711680);
         GL11.glVertex2d(10.0, height - 10.0);
         glColour(fadeOutColour);
         GL11.glVertex2d(0.0, height);
         GL11.glVertex2d(width, height);
         glColour(16711680);
         GL11.glVertex2d(width - 10.0, height - 10.0);
         GL11.glEnd();
         GL11.glEnable(3008);
         GL11.glShadeModel(7424);
         glRestoreBlend(restore);
         GL11.glEnable(3553);
         GL11.glTranslated(-x, -y, 0.0);
      }

   }

   public static void glDrawSidewaysGradientRect(double x, double y, double width, double height, int startColour, int endColour) {
      boolean restore = glEnableBlend();
      GL11.glDisable(3553);
      GL11.glShadeModel(7425);
      GL11.glBegin(7);
      glColour(startColour);
      GL11.glVertex2d(x, y);
      GL11.glVertex2d(x, y + height);
      glColour(endColour);
      GL11.glVertex2d(x + width, y + height);
      GL11.glVertex2d(x + width, y);
      GL11.glEnd();
      GL11.glShadeModel(7424);
      GL11.glEnable(3553);
      glRestoreBlend(restore);
   }

   public static void glDrawFilledRect(double x, double y, double x1, double y1, int startColour, int endColour) {
      boolean restore = glEnableBlend();
      GL11.glDisable(3553);
      GL11.glShadeModel(7425);
      GL11.glBegin(7);
      glColour(startColour);
      GL11.glVertex2d(x, y);
      glColour(endColour);
      GL11.glVertex2d(x, y1);
      GL11.glVertex2d(x1, y1);
      glColour(startColour);
      GL11.glVertex2d(x1, y);
      GL11.glEnd();
      GL11.glShadeModel(7424);
      GL11.glEnable(3553);
      glRestoreBlend(restore);
   }

   public static void glDrawOutlinedQuad(double x, double y, double width, double height, float thickness, int colour) {
      boolean restore = glEnableBlend();
      GL11.glDisable(3553);
      glColour(colour);
      GL11.glLineWidth(thickness);
      GL11.glBegin(2);
      GL11.glVertex2d(x, y);
      GL11.glVertex2d(x, y + height);
      GL11.glVertex2d(x + width, y + height);
      GL11.glVertex2d(x + width, y);
      GL11.glEnd();
      GL11.glEnable(3553);
      glRestoreBlend(restore);
   }

   public static void drawHollowRoundedRect(double x, double y, double width, double height, double cornerRadius, boolean smoothed, Color color) {
      GL11.glDisable(3553);
      GL11.glEnable(2848);
      GL11.glEnable(3042);
      GL11.glColor4f((float)color.getRed() / 255.0F, (float)color.getGreen() / 255.0F, (float)color.getBlue() / 255.0F, (float)color.getAlpha() / 255.0F);
      GL11.glLineWidth(1.0F);
      GL11.glBegin(2);
      double cornerX = x + width - cornerRadius;
      double cornerY = y + height - cornerRadius;

      int i;
      for(i = 0; i <= 90; i += 30) {
         GL11.glVertex2d(cornerX + Math.sin((double)i * Math.PI / 180.0) * cornerRadius, cornerY + Math.cos((double)i * Math.PI / 180.0) * cornerRadius);
      }

      GL11.glEnd();
      cornerX = x + width - cornerRadius;
      cornerY = y + cornerRadius;
      GL11.glBegin(2);

      for(i = 90; i <= 180; i += 30) {
         GL11.glVertex2d(cornerX + Math.sin((double)i * Math.PI / 180.0) * cornerRadius, cornerY + Math.cos((double)i * Math.PI / 180.0) * cornerRadius);
      }

      GL11.glEnd();
      cornerX = x + cornerRadius;
      cornerY = y + cornerRadius;
      GL11.glBegin(2);

      for(i = 180; i <= 270; i += 30) {
         GL11.glVertex2d(cornerX + Math.sin((double)i * Math.PI / 180.0) * cornerRadius, cornerY + Math.cos((double)i * Math.PI / 180.0) * cornerRadius);
      }

      GL11.glEnd();
      cornerX = x + cornerRadius;
      cornerY = y + height - cornerRadius;
      GL11.glBegin(2);

      for(i = 270; i <= 360; i += 30) {
         GL11.glVertex2d(cornerX + Math.sin((double)i * Math.PI / 180.0) * cornerRadius, cornerY + Math.cos((double)i * Math.PI / 180.0) * cornerRadius);
      }

      GL11.glEnd();
      GL11.glDisable(3042);
      GL11.glDisable(2848);
      GL11.glEnable(3553);
      glDrawLine(x + cornerRadius, y, x + width - cornerRadius, y, 1.0F, smoothed, color.getRGB());
      glDrawLine(x + cornerRadius, y + height, x + width - cornerRadius, y + height, 1.0F, smoothed, color.getRGB());
      glDrawLine(x, y + cornerRadius, x, y + height - cornerRadius, 1.0F, smoothed, color.getRGB());
      glDrawLine(x + width, y + cornerRadius, x + width, y + height - cornerRadius, 1.0F, smoothed, color.getRGB());
   }

   public static void glDrawOutlinedQuadGradient(double x, double y, double width, double height, float thickness, int colour, int secondaryColour) {
      boolean restore = glEnableBlend();
      GL11.glDisable(3553);
      GL11.glLineWidth(thickness);
      GL11.glShadeModel(7425);
      GL11.glBegin(2);
      glColour(colour);
      GL11.glVertex2d(x, y);
      GL11.glVertex2d(x, y + height);
      glColour(secondaryColour);
      GL11.glVertex2d(x + width, y + height);
      GL11.glVertex2d(x + width, y);
      GL11.glEnd();
      GL11.glShadeModel(7424);
      GL11.glEnable(3553);
      glRestoreBlend(restore);
   }

   public static void glDrawCheckmarkImage(double x, double y, double width, double height, int colour) {
      checkmarkImage.draw(x, y, width, height, colour);
   }

   public static void glDrawWarningImage(double x, double y, double width, double height, int colour) {
      warningImage.draw(x, y, width, height, colour);
   }

   public static void glDrawFilledQuad(double x, double y, double width, double height, int colour) {
      boolean restore = glEnableBlend();
      GL11.glDisable(3553);
      glColour(colour);
      GL11.glBegin(7);
      GL11.glVertex2d(x, y);
      GL11.glVertex2d(x, y + height);
      GL11.glVertex2d(x + width, y + height);
      GL11.glVertex2d(x + width, y);
      GL11.glEnd();
      glRestoreBlend(restore);
      GL11.glEnable(3553);
   }

   public static void glDrawFilledQuad(double x, double y, double width, double height, int startColour, int endColour) {
      boolean restore = glEnableBlend();
      GL11.glDisable(3553);
      GL11.glShadeModel(7425);
      GL11.glBegin(7);
      glColour(startColour);
      GL11.glVertex2d(x, y);
      glColour(endColour);
      GL11.glVertex2d(x, y + height);
      GL11.glVertex2d(x + width, y + height);
      glColour(startColour);
      GL11.glVertex2d(x + width, y);
      GL11.glEnd();
      GL11.glShadeModel(7424);
      glRestoreBlend(restore);
      GL11.glEnable(3553);
   }

   public static void glDrawFilledRect(double x, double y, double x1, double y1, int colour) {
      boolean restore = glEnableBlend();
      GL11.glDisable(3553);
      glColour(colour);
      GL11.glBegin(7);
      GL11.glVertex2d(x, y);
      GL11.glVertex2d(x, y1);
      GL11.glVertex2d(x1, y1);
      GL11.glVertex2d(x1, y);
      GL11.glEnd();
      glRestoreBlend(restore);
      GL11.glEnable(3553);
   }

   public static void glDrawArcFilled(double x, double y, float radius, float angleStart, float angleEnd, int segments, int colour) {
      boolean restore = glEnableBlend();
      GL11.glDisable(3553);
      glColour(colour);
      GL11.glDisable(2884);
      GL11.glTranslated(x, y, 0.0);
      GL11.glBegin(9);
      GL11.glVertex2f(0.0F, 0.0F);
      float[][] vertices = MathUtil.getArcVertices(radius, angleStart, angleEnd, segments);
      float[][] var11 = vertices;
      int var12 = vertices.length;

      for(int var13 = 0; var13 < var12; ++var13) {
         float[] vertex = var11[var13];
         GL11.glVertex2f(vertex[0], vertex[1]);
      }

      GL11.glEnd();
      GL11.glTranslated(-x, -y, 0.0);
      glRestoreBlend(restore);
      GL11.glEnable(2884);
      GL11.glEnable(3553);
   }

   public static void glDrawArcOutline(double x, double y, float radius, float angleStart, float angleEnd, float lineWidth, int colour) {
      int segments = (int)(radius * 4.0F);
      boolean restore = glEnableBlend();
      GL11.glDisable(3553);
      GL11.glLineWidth(lineWidth);
      glColour(colour);
      GL11.glEnable(2848);
      GL11.glHint(3154, 4354);
      GL11.glTranslated(x, y, 0.0);
      GL11.glBegin(3);
      float[][] vertices = MathUtil.getArcVertices(radius, angleStart, angleEnd, segments);
      float[][] var12 = vertices;
      int var13 = vertices.length;

      for(int var14 = 0; var14 < var13; ++var14) {
         float[] vertex = var12[var14];
         GL11.glVertex2f(vertex[0], vertex[1]);
      }

      GL11.glEnd();
      GL11.glTranslated(-x, -y, 0.0);
      GL11.glDisable(2848);
      GL11.glHint(3154, 4352);
      glRestoreBlend(restore);
      GL11.glEnable(3553);
   }

   public static void glDrawPoint(double x, double y, float radius, ScaledResolution scaledResolution, int colour) {
      boolean restore = glEnableBlend();
      GL11.glEnable(2832);
      GL11.glHint(3153, 4354);
      GL11.glDisable(3553);
      glColour(colour);
      GL11.glPointSize(radius * GL11.glGetFloat(2982) * (float)scaledResolution.getScaleFactor());
      GL11.glBegin(0);
      GL11.glVertex2d(x, y);
      GL11.glEnd();
      glRestoreBlend(restore);
      GL11.glDisable(2832);
      GL11.glHint(3153, 4352);
      GL11.glEnable(3553);
   }

   public static void glDrawRoundedOutline(double x, double y, double width, double height, float lineWidth, RoundingMode roundingMode, float rounding, int colour) {
      boolean bLeft = false;
      boolean tLeft = false;
      boolean bRight = false;
      boolean tRight = false;
      switch (roundingMode) {
         case TOP:
            tLeft = true;
            tRight = true;
            break;
         case BOTTOM:
            bLeft = true;
            bRight = true;
            break;
         case FULL:
            tLeft = true;
            tRight = true;
            bLeft = true;
            bRight = true;
            break;
         case LEFT:
            bLeft = true;
            tLeft = true;
            break;
         case RIGHT:
            bRight = true;
            tRight = true;
            break;
         case TOP_LEFT:
            tLeft = true;
            break;
         case TOP_RIGHT:
            tRight = true;
            break;
         case BOTTOM_LEFT:
            bLeft = true;
            break;
         case BOTTOM_RIGHT:
            bRight = true;
      }

      GL11.glTranslated(x, y, 0.0);
      boolean restore = glEnableBlend();
      if (tLeft) {
         glDrawArcOutline((double)rounding, (double)rounding, rounding, 270.0F, 360.0F, lineWidth, colour);
      }

      if (tRight) {
         glDrawArcOutline(width - (double)rounding, (double)rounding, rounding, 0.0F, 90.0F, lineWidth, colour);
      }

      if (bLeft) {
         glDrawArcOutline((double)rounding, height - (double)rounding, rounding, 180.0F, 270.0F, lineWidth, colour);
      }

      if (bRight) {
         glDrawArcOutline(width - (double)rounding, height - (double)rounding, rounding, 90.0F, 180.0F, lineWidth, colour);
      }

      GL11.glDisable(3553);
      glColour(colour);
      GL11.glBegin(1);
      if (tLeft) {
         GL11.glVertex2d(0.0, (double)rounding);
      } else {
         GL11.glVertex2d(0.0, 0.0);
      }

      if (bLeft) {
         GL11.glVertex2d(0.0, height - (double)rounding);
         GL11.glVertex2d((double)rounding, height);
      } else {
         GL11.glVertex2d(0.0, height);
         GL11.glVertex2d(0.0, height);
      }

      if (bRight) {
         GL11.glVertex2d(width - (double)rounding, height);
         GL11.glVertex2d(width, height - (double)rounding);
      } else {
         GL11.glVertex2d(width, height);
         GL11.glVertex2d(width, height);
      }

      if (tRight) {
         GL11.glVertex2d(width, (double)rounding);
         GL11.glVertex2d(width - (double)rounding, 0.0);
      } else {
         GL11.glVertex2d(width, 0.0);
         GL11.glVertex2d(width, 0.0);
      }

      if (tLeft) {
         GL11.glVertex2d((double)rounding, 0.0);
      } else {
         GL11.glVertex2d(0.0, 0.0);
      }

      GL11.glEnd();
      glRestoreBlend(restore);
      GL11.glTranslated(-x, -y, 0.0);
      GL11.glEnable(3553);
   }

   public static void glDrawSemiCircle(double x, double y, double diameter, float innerRadius, double percentage, int colour) {
      boolean restore = glEnableBlend();
      boolean alphaTest = GL11.glIsEnabled(3008);
      if (alphaTest) {
         GL11.glDisable(3008);
      }

      GL20.glUseProgram(CIRCLE_SHADER.getProgram());
      GL20.glUniform1f(CIRCLE_SHADER.getUniformLocation("innerRadius"), innerRadius);
      GL20.glUniform4f(CIRCLE_SHADER.getUniformLocation("colour"), (float)(colour >> 16 & 255) / 255.0F, (float)(colour >> 8 & 255) / 255.0F, (float)(colour & 255) / 255.0F, (float)(colour >> 24 & 255) / 255.0F);
      GL11.glBegin(7);
      GL11.glTexCoord2f(0.0F, 0.0F);
      GL11.glVertex2d(x, y);
      GL11.glTexCoord2f(0.0F, 1.0F);
      GL11.glVertex2d(x, y + diameter);
      GL11.glTexCoord2f(1.0F, 1.0F);
      GL11.glVertex2d(x + diameter, y + diameter);
      GL11.glTexCoord2f(1.0F, 0.0F);
      GL11.glVertex2d(x + diameter, y);
      GL11.glEnd();
      GL20.glUseProgram(0);
      if (alphaTest) {
         GL11.glEnable(3008);
      }

      glRestoreBlend(restore);
   }

   public static void glDrawRoundedQuad(double x, double y, float width, float height, float radius, int colour) {
      boolean restore = glEnableBlend();
      boolean alphaTest = GL11.glIsEnabled(3008);
      if (alphaTest) {
         GL11.glDisable(3008);
      }

      GL20.glUseProgram(ROUNDED_QUAD_SHADER.getProgram());
      GL20.glUniform1f(ROUNDED_QUAD_SHADER.getUniformLocation("width"), width);
      GL20.glUniform1f(ROUNDED_QUAD_SHADER.getUniformLocation("height"), height);
      GL20.glUniform1f(ROUNDED_QUAD_SHADER.getUniformLocation("radius"), radius);
      GL20.glUniform4f(ROUNDED_QUAD_SHADER.getUniformLocation("colour"), (float)(colour >> 16 & 255) / 255.0F, (float)(colour >> 8 & 255) / 255.0F, (float)(colour & 255) / 255.0F, (float)(colour >> 24 & 255) / 255.0F);
      GL11.glDisable(3553);
      GL11.glBegin(7);
      GL11.glTexCoord2f(0.0F, 0.0F);
      GL11.glVertex2d(x, y);
      GL11.glTexCoord2f(0.0F, 1.0F);
      GL11.glVertex2d(x, y + (double)height);
      GL11.glTexCoord2f(1.0F, 1.0F);
      GL11.glVertex2d(x + (double)width, y + (double)height);
      GL11.glTexCoord2f(1.0F, 0.0F);
      GL11.glVertex2d(x + (double)width, y);
      GL11.glEnd();
      GL20.glUseProgram(0);
      GL11.glEnable(3553);
      if (alphaTest) {
         GL11.glEnable(3008);
      }

      glRestoreBlend(restore);
   }

   public static void glDrawRoundedQuadRainbow(double x, double y, float width, float height, float radius) {
      boolean restore = glEnableBlend();
      boolean alphaTest = GL11.glIsEnabled(3008);
      if (alphaTest) {
         GL11.glDisable(3008);
      }

      GL_COLOUR_SHADER.use();
      GL20.glUniform1f(GL_COLOUR_SHADER.getUniformLocation("width"), width);
      GL20.glUniform1f(GL_COLOUR_SHADER.getUniformLocation("height"), height);
      GL20.glUniform1f(GL_COLOUR_SHADER.getUniformLocation("radius"), radius);
      GL11.glDisable(3553);
      GL11.glBegin(7);
      GL11.glTexCoord2f(0.0F, 0.0F);
      GL11.glVertex2d(x, y);
      GL11.glTexCoord2f(0.0F, 1.0F);
      GL11.glVertex2d(x, y + (double)height);
      GL11.glTexCoord2f(1.0F, 1.0F);
      GL11.glVertex2d(x + (double)width, y + (double)height);
      GL11.glTexCoord2f(1.0F, 0.0F);
      GL11.glVertex2d(x + (double)width, y);
      GL11.glEnd();
      GL20.glUseProgram(0);
      GL11.glEnable(3553);
      if (alphaTest) {
         GL11.glEnable(3008);
      }

      glRestoreBlend(restore);
   }

   public static void glDrawRoundedRect(double x, double y, double width, double height, RoundingMode roundingMode, float rounding, float scaleFactor, int colour) {
      boolean bLeft = false;
      boolean tLeft = false;
      boolean bRight = false;
      boolean tRight = false;
      switch (roundingMode) {
         case TOP:
            tLeft = true;
            tRight = true;
            break;
         case BOTTOM:
            bLeft = true;
            bRight = true;
            break;
         case FULL:
            tLeft = true;
            tRight = true;
            bLeft = true;
            bRight = true;
            break;
         case LEFT:
            bLeft = true;
            tLeft = true;
            break;
         case RIGHT:
            bRight = true;
            tRight = true;
            break;
         case TOP_LEFT:
            tLeft = true;
            break;
         case TOP_RIGHT:
            tRight = true;
            break;
         case BOTTOM_LEFT:
            bLeft = true;
            break;
         case BOTTOM_RIGHT:
            bRight = true;
      }

      float alpha = (float)(colour >> 24 & 255) / 255.0F;
      boolean restore = glEnableBlend();
      glColour(colour);
      GL11.glTranslated(x, y, 0.0);
      GL11.glDisable(3553);
      GL11.glBegin(9);
      if (tLeft) {
         GL11.glVertex2d((double)rounding, (double)rounding);
         GL11.glVertex2d(0.0, (double)rounding);
      } else {
         GL11.glVertex2d(0.0, 0.0);
      }

      if (bLeft) {
         GL11.glVertex2d(0.0, height - (double)rounding);
         GL11.glVertex2d((double)rounding, height - (double)rounding);
         GL11.glVertex2d((double)rounding, height);
      } else {
         GL11.glVertex2d(0.0, height);
      }

      if (bRight) {
         GL11.glVertex2d(width - (double)rounding, height);
         GL11.glVertex2d(width - (double)rounding, height - (double)rounding);
         GL11.glVertex2d(width, height - (double)rounding);
      } else {
         GL11.glVertex2d(width, height);
      }

      if (tRight) {
         GL11.glVertex2d(width, (double)rounding);
         GL11.glVertex2d(width - (double)rounding, (double)rounding);
         GL11.glVertex2d(width - (double)rounding, 0.0);
      } else {
         GL11.glVertex2d(width, 0.0);
      }

      if (tLeft) {
         GL11.glVertex2d((double)rounding, 0.0);
      }

      GL11.glEnd();
      GL11.glEnable(2832);
      GL11.glHint(3153, 4354);
      GL11.glPointSize(rounding * 2.0F * GL11.glGetFloat(2982) * scaleFactor);
      GL11.glBegin(0);
      if (tLeft) {
         GL11.glVertex2d((double)rounding, (double)rounding);
      }

      if (tRight) {
         GL11.glVertex2d(width - (double)rounding, (double)rounding);
      }

      if (bLeft) {
         GL11.glVertex2d((double)rounding, height - (double)rounding);
      }

      if (bRight) {
         GL11.glVertex2d(width - (double)rounding, height - (double)rounding);
      }

      GL11.glEnd();
      GL11.glDisable(2832);
      GL11.glHint(3153, 4352);
      glRestoreBlend(restore);
      GL11.glTranslated(-x, -y, 0.0);
      GL11.glEnable(3553);
   }

   public static void glDrawRoundedRectEllipse(double x, double y, double width, double height, RoundingMode roundingMode, int roundingDef, double roundingLevel, int colour) {
      boolean bLeft = false;
      boolean tLeft = false;
      boolean bRight = false;
      boolean tRight = false;
      switch (roundingMode) {
         case TOP:
            tLeft = true;
            tRight = true;
            break;
         case BOTTOM:
            bLeft = true;
            bRight = true;
            break;
         case FULL:
            tLeft = true;
            tRight = true;
            bLeft = true;
            bRight = true;
            break;
         case LEFT:
            bLeft = true;
            tLeft = true;
            break;
         case RIGHT:
            bRight = true;
            tRight = true;
            break;
         case TOP_LEFT:
            tLeft = true;
            break;
         case TOP_RIGHT:
            tRight = true;
            break;
         case BOTTOM_LEFT:
            bLeft = true;
            break;
         case BOTTOM_RIGHT:
            bRight = true;
      }

      GL11.glTranslated(x, y, 0.0);
      GL11.glEnable(2881);
      GL11.glHint(3154, 4354);
      boolean restore = glEnableBlend();
      if (tLeft) {
         glDrawFilledEllipse(roundingLevel, roundingLevel, roundingLevel, (int)((double)roundingDef * 0.5), (int)((double)roundingDef * 0.75), roundingDef, false, colour);
      }

      if (tRight) {
         glDrawFilledEllipse(width - roundingLevel, roundingLevel, roundingLevel, (int)((double)roundingDef * 0.75), roundingDef, roundingDef, false, colour);
      }

      if (bLeft) {
         glDrawFilledEllipse(roundingLevel, height - roundingLevel, roundingLevel, (int)((double)roundingDef * 0.25), (int)((double)roundingDef * 0.5), roundingDef, false, colour);
      }

      if (bRight) {
         glDrawFilledEllipse(width - roundingLevel, height - roundingLevel, roundingLevel, 0, (int)((double)roundingDef * 0.25), roundingDef, false, colour);
      }

      GL11.glDisable(2881);
      GL11.glHint(3154, 4352);
      GL11.glDisable(3553);
      glColour(colour);
      GL11.glBegin(9);
      if (tLeft) {
         GL11.glVertex2d(roundingLevel, roundingLevel);
         GL11.glVertex2d(0.0, roundingLevel);
      } else {
         GL11.glVertex2d(0.0, 0.0);
      }

      if (bLeft) {
         GL11.glVertex2d(0.0, height - roundingLevel);
         GL11.glVertex2d(roundingLevel, height - roundingLevel);
         GL11.glVertex2d(roundingLevel, height);
      } else {
         GL11.glVertex2d(0.0, height);
      }

      if (bRight) {
         GL11.glVertex2d(width - roundingLevel, height);
         GL11.glVertex2d(width - roundingLevel, height - roundingLevel);
         GL11.glVertex2d(width, height - roundingLevel);
      } else {
         GL11.glVertex2d(width, height);
      }

      if (tRight) {
         GL11.glVertex2d(width, roundingLevel);
         GL11.glVertex2d(width - roundingLevel, roundingLevel);
         GL11.glVertex2d(width - roundingLevel, 0.0);
      } else {
         GL11.glVertex2d(width, 0.0);
      }

      if (tLeft) {
         GL11.glVertex2d(roundingLevel, 0.0);
      }

      GL11.glEnd();
      glRestoreBlend(restore);
      GL11.glTranslated(-x, -y, 0.0);
      GL11.glEnable(3553);
   }

   public static boolean glEnableBlend() {
      boolean wasEnabled = GL11.glIsEnabled(3042);
      if (!wasEnabled) {
         GL11.glEnable(3042);
         GL14.glBlendFuncSeparate(770, 771, 1, 0);
      }

      return wasEnabled;
   }

   public static void glRestoreBlend(boolean wasEnabled) {
      if (!wasEnabled) {
         GL11.glDisable(3042);
      }

   }

   public static float interpolate(float old, float now, float progress) {
      return old + (now - old) * progress;
   }

   public static double interpolate(double old, double now, double progress) {
      return old + (now - old) * progress;
   }

   public static Vec3 interpolate(Vec3 old, Vec3 now, double progress) {
      Vec3 difVec = now.subtract(old);
      return new Vec3(old.xCoord + difVec.xCoord * progress, old.yCoord + difVec.yCoord * progress, old.zCoord + difVec.zCoord * progress);
   }

   public static double[] interpolate(Entity entity, float partialTicks) {
      return new double[]{interpolate(entity.prevPosX, entity.posX, (double)partialTicks), interpolate(entity.prevPosY, entity.posY, (double)partialTicks), interpolate(entity.prevPosZ, entity.posZ, (double)partialTicks)};
   }

   public static AxisAlignedBB interpolate(Entity entity, AxisAlignedBB boundingBox, float partialTicks) {
      float invertedPT = 1.0F - partialTicks;
      return boundingBox.offset((entity.posX - entity.prevPosX) * (double)(-invertedPT), (entity.posY - entity.prevPosY) * (double)(-invertedPT), (entity.posZ - entity.prevPosZ) * (double)(-invertedPT));
   }

   public static void glDrawBoundingBox(AxisAlignedBB bb, float lineWidth, boolean filled) {
      if (filled) {
         GL11.glBegin(8);
         GL11.glVertex3d(bb.minX, bb.minY, bb.minZ);
         GL11.glVertex3d(bb.minX, bb.maxY, bb.minZ);
         GL11.glVertex3d(bb.maxX, bb.minY, bb.minZ);
         GL11.glVertex3d(bb.maxX, bb.maxY, bb.minZ);
         GL11.glVertex3d(bb.maxX, bb.minY, bb.maxZ);
         GL11.glVertex3d(bb.maxX, bb.maxY, bb.maxZ);
         GL11.glVertex3d(bb.minX, bb.minY, bb.maxZ);
         GL11.glVertex3d(bb.minX, bb.maxY, bb.maxZ);
         GL11.glVertex3d(bb.minX, bb.minY, bb.minZ);
         GL11.glVertex3d(bb.minX, bb.maxY, bb.minZ);
         GL11.glEnd();
         GL11.glBegin(7);
         GL11.glVertex3d(bb.minX, bb.minY, bb.minZ);
         GL11.glVertex3d(bb.maxX, bb.minY, bb.minZ);
         GL11.glVertex3d(bb.maxX, bb.minY, bb.maxZ);
         GL11.glVertex3d(bb.minX, bb.minY, bb.maxZ);
         GL11.glEnd();
         GL11.glCullFace(1028);
         GL11.glBegin(7);
         GL11.glVertex3d(bb.minX, bb.maxY, bb.minZ);
         GL11.glVertex3d(bb.maxX, bb.maxY, bb.minZ);
         GL11.glVertex3d(bb.maxX, bb.maxY, bb.maxZ);
         GL11.glVertex3d(bb.minX, bb.maxY, bb.maxZ);
         GL11.glEnd();
         GL11.glCullFace(1029);
      }

      if (lineWidth > 0.0F) {
         GL11.glLineWidth(lineWidth);
         GL11.glEnable(2848);
         GL11.glHint(3154, 4354);
         GL11.glBegin(3);
         GL11.glVertex3d(bb.minX, bb.minY, bb.minZ);
         GL11.glVertex3d(bb.maxX, bb.minY, bb.minZ);
         GL11.glVertex3d(bb.maxX, bb.minY, bb.maxZ);
         GL11.glVertex3d(bb.minX, bb.minY, bb.maxZ);
         GL11.glVertex3d(bb.minX, bb.minY, bb.minZ);
         GL11.glVertex3d(bb.minX, bb.maxY, bb.minZ);
         GL11.glVertex3d(bb.maxX, bb.maxY, bb.minZ);
         GL11.glVertex3d(bb.maxX, bb.maxY, bb.maxZ);
         GL11.glVertex3d(bb.minX, bb.maxY, bb.maxZ);
         GL11.glVertex3d(bb.minX, bb.maxY, bb.minZ);
         GL11.glEnd();
         GL11.glBegin(1);
         GL11.glVertex3d(bb.maxX, bb.minY, bb.minZ);
         GL11.glVertex3d(bb.maxX, bb.maxY, bb.minZ);
         GL11.glVertex3d(bb.maxX, bb.minY, bb.maxZ);
         GL11.glVertex3d(bb.maxX, bb.maxY, bb.maxZ);
         GL11.glVertex3d(bb.minX, bb.minY, bb.maxZ);
         GL11.glVertex3d(bb.minX, bb.maxY, bb.maxZ);
         GL11.glEnd();
         GL11.glDisable(2848);
         GL11.glHint(3154, 4352);
      }

   }

   static {
      try {
         checkmarkImage = new StaticallySizedImage(ImageIO.read(ResourceUtil.getResourceStream("icons/notifications/success.png")), true, 3);
         warningImage = new StaticallySizedImage(ImageIO.read(ResourceUtil.getResourceStream("icons/notifications/warning.png")), true, 3);
      } catch (IOException var1) {
         checkmarkImage = null;
         warningImage = null;
      }

      CIRCLE_SHADER = new GLShader("#version 120 \n\nvoid main() {\n    gl_TexCoord[0] = gl_MultiTexCoord0;\n    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;\n}", "#version 120\n\nuniform float innerRadius;\nuniform vec4 colour;\n\nvoid main() {\n   vec2 pixel = gl_TexCoord[0].st;\n   vec2 centre = vec2(0.5, 0.5);\n   float d = length(pixel - centre);\n   float c = smoothstep(d+innerRadius, d+innerRadius+0.01, 0.5-innerRadius);\n   float a = smoothstep(0.0, 1.0, c) * colour.a;\n   gl_FragColor = vec4(colour.rgb, a);\n}\n") {
         public void setupUniforms() {
            this.setupUniform("colour");
            this.setupUniform("innerRadius");
         }
      };
      ROUNDED_QUAD_SHADER = new GLShader("#version 120 \n\nvoid main() {\n    gl_TexCoord[0] = gl_MultiTexCoord0;\n    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;\n}", "#version 120\nuniform float width;\nuniform float height;\nuniform float radius;\nuniform vec4 colour;\n\nfloat SDRoundedRect(vec2 p, vec2 b, float r) {\n    vec2 q = abs(p) - b + r;\n    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r;\n}\n\nvoid main() {\n    vec2 size = vec2(width, height);\n    vec2 pixel = gl_TexCoord[0].st * size;\n    vec2 centre = 0.5 * size;\n    float b = SDRoundedRect(pixel - centre, centre, radius);\n    float a = 1.0 - smoothstep(0, 1.0, b);\n    gl_FragColor = vec4(colour.rgb, colour.a * a);\n}") {
         public void setupUniforms() {
            this.setupUniform("width");
            this.setupUniform("height");
            this.setupUniform("colour");
            this.setupUniform("radius");
         }
      };
      GL_COLOUR_SHADER = new GLShader("#version 120 \n\nvoid main() {\n    gl_TexCoord[0] = gl_MultiTexCoord0;\n    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;\n}", "#version 120\nuniform float width;\nuniform float height;\nuniform float radius;\nuniform float u_time;\n\nfloat SDRoundedRect(vec2 p, vec2 b, float r) {\n    vec2 q = abs(p) - b + r;\n    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r;\n}\n\nvoid main() {\n    vec2 size = vec2(width, height);\n    vec2 pixel = gl_TexCoord[0].st * size;\n    vec2 centre = 0.5 * size;\n    float b = SDRoundedRect(pixel - centre, centre, radius);\n    float a = 1.0 - smoothstep(0, 1.0, b);\n    vec3 colour = 0.5 + 0.5*cos(u_time+gl_TexCoord[0].st.x+vec3(0,2,4));\n    gl_FragColor = vec4(colour, a);\n}") {
         private final long initTime = System.currentTimeMillis();

         public void setupUniforms() {
            this.setupUniform("width");
            this.setupUniform("height");
            this.setupUniform("radius");
            this.setupUniform("u_time");
         }

         public void updateUniforms() {
            GL20.glUniform1f(GL20.glGetUniformLocation(this.getProgram(), "u_time"), (float)(System.currentTimeMillis() - this.initTime) / 1000.0F);
         }
      };
   }

   public static enum RoundingMode {
      TOP_LEFT,
      BOTTOM_LEFT,
      TOP_RIGHT,
      BOTTOM_RIGHT,
      LEFT,
      RIGHT,
      TOP,
      BOTTOM,
      FULL;
   }
}
