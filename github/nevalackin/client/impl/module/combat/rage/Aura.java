package io.github.nevalackin.client.impl.module.combat.rage;

import com.google.common.collect.Lists;
import io.github.nevalackin.client.api.module.Category;
import io.github.nevalackin.client.api.module.Module;
import io.github.nevalackin.client.api.property.Property;
import io.github.nevalackin.client.impl.core.KetamineClient;
import io.github.nevalackin.client.impl.event.player.UpdatePositionEvent;
import io.github.nevalackin.client.impl.module.combat.healing.AutoPot;
import io.github.nevalackin.client.impl.module.misc.world.Scaffold;
import io.github.nevalackin.client.impl.property.BooleanProperty;
import io.github.nevalackin.client.impl.property.DoubleProperty;
import io.github.nevalackin.client.impl.property.EnumProperty;
import io.github.nevalackin.client.impl.property.MultiSelectionEnumProperty;
import io.github.nevalackin.client.util.player.RotationUtil;
import io.github.nevalackin.client.util.player.TeamsUtil;
import io.github.nevalackin.client.util.render.DrawUtil;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

public final class Aura extends Module {
   private final EnumProperty modeProperty;
   private final DoubleProperty maxTargetsProperty;
   private final EnumProperty sortingModeProperty;
   private static final EnumProperty autoBlockModeProperty;
   private final MultiSelectionEnumProperty entitySelectionProperty;
   private final BooleanProperty checkTeamsProperty;
   private final EnumProperty teamsCheckProperty;
   private final DoubleProperty reachProperty;
   private final BooleanProperty autoAttackProperty;
   private final EnumProperty swingMethodProperty;
   private final EnumProperty attackModeProperty;
   private final BooleanProperty keepSprintProperty;
   private final DoubleProperty ticksProperty;
   private final DoubleProperty retargetDelayProperty;
   private final DoubleProperty blockRangeProperty;
   private final EnumProperty rotationsModeProperty;
   private final EnumProperty rotationsPointProperty;
   private final DoubleProperty angleChangeProperty;
   private final BooleanProperty silentAimProperty;
   private final DoubleProperty boundingBoxScaleProperty;
   private final BooleanProperty drawBoundingBoxProperty;
   private final BooleanProperty passThroughWalls;
   private final EnumProperty criticalProperty;
   private final DoubleProperty criticalDelayProperty;
   private final EnumProperty extraDurabilityProperty;
   private final DoubleProperty pingProperty;
   private final List attackList;
   private static boolean entityInBlockRange;
   private int ticksSinceLastAttack;
   private int ticksSinceSendCritical;
   private int ticksSinceLastRetarget;
   private int ticksSinceLastFastAttack;
   private static final Map entityDistCache;
   private final float[] rotationStore;
   private boolean rotating;
   private boolean blocking;
   private Scaffold scaffold;
   private AutoPot autopot;
   @EventLink
   private final Listener onUpdateLook;
   @EventLink
   private final Listener onMoveYawUpdate;
   @EventLink
   private final Listener onJump;
   @EventLink
   private final Listener onRender3D;
   @EventLink(0)
   private final Listener onUpdatePosition;

