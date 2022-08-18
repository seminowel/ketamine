package io.github.nevalackin.client.api.module;

import java.util.Collection;

public interface ModuleManager {
   Module getModule(Class var1);

   Module getModule(String var1);

   void registerModule(Class var1, Module var2);

   Collection getModules();
}
