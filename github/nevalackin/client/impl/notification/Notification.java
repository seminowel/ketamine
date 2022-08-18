package io.github.nevalackin.client.impl.notification;

import io.github.nevalackin.client.api.notification.NotificationType;
import io.github.nevalackin.client.api.ui.cfont.CustomFontRenderer;
import io.github.nevalackin.client.util.render.ColourUtil;
import io.github.nevalackin.client.util.render.DrawUtil;
import org.lwjgl.opengl.GL11;

public final class Notification {
   private final CustomFontRenderer fontRenderer;
   private final String title;
   private final String body;
   private final long duration;
   private final long timeOfCreation;
   private final NotificationType type;
   private final double width;
   private final double height;

   public Notification(CustomFontRenderer fontRenderer, String title, String body, long duration, NotificationType type) {
      this.fontRenderer = fontRenderer;
      this.title = title;
      this.body = body;
      this.duration = duration;
      this.timeOfCreation = System.currentTimeMillis();
      this.type = type;
      this.width = Math.max(fontRenderer.getWidth(title), fontRenderer.getWidth(body)) + 16.0 + 6.0;
      this.height = 24.0;
   }

   public void render() {
      long timeExisted = System.currentTimeMillis() - this.timeOfCreation;
      double progress = DrawUtil.bezierBlendAnimation((double)timeExisted / (double)this.duration);
      long fadeInOutDuration = 100L;
      double animationTranslate = timeExisted < 100L ? DrawUtil.bezierBlendAnimation((double)timeExisted / 100.0) * this.width : (timeExisted > this.duration - 100L ? DrawUtil.bezierBlendAnimation(((double)this.duration - (double)timeExisted) / 100.0) * this.width : this.width);
      GL11.glTranslated(-animationTranslate, 0.0, 0.0);
      DrawUtil.glDrawFilledQuad(0.0, 0.0, this.width, this.height, Integer.MIN_VALUE);
      int colour = this.type.getColour();
      DrawUtil.glDrawFilledQuad(0.0, this.height - 2.0, this.width, 2.0, ColourUtil.darker(colour));
      DrawUtil.glDrawFilledQuad(0.0, this.height - 2.0, this.width * progress, 2.0, colour);
      this.type.getImage().draw(2.0, (this.height - 16.0) / 2.0, 16.0, 16.0, colour);
      double availableHeight = this.height - 2.0;
      this.fontRenderer.draw(this.title, 20.0, availableHeight / 2.0 - this.fontRenderer.getHeight(this.title), -1);
      this.fontRenderer.draw(this.body, 20.0, availableHeight / 2.0, -5592406);
      GL11.glTranslated(animationTranslate, 0.0, 0.0);
   }

   public boolean isDead() {
      return System.currentTimeMillis() > this.timeOfCreation + this.duration;
   }

   public double getWidth() {
      return this.width;
   }

   public double getHeight() {
      return this.height;
   }
}
