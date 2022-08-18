package io.github.nevalackin.client.impl.module.combat.rage;

import io.github.nevalackin.client.api.module.Category;
import io.github.nevalackin.client.api.module.Module;
import io.github.nevalackin.client.api.property.Property;
import io.github.nevalackin.client.impl.core.KetamineClient;
import io.github.nevalackin.client.impl.property.DoubleProperty;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S27PacketExplosion;

public final class Velocity extends Module {
   private final DoubleProperty horizontalPercentageProperty = new DoubleProperty("Horizontal", 0.0, 0.0, 100.0, 1.0);
   private final DoubleProperty verticalPercentageProperty = new DoubleProperty("Vertical", 0.0, 0.0, 100.0, 1.0);
   private Aura aura;
   @EventLink
   private final Listener onReceivePacket = (event) -> {
      Packet packet = event.getPacket();
      if (packet instanceof S12PacketEntityVelocity) {
         S12PacketEntityVelocity velocity = (S12PacketEntityVelocity)packet;
         double x = (double)velocity.getMotionX() / 8000.0;
         double y = (double)velocity.getMotionY() / 8000.0;
         double z = (double)velocity.getMotionZ() / 8000.0;
         if (velocity.getEntityID() != this.mc.thePlayer.getEntityId()) {
            return;
         }

         float verticalPercentagex = ((Double)this.verticalPercentageProperty.getValue()).floatValue() / 100.0F;
         float horizontalPercentage = ((Double)this.horizontalPercentageProperty.getValue()).floatValue() / 100.0F;
         if (verticalPercentagex == 0.0F && horizontalPercentage == 0.0F) {
            event.setCancelled();
            return;
         }

         velocity.setMotionX((int)Math.ceil((double)((float)velocity.getMotionX() * horizontalPercentage)));
         velocity.setMotionY((int)Math.ceil((double)((float)velocity.getMotionY() * verticalPercentagex)));
         velocity.setMotionZ((int)Math.ceil((double)((float)velocity.getMotionZ() * horizontalPercentage)));
      } else if (packet instanceof S27PacketExplosion) {
         S27PacketExplosion explosion = (S27PacketExplosion)packet;
         float verticalPercentage = ((Double)this.verticalPercentageProperty.getValue()).floatValue() / 100.0F;
         float horizontalPercentagex = ((Double)this.horizontalPercentageProperty.getValue()).floatValue() / 100.0F;
         if (verticalPercentage == 0.0F && horizontalPercentagex == 0.0F) {
            event.setCancelled();
            return;
         }

         explosion.setMotionY(explosion.getMotionY() * verticalPercentage);
         explosion.setMotionX(explosion.getMotionX() * horizontalPercentagex);
         explosion.setMotionZ(explosion.getMotionZ() * horizontalPercentagex);
      }

   };

   public Velocity() {
      super("Velocity", Category.COMBAT, Category.SubCategory.COMBAT_RAGE);
      this.setSuffix(() -> {
         return String.format("%s%% %s%%", ((Double)this.horizontalPercentageProperty.getValue()).intValue(), ((Double)this.verticalPercentageProperty.getValue()).intValue());
      });
      this.register(new Property[]{this.horizontalPercentageProperty, this.verticalPercentageProperty});
   }

   public void onEnable() {
      if (this.aura == null) {
         this.aura = (Aura)KetamineClient.getInstance().getModuleManager().getModule(Aura.class);
      }

   }

   public void onDisable() {
   }
}
