package io.github.nevalackin.client.impl.module.movement.main;

import io.github.nevalackin.client.api.module.Category;
import io.github.nevalackin.client.api.module.Module;
import io.github.nevalackin.client.api.property.Property;
import io.github.nevalackin.client.impl.module.combat.rage.Aura;
import io.github.nevalackin.client.impl.property.BooleanProperty;
import io.github.nevalackin.client.impl.property.EnumProperty;
import io.github.nevalackin.client.util.movement.MovementUtil;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;

public final class NoSlowDown extends Module {
   private static final C07PacketPlayerDigging UNBLOCK_PACKET;
   private static final C08PacketPlayerBlockPlacement BLOCK_PACKET;
   private final EnumProperty modeProperty;
   private final BooleanProperty interactProperty;
   @EventLink
   private final Listener onUseSlowDown;
   @EventLink
   private final Listener onUpdatePosition;

   public NoSlowDown() {
      super("No Slow", Category.MOVEMENT, Category.SubCategory.MOVEMENT_MAIN);
      this.modeProperty = new EnumProperty("Mode", NoSlowDown.Mode.Vanilla);
      this.interactProperty = new BooleanProperty("Interact", true, () -> {
         return this.modeProperty.getValue() == NoSlowDown.Mode.NCP;
      });
      this.onUseSlowDown = (event) -> {
         event.setCancelled();
      };
      this.onUpdatePosition = (event) -> {
         switch ((Mode)this.modeProperty.getValue()) {
            case NCP:
               if (this.mc.thePlayer.isBlocking() && !Aura.canAutoBlock() && MovementUtil.isMoving(this.mc.thePlayer)) {
                  if (event.isPre()) {
                     this.mc.thePlayer.sendQueue.sendPacket(UNBLOCK_PACKET);
                  } else {
                     this.mc.thePlayer.sendQueue.sendPacket(BLOCK_PACKET);
                     if ((Boolean)this.interactProperty.getValue() && this.mc.objectMouseOver != null && this.mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY && this.mc.objectMouseOver.entityHit != null) {
                        this.mc.playerController.interactWithEntitySendPacket(this.mc.thePlayer, this.mc.objectMouseOver.entityHit);
                     }
                  }
               }
               break;
            case Watchdog:
               if (this.mc.thePlayer.isUsingItem() && !Aura.canAutoBlock() && MovementUtil.isMoving(this.mc.thePlayer)) {
                  this.mc.thePlayer.sendQueue.sendPacket(new C09PacketHeldItemChange(this.mc.thePlayer.inventory.currentItem));
               }
         }

      };
      this.register(new Property[]{this.modeProperty, this.interactProperty});
   }

   public void onEnable() {
   }

   public void onDisable() {
   }

   static {
      UNBLOCK_PACKET = new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN);
      BLOCK_PACKET = new C08PacketPlayerBlockPlacement(new BlockPos(-1, -1, -1), 255, (ItemStack)null, 0.0F, 0.0F, 0.0F);
   }

   private static enum Mode {
      Vanilla,
      NCP,
      Watchdog;
   }
}
