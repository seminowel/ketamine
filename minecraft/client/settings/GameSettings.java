package net.minecraft.client.settings;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import io.github.nevalackin.client.impl.core.KetamineClient;
import io.github.nevalackin.client.impl.event.render.game.GetThirdPersonViewEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.network.play.client.C15PacketClientSettings;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumDifficulty;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

public class GameSettings {
   private static final Logger logger = LogManager.getLogger();
   private static final Gson gson = new Gson();
   private static final ParameterizedType typeListString = new ParameterizedType() {
      public Type[] getActualTypeArguments() {
         return new Type[]{String.class};
      }

      public Type getRawType() {
         return List.class;
      }

      public Type getOwnerType() {
         return null;
      }
   };
   private static final String[] GUISCALES = new String[]{"options.guiScale.auto", "options.guiScale.small", "options.guiScale.normal", "options.guiScale.large"};
   private static final String[] PARTICLES = new String[]{"options.particles.all", "options.particles.decreased", "options.particles.minimal"};
   private static final String[] AMBIENT_OCCLUSIONS = new String[]{"options.ao.off", "options.ao.min", "options.ao.max"};
   private static final String[] field_181149_aW = new String[]{"options.off", "options.graphics.fast", "options.graphics.fancy"};
   public float mouseSensitivity = 0.5F;
   public boolean invertMouse;
   public int renderDistanceChunks = -1;
   public boolean viewBobbing = true;
   public boolean fboEnable = true;
   public int limitFramerate = 120;
   public int clouds = 2;
   public boolean fancyGraphics = true;
   public int ambientOcclusion = 2;
   public List resourcePacks = Lists.newArrayList();
   public List field_183018_l = Lists.newArrayList();
   public EntityPlayer.EnumChatVisibility chatVisibility;
   public boolean chatColours;
   public boolean chatLinks;
   public boolean chatLinksPrompt;
   public float chatOpacity;
   public boolean snooperEnabled;
   public boolean fullScreen;
   public boolean enableVsync;
   public boolean useVbo;
   public boolean allowBlockAlternatives;
   public boolean reducedDebugInfo;
   public boolean hideServerAddress;
   public boolean advancedItemTooltips;
   public boolean pauseOnLostFocus;
   private final Set setModelParts;
   public boolean touchscreen;
   public int overrideWidth;
   public int overrideHeight;
   public boolean heldItemTooltips;
   public float chatScale;
   public float chatWidth;
   public float chatHeightUnfocused;
   public float chatHeightFocused;
   public boolean showInventoryAchievementHint;
   public int mipmapLevels;
   private final Map mapSoundLevels;
   public boolean field_181150_U;
   public boolean field_181151_V;
   public KeyBinding keyBindForward;
   public KeyBinding keyBindLeft;
   public KeyBinding keyBindBack;
   public KeyBinding keyBindRight;
   public KeyBinding keyBindJump;
   public KeyBinding keyBindSneak;
   public KeyBinding keyBindSprint;
   public KeyBinding keyBindInventory;
   public KeyBinding keyBindUseItem;
   public KeyBinding keyBindDrop;
   public KeyBinding keyBindAttack;
   public KeyBinding keyBindPickBlock;
   public KeyBinding keyBindChat;
   public KeyBinding keyBindPlayerList;
   public KeyBinding keyBindCommand;
   public KeyBinding keyBindScreenshot;
   public KeyBinding keyBindTogglePerspective;
   public KeyBinding keyBindSmoothCamera;
   public KeyBinding keyBindFullscreen;
   public KeyBinding[] keyBindsHotbar;
   public KeyBinding[] keyBindings;
   protected Minecraft mc;
   private File optionsFile;
   public EnumDifficulty difficulty;
   public boolean hideGUI;
   public int thirdPersonView;
   public boolean showDebugInfo;
   public boolean showDebugProfilerChart;
   public boolean field_181657_aC;
   public String lastServer;
   public boolean smoothCamera;
   public float fovSetting;
   public float gammaSetting;
   public float saturation;
   public int guiScale;
   public int particleSetting;
   public String language;
   public boolean forceUnicodeFont;

