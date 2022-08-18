package io.github.nevalackin.client.api.config;

import io.github.nevalackin.client.impl.config.Config;
import java.util.Collection;

public interface ConfigManager {
   String EXTENSION = ".keta";

   boolean load(String var1);

   boolean load(Config var1);

   void save(String var1);

   boolean save(Config var1);

   Config find(String var1);

   boolean delete(String var1);

   boolean delete(Config var1);

   boolean saveCurrent();

   boolean reloadCurrent();

   void refresh();

   Collection getConfigs();
}
