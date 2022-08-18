package net.minecraft.client.gui.spectator.categories;

import com.google.common.collect.Lists;
import io.github.nevalackin.client.util.render.DrawUtil;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiSpectator;
import net.minecraft.client.gui.spectator.ISpectatorMenuObject;
import net.minecraft.client.gui.spectator.ISpectatorMenuView;
import net.minecraft.client.gui.spectator.SpectatorMenu;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class TeleportToTeam implements ISpectatorMenuView, ISpectatorMenuObject {
   private final List field_178672_a = Lists.newArrayList();

   public TeleportToTeam() {
      Minecraft minecraft = Minecraft.getMinecraft();
      Iterator var2 = minecraft.theWorld.getScoreboard().getTeams().iterator();

      while(var2.hasNext()) {
         ScorePlayerTeam scoreplayerteam = (ScorePlayerTeam)var2.next();
         this.field_178672_a.add(new TeamSelectionObject(scoreplayerteam));
      }

   }

   public List func_178669_a() {
      return this.field_178672_a;
   }

   public IChatComponent func_178670_b() {
      return new ChatComponentText("Select a team to teleport to");
   }

   public void func_178661_a(SpectatorMenu menu) {
      menu.func_178647_a(this);
   }

   public IChatComponent getSpectatorName() {
      return new ChatComponentText("Teleport to team member");
   }

   public void func_178663_a(float p_178663_1_, int alpha) {
      Minecraft.getMinecraft().getTextureManager().bindTexture(GuiSpectator.field_175269_a);
      Gui.drawModalRectWithCustomSizedTexture(0, 0, 16.0F, 0.0F, 16, 16, 256.0F, 256.0F);
   }

   public boolean func_178662_A_() {
      Iterator var1 = this.field_178672_a.iterator();

      ISpectatorMenuObject ispectatormenuobject;
      do {
         if (!var1.hasNext()) {
            return false;
         }

         ispectatormenuobject = (ISpectatorMenuObject)var1.next();
      } while(!ispectatormenuobject.func_178662_A_());

      return true;
   }

   class TeamSelectionObject implements ISpectatorMenuObject {
      private final ScorePlayerTeam field_178676_b;
      private final ResourceLocation field_178677_c;
      private final List field_178675_d;

      public TeamSelectionObject(ScorePlayerTeam p_i45492_2_) {
         this.field_178676_b = p_i45492_2_;
         this.field_178675_d = Lists.newArrayList();
         Iterator var3 = p_i45492_2_.getMembershipCollection().iterator();

         while(var3.hasNext()) {
            String s = (String)var3.next();
            NetworkPlayerInfo networkplayerinfo = Minecraft.getMinecraft().getNetHandler().getPlayerInfo(s);
            if (networkplayerinfo != null) {
               this.field_178675_d.add(networkplayerinfo);
            }
         }

         if (!this.field_178675_d.isEmpty()) {
            String s1 = ((NetworkPlayerInfo)this.field_178675_d.get((new Random()).nextInt(this.field_178675_d.size()))).getGameProfile().getName();
            this.field_178677_c = AbstractClientPlayer.getLocationSkin(s1);
            AbstractClientPlayer.getDownloadImageSkin(this.field_178677_c, s1);
         } else {
            this.field_178677_c = DefaultPlayerSkin.getDefaultSkinLegacy();
         }

      }

      public void func_178661_a(SpectatorMenu menu) {
         menu.func_178647_a(new TeleportToPlayer(this.field_178675_d));
      }

      public IChatComponent getSpectatorName() {
         return new ChatComponentText(this.field_178676_b.getTeamName());
      }

      public void func_178663_a(float p_178663_1_, int alpha) {
         int i = -1;
         String s = FontRenderer.getFormatFromString(this.field_178676_b.getColorPrefix());
         if (s.length() >= 2) {
            i = Minecraft.getMinecraft().fontRendererObj.getColorCode(s.charAt(1));
         }

         if (i >= 0) {
            float f = (float)(i >> 16 & 255) / 255.0F;
            float f1 = (float)(i >> 8 & 255) / 255.0F;
            float f2 = (float)(i & 255) / 255.0F;
            DrawUtil.glDrawFilledRect(1.0, 1.0, 15.0, 15.0, MathHelper.func_180183_b(f * p_178663_1_, f1 * p_178663_1_, f2 * p_178663_1_) | alpha << 24);
         }

         Minecraft.getMinecraft().getTextureManager().bindTexture(this.field_178677_c);
         GL11.glColor4f(p_178663_1_, p_178663_1_, p_178663_1_, (float)alpha / 255.0F);
         Gui.drawScaledCustomSizeModalRect(2.0, 2.0, 8.0F, 8.0F, 8, 8, 12.0, 12.0, 64.0F, 64.0F);
         Gui.drawScaledCustomSizeModalRect(2.0, 2.0, 40.0F, 8.0F, 8, 8, 12.0, 12.0, 64.0F, 64.0F);
      }

      public boolean func_178662_A_() {
         return !this.field_178675_d.isEmpty();
      }
   }
}
