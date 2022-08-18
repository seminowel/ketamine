package net.minecraft.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityFX extends Entity {
   protected int particleTextureIndexX;
   protected int particleTextureIndexY;
   protected float particleTextureJitterX;
   protected float particleTextureJitterY;
   protected int particleAge;
   protected int particleMaxAge;
   protected float particleScale;
   protected float particleGravity;
   protected float particleRed;
   protected float particleGreen;
   protected float particleBlue;
   protected float particleAlpha;
   protected TextureAtlasSprite particleIcon;
   public static double interpPosX;
   public static double interpPosY;
   public static double interpPosZ;

   protected EntityFX(World worldIn, double posXIn, double posYIn, double posZIn) {
      super(worldIn);
      this.particleAlpha = 1.0F;
      this.setSize(0.2F, 0.2F);
      this.setPosition(posXIn, posYIn, posZIn);
      this.lastTickPosX = this.prevPosX = posXIn;
      this.lastTickPosY = this.prevPosY = posYIn;
      this.lastTickPosZ = this.prevPosZ = posZIn;
      this.particleRed = this.particleGreen = this.particleBlue = 1.0F;
      this.particleTextureJitterX = this.rand.nextFloat() * 3.0F;
      this.particleTextureJitterY = this.rand.nextFloat() * 3.0F;
      this.particleScale = (this.rand.nextFloat() * 0.5F + 0.5F) * 2.0F;
      this.particleMaxAge = (int)(4.0F / (this.rand.nextFloat() * 0.9F + 0.1F));
      this.particleAge = 0;
   }

   public EntityFX(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn) {
      this(worldIn, xCoordIn, yCoordIn, zCoordIn);
      this.motionX = xSpeedIn + (Math.random() * 2.0 - 1.0) * 0.4000000059604645;
      this.motionY = ySpeedIn + (Math.random() * 2.0 - 1.0) * 0.4000000059604645;
      this.motionZ = zSpeedIn + (Math.random() * 2.0 - 1.0) * 0.4000000059604645;
      float f = (float)(Math.random() + Math.random() + 1.0) * 0.15F;
      float f1 = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
      this.motionX = this.motionX / (double)f1 * (double)f * 0.4000000059604645;
      this.motionY = this.motionY / (double)f1 * (double)f * 0.4000000059604645 + 0.10000000149011612;
      this.motionZ = this.motionZ / (double)f1 * (double)f * 0.4000000059604645;
   }

   public EntityFX multiplyVelocity(float multiplier) {
      this.motionX *= (double)multiplier;
      this.motionY = (this.motionY - 0.10000000149011612) * (double)multiplier + 0.10000000149011612;
      this.motionZ *= (double)multiplier;
      return this;
   }

   public EntityFX multipleParticleScaleBy(float p_70541_1_) {
      this.setSize(0.2F * p_70541_1_, 0.2F * p_70541_1_);
      this.particleScale *= p_70541_1_;
      return this;
   }

   public void setRBGColorF(float particleRedIn, float particleGreenIn, float particleBlueIn) {
      this.particleRed = particleRedIn;
      this.particleGreen = particleGreenIn;
      this.particleBlue = particleBlueIn;
   }

   public void setAlphaF(float alpha) {
      if (this.particleAlpha == 1.0F && alpha < 1.0F) {
         Minecraft.getMinecraft().effectRenderer.moveToAlphaLayer(this);
      } else if (this.particleAlpha < 1.0F && alpha == 1.0F) {
         Minecraft.getMinecraft().effectRenderer.moveToNoAlphaLayer(this);
      }

      this.particleAlpha = alpha;
   }

   public float getRedColorF() {
      return this.particleRed;
   }

   public float getGreenColorF() {
      return this.particleGreen;
   }

   public float getBlueColorF() {
      return this.particleBlue;
   }

   public float getAlpha() {
      return this.particleAlpha;
   }

   protected boolean canTriggerWalking() {
      return false;
   }

   protected void entityInit() {
   }

   public void onUpdate() {
      this.prevPosX = this.posX;
      this.prevPosY = this.posY;
      this.prevPosZ = this.posZ;
      if (this.particleAge++ >= this.particleMaxAge) {
         this.setDead();
      }

      this.motionY -= 0.04 * (double)this.particleGravity;
      this.moveEntity(this.motionX, this.motionY, this.motionZ);
      this.motionX *= 0.9800000190734863;
      this.motionY *= 0.9800000190734863;
      this.motionZ *= 0.9800000190734863;
      if (this.onGround) {
         this.motionX *= 0.699999988079071;
         this.motionZ *= 0.699999988079071;
      }

   }

   public void renderParticle(WorldRenderer worldRendererIn, Entity entityIn, float partialTicks, float p_180434_4_, float p_180434_5_, float p_180434_6_, float p_180434_7_, float p_180434_8_) {
      float f = (float)this.particleTextureIndexX / 16.0F;
      float f1 = f + 0.0624375F;
      float f2 = (float)this.particleTextureIndexY / 16.0F;
      float f3 = f2 + 0.0624375F;
      float f4 = 0.1F * this.particleScale;
      if (this.particleIcon != null) {
         f = this.particleIcon.getMinU();
         f1 = this.particleIcon.getMaxU();
         f2 = this.particleIcon.getMinV();
         f3 = this.particleIcon.getMaxV();
      }

      float f5 = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)partialTicks - interpPosX);
      float f6 = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)partialTicks - interpPosY);
      float f7 = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)partialTicks - interpPosZ);
      int i = this.getBrightnessForRender(partialTicks);
      int j = i >> 16 & '\uffff';
      int k = i & '\uffff';
      worldRendererIn.pos((double)(f5 - p_180434_4_ * f4 - p_180434_7_ * f4), (double)(f6 - p_180434_5_ * f4), (double)(f7 - p_180434_6_ * f4 - p_180434_8_ * f4)).tex((double)f1, (double)f3).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
      worldRendererIn.pos((double)(f5 - p_180434_4_ * f4 + p_180434_7_ * f4), (double)(f6 + p_180434_5_ * f4), (double)(f7 - p_180434_6_ * f4 + p_180434_8_ * f4)).tex((double)f1, (double)f2).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
      worldRendererIn.pos((double)(f5 + p_180434_4_ * f4 + p_180434_7_ * f4), (double)(f6 + p_180434_5_ * f4), (double)(f7 + p_180434_6_ * f4 + p_180434_8_ * f4)).tex((double)f, (double)f2).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
      worldRendererIn.pos((double)(f5 + p_180434_4_ * f4 - p_180434_7_ * f4), (double)(f6 - p_180434_5_ * f4), (double)(f7 + p_180434_6_ * f4 - p_180434_8_ * f4)).tex((double)f, (double)f3).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
   }

   public int getFXLayer() {
      return 0;
   }

   public void writeEntityToNBT(NBTTagCompound tagCompound) {
   }

   public void readEntityFromNBT(NBTTagCompound tagCompund) {
   }

   public void setParticleIcon(TextureAtlasSprite icon) {
      int i = this.getFXLayer();
      if (i == 1) {
         this.particleIcon = icon;
      } else {
         throw new RuntimeException("Invalid call to Particle.setTex, use coordinate methods");
      }
   }

   public void setParticleTextureIndex(int particleTextureIndex) {
      if (this.getFXLayer() != 0) {
         throw new RuntimeException("Invalid call to Particle.setMiscTex");
      } else {
         this.particleTextureIndexX = particleTextureIndex % 16;
         this.particleTextureIndexY = particleTextureIndex / 16;
      }
   }

   public void nextTextureIndexX() {
      ++this.particleTextureIndexX;
   }

   public boolean canAttackWithItem() {
      return false;
   }

   public String toString() {
      return this.getClass().getSimpleName() + ", Pos (" + this.posX + "," + this.posY + "," + this.posZ + "), RGBA (" + this.particleRed + "," + this.particleGreen + "," + this.particleBlue + "," + this.particleAlpha + "), Age " + this.particleAge;
   }
}
