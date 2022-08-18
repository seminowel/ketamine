package net.minecraft.block;

import java.util.List;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;

public class BlockSandStone extends Block {
   public static final PropertyEnum TYPE = PropertyEnum.create("type", EnumType.class);

   public BlockSandStone() {
      super(Material.rock);
      this.setDefaultState(this.blockState.getBaseState().withProperty(TYPE, BlockSandStone.EnumType.DEFAULT));
      this.setCreativeTab(CreativeTabs.tabBlock);
   }

   public int damageDropped(IBlockState state) {
      return ((EnumType)state.getValue(TYPE)).getMetadata();
   }

   public void getSubBlocks(Item itemIn, CreativeTabs tab, List list) {
      EnumType[] var4 = BlockSandStone.EnumType.values();
      int var5 = var4.length;

      for(int var6 = 0; var6 < var5; ++var6) {
         EnumType blocksandstone$enumtype = var4[var6];
         list.add(new ItemStack(itemIn, 1, blocksandstone$enumtype.getMetadata()));
      }

   }

   public MapColor getMapColor(IBlockState state) {
      return MapColor.sandColor;
   }

   public IBlockState getStateFromMeta(int meta) {
      return this.getDefaultState().withProperty(TYPE, BlockSandStone.EnumType.byMetadata(meta));
   }

   public int getMetaFromState(IBlockState state) {
      return ((EnumType)state.getValue(TYPE)).getMetadata();
   }

   protected BlockState createBlockState() {
      return new BlockState(this, new IProperty[]{TYPE});
   }

   public static enum EnumType implements IStringSerializable {
      DEFAULT(0, "sandstone", "default"),
      CHISELED(1, "chiseled_sandstone", "chiseled"),
      SMOOTH(2, "smooth_sandstone", "smooth");

      private static final EnumType[] META_LOOKUP = new EnumType[values().length];
      private final int metadata;
      private final String name;
      private final String unlocalizedName;

      private EnumType(int meta, String name, String unlocalizedName) {
         this.metadata = meta;
         this.name = name;
         this.unlocalizedName = unlocalizedName;
      }

      public int getMetadata() {
         return this.metadata;
      }

      public String toString() {
         return this.name;
      }

      public static EnumType byMetadata(int meta) {
         if (meta < 0 || meta >= META_LOOKUP.length) {
            meta = 0;
         }

         return META_LOOKUP[meta];
      }

      public String getName() {
         return this.name;
      }

      public String getUnlocalizedName() {
         return this.unlocalizedName;
      }

      static {
         EnumType[] var0 = values();
         int var1 = var0.length;

         for(int var2 = 0; var2 < var1; ++var2) {
            EnumType blocksandstone$enumtype = var0[var2];
            META_LOOKUP[blocksandstone$enumtype.getMetadata()] = blocksandstone$enumtype;
         }

      }
   }
}
