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

public final class GuiDirectLogin extends GuiScreen {
   private final GuiScreen parent;
   private GuiTextField userField;
   private GuiTextField passwordField;

   public GuiDirectLogin(GuiScreen parent) {
      this.parent = parent;
   }

   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      DrawUtil.glDrawFilledQuad(0.0, 0.0, (double)this.width, (double)this.height, -16250872, -15790321);
      this.mc.fontRendererObj.drawStringWithShadow("Direct Login", (double)this.width / 2.0 - (double)this.mc.fontRendererObj.getStringWidth("Direct Login") / 2.0, (double)this.height / 5.0 - 4.5, -1);
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
      this.passwordField = new GuiTextField(1, this.mc.fontRendererObj, left, top + 24, width, 20, true);
      int buttonWidth = true;
      int buttonLeft = left + (fullWidth - 120) / 2;
      this.buttonList.add(new GuiButton(0, buttonLeft, top + 4 + 48, 120, 20, "Import"));
      this.buttonList.add(new GuiButton(1, buttonLeft, top + 4 + 72, 120, 20, "Login"));
      this.buttonList.add(new GuiButton(3, buttonLeft, top + 4 + 96, 120, 20, "Login Microsoft"));
      this.buttonList.add(new GuiButton(2, buttonLeft, top + 4 + 120, 120, 20, "Done"));
   }

   protected void actionPerformed(GuiButton button) throws IOException {
      switch (button.id) {
         case 0:
            String clipboardContents = ClipboardUtil.getClipboardContents();
            if (clipboardContents == null || clipboardContents.length() < 3) {
               return;
            }

            String[] split = clipboardContents.split(":", 2);
            if (split.length < 2) {
               return;
            }

            this.userField.setText(split[0]);
            this.passwordField.setText(split[1]);
            break;
         case 1:
            String email = this.userField.getText();
            String password = this.passwordField.getText();
            if (email.length() == 0) {
               return;
            }

            Account account = Account.builder().email(email).password(password).build();
            account.createLoginThread();
         case 3:
            KetamineClient.getInstance().getMicrosoftAuthenticator().loginWithAsyncWebview().exceptionally((throwable) -> {
               KetamineClient.getInstance().getNotificationManager().add(NotificationType.ERROR, "Error", throwable.getMessage().split(":")[1], 3000L);
               return null;
            }).thenAccept((microsoftAuthResult) -> {
               Account microsoftAccount = Account.builder().username(microsoftAuthResult.getProfile().getName()).profileId(microsoftAuthResult.getProfile().getId()).accessToken(microsoftAuthResult.getAccessToken()).refreshToken(microsoftAuthResult.getRefreshToken()).build();
               microsoftAccount.createLoginThread();
            });
         case 2:
            this.mc.displayGuiScreen(this.parent);
      }

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
