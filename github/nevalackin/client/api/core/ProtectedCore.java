package io.github.nevalackin.client.api.core;

import io.github.nevalackin.client.impl.account.AccountManagerImpl;
import io.github.nevalackin.client.impl.binding.BindManagerImpl;
import io.github.nevalackin.client.impl.core.KetamineClient;
import io.github.nevalackin.client.impl.file.FileManagerImpl;
import io.github.nevalackin.client.impl.notification.NotificationManagerImpl;
import io.github.nevalackin.client.impl.ui.cfont.FontGlyphs;
import io.github.nevalackin.client.impl.ui.cfont.MipMappedFontRenderer;
import io.github.nevalackin.client.util.misc.ResourceUtil;
import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.DiscordRPC;

public class ProtectedCore {
   public static void start() {
      KetamineClient.getInstance().notificationManager = new NotificationManagerImpl();
      KetamineClient.getInstance().fileManager = new FileManagerImpl();
      KetamineClient.getInstance().bindManager = new BindManagerImpl();
      KetamineClient.getInstance().accountManager = new AccountManagerImpl();
      KetamineClient.getInstance().fontRenderer = new MipMappedFontRenderer(new FontGlyphs[]{new FontGlyphs(ResourceUtil.createFontTTF("fonts/Regular.ttf"), 500), new FontGlyphs(ResourceUtil.createFontTTF("fonts/Medium.ttf"), 700), new FontGlyphs(ResourceUtil.createFontTTF("fonts/Bold.ttf"), 900)});
      DiscordRPC.discordInitialize("877275167653498941", (new DiscordEventHandlers.Builder()).build(), true);
   }
}
