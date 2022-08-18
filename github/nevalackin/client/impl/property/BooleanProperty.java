package io.github.nevalackin.client.impl.property;

import io.github.nevalackin.client.api.property.Property;

public final class BooleanProperty extends Property {
   public BooleanProperty(String name, boolean value) {
      super(name, value, (Property.Dependency)null);
   }

   public BooleanProperty(String name, boolean value, Property.Dependency dependency) {
      super(name, value, dependency);
   }
}
