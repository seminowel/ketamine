package io.github.nevalackin.client.impl.script;

import io.github.nevalackin.client.api.script.ScriptManager;
import io.github.nevalackin.client.impl.core.KetamineClient;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.FilenameUtils;

public final class ScriptManagerImpl implements ScriptManager {
   private final Map scripts = new HashMap();

   public ScriptManagerImpl() {
      File scriptsDir = KetamineClient.getInstance().getFileManager().getFile("scripts");
      File[] files = scriptsDir.listFiles();
      if (files != null) {
         File[] var3 = files;
         int var4 = files.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            File file = var3[var5];
            if (FilenameUtils.getExtension(file.getName()).equals(".js".substring(1))) {
               String script = FilenameUtils.removeExtension(file.getName());

               try {
                  BufferedReader reader = Files.newBufferedReader(Paths.get(script));
                  Throwable var9 = null;

                  try {
                     StringBuilder builder = new StringBuilder();

                     String nextLine;
                     while((nextLine = reader.readLine()) != null) {
                        builder.append(nextLine).append('\n');
                     }

                     this.scripts.put(script, new Script(builder.toString()));
                  } catch (Throwable var20) {
                     var9 = var20;
                     throw var20;
                  } finally {
                     if (reader != null) {
                        if (var9 != null) {
                           try {
                              reader.close();
                           } catch (Throwable var19) {
                              var9.addSuppressed(var19);
                           }
                        } else {
                           reader.close();
                        }
                     }

                  }
               } catch (IOException var22) {
               }
            }
         }
      }

   }

   public boolean enable(String script, boolean enabled) {
      Script scriptObj = (Script)this.scripts.get(script);
      if (scriptObj != null) {
         scriptObj.setEnabled(enabled);
         return true;
      } else {
         return false;
      }
   }
}
