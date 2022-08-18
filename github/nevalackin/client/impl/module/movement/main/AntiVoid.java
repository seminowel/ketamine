package io.github.nevalackin.client.impl.module.movement.main;

import io.github.nevalackin.client.api.module.Category;
import io.github.nevalackin.client.api.module.Module;
import io.github.nevalackin.client.api.property.Property;
import io.github.nevalackin.client.impl.property.BooleanProperty;
import io.github.nevalackin.client.impl.property.DoubleProperty;
import io.github.nevalackin.client.util.movement.MovementUtil;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.text.DecimalFormat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;

public class AntiVoid extends Module {
   private boolean sentPacket;
   private int ticksSinceLastAttempt;
   private DecimalFormat format = new DecimalFormat("0.0");
   private final DoubleProperty delayProperty = new DoubleProperty("Delay", 20.0, 0.0, 50.0, 1.0);
   private final BooleanProperty displayDelayProperty = new BooleanProperty("Display Delay", true);
   @EventLink
   private final Listener onUpdate = (event) -> {
      boolean overVoid = MovementUtil.isOverVoid(this.mc);
      if (event.isPre()) {
         ++this.ticksSinceLastAttempt;
         if ((double)this.mc.thePlayer.fallDistance > 3.0 && overVoid && (double)this.ticksSinceLastAttempt >= (Double)this.delayProperty.getValue()) {
            event.setPosY(event.getPosY() + 8.0);
            this.sentPacket = true;
            this.ticksSinceLastAttempt = 0;
         }

      }
   };
   @EventLink
   private final Listener onRenderOverlay = (event) -> {
      ScaledResolution sr = event.getScaledResolution();
      if ((Boolean)this.displayDelayProperty.getValue()) {
         this.mc.fontRendererObj.drawStringWithShadow(String.valueOf(this.ticksSinceLastAttempt), (float)sr.getScaledWidth() / 2.0F - (float)this.mc.fontRendererObj.getStringWidth(String.valueOf(this.ticksSinceLastAttempt)) / 2.0F, (float)sr.getScaledHeight() / 2.0F + 50.0F, -1);
      }

   };
   @EventLink
   private final Listener onReceivePacket = (event) -> {
      if (this.sentPacket && event.getPacket() instanceof S08PacketPlayerPosLook) {
         this.sentPacket = false;
      }

   };

   public AntiVoid() {
      super("Anti Void", Category.MOVEMENT, Category.SubCategory.MOVEMENT_MAIN);
      this.register(new Property[]{this.delayProperty, this.displayDelayProperty});
   }

   public void onEnable() {
      this.ticksSinceLastAttempt = 0;
   }

   public void onDisable() {
   }
}
