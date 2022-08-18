package net.minecraft.block;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Tuple;
import net.minecraft.world.World;

public class BlockSponge extends Block {
   public static final PropertyBool WET = PropertyBool.create("wet");

   protected BlockSponge() {
      super(Material.sponge);
      this.setDefaultState(this.blockState.getBaseState().withProperty(WET, false));
      this.setCreativeTab(CreativeTabs.tabBlock);
   }

   public String getLocalizedName() {
      return StatCollector.translateToLocal(this.getUnlocalizedName() + ".dry.name");
   }

   public int damageDropped(IBlockState state) {
      return (Boolean)state.getValue(WET) ? 1 : 0;
   }

   public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
      this.tryAbsorb(worldIn, pos, state);
   }

   public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock) {
      this.tryAbsorb(worldIn, pos, state);
      super.onNeighborBlockChange(worldIn, pos, state, neighborBlock);
   }

   protected void tryAbsorb(World worldIn, BlockPos pos, IBlockState state) {
      if (!(Boolean)state.getValue(WET) && this.absorb(worldIn, pos)) {
         worldIn.setBlockState(pos, state.withProperty(WET, true), 2);
         worldIn.playAuxSFX(2001, pos, Block.getIdFromBlock(Blocks.water));
      }

   }

   private boolean absorb(World worldIn, BlockPos pos) {
      Queue queue = Lists.newLinkedList();
      ArrayList arraylist = Lists.newArrayList();
      queue.add(new Tuple(pos, 0));
      int i = 0;

      BlockPos blockpos;
      while(!queue.isEmpty()) {
         Tuple tuple = (Tuple)queue.poll();
         blockpos = (BlockPos)tuple.getFirst();
         int j = (Integer)tuple.getSecond();
         EnumFacing[] var9 = EnumFacing.values();
         int var10 = var9.length;

         for(int var11 = 0; var11 < var10; ++var11) {
            EnumFacing enumfacing = var9[var11];
            BlockPos blockpos1 = blockpos.offset(enumfacing);
            if (worldIn.getBlockState(blockpos1).getBlock().getMaterial() == Material.water) {
               worldIn.setBlockState(blockpos1, Blocks.air.getDefaultState(), 2);
               arraylist.add(blockpos1);
               ++i;
               if (j < 6) {
                  queue.add(new Tuple(blockpos1, j + 1));
               }
            }
         }

         if (i > 64) {
            break;
         }
      }

      Iterator var14 = arraylist.iterator();

      while(var14.hasNext()) {
         blockpos = (BlockPos)var14.next();
         worldIn.notifyNeighborsOfStateChange(blockpos, Blocks.air);
      }

      return i > 0;
   }

   public void getSubBlocks(Item itemIn, CreativeTabs tab, List list) {
      list.add(new ItemStack(itemIn, 1, 0));
      list.add(new ItemStack(itemIn, 1, 1));
   }

   public IBlockState getStateFromMeta(int meta) {
      return this.getDefaultState().withProperty(WET, (meta & 1) == 1);
   }

   public int getMetaFromState(IBlockState state) {
      return (Boolean)state.getValue(WET) ? 1 : 0;
   }

   protected BlockState createBlockState() {
      return new BlockState(this, new IProperty[]{WET});
   }

   public void randomDisplayTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
      if ((Boolean)state.getValue(WET)) {
         EnumFacing enumfacing = EnumFacing.random(rand);
         if (enumfacing != EnumFacing.UP && !World.doesBlockHaveSolidTopSurface(worldIn, pos.offset(enumfacing))) {
            double d0 = (double)pos.getX();
            double d1 = (double)pos.getY();
            double d2 = (double)pos.getZ();
            if (enumfacing == EnumFacing.DOWN) {
               d1 -= 0.05;
               d0 += rand.nextDouble();
               d2 += rand.nextDouble();
            } else {
               d1 += rand.nextDouble() * 0.8;
               if (enumfacing.getAxis() == EnumFacing.Axis.X) {
                  d2 += rand.nextDouble();
                  if (enumfacing == EnumFacing.EAST) {
                     ++d0;
                  } else {
                     d0 += 0.05;
                  }
               } else {
                  d0 += rand.nextDouble();
                  if (enumfacing == EnumFacing.SOUTH) {
                     ++d2;
                  } else {
                     d2 += 0.05;
                  }
               }
            }

            worldIn.spawnParticle(EnumParticleTypes.DRIP_WATER, d0, d1, d2, 0.0, 0.0, 0.0);
         }
      }

   }
}
