package net.minecraft.world.gen;

import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.SpawnerAnimals;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.feature.WorldGenDungeons;
import net.minecraft.world.gen.feature.WorldGenLakes;
import net.minecraft.world.gen.structure.MapGenMineshaft;
import net.minecraft.world.gen.structure.MapGenScatteredFeature;
import net.minecraft.world.gen.structure.MapGenStronghold;
import net.minecraft.world.gen.structure.MapGenVillage;
import net.minecraft.world.gen.structure.StructureOceanMonument;

public class ChunkProviderGenerate implements IChunkProvider {
   private Random rand;
   private NoiseGeneratorOctaves field_147431_j;
   private NoiseGeneratorOctaves field_147432_k;
   private NoiseGeneratorOctaves field_147429_l;
   private NoiseGeneratorPerlin field_147430_m;
   public NoiseGeneratorOctaves noiseGen5;
   public NoiseGeneratorOctaves noiseGen6;
   public NoiseGeneratorOctaves mobSpawnerNoise;
   private World worldObj;
   private final boolean mapFeaturesEnabled;
   private WorldType field_177475_o;
   private final double[] field_147434_q;
   private final float[] parabolicField;
   private ChunkProviderSettings settings;
   private Block field_177476_s;
   private double[] stoneNoise;
   private MapGenBase caveGenerator;
   private MapGenStronghold strongholdGenerator;
   private MapGenVillage villageGenerator;
   private MapGenMineshaft mineshaftGenerator;
   private MapGenScatteredFeature scatteredFeatureGenerator;
   private MapGenBase ravineGenerator;
   private StructureOceanMonument oceanMonumentGenerator;
   private BiomeGenBase[] biomesForGeneration;
   double[] field_147427_d;
   double[] field_147428_e;
   double[] field_147425_f;
   double[] field_147426_g;

   public ChunkProviderGenerate(World worldIn, long p_i45636_2_, boolean p_i45636_4_, String p_i45636_5_) {
      this.field_177476_s = Blocks.water;
      this.stoneNoise = new double[256];
      this.caveGenerator = new MapGenCaves();
      this.strongholdGenerator = new MapGenStronghold();
      this.villageGenerator = new MapGenVillage();
      this.mineshaftGenerator = new MapGenMineshaft();
      this.scatteredFeatureGenerator = new MapGenScatteredFeature();
      this.ravineGenerator = new MapGenRavine();
      this.oceanMonumentGenerator = new StructureOceanMonument();
      this.worldObj = worldIn;
      this.mapFeaturesEnabled = p_i45636_4_;
      this.field_177475_o = worldIn.getWorldInfo().getTerrainType();
      this.rand = new Random(p_i45636_2_);
      this.field_147431_j = new NoiseGeneratorOctaves(this.rand, 16);
      this.field_147432_k = new NoiseGeneratorOctaves(this.rand, 16);
      this.field_147429_l = new NoiseGeneratorOctaves(this.rand, 8);
      this.field_147430_m = new NoiseGeneratorPerlin(this.rand, 4);
      this.noiseGen5 = new NoiseGeneratorOctaves(this.rand, 10);
      this.noiseGen6 = new NoiseGeneratorOctaves(this.rand, 16);
      this.mobSpawnerNoise = new NoiseGeneratorOctaves(this.rand, 8);
      this.field_147434_q = new double[825];
      this.parabolicField = new float[25];

      for(int i = -2; i <= 2; ++i) {
         for(int j = -2; j <= 2; ++j) {
            float f = 10.0F / MathHelper.sqrt_float((float)(i * i + j * j) + 0.2F);
            this.parabolicField[i + 2 + (j + 2) * 5] = f;
         }
      }

      if (p_i45636_5_ != null) {
         this.settings = ChunkProviderSettings.Factory.jsonToFactory(p_i45636_5_).func_177864_b();
         this.field_177476_s = this.settings.useLavaOceans ? Blocks.lava : Blocks.water;
         worldIn.func_181544_b(this.settings.seaLevel);
      }

   }

