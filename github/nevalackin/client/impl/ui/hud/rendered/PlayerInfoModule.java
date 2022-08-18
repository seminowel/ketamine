package io.github.nevalackin.client.impl.ui.hud.rendered;

import io.github.nevalackin.client.api.module.Category;
import io.github.nevalackin.client.api.module.Module;
import io.github.nevalackin.client.api.property.Property;
import io.github.nevalackin.client.impl.property.BooleanProperty;
import io.github.nevalackin.client.impl.ui.hud.components.HudComponent;
import io.github.nevalackin.client.impl.ui.hud.rendered.graphicbox.GraphicBoxField;
import io.github.nevalackin.client.impl.ui.hud.rendered.graphicbox.GraphicBoxUtils;
import io.github.nevalackin.client.util.render.ColourUtil;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;

public class PlayerInfoModule extends Module implements HudComponent {
   private double xPos;
   private double yPos;
   private double width;
   private double height;
   private boolean dragon;
   private final BooleanProperty hideNonTabListProperty = new BooleanProperty("Hide NPCs", false);
   @EventLink
   private final Listener onRenderOverlay = (event) -> {
      this.render(event.getScaledResolution().getScaledWidth(), event.getScaledResolution().getScaledHeight(), (double)event.getPartialTicks());
   };
   private static final float BLOCK_TO_FT = 3.28084F;
   private static final List FIELDS = Arrays.asList(new GraphicBoxField("Player", (player) -> {
      return player.getGameProfile().getName();
   }, (player) -> {
      return -1;
   }), new GraphicBoxField("HP", (player) -> {
      return String.format("%.1f%%", player.getHealth() / player.getMaxHealth() * 100.0F);
   }, (player) -> {
      return ColourUtil.blendHealthColours((double)(player.getHealth() / player.getMaxHealth()));
   }), new GraphicBoxField("Dist", (player) -> {
      Minecraft client = Minecraft.getMinecraft();
      return !(player instanceof EntityPlayerSP) && client.thePlayer != null ? String.format("%d ft", Math.round(client.thePlayer.getDistanceToEntity(player) * 3.28084F)) : "0 ft";
   }, (player) -> {
      return -1;
   }), new GraphicBoxField("Armor", (player) -> {
      return String.valueOf(player.getTotalArmorValue());
   }, (player) -> {
      Minecraft client = Minecraft.getMinecraft();
      return !(player instanceof EntityPlayerSP) && client.thePlayer != null && client.thePlayer.getTotalArmorValue() != player.getTotalArmorValue() ? client.thePlayer.getTotalArmorValue() > player.getTotalArmorValue() ? -16711847 : Short.MIN_VALUE : -256;
   }), new GraphicBoxField("Priority", (player) -> {
      return player instanceof EntityPlayerSP ? "LocalPlayer" : "Auto";
   }, (player) -> {
      return player instanceof EntityPlayerSP ? -256 : -1;
   }));

   public PlayerInfoModule() {
      super("Player Info", Category.RENDER, Category.SubCategory.RENDER_OVERLAY);
      this.register(new Property[]{this.hideNonTabListProperty});
      this.setX(4.0);
      this.setY(22.0);
   }

   public void render(int scaledWidth, int scaledHeight, double tickDelta) {
      this.fitInScreen(scaledWidth, scaledHeight);
      if (this.mc.theWorld != null) {
         List playerEntities = this.mc.theWorld.playerEntities;
         float[] dimensions = GraphicBoxUtils.drawGraphicBox(FIELDS, (Boolean)this.hideNonTabListProperty.getValue() ? (List)playerEntities.stream().filter(this::isOnTab).filter((entityPlayer) -> {
            return !entityPlayer.getDisplayName().getFormattedText().startsWith("§8[NPC]");
         }).collect(Collectors.toList()) : playerEntities, (float)this.getX(), (float)this.getY());
         this.setWidth((double)dimensions[0]);
         this.setHeight((double)dimensions[1]);
      }
   }

   private boolean isOnTab(Entity entity) {
      Iterator var2 = this.mc.getNetHandler().getPlayerInfoMap().iterator();

      NetworkPlayerInfo info;
      do {
         if (!var2.hasNext()) {
            return false;
         }

         info = (NetworkPlayerInfo)var2.next();
      } while(!info.getGameProfile().getName().equals(entity.getName()));

      return true;
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

   public boolean isDragging() {
      return this.dragon;
   }

   public void setDragging(boolean dragging) {
      this.dragon = dragging;
   }

   public boolean isVisible() {
      return true;
   }

   public void setVisible(boolean visible) {
   }
}
