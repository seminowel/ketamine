package io.github.nevalackin.client.impl.module.render.esp.esp;

import io.github.nevalackin.client.api.module.Category;
import io.github.nevalackin.client.api.module.Module;
import io.github.nevalackin.client.api.property.Property;
import io.github.nevalackin.client.impl.core.KetamineClient;
import io.github.nevalackin.client.impl.module.misc.world.Scaffold;
import io.github.nevalackin.client.impl.module.render.esp.esp.components.Bar;
import io.github.nevalackin.client.impl.module.render.esp.esp.components.Box;
import io.github.nevalackin.client.impl.module.render.esp.esp.components.Tag;
import io.github.nevalackin.client.impl.property.BooleanProperty;
import io.github.nevalackin.client.impl.property.ColourProperty;
import io.github.nevalackin.client.impl.property.EnumProperty;
import io.github.nevalackin.client.util.player.RotationUtil;
import io.github.nevalackin.client.util.render.ColourUtil;
import io.github.nevalackin.client.util.render.DrawUtil;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

public final class ESP extends Module {
   private final BooleanProperty tagsProperty = new BooleanProperty("Tags", true);
   private final ColourProperty tagsColourProperty;
   private final BooleanProperty boxProperty;
   private final BooleanProperty chestProperty;
   private final BooleanProperty blurProperty;
   private final BooleanProperty colourProperty;
   private final ColourProperty boxColourProperty;
   private final EnumProperty healthBarProperty;
   private final ColourProperty healthBarColourProperty;
   private final ColourProperty secondHealthBarColourProperty;
   private final BooleanProperty healthProperty;
   private final BooleanProperty armourBarProperty;
   private final ColourProperty armourBarColourProperty;
   private final BooleanProperty skeletonsProperty;
   private final List drawables;
   private final double[] projectionBuffer;
   private final double[] screenBuffer;
   private final DecimalFormat format;
   private Frustum frustum;
   private Scaffold scaffold;
   @EventLink
   private final Listener onRenderName;
   @EventLink(0)
   private final Listener onRenderGameOverlay;
   @EventLink
   private final Listener onFrustumCull;
   @EventLink
   private final Listener onUpdateFramebuffer;
   @EventLink
   private final Listener onRender3D;

