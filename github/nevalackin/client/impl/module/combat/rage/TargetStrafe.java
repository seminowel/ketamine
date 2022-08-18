package io.github.nevalackin.client.impl.module.combat.rage;

import io.github.nevalackin.client.api.module.Category;
import io.github.nevalackin.client.api.module.Module;
import io.github.nevalackin.client.api.property.Property;
import io.github.nevalackin.client.impl.core.KetamineClient;
import io.github.nevalackin.client.impl.event.player.MoveEvent;
import io.github.nevalackin.client.impl.event.player.moveflying.MoveFlyingInputEvent;
import io.github.nevalackin.client.impl.property.BooleanProperty;
import io.github.nevalackin.client.impl.property.ColourProperty;
import io.github.nevalackin.client.impl.property.DoubleProperty;
import io.github.nevalackin.client.impl.property.EnumProperty;
import io.github.nevalackin.client.util.math.MathUtil;
import io.github.nevalackin.client.util.movement.MovementUtil;
import io.github.nevalackin.client.util.player.RotationUtil;
import io.github.nevalackin.client.util.render.DrawUtil;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public final class TargetStrafe extends Module {
   public final BooleanProperty holdSpaceProperty = new BooleanProperty("Hold Space", true);
   private final EnumProperty modeProperty;
   private final DoubleProperty pointsProperty;
   private final DoubleProperty radiusProperty;
   private final BooleanProperty adaptiveSpeedProperty;
   private final BooleanProperty directionKeyProperty;
   private final EnumProperty renderProperty;
   private final BooleanProperty polyGradientProperty;
   private final ColourProperty activePointColorProperty;
   private final ColourProperty dormantPointColorProperty;
   private final ColourProperty invalidPointColorProperty;
   private final DoubleProperty widthProperty;
   private final List currentPoints;
   public EntityLivingBase currentTarget;
   private int direction;
   public Point currentPoint;
   private Aura aura;
   @EventLink
   public final Listener onRender3DEvent;
   @EventLink
   public final Listener onUpdatePositionEvent;

   public TargetStrafe() {
      super("Target Strafe", Category.COMBAT, Category.SubCategory.COMBAT_RAGE);
      this.modeProperty = new EnumProperty("Mode", TargetStrafe.Mode.FOLLOW);
      this.pointsProperty = new DoubleProperty("Points", 12.0, 1.0, 90.0, 1.0);
      this.radiusProperty = new DoubleProperty("Radius", 2.0, 0.1, 4.0, 0.1);
      this.adaptiveSpeedProperty = new BooleanProperty("Adapt Speed", true);
      this.directionKeyProperty = new BooleanProperty("Direction Keys", true);
      this.renderProperty = new EnumProperty("Render Mode", TargetStrafe.RenderMode.POINTS);
      this.polyGradientProperty = new BooleanProperty("Poly Gradient", true, () -> {
         return this.renderProperty.getValue() == TargetStrafe.RenderMode.POLYGON;
      });
      this.activePointColorProperty = new ColourProperty("Active", -2147418368, this::shouldRender);
      this.dormantPointColorProperty = new ColourProperty("Dormant", 553648127, this::shouldRender);
      this.invalidPointColorProperty = new ColourProperty("Invalid", 553582592, () -> {
         return this.renderProperty.getValue() == TargetStrafe.RenderMode.POINTS || this.polyGradientProperty.check() && (Boolean)this.polyGradientProperty.getValue();
      });
      this.widthProperty = new DoubleProperty("Width", 1.0, () -> {
         return this.renderProperty.getValue() == TargetStrafe.RenderMode.POLYGON;
      }, 0.5, 5.0, 0.5);
      this.currentPoints = new ArrayList();
      this.direction = 1;
      this.onRender3DEvent = (event) -> {
         if (this.shouldRender() && this.currentTarget != null) {
            float partialTicks = event.getPartialTicks();
            int dormantColor = (Integer)this.dormantPointColorProperty.getValue();
            int invalidColor = (Integer)this.invalidPointColorProperty.getValue();
            GL11.glDisable(3553);
            boolean restore = DrawUtil.glEnableBlend();
            GL11.glDisable(2929);
            GL11.glDepthMask(false);
            Point lastPoint = null;
            Point pointx;
            switch ((RenderMode)this.renderProperty.getValue()) {
               case POINTS:
                  for(Iterator var20 = this.currentPoints.iterator(); var20.hasNext(); lastPoint = pointx) {
                     pointx = (Point)var20.next();
                     Vec3 pos = pointx.calculateInterpolatedPos(partialTicks);
                     double x = pos.xCoord;
                     double y = pos.yCoord;
                     double z = pos.zCoord;
                     double pointSize = 0.03;
                     AxisAlignedBB bb = new AxisAlignedBB(x, y, z, x + 0.03, y + 0.03, z + 0.03);
                     if (lastPoint == null || lastPoint == this.currentPoint || pointx == this.currentPoint || lastPoint.valid != pointx.valid) {
                        int colorx;
                        if (this.currentPoint == pointx) {
                           colorx = (Integer)this.activePointColorProperty.getValue();
                        } else if (pointx.valid) {
                           colorx = dormantColor;
                        } else {
                           colorx = invalidColor;
                        }

                        DrawUtil.glColour(colorx);
                     }

                     DrawUtil.glDrawBoundingBox(bb, 0.0F, true);
                  }
                  break;
               case POLYGON:
                  GL11.glEnable(2848);
                  GL11.glHint(3154, 4354);
                  GL11.glLineWidth(((Double)this.widthProperty.getValue()).floatValue());
                  boolean polyGradient = (Boolean)this.polyGradientProperty.getValue();
                  if (polyGradient) {
                     GL11.glShadeModel(7425);
                  } else {
                     DrawUtil.glColour(this.shouldStrafe() ? (Integer)this.activePointColorProperty.getValue() : dormantColor);
                  }

                  GL11.glBegin(2);
                  Iterator var8 = this.currentPoints.iterator();

                  while(var8.hasNext()) {
                     Point point = (Point)var8.next();
                     if (polyGradient) {
                        if (lastPoint == null || lastPoint == this.currentPoint || point == this.currentPoint || lastPoint.valid != point.valid) {
                           int color;
                           if (this.currentPoint == point) {
                              color = (Integer)this.activePointColorProperty.getValue();
                           } else if (point.valid) {
                              color = dormantColor;
                           } else {
                              color = invalidColor;
                           }

                           DrawUtil.glColour(color);
                        }

                        lastPoint = point;
                     }

                     Vec3 posx = point.calculateInterpolatedPos(partialTicks);
                     double xx = posx.xCoord;
                     double yx = posx.yCoord;
                     double zx = posx.zCoord;
                     GL11.glVertex3d(xx, yx, zx);
                  }

                  GL11.glEnd();
                  if (polyGradient) {
                     GL11.glShadeModel(7424);
                  }

                  GL11.glDisable(2848);
            }

            GL11.glDisable(2848);
            GL11.glHint(3154, 4352);
            GL11.glEnable(2929);
            GL11.glDepthMask(true);
            DrawUtil.glRestoreBlend(restore);
            GL11.glEnable(3553);
         }

      };
      this.onUpdatePositionEvent = (event) -> {
         if (event.isPre()) {
            this.currentTarget = this.aura.getTarget();
            if (this.currentTarget != null) {
               if ((Boolean)this.directionKeyProperty.getValue()) {
                  if (this.mc.gameSettings.keyBindLeft.isPressed()) {
                     this.direction = 1;
                  }

                  if (this.mc.gameSettings.keyBindRight.isPressed()) {
                     this.direction = -1;
                  }
               }

               this.collectPoints(((Double)this.pointsProperty.getValue()).intValue(), (Double)this.radiusProperty.getValue(), this.currentTarget);
               this.currentPoint = this.findOptimalPoint(this.currentTarget, this.currentPoints);
            } else {
               this.currentPoint = null;
            }
         }

      };
      targetStrafeInstance = this;
      this.register(new Property[]{this.modeProperty, this.holdSpaceProperty, this.pointsProperty, this.radiusProperty, this.adaptiveSpeedProperty, this.directionKeyProperty, this.renderProperty, this.polyGradientProperty, this.activePointColorProperty, this.dormantPointColorProperty, this.invalidPointColorProperty, this.widthProperty});
   }

   private boolean shouldRender() {
      return this.renderProperty.getValue() != TargetStrafe.RenderMode.OFF;
   }

   private Point findOptimalPoint(EntityLivingBase target, List points) {
      switch ((Mode)this.modeProperty.getValue()) {
         case BEHIND:
            float biggestDif = -1.0F;
            Point bestPoint = null;
            Iterator var5 = points.iterator();

            while(var5.hasNext()) {
               Point point = (Point)var5.next();
               if (point.valid) {
                  float yawChange = Math.abs(RotationUtil.calculateYawFromSrcToDst(target.rotationYaw, target.posX, target.posZ, point.point.xCoord, point.point.zCoord));
                  if (yawChange > biggestDif) {
                     biggestDif = yawChange;
                     bestPoint = point;
                  }
               }
            }

            return bestPoint;
         case FOLLOW:
            return getClosestPoint(this.mc.thePlayer.posX, this.mc.thePlayer.posZ, points);
         default:
            Point closest = getClosestPoint(this.mc.thePlayer.posX, this.mc.thePlayer.posZ, points);
            if (closest == null) {
               return null;
            } else {
               int pointsSize = points.size();
               if (pointsSize == 1) {
                  return closest;
               } else {
                  int closestIndex = points.indexOf(closest);
                  int passes = 0;

                  Point nextPoint;
                  do {
                     if (passes > pointsSize) {
                        return null;
                     }

                     int nextIndex = closestIndex + this.direction;
                     if (nextIndex < 0) {
                        nextIndex = pointsSize - 1;
                     } else if (nextIndex >= pointsSize) {
                        nextIndex = 0;
                     }

                     nextPoint = (Point)points.get(nextIndex);
                     if (!nextPoint.valid) {
                        this.direction = -this.direction;
                     }

                     ++passes;
                  } while(!nextPoint.valid);

                  return nextPoint;
               }
            }
      }
   }

   private void collectPoints(int size, double radius, EntityLivingBase entity) {
      this.currentPoints.clear();
      double x = entity.posX;
      double z = entity.posZ;
      double pix2 = 6.283185307179586;

      for(int i = 0; i < size; ++i) {
         double cos = radius * StrictMath.cos((double)i * 6.283185307179586 / (double)size);
         double sin = radius * StrictMath.sin((double)i * 6.283185307179586 / (double)size);
         Point point = new Point(entity, new Vec3(cos, 0.0, sin), this.validatePoint(new Vec3(x + cos, entity.posY, z + sin)));
         this.currentPoints.add(point);
      }

   }

   private static Point getClosestPoint(double srcX, double srcZ, List points) {
      double closest = Double.MAX_VALUE;
      Point bestPoint = null;
      Iterator var8 = points.iterator();

      while(var8.hasNext()) {
         Point point = (Point)var8.next();
         if (point.valid) {
            double dist = MathUtil.distance(srcX, srcZ, point.point.xCoord, point.point.zCoord);
            if (dist < closest) {
               closest = dist;
               bestPoint = point;
            }
         }
      }

      return bestPoint;
   }

   private boolean validatePoint(Vec3 point) {
      EntityPlayer player = this.mc.thePlayer;
      WorldClient world = this.mc.theWorld;
      MovingObjectPosition rayTraceResult = this.mc.theWorld.rayTraceBlocks(player.getPositionVector(), point, false, true, false);
      if (rayTraceResult != null && rayTraceResult.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
         return false;
      } else {
         BlockPos pointPos = new BlockPos(point);
         IBlockState blockState = world.getBlockState(pointPos);
         if (blockState.getBlock().canCollideCheck(blockState, false) && !blockState.getBlock().isPassable(this.mc.theWorld, pointPos)) {
            return false;
         } else {
            IBlockState blockStateAbove = world.getBlockState(pointPos.add(0, 1, 0));
            return !blockStateAbove.getBlock().canCollideCheck(blockState, false) && !this.isOverVoid(point.xCoord, Math.min(point.yCoord, this.mc.thePlayer.posY), point.zCoord);
         }
      }
   }

   private boolean isOverVoid(double x, double y, double z) {
      for(double posY = y; posY > 0.0; --posY) {
         IBlockState state = this.mc.theWorld.getBlockState(new BlockPos(x, posY, z));
         if (state.getBlock().canCollideCheck(state, false)) {
            return y - posY > 2.0;
         }
      }

      return true;
   }

   public boolean isCloseToPoint(Point point) {
      return MathUtil.distance(this.mc.thePlayer.posX, this.mc.thePlayer.posZ, point.point.xCoord, point.point.zCoord) < 0.2;
   }

   public boolean shouldAdaptSpeed() {
      return !(Boolean)this.adaptiveSpeedProperty.getValue() ? false : this.isCloseToPoint(this.currentPoint);
   }

   public double getAdaptedSpeed() {
      EntityLivingBase entity = this.currentTarget;
      return entity == null ? 0.0 : MathUtil.distance(entity.prevPosX, entity.prevPosZ, entity.posX, entity.posZ);
   }

   public boolean shouldStrafe() {
      return this.isEnabled() && (!(Boolean)this.holdSpaceProperty.getValue() || Keyboard.isKeyDown(57)) && this.currentTarget != null && this.currentPoint != null;
   }

   public void setSpeed(MoveFlyingInputEvent event, double speed) {
      EntityPlayerSP player = this.mc.thePlayer;
      Point point = this.currentPoint;
      MovementUtil.setSpeedMoveFlying(this.mc.thePlayer, event, speed, 1.0F, 0.0F, RotationUtil.calculateYawFromSrcToDst(player.rotationYaw, player.posX, player.posZ, point.point.xCoord, point.point.zCoord));
   }

   public void setSpeed(MoveEvent event, double speed) {
      EntityPlayerSP player = this.mc.thePlayer;
      Point point = this.currentPoint;
      MovementUtil.setSpeed(event, speed, 1.0F, 0.0F, RotationUtil.calculateYawFromSrcToDst(player.rotationYaw, player.posX, player.posZ, point.point.xCoord, point.point.zCoord));
   }

   public void onEnable() {
      if (this.aura == null) {
         this.aura = (Aura)KetamineClient.getInstance().getModuleManager().getModule(Aura.class);
      }

   }

   public void onDisable() {
      this.currentPoints.clear();
   }

   public static final class Point {
      private final EntityLivingBase entity;
      private final Vec3 posOffset;
      public final Vec3 point;
      private final boolean valid;

      public Point(EntityLivingBase entity, Vec3 posOffset, boolean valid) {
         this.entity = entity;
         this.posOffset = posOffset;
         this.valid = valid;
         this.point = this.calculatePos();
      }

      private Vec3 calculatePos() {
         return this.entity.getPositionVector().add(this.posOffset);
      }

      private Vec3 calculateInterpolatedPos(float partialTicks) {
         double x = DrawUtil.interpolate(this.entity.prevPosX, this.entity.posX, (double)partialTicks);
         double y = DrawUtil.interpolate(this.entity.prevPosY, this.entity.posY, (double)partialTicks);
         double z = DrawUtil.interpolate(this.entity.prevPosZ, this.entity.posZ, (double)partialTicks);
         Vec3 interpolatedEntity = new Vec3(x, y, z);
         return interpolatedEntity.add(this.posOffset);
      }
   }

   private static enum RenderMode {
      OFF("Off"),
      POINTS("Points"),
      POLYGON("Polygon");

      private final String name;

      private RenderMode(String name) {
         this.name = name;
      }

      public String toString() {
         return this.name;
      }
   }

   private static enum Mode {
      BEHIND("Behind"),
      FOLLOW("Follow"),
      CIRCLE("Circle");

      private final String name;

      private Mode(String name) {
         this.name = name;
      }

      public String toString() {
         return this.name;
      }
   }
}
