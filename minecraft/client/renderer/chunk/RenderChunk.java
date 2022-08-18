package net.minecraft.client.renderer.chunk;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.github.nevalackin.client.impl.core.KetamineClient;
import io.github.nevalackin.client.impl.event.render.world.BlockRenderEvent;
import java.nio.FloatBuffer;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RegionRenderCache;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

public class RenderChunk {
   private World world;
   private final RenderGlobal renderGlobal;
   public static int renderChunksUpdated;
   private BlockPos position;
   public CompiledChunk compiledChunk;
   private final ReentrantLock lockCompileTask;
   private final ReentrantLock lockCompiledChunk;
   private ChunkCompileTaskGenerator compileTask;
   private final Set field_181056_j;
   private final int index;
   private final FloatBuffer modelviewMatrix;
   private final VertexBuffer[] vertexBuffers;
   public AxisAlignedBB boundingBox;
   private int frameIndex;
   private boolean needsUpdate;
   private EnumMap field_181702_p;

   public RenderChunk(World worldIn, RenderGlobal renderGlobalIn, BlockPos blockPosIn, int indexIn) {
      this.compiledChunk = CompiledChunk.DUMMY;
      this.lockCompileTask = new ReentrantLock();
      this.lockCompiledChunk = new ReentrantLock();
      this.compileTask = null;
      this.field_181056_j = Sets.newHashSet();
      this.modelviewMatrix = GLAllocation.createDirectFloatBuffer(16);
      this.vertexBuffers = new VertexBuffer[EnumWorldBlockLayer.values().length];
      this.frameIndex = -1;
      this.needsUpdate = true;
      this.field_181702_p = Maps.newEnumMap(EnumFacing.class);
      this.world = worldIn;
      this.renderGlobal = renderGlobalIn;
      this.index = indexIn;
      if (!blockPosIn.equals(this.getPosition())) {
         this.setPosition(blockPosIn);
      }

      if (OpenGlHelper.useVbo()) {
         for(int i = 0; i < EnumWorldBlockLayer.values().length; ++i) {
            this.vertexBuffers[i] = new VertexBuffer(DefaultVertexFormats.BLOCK);
         }
      }

   }

   public boolean setFrameIndex(int frameIndexIn) {
      if (this.frameIndex == frameIndexIn) {
         return false;
      } else {
         this.frameIndex = frameIndexIn;
         return true;
      }
   }

   public VertexBuffer getVertexBufferByLayer(int layer) {
      return this.vertexBuffers[layer];
   }

   public void setPosition(BlockPos pos) {
      this.stopCompileTask();
      this.position = pos;
      this.boundingBox = new AxisAlignedBB(pos, pos.add(16, 16, 16));
      EnumFacing[] var2 = EnumFacing.values();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         EnumFacing enumfacing = var2[var4];
         this.field_181702_p.put(enumfacing, pos.offset(enumfacing, 16));
      }

