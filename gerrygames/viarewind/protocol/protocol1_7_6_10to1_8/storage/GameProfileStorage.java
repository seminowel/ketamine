package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.storage;

import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.item.DataItem;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.ListTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.StringTag;
import com.viaversion.viaversion.util.ChatColorUtil;
import de.gerrygames.viarewind.utils.ChatUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GameProfileStorage extends StoredObject {
   private Map properties = new HashMap();

   public GameProfileStorage(UserConnection user) {
      super(user);
   }

   public GameProfile put(UUID uuid, String name) {
      GameProfile gameProfile = new GameProfile(uuid, name);
      this.properties.put(uuid, gameProfile);
      return gameProfile;
   }

   public void putProperty(UUID uuid, Property property) {
      ((GameProfile)this.properties.computeIfAbsent(uuid, (profile) -> {
         return new GameProfile(uuid, (String)null);
      })).properties.add(property);
   }

   public void putProperty(UUID uuid, String name, String value, String signature) {
      this.putProperty(uuid, new Property(name, value, signature));
   }

   public GameProfile get(UUID uuid) {
      return (GameProfile)this.properties.get(uuid);
   }

   public GameProfile get(String name, boolean ignoreCase) {
      if (ignoreCase) {
         name = name.toLowerCase();
      }

      Iterator var3 = this.properties.values().iterator();

      while(var3.hasNext()) {
         GameProfile profile = (GameProfile)var3.next();
         if (profile.name != null) {
            String n = ignoreCase ? profile.name.toLowerCase() : profile.name;
            if (n.equals(name)) {
               return profile;
            }
         }
      }

      return null;
   }

   public List getAllWithPrefix(String prefix, boolean ignoreCase) {
      if (ignoreCase) {
         prefix = prefix.toLowerCase();
      }

      ArrayList profiles = new ArrayList();
      Iterator var4 = this.properties.values().iterator();

      while(var4.hasNext()) {
         GameProfile profile = (GameProfile)var4.next();
         if (profile.name != null) {
            String n = ignoreCase ? profile.name.toLowerCase() : profile.name;
            if (n.startsWith(prefix)) {
               profiles.add(profile);
            }
         }
      }

      return profiles;
   }

   public GameProfile remove(UUID uuid) {
      return (GameProfile)this.properties.remove(uuid);
   }

   public static class Property {
      public String name;
      public String value;
      public String signature;

      public Property(String name, String value, String signature) {
         this.name = name;
         this.value = value;
         this.signature = signature;
      }
   }

   public static class GameProfile {
      public String name;
      public String displayName;
      public int ping;
      public UUID uuid;
      public List properties = new ArrayList();
      public int gamemode = 0;

      public GameProfile(UUID uuid, String name) {
         this.name = name;
         this.uuid = uuid;
      }

      public Item getSkull() {
         CompoundTag tag = new CompoundTag();
         CompoundTag ownerTag = new CompoundTag();
         tag.put("SkullOwner", ownerTag);
         ownerTag.put("Id", new StringTag(this.uuid.toString()));
         CompoundTag properties = new CompoundTag();
         ownerTag.put("Properties", properties);
         ListTag textures = new ListTag(CompoundTag.class);
         properties.put("textures", textures);
         Iterator var5 = this.properties.iterator();

         while(var5.hasNext()) {
            Property property = (Property)var5.next();
            if (property.name.equals("textures")) {
               CompoundTag textureTag = new CompoundTag();
               textureTag.put("Value", new StringTag(property.value));
               if (property.signature != null) {
                  textureTag.put("Signature", new StringTag(property.signature));
               }

               textures.add(textureTag);
            }
         }

         return new DataItem(397, (byte)1, (short)3, tag);
      }

      public String getDisplayName() {
         String displayName = this.displayName == null ? this.name : this.displayName;
         if (displayName.length() > 16) {
            displayName = ChatUtil.removeUnusedColor(displayName, 'f');
         }

         if (displayName.length() > 16) {
            displayName = ChatColorUtil.stripColor(displayName);
         }

         if (displayName.length() > 16) {
            displayName = displayName.substring(0, 16);
         }

         return displayName;
      }

      public void setDisplayName(String displayName) {
         this.displayName = displayName;
      }
   }
}