   public void setBlocksInChunk(int p_180518_1_, int p_180518_2_, ChunkPrimer p_180518_3_) {
      this.biomesForGeneration = this.worldObj.getWorldChunkManager().getBiomesForGeneration(this.biomesForGeneration, p_180518_1_ * 4 - 2, p_180518_2_ * 4 - 2, 10, 10);
      this.func_147423_a(p_180518_1_ * 4, 0, p_180518_2_ * 4);

      for(int i = 0; i < 4; ++i) {
         int j = i * 5;
         int k = (i + 1) * 5;

         for(int l = 0; l < 4; ++l) {
            int i1 = (j + l) * 33;
            int j1 = (j + l + 1) * 33;
            int k1 = (k + l) * 33;
            int l1 = (k + l + 1) * 33;

            for(int i2 = 0; i2 < 32; ++i2) {
               double d0 = 0.125;
               double d1 = this.field_147434_q[i1 + i2];
               double d2 = this.field_147434_q[j1 + i2];
               double d3 = this.field_147434_q[k1 + i2];
               double d4 = this.field_147434_q[l1 + i2];
               double d5 = (this.field_147434_q[i1 + i2 + 1] - d1) * d0;
               double d6 = (this.field_147434_q[j1 + i2 + 1] - d2) * d0;
               double d7 = (this.field_147434_q[k1 + i2 + 1] - d3) * d0;
               double d8 = (this.field_147434_q[l1 + i2 + 1] - d4) * d0;

               for(int j2 = 0; j2 < 8; ++j2) {
                  double d9 = 0.25;
                  double d10 = d1;
                  double d11 = d2;
                  double d12 = (d3 - d1) * d9;
                  double d13 = (d4 - d2) * d9;

                  for(int k2 = 0; k2 < 4; ++k2) {
                     double d14 = 0.25;
                     double d16 = (d11 - d10) * d14;
                     double lvt_45_1_ = d10 - d16;

                     for(int l2 = 0; l2 < 4; ++l2) {
                        if ((lvt_45_1_ += d16) > 0.0) {
                           p_180518_3_.setBlockState(i * 4 + k2, i2 * 8 + j2, l * 4 + l2, Blocks.stone.getDefaultState());
                        } else if (i2 * 8 + j2 < this.settings.seaLevel) {
                           p_180518_3_.setBlockState(i * 4 + k2, i2 * 8 + j2, l * 4 + l2, this.field_177476_s.getDefaultState());
                        }
                     }

                     d10 += d12;
                     d11 += d13;
                  }

                  d1 += d5;
                  d2 += d6;
                  d3 += d7;
                  d4 += d8;
               }
            }
         }
      }

   }

   public void replaceBlocksForBiome(int p_180517_1_, int p_180517_2_, ChunkPrimer p_180517_3_, BiomeGenBase[] p_180517_4_) {
      double d0 = 0.03125;
      this.stoneNoise = this.field_147430_m.func_151599_a(this.stoneNoise, (double)(p_180517_1_ * 16), (double)(p_180517_2_ * 16), 16, 16, d0 * 2.0, d0 * 2.0, 1.0);

      for(int i = 0; i < 16; ++i) {
         for(int j = 0; j < 16; ++j) {
            BiomeGenBase biomegenbase = p_180517_4_[j + i * 16];
            biomegenbase.genTerrainBlocks(this.worldObj, this.rand, p_180517_3_, p_180517_1_ * 16 + i, p_180517_2_ * 16 + j, this.stoneNoise[j + i * 16]);
         }
      }

   }

   public Chunk provideChunk(int x, int z) {
      this.rand.setSeed((long)x * 341873128712L + (long)z * 132897987541L);
      ChunkPrimer chunkprimer = new ChunkPrimer();
      this.setBlocksInChunk(x, z, chunkprimer);
      this.biomesForGeneration = this.worldObj.getWorldChunkManager().loadBlockGeneratorData(this.biomesForGeneration, x * 16, z * 16, 16, 16);
      this.replaceBlocksForBiome(x, z, chunkprimer, this.biomesForGeneration);
      if (this.settings.useCaves) {
         this.caveGenerator.generate(this, this.worldObj, x, z, chunkprimer);
      }

      if (this.settings.useRavines) {
         this.ravineGenerator.generate(this, this.worldObj, x, z, chunkprimer);
      }

      if (this.settings.useMineShafts && this.mapFeaturesEnabled) {
         this.mineshaftGenerator.generate(this, this.worldObj, x, z, chunkprimer);
      }

      if (this.settings.useVillages && this.mapFeaturesEnabled) {
         this.villageGenerator.generate(this, this.worldObj, x, z, chunkprimer);
      }

      if (this.settings.useStrongholds && this.mapFeaturesEnabled) {
         this.strongholdGenerator.generate(this, this.worldObj, x, z, chunkprimer);
      }

      if (this.settings.useTemples && this.mapFeaturesEnabled) {
         this.scatteredFeatureGenerator.generate(this, this.worldObj, x, z, chunkprimer);
      }

      if (this.settings.useMonuments && this.mapFeaturesEnabled) {
         this.oceanMonumentGenerator.generate(this, this.worldObj, x, z, chunkprimer);
      }

      Chunk chunk = new Chunk(this.worldObj, chunkprimer, x, z);
      byte[] abyte = chunk.getBiomeArray();

      for(int i = 0; i < abyte.length; ++i) {
         abyte[i] = (byte)this.biomesForGeneration[i].biomeID;
      }

      chunk.generateSkylightMap();
      return chunk;
   }

