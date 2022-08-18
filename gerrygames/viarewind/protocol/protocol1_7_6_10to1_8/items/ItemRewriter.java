package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.items;

import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.ByteTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.ListTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.NumberTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.ShortTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.StringTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.Tag;
import de.gerrygames.viarewind.utils.ChatUtil;
import de.gerrygames.viarewind.utils.Enchantments;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ItemRewriter {
   public static Item toClient(Item item) {
      if (item == null) {
         return null;
      } else {
         CompoundTag tag = item.tag();
         if (tag == null) {
            item.setTag(tag = new CompoundTag());
         }

         CompoundTag viaVersionTag = new CompoundTag();
         tag.put("ViaRewind1_7_6_10to1_8", viaVersionTag);
         viaVersionTag.put("id", new ShortTag((short)item.identifier()));
         viaVersionTag.put("data", new ShortTag(item.data()));
         CompoundTag display = (CompoundTag)tag.get("display");
         if (display != null && display.contains("Name")) {
            viaVersionTag.put("displayName", new StringTag((String)display.get("Name").getValue()));
         }

         if (display != null && display.contains("Lore")) {
            viaVersionTag.put("lore", new ListTag(((ListTag)display.get("Lore")).getValue()));
         }

         ListTag pages;
         if (tag.contains("ench") || tag.contains("StoredEnchantments")) {
            pages = tag.contains("ench") ? (ListTag)tag.get("ench") : (ListTag)tag.get("StoredEnchantments");
            List lore = new ArrayList();
            Iterator var6 = (new ArrayList(pages.getValue())).iterator();

            while(var6.hasNext()) {
               Tag ench = (Tag)var6.next();
               short id = ((NumberTag)((CompoundTag)ench).get("id")).asShort();
               short lvl = ((NumberTag)((CompoundTag)ench).get("lvl")).asShort();
               if (id == 8) {
                  String s = "§r§7Depth Strider ";
                  pages.remove(ench);
                  s = s + (String)Enchantments.ENCHANTMENTS.getOrDefault(lvl, "enchantment.level." + lvl);
                  lore.add(new StringTag(s));
               }
            }

            if (!lore.isEmpty()) {
               if (display == null) {
                  tag.put("display", display = new CompoundTag());
                  viaVersionTag.put("noDisplay", new ByteTag());
               }

               ListTag loreTag = (ListTag)display.get("Lore");
               if (loreTag == null) {
                  display.put("Lore", loreTag = new ListTag(StringTag.class));
               }

               lore.addAll(loreTag.getValue());
               loreTag.setValue(lore);
            }
         }

         if (item.identifier() == 387 && tag.contains("pages")) {
            pages = (ListTag)tag.get("pages");
            ListTag oldPages = new ListTag(StringTag.class);
            viaVersionTag.put("pages", oldPages);

            for(int i = 0; i < pages.size(); ++i) {
               StringTag page = (StringTag)pages.get(i);
               String value = page.getValue();
               oldPages.add(new StringTag(value));
               value = ChatUtil.jsonToLegacy(value);
               page.setValue(value);
            }
         }

         ReplacementRegistry1_7_6_10to1_8.replace(item);
         if (viaVersionTag.size() == 2 && (Short)viaVersionTag.get("id").getValue() == item.identifier() && (Short)viaVersionTag.get("data").getValue() == item.data()) {
            item.tag().remove("ViaRewind1_7_6_10to1_8");
            if (item.tag().isEmpty()) {
               item.setTag((CompoundTag)null);
            }
         }

         return item;
      }
   }

   public static Item toServer(Item item) {
      if (item == null) {
         return null;
      } else {
         CompoundTag tag = item.tag();
         if (tag != null && item.tag().contains("ViaRewind1_7_6_10to1_8")) {
            CompoundTag viaVersionTag = (CompoundTag)tag.remove("ViaRewind1_7_6_10to1_8");
            item.setIdentifier((Short)viaVersionTag.get("id").getValue());
            item.setData((Short)viaVersionTag.get("data").getValue());
            if (viaVersionTag.contains("noDisplay")) {
               tag.remove("display");
            }

            if (viaVersionTag.contains("displayName")) {
               CompoundTag display = (CompoundTag)tag.get("display");
               if (display == null) {
                  tag.put("display", display = new CompoundTag());
               }

               StringTag name = (StringTag)display.get("Name");
               if (name == null) {
                  display.put("Name", new StringTag((String)viaVersionTag.get("displayName").getValue()));
               } else {
                  name.setValue((String)viaVersionTag.get("displayName").getValue());
               }
            } else if (tag.contains("display")) {
               ((CompoundTag)tag.get("display")).remove("Name");
            }

            if (item.identifier() == 387) {
               ListTag oldPages = (ListTag)viaVersionTag.get("pages");
               tag.remove("pages");
               tag.put("pages", oldPages);
            }

            return item;
         } else {
            return item;
         }
      }
   }
}
