package io.github.nevalackin.client.impl.module.combat.miniGames;

import com.google.common.collect.Lists;
import io.github.nevalackin.client.api.module.Category;
import io.github.nevalackin.client.api.module.Module;
import io.github.nevalackin.client.api.property.Property;
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
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.server.S2APacketParticles;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.StringUtils;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

public final class CripsVsBloods extends Module {
   private final BooleanProperty autoShootProperty = new BooleanProperty("Auto Shoot", true);
   private final DoubleProperty lagCompensation = new DoubleProperty("Lag Compensation", 0.0, 0.0, 500.0, 1.0);
   private final BooleanProperty recoilControlProperty = new BooleanProperty("Recoil Control", true);
   private final BooleanProperty silentAimProperty = new BooleanProperty("Silent Aim", false);
   private final BooleanProperty visualizePredictionProperty = new BooleanProperty("Visualize Prediction", true, this::isLagCompensating);
   private final BooleanProperty jumpShotProperty = new BooleanProperty("Jump Shot", true);
   private final BooleanProperty bulletTracersProperty = new BooleanProperty("Bullet Tracers", false);
   private final EnumProperty configuringProperty;
   private final DoubleProperty[] minDamageProperties;
   private final MultiSelectionEnumProperty[] hitBoxSelection;
   private final DoubleProperty maxTargetRange;
   private float recoilCompensation;
   private Shot bestShot;
   private final long[] clientSideShotTimes;
   private final long[] serverSideShotTimes;
   private final List tracers;
   @EventLink
   private final Listener onSendPacket;
   @EventLink(-5)
   private final Listener onRenderModel;
   @EventLink
   private final Listener onReceivePacket;
   @EventLink
   private final Listener onGetFOV;
   @EventLink
   private final Listener onRender3D;
   @EventLink
   private final Listener onUpdatePosition;
   private final Map positionHistoryCache;
   @EventLink
   private final Listener onPlayerUpdatePosition;
   @EventLink
   private final Listener onLoadWorld;

