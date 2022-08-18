package io.github.nevalackin.client.impl.module.misc.player;

import com.google.common.collect.Lists;
import io.github.nevalackin.client.api.module.Category;
import io.github.nevalackin.client.api.module.Module;
import io.github.nevalackin.client.api.notification.NotificationType;
import io.github.nevalackin.client.api.property.Property;
import io.github.nevalackin.client.impl.core.KetamineClient;
import io.github.nevalackin.client.impl.property.BooleanProperty;
import io.github.nevalackin.client.impl.property.EnumProperty;
import io.github.nevalackin.client.impl.property.MultiSelectionEnumProperty;
import io.github.nevalackin.client.util.misc.ServerUtil;
import io.github.nevalackin.client.util.misc.TimeUtil;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S45PacketTitle;

public final class AutoHypixel extends Module {
   private final BooleanProperty autoPlayProperty = new BooleanProperty("Auto Play", true);
   private final EnumProperty gameTypeProperty;
   private final EnumProperty killSults;
   private final MultiSelectionEnumProperty mapBlacklistOptionsProperty;
   private final File customKillSultsFile;
   private TimeUtil delay;
   private TimeUtil banMSGDelay;
   private boolean hasSkipped;
   private static final char[] BYPASS_CHARS = new char[]{'⛏', '⛘', '⛜', '⛠', '⛟', '⛐', '⛍', '⛡', '⛋', '⛌', '⛗', '⛩', '⛉'};
   private final String[] insults;
   @EventLink
   private final Listener onReceivePacket;
   @EventLink
   private final Listener onUpdatePosition;
   @EventLink
   private final Listener onLoadWorld;

   public AutoHypixel() {
      super("Auto Hypixel", Category.MISC, Category.SubCategory.MISC_PLAYER);
      gameMode var10004 = AutoHypixel.gameMode.SOLOS_INSANE;
      BooleanProperty var10005 = this.autoPlayProperty;
      var10005.getClass();
      this.gameTypeProperty = new EnumProperty("Mode", var10004, var10005::getValue);
      this.killSults = new EnumProperty("Kill Sults", AutoHypixel.KillSultsMode.ENGLISH);
      ArrayList var4 = Lists.newArrayList();
      MapBlacklistOption[] var3 = AutoHypixel.MapBlacklistOption.values();
      BooleanProperty var10006 = this.autoPlayProperty;
      var10006.getClass();
      this.mapBlacklistOptionsProperty = new MultiSelectionEnumProperty("Blacklisted Maps", var4, var3, var10006::getValue);
      this.delay = new TimeUtil();
      this.banMSGDelay = new TimeUtil();
      this.insults = new String[]{"%s shut the /fuck/ up /fatass/", "Childhood obesity is an epidemic, %s needs help.", "Solve for %s's chromosomes, x = 46 + 1.", "%s I can count your friends on one hand.", "%s got /raped/ by /trannyhack/.", "I /shagged/ %s's mum", "I'm fucking rapid mate", ""};
      this.onReceivePacket = (event) -> {
         Packet packet = event.getPacket();
         String message;
         if (packet instanceof S45PacketTitle) {
            S45PacketTitle s45 = (S45PacketTitle)packet;
            if (s45.getMessage() != null && (Boolean)this.autoPlayProperty.getValue()) {
               message = s45.getMessage().getFormattedText().toLowerCase();
               if (!message.contains("you died") && !message.contains("game over")) {
                  if (message.contains("you win") || message.contains("victory")) {
                     KetamineClient.getInstance().getNotificationManager().add(NotificationType.SUCCESS, "You Won!", "Sending Into Next Game", 2000L);
                     switch ((gameMode)this.gameTypeProperty.getValue()) {
                        case SOLOS_INSANE:
                           this.mc.thePlayer.dispatchCommand("play solo_insane");
                           break;
                        case TEAMS_INSANE:
                           this.mc.thePlayer.dispatchCommand("play teams_insane");
                     }
                  }
               } else {
                  KetamineClient.getInstance().getNotificationManager().add(NotificationType.ERROR, "You Lost", "Sending Into Next Game, Play Better Perhaps?", 2000L);
                  switch ((gameMode)this.gameTypeProperty.getValue()) {
                     case SOLOS_INSANE:
                        this.mc.thePlayer.dispatchCommand("play solo_insane");
                        break;
                     case TEAMS_INSANE:
                        this.mc.thePlayer.dispatchCommand("play teams_insane");
                  }
               }
            }
         }

         if (packet instanceof S02PacketChat) {
            S02PacketChat s02 = (S02PacketChat)packet;
            message = "A player has been removed from your game.";
            if (this.banMSGDelay.passed(5000L) && s02.getChatComponent().getUnformattedText().startsWith(message)) {
               this.mc.thePlayer.sendChatMessage("/hub");
            }
         }

      };
      this.onUpdatePosition = (event) -> {
         Iterator var2 = ((List)this.mapBlacklistOptionsProperty.getValue()).iterator();

         while(var2.hasNext()) {
            MapBlacklistOption blacklistedMap = (MapBlacklistOption)var2.next();
            if (ServerUtil.getCurrentHypixelMap() == null) {
               return;
            }

            if (ServerUtil.getCurrentHypixelMap().contains(blacklistedMap.toString()) && this.delay.passed(6000L) && !this.hasSkipped) {
               KetamineClient.getInstance().getNotificationManager().add(NotificationType.WARNING, this.getName(), blacklistedMap + " is Blacklisted, Skipping.", 2000L);
               switch ((gameMode)this.gameTypeProperty.getValue()) {
                  case SOLOS_INSANE:
                     this.mc.thePlayer.dispatchCommand("play solo_insane");
                     break;
                  case TEAMS_INSANE:
                     this.mc.thePlayer.dispatchCommand("play teams_insane");
               }

               this.hasSkipped = true;
            }
         }

      };
      this.onLoadWorld = (event) -> {
         this.delay.reset();
         this.banMSGDelay.reset();
         this.hasSkipped = false;
      };
      this.register(new Property[]{this.autoPlayProperty, this.gameTypeProperty, this.mapBlacklistOptionsProperty});
      this.customKillSultsFile = new File("killsults.txt");
      if (!this.customKillSultsFile.exists()) {
         try {
            boolean var1 = this.customKillSultsFile.createNewFile();
         } catch (IOException var2) {
         }
      }

   }

