package io.github.nevalackin.client.impl.account;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.nevalackin.client.api.account.Account;
import io.github.nevalackin.client.api.account.AccountManager;
import io.github.nevalackin.client.api.file.FileManager;
import io.github.nevalackin.client.impl.core.KetamineClient;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.Session;

public final class AccountManagerImpl implements AccountManager {
   private final List accounts = new ArrayList();
   @EventLink
   private final Listener onWatchdogBan = (event) -> {
      IChatComponent reason = event.getReason();
      String text = reason.getUnformattedText();
      String[] lines = text.split("\n");
      if (lines.length == 7) {
         String firstLine = lines[0];
         if (firstLine.equals("You are permanently banned from this server!")) {
            Session currentSession = Minecraft.getMinecraft().getSession();
            if (currentSession != null) {
               Account currentAccountx = this.getAccountByUsername(currentSession.getUsername());
               if (currentAccountx != null) {
                  currentAccountx.setUnbanDate(-1337L);
               }
            }
         } else if (firstLine.startsWith("You are temporarily banned for") && firstLine.endsWith("from this server!")) {
            String banLengthStr = firstLine.substring(31).replace(" from this server!", "");
            SimpleDateFormat hypixelDateFormat = new SimpleDateFormat("D'd' H'h' m'm' s's'");

            try {
               long banTimeRemaining = hypixelDateFormat.parse(banLengthStr).getTime();
               Session currentSessionx = Minecraft.getMinecraft().getSession();
               if (currentSessionx != null) {
                  Account currentAccount = this.getAccountByUsername(currentSessionx.getUsername());
                  if (currentAccount != null) {
                     currentAccount.setUnbanDate(System.currentTimeMillis() + banTimeRemaining);
                  }
               }
            } catch (ParseException var12) {
            }
         }

      }
   };

   public AccountManagerImpl() {
      KetamineClient.getInstance().getEventBus().subscribe(this);
   }

   private Account getAccountByUsername(String username) {
      Iterator var2 = this.getAccounts().iterator();

      Account account;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         account = (Account)var2.next();
      } while(!account.getUsername().equals(username));

      return account;
   }

   public void load() {
      FileManager fileManager = KetamineClient.getInstance().getFileManager();
      File accountsFile = fileManager.getFile("alts");
      JsonElement array = fileManager.parse(accountsFile);
      if (array.isJsonArray()) {
         Iterator var4 = array.getAsJsonArray().iterator();

         while(var4.hasNext()) {
            JsonElement element = (JsonElement)var4.next();
            JsonObject object = element.getAsJsonObject();
            this.addAccount(Account.from(object));
         }
      }

   }

   public void save() {
      FileManager fileManager = KetamineClient.getInstance().getFileManager();
      File accountsFile = fileManager.getFile("alts");
      JsonArray accountsArray = new JsonArray();
      Iterator var4 = this.getAccounts().iterator();

      while(var4.hasNext()) {
         Account account = (Account)var4.next();
         accountsArray.add(account.save());
      }

      fileManager.writeJson(accountsFile, accountsArray);
   }

   public void addAccount(Account account) {
      this.accounts.add(account);
   }

   public void deleteAccount(Account account) {
      this.accounts.remove(account);
   }

   public Collection getAccounts() {
      return this.accounts;
   }

   public Collection getWorkingAccounts() {
      return (Collection)this.accounts.stream().filter((account) -> {
         return account.isWorking() || account.getUsername().length() == 0;
      }).collect(Collectors.toList());
   }

   public Collection getUnbannedAccounts() {
      return (Collection)this.getWorkingAccounts().stream().filter((account) -> {
         return !account.isBanned();
      }).collect(Collectors.toList());
   }
}
