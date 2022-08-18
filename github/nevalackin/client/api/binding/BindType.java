package io.github.nevalackin.client.api.binding;

public enum BindType {
   TOGGLE("Toggle"),
   HOLD("Hold"),
   NONE("None");

   private final String name;

   private BindType(String name) {
      this.name = name;
   }

   public String toString() {
      return this.name;
   }
}