   public CripsVsBloods() {
      super("Crips Vs Bloods", Category.COMBAT, Category.SubCategory.COMBAT_MINI_GAMES);
      this.configuringProperty = new EnumProperty("Configuring", CripsVsBloods.WeaponType.RIFLE);
      this.minDamageProperties = new DoubleProperty[]{new DoubleProperty("Pistol Min. Damage", 30.0, () -> {
         return this.configuringProperty.getValue() == CripsVsBloods.WeaponType.PISTOL;
      }, 1.0, 101.0, 1.0), new DoubleProperty("Shotgun Min. Damage", 50.0, () -> {
         return this.configuringProperty.getValue() == CripsVsBloods.WeaponType.SHOTGUN;
      }, 1.0, 101.0, 1.0), new DoubleProperty("Rifle Min. Damage", 30.0, () -> {
         return this.configuringProperty.getValue() == CripsVsBloods.WeaponType.RIFLE;
      }, 1.0, 101.0, 1.0), new DoubleProperty("SMG Min. Damage", 5.0, () -> {
         return this.configuringProperty.getValue() == CripsVsBloods.WeaponType.SMG;
      }, 1.0, 101.0, 1.0), new DoubleProperty("Sniper Min. Damage", 101.0, () -> {
         return this.configuringProperty.getValue() == CripsVsBloods.WeaponType.SNIPER;
      }, 1.0, 101.0, 1.0)};
      this.hitBoxSelection = new MultiSelectionEnumProperty[]{new MultiSelectionEnumProperty("Pistol Hit Boxes", Lists.newArrayList(new HitBoxType[]{CripsVsBloods.HitBoxType.HEAD}), CripsVsBloods.HitBoxType.values(), () -> {
         return this.configuringProperty.getValue() == CripsVsBloods.WeaponType.PISTOL;
      }), new MultiSelectionEnumProperty("Shotgun Hit Boxes", Lists.newArrayList(new HitBoxType[]{CripsVsBloods.HitBoxType.HEAD, CripsVsBloods.HitBoxType.BODY}), CripsVsBloods.HitBoxType.values(), () -> {
         return this.configuringProperty.getValue() == CripsVsBloods.WeaponType.SHOTGUN;
      }), new MultiSelectionEnumProperty("Rifle Hit Boxes", Lists.newArrayList(new HitBoxType[]{CripsVsBloods.HitBoxType.HEAD, CripsVsBloods.HitBoxType.BODY}), CripsVsBloods.HitBoxType.values(), () -> {
         return this.configuringProperty.getValue() == CripsVsBloods.WeaponType.RIFLE;
      }), new MultiSelectionEnumProperty("SMG Hit Boxes", Lists.newArrayList(new HitBoxType[]{CripsVsBloods.HitBoxType.HEAD, CripsVsBloods.HitBoxType.BODY}), CripsVsBloods.HitBoxType.values(), () -> {
         return this.configuringProperty.getValue() == CripsVsBloods.WeaponType.SMG;
      }), new MultiSelectionEnumProperty("Sniper Hit Boxes", Lists.newArrayList(new HitBoxType[]{CripsVsBloods.HitBoxType.HEAD, CripsVsBloods.HitBoxType.BODY}), CripsVsBloods.HitBoxType.values(), () -> {
         return this.configuringProperty.getValue() == CripsVsBloods.WeaponType.SNIPER;
      })};
      this.maxTargetRange = new DoubleProperty("Max Range", 64.0, 0.0, 128.0, 1.0);
      this.clientSideShotTimes = new long[10];
      this.serverSideShotTimes = new long[10];
      this.tracers = new ArrayList();
      this.onSendPacket = (event) -> {
         Packet packet = event.getPacket();
         if (packet instanceof C08PacketPlayerBlockPlacement) {
            C08PacketPlayerBlockPlacement blockPlacementPacket = (C08PacketPlayerBlockPlacement)packet;
            Weapon weapon;
            if (blockPlacementPacket.getStack() != null && (weapon = this.getWeapon(blockPlacementPacket.getStack())) != null) {
               this.clientSideShotTimes[0] = System.currentTimeMillis();
               Arrays.sort(this.clientSideShotTimes);
               this.recoilCompensation += weapon.recoil * 0.15F;
            }
         }

      };
      this.onRenderModel = (event) -> {
         if (!event.isPre() && (Boolean)this.visualizePredictionProperty.getValue() && this.isLagCompensating() && this.isFeasibleTarget(event.getEntity())) {
            Vec3 prediction = this.calculatePredictionOffset(event.getEntity());
            if (prediction.lengthVector() > 0.0) {
               OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
               GL11.glDisable(3553);
               boolean restore = DrawUtil.glEnableBlend();
               GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
               GL11.glPushMatrix();
               GL11.glRotatef(180.0F - event.getBodyYaw(), 0.0F, -1.0F, 0.0F);
               GL11.glTranslated(prediction.xCoord, prediction.yCoord, prediction.zCoord);
               GL11.glRotatef(180.0F - event.getBodyYaw(), 0.0F, 1.0F, 0.0F);
               event.drawModel();
               GL11.glPopMatrix();
               DrawUtil.glRestoreBlend(restore);
               GL11.glEnable(3553);
            }
         }

      };
      this.onReceivePacket = (event) -> {
         Packet packet = event.getPacket();
         if (packet instanceof S2FPacketSetSlot) {
            S2FPacketSetSlot setSlotPacket = (S2FPacketSetSlot)packet;
            int windowID = setSlotPacket.func_149175_c();
            int slot = setSlotPacket.func_149173_d();
            ItemStack stack = setSlotPacket.func_149174_e();
            if (windowID == 0 && slot >= 36 && stack != null) {
               Weapon weaponInSlot = this.getWeapon(stack);
               if (weaponInSlot != null && !this.isReloading(stack)) {
                  ItemStack stackInSlot = this.mc.thePlayer.inventoryContainer.getSlot(slot).getStack();
                  if (this.getWeapon(stackInSlot) != null && !this.isReloading(stackInSlot)) {
                     if (stack.stackSize < stackInSlot.stackSize) {
                        this.onServerSideShot(weaponInSlot);
                     }

                     return;
                  }

                  return;
               }

               return;
            }

            return;
         } else if (packet instanceof S2APacketParticles) {
            S2APacketParticles particles = (S2APacketParticles)packet;
            if (particles.getParticleType() == EnumParticleTypes.FLAME) {
            }
         }

      };
      this.onGetFOV = (event) -> {
         if (this.getHeldWeapon() == CripsVsBloods.Weapon.AWP && this.isScoped()) {
            event.setFov(event.getFov() / 2.0F);
         }

      };
      this.onRender3D = (event) -> {
         if (!this.tracers.isEmpty()) {
            GL11.glDisable(3553);
            boolean restore = DrawUtil.glEnableBlend();
            GL11.glDisable(2929);
            GL11.glLineWidth(2.0F);
            GL11.glEnable(2848);
            GL11.glHint(3154, 4354);
            long timeOfFrame = System.currentTimeMillis();
            GL11.glBegin(1);
            GL11.glColor4f(1.0F, 0.0F, 0.0F, 0.5F);
            List bulletTracers = this.tracers;
            int i = 0;

            for(int bulletTracersSize = bulletTracers.size(); i < bulletTracersSize; ++i) {
               BulletTracer tracer = (BulletTracer)bulletTracers.get(i);
               if (timeOfFrame - tracer.timeOfCreation > 1000L) {
                  this.tracers.remove(i);
                  --bulletTracersSize;
                  --i;
               } else {
                  GL11.glVertex3d(tracer.start.xCoord, tracer.start.yCoord, tracer.start.zCoord);
                  GL11.glVertex3d(tracer.end.xCoord, tracer.end.yCoord, tracer.end.zCoord);
               }
            }

            GL11.glEnd();
            GL11.glDisable(2848);
            GL11.glHint(3154, 4352);
            GL11.glEnable(2929);
            DrawUtil.glRestoreBlend(restore);
            GL11.glEnable(3553);
         }

      };
      this.onUpdatePosition = (event) -> {
         Weapon weapon;
         if (event.isPre()) {
            this.bestShot = null;
            if (this.timeSinceLastShot(this.serverSideShotTimes) > 500L) {
               this.recoilCompensation = 0.0F;
            }

            if (!(Boolean)this.jumpShotProperty.getValue() && !event.isOnGround()) {
               return;
            }

            if (this.isInvulnerable()) {
               return;
            }

            if (this.isReloading()) {
               return;
            }

            weapon = this.getHeldWeapon();
            if (weapon == null) {
               return;
            }

            this.onFindOptimalShot((shot) -> {
               this.bestShot = shot;
               float[] rotations = RotationUtil.getRotations(shot.src, shot.predictionShotDst);
               event.setYaw(rotations[0]);
               event.setPitch(rotations[1]);
               if (!(Boolean)this.silentAimProperty.getValue()) {
                  this.mc.thePlayer.rotationYaw = rotations[0];
                  this.mc.thePlayer.rotationPitch = rotations[1];
               }

            });
            if ((Boolean)this.recoilControlProperty.getValue()) {
               event.setPitch(event.getPitch() + this.recoilCompensation);
            }
         } else if (this.bestShot != null && (Boolean)this.autoShootProperty.getValue()) {
            weapon = this.getHeldWeapon();
            if (weapon == null) {
               return;
            }

            if (weapon == CripsVsBloods.Weapon.AWP && !this.isScoped()) {
               return;
            }

            if (this.timeSinceLastShot(this.clientSideShotTimes) >= weapon.delay) {
               if ((Boolean)this.bulletTracersProperty.getValue()) {
                  this.tracers.add(new BulletTracer(this.bestShot.src, this.bestShot.predictionShotDst, CripsVsBloods.TracerType.CLIENT));
               }

               this.mc.thePlayer.sendQueue.sendPacket(new C08PacketPlayerBlockPlacement(this.mc.thePlayer.getHeldItem()));
            }
         }

      };
      this.positionHistoryCache = new HashMap(8);
      this.onPlayerUpdatePosition = (event) -> {
         EntityPlayer player = event.getPlayer();
         if (this.isFeasibleTarget(player)) {
            String key = player.getName();
            TimestampedPosition[] history = (TimestampedPosition[])this.positionHistoryCache.get(key);
            if (history != null) {
               Arrays.sort(history);
            } else {
               TimestampedPosition[] buffer = new TimestampedPosition[10];
               buffer[0] = new TimestampedPosition(event.getMove());
               this.positionHistoryCache.put(key, buffer);
            }
         }

      };
      this.onLoadWorld = (event) -> {
         this.clearCaches();
      };
      this.lagCompensation.addValueAlias(0.0, "Auto");
      this.register(new Property[]{this.configuringProperty});
      DoubleProperty[] var1 = this.minDamageProperties;
      int var2 = var1.length;

      int var3;
      for(var3 = 0; var3 < var2; ++var3) {
         DoubleProperty minDmgProp = var1[var3];
         minDmgProp.addValueAlias(101.0, "HP");
         this.register(new Property[]{minDmgProp});
      }

      MultiSelectionEnumProperty[] var5 = this.hitBoxSelection;
      var2 = var5.length;

      for(var3 = 0; var3 < var2; ++var3) {
         MultiSelectionEnumProperty hitBoxProp = var5[var3];
         this.register(new Property[]{hitBoxProp});
      }

      this.register(new Property[]{this.autoShootProperty, this.bulletTracersProperty, this.jumpShotProperty, this.visualizePredictionProperty, this.recoilControlProperty, this.silentAimProperty, this.maxTargetRange, this.lagCompensation});
   }

