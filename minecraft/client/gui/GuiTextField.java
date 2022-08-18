package net.minecraft.client.gui;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import io.github.nevalackin.client.util.render.ColourUtil;
import io.github.nevalackin.client.util.render.DrawUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

public class GuiTextField extends Gui {
   private final int id;
   private final FontRenderer fontRendererInstance;
   public int xPosition;
   public int yPosition;
   private final int width;
   private final int height;
   private String text;
   private int maxStringLength;
   private int cursorCounter;
   private boolean enableBackgroundDrawing;
   private boolean canLoseFocus;
   private boolean isFocused;
   private boolean isEnabled;
   private int lineScrollOffset;
   private int cursorPosition;
   private int selectionEnd;
   private int enabledColor;
   private int disabledColor;
   private boolean visible;
   private GuiPageButtonList.GuiResponder field_175210_x;
   private Predicate field_175209_y;
   private boolean hideText;

   public GuiTextField(int componentId, FontRenderer fontrendererObj, int x, int y, int par5Width, int par6Height) {
      this.text = "";
      this.maxStringLength = 32;
      this.enableBackgroundDrawing = true;
      this.canLoseFocus = true;
      this.isEnabled = true;
      this.enabledColor = 14737632;
      this.disabledColor = 7368816;
      this.visible = true;
      this.field_175209_y = Predicates.alwaysTrue();
      this.hideText = false;
      this.id = componentId;
      this.fontRendererInstance = fontrendererObj;
      this.xPosition = x;
      this.yPosition = y;
      this.width = par5Width;
      this.height = par6Height;
   }

   public GuiTextField(int componentId, FontRenderer fontrendererObj, int x, int y, int par5Width, int par6Height, boolean hideText) {
      this(componentId, fontrendererObj, x, y, par5Width, par6Height);
      this.hideText = hideText;
   }

   public void func_175207_a(GuiPageButtonList.GuiResponder p_175207_1_) {
      this.field_175210_x = p_175207_1_;
   }

   public void updateCursorCounter() {
      ++this.cursorCounter;
   }

   public void setText(String p_146180_1_) {
      if (this.field_175209_y.apply(p_146180_1_)) {
         if (p_146180_1_.length() > this.maxStringLength) {
            this.text = p_146180_1_.substring(0, this.maxStringLength);
         } else {
            this.text = p_146180_1_;
         }

         this.setCursorPositionEnd();
      }

   }

   public String getText() {
      return this.text;
   }

   public String getSelectedText() {
      int i = this.cursorPosition < this.selectionEnd ? this.cursorPosition : this.selectionEnd;
      int j = this.cursorPosition < this.selectionEnd ? this.selectionEnd : this.cursorPosition;
      return this.text.substring(i, j);
   }

   public void func_175205_a(Predicate p_175205_1_) {
      this.field_175209_y = p_175205_1_;
   }

   public void writeText(String p_146191_1_) {
      String s = "";
      String s1 = ChatAllowedCharacters.filterAllowedCharacters(p_146191_1_);
      int i = this.cursorPosition < this.selectionEnd ? this.cursorPosition : this.selectionEnd;
      int j = this.cursorPosition < this.selectionEnd ? this.selectionEnd : this.cursorPosition;
      int k = this.maxStringLength - this.text.length() - (i - j);
      int l = false;
      if (this.text.length() > 0) {
         s = s + this.text.substring(0, i);
      }

      int l;
      if (k < s1.length()) {
         s = s + s1.substring(0, k);
         l = k;
      } else {
         s = s + s1;
         l = s1.length();
      }

      if (this.text.length() > 0 && j < this.text.length()) {
         s = s + this.text.substring(j);
      }

      if (this.field_175209_y.apply(s)) {
         this.text = s;
         this.moveCursorBy(i - this.selectionEnd + l);
         if (this.field_175210_x != null) {
            this.field_175210_x.func_175319_a(this.id, this.text);
         }
      }

   }

   public void deleteWords(int p_146177_1_) {
      if (this.text.length() != 0) {
         if (this.selectionEnd != this.cursorPosition) {
            this.writeText("");
         } else {
            this.deleteFromCursor(this.getNthWordFromCursor(p_146177_1_) - this.cursorPosition);
         }
      }

   }

   public void deleteFromCursor(int p_146175_1_) {
      if (this.text.length() != 0) {
         if (this.selectionEnd != this.cursorPosition) {
            this.writeText("");
         } else {
            boolean flag = p_146175_1_ < 0;
            int i = flag ? this.cursorPosition + p_146175_1_ : this.cursorPosition;
            int j = flag ? this.cursorPosition : this.cursorPosition + p_146175_1_;
            String s = "";
            if (i >= 0) {
               s = this.text.substring(0, i);
            }

            if (j < this.text.length()) {
               s = s + this.text.substring(j);
            }

            if (this.field_175209_y.apply(s)) {
               this.text = s;
               if (flag) {
                  this.moveCursorBy(p_146175_1_);
               }

               if (this.field_175210_x != null) {
                  this.field_175210_x.func_175319_a(this.id, this.text);
               }
            }
         }
      }

   }

