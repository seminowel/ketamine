package net.minecraft.world.gen.structure;

import java.util.Iterator;
import java.util.Map;
import net.minecraft.util.MathHelper;

public class MapGenMineshaft extends MapGenStructure {
   private double field_82673_e = 0.004;

   public MapGenMineshaft() {
   }

   public String getStructureName() {
      return "Mineshaft";
   }

   public MapGenMineshaft(Map p_i2034_1_) {
      Iterator var2 = p_i2034_1_.entrySet().iterator();

      while(var2.hasNext()) {
         Map.Entry entry = (Map.Entry)var2.next();
         if (((String)entry.getKey()).equals("chance")) {
            this.field_82673_e = MathHelper.parseDoubleWithDefault((String)entry.getValue(), this.field_82673_e);
         }
      }

   }

   protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ) {
      return this.rand.nextDouble() < this.field_82673_e && this.rand.nextInt(80) < Math.max(Math.abs(chunkX), Math.abs(chunkZ));
   }

   protected StructureStart getStructureStart(int chunkX, int chunkZ) {
      return new StructureMineshaftStart(this.worldObj, this.rand, chunkX, chunkZ);
   }
}
