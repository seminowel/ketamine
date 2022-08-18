package io.github.nevalackin.client.impl.module.render.world;

import io.github.nevalackin.client.api.module.Category;
import io.github.nevalackin.client.api.module.Module;
import io.github.nevalackin.client.impl.event.render.world.BlockSideRenderEvent;
import io.github.nevalackin.client.impl.property.BooleanProperty;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.util.Arrays;
import java.util.List;
import net.minecraft.block.BlockMobSpawner;
import net.minecraft.block.BlockOre;

public final class XRay extends Module {
   private final List blocksToFind = Arrays.asList(BlockOre.class, BlockMobSpawner.class);
   private final BooleanProperty xrayBypassProperty = new BooleanProperty("Bypass", true);
   private BlockSideRenderEvent.Callback opaqueCallback;
   private float prevGamma;
   @EventLink
   private final Listener onGetLightLevel = (event) -> {
      event.setLightLevel(1);
   };
   @EventLink
   private final Listener onBlockRender = (event) -> {
      if (!this.blocksToFind.contains(event.getBlock().getClass())) {
         event.setCancelled();
      }

   };
   @EventLink
   private final Listener onSideRender = (event) -> {
      if (!(Boolean)this.xrayBypassProperty.getValue()) {
         event.setCallback((worldIn, pos, side) -> {
            return true;
         });
      } else {
         if (this.opaqueCallback == null) {
            this.opaqueCallback = (worldIn, pos, side) -> {
               return worldIn.isAirBlock(pos.offset(side));
            };
         }

         event.setCallback(this.opaqueCallback);
      }
   };

   public XRay() {
      super("XRay", Category.RENDER, Category.SubCategory.RENDER_WORLD);
   }

   public void onEnable() {
      this.prevGamma = this.mc.gameSettings.gammaSetting;
      this.mc.gameSettings.gammaSetting = 1000.0F;
      if (this.mc.renderGlobal != null) {
         this.mc.renderGlobal.loadRenderers();
      }

   }

   public void onDisable() {
      this.mc.gameSettings.gammaSetting = this.prevGamma;
      if (this.mc.renderGlobal != null) {
         this.mc.renderGlobal.loadRenderers();
      }

   }
}
