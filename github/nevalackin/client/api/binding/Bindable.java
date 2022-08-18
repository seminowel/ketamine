package io.github.nevalackin.client.api.binding;

import com.google.gson.JsonObject;

public interface Bindable {
   String getName();

   void setActive(boolean var1);

   boolean isActive();

   default void loadBind(JsonObject object) {
   }

   default void saveBind(JsonObject object) {
   }

   default void toggle() {
      this.setActive(!this.isActive());
   }
}
