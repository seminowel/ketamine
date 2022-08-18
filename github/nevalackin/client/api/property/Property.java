package io.github.nevalackin.client.api.property;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class Property {
   private final String name;
   private Object value;
   private final List valueChangeListeners;
   private final Dependency dependency;
   private List attachedProperties;

   public Property(String name, Object value, Dependency dependency) {
      this.name = name;
      this.valueChangeListeners = new ArrayList();
      this.dependency = dependency;
      this.setValue(value);
   }

   public Property(String name, Object value) {
      this(name, value, (Dependency)null);
   }

   public void addChangeListener(ValueChangeListener changeListener) {
      this.valueChangeListeners.add(changeListener);
   }

   public Object getValue() {
      return this.value;
   }

   public boolean check() {
      return this.dependency == null || this.dependency.test();
   }

   public void setValue(Object value) {
      Iterator var2 = this.valueChangeListeners.iterator();

      while(var2.hasNext()) {
         ValueChangeListener changeListener = (ValueChangeListener)var2.next();
         changeListener.onChange(value);
      }

      this.value = value;
   }

   public List getAttachedProperties() {
      return this.attachedProperties;
   }

   public void attachProperty(Property property) {
      if (this.attachedProperties == null) {
         this.attachedProperties = new ArrayList();
      }

      this.attachedProperties.add(property);
   }

   public String getName() {
      return this.name;
   }

   @FunctionalInterface
   public interface Dependency {
      boolean test();
   }
}
