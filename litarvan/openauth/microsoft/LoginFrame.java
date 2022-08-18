package fr.litarvan.openauth.microsoft;

import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import javax.swing.JFrame;
import sun.net.www.protocol.https.Handler;

public class LoginFrame extends JFrame {
   private CompletableFuture future;

   public LoginFrame() {
      this.setTitle("Microsoft Authentication");
      this.setSize(750, 750);
      this.setLocationRelativeTo((Component)null);
      this.setContentPane(new JFXPanel());
   }

   public CompletableFuture start(String url) {
      if (this.future != null) {
         return this.future;
      } else {
         this.future = new CompletableFuture();
         this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
               LoginFrame.this.future.completeExceptionally(new MicrosoftAuthenticationException("User closed the authentication window"));
            }
         });
         Platform.runLater(() -> {
            this.init(url);
         });
         return this.future;
      }
   }

   protected void init(String url) {
      try {
         overrideFactory();
      } catch (Throwable var4) {
      }

      WebView webView = new WebView();
      JFXPanel content = (JFXPanel)this.getContentPane();
      content.setScene(new Scene(webView, (double)this.getWidth(), (double)this.getHeight()));
      webView.getEngine().locationProperty().addListener((observable, oldValue, newValue) -> {
         if (newValue.contains("access_token")) {
            this.setVisible(false);
            this.future.complete(newValue);
         }

      });
      webView.getEngine().load(url);
      this.setVisible(true);
   }

   protected static void overrideFactory() {
      URL.setURLStreamHandlerFactory((protocol) -> {
         return "https".equals(protocol) ? new Handler() {
            protected URLConnection openConnection(URL url) throws IOException {
               return this.openConnection(url, (Proxy)null);
            }

            protected URLConnection openConnection(URL url, Proxy proxy) throws IOException {
               HttpURLConnection connection = (HttpURLConnection)super.openConnection(url, proxy);
               return (URLConnection)((!"login.microsoftonline.com".equals(url.getHost()) || !url.getPath().endsWith("/oauth2/authorize")) && (!"login.live.com".equals(url.getHost()) || !"/oauth20_authorize.srf".equals(url.getPath())) && (!"login.live.com".equals(url.getHost()) || !"/ppsecure/post.srf".equals(url.getPath())) && (!"login.microsoftonline.com".equals(url.getHost()) || !"/login.srf".equals(url.getPath())) && (!"login.microsoftonline.com".equals(url.getHost()) || !url.getPath().endsWith("/login")) && (!"login.microsoftonline.com".equals(url.getHost()) || !url.getPath().endsWith("/SAS/ProcessAuth")) && (!"login.microsoftonline.com".equals(url.getHost()) || !url.getPath().endsWith("/federation/oauth2")) && (!"login.microsoftonline.com".equals(url.getHost()) || !url.getPath().endsWith("/oauth2/v2.0/authorize")) ? connection : new MicrosoftPatchedHttpURLConnection(url, connection));
            }
         } : null;
      });
   }
}
