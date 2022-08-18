package net.minecraft.block;

import com.google.common.base.Predicate;
import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockDoor extends Block {
   public static final PropertyDirection FACING;
   public static final PropertyBool OPEN;
   public static final PropertyEnum HINGE;
   public static final PropertyBool POWERED;
   public static final PropertyEnum HALF;

   protected BlockDoor(Material materialIn) {
      super(materialIn);
      this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(OPEN, false).withProperty(HINGE, BlockDoor.EnumHingePosition.LEFT).withProperty(POWERED, false).withProperty(HALF, BlockDoor.EnumDoorHalf.LOWER));
   }

   public String getLocalizedName() {
      return StatCollector.translateToLocal((this.getUnlocalizedName() + ".name").replaceAll("tile", "item"));
   }

   public boolean isOpaqueCube() {
      return false;
   }

   public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
      return isOpen(combineMetadata(worldIn, pos));
   }

   public boolean isFullCube() {
      return false;
   }

   public AxisAlignedBB getSelectedBoundingBox(World worldIn, BlockPos pos) {
      this.setBlockBoundsBasedOnState(worldIn, pos);
      return super.getSelectedBoundingBox(worldIn, pos);
   }

   public AxisAlignedBB getCollisionBoundingBox(World worldIn, BlockPos pos, IBlockState state) {
      this.setBlockBoundsBasedOnState(worldIn, pos);
      return super.getCollisionBoundingBox(worldIn, pos, state);
   }

   public void setBlockBoundsBasedOnState(IBlockAccess worldIn, BlockPos pos) {
      this.setBoundBasedOnMeta(combineMetadata(worldIn, pos));
   }

   private void setBoundBasedOnMeta(int combinedMeta) {
      float f = 0.1875F;
      this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 2.0F, 1.0F);
      EnumFacing enumfacing = getFacing(combinedMeta);
      boolean flag = isOpen(combinedMeta);
      boolean flag1 = isHingeLeft(combinedMeta);
      if (flag) {
         if (enumfacing == EnumFacing.EAST) {
            if (!flag1) {
               this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, f);
            } else {
               this.setBlockBounds(0.0F, 0.0F, 1.0F - f, 1.0F, 1.0F, 1.0F);
            }
         } else if (enumfacing == EnumFacing.SOUTH) {
            if (!flag1) {
               this.setBlockBounds(1.0F - f, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
            } else {
               this.setBlockBounds(0.0F, 0.0F, 0.0F, f, 1.0F, 1.0F);
            }
         } else if (enumfacing == EnumFacing.WEST) {
            if (!flag1) {
               this.setBlockBounds(0.0F, 0.0F, 1.0F - f, 1.0F, 1.0F, 1.0F);
            } else {
               this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, f);
            }
         } else if (enumfacing == EnumFacing.NORTH) {
            if (!flag1) {
               this.setBlockBounds(0.0F, 0.0F, 0.0F, f, 1.0F, 1.0F);
            } else {
               this.setBlockBounds(1.0F - f, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
            }
         }
      } else if (enumfacing == EnumFacing.EAST) {
         this.setBlockBounds(0.0F, 0.0F, 0.0F, f, 1.0F, 1.0F);
      } else if (enumfacing == EnumFacing.SOUTH) {
         this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, f);
      } else if (enumfacing == EnumFacing.WEST) {
         this.setBlockBounds(1.0F - f, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
      } else if (enumfacing == EnumFacing.NORTH) {
         this.setBlockBounds(0.0F, 0.0F, 1.0F - f, 1.0F, 1.0F, 1.0F);
      }

   }

   public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ) {
      if (this.blockMaterial == Material.iron) {
         return true;
      } else {
         BlockPos blockpos = state.getValue(HALF) == BlockDoor.EnumDoorHalf.LOWER ? pos : pos.down();
         IBlockState iblockstate = pos.equals(blockpos) ? state : worldIn.getBlockState(blockpos);
         if (iblockstate.getBlock() != this) {
            return false;
         } else {
            state = iblockstate.cycleProperty(OPEN);
            worldIn.setBlockState(blockpos, state, 2);
            worldIn.markBlockRangeForRenderUpdate(blockpos, pos);
            worldIn.playAuxSFXAtEntity(playerIn, (Boolean)state.getValue(OPEN) ? 1003 : 1006, pos, 0);
            return true;
         }
      }
   }

   public void toggleDoor(World worldIn, BlockPos pos, boolean open) {
      IBlockState iblockstate = worldIn.getBlockState(pos);
      if (iblockstate.getBlock() == this) {
         BlockPos blockpos = iblockstate.getValue(HALF) == BlockDoor.EnumDoorHalf.LOWER ? pos : pos.down();
         IBlockState iblockstate1 = pos == blockpos ? iblockstate : worldIn.getBlockState(blockpos);
         if (iblockstate1.getBlock() == this && (Boolean)iblockstate1.getValue(OPEN) != open) {
            worldIn.setBlockState(blockpos, iblockstate1.withProperty(OPEN, open), 2);
            worldIn.markBlockRangeForRenderUpdate(blockpos, pos);
            worldIn.playAuxSFXAtEntity((EntityPlayer)null, open ? 1003 : 1006, pos, 0);
         }
      }

   }

   public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock) {
      if (state.getValue(HALF) == BlockDoor.EnumDoorHalf.UPPER) {
         BlockPos blockpos = pos.down();
         IBlockState iblockstate = worldIn.getBlockState(blockpos);
         if (iblockstate.getBlock() != this) {
            worldIn.setBlockToAir(pos);
         } else if (neighborBlock != this) {
            this.onNeighborBlockChange(worldIn, blockpos, iblockstate, neighborBlock);
         }
      } else {
         boolean flag1 = false;
         BlockPos blockpos1 = pos.up();
         IBlockState iblockstate1 = worldIn.getBlockState(blockpos1);
         if (iblockstate1.getBlock() != this) {
            worldIn.setBlockToAir(pos);
            flag1 = true;
         }

         if (!World.doesBlockHaveSolidTopSurface(worldIn, pos.down())) {
            worldIn.setBlockToAir(pos);
            flag1 = true;
            if (iblockstate1.getBlock() == this) {
               worldIn.setBlockToAir(blockpos1);
            }
         }

         if (flag1) {
            if (!worldIn.isRemote) {
               this.dropBlockAsItem(worldIn, pos, state, 0);
            }
         } else {
            boolean flag = worldIn.isBlockPowered(pos) || worldIn.isBlockPowered(blockpos1);
            if ((flag || neighborBlock.canProvidePower()) && neighborBlock != this && flag != (Boolean)iblockstate1.getValue(POWERED)) {
               worldIn.setBlockState(blockpos1, iblockstate1.withProperty(POWERED, flag), 2);
               if (flag != (Boolean)state.getValue(OPEN)) {
                  worldIn.setBlockState(pos, state.withProperty(OPEN, flag), 2);
                  worldIn.markBlockRangeForRenderUpdate(pos, pos);
                  worldIn.playAuxSFXAtEntity((EntityPlayer)null, flag ? 1003 : 1006, pos, 0);
               }
            }
         }
      }

   }

   public Item getItemDropped(IBlockState state, Random rand, int fortune) {
      return state.getValue(HALF) == BlockDoor.EnumDoorHalf.UPPER ? null : this.getItem();
   }

   public MovingObjectPosition collisionRayTrace(World worldIn, BlockPos pos, Vec3 start, Vec3 end) {
      this.setBlockBoundsBasedOnState(worldIn, pos);
      return super.collisionRayTrace(worldIn, pos, start, end);
   }

   public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
      return pos.getY() >= 255 ? false : World.doesBlockHaveSolidTopSurface(worldIn, pos.down()) && super.canPlaceBlockAt(worldIn, pos) && super.canPlaceBlockAt(worldIn, pos.up());
   }

   public int getMobilityFlag() {
      return 1;
   }

   public static int combineMetadata(IBlockAccess worldIn, BlockPos pos) {
      IBlockState iblockstate = worldIn.getBlockState(pos);
      int i = iblockstate.getBlock().getMetaFromState(iblockstate);
      boolean flag = isTop(i);
      IBlockState iblockstate1 = worldIn.getBlockState(pos.down());
      int j = iblockstate1.getBlock().getMetaFromState(iblockstate1);
      int k = flag ? j : i;
      IBlockState iblockstate2 = worldIn.getBlockState(pos.up());
      int l = iblockstate2.getBlock().getMetaFromState(iblockstate2);
      int i1 = flag ? i : l;
      boolean flag1 = (i1 & 1) != 0;
      boolean flag2 = (i1 & 2) != 0;
      return removeHalfBit(k) | (flag ? 8 : 0) | (flag1 ? 16 : 0) | (flag2 ? 32 : 0);
   }

   public Item getItem(World worldIn, BlockPos pos) {
      return this.getItem();
   }

   private Item getItem() {
      return this == Blocks.iron_door ? Items.iron_door : (this == Blocks.spruce_door ? Items.spruce_door : (this == Blocks.birch_door ? Items.birch_door : (this == Blocks.jungle_door ? Items.jungle_door : (this == Blocks.acacia_door ? Items.acacia_door : (this == Blocks.dark_oak_door ? Items.dark_oak_door : Items.oak_door)))));
   }

   public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
      BlockPos blockpos = pos.down();
      if (player.capabilities.isCreativeMode && state.getValue(HALF) == BlockDoor.EnumDoorHalf.UPPER && worldIn.getBlockState(blockpos).getBlock() == this) {
         worldIn.setBlockToAir(blockpos);
      }

   }

   public EnumWorldBlockLayer getBlockLayer() {
      return EnumWorldBlockLayer.CUTOUT;
   }

   public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
      IBlockState iblockstate;
      if (state.getValue(HALF) == BlockDoor.EnumDoorHalf.LOWER) {
         iblockstate = worldIn.getBlockState(pos.up());
         if (iblockstate.getBlock() == this) {
            state = state.withProperty(HINGE, iblockstate.getValue(HINGE)).withProperty(POWERED, iblockstate.getValue(POWERED));
         }
      } else {
         iblockstate = worldIn.getBlockState(pos.down());
         if (iblockstate.getBlock() == this) {
            state = state.withProperty(FACING, iblockstate.getValue(FACING)).withProperty(OPEN, iblockstate.getValue(OPEN));
         }
      }

      return state;
   }

   public IBlockState getStateFromMeta(int meta) {
      return (meta & 8) > 0 ? this.getDefaultState().withProperty(HALF, BlockDoor.EnumDoorHalf.UPPER).withProperty(HINGE, (meta & 1) > 0 ? BlockDoor.EnumHingePosition.RIGHT : BlockDoor.EnumHingePosition.LEFT).withProperty(POWERED, (meta & 2) > 0) : this.getDefaultState().withProperty(HALF, BlockDoor.EnumDoorHalf.LOWER).withProperty(FACING, EnumFacing.getHorizontal(meta & 3).rotateYCCW()).withProperty(OPEN, (meta & 4) > 0);
   }

   public int getMetaFromState(IBlockState state) {
      int i = 0;
      if (state.getValue(HALF) == BlockDoor.EnumDoorHalf.UPPER) {
         i |= 8;
         if (state.getValue(HINGE) == BlockDoor.EnumHingePosition.RIGHT) {
            i |= 1;
         }

         if ((Boolean)state.getValue(POWERED)) {
            i |= 2;
         }
      } else {
         i |= ((EnumFacing)state.getValue(FACING)).rotateY().getHorizontalIndex();
         if ((Boolean)state.getValue(OPEN)) {
            i |= 4;
         }
      }

      return i;
   }

   protected static int removeHalfBit(int meta) {
      return meta & 7;
   }

   public static boolean isOpen(IBlockAccess worldIn, BlockPos pos) {
      return isOpen(combineMetadata(worldIn, pos));
   }

   public static EnumFacing getFacing(IBlockAccess worldIn, BlockPos pos) {
      return getFacing(combineMetadata(worldIn, pos));
   }

   public static EnumFacing getFacing(int combinedMeta) {
      return EnumFacing.getHorizontal(combinedMeta & 3).rotateYCCW();
   }

   protected static boolean isOpen(int combinedMeta) {
      return (combinedMeta & 4) != 0;
   }

   protected static boolean isTop(int meta) {
      return (meta & 8) != 0;
   }

   protected static boolean isHingeLeft(int combinedMeta) {
      return (combinedMeta & 16) != 0;
   }

   protected BlockState createBlockState() {
      return new BlockState(this, new IProperty[]{HALF, FACING, OPEN, HINGE, POWERED});
   }

   static {
      FACING = PropertyDirection.create("facing", (Predicate)EnumFacing.Plane.HORIZONTAL);
      OPEN = PropertyBool.create("open");
      HINGE = PropertyEnum.create("hinge", EnumHingePosition.class);
      POWERED = PropertyBool.create("powered");
      HALF = PropertyEnum.create("half", EnumDoorHalf.class);
   }

   public static enum EnumHingePosition implements IStringSerializable {
      LEFT,
      RIGHT;

      public String toString() {
         return this.getName();
      }

      public String getName() {
         return this == LEFT ? "left" : "right";
      }
   }

   public static enum EnumDoorHalf implements IStringSerializable {
      UPPER,
      LOWER;

      public String toString() {
         return this.getName();
      }

      public String getName() {
         return this == UPPER ? "upper" : "lower";
      }
   }
}
