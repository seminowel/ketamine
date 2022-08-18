package io.github.nevalackin.client.impl.module.render.overlay;

import io.github.nevalackin.client.api.module.Category;
import io.github.nevalackin.client.api.module.Module;
import io.github.nevalackin.client.api.property.Property;
import io.github.nevalackin.client.impl.property.BooleanProperty;
import io.github.nevalackin.client.impl.property.DoubleProperty;
import io.github.nevalackin.client.util.render.ColourUtil;
import io.github.nevalackin.client.util.render.DrawUtil;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public final class Camera extends Module {
   private final BooleanProperty noHurtShakeProperty = new BooleanProperty("No Hurt Shake", true);
   private final BooleanProperty hurtFlashProperty = new BooleanProperty("Hurt Flash", true);
   private final BooleanProperty zoomProperty = new BooleanProperty("Optifine Zoom", true);
   private final DoubleProperty zoomAmountProperty;
   @EventLink
   private final Listener onFov;
   @EventLink
   private final Listener onHurtShake;
   @EventLink(0)
   private final Listener onRenderGameOverlay;

   public Camera() {
      super("Camera", Category.RENDER, Category.SubCategory.RENDER_OVERLAY);
      this.zoomAmountProperty = new DoubleProperty("Zoom Amount", 2.0, this.zoomProperty::getValue, 2.0, 10.0, 1.0);
      this.onFov = (event) -> {
         if (Keyboard.isKeyDown(46) && (Boolean)this.zoomProperty.getValue() && this.mc.currentScreen == null) {
            event.setFov(event.getFov() / ((Double)this.zoomAmountProperty.getValue()).floatValue());
         }

      };
      this.onHurtShake = (event) -> {
         if ((Boolean)this.noHurtShakeProperty.getValue()) {
            event.setCancelled();
         }

      };
      this.onRenderGameOverlay = (event) -> {
         if ((Boolean)this.hurtFlashProperty.getValue()) {
            float hurtTimePercentage = ((float)this.mc.thePlayer.hurtTime - event.getPartialTicks()) / (float)this.mc.thePlayer.maxHurtTime;
            if ((double)hurtTimePercentage > 0.0) {
               GL11.glDisable(3553);
               boolean restore = DrawUtil.glEnableBlend();
               GL11.glShadeModel(7425);
               GL11.glDisable(3008);
               ScaledResolution scaledResolution = event.getScaledResolution();
               float lineWidth = 20.0F;
               GL11.glLineWidth(20.0F);
               int width = scaledResolution.getScaledWidth();
               int height = scaledResolution.getScaledHeight();
               int fadeOutColour = ColourUtil.fadeTo(0, ColourUtil.blendHealthColours((double)(this.mc.thePlayer.getHealth() / this.mc.thePlayer.getMaxHealth())), (double)hurtTimePercentage);
               GL11.glBegin(7);
               DrawUtil.glColour(fadeOutColour);
               GL11.glVertex2f(0.0F, 0.0F);
               GL11.glVertex2f(0.0F, (float)height);
               DrawUtil.glColour(16711680);
               GL11.glVertex2f(20.0F, (float)height - 20.0F);
               GL11.glVertex2f(20.0F, 20.0F);
               DrawUtil.glColour(16711680);
               GL11.glVertex2f((float)width - 20.0F, 20.0F);
               GL11.glVertex2f((float)width - 20.0F, (float)height - 20.0F);
               DrawUtil.glColour(fadeOutColour);
               GL11.glVertex2f((float)width, (float)height);
               GL11.glVertex2f((float)width, 0.0F);
               DrawUtil.glColour(fadeOutColour);
               GL11.glVertex2f(0.0F, 0.0F);
               DrawUtil.glColour(16711680);
               GL11.glVertex2d(20.0, 20.0);
               GL11.glVertex2f((float)width - 20.0F, 20.0F);
               DrawUtil.glColour(fadeOutColour);
               GL11.glVertex2f((float)width, 0.0F);
               DrawUtil.glColour(16711680);
               GL11.glVertex2f(20.0F, (float)height - 20.0F);
               DrawUtil.glColour(fadeOutColour);
               GL11.glVertex2d(0.0, (double)height);
               GL11.glVertex2f((float)width, (float)height);
               DrawUtil.glColour(16711680);
               GL11.glVertex2f((float)width - 20.0F, (float)height - 20.0F);
               GL11.glEnd();
               GL11.glEnable(3008);
               GL11.glShadeModel(7424);
               DrawUtil.glRestoreBlend(restore);
               GL11.glEnable(3553);
            }
         }

      };
      this.register(new Property[]{this.noHurtShakeProperty, this.hurtFlashProperty, this.zoomProperty, this.zoomAmountProperty});
   }

   public void onEnable() {
      if (this.mc.thePlayer != null) {
         this.mc.gameSettings.gammaSetting = 2000.0F;
      }
   }

   public void onDisable() {
   }
}