   public GameSettings(Minecraft mcIn, File p_i46326_2_) {
      this.chatVisibility = EntityPlayer.EnumChatVisibility.FULL;
      this.chatColours = true;
      this.chatLinks = true;
      this.chatLinksPrompt = true;
      this.chatOpacity = 1.0F;
      this.snooperEnabled = true;
      this.enableVsync = true;
      this.useVbo = false;
      this.allowBlockAlternatives = true;
      this.reducedDebugInfo = false;
      this.pauseOnLostFocus = true;
      this.setModelParts = Sets.newHashSet(EnumPlayerModelParts.values());
      this.heldItemTooltips = true;
      this.chatScale = 1.0F;
      this.chatWidth = 1.0F;
      this.chatHeightUnfocused = 0.44366196F;
      this.chatHeightFocused = 1.0F;
      this.showInventoryAchievementHint = true;
      this.mipmapLevels = 4;
      this.mapSoundLevels = Maps.newEnumMap(SoundCategory.class);
      this.field_181150_U = true;
      this.field_181151_V = true;
      this.keyBindForward = new KeyBinding("key.forward", 17, "key.categories.movement");
      this.keyBindLeft = new KeyBinding("key.left", 30, "key.categories.movement");
      this.keyBindBack = new KeyBinding("key.back", 31, "key.categories.movement");
      this.keyBindRight = new KeyBinding("key.right", 32, "key.categories.movement");
      this.keyBindJump = new KeyBinding("key.jump", 57, "key.categories.movement");
      this.keyBindSneak = new KeyBinding("key.sneak", 42, "key.categories.movement");
      this.keyBindSprint = new KeyBinding("key.sprint", 29, "key.categories.movement");
      this.keyBindInventory = new KeyBinding("key.inventory", 18, "key.categories.inventory");
      this.keyBindUseItem = new KeyBinding("key.use", -99, "key.categories.gameplay");
      this.keyBindDrop = new KeyBinding("key.drop", 16, "key.categories.gameplay");
      this.keyBindAttack = new KeyBinding("key.attack", -100, "key.categories.gameplay");
      this.keyBindPickBlock = new KeyBinding("key.pickItem", -98, "key.categories.gameplay");
      this.keyBindChat = new KeyBinding("key.chat", 20, "key.categories.multiplayer");
      this.keyBindPlayerList = new KeyBinding("key.playerlist", 15, "key.categories.multiplayer");
      this.keyBindCommand = new KeyBinding("key.command", 53, "key.categories.multiplayer");
      this.keyBindScreenshot = new KeyBinding("key.screenshot", 60, "key.categories.misc");
      this.keyBindTogglePerspective = new KeyBinding("key.togglePerspective", 63, "key.categories.misc");
      this.keyBindSmoothCamera = new KeyBinding("key.smoothCamera", 0, "key.categories.misc");
      this.keyBindFullscreen = new KeyBinding("key.fullscreen", 87, "key.categories.misc");
      this.keyBindsHotbar = new KeyBinding[]{new KeyBinding("key.hotbar.1", 2, "key.categories.inventory"), new KeyBinding("key.hotbar.2", 3, "key.categories.inventory"), new KeyBinding("key.hotbar.3", 4, "key.categories.inventory"), new KeyBinding("key.hotbar.4", 5, "key.categories.inventory"), new KeyBinding("key.hotbar.5", 6, "key.categories.inventory"), new KeyBinding("key.hotbar.6", 7, "key.categories.inventory"), new KeyBinding("key.hotbar.7", 8, "key.categories.inventory"), new KeyBinding("key.hotbar.8", 9, "key.categories.inventory"), new KeyBinding("key.hotbar.9", 10, "key.categories.inventory")};
      this.keyBindings = (KeyBinding[])ArrayUtils.addAll(new KeyBinding[]{this.keyBindAttack, this.keyBindUseItem, this.keyBindForward, this.keyBindLeft, this.keyBindBack, this.keyBindRight, this.keyBindJump, this.keyBindSneak, this.keyBindSprint, this.keyBindDrop, this.keyBindInventory, this.keyBindChat, this.keyBindPlayerList, this.keyBindPickBlock, this.keyBindCommand, this.keyBindScreenshot, this.keyBindTogglePerspective, this.keyBindSmoothCamera, this.keyBindFullscreen}, this.keyBindsHotbar);
      this.difficulty = EnumDifficulty.NORMAL;
      this.lastServer = "";
      this.fovSetting = 70.0F;
      this.language = "en_US";
      this.forceUnicodeFont = false;
      this.mc = mcIn;
      this.optionsFile = new File(p_i46326_2_, "options.txt");
      if (mcIn.isJava64bit() && Runtime.getRuntime().maxMemory() >= 1000000000L) {
         GameSettings.Options.RENDER_DISTANCE.setValueMax(32.0F);
      } else {
         GameSettings.Options.RENDER_DISTANCE.setValueMax(16.0F);
      }

      this.renderDistanceChunks = mcIn.isJava64bit() ? 12 : 8;
      this.loadOptions();
   }

   public GameSettings() {
      this.chatVisibility = EntityPlayer.EnumChatVisibility.FULL;
      this.chatColours = true;
      this.chatLinks = true;
      this.chatLinksPrompt = true;
      this.chatOpacity = 1.0F;
      this.snooperEnabled = true;
      this.enableVsync = true;
      this.useVbo = false;
      this.allowBlockAlternatives = true;
      this.reducedDebugInfo = false;
      this.pauseOnLostFocus = true;
      this.setModelParts = Sets.newHashSet(EnumPlayerModelParts.values());
      this.heldItemTooltips = true;
      this.chatScale = 1.0F;
      this.chatWidth = 1.0F;
      this.chatHeightUnfocused = 0.44366196F;
      this.chatHeightFocused = 1.0F;
      this.showInventoryAchievementHint = true;
      this.mipmapLevels = 4;
      this.mapSoundLevels = Maps.newEnumMap(SoundCategory.class);
      this.field_181150_U = true;
      this.field_181151_V = true;
      this.keyBindForward = new KeyBinding("key.forward", 17, "key.categories.movement");
      this.keyBindLeft = new KeyBinding("key.left", 30, "key.categories.movement");
      this.keyBindBack = new KeyBinding("key.back", 31, "key.categories.movement");
      this.keyBindRight = new KeyBinding("key.right", 32, "key.categories.movement");
      this.keyBindJump = new KeyBinding("key.jump", 57, "key.categories.movement");
      this.keyBindSneak = new KeyBinding("key.sneak", 42, "key.categories.movement");
      this.keyBindSprint = new KeyBinding("key.sprint", 29, "key.categories.movement");
      this.keyBindInventory = new KeyBinding("key.inventory", 18, "key.categories.inventory");
      this.keyBindUseItem = new KeyBinding("key.use", -99, "key.categories.gameplay");
      this.keyBindDrop = new KeyBinding("key.drop", 16, "key.categories.gameplay");
      this.keyBindAttack = new KeyBinding("key.attack", -100, "key.categories.gameplay");
      this.keyBindPickBlock = new KeyBinding("key.pickItem", -98, "key.categories.gameplay");
      this.keyBindChat = new KeyBinding("key.chat", 20, "key.categories.multiplayer");
      this.keyBindPlayerList = new KeyBinding("key.playerlist", 15, "key.categories.multiplayer");
      this.keyBindCommand = new KeyBinding("key.command", 53, "key.categories.multiplayer");
      this.keyBindScreenshot = new KeyBinding("key.screenshot", 60, "key.categories.misc");
      this.keyBindTogglePerspective = new KeyBinding("key.togglePerspective", 63, "key.categories.misc");
      this.keyBindSmoothCamera = new KeyBinding("key.smoothCamera", 0, "key.categories.misc");
      this.keyBindFullscreen = new KeyBinding("key.fullscreen", 87, "key.categories.misc");
      this.keyBindsHotbar = new KeyBinding[]{new KeyBinding("key.hotbar.1", 2, "key.categories.inventory"), new KeyBinding("key.hotbar.2", 3, "key.categories.inventory"), new KeyBinding("key.hotbar.3", 4, "key.categories.inventory"), new KeyBinding("key.hotbar.4", 5, "key.categories.inventory"), new KeyBinding("key.hotbar.5", 6, "key.categories.inventory"), new KeyBinding("key.hotbar.6", 7, "key.categories.inventory"), new KeyBinding("key.hotbar.7", 8, "key.categories.inventory"), new KeyBinding("key.hotbar.8", 9, "key.categories.inventory"), new KeyBinding("key.hotbar.9", 10, "key.categories.inventory")};
      this.keyBindings = (KeyBinding[])ArrayUtils.addAll(new KeyBinding[]{this.keyBindAttack, this.keyBindUseItem, this.keyBindForward, this.keyBindLeft, this.keyBindBack, this.keyBindRight, this.keyBindJump, this.keyBindSneak, this.keyBindSprint, this.keyBindDrop, this.keyBindInventory, this.keyBindChat, this.keyBindPlayerList, this.keyBindPickBlock, this.keyBindCommand, this.keyBindScreenshot, this.keyBindTogglePerspective, this.keyBindSmoothCamera, this.keyBindFullscreen}, this.keyBindsHotbar);
      this.difficulty = EnumDifficulty.NORMAL;
      this.lastServer = "";
      this.fovSetting = 70.0F;
      this.language = "en_US";
      this.forceUnicodeFont = false;
   }

