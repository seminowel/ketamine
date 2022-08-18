package io.github.nevalackin.client.impl.ui.click;

import io.github.nevalackin.client.api.config.ConfigManager;
import io.github.nevalackin.client.api.module.Category;
import io.github.nevalackin.client.api.module.Module;
import io.github.nevalackin.client.api.notification.NotificationType;
import io.github.nevalackin.client.impl.config.Config;
import io.github.nevalackin.client.impl.core.KetamineClient;
import io.github.nevalackin.client.impl.module.ModuleManagerImpl;
import io.github.nevalackin.client.impl.module.render.overlay.Gui;
import io.github.nevalackin.client.impl.property.BooleanProperty;
import io.github.nevalackin.client.impl.property.ColourProperty;
import io.github.nevalackin.client.impl.property.EnumProperty;
import io.github.nevalackin.client.impl.ui.click.components.ModuleComponent;
import io.github.nevalackin.client.impl.ui.click.components.PanelComponent;
import io.github.nevalackin.client.impl.ui.click.components.sub.ButtonComponent;
import io.github.nevalackin.client.impl.ui.click.components.sub.CheckBoxComponent;
import io.github.nevalackin.client.impl.ui.click.components.sub.ColourPickerComponent;
import io.github.nevalackin.client.impl.ui.click.components.sub.ConfigComponent;
import io.github.nevalackin.client.impl.ui.click.components.sub.DropDownComponent;
import io.github.nevalackin.client.impl.ui.click.components.sub.ImageRendererComponent;
import io.github.nevalackin.client.impl.ui.hud.components.HudComponent;
import io.github.nevalackin.client.util.render.BloomUtil;
import io.github.nevalackin.client.util.render.BlurUtil;
import io.github.nevalackin.client.util.render.ColourUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;

public final class GuiUIScreen extends GuiScreen {
   private final PanelComponent configsPanel;
   public Collection configCache;
   private Config selectedConfig;
   private final Collection hudComponents;
   private final List panels = new ArrayList();
   private final List components = new ArrayList();
   private PanelComponent selectedPanel;
   private double prevX;
   private double prevY;
   public final ImageRendererComponent imageRendererComponent;

