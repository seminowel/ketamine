package io.github.nevalackin.client.impl.ui.click.components.sub;

import io.github.nevalackin.client.api.ui.framework.Component;
import io.github.nevalackin.client.impl.module.render.overlay.Gui;
import io.github.nevalackin.client.util.render.Texture;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class ImageRendererComponent extends Component {
   private final HashMap bufferedImageCache = new HashMap();
   private Texture texture;
   private String lastPath;

   public ImageRendererComponent(double x, double y, double width, double height) {
      super((Component)null, x, y, width, height);
   }

   public void setLocation(ResourceLocation texture) {
      if (texture == null) {
         this.texture = null;
      } else if (this.lastPath == null || !this.lastPath.equals(texture.getResourcePath())) {
         if (this.bufferedImageCache.containsKey(texture)) {
            this.texture = (Texture)this.bufferedImageCache.get(texture);
         } else {
            BufferedImage image = null;

            try {
               image = ImageIO.read(Minecraft.getMinecraft().getResourceManager().getResource(texture).getInputStream());
            } catch (Exception var4) {
               var4.printStackTrace();
            }

            if (image == null) {
               this.texture = null;
            } else {
               Texture texture1 = new Texture(image, true);
               this.texture = texture1;
               this.bufferedImageCache.put(texture, texture1);
               this.lastPath = texture.getResourcePath();
            }
         }
      }
   }

   private ResourceLocation buildLocation(Gui.ImageMode value) {
      return value == Gui.ImageMode.NONE ? null : new ResourceLocation("ketamine/textures/images/" + value.toString().toLowerCase() + ".png");
   }

   public void onDraw(ScaledResolution scaledResolution, int mouseX, int mouseY) {
      if (!((Double)Gui.imageScaleProperty.getValue() <= 0.0)) {
         this.setLocation(this.buildLocation((Gui.ImageMode)Gui.imageModeProperty.getValue()));
         if (this.texture != null) {
            GL11.glTranslated((double)scaledResolution.getScaledWidth() - (double)scaledResolution.getScaledWidth() * (Double)Gui.imageScaleProperty.getValue(), (double)scaledResolution.getScaledHeight() - (double)scaledResolution.getScaledHeight() * (Double)Gui.imageScaleProperty.getValue(), 0.0);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glEnable(3008);
            this.texture.bind();
            GL11.glBegin(7);
            GL11.glTexCoord2d(0.0, 0.0);
            GL11.glVertex2d(0.0, 0.0);
            GL11.glTexCoord2d(0.0, 1.0);
            GL11.glVertex2d(0.0, (double)scaledResolution.getScaledHeight() * (Double)Gui.imageScaleProperty.getValue());
            GL11.glTexCoord2d(1.0, 1.0);
            GL11.glVertex2d((double)scaledResolution.getScaledWidth() * (Double)Gui.imageScaleProperty.getValue(), (double)scaledResolution.getScaledHeight() * (Double)Gui.imageScaleProperty.getValue());
            GL11.glTexCoord2d(1.0, 0.0);
            GL11.glVertex2d((double)scaledResolution.getScaledWidth() * (Double)Gui.imageScaleProperty.getValue(), 0.0);
            GL11.glEnd();
            this.texture.unbind();
            GL11.glDisable(3008);
            GL11.glTranslated((double)(-scaledResolution.getScaledWidth()) + (double)scaledResolution.getScaledWidth() * (Double)Gui.imageScaleProperty.getValue(), (double)(-scaledResolution.getScaledHeight()) + (double)scaledResolution.getScaledHeight() * (Double)Gui.imageScaleProperty.getValue(), 0.0);
         }
      }
   }
}
