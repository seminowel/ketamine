package io.github.nevalackin.client.impl.module.misc.world;

import io.github.nevalackin.client.api.module.Category;
import io.github.nevalackin.client.api.module.Module;
import io.github.nevalackin.client.util.misc.TimeUtil;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.util.Iterator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.util.ChatComponentText;

public class EntityAura extends Module {
   private boolean interacted;
   private TimeUtil delay = new TimeUtil();
   @EventLink
   public final Listener onUpdatePositionEvent = (event) -> {
      if (this.interacted && this.delay.passed(1000L)) {
         this.interacted = false;
         this.mc.thePlayer.addChatMessage(new ChatComponentText("Stopped Interacting With Entity"));
         this.delay.reset();
      }

      Iterator var2 = this.mc.theWorld.loadedEntityList.iterator();

      while(true) {
         Entity target;
         do {
            do {
               if (!var2.hasNext()) {
                  return;
               }

               target = (Entity)var2.next();
            } while(this.interacted);
         } while(!(target instanceof EntityBlaze) && !(target instanceof EntitySpider) && !(target instanceof EntityCreeper) && !(target instanceof EntityEnderman) && !(target instanceof EntityZombie) && !(target instanceof EntitySkeleton) && !(target instanceof EntityGhast) && !(target instanceof EntityPig) && !(target instanceof EntitySheep));

         if (this.mc.thePlayer.getDistanceToEntity(target) < 6.0F) {
            this.mc.playerController.interactWithEntitySendPacket(this.mc.thePlayer, target);
            this.interacted = true;
            this.mc.thePlayer.addChatMessage(new ChatComponentText("Interacted With Entity"));
         }
      }
   };

   public EntityAura() {
      super("Entity Desync", Category.MISC, Category.SubCategory.MISC_WORLD);
   }

   public void onEnable() {
      this.interacted = false;
   }

   public void onDisable() {
   }
}
