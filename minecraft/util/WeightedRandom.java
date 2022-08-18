package net.minecraft.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Random;

public class WeightedRandom {
   public static int getTotalWeight(Collection collection) {
      int i = 0;

      Item weightedrandom$item;
      for(Iterator var2 = collection.iterator(); var2.hasNext(); i += weightedrandom$item.itemWeight) {
         weightedrandom$item = (Item)var2.next();
      }

      return i;
   }

   public static Item getRandomItem(Random random, Collection collection, int totalWeight) {
      if (totalWeight <= 0) {
         throw new IllegalArgumentException();
      } else {
         int i = random.nextInt(totalWeight);
         return getRandomItem(collection, i);
      }
   }

   public static Item getRandomItem(Collection collection, int weight) {
      Iterator var2 = collection.iterator();

      Item t;
      do {
         if (!var2.hasNext()) {
            return (Item)null;
         }

         t = (Item)var2.next();
         weight -= t.itemWeight;
      } while(weight >= 0);

      return t;
   }

   public static Item getRandomItem(Random random, Collection collection) {
      return getRandomItem(random, collection, getTotalWeight(collection));
   }

   public static class Item {
      protected int itemWeight;

      public Item(int itemWeightIn) {
         this.itemWeight = itemWeightIn;
      }
   }
}
