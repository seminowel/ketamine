package net.minecraft.client.renderer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.github.nevalackin.client.impl.core.KetamineClient;
import io.github.nevalackin.client.impl.event.render.world.DrawSelectedBoundingBoxEvent;
import io.github.nevalackin.client.util.render.DrawUtil;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.BlockSign;
import net.minecraft.block.BlockSkull;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.IRenderChunkFactory;
import net.minecraft.client.renderer.chunk.ListChunkFactory;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.chunk.VboChunkFactory;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.culling.ClippingHelperImpl;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityWitherSkull;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemRecord;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Matrix4f;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vector3d;
import net.minecraft.world.IWorldAccess;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public class RenderGlobal implements IWorldAccess, IResourceManagerReloadListener {
   private static final Logger logger = LogManager.getLogger();
   private static final ResourceLocation locationMoonPhasesPng = new ResourceLocation("textures/environment/moon_phases.png");
   private static final ResourceLocation locationSunPng = new ResourceLocation("textures/environment/sun.png");
   private static final ResourceLocation locationCloudsPng = new ResourceLocation("textures/environment/clouds.png");
   private static final ResourceLocation locationEndSkyPng = new ResourceLocation("textures/environment/end_sky.png");
   private static final ResourceLocation locationForcefieldPng = new ResourceLocation("textures/misc/forcefield.png");
   private final Minecraft mc;
   private final TextureManager renderEngine;
   private final RenderManager renderManager;
   private WorldClient theWorld;
   private Set chunksToUpdate = Sets.newLinkedHashSet();
   private List renderInfos = Lists.newArrayListWithCapacity(69696);
   private final Set field_181024_n = Sets.newHashSet();
   private ViewFrustum viewFrustum;
   private int starGLCallList = -1;
   private int glSkyList = -1;
   private int glSkyList2 = -1;
   private final VertexFormat vertexBufferFormat;
   private VertexBuffer starVBO;
   private VertexBuffer skyVBO;
   private VertexBuffer sky2VBO;
   private int cloudTickCounter;
   private final Map damagedBlocks = Maps.newHashMap();
   private final Map mapSoundPositions = Maps.newHashMap();
   private final TextureAtlasSprite[] destroyBlockIcons = new TextureAtlasSprite[10];
   private double frustumUpdatePosX = Double.MIN_VALUE;
   private double frustumUpdatePosY = Double.MIN_VALUE;
   private double frustumUpdatePosZ = Double.MIN_VALUE;
   private int frustumUpdatePosChunkX = Integer.MIN_VALUE;
   private int frustumUpdatePosChunkY = Integer.MIN_VALUE;
   private int frustumUpdatePosChunkZ = Integer.MIN_VALUE;
   private double lastViewEntityX = Double.MIN_VALUE;
   private double lastViewEntityY = Double.MIN_VALUE;
   private double lastViewEntityZ = Double.MIN_VALUE;
   private double lastViewEntityPitch = Double.MIN_VALUE;
   private double lastViewEntityYaw = Double.MIN_VALUE;
   private final ChunkRenderDispatcher renderDispatcher = new ChunkRenderDispatcher();
   private ChunkRenderContainer renderContainer;
   private int renderDistanceChunks = -1;
   private int renderEntitiesStartupCounter = 2;
   private int countEntitiesTotal;
   private int countEntitiesRendered;
   private int countEntitiesHidden;
   private boolean debugFixTerrainFrustum = false;
   private ClippingHelper debugFixedClippingHelper;
   private final Vector4f[] debugTerrainMatrix = new Vector4f[8];
   private final Vector3d debugTerrainFrustumPosition = new Vector3d();
   private boolean vboEnabled = false;
   IRenderChunkFactory renderChunkFactory;
   private double prevRenderSortX;
   private double prevRenderSortY;
   private double prevRenderSortZ;
   private boolean displayListEntitiesDirty = true;

   public RenderGlobal(Minecraft mcIn) {
      this.mc = mcIn;
      this.renderManager = mcIn.getRenderManager();
      this.renderEngine = mcIn.getTextureManager();
      this.renderEngine.bindTexture(locationForcefieldPng);
      GL11.glTexParameteri(3553, 10242, 10497);
      GL11.glTexParameteri(3553, 10243, 10497);
      GL11.glBindTexture(3553, 0);
      this.updateDestroyBlockIcons();
      this.vboEnabled = OpenGlHelper.useVbo();
      if (this.vboEnabled) {
         this.renderContainer = new VboRenderList();
         this.renderChunkFactory = new VboChunkFactory();
      } else {
         this.renderContainer = new RenderList();
         this.renderChunkFactory = new ListChunkFactory();
      }

      this.vertexBufferFormat = new VertexFormat();
      this.vertexBufferFormat.func_181721_a(new VertexFormatElement(0, VertexFormatElement.EnumType.FLOAT, VertexFormatElement.EnumUsage.POSITION, 3));
      this.generateStars();
      this.generateSky();
      this.generateSky2();
   }

   public void onResourceManagerReload(IResourceManager resourceManager) {
      this.updateDestroyBlockIcons();
   }

   public void markBlockForUpdate(BlockPos pos) {
      int i = pos.getX();
      int j = pos.getY();
      int k = pos.getZ();
      this.markBlocksForUpdate(i - 1, j - 1, k - 1, i + 1, j + 1, k + 1);
   }

   public void notifyLightSet(BlockPos pos) {
      int i = pos.getX();
      int j = pos.getY();
      int k = pos.getZ();
      this.markBlocksForUpdate(i - 1, j - 1, k - 1, i + 1, j + 1, k + 1);
   }

   public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {
      this.markBlocksForUpdate(x1 - 1, y1 - 1, z1 - 1, x2 + 1, y2 + 1, z2 + 1);
   }

   public void playRecord(String recordName, BlockPos blockPosIn) {
      ISound isound = (ISound)this.mapSoundPositions.get(blockPosIn);
      if (isound != null) {
         this.mc.getSoundHandler().stopSound(isound);
         this.mapSoundPositions.remove(blockPosIn);
      }

      if (recordName != null) {
         PositionedSoundRecord positionedsoundrecord = PositionedSoundRecord.create(new ResourceLocation(recordName), (float)blockPosIn.getX(), (float)blockPosIn.getY(), (float)blockPosIn.getZ());
         this.mapSoundPositions.put(blockPosIn, positionedsoundrecord);
         this.mc.getSoundHandler().playSound(positionedsoundrecord);
      }

   }

   public void playSound(String soundName, double x, double y, double z, float volume, float pitch) {
   }

   public void playSoundToNearExcept(EntityPlayer except, String soundName, double x, double y, double z, float volume, float pitch) {
   }

   public void spawnParticle(int particleID, boolean ignoreRange, final double xCoord, final double yCoord, final double zCoord, double xOffset, double yOffset, double zOffset, int... p_180442_15_) {
      try {
         this.spawnEntityFX(particleID, ignoreRange, xCoord, yCoord, zCoord, xOffset, yOffset, zOffset, p_180442_15_);
      } catch (Throwable var19) {
         CrashReport crashreport = CrashReport.makeCrashReport(var19, "Exception while adding particle");
         CrashReportCategory crashreportcategory = crashreport.makeCategory("Particle being added");
         crashreportcategory.addCrashSection("ID", particleID);
         if (p_180442_15_ != null) {
            crashreportcategory.addCrashSection("Parameters", p_180442_15_);
         }

         crashreportcategory.addCrashSectionCallable("Position", new Callable() {
            public String call() throws Exception {
               return CrashReportCategory.getCoordinateInfo(xCoord, yCoord, zCoord);
            }
         });
         throw new ReportedException(crashreport);
      }
   }

   public void onEntityAdded(Entity entityIn) {
   }

   public void onEntityRemoved(Entity entityIn) {
   }

   public void broadcastSound(int p_180440_1_, BlockPos p_180440_2_, int p_180440_3_) {
      switch (p_180440_1_) {
         case 1013:
         case 1018:
            if (this.mc.getRenderViewEntity() != null) {
               double d0 = (double)p_180440_2_.getX() - this.mc.getRenderViewEntity().posX;
               double d1 = (double)p_180440_2_.getY() - this.mc.getRenderViewEntity().posY;
               double d2 = (double)p_180440_2_.getZ() - this.mc.getRenderViewEntity().posZ;
               double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
               double d4 = this.mc.getRenderViewEntity().posX;
               double d5 = this.mc.getRenderViewEntity().posY;
               double d6 = this.mc.getRenderViewEntity().posZ;
               if (d3 > 0.0) {
                  d4 += d0 / d3 * 2.0;
                  d5 += d1 / d3 * 2.0;
                  d6 += d2 / d3 * 2.0;
               }

               if (p_180440_1_ == 1013) {
                  this.theWorld.playSound(d4, d5, d6, "mob.wither.spawn", 1.0F, 1.0F, false);
               } else {
                  this.theWorld.playSound(d4, d5, d6, "mob.enderdragon.end", 5.0F, 1.0F, false);
               }
            }
         default:
      }
   }

   public void playAuxSFX(EntityPlayer player, int sfxType, BlockPos blockPosIn, int p_180439_4_) {
      Random random = this.theWorld.rand;
      double d13;
      double d14;
      double d16;
      double d22;
      int j;
      double d9;
      double d11;
      switch (sfxType) {
         case 1000:
            this.theWorld.playSoundAtPos(blockPosIn, "random.click", 1.0F, 1.0F, false);
            break;
         case 1001:
            this.theWorld.playSoundAtPos(blockPosIn, "random.click", 1.0F, 1.2F, false);
            break;
         case 1002:
            this.theWorld.playSoundAtPos(blockPosIn, "random.bow", 1.0F, 1.2F, false);
            break;
         case 1003:
            this.theWorld.playSoundAtPos(blockPosIn, "random.door_open", 1.0F, this.theWorld.rand.nextFloat() * 0.1F + 0.9F, false);
            break;
         case 1004:
            this.theWorld.playSoundAtPos(blockPosIn, "random.fizz", 0.5F, 2.6F + (random.nextFloat() - random.nextFloat()) * 0.8F, false);
            break;
         case 1005:
            if (Item.getItemById(p_180439_4_) instanceof ItemRecord) {
               this.theWorld.playRecord(blockPosIn, "records." + ((ItemRecord)Item.getItemById(p_180439_4_)).recordName);
            } else {
               this.theWorld.playRecord(blockPosIn, (String)null);
            }
            break;
         case 1006:
            this.theWorld.playSoundAtPos(blockPosIn, "random.door_close", 1.0F, this.theWorld.rand.nextFloat() * 0.1F + 0.9F, false);
            break;
         case 1007:
            this.theWorld.playSoundAtPos(blockPosIn, "mob.ghast.charge", 10.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1008:
            this.theWorld.playSoundAtPos(blockPosIn, "mob.ghast.fireball", 10.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1009:
            this.theWorld.playSoundAtPos(blockPosIn, "mob.ghast.fireball", 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1010:
            this.theWorld.playSoundAtPos(blockPosIn, "mob.zombie.wood", 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1011:
            this.theWorld.playSoundAtPos(blockPosIn, "mob.zombie.metal", 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1012:
            this.theWorld.playSoundAtPos(blockPosIn, "mob.zombie.woodbreak", 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1014:
            this.theWorld.playSoundAtPos(blockPosIn, "mob.wither.shoot", 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1015:
            this.theWorld.playSoundAtPos(blockPosIn, "mob.bat.takeoff", 0.05F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1016:
            this.theWorld.playSoundAtPos(blockPosIn, "mob.zombie.infect", 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1017:
            this.theWorld.playSoundAtPos(blockPosIn, "mob.zombie.unfect", 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1020:
            this.theWorld.playSoundAtPos(blockPosIn, "random.anvil_break", 1.0F, this.theWorld.rand.nextFloat() * 0.1F + 0.9F, false);
            break;
         case 1021:
            this.theWorld.playSoundAtPos(blockPosIn, "random.anvil_use", 1.0F, this.theWorld.rand.nextFloat() * 0.1F + 0.9F, false);
            break;
         case 1022:
            this.theWorld.playSoundAtPos(blockPosIn, "random.anvil_land", 0.3F, this.theWorld.rand.nextFloat() * 0.1F + 0.9F, false);
            break;
         case 2000:
            int l = p_180439_4_ % 3 - 1;
            int i = p_180439_4_ / 3 % 3 - 1;
            double d15 = (double)blockPosIn.getX() + (double)l * 0.6 + 0.5;
            double d17 = (double)blockPosIn.getY() + 0.5;
            double d19 = (double)blockPosIn.getZ() + (double)i * 0.6 + 0.5;

            for(int k1 = 0; k1 < 10; ++k1) {
               d13 = random.nextDouble() * 0.2 + 0.01;
               d14 = d15 + (double)l * 0.01 + (random.nextDouble() - 0.5) * (double)i * 0.5;
               d16 = d17 + (random.nextDouble() - 0.5) * 0.5;
               double d6 = d19 + (double)i * 0.01 + (random.nextDouble() - 0.5) * (double)l * 0.5;
               double d8 = (double)l * d13 + random.nextGaussian() * 0.01;
               double d10 = -0.03 + random.nextGaussian() * 0.01;
               d22 = (double)i * d13 + random.nextGaussian() * 0.01;
               this.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d14, d16, d6, d8, d10, d22);
            }

            return;
         case 2001:
            Block block = Block.getBlockById(p_180439_4_ & 4095);
            if (block.getMaterial() != Material.air) {
               this.mc.getSoundHandler().playSound(new PositionedSoundRecord(new ResourceLocation(block.stepSound.getBreakSound()), (block.stepSound.getVolume() + 1.0F) / 2.0F, block.stepSound.getFrequency() * 0.8F, (float)blockPosIn.getX() + 0.5F, (float)blockPosIn.getY() + 0.5F, (float)blockPosIn.getZ() + 0.5F));
            }

            this.mc.effectRenderer.addBlockDestroyEffects(blockPosIn, block.getStateFromMeta(p_180439_4_ >> 12 & 255));
            break;
         case 2002:
            d13 = (double)blockPosIn.getX();
            d14 = (double)blockPosIn.getY();
            d16 = (double)blockPosIn.getZ();

            int j1;
            for(j1 = 0; j1 < 8; ++j1) {
               this.spawnParticle(EnumParticleTypes.ITEM_CRACK, d13, d14, d16, random.nextGaussian() * 0.15, random.nextDouble() * 0.2, random.nextGaussian() * 0.15, Item.getIdFromItem(Items.potionitem), p_180439_4_);
            }

            j1 = Items.potionitem.getColorFromDamage(p_180439_4_);
            float f = (float)(j1 >> 16 & 255) / 255.0F;
            float f1 = (float)(j1 >> 8 & 255) / 255.0F;
            float f2 = (float)(j1 >> 0 & 255) / 255.0F;
            EnumParticleTypes enumparticletypes = EnumParticleTypes.SPELL;
            if (Items.potionitem.isEffectInstant(p_180439_4_)) {
               enumparticletypes = EnumParticleTypes.SPELL_INSTANT;
            }

            for(int l1 = 0; l1 < 100; ++l1) {
               d22 = random.nextDouble() * 4.0;
               double d23 = random.nextDouble() * Math.PI * 2.0;
               double d24 = Math.cos(d23) * d22;
               d9 = 0.01 + random.nextDouble() * 0.5;
               d11 = Math.sin(d23) * d22;
               EntityFX entityfx = this.spawnEntityFX(enumparticletypes.getParticleID(), enumparticletypes.getShouldIgnoreRange(), d13 + d24 * 0.1, d14 + 0.3, d16 + d11 * 0.1, d24, d9, d11);
               if (entityfx != null) {
                  float f3 = 0.75F + random.nextFloat() * 0.25F;
                  entityfx.setRBGColorF(f * f3, f1 * f3, f2 * f3);
                  entityfx.multiplyVelocity((float)d22);
               }
            }

            this.theWorld.playSoundAtPos(blockPosIn, "game.potion.smash", 1.0F, this.theWorld.rand.nextFloat() * 0.1F + 0.9F, false);
            break;
         case 2003:
            double d0 = (double)blockPosIn.getX() + 0.5;
            double d1 = (double)blockPosIn.getY();
            double d2 = (double)blockPosIn.getZ() + 0.5;

            for(j = 0; j < 8; ++j) {
               this.spawnParticle(EnumParticleTypes.ITEM_CRACK, d0, d1, d2, random.nextGaussian() * 0.15, random.nextDouble() * 0.2, random.nextGaussian() * 0.15, Item.getIdFromItem(Items.ender_eye));
            }

            for(double d18 = 0.0; d18 < 6.283185307179586; d18 += 0.15707963267948966) {
               this.spawnParticle(EnumParticleTypes.PORTAL, d0 + Math.cos(d18) * 5.0, d1 - 0.4, d2 + Math.sin(d18) * 5.0, Math.cos(d18) * -5.0, 0.0, Math.sin(d18) * -5.0);
               this.spawnParticle(EnumParticleTypes.PORTAL, d0 + Math.cos(d18) * 5.0, d1 - 0.4, d2 + Math.sin(d18) * 5.0, Math.cos(d18) * -7.0, 0.0, Math.sin(d18) * -7.0);
            }

            return;
         case 2004:
            for(j = 0; j < 20; ++j) {
               d9 = (double)blockPosIn.getX() + 0.5 + ((double)this.theWorld.rand.nextFloat() - 0.5) * 2.0;
               d11 = (double)blockPosIn.getY() + 0.5 + ((double)this.theWorld.rand.nextFloat() - 0.5) * 2.0;
               double d7 = (double)blockPosIn.getZ() + 0.5 + ((double)this.theWorld.rand.nextFloat() - 0.5) * 2.0;
               this.theWorld.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d9, d11, d7, 0.0, 0.0, 0.0, new int[0]);
               this.theWorld.spawnParticle(EnumParticleTypes.FLAME, d9, d11, d7, 0.0, 0.0, 0.0, new int[0]);
            }

            return;
         case 2005:
            ItemDye.spawnBonemealParticles(this.theWorld, blockPosIn, p_180439_4_);
      }

   }

   public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {
      if (progress >= 0 && progress < 10) {
         DestroyBlockProgress destroyblockprogress = (DestroyBlockProgress)this.damagedBlocks.get(breakerId);
         if (destroyblockprogress == null || destroyblockprogress.getPosition().getX() != pos.getX() || destroyblockprogress.getPosition().getY() != pos.getY() || destroyblockprogress.getPosition().getZ() != pos.getZ()) {
            destroyblockprogress = new DestroyBlockProgress(breakerId, pos);
            this.damagedBlocks.put(breakerId, destroyblockprogress);
         }

         destroyblockprogress.setPartialBlockDamage(progress);
         destroyblockprogress.setCloudUpdateTick(this.cloudTickCounter);
      } else {
         this.damagedBlocks.remove(breakerId);
      }

   }

   public static void func_181563_a(AxisAlignedBB p_181563_0_, int p_181563_1_, int p_181563_2_, int p_181563_3_, int p_181563_4_) {
      Tessellator tessellator = Tessellator.getInstance();
      WorldRenderer worldrenderer = tessellator.getWorldRenderer();
      worldrenderer.begin(3, DefaultVertexFormats.POSITION_COLOR);
      worldrenderer.pos(p_181563_0_.minX, p_181563_0_.minY, p_181563_0_.minZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
      worldrenderer.pos(p_181563_0_.maxX, p_181563_0_.minY, p_181563_0_.minZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
      worldrenderer.pos(p_181563_0_.maxX, p_181563_0_.minY, p_181563_0_.maxZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
      worldrenderer.pos(p_181563_0_.minX, p_181563_0_.minY, p_181563_0_.maxZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
      worldrenderer.pos(p_181563_0_.minX, p_181563_0_.minY, p_181563_0_.minZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
      tessellator.draw();
      worldrenderer.begin(3, DefaultVertexFormats.POSITION_COLOR);
      worldrenderer.pos(p_181563_0_.minX, p_181563_0_.maxY, p_181563_0_.minZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
      worldrenderer.pos(p_181563_0_.maxX, p_181563_0_.maxY, p_181563_0_.minZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
      worldrenderer.pos(p_181563_0_.maxX, p_181563_0_.maxY, p_181563_0_.maxZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
      worldrenderer.pos(p_181563_0_.minX, p_181563_0_.maxY, p_181563_0_.maxZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
      worldrenderer.pos(p_181563_0_.minX, p_181563_0_.maxY, p_181563_0_.minZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
      tessellator.draw();
      worldrenderer.begin(1, DefaultVertexFormats.POSITION_COLOR);
      worldrenderer.pos(p_181563_0_.minX, p_181563_0_.minY, p_181563_0_.minZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
      worldrenderer.pos(p_181563_0_.minX, p_181563_0_.maxY, p_181563_0_.minZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
      worldrenderer.pos(p_181563_0_.maxX, p_181563_0_.minY, p_181563_0_.minZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
      worldrenderer.pos(p_181563_0_.maxX, p_181563_0_.maxY, p_181563_0_.minZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
      worldrenderer.pos(p_181563_0_.maxX, p_181563_0_.minY, p_181563_0_.maxZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
      worldrenderer.pos(p_181563_0_.maxX, p_181563_0_.maxY, p_181563_0_.maxZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
      worldrenderer.pos(p_181563_0_.minX, p_181563_0_.minY, p_181563_0_.maxZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
      worldrenderer.pos(p_181563_0_.minX, p_181563_0_.maxY, p_181563_0_.maxZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
      tessellator.draw();
   }

   private void updateDestroyBlockIcons() {
      TextureMap texturemap = this.mc.getTextureMapBlocks();

      for(int i = 0; i < this.destroyBlockIcons.length; ++i) {
         this.destroyBlockIcons[i] = texturemap.getAtlasSprite("minecraft:blocks/destroy_stage_" + i);
      }

   }

   private void generateSky2() {
      Tessellator tessellator = Tessellator.getInstance();
      WorldRenderer worldrenderer = tessellator.getWorldRenderer();
      if (this.sky2VBO != null) {
         this.sky2VBO.deleteGlBuffers();
      }

      if (this.glSkyList2 >= 0) {
         GLAllocation.deleteDisplayLists(this.glSkyList2);
         this.glSkyList2 = -1;
      }

      if (this.vboEnabled) {
         this.sky2VBO = new VertexBuffer(this.vertexBufferFormat);
         this.renderSky(worldrenderer, -16.0F, true);
         worldrenderer.finishDrawing();
         worldrenderer.reset();
         this.sky2VBO.func_181722_a(worldrenderer.getByteBuffer());
      } else {
         this.glSkyList2 = GLAllocation.generateDisplayLists(1);
         GL11.glNewList(this.glSkyList2, 4864);
         this.renderSky(worldrenderer, -16.0F, true);
         tessellator.draw();
         GL11.glEndList();
      }

   }

   private void generateSky() {
      Tessellator tessellator = Tessellator.getInstance();
      WorldRenderer worldrenderer = tessellator.getWorldRenderer();
      if (this.skyVBO != null) {
         this.skyVBO.deleteGlBuffers();
      }

      if (this.glSkyList >= 0) {
         GLAllocation.deleteDisplayLists(this.glSkyList);
         this.glSkyList = -1;
      }

      if (this.vboEnabled) {
         this.skyVBO = new VertexBuffer(this.vertexBufferFormat);
         this.renderSky(worldrenderer, 16.0F, false);
         worldrenderer.finishDrawing();
         worldrenderer.reset();
         this.skyVBO.func_181722_a(worldrenderer.getByteBuffer());
      } else {
         this.glSkyList = GLAllocation.generateDisplayLists(1);
         GL11.glNewList(this.glSkyList, 4864);
         this.renderSky(worldrenderer, 16.0F, false);
         tessellator.draw();
         GL11.glEndList();
      }

   }

   private void renderSky(WorldRenderer worldRendererIn, float p_174968_2_, boolean p_174968_3_) {
      int i = true;
      int j = true;
      worldRendererIn.begin(7, DefaultVertexFormats.POSITION);

      for(int k = -384; k <= 384; k += 64) {
         for(int l = -384; l <= 384; l += 64) {
            float f = (float)k;
            float f1 = (float)(k + 64);
            if (p_174968_3_) {
               f1 = (float)k;
               f = (float)(k + 64);
            }

            worldRendererIn.pos((double)f, (double)p_174968_2_, (double)l).endVertex();
            worldRendererIn.pos((double)f1, (double)p_174968_2_, (double)l).endVertex();
            worldRendererIn.pos((double)f1, (double)p_174968_2_, (double)(l + 64)).endVertex();
            worldRendererIn.pos((double)f, (double)p_174968_2_, (double)(l + 64)).endVertex();
         }
      }

   }

   private void generateStars() {
      Tessellator tessellator = Tessellator.getInstance();
      WorldRenderer worldrenderer = tessellator.getWorldRenderer();
      if (this.starVBO != null) {
         this.starVBO.deleteGlBuffers();
      }

      if (this.starGLCallList >= 0) {
         GLAllocation.deleteDisplayLists(this.starGLCallList);
         this.starGLCallList = -1;
      }

      if (this.vboEnabled) {
         this.starVBO = new VertexBuffer(this.vertexBufferFormat);
         this.renderStars(worldrenderer);
         worldrenderer.finishDrawing();
         worldrenderer.reset();
         this.starVBO.func_181722_a(worldrenderer.getByteBuffer());
      } else {
         this.starGLCallList = GLAllocation.generateDisplayLists(1);
         GL11.glPushMatrix();
         GL11.glNewList(this.starGLCallList, 4864);
         this.renderStars(worldrenderer);
         tessellator.draw();
         GL11.glEndList();
         GL11.glPopMatrix();
      }

   }

   private void renderStars(WorldRenderer worldRendererIn) {
      Random random = new Random(10842L);
      worldRendererIn.begin(7, DefaultVertexFormats.POSITION);

      for(int i = 0; i < 1500; ++i) {
         double d0 = (double)(random.nextFloat() * 2.0F - 1.0F);
         double d1 = (double)(random.nextFloat() * 2.0F - 1.0F);
         double d2 = (double)(random.nextFloat() * 2.0F - 1.0F);
         double d3 = (double)(0.15F + random.nextFloat() * 0.1F);
         double d4 = d0 * d0 + d1 * d1 + d2 * d2;
         if (d4 < 1.0 && d4 > 0.01) {
            d4 = 1.0 / Math.sqrt(d4);
            d0 *= d4;
            d1 *= d4;
            d2 *= d4;
            double d5 = d0 * 100.0;
            double d6 = d1 * 100.0;
            double d7 = d2 * 100.0;
            double d8 = Math.atan2(d0, d2);
            double d9 = Math.sin(d8);
            double d10 = Math.cos(d8);
            double d11 = Math.atan2(Math.sqrt(d0 * d0 + d2 * d2), d1);
            double d12 = Math.sin(d11);
            double d13 = Math.cos(d11);
            double d14 = random.nextDouble() * Math.PI * 2.0;
            double d15 = Math.sin(d14);
            double d16 = Math.cos(d14);

            for(int j = 0; j < 4; ++j) {
               double d17 = 0.0;
               double d18 = (double)((j & 2) - 1) * d3;
               double d19 = (double)((j + 1 & 2) - 1) * d3;
               double d20 = 0.0;
               double d21 = d18 * d16 - d19 * d15;
               double d22 = d19 * d16 + d18 * d15;
               double d23 = d21 * d12 + 0.0 * d13;
               double d24 = 0.0 * d12 - d21 * d13;
               double d25 = d24 * d9 - d22 * d10;
               double d26 = d22 * d9 + d24 * d10;
               worldRendererIn.pos(d5 + d25, d6 + d23, d7 + d26).endVertex();
            }
         }
      }

   }

   public void setWorldAndLoadRenderers(WorldClient worldClientIn) {
      if (this.theWorld != null) {
         this.theWorld.removeWorldAccess(this);
      }

      this.frustumUpdatePosX = Double.MIN_VALUE;
      this.frustumUpdatePosY = Double.MIN_VALUE;
      this.frustumUpdatePosZ = Double.MIN_VALUE;
      this.frustumUpdatePosChunkX = Integer.MIN_VALUE;
      this.frustumUpdatePosChunkY = Integer.MIN_VALUE;
      this.frustumUpdatePosChunkZ = Integer.MIN_VALUE;
      this.renderManager.set(worldClientIn);
      this.theWorld = worldClientIn;
      if (worldClientIn != null) {
         worldClientIn.addWorldAccess(this);
         this.loadRenderers();
      }

   }

   public void loadRenderers() {
      if (this.theWorld != null) {
         this.displayListEntitiesDirty = true;
         Blocks.leaves.setGraphicsLevel(this.mc.gameSettings.fancyGraphics);
         Blocks.leaves2.setGraphicsLevel(this.mc.gameSettings.fancyGraphics);
         this.renderDistanceChunks = this.mc.gameSettings.renderDistanceChunks;
         boolean flag = this.vboEnabled;
         this.vboEnabled = OpenGlHelper.useVbo();
         if (flag && !this.vboEnabled) {
            this.renderContainer = new RenderList();
            this.renderChunkFactory = new ListChunkFactory();
         } else if (!flag && this.vboEnabled) {
            this.renderContainer = new VboRenderList();
            this.renderChunkFactory = new VboChunkFactory();
         }

         if (flag != this.vboEnabled) {
            this.generateStars();
            this.generateSky();
            this.generateSky2();
         }

         if (this.viewFrustum != null) {
            this.viewFrustum.deleteGlResources();
         }

         this.stopChunkUpdates();
         synchronized(this.field_181024_n) {
            this.field_181024_n.clear();
         }

         this.viewFrustum = new ViewFrustum(this.theWorld, this.mc.gameSettings.renderDistanceChunks, this, this.renderChunkFactory);
         if (this.theWorld != null) {
            Entity entity = this.mc.getRenderViewEntity();
            if (entity != null) {
               this.viewFrustum.updateChunkPositions(entity.posX, entity.posZ);
            }
         }

         this.renderEntitiesStartupCounter = 2;
      }

   }

   protected void stopChunkUpdates() {
      this.chunksToUpdate.clear();
      this.renderDispatcher.stopChunkUpdates();
   }

   public void renderEntities(Entity renderViewEntity, ICamera camera, float partialTicks) {
      if (this.renderEntitiesStartupCounter > 0) {
         --this.renderEntitiesStartupCounter;
      } else {
         double d0 = renderViewEntity.prevPosX + (renderViewEntity.posX - renderViewEntity.prevPosX) * (double)partialTicks;
         double d1 = renderViewEntity.prevPosY + (renderViewEntity.posY - renderViewEntity.prevPosY) * (double)partialTicks;
         double d2 = renderViewEntity.prevPosZ + (renderViewEntity.posZ - renderViewEntity.prevPosZ) * (double)partialTicks;
         TileEntityRendererDispatcher.instance.cacheActiveRenderInfo(this.theWorld, this.mc.getTextureManager(), this.mc.fontRendererObj, this.mc.getRenderViewEntity(), partialTicks);
         this.renderManager.cacheActiveRenderInfo(this.theWorld, this.mc.fontRendererObj, this.mc.getRenderViewEntity(), this.mc.pointedEntity, this.mc.gameSettings, partialTicks);
         this.countEntitiesTotal = 0;
         this.countEntitiesRendered = 0;
         this.countEntitiesHidden = 0;
         Entity entity = this.mc.getRenderViewEntity();
         double d3 = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double)partialTicks;
         double d4 = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double)partialTicks;
         double d5 = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double)partialTicks;
         TileEntityRendererDispatcher.staticPlayerX = d3;
         TileEntityRendererDispatcher.staticPlayerY = d4;
         TileEntityRendererDispatcher.staticPlayerZ = d5;
         this.renderManager.setRenderPosition(d3, d4, d5);
         this.mc.entityRenderer.enableLightmap();
         List list = this.theWorld.getLoadedEntityList();
         this.countEntitiesTotal = list.size();

         for(int i = 0; i < this.theWorld.weatherEffects.size(); ++i) {
            Entity entity1 = (Entity)this.theWorld.weatherEffects.get(i);
            ++this.countEntitiesRendered;
            if (entity1.isInRangeToRender3d(d0, d1, d2)) {
               this.renderManager.renderEntitySimple(entity1, partialTicks);
            }
         }

         Iterator var28 = this.renderInfos.iterator();

         label160:
         while(true) {
            ClassInheritanceMultiMap classinheritancemultimap;
            do {
               ContainerLocalRenderInformation renderglobal$containerlocalrenderinformation1;
               if (!var28.hasNext()) {
                  RenderHelper.enableStandardItemLighting();
                  var28 = this.renderInfos.iterator();

                  while(true) {
                     List list1;
                     do {
                        if (!var28.hasNext()) {
                           synchronized(this.field_181024_n) {
                              Iterator var30 = this.field_181024_n.iterator();

                              while(var30.hasNext()) {
                                 TileEntity tileentity = (TileEntity)var30.next();
                                 TileEntityRendererDispatcher.instance.renderTileEntity(tileentity, partialTicks, -1);
                              }
                           }

                           this.preRenderDamagedBlocks();
                           var28 = this.damagedBlocks.values().iterator();

                           while(true) {
                              DestroyBlockProgress destroyblockprogress;
                              TileEntity tileentity1;
                              Block block;
                              do {
                                 do {
                                    if (!var28.hasNext()) {
                                       this.postRenderDamagedBlocks();
                                       this.mc.entityRenderer.disableLightmap();
                                       return;
                                    }

                                    destroyblockprogress = (DestroyBlockProgress)var28.next();
                                    BlockPos blockpos = destroyblockprogress.getPosition();
                                    tileentity1 = this.theWorld.getTileEntity(blockpos);
                                    if (tileentity1 instanceof TileEntityChest) {
                                       TileEntityChest tileentitychest = (TileEntityChest)tileentity1;
                                       if (tileentitychest.adjacentChestXNeg != null) {
                                          blockpos = blockpos.offset(EnumFacing.WEST);
                                          tileentity1 = this.theWorld.getTileEntity(blockpos);
                                       } else if (tileentitychest.adjacentChestZNeg != null) {
                                          blockpos = blockpos.offset(EnumFacing.NORTH);
                                          tileentity1 = this.theWorld.getTileEntity(blockpos);
                                       }
                                    }

                                    block = this.theWorld.getBlockState(blockpos).getBlock();
                                 } while(tileentity1 == null);
                              } while(!(block instanceof BlockChest) && !(block instanceof BlockEnderChest) && !(block instanceof BlockSign) && !(block instanceof BlockSkull));

                              TileEntityRendererDispatcher.instance.renderTileEntity(tileentity1, partialTicks, destroyblockprogress.getPartialBlockDamage());
                           }
                        }

                        renderglobal$containerlocalrenderinformation1 = (ContainerLocalRenderInformation)var28.next();
                        list1 = renderglobal$containerlocalrenderinformation1.renderChunk.getCompiledChunk().getTileEntities();
                     } while(list1.isEmpty());

                     Iterator var35 = list1.iterator();

                     while(var35.hasNext()) {
                        TileEntity tileentity2 = (TileEntity)var35.next();
                        TileEntityRendererDispatcher.instance.renderTileEntity(tileentity2, partialTicks, -1);
                     }
                  }
               }

               renderglobal$containerlocalrenderinformation1 = (ContainerLocalRenderInformation)var28.next();
               Chunk chunk = this.theWorld.getChunkFromBlockCoords(renderglobal$containerlocalrenderinformation1.renderChunk.getPosition());
               classinheritancemultimap = chunk.getEntityLists()[renderglobal$containerlocalrenderinformation1.renderChunk.getPosition().getY() / 16];
            } while(classinheritancemultimap.isEmpty());

            Iterator iterator = classinheritancemultimap.iterator();

            while(true) {
               Entity entity2;
               boolean flag2;
               while(true) {
                  if (!iterator.hasNext()) {
                     continue label160;
                  }

                  entity2 = (Entity)iterator.next();
                  flag2 = this.renderManager.shouldRender(entity2, camera, d0, d1, d2) || entity2.riddenByEntity == this.mc.thePlayer;
                  if (!flag2) {
                     break;
                  }

                  boolean flag3 = this.mc.getRenderViewEntity() instanceof EntityLivingBase && ((EntityLivingBase)this.mc.getRenderViewEntity()).isPlayerSleeping();
                  if ((entity2 != this.mc.getRenderViewEntity() || this.mc.gameSettings.getThirdPersonView() != 0 || flag3) && (entity2.posY < 0.0 || entity2.posY >= 256.0 || this.theWorld.isBlockLoaded(new BlockPos(entity2)))) {
                     ++this.countEntitiesRendered;
                     this.renderManager.renderEntitySimple(entity2, partialTicks);
                     break;
                  }
               }

               if (!flag2 && entity2 instanceof EntityWitherSkull) {
                  this.mc.getRenderManager().renderWitherSkull(entity2, partialTicks);
               }
            }
         }
      }
   }

   public String getDebugInfoRenders() {
      int i = this.viewFrustum.renderChunks.length;
      int j = 0;
      Iterator var3 = this.renderInfos.iterator();

      while(var3.hasNext()) {
         ContainerLocalRenderInformation renderglobal$containerlocalrenderinformation = (ContainerLocalRenderInformation)var3.next();
         CompiledChunk compiledchunk = renderglobal$containerlocalrenderinformation.renderChunk.compiledChunk;
         if (compiledchunk != CompiledChunk.DUMMY && !compiledchunk.isEmpty()) {
            ++j;
         }
      }

      return String.format("C: %d/%d %sD: %d, %s", j, i, this.mc.renderChunksMany ? "(s) " : "", this.renderDistanceChunks, this.renderDispatcher.getDebugInfo());
   }

   public String getDebugInfoEntities() {
      return "E: " + this.countEntitiesRendered + "/" + this.countEntitiesTotal + ", B: " + this.countEntitiesHidden + ", I: " + (this.countEntitiesTotal - this.countEntitiesHidden - this.countEntitiesRendered);
   }

   public void setupTerrain(Entity viewEntity, double partialTicks, ICamera camera, int frameCount, boolean playerSpectator) {
      if (this.mc.gameSettings.renderDistanceChunks != this.renderDistanceChunks) {
         this.loadRenderers();
      }

      double d0 = viewEntity.posX - this.frustumUpdatePosX;
      double d1 = viewEntity.posY - this.frustumUpdatePosY;
      double d2 = viewEntity.posZ - this.frustumUpdatePosZ;
      if (this.frustumUpdatePosChunkX != viewEntity.chunkCoordX || this.frustumUpdatePosChunkY != viewEntity.chunkCoordY || this.frustumUpdatePosChunkZ != viewEntity.chunkCoordZ || d0 * d0 + d1 * d1 + d2 * d2 > 16.0) {
         this.frustumUpdatePosX = viewEntity.posX;
         this.frustumUpdatePosY = viewEntity.posY;
         this.frustumUpdatePosZ = viewEntity.posZ;
         this.frustumUpdatePosChunkX = viewEntity.chunkCoordX;
         this.frustumUpdatePosChunkY = viewEntity.chunkCoordY;
         this.frustumUpdatePosChunkZ = viewEntity.chunkCoordZ;
         this.viewFrustum.updateChunkPositions(viewEntity.posX, viewEntity.posZ);
      }

      double d3 = viewEntity.lastTickPosX + (viewEntity.posX - viewEntity.lastTickPosX) * partialTicks;
      double d4 = viewEntity.lastTickPosY + (viewEntity.posY - viewEntity.lastTickPosY) * partialTicks;
      double d5 = viewEntity.lastTickPosZ + (viewEntity.posZ - viewEntity.lastTickPosZ) * partialTicks;
      this.renderContainer.initialize(d3, d4, d5);
      if (this.debugFixedClippingHelper != null) {
         Frustum frustum = new Frustum(this.debugFixedClippingHelper);
         frustum.setPosition(this.debugTerrainFrustumPosition.field_181059_a, this.debugTerrainFrustumPosition.field_181060_b, this.debugTerrainFrustumPosition.field_181061_c);
         camera = frustum;
      }

      BlockPos blockpos1 = new BlockPos(d3, d4 + (double)viewEntity.getEyeHeight(), d5);
      RenderChunk renderchunk = this.viewFrustum.getRenderChunk(blockpos1);
      BlockPos blockpos = new BlockPos(MathHelper.floor_double(d3 / 16.0) * 16, MathHelper.floor_double(d4 / 16.0) * 16, MathHelper.floor_double(d5 / 16.0) * 16);
      this.displayListEntitiesDirty = this.displayListEntitiesDirty || !this.chunksToUpdate.isEmpty() || viewEntity.posX != this.lastViewEntityX || viewEntity.posY != this.lastViewEntityY || viewEntity.posZ != this.lastViewEntityZ || (double)viewEntity.rotationPitch != this.lastViewEntityPitch || (double)viewEntity.rotationYaw != this.lastViewEntityYaw;
      this.lastViewEntityX = viewEntity.posX;
      this.lastViewEntityY = viewEntity.posY;
      this.lastViewEntityZ = viewEntity.posZ;
      this.lastViewEntityPitch = (double)viewEntity.rotationPitch;
      this.lastViewEntityYaw = (double)viewEntity.rotationYaw;
      boolean flag = this.debugFixedClippingHelper != null;
      ContainerLocalRenderInformation renderglobal$containerlocalrenderinformation1;
      RenderChunk renderchunk3;
      if (!flag && this.displayListEntitiesDirty) {
         this.displayListEntitiesDirty = false;
         this.renderInfos = Lists.newArrayList();
         Queue queue = Lists.newLinkedList();
         boolean flag1 = this.mc.renderChunksMany;
         if (renderchunk != null) {
            boolean flag2 = false;
            ContainerLocalRenderInformation renderglobal$containerlocalrenderinformation3 = new ContainerLocalRenderInformation(renderchunk, (EnumFacing)null, 0);
            Set set1 = this.getVisibleFacings(blockpos1);
            if (set1.size() == 1) {
               Vector3f vector3f = this.getViewVector(viewEntity, partialTicks);
               EnumFacing enumfacing = EnumFacing.getFacingFromVector(vector3f.x, vector3f.y, vector3f.z).getOpposite();
               set1.remove(enumfacing);
            }

            if (set1.isEmpty()) {
               flag2 = true;
            }

            if (flag2 && !playerSpectator) {
               this.renderInfos.add(renderglobal$containerlocalrenderinformation3);
            } else {
               if (playerSpectator && this.theWorld.getBlockState(blockpos1).getBlock().isOpaqueCube()) {
                  flag1 = false;
               }

               renderchunk.setFrameIndex(frameCount);
               queue.add(renderglobal$containerlocalrenderinformation3);
            }
         } else {
            int i = blockpos1.getY() > 0 ? 248 : 8;

            for(int j = -this.renderDistanceChunks; j <= this.renderDistanceChunks; ++j) {
               for(int k = -this.renderDistanceChunks; k <= this.renderDistanceChunks; ++k) {
                  RenderChunk renderchunk1 = this.viewFrustum.getRenderChunk(new BlockPos((j << 4) + 8, i, (k << 4) + 8));
                  if (renderchunk1 != null && ((ICamera)camera).isBoundingBoxInFrustum(renderchunk1.boundingBox)) {
                     renderchunk1.setFrameIndex(frameCount);
                     queue.add(new ContainerLocalRenderInformation(renderchunk1, (EnumFacing)null, 0));
                  }
               }
            }
         }

         while(!queue.isEmpty()) {
            renderglobal$containerlocalrenderinformation1 = (ContainerLocalRenderInformation)queue.poll();
            renderchunk3 = renderglobal$containerlocalrenderinformation1.renderChunk;
            EnumFacing enumfacing2 = renderglobal$containerlocalrenderinformation1.facing;
            BlockPos blockpos2 = renderchunk3.getPosition();
            this.renderInfos.add(renderglobal$containerlocalrenderinformation1);
            EnumFacing[] var46 = EnumFacing.values();
            int var30 = var46.length;

            for(int var31 = 0; var31 < var30; ++var31) {
               EnumFacing enumfacing1 = var46[var31];
               RenderChunk renderchunk2 = this.func_181562_a(blockpos, renderchunk3, enumfacing1);
               if ((!flag1 || !renderglobal$containerlocalrenderinformation1.setFacing.contains(enumfacing1.getOpposite())) && (!flag1 || enumfacing2 == null || renderchunk3.getCompiledChunk().isVisible(enumfacing2.getOpposite(), enumfacing1)) && renderchunk2 != null && renderchunk2.setFrameIndex(frameCount) && ((ICamera)camera).isBoundingBoxInFrustum(renderchunk2.boundingBox)) {
                  ContainerLocalRenderInformation renderglobal$containerlocalrenderinformation = new ContainerLocalRenderInformation(renderchunk2, enumfacing1, renderglobal$containerlocalrenderinformation1.counter + 1);
                  renderglobal$containerlocalrenderinformation.setFacing.addAll(renderglobal$containerlocalrenderinformation1.setFacing);
                  renderglobal$containerlocalrenderinformation.setFacing.add(enumfacing1);
                  queue.add(renderglobal$containerlocalrenderinformation);
               }
            }
         }
      }

      if (this.debugFixTerrainFrustum) {
         this.fixTerrainFrustum(d3, d4, d5);
         this.debugFixTerrainFrustum = false;
      }

      this.renderDispatcher.clearChunkUpdates();
      Set set = this.chunksToUpdate;
      this.chunksToUpdate = Sets.newLinkedHashSet();
      Iterator var37 = this.renderInfos.iterator();

      while(true) {
         do {
            if (!var37.hasNext()) {
               this.chunksToUpdate.addAll(set);
               return;
            }

            renderglobal$containerlocalrenderinformation1 = (ContainerLocalRenderInformation)var37.next();
            renderchunk3 = renderglobal$containerlocalrenderinformation1.renderChunk;
         } while(!renderchunk3.isNeedsUpdate() && !set.contains(renderchunk3));

         this.displayListEntitiesDirty = true;
         if (this.isPositionInRenderChunk(blockpos, renderglobal$containerlocalrenderinformation1.renderChunk)) {
            this.renderDispatcher.updateChunkNow(renderchunk3);
            renderchunk3.setNeedsUpdate(false);
         } else {
            this.chunksToUpdate.add(renderchunk3);
         }
      }
   }

   private boolean isPositionInRenderChunk(BlockPos pos, RenderChunk renderChunkIn) {
      BlockPos blockpos = renderChunkIn.getPosition();
      return MathHelper.abs_int(pos.getX() - blockpos.getX()) <= 16 && MathHelper.abs_int(pos.getY() - blockpos.getY()) <= 16 && MathHelper.abs_int(pos.getZ() - blockpos.getZ()) <= 16;
   }

   private Set getVisibleFacings(BlockPos pos) {
      VisGraph visgraph = new VisGraph();
      BlockPos blockpos = new BlockPos(pos.getX() >> 4 << 4, pos.getY() >> 4 << 4, pos.getZ() >> 4 << 4);
      Chunk chunk = this.theWorld.getChunkFromBlockCoords(blockpos);
      Iterator var5 = BlockPos.getAllInBoxMutable(blockpos, blockpos.add(15, 15, 15)).iterator();

      while(var5.hasNext()) {
         BlockPos.MutableBlockPos blockpos$mutableblockpos = (BlockPos.MutableBlockPos)var5.next();
         if (chunk.getBlock(blockpos$mutableblockpos).isOpaqueCube()) {
            visgraph.func_178606_a(blockpos$mutableblockpos);
         }
      }

      return visgraph.func_178609_b(pos);
   }

   private RenderChunk func_181562_a(BlockPos p_181562_1_, RenderChunk p_181562_2_, EnumFacing p_181562_3_) {
      BlockPos blockpos = p_181562_2_.func_181701_a(p_181562_3_);
      return MathHelper.abs_int(p_181562_1_.getX() - blockpos.getX()) > this.renderDistanceChunks * 16 ? null : (blockpos.getY() >= 0 && blockpos.getY() < 256 ? (MathHelper.abs_int(p_181562_1_.getZ() - blockpos.getZ()) > this.renderDistanceChunks * 16 ? null : this.viewFrustum.getRenderChunk(blockpos)) : null);
   }

   private void fixTerrainFrustum(double x, double y, double z) {
      this.debugFixedClippingHelper = new ClippingHelperImpl();
      ((ClippingHelperImpl)this.debugFixedClippingHelper).init();
      Matrix4f matrix4f = new Matrix4f(this.debugFixedClippingHelper.modelviewMatrix);
      matrix4f.transpose();
      Matrix4f matrix4f1 = new Matrix4f(this.debugFixedClippingHelper.projectionMatrix);
      matrix4f1.transpose();
      Matrix4f matrix4f2 = new Matrix4f();
      Matrix4f.mul(matrix4f1, matrix4f, matrix4f2);
      matrix4f2.invert();
      this.debugTerrainFrustumPosition.field_181059_a = x;
      this.debugTerrainFrustumPosition.field_181060_b = y;
      this.debugTerrainFrustumPosition.field_181061_c = z;
      this.debugTerrainMatrix[0] = new Vector4f(-1.0F, -1.0F, -1.0F, 1.0F);
      this.debugTerrainMatrix[1] = new Vector4f(1.0F, -1.0F, -1.0F, 1.0F);
      this.debugTerrainMatrix[2] = new Vector4f(1.0F, 1.0F, -1.0F, 1.0F);
      this.debugTerrainMatrix[3] = new Vector4f(-1.0F, 1.0F, -1.0F, 1.0F);
      this.debugTerrainMatrix[4] = new Vector4f(-1.0F, -1.0F, 1.0F, 1.0F);
      this.debugTerrainMatrix[5] = new Vector4f(1.0F, -1.0F, 1.0F, 1.0F);
      this.debugTerrainMatrix[6] = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
      this.debugTerrainMatrix[7] = new Vector4f(-1.0F, 1.0F, 1.0F, 1.0F);

      for(int i = 0; i < 8; ++i) {
         Matrix4f.transform(matrix4f2, this.debugTerrainMatrix[i], this.debugTerrainMatrix[i]);
         Vector4f var10000 = this.debugTerrainMatrix[i];
         var10000.x /= this.debugTerrainMatrix[i].w;
         var10000 = this.debugTerrainMatrix[i];
         var10000.y /= this.debugTerrainMatrix[i].w;
         var10000 = this.debugTerrainMatrix[i];
         var10000.z /= this.debugTerrainMatrix[i].w;
         this.debugTerrainMatrix[i].w = 1.0F;
      }

   }

   protected Vector3f getViewVector(Entity entityIn, double partialTicks) {
      float f = (float)((double)entityIn.prevRotationPitch + (double)(entityIn.rotationPitch - entityIn.prevRotationPitch) * partialTicks);
      float f1 = (float)((double)entityIn.prevRotationYaw + (double)(entityIn.rotationYaw - entityIn.prevRotationYaw) * partialTicks);
      if (this.mc.gameSettings.getThirdPersonView() == 2) {
         f += 180.0F;
      }

      float f2 = MathHelper.cos(-f1 * 0.017453292F - 3.1415927F);
      float f3 = MathHelper.sin(-f1 * 0.017453292F - 3.1415927F);
      float f4 = -MathHelper.cos(-f * 0.017453292F);
      float f5 = MathHelper.sin(-f * 0.017453292F);
      return new Vector3f(f3 * f4, f5, f2 * f4);
   }

   public void renderBlockLayer(EnumWorldBlockLayer blockLayerIn, Entity entityIn) {
      RenderHelper.disableStandardItemLighting();
      if (blockLayerIn == EnumWorldBlockLayer.TRANSLUCENT) {
         double d0 = entityIn.posX - this.prevRenderSortX;
         double d1 = entityIn.posY - this.prevRenderSortY;
         double d2 = entityIn.posZ - this.prevRenderSortZ;
         if (d0 * d0 + d1 * d1 + d2 * d2 > 1.0) {
            this.prevRenderSortX = entityIn.posX;
            this.prevRenderSortY = entityIn.posY;
            this.prevRenderSortZ = entityIn.posZ;
            int k = 0;
            Iterator var10 = this.renderInfos.iterator();

            while(var10.hasNext()) {
               ContainerLocalRenderInformation renderglobal$containerlocalrenderinformation = (ContainerLocalRenderInformation)var10.next();
               if (renderglobal$containerlocalrenderinformation.renderChunk.compiledChunk.isLayerStarted(blockLayerIn) && k++ < 15) {
                  this.renderDispatcher.updateTransparencyLater(renderglobal$containerlocalrenderinformation.renderChunk);
               }
            }
         }
      }

      int l = 0;
      boolean flag = blockLayerIn == EnumWorldBlockLayer.TRANSLUCENT;
      int i1 = flag ? this.renderInfos.size() - 1 : 0;
      int i = flag ? -1 : this.renderInfos.size();
      int j1 = flag ? -1 : 1;

      for(int j = i1; j != i; j += j1) {
         RenderChunk renderchunk = ((ContainerLocalRenderInformation)this.renderInfos.get(j)).renderChunk;
         if (!renderchunk.getCompiledChunk().isLayerEmpty(blockLayerIn)) {
            ++l;
            this.renderContainer.addRenderChunk(renderchunk, blockLayerIn);
         }
      }

      this.renderBlockLayer(blockLayerIn);
   }

   private void renderBlockLayer(EnumWorldBlockLayer blockLayerIn) {
      this.mc.entityRenderer.enableLightmap();
      if (OpenGlHelper.useVbo()) {
         GL11.glEnableClientState(32884);
         OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
         GL11.glEnableClientState(32888);
         OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
         GL11.glEnableClientState(32888);
         OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
         GL11.glEnableClientState(32886);
      }

      this.renderContainer.renderChunkLayer(blockLayerIn);
      if (OpenGlHelper.useVbo()) {
         Iterator var2 = DefaultVertexFormats.BLOCK.getElements().iterator();

         while(var2.hasNext()) {
            VertexFormatElement vertexformatelement = (VertexFormatElement)var2.next();
            VertexFormatElement.EnumUsage vertexformatelement$enumusage = vertexformatelement.getUsage();
            int i = vertexformatelement.getIndex();
            switch (vertexformatelement$enumusage) {
               case POSITION:
                  GL11.glDisableClientState(32884);
                  break;
               case UV:
                  OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit + i);
                  GL11.glDisableClientState(32888);
                  OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
                  break;
               case COLOR:
                  GL11.glDisableClientState(32886);
            }
         }
      }

      this.mc.entityRenderer.disableLightmap();
   }

   private void cleanupDamagedBlocks(Iterator iteratorIn) {
      while(iteratorIn.hasNext()) {
         DestroyBlockProgress destroyblockprogress = (DestroyBlockProgress)iteratorIn.next();
         int i = destroyblockprogress.getCreationCloudUpdateTick();
         if (this.cloudTickCounter - i > 400) {
            iteratorIn.remove();
         }
      }

   }

   public void updateClouds() {
      ++this.cloudTickCounter;
      if (this.cloudTickCounter % 20 == 0) {
         this.cleanupDamagedBlocks(this.damagedBlocks.values().iterator());
      }

   }

   private void renderSkyEnd() {
      GlStateManager.disableFog();
      GlStateManager.disableAlpha();
      GL11.glEnable(3042);
      GL14.glBlendFuncSeparate(770, 771, 1, 0);
      RenderHelper.disableStandardItemLighting();
      GL11.glDepthMask(false);
      this.renderEngine.bindTexture(locationEndSkyPng);
      Tessellator tessellator = Tessellator.getInstance();
      WorldRenderer worldrenderer = tessellator.getWorldRenderer();

      for(int i = 0; i < 6; ++i) {
         GL11.glPushMatrix();
         if (i == 1) {
            GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
         }

         if (i == 2) {
            GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
         }

         if (i == 3) {
            GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
         }

         if (i == 4) {
            GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
         }

         if (i == 5) {
            GL11.glRotatef(-90.0F, 0.0F, 0.0F, 1.0F);
         }

         worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
         worldrenderer.pos(-100.0, -100.0, -100.0).tex(0.0, 0.0).color(40, 40, 40, 255).endVertex();
         worldrenderer.pos(-100.0, -100.0, 100.0).tex(0.0, 16.0).color(40, 40, 40, 255).endVertex();
         worldrenderer.pos(100.0, -100.0, 100.0).tex(16.0, 16.0).color(40, 40, 40, 255).endVertex();
         worldrenderer.pos(100.0, -100.0, -100.0).tex(16.0, 0.0).color(40, 40, 40, 255).endVertex();
         tessellator.draw();
         GL11.glPopMatrix();
      }

      GL11.glDepthMask(true);
      GL11.glEnable(3553);
      GlStateManager.enableAlpha();
   }

   public void renderSky(float partialTicks) {
      if (this.mc.theWorld.provider.getDimensionId() == 1) {
         this.renderSkyEnd();
      } else if (this.mc.theWorld.provider.isSurfaceWorld()) {
         GL11.glDisable(3553);
         Vec3 vec3 = this.theWorld.getSkyColor(this.mc.getRenderViewEntity(), partialTicks);
         float f = (float)vec3.xCoord;
         float f1 = (float)vec3.yCoord;
         float f2 = (float)vec3.zCoord;
         GL11.glColor3f(f, f1, f2);
         Tessellator tessellator = Tessellator.getInstance();
         WorldRenderer worldrenderer = tessellator.getWorldRenderer();
         GL11.glDepthMask(false);
         GlStateManager.enableFog();
         GL11.glColor3f(f, f1, f2);
         if (this.vboEnabled) {
            this.skyVBO.bindBuffer();
            GL11.glEnableClientState(32884);
            GL11.glVertexPointer(3, 5126, 12, 0L);
            this.skyVBO.drawArrays(7);
            this.skyVBO.unbindBuffer();
            GL11.glDisableClientState(32884);
         } else {
            GL11.glCallList(this.glSkyList);
         }

         GlStateManager.disableFog();
         GlStateManager.disableAlpha();
         GL11.glEnable(3042);
         GL14.glBlendFuncSeparate(770, 771, 1, 0);
         RenderHelper.disableStandardItemLighting();
         float[] afloat = this.theWorld.provider.calcSunriseSunsetColors(this.theWorld.getCelestialAngle(partialTicks), partialTicks);
         float f16;
         float f17;
         int l;
         float f12;
         float f13;
         if (afloat != null) {
            GL11.glDisable(3553);
            GL11.glShadeModel(7425);
            GL11.glPushMatrix();
            GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(MathHelper.sin(this.theWorld.getCelestialAngleRadians(partialTicks)) < 0.0F ? 180.0F : 0.0F, 0.0F, 0.0F, 1.0F);
            GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
            f16 = afloat[0];
            f17 = afloat[1];
            float f8 = afloat[2];
            worldrenderer.begin(6, DefaultVertexFormats.POSITION_COLOR);
            worldrenderer.pos(0.0, 100.0, 0.0).color(f16, f17, f8, afloat[3]).endVertex();

            for(l = 0; l <= 16; ++l) {
               float f21 = (float)l * 3.1415927F * 2.0F / 16.0F;
               f12 = MathHelper.sin(f21);
               f13 = MathHelper.cos(f21);
               worldrenderer.pos((double)(f12 * 120.0F), (double)(f13 * 120.0F), (double)(-f13 * 40.0F * afloat[3])).color(afloat[0], afloat[1], afloat[2], 0.0F).endVertex();
            }

            tessellator.draw();
            GL11.glPopMatrix();
            GL11.glShadeModel(7424);
         }

         GL11.glEnable(3553);
         GL14.glBlendFuncSeparate(770, 1, 1, 0);
         GL11.glPushMatrix();
         f16 = 1.0F - this.theWorld.getRainStrength(partialTicks);
         GL11.glColor4f(1.0F, 1.0F, 1.0F, f16);
         GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
         GL11.glRotatef(this.theWorld.getCelestialAngle(partialTicks) * 360.0F, 1.0F, 0.0F, 0.0F);
         f17 = 30.0F;
         this.renderEngine.bindTexture(locationSunPng);
         worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
         worldrenderer.pos((double)(-f17), 100.0, (double)(-f17)).tex(0.0, 0.0).endVertex();
         worldrenderer.pos((double)f17, 100.0, (double)(-f17)).tex(1.0, 0.0).endVertex();
         worldrenderer.pos((double)f17, 100.0, (double)f17).tex(1.0, 1.0).endVertex();
         worldrenderer.pos((double)(-f17), 100.0, (double)f17).tex(0.0, 1.0).endVertex();
         tessellator.draw();
         f17 = 20.0F;
         this.renderEngine.bindTexture(locationMoonPhasesPng);
         int i = this.theWorld.getMoonPhase();
         l = i % 4;
         int i1 = i / 4 % 2;
         f12 = (float)(l + 0) / 4.0F;
         f13 = (float)(i1 + 0) / 2.0F;
         float f24 = (float)(l + 1) / 4.0F;
         float f14 = (float)(i1 + 1) / 2.0F;
         worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
         worldrenderer.pos((double)(-f17), -100.0, (double)f17).tex((double)f24, (double)f14).endVertex();
         worldrenderer.pos((double)f17, -100.0, (double)f17).tex((double)f12, (double)f14).endVertex();
         worldrenderer.pos((double)f17, -100.0, (double)(-f17)).tex((double)f12, (double)f13).endVertex();
         worldrenderer.pos((double)(-f17), -100.0, (double)(-f17)).tex((double)f24, (double)f13).endVertex();
         tessellator.draw();
         GL11.glDisable(3553);
         float f15 = this.theWorld.getStarBrightness(partialTicks) * f16;
         if (f15 > 0.0F) {
            GL11.glColor4f(f15, f15, f15, f15);
            if (this.vboEnabled) {
               this.starVBO.bindBuffer();
               GL11.glEnableClientState(32884);
               GL11.glVertexPointer(3, 5126, 12, 0L);
               this.starVBO.drawArrays(7);
               this.starVBO.unbindBuffer();
               GL11.glDisableClientState(32884);
            } else {
               GL11.glCallList(this.starGLCallList);
            }
         }

         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         GL11.glDisable(3042);
         GlStateManager.enableAlpha();
         GlStateManager.enableFog();
         GL11.glPopMatrix();
         GL11.glDisable(3553);
         GL11.glColor3f(0.0F, 0.0F, 0.0F);
         double d0 = this.mc.thePlayer.getPositionEyes(partialTicks).yCoord - this.theWorld.getHorizon();
         if (d0 < 0.0) {
            GL11.glPushMatrix();
            GL11.glTranslatef(0.0F, 12.0F, 0.0F);
            if (this.vboEnabled) {
               this.sky2VBO.bindBuffer();
               GL11.glEnableClientState(32884);
               GL11.glVertexPointer(3, 5126, 12, 0L);
               this.sky2VBO.drawArrays(7);
               this.sky2VBO.unbindBuffer();
               GL11.glDisableClientState(32884);
            } else {
               GL11.glCallList(this.glSkyList2);
            }

            GL11.glPopMatrix();
            float f18 = 1.0F;
            float f19 = -((float)(d0 + 65.0));
            float f20 = -1.0F;
            worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
            worldrenderer.pos(-1.0, (double)f19, 1.0).color(0, 0, 0, 255).endVertex();
            worldrenderer.pos(1.0, (double)f19, 1.0).color(0, 0, 0, 255).endVertex();
            worldrenderer.pos(1.0, -1.0, 1.0).color(0, 0, 0, 255).endVertex();
            worldrenderer.pos(-1.0, -1.0, 1.0).color(0, 0, 0, 255).endVertex();
            worldrenderer.pos(-1.0, -1.0, -1.0).color(0, 0, 0, 255).endVertex();
            worldrenderer.pos(1.0, -1.0, -1.0).color(0, 0, 0, 255).endVertex();
            worldrenderer.pos(1.0, (double)f19, -1.0).color(0, 0, 0, 255).endVertex();
            worldrenderer.pos(-1.0, (double)f19, -1.0).color(0, 0, 0, 255).endVertex();
            worldrenderer.pos(1.0, -1.0, -1.0).color(0, 0, 0, 255).endVertex();
            worldrenderer.pos(1.0, -1.0, 1.0).color(0, 0, 0, 255).endVertex();
            worldrenderer.pos(1.0, (double)f19, 1.0).color(0, 0, 0, 255).endVertex();
            worldrenderer.pos(1.0, (double)f19, -1.0).color(0, 0, 0, 255).endVertex();
            worldrenderer.pos(-1.0, (double)f19, -1.0).color(0, 0, 0, 255).endVertex();
            worldrenderer.pos(-1.0, (double)f19, 1.0).color(0, 0, 0, 255).endVertex();
            worldrenderer.pos(-1.0, -1.0, 1.0).color(0, 0, 0, 255).endVertex();
            worldrenderer.pos(-1.0, -1.0, -1.0).color(0, 0, 0, 255).endVertex();
            worldrenderer.pos(-1.0, -1.0, -1.0).color(0, 0, 0, 255).endVertex();
            worldrenderer.pos(-1.0, -1.0, 1.0).color(0, 0, 0, 255).endVertex();
            worldrenderer.pos(1.0, -1.0, 1.0).color(0, 0, 0, 255).endVertex();
            worldrenderer.pos(1.0, -1.0, -1.0).color(0, 0, 0, 255).endVertex();
            tessellator.draw();
         }

         if (this.theWorld.provider.isSkyColored()) {
            GL11.glColor3f(f * 0.2F + 0.04F, f1 * 0.2F + 0.04F, f2 * 0.6F + 0.1F);
         } else {
            GL11.glColor3f(f, f1, f2);
         }

         GL11.glPushMatrix();
         GL11.glTranslatef(0.0F, -((float)(d0 - 16.0)), 0.0F);
         GL11.glCallList(this.glSkyList2);
         GL11.glPopMatrix();
         GL11.glEnable(3553);
         GL11.glDepthMask(true);
      }

   }

   public void renderClouds(float partialTicks) {
      if (this.mc.theWorld.provider.isSurfaceWorld()) {
         if (this.mc.gameSettings.func_181147_e() == 2) {
            this.renderCloudsFancy(partialTicks);
         } else {
            GL11.glDisable(2884);
            float f = (float)(this.mc.getRenderViewEntity().lastTickPosY + (this.mc.getRenderViewEntity().posY - this.mc.getRenderViewEntity().lastTickPosY) * (double)partialTicks);
            int i = true;
            int j = true;
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();
            this.renderEngine.bindTexture(locationCloudsPng);
            GL11.glEnable(3042);
            GL14.glBlendFuncSeparate(770, 771, 1, 0);
            Vec3 vec3 = this.theWorld.getCloudColour(partialTicks);
            float f1 = (float)vec3.xCoord;
            float f2 = (float)vec3.yCoord;
            float f3 = (float)vec3.zCoord;
            double d2 = (double)((float)this.cloudTickCounter + partialTicks);
            double d0 = this.mc.getRenderViewEntity().prevPosX + (this.mc.getRenderViewEntity().posX - this.mc.getRenderViewEntity().prevPosX) * (double)partialTicks + d2 * 0.029999999329447746;
            double d1 = this.mc.getRenderViewEntity().prevPosZ + (this.mc.getRenderViewEntity().posZ - this.mc.getRenderViewEntity().prevPosZ) * (double)partialTicks;
            int k = MathHelper.floor_double(d0 / 2048.0);
            int l = MathHelper.floor_double(d1 / 2048.0);
            d0 -= (double)(k * 2048);
            d1 -= (double)(l * 2048);
            float f7 = this.theWorld.provider.getCloudHeight() - f + 0.33F;
            float f8 = (float)(d0 * 4.8828125E-4);
            float f9 = (float)(d1 * 4.8828125E-4);
            worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);

            for(int i1 = -256; i1 < 256; i1 += 32) {
               for(int j1 = -256; j1 < 256; j1 += 32) {
                  worldrenderer.pos((double)(i1 + 0), (double)f7, (double)(j1 + 32)).tex((double)((float)(i1 + 0) * 4.8828125E-4F + f8), (double)((float)(j1 + 32) * 4.8828125E-4F + f9)).color(f1, f2, f3, 0.8F).endVertex();
                  worldrenderer.pos((double)(i1 + 32), (double)f7, (double)(j1 + 32)).tex((double)((float)(i1 + 32) * 4.8828125E-4F + f8), (double)((float)(j1 + 32) * 4.8828125E-4F + f9)).color(f1, f2, f3, 0.8F).endVertex();
                  worldrenderer.pos((double)(i1 + 32), (double)f7, (double)(j1 + 0)).tex((double)((float)(i1 + 32) * 4.8828125E-4F + f8), (double)((float)(j1 + 0) * 4.8828125E-4F + f9)).color(f1, f2, f3, 0.8F).endVertex();
                  worldrenderer.pos((double)(i1 + 0), (double)f7, (double)(j1 + 0)).tex((double)((float)(i1 + 0) * 4.8828125E-4F + f8), (double)((float)(j1 + 0) * 4.8828125E-4F + f9)).color(f1, f2, f3, 0.8F).endVertex();
               }
            }

            tessellator.draw();
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glDisable(3042);
            GL11.glEnable(2884);
         }
      }

   }

   private void renderCloudsFancy(float partialTicks) {
      GL11.glDisable(2884);
      float f = (float)(this.mc.getRenderViewEntity().lastTickPosY + (this.mc.getRenderViewEntity().posY - this.mc.getRenderViewEntity().lastTickPosY) * (double)partialTicks);
      Tessellator tessellator = Tessellator.getInstance();
      WorldRenderer worldrenderer = tessellator.getWorldRenderer();
      float f1 = 12.0F;
      float f2 = 4.0F;
      double d0 = (double)((float)this.cloudTickCounter + partialTicks);
      double d1 = (this.mc.getRenderViewEntity().prevPosX + (this.mc.getRenderViewEntity().posX - this.mc.getRenderViewEntity().prevPosX) * (double)partialTicks + d0 * 0.029999999329447746) / 12.0;
      double d2 = (this.mc.getRenderViewEntity().prevPosZ + (this.mc.getRenderViewEntity().posZ - this.mc.getRenderViewEntity().prevPosZ) * (double)partialTicks) / 12.0 + 0.33000001311302185;
      float f3 = this.theWorld.provider.getCloudHeight() - f + 0.33F;
      int i = MathHelper.floor_double(d1 / 2048.0);
      int j = MathHelper.floor_double(d2 / 2048.0);
      d1 -= (double)(i * 2048);
      d2 -= (double)(j * 2048);
      this.renderEngine.bindTexture(locationCloudsPng);
      GL11.glEnable(3042);
      GL14.glBlendFuncSeparate(770, 771, 1, 0);
      Vec3 vec3 = this.theWorld.getCloudColour(partialTicks);
      float f4 = (float)vec3.xCoord;
      float f5 = (float)vec3.yCoord;
      float f6 = (float)vec3.zCoord;
      float f26 = f4 * 0.9F;
      float f27 = f5 * 0.9F;
      float f28 = f6 * 0.9F;
      float f10 = f4 * 0.7F;
      float f11 = f5 * 0.7F;
      float f12 = f6 * 0.7F;
      float f13 = f4 * 0.8F;
      float f14 = f5 * 0.8F;
      float f15 = f6 * 0.8F;
      float f17 = (float)MathHelper.floor_double(d1) * 0.00390625F;
      float f18 = (float)MathHelper.floor_double(d2) * 0.00390625F;
      float f19 = (float)(d1 - (double)MathHelper.floor_double(d1));
      float f20 = (float)(d2 - (double)MathHelper.floor_double(d2));
      GL11.glScalef(12.0F, 1.0F, 12.0F);

      for(int i1 = 0; i1 < 2; ++i1) {
         if (i1 == 0) {
            GL11.glColorMask(false, false, false, false);
         } else {
            GL11.glColorMask(true, true, true, true);
         }

         for(int j1 = -3; j1 <= 4; ++j1) {
            for(int k1 = -3; k1 <= 4; ++k1) {
               worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
               float f22 = (float)(j1 * 8);
               float f23 = (float)(k1 * 8);
               float f24 = f22 - f19;
               float f25 = f23 - f20;
               if (f3 > -5.0F) {
                  worldrenderer.pos((double)(f24 + 0.0F), (double)(f3 + 0.0F), (double)(f25 + 8.0F)).tex((double)((f22 + 0.0F) * 0.00390625F + f17), (double)((f23 + 8.0F) * 0.00390625F + f18)).color(f10, f11, f12, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
                  worldrenderer.pos((double)(f24 + 8.0F), (double)(f3 + 0.0F), (double)(f25 + 8.0F)).tex((double)((f22 + 8.0F) * 0.00390625F + f17), (double)((f23 + 8.0F) * 0.00390625F + f18)).color(f10, f11, f12, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
                  worldrenderer.pos((double)(f24 + 8.0F), (double)(f3 + 0.0F), (double)(f25 + 0.0F)).tex((double)((f22 + 8.0F) * 0.00390625F + f17), (double)((f23 + 0.0F) * 0.00390625F + f18)).color(f10, f11, f12, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
                  worldrenderer.pos((double)(f24 + 0.0F), (double)(f3 + 0.0F), (double)(f25 + 0.0F)).tex((double)((f22 + 0.0F) * 0.00390625F + f17), (double)((f23 + 0.0F) * 0.00390625F + f18)).color(f10, f11, f12, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
               }

               if (f3 <= 5.0F) {
                  worldrenderer.pos((double)(f24 + 0.0F), (double)(f3 + 4.0F - 9.765625E-4F), (double)(f25 + 8.0F)).tex((double)((f22 + 0.0F) * 0.00390625F + f17), (double)((f23 + 8.0F) * 0.00390625F + f18)).color(f4, f5, f6, 0.8F).normal(0.0F, 1.0F, 0.0F).endVertex();
                  worldrenderer.pos((double)(f24 + 8.0F), (double)(f3 + 4.0F - 9.765625E-4F), (double)(f25 + 8.0F)).tex((double)((f22 + 8.0F) * 0.00390625F + f17), (double)((f23 + 8.0F) * 0.00390625F + f18)).color(f4, f5, f6, 0.8F).normal(0.0F, 1.0F, 0.0F).endVertex();
                  worldrenderer.pos((double)(f24 + 8.0F), (double)(f3 + 4.0F - 9.765625E-4F), (double)(f25 + 0.0F)).tex((double)((f22 + 8.0F) * 0.00390625F + f17), (double)((f23 + 0.0F) * 0.00390625F + f18)).color(f4, f5, f6, 0.8F).normal(0.0F, 1.0F, 0.0F).endVertex();
                  worldrenderer.pos((double)(f24 + 0.0F), (double)(f3 + 4.0F - 9.765625E-4F), (double)(f25 + 0.0F)).tex((double)((f22 + 0.0F) * 0.00390625F + f17), (double)((f23 + 0.0F) * 0.00390625F + f18)).color(f4, f5, f6, 0.8F).normal(0.0F, 1.0F, 0.0F).endVertex();
               }

               int k2;
               if (j1 > -1) {
                  for(k2 = 0; k2 < 8; ++k2) {
                     worldrenderer.pos((double)(f24 + (float)k2 + 0.0F), (double)(f3 + 0.0F), (double)(f25 + 8.0F)).tex((double)((f22 + (float)k2 + 0.5F) * 0.00390625F + f17), (double)((f23 + 8.0F) * 0.00390625F + f18)).color(f26, f27, f28, 0.8F).normal(-1.0F, 0.0F, 0.0F).endVertex();
                     worldrenderer.pos((double)(f24 + (float)k2 + 0.0F), (double)(f3 + 4.0F), (double)(f25 + 8.0F)).tex((double)((f22 + (float)k2 + 0.5F) * 0.00390625F + f17), (double)((f23 + 8.0F) * 0.00390625F + f18)).color(f26, f27, f28, 0.8F).normal(-1.0F, 0.0F, 0.0F).endVertex();
                     worldrenderer.pos((double)(f24 + (float)k2 + 0.0F), (double)(f3 + 4.0F), (double)(f25 + 0.0F)).tex((double)((f22 + (float)k2 + 0.5F) * 0.00390625F + f17), (double)((f23 + 0.0F) * 0.00390625F + f18)).color(f26, f27, f28, 0.8F).normal(-1.0F, 0.0F, 0.0F).endVertex();
                     worldrenderer.pos((double)(f24 + (float)k2 + 0.0F), (double)(f3 + 0.0F), (double)(f25 + 0.0F)).tex((double)((f22 + (float)k2 + 0.5F) * 0.00390625F + f17), (double)((f23 + 0.0F) * 0.00390625F + f18)).color(f26, f27, f28, 0.8F).normal(-1.0F, 0.0F, 0.0F).endVertex();
                  }
               }

               if (j1 <= 1) {
                  for(k2 = 0; k2 < 8; ++k2) {
                     worldrenderer.pos((double)(f24 + (float)k2 + 1.0F - 9.765625E-4F), (double)(f3 + 0.0F), (double)(f25 + 8.0F)).tex((double)((f22 + (float)k2 + 0.5F) * 0.00390625F + f17), (double)((f23 + 8.0F) * 0.00390625F + f18)).color(f26, f27, f28, 0.8F).normal(1.0F, 0.0F, 0.0F).endVertex();
                     worldrenderer.pos((double)(f24 + (float)k2 + 1.0F - 9.765625E-4F), (double)(f3 + 4.0F), (double)(f25 + 8.0F)).tex((double)((f22 + (float)k2 + 0.5F) * 0.00390625F + f17), (double)((f23 + 8.0F) * 0.00390625F + f18)).color(f26, f27, f28, 0.8F).normal(1.0F, 0.0F, 0.0F).endVertex();
                     worldrenderer.pos((double)(f24 + (float)k2 + 1.0F - 9.765625E-4F), (double)(f3 + 4.0F), (double)(f25 + 0.0F)).tex((double)((f22 + (float)k2 + 0.5F) * 0.00390625F + f17), (double)((f23 + 0.0F) * 0.00390625F + f18)).color(f26, f27, f28, 0.8F).normal(1.0F, 0.0F, 0.0F).endVertex();
                     worldrenderer.pos((double)(f24 + (float)k2 + 1.0F - 9.765625E-4F), (double)(f3 + 0.0F), (double)(f25 + 0.0F)).tex((double)((f22 + (float)k2 + 0.5F) * 0.00390625F + f17), (double)((f23 + 0.0F) * 0.00390625F + f18)).color(f26, f27, f28, 0.8F).normal(1.0F, 0.0F, 0.0F).endVertex();
                  }
               }

               if (k1 > -1) {
                  for(k2 = 0; k2 < 8; ++k2) {
                     worldrenderer.pos((double)(f24 + 0.0F), (double)(f3 + 4.0F), (double)(f25 + (float)k2 + 0.0F)).tex((double)((f22 + 0.0F) * 0.00390625F + f17), (double)((f23 + (float)k2 + 0.5F) * 0.00390625F + f18)).color(f13, f14, f15, 0.8F).normal(0.0F, 0.0F, -1.0F).endVertex();
                     worldrenderer.pos((double)(f24 + 8.0F), (double)(f3 + 4.0F), (double)(f25 + (float)k2 + 0.0F)).tex((double)((f22 + 8.0F) * 0.00390625F + f17), (double)((f23 + (float)k2 + 0.5F) * 0.00390625F + f18)).color(f13, f14, f15, 0.8F).normal(0.0F, 0.0F, -1.0F).endVertex();
                     worldrenderer.pos((double)(f24 + 8.0F), (double)(f3 + 0.0F), (double)(f25 + (float)k2 + 0.0F)).tex((double)((f22 + 8.0F) * 0.00390625F + f17), (double)((f23 + (float)k2 + 0.5F) * 0.00390625F + f18)).color(f13, f14, f15, 0.8F).normal(0.0F, 0.0F, -1.0F).endVertex();
                     worldrenderer.pos((double)(f24 + 0.0F), (double)(f3 + 0.0F), (double)(f25 + (float)k2 + 0.0F)).tex((double)((f22 + 0.0F) * 0.00390625F + f17), (double)((f23 + (float)k2 + 0.5F) * 0.00390625F + f18)).color(f13, f14, f15, 0.8F).normal(0.0F, 0.0F, -1.0F).endVertex();
                  }
               }

               if (k1 <= 1) {
                  for(k2 = 0; k2 < 8; ++k2) {
                     worldrenderer.pos((double)(f24 + 0.0F), (double)(f3 + 4.0F), (double)(f25 + (float)k2 + 1.0F - 9.765625E-4F)).tex((double)((f22 + 0.0F) * 0.00390625F + f17), (double)((f23 + (float)k2 + 0.5F) * 0.00390625F + f18)).color(f13, f14, f15, 0.8F).normal(0.0F, 0.0F, 1.0F).endVertex();
                     worldrenderer.pos((double)(f24 + 8.0F), (double)(f3 + 4.0F), (double)(f25 + (float)k2 + 1.0F - 9.765625E-4F)).tex((double)((f22 + 8.0F) * 0.00390625F + f17), (double)((f23 + (float)k2 + 0.5F) * 0.00390625F + f18)).color(f13, f14, f15, 0.8F).normal(0.0F, 0.0F, 1.0F).endVertex();
                     worldrenderer.pos((double)(f24 + 8.0F), (double)(f3 + 0.0F), (double)(f25 + (float)k2 + 1.0F - 9.765625E-4F)).tex((double)((f22 + 8.0F) * 0.00390625F + f17), (double)((f23 + (float)k2 + 0.5F) * 0.00390625F + f18)).color(f13, f14, f15, 0.8F).normal(0.0F, 0.0F, 1.0F).endVertex();
                     worldrenderer.pos((double)(f24 + 0.0F), (double)(f3 + 0.0F), (double)(f25 + (float)k2 + 1.0F - 9.765625E-4F)).tex((double)((f22 + 0.0F) * 0.00390625F + f17), (double)((f23 + (float)k2 + 0.5F) * 0.00390625F + f18)).color(f13, f14, f15, 0.8F).normal(0.0F, 0.0F, 1.0F).endVertex();
                  }
               }

               tessellator.draw();
            }
         }
      }

      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      GL11.glDisable(3042);
      GL11.glEnable(2884);
   }

   public void updateChunks(long finishTimeNano) {
      this.displayListEntitiesDirty |= this.renderDispatcher.runChunkUploads(finishTimeNano);
      if (!this.chunksToUpdate.isEmpty()) {
         Iterator iterator = this.chunksToUpdate.iterator();

         while(iterator.hasNext()) {
            RenderChunk renderchunk = (RenderChunk)iterator.next();
            if (!this.renderDispatcher.updateChunkLater(renderchunk)) {
               break;
            }

            renderchunk.setNeedsUpdate(false);
            iterator.remove();
            long i = finishTimeNano - System.nanoTime();
            if (i < 0L) {
               break;
            }
         }
      }

   }

   public void renderWorldBorder(Entity p_180449_1_, float partialTicks) {
      Tessellator tessellator = Tessellator.getInstance();
      WorldRenderer worldrenderer = tessellator.getWorldRenderer();
      WorldBorder worldborder = this.theWorld.getWorldBorder();
      double d0 = (double)(this.mc.gameSettings.renderDistanceChunks * 16);
      if (p_180449_1_.posX >= worldborder.maxX() - d0 || p_180449_1_.posX <= worldborder.minX() + d0 || p_180449_1_.posZ >= worldborder.maxZ() - d0 || p_180449_1_.posZ <= worldborder.minZ() + d0) {
         double d1 = 1.0 - worldborder.getClosestDistance(p_180449_1_) / d0;
         d1 = Math.pow(d1, 4.0);
         double d2 = p_180449_1_.lastTickPosX + (p_180449_1_.posX - p_180449_1_.lastTickPosX) * (double)partialTicks;
         double d3 = p_180449_1_.lastTickPosY + (p_180449_1_.posY - p_180449_1_.lastTickPosY) * (double)partialTicks;
         double d4 = p_180449_1_.lastTickPosZ + (p_180449_1_.posZ - p_180449_1_.lastTickPosZ) * (double)partialTicks;
         GL11.glEnable(3042);
         GL14.glBlendFuncSeparate(770, 1, 1, 0);
         this.renderEngine.bindTexture(locationForcefieldPng);
         GL11.glDepthMask(false);
         GL11.glPushMatrix();
         int i = worldborder.getStatus().getID();
         float f = (float)(i >> 16 & 255) / 255.0F;
         float f1 = (float)(i >> 8 & 255) / 255.0F;
         float f2 = (float)(i & 255) / 255.0F;
         GL11.glColor4f(f, f1, f2, (float)d1);
         GlStateManager.doPolygonOffset(-3.0F, -3.0F);
         GlStateManager.enablePolygonOffset();
         GL11.glAlphaFunc(516, 0.1F);
         GlStateManager.enableAlpha();
         GL11.glDisable(2884);
         float f3 = (float)(Minecraft.getSystemTime() % 3000L) / 3000.0F;
         float f4 = 0.0F;
         float f5 = 0.0F;
         float f6 = 128.0F;
         worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
         worldrenderer.setTranslation(-d2, -d3, -d4);
         double d5 = Math.max((double)MathHelper.floor_double(d4 - d0), worldborder.minZ());
         double d6 = Math.min((double)MathHelper.ceiling_double_int(d4 + d0), worldborder.maxZ());
         float f11;
         double d11;
         double d14;
         float f14;
         if (d2 > worldborder.maxX() - d0) {
            f11 = 0.0F;

            for(d11 = d5; d11 < d6; f11 += 0.5F) {
               d14 = Math.min(1.0, d6 - d11);
               f14 = (float)d14 * 0.5F;
               worldrenderer.pos(worldborder.maxX(), 256.0, d11).tex((double)(f3 + f11), (double)(f3 + 0.0F)).endVertex();
               worldrenderer.pos(worldborder.maxX(), 256.0, d11 + d14).tex((double)(f3 + f14 + f11), (double)(f3 + 0.0F)).endVertex();
               worldrenderer.pos(worldborder.maxX(), 0.0, d11 + d14).tex((double)(f3 + f14 + f11), (double)(f3 + 128.0F)).endVertex();
               worldrenderer.pos(worldborder.maxX(), 0.0, d11).tex((double)(f3 + f11), (double)(f3 + 128.0F)).endVertex();
               ++d11;
            }
         }

         if (d2 < worldborder.minX() + d0) {
            f11 = 0.0F;

            for(d11 = d5; d11 < d6; f11 += 0.5F) {
               d14 = Math.min(1.0, d6 - d11);
               f14 = (float)d14 * 0.5F;
               worldrenderer.pos(worldborder.minX(), 256.0, d11).tex((double)(f3 + f11), (double)(f3 + 0.0F)).endVertex();
               worldrenderer.pos(worldborder.minX(), 256.0, d11 + d14).tex((double)(f3 + f14 + f11), (double)(f3 + 0.0F)).endVertex();
               worldrenderer.pos(worldborder.minX(), 0.0, d11 + d14).tex((double)(f3 + f14 + f11), (double)(f3 + 128.0F)).endVertex();
               worldrenderer.pos(worldborder.minX(), 0.0, d11).tex((double)(f3 + f11), (double)(f3 + 128.0F)).endVertex();
               ++d11;
            }
         }

         d5 = Math.max((double)MathHelper.floor_double(d2 - d0), worldborder.minX());
         d6 = Math.min((double)MathHelper.ceiling_double_int(d2 + d0), worldborder.maxX());
         if (d4 > worldborder.maxZ() - d0) {
            f11 = 0.0F;

            for(d11 = d5; d11 < d6; f11 += 0.5F) {
               d14 = Math.min(1.0, d6 - d11);
               f14 = (float)d14 * 0.5F;
               worldrenderer.pos(d11, 256.0, worldborder.maxZ()).tex((double)(f3 + f11), (double)(f3 + 0.0F)).endVertex();
               worldrenderer.pos(d11 + d14, 256.0, worldborder.maxZ()).tex((double)(f3 + f14 + f11), (double)(f3 + 0.0F)).endVertex();
               worldrenderer.pos(d11 + d14, 0.0, worldborder.maxZ()).tex((double)(f3 + f14 + f11), (double)(f3 + 128.0F)).endVertex();
               worldrenderer.pos(d11, 0.0, worldborder.maxZ()).tex((double)(f3 + f11), (double)(f3 + 128.0F)).endVertex();
               ++d11;
            }
         }

         if (d4 < worldborder.minZ() + d0) {
            f11 = 0.0F;

            for(d11 = d5; d11 < d6; f11 += 0.5F) {
               d14 = Math.min(1.0, d6 - d11);
               f14 = (float)d14 * 0.5F;
               worldrenderer.pos(d11, 256.0, worldborder.minZ()).tex((double)(f3 + f11), (double)(f3 + 0.0F)).endVertex();
               worldrenderer.pos(d11 + d14, 256.0, worldborder.minZ()).tex((double)(f3 + f14 + f11), (double)(f3 + 0.0F)).endVertex();
               worldrenderer.pos(d11 + d14, 0.0, worldborder.minZ()).tex((double)(f3 + f14 + f11), (double)(f3 + 128.0F)).endVertex();
               worldrenderer.pos(d11, 0.0, worldborder.minZ()).tex((double)(f3 + f11), (double)(f3 + 128.0F)).endVertex();
               ++d11;
            }
         }

         tessellator.draw();
         worldrenderer.setTranslation(0.0, 0.0, 0.0);
         GL11.glEnable(2884);
         GlStateManager.disableAlpha();
         GlStateManager.doPolygonOffset(0.0F, 0.0F);
         GlStateManager.disablePolygonOffset();
         GlStateManager.enableAlpha();
         GL11.glDisable(3042);
         GL11.glPopMatrix();
         GL11.glDepthMask(true);
      }

   }

   private void preRenderDamagedBlocks() {
      GL14.glBlendFuncSeparate(774, 768, 1, 0);
      GL11.glEnable(3042);
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.5F);
      GlStateManager.doPolygonOffset(-3.0F, -3.0F);
      GlStateManager.enablePolygonOffset();
      GL11.glAlphaFunc(516, 0.1F);
      GlStateManager.enableAlpha();
      GL11.glPushMatrix();
   }

   private void postRenderDamagedBlocks() {
      GlStateManager.disableAlpha();
      GlStateManager.doPolygonOffset(0.0F, 0.0F);
      GlStateManager.disablePolygonOffset();
      GlStateManager.enableAlpha();
      GL11.glDepthMask(true);
      GL11.glPopMatrix();
   }

   public void drawBlockDamageTexture(Tessellator tessellatorIn, WorldRenderer worldRendererIn, Entity entityIn, float partialTicks) {
      double d0 = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * (double)partialTicks;
      double d1 = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * (double)partialTicks;
      double d2 = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * (double)partialTicks;
      if (!this.damagedBlocks.isEmpty()) {
         this.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
         this.preRenderDamagedBlocks();
         worldRendererIn.begin(7, DefaultVertexFormats.BLOCK);
         worldRendererIn.setTranslation(-d0, -d1, -d2);
         worldRendererIn.markDirty();
         Iterator iterator = this.damagedBlocks.values().iterator();

         while(iterator.hasNext()) {
            DestroyBlockProgress destroyblockprogress = (DestroyBlockProgress)iterator.next();
            BlockPos blockpos = destroyblockprogress.getPosition();
            double d3 = (double)blockpos.getX() - d0;
            double d4 = (double)blockpos.getY() - d1;
            double d5 = (double)blockpos.getZ() - d2;
            Block block = this.theWorld.getBlockState(blockpos).getBlock();
            if (!(block instanceof BlockChest) && !(block instanceof BlockEnderChest) && !(block instanceof BlockSign) && !(block instanceof BlockSkull)) {
               if (d3 * d3 + d4 * d4 + d5 * d5 > 1024.0) {
                  iterator.remove();
               } else {
                  IBlockState iblockstate = this.theWorld.getBlockState(blockpos);
                  if (iblockstate.getBlock().getMaterial() != Material.air) {
                     int i = destroyblockprogress.getPartialBlockDamage();
                     TextureAtlasSprite textureatlassprite = this.destroyBlockIcons[i];
                     BlockRendererDispatcher blockrendererdispatcher = this.mc.getBlockRendererDispatcher();
                     blockrendererdispatcher.renderBlockDamage(iblockstate, blockpos, textureatlassprite, this.theWorld);
                  }
               }
            }
         }

         tessellatorIn.draw();
         worldRendererIn.setTranslation(0.0, 0.0, 0.0);
         this.postRenderDamagedBlocks();
      }

   }

   public void drawSelectionBox(EntityPlayer player, MovingObjectPosition movingObjectPositionIn, int p_72731_3_, float partialTicks) {
      if (p_72731_3_ == 0 && movingObjectPositionIn.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
         GL11.glEnable(3042);
         GL14.glBlendFuncSeparate(770, 771, 1, 0);
         GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.4F);
         GL11.glDisable(3553);
         GL11.glDepthMask(false);
         float f = 0.002F;
         BlockPos blockpos = movingObjectPositionIn.getBlockPos();
         Block block = this.theWorld.getBlockState(blockpos).getBlock();
         if (block.getMaterial() != Material.air && this.theWorld.getWorldBorder().contains(blockpos)) {
            block.setBlockBoundsBasedOnState(this.theWorld, blockpos);
            double d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double)partialTicks;
            double d1 = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double)partialTicks;
            double d2 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double)partialTicks;
            DrawSelectedBoundingBoxEvent event = new DrawSelectedBoundingBoxEvent();
            KetamineClient.getInstance().getEventBus().post(event);
            DrawUtil.glColour(event.getColour());
            DrawUtil.glDrawBoundingBox(block.getSelectedBoundingBox(this.theWorld, blockpos).expand(0.0020000000949949026, 0.0020000000949949026, 0.0020000000949949026).offset(-d0, -d1, -d2), event.getOutlineWidth(), event.isFilled());
         }

         GL11.glDepthMask(true);
         GL11.glEnable(3553);
         GL11.glDisable(3042);
      }

   }

   private void markBlocksForUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {
      this.viewFrustum.markBlocksForUpdate(x1, y1, z1, x2, y2, z2);
   }

   private void spawnParticle(EnumParticleTypes particleIn, double p_174972_2_, double p_174972_4_, double p_174972_6_, double p_174972_8_, double p_174972_10_, double p_174972_12_, int... p_174972_14_) {
      this.spawnParticle(particleIn.getParticleID(), particleIn.getShouldIgnoreRange(), p_174972_2_, p_174972_4_, p_174972_6_, p_174972_8_, p_174972_10_, p_174972_12_, p_174972_14_);
   }

   private EntityFX spawnEntityFX(int p_174974_1_, boolean ignoreRange, double p_174974_3_, double p_174974_5_, double p_174974_7_, double p_174974_9_, double p_174974_11_, double p_174974_13_, int... p_174974_15_) {
      if (this.mc != null && this.mc.getRenderViewEntity() != null && this.mc.effectRenderer != null) {
         int i = this.mc.gameSettings.particleSetting;
         if (i == 1 && this.theWorld.rand.nextInt(3) == 0) {
            i = 2;
         }

         double d0 = this.mc.getRenderViewEntity().posX - p_174974_3_;
         double d1 = this.mc.getRenderViewEntity().posY - p_174974_5_;
         double d2 = this.mc.getRenderViewEntity().posZ - p_174974_7_;
         if (ignoreRange) {
            return this.mc.effectRenderer.spawnEffectParticle(p_174974_1_, p_174974_3_, p_174974_5_, p_174974_7_, p_174974_9_, p_174974_11_, p_174974_13_, p_174974_15_);
         } else {
            double d3 = 16.0;
            return d0 * d0 + d1 * d1 + d2 * d2 > 256.0 ? null : (i > 1 ? null : this.mc.effectRenderer.spawnEffectParticle(p_174974_1_, p_174974_3_, p_174974_5_, p_174974_7_, p_174974_9_, p_174974_11_, p_174974_13_, p_174974_15_));
         }
      } else {
         return null;
      }
   }

   public void deleteAllDisplayLists() {
   }

   public void setDisplayListEntitiesDirty() {
      this.displayListEntitiesDirty = true;
   }

   public void func_181023_a(Collection p_181023_1_, Collection p_181023_2_) {
      synchronized(this.field_181024_n) {
         this.field_181024_n.removeAll(p_181023_1_);
         this.field_181024_n.addAll(p_181023_2_);
      }
   }

   class ContainerLocalRenderInformation {
      final RenderChunk renderChunk;
      final EnumFacing facing;
      final Set setFacing;
      final int counter;

      private ContainerLocalRenderInformation(RenderChunk renderChunkIn, EnumFacing facingIn, int counterIn) {
         this.setFacing = EnumSet.noneOf(EnumFacing.class);
         this.renderChunk = renderChunkIn;
         this.facing = facingIn;
         this.counter = counterIn;
      }

      // $FF: synthetic method
      ContainerLocalRenderInformation(RenderChunk x1, EnumFacing x2, int x3, Object x4) {
         this(x1, x2, x3);
      }
   }
}
