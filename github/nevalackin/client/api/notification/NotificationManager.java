package io.github.nevalackin.client.api.notification;

import net.minecraft.client.gui.ScaledResolution;

public interface NotificationManager {
   void add(NotificationType var1, String var2, String var3, long var4);

   void onDraw(ScaledResolution var1);
}
