package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import io.github.nevalackin.client.util.render.BlurUtil;
import io.github.nevalackin.client.util.render.DrawUtil;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

public class GuiNewChat extends Gui {
   private static final Logger logger = LogManager.getLogger();
   private final Minecraft mc;
   private final List sentMessages = Lists.newArrayList();
   private final List chatLines = Lists.newArrayList();
   private final List lines = Lists.newArrayList();
   private int scrollPos;
   private boolean isScrolled;
   private int calculatedHeight;

   public GuiNewChat(Minecraft mcIn) {
      this.mc = mcIn;
   }

   public void drawChat(ScaledResolution scaledResolution, int updateCounter) {
      if (this.mc.gameSettings.chatVisibility != EntityPlayer.EnumChatVisibility.HIDDEN) {
         int lineCount = this.getLineCount();
         int nLines = this.lines.size();
         float opacity = this.mc.gameSettings.chatOpacity;
         boolean chatOpen = this.getChatOpen();
         float scale = this.getChatScale();
         if (nLines > 0) {
            GL11.glPushMatrix();
            GL11.glTranslatef(2.0F, (float)(scaledResolution.getScaledHeight() - (chatOpen ? 16 : 2)), 0.0F);
            GL11.glScalef(scale, scale, 1.0F);
            GL11.glEnable(3042);
            GL11.glBlendFunc(770, 771);
            int chatHeight = this.getChatHeight();
            int chatWidth = this.getChatWidth();
            if (chatOpen) {
               BlurUtil.blurArea(2.0, (double)((float)(scaledResolution.getScaledHeight() - 16) + (float)(-chatHeight - 2) * scale), (double)((float)(chatWidth + 6) * scale), (double)((float)(chatHeight + 2) * scale));
               DrawUtil.glDrawFilledQuad(0.0, (double)(-chatHeight - 2), (double)(chatWidth + 6), (double)(chatHeight + 2), (int)(opacity * 255.0F) << 24);
            } else if (this.calculatedHeight > 0) {
               BlurUtil.blurArea(2.0, (double)((float)(scaledResolution.getScaledHeight() - 2) + (float)(-this.calculatedHeight - 2) * scale), (double)((float)(chatWidth + 6) * scale), (double)((float)(this.calculatedHeight + 2) * scale));
               DrawUtil.glDrawFilledQuad(0.0, (double)(-this.calculatedHeight - 2), (double)(chatWidth + 6), (double)(this.calculatedHeight + 2), (int)(opacity * 255.0F) << 24);
            }

            int visibleDuration = true;
            int fadeInDelay = true;
            int fadeOutDelay = true;
            this.calculatedHeight = 0;

            for(int i = 0; i + this.scrollPos < nLines; ++i) {
               int yOffset = (i + 1) * 9;
               if (yOffset > chatHeight) {
                  break;
               }

               ChatLine chatline = (ChatLine)this.lines.get(i + this.scrollPos);
               int ticksExisted = updateCounter - chatline.getUpdatedCounter();
               if (!chatOpen && ticksExisted > 190) {
                  break;
               }

               int colour = 16777215;
               if (chatOpen) {
                  colour |= -16777216;
               } else if (ticksExisted <= 10) {
                  colour += ticksExisted * 25 << 24;
               } else if (ticksExisted <= 160) {
                  colour |= -16777216;
               } else {
                  float alpha = (float)(ticksExisted - 160) / 30.0F;
                  int inv = (int)((1.0F - alpha) * 255.0F);
                  if (inv < 32) {
                     break;
                  }

                  colour += inv << 24;
               }

               this.calculatedHeight += 9;
               this.mc.fontRendererObj.drawStringWithShadow(chatline.getChatComponent().getFormattedText(), 2.0F, (float)(-yOffset), colour);
            }

            GL11.glPopMatrix();
         }
      }

   }

   public void clearChatMessages() {
      this.lines.clear();
      this.chatLines.clear();
      this.sentMessages.clear();
   }

   public void printChatMessage(IChatComponent p_146227_1_) {
      this.printChatMessageWithOptionalDeletion(p_146227_1_, 0);
   }

   public void printChatMessageWithOptionalDeletion(IChatComponent chatComponent, int chatLineId) {
      this.setChatLine(chatComponent, chatLineId, this.mc.ingameGUI.getUpdateCounter(), false);
      if (!chatComponent.getUnformattedText().contains("${")) {
         logger.info("[CHAT] " + chatComponent.getUnformattedText());
      }
   }