      this.initModelviewMatrix();
   }

   public void resortTransparency(float x, float y, float z, ChunkCompileTaskGenerator generator) {
      CompiledChunk compiledchunk = generator.getCompiledChunk();
      if (compiledchunk.getState() != null && !compiledchunk.isLayerEmpty(EnumWorldBlockLayer.TRANSLUCENT)) {
         this.preRenderBlocks(generator.getRegionRenderCacheBuilder().getWorldRendererByLayer(EnumWorldBlockLayer.TRANSLUCENT), this.position);
         generator.getRegionRenderCacheBuilder().getWorldRendererByLayer(EnumWorldBlockLayer.TRANSLUCENT).setVertexState(compiledchunk.getState());
         this.postRenderBlocks(EnumWorldBlockLayer.TRANSLUCENT, x, y, z, generator.getRegionRenderCacheBuilder().getWorldRendererByLayer(EnumWorldBlockLayer.TRANSLUCENT), compiledchunk);
      }

   }

   public void rebuildChunk(float x, float y, float z, ChunkCompileTaskGenerator generator) {
      CompiledChunk compiledchunk = new CompiledChunk();
      int i = true;
      BlockPos blockpos = this.position;
      BlockPos blockpos1 = blockpos.add(15, 15, 15);
      generator.getLock().lock();

      RegionRenderCache iblockaccess;
      label237: {
         try {
            if (generator.getStatus() == ChunkCompileTaskGenerator.Status.COMPILING) {
               iblockaccess = new RegionRenderCache(this.world, blockpos.add(-1, -1, -1), blockpos1.add(1, 1, 1), 1);
               generator.setCompiledChunk(compiledchunk);
               break label237;
            }
         } finally {
            generator.getLock().unlock();
         }

         return;
      }

      VisGraph lvt_10_1_ = new VisGraph();
      HashSet lvt_11_1_ = Sets.newHashSet();
      if (!iblockaccess.extendedLevelsInChunkCache()) {
         ++renderChunksUpdated;
         boolean[] aboolean = new boolean[EnumWorldBlockLayer.values().length];
         BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
         Iterator var14 = BlockPos.getAllInBoxMutable(blockpos, blockpos1).iterator();

         while(var14.hasNext()) {
            BlockPos.MutableBlockPos blockpos$mutableblockpos = (BlockPos.MutableBlockPos)var14.next();
            IBlockState iblockstate = iblockaccess.getBlockState(blockpos$mutableblockpos);
            Block block = iblockstate.getBlock();
            if (block.isOpaqueCube()) {
               lvt_10_1_.func_178606_a(blockpos$mutableblockpos);
            }

            if (block.hasTileEntity()) {
               TileEntity tileentity = iblockaccess.getTileEntity(new BlockPos(blockpos$mutableblockpos));
               TileEntitySpecialRenderer tileentityspecialrenderer = TileEntityRendererDispatcher.instance.getSpecialRenderer(tileentity);
               if (tileentity != null && tileentityspecialrenderer != null) {
                  compiledchunk.addTileEntity(tileentity);
                  if (tileentityspecialrenderer.func_181055_a()) {
                     lvt_11_1_.add(tileentity);
                  }
               }
            }

            EnumWorldBlockLayer enumworldblocklayer1 = block.getBlockLayer();
            int j = enumworldblocklayer1.ordinal();
            if (block.getRenderType() != -1) {
               WorldRenderer worldrenderer = generator.getRegionRenderCacheBuilder().getWorldRendererByLayerId(j);
               if (!compiledchunk.isLayerStarted(enumworldblocklayer1)) {
                  compiledchunk.setLayerStarted(enumworldblocklayer1);
                  this.preRenderBlocks(worldrenderer, blockpos);
               }

               BlockRenderEvent event = new BlockRenderEvent(block);
               KetamineClient.getInstance().getEventBus().post(event);
               if (!event.isCancelled()) {
                  aboolean[j] |= blockrendererdispatcher.renderBlock(iblockstate, blockpos$mutableblockpos, iblockaccess, worldrenderer);
               }
            }
         }

         EnumWorldBlockLayer[] var31 = EnumWorldBlockLayer.values();
         int var32 = var31.length;

         for(int var33 = 0; var33 < var32; ++var33) {
            EnumWorldBlockLayer enumworldblocklayer = var31[var33];
            if (aboolean[enumworldblocklayer.ordinal()]) {
               compiledchunk.setLayerUsed(enumworldblocklayer);
            }

            if (compiledchunk.isLayerStarted(enumworldblocklayer)) {
               this.postRenderBlocks(enumworldblocklayer, x, y, z, generator.getRegionRenderCacheBuilder().getWorldRendererByLayer(enumworldblocklayer), compiledchunk);
            }
         }
      }

      compiledchunk.setVisibility(lvt_10_1_.computeVisibility());
      this.lockCompileTask.lock();

      try {
         Set set = Sets.newHashSet(lvt_11_1_);
         Set set1 = Sets.newHashSet(this.field_181056_j);
         set.removeAll(this.field_181056_j);
         set1.removeAll(lvt_11_1_);
         this.field_181056_j.clear();
         this.field_181056_j.addAll(lvt_11_1_);
         this.renderGlobal.func_181023_a(set1, set);
      } finally {
         this.lockCompileTask.unlock();
      }

   }

   protected void finishCompileTask() {
      this.lockCompileTask.lock();

      try {
         if (this.compileTask != null && this.compileTask.getStatus() != ChunkCompileTaskGenerator.Status.DONE) {
            this.compileTask.finish();
            this.compileTask = null;
         }
      } finally {
         this.lockCompileTask.unlock();
      }

   }

   public ReentrantLock getLockCompileTask() {
      return this.lockCompileTask;
   }

   public ChunkCompileTaskGenerator makeCompileTaskChunk() {
      this.lockCompileTask.lock();

      ChunkCompileTaskGenerator chunkcompiletaskgenerator;
      try {
         this.finishCompileTask();
         this.compileTask = new ChunkCompileTaskGenerator(this, ChunkCompileTaskGenerator.Type.REBUILD_CHUNK);
         chunkcompiletaskgenerator = this.compileTask;
      } finally {
         this.lockCompileTask.unlock();
      }

      return chunkcompiletaskgenerator;
   }

   public ChunkCompileTaskGenerator makeCompileTaskTransparency() {
      this.lockCompileTask.lock();

      ChunkCompileTaskGenerator chunkcompiletaskgenerator;
      try {
         if (this.compileTask == null || this.compileTask.getStatus() != ChunkCompileTaskGenerator.Status.PENDING) {
            if (this.compileTask != null && this.compileTask.getStatus() != ChunkCompileTaskGenerator.Status.DONE) {
               this.compileTask.finish();
               this.compileTask = null;
            }

            this.compileTask = new ChunkCompileTaskGenerator(this, ChunkCompileTaskGenerator.Type.RESORT_TRANSPARENCY);
            this.compileTask.setCompiledChunk(this.compiledChunk);
            chunkcompiletaskgenerator = this.compileTask;
            ChunkCompileTaskGenerator var2 = chunkcompiletaskgenerator;
            return var2;
         }

         chunkcompiletaskgenerator = null;
      } finally {
         this.lockCompileTask.unlock();
      }

      return chunkcompiletaskgenerator;
   }

   private void preRenderBlocks(WorldRenderer worldRendererIn, BlockPos pos) {
      worldRendererIn.begin(7, DefaultVertexFormats.BLOCK);
      worldRendererIn.setTranslation((double)(-pos.getX()), (double)(-pos.getY()), (double)(-pos.getZ()));
   }

   private void postRenderBlocks(EnumWorldBlockLayer layer, float x, float y, float z, WorldRenderer worldRendererIn, CompiledChunk compiledChunkIn) {
      if (layer == EnumWorldBlockLayer.TRANSLUCENT && !compiledChunkIn.isLayerEmpty(layer)) {
         worldRendererIn.func_181674_a(x, y, z);
         compiledChunkIn.setState(worldRendererIn.func_181672_a());
      }

      worldRendererIn.finishDrawing();
   }

   private void initModelviewMatrix() {
      GL11.glPushMatrix();
      GL11.glLoadIdentity();
      float f = 1.000001F;
      GL11.glTranslatef(-8.0F, -8.0F, -8.0F);
      GL11.glScalef(f, f, f);
      GL11.glTranslatef(8.0F, 8.0F, 8.0F);
      GL11.glGetFloat(2982, this.modelviewMatrix);
      GL11.glPopMatrix();
   }

   public void multModelviewMatrix() {
      GL11.glMultMatrix(this.modelviewMatrix);
   }

   public CompiledChunk getCompiledChunk() {
      return this.compiledChunk;
   }

   public void setCompiledChunk(CompiledChunk compiledChunkIn) {
      this.lockCompiledChunk.lock();

      try {
         this.compiledChunk = compiledChunkIn;
      } finally {
         this.lockCompiledChunk.unlock();
      }

   }

   public void stopCompileTask() {
      this.finishCompileTask();
      this.compiledChunk = CompiledChunk.DUMMY;
   }

   public void deleteGlResources() {
      this.stopCompileTask();
      this.world = null;

      for(int i = 0; i < EnumWorldBlockLayer.values().length; ++i) {
         if (this.vertexBuffers[i] != null) {
            this.vertexBuffers[i].deleteGlBuffers();
         }
      }

   }

   public BlockPos getPosition() {
      return this.position;
   }

   public void setNeedsUpdate(boolean needsUpdateIn) {
      this.needsUpdate = needsUpdateIn;
   }

   public boolean isNeedsUpdate() {
      return this.needsUpdate;
   }

   public BlockPos func_181701_a(EnumFacing p_181701_1_) {
      return (BlockPos)this.field_181702_p.get(p_181701_1_);
   }
}