   private boolean isLagCompensating() {
      return (Double)this.lagCompensation.getValue() > 0.0;
   }

   private long timeBetweenClientServerShot() {
      return this.serverSideShotTimes[this.serverSideShotTimes.length - 1] - this.clientSideShotTimes[this.clientSideShotTimes.length - 1];
   }

   private long timeSinceLastShot(long[] shots) {
      return System.currentTimeMillis() - shots[shots.length - 1];
   }

   private void onServerSideShot(Weapon weapon) {
      this.serverSideShotTimes[0] = System.currentTimeMillis();
      Arrays.sort(this.serverSideShotTimes);
   }

   private boolean isScoped() {
      return this.mc.thePlayer.isSneaking();
   }

   private boolean validateDistToEntity(EntityPlayer entity) {
      double dist = (double)entity.getDistanceToEntity(this.mc.thePlayer);
      return dist < (Double)this.maxTargetRange.getValue();
   }

   private boolean isFeasibleTarget(EntityPlayer entity) {
      return entity.isEntityAlive() && !entity.isInvisible() && entity instanceof EntityOtherPlayerMP && !TeamsUtil.TeamsMode.NAME.getComparator().isOnSameTeam(this.mc.thePlayer, entity) && this.validateDistToEntity(entity);
   }

   private void onFindOptimalShot(Consumer shotConsumer) {
      this.mc.theWorld.playerEntities.stream().filter(this::isFeasibleTarget).sorted(this.getTargetComparator().reversed()).map(this::calculateOptimalShot).filter(Objects::nonNull).findFirst().ifPresent(shotConsumer);
   }

