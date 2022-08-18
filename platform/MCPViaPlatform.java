package viamcp.platform;

import com.viaversion.viaversion.api.ViaAPI;
import com.viaversion.viaversion.api.command.ViaCommandSender;
import com.viaversion.viaversion.api.configuration.ConfigurationProvider;
import com.viaversion.viaversion.api.configuration.ViaVersionConfig;
import com.viaversion.viaversion.api.platform.PlatformTask;
import com.viaversion.viaversion.api.platform.ViaPlatform;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.libs.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import com.viaversion.viaversion.libs.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import io.netty.util.concurrent.GenericFutureListener;
import java.io.File;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import org.apache.logging.log4j.LogManager;
import viamcp.ViaMCP;
import viamcp.utils.FutureTaskId;
import viamcp.utils.JLoggerToLog4j;

public class MCPViaPlatform implements ViaPlatform {
   private final Logger logger = new JLoggerToLog4j(LogManager.getLogger("ViaVersion"));
   private final MCPViaConfig config;
   private final File dataFolder;
   private final ViaAPI api;

   public MCPViaPlatform(File dataFolder) {
      Path configDir = dataFolder.toPath().resolve("ViaVersion");
      this.config = new MCPViaConfig(configDir.resolve("viaversion.yml").toFile());
      this.dataFolder = configDir.toFile();
      this.api = new MCPViaAPI();
   }

   public static String legacyToJson(String legacy) {
      return (String)GsonComponentSerializer.gson().serialize(LegacyComponentSerializer.legacySection().deserialize(legacy));
   }

   public Logger getLogger() {
      return this.logger;
   }

   public String getPlatformName() {
      return "ViaMCP";
   }

   public String getPlatformVersion() {
      return String.valueOf(47);
   }

   public String getPluginVersion() {
      return "4.1.1";
   }

   public FutureTaskId runAsync(Runnable runnable) {
      return new FutureTaskId(CompletableFuture.runAsync(runnable, ViaMCP.getInstance().getAsyncExecutor()).exceptionally((throwable) -> {
         if (!(throwable instanceof CancellationException)) {
            throwable.printStackTrace();
         }

         return null;
      }));
   }

   public FutureTaskId runSync(Runnable runnable) {
      return new FutureTaskId(ViaMCP.getInstance().getEventLoop().submit(runnable).addListener(this.errorLogger()));
   }

   public PlatformTask runSync(Runnable runnable, long ticks) {
      return new FutureTaskId(ViaMCP.getInstance().getEventLoop().schedule(() -> {
         return this.runSync(runnable);
      }, ticks * 50L, TimeUnit.MILLISECONDS).addListener(this.errorLogger()));
   }

   public PlatformTask runRepeatingSync(Runnable runnable, long ticks) {
      return new FutureTaskId(ViaMCP.getInstance().getEventLoop().scheduleAtFixedRate(() -> {
         this.runSync(runnable);
      }, 0L, ticks * 50L, TimeUnit.MILLISECONDS).addListener(this.errorLogger()));
   }

   private GenericFutureListener errorLogger() {
      return (future) -> {
         if (!future.isCancelled() && future.cause() != null) {
            future.cause().printStackTrace();
         }

      };
   }

   public ViaCommandSender[] getOnlinePlayers() {
      return new ViaCommandSender[1337];
   }

   private ViaCommandSender[] getServerPlayers() {
      return new ViaCommandSender[1337];
   }

   public void sendMessage(UUID uuid, String s) {
   }

   public boolean kickPlayer(UUID uuid, String s) {
      return false;
   }

   public boolean isPluginEnabled() {
      return true;
   }

   public ViaAPI getApi() {
      return this.api;
   }

   public ViaVersionConfig getConf() {
      return this.config;
   }

   public ConfigurationProvider getConfigurationProvider() {
      return this.config;
   }

   public File getDataFolder() {
      return this.dataFolder;
   }

   public void onReload() {
      this.logger.info("ViaVersion was reloaded? (How did that happen)");
   }

   public JsonObject getDump() {
      JsonObject platformSpecific = new JsonObject();
      return platformSpecific;
   }

   public boolean isOldClientsAllowed() {
      return true;
   }
}
