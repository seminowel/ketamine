package io.github.nevalackin.client.api.command;

import io.github.nevalackin.client.api.notification.NotificationType;
import io.github.nevalackin.client.impl.core.KetamineClient;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;

public class ConfigCommand extends Command {
   private final KetamineClient clientMain = KetamineClient.getInstance();

   public ConfigCommand() {
      super("save/load configs", "config", "cfg");
   }

   public void execute(String[] args) {
      if (args.length == 3) {
         if (args[1].equalsIgnoreCase("load")) {
            this.clientMain.getConfigManager().load(args[2]);
            KetamineClient.getInstance().getNotificationManager().add(NotificationType.SUCCESS, "Config", "Successfully Loaded Config: " + args[2], 1500L);
            return;
         }

         if (args[1].equalsIgnoreCase("save")) {
            this.clientMain.getConfigManager().save(args[2]);
            this.clientMain.getConfigManager().refresh();
            return;
         }
      }

      Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("[Ketamine]: §cWrong syntax!"));
      Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("[Ketamine]: §c.cfg save <name>"));
      Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("[Ketamine]: §c.cfg load <name>"));
   }
}
