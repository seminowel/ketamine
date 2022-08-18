package io.github.nevalackin.client.impl.property;

import io.github.nevalackin.client.api.property.Property;
import io.github.nevalackin.client.util.math.MathUtil;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public final class DoubleProperty extends Property {
   private final double min;
   private final double max;
   private final double inc;
   private final Map valueAliasMap;
   public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.####");

   public DoubleProperty(String name, double value, double min, double max, double inc) {
      this(name, value, (Property.Dependency)null, min, max, inc);
   }

   public DoubleProperty(String name, double value, Property.Dependency dependency, double min, double max, double inc) {
      super(name, value, dependency);
      this.valueAliasMap = new HashMap();
      this.min = min;
      this.max = max;
      this.inc = inc;
   }

   public String getDisplayString() {
      return this.valueAliasMap.containsKey(this.getValue()) ? (String)this.valueAliasMap.get(this.getValue()) : DECIMAL_FORMAT.format(this.getValue());
   }

   public void addValueAlias(double value, String alias) {
      this.valueAliasMap.put(value, alias);
   }

   public void setValue(Double value) {
      super.setValue(value > this.max ? this.max : (value < this.min ? this.min : MathUtil.round(value, this.inc)));
   }

   public double getMin() {
      return this.min;
   }

   public double getMax() {
      return this.max;
   }

   public double getInc() {
      return this.inc;
   }
}
