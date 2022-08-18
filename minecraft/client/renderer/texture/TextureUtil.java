package net.minecraft.client.renderer.texture;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;
import javax.imageio.ImageIO;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

public class TextureUtil {
   private static final Logger logger = LogManager.getLogger();
   private static final IntBuffer dataBuffer = GLAllocation.createDirectIntBuffer(4194304);
   public static final DynamicTexture missingTexture = new DynamicTexture(16, 16);
   public static final int[] missingTextureData;
   private static final int[] mipmapBuffer;

   public static int glGenTextures() {
      return GL11.glGenTextures();
   }

   public static void deleteTexture(int textureId) {
      GlStateManager.deleteTexture(textureId);
   }

   public static int uploadTextureImage(int p_110987_0_, BufferedImage p_110987_1_) {
      return uploadTextureImageAllocate(p_110987_0_, p_110987_1_, false, false);
   }

   public static void uploadTexture(int textureId, int[] p_110988_1_, int p_110988_2_, int p_110988_3_) {
      bindTexture(textureId);
      uploadTextureSub(0, p_110988_1_, p_110988_2_, p_110988_3_, 0, 0, false, false, false);
   }

   public static int[][] generateMipmapData(int p_147949_0_, int p_147949_1_, int[][] p_147949_2_) {
      int[][] aint = new int[p_147949_0_ + 1][];
      aint[0] = p_147949_2_[0];
      if (p_147949_0_ > 0) {
         boolean flag = false;

         int l1;
         for(l1 = 0; l1 < p_147949_2_.length; ++l1) {
            if (p_147949_2_[0][l1] >> 24 == 0) {
               flag = true;
               break;
            }
         }

         for(l1 = 1; l1 <= p_147949_0_; ++l1) {
            if (p_147949_2_[l1] != null) {
               aint[l1] = p_147949_2_[l1];
            } else {
               int[] aint1 = aint[l1 - 1];
               int[] aint2 = new int[aint1.length >> 2];
               int j = p_147949_1_ >> l1;
               int k = aint2.length / j;
               int l = j << 1;

               for(int i1 = 0; i1 < j; ++i1) {
                  for(int j1 = 0; j1 < k; ++j1) {
                     int k1 = 2 * (i1 + j1 * l);
                     aint2[i1 + j1 * j] = blendColors(aint1[k1 + 0], aint1[k1 + 1], aint1[k1 + 0 + l], aint1[k1 + 1 + l], flag);
                  }
               }

               aint[l1] = aint2;
            }
         }
      }

      return aint;
   }

   private static int blendColors(int p_147943_0_, int p_147943_1_, int p_147943_2_, int p_147943_3_, boolean p_147943_4_) {
      if (!p_147943_4_) {
         int i1 = blendColorComponent(p_147943_0_, p_147943_1_, p_147943_2_, p_147943_3_, 24);
         int j1 = blendColorComponent(p_147943_0_, p_147943_1_, p_147943_2_, p_147943_3_, 16);
         int k1 = blendColorComponent(p_147943_0_, p_147943_1_, p_147943_2_, p_147943_3_, 8);
         int l1 = blendColorComponent(p_147943_0_, p_147943_1_, p_147943_2_, p_147943_3_, 0);
         return i1 << 24 | j1 << 16 | k1 << 8 | l1;
      } else {
         mipmapBuffer[0] = p_147943_0_;
         mipmapBuffer[1] = p_147943_1_;
         mipmapBuffer[2] = p_147943_2_;
         mipmapBuffer[3] = p_147943_3_;
         float f = 0.0F;
         float f1 = 0.0F;
         float f2 = 0.0F;
         float f3 = 0.0F;

         int i2;
         for(i2 = 0; i2 < 4; ++i2) {
            if (mipmapBuffer[i2] >> 24 != 0) {
               f += (float)Math.pow((double)((float)(mipmapBuffer[i2] >> 24 & 255) / 255.0F), 2.2);
               f1 += (float)Math.pow((double)((float)(mipmapBuffer[i2] >> 16 & 255) / 255.0F), 2.2);
               f2 += (float)Math.pow((double)((float)(mipmapBuffer[i2] >> 8 & 255) / 255.0F), 2.2);
               f3 += (float)Math.pow((double)((float)(mipmapBuffer[i2] >> 0 & 255) / 255.0F), 2.2);
            }
         }

         f /= 4.0F;
         f1 /= 4.0F;
         f2 /= 4.0F;
         f3 /= 4.0F;
         i2 = (int)(Math.pow((double)f, 0.45454545454545453) * 255.0);
         int j = (int)(Math.pow((double)f1, 0.45454545454545453) * 255.0);
         int k = (int)(Math.pow((double)f2, 0.45454545454545453) * 255.0);
         int l = (int)(Math.pow((double)f3, 0.45454545454545453) * 255.0);
         if (i2 < 96) {
            i2 = 0;
         }

         return i2 << 24 | j << 16 | k << 8 | l;
      }
   }

