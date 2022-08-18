package io.github.nevalackin.client.impl.event.render.world;

import io.github.nevalackin.client.api.event.Event;
import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;

public final class BlockSideRenderEvent implements Event {
   private final Block block;
   private Callback callback;

   public BlockSideRenderEvent(Block block, Callback callback) {
      this.block = block;
      this.callback = callback;
   }

   public Callback getCallback() {
      return this.callback;
   }

   public void setCallback(Callback callback) {
      this.callback = callback;
   }

   public Block getBlock() {
      return this.block;
   }

   @FunctionalInterface
   public interface Callback {
      boolean shouldRenderSide(IBlockAccess var1, BlockPos var2, EnumFacing var3);
   }
}
