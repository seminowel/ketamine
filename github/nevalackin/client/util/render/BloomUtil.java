package io.github.nevalackin.client.util.render;

import io.github.nevalackin.client.impl.event.render.RenderCallback;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.ContextCapabilities;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GLContext;

public class BloomUtil {
   private static final String OUTLINE_SHADER = "#version 120\n\nuniform sampler2D texture;\nuniform vec2 texelSize;\n\nuniform vec4 colour;\nuniform float radius;\n\nvoid main() {\n    float a = 0.0;\n    vec3 rgb = colour.rgb;\n    float closest = 1.0;\n    for (float x = -radius; x <= radius; x++) {\n        for (float y = -radius; y <= radius; y++) {\n            vec2 st = gl_TexCoord[0].st + vec2(x, y) * texelSize;\n            vec4 smpl = texture2D(texture, st);\n            float dist = distance(st, gl_TexCoord[0].st);\n            if (smpl.a > 0.0 && dist < closest) {               rgb = smpl.rgb;\n               closest = dist;\n            }\n            a += smpl.a*smpl.a;\n        }\n    }\n    vec4 smpl = texture2D(texture, gl_TexCoord[0].st);\n    gl_FragColor = vec4(rgb, a * colour.a / (4.0 * radius * radius)) * (smpl.a > 0.0 ? 0.0 : 1.0);\n}\n";
   private static final String VERTEX_SHADER = "#version 120 \n\nvoid main() {\n    gl_TexCoord[0] = gl_MultiTexCoord0;\n    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;\n}";
   private static final GLShader shader = new GLShader("#version 120 \n\nvoid main() {\n    gl_TexCoord[0] = gl_MultiTexCoord0;\n    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;\n}", "#version 120\n\nuniform sampler2D texture;\nuniform vec2 texelSize;\n\nuniform vec4 colour;\nuniform float radius;\n\nvoid main() {\n    float a = 0.0;\n    vec3 rgb = colour.rgb;\n    float closest = 1.0;\n    for (float x = -radius; x <= radius; x++) {\n        for (float y = -radius; y <= radius; y++) {\n            vec2 st = gl_TexCoord[0].st + vec2(x, y) * texelSize;\n            vec4 smpl = texture2D(texture, st);\n            float dist = distance(st, gl_TexCoord[0].st);\n            if (smpl.a > 0.0 && dist < closest) {               rgb = smpl.rgb;\n               closest = dist;\n            }\n            a += smpl.a*smpl.a;\n        }\n    }\n    vec4 smpl = texture2D(texture, gl_TexCoord[0].st);\n    gl_FragColor = vec4(rgb, a * colour.a / (4.0 * radius * radius)) * (smpl.a > 0.0 ? 0.0 : 1.0);\n}\n") {
      public void setupUniforms() {
         this.setupUniform("texture");
         this.setupUniform("texelSize");
         this.setupUniform("colour");
         this.setupUniform("radius");
         GL20.glUniform1i(this.getUniformLocation("texture"), 0);
      }

      public void updateUniforms() {
         GL20.glUniform4f(this.getUniformLocation("colour"), 1.0F, 1.0F, 1.0F, 1.0F);
         GL20.glUniform2f(this.getUniformLocation("texelSize"), 1.0F / (float)Minecraft.getMinecraft().displayWidth, 1.0F / (float)Minecraft.getMinecraft().displayHeight);
         GL20.glUniform1f(this.getUniformLocation("radius"), 5.0F);
      }
   };
   private static Framebuffer framebuffer;
   public static boolean disableBloom;
   private static List renders = new ArrayList();

   private BloomUtil() {
   }

   public static void bloom(RenderCallback render) {
      ContextCapabilities cc = GLContext.getCapabilities();
      if (!disableBloom && cc.OpenGL20) {
         renders.add(render);
      }
   }

   public static void drawAndBloom(RenderCallback render) {
      render.render();
      if (!disableBloom) {
         renders.add(render);
      }
   }

   public static void onRenderGameOverlay(ScaledResolution scaledResolution, Framebuffer mcFramebuffer) {
      if (framebuffer != null) {
         framebuffer.bindFramebuffer(false);
         Iterator var2 = renders.iterator();

         while(var2.hasNext()) {
            RenderCallback callback = (RenderCallback)var2.next();
            callback.render();
         }

         renders.clear();
         mcFramebuffer.bindFramebuffer(false);
         shader.use();
         DrawUtil.glDrawFramebuffer(framebuffer.framebufferTexture, scaledResolution.getScaledWidth(), scaledResolution.getScaledHeight());
         GL20.glUseProgram(0);
         framebuffer.framebufferClear();
         mcFramebuffer.bindFramebuffer(false);
      }
   }

   public static void onFrameBufferResize(int width, int height) {
      if (framebuffer != null) {
         framebuffer.deleteFramebuffer();
      }

      framebuffer = new Framebuffer(width, height, false);
   }
}