   private static int blendColorComponent(int p_147944_0_, int p_147944_1_, int p_147944_2_, int p_147944_3_, int p_147944_4_) {
      float f = (float)Math.pow((double)((float)(p_147944_0_ >> p_147944_4_ & 255) / 255.0F), 2.2);
      float f1 = (float)Math.pow((double)((float)(p_147944_1_ >> p_147944_4_ & 255) / 255.0F), 2.2);
      float f2 = (float)Math.pow((double)((float)(p_147944_2_ >> p_147944_4_ & 255) / 255.0F), 2.2);
      float f3 = (float)Math.pow((double)((float)(p_147944_3_ >> p_147944_4_ & 255) / 255.0F), 2.2);
      float f4 = (float)Math.pow((double)(f + f1 + f2 + f3) * 0.25, 0.45454545454545453);
      return (int)((double)f4 * 255.0);
   }

   public static void uploadTextureMipmap(int[][] p_147955_0_, int p_147955_1_, int p_147955_2_, int p_147955_3_, int p_147955_4_, boolean p_147955_5_, boolean p_147955_6_) {
      for(int i = 0; i < p_147955_0_.length; ++i) {
         int[] aint = p_147955_0_[i];
         uploadTextureSub(i, aint, p_147955_1_ >> i, p_147955_2_ >> i, p_147955_3_ >> i, p_147955_4_ >> i, p_147955_5_, p_147955_6_, p_147955_0_.length > 1);
      }

   }

   private static void uploadTextureSub(int p_147947_0_, int[] p_147947_1_, int p_147947_2_, int p_147947_3_, int p_147947_4_, int p_147947_5_, boolean p_147947_6_, boolean p_147947_7_, boolean p_147947_8_) {
      int i = 4194304 / p_147947_2_;
      setTextureBlurMipmap(p_147947_6_, p_147947_8_);
      setTextureClamped(p_147947_7_);

      int l;
      for(int j = 0; j < p_147947_2_ * p_147947_3_; j += p_147947_2_ * l) {
         int k = j / p_147947_2_;
         l = Math.min(i, p_147947_3_ - k);
         int i1 = p_147947_2_ * l;
         copyToBufferPos(p_147947_1_, j, i1);
         GL11.glTexSubImage2D(3553, p_147947_0_, p_147947_4_, p_147947_5_ + k, p_147947_2_, l, 32993, 33639, dataBuffer);
      }

   }

   public static int uploadTextureImageAllocate(int p_110989_0_, BufferedImage p_110989_1_, boolean p_110989_2_, boolean p_110989_3_) {
      allocateTexture(p_110989_0_, p_110989_1_.getWidth(), p_110989_1_.getHeight());
      return uploadTextureImageSub(p_110989_0_, p_110989_1_, 0, 0, p_110989_2_, p_110989_3_);
   }

   public static void allocateTexture(int p_110991_0_, int p_110991_1_, int p_110991_2_) {
      allocateTextureImpl(p_110991_0_, 0, p_110991_1_, p_110991_2_);
   }

   public static void allocateTextureImpl(int p_180600_0_, int p_180600_1_, int p_180600_2_, int p_180600_3_) {
      deleteTexture(p_180600_0_);
      bindTexture(p_180600_0_);
      if (p_180600_1_ >= 0) {
         GL11.glTexParameteri(3553, 33085, p_180600_1_);
         GL11.glTexParameterf(3553, 33082, 0.0F);
         GL11.glTexParameterf(3553, 33083, (float)p_180600_1_);
         GL11.glTexParameterf(3553, 34049, 0.0F);
      }

      for(int i = 0; i <= p_180600_1_; ++i) {
         GL11.glTexImage2D(3553, i, 6408, p_180600_2_ >> i, p_180600_3_ >> i, 0, 32993, 33639, (IntBuffer)null);
      }

   }

   public static int uploadTextureImageSub(int textureId, BufferedImage p_110995_1_, int p_110995_2_, int p_110995_3_, boolean p_110995_4_, boolean p_110995_5_) {
      bindTexture(textureId);
      uploadTextureImageSubImpl(p_110995_1_, p_110995_2_, p_110995_3_, p_110995_4_, p_110995_5_);
      return textureId;
   }

