package net.minecraft.client.gui;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.UnmodifiableIterator;
import io.github.nevalackin.client.util.render.DrawUtil;
import java.util.List;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.FrameTimer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.Chunk;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

public class GuiOverlayDebug extends Gui {
   private final Minecraft mc;
   private final FontRenderer fontRenderer;

   public GuiOverlayDebug(Minecraft mc) {
      this.mc = mc;
      this.fontRenderer = mc.fontRendererObj;
   }

   public void renderDebugInfo(ScaledResolution scaledResolutionIn) {
      GL11.glPushMatrix();
      this.renderDebugInfoLeft();
      this.renderDebugInfoRight(scaledResolutionIn);
      GL11.glPopMatrix();
      if (this.mc.gameSettings.field_181657_aC) {
         this.func_181554_e();
      }

   }

   private boolean isReducedDebug() {
      return this.mc.thePlayer.hasReducedDebug() || this.mc.gameSettings.reducedDebugInfo;
   }

   protected void renderDebugInfoLeft() {
      List list = this.call();

      for(int i = 0; i < list.size(); ++i) {
         String s = (String)list.get(i);
         if (!Strings.isNullOrEmpty(s)) {
            int j = this.fontRenderer.FONT_HEIGHT;
            int k = this.fontRenderer.getStringWidth(s);
            int l = true;
            int i1 = 2 + j * i;
            DrawUtil.glDrawFilledRect(1.0, (double)(i1 - 1), (double)(2 + k + 1), (double)(i1 + j - 1), -1873784752);
            this.fontRenderer.drawString(s, 2, i1, 14737632);
         }
      }

   }

   protected void renderDebugInfoRight(ScaledResolution p_175239_1_) {
      List list = this.getDebugInfoRight();

      for(int i = 0; i < list.size(); ++i) {
         String s = (String)list.get(i);
         if (!Strings.isNullOrEmpty(s)) {
            int j = this.fontRenderer.FONT_HEIGHT;
            int k = this.fontRenderer.getStringWidth(s);
            int l = p_175239_1_.getScaledWidth() - 2 - k;
            int i1 = 2 + j * i;
            DrawUtil.glDrawFilledRect((double)(l - 1), (double)(i1 - 1), (double)(l + k + 1), (double)(i1 + j - 1), -1873784752);
            this.fontRenderer.drawString(s, l, i1, 14737632);
         }
      }

   }

