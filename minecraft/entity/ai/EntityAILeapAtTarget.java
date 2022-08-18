package net.minecraft.entity.ai;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;

public class EntityAILeapAtTarget extends EntityAIBase {
   EntityLiving leaper;
   EntityLivingBase leapTarget;
   float leapMotionY;

   public EntityAILeapAtTarget(EntityLiving leapingEntity, float leapMotionYIn) {
      this.leaper = leapingEntity;
      this.leapMotionY = leapMotionYIn;
      this.setMutexBits(5);
   }

   public boolean shouldExecute() {
      this.leapTarget = this.leaper.getAttackTarget();
      if (this.leapTarget == null) {
         return false;
      } else {
         double d0 = this.leaper.getDistanceSqToEntity(this.leapTarget);
         return d0 >= 4.0 && d0 <= 16.0 ? (!this.leaper.onGround ? false : this.leaper.getRNG().nextInt(5) == 0) : false;
      }
   }

   public boolean continueExecuting() {
      return !this.leaper.onGround;
   }

   public void startExecuting() {
      double d0 = this.leapTarget.posX - this.leaper.posX;
      double d1 = this.leapTarget.posZ - this.leaper.posZ;
      float f = MathHelper.sqrt_double(d0 * d0 + d1 * d1);
      EntityLiving var10000 = this.leaper;
      var10000.motionX += d0 / (double)f * 0.5 * 0.800000011920929 + this.leaper.motionX * 0.20000000298023224;
      var10000 = this.leaper;
      var10000.motionZ += d1 / (double)f * 0.5 * 0.800000011920929 + this.leaper.motionZ * 0.20000000298023224;
      this.leaper.motionY = (double)this.leapMotionY;
   }
}
