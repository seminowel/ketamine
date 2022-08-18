package org.yaml.snakeyaml.util;

public class EnumUtils {
   public static Enum findEnumInsensitiveCase(Class enumType, String name) {
      Enum[] arr$ = (Enum[])enumType.getEnumConstants();
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         Enum constant = arr$[i$];
         if (constant.name().compareToIgnoreCase(name) == 0) {
            return constant;
         }
      }

      throw new IllegalArgumentException("No enum constant " + enumType.getCanonicalName() + "." + name);
   }
}
