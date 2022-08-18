package io.github.nevalackin.client.impl.event.render.model;

import io.github.nevalackin.client.api.event.Event;
import io.github.nevalackin.client.impl.event.render.RenderCallback;
import net.minecraft.tileentity.TileEntityChest;

public final class ChestModelRenderEvent implements Event {
   private final TileEntityChest entity;
   private final RenderCallback modelRenderer;
   private final double x;
   private final double y;
   private final double z;

   public ChestModelRenderEvent(TileEntityChest entity, RenderCallback modelRenderer, double x, double y, double z) {
      this.entity = entity;
      this.modelRenderer = modelRenderer;
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public TileEntityChest getEntity() {
      return this.entity;
   }

   public void draw() {
      this.modelRenderer.render();
   }

   public double getX() {
      return this.x;
   }

   public double getY() {
      return this.y;
   }

   public double getZ() {
      return this.z;
   }
}
