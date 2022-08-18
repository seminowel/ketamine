package io.github.nevalackin.client.api.account;

import java.util.Collection;

public interface AccountManager {
   void save();

   void load();

   void addAccount(Account var1);

   void deleteAccount(Account var1);

   Collection getAccounts();

   Collection getWorkingAccounts();

   Collection getUnbannedAccounts();
}
