package io.github.nevalackin.client.util.misc;

import java.awt.Font;
import java.io.InputStream;

public final class ResourceUtil {
   private ResourceUtil() {
   }

   public static Font createFontTTF(String path) {
      try {
         return Font.createFont(0, getResourceStream(path));
      } catch (Exception var2) {
         return null;
      }
   }

   public static InputStream getResourceStream(String path) {
      String s = "/assets/minecraft/ketamine/" + path;
      return ResourceUtil.class.getResourceAsStream(s);
   }
}