   public static String getKeyDisplayString(int p_74298_0_) {
      return p_74298_0_ < 0 ? I18n.format("key.mouseButton", p_74298_0_ + 101) : (p_74298_0_ < 256 ? Keyboard.getKeyName(p_74298_0_) : String.format("%c", (char)(p_74298_0_ - 256)).toUpperCase());
   }

   public static boolean isKeyDown(KeyBinding p_100015_0_) {
      boolean var10000;
      label25: {
         if (p_100015_0_.getKeyCode() != 0) {
            if (p_100015_0_.getKeyCode() < 0) {
               if (Mouse.isButtonDown(p_100015_0_.getKeyCode() + 100)) {
                  break label25;
               }
            } else if (Keyboard.isKeyDown(p_100015_0_.getKeyCode())) {
               break label25;
            }
         }

         var10000 = false;
         return var10000;
      }

      var10000 = true;
      return var10000;
   }

   private static String getTranslation(String[] p_74299_0_, int p_74299_1_) {
      if (p_74299_1_ < 0 || p_74299_1_ >= p_74299_0_.length) {
         p_74299_1_ = 0;
      }

      return I18n.format(p_74299_0_[p_74299_1_]);
   }

   public int getThirdPersonView() {
      GetThirdPersonViewEvent event = new GetThirdPersonViewEvent(this.thirdPersonView);
      KetamineClient.getInstance().getEventBus().post(event);
      return event.getThirdPersonView();
   }

   public void setOptionKeyBinding(KeyBinding p_151440_1_, int p_151440_2_) {
      p_151440_1_.setKeyCode(p_151440_2_);
      this.saveOptions();
   }

   public void setOptionFloatValue(Options p_74304_1_, float p_74304_2_) {
      if (p_74304_1_ == GameSettings.Options.SENSITIVITY) {
         this.mouseSensitivity = p_74304_2_;
      }

      if (p_74304_1_ == GameSettings.Options.FOV) {
         this.fovSetting = p_74304_2_;
      }

      if (p_74304_1_ == GameSettings.Options.GAMMA) {
         this.gammaSetting = p_74304_2_;
      }

      if (p_74304_1_ == GameSettings.Options.FRAMERATE_LIMIT) {
         this.limitFramerate = (int)p_74304_2_;
      }

      if (p_74304_1_ == GameSettings.Options.CHAT_OPACITY) {
         this.chatOpacity = p_74304_2_;
         this.mc.ingameGUI.getChatGUI().refreshChat();
      }

      if (p_74304_1_ == GameSettings.Options.CHAT_HEIGHT_FOCUSED) {
         this.chatHeightFocused = p_74304_2_;
         this.mc.ingameGUI.getChatGUI().refreshChat();
      }

      if (p_74304_1_ == GameSettings.Options.CHAT_HEIGHT_UNFOCUSED) {
         this.chatHeightUnfocused = p_74304_2_;
         this.mc.ingameGUI.getChatGUI().refreshChat();
      }

      if (p_74304_1_ == GameSettings.Options.CHAT_WIDTH) {
         this.chatWidth = p_74304_2_;
         this.mc.ingameGUI.getChatGUI().refreshChat();
      }

      if (p_74304_1_ == GameSettings.Options.CHAT_SCALE) {
         this.chatScale = p_74304_2_;
         this.mc.ingameGUI.getChatGUI().refreshChat();
      }

      if (p_74304_1_ == GameSettings.Options.MIPMAP_LEVELS) {
         int i = this.mipmapLevels;
         this.mipmapLevels = (int)p_74304_2_;
         if ((float)i != p_74304_2_) {
            this.mc.getTextureMapBlocks().setMipmapLevels(this.mipmapLevels);
            this.mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
            this.mc.getTextureMapBlocks().setBlurMipmapDirect(false, this.mipmapLevels > 0);
            this.mc.scheduleResourcesRefresh();
         }
      }

      if (p_74304_1_ == GameSettings.Options.BLOCK_ALTERNATIVES) {
         this.allowBlockAlternatives = !this.allowBlockAlternatives;
         this.mc.renderGlobal.loadRenderers();
      }

      if (p_74304_1_ == GameSettings.Options.RENDER_DISTANCE) {
         this.renderDistanceChunks = (int)p_74304_2_;
         this.mc.renderGlobal.setDisplayListEntitiesDirty();
      }

   }

