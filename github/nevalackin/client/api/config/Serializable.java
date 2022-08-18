package io.github.nevalackin.client.api.config;

import com.google.gson.JsonObject;

public interface Serializable {
   void load(JsonObject var1);

   void save(JsonObject var1);
}
