package net.minecraft.client.renderer.texture;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.StitcherException;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.client.resources.data.TextureMetadataSection;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TextureMap extends AbstractTexture implements ITickableTextureObject {
   private static final Logger logger = LogManager.getLogger();
   public static final ResourceLocation LOCATION_MISSING_TEXTURE = new ResourceLocation("missingno");
   public static final ResourceLocation locationBlocksTexture = new ResourceLocation("textures/atlas/blocks.png");
   private final List listAnimatedSprites;
   private final Map mapRegisteredSprites;
   private final Map mapUploadedSprites;
   private final String basePath;
   private final IIconCreator iconCreator;
   private int mipmapLevels;
   private final TextureAtlasSprite missingImage;

   public TextureMap(String p_i46099_1_) {
      this(p_i46099_1_, (IIconCreator)null);
   }

   public TextureMap(String p_i46100_1_, IIconCreator iconCreatorIn) {
      this.listAnimatedSprites = Lists.newArrayList();
      this.mapRegisteredSprites = Maps.newHashMap();
      this.mapUploadedSprites = Maps.newHashMap();
      this.missingImage = new TextureAtlasSprite("missingno");
      this.basePath = p_i46100_1_;
      this.iconCreator = iconCreatorIn;
   }

   private void initMissingImage() {
      int[] aint = TextureUtil.missingTextureData;
      this.missingImage.setIconWidth(16);
      this.missingImage.setIconHeight(16);
      int[][] aint1 = new int[this.mipmapLevels + 1][];
      aint1[0] = aint;
      this.missingImage.setFramesTextureData(Lists.newArrayList(new int[][][]{aint1}));
   }

   public void loadTexture(IResourceManager resourceManager) throws IOException {
      if (this.iconCreator != null) {
         this.loadSprites(resourceManager, this.iconCreator);
      }

   }

   public void loadSprites(IResourceManager resourceManager, IIconCreator p_174943_2_) {
      this.mapRegisteredSprites.clear();
      p_174943_2_.registerSprites(this);
      this.initMissingImage();
      this.deleteGlTexture();
      this.loadTextureAtlas(resourceManager);
   }

   public void loadTextureAtlas(IResourceManager resourceManager) {
      int i = Minecraft.getGLMaximumTextureSize();
      Stitcher stitcher = new Stitcher(i, i, true, 0, this.mipmapLevels);
      this.mapUploadedSprites.clear();
      this.listAnimatedSprites.clear();
      int j = Integer.MAX_VALUE;
      int k = 1 << this.mipmapLevels;
      Iterator var6 = this.mapRegisteredSprites.entrySet().iterator();

      while(var6.hasNext()) {
         Map.Entry entry = (Map.Entry)var6.next();
         TextureAtlasSprite textureatlassprite = (TextureAtlasSprite)entry.getValue();
         ResourceLocation resourcelocation = new ResourceLocation(textureatlassprite.getIconName());
         ResourceLocation resourcelocation1 = this.completeResourceLocation(resourcelocation, 0);

         try {
            IResource iresource = resourceManager.getResource(resourcelocation1);
            BufferedImage[] abufferedimage = new BufferedImage[1 + this.mipmapLevels];
            abufferedimage[0] = TextureUtil.readBufferedImage(iresource.getInputStream());
            TextureMetadataSection texturemetadatasection = (TextureMetadataSection)iresource.getMetadata("texture");
            if (texturemetadatasection != null) {
               List list = texturemetadatasection.getListMipmaps();
               int i2;
               if (!list.isEmpty()) {
                  int l = abufferedimage[0].getWidth();
                  i2 = abufferedimage[0].getHeight();
                  if (MathHelper.roundUpToPowerOfTwo(l) != l || MathHelper.roundUpToPowerOfTwo(i2) != i2) {
                     throw new RuntimeException("Unable to load extra miplevels, source-texture is not power of two");
                  }
               }

               Iterator iterator = list.iterator();

               while(iterator.hasNext()) {
                  i2 = (Integer)iterator.next();
                  if (i2 > 0 && i2 < abufferedimage.length - 1 && abufferedimage[i2] == null) {
                     ResourceLocation resourcelocation2 = this.completeResourceLocation(resourcelocation, i2);

                     try {
                        abufferedimage[i2] = TextureUtil.readBufferedImage(resourceManager.getResource(resourcelocation2).getInputStream());
                     } catch (IOException var22) {
                        logger.error("Unable to load miplevel {} from: {}", new Object[]{i2, resourcelocation2, var22});
                     }
                  }
               }
            }

            AnimationMetadataSection animationmetadatasection = (AnimationMetadataSection)iresource.getMetadata("animation");
            textureatlassprite.loadSprite(abufferedimage, animationmetadatasection);
         } catch (RuntimeException var23) {
            logger.error("Unable to parse metadata from " + resourcelocation1, var23);
            continue;
         } catch (IOException var24) {
            logger.error("Using missing texture, unable to load " + resourcelocation1, var24);
            continue;
         }

         j = Math.min(j, Math.min(textureatlassprite.getIconWidth(), textureatlassprite.getIconHeight()));
         int l1 = Math.min(Integer.lowestOneBit(textureatlassprite.getIconWidth()), Integer.lowestOneBit(textureatlassprite.getIconHeight()));
         if (l1 < k) {
            logger.warn("Texture {} with size {}x{} limits mip level from {} to {}", new Object[]{resourcelocation1, textureatlassprite.getIconWidth(), textureatlassprite.getIconHeight(), MathHelper.calculateLogBaseTwo(k), MathHelper.calculateLogBaseTwo(l1)});
            k = l1;
         }

         stitcher.addSprite(textureatlassprite);
      }

      int j1 = Math.min(j, k);
      int k1 = MathHelper.calculateLogBaseTwo(j1);
      if (k1 < this.mipmapLevels) {
         logger.warn("{}: dropping miplevel from {} to {}, because of minimum power of two: {}", new Object[]{this.basePath, this.mipmapLevels, k1, j1});
         this.mipmapLevels = k1;
      }

      Iterator var27 = this.mapRegisteredSprites.values().iterator();

      while(var27.hasNext()) {
         final TextureAtlasSprite textureatlassprite1 = (TextureAtlasSprite)var27.next();

         try {
            textureatlassprite1.generateMipmaps(this.mipmapLevels);
         } catch (Throwable var21) {
            CrashReport crashreport = CrashReport.makeCrashReport(var21, "Applying mipmap");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Sprite being mipmapped");
            crashreportcategory.addCrashSectionCallable("Sprite name", new Callable() {
               public String call() throws Exception {
                  return textureatlassprite1.getIconName();
               }
            });
            crashreportcategory.addCrashSectionCallable("Sprite size", new Callable() {
               public String call() throws Exception {
                  return textureatlassprite1.getIconWidth() + " x " + textureatlassprite1.getIconHeight();
               }
            });
            crashreportcategory.addCrashSectionCallable("Sprite frames", new Callable() {
               public String call() throws Exception {
                  return textureatlassprite1.getFrameCount() + " frames";
               }
            });
            crashreportcategory.addCrashSection("Mipmap levels", this.mipmapLevels);
            throw new ReportedException(crashreport);
         }
      }

      this.missingImage.generateMipmaps(this.mipmapLevels);
      stitcher.addSprite(this.missingImage);

      try {
         stitcher.doStitch();
      } catch (StitcherException var20) {
         throw var20;
      }

      logger.info("Created: {}x{} {}-atlas", new Object[]{stitcher.getCurrentWidth(), stitcher.getCurrentHeight(), this.basePath});
      TextureUtil.allocateTextureImpl(this.getGlTextureId(), this.mipmapLevels, stitcher.getCurrentWidth(), stitcher.getCurrentHeight());
      Map map = Maps.newHashMap(this.mapRegisteredSprites);
      Iterator var30 = stitcher.getStichSlots().iterator();

      TextureAtlasSprite textureatlassprite2;
      while(var30.hasNext()) {
         textureatlassprite2 = (TextureAtlasSprite)var30.next();
         String s = textureatlassprite2.getIconName();
         map.remove(s);
         this.mapUploadedSprites.put(s, textureatlassprite2);

         try {
            TextureUtil.uploadTextureMipmap(textureatlassprite2.getFrameTextureData(0), textureatlassprite2.getIconWidth(), textureatlassprite2.getIconHeight(), textureatlassprite2.getOriginX(), textureatlassprite2.getOriginY(), false, false);
         } catch (Throwable var19) {
            CrashReport crashreport1 = CrashReport.makeCrashReport(var19, "Stitching texture atlas");
            CrashReportCategory crashreportcategory1 = crashreport1.makeCategory("Texture being stitched together");
            crashreportcategory1.addCrashSection("Atlas path", this.basePath);
            crashreportcategory1.addCrashSection("Sprite", textureatlassprite2);
            throw new ReportedException(crashreport1);
         }

         if (textureatlassprite2.hasAnimationMetadata()) {
            this.listAnimatedSprites.add(textureatlassprite2);
         }
      }

      var30 = map.values().iterator();

      while(var30.hasNext()) {
         textureatlassprite2 = (TextureAtlasSprite)var30.next();
         textureatlassprite2.copyFrom(this.missingImage);
      }

   }

   private ResourceLocation completeResourceLocation(ResourceLocation location, int p_147634_2_) {
      return p_147634_2_ == 0 ? new ResourceLocation(location.getResourceDomain(), String.format("%s/%s%s", this.basePath, location.getResourcePath(), ".png")) : new ResourceLocation(location.getResourceDomain(), String.format("%s/mipmaps/%s.%d%s", this.basePath, location.getResourcePath(), p_147634_2_, ".png"));
   }

   public TextureAtlasSprite getAtlasSprite(String iconName) {
      TextureAtlasSprite textureatlassprite = (TextureAtlasSprite)this.mapUploadedSprites.get(iconName);
      if (textureatlassprite == null) {
         textureatlassprite = this.missingImage;
      }

      return textureatlassprite;
   }

   public void updateAnimations() {
      TextureUtil.bindTexture(this.getGlTextureId());
      Iterator var1 = this.listAnimatedSprites.iterator();

      while(var1.hasNext()) {
         TextureAtlasSprite textureatlassprite = (TextureAtlasSprite)var1.next();
         textureatlassprite.updateAnimation();
      }

   }

   public TextureAtlasSprite registerSprite(ResourceLocation location) {
      if (location == null) {
         throw new IllegalArgumentException("Location cannot be null!");
      } else {
         TextureAtlasSprite textureatlassprite = (TextureAtlasSprite)this.mapRegisteredSprites.get(location);
         if (textureatlassprite == null) {
            textureatlassprite = TextureAtlasSprite.makeAtlasSprite(location);
            this.mapRegisteredSprites.put(location.toString(), textureatlassprite);
         }

         return textureatlassprite;
      }
   }

   public void tick() {
      this.updateAnimations();
   }

   public void setMipmapLevels(int mipmapLevelsIn) {
      this.mipmapLevels = mipmapLevelsIn;
   }

   public TextureAtlasSprite getMissingSprite() {
      return this.missingImage;
   }
}