   public ESP() {
      super("ESP", Category.RENDER, Category.SubCategory.RENDER_ESP);
      BooleanProperty var10005 = this.tagsProperty;
      var10005.getClass();
      this.tagsColourProperty = new ColourProperty("Tags Colour", -1, var10005::getValue);
      this.boxProperty = new BooleanProperty("Box", true);
      var10005 = this.boxProperty;
      var10005.getClass();
      this.chestProperty = new BooleanProperty("Chests", false, var10005::getValue);
      this.blurProperty = new BooleanProperty("Blur", true);
      var10005 = this.blurProperty;
      var10005.getClass();
      this.colourProperty = new BooleanProperty("Coloured Blur", true, var10005::getValue);
      var10005 = this.boxProperty;
      var10005.getClass();
      this.boxColourProperty = new ColourProperty("Box Colour", -10020834, var10005::getValue);
      this.healthBarProperty = new EnumProperty("Health Bar", ESP.HealthBarColour.HEALTH);
      this.healthBarColourProperty = new ColourProperty("Health Bar Colour", ColourUtil.getClientColour(), () -> {
         return this.healthBarProperty.check() && this.healthBarProperty.getValue() != ESP.HealthBarColour.HEALTH;
      });
      this.secondHealthBarColourProperty = new ColourProperty("Second Health Bar Colour", ColourUtil.getSecondaryColour(), () -> {
         return this.healthBarProperty.check() && this.healthBarProperty.getValue() == ESP.HealthBarColour.GRADIENT;
      });
      this.healthProperty = new BooleanProperty("Health", true, () -> {
         return this.healthBarProperty.getValue() != ESP.HealthBarColour.OFF;
      });
      this.armourBarProperty = new BooleanProperty("Armour Bar", true);
      var10005 = this.armourBarProperty;
      var10005.getClass();
      this.armourBarColourProperty = new ColourProperty("Armour Bar Colour", -12549889, var10005::getValue);
      this.skeletonsProperty = new BooleanProperty("Skeletons", true);
      this.drawables = new ArrayList();
      this.projectionBuffer = new double[2];
      this.screenBuffer = new double[3];
      this.format = new DecimalFormat("0.0");
      this.onRenderName = (event) -> {
         if ((Boolean)this.tagsProperty.getValue() && event.getEntity() instanceof EntityPlayer && this.validatePlayer((EntityPlayer)event.getEntity())) {
            event.setCancelled();
         }

      };
      this.onRenderGameOverlay = (event) -> {
         ScaledResolution scaledResolution = event.getScaledResolution();
         int scaling = scaledResolution.getScaleFactor();
         double scale = 2.0 / (double)scaling;
         GL11.glScaled(scale, scale, 1.0);
         GL11.glEnable(2848);
         GL11.glHint(3154, 4354);
         GL11.glLineWidth(1.0F);
         boolean restore = DrawUtil.glEnableBlend();

         for(int size = this.drawables.size(); size > 0; --size) {
            Drawable drawable = (Drawable)this.drawables.remove(0);
            drawable.draw(KetamineClient.getInstance().getFontRenderer());
         }

         DrawUtil.glRestoreBlend(restore);
         GL11.glDisable(2848);
         GL11.glHint(3154, 4352);
         GL11.glScaled(1.0 / scale, 1.0 / scale, 1.0);
      };
      this.onFrustumCull = (event) -> {
         this.frustum = event.getFrustum();
      };
      this.onUpdateFramebuffer = (event) -> {
         this.updateScreenBuffer(new ScaledResolution(this.mc));
      };
      this.onRender3D = (event) -> {
         if (this.frustum != null) {
            ScaledResolution scaledResolution = event.getScaledResolution();
            double maxBoxWidth = (double)scaledResolution.getScaledWidth() / 3.0;
            Iterator var5 = this.mc.theWorld.playerEntities.iterator();

            while(true) {
               EntityPlayer player;
               double[] position;
               do {
                  do {
                     AxisAlignedBB boundingBox;
                     do {
                        do {
                           if (!var5.hasNext()) {
                              if ((Boolean)this.chestProperty.getValue() && (Boolean)this.boxProperty.getValue()) {
                                 var5 = this.mc.theWorld.loadedTileEntityList.iterator();

                                 while(var5.hasNext()) {
                                    TileEntity tileEntity = (TileEntity)var5.next();
                                    if (this.validateTileEntity(tileEntity)) {
                                       boundingBox = tileEntity.getBlockType().getCollisionBoundingBox(this.mc.theWorld, tileEntity.getPos(), tileEntity.getBlockType().getBlockState().getBaseState());
                                       if (boundingBox != null && this.frustum.isBoundingBoxInFrustum(boundingBox)) {
                                          position = DrawUtil.worldToScreen((double[])null, boundingBox, this.screenBuffer, this.projectionBuffer);
                                          if (position != null && !(position[2] - position[0] > maxBoxWidth)) {
                                             this.drawables.add(new Drawable(position, new Box(0.5, -220923), (List)null, (List)null, (Boolean)this.blurProperty.getValue(), (Boolean)this.colourProperty.getValue()));
                                          }
                                       }
                                    }
                                 }
                              }

                              return;
                           }

                           player = (EntityPlayer)var5.next();
                        } while(!this.validatePlayer(player));

                        boundingBox = DrawUtil.interpolate(player, RotationUtil.getHittableBoundingBox(player), event.getPartialTicks());
                     } while(!this.frustum.isBoundingBoxInFrustum(boundingBox));

                     position = DrawUtil.worldToScreen(DrawUtil.interpolate(player, event.getPartialTicks()), boundingBox, this.screenBuffer, this.projectionBuffer);
                  } while(position == null);
               } while(position[2] - position[0] > maxBoxWidth);

               List bars = new ArrayList();
               List tags = new ArrayList();
               if (this.healthBarProperty.getValue() != ESP.HealthBarColour.OFF) {
                  float healthPercentage = player.getHealth() / player.getMaxHealth();
                  bars.add(new Bar(1.0, EnumPosition.LEFT, true, (Boolean)this.healthProperty.getValue(), this.healthBarProperty.getValue() == ESP.HealthBarColour.HEALTH ? ColourUtil.blendHealthColours((double)healthPercentage) : (Integer)this.healthBarColourProperty.getValue(), (Integer)this.secondHealthBarColourProperty.getValue(), this.healthBarProperty.getValue() == ESP.HealthBarColour.GRADIENT, () -> {
                     return (double)healthPercentage;
                  }));
                  if (player.getAbsorptionAmount() > 0.0F) {
                     double percentage = (double)(player.getAbsorptionAmount() / player.getMaxHealth());
                     if (percentage <= 1.0) {
                        bars.add(new Bar(1.0, EnumPosition.LEFT, true, false, -256, 0, false, () -> {
                           return percentage;
                        }));
                     } else {
                        double temp = percentage;

                        do {
                           double min = Math.min(1.0, temp);
                           temp -= min;
                           bars.add(new Bar(1.0, EnumPosition.LEFT, true, false, -256, 0, false, () -> {
                              return min;
                           }));
                        } while(temp > 1.0);

                        bars.add(new Bar(1.0, EnumPosition.LEFT, true, false, -256, 0, false, () -> {
                           return temp;
                        }));
                     }
                  }
               }

               if ((Boolean)this.armourBarProperty.getValue()) {
                  bars.add(new Bar(1.0, EnumPosition.BOTTOM, true, false, (Integer)this.armourBarColourProperty.getValue(), 0, false, () -> {
                     return (double)player.getTotalArmorValue() / 20.0;
                  }));
               }

               if ((Boolean)this.tagsProperty.getValue()) {
                  String name = player.getDisplayName().getFormattedText();
                  tags.add(new Tag(name, EnumPosition.TOP, (Integer)this.tagsColourProperty.getValue(), true));
               }

               if (player instanceof EntityPlayerSP && this.scaffold.angles != null) {
                  tags.add(new Tag(this.format.format((double)MathHelper.wrapAngleTo180_float(this.scaffold.angles[0])), EnumPosition.RIGHT, -1, true));
                  tags.add(new Tag(this.format.format(Scaffold.towering ? 90.0 : 70.0), EnumPosition.LOWER_RIGHT, -1, true));
               }

               this.drawables.add(new Drawable(position, (Boolean)this.boxProperty.getValue() ? new Box(0.5, (Integer)this.boxColourProperty.getValue()) : null, bars, tags, (Boolean)this.blurProperty.getValue(), (Boolean)this.colourProperty.getValue()));
            }
         }
      };
      this.tagsProperty.attachProperty(this.tagsColourProperty);
      this.boxProperty.attachProperty(this.boxColourProperty);
      this.healthBarProperty.attachProperty(this.healthBarColourProperty);
      this.healthBarProperty.attachProperty(this.secondHealthBarColourProperty);
      this.armourBarProperty.attachProperty(this.armourBarColourProperty);
      this.register(new Property[]{this.tagsProperty, this.tagsColourProperty, this.boxProperty, this.boxColourProperty, this.chestProperty, this.blurProperty, this.colourProperty, this.healthBarProperty, this.healthBarColourProperty, this.secondHealthBarColourProperty, this.healthProperty, this.armourBarProperty, this.armourBarColourProperty, this.skeletonsProperty});
   }

