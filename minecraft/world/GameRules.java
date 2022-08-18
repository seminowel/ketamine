package net.minecraft.world;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import net.minecraft.nbt.NBTTagCompound;

public class GameRules {
   private TreeMap theGameRules = new TreeMap();

   public GameRules() {
      this.addGameRule("doFireTick", "true", GameRules.ValueType.BOOLEAN_VALUE);
      this.addGameRule("mobGriefing", "true", GameRules.ValueType.BOOLEAN_VALUE);
      this.addGameRule("keepInventory", "false", GameRules.ValueType.BOOLEAN_VALUE);
      this.addGameRule("doMobSpawning", "true", GameRules.ValueType.BOOLEAN_VALUE);
      this.addGameRule("doMobLoot", "true", GameRules.ValueType.BOOLEAN_VALUE);
      this.addGameRule("doTileDrops", "true", GameRules.ValueType.BOOLEAN_VALUE);
      this.addGameRule("doEntityDrops", "true", GameRules.ValueType.BOOLEAN_VALUE);
      this.addGameRule("commandBlockOutput", "true", GameRules.ValueType.BOOLEAN_VALUE);
      this.addGameRule("naturalRegeneration", "true", GameRules.ValueType.BOOLEAN_VALUE);
      this.addGameRule("doDaylightCycle", "true", GameRules.ValueType.BOOLEAN_VALUE);
      this.addGameRule("logAdminCommands", "true", GameRules.ValueType.BOOLEAN_VALUE);
      this.addGameRule("showDeathMessages", "true", GameRules.ValueType.BOOLEAN_VALUE);
      this.addGameRule("randomTickSpeed", "3", GameRules.ValueType.NUMERICAL_VALUE);
      this.addGameRule("sendCommandFeedback", "true", GameRules.ValueType.BOOLEAN_VALUE);
      this.addGameRule("reducedDebugInfo", "false", GameRules.ValueType.BOOLEAN_VALUE);
   }

   public void addGameRule(String key, String value, ValueType type) {
      this.theGameRules.put(key, new Value(value, type));
   }

   public void setOrCreateGameRule(String key, String ruleValue) {
      Value gamerules$value = (Value)this.theGameRules.get(key);
      if (gamerules$value != null) {
         gamerules$value.setValue(ruleValue);
      } else {
         this.addGameRule(key, ruleValue, GameRules.ValueType.ANY_VALUE);
      }

   }

   public String getString(String name) {
      Value gamerules$value = (Value)this.theGameRules.get(name);
      return gamerules$value != null ? gamerules$value.getString() : "";
   }

   public boolean getBoolean(String name) {
      Value gamerules$value = (Value)this.theGameRules.get(name);
      return gamerules$value != null ? gamerules$value.getBoolean() : false;
   }

   public int getInt(String name) {
      Value gamerules$value = (Value)this.theGameRules.get(name);
      return gamerules$value != null ? gamerules$value.getInt() : 0;
   }

   public NBTTagCompound writeToNBT() {
      NBTTagCompound nbttagcompound = new NBTTagCompound();
      Iterator var2 = this.theGameRules.keySet().iterator();

      while(var2.hasNext()) {
         String s = (String)var2.next();
         Value gamerules$value = (Value)this.theGameRules.get(s);
         nbttagcompound.setString(s, gamerules$value.getString());
      }

      return nbttagcompound;
   }

   public void readFromNBT(NBTTagCompound nbt) {
      Iterator var2 = nbt.getKeySet().iterator();

      while(var2.hasNext()) {
         String s = (String)var2.next();
         String s1 = nbt.getString(s);
         this.setOrCreateGameRule(s, s1);
      }

   }

   public String[] getRules() {
      Set set = this.theGameRules.keySet();
      return (String[])((String[])set.toArray(new String[set.size()]));
   }

   public boolean hasRule(String name) {
      return this.theGameRules.containsKey(name);
   }

   public boolean areSameType(String key, ValueType otherValue) {
      Value gamerules$value = (Value)this.theGameRules.get(key);
      return gamerules$value != null && (gamerules$value.getType() == otherValue || otherValue == GameRules.ValueType.ANY_VALUE);
   }

   public static enum ValueType {
      ANY_VALUE,
      BOOLEAN_VALUE,
      NUMERICAL_VALUE;
   }

   static class Value {
      private String valueString;
      private boolean valueBoolean;
      private int valueInteger;
      private double valueDouble;
      private final ValueType type;

      public Value(String value, ValueType type) {
         this.type = type;
         this.setValue(value);
      }

      public void setValue(String value) {
         this.valueString = value;
         this.valueBoolean = Boolean.parseBoolean(value);
         this.valueInteger = this.valueBoolean ? 1 : 0;

         try {
            this.valueInteger = Integer.parseInt(value);
         } catch (NumberFormatException var4) {
         }

         try {
            this.valueDouble = Double.parseDouble(value);
         } catch (NumberFormatException var3) {
         }

      }

      public String getString() {
         return this.valueString;
      }

      public boolean getBoolean() {
         return this.valueBoolean;
      }

      public int getInt() {
         return this.valueInteger;
      }

      public ValueType getType() {
         return this.type;
      }
   }
}
