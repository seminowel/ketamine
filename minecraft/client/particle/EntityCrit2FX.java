package net.minecraft.client.particle;

import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityCrit2FX extends EntityFX {
   float field_174839_a;

   protected EntityCrit2FX(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double p_i46284_8_, double p_i46284_10_, double p_i46284_12_) {
      this(worldIn, xCoordIn, yCoordIn, zCoordIn, p_i46284_8_, p_i46284_10_, p_i46284_12_, 1.0F);
   }

   protected EntityCrit2FX(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double p_i46285_8_, double p_i46285_10_, double p_i46285_12_, float p_i46285_14_) {
      super(worldIn, xCoordIn, yCoordIn, zCoordIn, 0.0, 0.0, 0.0);
      this.motionX *= 0.10000000149011612;
      this.motionY *= 0.10000000149011612;
      this.motionZ *= 0.10000000149011612;
      this.motionX += p_i46285_8_ * 0.4;
      this.motionY += p_i46285_10_ * 0.4;
      this.motionZ += p_i46285_12_ * 0.4;
      this.particleRed = this.particleGreen = this.particleBlue = (float)(Math.random() * 0.30000001192092896 + 0.6000000238418579);
      this.particleScale *= 0.75F;
      this.particleScale *= p_i46285_14_;
      this.field_174839_a = this.particleScale;
      this.particleMaxAge = (int)(6.0 / (Math.random() * 0.8 + 0.6));
      this.particleMaxAge = (int)((float)this.particleMaxAge * p_i46285_14_);
      this.noClip = false;
      this.setParticleTextureIndex(65);
      this.onUpdate();
   }

   public void renderParticle(WorldRenderer worldRendererIn, Entity entityIn, float partialTicks, float p_180434_4_, float p_180434_5_, float p_180434_6_, float p_180434_7_, float p_180434_8_) {
      float f = ((float)this.particleAge + partialTicks) / (float)this.particleMaxAge * 32.0F;
      f = MathHelper.clamp_float(f, 0.0F, 1.0F);
      this.particleScale = this.field_174839_a * f;
      super.renderParticle(worldRendererIn, entityIn, partialTicks, p_180434_4_, p_180434_5_, p_180434_6_, p_180434_7_, p_180434_8_);
   }

   public void onUpdate() {
      this.prevPosX = this.posX;
      this.prevPosY = this.posY;
      this.prevPosZ = this.posZ;
      if (this.particleAge++ >= this.particleMaxAge) {
         this.setDead();
      }

      this.moveEntity(this.motionX, this.motionY, this.motionZ);
      this.particleGreen = (float)((double)this.particleGreen * 0.96);
      this.particleBlue = (float)((double)this.particleBlue * 0.9);
      this.motionX *= 0.699999988079071;
      this.motionY *= 0.699999988079071;
      this.motionZ *= 0.699999988079071;
      this.motionY -= 0.019999999552965164;
      if (this.onGround) {
         this.motionX *= 0.699999988079071;
         this.motionZ *= 0.699999988079071;
      }

   }

   public static class MagicFactory implements IParticleFactory {
      public EntityFX getEntityFX(int particleID, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, int... p_178902_15_) {
         EntityFX entityfx = new EntityCrit2FX(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
         entityfx.setRBGColorF(entityfx.getRedColorF() * 0.3F, entityfx.getGreenColorF() * 0.8F, entityfx.getBlueColorF());
         entityfx.nextTextureIndexX();
         return entityfx;
      }
   }

   public static class Factory implements IParticleFactory {
      public EntityFX getEntityFX(int particleID, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, int... p_178902_15_) {
         return new EntityCrit2FX(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
      }
   }
}
