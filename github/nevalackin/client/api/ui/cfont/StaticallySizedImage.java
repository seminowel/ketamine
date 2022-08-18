package io.github.nevalackin.client.api.ui.cfont;

import io.github.nevalackin.client.util.render.DrawUtil;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL42;

public final class StaticallySizedImage {
   private final int textureId = GL11.glGenTextures();
   private final int width;
   private final int height;

   public StaticallySizedImage(BufferedImage image, boolean useMipMap, int numMinMaps) {
      this.width = image.getWidth();
      this.height = image.getHeight();
      int[] pixels = new int[this.width * this.height];
      image.getRGB(0, 0, this.width, this.height, pixels, 0, this.width);
      ByteBuffer buffer = BufferUtils.createByteBuffer(this.width * this.height * 4);

      for(int y = 0; y < this.height; ++y) {
         for(int x = 0; x < this.width; ++x) {
            int pixel = pixels[y * this.width + x];
            buffer.put((byte)-1);
            buffer.put((byte)-1);
            buffer.put((byte)-1);
            buffer.put((byte)(pixel >> 24 & 255));
         }
      }

      buffer.flip();
      GL11.glBindTexture(3553, this.textureId);
      GL42.glTexStorage2D(3553, useMipMap ? numMinMaps : 1, 32856, this.width, this.height);
      GL11.glTexSubImage2D(3553, 0, 0, 0, this.width, this.height, 32993, 5121, buffer);
      if (useMipMap) {
         GL30.glGenerateMipmap(3553);
         GL11.glTexParameteri(3553, 10241, 9987);
      } else {
         GL11.glTexParameteri(3553, 10240, 9729);
      }

      GL11.glTexParameterf(3553, 34049, 0.0F);
      GL11.glTexParameteri(3553, 10240, 9729);
      GL11.glTexImage2D(3553, 0, 6408, this.width, this.height, 0, 6408, 5121, buffer);
   }

   public int getWidth() {
      return this.width;
   }

   public int getHeight() {
      return this.height;
   }

   public void bind() {
      GL11.glBindTexture(3553, this.textureId);
   }

   public void draw(double x, double y) {
      this.draw(x, y, (double)this.width, (double)this.height);
   }

   public void draw(double x, double y, double width, double height) {
      this.bind();
      GL11.glEnable(3042);
      GL11.glBegin(7);
      GL11.glTexCoord2d(0.0, 0.0);
      GL11.glVertex2d(x, y);
      GL11.glTexCoord2d(0.0, 1.0);
      GL11.glVertex2d(x, y + height);
      GL11.glTexCoord2d(1.0, 1.0);
      GL11.glVertex2d(x + width, y + height);
      GL11.glTexCoord2d(1.0, 0.0);
      GL11.glVertex2d(x + width, y);
      GL11.glEnd();
      GL11.glDisable(3042);
   }

   public void draw(double x, double y, double width, double height, int colour) {
      this.bind();
      DrawUtil.glColour(colour);
      GL11.glEnable(3042);
      GL11.glBegin(7);
      GL11.glTexCoord2d(0.0, 0.0);
      GL11.glVertex2d(x, y);
      GL11.glTexCoord2d(0.0, 1.0);
      GL11.glVertex2d(x, y + height);
      GL11.glTexCoord2d(1.0, 1.0);
      GL11.glVertex2d(x + width, y + height);
      GL11.glTexCoord2d(1.0, 0.0);
      GL11.glVertex2d(x + width, y);
      GL11.glEnd();
      GL11.glDisable(3042);
   }
}
