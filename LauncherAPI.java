import java.io.File;
import net.minecraft.client.main.Main;

public class LauncherAPI {
   public static void launch() {
      String appData = System.getenv("APPDATA");
      File workingDirectory = new File(appData, ".minecraft/");
      Main.main(new String[]{"--version", "1.8.9", "--accessToken", "0", "--assetIndex", "1.8", "--userProperties", "{}", "--assetsDir", (new File(workingDirectory, "assets/")).getAbsolutePath()});
   }
}
