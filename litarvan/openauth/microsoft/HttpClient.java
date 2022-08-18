package fr.litarvan.openauth.microsoft;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HttpClient {
   public static final String MIME_TYPE_JSON = "application/json";
   public static final String MIME_TYPE_URLENCODED_FORM = "application/x-www-form-urlencoded";
   private final Gson gson = new Gson();

   public String getText(String url, Map params) throws MicrosoftAuthenticationException {
      return this.readResponse(this.createConnection(url + '?' + this.buildParams(params)));
   }

   public Object getJson(String url, String token, Class responseClass) throws MicrosoftAuthenticationException {
      HttpURLConnection connection = this.createConnection(url);
      connection.addRequestProperty("Authorization", "Bearer " + token);
      connection.addRequestProperty("Accept", "application/json");
      return this.readJson(connection, responseClass);
   }

   public HttpURLConnection postForm(String url, Map params) throws MicrosoftAuthenticationException {
      return this.post(url, "application/x-www-form-urlencoded", "*/*", this.buildParams(params));
   }

   public Object postJson(String url, Object request, Class responseClass) throws MicrosoftAuthenticationException {
      HttpURLConnection connection = this.post(url, "application/json", "application/json", this.gson.toJson(request));
      return this.readJson(connection, responseClass);
   }

   public Object postFormGetJson(String url, Map params, Class responseClass) throws MicrosoftAuthenticationException {
      return this.readJson(this.postForm(url, params), responseClass);
   }

   protected HttpURLConnection post(String url, String contentType, String accept, String data) throws MicrosoftAuthenticationException {
      HttpURLConnection connection = this.createConnection(url);
      connection.setDoOutput(true);
      connection.addRequestProperty("Content-Type", contentType);
      connection.addRequestProperty("Accept", accept);

      try {
         connection.setRequestMethod("POST");
         connection.getOutputStream().write(data.getBytes(StandardCharsets.UTF_8));
         return connection;
      } catch (IOException var7) {
         throw new MicrosoftAuthenticationException(var7);
      }
   }

   protected Object readJson(HttpURLConnection connection, Class responseType) throws MicrosoftAuthenticationException {
      return this.gson.fromJson(this.readResponse(connection), responseType);
   }

   protected String readResponse(HttpURLConnection connection) throws MicrosoftAuthenticationException {
      String redirection = connection.getHeaderField("Location");
      if (redirection != null) {
         return this.readResponse(this.createConnection(redirection));
      } else {
         StringBuilder response = new StringBuilder();

         try {
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String line;
            try {
               while((line = br.readLine()) != null) {
                  response.append(line).append('\n');
               }
            } catch (Throwable var8) {
               try {
                  br.close();
               } catch (Throwable var7) {
                  var8.addSuppressed(var7);
               }

               throw var8;
            }

            br.close();
         } catch (IOException var9) {
            throw new MicrosoftAuthenticationException(var9);
         }

         return response.toString();
      }
   }

   protected HttpURLConnection followRedirects(HttpURLConnection connection) throws MicrosoftAuthenticationException {
      String redirection = connection.getHeaderField("Location");
      if (redirection != null) {
         connection = this.followRedirects(this.createConnection(redirection));
      }

      return connection;
   }

   protected String buildParams(Map params) {
      StringBuilder query = new StringBuilder();
      params.forEach((key, value) -> {
         if (query.length() > 0) {
            query.append('&');
         }

         try {
            query.append(key).append('=').append(URLEncoder.encode(value, "UTF-8"));
         } catch (UnsupportedEncodingException var4) {
         }

      });
      return query.toString();
   }

   protected HttpURLConnection createConnection(String url) throws MicrosoftAuthenticationException {
      HttpURLConnection connection;
      try {
         connection = (HttpURLConnection)(new URL(url)).openConnection();
      } catch (IOException var4) {
         throw new MicrosoftAuthenticationException(var4);
      }

      String userAgent = "Mozilla/5.0 (XboxReplay; XboxLiveAuth/3.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36";
      connection.setRequestProperty("Accept-Language", "en-US");
      connection.setRequestProperty("Accept-Charset", "UTF-8");
      connection.setRequestProperty("User-Agent", userAgent);
      return connection;
   }
}
