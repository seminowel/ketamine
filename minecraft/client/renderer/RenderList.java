package net.minecraft.client.renderer;

import java.util.Iterator;
import net.minecraft.client.renderer.chunk.ListedRenderChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.EnumWorldBlockLayer;
import org.lwjgl.opengl.GL11;

public class RenderList extends ChunkRenderContainer {
   public void renderChunkLayer(EnumWorldBlockLayer layer) {
      if (this.initialized) {
         Iterator var2 = this.renderChunks.iterator();

         while(var2.hasNext()) {
            RenderChunk renderchunk = (RenderChunk)var2.next();
            ListedRenderChunk listedrenderchunk = (ListedRenderChunk)renderchunk;
            GL11.glPushMatrix();
            this.preRenderChunk(renderchunk);
            GL11.glCallList(listedrenderchunk.getDisplayList(layer, listedrenderchunk.getCompiledChunk()));
            GL11.glPopMatrix();
         }

         this.renderChunks.clear();
      }

   }
}