   public int getId() {
      return this.id;
   }

   public int getNthWordFromCursor(int p_146187_1_) {
      return this.getNthWordFromPos(p_146187_1_, this.getCursorPosition());
   }

   public int getNthWordFromPos(int p_146183_1_, int p_146183_2_) {
      return this.func_146197_a(p_146183_1_, p_146183_2_, true);
   }

   public int func_146197_a(int p_146197_1_, int p_146197_2_, boolean p_146197_3_) {
      int i = p_146197_2_;
      boolean flag = p_146197_1_ < 0;
      int j = Math.abs(p_146197_1_);

      for(int k = 0; k < j; ++k) {
         if (!flag) {
            int l = this.text.length();
            i = this.text.indexOf(32, i);
            if (i == -1) {
               i = l;
            } else {
               while(p_146197_3_ && i < l && this.text.charAt(i) == ' ') {
                  ++i;
               }
            }
         } else {
            while(p_146197_3_ && i > 0 && this.text.charAt(i - 1) == ' ') {
               --i;
            }

            while(i > 0 && this.text.charAt(i - 1) != ' ') {
               --i;
            }
         }
      }

      return i;
   }

   public void moveCursorBy(int p_146182_1_) {
      this.setCursorPosition(this.selectionEnd + p_146182_1_);
   }

   public void setCursorPosition(int p_146190_1_) {
      this.cursorPosition = p_146190_1_;
      int i = (this.hideText ? this.getTextHidden() : this.text).length();
      this.cursorPosition = MathHelper.clamp_int(this.cursorPosition, 0, i);
      this.setSelectionPos(this.cursorPosition);
   }

   public void setCursorPositionZero() {
      this.setCursorPosition(0);
   }

   public void setCursorPositionEnd() {
      this.setCursorPosition((this.hideText ? this.getTextHidden() : this.text).length());
   }

   public boolean textboxKeyTyped(char p_146201_1_, int p_146201_2_) {
      if (!this.isFocused) {
         return false;
      } else if (GuiScreen.isKeyComboCtrlA(p_146201_2_)) {
         this.setCursorPositionEnd();
         this.setSelectionPos(0);
         return true;
      } else if (GuiScreen.isKeyComboCtrlC(p_146201_2_)) {
         GuiScreen.setClipboardString(this.getSelectedText());
         return true;
      } else if (GuiScreen.isKeyComboCtrlV(p_146201_2_)) {
         if (this.isEnabled) {
            this.writeText(GuiScreen.getClipboardString());
         }

         return true;
      } else if (GuiScreen.isKeyComboCtrlX(p_146201_2_)) {
         GuiScreen.setClipboardString(this.getSelectedText());
         if (this.isEnabled) {
            this.writeText("");
         }

         return true;
      } else {
         switch (p_146201_2_) {
            case 14:
               if (GuiScreen.isCtrlKeyDown()) {
                  if (this.isEnabled) {
                     this.deleteWords(-1);
                  }
               } else if (this.isEnabled) {
                  this.deleteFromCursor(-1);
               }

               return true;
            case 199:
               if (GuiScreen.isShiftKeyDown()) {
                  this.setSelectionPos(0);
               } else {
                  this.setCursorPositionZero();
               }

               return true;
            case 203:
               if (GuiScreen.isShiftKeyDown()) {
                  if (GuiScreen.isCtrlKeyDown()) {
                     this.setSelectionPos(this.getNthWordFromPos(-1, this.getSelectionEnd()));
                  } else {
                     this.setSelectionPos(this.getSelectionEnd() - 1);
                  }
               } else if (GuiScreen.isCtrlKeyDown()) {
                  this.setCursorPosition(this.getNthWordFromCursor(-1));
               } else {
                  this.moveCursorBy(-1);
               }

               return true;
            case 205:
               if (GuiScreen.isShiftKeyDown()) {
                  if (GuiScreen.isCtrlKeyDown()) {
                     this.setSelectionPos(this.getNthWordFromPos(1, this.getSelectionEnd()));
                  } else {
                     this.setSelectionPos(this.getSelectionEnd() + 1);
                  }
               } else if (GuiScreen.isCtrlKeyDown()) {
                  this.setCursorPosition(this.getNthWordFromCursor(1));
               } else {
                  this.moveCursorBy(1);
               }

               return true;
            case 207:
               if (GuiScreen.isShiftKeyDown()) {
                  this.setSelectionPos((this.hideText ? this.getTextHidden() : this.text).length());
               } else {
                  this.setCursorPositionEnd();
               }

               return true;
            case 211:
               if (GuiScreen.isCtrlKeyDown()) {
                  if (this.isEnabled) {
                     this.deleteWords(1);
                  }
               } else if (this.isEnabled) {
                  this.deleteFromCursor(1);
               }

               return true;
            default:
               if (ChatAllowedCharacters.isAllowedCharacter(p_146201_1_)) {
                  if (this.isEnabled) {
                     this.writeText(Character.toString(p_146201_1_));
                  }

                  return true;
               } else {
                  return false;
               }
         }
      }
   }

