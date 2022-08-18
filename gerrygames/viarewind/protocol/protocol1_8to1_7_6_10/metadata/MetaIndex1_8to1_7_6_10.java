package de.gerrygames.viarewind.protocol.protocol1_8to1_7_6_10.metadata;

import com.viaversion.viaversion.api.minecraft.entities.Entity1_10Types;
import com.viaversion.viaversion.api.minecraft.entities.Entity1_10Types.EntityType;
import com.viaversion.viaversion.api.minecraft.metadata.types.MetaType1_8;
import com.viaversion.viaversion.util.Pair;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.types.MetaType1_7_6_10;
import java.util.HashMap;
import java.util.Optional;

public enum MetaIndex1_8to1_7_6_10 {
   ENTITY_FLAGS(EntityType.ENTITY, 0, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
   ENTITY_AIR(EntityType.ENTITY, 1, MetaType1_7_6_10.Short, MetaType1_8.Short),
   ENTITY_NAME_TAG(EntityType.ENTITY, -1, MetaType1_7_6_10.NonExistent, 2, MetaType1_8.String),
   ENTITY_NAME_TAG_VISIBILITY(EntityType.ENTITY, -1, MetaType1_7_6_10.NonExistent, 3, MetaType1_8.Byte),
   ENTITY_SILENT(EntityType.ENTITY, -1, MetaType1_7_6_10.NonExistent, 4, MetaType1_8.Byte),
   ENTITY_LIVING_HEALTH(EntityType.ENTITY_LIVING, 6, MetaType1_7_6_10.Float, MetaType1_8.Float),
   ENTITY_LIVING_POTION_EFFECT_COLOR(EntityType.ENTITY_LIVING, 7, MetaType1_7_6_10.Int, MetaType1_8.Int),
   ENTITY_LIVING_IS_POTION_EFFECT_AMBIENT(EntityType.ENTITY_LIVING, 8, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
   ENTITY_LIVING_ARROWS(EntityType.ENTITY_LIVING, 9, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
   ENTITY_LIVING_NAME_TAG(EntityType.ENTITY_LIVING, 10, MetaType1_7_6_10.String, 2, MetaType1_8.String),
   ENTITY_LIVING_NAME_TAG_VISIBILITY(EntityType.ENTITY_LIVING, 11, MetaType1_7_6_10.Byte, 3, MetaType1_8.Byte),
   ENTITY_LIVING_AI(EntityType.ENTITY_LIVING, -1, MetaType1_7_6_10.NonExistent, 15, MetaType1_8.Byte),
   ENTITY_AGEABLE_AGE(EntityType.ENTITY_AGEABLE, 12, MetaType1_7_6_10.Int, MetaType1_8.Byte),
   ARMOR_STAND_FLAGS(EntityType.ARMOR_STAND, -1, MetaType1_7_6_10.NonExistent, 10, MetaType1_8.Byte),
   ARMOR_STAND_HEAD_POSITION(EntityType.ARMOR_STAND, -1, MetaType1_7_6_10.NonExistent, 11, MetaType1_8.Rotation),
   ARMOR_STAND_BODY_POSITION(EntityType.ARMOR_STAND, -1, MetaType1_7_6_10.NonExistent, 12, MetaType1_8.Rotation),
   ARMOR_STAND_LEFT_ARM_POSITION(EntityType.ARMOR_STAND, -1, MetaType1_7_6_10.NonExistent, 13, MetaType1_8.Rotation),
   ARMOR_STAND_RIGHT_ARM_POSITION(EntityType.ARMOR_STAND, -1, MetaType1_7_6_10.NonExistent, 14, MetaType1_8.Rotation),
   ARMOR_STAND_LEFT_LEG_POSITION(EntityType.ARMOR_STAND, -1, MetaType1_7_6_10.NonExistent, 15, MetaType1_8.Rotation),
   ARMOR_STAND_RIGHT_LEG_POSITION(EntityType.ARMOR_STAND, -1, MetaType1_7_6_10.NonExistent, 16, MetaType1_8.Rotation),
   HUMAN_SKIN_FLAGS(EntityType.ENTITY_HUMAN, 16, MetaType1_7_6_10.Byte, 10, MetaType1_8.Byte),
   HUMAN_UNUSED(EntityType.ENTITY_HUMAN, -1, MetaType1_7_6_10.NonExistent, 16, MetaType1_8.Byte),
   HUMAN_ABSORPTION_HEATS(EntityType.ENTITY_HUMAN, 17, MetaType1_7_6_10.Float, MetaType1_8.Float),
   HUMAN_SCORE(EntityType.ENTITY_HUMAN, 18, MetaType1_7_6_10.Int, MetaType1_8.Int),
   HORSE_FLAGS(EntityType.HORSE, 16, MetaType1_7_6_10.Int, MetaType1_8.Int),
   HORSE_TYPE(EntityType.HORSE, 19, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
   HORSE_COLOR(EntityType.HORSE, 20, MetaType1_7_6_10.Int, MetaType1_8.Int),
   HORSE_OWNER(EntityType.HORSE, 21, MetaType1_7_6_10.String, MetaType1_8.String),
   HORSE_ARMOR(EntityType.HORSE, 22, MetaType1_7_6_10.Int, MetaType1_8.Int),
   BAT_HANGING(EntityType.BAT, 16, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
   TAMEABLE_FLAGS(EntityType.ENTITY_TAMEABLE_ANIMAL, 16, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
   TAMEABLE_OWNER(EntityType.ENTITY_TAMEABLE_ANIMAL, 17, MetaType1_7_6_10.String, MetaType1_8.String),
   OCELOT_TYPE(EntityType.OCELOT, 18, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
   WOLF_FLAGS(EntityType.WOLF, 16, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
   WOLF_HEALTH(EntityType.WOLF, 18, MetaType1_7_6_10.Float, MetaType1_8.Float),
   WOLF_BEGGING(EntityType.WOLF, 19, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
   WOLF_COLLAR_COLOR(EntityType.WOLF, 20, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
   PIG_SADDLE(EntityType.PIG, 16, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
   SHEEP_COLOR_OR_SHEARED(EntityType.SHEEP, 16, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
   VILLAGER_TYPE(EntityType.VILLAGER, 16, MetaType1_7_6_10.Int, MetaType1_8.Int),
   ENDERMAN_CARRIED_BLOCK(EntityType.ENDERMAN, 16, MetaType1_7_6_10.NonExistent, MetaType1_8.Short),
   ENDERMAN_CARRIED_BLOCK_DATA(EntityType.ENDERMAN, 17, MetaType1_7_6_10.NonExistent, MetaType1_8.Byte),
   ENDERMAN_IS_SCREAMING(EntityType.ENDERMAN, 18, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
   ZOMBIE_CHILD(EntityType.ZOMBIE, 12, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
   ZOMBIE_VILLAGER(EntityType.ZOMBIE, 13, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
   ZOMBIE_CONVERTING(EntityType.ZOMBIE, 14, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
   BLAZE_ON_FIRE(EntityType.BLAZE, 16, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
   SPIDER_CLIMBING(EntityType.SPIDER, 16, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
   CREEPER_STATE(EntityType.CREEPER, 16, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
   CREEPER_POWERED(EntityType.CREEPER, 17, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
   GHAST_STATE(EntityType.GHAST, 16, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
   GHAST_IS_POWERED(EntityType.GHAST, 17, MetaType1_7_6_10.NonExistent, MetaType1_8.Byte),
   SLIME_SIZE(EntityType.SLIME, 16, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
   SKELETON_TYPE(EntityType.SKELETON, 13, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
   WITCH_AGRESSIVE(EntityType.WITCH, 21, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
   IRON_GOLEM_IS_PLAYER_CREATED(EntityType.IRON_GOLEM, 16, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
   WITHER_WATCHED_TAGRET_1(EntityType.WITHER, 17, MetaType1_7_6_10.Int, MetaType1_8.Int),
   WITHER_WATCHED_TAGRET_2(EntityType.WITHER, 18, MetaType1_7_6_10.Int, MetaType1_8.Int),
   WITHER_WATCHED_TAGRET_3(EntityType.WITHER, 19, MetaType1_7_6_10.Int, MetaType1_8.Int),
   WITHER_INVULNERABLE_TIME(EntityType.WITHER, 20, MetaType1_7_6_10.Int, MetaType1_8.Int),
   GUARDIAN_FLAGS(EntityType.GUARDIAN, 16, MetaType1_7_6_10.NonExistent, MetaType1_8.Byte),
   GUARDIAN_TARGET(EntityType.GUARDIAN, 17, MetaType1_7_6_10.NonExistent, MetaType1_8.Int),
   BOAT_TIME_SINCE_HIT(EntityType.BOAT, 17, MetaType1_7_6_10.Int, MetaType1_8.Int),
   BOAT_FORWARD_DIRECTION(EntityType.BOAT, 18, MetaType1_7_6_10.Int, MetaType1_8.Int),
   BOAT_DAMAGE_TAKEN(EntityType.BOAT, 19, MetaType1_7_6_10.Float, MetaType1_8.Float),
   MINECART_SHAKING_POWER(EntityType.MINECART_ABSTRACT, 17, MetaType1_7_6_10.Int, MetaType1_8.Int),
   MINECART_SHAKING_DIRECTION(EntityType.MINECART_ABSTRACT, 18, MetaType1_7_6_10.Int, MetaType1_8.Int),
   MINECART_DAMAGE_TAKEN(EntityType.MINECART_ABSTRACT, 19, MetaType1_7_6_10.Float, MetaType1_8.Float),
   MINECART_BLOCK_INSIDE(EntityType.MINECART_ABSTRACT, 20, MetaType1_7_6_10.Int, MetaType1_8.Int),
   MINECART_BLOCK_Y(EntityType.MINECART_ABSTRACT, 21, MetaType1_7_6_10.Int, MetaType1_8.Int),
   MINECART_SHOW_BLOCK(EntityType.MINECART_ABSTRACT, 22, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
   FURNACE_MINECART_IS_POWERED(EntityType.MINECART_FURNACE, 16, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
   ITEM_ITEM(EntityType.DROPPED_ITEM, 10, MetaType1_7_6_10.Slot, MetaType1_8.Slot),
   ARROW_IS_CRITICAL(EntityType.ARROW, 16, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
   FIREWORK_INFO(EntityType.FIREWORK, 8, MetaType1_7_6_10.Slot, MetaType1_8.Slot),
   ITEM_FRAME_ITEM(EntityType.ITEM_FRAME, 2, MetaType1_7_6_10.Slot, 8, MetaType1_8.Slot),
   ITEM_FRAME_ROTATION(EntityType.ITEM_FRAME, 3, MetaType1_7_6_10.Byte, 9, MetaType1_8.Byte),
   ENDER_CRYSTAL_HEALTH(EntityType.ENDER_CRYSTAL, 8, MetaType1_7_6_10.Int, 9, MetaType1_8.Int);

   private static final HashMap metadataRewrites = new HashMap();
   private Entity1_10Types.EntityType clazz;
   private int newIndex;
   private MetaType1_8 newType;
   private MetaType1_7_6_10 oldType;
   private int index;

   private MetaIndex1_8to1_7_6_10(Entity1_10Types.EntityType type, int index, MetaType1_7_6_10 oldType, MetaType1_8 newType) {
      this.clazz = type;
      this.index = index;
      this.newIndex = index;
      this.oldType = oldType;
      this.newType = newType;
   }

   private MetaIndex1_8to1_7_6_10(Entity1_10Types.EntityType type, int index, MetaType1_7_6_10 oldType, int newIndex, MetaType1_8 newType) {
      this.clazz = type;
      this.index = index;
      this.oldType = oldType;
      this.newIndex = newIndex;
      this.newType = newType;
   }

   private static Optional getIndex(Entity1_10Types.EntityType type, int index) {
      Pair pair = new Pair(type, index);
      return metadataRewrites.containsKey(pair) ? Optional.of((MetaIndex1_8to1_7_6_10)metadataRewrites.get(pair)) : Optional.empty();
   }

   public Entity1_10Types.EntityType getClazz() {
      return this.clazz;
   }

   public int getNewIndex() {
      return this.newIndex;
   }

   public MetaType1_8 getNewType() {
      return this.newType;
   }

   public MetaType1_7_6_10 getOldType() {
      return this.oldType;
   }

   public int getIndex() {
      return this.index;
   }

   public static MetaIndex1_8to1_7_6_10 searchIndex(Entity1_10Types.EntityType type, int index) {
      Entity1_10Types.EntityType currentType = type;

      do {
         Optional optMeta = getIndex(currentType, index);
         if (optMeta.isPresent()) {
            return (MetaIndex1_8to1_7_6_10)optMeta.get();
         }

         currentType = currentType.getParent();
      } while(currentType != null);

      return null;
   }

   static {
      MetaIndex1_8to1_7_6_10[] var0 = values();
      int var1 = var0.length;

      for(int var2 = 0; var2 < var1; ++var2) {
         MetaIndex1_8to1_7_6_10 index = var0[var2];
         metadataRewrites.put(new Pair(index.getClazz(), index.getIndex()), index);
      }

   }
}