   public Aura() {
      super("Kill Aura", Category.COMBAT, Category.SubCategory.COMBAT_RAGE);
      this.modeProperty = new EnumProperty("Mode", Aura.Mode.MULTI);
      this.maxTargetsProperty = new DoubleProperty("Targets", 2.0, () -> {
         return this.modeProperty.getValue() == Aura.Mode.MULTI;
      }, 2.0, 8.0, 1.0);
      this.sortingModeProperty = new EnumProperty("Sorting", Aura.SortingMode.HEALTH, this::doesSingleAura);
      this.entitySelectionProperty = new MultiSelectionEnumProperty("Targeted Entities", Lists.newArrayList(new EntityTargetMode[]{Aura.EntityTargetMode.PLAYERS}), Aura.EntityTargetMode.values());
      this.checkTeamsProperty = new BooleanProperty("Teams", false);
      TeamsUtil.TeamsMode var10004 = TeamsUtil.TeamsMode.NAME;
      BooleanProperty var10005 = this.checkTeamsProperty;
      var10005.getClass();
      this.teamsCheckProperty = new EnumProperty("Teams Check", var10004, var10005::getValue);
      this.reachProperty = new DoubleProperty("Range", 4.2, 3.0, 6.0, 0.1);
      this.autoAttackProperty = new BooleanProperty("Auto Attack", true);
      Swing var3 = Aura.Swing.CLIENT;
      var10005 = this.autoAttackProperty;
      var10005.getClass();
      this.swingMethodProperty = new EnumProperty("Swing", var3, var10005::getValue);
      AttackMode var4 = Aura.AttackMode.PRE;
      var10005 = this.autoAttackProperty;
      var10005.getClass();
      this.attackModeProperty = new EnumProperty("Attack Mode", var4, var10005::getValue);
      var10005 = this.autoAttackProperty;
      var10005.getClass();
      this.keepSprintProperty = new BooleanProperty("Keep Sprint", true, var10005::getValue);
      this.ticksProperty = new DoubleProperty("Delay", 0.0, this.autoAttackProperty::getValue, 0.0, 10.0, 1.0);
      this.retargetDelayProperty = new DoubleProperty("Switch Delay", 7.0, this::doesSingleAura, 1.0, 20.0, 1.0);
      this.blockRangeProperty = new DoubleProperty("Block Range", 6.0, () -> {
         return autoBlockModeProperty.getValue() != Aura.AutoBlockMode.OFF;
      }, 3.0, 8.0, 0.1);
      this.rotationsModeProperty = new EnumProperty("Rotations", Aura.RotationsMode.SMOOTH);
      this.rotationsPointProperty = new EnumProperty("Rotations Point", RotationUtil.RotationsPoint.CLOSEST);
      this.angleChangeProperty = new DoubleProperty("Smoothing", 10.0, () -> {
         return this.rotationsModeProperty.getValue() == Aura.RotationsMode.SMOOTH;
      }, 1.0, 80.0, 0.5);
      this.silentAimProperty = new BooleanProperty("Silent Aim", true, this::doesRotations);
      this.boundingBoxScaleProperty = new DoubleProperty("Bounding Box Scale", 0.1, this.autoAttackProperty::getValue, -1.0, 1.0, 0.1);
      var10005 = this.autoAttackProperty;
      var10005.getClass();
      this.drawBoundingBoxProperty = new BooleanProperty("Draw Bounding Box", true, var10005::getValue);
      this.passThroughWalls = new BooleanProperty("Walls", true, () -> {
         return this.doesRotations() && (Boolean)this.autoAttackProperty.getValue();
      });
      CriticalMode var1 = Aura.CriticalMode.WATCHDOG;
      var10005 = this.autoAttackProperty;
      var10005.getClass();
      this.criticalProperty = new EnumProperty("Crits", var1, var10005::getValue);
      this.criticalDelayProperty = new DoubleProperty("Crits Delay", 0.0, () -> {
         return this.criticalProperty.getValue() != Aura.CriticalMode.OFF && this.criticalProperty.check();
      }, 0.0, 20.0, 1.0);
      ExtraDurabilityMode var2 = Aura.ExtraDurabilityMode.OFF;
      var10005 = this.autoAttackProperty;
      var10005.getClass();
      this.extraDurabilityProperty = new EnumProperty("Dura", var2, var10005::getValue);
      this.pingProperty = new DoubleProperty("Ping", -1.0, this.autoAttackProperty::getValue, -1.0, 10.0, 1.0);
      this.attackList = new ArrayList();
      this.rotationStore = new float[2];
      this.onUpdateLook = (event) -> {
         if (this.isBypassRotations()) {
            Vec3 hitOrigin = RotationUtil.getHitOrigin(this.mc.thePlayer);
            if (!this.rotating) {
               this.rotationStore[0] = this.mc.thePlayer.rotationYaw;
               this.rotationStore[1] = this.mc.thePlayer.rotationPitch;
            }

            this.rotating = false;
            EntityLivingBase target = this.getTarget();
            if (target == null) {
               return;
            }

            Vec3 attackHitVec = RotationUtil.getCenterPointOnBB(target.getEntityBoundingBox(), 0.5 + Math.random() * 0.1);
            float[] rotations = RotationUtil.getRotations(hitOrigin, attackHitVec);
            RotationUtil.applyGCD(rotations, this.rotationStore);
            this.rotationStore[0] = rotations[0];
            this.rotationStore[1] = rotations[1];
            this.rotating = true;
         }

      };
      this.onMoveYawUpdate = (event) -> {
         if (this.isBypassRotations() && this.rotating) {
            event.setYaw(this.rotationStore[0]);
         }

      };
      this.onJump = (event) -> {
         if (this.isBypassRotations() && this.rotating) {
            event.setYaw(this.rotationStore[0]);
         }

      };
      this.onRender3D = (event) -> {
         if ((Boolean)this.drawBoundingBoxProperty.getValue() && this.getTarget() != null) {
            GL11.glDisable(3553);
            GL11.glDisable(2929);
            boolean restore = DrawUtil.glEnableBlend();
            int lastColour = 0;

            AxisAlignedBB boundingBox;
            for(Iterator var4 = this.attackList.iterator(); var4.hasNext(); DrawUtil.glDrawBoundingBox(boundingBox, 0.0F, true)) {
               EntityLivingBase target = (EntityLivingBase)var4.next();
               boundingBox = DrawUtil.interpolate(target, RotationUtil.getHittableBoundingBox(target, (Double)this.boundingBoxScaleProperty.getValue()), event.getPartialTicks());
               int colour = target.hurtTime > 0 ? 1073807193 : 1090453504;
               if (colour != lastColour) {
                  DrawUtil.glColour(colour);
                  lastColour = colour;
               }
            }

            GL11.glEnable(3553);
            GL11.glEnable(2929);
            DrawUtil.glRestoreBlend(restore);
         }

      };
      this.onUpdatePosition = (event) -> {
         if (event.isPre()) {
            ++this.ticksSinceLastAttack;
            ++this.ticksSinceSendCritical;
            ++this.ticksSinceLastRetarget;
            ++this.ticksSinceLastFastAttack;
            entityDistCache.clear();
            this.attackList.clear();
            if (event.isRotating() && this.autopot.setPitch && this.autopot.potting) {
               return;
            }

            if (this.scaffold.isEnabled()) {
               return;
            }

            EntityLivingBase lastTarget = this.getTarget();
            Stream var10000 = this.mc.theWorld.getLoadedEntityList().stream();
            EntityLivingBase.class.getClass();
            List validEntities = (List)var10000.filter(EntityLivingBase.class::isInstance).map((entity) -> {
               return (EntityLivingBase)entity;
            }).filter(this::validateEntity).collect(Collectors.toList());
            entityInBlockRange = validEntities.stream().anyMatch((entity) -> {
               return (Double)entityDistCache.get(entity) < (Double)this.blockRangeProperty.getValue();
            });
            Stream hittableEntities = validEntities.stream().filter((entity) -> {
               return (Double)entityDistCache.get(entity) < (Double)this.reachProperty.getValue();
            });
            Vec3 hitOrigin = RotationUtil.getHitOrigin(this.mc.thePlayer);
            boolean isLockView = !(Boolean)this.silentAimProperty.getValue();
            if (this.mc.gameSettings.keyBindUseItem.pressed && !this.mc.thePlayer.isUsingItem()) {
               this.mc.gameSettings.keyBindUseItem.pressed = false;
            }

            switch ((Mode)this.modeProperty.getValue()) {
               case MULTI:
                  boolean calculateRotations = this.doesRotations();
                  List rotationsToEntities = new ArrayList();
                  hittableEntities.sorted(Aura.SortingMode.ANGLE.getSorter()).limit(((Double)this.maxTargetsProperty.getValue()).longValue()).forEach((entity) -> {
                     Vec3 attackHitVec = this.getAttackHitVec(hitOrigin, entity);
                     if (attackHitVec != null) {
                        if (calculateRotations) {
                           float[] rotations = RotationUtil.getRotations(new float[]{isLockView ? this.mc.thePlayer.rotationYaw : event.getLastTickYaw(), isLockView ? this.mc.thePlayer.rotationPitch : event.getLastTickPitch()}, this.rotationsModeProperty.getValue() == Aura.RotationsMode.SNAP ? 0.0F : ((Double)this.angleChangeProperty.getValue()).floatValue(), hitOrigin, attackHitVec);
                           rotationsToEntities.add(rotations);
                        }

                        this.attackList.add(entity);
                     }

                  });
                  if (calculateRotations && !this.attackList.isEmpty()) {
                     float[] avgRotations = RotationUtil.calculateAverageRotations(rotationsToEntities);
                     this.setServerSideRotations(avgRotations, event, isLockView);
                  }
                  break;
               case SINGLE:
                  hittableEntities.min(((SortingMode)this.sortingModeProperty.getValue()).getSorter()).ifPresent((entity) -> {
                     if (lastTarget != null && this.validateEntity(lastTarget) && (Double)entityDistCache.get(entity) < (Double)this.reachProperty.getValue() && lastTarget != entity) {
                        if ((double)this.ticksSinceLastRetarget >= (Double)this.retargetDelayProperty.getValue()) {
                           this.ticksSinceLastRetarget = 0;
                        } else {
                           entity = lastTarget;
                        }
                     }

                     Vec3 attackHitVec = this.getAttackHitVec(hitOrigin, entity);
                     if (attackHitVec != null) {
                        this.attackList.add(entity);
                        if (this.doesRotations()) {
                           float[] rotations = RotationUtil.getRotations(new float[]{isLockView ? this.mc.thePlayer.rotationYaw : event.getLastTickYaw(), isLockView ? this.mc.thePlayer.rotationPitch : event.getLastTickPitch()}, this.rotationsModeProperty.getValue() == Aura.RotationsMode.SNAP ? 0.0F : ((Double)this.angleChangeProperty.getValue()).floatValue(), hitOrigin, attackHitVec);
                           this.setServerSideRotations(rotations, event, isLockView);
                        }
                     }

                  });
            }

            if (this.attackModeProperty.getValue() == Aura.AttackMode.PRE) {
               this.attack(event);
            }
         } else {
            if (this.attackModeProperty.getValue() == Aura.AttackMode.POST) {
               this.attack(event);
            }

            if (canAutoBlock()) {
               switch ((AutoBlockMode)autoBlockModeProperty.getValue()) {
                  case INTERACT:
                     Iterator var10 = this.attackList.iterator();

                     while(var10.hasNext()) {
                        EntityLivingBase target = (EntityLivingBase)var10.next();
                        this.mc.playerController.interactWithEntitySendPacket(this.mc.thePlayer, target);
                     }
                  case NCP:
                     this.mc.playerController.sendUseItem(this.mc.thePlayer, this.mc.theWorld, this.mc.thePlayer.getHeldItem());
                     break;
                  case WATCHDOG:
                     this.mc.gameSettings.keyBindUseItem.pressed = true;
               }
            }
         }

      };
      this.register(new Property[]{this.modeProperty, this.sortingModeProperty, this.maxTargetsProperty, this.reachProperty, autoBlockModeProperty, this.blockRangeProperty, this.entitySelectionProperty, this.checkTeamsProperty, this.teamsCheckProperty, this.rotationsModeProperty, this.angleChangeProperty, this.silentAimProperty, this.rotationsPointProperty, this.autoAttackProperty, this.swingMethodProperty, this.attackModeProperty, this.keepSprintProperty, this.ticksProperty, this.boundingBoxScaleProperty, this.drawBoundingBoxProperty, this.passThroughWalls, this.pingProperty, this.retargetDelayProperty, this.criticalProperty, this.criticalDelayProperty, this.extraDurabilityProperty});
      this.boundingBoxScaleProperty.addValueAlias(0.1, "Vanilla");
      this.reachProperty.addValueAlias(4.2, "NCP");
      this.reachProperty.addValueAlias(3.0, "Legit");
      this.ticksProperty.addValueAlias(0.0, "Auto");
      this.ticksProperty.addValueAlias(1.0, "20 APS");
      this.ticksProperty.addValueAlias(2.0, "10 APS");
      this.pingProperty.addValueAlias(-1.0, "Auto");
      this.retargetDelayProperty.addValueAlias(1.0, "None");
      this.criticalDelayProperty.addValueAlias(0.0, "Auto");
      this.criticalDelayProperty.addValueAlias(1.0, "None");
      this.setSuffix(() -> {
         return ((Mode)this.modeProperty.getValue()).toString();
      });
   }

