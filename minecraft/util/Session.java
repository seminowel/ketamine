package net.minecraft.util;

import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.util.UUIDTypeAdapter;
import java.util.Map;
import java.util.UUID;

public class Session {
   private final String username;
   private final String playerID;
   private final String token;
   private final Type sessionType;

   public Session(String usernameIn, String playerIDIn, String tokenIn, String sessionTypeIn) {
      this.username = usernameIn;
      this.playerID = playerIDIn;
      this.token = tokenIn;
      this.sessionType = Session.Type.setSessionType(sessionTypeIn);
   }

   public String getSessionID() {
      return "token:" + this.token + ":" + this.playerID;
   }

   public String getPlayerID() {
      return this.playerID;
   }

   public String getUsername() {
      return this.username;
   }

   public String getToken() {
      return this.token;
   }

   public GameProfile getProfile() {
      try {
         UUID uuid = UUIDTypeAdapter.fromString(this.getPlayerID());
         return new GameProfile(uuid, this.getUsername());
      } catch (IllegalArgumentException var2) {
         return new GameProfile((UUID)null, this.getUsername());
      }
   }

   public Type getSessionType() {
      return this.sessionType;
   }

   public static enum Type {
      LEGACY("legacy"),
      MOJANG("mojang");

      private static final Map SESSION_TYPES = Maps.newHashMap();
      private final String sessionType;

      private Type(String sessionTypeIn) {
         this.sessionType = sessionTypeIn;
      }

      public static Type setSessionType(String sessionTypeIn) {
         return (Type)SESSION_TYPES.get(sessionTypeIn.toLowerCase());
      }

      static {
         Type[] var0 = values();
         int var1 = var0.length;

         for(int var2 = 0; var2 < var1; ++var2) {
            Type session$type = var0[var2];
            SESSION_TYPES.put(session$type.sessionType, session$type);
         }

      }
   }
}