   public void mouseClicked(int p_146192_1_, int p_146192_2_, int p_146192_3_) {
      boolean flag = p_146192_1_ >= this.xPosition && p_146192_1_ < this.xPosition + this.width && p_146192_2_ >= this.yPosition && p_146192_2_ < this.yPosition + this.height;
      if (this.canLoseFocus) {
         this.setFocused(flag);
      }

      if (this.isFocused && flag && p_146192_3_ == 0) {
         int i = p_146192_1_ - this.xPosition;
         if (this.enableBackgroundDrawing) {
            i -= 4;
         }

         String s = this.fontRendererInstance.trimStringToWidth(this.hideText ? this.getTextHidden() : this.text.substring(this.lineScrollOffset), this.getWidth());
         this.setCursorPosition(this.fontRendererInstance.trimStringToWidth(s, i).length() + this.lineScrollOffset);
      }

   }

   public void drawTextBox() {
      if (this.getVisible()) {
         if (this.getEnableBackgroundDrawing()) {
            DrawUtil.glDrawFilledRect((double)this.xPosition, (double)this.yPosition, (double)(this.xPosition + this.width), (double)(this.yPosition + this.height), -2146562546);
            DrawUtil.glDrawFilledRect((double)this.xPosition, (double)(this.yPosition + this.height - 1), (double)(this.xPosition + this.width), (double)(this.yPosition + this.height), this.isFocused && this.isEnabled ? ColourUtil.getClientColour() : -12369085);
         }

         int i = this.isEnabled ? this.enabledColor : this.disabledColor;
         int j = this.cursorPosition - this.lineScrollOffset;
         int k = this.selectionEnd - this.lineScrollOffset;
         String s = this.fontRendererInstance.trimStringToWidth(this.hideText ? this.getTextHidden() : this.text.substring(this.lineScrollOffset), this.getWidth());
         boolean flag = j >= 0 && j <= s.length();
         int l = this.enableBackgroundDrawing ? this.xPosition + 4 : this.xPosition;
         int i1 = this.enableBackgroundDrawing ? this.yPosition + (this.height - 8) / 2 : this.yPosition;
         int j1 = l;
         if (k > s.length()) {
            k = s.length();
         }

         if (s.length() > 0) {
            String s1 = flag ? s.substring(0, j) : s;
            j1 = this.fontRendererInstance.drawStringWithShadow(s1, (float)l, (float)i1, i);
         }

         boolean flag2 = this.cursorPosition < (this.hideText ? this.getTextHidden() : this.text).length() || (this.hideText ? this.getTextHidden() : this.text).length() >= this.getMaxStringLength();
         int k1 = j1;
         if (!flag) {
            k1 = j > 0 ? l + this.width : l;
         } else if (flag2) {
            k1 = j1 - 1;
            --j1;
         }

         if (s.length() > 0 && flag && j < s.length()) {
            j1 = this.fontRendererInstance.drawStringWithShadow(s.substring(j), (float)j1, (float)i1, i);
         }

         if (this.isFocused && flag) {
            double percentage = (double)(this.cursorCounter % 14) / 7.0;
            double interpPerc = percentage > 1.0 ? 1.0 - percentage : percentage;
            DrawUtil.glDrawFilledRect((double)k1, (double)(i1 - 1), (double)k1 + 0.5, (double)(i1 + this.fontRendererInstance.FONT_HEIGHT), 14737632 | (int)(255.0 * interpPerc) << 24);
         }

         if (k != j) {
            int l1 = l + this.fontRendererInstance.getStringWidth(s.substring(0, k));
            this.drawCursorVertical(k1, i1 - 1, l1 - 1, i1 + this.fontRendererInstance.FONT_HEIGHT);
         }
      }

   }

