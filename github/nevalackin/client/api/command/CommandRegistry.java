package io.github.nevalackin.client.api.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CommandRegistry {
   public final String PREFIX = ".";
   private final List commands = new ArrayList();
   private final Map aliasesMap = new HashMap();
   private final CommandHandler handler = new CommandHandler(this);

   public CommandRegistry() {
      this.register(new ConfigCommand());
   }

   public void register(Command command) {
      this.commands.add(command);
      String[] var2 = command.getAliases();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         String alias = var2[var4];
         this.aliasesMap.put(alias, command);
      }

   }

   public List getCommands() {
      return this.commands;
   }

   public Map getAliasesMap() {
      return this.aliasesMap;
   }

   public CommandHandler getHandler() {
      return this.handler;
   }
}
