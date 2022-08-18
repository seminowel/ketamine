package io.github.nevalackin.client.impl.core;

import com.google.gson.JsonObject;
import fr.litarvan.openauth.microsoft.MicrosoftAuthenticator;
import io.github.nevalackin.client.api.account.AccountManager;
import io.github.nevalackin.client.api.binding.Bind;
import io.github.nevalackin.client.api.binding.BindManager;
import io.github.nevalackin.client.api.binding.BindType;
import io.github.nevalackin.client.api.binding.Bindable;
import io.github.nevalackin.client.api.command.CommandRegistry;
import io.github.nevalackin.client.api.config.ConfigManager;
import io.github.nevalackin.client.api.core.AbstractClientCore;
import io.github.nevalackin.client.api.file.FileManager;
import io.github.nevalackin.client.api.friends.FriendManager;
import io.github.nevalackin.client.api.module.ModuleManager;
import io.github.nevalackin.client.api.notification.NotificationManager;
import io.github.nevalackin.client.api.script.ScriptManager;
import io.github.nevalackin.client.api.ui.cfont.CustomFontRenderer;
import io.github.nevalackin.client.impl.account.AccountManagerImpl;
import io.github.nevalackin.client.impl.binding.BindManagerImpl;
import io.github.nevalackin.client.impl.config.ConfigManagerImpl;
import io.github.nevalackin.client.impl.event.GameFastTickEvent;
import io.github.nevalackin.client.impl.event.game.input.InputType;
import io.github.nevalackin.client.impl.file.FileManagerImpl;
import io.github.nevalackin.client.impl.module.ModuleManagerImpl;
import io.github.nevalackin.client.impl.module.render.overlay.Gui;
import io.github.nevalackin.client.impl.notification.NotificationManagerImpl;
import io.github.nevalackin.client.impl.script.ScriptManagerImpl;
import io.github.nevalackin.client.impl.ui.click.GuiUIScreen;
import io.github.nevalackin.client.impl.ui.nl.GuiNLUIScreen;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import io.github.nevalackin.homoBus.bus.Bus;
import io.github.nevalackin.homoBus.bus.impl.EventBus;
import java.security.KeyPair;
import javax.crypto.spec.SecretKeySpec;
import net.arikia.dev.drpc.DiscordRPC;
import net.arikia.dev.drpc.DiscordRichPresence;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ChatComponentText;
import viamcp.ViaMCP;

public final class KetamineClient extends AbstractClientCore {
   private static KetamineClient instance;
   public CustomFontRenderer fontRenderer;
   public SecretKeySpec aesKey;
   public KeyPair keyPair;
   public String sessionToken;
   private GuiScreen uiScreen;
   private GuiUIScreen dropdownGUI;
   private GuiNLUIScreen neverloseGUI;
   private long startTime;
   public FileManagerImpl fileManager;
   public AccountManagerImpl accountManager;
   public BindManagerImpl bindManager;
   public FriendManager friendManager;
   public NotificationManagerImpl notificationManager;
   public MicrosoftAuthenticator microsoftAuthenticator;
   Thread fastTickThread;
   @EventLink
   private final Listener onCloseGame = (event) -> {
      this.getConfigManager().save("default");
      this.getBindManager().save();
      this.getAccountManager().save();
      DiscordRPC.discordShutdown();
   };

   public KetamineClient() {
      super("Ketamine", "v5.7");
   }

   public void init() {
      this.getModuleManager();
      this.getFileManager();
      this.getBindManager().register(this.openCloseUI(), new Bind(InputType.KEYBOARD, 54, BindType.TOGGLE));
      this.getAccountManager().load();
      this.getBindManager().load();
      this.getConfigManager().load("default");
      this.friendManager = new FriendManager();
      this.microsoftAuthenticator = new MicrosoftAuthenticator();
      this.dropdownGUI = new GuiUIScreen();
      this.neverloseGUI = new GuiNLUIScreen();

      try {
         ViaMCP.getInstance().start();
      } catch (Exception var2) {
         var2.printStackTrace();
      }

      (this.fastTickThread = new Thread(() -> {
         while(this.fastTickThread != null && !this.fastTickThread.isInterrupted()) {
            try {
               Thread.sleep(15L);
               this.getEventBus().post(new GameFastTickEvent());
            } catch (Exception var2) {
            }
         }

      })).start();
   }

   private Bindable openCloseUI() {
      this.uiScreen = new GuiUIScreen();
      return new Bindable() {
         public String getName() {
            return "UI";
         }

         public void setActive(boolean active) {
            Minecraft.getMinecraft().displayGuiScreen((GuiScreen)(active ? (Gui.guiModeProperty.getValue() == Gui.GuiMode.NEVERLOSE ? KetamineClient.this.neverloseGUI : KetamineClient.this.dropdownGUI) : null));
         }

         public boolean isActive() {
            return Minecraft.getMinecraft().currentScreen == KetamineClient.this.uiScreen;
         }

         public void loadBind(JsonObject object) {
         }

         public void saveBind(JsonObject object) {
         }
      };
   }

   public void updateDiscordRPC(String msg) {
      DiscordRichPresence rpc = (new DiscordRichPresence.Builder(msg)).setSmallImage("rpc", "Join").setBigImage("big_rpc", "discord.gg/6zmsj9m4Ta").setStartTimestamps(System.currentTimeMillis()).build();
      DiscordRPC.discordUpdatePresence(rpc);
   }

   public void logChat(String format, Object... args) {
      Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new ChatComponentText("[§5Ketamine§r]: " + String.format(format, args)));
   }

   public void logChat(String format) {
      this.logChat(format);
   }

   protected Bus getBusImpl() {
      return new EventBus();
   }

   protected FileManager getFileImpl() {
      return new FileManagerImpl();
   }

   protected ModuleManager getModuleManagerImpl() {
      return new ModuleManagerImpl();
   }

   protected ConfigManager getConfigManagerImpl() {
      return new ConfigManagerImpl();
   }

   protected AccountManager getAccountManagerImpl() {
      return new AccountManagerImpl();
   }

   protected ScriptManager getScriptManagerImpl() {
      return new ScriptManagerImpl();
   }

   public CustomFontRenderer getFontRenderer() {
      return this.fontRenderer;
   }

   protected NotificationManager getNotificationManagerImpl() {
      return new NotificationManagerImpl();
   }

   protected BindManager getBindManagerImpl() {
      return new BindManagerImpl();
   }

   public MicrosoftAuthenticator getMicrosoftAuthenticator() {
      return this.microsoftAuthenticator;
   }

   public CommandRegistry getCommandRegistryImpl() {
      return new CommandRegistry();
   }

   public long getStartTime() {
      return this.startTime;
   }

   public void setStartTime(long startTime) {
      this.startTime = startTime;
   }

   public GuiUIScreen getDropdownGUI() {
      return this.dropdownGUI;
   }

   public static KetamineClient getInstance() {
      if (instance == null) {
         instance = new KetamineClient();
      }

      return instance;
   }
}
