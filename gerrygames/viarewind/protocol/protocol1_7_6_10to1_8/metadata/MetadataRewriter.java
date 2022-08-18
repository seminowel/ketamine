package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.metadata;

import com.viaversion.viaversion.api.minecraft.entities.Entity1_10Types;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.minecraft.metadata.types.MetaType1_8;
import de.gerrygames.viarewind.ViaRewind;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.items.ItemRewriter;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.types.MetaType1_7_6_10;
import de.gerrygames.viarewind.protocol.protocol1_8to1_7_6_10.metadata.MetaIndex1_8to1_7_6_10;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MetadataRewriter {
   public static void transform(Entity1_10Types.EntityType type, List list) {
      Iterator var2 = (new ArrayList(list)).iterator();

      while(var2.hasNext()) {
         Metadata entry = (Metadata)var2.next();
         MetaIndex1_8to1_7_6_10 metaIndex = MetaIndex1_7_6_10to1_8.searchIndex(type, entry.id());

         try {
            if (metaIndex == null) {
               throw new Exception("Could not find valid metadata");
            }

            if (metaIndex.getOldType() == MetaType1_7_6_10.NonExistent) {
               list.remove(entry);
            } else {
               Object value = entry.getValue();
               if (!metaIndex.getNewType().type().getOutputClass().isAssignableFrom(value.getClass())) {
                  list.remove(entry);
               } else {
                  entry.setMetaTypeUnsafe(metaIndex.getOldType());
                  entry.setId(metaIndex.getIndex());
                  switch (metaIndex.getOldType()) {
                     case Int:
                        if (metaIndex.getNewType() == MetaType1_8.Byte) {
                           entry.setValue(((Byte)value).intValue());
                           if (metaIndex == MetaIndex1_8to1_7_6_10.ENTITY_AGEABLE_AGE && (Integer)entry.getValue() < 0) {
                              entry.setValue(-25000);
                           }
                        }

                        if (metaIndex.getNewType() == MetaType1_8.Short) {
                           entry.setValue(((Short)value).intValue());
                        }

                        if (metaIndex.getNewType() == MetaType1_8.Int) {
                           entry.setValue(value);
                        }
                        break;
                     case Byte:
                        if (metaIndex.getNewType() == MetaType1_8.Int) {
                           entry.setValue(((Integer)value).byteValue());
                        }

                        if (metaIndex.getNewType() == MetaType1_8.Byte) {
                           if (metaIndex == MetaIndex1_8to1_7_6_10.ITEM_FRAME_ROTATION) {
                              value = Integer.valueOf((Byte)value / 2).byteValue();
                           }

                           entry.setValue(value);
                        }

                        if (metaIndex == MetaIndex1_8to1_7_6_10.HUMAN_SKIN_FLAGS) {
                           byte flags = (Byte)value;
                           boolean cape = (flags & 1) != 0;
                           flags = (byte)(cape ? 0 : 2);
                           entry.setValue(flags);
                        }
                        break;
                     case Slot:
                        entry.setValue(ItemRewriter.toClient((Item)value));
                        break;
                     case Float:
                        entry.setValue(value);
                        break;
                     case Short:
                        entry.setValue(value);
                        break;
                     case String:
                        entry.setValue(value);
                        break;
                     case Position:
                        entry.setValue(value);
                        break;
                     default:
                        ViaRewind.getPlatform().getLogger().warning("[Out] Unhandled MetaDataType: " + metaIndex.getNewType());
                        list.remove(entry);
                  }
               }
            }
         } catch (Exception var8) {
            list.remove(entry);
         }
      }

   }
}
