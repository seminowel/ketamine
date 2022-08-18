package io.github.nevalackin.client.impl.module.misc.player;

import io.github.nevalackin.client.api.module.Category;
import io.github.nevalackin.client.api.module.Module;
import io.github.nevalackin.client.api.property.Property;
import io.github.nevalackin.client.impl.property.DoubleProperty;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.network.play.client.C03PacketPlayer;

public final class FastUse extends Module {
   private final DoubleProperty delayProperty = new DoubleProperty("Delay", 16.0, 1.0, 33.0, 1.0);
   @EventLink
   private final Listener onUpdate = (event) -> {
      if (event.isPre() && event.isOnGround() && (double)this.mc.thePlayer.getItemInUseDuration() == (Double)this.delayProperty.getValue()) {
         for(int i = 0; (double)i < 33.0 - (Double)this.delayProperty.getValue(); ++i) {
            this.mc.thePlayer.sendQueue.sendPacket(new C03PacketPlayer(true));
         }
      }

   };

   public FastUse() {
      super("Fast Use", Category.MISC, Category.SubCategory.MISC_PLAYER);
      this.delayProperty.addValueAlias(1.0, "Instant");
      this.register(new Property[]{this.delayProperty});
      DoubleProperty var10001 = this.delayProperty;
      this.setSuffix(var10001::getDisplayString);
   }

   public void onEnable() {
   }

   public void onDisable() {
   }
}