   private static void drawSkeleton(float partialTicks, EntityPlayer player, float[][] modelRotations) {
      GL11.glPushMatrix();
      double x = DrawUtil.interpolate(player.prevPosX, player.posX, (double)partialTicks);
      double y = DrawUtil.interpolate(player.prevPosY, player.posY, (double)partialTicks);
      double z = DrawUtil.interpolate(player.prevPosZ, player.posZ, (double)partialTicks);
      float rotationYawHead = player.rotationYawHead;
      float prevRotationYawHead = player.prevRotationYawHead;
      float renderYawOffset = player.renderYawOffset;
      float prevRenderYawOffset = player.prevRenderYawOffset;
      rotationYawHead = DrawUtil.interpolate(prevRotationYawHead, rotationYawHead, partialTicks);
      renderYawOffset = DrawUtil.interpolate(prevRenderYawOffset, renderYawOffset, partialTicks);
      boolean sneaking = player.isSneaking();
      float yOff = sneaking ? 0.6F : 0.75F;
      GL11.glTranslated(x, y, z);
      GL11.glRotatef(-renderYawOffset, 0.0F, 1.0F, 0.0F);
      GL11.glTranslatef(0.0F, 0.0F, sneaking ? -0.235F : 0.0F);
      GL11.glPushMatrix();
      float[] leftArmAngles = modelRotations[3];
      GL11.glTranslatef(-0.125F, yOff, 0.0F);
      GL11.glRotatef(leftArmAngles[0] * 57.295776F, 1.0F, 0.0F, 0.0F);
      GL11.glRotatef(leftArmAngles[1] * 57.295776F, 0.0F, 1.0F, 0.0F);
      GL11.glRotatef(leftArmAngles[2] * 57.295776F, 0.0F, 0.0F, 1.0F);
      GL11.glBegin(3);
      GL11.glVertex3i(0, 0, 0);
      GL11.glVertex3f(0.0F, -yOff + 0.125F, 0.0F);
      GL11.glEnd();
      GL11.glPopMatrix();
      GL11.glPushMatrix();
      leftArmAngles = modelRotations[4];
      GL11.glTranslatef(0.125F, yOff, 0.0F);
      GL11.glRotatef(leftArmAngles[0] * 57.295776F, 1.0F, 0.0F, 0.0F);
      GL11.glRotatef(leftArmAngles[1] * 57.295776F, 0.0F, 1.0F, 0.0F);
      GL11.glRotatef(leftArmAngles[2] * 57.295776F, 0.0F, 0.0F, 1.0F);
      GL11.glBegin(3);
      GL11.glVertex3i(0, 0, 0);
      GL11.glVertex3f(0.0F, -yOff + 0.125F, 0.0F);
      GL11.glEnd();
      GL11.glPopMatrix();
      GL11.glTranslatef(0.0F, 0.0F, sneaking ? 0.25F : 0.0F);
      GL11.glPushMatrix();
      GL11.glTranslatef(0.0F, sneaking ? -0.05F : 0.0F, sneaking ? -0.01725F : 0.0F);
      GL11.glPushMatrix();
      leftArmAngles = modelRotations[1];
      GL11.glTranslatef(-0.375F, yOff + 0.55F, 0.0F);
      GL11.glRotatef(leftArmAngles[0] * 57.295776F, 1.0F, 0.0F, 0.0F);
      GL11.glRotatef(leftArmAngles[1] * 57.295776F, 0.0F, 1.0F, 0.0F);
      GL11.glRotatef(-leftArmAngles[2] * 57.295776F, 0.0F, 0.0F, 1.0F);
      GL11.glBegin(3);
      GL11.glVertex3i(0, 0, 0);
      GL11.glVertex3f(0.0F, -0.45F, 0.0F);
      GL11.glEnd();
      GL11.glPopMatrix();
      GL11.glPushMatrix();
      leftArmAngles = modelRotations[2];
      GL11.glTranslatef(0.375F, yOff + 0.55F, 0.0F);
      GL11.glRotatef(leftArmAngles[0] * 57.295776F, 1.0F, 0.0F, 0.0F);
      GL11.glRotatef(leftArmAngles[1] * 57.295776F, 0.0F, 1.0F, 0.0F);
      GL11.glRotatef(-leftArmAngles[2] * 57.295776F, 0.0F, 0.0F, 1.0F);
      GL11.glBegin(3);
      GL11.glVertex3i(0, 0, 0);
      GL11.glVertex3f(0.0F, -0.45F, 0.0F);
      GL11.glEnd();
      GL11.glPopMatrix();
      GL11.glRotatef(renderYawOffset - rotationYawHead, 0.0F, 1.0F, 0.0F);
      GL11.glPushMatrix();
      GL11.glTranslatef(0.0F, yOff + 0.55F, 0.0F);
      GL11.glRotatef(modelRotations[0][0] * 57.295776F, 1.0F, 0.0F, 0.0F);
      GL11.glBegin(3);
      GL11.glVertex3i(0, 0, 0);
      GL11.glVertex3f(0.0F, 0.3F, 0.0F);
      GL11.glEnd();
      GL11.glPopMatrix();
      GL11.glPopMatrix();
      GL11.glRotatef(sneaking ? 25.0F : 0.0F, 1.0F, 0.0F, 0.0F);
      GL11.glTranslatef(0.0F, sneaking ? -0.16175F : 0.0F, sneaking ? -0.48025F : 0.0F);
      GL11.glTranslated(0.0, (double)yOff, 0.0);
      GL11.glBegin(3);
      GL11.glVertex3f(-0.125F, 0.0F, 0.0F);
      GL11.glVertex3f(0.125F, 0.0F, 0.0F);
      GL11.glVertex3i(0, 0, 0);
      GL11.glVertex3f(0.0F, 0.55F, 0.0F);
      GL11.glVertex3f(-0.375F, 0.55F, 0.0F);
      GL11.glVertex3f(0.375F, 0.55F, 0.0F);
      GL11.glEnd();
      GL11.glPopMatrix();
   }

