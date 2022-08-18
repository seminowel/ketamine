package io.github.nevalackin.client.util.render;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.ContextCapabilities;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GLContext;

public final class BlurUtil {
   private static final String BLUR_FRAG_SHADER = "#version 120\n\nuniform sampler2D texture;\nuniform sampler2D texture2;\nuniform vec2 texelSize;\nuniform vec2 direction;\nuniform float radius;\nuniform float weights[256];\n\nvoid main() {\n    vec4 color = vec4(0.0);\n    vec2 texCoord = gl_TexCoord[0].st;\n    if (direction.y == 0)\n        if (texture2D(texture2, texCoord).a == 0.0) return;\n    for (float f = -radius; f <= radius; f++) {\n        color += texture2D(texture, texCoord + f * texelSize * direction) * (weights[int(abs(f))]);\n    }\n    gl_FragColor = vec4(color.rgb, 1.0);\n}";
   public static final String VERTEX_SHADER = "#version 120 \n\nvoid main() {\n    gl_TexCoord[0] = gl_MultiTexCoord0;\n    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;\n}";
   private static final GLShader blurShader = new GLShader("#version 120 \n\nvoid main() {\n    gl_TexCoord[0] = gl_MultiTexCoord0;\n    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;\n}", "#version 120\n\nuniform sampler2D texture;\nuniform sampler2D texture2;\nuniform vec2 texelSize;\nuniform vec2 direction;\nuniform float radius;\nuniform float weights[256];\n\nvoid main() {\n    vec4 color = vec4(0.0);\n    vec2 texCoord = gl_TexCoord[0].st;\n    if (direction.y == 0)\n        if (texture2D(texture2, texCoord).a == 0.0) return;\n    for (float f = -radius; f <= radius; f++) {\n        color += texture2D(texture, texCoord + f * texelSize * direction) * (weights[int(abs(f))]);\n    }\n    gl_FragColor = vec4(color.rgb, 1.0);\n}") {
      public void setupUniforms() {
         this.setupUniform("texture");
         this.setupUniform("texture2");
         this.setupUniform("texelSize");
         this.setupUniform("radius");
         this.setupUniform("direction");
         this.setupUniform("weights");
      }

      public void updateUniforms() {
         float radius = 20.0F;
         GL20.glUniform1i(this.getUniformLocation("texture"), 0);
         GL20.glUniform1i(this.getUniformLocation("texture2"), 20);
         GL20.glUniform1f(this.getUniformLocation("radius"), 20.0F);
         FloatBuffer buffer = BufferUtils.createFloatBuffer(256);
         float blurRadius = 10.0F;

         for(int i = 0; (float)i <= 10.0F; ++i) {
            buffer.put(BlurUtil.calculateGaussianOffset((float)i, 5.0F));
         }

         buffer.rewind();
         GL20.glUniform1(this.getUniformLocation("weights"), buffer);
         GL20.glUniform2f(this.getUniformLocation("texelSize"), 1.0F / (float)Display.getWidth(), 1.0F / (float)Display.getHeight());
      }
   };
   private static Framebuffer framebuffer;
   private static Framebuffer framebufferRender;
   public static boolean disableBlur;
   private static List blurAreas = new ArrayList();

   private BlurUtil() {
   }

   public static void blurArea(double x, double y, double width, double height) {
      ContextCapabilities cc = GLContext.getCapabilities();
      if (!disableBlur && cc.OpenGL20) {
         blurAreas.add(new double[]{x, y, width, height});
      }
   }

   public static void onRenderGameOverlay(Framebuffer mcFramebuffer, ScaledResolution sr) {
      if (framebuffer != null && framebufferRender != null && !blurAreas.isEmpty()) {
         framebufferRender.framebufferClear();
         framebufferRender.bindFramebuffer(false);
         Iterator var2 = blurAreas.iterator();

         while(var2.hasNext()) {
            double[] area = (double[])var2.next();
            DrawUtil.glDrawFilledQuad(area[0], area[1], area[2], area[3], -16777216);
         }

         blurAreas.clear();
         boolean restore = DrawUtil.glEnableBlend();
         framebuffer.bindFramebuffer(false);
         blurShader.use();
         onPass(1);
         glDrawFramebuffer(sr, mcFramebuffer);
         GL20.glUseProgram(0);
         mcFramebuffer.bindFramebuffer(false);
         blurShader.use();
         onPass(0);
         GL13.glActiveTexture(34004);
         GL11.glBindTexture(3553, framebufferRender.framebufferTexture);
         GL13.glActiveTexture(33984);
         glDrawFramebuffer(sr, framebuffer);
         GL20.glUseProgram(0);
         DrawUtil.glRestoreBlend(restore);
      }
   }

   private static void onPass(int pass) {
      GL20.glUniform2f(blurShader.getUniformLocation("direction"), (float)(1 - pass), (float)pass);
   }

   private static void glDrawFramebuffer(ScaledResolution scaledResolution, Framebuffer framebuffer) {
      GL11.glBindTexture(3553, framebuffer.framebufferTexture);
      GL11.glBegin(7);
      GL11.glTexCoord2f(0.0F, 1.0F);
      GL11.glVertex2i(0, 0);
      GL11.glTexCoord2f(0.0F, 0.0F);
      GL11.glVertex2i(0, scaledResolution.getScaledHeight());
      GL11.glTexCoord2f(1.0F, 0.0F);
      GL11.glVertex2i(scaledResolution.getScaledWidth(), scaledResolution.getScaledHeight());
      GL11.glTexCoord2f(1.0F, 1.0F);
      GL11.glVertex2i(scaledResolution.getScaledWidth(), 0);
      GL11.glEnd();
   }

   public static void onFrameBufferResize(int width, int height) {
      if (framebuffer != null) {
         framebuffer.deleteFramebuffer();
      }

      if (framebufferRender != null) {
         framebufferRender.deleteFramebuffer();
      }

      framebuffer = new Framebuffer(width, height, false);
      framebufferRender = new Framebuffer(width, height, false);
   }

   static float calculateGaussianOffset(float x, float sigma) {
      float pow = x / sigma;
      return (float)(1.0 / ((double)Math.abs(sigma) * 2.50662827463) * Math.exp(-0.5 * (double)pow * (double)pow));
   }
}
