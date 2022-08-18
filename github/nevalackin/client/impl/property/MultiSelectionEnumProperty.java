package io.github.nevalackin.client.impl.property;

import io.github.nevalackin.client.api.property.Property;
import java.util.Arrays;
import java.util.List;

public final class MultiSelectionEnumProperty extends Property {
   private final Enum[] values;

   public MultiSelectionEnumProperty(String name, List selected, Enum[] values, Property.Dependency dependency) {
      super(name, selected, dependency);
      this.values = (Enum[])values[0].getClass().getEnumConstants();
   }

   public MultiSelectionEnumProperty(String name, List selected, Enum[] values) {
      this(name, selected, values, (Property.Dependency)null);
   }

   public void select(int idx) {
      ((List)this.getValue()).add(this.values[this.clamp(idx)]);
   }

   public void unselect(int idx) {
      ((List)this.getValue()).remove(this.values[this.clamp(idx)]);
   }

   public boolean isSelected(Enum value) {
      return ((List)this.getValue()).contains(value);
   }

   public boolean isSelected(int idx) {
      return ((List)this.getValue()).contains(this.getValues()[idx]);
   }

   public boolean hasSelections() {
      return !((List)this.getValue()).isEmpty();
   }

   private int clamp(int idx) {
      return Math.max(0, Math.min(this.values.length - 1, idx));
   }

   public String[] getValueNames() {
      return (String[])Arrays.stream(this.values).map(Enum::toString).toArray((x$0) -> {
         return new String[x$0];
      });
   }

   public int[] getValueIndices() {
      return ((List)this.getValue()).stream().mapToInt(Enum::ordinal).toArray();
   }

   public Enum[] getValues() {
      return this.values;
   }
}
