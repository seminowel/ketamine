package net.minecraft.profiler;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Profiler {
   private static final Logger logger = LogManager.getLogger();
   private final List sectionList = Lists.newArrayList();
   private final List timestampList = Lists.newArrayList();
   public boolean profilingEnabled;
   private String profilingSection = "";
   private final Map profilingMap = Maps.newHashMap();

   public void clearProfiling() {
      this.profilingMap.clear();
      this.profilingSection = "";
      this.sectionList.clear();
   }

   public void startSection(String name) {
      if (this.profilingEnabled) {
         if (this.profilingSection.length() > 0) {
            this.profilingSection = this.profilingSection + ".";
         }

         this.profilingSection = this.profilingSection + name;
         this.sectionList.add(this.profilingSection);
         this.timestampList.add(System.nanoTime());
      }

   }

   public void endSection() {
      if (this.profilingEnabled) {
         long i = System.nanoTime();
         long j = (Long)this.timestampList.remove(this.timestampList.size() - 1);
         this.sectionList.remove(this.sectionList.size() - 1);
         long k = i - j;
         if (this.profilingMap.containsKey(this.profilingSection)) {
            this.profilingMap.put(this.profilingSection, (Long)this.profilingMap.get(this.profilingSection) + k);
         } else {
            this.profilingMap.put(this.profilingSection, k);
         }

         if (k > 100000000L) {
            logger.warn("Something's taking too long! '" + this.profilingSection + "' took aprox " + (double)k / 1000000.0 + " ms");
         }

         this.profilingSection = !this.sectionList.isEmpty() ? (String)this.sectionList.get(this.sectionList.size() - 1) : "";
      }

   }

   public List getProfilingData(String p_76321_1_) {
      if (!this.profilingEnabled) {
         return null;
      } else {
         long i = this.profilingMap.containsKey("root") ? (Long)this.profilingMap.get("root") : 0L;
         long j = this.profilingMap.containsKey(p_76321_1_) ? (Long)this.profilingMap.get(p_76321_1_) : -1L;
         List list = Lists.newArrayList();
         if (p_76321_1_.length() > 0) {
            p_76321_1_ = p_76321_1_ + ".";
         }

         long k = 0L;
         Iterator var9 = this.profilingMap.keySet().iterator();

         while(var9.hasNext()) {
            String s = (String)var9.next();
            if (s.length() > p_76321_1_.length() && s.startsWith(p_76321_1_) && s.indexOf(".", p_76321_1_.length() + 1) < 0) {
               k += (Long)this.profilingMap.get(s);
            }
         }

         float f = (float)k;
         if (k < j) {
            k = j;
         }

         if (i < k) {
            i = k;
         }

         Iterator var20 = this.profilingMap.keySet().iterator();

         String s3;
         while(var20.hasNext()) {
            s3 = (String)var20.next();
            if (s3.length() > p_76321_1_.length() && s3.startsWith(p_76321_1_) && s3.indexOf(".", p_76321_1_.length() + 1) < 0) {
               long l = (Long)this.profilingMap.get(s3);
               double d0 = (double)l * 100.0 / (double)k;
               double d1 = (double)l * 100.0 / (double)i;
               String s2 = s3.substring(p_76321_1_.length());
               list.add(new Result(s2, d0, d1));
            }
         }

         var20 = this.profilingMap.keySet().iterator();

         while(var20.hasNext()) {
            s3 = (String)var20.next();
            this.profilingMap.put(s3, (Long)this.profilingMap.get(s3) * 999L / 1000L);
         }

         if ((float)k > f) {
            list.add(new Result("unspecified", (double)((float)k - f) * 100.0 / (double)k, (double)((float)k - f) * 100.0 / (double)i));
         }

         Collections.sort(list);
         list.add(0, new Result(p_76321_1_, 100.0, (double)k * 100.0 / (double)i));
         return list;
      }
   }

   public void endStartSection(String name) {
      this.endSection();
      this.startSection(name);
   }

   public String getNameOfLastSection() {
      return this.sectionList.size() == 0 ? "[UNKNOWN]" : (String)this.sectionList.get(this.sectionList.size() - 1);
   }

   public static final class Result implements Comparable {
      public double field_76332_a;
      public double field_76330_b;
      public String field_76331_c;

      public Result(String p_i1554_1_, double p_i1554_2_, double p_i1554_4_) {
         this.field_76331_c = p_i1554_1_;
         this.field_76332_a = p_i1554_2_;
         this.field_76330_b = p_i1554_4_;
      }

      public int compareTo(Result p_compareTo_1_) {
         return p_compareTo_1_.field_76332_a < this.field_76332_a ? -1 : (p_compareTo_1_.field_76332_a > this.field_76332_a ? 1 : p_compareTo_1_.field_76331_c.compareTo(this.field_76331_c));
      }

      public int func_76329_a() {
         return (this.field_76331_c.hashCode() & 11184810) + 4473924;
      }
   }
}
