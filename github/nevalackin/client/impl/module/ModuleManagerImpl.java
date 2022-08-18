package io.github.nevalackin.client.impl.module;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;
import io.github.nevalackin.client.api.module.Module;
import io.github.nevalackin.client.api.module.ModuleManager;
import io.github.nevalackin.client.impl.core.KetamineClient;
import io.github.nevalackin.client.impl.module.combat.healing.AutoHead;
import io.github.nevalackin.client.impl.module.combat.healing.AutoPot;
import io.github.nevalackin.client.impl.module.combat.healing.Regen;
import io.github.nevalackin.client.impl.module.combat.miniGames.CripsVsBloods;
import io.github.nevalackin.client.impl.module.combat.rage.Aura;
import io.github.nevalackin.client.impl.module.combat.rage.TargetStrafe;
import io.github.nevalackin.client.impl.module.combat.rage.Velocity;
import io.github.nevalackin.client.impl.module.misc.inventory.AutoTool;
import io.github.nevalackin.client.impl.module.misc.inventory.ChestStealer;
import io.github.nevalackin.client.impl.module.misc.inventory.Inventory;
import io.github.nevalackin.client.impl.module.misc.inventory.InventoryManager;
import io.github.nevalackin.client.impl.module.misc.player.AutoHypixel;
import io.github.nevalackin.client.impl.module.misc.player.Blink;
import io.github.nevalackin.client.impl.module.misc.player.FastUse;
import io.github.nevalackin.client.impl.module.misc.player.MCF;
import io.github.nevalackin.client.impl.module.misc.player.NoRotate;
import io.github.nevalackin.client.impl.module.misc.player.PingSpoof;
import io.github.nevalackin.client.impl.module.misc.player.StaffAnalyzer;
import io.github.nevalackin.client.impl.module.misc.world.EntityAura;
import io.github.nevalackin.client.impl.module.misc.world.FastBreak;
import io.github.nevalackin.client.impl.module.misc.world.Phase;
import io.github.nevalackin.client.impl.module.misc.world.Scaffold;
import io.github.nevalackin.client.impl.module.misc.world.Timer;
import io.github.nevalackin.client.impl.module.movement.extras.Flight;
import io.github.nevalackin.client.impl.module.movement.extras.LongJump;
import io.github.nevalackin.client.impl.module.movement.extras.Speed;
import io.github.nevalackin.client.impl.module.movement.main.AntiVoid;
import io.github.nevalackin.client.impl.module.movement.main.NoFall;
import io.github.nevalackin.client.impl.module.movement.main.NoSlowDown;
import io.github.nevalackin.client.impl.module.movement.main.Sprint;
import io.github.nevalackin.client.impl.module.movement.main.Step;
import io.github.nevalackin.client.impl.module.render.esp.Glow;
import io.github.nevalackin.client.impl.module.render.esp.OffScreenESP;
import io.github.nevalackin.client.impl.module.render.esp.esp.ESP;
import io.github.nevalackin.client.impl.module.render.model.Chams;
import io.github.nevalackin.client.impl.module.render.model.HurtEffect;
import io.github.nevalackin.client.impl.module.render.model.NoRender;
import io.github.nevalackin.client.impl.module.render.overlay.Camera;
import io.github.nevalackin.client.impl.module.render.overlay.Crosshair;
import io.github.nevalackin.client.impl.module.render.overlay.Gui;
import io.github.nevalackin.client.impl.module.render.overlay.NoFOV;
import io.github.nevalackin.client.impl.module.render.overlay.NoOverlays;
import io.github.nevalackin.client.impl.module.render.self.ChinaHat;
import io.github.nevalackin.client.impl.module.render.self.SwingModifier;
import io.github.nevalackin.client.impl.module.render.self.ThirdPerson;
import io.github.nevalackin.client.impl.module.render.world.BlockOverlay;
import io.github.nevalackin.client.impl.module.render.world.WorldTime;
import io.github.nevalackin.client.impl.module.render.world.XRay;
import io.github.nevalackin.client.impl.ui.hud.components.HudComponent;
import io.github.nevalackin.client.impl.ui.hud.rendered.ArraylistModule;
import io.github.nevalackin.client.impl.ui.hud.rendered.PlayerInfoModule;
import io.github.nevalackin.client.impl.ui.hud.rendered.TargetHudModule;
import io.github.nevalackin.client.impl.ui.hud.rendered.WatermarkModule;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public final class ModuleManagerImpl implements ModuleManager {
   private final ClassToInstanceMap moduleInstances = populateInstanceMap(new Aura(), new CripsVsBloods(), new EntityAura(), new TargetStrafe(), new Velocity(), new Regen(), new AutoPot(), new Inventory(), new NoFall(), new MCF(), new ChestStealer(), new InventoryManager(), new NoRotate(), new AutoTool(), new PingSpoof(), new Blink(), new Phase(), new FastUse(), new Sprint(), new NoSlowDown(), new Speed(), new Gui(), new LongJump(), new Step(), new AutoTool(), new Scaffold(), new WorldTime(), new FastBreak(), new Timer(), new AutoHypixel(), new NoRender(), new OffScreenESP(), new Crosshair(), new Camera(), new NoOverlays(), new SwingModifier(), new AntiVoid(), new AutoHead(), new HurtEffect(), new XRay(), new Chams(), new BlockOverlay(), new ChinaHat(), new NoFOV(), new ThirdPerson(), new Glow(), new ArraylistModule(), new WatermarkModule(), new TargetHudModule(), new StaffAnalyzer(), new ESP(), new PlayerInfoModule(), new Flight());

   public ModuleManagerImpl() {
      KetamineClient.getInstance().getEventBus().subscribe(this);
   }

   public Module getModule(Class clazz) {
      return (Module)this.moduleInstances.getInstance(clazz);
   }

   public Module getModule(String name) {
      return null;
   }

   public void registerModule(Class clazz, Module module) {
      this.moduleInstances.putInstance(clazz, module);
   }

   public static Collection getHudComponents() {
      return (Collection)KetamineClient.getInstance().getModuleManager().getModules().stream().filter((feature) -> {
         return feature instanceof HudComponent;
      }).map((feature) -> {
         return (HudComponent)feature;
      }).collect(Collectors.toList());
   }

   private static ClassToInstanceMap populateInstanceMap(Module... modules) {
      ClassToInstanceMap instanceMap = MutableClassToInstanceMap.create();
      Arrays.stream(modules).forEach((module) -> {
         Module var10000 = (Module)instanceMap.putInstance(module.getClass(), module);
      });
      return instanceMap;
   }

   public Collection getModules() {
      return this.moduleInstances.values();
   }
}
