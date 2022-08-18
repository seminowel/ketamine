package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHugeMushroom;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class WorldGenBigMushroom extends WorldGenerator {
   private Block mushroomType;

   public WorldGenBigMushroom(Block p_i46449_1_) {
      super(true);
      this.mushroomType = p_i46449_1_;
   }

   public WorldGenBigMushroom() {
      super(false);
   }

   public boolean generate(World worldIn, Random rand, BlockPos position) {
      if (this.mushroomType == null) {
         this.mushroomType = rand.nextBoolean() ? Blocks.brown_mushroom_block : Blocks.red_mushroom_block;
      }

      int i = rand.nextInt(3) + 4;
      boolean flag = true;
      if (position.getY() >= 1 && position.getY() + i + 1 < 256) {
         int k2;
         int j3;
         int k3;
         for(int j = position.getY(); j <= position.getY() + 1 + i; ++j) {
            k2 = 3;
            if (j <= position.getY() + 3) {
               k2 = 0;
            }

            BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

            for(j3 = position.getX() - k2; j3 <= position.getX() + k2 && flag; ++j3) {
               for(k3 = position.getZ() - k2; k3 <= position.getZ() + k2 && flag; ++k3) {
                  if (j >= 0 && j < 256) {
                     Block block = worldIn.getBlockState(blockpos$mutableblockpos.func_181079_c(j3, j, k3)).getBlock();
                     if (block.getMaterial() != Material.air && block.getMaterial() != Material.leaves) {
                        flag = false;
                     }
                  } else {
                     flag = false;
                  }
               }
            }
         }

         if (!flag) {
            return false;
         } else {
            Block block1 = worldIn.getBlockState(position.down()).getBlock();
            if (block1 != Blocks.dirt && block1 != Blocks.grass && block1 != Blocks.mycelium) {
               return false;
            } else {
               k2 = position.getY() + i;
               if (this.mushroomType == Blocks.red_mushroom_block) {
                  k2 = position.getY() + i - 3;
               }

               int l2;
               for(l2 = k2; l2 <= position.getY() + i; ++l2) {
                  j3 = 1;
                  if (l2 < position.getY() + i) {
                     ++j3;
                  }

                  if (this.mushroomType == Blocks.brown_mushroom_block) {
                     j3 = 3;
                  }

                  k3 = position.getX() - j3;
                  int l3 = position.getX() + j3;
                  int j1 = position.getZ() - j3;
                  int k1 = position.getZ() + j3;

                  for(int l1 = k3; l1 <= l3; ++l1) {
                     for(int i2 = j1; i2 <= k1; ++i2) {
                        int j2 = 5;
                        if (l1 == k3) {
                           --j2;
                        } else if (l1 == l3) {
                           ++j2;
                        }

                        if (i2 == j1) {
                           j2 -= 3;
                        } else if (i2 == k1) {
                           j2 += 3;
                        }

                        BlockHugeMushroom.EnumType blockhugemushroom$enumtype = BlockHugeMushroom.EnumType.byMetadata(j2);
                        if (this.mushroomType == Blocks.brown_mushroom_block || l2 < position.getY() + i) {
                           if ((l1 == k3 || l1 == l3) && (i2 == j1 || i2 == k1)) {
                              continue;
                           }

                           if (l1 == position.getX() - (j3 - 1) && i2 == j1) {
                              blockhugemushroom$enumtype = BlockHugeMushroom.EnumType.NORTH_WEST;
                           }

                           if (l1 == k3 && i2 == position.getZ() - (j3 - 1)) {
                              blockhugemushroom$enumtype = BlockHugeMushroom.EnumType.NORTH_WEST;
                           }

                           if (l1 == position.getX() + (j3 - 1) && i2 == j1) {
                              blockhugemushroom$enumtype = BlockHugeMushroom.EnumType.NORTH_EAST;
                           }

                           if (l1 == l3 && i2 == position.getZ() - (j3 - 1)) {
                              blockhugemushroom$enumtype = BlockHugeMushroom.EnumType.NORTH_EAST;
                           }

                           if (l1 == position.getX() - (j3 - 1) && i2 == k1) {
                              blockhugemushroom$enumtype = BlockHugeMushroom.EnumType.SOUTH_WEST;
                           }

                           if (l1 == k3 && i2 == position.getZ() + (j3 - 1)) {
                              blockhugemushroom$enumtype = BlockHugeMushroom.EnumType.SOUTH_WEST;
                           }

                           if (l1 == position.getX() + (j3 - 1) && i2 == k1) {
                              blockhugemushroom$enumtype = BlockHugeMushroom.EnumType.SOUTH_EAST;
                           }

                           if (l1 == l3 && i2 == position.getZ() + (j3 - 1)) {
                              blockhugemushroom$enumtype = BlockHugeMushroom.EnumType.SOUTH_EAST;
                           }
                        }

                        if (blockhugemushroom$enumtype == BlockHugeMushroom.EnumType.CENTER && l2 < position.getY() + i) {
                           blockhugemushroom$enumtype = BlockHugeMushroom.EnumType.ALL_INSIDE;
                        }

                        if (position.getY() >= position.getY() + i - 1 || blockhugemushroom$enumtype != BlockHugeMushroom.EnumType.ALL_INSIDE) {
                           BlockPos blockpos = new BlockPos(l1, l2, i2);
                           if (!worldIn.getBlockState(blockpos).getBlock().isFullBlock()) {
                              this.setBlockAndNotifyAdequately(worldIn, blockpos, this.mushroomType.getDefaultState().withProperty(BlockHugeMushroom.VARIANT, blockhugemushroom$enumtype));
                           }
                        }
                     }
                  }
               }

               for(l2 = 0; l2 < i; ++l2) {
                  Block block2 = worldIn.getBlockState(position.up(l2)).getBlock();
                  if (!block2.isFullBlock()) {
                     this.setBlockAndNotifyAdequately(worldIn, position.up(l2), this.mushroomType.getDefaultState().withProperty(BlockHugeMushroom.VARIANT, BlockHugeMushroom.EnumType.STEM));
                  }
               }

               return true;
            }
         }
      } else {
         return false;
      }
   }
}
