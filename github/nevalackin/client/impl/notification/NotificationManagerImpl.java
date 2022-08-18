package io.github.nevalackin.client.impl.notification;

import io.github.nevalackin.client.api.notification.NotificationManager;
import io.github.nevalackin.client.api.notification.NotificationType;
import io.github.nevalackin.client.impl.core.KetamineClient;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;

public final class NotificationManagerImpl implements NotificationManager {
   private final List notifications = new ArrayList();
   @EventLink
   private final Listener onDrawGameOverlay = (event) -> {
      this.onDraw(event.getScaledResolution());
   };
   @EventLink
   private final Listener onDrawScreen = (event) -> {
      this.onDraw(event.getScaledResolution());
   };

   public NotificationManagerImpl() {
      KetamineClient.getInstance().getEventBus().subscribe(this);
   }

   public void add(NotificationType type, String title, String body, long duration) {
      this.notifications.add(new Notification(KetamineClient.getInstance().getFontRenderer(), title, body, duration, type));
   }

   public void onDraw(ScaledResolution scaledResolution) {
      int removeIndex = -1;
      GL11.glPushMatrix();
      GL11.glTranslated((double)scaledResolution.getScaledWidth(), (double)scaledResolution.getScaledHeight(), 1.0);
      int i = 0;

      for(int size = this.notifications.size(); i < size; ++i) {
         Notification notification = (Notification)this.notifications.get(i);
         if (notification.isDead()) {
            removeIndex = i;
         } else {
            GL11.glTranslated(0.0, -(notification.getHeight() + 2.0), 0.0);
            notification.render();
         }
      }

      GL11.glPopMatrix();
      if (removeIndex != -1) {
         this.notifications.remove(removeIndex);
      }

   }
}