   private Comparator getTargetComparator() {
      return Comparator.comparingDouble((player) -> {
         double distanceWeight = 1.0 - (double)this.mc.thePlayer.getDistanceToEntity(player) / (Double)this.maxTargetRange.getValue();
         double heightWeight = 1.0 - (double)(player.getHealth() / player.getMaxHealth());
         return distanceWeight + heightWeight;
      });
   }

   private Shot calculateOptimalShot(EntityPlayer player) {
      Vec3 baseShot = RotationUtil.getHitOrigin(player);
      Vec3 dst = baseShot.add(this.calculatePredictionOffset(player));
      Vec3 src = RotationUtil.getHitOrigin(this.mc.thePlayer);
      return RotationUtil.validateHitVec(this.mc, src, dst, false) ? new Shot(src, baseShot, dst, true, false) : null;
   }

   private Prediction runPrediction(EntityPlayer player) {
      Vec3 src = RotationUtil.getHitOrigin(player);
      AxisAlignedBB bb = player.getEntityBoundingBox();
      return null;
   }

   private Vec3 calculatePredictionOffset(EntityPlayer player) {
      double lagCompTicks = (Double)this.lagCompensation.getValue() / 50.0;
      double dx = player.posX - player.prevPosX;
      double dy = player.posY - player.prevPosY;
      double dz = player.posZ - player.prevPosZ;
      return new Vec3(dx * lagCompTicks, dy, dz * lagCompTicks);
   }

   private boolean isInvulnerable() {
      return this.mc.ingameGUI.field_175200_y.startsWith("Invulnerable");
   }

   private boolean isReloading() {
      return this.isReloading(this.mc.thePlayer.getHeldItem());
   }

   private boolean isReloading(ItemStack stack) {
      return stack == null ? false : stack.isItemDamaged();
   }

   private Weapon getHeldWeapon() {
      return this.getWeapon(this.mc.thePlayer.getHeldItem());
   }

   private Weapon getWeapon(ItemStack stack) {
      if (stack == null) {
         return null;
      } else {
         String stackDisplayName = StringUtils.stripControlCodes(stack.getDisplayName());
         return stackDisplayName == null ? null : (Weapon)this.getWeapons().filter((weapon) -> {
            return stackDisplayName.startsWith(weapon.name);
         }).findAny().orElse((Object)null);
      }
   }

   private double getMinDamage(Weapon weapon) {
      return (Double)this.minDamageProperties[weapon.type.ordinal()].getValue();
   }

   private Stream getWeapons() {
      return Arrays.stream(CripsVsBloods.Weapon.values());
   }

   public void onEnable() {
      this.bestShot = null;
      this.recoilCompensation = 0.0F;
      this.clearCaches();
   }