   private boolean validateTileEntity(TileEntity entity) {
      return entity instanceof TileEntityChest && !entity.isInvalid() && getSqDistToTileEntity(this.mc.thePlayer, entity) < 4096.0;
   }

   private static double getSqDistToTileEntity(Entity start, TileEntity entity) {
      BlockPos chestBPos = entity.getPos();
      double xDist = (double)chestBPos.getX() - start.posX;
      double yDist = (double)chestBPos.getY() - start.posY;
      double zDist = (double)chestBPos.getZ() - start.posZ;
      return xDist * xDist + yDist * yDist + zDist * zDist;
   }

   private boolean validatePlayer(EntityPlayer player) {
      return player.isEntityAlive() && !player.isInvisible() && (player instanceof EntityOtherPlayerMP || this.mc.gameSettings.getThirdPersonView() > 0);
   }

   public void onEnable() {
      if (this.scaffold == null) {
         this.scaffold = (Scaffold)KetamineClient.getInstance().getModuleManager().getModule(Scaffold.class);
      }

      this.updateScreenBuffer(new ScaledResolution(this.mc));
   }

   private void updateScreenBuffer(ScaledResolution scaledResolution) {
      double scale = 2.0 / (double)scaledResolution.getScaleFactor();
      this.screenBuffer[0] = (double)scaledResolution.getScaledWidth() / scale;
      this.screenBuffer[1] = (double)scaledResolution.getScaledHeight() / scale;
      this.screenBuffer[2] = 2.0;
   }

   public void onDisable() {
      this.drawables.clear();
   }

   private static enum HealthBarColour {
      OFF("Off"),
      HEALTH("Health"),
      COLOUR("Colour"),
      GRADIENT("Gradient");

      private final String name;

      private HealthBarColour(String name) {
         this.name = name;
      }

      public String toString() {
         return this.name;
      }
   }
}
