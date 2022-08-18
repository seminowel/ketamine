package net.minecraft.entity.ai;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EntitySelectors;

public class EntityAINearestAttackableTarget extends EntityAITarget {
   protected final Class targetClass;
   private final int targetChance;
   protected final Sorter theNearestAttackableTargetSorter;
   protected Predicate targetEntitySelector;
   protected EntityLivingBase targetEntity;

   public EntityAINearestAttackableTarget(EntityCreature creature, Class classTarget, boolean checkSight) {
      this(creature, classTarget, checkSight, false);
   }

   public EntityAINearestAttackableTarget(EntityCreature creature, Class classTarget, boolean checkSight, boolean onlyNearby) {
      this(creature, classTarget, 10, checkSight, onlyNearby, (Predicate)null);
   }

   public EntityAINearestAttackableTarget(EntityCreature creature, Class classTarget, int chance, boolean checkSight, boolean onlyNearby, final Predicate targetSelector) {
      super(creature, checkSight, onlyNearby);
      this.targetClass = classTarget;
      this.targetChance = chance;
      this.theNearestAttackableTargetSorter = new Sorter(creature);
      this.setMutexBits(1);
      this.targetEntitySelector = new Predicate() {
         public boolean apply(EntityLivingBase p_apply_1_) {
            if (targetSelector != null && !targetSelector.apply(p_apply_1_)) {
               return false;
            } else {
               if (p_apply_1_ instanceof EntityPlayer) {
                  double d0 = EntityAINearestAttackableTarget.this.getTargetDistance();
                  if (p_apply_1_.isSneaking()) {
                     d0 *= 0.800000011920929;
                  }

                  if (p_apply_1_.isInvisible()) {
                     float f = ((EntityPlayer)p_apply_1_).getArmorVisibility();
                     if (f < 0.1F) {
                        f = 0.1F;
                     }

                     d0 *= (double)(0.7F * f);
                  }

                  if ((double)p_apply_1_.getDistanceToEntity(EntityAINearestAttackableTarget.this.taskOwner) > d0) {
                     return false;
                  }
               }

               return EntityAINearestAttackableTarget.this.isSuitableTarget(p_apply_1_, false);
            }
         }
      };
   }

   public boolean shouldExecute() {
      if (this.targetChance > 0 && this.taskOwner.getRNG().nextInt(this.targetChance) != 0) {
         return false;
      } else {
         double d0 = this.getTargetDistance();
         List list = this.taskOwner.worldObj.getEntitiesWithinAABB(this.targetClass, this.taskOwner.getEntityBoundingBox().expand(d0, 4.0, d0), Predicates.and(this.targetEntitySelector, EntitySelectors.NOT_SPECTATING));
         Collections.sort(list, this.theNearestAttackableTargetSorter);
         if (list.isEmpty()) {
            return false;
         } else {
            this.targetEntity = (EntityLivingBase)list.get(0);
            return true;
         }
      }
   }

   public void startExecuting() {
      this.taskOwner.setAttackTarget(this.targetEntity);
      super.startExecuting();
   }

   public static class Sorter implements Comparator {
      private final Entity theEntity;

      public Sorter(Entity theEntityIn) {
         this.theEntity = theEntityIn;
      }

      public int compare(Entity p_compare_1_, Entity p_compare_2_) {
         double d0 = this.theEntity.getDistanceSqToEntity(p_compare_1_);
         double d1 = this.theEntity.getDistanceSqToEntity(p_compare_2_);
         return d0 < d1 ? -1 : (d0 > d1 ? 1 : 0);
      }
   }
}