   private void clearCaches() {
      Arrays.fill(this.clientSideShotTimes, 0L);
      Arrays.fill(this.serverSideShotTimes, 0L);
      this.tracers.clear();
      this.positionHistoryCache.clear();
   }

   public void onDisable() {
   }

   private static enum Weapon {
      USP("USP", CripsVsBloods.WeaponType.PISTOL, 1, 13, 41.0, 80.0, 400L),
      HK45("HK45", CripsVsBloods.WeaponType.PISTOL, 1, 10, 44.0, 88.0, 250L),
      MP5("MP5", CripsVsBloods.WeaponType.SMG, 3, 40, 1.0, 1.0, 50L),
      M4("M4", CripsVsBloods.WeaponType.RIFLE, 2, 30, 33.0, 104.0, 100L),
      P90("P90", CripsVsBloods.WeaponType.SMG, 3, 35, 24.0, 50.0, 100L),
      PUMP_ACTION("Pump Action", CripsVsBloods.WeaponType.SHOTGUN, 2, 8, 1.0, 1.0, 1400L),
      SPAS("SPAS-12", CripsVsBloods.WeaponType.SHOTGUN, 2, 10, 1.0, 1.0, 550L),
      DEAGLE("Desert Eagle", CripsVsBloods.WeaponType.PISTOL, 3, 7, 50.0, 122.0, 400L),
      AUG("Steyr AUG", CripsVsBloods.WeaponType.RIFLE, 4, 30, 1.0, 1.0, 500L),
      AWP("50 Cal", CripsVsBloods.WeaponType.SNIPER, 1, 10, 80.0, 711.0, 950L),
      AK_47("AK-47", CripsVsBloods.WeaponType.RIFLE, 4, 30, 44.0, 121.0, 150L);

      private final String name;
      private final WeaponType type;
      private final float recoil;
      private final int maxAmmo;
      private final double baseDamage;
      private final double headshotDamage;
      private final long delay;

      private Weapon(String name, WeaponType type, int recoil, int maxAmmo, double baseDamage, double headshotDamage, long delay) {
         this.name = name;
         this.type = type;
         this.recoil = (float)recoil;
         this.baseDamage = baseDamage;
         this.headshotDamage = headshotDamage;
         this.maxAmmo = maxAmmo;
         this.delay = delay;
      }
   }

   private static enum HitBoxType {
      HEAD("Head"),
      BODY("Body");

      private final String name;

      private HitBoxType(String name) {
         this.name = name;
      }

      public String toString() {
         return this.name;
      }
   }

   private static class TimestampedPosition implements Comparable {
      private final Vec3 position;
      private final long timestamp;

      public TimestampedPosition(Vec3 position) {
         this.position = position;
         this.timestamp = System.currentTimeMillis();
      }

      public int compareTo(TimestampedPosition other) {
         return Long.compare(this.timestamp, other.timestamp);
      }
   }

   private static class Prediction {
      private final Vec3 src;
      private final Vec3 dst;
      private final List samples;
      private final double confidence;
      private final double srcDistance;

      public Prediction(Vec3 src, Vec3 dst, List samples, double confidence, double srcDistance) {
         this.src = src;
         this.dst = dst;
         this.samples = samples;
         this.confidence = confidence;
         this.srcDistance = srcDistance;
      }
   }

   private static enum TracerType {
      CLIENT,
      SERVER;
   }

   private static final class BulletTracer {
      private final Vec3 start;
      private final Vec3 end;
      private final TracerType type;
      private final long timeOfCreation;

      BulletTracer(Vec3 start, Vec3 end, TracerType type) {
         this.start = start;
         this.end = end;
         this.type = type;
         this.timeOfCreation = System.currentTimeMillis();
      }
   }

   private static enum WeaponType {
      PISTOL("Pistol"),
      SHOTGUN("Shotgun"),
      RIFLE("Rifle"),
      SMG("SMG"),
      SNIPER("Sniper");

      private final String name;

      private WeaponType(String name) {
         this.name = name;
      }

      public String toString() {
         return this.name;
      }
   }

   private static class Shot {
      private final Vec3 src;
      private final Vec3 baseShotDst;
      private final Vec3 predictionShotDst;
      private final boolean headshot;
      private final boolean wallbang;

      public Shot(Vec3 src, Vec3 baseShotDst, Vec3 predictionShotDst, boolean headshot, boolean wallbang) {
         this.src = src;
         this.baseShotDst = baseShotDst;
         this.predictionShotDst = predictionShotDst;
         this.headshot = headshot;
         this.wallbang = wallbang;
      }
   }
}
