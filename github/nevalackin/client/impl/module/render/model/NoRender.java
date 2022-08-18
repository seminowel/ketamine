package io.github.nevalackin.client.impl.module.render.model;

import io.github.nevalackin.client.api.module.Category;
import io.github.nevalackin.client.api.module.Module;
import io.github.nevalackin.client.api.property.Property;
import io.github.nevalackin.client.impl.property.BooleanProperty;
import io.github.nevalackin.client.util.player.TeamsUtil;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

public final class NoRender extends Module {
   private final BooleanProperty drawTeammatesProperty = new BooleanProperty("Draw Teammates", true);
   @EventLink(6)
   private final Listener onRenderLivingEntity = (event) -> {
      EntityLivingBase entity = event.getEntity();
      if (!(Boolean)this.drawTeammatesProperty.getValue() && entity instanceof EntityOtherPlayerMP && TeamsUtil.TeamsMode.NAME.getComparator().isOnSameTeam(this.mc.thePlayer, (EntityPlayer)entity)) {
         event.setCancelled();
      }

   };

   public NoRender() {
      super("No Render", Category.RENDER, Category.SubCategory.RENDER_MODEL);
      this.register(new Property[]{this.drawTeammatesProperty});
   }

   public void onEnable() {
   }

   public void onDisable() {
   }
}