   private void func_147423_a(int p_147423_1_, int p_147423_2_, int p_147423_3_) {
      this.field_147426_g = this.noiseGen6.generateNoiseOctaves(this.field_147426_g, p_147423_1_, p_147423_3_, 5, 5, (double)this.settings.depthNoiseScaleX, (double)this.settings.depthNoiseScaleZ, (double)this.settings.depthNoiseScaleExponent);
      float f = this.settings.coordinateScale;
      float f1 = this.settings.heightScale;
      this.field_147427_d = this.field_147429_l.generateNoiseOctaves(this.field_147427_d, p_147423_1_, p_147423_2_, p_147423_3_, 5, 33, 5, (double)(f / this.settings.mainNoiseScaleX), (double)(f1 / this.settings.mainNoiseScaleY), (double)(f / this.settings.mainNoiseScaleZ));
      this.field_147428_e = this.field_147431_j.generateNoiseOctaves(this.field_147428_e, p_147423_1_, p_147423_2_, p_147423_3_, 5, 33, 5, (double)f, (double)f1, (double)f);
      this.field_147425_f = this.field_147432_k.generateNoiseOctaves(this.field_147425_f, p_147423_1_, p_147423_2_, p_147423_3_, 5, 33, 5, (double)f, (double)f1, (double)f);
      int p_147423_3_ = false;
      int p_147423_1_ = false;
      int i = 0;
      int j = 0;

      for(int k = 0; k < 5; ++k) {
         for(int l = 0; l < 5; ++l) {
            float f2 = 0.0F;
            float f3 = 0.0F;
            float f4 = 0.0F;
            int i1 = 2;
            BiomeGenBase biomegenbase = this.biomesForGeneration[k + 2 + (l + 2) * 10];

            for(int j1 = -i1; j1 <= i1; ++j1) {
               for(int k1 = -i1; k1 <= i1; ++k1) {
                  BiomeGenBase biomegenbase1 = this.biomesForGeneration[k + j1 + 2 + (l + k1 + 2) * 10];
                  float f5 = this.settings.biomeDepthOffSet + biomegenbase1.minHeight * this.settings.biomeDepthWeight;
                  float f6 = this.settings.biomeScaleOffset + biomegenbase1.maxHeight * this.settings.biomeScaleWeight;
                  if (this.field_177475_o == WorldType.AMPLIFIED && f5 > 0.0F) {
                     f5 = 1.0F + f5 * 2.0F;
                     f6 = 1.0F + f6 * 4.0F;
                  }

                  float f7 = this.parabolicField[j1 + 2 + (k1 + 2) * 5] / (f5 + 2.0F);
                  if (biomegenbase1.minHeight > biomegenbase.minHeight) {
                     f7 /= 2.0F;
                  }

                  f2 += f6 * f7;
                  f3 += f5 * f7;
                  f4 += f7;
               }
            }

            f2 /= f4;
            f3 /= f4;
            f2 = f2 * 0.9F + 0.1F;
            f3 = (f3 * 4.0F - 1.0F) / 8.0F;
            double d7 = this.field_147426_g[j] / 8000.0;
            if (d7 < 0.0) {
               d7 = -d7 * 0.3;
            }

            d7 = d7 * 3.0 - 2.0;
            if (d7 < 0.0) {
               d7 /= 2.0;
               if (d7 < -1.0) {
                  d7 = -1.0;
               }

               d7 /= 1.4;
               d7 /= 2.0;
            } else {
               if (d7 > 1.0) {
                  d7 = 1.0;
               }

               d7 /= 8.0;
            }

            ++j;
            double d8 = (double)f3;
            double d9 = (double)f2;
            d8 += d7 * 0.2;
            d8 = d8 * (double)this.settings.baseSize / 8.0;
            double d0 = (double)this.settings.baseSize + d8 * 4.0;

            for(int l1 = 0; l1 < 33; ++l1) {
               double d1 = ((double)l1 - d0) * (double)this.settings.stretchY * 128.0 / 256.0 / d9;
               if (d1 < 0.0) {
                  d1 *= 4.0;
               }

               double d2 = this.field_147428_e[i] / (double)this.settings.lowerLimitScale;
               double d3 = this.field_147425_f[i] / (double)this.settings.upperLimitScale;
               double d4 = (this.field_147427_d[i] / 10.0 + 1.0) / 2.0;
               double d5 = MathHelper.denormalizeClamp(d2, d3, d4) - d1;
               if (l1 > 29) {
                  double d6 = (double)((float)(l1 - 29) / 3.0F);
                  d5 = d5 * (1.0 - d6) + -10.0 * d6;
               }

               this.field_147434_q[i] = d5;
               ++i;
            }
         }
      }

   }