   private static void uploadTextureImageSubImpl(BufferedImage p_110993_0_, int p_110993_1_, int p_110993_2_, boolean p_110993_3_, boolean p_110993_4_) {
      int i = p_110993_0_.getWidth();
      int j = p_110993_0_.getHeight();
      int k = 4194304 / i;
      int[] aint = new int[k * i];
      setTextureBlurred(p_110993_3_);
      setTextureClamped(p_110993_4_);

      for(int l = 0; l < i * j; l += i * k) {
         int i1 = l / i;
         int j1 = Math.min(k, j - i1);
         int k1 = i * j1;
         p_110993_0_.getRGB(0, i1, i, j1, aint, 0, i);
         copyToBuffer(aint, k1);
         GL11.glTexSubImage2D(3553, 0, p_110993_1_, p_110993_2_ + i1, i, j1, 32993, 33639, dataBuffer);
      }

   }

   private static void setTextureClamped(boolean p_110997_0_) {
      if (p_110997_0_) {
         GL11.glTexParameteri(3553, 10242, 10496);
         GL11.glTexParameteri(3553, 10243, 10496);
      } else {
         GL11.glTexParameteri(3553, 10242, 10497);
         GL11.glTexParameteri(3553, 10243, 10497);
      }

   }

   private static void setTextureBlurred(boolean p_147951_0_) {
      setTextureBlurMipmap(p_147951_0_, false);
   }

   private static void setTextureBlurMipmap(boolean p_147954_0_, boolean p_147954_1_) {
      if (p_147954_0_) {
         GL11.glTexParameteri(3553, 10241, p_147954_1_ ? 9987 : 9729);
         GL11.glTexParameteri(3553, 10240, 9729);
      } else {
         GL11.glTexParameteri(3553, 10241, p_147954_1_ ? 9986 : 9728);
         GL11.glTexParameteri(3553, 10240, 9728);
      }

   }

   private static void copyToBuffer(int[] p_110990_0_, int p_110990_1_) {
      copyToBufferPos(p_110990_0_, 0, p_110990_1_);
   }

   private static void copyToBufferPos(int[] p_110994_0_, int p_110994_1_, int p_110994_2_) {
      dataBuffer.clear();
      dataBuffer.put(p_110994_0_, p_110994_1_, p_110994_2_);
      dataBuffer.position(0).limit(p_110994_2_);
   }

   static void bindTexture(int p_94277_0_) {
      GL11.glBindTexture(3553, p_94277_0_);
   }

   public static int[] readImageData(IResourceManager resourceManager, ResourceLocation imageLocation) throws IOException {
      BufferedImage bufferedimage = readBufferedImage(resourceManager.getResource(imageLocation).getInputStream());
      int i = bufferedimage.getWidth();
      int j = bufferedimage.getHeight();
      int[] aint = new int[i * j];
      bufferedimage.getRGB(0, 0, i, j, aint, 0, i);
      return aint;
   }

   public static BufferedImage readBufferedImage(InputStream imageStream) throws IOException {
      BufferedImage bufferedimage;
      try {
         bufferedimage = ImageIO.read(imageStream);
      } finally {
         IOUtils.closeQuietly(imageStream);
      }

      return bufferedimage;
   }

   public static void processPixelValues(int[] p_147953_0_, int p_147953_1_, int p_147953_2_) {
      int[] aint = new int[p_147953_1_];
      int i = p_147953_2_ / 2;

      for(int j = 0; j < i; ++j) {
         System.arraycopy(p_147953_0_, j * p_147953_1_, aint, 0, p_147953_1_);
         System.arraycopy(p_147953_0_, (p_147953_2_ - 1 - j) * p_147953_1_, p_147953_0_, j * p_147953_1_, p_147953_1_);
         System.arraycopy(aint, 0, p_147953_0_, (p_147953_2_ - 1 - j) * p_147953_1_, p_147953_1_);
      }

   }

   static {
      missingTextureData = missingTexture.getTextureData();
      int i = -16777216;
      int j = -524040;
      int[] aint = new int[]{-524040, -524040, -524040, -524040, -524040, -524040, -524040, -524040};
      int[] aint1 = new int[]{-16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216};
      int k = aint.length;

      for(int l = 0; l < 16; ++l) {
         System.arraycopy(l < k ? aint : aint1, 0, missingTextureData, 16 * l, k);
         System.arraycopy(l < k ? aint1 : aint, 0, missingTextureData, 16 * l + k, k);
      }

      missingTexture.updateDynamicTexture();
      mipmapBuffer = new int[4];
   }
}