   public void setOptionValue(Options p_74306_1_, int p_74306_2_) {
      if (p_74306_1_ == GameSettings.Options.INVERT_MOUSE) {
         this.invertMouse = !this.invertMouse;
      }

      if (p_74306_1_ == GameSettings.Options.GUI_SCALE) {
         this.guiScale = this.guiScale + p_74306_2_ & 3;
      }

      if (p_74306_1_ == GameSettings.Options.PARTICLES) {
         this.particleSetting = (this.particleSetting + p_74306_2_) % 3;
      }

      if (p_74306_1_ == GameSettings.Options.VIEW_BOBBING) {
         this.viewBobbing = !this.viewBobbing;
      }

      if (p_74306_1_ == GameSettings.Options.RENDER_CLOUDS) {
         this.clouds = (this.clouds + p_74306_2_) % 3;
      }

      if (p_74306_1_ == GameSettings.Options.FORCE_UNICODE_FONT) {
         this.forceUnicodeFont = !this.forceUnicodeFont;
         this.mc.fontRendererObj.setUnicodeFlag(this.mc.getLanguageManager().isCurrentLocaleUnicode() || this.forceUnicodeFont);
      }

      if (p_74306_1_ == GameSettings.Options.FBO_ENABLE) {
         this.fboEnable = !this.fboEnable;
      }

      if (p_74306_1_ == GameSettings.Options.GRAPHICS) {
         this.fancyGraphics = !this.fancyGraphics;
         this.mc.renderGlobal.loadRenderers();
      }

      if (p_74306_1_ == GameSettings.Options.AMBIENT_OCCLUSION) {
         this.ambientOcclusion = (this.ambientOcclusion + p_74306_2_) % 3;
         this.mc.renderGlobal.loadRenderers();
      }

      if (p_74306_1_ == GameSettings.Options.CHAT_VISIBILITY) {
         this.chatVisibility = EntityPlayer.EnumChatVisibility.getEnumChatVisibility((this.chatVisibility.getChatVisibility() + p_74306_2_) % 3);
      }

      if (p_74306_1_ == GameSettings.Options.CHAT_COLOR) {
         this.chatColours = !this.chatColours;
      }

      if (p_74306_1_ == GameSettings.Options.CHAT_LINKS) {
         this.chatLinks = !this.chatLinks;
      }

      if (p_74306_1_ == GameSettings.Options.CHAT_LINKS_PROMPT) {
         this.chatLinksPrompt = !this.chatLinksPrompt;
      }

      if (p_74306_1_ == GameSettings.Options.SNOOPER_ENABLED) {
         this.snooperEnabled = !this.snooperEnabled;
      }

      if (p_74306_1_ == GameSettings.Options.TOUCHSCREEN) {
         this.touchscreen = !this.touchscreen;
      }

      if (p_74306_1_ == GameSettings.Options.USE_FULLSCREEN) {
         this.fullScreen = !this.fullScreen;
         if (this.mc.isFullScreen() != this.fullScreen) {
            this.mc.toggleFullscreen();
         }
      }

      if (p_74306_1_ == GameSettings.Options.ENABLE_VSYNC) {
         this.enableVsync = !this.enableVsync;
         Display.setVSyncEnabled(this.enableVsync);
      }

      if (p_74306_1_ == GameSettings.Options.USE_VBO) {
         this.useVbo = !this.useVbo;
         this.mc.renderGlobal.loadRenderers();
      }

      if (p_74306_1_ == GameSettings.Options.BLOCK_ALTERNATIVES) {
         this.allowBlockAlternatives = !this.allowBlockAlternatives;
         this.mc.renderGlobal.loadRenderers();
      }

      if (p_74306_1_ == GameSettings.Options.REDUCED_DEBUG_INFO) {
         this.reducedDebugInfo = !this.reducedDebugInfo;
      }

      if (p_74306_1_ == GameSettings.Options.ENTITY_SHADOWS) {
         this.field_181151_V = !this.field_181151_V;
      }

      this.saveOptions();
   }

   public float getOptionFloatValue(Options p_74296_1_) {
      return p_74296_1_ == GameSettings.Options.FOV ? this.fovSetting : (p_74296_1_ == GameSettings.Options.GAMMA ? this.gammaSetting : (p_74296_1_ == GameSettings.Options.SATURATION ? this.saturation : (p_74296_1_ == GameSettings.Options.SENSITIVITY ? this.mouseSensitivity : (p_74296_1_ == GameSettings.Options.CHAT_OPACITY ? this.chatOpacity : (p_74296_1_ == GameSettings.Options.CHAT_HEIGHT_FOCUSED ? this.chatHeightFocused : (p_74296_1_ == GameSettings.Options.CHAT_HEIGHT_UNFOCUSED ? this.chatHeightUnfocused : (p_74296_1_ == GameSettings.Options.CHAT_SCALE ? this.chatScale : (p_74296_1_ == GameSettings.Options.CHAT_WIDTH ? this.chatWidth : (p_74296_1_ == GameSettings.Options.FRAMERATE_LIMIT ? (float)this.limitFramerate : (p_74296_1_ == GameSettings.Options.MIPMAP_LEVELS ? (float)this.mipmapLevels : (p_74296_1_ == GameSettings.Options.RENDER_DISTANCE ? (float)this.renderDistanceChunks : 0.0F)))))))))));
   }

   public boolean getOptionOrdinalValue(Options p_74308_1_) {
      switch (p_74308_1_) {
         case INVERT_MOUSE:
            return this.invertMouse;
         case VIEW_BOBBING:
            return this.viewBobbing;
         case FBO_ENABLE:
            return this.fboEnable;
         case CHAT_COLOR:
            return this.chatColours;
         case CHAT_LINKS:
            return this.chatLinks;
         case CHAT_LINKS_PROMPT:
            return this.chatLinksPrompt;
         case SNOOPER_ENABLED:
            return this.snooperEnabled;
         case USE_FULLSCREEN:
            return this.fullScreen;
         case ENABLE_VSYNC:
            return this.enableVsync;
         case USE_VBO:
            return this.useVbo;
         case TOUCHSCREEN:
            return this.touchscreen;
         case FORCE_UNICODE_FONT:
            return this.forceUnicodeFont;
         case BLOCK_ALTERNATIVES:
            return this.allowBlockAlternatives;
         case REDUCED_DEBUG_INFO:
            return this.reducedDebugInfo;
         case ENTITY_SHADOWS:
            return this.field_181151_V;
         default:
            return false;
      }
   }

