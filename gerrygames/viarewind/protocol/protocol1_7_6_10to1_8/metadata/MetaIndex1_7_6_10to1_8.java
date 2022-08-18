package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.metadata;

import com.viaversion.viaversion.api.minecraft.entities.Entity1_10Types;
import com.viaversion.viaversion.util.Pair;
import de.gerrygames.viarewind.protocol.protocol1_8to1_7_6_10.metadata.MetaIndex1_8to1_7_6_10;
import java.util.HashMap;
import java.util.Optional;

public class MetaIndex1_7_6_10to1_8 {
   private static final HashMap metadataRewrites = new HashMap();

   private static Optional getIndex(Entity1_10Types.EntityType type, int index) {
      Pair pair = new Pair(type, index);
      return metadataRewrites.containsKey(pair) ? Optional.of((MetaIndex1_8to1_7_6_10)metadataRewrites.get(pair)) : Optional.empty();
   }

   public static MetaIndex1_8to1_7_6_10 searchIndex(Entity1_10Types.EntityType type, int index) {
      Entity1_10Types.EntityType currentType = type;

      do {
         Optional optMeta = getIndex(currentType, index);
         if (optMeta.isPresent()) {
            return (MetaIndex1_8to1_7_6_10)optMeta.get();
         }

         currentType = currentType.getParent();
      } while(currentType != null);

      return null;
   }

   static {
      MetaIndex1_8to1_7_6_10[] var0 = MetaIndex1_8to1_7_6_10.values();
      int var1 = var0.length;

      for(int var2 = 0; var2 < var1; ++var2) {
         MetaIndex1_8to1_7_6_10 index = var0[var2];
         metadataRewrites.put(new Pair(index.getClazz(), index.getNewIndex()), index);
      }

   }
}
