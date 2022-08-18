package io.github.nevalackin.client.api.notification;

import io.github.nevalackin.client.api.ui.cfont.StaticallySizedImage;
import io.github.nevalackin.client.util.misc.ResourceUtil;
import java.io.IOException;
import javax.imageio.ImageIO;

public enum NotificationType {
   WARNING(-256),
   SUCCESS(-16711847),
   INFO(-5971460),
   ERROR(-65536);

   private StaticallySizedImage image;
   private final int colour;

   private NotificationType(int colour) {
      this.colour = colour;
   }

   public int getColour() {
      return this.colour;
   }

   public StaticallySizedImage getImage() {
      if (this.image == null) {
         try {
            this.image = new StaticallySizedImage(ImageIO.read(ResourceUtil.getResourceStream(String.format("icons/notifications/%s.png", this.name().toLowerCase()))), true, 3);
         } catch (IOException var2) {
         }
      }

      return this.image;
   }
}
