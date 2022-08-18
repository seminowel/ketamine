package io.github.nevalackin.client.util.render;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public class Texture {
   private final BufferedImage bufferedImage;
   private final int width;
   private final int height;
   private final int id;
   private final boolean smooth;
   private boolean initialized;
   private int delay;

   public Texture(BufferedImage bufferedImage, boolean smooth) {
      this.bufferedImage = bufferedImage;
      this.width = bufferedImage.getWidth();
      this.height = bufferedImage.getHeight();
      this.id = GL11.glGenTextures();
      this.smooth = smooth;
   }

   public Texture(BufferedImage bufferedImage) {
      this(bufferedImage, false);
   }

   public void bind() {
      GL11.glBindTexture(3553, this.id);
      if (!this.initialized) {
         try {
            this.initTexture();
         } catch (Exception var2) {
         }
      }

   }

   private void initTexture() {
      GL11.glPixelStorei(3317, 1);
      GL11.glTexParameteri(3553, 10241, this.smooth ? 9987 : 9728);
      GL11.glTexParameteri(3553, 10240, this.smooth ? 9729 : 9728);
      GL11.glTexParameteri(3553, 10242, 33071);
      GL11.glTexParameteri(3553, 10243, 33071);
      int[] pixels = new int[this.bufferedImage.getWidth() * this.bufferedImage.getHeight()];
      this.bufferedImage.getRGB(0, 0, this.bufferedImage.getWidth(), this.bufferedImage.getHeight(), pixels, 0, this.bufferedImage.getWidth());
      ByteBuffer buffer = BufferUtils.createByteBuffer(this.bufferedImage.getWidth() * this.bufferedImage.getHeight() * 4);

      for(int y1 = 0; y1 < this.bufferedImage.getHeight(); ++y1) {
         for(int x1 = 0; x1 < this.bufferedImage.getWidth(); ++x1) {
            int pixel = pixels[y1 * this.bufferedImage.getWidth() + x1];
            buffer.put((byte)(pixel >> 16 & 255));
            buffer.put((byte)(pixel >> 8 & 255));
            buffer.put((byte)(pixel & 255));
            buffer.put((byte)(pixel >> 24 & 255));
         }
      }

      buffer.flip();
      GL11.glTexImage2D(3553, 0, 6408, this.width, this.height, 0, 6408, 5121, buffer);
      GL30.glGenerateMipmap(3553);
      this.initialized = true;
   }

   public void unbind() {
      GL11.glBindTexture(3553, 0);
   }

   public BufferedImage getBufferedImage() {
      return this.bufferedImage;
   }

   public int getWidth() {
      return this.width;
   }

   public int getHeight() {
      return this.height;
   }

   public int getId() {
      return this.id;
   }

   public boolean isInitialized() {
      return this.initialized;
   }

   public int getDelay() {
      return this.delay;
   }

   public void setDelay(int delay) {
      this.delay = delay;
   }

   public void setInitialized(boolean initialized) {
      this.initialized = initialized;
   }
}