   public void onEnable() {
   }

   public void onDisable() {
   }

   private static enum MapBlacklistOption {
      AEGIS("Aegis"),
      ATLAS("Atlas"),
      CLEARING("Clearing"),
      DAWN("Dawn"),
      DESSERTED_ISLAND("Desserted Island"),
      ELVEN("Elven"),
      EMBERCELL("Embercell"),
      ENTANGLED("Entangled"),
      FOSSIL("Fossil"),
      FRAGMENT("Fragment"),
      FIRELINK_SHRINE("Firelink Shrine"),
      FROSTBOUND("Frostbound"),
      GARAGE("Garage"),
      GARRISON("Garrision"),
      HANGING_GARDENS("Hanging Gardens"),
      HEAVEN_PALACE("Heaven Palace"),
      JAGGED("Jagged"),
      MYTHIC("Mythic"),
      NIU("Niu"),
      OCEANA("Oceana"),
      ONIONRING_2("Onionring 2"),
      PALETTE("Palette"),
      RAILROAD("Railroad"),
      SANCTUARY("Sanctuary"),
      SHIRE("Shire"),
      SENTINEL("Sentinel"),
      SHAOHAO("Shaohao"),
      SIEGE("Siege"),
      SUBMERGED("Submerged"),
      TIKI("Tiki"),
      TRIBAL("Tribal"),
      TOWER("Tower"),
      MARTIAN("Martian"),
      TRIBUTE("Trbiute"),
      WATERWAYS("Waterways"),
      WINTERHELM("Winterhelm");

      private final String name;

      private MapBlacklistOption(String name) {
         this.name = name;
      }

      public String toString() {
         return this.name;
      }
   }

   private static enum KillSultsMode {
      DISABLED("Disabled"),
      ENGLISH("English"),
      CUSTOM("Custom"),
      CZECH("Czech");

      private final String name;

      private KillSultsMode(String name) {
         this.name = name;
      }

      public String toString() {
         return this.name;
      }
   }

   private static enum gameMode {
      SOLOS_INSANE("Solo Insane"),
      TEAMS_INSANE("Teams Insane");

      private final String name;

      private gameMode(String name) {
         this.name = name;
      }

      public String toString() {
         return this.name;
      }
   }
}
