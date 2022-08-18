package io.github.nevalackin.client.impl.module.render.model;

import io.github.nevalackin.client.api.module.Category;
import io.github.nevalackin.client.api.module.Module;
import io.github.nevalackin.client.api.property.Property;
import io.github.nevalackin.client.impl.property.BooleanProperty;
import io.github.nevalackin.client.impl.property.ColourProperty;
import io.github.nevalackin.client.impl.property.EnumProperty;
import io.github.nevalackin.client.util.render.ColourUtil;
import io.github.nevalackin.client.util.render.DrawUtil;
import io.github.nevalackin.client.util.render.GLShader;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.awt.Color;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public final class Chams extends Module {
   private final EnumProperty modeProperty;
   private final ColourProperty shaderColourProperty;
   private final BooleanProperty wallHackProperty;
   private final EnumProperty layerProperty;
   private final BooleanProperty visibleEnabledProperty;
   private final BooleanProperty visibleTexturedProperty;
   private final BooleanProperty visibleFlatProperty;
   private final ColourProperty visibleColourProperty;
   private final BooleanProperty occludedEnabledProperty;
   private final BooleanProperty occludedTexturedProperty;
   private final BooleanProperty occludedFlatProperty;
   private final ColourProperty occludedColourProperty;
   private final BooleanProperty handsEnabledProperty;
   private final ColourProperty handsColourProperty;
   private final BooleanProperty leftHandedProperty;
   private static final String FILL_SHADER = "#version 120\n\nuniform sampler2D texture;\nuniform vec2 texelSize;\n\nuniform vec4 colour;\n\nvoid main(void) {\n    vec4 sample = texture2D(texture, gl_TexCoord[0].xy);\n    gl_FragColor = vec4(colour.rgb, min(sample.a, colour.a));\n}\n";
   public static final String VERTEX_SHADER = "#version 120 \n\nvoid main() {\n    gl_TexCoord[0] = gl_MultiTexCoord0;\n    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;\n}";
   private final GLShader shader;
   private Framebuffer framebuffer;
   private boolean wasBlendEnabled;
   private boolean isTextureEnabled;
   private boolean isLightingEnabled;
   @EventLink
   private final Listener onRenderHand;
   @EventLink
   private final Listener onFramebufferResize;
   @EventLink(4)
   private final Listener onModelRender;
   @EventLink
   private final Listener onRenderGameOverlay;
   @EventLink
   private final Listener onRenderLivingEntity;

   public Chams() {
      super("Chams", Category.RENDER, Category.SubCategory.RENDER_MODEL);
      this.modeProperty = new EnumProperty("Mode", Chams.Mode.OLD);
      this.shaderColourProperty = new ColourProperty("Shader Colour", ColourUtil.getClientColour(), () -> {
         return this.modeProperty.getValue() == Chams.Mode.SHADER;
      });
      this.wallHackProperty = new BooleanProperty("Wall Hack", true);
      this.layerProperty = new EnumProperty("Layer", Chams.Layer.VISIBLE);
      this.visibleEnabledProperty = new BooleanProperty("V-Enabled", true, this::isShowingVisibleLayer);
      this.visibleTexturedProperty = new BooleanProperty("V-Textured", false, this::isShowingVisibleOptions);
      this.visibleFlatProperty = new BooleanProperty("V-Flat", true, this::isShowingVisibleOptions);
      this.visibleColourProperty = new ColourProperty("V-Colour", ColourUtil.getClientColour(), this::isShowingVisibleOptions);
      this.occludedEnabledProperty = new BooleanProperty("O-Enabled", true, this::isShowingOccludedLayer);
      this.occludedTexturedProperty = new BooleanProperty("O-Textured", false, this::isShowingOccludedOptions);
      this.occludedFlatProperty = new BooleanProperty("O-Flat", true, this::isShowingOccludedOptions);
      this.occludedColourProperty = new ColourProperty("O-Colour", ColourUtil.getSecondaryColour(), this::isShowingOccludedOptions);
      this.handsEnabledProperty = new BooleanProperty("Hands Enabled", true, this::isHandsLayerVisible);
      this.handsColourProperty = new ColourProperty("Hands Colour", ColourUtil.overwriteAlphaComponent(ColourUtil.getClientColour(), 128), this::isHandsOptionsVisible);
      this.leftHandedProperty = new BooleanProperty("Left Handed", false, this::isHandsOptionsVisible);
      this.shader = new GLShader("#version 120 \n\nvoid main() {\n    gl_TexCoord[0] = gl_MultiTexCoord0;\n    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;\n}", "#version 120\n\nuniform sampler2D texture;\nuniform vec2 texelSize;\n\nuniform vec4 colour;\n\nvoid main(void) {\n    vec4 sample = texture2D(texture, gl_TexCoord[0].xy);\n    gl_FragColor = vec4(colour.rgb, min(sample.a, colour.a));\n}\n") {
         public void setupUniforms() {
            this.setupUniform("texture");
            this.setupUniform("texelSize");
            this.setupUniform("colour");
            GL20.glUniform1i(this.getUniformLocation("texture"), 0);
         }

         public void updateUniforms() {
            Color colour = Chams.this.shaderColourProperty.getColour();
            GL20.glUniform2f(this.getUniformLocation("texelSize"), 1.0F / (float)Chams.this.mc.displayWidth, 1.0F / (float)Chams.this.mc.displayHeight);
            GL20.glUniform4f(this.getUniformLocation("colour"), (float)colour.getRed() / 255.0F, (float)colour.getGreen() / 255.0F, (float)colour.getBlue() / 255.0F, (float)colour.getAlpha() / 255.0F);
         }
      };
      this.onRenderHand = (event) -> {
         if ((Boolean)this.handsEnabledProperty.getValue()) {
            event.setBindTexture(false);
            event.setPreRenderCallback(this::preHandsRenderCallback);
            event.setPostRenderCallback(this::postHandsRenderCallback);
            event.setLeft((Boolean)this.leftHandedProperty.getValue());
         }

      };
      this.onFramebufferResize = (event) -> {
         if (this.framebuffer != null) {
            this.framebuffer.deleteFramebuffer();
         }

         this.framebuffer = new Framebuffer(event.getWidth(), event.getHeight(), false);
      };
      this.onModelRender = (event) -> {
         if (this.validateEntity(event.getEntity())) {
            switch ((Mode)this.modeProperty.getValue()) {
               case SHADER:
                  if (!event.isPre()) {
                     this.framebuffer.bindFramebuffer(false);
                     event.drawModel();
                     LayerRenderer.renderEnchantGlint = false;
                     event.drawLayers();
                     LayerRenderer.renderEnchantGlint = true;
                     this.mc.getFramebuffer().bindFramebuffer(false);
                  }
                  break;
               case OLD:
                  if (!(Boolean)this.visibleEnabledProperty.getValue() && !(Boolean)this.occludedEnabledProperty.getValue()) {
                     return;
                  }

                  if (event.isPre()) {
                     this.wasBlendEnabled = false;
                     this.isLightingEnabled = true;
                     this.isTextureEnabled = true;
                     boolean visibleFlat = (Boolean)this.visibleFlatProperty.getValue();
                     boolean occludedFlat = (Boolean)this.occludedFlatProperty.getValue();
                     boolean visibleTexture = (Boolean)this.visibleTexturedProperty.getValue();
                     boolean occludedTexture = (Boolean)this.occludedTexturedProperty.getValue();
                     OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
                     int occludedColour = (Integer)this.occludedColourProperty.getValue();
                     int visibleColour = (Integer)this.visibleColourProperty.getValue();
                     if ((occludedColour >> 24 & 255) < 255 || (visibleColour >> 24 & 255) < 255) {
                        this.wasBlendEnabled = DrawUtil.glEnableBlend();
                     }

                     if ((Boolean)this.occludedEnabledProperty.getValue()) {
                        if (!occludedTexture) {
                           GL11.glDisable(3553);
                           this.isTextureEnabled = false;
                        }

                        if (occludedFlat) {
                           GL11.glDisable(2896);
                           this.isLightingEnabled = false;
                        }

                        GL11.glDisable(2929);
                        GL11.glDepthMask(false);
                        DrawUtil.glColour(occludedColour);
                        event.drawModel();
                        GL11.glEnable(2929);
                        GL11.glDepthMask(true);
                     }

                     if ((Boolean)this.visibleEnabledProperty.getValue()) {
                        if (this.isLightingEnabled == visibleFlat) {
                           if (visibleFlat) {
                              GL11.glDisable(2896);
                           } else {
                              GL11.glEnable(2896);
                           }

                           this.isLightingEnabled = !visibleFlat;
                        }

                        if (this.isTextureEnabled != visibleTexture) {
                           if (visibleTexture) {
                              GL11.glEnable(3553);
                           } else {
                              GL11.glDisable(3553);
                           }

                           this.isTextureEnabled = visibleTexture;
                        }

                        DrawUtil.glColour(visibleColour);
                     } else {
                        this.restore();
                     }
                  } else if ((Boolean)this.visibleEnabledProperty.getValue()) {
                     this.restore();
                  }
            }
         }

      };
      this.onRenderGameOverlay = (event) -> {
         switch ((Mode)this.modeProperty.getValue()) {
            case SHADER:
               ScaledResolution scaledResolution = event.getScaledResolution();
               this.shader.use();
               DrawUtil.glDrawFramebuffer(this.framebuffer.framebufferTexture, scaledResolution.getScaledWidth(), scaledResolution.getScaledHeight());
               GL20.glUseProgram(0);
               this.framebuffer.framebufferClear();
               this.mc.getFramebuffer().bindFramebuffer(false);
            default:
         }
      };
      this.onRenderLivingEntity = (event) -> {
         if (this.modeProperty.getValue() == Chams.Mode.OLD && (Boolean)this.wallHackProperty.getValue() && this.validateEntity(event.getEntity())) {
            event.setPreRenderCallback(this::preRenderCallback);
            event.setPostRenderCallback(this::postRenderCallback);
         }

      };
      this.register(new Property[]{this.modeProperty, this.shaderColourProperty, this.wallHackProperty, this.layerProperty, this.visibleEnabledProperty, this.visibleTexturedProperty, this.visibleFlatProperty, this.visibleColourProperty, this.occludedEnabledProperty, this.occludedTexturedProperty, this.occludedFlatProperty, this.occludedColourProperty, this.handsEnabledProperty, this.handsColourProperty, this.leftHandedProperty});
   }

   private boolean isShowingVisibleLayer() {
      return this.modeProperty.getValue() == Chams.Mode.OLD && this.layerProperty.check() && this.layerProperty.getValue() == Chams.Layer.VISIBLE;
   }

   private boolean isHandsLayerVisible() {
      return this.layerProperty.check() && this.layerProperty.getValue() == Chams.Layer.HANDS;
   }

   private boolean isShowingOccludedLayer() {
      return this.modeProperty.getValue() == Chams.Mode.OLD && this.layerProperty.check() && this.layerProperty.getValue() == Chams.Layer.OCCLUDED;
   }

   private boolean isShowingVisibleOptions() {
      return this.isShowingVisibleLayer() && (Boolean)this.visibleEnabledProperty.getValue();
   }

   private boolean isShowingOccludedOptions() {
      return this.isShowingOccludedLayer() && (Boolean)this.occludedEnabledProperty.getValue();
   }

   private boolean isHandsOptionsVisible() {
      return this.isHandsLayerVisible() && (Boolean)this.handsEnabledProperty.getValue();
   }

   private void preHandsRenderCallback() {
      this.wasBlendEnabled = DrawUtil.glEnableBlend();
      OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
      DrawUtil.glColour((Integer)this.handsColourProperty.getValue());
      GL11.glDisable(3008);
      GL11.glDisable(3553);
      GL11.glDisable(2896);
   }

   private void postHandsRenderCallback() {
      DrawUtil.glRestoreBlend(this.wasBlendEnabled);
      GL11.glEnable(3008);
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      GL11.glEnable(3553);
      GL11.glEnable(2896);
   }

   public void restore() {
      if (!this.isLightingEnabled) {
         GL11.glEnable(2896);
      }

      if (!this.isTextureEnabled) {
         GL11.glEnable(3553);
      }

      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      DrawUtil.glRestoreBlend(this.wasBlendEnabled);
   }

   private void preRenderCallback() {
      GL11.glEnable(32823);
      GL11.glPolygonOffset(1.0F, -1000000.0F);
   }

   private void postRenderCallback() {
      GL11.glPolygonOffset(1.0F, 1000000.0F);
      GL11.glDisable(32823);
   }

   private boolean validateEntity(EntityLivingBase entity) {
      return entity instanceof EntityPlayer && entity.isEntityAlive() && !entity.isInvisible();
   }

   public void onEnable() {
      this.framebuffer = new Framebuffer(this.mc.displayWidth, this.mc.displayHeight, false);
   }

   public void onDisable() {
      this.framebuffer.deleteFramebuffer();
   }

   private static enum Layer {
      VISIBLE("Visible"),
      OCCLUDED("Occluded"),
      HANDS("Hands");

      private final String name;

      private Layer(String name) {
         this.name = name;
      }

      public String toString() {
         return this.name;
      }
   }

   private static enum Mode {
      SHADER("Shader"),
      OLD("Old");

      private final String name;

      private Mode(String name) {
         this.name = name;
      }

      public String toString() {
         return this.name;
      }
   }
}