   public GuiUIScreen() {
      int margin = true;
      int height = true;
      int width = true;
      this.allowUserInput = true;
      int xPos = 201;
      this.configCache = KetamineClient.getInstance().getConfigManager().getConfigs();
      this.imageRendererComponent = new ImageRendererComponent(0.0, 0.0, (double)this.width, (double)this.height);
      this.components.add(this.imageRendererComponent);
      Category[] var5 = Category.values();
      int var6 = var5.length;

      for(int var7 = 0; var7 < var6; ++var7) {
         final Category category = var5[var7];
         PanelComponent panel = new PanelComponent(category.toString(), category.getCategoryName(), category.getColour(), (double)xPos, 1.0, 120.0, 18.0, true) {
            public void initComponents() {
               List modules = (List)KetamineClient.getInstance().getModuleManager().getModules().stream().filter((modulex) -> {
                  return modulex.getCategory() == category;
               }).collect(Collectors.toList());
               this.setChildColourFunc((child) -> {
                  return this.getTabColour();
               });
               Iterator iterator = modules.iterator();

               ModuleComponent moduleComponent;
               for(ModuleComponent previousComponent = null; iterator.hasNext(); previousComponent = moduleComponent) {
                  Module module = (Module)iterator.next();
                  moduleComponent = new ModuleComponent(this, module, 0.0, 0.0, this.getExpandedWidth(), 18.0, !iterator.hasNext(), previousComponent);
                  this.addChild(moduleComponent);
               }

            }
         };
         this.panels.add(panel);
         xPos = (int)((double)xPos + panel.getWidth() + 1.0);
      }

      PanelComponent settingsPanel = new PanelComponent("Settings", "Settings", 1350598784, (double)xPos, 1.0, 120.0, 18.0, false) {
         public void initComponents() {
            this.setChildColourFunc((child) -> {
               return this.getTabColour();
            });
            EnumProperty uiColourProperty = new EnumProperty("UI Colour", GuiUIScreen.UIColour.TAB);
            uiColourProperty.addChangeListener((now) -> {
               switch (now) {
                  case TAB:
                     GuiUIScreen.this.panels.forEach((panel) -> {
                        panel.setChildColourFunc((child) -> {
                           return panel.getTabColour();
                        });
                     });
                     break;
                  case STATIC:
                     GuiUIScreen.this.panels.forEach((panel) -> {
                        panel.setChildColourFunc((child) -> {
                           return (Boolean)Gui.oldGuiProperty.getValue() ? ColourUtil.getClientColour() : ColourUtil.overwriteAlphaComponent(ColourUtil.getClientColour(), 80);
                        });
                     });
                     break;
                  case ASTOLFO:
                     GuiUIScreen.this.panels.forEach((panel) -> {
                        panel.setChildColourFunc((child) -> {
                           return (Boolean)Gui.oldGuiProperty.getValue() ? ColourUtil.blendRainbowColours((long)child.getY() * 5L) : ColourUtil.blendOpacityRainbowColours((long)child.getY() * 5L, 80);
                        });
                     });
                     break;
                  case BLEND:
                     GuiUIScreen.this.panels.forEach((panel) -> {
                        panel.setChildColourFunc((child) -> {
                           return (Boolean)Gui.oldGuiProperty.getValue() ? ColourUtil.fadeBetween(ColourUtil.getClientColour(), ColourUtil.getSecondaryColour(), (long)child.getY() * 5L) : ColourUtil.overwriteAlphaComponent(ColourUtil.fadeBetween(ColourUtil.getClientColour(), ColourUtil.getSecondaryColour(), (long)child.getY() * 5L), 80);
                        });
                     });
               }

            });
            this.addChild(new DropDownComponent(this, uiColourProperty, 0.0, 0.0, this.getExpandedWidth(), 16.0, false));
            BooleanProperty disableBlurProperty = new BooleanProperty("Disable All Blur", false);
            disableBlurProperty.addChangeListener((now) -> {
               BlurUtil.disableBlur = now;
            });
            this.addChild(new CheckBoxComponent(this, disableBlurProperty, 0.0, 0.0, this.getExpandedWidth(), 16.0));
            BooleanProperty disableBloomProperty = new BooleanProperty("Disable All Bloom", false);
            disableBloomProperty.addChangeListener((now) -> {
               BloomUtil.disableBloom = now;
            });
            this.addChild(new CheckBoxComponent(this, disableBloomProperty, 0.0, 0.0, this.getExpandedWidth(), 16.0));
            ColourProperty clientColourProperty = new ColourProperty("Client Colour", ColourUtil.getClientColour(), () -> {
               return uiColourProperty.getValue() == GuiUIScreen.UIColour.STATIC || uiColourProperty.getValue() == GuiUIScreen.UIColour.BLEND;
            });
            clientColourProperty.addChangeListener(ColourUtil::setClientColour);
            this.addChild(new ColourPickerComponent(this, clientColourProperty, 0.0, 0.0, this.getExpandedWidth(), 16.0, false));
            ColourProperty secondaryColourProperty = new ColourProperty("Secondary Colour", ColourUtil.getSecondaryColour(), () -> {
               return uiColourProperty.getValue() == GuiUIScreen.UIColour.BLEND;
            });
            secondaryColourProperty.addChangeListener(ColourUtil::setSecondaryColour);
            this.addChild(new ColourPickerComponent(this, secondaryColourProperty, 0.0, 0.0, this.getExpandedWidth(), 16.0, true));
         }
      };
      this.panels.add(settingsPanel);
      this.configsPanel = new PanelComponent("Configs", "Configs", 1358954495, (double)(xPos + 120 + 1), 1.0, 120.0, 18.0, false) {
         public void initComponents() {
            this.setChildColourFunc((child) -> {
               return this.getTabColour();
            });
            GuiUIScreen.this.configCache.forEach((config) -> {
               this.addChild(new ConfigComponent(this, config, 0.0, 0.0, this.getExpandedWidth(), 16.0));
            });

            for(int i = 0; i < GuiUIScreen.ConfigButton.values().length; ++i) {
               final ConfigButton configButton = GuiUIScreen.ConfigButton.values()[i];
               this.addChild(new ButtonComponent(this, configButton.toString(), 0.0, 0.0, this.getExpandedWidth(), 16.0) {
                  public void onMouseClick(int mouseX, int mouseY, int button) {
                     if (button == 0 && this.isHovered(mouseX, mouseY)) {
                        if (!configButton.equals(GuiUIScreen.ConfigButton.LOAD) && !configButton.equals(GuiUIScreen.ConfigButton.SAVE)) {
                           assert configButton.onClickedRunnable != null;

                           configButton.onClickedRunnable.run();
                        } else {
                           assert configButton.onClicked != null;

                           configButton.onClicked.accept(GuiUIScreen.this.selectedConfig);
                        }
                     }

                  }
               });
            }

         }
      };
      this.panels.add(this.configsPanel);
      this.hudComponents = ModuleManagerImpl.getHudComponents();
   }

   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      GL11.glAlphaFunc(519, 0.0F);
      this.components.forEach((component) -> {
         component.onDraw(new ScaledResolution(this.mc), mouseX, mouseY);
      });
      Iterator var4 = this.hudComponents.iterator();