   protected List call() {
      BlockPos blockpos = new BlockPos(this.mc.getRenderViewEntity().posX, this.mc.getRenderViewEntity().getEntityBoundingBox().minY, this.mc.getRenderViewEntity().posZ);
      if (this.isReducedDebug()) {
         return Lists.newArrayList(new String[]{"Minecraft 1.8.8 (" + this.mc.getVersion() + "/" + ClientBrandRetriever.getClientModName() + ")", this.mc.debug, this.mc.renderGlobal.getDebugInfoRenders(), this.mc.renderGlobal.getDebugInfoEntities(), "P: " + this.mc.effectRenderer.getStatistics() + ". T: " + this.mc.theWorld.getDebugLoadedEntities(), this.mc.theWorld.getProviderName(), "", String.format("Chunk-relative: %d %d %d", blockpos.getX() & 15, blockpos.getY() & 15, blockpos.getZ() & 15)});
      } else {
         Entity entity = this.mc.getRenderViewEntity();
         EnumFacing enumfacing = entity.getHorizontalFacing();
         String s = "Invalid";
         switch (enumfacing) {
            case NORTH:
               s = "Towards negative Z";
               break;
            case SOUTH:
               s = "Towards positive Z";
               break;
            case WEST:
               s = "Towards negative X";
               break;
            case EAST:
               s = "Towards positive X";
         }

         List list = Lists.newArrayList(new String[]{"Minecraft 1.8.8 (" + this.mc.getVersion() + "/" + ClientBrandRetriever.getClientModName() + ")", this.mc.debug, this.mc.renderGlobal.getDebugInfoRenders(), this.mc.renderGlobal.getDebugInfoEntities(), "P: " + this.mc.effectRenderer.getStatistics() + ". T: " + this.mc.theWorld.getDebugLoadedEntities(), this.mc.theWorld.getProviderName(), "", String.format("XYZ: %.3f / %.5f / %.3f", this.mc.getRenderViewEntity().posX, this.mc.getRenderViewEntity().getEntityBoundingBox().minY, this.mc.getRenderViewEntity().posZ), String.format("Block: %d %d %d", blockpos.getX(), blockpos.getY(), blockpos.getZ()), String.format("Chunk: %d %d %d in %d %d %d", blockpos.getX() & 15, blockpos.getY() & 15, blockpos.getZ() & 15, blockpos.getX() >> 4, blockpos.getY() >> 4, blockpos.getZ() >> 4), String.format("Facing: %s (%s) (%.1f / %.1f)", enumfacing, s, MathHelper.wrapAngleTo180_float(entity.rotationYaw), MathHelper.wrapAngleTo180_float(entity.rotationPitch))});
         if (this.mc.theWorld != null && this.mc.theWorld.isBlockLoaded(blockpos)) {
            Chunk chunk = this.mc.theWorld.getChunkFromBlockCoords(blockpos);
            list.add("Biome: " + chunk.getBiome(blockpos, this.mc.theWorld.getWorldChunkManager()).biomeName);
            list.add("Light: " + chunk.getLightSubtracted(blockpos, 0) + " (" + chunk.getLightFor(EnumSkyBlock.SKY, blockpos) + " sky, " + chunk.getLightFor(EnumSkyBlock.BLOCK, blockpos) + " block)");
            DifficultyInstance difficultyinstance = this.mc.theWorld.getDifficultyForLocation(blockpos);
            if (this.mc.isIntegratedServerRunning() && this.mc.getIntegratedServer() != null) {
               EntityPlayerMP entityplayermp = this.mc.getIntegratedServer().getConfigurationManager().getPlayerByUUID(this.mc.thePlayer.getUniqueID());
               if (entityplayermp != null) {
                  difficultyinstance = entityplayermp.worldObj.getDifficultyForLocation(new BlockPos(entityplayermp));
               }
            }

            list.add(String.format("Local Difficulty: %.2f (Day %d)", difficultyinstance.getAdditionalDifficulty(), this.mc.theWorld.getWorldTime() / 24000L));
         }

         if (this.mc.entityRenderer != null && this.mc.entityRenderer.isShaderActive()) {
            list.add("Shader: " + this.mc.entityRenderer.getShaderGroup().getShaderGroupName());
         }

         if (this.mc.objectMouseOver != null && this.mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && this.mc.objectMouseOver.getBlockPos() != null) {
            BlockPos blockpos1 = this.mc.objectMouseOver.getBlockPos();
            list.add(String.format("Looking at: %d %d %d", blockpos1.getX(), blockpos1.getY(), blockpos1.getZ()));
         }

         return list;
      }
   }

   protected List getDebugInfoRight() {
      long i = Runtime.getRuntime().maxMemory();
      long j = Runtime.getRuntime().totalMemory();
      long k = Runtime.getRuntime().freeMemory();
      long l = j - k;
      List list = Lists.newArrayList(new String[]{String.format("Java: %s %dbit", System.getProperty("java.version"), this.mc.isJava64bit() ? 64 : 32), String.format("Mem: % 2d%% %03d/%03dMB", l * 100L / i, bytesToMb(l), bytesToMb(i)), String.format("Allocated: % 2d%% %03dMB", j * 100L / i, bytesToMb(j)), "", String.format("CPU: %s", OpenGlHelper.func_183029_j()), "", String.format("Display: %dx%d (%s)", Display.getWidth(), Display.getHeight(), GL11.glGetString(7936)), GL11.glGetString(7937), GL11.glGetString(7938)});
      if (this.isReducedDebug()) {
         return list;
      } else {
         if (this.mc.objectMouseOver != null && this.mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && this.mc.objectMouseOver.getBlockPos() != null) {
            BlockPos blockpos = this.mc.objectMouseOver.getBlockPos();
            IBlockState iblockstate = this.mc.theWorld.getBlockState(blockpos);
            if (this.mc.theWorld.getWorldType() != WorldType.DEBUG_WORLD) {
               iblockstate = iblockstate.getBlock().getActualState(iblockstate, this.mc.theWorld, blockpos);
            }

            list.add("");
            list.add(String.valueOf(Block.blockRegistry.getNameForObject(iblockstate.getBlock())));

            Map.Entry entry;
            String s;
            for(UnmodifiableIterator var12 = iblockstate.getProperties().entrySet().iterator(); var12.hasNext(); list.add(((IProperty)entry.getKey()).getName() + ": " + s)) {
               entry = (Map.Entry)var12.next();
               s = ((Comparable)entry.getValue()).toString();
               if (entry.getValue() == Boolean.TRUE) {
                  s = EnumChatFormatting.GREEN + s;
               } else if (entry.getValue() == Boolean.FALSE) {
                  s = EnumChatFormatting.RED + s;
               }
            }
         }

         return list;
      }
   }

