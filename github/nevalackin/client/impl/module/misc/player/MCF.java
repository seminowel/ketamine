package io.github.nevalackin.client.impl.module.misc.player;

import io.github.nevalackin.client.api.module.Category;
import io.github.nevalackin.client.api.module.Module;
import io.github.nevalackin.client.impl.core.KetamineClient;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MovingObjectPosition;

public class MCF extends Module {
   @EventLink
   private final Listener onMiddleClick = (event) -> {
      if (this.mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY && this.mc.objectMouseOver.entityHit instanceof EntityPlayer) {
         String s = this.mc.objectMouseOver.entityHit.getName();
         if (!KetamineClient.getInstance().friendManager.isFriend(s)) {
            KetamineClient.getInstance().friendManager.addFriend(s, s);
            this.mc.thePlayer.addChatMessage(new ChatComponentText("[Ketamine]: §aAdded \"" + s + "\"."));
         } else {
            KetamineClient.getInstance().friendManager.deleteFriend(s);
            this.mc.thePlayer.addChatMessage(new ChatComponentText("[Ketamine]: §cDeleted \"" + s + "\"."));
         }
      }

   };

   public MCF() {
      super("MCF", Category.MISC, Category.SubCategory.MISC_PLAYER);
   }

   public void onEnable() {
   }

   public void onDisable() {
   }
}
