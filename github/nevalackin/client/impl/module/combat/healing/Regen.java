package io.github.nevalackin.client.impl.module.combat.healing;

import io.github.nevalackin.client.api.module.Category;
import io.github.nevalackin.client.api.module.Module;
import io.github.nevalackin.client.api.property.Property;
import io.github.nevalackin.client.impl.property.DoubleProperty;
import io.github.nevalackin.client.impl.property.EnumProperty;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.network.play.client.C03PacketPlayer;

public final class Regen extends Module {
   private final EnumProperty modeProperty;
   private final DoubleProperty packetsProperty;
   @EventLink
   private final Listener onUpdatePosition;

   public Regen() {
      super("Regen", Category.COMBAT, Category.SubCategory.COMBAT_HEALING);
      this.modeProperty = new EnumProperty("Mode", Regen.Mode.PACKET);
      this.packetsProperty = new DoubleProperty("Packets", 5.0, 1.0, 20.0, 1.0);
      this.onUpdatePosition = (event) -> {
         if (event.isPre() && event.isOnGround() && this.mc.thePlayer.getHealth() < this.mc.thePlayer.getMaxHealth()) {
            for(int i = 0; (double)i < (Double)this.packetsProperty.getValue(); ++i) {
               this.mc.thePlayer.sendQueue.sendPacket(new C03PacketPlayer(true));
            }
         }

      };
      this.setSuffix(() -> {
         return ((Mode)this.modeProperty.getValue()).toString();
      });
      this.register(new Property[]{this.modeProperty, this.packetsProperty});
   }

   public void onEnable() {
   }

   public void onDisable() {
   }

   private static enum Mode {
      PACKET("Packet");

      private final String name;

      private Mode(String name) {
         this.name = name;
      }

      public String toString() {
         return this.name;
      }
   }
}