   private void func_181554_e() {
      GL11.glDisable(2929);
      FrameTimer frametimer = this.mc.func_181539_aj();
      int i = frametimer.func_181749_a();
      int j = frametimer.func_181750_b();
      long[] along = frametimer.func_181746_c();
      ScaledResolution scaledresolution = new ScaledResolution(this.mc);
      int k = i;
      int l = 0;
      DrawUtil.glDrawFilledRect(0.0, (double)(scaledresolution.getScaledHeight() - 60), 240.0, (double)scaledresolution.getScaledHeight(), -1873784752);

      while(k != j) {
         int i1 = frametimer.func_181748_a(along[k], 30);
         int j1 = this.func_181552_c(MathHelper.clamp_int(i1, 0, 60), 0, 30, 60);
         this.drawVerticalLine(l, scaledresolution.getScaledHeight(), scaledresolution.getScaledHeight() - i1, j1);
         ++l;
         k = frametimer.func_181751_b(k + 1);
      }

      DrawUtil.glDrawFilledRect(1.0, (double)(scaledresolution.getScaledHeight() - 30 + 1), 14.0, (double)(scaledresolution.getScaledHeight() - 30 + 10), -1873784752);
      this.fontRenderer.drawString("60", 2, scaledresolution.getScaledHeight() - 30 + 2, 14737632);
      this.drawHorizontalLine(0, 239, scaledresolution.getScaledHeight() - 30, -1);
      DrawUtil.glDrawFilledRect(1.0, (double)(scaledresolution.getScaledHeight() - 60 + 1), 14.0, (double)(scaledresolution.getScaledHeight() - 60 + 10), -1873784752);
      this.fontRenderer.drawString("30", 2, scaledresolution.getScaledHeight() - 60 + 2, 14737632);
      this.drawHorizontalLine(0, 239, scaledresolution.getScaledHeight() - 60, -1);
      this.drawHorizontalLine(0, 239, scaledresolution.getScaledHeight() - 1, -1);
      this.drawVerticalLine(0, scaledresolution.getScaledHeight() - 60, scaledresolution.getScaledHeight(), -1);
      this.drawVerticalLine(239, scaledresolution.getScaledHeight() - 60, scaledresolution.getScaledHeight(), -1);
      if (this.mc.gameSettings.limitFramerate <= 120) {
         this.drawHorizontalLine(0, 239, scaledresolution.getScaledHeight() - 60 + this.mc.gameSettings.limitFramerate / 2, -16711681);
      }

      GL11.glEnable(2929);
   }

   private int func_181552_c(int p_181552_1_, int p_181552_2_, int p_181552_3_, int p_181552_4_) {
      return p_181552_1_ < p_181552_3_ ? this.func_181553_a(-16711936, -256, (float)p_181552_1_ / (float)p_181552_3_) : this.func_181553_a(-256, -65536, (float)(p_181552_1_ - p_181552_3_) / (float)(p_181552_4_ - p_181552_3_));
   }

   private int func_181553_a(int p_181553_1_, int p_181553_2_, float p_181553_3_) {
      int i = p_181553_1_ >> 24 & 255;
      int j = p_181553_1_ >> 16 & 255;
      int k = p_181553_1_ >> 8 & 255;
      int l = p_181553_1_ & 255;
      int i1 = p_181553_2_ >> 24 & 255;
      int j1 = p_181553_2_ >> 16 & 255;
      int k1 = p_181553_2_ >> 8 & 255;
      int l1 = p_181553_2_ & 255;
      int i2 = MathHelper.clamp_int((int)((float)i + (float)(i1 - i) * p_181553_3_), 0, 255);
      int j2 = MathHelper.clamp_int((int)((float)j + (float)(j1 - j) * p_181553_3_), 0, 255);
      int k2 = MathHelper.clamp_int((int)((float)k + (float)(k1 - k) * p_181553_3_), 0, 255);
      int l2 = MathHelper.clamp_int((int)((float)l + (float)(l1 - l) * p_181553_3_), 0, 255);
      return i2 << 24 | j2 << 16 | k2 << 8 | l2;
   }

   private static long bytesToMb(long bytes) {
      return bytes / 1024L / 1024L;
   }
}
