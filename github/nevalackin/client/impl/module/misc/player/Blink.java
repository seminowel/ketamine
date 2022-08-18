package io.github.nevalackin.client.impl.module.misc.player;

import io.github.nevalackin.client.api.module.Category;
import io.github.nevalackin.client.api.module.Module;
import io.github.nevalackin.client.api.property.Property;
import io.github.nevalackin.client.impl.property.DoubleProperty;
import io.github.nevalackin.client.util.render.DrawUtil;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S18PacketEntityTeleport;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

public final class Blink extends Module {
   private final DoubleProperty maxPacketsProperty = new DoubleProperty("Max Packets", 80.0, 0.0, 200.0, 1.0);
   private final List packets = new ArrayList();
   private Vec3 lastLocation;
   private int clientUpdatePositionPackets;
   @EventLink
   private final Listener onSendPacket = (event) -> {
      this.packets.add(event.getPacket());
      event.setCancelled();
   };
   @EventLink
   private final Listener onReceivePacket = (event) -> {
      Packet packet = event.getPacket();
      if (packet instanceof S08PacketPlayerPosLook) {
         S08PacketPlayerPosLook posLook = (S08PacketPlayerPosLook)packet;
         this.lastLocation = new Vec3(posLook.getX(), posLook.getY(), posLook.getZ());
         this.packets.clear();
      } else if (packet instanceof S18PacketEntityTeleport) {
         S18PacketEntityTeleport teleport = (S18PacketEntityTeleport)packet;
         if (teleport.getEntityId() == this.mc.thePlayer.getEntityId()) {
            this.lastLocation = new Vec3((double)teleport.getX(), (double)teleport.getY(), (double)teleport.getZ());
            this.packets.clear();
         }
      }

   };
   @EventLink
   private final Listener onUpdatePosition = (event) -> {
      if (event.isPre() && this.isPulseBlink()) {
         if (this.clientUpdatePositionPackets > ((Double)this.maxPacketsProperty.getValue()).intValue()) {
            while(!this.packets.isEmpty()) {
               this.mc.thePlayer.sendQueue.sendPacketDirect((Packet)this.packets.remove(0));
            }

            this.clientUpdatePositionPackets = 0;
            this.lastLocation = this.mc.thePlayer.getPositionVector();
         }

         ++this.clientUpdatePositionPackets;
      }

   };
   @EventLink
   private final Listener onRenderGameOverlay = (event) -> {
      if (this.isPulseBlink()) {
      }

   };
   @EventLink
   private final Listener onRender3D = (event) -> {
      GL11.glDisable(3553);
      GL11.glDisable(2884);
      GL11.glDepthMask(false);
      GL11.glDisable(2929);
      GL11.glEnable(2848);
      GL11.glHint(3154, 4354);
      GL11.glLineWidth(2.5F);
      boolean restore = DrawUtil.glEnableBlend();
      GL11.glTranslated(this.lastLocation.xCoord, this.lastLocation.yCoord, this.lastLocation.zCoord);
      DrawUtil.glColour(-2130706433);
      GL11.glBegin(9);
      addCircleVertices(20, 0.6);
      GL11.glEnd();
      DrawUtil.glColour(-1);
      GL11.glBegin(2);
      addCircleVertices(20, 0.6);
      GL11.glEnd();
      GL11.glTranslated(-this.lastLocation.xCoord, -this.lastLocation.yCoord, -this.lastLocation.zCoord);
      DrawUtil.glRestoreBlend(restore);
      GL11.glDepthMask(true);
      GL11.glEnable(2929);
      GL11.glEnable(2884);
      GL11.glDisable(2848);
      GL11.glHint(3154, 4352);
      GL11.glEnable(3553);
   };

   public Blink() {
      super("Blink", Category.MISC, Category.SubCategory.MISC_PLAYER);
      this.maxPacketsProperty.addValueAlias(0.0, "Unlimited");
      this.setSuffix(() -> {
         return String.valueOf(this.clientUpdatePositionPackets);
      });
      this.register(new Property[]{this.maxPacketsProperty});
   }

   private boolean isPulseBlink() {
      return (Double)this.maxPacketsProperty.getValue() != 0.0;
   }

   private static void addCircleVertices(int points, double radius) {
      for(int i = 0; i <= points; ++i) {
         double delta = (double)i * Math.PI * 2.0 / (double)points;
         GL11.glVertex3d(radius * Math.cos(delta), 0.0, radius * Math.sin(delta));
      }

   }

   public void onEnable() {
      if (this.mc.thePlayer == null) {
         this.setEnabled(false);
      } else {
         this.lastLocation = this.mc.thePlayer.getPositionVector();
      }
   }

   public void onDisable() {
      if (this.mc.thePlayer != null) {
         while(!this.packets.isEmpty()) {
            this.mc.thePlayer.sendQueue.sendPacketDirect((Packet)this.packets.remove(0));
         }
      }

   }
}
