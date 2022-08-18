package io.github.nevalackin.client.impl.module.movement.main;

import io.github.nevalackin.client.api.module.Category;
import io.github.nevalackin.client.api.module.Module;
import io.github.nevalackin.client.api.property.Property;
import io.github.nevalackin.client.impl.core.KetamineClient;
import io.github.nevalackin.client.impl.event.player.UpdatePositionEvent;
import io.github.nevalackin.client.impl.property.BooleanProperty;
import io.github.nevalackin.client.impl.property.EnumProperty;
import io.github.nevalackin.client.util.movement.JumpUtil;
import io.github.nevalackin.client.util.movement.MovementUtil;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.play.client.C03PacketPlayer;

public final class NoFall extends Module {
   private final EnumProperty modeProperty;
   private final BooleanProperty roundPosProperty;
   private AntiVoid antivoid;
   @EventLink(4)
   private final Listener onUpdatePosition;

   public NoFall() {
      super("No Fall", Category.MOVEMENT, Category.SubCategory.MOVEMENT_MAIN);
      this.modeProperty = new EnumProperty("Mode", NoFall.Mode.EDIT);
      this.roundPosProperty = new BooleanProperty("Round Pos", true, () -> {
         return this.modeProperty.getValue() != NoFall.Mode.NO_GROUND;
      });
      this.onUpdatePosition = (event) -> {
         if (event.isPre()) {
            if (this.antivoid.isEnabled()) {
               boolean overVoid = MovementUtil.isOverVoid(this.mc);
               if (overVoid) {
                  return;
               }
            }

            if (((Mode)this.modeProperty.getValue()).isRequiresRounding()) {
               if (this.canRoundPos(event.getPosY()) && ((Mode)this.modeProperty.getValue()).onUpdatePosition(this.mc.thePlayer, event)) {
                  this.roundPosition(event);
               }
            } else {
               ((Mode)this.modeProperty.getValue()).onUpdatePosition(this.mc.thePlayer, event);
            }
         }

      };
      this.setSuffix(() -> {
         return ((Mode)this.modeProperty.getValue()).toString();
      });
      this.register(new Property[]{this.modeProperty, this.roundPosProperty});
   }

   private boolean canRoundPos(double currentPos) {
      if (this.roundPosProperty.check() && (Boolean)this.roundPosProperty.getValue()) {
         double roundedPos = (double)((float)Math.round(currentPos / 0.015625) * 0.015625F);
         return Math.abs(currentPos - roundedPos) <= 0.005;
      } else {
         return true;
      }
   }

   private void roundPosition(UpdatePositionEvent event) {
      if ((Boolean)this.roundPosProperty.getValue()) {
         event.setPosY((double)((float)Math.round(event.getPosY() / 0.015625) * 0.015625F));
      }

   }

   public void onEnable() {
      if (this.antivoid == null) {
         this.antivoid = (AntiVoid)KetamineClient.getInstance().getModuleManager().getModule(AntiVoid.class);
      }

   }

   public void onDisable() {
   }

   private static enum Mode {
      PACKET("Packet", (player, event) -> {
         if (player.fallDistance > JumpUtil.getMinFallDist(player)) {
            player.sendQueue.sendPacket(new C03PacketPlayer(true));
            player.fallDistance = 0.0F;
            return true;
         } else {
            return false;
         }
      }, true),
      NO_GROUND("No Ground", (player, event) -> {
         event.setOnGround(false);
         return false;
      }, false),
      NONE("None", (OnUpdateFunc)null, false),
      EDIT("Edit", (player, event) -> {
         if (player.fallDistance > JumpUtil.getMinFallDist(player)) {
            event.setOnGround(true);
            player.fallDistance = 0.0F;
            return true;
         } else {
            return false;
         }
      }, true);

      private final String name;
      private final OnUpdateFunc onUpdate;
      private final boolean requiresRounding;

      private Mode(String name, OnUpdateFunc onUpdate, boolean requiresRounding) {
         this.name = name;
         this.onUpdate = onUpdate;
         this.requiresRounding = requiresRounding;
      }

      public boolean onUpdatePosition(EntityPlayerSP player, UpdatePositionEvent event) {
         return this.onUpdate == null ? false : this.onUpdate.onUpdate(player, event);
      }

      public boolean isRequiresRounding() {
         return this.requiresRounding;
      }

      public String toString() {
         return this.name;
      }
   }

   @FunctionalInterface
   private interface OnUpdateFunc {
      boolean onUpdate(EntityPlayerSP var1, UpdatePositionEvent var2);
   }
}
