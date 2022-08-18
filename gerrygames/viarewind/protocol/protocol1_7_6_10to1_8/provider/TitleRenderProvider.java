package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.provider;

import com.viaversion.viaversion.api.platform.providers.Provider;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class TitleRenderProvider implements Provider {
   protected Map fadeIn = new HashMap();
   protected Map stay = new HashMap();
   protected Map fadeOut = new HashMap();
   protected Map titles = new HashMap();
   protected Map subTitles = new HashMap();
   protected Map times = new HashMap();

   public void setTimings(UUID uuid, int fadeIn, int stay, int fadeOut) {
      this.setFadeIn(uuid, fadeIn);
      this.setStay(uuid, stay);
      this.setFadeOut(uuid, fadeOut);
      AtomicInteger time = this.getTime(uuid);
      if (time.get() > 0) {
         time.set(this.getFadeIn(uuid) + this.getStay(uuid) + this.getFadeOut(uuid));
      }

   }

   public void reset(UUID uuid) {
      this.titles.remove(uuid);
      this.subTitles.remove(uuid);
      this.getTime(uuid).set(0);
      this.fadeIn.remove(uuid);
      this.stay.remove(uuid);
      this.fadeOut.remove(uuid);
   }

   public void setTitle(UUID uuid, String title) {
      this.titles.put(uuid, title);
      this.getTime(uuid).set(this.getFadeIn(uuid) + this.getStay(uuid) + this.getFadeOut(uuid));
   }

   public void setSubTitle(UUID uuid, String subTitle) {
      this.subTitles.put(uuid, subTitle);
   }

   public void clear(UUID uuid) {
      this.titles.remove(uuid);
      this.subTitles.remove(uuid);
      this.getTime(uuid).set(0);
   }

   public AtomicInteger getTime(UUID uuid) {
      return (AtomicInteger)this.times.computeIfAbsent(uuid, (key) -> {
         return new AtomicInteger(0);
      });
   }

   public int getFadeIn(UUID uuid) {
      return (Integer)this.fadeIn.getOrDefault(uuid, 10);
   }

   public int getStay(UUID uuid) {
      return (Integer)this.stay.getOrDefault(uuid, 70);
   }

   public int getFadeOut(UUID uuid) {
      return (Integer)this.fadeOut.getOrDefault(uuid, 20);
   }

   public void setFadeIn(UUID uuid, int fadeIn) {
      if (fadeIn >= 0) {
         this.fadeIn.put(uuid, fadeIn);
      } else {
         this.fadeIn.remove(uuid);
      }

   }

   public void setStay(UUID uuid, int stay) {
      if (stay >= 0) {
         this.stay.put(uuid, stay);
      } else {
         this.stay.remove(uuid);
      }

   }

   public void setFadeOut(UUID uuid, int fadeOut) {
      if (fadeOut >= 0) {
         this.fadeOut.put(uuid, fadeOut);
      } else {
         this.fadeOut.remove(uuid);
      }

   }
}
