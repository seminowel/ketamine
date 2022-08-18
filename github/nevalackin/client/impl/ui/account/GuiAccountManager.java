package io.github.nevalackin.client.impl.ui.account;

import io.github.nevalackin.client.api.account.Account;
import io.github.nevalackin.client.api.account.AccountManager;
import io.github.nevalackin.client.api.account.AccountType;
import io.github.nevalackin.client.api.notification.NotificationType;
import io.github.nevalackin.client.impl.core.KetamineClient;
import io.github.nevalackin.client.util.render.DrawUtil;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.DefaultPlayerSkin;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public final class GuiAccountManager extends GuiScreen {
   private final GuiScreen parent;
   private Account selectedAccount;
   private GuiButton loginButton;
   private GuiButton deleteButton;
   private boolean hasScrollbar;
   private int scrollOffset;
   private long lastClick;

   public GuiAccountManager(GuiScreen parent) {
      this.parent = parent;
   }

   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      AccountManager accountManager = KetamineClient.getInstance().getAccountManager();
      this.deleteButton.enabled = this.loginButton.enabled = this.selectedAccount != null;
      GL11.glDisable(3008);
      DrawUtil.glDrawFilledQuad(0.0, 0.0, (double)this.width, (double)this.height, -16250872, -15790321);
      DrawUtil.glDrawFilledQuad(0.0, 0.0, (double)this.width, 30.0, -16250872, -15658735);
      DrawUtil.glDrawFilledQuad(0.0, 30.0, (double)this.width, 4.0, -1778384896, 0);
      DrawUtil.glDrawFilledQuad(0.0, (double)(this.height - 58), (double)this.width, 58.0, -16185079);
      DrawUtil.glDrawFilledQuad(0.0, (double)(this.height - 62), (double)this.width, 4.0, 0, -1778384896);
      GL11.glEnable(3008);
      super.drawScreen(mouseX, mouseY, partialTicks);
      this.mc.fontRendererObj.drawStringWithShadow(String.format("§7Username: §A%s", this.mc.getSession().getUsername()), 4.0F, 4.0F, -1);
      String title = String.format("Alt Manager §8[§7%s§8]", accountManager.getAccounts().size());
      this.mc.fontRendererObj.drawStringWithShadow(title, (double)this.width / 2.0 - (double)this.fontRendererObj.getStringWidth(title) / 2.0, 10.5, -1);
      Collection accounts = accountManager.getAccounts();
      int top = 30;
      int xBuffer = true;
      int yBuffer = true;
      int headSize = true;
      int accountEntryHeight = true;
      int left = this.width / 3;
      int uncutHeight = accounts.size() * 36;
      int maxHeight = this.height - top - 58;
      this.hasScrollbar = uncutHeight > maxHeight;
      if (!this.hasScrollbar) {
         this.scrollOffset = 0;
      }

      if (this.hasScrollbar) {
         int scrollAmount = -Mouse.getDWheel() / 5;
         this.scrollOffset = Math.max(0, Math.min(uncutHeight - maxHeight, this.scrollOffset + scrollAmount));
         DrawUtil.glScissorBox((double)left, (double)top, (double)this.width / 1.5, (double)(this.height - top - 58), new ScaledResolution(this.mc));
         if (this.scrollOffset > 0) {
            GL11.glTranslated(0.0, (double)(-this.scrollOffset), 0.0);
         }
      }

      for(Iterator var23 = accounts.iterator(); var23.hasNext(); top += 36) {
         Account account = (Account)var23.next();
         String username = account.getUsername();
         String displayName = username.length() == 0 ? (account.getType() == AccountType.MICROSOFT ? (account.getProfileId() != null ? account.getProfileId() : "Microsoft Account") : (account.getEmail() != null ? account.getEmail() : "No data present.")) : username;
         this.mc.fontRendererObj.drawStringWithShadow(displayName, (float)(left + 36), (float)(top + 4), -1);
         if (this.selectedAccount == account) {
            DrawUtil.glDrawOutlinedQuad((double)(left + 1), (double)(top + 1), (double)(Math.max(left, 38 + this.mc.fontRendererObj.getStringWidth(displayName)) - 2), 34.0, 1.0F, -12369085);
         }

         String passwordStr = account.getPassword();
         if (passwordStr != null && passwordStr.length() > 0) {
            char[] password = new char[passwordStr.length()];
            Arrays.fill(password, '*');
            String censoredPwd = new String(password);
            this.mc.fontRendererObj.drawStringWithShadow(censoredPwd, (float)(left + 32 + 4), (float)(top + 4 + 9 + 2), -12369085);
         }

         DrawUtil.glDrawPlayerFace((double)(left + 2), (double)(top + 2), 32.0, 32.0, DefaultPlayerSkin.getDefaultSkinLegacy());
         if (account.isWorking()) {
            int iconSize = true;
            if (account.isBanned()) {
               if (account.getUnbanDate() == -1337L) {
                  this.mc.fontRendererObj.drawStringWithShadow("Sec Alert", (float)(left + 32 + 4), (float)(top + 4 + 2 + 18), -57255);
               } else {
                  Date timeRemaining = new Date(account.getUnbanDate() - System.currentTimeMillis());
                  SimpleDateFormat hypixelDateFormat = new SimpleDateFormat("D'd' H'h' m'm' s's'");
                  this.mc.fontRendererObj.drawStringWithShadow(hypixelDateFormat.format(timeRemaining), (float)(left + 32 + 4), (float)(top + 4 + 2 + 18), -167);
               }

               DrawUtil.glDrawWarningImage((double)left * 2.0 - 16.0 - 2.0, (double)(top + 2), 16.0, 16.0, -256);
            } else {
               DrawUtil.glDrawCheckmarkImage((double)left * 2.0 - 16.0 - 2.0, (double)(top + 2), 16.0, 16.0, -16711847);
            }
         }
      }

      if (this.hasScrollbar) {
         if (this.scrollOffset > 0) {
            GL11.glTranslated(0.0, (double)this.scrollOffset, 0.0);
         }

         DrawUtil.glEndScissor();
      }

   }

   public void initGui() {
      int xBuffer = true;
      int yBuffer = true;
      int unbufferedWidth = this.width / 6;
      int buttonWidth = this.width / 6 - 4;
      int buttonHeight = true;
      int unbufferedHeight = true;
      int left = this.width / 3;
      int top = this.height - 50;
      this.buttonList.add(this.loginButton = new GuiButton(0, left + 2, top + 2, buttonWidth, 20, "Login"));
      this.buttonList.add(new GuiButton(1, left + unbufferedWidth + 2, top + 2, buttonWidth, 20, "Add"));
      this.buttonList.add(this.deleteButton = new GuiButton(2, left + 2, top + 2 + 24, buttonWidth, 20, "Delete"));
      this.buttonList.add(new GuiButton(3, left + unbufferedWidth + 2, top + 2 + 24, buttonWidth, 20, "Done"));
      int directLoginWidth = Math.max(buttonWidth / 2, this.mc.fontRendererObj.getStringWidth("Direct Login") + 4);
      this.buttonList.add(new GuiButton(4, left - directLoginWidth - 2, top + 2 + 24, directLoginWidth, 20, "Direct Login"));
      int dupesWidth = Math.max(buttonWidth / 2, this.mc.fontRendererObj.getStringWidth("Filter Unique") + 4);
      int notWorkingWidth = Math.max(buttonWidth / 2, this.mc.fontRendererObj.getStringWidth("Filter Working") + 4);
      int bannedWidth = Math.max(buttonWidth / 2, this.mc.fontRendererObj.getStringWidth("Filter Unbanned") + 4);
      this.buttonList.add(new GuiButton(5, left - dupesWidth - 2, top + 2, dupesWidth, 20, "Filter Unique"));
      this.buttonList.add(new GuiButton(6, left * 2 + 2, top + 2 + 24, notWorkingWidth, 20, "Filter Working"));
      this.buttonList.add(new GuiButton(7, left * 2 + 2, top + 2, bannedWidth, 20, "Filter Unbanned"));
   }

   protected void actionPerformed(GuiButton button) throws IOException {
      AccountManager accountManager = KetamineClient.getInstance().getAccountManager();
      switch (button.id) {
         case 0:
            if (this.selectedAccount != null) {
               this.selectedAccount.createLoginThread();
            }
            break;
         case 1:
            this.mc.displayGuiScreen(new GuiAddAccount(this));
            break;
         case 2:
            accountManager.deleteAccount(this.selectedAccount);
            KetamineClient.getInstance().getNotificationManager().add(NotificationType.SUCCESS, "Deleted Account", String.format("Successfully deleted %s", this.selectedAccount), 1000L);
            this.selectedAccount = null;
            break;
         case 3:
            this.mc.displayGuiScreen(this.parent);
            break;
         case 4:
            this.mc.displayGuiScreen(new GuiDirectLogin(this));
            break;
         case 5:
            List dupes = new ArrayList();
            List emails = new ArrayList();
            accountManager.getAccounts().forEach((account) -> {
               if (emails.contains(account.getEmail())) {
                  dupes.add(account);
               } else {
                  emails.add(account.getEmail());
               }

            });
            dupes.forEach(accountManager::deleteAccount);
            break;
         case 6:
            Collection workingAccounts = accountManager.getWorkingAccounts();
            List notWorking = new ArrayList();
            accountManager.getAccounts().forEach((account) -> {
               if (!workingAccounts.contains(account)) {
                  notWorking.add(account);
               }

            });
            notWorking.forEach(accountManager::deleteAccount);
            break;
         case 7:
            Collection unbannedAccounts = accountManager.getUnbannedAccounts();
            List banned = new ArrayList();
            accountManager.getAccounts().forEach((account) -> {
               if (!unbannedAccounts.contains(account)) {
                  banned.add(account);
               }

            });
            banned.forEach(accountManager::deleteAccount);
      }

   }

   protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
      int top = 30;
      int accountEntryHeight = true;
      int left = this.width / 3;
      boolean selectedAccount = false;
      if (mouseY > top && mouseY < this.height - 58 && mouseX > left) {
         mouseY += this.scrollOffset;

         for(Iterator var8 = KetamineClient.getInstance().getAccountManager().getAccounts().iterator(); var8.hasNext(); top += 36) {
            Account account = (Account)var8.next();
            if (mouseY > top && mouseY < top + 36 && mouseX < left + Math.max(left, 36 + this.mc.fontRendererObj.getStringWidth(account.getUsername().length() == 0 ? account.getEmail() : account.getUsername()) + 2)) {
               this.selectedAccount = account;
               selectedAccount = true;
               break;
            }
         }

         mouseY -= this.scrollOffset;
      }

      if (mouseButton == 0 && selectedAccount) {
         if (System.currentTimeMillis() - this.lastClick < 200L) {
            this.selectedAccount.createLoginThread();
         }

         this.lastClick = System.currentTimeMillis();
      }

      if (!selectedAccount && mouseY < this.height - 58) {
         this.selectedAccount = null;
      }

      super.mouseClicked(mouseX, mouseY, mouseButton);
   }

   protected void mouseReleased(int mouseX, int mouseY, int state) {
      super.mouseReleased(mouseX, mouseY, state);
   }

   protected void keyTyped(char typedChar, int keyCode) throws IOException {
      if (keyCode == 1) {
         this.mc.displayGuiScreen(this.parent);
      }
   }
}
