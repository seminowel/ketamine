package fr.litarvan.openauth.microsoft;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class MicrosoftPatchedHttpURLConnection extends HttpURLConnection {
   private final HttpURLConnection inner;

   public MicrosoftPatchedHttpURLConnection(URL url, HttpURLConnection inner) {
      super(url);
      this.inner = inner;
   }

   public void setRequestMethod(String method) throws ProtocolException {
      this.inner.setRequestMethod(method);
   }

   public void setInstanceFollowRedirects(boolean followRedirects) {
      this.inner.setInstanceFollowRedirects(followRedirects);
   }

   public boolean getInstanceFollowRedirects() {
      return this.inner.getInstanceFollowRedirects();
   }

   public String getRequestMethod() {
      return this.inner.getRequestMethod();
   }

   public int getResponseCode() throws IOException {
      return this.inner.getResponseCode();
   }

   public String getResponseMessage() throws IOException {
      return this.inner.getResponseMessage();
   }

   public Map getHeaderFields() {
      return this.inner.getHeaderFields();
   }

   public String getHeaderField(String name) {
      return this.inner.getHeaderField(name);
   }

   public String getHeaderField(int n) {
      return this.inner.getHeaderField(n);
   }

   public void disconnect() {
      this.inner.disconnect();
   }

   public void setDoOutput(boolean dooutput) {
      this.inner.setDoOutput(dooutput);
   }

   public boolean usingProxy() {
      return this.inner.usingProxy();
   }

   public void connect() throws IOException {
      this.inner.connect();
   }

   public InputStream getInputStream() throws IOException {
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      InputStream in = this.inner.getInputStream();

      try {
         byte[] data = new byte[8192];

         int n;
         while((n = in.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, n);
         }
      } catch (Throwable var6) {
         if (in != null) {
            try {
               in.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }
         }

         throw var6;
      }

      if (in != null) {
         in.close();
      }

      byte[] patched = buffer.toString("UTF-8").replaceAll("integrity ?=", "integrity.disabled=").replaceAll("setAttribute\\(\"integrity\"", "setAttribute(\"integrity.disabled\"").getBytes(StandardCharsets.UTF_8);
      return new ByteArrayInputStream(patched);
   }

   public OutputStream getOutputStream() throws IOException {
      return this.inner.getOutputStream();
   }

   public InputStream getErrorStream() {
      return this.inner.getErrorStream();
   }
}
