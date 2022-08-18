package net.minecraft.block.state.pattern;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;

public class BlockStateHelper implements Predicate {
   private final BlockState blockstate;
   private final Map propertyPredicates = Maps.newHashMap();

   private BlockStateHelper(BlockState blockStateIn) {
      this.blockstate = blockStateIn;
   }

   public static BlockStateHelper forBlock(Block blockIn) {
      return new BlockStateHelper(blockIn.getBlockState());
   }

   public boolean apply(IBlockState p_apply_1_) {
      if (p_apply_1_ != null && p_apply_1_.getBlock().equals(this.blockstate.getBlock())) {
         Iterator var2 = this.propertyPredicates.entrySet().iterator();

         Map.Entry entry;
         Comparable object;
         do {
            if (!var2.hasNext()) {
               return true;
            }

            entry = (Map.Entry)var2.next();
            object = p_apply_1_.getValue((IProperty)entry.getKey());
         } while(((Predicate)entry.getValue()).apply(object));

         return false;
      } else {
         return false;
      }
   }

   public BlockStateHelper where(IProperty property, Predicate is) {
      if (!this.blockstate.getProperties().contains(property)) {
         throw new IllegalArgumentException(this.blockstate + " cannot support property " + property);
      } else {
         this.propertyPredicates.put(property, is);
         return this;
      }
   }
}