   public boolean chunkExists(int x, int z) {
      return true;
   }

   public void populate(IChunkProvider p_73153_1_, int p_73153_2_, int p_73153_3_) {
      BlockFalling.fallInstantly = true;
      int i = p_73153_2_ * 16;
      int j = p_73153_3_ * 16;
      BlockPos blockpos = new BlockPos(i, 0, j);
      BiomeGenBase biomegenbase = this.worldObj.getBiomeGenForCoords(blockpos.add(16, 0, 16));
      this.rand.setSeed(this.worldObj.getSeed());
      long k = this.rand.nextLong() / 2L * 2L + 1L;
      long l = this.rand.nextLong() / 2L * 2L + 1L;
      this.rand.setSeed((long)p_73153_2_ * k + (long)p_73153_3_ * l ^ this.worldObj.getSeed());
      boolean flag = false;
      ChunkCoordIntPair chunkcoordintpair = new ChunkCoordIntPair(p_73153_2_, p_73153_3_);
      if (this.settings.useMineShafts && this.mapFeaturesEnabled) {
         this.mineshaftGenerator.generateStructure(this.worldObj, this.rand, chunkcoordintpair);
      }

      if (this.settings.useVillages && this.mapFeaturesEnabled) {
         flag = this.villageGenerator.generateStructure(this.worldObj, this.rand, chunkcoordintpair);
      }

      if (this.settings.useStrongholds && this.mapFeaturesEnabled) {
         this.strongholdGenerator.generateStructure(this.worldObj, this.rand, chunkcoordintpair);
      }

      if (this.settings.useTemples && this.mapFeaturesEnabled) {
         this.scatteredFeatureGenerator.generateStructure(this.worldObj, this.rand, chunkcoordintpair);
      }

      if (this.settings.useMonuments && this.mapFeaturesEnabled) {
         this.oceanMonumentGenerator.generateStructure(this.worldObj, this.rand, chunkcoordintpair);
      }

      int k2;
      int j3;
      int l3;
      if (biomegenbase != BiomeGenBase.desert && biomegenbase != BiomeGenBase.desertHills && this.settings.useWaterLakes && !flag && this.rand.nextInt(this.settings.waterLakeChance) == 0) {
         k2 = this.rand.nextInt(16) + 8;
         j3 = this.rand.nextInt(256);
         l3 = this.rand.nextInt(16) + 8;
         (new WorldGenLakes(Blocks.water)).generate(this.worldObj, this.rand, blockpos.add(k2, j3, l3));
      }

      if (!flag && this.rand.nextInt(this.settings.lavaLakeChance / 10) == 0 && this.settings.useLavaLakes) {
         k2 = this.rand.nextInt(16) + 8;
         j3 = this.rand.nextInt(this.rand.nextInt(248) + 8);
         l3 = this.rand.nextInt(16) + 8;
         if (j3 < this.worldObj.func_181545_F() || this.rand.nextInt(this.settings.lavaLakeChance / 8) == 0) {
            (new WorldGenLakes(Blocks.lava)).generate(this.worldObj, this.rand, blockpos.add(k2, j3, l3));
         }
      }

      if (this.settings.useDungeons) {
         for(k2 = 0; k2 < this.settings.dungeonChance; ++k2) {
            j3 = this.rand.nextInt(16) + 8;
            l3 = this.rand.nextInt(256);
            int l1 = this.rand.nextInt(16) + 8;
            (new WorldGenDungeons()).generate(this.worldObj, this.rand, blockpos.add(j3, l3, l1));
         }
      }

      biomegenbase.decorate(this.worldObj, this.rand, new BlockPos(i, 0, j));
      SpawnerAnimals.performWorldGenSpawning(this.worldObj, biomegenbase, i + 8, j + 8, 16, 16, this.rand);
      blockpos = blockpos.add(8, 0, 8);

      for(k2 = 0; k2 < 16; ++k2) {
         for(j3 = 0; j3 < 16; ++j3) {
            BlockPos blockpos1 = this.worldObj.getPrecipitationHeight(blockpos.add(k2, 0, j3));
            BlockPos blockpos2 = blockpos1.down();
            if (this.worldObj.canBlockFreezeWater(blockpos2)) {
               this.worldObj.setBlockState(blockpos2, Blocks.ice.getDefaultState(), 2);
            }

            if (this.worldObj.canSnowAt(blockpos1, true)) {
               this.worldObj.setBlockState(blockpos1, Blocks.snow_layer.getDefaultState(), 2);
            }
         }
      }

      BlockFalling.fallInstantly = false;
   }