   private void setChatLine(IChatComponent p_146237_1_, int p_146237_2_, int p_146237_3_, boolean p_146237_4_) {
      if (p_146237_2_ != 0) {
         this.deleteChatLine(p_146237_2_);
      }

      int i = MathHelper.floor_float((float)this.getChatWidth() / this.getChatScale());
      List list = GuiUtilRenderComponents.func_178908_a(p_146237_1_, i, this.mc.fontRendererObj, false, false);
      boolean flag = this.getChatOpen();

      IChatComponent ichatcomponent;
      for(Iterator var8 = list.iterator(); var8.hasNext(); this.lines.add(0, new ChatLine(p_146237_3_, ichatcomponent, p_146237_2_))) {
         ichatcomponent = (IChatComponent)var8.next();
         if (flag && this.scrollPos > 0) {
            this.isScrolled = true;
            this.scroll(1);
         }
      }

      while(this.lines.size() > 100) {
         this.lines.remove(this.lines.size() - 1);
      }

      if (!p_146237_4_) {
         this.chatLines.add(0, new ChatLine(p_146237_3_, p_146237_1_, p_146237_2_));

         while(this.chatLines.size() > 100) {
            this.chatLines.remove(this.chatLines.size() - 1);
         }
      }

   }

   public void refreshChat() {
      this.lines.clear();
      this.resetScroll();

      for(int i = this.chatLines.size() - 1; i >= 0; --i) {
         ChatLine chatline = (ChatLine)this.chatLines.get(i);
         this.setChatLine(chatline.getChatComponent(), chatline.getChatLineID(), chatline.getUpdatedCounter(), true);
      }

   }

   public List getSentMessages() {
      return this.sentMessages;
   }

   public void addToSentMessages(String p_146239_1_) {
      if (this.sentMessages.isEmpty() || !((String)this.sentMessages.get(this.sentMessages.size() - 1)).equals(p_146239_1_)) {
         this.sentMessages.add(p_146239_1_);
      }

   }

   public void resetScroll() {
      this.scrollPos = 0;
      this.isScrolled = false;
   }

   public void scroll(int p_146229_1_) {
      this.scrollPos += p_146229_1_;
      int i = this.lines.size();
      if (this.scrollPos > i - this.getLineCount()) {
         this.scrollPos = i - this.getLineCount();
      }

      if (this.scrollPos <= 0) {
         this.scrollPos = 0;
         this.isScrolled = false;
      }

   }

   public IChatComponent getChatComponent(int mouseX, int mouseY) {
      if (this.getChatOpen()) {
         ScaledResolution scaledResolution = new ScaledResolution(this.mc);
         float scale = this.getChatScale();
         float textHeight = (float)this.mc.fontRendererObj.FONT_HEIGHT * scale;
         if (mouseX > 2 && (float)mouseX < 2.0F + (float)this.getChatWidth() * scale && mouseY < scaledResolution.getScaledHeight() - 16 && (float)mouseY > (float)(scaledResolution.getScaledHeight() - 16) - (float)this.getChatHeight() * scale) {
            int mouseIndexOffWithScroll = (int)((float)(scaledResolution.getScaledHeight() - 16 - mouseY) / textHeight) + this.scrollPos;
            if (mouseIndexOffWithScroll >= 0 && mouseIndexOffWithScroll < this.lines.size()) {
               ChatLine hoveredLine = (ChatLine)this.lines.get(mouseIndexOffWithScroll);
               int textXOffset = 0;
               Iterator var9 = hoveredLine.getChatComponent().iterator();

               while(var9.hasNext()) {
                  IChatComponent ichatcomponent = (IChatComponent)var9.next();
                  if (ichatcomponent instanceof ChatComponentText) {
                     textXOffset += this.mc.fontRendererObj.getStringWidth(GuiUtilRenderComponents.func_178909_a(((ChatComponentText)ichatcomponent).getChatComponentText_TextValue(), false));
                     if (textXOffset > mouseX) {
                        return ichatcomponent;
                     }
                  }
               }
            }
         }
      }

      return null;
   }

   public boolean getChatOpen() {
      return this.mc.currentScreen instanceof GuiChat;
   }

   public void deleteChatLine(int p_146242_1_) {
      Iterator iterator = this.lines.iterator();

      ChatLine chatline1;
      while(iterator.hasNext()) {
         chatline1 = (ChatLine)iterator.next();
         if (chatline1.getChatLineID() == p_146242_1_) {
            iterator.remove();
         }
      }

      iterator = this.chatLines.iterator();

      while(iterator.hasNext()) {
         chatline1 = (ChatLine)iterator.next();
         if (chatline1.getChatLineID() == p_146242_1_) {
            iterator.remove();
            break;
         }
      }

   }

   public int getChatWidth() {
      return calculateChatboxWidth(this.mc.gameSettings.chatWidth);
   }

   public int getChatHeight() {
      return calculateChatboxHeight(this.getChatOpen() ? this.mc.gameSettings.chatHeightFocused : this.mc.gameSettings.chatHeightUnfocused);
   }

   public float getChatScale() {
      return this.mc.gameSettings.chatScale;
   }

   public static int calculateChatboxWidth(float p_146233_0_) {
      int i = 320;
      int j = 40;
      return MathHelper.floor_float(p_146233_0_ * (float)(i - j) + (float)j);
   }

   public static int calculateChatboxHeight(float p_146243_0_) {
      int i = 180;
      int j = 20;
      return MathHelper.floor_float(p_146243_0_ * (float)(i - j) + (float)j);
   }

   public int getLineCount() {
      return this.getChatHeight() / 9;
   }
}
