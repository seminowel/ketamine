package io.github.nevalackin.client.impl.module.movement.main;

import io.github.nevalackin.client.api.module.Category;
import io.github.nevalackin.client.api.module.Module;
import io.github.nevalackin.client.api.property.Property;
import io.github.nevalackin.client.impl.property.BooleanProperty;
import io.github.nevalackin.client.util.movement.MovementUtil;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;

public final class Sprint extends Module {
   private final BooleanProperty omniProperty = new BooleanProperty("Multi-Directional", true);
   @EventLink
   private final Listener onSendSprint = (event) -> {
      if (MovementUtil.canSprint(this.mc.thePlayer, (Boolean)this.omniProperty.getValue())) {
         event.setSprintState(true);
         this.mc.thePlayer.setSprinting(true);
      }

   };

   public Sprint() {
      super("Sprint", Category.MOVEMENT, Category.SubCategory.MOVEMENT_MAIN);
      this.register(new Property[]{this.omniProperty});
   }

   public void onEnable() {
   }

   public void onDisable() {
   }
}
