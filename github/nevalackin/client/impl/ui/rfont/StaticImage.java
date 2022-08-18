package io.github.nevalackin.client.impl.ui.rfont;

import io.github.nevalackin.client.util.render.DrawUtil;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.ContextCapabilities;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GLContext;

public final class StaticImage {
   private final int textureID;
   private final int width;
   private final int height;

   public StaticImage(BufferedImage image, boolean nearest_filtering) {
      this.width = image.getWidth();
      this.height = image.getHeight();
      int[] pixels = new int[image.getWidth() * image.getHeight()];
      image.getRGB(0, 0, this.width, this.height, pixels, 0, this.width);
      ByteBuffer buffer = BufferUtils.createByteBuffer(pixels.length * 4);

      for(int y = 0; y < this.height; ++y) {
         for(int x = 0; x < this.width; ++x) {
            int pixel = pixels[y * this.width + x];
            buffer.put((byte)(pixel >> 16 & 255));
            buffer.put((byte)(pixel >> 8 & 255));
            buffer.put((byte)(pixel >> 0 & 255));
            buffer.put((byte)(pixel >> 24 & 255));
         }
      }

      buffer.flip();
      this.textureID = GL11.glGenTextures();
      GL11.glBindTexture(3553, this.textureID);
      GL11.glTexImage2D(3553, 0, 6408, this.width, this.height, 0, 6408, 5121, buffer);
      if (nearest_filtering) {
         GL11.glTexParameteri(3553, 10241, 9728);
         GL11.glTexParameteri(3553, 10240, 9728);
      } else {
         ContextCapabilities cc = GLContext.getCapabilities();
         if (cc.OpenGL30) {
            GL30.glGenerateMipmap(3553);
            GL11.glTexParameteri(3553, 10241, 9985);
         } else {
            GL11.glTexParameteri(3553, 10241, 9729);
         }

         GL11.glTexParameteri(3553, 10240, 9729);
      }

      GL11.glTexParameteri(3553, 10242, 33071);
      GL11.glTexParameteri(3553, 10243, 33071);
      GL11.glBindTexture(3553, 0);
      DrawUtil.checkGLError("creating static image");
   }

   public void draw(float x, float y) {
      this.draw(x, y, (float)this.width, (float)this.height);
   }

   public void draw(float x, float y, float scale) {
      this.draw(x, y, (float)this.width * scale, (float)this.height * scale);
   }

   public void draw(float x, float y, float w, float h) {
      this.bind();
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      GL11.glBegin(7);
      GL11.glTexCoord2f(0.0F, 0.0F);
      GL11.glVertex2f(x, y);
      GL11.glTexCoord2f(0.0F, 1.0F);
      GL11.glVertex2f(x, y + h);
      GL11.glTexCoord2f(1.0F, 1.0F);
      GL11.glVertex2f(x + w, y + h);
      GL11.glTexCoord2f(1.0F, 0.0F);
      GL11.glVertex2f(x + w, y);
      GL11.glEnd();
      DrawUtil.checkGLError("drawing static image");
   }

   public void bind() {
      GL11.glBindTexture(3553, this.textureID);
   }

   public void delete() {
      GL11.glDeleteTextures(this.textureID);
      DrawUtil.checkGLError("deleting static image");
   }

   public int getWidth() {
      return this.width;
   }

   public int getHeight() {
      return this.height;
   }
}