      HudComponent hudComponent;
      do {
         if (!var4.hasNext()) {
            this.panels.stream().sorted(Comparator.comparing((panel) -> {
               return panel == this.selectedPanel;
            })).forEach((panel) -> {
               panel.onDraw(new ScaledResolution(this.mc), mouseX, mouseY);
            });
            GL11.glAlphaFunc(516, 0.1F);
            return;
         }

         hudComponent = (HudComponent)var4.next();
      } while(!hudComponent.isDragging());

      hudComponent.setX((double)mouseX - this.prevX);
      hudComponent.setY((double)mouseY - this.prevY);
   }

   protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
      Iterator var4;
      if (mouseButton == 0) {
         var4 = this.hudComponents.iterator();

         while(var4.hasNext()) {
            HudComponent hudComponent = (HudComponent)var4.next();
            if (hudComponent.isHovered((double)mouseX, (double)mouseY)) {
               hudComponent.setDragging(true);
               this.prevX = (double)mouseX - hudComponent.getX();
               this.prevY = (double)mouseY - hudComponent.getY();
               return;
            }
         }
      }

      if (this.selectedPanel != null && (this.selectedPanel.isHovered(mouseX, mouseY) || this.selectedPanel.isHoveredExpand(mouseX, mouseY))) {
         this.selectedPanel.onMouseClick(mouseX, mouseY, mouseButton);
      } else {
         var4 = this.panels.iterator();

         while(var4.hasNext()) {
            PanelComponent panel = (PanelComponent)var4.next();
            if (panel.isHovered(mouseX, mouseY) || panel.isHoveredExpand(mouseX, mouseY)) {
               this.selectedPanel = panel;
               break;
            }
         }

         if (this.selectedPanel != null) {
            this.selectedPanel.onMouseClick(mouseX, mouseY, mouseButton);
         }

      }
   }

   protected void keyTyped(char typedChar, int keyCode) throws IOException {
      super.keyTyped(typedChar, keyCode);
      this.panels.forEach((panel) -> {
         panel.onKeyPress(keyCode);
      });
   }

   protected void mouseReleased(int mouseX, int mouseY, int state) {
      super.mouseReleased(mouseX, mouseY, state);
      if (state == 0) {
         Iterator var4 = this.hudComponents.iterator();

         while(var4.hasNext()) {
            HudComponent hudComponent = (HudComponent)var4.next();
            hudComponent.setDragging(false);
         }
      }

      this.panels.forEach((panel) -> {
         panel.onMouseRelease(state);
      });
   }

   public boolean doesGuiPauseGame() {
      return false;
   }

   public void updateConfigs(Collection configCollection) {
      this.configCache = configCollection;
      this.configsPanel.getChildren().clear();
      this.configsPanel.initComponents();
   }

   public void setSelectedConfig(Config selectedConfig) {
      this.selectedConfig = selectedConfig;
   }

   public Config getSelectedConfig() {
      return this.selectedConfig;
   }

   private static enum ConfigButton {
      SAVE,
      LOAD,
      REFRESH;

      private final String name;
      private final Consumer onClicked;
      private final Runnable onClickedRunnable;

      private ConfigButton(String name, Consumer configConsumer) {
         this.name = name;
         this.onClicked = configConsumer;
         this.onClickedRunnable = null;
      }

      private ConfigButton(String name, Runnable onClickedRunnable) {
         this.name = name;
         this.onClicked = null;
         this.onClickedRunnable = onClickedRunnable;
      }

      public String toString() {
         return this.name;
      }

      static {
         ConfigManager var10005 = KetamineClient.getInstance().getConfigManager();
         var10005.getClass();
         SAVE = new ConfigButton("SAVE", 0, "Save", var10005::save);
         LOAD = new ConfigButton("LOAD", 1, "Load", (config) -> {
            KetamineClient.getInstance().getConfigManager().load(config);
            KetamineClient.getInstance().getNotificationManager().add(NotificationType.SUCCESS, "Config", "Successfully Loaded Config: " + config.getName(), 1500L);
         });
         var10005 = KetamineClient.getInstance().getConfigManager();
         var10005.getClass();
         REFRESH = new ConfigButton("REFRESH", 2, "Refresh", var10005::refresh);
      }
   }

   private static enum Position {
      LEFT("Left"),
      CENTER("Center");

      private final String name;

      private Position(String name) {
         this.name = name;
      }

      public String toString() {
         return this.name;
      }
   }

   private static enum UIColour {
      TAB("Tab"),
      STATIC("Static"),
      ASTOLFO("Astolfo"),
      BLEND("Blend");

      private final String name;

      private UIColour(String name) {
         this.name = name;
      }

      public String toString() {
         return this.name;
      }
   }
}
