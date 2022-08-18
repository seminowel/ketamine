package io.github.nevalackin.client.impl.file;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.github.nevalackin.client.api.file.FileManager;
import io.github.nevalackin.client.impl.core.KetamineClient;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import org.apache.commons.io.FilenameUtils;

public final class FileManagerImpl implements FileManager {
   private File directory;
   private final Map aliasMap = new HashMap();
   private static final Gson GSON = new Gson();
   private static final JsonParser PARSER = new JsonParser();

   public FileManagerImpl() {
      File directory = this.getClientDirectory();
      this.createFile("configs", new File(directory, "configs"));
      this.createFile("scripts", new File(directory, "scripts"));
      this.createFile("alts", new File(directory, "accounts.keta"));
      this.createFile("binds", new File(directory, "binds.keta"));
   }

   public void writeJson(File file, JsonElement json) {
      Thread writeThread = new Thread(() -> {
         try {
            String jsonS = GSON.toJson(json);
            byte[] input = jsonS.getBytes(StandardCharsets.UTF_8);
            byte[] compressed = compress(input);
            Files.write(file.toPath(), compressed, new OpenOption[0]);
         } catch (IOException var5) {
            var5.printStackTrace();
         }

      });
      writeThread.start();
   }

   public JsonElement parse(File file) {
      try {
         byte[] compressed = Files.readAllBytes(file.toPath());
         byte[] decompressed = decompress(compressed);
         String toString = new String(decompressed, 0, decompressed.length, StandardCharsets.UTF_8);
         return PARSER.parse(toString);
      } catch (Exception var5) {
         var5.printStackTrace();
         return null;
      }
   }

   private static byte[] compress(byte[] input) throws IOException {
      if (input.length == 0) {
         return input;
      } else {
         Deflater compressor = new Deflater();
         compressor.setLevel(9);
         compressor.setInput(input);
         compressor.finish();
         ByteArrayOutputStream stream = new ByteArrayOutputStream(input.length);
         byte[] buffer = new byte[1024];

         while(!compressor.finished()) {
            int compressedLen = compressor.deflate(buffer);
            stream.write(buffer, 0, compressedLen);
         }

         stream.close();
         compressor.end();
         return stream.toByteArray();
      }
   }

   private static byte[] decompress(byte[] input) throws DataFormatException, IOException {
      if (input.length == 0) {
         return input;
      } else {
         Inflater decompressor = new Inflater();
         decompressor.setInput(input);
         ByteArrayOutputStream stream = new ByteArrayOutputStream(input.length);
         byte[] buffer = new byte[1024];

         while(!decompressor.finished()) {
            int decompressedLen = decompressor.inflate(buffer);
            stream.write(buffer, 0, decompressedLen);
         }

         stream.close();
         decompressor.end();
         return stream.toByteArray();
      }
   }

   public File getFile(String alias) {
      return (File)this.aliasMap.get(alias);
   }

   public void createFile(String alias, File file) {
      try {
         if (FilenameUtils.getExtension(file.getName()).equals("")) {
            file.mkdirs();
         } else {
            file.createNewFile();
         }

         this.aliasMap.put(alias, file);
      } catch (IOException var4) {
      }

   }

   public File getClientDirectory() {
      if (this.directory == null) {
         this.directory = new File(KetamineClient.getInstance().getName());
         if (!this.directory.mkdir()) {
         }
      }

      return this.directory;
   }
}
