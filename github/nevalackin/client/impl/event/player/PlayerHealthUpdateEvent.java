package io.github.nevalackin.client.impl.event.player;

import io.github.nevalackin.client.api.event.Event;
import net.minecraft.entity.player.EntityPlayer;

public final class PlayerHealthUpdateEvent implements Event {
   private final EntityPlayer player;
   private final float health;
   private final float prevHealth;

   public PlayerHealthUpdateEvent(EntityPlayer player, float health, float prevHealth) {
      this.player = player;
      this.health = health;
      this.prevHealth = prevHealth;
   }

   public EntityPlayer getPlayer() {
      return this.player;
   }

   public float getHealth() {
      return this.health;
   }

   public float getPrevHealth() {
      return this.prevHealth;
   }
}
