package io.github.nevalackin.client.util.player;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

public final class TeamsUtil {
   private static final int[] COLOR_CODE_TABLE = new int[32];

   private TeamsUtil() {
   }

   private static char getColourChar(EntityPlayer player) {
      String name = player.getDisplayName().getFormattedText();
      if (name.length() < 2) {
         return '?';
      } else {
         int index = name.indexOf(167);
         return index != -1 && index + 1 < name.length() ? name.charAt(index + 1) : '?';
      }
   }

   static {
      for(int i = 0; i < 32; ++i) {
         int j = (i >> 3 & 1) * 85;
         int k = (i >> 2 & 1) * 170 + j;
         int l = (i >> 1 & 1) * 170 + j;
         int i1 = (i & 1) * 170 + j;
         if (i == 6) {
            k += 85;
         }

         if (i >= 16) {
            k /= 4;
            l /= 4;
            i1 /= 4;
         }

         COLOR_CODE_TABLE[i] = (k & 255) << 16 | (l & 255) << 8 | i1 & 255;
      }

   }

   public interface TeamComparator {
      boolean isOnSameTeam(EntityPlayer var1, EntityPlayer var2);
   }

   public interface TeamsColourSupplier {
      int getTeamColour(EntityPlayer var1);
   }

   public static enum TeamsMode {
      NAME("Name", (player) -> {
         char character = TeamsUtil.getColourChar(player);
         if (character == '?') {
            return 0;
         } else {
            int colourIndex = "0123456789abcdef".indexOf(character);
            return colourIndex == -1 ? 0 : TeamsUtil.COLOR_CODE_TABLE[colourIndex] | -16777216;
         }
      }),
      ARMOUR("Armour", (player) -> {
         ItemStack helmet = player.getCurrentArmor(3);
         ItemStack chestPlate = player.getCurrentArmor(2);
         if (helmet != null && chestPlate != null && helmet.getItem() instanceof ItemArmor && chestPlate.getItem() instanceof ItemArmor) {
            ItemArmor hArmor = (ItemArmor)helmet.getItem();
            ItemArmor cpArmor = (ItemArmor)chestPlate.getItem();
            if (hArmor.getArmorMaterial() == ItemArmor.ArmorMaterial.LEATHER && cpArmor.getArmorMaterial() == ItemArmor.ArmorMaterial.LEATHER) {
               int hColour = hArmor.getColor(helmet);
               int cpColour = cpArmor.getColor(chestPlate);
               return hColour != cpColour ? 0 : hColour | -16777216;
            } else {
               return 0;
            }
         } else {
            return 0;
         }
      });

      private final String name;
      private final TeamsColourSupplier colourSupplier;
      private final TeamComparator comparator;

      private TeamsMode(String name, TeamsColourSupplier colourSupplier) {
         this.name = name;
         this.colourSupplier = colourSupplier;
         this.comparator = (player1, player2) -> {
            int colour1 = this.getColourSupplier().getTeamColour(player1);
            if (colour1 == 0) {
               return false;
            } else {
               int colour2 = this.getColourSupplier().getTeamColour(player2);
               if (colour2 == 0) {
                  return false;
               } else {
                  return colour1 == colour2;
               }
            }
         };
      }

      public String toString() {
         return this.name;
      }

      public TeamsColourSupplier getColourSupplier() {
         return this.colourSupplier;
      }

      public TeamComparator getComparator() {
         return this.comparator;
      }
   }
}