   public String getKeyBinding(Options p_74297_1_) {
      String s = I18n.format(p_74297_1_.getEnumString()) + ": ";
      if (p_74297_1_.getEnumFloat()) {
         float f1 = this.getOptionFloatValue(p_74297_1_);
         float f = p_74297_1_.normalizeValue(f1);
         return p_74297_1_ == GameSettings.Options.SENSITIVITY ? (f == 0.0F ? s + I18n.format("options.sensitivity.min") : (f == 1.0F ? s + I18n.format("options.sensitivity.max") : s + (int)(f * 200.0F) + "%")) : (p_74297_1_ == GameSettings.Options.FOV ? (f1 == 70.0F ? s + I18n.format("options.fov.min") : (f1 == 110.0F ? s + I18n.format("options.fov.max") : s + (int)f1)) : (p_74297_1_ == GameSettings.Options.FRAMERATE_LIMIT ? (f1 == p_74297_1_.valueMax ? s + I18n.format("options.framerateLimit.max") : s + (int)f1 + " fps") : (p_74297_1_ == GameSettings.Options.RENDER_CLOUDS ? (f1 == p_74297_1_.valueMin ? s + I18n.format("options.cloudHeight.min") : s + ((int)f1 + 128)) : (p_74297_1_ == GameSettings.Options.GAMMA ? (f == 0.0F ? s + I18n.format("options.gamma.min") : (f == 1.0F ? s + I18n.format("options.gamma.max") : s + "+" + (int)(f * 100.0F) + "%")) : (p_74297_1_ == GameSettings.Options.SATURATION ? s + (int)(f * 400.0F) + "%" : (p_74297_1_ == GameSettings.Options.CHAT_OPACITY ? s + (int)(f * 90.0F + 10.0F) + "%" : (p_74297_1_ == GameSettings.Options.CHAT_HEIGHT_UNFOCUSED ? s + GuiNewChat.calculateChatboxHeight(f) + "px" : (p_74297_1_ == GameSettings.Options.CHAT_HEIGHT_FOCUSED ? s + GuiNewChat.calculateChatboxHeight(f) + "px" : (p_74297_1_ == GameSettings.Options.CHAT_WIDTH ? s + GuiNewChat.calculateChatboxWidth(f) + "px" : (p_74297_1_ == GameSettings.Options.RENDER_DISTANCE ? s + (int)f1 + " chunks" : (p_74297_1_ == GameSettings.Options.MIPMAP_LEVELS ? (f1 == 0.0F ? s + I18n.format("options.off") : s + (int)f1) : (f == 0.0F ? s + I18n.format("options.off") : s + (int)(f * 100.0F) + "%"))))))))))));
      } else if (p_74297_1_.getEnumBoolean()) {
         boolean flag = this.getOptionOrdinalValue(p_74297_1_);
         return flag ? s + I18n.format("options.on") : s + I18n.format("options.off");
      } else if (p_74297_1_ == GameSettings.Options.GUI_SCALE) {
         return s + getTranslation(GUISCALES, this.guiScale);
      } else if (p_74297_1_ == GameSettings.Options.CHAT_VISIBILITY) {
         return s + I18n.format(this.chatVisibility.getResourceKey());
      } else if (p_74297_1_ == GameSettings.Options.PARTICLES) {
         return s + getTranslation(PARTICLES, this.particleSetting);
      } else if (p_74297_1_ == GameSettings.Options.AMBIENT_OCCLUSION) {
         return s + getTranslation(AMBIENT_OCCLUSIONS, this.ambientOcclusion);
      } else if (p_74297_1_ == GameSettings.Options.RENDER_CLOUDS) {
         return s + getTranslation(field_181149_aW, this.clouds);
      } else if (p_74297_1_ == GameSettings.Options.GRAPHICS) {
         if (this.fancyGraphics) {
            return s + I18n.format("options.graphics.fancy");
         } else {
            String s1 = "options.graphics.fast";
            return s + I18n.format("options.graphics.fast");
         }
      } else {
         return s;
      }
   }