   private boolean doesRotations() {
      return this.rotationsModeProperty.getValue() != Aura.RotationsMode.OFF;
   }

   private boolean doesSingleAura() {
      return this.modeProperty.getValue() == Aura.Mode.SINGLE;
   }

   private boolean isBypassRotations() {
      return this.rotationsModeProperty.getValue() == Aura.RotationsMode.UNDETECTABLE;
   }

   private void setServerSideRotations(float[] rotations, UpdatePositionEvent event, boolean lockView) {
      if (this.isBypassRotations()) {
         rotations = this.rotationStore;
      }

      event.setYaw(rotations[0]);
      event.setPitch(rotations[1]);
      if (lockView) {
         this.mc.thePlayer.rotationYaw = rotations[0];
         this.mc.thePlayer.rotationPitch = rotations[1];
      }

   }

   private Vec3 getAttackHitVec(Vec3 hitOrigin, EntityLivingBase entity) {
      AxisAlignedBB boundingBox = RotationUtil.getHittableBoundingBox(entity, (double)((Double)this.boundingBoxScaleProperty.getValue()).floatValue());
      return RotationUtil.getAttackHitVec(this.mc, hitOrigin, boundingBox, ((RotationUtil.RotationsPoint)this.rotationsPointProperty.getValue()).getHitVec(hitOrigin, boundingBox), (Boolean)this.passThroughWalls.getValue(), -1);
   }

