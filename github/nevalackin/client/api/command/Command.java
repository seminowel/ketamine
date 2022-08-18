package io.github.nevalackin.client.api.command;

import io.github.nevalackin.client.impl.core.KetamineClient;

public abstract class Command {
   private final String description;
   private final String[] aliases;

   public Command(String description, String... aliases) {
      this.description = description;
      this.aliases = aliases;
   }

   public abstract void execute(String[] var1);

   public String getDescription() {
      return this.description;
   }

   public String[] getAliases() {
      return this.aliases;
   }

   public String toString() {
      StringBuilder var10000 = new StringBuilder();
      KetamineClient.getInstance().getCommandRegistryImpl().getClass();
      return var10000.append(".").append(this.aliases[0]).append(" - ").append(this.description).toString();
   }
}