   public void loadOptions() {
      try {
         if (!this.optionsFile.exists()) {
            return;
         }

         BufferedReader bufferedreader = new BufferedReader(new FileReader(this.optionsFile));
         String s = "";
         this.mapSoundLevels.clear();

         while((s = bufferedreader.readLine()) != null) {
            try {
               String[] astring = s.split(":");
               if (astring[0].equals("mouseSensitivity")) {
                  this.mouseSensitivity = this.parseFloat(astring[1]);
               }

               if (astring[0].equals("fov")) {
                  this.fovSetting = this.parseFloat(astring[1]) * 40.0F + 70.0F;
               }

               if (astring[0].equals("gamma")) {
                  this.gammaSetting = this.parseFloat(astring[1]);
               }

               if (astring[0].equals("saturation")) {
                  this.saturation = this.parseFloat(astring[1]);
               }

               if (astring[0].equals("invertYMouse")) {
                  this.invertMouse = astring[1].equals("true");
               }

               if (astring[0].equals("renderDistance")) {
                  this.renderDistanceChunks = Integer.parseInt(astring[1]);
               }

               if (astring[0].equals("guiScale")) {
                  this.guiScale = Integer.parseInt(astring[1]);
               }

               if (astring[0].equals("particles")) {
                  this.particleSetting = Integer.parseInt(astring[1]);
               }

               if (astring[0].equals("bobView")) {
                  this.viewBobbing = astring[1].equals("true");
               }

               if (astring[0].equals("maxFps")) {
                  this.limitFramerate = Integer.parseInt(astring[1]);
               }

               if (astring[0].equals("fboEnable")) {
                  this.fboEnable = astring[1].equals("true");
               }

               if (astring[0].equals("difficulty")) {
                  this.difficulty = EnumDifficulty.getDifficultyEnum(Integer.parseInt(astring[1]));
               }

               if (astring[0].equals("fancyGraphics")) {
                  this.fancyGraphics = astring[1].equals("true");
               }

               if (astring[0].equals("ao")) {
                  if (astring[1].equals("true")) {
                     this.ambientOcclusion = 2;
                  } else if (astring[1].equals("false")) {
                     this.ambientOcclusion = 0;
                  } else {
                     this.ambientOcclusion = Integer.parseInt(astring[1]);
                  }
               }

               if (astring[0].equals("renderClouds")) {
                  if (astring[1].equals("true")) {
                     this.clouds = 2;
                  } else if (astring[1].equals("false")) {
                     this.clouds = 0;
                  } else if (astring[1].equals("fast")) {
                     this.clouds = 1;
                  }
               }

               if (astring[0].equals("resourcePacks")) {
                  this.resourcePacks = (List)gson.fromJson(s.substring(s.indexOf(58) + 1), typeListString);
                  if (this.resourcePacks == null) {
                     this.resourcePacks = Lists.newArrayList();
                  }
               }

               if (astring[0].equals("incompatibleResourcePacks")) {
                  this.field_183018_l = (List)gson.fromJson(s.substring(s.indexOf(58) + 1), typeListString);
                  if (this.field_183018_l == null) {
                     this.field_183018_l = Lists.newArrayList();
                  }
               }

               if (astring[0].equals("lastServer") && astring.length >= 2) {
                  this.lastServer = s.substring(s.indexOf(58) + 1);
               }

               if (astring[0].equals("lang") && astring.length >= 2) {
                  this.language = astring[1];
               }

               if (astring[0].equals("chatVisibility")) {
                  this.chatVisibility = EntityPlayer.EnumChatVisibility.getEnumChatVisibility(Integer.parseInt(astring[1]));
               }

               if (astring[0].equals("chatColors")) {
                  this.chatColours = astring[1].equals("true");
               }

               if (astring[0].equals("chatLinks")) {
                  this.chatLinks = astring[1].equals("true");
               }

               if (astring[0].equals("chatLinksPrompt")) {
                  this.chatLinksPrompt = astring[1].equals("true");
               }

               if (astring[0].equals("chatOpacity")) {
                  this.chatOpacity = this.parseFloat(astring[1]);
               }

               if (astring[0].equals("snooperEnabled")) {
                  this.snooperEnabled = astring[1].equals("true");
               }

               if (astring[0].equals("fullscreen")) {
                  this.fullScreen = astring[1].equals("true");
               }

               if (astring[0].equals("enableVsync")) {
                  this.enableVsync = astring[1].equals("true");
               }

               if (astring[0].equals("useVbo")) {
                  this.useVbo = astring[1].equals("true");
               }

               if (astring[0].equals("hideServerAddress")) {
                  this.hideServerAddress = astring[1].equals("true");
               }

               if (astring[0].equals("advancedItemTooltips")) {
                  this.advancedItemTooltips = astring[1].equals("true");
               }

               if (astring[0].equals("pauseOnLostFocus")) {
                  this.pauseOnLostFocus = astring[1].equals("true");
               }

               if (astring[0].equals("touchscreen")) {
                  this.touchscreen = astring[1].equals("true");
               }

               if (astring[0].equals("overrideHeight")) {
                  this.overrideHeight = Integer.parseInt(astring[1]);
               }

               if (astring[0].equals("overrideWidth")) {
                  this.overrideWidth = Integer.parseInt(astring[1]);
               }

               if (astring[0].equals("heldItemTooltips")) {
                  this.heldItemTooltips = astring[1].equals("true");
               }

               if (astring[0].equals("chatHeightFocused")) {
                  this.chatHeightFocused = this.parseFloat(astring[1]);
               }

               if (astring[0].equals("chatHeightUnfocused")) {
                  this.chatHeightUnfocused = this.parseFloat(astring[1]);
               }

               if (astring[0].equals("chatScale")) {
                  this.chatScale = this.parseFloat(astring[1]);
               }

               if (astring[0].equals("chatWidth")) {
                  this.chatWidth = this.parseFloat(astring[1]);
               }

               if (astring[0].equals("showInventoryAchievementHint")) {
                  this.showInventoryAchievementHint = astring[1].equals("true");
               }

               if (astring[0].equals("mipmapLevels")) {
                  this.mipmapLevels = Integer.parseInt(astring[1]);
               }

               if (astring[0].equals("forceUnicodeFont")) {
                  this.forceUnicodeFont = astring[1].equals("true");
               }

               if (astring[0].equals("allowBlockAlternatives")) {
                  this.allowBlockAlternatives = astring[1].equals("true");
               }

               if (astring[0].equals("reducedDebugInfo")) {
                  this.reducedDebugInfo = astring[1].equals("true");
               }

               if (astring[0].equals("useNativeTransport")) {
                  this.field_181150_U = astring[1].equals("true");
               }

               if (astring[0].equals("entityShadows")) {
                  this.field_181151_V = astring[1].equals("true");
               }

               KeyBinding[] var4 = this.keyBindings;
               int var5 = var4.length;

               int var6;
               for(var6 = 0; var6 < var5; ++var6) {
                  KeyBinding keybinding = var4[var6];
                  if (astring[0].equals("key_" + keybinding.getKeyDescription())) {
                     keybinding.setKeyCode(Integer.parseInt(astring[1]));
                  }
               }

               SoundCategory[] var10 = SoundCategory.values();
               var5 = var10.length;

               for(var6 = 0; var6 < var5; ++var6) {
                  SoundCategory soundcategory = var10[var6];
                  if (astring[0].equals("soundCategory_" + soundcategory.getCategoryName())) {
                     this.mapSoundLevels.put(soundcategory, this.parseFloat(astring[1]));
                  }
               }

               EnumPlayerModelParts[] var11 = EnumPlayerModelParts.values();
               var5 = var11.length;

               for(var6 = 0; var6 < var5; ++var6) {
                  EnumPlayerModelParts enumplayermodelparts = var11[var6];
                  if (astring[0].equals("modelPart_" + enumplayermodelparts.getPartName())) {
                     this.setModelPartEnabled(enumplayermodelparts, astring[1].equals("true"));
                  }
               }
            } catch (Exception var8) {
               logger.warn("Skipping bad option: " + s);
            }
         }

         KeyBinding.resetKeyBindingArrayAndHash();
         bufferedreader.close();
      } catch (Exception var9) {
         logger.error("Failed to load options", var9);
      }

   }

