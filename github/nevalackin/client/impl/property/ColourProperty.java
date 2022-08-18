package io.github.nevalackin.client.impl.property;

import io.github.nevalackin.client.api.property.Property;
import java.awt.Color;

public final class ColourProperty extends Property {
   private Color colour;

   public ColourProperty(String name, int colour, Property.Dependency dependency) {
      super(name, colour, dependency);
   }

   public ColourProperty(String name, int colour) {
      super(name, colour, (Property.Dependency)null);
   }

   public void setValue(Integer colour) {
      super.setValue(colour);
      this.colour = new Color(colour);
   }

   public void setValue(Color colour) {
      this.colour = colour;
      super.setValue(colour.getRGB());
   }

   public Color getColour() {
      return this.colour;
   }
}
