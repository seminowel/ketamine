package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.types;

import com.viaversion.viaversion.api.type.Type;

public class Types1_7_6_10 {
   public static final Type COMPRESSED_NBT = new CompressedNBTType();
   public static final Type ITEM_ARRAY = new ItemArrayType(false);
   public static final Type COMPRESSED_NBT_ITEM_ARRAY = new ItemArrayType(true);
   public static final Type ITEM = new ItemType(false);
   public static final Type COMPRESSED_NBT_ITEM = new ItemType(true);
   public static final Type METADATA_LIST = new MetadataListType();
   public static final Type METADATA = new MetadataType();
   public static final Type NBT = new NBTType();
   public static final Type INT_ARRAY = new IntArrayType();
}
