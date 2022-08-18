package io.github.nevalackin.client.impl.property;

import io.github.nevalackin.client.api.property.Property;
import java.util.Arrays;

public final class EnumProperty extends Property {
   private final Enum[] values;

   public EnumProperty(String name, Enum value) {
      this(name, value, (Property.Dependency)null);
   }

   public EnumProperty(String name, Enum value, Property.Dependency dependency) {
      super(name, value, dependency);
      this.values = (Enum[])this.getType().getEnumConstants();
   }

   public void setValue(int index) {
      this.setValue(this.values[Math.max(0, Math.min(this.values.length - 1, index))]);
   }

   public Class getType() {
      return ((Enum)this.getValue()).getClass();
   }

   public String[] getValueNames() {
      return (String[])Arrays.stream(this.values).map(Enum::toString).toArray((x$0) -> {
         return new String[x$0];
      });
   }

   public Enum[] getValues() {
      return this.values;
   }
}
