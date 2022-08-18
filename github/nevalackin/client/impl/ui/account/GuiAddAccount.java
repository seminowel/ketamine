package io.github.nevalackin.client.impl.ui.account;

import io.github.nevalackin.client.api.account.Account;
import io.github.nevalackin.client.api.notification.NotificationType;
import io.github.nevalackin.client.impl.core.KetamineClient;
import io.github.nevalackin.client.util.misc.ClipboardUtil;
import io.github.nevalackin.client.util.render.DrawUtil;
import java.io.IOException;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

public final class GuiAddAccount extends GuiScreen {
   private final GuiScreen parent;
   private GuiTextField userField;
   private GuiTextField passwordField;

   public GuiAddAccount(GuiScreen parent) {
      this.parent = parent;
   }

   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      DrawUtil.glDrawFilledQuad(0.0, 0.0, (double)this.width, (double)this.height, -16250872, -15790321);
      this.mc.fontRendererObj.drawStringWithShadow("Add Account", (double)this.width / 2.0 - (double)this.mc.fontRendererObj.getStringWidth("Add Account") / 2.0, (double)this.height / 5.0 - 4.5, -1);
      this.userField.drawTextBox();
      this.passwordField.drawTextBox();
      super.drawScreen(mouseX, mouseY, partialTicks);
   }

   public void initGui() {
      int yBuffer = true;
      int xBuffer = true;
      int fullWidth = this.width / 4;
      int width = fullWidth - 4;
      int left = this.width / 2 - fullWidth / 2;
      int top = this.height / 3;
      int height = true;
      this.userField = new GuiTextField(0, this.mc.fontRendererObj, left, top, width, 20);
      this.userField.setMaxStringLength(128);
      this.passwordField = new GuiTextField(1, this.mc.fontRendererObj, left, top + 24, width, 20, true);
      this.passwordField.setMaxStringLength(128);
      int buttonWidth = true;
      int buttonLeft = left + (fullWidth - 120) / 2;
      this.buttonList.add(new GuiButton(0, buttonLeft, top + 4 + 48, 120, 20, "Import"));
      this.buttonList.add(new GuiButton(1, buttonLeft, top + 4 + 120, 120, 20, "Add & Login"));
      this.buttonList.add(new GuiButton(2, buttonLeft, top + 4 + 72, 120, 20, "Add"));
      this.buttonList.add(new GuiButton(4, buttonLeft, top + 4 + 96, 120, 20, "Add Microsoft"));
      this.buttonList.add(new GuiButton(3, buttonLeft, top + 4 + 144, 120, 20, "Done"));
   }

   protected void actionPerformed(GuiButton button) throws IOException {
      Account account = null;
      switch (button.id) {
         case 0:
            String clipboardContents = ClipboardUtil.getClipboardContents();
            if (clipboardContents != null && clipboardContents.length() >= 3) {
               String[] split = clipboardContents.split(":", 2);
               if (split.length < 2) {
                  return;
               }

               this.userField.setText(split[0]);
               this.passwordField.setText(split[1]);
               break;
            }

            return;
         case 1:
            account = this.createAccountFromFields();
            if (account == null) {
               return;
            }

            account.createLoginThread();
         case 2:
            if (account == null && (account = this.createAccountFromFields()) == null) {
               return;
            }

            KetamineClient.getInstance().getAccountManager().addAccount(account);
         case 3:
            this.mc.displayGuiScreen(this.parent);
            break;
         case 4:
            KetamineClient.getInstance().getMicrosoftAuthenticator().loginWithAsyncWebview().exceptionally((throwable) -> {
               KetamineClient.getInstance().getNotificationManager().add(NotificationType.ERROR, "Error", throwable.getMessage().split(":")[1], 3000L);
               return null;
            }).thenAccept((microsoftAuthResult) -> {
               Account microsoftAccount = Account.builder().username(microsoftAuthResult.getProfile().getName()).profileId(microsoftAuthResult.getProfile().getId()).accessToken(microsoftAuthResult.getAccessToken()).refreshToken(microsoftAuthResult.getRefreshToken()).build();
               KetamineClient.getInstance().getAccountManager().addAccount(microsoftAccount);
               this.mc.displayGuiScreen(this.parent);
            });
      }

   }

   private Account createAccountFromFields() {
      String email = this.userField.getText();
      String password = this.passwordField.getText();
      return email.length() == 0 ? null : Account.builder().email(email).password(password).build();
   }

   protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
      this.userField.mouseClicked(mouseX, mouseY, mouseButton);
      this.passwordField.mouseClicked(mouseX, mouseY, mouseButton);
      super.mouseClicked(mouseX, mouseY, mouseButton);
   }

   public void updateScreen() {
      this.userField.updateCursorCounter();
      this.passwordField.updateCursorCounter();
   }

   protected void mouseReleased(int mouseX, int mouseY, int state) {
      super.mouseReleased(mouseX, mouseY, state);
   }

   protected void keyTyped(char typedChar, int keyCode) throws IOException {
      if (keyCode == 1) {
         this.mc.displayGuiScreen(this.parent);
      } else {
         this.userField.textboxKeyTyped(typedChar, keyCode);
         this.passwordField.textboxKeyTyped(typedChar, keyCode);
      }
   }
}
