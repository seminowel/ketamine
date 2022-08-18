package net.minecraft.entity.projectile;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class EntitySnowball extends EntityThrowable {
   public EntitySnowball(World worldIn) {
      super(worldIn);
   }

   public EntitySnowball(World worldIn, EntityLivingBase throwerIn) {
      super(worldIn, throwerIn);
   }

   public EntitySnowball(World worldIn, double x, double y, double z) {
      super(worldIn, x, y, z);
   }

   protected void onImpact(MovingObjectPosition p_70184_1_) {
      int i;
      if (p_70184_1_.entityHit != null) {
         i = 0;
         if (p_70184_1_.entityHit instanceof EntityBlaze) {
            i = 3;
         }

         p_70184_1_.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, this.getThrower()), (float)i);
      }

      for(i = 0; i < 8; ++i) {
         this.worldObj.spawnParticle(EnumParticleTypes.SNOWBALL, this.posX, this.posY, this.posZ, 0.0, 0.0, 0.0);
      }

      if (!this.worldObj.isRemote) {
         this.setDead();
      }

   }
}
