package io.github.nevalackin.client.impl.module.render.esp;

import io.github.nevalackin.client.api.module.Category;
import io.github.nevalackin.client.api.module.Module;
import io.github.nevalackin.client.api.property.Property;
import io.github.nevalackin.client.impl.property.BooleanProperty;
import io.github.nevalackin.client.impl.property.ColourProperty;
import io.github.nevalackin.client.impl.property.DoubleProperty;
import io.github.nevalackin.client.impl.property.EnumProperty;
import io.github.nevalackin.client.util.player.TeamsUtil;
import io.github.nevalackin.client.util.render.ColourUtil;
import io.github.nevalackin.client.util.render.DrawUtil;
import io.github.nevalackin.client.util.render.GLShader;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.awt.Color;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL20;

public final class Glow extends Module {
   private static final String OUTLINE_SHADER = "#version 120\n\nuniform sampler2D texture;\nuniform vec2 texelSize;\n\nuniform vec4 colour;\nuniform float radius;\n\nvoid main() {\n    float a = 0.0;\n    vec3 rgb = colour.rgb;\n    float closest = 1.0;\n    for (float x = -radius; x <= radius; x++) {\n        for (float y = -radius; y <= radius; y++) {\n            vec2 st = gl_TexCoord[0].st + vec2(x, y) * texelSize;\n            vec4 smpl = texture2D(texture, st);\n            float dist = distance(st, gl_TexCoord[0].st);\n            if (smpl.a > 0.0 && dist < closest) {               rgb = smpl.rgb;\n               closest = dist;\n            }\n            a += smpl.a*smpl.a;\n        }\n    }\n    vec4 smpl = texture2D(texture, gl_TexCoord[0].st);\n    gl_FragColor = vec4(rgb, a * colour.a / (4.0 * radius * radius)) * (smpl.a > 0.0 ? 0.0 : 1.0);\n}\n";
   public static final String VERTEX_SHADER = "#version 120 \n\nvoid main() {\n    gl_TexCoord[0] = gl_MultiTexCoord0;\n    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;\n}";
   private static final String FILL_SHADER = "#version 120\n\nuniform sampler2D texture;\nuniform vec4 colour;\n\nvoid main() {\n    vec4 smpl = texture2D(texture, gl_TexCoord[0].st);\n    gl_FragColor = vec4(colour.rgb, smpl.a > 0.0 ? colour.a : 0.0);\n}\n";
   private final GLShader shader = new GLShader("#version 120 \n\nvoid main() {\n    gl_TexCoord[0] = gl_MultiTexCoord0;\n    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;\n}", "#version 120\n\nuniform sampler2D texture;\nuniform vec2 texelSize;\n\nuniform vec4 colour;\nuniform float radius;\n\nvoid main() {\n    float a = 0.0;\n    vec3 rgb = colour.rgb;\n    float closest = 1.0;\n    for (float x = -radius; x <= radius; x++) {\n        for (float y = -radius; y <= radius; y++) {\n            vec2 st = gl_TexCoord[0].st + vec2(x, y) * texelSize;\n            vec4 smpl = texture2D(texture, st);\n            float dist = distance(st, gl_TexCoord[0].st);\n            if (smpl.a > 0.0 && dist < closest) {               rgb = smpl.rgb;\n               closest = dist;\n            }\n            a += smpl.a*smpl.a;\n        }\n    }\n    vec4 smpl = texture2D(texture, gl_TexCoord[0].st);\n    gl_FragColor = vec4(rgb, a * colour.a / (4.0 * radius * radius)) * (smpl.a > 0.0 ? 0.0 : 1.0);\n}\n") {
      public void setupUniforms() {
         this.setupUniform("texture");
         this.setupUniform("texelSize");
         this.setupUniform("colour");
         this.setupUniform("radius");
         GL20.glUniform1i(this.getUniformLocation("texture"), 0);
      }

      public void updateUniforms() {
         Color colour = Glow.this.colourProperty.getColour();
         GL20.glUniform4f(this.getUniformLocation("colour"), (float)colour.getRed() / 255.0F, (float)colour.getGreen() / 255.0F, (float)colour.getBlue() / 255.0F, (Boolean)Glow.this.breathingProperty.getValue() ? ColourUtil.getBreathingProgress() * ((float)colour.getAlpha() / 255.0F) : (float)colour.getAlpha() / 255.0F);
         GL20.glUniform2f(this.getUniformLocation("texelSize"), 1.0F / (float)Glow.this.mc.displayWidth, 1.0F / (float)Glow.this.mc.displayHeight);
         GL20.glUniform1f(this.getUniformLocation("radius"), ((Double)Glow.this.radiusProperty.getValue()).floatValue());
      }
   };
   private final GLShader fillShader = new GLShader("#version 120 \n\nvoid main() {\n    gl_TexCoord[0] = gl_MultiTexCoord0;\n    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;\n}", "#version 120\n\nuniform sampler2D texture;\nuniform vec4 colour;\n\nvoid main() {\n    vec4 smpl = texture2D(texture, gl_TexCoord[0].st);\n    gl_FragColor = vec4(colour.rgb, smpl.a > 0.0 ? colour.a : 0.0);\n}\n") {
      public void setupUniforms() {
         this.setupUniform("texture");
         this.setupUniform("colour");
         GL20.glUniform1i(this.getUniformLocation("texture"), 0);
      }
   };
   private Framebuffer framebuffer;
   private final EnumProperty colourModeProperty;
   private final ColourProperty colourProperty;
   private final ColourProperty enemyColourProperty;
   private final ColourProperty friendlyColourProperty;
   private final DoubleProperty radiusProperty;
   private final BooleanProperty chestsProperty;
   private final BooleanProperty breathingProperty;
   @EventLink
   private final Listener onFramebufferResize;
   @EventLink
   private final Listener onRenderChestModel;
   private final float[] colourBuffer;
   @EventLink(0)
   private final Listener onModelRender;
   @EventLink
   private final Listener onRenderGameOverlay;

