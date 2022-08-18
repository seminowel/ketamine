package io.github.nevalackin.client.api.module;

import com.google.gson.JsonObject;
import io.github.nevalackin.client.api.binding.Bind;
import io.github.nevalackin.client.api.binding.BindType;
import io.github.nevalackin.client.api.binding.Bindable;
import io.github.nevalackin.client.api.config.Serializable;
import io.github.nevalackin.client.api.property.Property;
import io.github.nevalackin.client.impl.core.KetamineClient;
import io.github.nevalackin.client.impl.event.game.input.InputType;
import io.github.nevalackin.client.impl.module.combat.rage.TargetStrafe;
import io.github.nevalackin.client.impl.property.BooleanProperty;
import io.github.nevalackin.client.impl.property.ColourProperty;
import io.github.nevalackin.client.impl.property.DoubleProperty;
import io.github.nevalackin.client.impl.property.EnumProperty;
import io.github.nevalackin.client.impl.property.MultiSelectionEnumProperty;
import io.github.nevalackin.client.util.render.Position2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;

public abstract class Module implements Serializable, Bindable {
   private final String name;
   private final Category category;
   private final Category.SubCategory subCategory;
   private int key;
   private Bind bind;
   private boolean enabled;
   private boolean hidden;
   private Position2D pos2D = new Position2D(0.0, 0.0);
   private Supplier suffix;
   private final List properties = new ArrayList();
   protected final Minecraft mc = Minecraft.getMinecraft();
   protected static TargetStrafe targetStrafeInstance;

   public Module(String name, Category category, Category.SubCategory subCategory) {
      this.name = name;
      this.category = category;
      this.subCategory = subCategory;
      this.setBind((Bind)null);
   }

   public Position2D getPos2D() {
      return this.pos2D;
   }

   public void loadBind(JsonObject object) {
      InputType inputType = InputType.KEYBOARD;
      int button = 0;
      BindType bindType = BindType.TOGGLE;
      if (object.has("input")) {
         inputType = InputType.values()[object.get("input").getAsInt()];
      }

      if (object.has("button")) {
         button = object.get("button").getAsInt();
      }

      if (object.has("type")) {
         bindType = BindType.values()[object.get("type").getAsInt()];
      }

      this.setBind(new Bind(inputType, button, bindType));
   }

   public void saveBind(JsonObject object) {
      Bind bind = this.getBind();
      if (bind != null) {
         JsonObject savedDataObject = new JsonObject();
         savedDataObject.addProperty("input", bind.getInputType().ordinal());
         savedDataObject.addProperty("button", bind.getCode());
         savedDataObject.addProperty("type", bind.getBindType().ordinal());
         object.add(this.getName(), savedDataObject);
      }
   }

   public void load(JsonObject object) {
      if (object.has("enabled")) {
         this.setEnabled(object.get("enabled").getAsBoolean());
      }

      if (object.has("hidden")) {
         this.setHidden(object.get("hidden").getAsBoolean());
      }

      if (object.has("props")) {
         JsonObject propsObject = object.get("props").getAsJsonObject();
         Iterator var3 = this.getProperties().iterator();

         while(var3.hasNext()) {
            Property property = (Property)var3.next();
            if (propsObject.has(property.getName())) {
               if (property instanceof BooleanProperty) {
                  BooleanProperty booleanProperty = (BooleanProperty)property;
                  booleanProperty.setValue(propsObject.get(property.getName()).getAsBoolean());
               } else if (property instanceof DoubleProperty) {
                  DoubleProperty doubleProperty = (DoubleProperty)property;
                  doubleProperty.setValue(propsObject.get(property.getName()).getAsDouble());
               } else if (property instanceof ColourProperty) {
                  ColourProperty colourProperty = (ColourProperty)property;
                  colourProperty.setValue(propsObject.get(property.getName()).getAsInt());
               } else if (property instanceof EnumProperty) {
                  EnumProperty enumProperty = (EnumProperty)property;
                  enumProperty.setValue(propsObject.get(property.getName()).getAsInt());
               } else if (property instanceof MultiSelectionEnumProperty) {
               }
            }
         }
      }

   }

   public void save(JsonObject object) {
      JsonObject savedDataObject = new JsonObject();
      savedDataObject.addProperty("enabled", this.isEnabled());
      savedDataObject.addProperty("hidden", this.isHidden());
      JsonObject propertiesObject = new JsonObject();
      Iterator var4 = this.getProperties().iterator();

      while(var4.hasNext()) {
         Property property = (Property)var4.next();
         if (property instanceof BooleanProperty) {
            BooleanProperty booleanProperty = (BooleanProperty)property;
            propertiesObject.addProperty(property.getName(), (Boolean)booleanProperty.getValue());
         } else if (property instanceof DoubleProperty) {
            DoubleProperty doubleProperty = (DoubleProperty)property;
            propertiesObject.addProperty(property.getName(), (Number)doubleProperty.getValue());
         } else if (property instanceof ColourProperty) {
            ColourProperty colourProperty = (ColourProperty)property;
            propertiesObject.addProperty(property.getName(), (Number)colourProperty.getValue());
         } else if (property instanceof EnumProperty) {
            EnumProperty enumProperty = (EnumProperty)property;
            propertiesObject.addProperty(property.getName(), ((Enum)enumProperty.getValue()).ordinal());
         } else if (property instanceof MultiSelectionEnumProperty) {
            MultiSelectionEnumProperty enumProperty = (MultiSelectionEnumProperty)property;
            propertiesObject.addProperty(property.getName(), Arrays.toString(enumProperty.getValueIndices()));
         }
      }

      savedDataObject.add("props", propertiesObject);
      object.add(this.getName(), savedDataObject);
   }

   protected void register(Property... properties) {
      this.properties.addAll(Arrays.asList(properties));
   }

   public Supplier getSuffix() {
      return this.suffix;
   }

   public void setSuffix(Supplier suffix) {
      this.suffix = suffix;
   }

   public String getDisplayName() {
      return this.suffix == null ? this.name : String.format("%s §7%s", this.name, this.suffix.get());
   }

   public List getProperties() {
      return this.properties;
   }

   public String getName() {
      return this.name;
   }

   public Category getCategory() {
      return this.category;
   }

   public Category.SubCategory getSubCategory() {
      return this.subCategory;
   }

   public int getKey() {
      return this.key;
   }

   public void setKey(int key) {
      this.key = key;
   }

   public void setBind(Bind bind) {
      KetamineClient.getInstance().getBindManager().register(this, bind);
      this.bind = bind;
   }

   public Bind getBind() {
      return this.bind;
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public boolean isDisplayed() {
      return !this.hidden && this.enabled;
   }

   public boolean isHidden() {
      return this.hidden;
   }

   public void setHidden(boolean hidden) {
      this.hidden = hidden;
   }

   public void setEnabled(boolean enabled) {
      if (this.enabled != enabled) {
         this.enabled = enabled;
         if (enabled) {
            this.onEnable();
            KetamineClient.getInstance().getEventBus().subscribe(this);
         } else {
            KetamineClient.getInstance().getEventBus().unsubscribe(this);
            this.onDisable();
         }
      }

   }

   public void setActive(boolean active) {
      this.setEnabled(active);
   }

   public boolean isActive() {
      return this.isEnabled();
   }

   public abstract void onEnable();

   public abstract void onDisable();
}