   private float parseFloat(String p_74305_1_) {
      return p_74305_1_.equals("true") ? 1.0F : (p_74305_1_.equals("false") ? 0.0F : Float.parseFloat(p_74305_1_));
   }

   public void saveOptions() {
      try {
         PrintWriter printwriter = new PrintWriter(new FileWriter(this.optionsFile));
         printwriter.println("invertYMouse:" + this.invertMouse);
         printwriter.println("mouseSensitivity:" + this.mouseSensitivity);
         printwriter.println("fov:" + (this.fovSetting - 70.0F) / 40.0F);
         printwriter.println("gamma:" + this.gammaSetting);
         printwriter.println("saturation:" + this.saturation);
         printwriter.println("renderDistance:" + this.renderDistanceChunks);
         printwriter.println("guiScale:" + this.guiScale);
         printwriter.println("particles:" + this.particleSetting);
         printwriter.println("bobView:" + this.viewBobbing);
         printwriter.println("maxFps:" + this.limitFramerate);
         printwriter.println("fboEnable:" + this.fboEnable);
         printwriter.println("difficulty:" + this.difficulty.getDifficultyId());
         printwriter.println("fancyGraphics:" + this.fancyGraphics);
         printwriter.println("ao:" + this.ambientOcclusion);
         switch (this.clouds) {
            case 0:
               printwriter.println("renderClouds:false");
               break;
            case 1:
               printwriter.println("renderClouds:fast");
               break;
            case 2:
               printwriter.println("renderClouds:true");
         }

         printwriter.println("resourcePacks:" + gson.toJson(this.resourcePacks));
         printwriter.println("incompatibleResourcePacks:" + gson.toJson(this.field_183018_l));
         printwriter.println("lastServer:" + this.lastServer);
         printwriter.println("lang:" + this.language);
         printwriter.println("chatVisibility:" + this.chatVisibility.getChatVisibility());
         printwriter.println("chatColors:" + this.chatColours);
         printwriter.println("chatLinks:" + this.chatLinks);
         printwriter.println("chatLinksPrompt:" + this.chatLinksPrompt);
         printwriter.println("chatOpacity:" + this.chatOpacity);
         printwriter.println("snooperEnabled:" + this.snooperEnabled);
         printwriter.println("fullscreen:" + this.fullScreen);
         printwriter.println("enableVsync:" + this.enableVsync);
         printwriter.println("useVbo:" + this.useVbo);
         printwriter.println("hideServerAddress:" + this.hideServerAddress);
         printwriter.println("advancedItemTooltips:" + this.advancedItemTooltips);
         printwriter.println("pauseOnLostFocus:" + this.pauseOnLostFocus);
         printwriter.println("touchscreen:" + this.touchscreen);
         printwriter.println("overrideWidth:" + this.overrideWidth);
         printwriter.println("overrideHeight:" + this.overrideHeight);
         printwriter.println("heldItemTooltips:" + this.heldItemTooltips);
         printwriter.println("chatHeightFocused:" + this.chatHeightFocused);
         printwriter.println("chatHeightUnfocused:" + this.chatHeightUnfocused);
         printwriter.println("chatScale:" + this.chatScale);
         printwriter.println("chatWidth:" + this.chatWidth);
         printwriter.println("showInventoryAchievementHint:" + this.showInventoryAchievementHint);
         printwriter.println("mipmapLevels:" + this.mipmapLevels);
         printwriter.println("forceUnicodeFont:" + this.forceUnicodeFont);
         printwriter.println("allowBlockAlternatives:" + this.allowBlockAlternatives);
         printwriter.println("reducedDebugInfo:" + this.reducedDebugInfo);
         printwriter.println("useNativeTransport:" + this.field_181150_U);
         printwriter.println("entityShadows:" + this.field_181151_V);
         KeyBinding[] var2 = this.keyBindings;
         int var3 = var2.length;

         int var4;
         for(var4 = 0; var4 < var3; ++var4) {
            KeyBinding keybinding = var2[var4];
            printwriter.println("key_" + keybinding.getKeyDescription() + ":" + keybinding.getKeyCode());
         }

         SoundCategory[] var7 = SoundCategory.values();
         var3 = var7.length;

         for(var4 = 0; var4 < var3; ++var4) {
            SoundCategory soundcategory = var7[var4];
            printwriter.println("soundCategory_" + soundcategory.getCategoryName() + ":" + this.getSoundLevel(soundcategory));
         }

         EnumPlayerModelParts[] var8 = EnumPlayerModelParts.values();
         var3 = var8.length;

         for(var4 = 0; var4 < var3; ++var4) {
            EnumPlayerModelParts enumplayermodelparts = var8[var4];
            printwriter.println("modelPart_" + enumplayermodelparts.getPartName() + ":" + this.setModelParts.contains(enumplayermodelparts));
         }

         printwriter.close();
      } catch (Exception var6) {
         logger.error("Failed to save options", var6);
      }

      this.sendSettingsToServer();
   }

   public float getSoundLevel(SoundCategory p_151438_1_) {
      return this.mapSoundLevels.containsKey(p_151438_1_) ? (Float)this.mapSoundLevels.get(p_151438_1_) : 1.0F;
   }

   public void setSoundLevel(SoundCategory p_151439_1_, float p_151439_2_) {
      this.mc.getSoundHandler().setSoundLevel(p_151439_1_, p_151439_2_);
      this.mapSoundLevels.put(p_151439_1_, p_151439_2_);
   }

   public void sendSettingsToServer() {
      if (this.mc.thePlayer != null) {
         int i = 0;

         EnumPlayerModelParts enumplayermodelparts;
         for(Iterator var2 = this.setModelParts.iterator(); var2.hasNext(); i |= enumplayermodelparts.getPartMask()) {
            enumplayermodelparts = (EnumPlayerModelParts)var2.next();
         }

         this.mc.thePlayer.sendQueue.sendPacket(new C15PacketClientSettings(this.language, this.renderDistanceChunks, this.chatVisibility, this.chatColours, i));
      }

   }