   public Glow() {
      super("Glow", Category.RENDER, Category.SubCategory.RENDER_ESP);
      this.colourModeProperty = new EnumProperty("Colour Mode", Glow.ColourMode.TEAMS);
      this.colourProperty = new ColourProperty("Colour", ColourUtil.getClientColour(), () -> {
         return this.colourModeProperty.getValue() == Glow.ColourMode.STATIC;
      });
      this.enemyColourProperty = new ColourProperty("Enemy Colour", -65536, () -> {
         return this.colourModeProperty.getValue() == Glow.ColourMode.TEAMS;
      });
      this.friendlyColourProperty = new ColourProperty("Friendly Colour", -16711831, () -> {
         return this.colourModeProperty.getValue() == Glow.ColourMode.TEAMS;
      });
      this.radiusProperty = new DoubleProperty("Radius", 3.0, 1.0, 10.0, 1.0);
      this.chestsProperty = new BooleanProperty("Chests", true);
      this.breathingProperty = new BooleanProperty("Breathing", true);
      this.onFramebufferResize = (event) -> {
         if (this.framebuffer != null) {
            this.framebuffer.deleteFramebuffer();
         }

         this.framebuffer = new Framebuffer(event.getWidth(), event.getHeight(), false);
      };
      this.onRenderChestModel = (event) -> {
         if ((Boolean)this.chestsProperty.getValue()) {
            this.framebuffer.bindFramebuffer(false);
            GL20.glUseProgram(this.fillShader.getProgram());
            GL20.glUniform4f(this.fillShader.getUniformLocation("colour"), 1.0F, 0.6F, 0.0F, 1.0F);
            event.draw();
            GL20.glUseProgram(0);
            this.mc.getFramebuffer().bindFramebuffer(false);
         }

      };
      this.colourBuffer = new float[4];
      this.onModelRender = (event) -> {
         if (!event.isPre()) {
            this.framebuffer.bindFramebuffer(false);
            GL20.glUseProgram(this.fillShader.getProgram());
            this.fillColourBuffer(this.colourModeProperty.getValue() == Glow.ColourMode.STATIC ? (Integer)this.colourProperty.getValue() : (TeamsUtil.TeamsMode.NAME.getComparator().isOnSameTeam(this.mc.thePlayer, event.getEntity()) ? (Integer)this.friendlyColourProperty.getValue() : (Integer)this.enemyColourProperty.getValue()));
            GL20.glUniform4f(this.fillShader.getUniformLocation("colour"), this.colourBuffer[0], this.colourBuffer[1], this.colourBuffer[2], this.colourBuffer[3]);
            event.drawModel();
            LayerRenderer.renderEnchantGlint = false;
            event.drawLayers();
            LayerRenderer.renderEnchantGlint = true;
            GL20.glUseProgram(0);
            this.mc.getFramebuffer().bindFramebuffer(false);
         }

      };
      this.onRenderGameOverlay = (event) -> {
         ScaledResolution scaledResolution = event.getScaledResolution();
         this.shader.use();
         DrawUtil.glDrawFramebuffer(this.framebuffer.framebufferTexture, scaledResolution.getScaledWidth(), scaledResolution.getScaledHeight());
         GL20.glUseProgram(0);
         this.framebuffer.framebufferClear();
         this.mc.getFramebuffer().bindFramebuffer(false);
      };
      this.register(new Property[]{this.colourModeProperty, this.colourProperty, this.friendlyColourProperty, this.enemyColourProperty, this.radiusProperty, this.chestsProperty, this.breathingProperty});
   }

   private void fillColourBuffer(int colour) {
      int red = colour >> 16 & 255;
      int green = colour >> 8 & 255;
      int blue = colour & 255;
      int alpha = colour >> 24 & 255;
      this.colourBuffer[0] = (float)red / 255.0F;
      this.colourBuffer[1] = (float)green / 255.0F;
      this.colourBuffer[2] = (float)blue / 255.0F;
      this.colourBuffer[3] = (float)alpha / 255.0F;
   }

   public void onEnable() {
      this.framebuffer = new Framebuffer(this.mc.displayWidth, this.mc.displayHeight, false);
   }

   public void onDisable() {
      this.framebuffer.deleteFramebuffer();
   }

   private static enum ColourMode {
      STATIC("Static"),
      TEAMS("Teams");

      private final String name;

      private ColourMode(String name) {
         this.name = name;
      }

      public String toString() {
         return this.name;
      }
   }
}
