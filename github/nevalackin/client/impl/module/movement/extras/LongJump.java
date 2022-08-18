package io.github.nevalackin.client.impl.module.movement.extras;

import io.github.nevalackin.client.api.module.Category;
import io.github.nevalackin.client.api.module.Module;
import io.github.nevalackin.client.util.movement.MovementUtil;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;

public final class LongJump extends Module {
   private double lastDist;
   private double moveSpeed;
   private int stage;
   private int groundTicks;
   @EventLink
   private final Listener onUpdate = (event) -> {
      if (event.isPre()) {
         double xDist = event.getLastTickPosX() - event.getPosX();
         double zDist = event.getLastTickPosZ() - event.getPosZ();
         this.lastDist = Math.sqrt(xDist * xDist + zDist * zDist);
      }

      if (this.mc.thePlayer.onGround && ++this.groundTicks >= 3) {
         this.setEnabled(false);
      }

   };
   @EventLink
   private final Listener onMove = (event) -> {
      double baseMoveSpeed = MovementUtil.getBaseMoveSpeed(this.mc.thePlayer);
      if (MovementUtil.isMoving(this.mc.thePlayer)) {
         if (this.mc.thePlayer.onGround && MovementUtil.isMoving(this.mc.thePlayer) && this.stage == 2) {
            this.moveSpeed = baseMoveSpeed * 1.7000000476837158;
            event.setY(this.mc.thePlayer.motionY = 0.800000011920929);
         } else if (this.stage == 3) {
            double difference = 0.66 * (this.lastDist - baseMoveSpeed);
            this.moveSpeed = this.lastDist - difference;
         } else {
            if (MovementUtil.isOnGround(this.mc.theWorld, this.mc.thePlayer, -this.mc.thePlayer.motionY) || this.mc.thePlayer.isCollidedVertically && this.mc.thePlayer.onGround) {
               this.stage = 1;
            }

            this.moveSpeed = this.lastDist - this.lastDist / 159.0;
         }

         this.moveSpeed = Math.max(this.moveSpeed, baseMoveSpeed);
         MovementUtil.setSpeed(this.mc.thePlayer, event, targetStrafeInstance, this.moveSpeed);
         ++this.stage;
      }

   };

   public LongJump() {
      super("Long Jump", Category.MOVEMENT, Category.SubCategory.MOVEMENT_EXTRAS);
   }

   public void onEnable() {
      if (this.mc.thePlayer != null) {
         MovementUtil.zaneDamage(this.mc.thePlayer);
      }

      this.moveSpeed = 0.221;
      this.lastDist = 0.0;
      this.groundTicks = 0;
      this.stage = 0;
   }

   public void onDisable() {
   }
}