   private void attack(UpdatePositionEvent event) {
      if (!this.attackList.isEmpty() && (Boolean)this.autoAttackProperty.getValue()) {
         boolean attacked = false;
         Iterator var3 = this.attackList.iterator();

         while(true) {
            EntityLivingBase target;
            boolean checkRotations;
            Vec3 origin;
            MovingObjectPosition intercept;
            do {
               do {
                  if (!var3.hasNext()) {
                     if (attacked) {
                        this.ticksSinceLastAttack = 0;
                     }

                     return;
                  }

                  target = (EntityLivingBase)var3.next();
               } while(!this.shouldAttack(target));

               checkRotations = this.doesRotations() && this.attackList.size() == 1;
               origin = RotationUtil.getHitOrigin(this.mc.thePlayer);
               intercept = RotationUtil.calculateIntercept(RotationUtil.getHittableBoundingBox(target, (Double)this.boundingBoxScaleProperty.getValue()), origin, event.isPre() ? event.getLastTickYaw() : event.getYaw(), event.isPre() ? event.getLastTickPitch() : event.getPitch(), (Double)this.reachProperty.getValue());
            } while(checkRotations && (intercept == null || !RotationUtil.validateHitVec(this.mc, origin, intercept.hitVec, (Boolean)this.passThroughWalls.getValue())));

            boolean var10000;
            label82: {
               double criticalDelay = (Double)this.criticalDelayProperty.getValue();
               int hurtTicksRemaining = target.hurtTime;
               if (this.criticalProperty.getValue() != Aura.CriticalMode.OFF) {
                  label80: {
                     if (criticalDelay == 0.0) {
                        if (hurtTicksRemaining > this.getPing()) {
                           break label80;
                        }
                     } else if (!((double)this.ticksSinceSendCritical >= criticalDelay)) {
                        break label80;
                     }

                     if (this.mc.thePlayer.onGround && this.mc.thePlayer.isCollidedVertically) {
                        var10000 = true;
                        break label82;
                     }
                  }
               }

               var10000 = false;
            }

            boolean willCritical = var10000;
            switch ((ExtraDurabilityMode)this.extraDurabilityProperty.getValue()) {
               case ITEM_SWAP:
                  this.mc.thePlayer.sendQueue.sendPacketDirect(new C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT));
                  this.doSwordSwap();
                  this.mc.thePlayer.sendQueue.sendPacketDirect(new C0DPacketCloseWindow(this.mc.thePlayer.inventoryContainer.windowId));
                  this.doAttack(target, Aura.Swing.SILENT);
                  this.mc.thePlayer.sendQueue.sendPacketDirect(new C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT));
                  this.doSwordSwap();
                  this.mc.thePlayer.sendQueue.sendPacketDirect(new C0DPacketCloseWindow(this.mc.thePlayer.inventoryContainer.windowId));
                  this.doAttack(target, Aura.Swing.SILENT);
               case ON_CRITICAL:
                  if (willCritical) {
                     this.doAttack(target, Aura.Swing.SILENT);
                  }
               default:
                  if (willCritical) {
                     this.doCritical(event, target, false);
                  }

                  this.doAttack(target, (Swing)this.swingMethodProperty.getValue());
                  attacked = true;
                  if (!(Boolean)this.keepSprintProperty.getValue()) {
                     this.mc.thePlayer.setSprinting(false);
                     EntityPlayerSP var12 = this.mc.thePlayer;
                     var12.motionX *= 0.6;
                     var12 = this.mc.thePlayer;
                     var12.motionZ *= 0.6;
                  }
            }
         }
      }
   }

   private void doAttack(EntityLivingBase entity, Swing swingMethod) {
      this.mc.playerController.syncCurrentPlayItem();
      switch (swingMethod) {
         case CLIENT:
            this.mc.thePlayer.swingItem();
            break;
         case SILENT:
            this.mc.thePlayer.sendQueue.sendPacket(new C0APacketAnimation());
      }

      this.mc.thePlayer.sendQueue.sendPacket(new C02PacketUseEntity(entity, C02PacketUseEntity.Action.ATTACK));
   }

   private void doSwordSwap() {
      ItemStack itemstack = this.mc.thePlayer.openContainer.slotClick(9, 0, 2, this.mc.thePlayer);
      this.mc.thePlayer.sendQueue.sendPacket(new C0EPacketClickWindow(this.mc.thePlayer.inventoryContainer.windowId, 9, 0, 2, itemstack, this.mc.thePlayer.openContainer.getNextTransactionID(this.mc.thePlayer.inventory)));
   }

   private void doCritical(UpdatePositionEvent event, EntityLivingBase target, boolean forceCritical) {
      double criticalDelay = (Double)this.criticalDelayProperty.getValue();
      boolean willCritical = !forceCritical && criticalDelay != 0.0 ? (double)this.ticksSinceSendCritical >= criticalDelay : this.ticksSinceSendCritical >= 3 && target.hurtTime >= this.getPing();
      if (willCritical) {
         double[] var7 = ((CriticalMode)this.criticalProperty.getValue()).offsets;
         int var8 = var7.length;

         for(int var9 = 0; var9 < var8; ++var9) {
            double offset = var7[var9];
            this.mc.thePlayer.sendQueue.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(event.getPosX(), event.getPosY() + offset + Math.random() * 0.0010000000474974513, event.getPosZ(), false));
         }

         this.ticksSinceSendCritical = 0;
      }

   }

   private boolean shouldAttack(EntityLivingBase entity) {
      if ((Double)this.ticksProperty.getValue() != 0.0) {
         return (double)this.ticksSinceLastAttack >= (Double)this.ticksProperty.getValue();
      } else {
         int hurtTicksRemaining = entity.hurtTime;
         if (hurtTicksRemaining != this.getPing() && (hurtTicksRemaining != 0 || this.ticksSinceLastFastAttack < 2 * ((ExtraDurabilityMode)this.extraDurabilityProperty.getValue()).numberOfAttacks * this.attackList.size())) {
            return false;
         } else {
            this.ticksSinceLastFastAttack = 0;
            return true;
         }
      }
   }

   public int getPing() {
      if ((Double)this.pingProperty.getValue() != -1.0) {
         return ((Double)this.pingProperty.getValue()).intValue();
      } else {
         NetworkPlayerInfo info = this.mc.getNetHandler().getPlayerInfo(this.mc.thePlayer.getUniqueID());
         return info == null ? 0 : (int)Math.ceil((double)info.getResponseTime() / 50.0);
      }
   }

   public static boolean canAutoBlock() {
      ItemStack heldItem = Minecraft.getMinecraft().thePlayer.getHeldItem();
      return autoBlockModeProperty.getValue() != Aura.AutoBlockMode.OFF && entityInBlockRange && heldItem != null && heldItem.getItem() instanceof ItemSword;
   }

   private boolean validateEntity(EntityLivingBase entity) {
      return entity.isEntityAlive() && (this.entitySelectionProperty.isSelected(Aura.EntityTargetMode.PLAYERS) && entity instanceof EntityOtherPlayerMP || this.entitySelectionProperty.isSelected(Aura.EntityTargetMode.CREATURES) && entity instanceof EntityCreature || this.entitySelectionProperty.isSelected(Aura.EntityTargetMode.ANIMALS) && entity instanceof EntityAnimal || this.entitySelectionProperty.isSelected(Aura.EntityTargetMode.WATERMOBS) && entity instanceof EntityWaterMob) && this.validateDistToEntity(entity) && this.runTeamsCheck(entity) && this.isBot(entity) && !KetamineClient.getInstance().friendManager.isFriend(entity.getName()) && !entity.isInvisible();
   }

   private boolean isBot(Entity entity) {
      if (this.entitySelectionProperty.isSelected(Aura.EntityTargetMode.BOTS)) {
         return true;
      } else {
         return this.isNPC(entity) && this.isOnTabList(entity);
      }
   }

   private boolean isNPC(Entity entity) {
      return !entity.getDisplayName().getFormattedText().startsWith("§8[NPC]");
   }

   private boolean isOnTabList(Entity entity) {
      Iterator var2 = this.mc.getNetHandler().getPlayerInfoMap().iterator();

      NetworkPlayerInfo info;
      do {
         if (!var2.hasNext()) {
            return false;
         }

         info = (NetworkPlayerInfo)var2.next();
      } while(!info.getGameProfile().getName().equals(entity.getName()));

      return true;
   }

   private boolean runTeamsCheck(EntityLivingBase entity) {
      if (entity instanceof EntityPlayer) {
         EntityPlayer player = (EntityPlayer)entity;
         if ((Boolean)this.checkTeamsProperty.getValue()) {
            return !((TeamsUtil.TeamsMode)this.teamsCheckProperty.getValue()).getComparator().isOnSameTeam(this.mc.thePlayer, player);
         }
      }

      return true;
   }

   private boolean validateDistToEntity(EntityLivingBase entity) {
      double dist = (double)entity.getDistanceToEntity(this.mc.thePlayer);
      if (dist < Math.max((Double)this.reachProperty.getValue(), (Double)this.blockRangeProperty.getValue())) {
         entityDistCache.put(entity, dist);
         return true;
      } else {
         return false;
      }
   }

   public double getCachedDistance(EntityLivingBase entity) {
      return !this.validateDistToEntity(entity) ? 0.0 : (Double)entityDistCache.get(entity);
   }

   public double getMaxDistance() {
      return (Double)this.reachProperty.getValue();
   }

   public void onEnable() {
      if (this.scaffold == null) {
         this.scaffold = (Scaffold)KetamineClient.getInstance().getModuleManager().getModule(Scaffold.class);
      }

      if (this.autopot == null) {
         this.autopot = (AutoPot)KetamineClient.getInstance().getModuleManager().getModule(AutoPot.class);
      }

      this.blocking = false;
      this.ticksSinceLastAttack = 0;
      this.rotating = false;
   }

   public void onDisable() {
      this.attackList.clear();
      entityDistCache.clear();
      entityInBlockRange = false;
   }

   public EntityLivingBase getTarget() {
      return this.attackList.isEmpty() ? null : (EntityLivingBase)this.attackList.get(0);
   }

   public List getAttackList() {
      return this.attackList;
   }

   static {
      autoBlockModeProperty = new EnumProperty("Autoblock", Aura.AutoBlockMode.INTERACT);
      entityDistCache = new HashMap();
   }

   private static enum SortingMode {
      HURT_TIME("HurtTime", Comparator.comparingInt((entity) -> {
         return entity.hurtTime;
      })),
      HEALTH("Health", Comparator.comparingDouble(EntityLivingBase::getHealth)),
      DISTANCE,
      ANGLE,
      ARMOR;

      private final String name;
      private final Comparator sorter;

      public Comparator getSorter() {
         return this.sorter;
      }

      private SortingMode(String name, Comparator sorter) {
         this.name = name;
         this.sorter = sorter;
      }

      public String toString() {
         return this.name;
      }

      static {
         Map var10005 = Aura.entityDistCache;
         var10005.getClass();
         DISTANCE = new SortingMode("DISTANCE", 2, "Distance", Comparator.comparingDouble(var10005::get));
         ANGLE = new SortingMode("ANGLE", 3, "Angle", Comparator.comparingDouble((entity) -> {
            Entity player = Minecraft.getMinecraft().thePlayer;
            return (double)Math.abs(RotationUtil.calculateYawFromSrcToDst(player.rotationYaw, player.posX, player.posZ, entity.posX, entity.posZ) - player.rotationYaw);
         }));
         ARMOR = new SortingMode("ARMOR", 4, "Armor", Comparator.comparingDouble(EntityLivingBase::getTotalArmorValue));
      }
   }

   private static enum EntityTargetMode {
      PLAYERS("Players"),
      CREATURES("Creatures"),
      ANIMALS("Animals"),
      WATERMOBS("Water Mobs"),
      BOTS("Bots");

      private final String name;

      private EntityTargetMode(String name) {
         this.name = name;
      }

      public String toString() {
         return this.name;
      }
   }

   private static enum AutoBlockMode {
      OFF("Off"),
      NCP("NCP"),
      INTERACT("Interact"),
      WATCHDOG("Watchdog");

      private final String name;

      private AutoBlockMode(String name) {
         this.name = name;
      }

      public String toString() {
         return this.name;
      }
   }

   private static enum Swing {
      CLIENT("Client"),
      SILENT("Silent"),
      NO_SWING("No Swing");

      private final String name;

      private Swing(String name) {
         this.name = name;
      }

      public String toString() {
         return this.name;
      }
   }

   private static enum RotationsMode {
      OFF("Off"),
      SNAP("Snap"),
      UNDETECTABLE("Undetectable"),
      SMOOTH("Smooth");

      private final String name;

      private RotationsMode(String name) {
         this.name = name;
      }

      public String toString() {
         return this.name;
      }
   }

   private static enum AttackMode {
      PRE("Pre"),
      POST("Post");

      private final String name;

      private AttackMode(String name) {
         this.name = name;
      }

      public String toString() {
         return this.name;
      }
   }

   private static enum CriticalMode {
      OFF("Off", (double[])null),
      NCP("NCP", new double[]{0.0626, 0.0}),
      WATCHDOG("Watchdog", new double[]{0.05999999865889549, 0.009999999776482582});

      private final String name;
      private final double[] offsets;

      private CriticalMode(String name, double[] offsets) {
         this.name = name;
         this.offsets = offsets;
      }

      public String toString() {
         return this.name;
      }
   }

   private static enum ExtraDurabilityMode {
      OFF("Off", 1),
      ON_CRITICAL("Do Critical", 2),
      ITEM_SWAP("Item Swap", 4);

      private final String name;
      private final int numberOfAttacks;

      private ExtraDurabilityMode(String name, int numberOfAttacks) {
         this.name = name;
         this.numberOfAttacks = numberOfAttacks;
      }

      public String toString() {
         return this.name;
      }
   }

   private static enum Mode {
      SINGLE("Single"),
      MULTI("Multi");

      private final String name;

      private Mode(String name) {
         this.name = name;
      }

      public String toString() {
         return this.name;
      }
   }
}
