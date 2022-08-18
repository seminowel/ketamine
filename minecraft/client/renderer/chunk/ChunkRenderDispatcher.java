package net.minecraft.client.renderer.chunk;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.client.renderer.VertexBufferUploader;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.util.EnumWorldBlockLayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

public class ChunkRenderDispatcher {
   private static final Logger logger = LogManager.getLogger();
   private static final ThreadFactory threadFactory = (new ThreadFactoryBuilder()).setNameFormat("Chunk Batcher %d").setDaemon(true).build();
   private final List listThreadedWorkers = Lists.newArrayList();
   private final BlockingQueue queueChunkUpdates = Queues.newArrayBlockingQueue(100);
   private final BlockingQueue queueFreeRenderBuilders = Queues.newArrayBlockingQueue(5);
   private final WorldVertexBufferUploader worldVertexUploader = new WorldVertexBufferUploader();
   private final VertexBufferUploader vertexUploader = new VertexBufferUploader();
   private final Queue queueChunkUploads = Queues.newArrayDeque();
   private final ChunkRenderWorker renderWorker;

   public ChunkRenderDispatcher() {
      int j;
      for(j = 0; j < 2; ++j) {
         ChunkRenderWorker chunkrenderworker = new ChunkRenderWorker(this);
         Thread thread = threadFactory.newThread(chunkrenderworker);
         thread.start();
         this.listThreadedWorkers.add(chunkrenderworker);
      }

      for(j = 0; j < 5; ++j) {
         this.queueFreeRenderBuilders.add(new RegionRenderCacheBuilder());
      }

      this.renderWorker = new ChunkRenderWorker(this, new RegionRenderCacheBuilder());
   }

   public String getDebugInfo() {
      return String.format("pC: %03d, pU: %1d, aB: %1d", this.queueChunkUpdates.size(), this.queueChunkUploads.size(), this.queueFreeRenderBuilders.size());
   }

   public boolean runChunkUploads(long p_178516_1_) {
      boolean flag = false;

      long i;
      do {
         boolean flag1 = false;
         synchronized(this.queueChunkUploads) {
            if (!this.queueChunkUploads.isEmpty()) {
               ((ListenableFutureTask)this.queueChunkUploads.poll()).run();
               flag1 = true;
               flag = true;
            }
         }

         if (p_178516_1_ == 0L || !flag1) {
            break;
         }

         i = p_178516_1_ - System.nanoTime();
      } while(i >= 0L);

      return flag;
   }

   public boolean updateChunkLater(RenderChunk chunkRenderer) {
      chunkRenderer.getLockCompileTask().lock();

      boolean flag1;
      try {
         final ChunkCompileTaskGenerator chunkcompiletaskgenerator = chunkRenderer.makeCompileTaskChunk();
         chunkcompiletaskgenerator.addFinishRunnable(new Runnable() {
            public void run() {
               ChunkRenderDispatcher.this.queueChunkUpdates.remove(chunkcompiletaskgenerator);
            }
         });
         boolean flag = this.queueChunkUpdates.offer(chunkcompiletaskgenerator);
         if (!flag) {
            chunkcompiletaskgenerator.finish();
         }

         flag1 = flag;
      } finally {
         chunkRenderer.getLockCompileTask().unlock();
      }

      return flag1;
   }

   public boolean updateChunkNow(RenderChunk chunkRenderer) {
      chunkRenderer.getLockCompileTask().lock();

      boolean flag;
      try {
         ChunkCompileTaskGenerator chunkcompiletaskgenerator = chunkRenderer.makeCompileTaskChunk();

         try {
            this.renderWorker.processTask(chunkcompiletaskgenerator);
         } catch (InterruptedException var8) {
         }

         flag = true;
      } finally {
         chunkRenderer.getLockCompileTask().unlock();
      }

      return flag;
   }

