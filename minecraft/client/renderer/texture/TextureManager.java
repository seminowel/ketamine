package net.minecraft.client.renderer.texture;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TextureManager implements ITickable, IResourceManagerReloadListener {
   private static final Logger logger = LogManager.getLogger();
   private final Map mapTextureObjects = Maps.newHashMap();
   private final List listTickables = Lists.newArrayList();
   private final Map mapTextureCounters = Maps.newHashMap();
   private IResourceManager theResourceManager;

   public TextureManager(IResourceManager resourceManager) {
      this.theResourceManager = resourceManager;
   }

   public void bindTexture(ResourceLocation resource) {
      ITextureObject itextureobject = (ITextureObject)this.mapTextureObjects.get(resource);
      if (itextureobject == null) {
         itextureobject = new SimpleTexture(resource);
         this.loadTexture(resource, (ITextureObject)itextureobject);
      }

      TextureUtil.bindTexture(((ITextureObject)itextureobject).getGlTextureId());
   }

   public boolean loadTickableTexture(ResourceLocation textureLocation, ITickableTextureObject textureObj) {
      if (this.loadTexture(textureLocation, textureObj)) {
         this.listTickables.add(textureObj);
         return true;
      } else {
         return false;
      }
   }

   public boolean loadTexture(ResourceLocation textureLocation, final ITextureObject textureObj) {
      boolean flag = true;

      try {
         ((ITextureObject)textureObj).loadTexture(this.theResourceManager);
      } catch (IOException var8) {
         logger.warn("Failed to load texture: " + textureLocation, var8);
         textureObj = TextureUtil.missingTexture;
         this.mapTextureObjects.put(textureLocation, textureObj);
         flag = false;
      } catch (Throwable var9) {
         CrashReport crashreport = CrashReport.makeCrashReport(var9, "Registering texture");
         CrashReportCategory crashreportcategory = crashreport.makeCategory("Resource location being registered");
         crashreportcategory.addCrashSection("Resource location", textureLocation);
         crashreportcategory.addCrashSectionCallable("Texture object class", new Callable() {
            public String call() throws Exception {
               return textureObj.getClass().getName();
            }
         });
         throw new ReportedException(crashreport);
      }

      this.mapTextureObjects.put(textureLocation, textureObj);
      return flag;
   }

   public ITextureObject getTexture(ResourceLocation textureLocation) {
      return (ITextureObject)this.mapTextureObjects.get(textureLocation);
   }

   public ResourceLocation getDynamicTextureLocation(String name, DynamicTexture texture) {
      Integer integer = (Integer)this.mapTextureCounters.get(name);
      if (integer == null) {
         integer = 1;
      } else {
         integer = integer + 1;
      }

      this.mapTextureCounters.put(name, integer);
      ResourceLocation resourcelocation = new ResourceLocation(String.format("dynamic/%s_%d", name, integer));
      this.loadTexture(resourcelocation, texture);
      return resourcelocation;
   }

   public void tick() {
      Iterator var1 = this.listTickables.iterator();

      while(var1.hasNext()) {
         ITickable itickable = (ITickable)var1.next();
         itickable.tick();
      }

   }

   public void deleteTexture(ResourceLocation textureLocation) {
      ITextureObject itextureobject = this.getTexture(textureLocation);
      if (itextureobject != null) {
         TextureUtil.deleteTexture(itextureobject.getGlTextureId());
      }

   }

   public void onResourceManagerReload(IResourceManager resourceManager) {
      Iterator var2 = this.mapTextureObjects.entrySet().iterator();

      while(var2.hasNext()) {
         Map.Entry entry = (Map.Entry)var2.next();
         this.loadTexture((ResourceLocation)entry.getKey(), (ITextureObject)entry.getValue());
      }

   }
}
