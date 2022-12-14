package net.minecraft.client.particle;

import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityLavaFX extends EntityFX {
   private float lavaParticleScale;

   protected EntityLavaFX(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn) {
      super(worldIn, xCoordIn, yCoordIn, zCoordIn, 0.0, 0.0, 0.0);
      this.motionX *= 0.800000011920929;
      this.motionY *= 0.800000011920929;
      this.motionZ *= 0.800000011920929;
      this.motionY = (double)(this.rand.nextFloat() * 0.4F + 0.05F);
      this.particleRed = this.particleGreen = this.particleBlue = 1.0F;
      this.particleScale *= this.rand.nextFloat() * 2.0F + 0.2F;
      this.lavaParticleScale = this.particleScale;
      this.particleMaxAge = (int)(16.0 / (Math.random() * 0.8 + 0.2));
      this.noClip = false;
      this.setParticleTextureIndex(49);
   }

   public int getBrightnessForRender(float partialTicks) {
      float f = ((float)this.particleAge + partialTicks) / (float)this.particleMaxAge;
      f = MathHelper.clamp_float(f, 0.0F, 1.0F);
      int i = super.getBrightnessForRender(partialTicks);
      int j = 240;
      int k = i >> 16 & 255;
      return j | k << 16;
   }

   public float getBrightness(float partialTicks) {
      return 1.0F;
   }

   public void renderParticle(WorldRenderer worldRendererIn, Entity entityIn, float partialTicks, float p_180434_4_, float p_180434_5_, float p_180434_6_, float p_180434_7_, float p_180434_8_) {
      float f = ((float)this.particleAge + partialTicks) / (float)this.particleMaxAge;
      this.particleScale = this.lavaParticleScale * (1.0F - f * f);
      super.renderParticle(worldRendererIn, entityIn, partialTicks, p_180434_4_, p_180434_5_, p_180434_6_, p_180434_7_, p_180434_8_);
   }

   public void onUpdate() {
      this.prevPosX = this.posX;
      this.prevPosY = this.posY;
      this.prevPosZ = this.posZ;
      if (this.particleAge++ >= this.particleMaxAge) {
         this.setDead();
      }

      float f = (float)this.particleAge / (float)this.particleMaxAge;
      if (this.rand.nextFloat() > f) {
         this.worldObj.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX, this.posY, this.posZ, this.motionX, this.motionY, this.motionZ);
      }

      this.motionY -= 0.03;
      this.moveEntity(this.motionX, this.motionY, this.motionZ);
      this.motionX *= 0.9990000128746033;
      this.motionY *= 0.9990000128746033;
      this.motionZ *= 0.9990000128746033;
      if (this.onGround) {
         this.motionX *= 0.699999988079071;
         this.motionZ *= 0.699999988079071;
      }

   }

   public static class Factory implements IParticleFactory {
      public EntityFX getEntityFX(int particleID, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, int... p_178902_15_) {
         return new EntityLavaFX(worldIn, xCoordIn, yCoordIn, zCoordIn);
      }
   }
}
