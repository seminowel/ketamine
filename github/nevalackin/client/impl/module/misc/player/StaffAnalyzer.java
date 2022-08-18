package io.github.nevalackin.client.impl.module.misc.player;

import io.github.nevalackin.client.api.module.Category;
import io.github.nevalackin.client.api.module.Module;
import io.github.nevalackin.client.api.notification.NotificationType;
import io.github.nevalackin.client.api.property.Property;
import io.github.nevalackin.client.impl.core.KetamineClient;
import io.github.nevalackin.client.impl.property.DoubleProperty;
import io.github.nevalackin.client.util.misc.ServerUtil;
import io.github.nevalackin.client.util.misc.TimeUtil;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import net.minecraft.network.play.server.S02PacketChat;
import org.apache.commons.lang3.StringUtils;

public class StaffAnalyzer extends Module {
   private String apiKey = "";
   private int lastStaffBans;
   private TimeUtil delay = new TimeUtil();
   private TimeUtil apiDelay = new TimeUtil();
   public static int totalBans = 0;
   private final DoubleProperty delayProperty = new DoubleProperty("Time Until Check", 60.0, 1.0, 60.0, 1.0);
   @EventLink
   private final Listener onReceivePacket = (event) -> {
      if (event.getPacket() instanceof S02PacketChat) {
         S02PacketChat s = (S02PacketChat)event.getPacket();
         String s0 = s.getChatComponent().getFormattedText();
         if (s0.contains("Your new API key is ")) {
            String s1 = this.getTextWithoutFormatting(s0);
            this.apiKey = s1.split("Your new API key is ")[1];
            event.setCancelled();
         }
      }

   };
   @EventLink
   private final Listener onUpdatePosition = (event) -> {
      if (ServerUtil.isHypixel()) {
         if (this.apiDelay.passed(2000L)) {
            this.mc.thePlayer.sendChatMessage("/api new");
            this.apiDelay.reset();
         }

         if (this.delay.passed((Double)this.delayProperty.getValue() * 1000.0) && !this.apiKey.isEmpty()) {
            (new Thread(() -> {
               try {
                  URL url = new URL("https://api.hypixel.net/punishmentstats?key=" + this.apiKey);

                  String read;
                  String line;
                  for(BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream())); (read = in.readLine()) != null; this.lastStaffBans = Integer.parseInt(line)) {
                     line = StringUtils.substringAfterLast(read, "staff_total\"").replace("}", "").replace(":", "");
                     if (this.lastStaffBans == 0) {
                        this.lastStaffBans = Integer.parseInt(line);
                     } else {
                        int bans = Integer.parseInt(line) - this.lastStaffBans;
                        if (bans > 0) {
                           ++totalBans;
                           totalBans = totalBans + bans - 1;
                           KetamineClient.getInstance().getNotificationManager().add(NotificationType.WARNING, this.getName(), "Staff banned " + bans + " player" + (bans == 1 ? "" : "s") + " in the last " + this.delayProperty.getValue() + " seconds " + totalBans + " total", 5000L);
                        } else {
                           KetamineClient.getInstance().getNotificationManager().add(NotificationType.INFO, this.getName(), "Staff banned 0 players in the last " + this.delayProperty.getValue() + " seconds " + totalBans + " total", 5000L);
                        }
                     }
                  }
               } catch (Exception var6) {
               }

            })).start();
            this.delay.reset();
         }
      } else {
         totalBans = 0;
      }

   };

   public StaffAnalyzer() {
      super("Staff Analyzer", Category.MISC, Category.SubCategory.MISC_PLAYER);
      this.register(new Property[]{this.delayProperty});
   }

   private String getTextWithoutFormatting(String input) {
      StringBuilder builder = new StringBuilder();
      boolean skip = false;
      char[] var4 = input.toCharArray();
      int var5 = var4.length;

      for(int var6 = 0; var6 < var5; ++var6) {
         char c = var4[var6];
         if (c == 167) {
            skip = true;
         } else if (skip) {
            skip = false;
         } else {
            builder.append(c);
         }
      }

      return builder.toString();
   }

   public void onEnable() {
   }

   public void onDisable() {
   }
}