   public void stopChunkUpdates() {
      this.clearChunkUpdates();

      while(this.runChunkUploads(0L)) {
      }

      List list = Lists.newArrayList();

      while(list.size() != 5) {
         try {
            list.add(this.allocateRenderBuilder());
         } catch (InterruptedException var3) {
         }
      }

      this.queueFreeRenderBuilders.addAll(list);
   }

   public void freeRenderBuilder(RegionRenderCacheBuilder p_178512_1_) {
      this.queueFreeRenderBuilders.add(p_178512_1_);
   }

   public RegionRenderCacheBuilder allocateRenderBuilder() throws InterruptedException {
      return (RegionRenderCacheBuilder)this.queueFreeRenderBuilders.take();
   }

   public ChunkCompileTaskGenerator getNextChunkUpdate() throws InterruptedException {
      return (ChunkCompileTaskGenerator)this.queueChunkUpdates.take();
   }

   public boolean updateTransparencyLater(RenderChunk chunkRenderer) {
      chunkRenderer.getLockCompileTask().lock();

      boolean var4;
      try {
         final ChunkCompileTaskGenerator chunkcompiletaskgenerator = chunkRenderer.makeCompileTaskTransparency();
         boolean flag;
         if (chunkcompiletaskgenerator != null) {
            chunkcompiletaskgenerator.addFinishRunnable(new Runnable() {
               public void run() {
                  ChunkRenderDispatcher.this.queueChunkUpdates.remove(chunkcompiletaskgenerator);
               }
            });
            flag = this.queueChunkUpdates.offer(chunkcompiletaskgenerator);
            return flag;
         }

         flag = true;
         var4 = flag;
      } finally {
         chunkRenderer.getLockCompileTask().unlock();
      }

      return var4;
   }

   public ListenableFuture uploadChunk(final EnumWorldBlockLayer player, final WorldRenderer p_178503_2_, final RenderChunk chunkRenderer, final CompiledChunk compiledChunkIn) {
      if (Minecraft.getMinecraft().isCallingFromMinecraftThread()) {
         if (OpenGlHelper.useVbo()) {
            this.uploadVertexBuffer(p_178503_2_, chunkRenderer.getVertexBufferByLayer(player.ordinal()));
         } else {
            this.uploadDisplayList(p_178503_2_, ((ListedRenderChunk)chunkRenderer).getDisplayList(player, compiledChunkIn), chunkRenderer);
         }

         p_178503_2_.setTranslation(0.0, 0.0, 0.0);
         return Futures.immediateFuture((Object)null);
      } else {
         ListenableFutureTask listenablefuturetask = ListenableFutureTask.create(new Runnable() {
            public void run() {
               ChunkRenderDispatcher.this.uploadChunk(player, p_178503_2_, chunkRenderer, compiledChunkIn);
            }
         }, (Object)null);
         synchronized(this.queueChunkUploads) {
            this.queueChunkUploads.add(listenablefuturetask);
            return listenablefuturetask;
         }
      }
   }

   private void uploadDisplayList(WorldRenderer p_178510_1_, int p_178510_2_, RenderChunk chunkRenderer) {
      GL11.glNewList(p_178510_2_, 4864);
      GL11.glPushMatrix();
      chunkRenderer.multModelviewMatrix();
      this.worldVertexUploader.func_181679_a(p_178510_1_);
      GL11.glPopMatrix();
      GL11.glEndList();
   }

   private void uploadVertexBuffer(WorldRenderer p_178506_1_, VertexBuffer vertexBufferIn) {
      this.vertexUploader.setVertexBuffer(vertexBufferIn);
      this.vertexUploader.func_181679_a(p_178506_1_);
   }

   public void clearChunkUpdates() {
      while(!this.queueChunkUpdates.isEmpty()) {
         ChunkCompileTaskGenerator chunkcompiletaskgenerator = (ChunkCompileTaskGenerator)this.queueChunkUpdates.poll();
         if (chunkcompiletaskgenerator != null) {
            chunkcompiletaskgenerator.finish();
         }
      }

   }
}
