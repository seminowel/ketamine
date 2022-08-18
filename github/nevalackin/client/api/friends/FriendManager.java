package io.github.nevalackin.client.api.friends;

import java.util.Iterator;
import net.minecraft.util.StringUtils;

public class FriendManager extends Manager {
   public void addFriend(String name, String alias) {
      this.getContents().add(new Friend(name, alias));
   }

   public void deleteFriend(String name) {
      Iterator var2 = this.getContents().iterator();

      while(var2.hasNext()) {
         Friend friend = (Friend)var2.next();
         if (friend.getName().equalsIgnoreCase(StringUtils.stripControlCodes(name)) || friend.getAlias().equalsIgnoreCase(StringUtils.stripControlCodes(name))) {
            this.getContents().remove(friend);
            break;
         }
      }

   }

   public Friend getFriend(String name) {
      Iterator var2 = this.getContents().iterator();

      Friend friend;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         friend = (Friend)var2.next();
      } while(!friend.getName().equalsIgnoreCase(StringUtils.stripControlCodes(name)) && !friend.getAlias().equalsIgnoreCase(StringUtils.stripControlCodes(name)));

      return friend;
   }

   public boolean isFriend(String name) {
      Iterator var2 = this.getContents().iterator();

      Friend friend;
      do {
         if (!var2.hasNext()) {
            return false;
         }

         friend = (Friend)var2.next();
      } while(!friend.getName().equalsIgnoreCase(StringUtils.stripControlCodes(name)) && !friend.getAlias().equalsIgnoreCase(StringUtils.stripControlCodes(name)));

      return true;
   }
}