   public Set getModelParts() {
      return ImmutableSet.copyOf(this.setModelParts);
   }

   public void setModelPartEnabled(EnumPlayerModelParts p_178878_1_, boolean p_178878_2_) {
      if (p_178878_2_) {
         this.setModelParts.add(p_178878_1_);
      } else {
         this.setModelParts.remove(p_178878_1_);
      }

      this.sendSettingsToServer();
   }

   public void switchModelPartEnabled(EnumPlayerModelParts p_178877_1_) {
      if (!this.getModelParts().contains(p_178877_1_)) {
         this.setModelParts.add(p_178877_1_);
      } else {
         this.setModelParts.remove(p_178877_1_);
      }

      this.sendSettingsToServer();
   }

   public int func_181147_e() {
      return this.renderDistanceChunks >= 4 ? this.clouds : 0;
   }

   public boolean func_181148_f() {
      return this.field_181150_U;
   }

   public static enum Options {
      INVERT_MOUSE("options.invertMouse", false, true),
      SENSITIVITY("options.sensitivity", true, false),
      FOV("options.fov", true, false, 30.0F, 110.0F, 1.0F),
      GAMMA("options.gamma", true, false),
      SATURATION("options.saturation", true, false),
      RENDER_DISTANCE("options.renderDistance", true, false, 2.0F, 16.0F, 1.0F),
      VIEW_BOBBING("options.viewBobbing", false, true),
      FRAMERATE_LIMIT("options.framerateLimit", true, false, 10.0F, 260.0F, 10.0F),
      FBO_ENABLE("options.fboEnable", false, true),
      RENDER_CLOUDS("options.renderClouds", false, false),
      GRAPHICS("options.graphics", false, false),
      AMBIENT_OCCLUSION("options.ao", false, false),
      GUI_SCALE("options.guiScale", false, false),
      PARTICLES("options.particles", false, false),
      CHAT_VISIBILITY("options.chat.visibility", false, false),
      CHAT_COLOR("options.chat.color", false, true),
      CHAT_LINKS("options.chat.links", false, true),
      CHAT_OPACITY("options.chat.opacity", true, false),
      CHAT_LINKS_PROMPT("options.chat.links.prompt", false, true),
      SNOOPER_ENABLED("options.snooper", false, true),
      USE_FULLSCREEN("options.fullscreen", false, true),
      ENABLE_VSYNC("options.vsync", false, true),
      USE_VBO("options.vbo", false, true),
      TOUCHSCREEN("options.touchscreen", false, true),
      CHAT_SCALE("options.chat.scale", true, false),
      CHAT_WIDTH("options.chat.width", true, false),
      CHAT_HEIGHT_FOCUSED("options.chat.height.focused", true, false),
      CHAT_HEIGHT_UNFOCUSED("options.chat.height.unfocused", true, false),
      MIPMAP_LEVELS("options.mipmapLevels", true, false, 0.0F, 4.0F, 1.0F),
      FORCE_UNICODE_FONT("options.forceUnicodeFont", false, true),
      BLOCK_ALTERNATIVES("options.blockAlternatives", false, true),
      REDUCED_DEBUG_INFO("options.reducedDebugInfo", false, true),
      ENTITY_SHADOWS("options.entityShadows", false, true);

      private final boolean enumFloat;
      private final boolean enumBoolean;
      private final String enumString;
      private final float valueStep;
      private final float valueMin;
      private float valueMax;

      private Options(String p_i1015_3_, boolean p_i1015_4_, boolean p_i1015_5_) {
         this(p_i1015_3_, p_i1015_4_, p_i1015_5_, 0.0F, 1.0F, 0.0F);
      }

      private Options(String p_i45004_3_, boolean p_i45004_4_, boolean p_i45004_5_, float p_i45004_6_, float p_i45004_7_, float p_i45004_8_) {
         this.enumString = p_i45004_3_;
         this.enumFloat = p_i45004_4_;
         this.enumBoolean = p_i45004_5_;
         this.valueMin = p_i45004_6_;
         this.valueMax = p_i45004_7_;
         this.valueStep = p_i45004_8_;
      }

      public static Options getEnumOptions(int p_74379_0_) {
         Options[] var1 = values();
         int var2 = var1.length;

         for(int var3 = 0; var3 < var2; ++var3) {
            Options gamesettings$options = var1[var3];
            if (gamesettings$options.returnEnumOrdinal() == p_74379_0_) {
               return gamesettings$options;
            }
         }

         return null;
      }

      public boolean getEnumFloat() {
         return this.enumFloat;
      }

      public boolean getEnumBoolean() {
         return this.enumBoolean;
      }

      public int returnEnumOrdinal() {
         return this.ordinal();
      }

      public String getEnumString() {
         return this.enumString;
      }

      public float getValueMax() {
         return this.valueMax;
      }

      public void setValueMax(float p_148263_1_) {
         this.valueMax = p_148263_1_;
      }

      public float normalizeValue(float p_148266_1_) {
         return MathHelper.clamp_float((this.snapToStepClamp(p_148266_1_) - this.valueMin) / (this.valueMax - this.valueMin), 0.0F, 1.0F);
      }

      public float denormalizeValue(float p_148262_1_) {
         return this.snapToStepClamp(this.valueMin + (this.valueMax - this.valueMin) * MathHelper.clamp_float(p_148262_1_, 0.0F, 1.0F));
      }

      public float snapToStepClamp(float p_148268_1_) {
         p_148268_1_ = this.snapToStep(p_148268_1_);
         return MathHelper.clamp_float(p_148268_1_, this.valueMin, this.valueMax);
      }

      protected float snapToStep(float p_148264_1_) {
         if (this.valueStep > 0.0F) {
            p_148264_1_ = this.valueStep * (float)Math.round(p_148264_1_ / this.valueStep);
         }

         return p_148264_1_;
      }
   }
}
