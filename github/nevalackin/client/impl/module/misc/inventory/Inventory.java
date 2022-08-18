package io.github.nevalackin.client.impl.module.misc.inventory;

import io.github.nevalackin.client.api.module.Category;
import io.github.nevalackin.client.api.module.Module;
import io.github.nevalackin.client.api.property.Property;
import io.github.nevalackin.client.impl.property.BooleanProperty;

public final class Inventory extends Module {
   private final BooleanProperty moveInsideGUIProperty = new BooleanProperty("Move Inside GUIs", true);

   public Inventory() {
      super("Inventory+", Category.MISC, Category.SubCategory.MISC_INVENTORY);
      this.register(new Property[]{this.moveInsideGUIProperty});
   }

   public void onEnable() {
   }

   public void onDisable() {
   }
}
