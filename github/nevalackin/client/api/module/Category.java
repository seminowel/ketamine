package io.github.nevalackin.client.api.module;

public enum Category {
   COMBAT("Combat", 1358905941, new SubCategory[]{Category.SubCategory.COMBAT_RAGE, Category.SubCategory.COMBAT_LEGIT, Category.SubCategory.COMBAT_MINI_GAMES, Category.SubCategory.COMBAT_HEALING}),
   MOVEMENT("Movement", 1342362879, new SubCategory[]{Category.SubCategory.MOVEMENT_MAIN, Category.SubCategory.MOVEMENT_EXTRAS}),
   MISC("Miscellaneous", 1346502402, new SubCategory[]{Category.SubCategory.MISC_INVENTORY, Category.SubCategory.MISC_WORLD, Category.SubCategory.MISC_PLAYER}),
   RENDER("Render", 1354695423, new SubCategory[]{Category.SubCategory.RENDER_MODEL, Category.SubCategory.RENDER_ESP, Category.SubCategory.RENDER_WORLD, Category.SubCategory.RENDER_SELF, Category.SubCategory.RENDER_OVERLAY});

   private final String name;
   private final SubCategory[] subCategories;
   private final int colour;

   public String getCategoryName() {
      return this.name;
   }

   private Category(String name, int colour, SubCategory... subCategories) {
      this.name = name;
      this.colour = colour;
      this.subCategories = subCategories;
   }

   public SubCategory[] getSubCategories() {
      return this.subCategories;
   }

   public int getColour() {
      return this.colour;
   }

   public String toString() {
      return this.name;
   }

   public static enum SubCategory {
      COMBAT_RAGE("Rage"),
      COMBAT_LEGIT("Legit"),
      COMBAT_MINI_GAMES("Mini Games"),
      COMBAT_HEALING("Healing"),
      MOVEMENT_MAIN("Main"),
      MOVEMENT_EXTRAS("Extras"),
      MISC_INVENTORY("Inventory"),
      MISC_WORLD("World"),
      MISC_PLAYER("Player"),
      RENDER_MODEL("Model"),
      RENDER_ESP("ESP"),
      RENDER_WORLD("World"),
      RENDER_SELF("Self"),
      RENDER_OVERLAY("Overlay");

      private final String name;

      private SubCategory(String name) {
         this.name = name;
      }

      public String toString() {
         return this.name;
      }
   }
}
