package net.minecraft.client.particle;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityRainFX extends EntityFX {
   protected EntityRainFX(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn) {
      super(worldIn, xCoordIn, yCoordIn, zCoordIn, 0.0, 0.0, 0.0);
      this.motionX *= 0.30000001192092896;
      this.motionY = Math.random() * 0.20000000298023224 + 0.10000000149011612;
      this.motionZ *= 0.30000001192092896;
      this.particleRed = 1.0F;
      this.particleGreen = 1.0F;
      this.particleBlue = 1.0F;
      this.setParticleTextureIndex(19 + this.rand.nextInt(4));
      this.setSize(0.01F, 0.01F);
      this.particleGravity = 0.06F;
      this.particleMaxAge = (int)(8.0 / (Math.random() * 0.8 + 0.2));
   }

   public void onUpdate() {
      this.prevPosX = this.posX;
      this.prevPosY = this.posY;
      this.prevPosZ = this.posZ;
      this.motionY -= (double)this.particleGravity;
      this.moveEntity(this.motionX, this.motionY, this.motionZ);
      this.motionX *= 0.9800000190734863;
      this.motionY *= 0.9800000190734863;
      this.motionZ *= 0.9800000190734863;
      if (this.particleMaxAge-- <= 0) {
         this.setDead();
      }

      if (this.onGround) {
         if (Math.random() < 0.5) {
            this.setDead();
         }

         this.motionX *= 0.699999988079071;
         this.motionZ *= 0.699999988079071;
      }

      BlockPos blockpos = new BlockPos(this);
      IBlockState iblockstate = this.worldObj.getBlockState(blockpos);
      Block block = iblockstate.getBlock();
      block.setBlockBoundsBasedOnState(this.worldObj, blockpos);
      Material material = iblockstate.getBlock().getMaterial();
      if (material.isLiquid() || material.isSolid()) {
         double d0 = 0.0;
         if (iblockstate.getBlock() instanceof BlockLiquid) {
            d0 = (double)(1.0F - BlockLiquid.getLiquidHeightPercent((Integer)iblockstate.getValue(BlockLiquid.LEVEL)));
         } else {
            d0 = block.getBlockBoundsMaxY();
         }

         double d1 = (double)MathHelper.floor_double(this.posY) + d0;
         if (this.posY < d1) {
            this.setDead();
         }
      }

   }

   public static class Factory implements IParticleFactory {
      public EntityFX getEntityFX(int particleID, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, int... p_178902_15_) {
         return new EntityRainFX(worldIn, xCoordIn, yCoordIn, zCoordIn);
      }
   }
}
