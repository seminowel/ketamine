package io.github.nevalackin.client.api.account;

import com.google.gson.JsonObject;
import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.UserAuthentication;
import com.mojang.authlib.UserType;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.util.UUIDTypeAdapter;
import io.github.nevalackin.client.api.notification.NotificationType;
import io.github.nevalackin.client.impl.core.KetamineClient;
import java.net.Proxy;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;

public final class Account {
   private String username;
   private final String email;
   private final String password;
   private final String accessToken;
   private final String refreshToken;
   private final String profileId;
   private AccountType type;
   private long unbanTime;
   private boolean working;

   Account(String username, String email, String password, String profileId, String accessToken, String refreshToken, AccountType type, long unbanTime, boolean working) {
      this.username = username;
      this.email = email;
      this.password = password;
      this.profileId = profileId;
      this.accessToken = accessToken;
      this.refreshToken = refreshToken;
      this.type = type;
      this.working = working;
      this.unbanTime = unbanTime;
   }

   public void createLoginThread() {
      Thread altLoginThread = new Thread(() -> {
         switch (this.getType()) {
            case MOJANG:
               UserAuthentication authentication = (new YggdrasilAuthenticationService(Proxy.NO_PROXY, "")).createUserAuthentication(Agent.MINECRAFT);
               authentication.setUsername(this.getEmail());
               authentication.setPassword(this.getPassword());
               KetamineClient.getInstance().getNotificationManager().add(NotificationType.INFO, "Logging In", String.format("Attempting to login to %s.", this.getEmail()), 1000L);

               try {
                  authentication.logIn();
               } catch (AuthenticationException var3) {
                  KetamineClient.getInstance().getNotificationManager().add(NotificationType.ERROR, "Invalid Credentials", String.format("Invalid credentials for account %s.", this.getUsername()), 1500L);
                  if (this.getUsername().length() == 0) {
                     this.setUsername("invalid_account");
                  }

                  this.setWorking(false);
                  return;
               }

               String username = authentication.getSelectedProfile().getName();
               this.setUsername(username);
               this.setWorking(true);
               KetamineClient.getInstance().getNotificationManager().add(NotificationType.SUCCESS, "Logged In", String.format("Successfully logged into %s.", username), 1500L);
               Minecraft.getMinecraft().setSession(new Session(username, UUIDTypeAdapter.fromUUID(authentication.getSelectedProfile().getId()), authentication.getAuthenticatedToken(), authentication.getUserType().getName()));
               break;
            case MICROSOFT:
               KetamineClient.getInstance().getNotificationManager().add(NotificationType.SUCCESS, "Logged In", String.format("Successfully logged into %s.", this.username), 1500L);
               Minecraft.getMinecraft().setSession(new Session(this.getUsername(), this.getProfileId(), this.getAccessToken(), "mojang"));
               break;
            default:
               this.setUsername(this.getEmail());
               GameProfile profile = new GameProfile(UUID.randomUUID(), this.getUsername());
               Minecraft.getMinecraft().setSession(new Session(profile.getName(), UUIDTypeAdapter.fromUUID(profile.getId()), "", UserType.MOJANG.getName()));
         }

      });
      altLoginThread.start();
   }

   public String getUsername() {
      return this.username;
   }

   public void setUsername(String username) {
      this.username = username;
   }

   public String getEmail() {
      return this.email;
   }

   public String getPassword() {
      return this.password;
   }

   public String getAccessToken() {
      return this.accessToken;
   }

   public String getRefreshToken() {
      return this.refreshToken;
   }

   public String getProfileId() {
      return this.profileId;
   }

   public AccountType getType() {
      return this.type;
   }

   public long getUnbanDate() {
      return this.unbanTime;
   }

   public void setUnbanDate(long unbanDate) {
      this.unbanTime = unbanDate;
   }

   public boolean isBanned() {
      return this.unbanTime == -1337L || System.currentTimeMillis() < this.unbanTime;
   }

   public boolean isWorking() {
      return this.working;
   }

   public void setWorking(boolean working) {
      this.working = working;
   }

   public JsonObject save() {
      JsonObject object = new JsonObject();
      object.addProperty("email", this.email);
      object.addProperty("pass", this.password);
      if (this.username != null) {
         object.addProperty("user", this.username);
      }

      object.addProperty("working", this.working);
      object.addProperty("unban_time", this.unbanTime);
      if (this.accessToken != null) {
         object.addProperty("access", this.accessToken);
      }

      if (this.refreshToken != null) {
         object.addProperty("refresh", this.refreshToken);
      }

      if (this.profileId != null) {
         object.addProperty("profileId", this.profileId);
      }

      return object;
   }

   public static Account from(JsonObject object) {
      AccountBuilder builder = new AccountBuilder();
      if (object.has("email")) {
         builder.email(object.get("email").getAsString());
      }

      if (object.has("pass")) {
         builder.password(object.get("pass").getAsString());
      }

      if (object.has("user")) {
         builder.username(object.get("user").getAsString());
      }

      if (object.has("working")) {
         builder.working(object.get("working").getAsBoolean());
      }

      if (object.has("unban_time")) {
         builder.unbanTime(object.get("unban_time").getAsLong());
      }

      if (object.has("refresh")) {
         builder.refreshToken(object.get("refresh").getAsString());
      }

      if (object.has("access")) {
         builder.accessToken(object.get("access").getAsString());
      }

      if (object.has("profileId")) {
         builder.profileId(object.get("profileId").getAsString());
      }

      return builder.build();
   }

   public static AccountBuilder builder() {
      return new AccountBuilder();
   }

   public String toString() {
      return this.getUsername() != null ? this.getUsername() : (this.getEmail() != null ? this.getEmail() : (this.getType() == AccountType.MICROSOFT ? (this.getProfileId() != null ? this.getProfileId() : "Invalid Account") : "Invalid Account"));
   }

   public static class AccountBuilder {
      private String username;
      private String email;
      private String password;
      private String profileId;
      private String accessToken;
      private String refreshToken;
      private AccountType type;
      private long unbanTime;
      private boolean working;

      private AccountBuilder() {
         this.username = "";
         this.password = "";
         this.profileId = null;
         this.accessToken = null;
         this.refreshToken = null;
         this.type = AccountType.MOJANG;
      }

      public AccountBuilder username(String username) {
         this.username = username;
         return this;
      }

      public AccountBuilder email(String email) {
         this.email = email;
         return this;
      }

      public AccountBuilder password(String password) {
         this.password = password;
         return this;
      }

      public AccountBuilder profileId(String profileId) {
         this.profileId = profileId;
         return this;
      }

      public AccountBuilder accessToken(String accessToken) {
         this.accessToken = accessToken;
         return this;
      }

      public AccountBuilder refreshToken(String refreshToken) {
         this.refreshToken = refreshToken;
         return this;
      }

      public AccountBuilder unbanTime(long unbanTime) {
         this.unbanTime = unbanTime;
         return this;
      }

      public AccountBuilder working(boolean working) {
         this.working = working;
         return this;
      }

      public Account build() {
         if (this.accessToken != null && this.refreshToken != null && this.profileId != null) {
            this.type = AccountType.MICROSOFT;
         } else if (this.password.length() == 0) {
            this.type = AccountType.OFFLINE;
         }

         return new Account(this.username, this.email, this.password, this.profileId, this.accessToken, this.refreshToken, this.type, this.unbanTime, this.working);
      }

      // $FF: synthetic method
      AccountBuilder(Object x0) {
         this();
      }
   }
}