   public boolean func_177460_a(IChunkProvider p_177460_1_, Chunk p_177460_2_, int p_177460_3_, int p_177460_4_) {
      boolean flag = false;
      if (this.settings.useMonuments && this.mapFeaturesEnabled && p_177460_2_.getInhabitedTime() < 3600L) {
         flag |= this.oceanMonumentGenerator.generateStructure(this.worldObj, this.rand, new ChunkCoordIntPair(p_177460_3_, p_177460_4_));
      }

      return flag;
   }

   public boolean saveChunks(boolean p_73151_1_, IProgressUpdate progressCallback) {
      return true;
   }

   public void saveExtraData() {
   }

   public boolean unloadQueuedChunks() {
      return false;
   }

   public boolean canSave() {
      return true;
   }

   public String makeString() {
      return "RandomLevelSource";
   }

   public List getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
      BiomeGenBase biomegenbase = this.worldObj.getBiomeGenForCoords(pos);
      if (this.mapFeaturesEnabled) {
         if (creatureType == EnumCreatureType.MONSTER && this.scatteredFeatureGenerator.func_175798_a(pos)) {
            return this.scatteredFeatureGenerator.getScatteredFeatureSpawnList();
         }

         if (creatureType == EnumCreatureType.MONSTER && this.settings.useMonuments && this.oceanMonumentGenerator.func_175796_a(this.worldObj, pos)) {
            return this.oceanMonumentGenerator.func_175799_b();
         }
      }

      return biomegenbase.getSpawnableList(creatureType);
   }

   public BlockPos getStrongholdGen(World worldIn, String structureName, BlockPos position) {
      return "Stronghold".equals(structureName) && this.strongholdGenerator != null ? this.strongholdGenerator.getClosestStrongholdPos(worldIn, position) : null;
   }

   public int getLoadedChunkCount() {
      return 0;
   }

   public void recreateStructures(Chunk p_180514_1_, int p_180514_2_, int p_180514_3_) {
      if (this.settings.useMineShafts && this.mapFeaturesEnabled) {
         this.mineshaftGenerator.generate(this, this.worldObj, p_180514_2_, p_180514_3_, (ChunkPrimer)null);
      }

      if (this.settings.useVillages && this.mapFeaturesEnabled) {
         this.villageGenerator.generate(this, this.worldObj, p_180514_2_, p_180514_3_, (ChunkPrimer)null);
      }

      if (this.settings.useStrongholds && this.mapFeaturesEnabled) {
         this.strongholdGenerator.generate(this, this.worldObj, p_180514_2_, p_180514_3_, (ChunkPrimer)null);
      }

      if (this.settings.useTemples && this.mapFeaturesEnabled) {
         this.scatteredFeatureGenerator.generate(this, this.worldObj, p_180514_2_, p_180514_3_, (ChunkPrimer)null);
      }

      if (this.settings.useMonuments && this.mapFeaturesEnabled) {
         this.oceanMonumentGenerator.generate(this, this.worldObj, p_180514_2_, p_180514_3_, (ChunkPrimer)null);
      }

   }

   public Chunk provideChunk(BlockPos blockPosIn) {
      return this.provideChunk(blockPosIn.getX() >> 4, blockPosIn.getZ() >> 4);
   }
}
