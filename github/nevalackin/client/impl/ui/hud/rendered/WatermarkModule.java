package io.github.nevalackin.client.impl.ui.hud.rendered;

import io.github.nevalackin.client.api.module.Category;
import io.github.nevalackin.client.api.module.Module;
import io.github.nevalackin.client.api.property.Property;
import io.github.nevalackin.client.api.ui.cfont.CustomFontRenderer;
import io.github.nevalackin.client.impl.core.KetamineClient;
import io.github.nevalackin.client.impl.event.game.GetTimerSpeedEvent;
import io.github.nevalackin.client.impl.module.misc.player.StaffAnalyzer;
import io.github.nevalackin.client.impl.property.ColourProperty;
import io.github.nevalackin.client.impl.property.EnumProperty;
import io.github.nevalackin.client.impl.ui.hud.components.HudComponent;
import io.github.nevalackin.client.util.render.BloomUtil;
import io.github.nevalackin.client.util.render.ColourUtil;
import io.github.nevalackin.client.util.render.DrawUtil;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class WatermarkModule extends Module implements HudComponent {
   private double xPos;
   private double yPos;
   private double width;
   private double height;
   private boolean dragon;
   private final EnumProperty colourProperty;
   public final ColourProperty startColourProperty;
   public final ColourProperty endColourProperty;
   private final DateFormat format;
   @EventLink
   private final Listener onRenderOverlay;

   public WatermarkModule() {
      super("Watermark", Category.RENDER, Category.SubCategory.RENDER_OVERLAY);
      this.colourProperty = new EnumProperty("Colour", WatermarkModule.Colour.CLIENT);
      this.startColourProperty = new ColourProperty("Start Colour", -393028, this::isBlend);
      this.endColourProperty = new ColourProperty("End Colour", -16718593, this::isBlend);
      this.format = new SimpleDateFormat("kk:mm:ss");
      this.onRenderOverlay = (event) -> {
         this.render(event.getScaledResolution().getScaledWidth(), event.getScaledResolution().getScaledHeight(), (double)event.getPartialTicks());
      };
      this.register(new Property[]{this.colourProperty, this.startColourProperty, this.endColourProperty});
      this.setX(4.0);
      this.setY(4.0);
      this.setEnabled(true);
   }

   private boolean isBlend() {
      return this.colourProperty.getValue() == WatermarkModule.Colour.BLEND;
   }

   public boolean isDragging() {
      return this.dragon;
   }

   public void setDragging(boolean dragging) {
      this.dragon = dragging;
   }

   public void render(int scaledWidth, int scaledHeight, double tickDelta) {
      this.fitInScreen(scaledWidth, scaledHeight);
      double x = this.getX();
      double y = this.getY();
      double margin = 2.0;
      CustomFontRenderer fontRenderer = KetamineClient.getInstance().getFontRenderer();
      String playTime = "";
      long totalSecs = (System.currentTimeMillis() - KetamineClient.getInstance().getStartTime()) / 1000L;
      long secs = totalSecs % 60L;
      long mins = totalSecs / 60L % 60L;
      long hours = totalSecs / 60L / 60L % 24L;
      long days = totalSecs / 60L / 60L / 24L;

      try {
         if (days > 0L) {
            playTime = playTime + days + "d ";
         }

         if (hours > 0L) {
            playTime = playTime + hours + "h ";
         }

         if (mins > 0L) {
            playTime = playTime + mins + "m ";
         }

         if (secs > 0L) {
            playTime = playTime + secs + "s ";
         }

         if (playTime.endsWith(" ")) {
            playTime = playTime.substring(0, playTime.length() - 1);
         }
      } catch (Exception var40) {
      }

      String name = KetamineClient.getInstance().getName();
      String serverName = this.mc.getCurrentServerData().serverIP.toLowerCase();
      String bps = String.format("%.1f b/ps", Math.hypot(this.mc.thePlayer.posX - this.mc.thePlayer.prevPosX, this.mc.thePlayer.posZ - this.mc.thePlayer.prevPosZ) * GetTimerSpeedEvent.lastTimerSpeed * 20.0);
      String banCount = StaffAnalyzer.totalBans + " Bans";
      String text = String.format("%s | %s | %s | %s | %s", name, serverName, bps, playTime, banCount);
      int startColour = ((Colour)this.colourProperty.getValue()).getModColour((Integer)this.startColourProperty.getValue(), (Integer)this.endColourProperty.getValue(), 0);
      int endColour = ((Colour)this.colourProperty.getValue()).getModColour((Integer)this.endColourProperty.getValue(), (Integer)this.startColourProperty.getValue(), 250);
      double bounds = fontRenderer.getWidth(text);
      double heightBounds = fontRenderer.getHeight(text);
      double thickness = 2.0;
      double width = bounds + margin * 2.0;
      double height = heightBounds + margin * 2.0;
      DrawUtil.glDrawFilledQuad(x, y + thickness, width, height, Integer.MIN_VALUE);
      BloomUtil.drawAndBloom(() -> {
         DrawUtil.glDrawSidewaysGradientRect(x, y + thickness - 1.0, width, 1.0, startColour, endColour);
      });
      fontRenderer.drawWithShadow(text, x + margin, y + margin + thickness, 0.5, -1);
      this.width = width;
      this.height = height + thickness;
   }

   public void onEnable() {
   }

   public void onDisable() {
   }

   public void setX(double x) {
      this.xPos = x;
   }

   public void setY(double y) {
      this.yPos = y;
   }

   public double getX() {
      return this.xPos;
   }

   public double getY() {
      return this.yPos;
   }

   public double setWidth(double width) {
      return this.width = width;
   }

   public double setHeight(double height) {
      return this.height = height;
   }

   public double getWidth() {
      return this.width;
   }

   public double getHeight() {
      return this.height;
   }

   public boolean isVisible() {
      return true;
   }

   public void setVisible(boolean visible) {
   }

   private static enum Colour {
      BLEND("Blend", (startColour, endColour, index) -> {
         return ColourUtil.fadeBetween(startColour, endColour, (long)index * 150L);
      }),
      CLIENT("Client Colour", (startColour, endColour, index) -> {
         return ColourUtil.fadeBetween(ColourUtil.getClientColour(), ColourUtil.getSecondaryColour(), (long)index * 150L);
      });

      private final String name;
      private final ModColourFunc modColourFunc;

      private Colour(String name, ModColourFunc modColourFunc) {
         this.name = name;
         this.modColourFunc = modColourFunc;
      }

      public int getModColour(int startColour, int endColour, int index) {
         return this.modColourFunc.getColour(startColour, endColour, index);
      }

      public String toString() {
         return this.name;
      }
   }

   @FunctionalInterface
   private interface ModColourFunc {
      int getColour(int var1, int var2, int var3);
   }
}
