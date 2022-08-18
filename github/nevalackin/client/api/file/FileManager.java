package io.github.nevalackin.client.api.file;

import com.google.gson.JsonElement;
import java.io.File;

public interface FileManager {
   void writeJson(File var1, JsonElement var2);

   JsonElement parse(File var1);

   File getFile(String var1);

   void createFile(String var1, File var2);

   File getClientDirectory();
}