   private void drawCursorVertical(int p_146188_1_, int p_146188_2_, int p_146188_3_, int p_146188_4_) {
      int j;
      if (p_146188_1_ < p_146188_3_) {
         j = p_146188_1_;
         p_146188_1_ = p_146188_3_;
         p_146188_3_ = j;
      }

      if (p_146188_2_ < p_146188_4_) {
         j = p_146188_2_;
         p_146188_2_ = p_146188_4_;
         p_146188_4_ = j;
      }

      if (p_146188_3_ > this.xPosition + this.width) {
         p_146188_3_ = this.xPosition + this.width;
      }

      if (p_146188_1_ > this.xPosition + this.width) {
         p_146188_1_ = this.xPosition + this.width;
      }

      Tessellator tessellator = Tessellator.getInstance();
      WorldRenderer worldrenderer = tessellator.getWorldRenderer();
      GL11.glColor4f(0.0F, 0.0F, 255.0F, 255.0F);
      GL11.glDisable(3553);
      GlStateManager.enableColorLogic();
      GlStateManager.colorLogicOp(5387);
      worldrenderer.begin(7, DefaultVertexFormats.POSITION);
      worldrenderer.pos((double)p_146188_1_, (double)p_146188_4_, 0.0).endVertex();
      worldrenderer.pos((double)p_146188_3_, (double)p_146188_4_, 0.0).endVertex();
      worldrenderer.pos((double)p_146188_3_, (double)p_146188_2_, 0.0).endVertex();
      worldrenderer.pos((double)p_146188_1_, (double)p_146188_2_, 0.0).endVertex();
      tessellator.draw();
      GlStateManager.disableColorLogic();
      GL11.glEnable(3553);
   }

   public void setMaxStringLength(int p_146203_1_) {
      this.maxStringLength = p_146203_1_;
      if (this.text.length() > p_146203_1_) {
         this.text = this.text.substring(0, p_146203_1_);
      }

   }

   public int getMaxStringLength() {
      return this.maxStringLength;
   }

   public int getCursorPosition() {
      return this.cursorPosition;
   }

   public boolean getEnableBackgroundDrawing() {
      return this.enableBackgroundDrawing;
   }

   public void setEnableBackgroundDrawing(boolean p_146185_1_) {
      this.enableBackgroundDrawing = p_146185_1_;
   }

   public void setTextColor(int p_146193_1_) {
      this.enabledColor = p_146193_1_;
   }

   public void setDisabledTextColour(int p_146204_1_) {
      this.disabledColor = p_146204_1_;
   }

   public void setFocused(boolean p_146195_1_) {
      if (p_146195_1_ && !this.isFocused) {
         this.cursorCounter = 0;
      }

      this.isFocused = p_146195_1_;
   }

   public boolean isFocused() {
      return this.isFocused;
   }

   public void setEnabled(boolean p_146184_1_) {
      this.isEnabled = p_146184_1_;
   }

   public int getSelectionEnd() {
      return this.selectionEnd;
   }

   public int getWidth() {
      return this.getEnableBackgroundDrawing() ? this.width - 8 : this.width;
   }

   public void setSelectionPos(int p_146199_1_) {
      int i = (this.hideText ? this.getTextHidden() : this.text).length();
      if (p_146199_1_ > i) {
         p_146199_1_ = i;
      }

      if (p_146199_1_ < 0) {
         p_146199_1_ = 0;
      }

      this.selectionEnd = p_146199_1_;
      if (this.fontRendererInstance != null) {
         if (this.lineScrollOffset > i) {
            this.lineScrollOffset = i;
         }

         int j = this.getWidth();
         String s = this.fontRendererInstance.trimStringToWidth((this.hideText ? this.getTextHidden() : this.text).substring(this.lineScrollOffset), j);
         int k = s.length() + this.lineScrollOffset;
         if (p_146199_1_ == this.lineScrollOffset) {
            this.lineScrollOffset -= this.fontRendererInstance.trimStringToWidth(this.hideText ? this.getTextHidden() : this.text, j, true).length();
         }

         if (p_146199_1_ > k) {
            this.lineScrollOffset += p_146199_1_ - k;
         } else if (p_146199_1_ <= this.lineScrollOffset) {
            this.lineScrollOffset -= this.lineScrollOffset - p_146199_1_;
         }

         this.lineScrollOffset = MathHelper.clamp_int(this.lineScrollOffset, 0, i);
      }

   }

   public void setCanLoseFocus(boolean p_146205_1_) {
      this.canLoseFocus = p_146205_1_;
   }

   public boolean getVisible() {
      return this.visible;
   }

   public void setVisible(boolean p_146189_1_) {
      this.visible = p_146189_1_;
   }

   public String getTextHidden() {
      StringBuilder stringBuilder = new StringBuilder();
      if (!this.text.isEmpty() && this.hideText) {
         for(int i = 0; i < this.text.length(); ++i) {
            stringBuilder.append("*");
         }
      }

      return stringBuilder.toString();
   }
}
