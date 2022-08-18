package io.github.nevalackin.client.impl.module.misc.world;

import com.google.common.collect.Lists;
import io.github.nevalackin.client.api.module.Category;
import io.github.nevalackin.client.api.module.Module;
import io.github.nevalackin.client.api.property.Property;
import io.github.nevalackin.client.api.ui.cfont.CustomFontRenderer;
import io.github.nevalackin.client.impl.core.KetamineClient;
import io.github.nevalackin.client.impl.event.player.UpdatePositionEvent;
import io.github.nevalackin.client.impl.property.BooleanProperty;
import io.github.nevalackin.client.impl.property.DoubleProperty;
import io.github.nevalackin.client.impl.property.EnumProperty;
import io.github.nevalackin.client.impl.property.MultiSelectionEnumProperty;
import io.github.nevalackin.client.util.movement.JumpUtil;
import io.github.nevalackin.client.util.movement.MovementUtil;
import io.github.nevalackin.client.util.player.InventoryUtil;
import io.github.nevalackin.client.util.player.RotationUtil;
import io.github.nevalackin.client.util.player.WindowClickRequest;
import io.github.nevalackin.client.util.render.BlurUtil;
import io.github.nevalackin.client.util.render.ColourUtil;
import io.github.nevalackin.client.util.render.DrawUtil;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vec3i;
import org.apache.commons.lang3.RandomUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public final class Scaffold extends Module {
   private static final EnumFacing[] FACINGS;
   private final BooleanProperty towerProperty = new BooleanProperty("Tower", true);
   private final MultiSelectionEnumProperty drawOptionsProperty;
   private final BooleanProperty spoofHeldItemProperty;
   private final DoubleProperty placeDelayProperty;
   private final EnumProperty swingProperty;
   private final BooleanProperty rayTraceCheckProperty;
   private final BooleanProperty safeWalkProperty;
   private final BooleanProperty keepPosProperty;
   private final BooleanProperty noSprintProperty;
   private final List breadcrumbs;
   private double fadeInOutProgress;
   private int totalBlockCount;
   private int ticksSinceWindowClick;
   private int ticksSincePlace;
   private BlockData data;
   private BlockData lastPlacement;
   public float[] angles;
   public static boolean towering;
   public int placedBlocks;
   private int bestBlockStack;
   private double startPosY;
   private WindowClickRequest lastRequest;
   @EventLink
   private final Listener onGetCurrentItem;
   @EventLink
   private final Listener onSafeWalkEvent;
   @EventLink
   private final Listener onWindowClick;
   @EventLink
   private final Listener onBlockPlace;
   @EventLink(0)
   private final Listener onSprintEvent;
   @EventLink
   private final Listener onUpdatePosition;
   @EventLink
   private final Listener onRenderGameOverlay;
   @EventLink
   private final Listener onRender3D;

   public Scaffold() {
      super("Scaffold", Category.MISC, Category.SubCategory.MISC_WORLD);
      this.drawOptionsProperty = new MultiSelectionEnumProperty("Draw Options", Lists.newArrayList(new DrawOption[]{Scaffold.DrawOption.PLACEMENT}), Scaffold.DrawOption.values());
      this.spoofHeldItemProperty = new BooleanProperty("Spoof Held Item", true);
      this.placeDelayProperty = new DoubleProperty("Place Delay", 0.0, 0.0, 10.0, 1.0);
      this.swingProperty = new EnumProperty("Swing", Scaffold.Swing.SILENT);
      this.rayTraceCheckProperty = new BooleanProperty("Ray Trace Check", false);
      this.safeWalkProperty = new BooleanProperty("Safe Walk", false);
      this.keepPosProperty = new BooleanProperty("No Y Gain", true);
      this.noSprintProperty = new BooleanProperty("No Sprint", true);
      this.breadcrumbs = new ArrayList();
      this.onGetCurrentItem = (event) -> {
         if ((Boolean)this.spoofHeldItemProperty.getValue() && this.bestBlockStack != -1 && this.bestBlockStack >= 36) {
            event.setCurrentItem(this.bestBlockStack - 36);
         }

      };
      this.onSafeWalkEvent = (event) -> {
         if ((Boolean)this.safeWalkProperty.getValue()) {
            event.setCancelled();
         }

      };
      this.onWindowClick = (event) -> {
         this.ticksSinceWindowClick = 0;
      };
      this.onBlockPlace = (event) -> {
         this.ticksSincePlace = 0;
      };
      this.onSprintEvent = (event) -> {
         if (event.isSprintState() && (Boolean)this.noSprintProperty.getValue()) {
            event.setSprintState(false);
            this.mc.thePlayer.setSprinting(false);
         }

      };
      this.onUpdatePosition = (event) -> {
         if (event.isPre()) {
            ++this.ticksSinceWindowClick;
            ++this.ticksSincePlace;
            this.data = null;
            towering = (Boolean)this.towerProperty.getValue() && this.mc.gameSettings.keyBindJump.isKeyDown();
            this.bestBlockStack = InventoryUtil.getBestBlockStack(this.mc, 36, 45);
            this.calculateTotalBlockCount();
            this.moveBlocksIntoHotBar();
            if (this.bestBlockStack >= 36) {
               BlockPos blockUnder = this.getBlockUnder();
               this.data = this.getBlockData(blockUnder);
               if (this.data == null) {
                  this.data = this.getBlockData(blockUnder.offset(EnumFacing.DOWN));
               }

               if (this.data != null) {
                  if (this.validateReplaceable(this.data) && this.data.hitVec != null) {
                     if (towering) {
                        if (this.mc.thePlayer.onGround || this.hasPlacedLastPlacement()) {
                           this.mc.thePlayer.motionY = JumpUtil.getJumpHeight(this.mc.thePlayer);
                        }

                        this.doPlace(event);
                     }

                     this.angles = RotationUtil.getRotations(new float[]{event.getLastTickYaw(), event.getLastTickPitch()}, 12.0F, RotationUtil.getHitOrigin(this.mc.thePlayer), this.data.hitVec);
                  } else {
                     this.data = null;
                  }
               }

               if ((Boolean)this.noSprintProperty.getValue() && this.mc.thePlayer.onGround) {
                  PotionEffect speed = this.mc.thePlayer.getActivePotionEffect(Potion.moveSpeed);
                  int moveSpeedAmp = speed == null ? 0 : speed.getAmplifier() + 1;
                  if (moveSpeedAmp > 0) {
                     double walkSpeed = 1.0 + 0.2 * (double)moveSpeedAmp + 0.1;
                     EntityPlayerSP var10000 = this.mc.thePlayer;
                     var10000.motionX /= walkSpeed;
                     var10000 = this.mc.thePlayer;
                     var10000.motionZ /= walkSpeed;
                  }
               }

               if (this.angles == null || this.lastPlacement == null) {
                  float[] lastAngles = this.angles != null ? this.angles : new float[]{event.getYaw(), event.getPitch()};
                  float moveDir = MovementUtil.getMovementDirection(this.mc.thePlayer.moveForward, this.mc.thePlayer.moveStrafing, this.mc.thePlayer.rotationYaw);
                  float[] dstRotations = new float[]{moveDir + 180.0F, towering ? 90.0F : 70.0F};
                  RotationUtil.applySmoothing(lastAngles, 12.0F, dstRotations);
                  RotationUtil.applyGCD(dstRotations, lastAngles);
                  this.angles = dstRotations;
               }

               event.setYaw(this.angles[0]);
               event.setPitch(towering ? 81.0F : 70.0F);
            }
         } else {
            this.doPlace(event);
         }

      };
      this.onRenderGameOverlay = (event) -> {
         ScaledResolution sr = event.getScaledResolution();
         float mx = (float)sr.getScaledWidth() / 2.0F;
         float my = (float)sr.getScaledHeight() / 2.0F;
         EntityPlayerSP player = this.mc.thePlayer;
         double minFadeInProgress = 0.7;
         if (this.bestBlockStack != -1) {
            ItemStack stack = player.inventoryContainer.getSlot(this.bestBlockStack).getStack();
            if (stack != null) {
               if (this.fadeInOutProgress < 1.0) {
                  this.fadeInOutProgress += 1.0 / (double)Minecraft.getDebugFPS() * 2.0;
               }

               double width = 60.0;
               double height = 20.0;
               double left = (double)mx - 30.0;
               double top = (double)(my + 20.0F + 10.0F);
               BlurUtil.blurArea(left, top, 60.0, 20.0);
               DrawUtil.glDrawFilledQuad(left, top, 60.0, 20.0, Integer.MIN_VALUE);
               DrawUtil.glDrawGradientLine(left, top, (double)mx + 30.0, top, 1.0F, ColourUtil.getClientColour());
               String blockCount = String.format("%s blocks", this.totalBlockCount);
               CustomFontRenderer fontRenderer = KetamineClient.getInstance().getFontRenderer();
               int itemStackSize = true;
               int textWidth = 18 + (int)Math.ceil(fontRenderer.getWidth(blockCount));
               int iconRenderPosX = (int)(left + 30.0 - (double)(textWidth / 2));
               int iconRenderPosY = (int)(top + 2.0);
               boolean restore = DrawUtil.glEnableBlend();
               GlStateManager.enableRescaleNormal();
               RenderHelper.enableGUIStandardItemLighting();
               this.mc.getRenderItem().renderItemAndEffectIntoGUI(stack, iconRenderPosX, iconRenderPosY);
               RenderHelper.disableStandardItemLighting();
               GlStateManager.disableRescaleNormal();
               GL11.glEnable(3008);
               DrawUtil.glRestoreBlend(restore);
               fontRenderer.draw(blockCount, (double)(iconRenderPosX + 16 + 2), top + 10.0 - fontRenderer.getHeight(blockCount) / 2.0, -1);
            }
         }

         float partialTicks = event.getPartialTicks();
         float client = DrawUtil.interpolate(player.prevRotationYaw, player.rotationYaw, partialTicks);
         float server = DrawUtil.interpolate(player.getLastTickYaw(), player.getYaw(), partialTicks);
         float rotation = server - client;
         GL11.glDisable(3553);
         boolean restorex = DrawUtil.glEnableBlend();
         GL11.glEnable(2881);
         GL11.glEnable(2848);
         GL11.glLineWidth(1.0F);
         GL11.glHint(3154, 4354);
         GL11.glHint(3155, 4354);
         GL11.glPushMatrix();
         GL11.glTranslatef(mx, my, 0.0F);
         GL11.glRotatef(rotation, 0.0F, 0.0F, 1.0F);
         GL11.glTranslatef(0.0F, -20.0F, 0.0F);
         DrawUtil.glColour(-1);
         GL11.glBegin(2);
         addTriangleVertices(10.0);
         GL11.glEnd();
         GL11.glEnable(2881);
         GL11.glHint(3155, 4354);
         DrawUtil.glColour(-2130706433);
         GL11.glBegin(4);
         addTriangleVertices(10.0);
         GL11.glEnd();
         GL11.glPopMatrix();
         GL11.glDisable(2881);
         GL11.glDisable(2848);
         GL11.glHint(3154, 4352);
         GL11.glHint(3155, 4352);
         DrawUtil.glRestoreBlend(restorex);
         GL11.glEnable(3553);
      };
      this.onRender3D = (event) -> {
         GL11.glDisable(3553);
         GL11.glEnable(3042);
         GL11.glDisable(3008);
         GL11.glShadeModel(7425);
         GL11.glDisable(2884);
         float partialTicks = event.getPartialTicks();
         double x = DrawUtil.interpolate(this.mc.thePlayer.prevPosX, this.mc.thePlayer.posX, (double)partialTicks);
         double y = DrawUtil.interpolate(this.mc.thePlayer.prevPosY, this.mc.thePlayer.posY, (double)partialTicks);
         double z = DrawUtil.interpolate(this.mc.thePlayer.prevPosZ, this.mc.thePlayer.posZ, (double)partialTicks);
         int colour = ColourUtil.getClientColour();
         int clear = ColourUtil.removeAlphaComponent(colour);
         GL11.glTranslated(x, y + 0.01, z);
         GL11.glBegin(6);
         DrawUtil.glColour(colour);
         GL11.glVertex3f(0.0F, 0.0F, 0.0F);
         float radius = 0.6F;
         int points = true;
         double pix2 = 6.283185307179586;
         if (this.drawOptionsProperty.isSelected(Scaffold.DrawOption.GLOW)) {
            DrawUtil.glColour(clear);

            for(int i = 0; i <= 40; ++i) {
               float px = 0.6F * (float)Math.cos((double)i * 6.283185307179586 / 40.0);
               float pz = 0.6F * (float)Math.sin((double)i * 6.283185307179586 / 40.0);
               GL11.glVertex3f(px, 0.0F, pz);
            }
         }

         GL11.glEnd();
         GL11.glTranslated(-x, -y, -z);
         GL11.glEnable(2884);
         GL11.glShadeModel(7424);
         GL11.glDisable(2929);
         GL11.glDepthMask(false);
         if (this.drawOptionsProperty.hasSelections()) {
            GL11.glEnable(2848);
            GL11.glHint(3154, 4354);
            GL11.glLineWidth(1.0F);
            if (this.drawOptionsProperty.isSelected(Scaffold.DrawOption.PLACEMENT) && this.lastPlacement != null) {
               BlockPos placePos = this.lastPlacement.pos.offset(this.lastPlacement.face);
               DrawUtil.glColour(ColourUtil.overwriteAlphaComponent(colour, 80));
               DrawUtil.glDrawBoundingBox(this.mc.theWorld.getBlockState(placePos).getBlock().getSelectedBoundingBox(this.mc.theWorld, placePos), 1.0F, true);
            }

            if (this.drawOptionsProperty.isSelected(Scaffold.DrawOption.BREADCRUMBS)) {
               GL11.glPointSize(5.0F);
               GL11.glEnable(2832);
               GL11.glHint(3153, 4354);
               DrawUtil.glColour(ColourUtil.getSecondaryColour());
               Vec3 lastCrumb = null;
               GL11.glBegin(3);

               Iterator var20;
               Vec3 breadcrumb;
               for(var20 = this.breadcrumbs.iterator(); var20.hasNext(); lastCrumb = breadcrumb) {
                  breadcrumb = (Vec3)var20.next();
                  if (lastCrumb != null && lastCrumb.distanceTo(breadcrumb) > Math.sqrt(3.0)) {
                     GL11.glEnd();
                     GL11.glBegin(3);
                  }

                  GL11.glVertex3d(breadcrumb.xCoord, breadcrumb.yCoord, breadcrumb.zCoord);
               }

               GL11.glEnd();
               DrawUtil.glColour(colour);
               GL11.glBegin(0);
               var20 = this.breadcrumbs.iterator();

               while(var20.hasNext()) {
                  breadcrumb = (Vec3)var20.next();
                  GL11.glVertex3d(breadcrumb.xCoord, breadcrumb.yCoord, breadcrumb.zCoord);
               }

               GL11.glEnd();
               GL11.glDisable(2832);
               GL11.glHint(3153, 4352);
            }

            GL11.glDisable(2848);
            GL11.glHint(3154, 4352);
         }

         GL11.glEnable(3008);
         GL11.glEnable(3553);
         GL11.glEnable(2929);
         GL11.glDepthMask(true);
         GL11.glDisable(3042);
      };
      this.placeDelayProperty.addValueAlias(0.0, "None");
      this.register(new Property[]{this.spoofHeldItemProperty, this.placeDelayProperty, this.swingProperty, this.rayTraceCheckProperty, this.towerProperty, this.safeWalkProperty, this.noSprintProperty, this.keepPosProperty, this.drawOptionsProperty});
   }

   private boolean hasPlacedLastPlacement() {
      return this.lastPlacement != null || !this.validateReplaceable(this.data);
   }

   private void doPlace(UpdatePositionEvent event) {
      if (this.bestBlockStack >= 36 && this.data != null && !((double)this.ticksSincePlace <= (Double)this.placeDelayProperty.getValue())) {
         Vec3 hitVec;
         if ((Boolean)this.rayTraceCheckProperty.getValue()) {
            MovingObjectPosition rayTraceResult = RotationUtil.rayTraceBlocks(this.mc, event.isPre() ? event.getLastTickYaw() : event.getYaw(), event.isPre() ? event.getLastTickPitch() : event.getPitch());
            if (rayTraceResult == null) {
               return;
            }

            if (rayTraceResult.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) {
               return;
            }

            if (rayTraceResult.sideHit != this.data.face) {
               return;
            }

            BlockPos dstPos = this.data.pos;
            BlockPos rayDstPos = rayTraceResult.getBlockPos();
            if (rayDstPos.getX() != dstPos.getX() || rayDstPos.getY() != dstPos.getY() || rayDstPos.getZ() != dstPos.getZ()) {
               return;
            }

            hitVec = rayTraceResult.hitVec;
         } else {
            hitVec = this.data.hitVec;
         }

         ItemStack heldItem;
         if ((Boolean)this.spoofHeldItemProperty.getValue()) {
            heldItem = this.mc.thePlayer.inventoryContainer.getSlot(this.bestBlockStack).getStack();
         } else {
            this.mc.thePlayer.inventory.currentItem = this.bestBlockStack - 36;
            heldItem = this.mc.thePlayer.getCurrentEquippedItem();
         }

         if (heldItem != null) {
            if (this.mc.playerController.onPlayerRightClick(this.mc.thePlayer, this.mc.theWorld, heldItem, this.data.pos, this.data.face, hitVec)) {
               this.lastPlacement = this.data;
               ++this.placedBlocks;
               if (this.drawOptionsProperty.isSelected(Scaffold.DrawOption.BREADCRUMBS)) {
                  this.breadcrumbs.add(hitVec);
               }

               switch ((Swing)this.swingProperty.getValue()) {
                  case CLIENT:
                     this.mc.thePlayer.swingItem();
                     break;
                  case SILENT:
                     this.mc.thePlayer.sendQueue.sendPacket(new C0APacketAnimation());
               }
            }

         }
      }
   }

   private static void addTriangleVertices(double size) {
      GL11.glVertex2d(0.0, -size / 2.0);
      GL11.glVertex2d(-size / 2.0, size / 2.0);
      GL11.glVertex2d(size / 2.0, size / 2.0);
   }

   public void onEnable() {
      this.lastPlacement = null;
      towering = false;
      this.placedBlocks = 0;
      if (this.mc.thePlayer != null) {
         this.startPosY = this.mc.thePlayer.posY;
      }

   }

   public void onDisable() {
      this.angles = null;
      this.breadcrumbs.clear();
   }

   private BlockData getBlockData(BlockPos pos) {
      EnumFacing[] facings = FACINGS;
      EnumFacing[] var3 = facings;
      int var4 = facings.length;

      int var5;
      for(var5 = 0; var5 < var4; ++var5) {
         EnumFacing facing = var3[var5];
         BlockPos blockPos = pos.add(facing.getOpposite().getDirectionVec());
         if (InventoryUtil.validateBlock(this.mc.theWorld.getBlockState(blockPos).getBlock(), InventoryUtil.BlockAction.PLACE_ON)) {
            BlockData data = new BlockData(blockPos, facing);
            if (this.validateBlockRange(data)) {
               return data;
            }
         }
      }

      BlockPos posBelow = pos.add(0, -1, 0);
      if (InventoryUtil.validateBlock(this.mc.theWorld.getBlockState(posBelow).getBlock(), InventoryUtil.BlockAction.PLACE_ON)) {
         BlockData data = new BlockData(posBelow, EnumFacing.UP);
         if (this.validateBlockRange(data)) {
            return data;
         }
      }

      EnumFacing[] var17 = facings;
      var5 = facings.length;

      for(int var18 = 0; var18 < var5; ++var18) {
         EnumFacing facing = var17[var18];
         BlockPos blockPos = pos.add(facing.getOpposite().getDirectionVec());
         EnumFacing[] var9 = facings;
         int var10 = facings.length;

         for(int var11 = 0; var11 < var10; ++var11) {
            EnumFacing facing1 = var9[var11];
            BlockPos blockPos1 = blockPos.add(facing1.getOpposite().getDirectionVec());
            if (InventoryUtil.validateBlock(this.mc.theWorld.getBlockState(blockPos1).getBlock(), InventoryUtil.BlockAction.PLACE_ON)) {
               BlockData data = new BlockData(blockPos1, facing1);
               if (this.validateBlockRange(data)) {
                  return data;
               }
            }
         }
      }

      return null;
   }

   private boolean validateBlockRange(BlockData data) {
      Vec3 pos = data.hitVec;
      if (pos == null) {
         return false;
      } else {
         EntityPlayerSP player = this.mc.thePlayer;
         double x = pos.xCoord - player.posX;
         double y = pos.yCoord - (player.posY + (double)player.getEyeHeight());
         double z = pos.zCoord - player.posZ;
         float reach = this.mc.playerController.getBlockReachDistance();
         return Math.sqrt(x * x + y * y + z * z) <= (double)reach;
      }
   }

   private boolean validateReplaceable(BlockData data) {
      BlockPos pos = data.pos.offset(data.face);
      return this.mc.theWorld.getBlockState(pos).getBlock().isReplaceable(this.mc.theWorld, pos);
   }

   private BlockPos getBlockUnder() {
      if ((Boolean)this.keepPosProperty.getValue() && !Keyboard.isKeyDown(57)) {
         return new BlockPos(this.mc.thePlayer.posX, Math.min(this.startPosY, this.mc.thePlayer.posY) - 1.0, this.mc.thePlayer.posZ);
      } else {
         this.startPosY = this.mc.thePlayer.posY;
         return new BlockPos(this.mc.thePlayer.posX, this.mc.thePlayer.posY - 1.0, this.mc.thePlayer.posZ);
      }
   }

   private void moveBlocksIntoHotBar() {
      if (this.ticksSinceWindowClick > 3) {
         final int bestStackInInv = InventoryUtil.getBestBlockStack(this.mc, 9, 36);
         if (bestStackInInv == -1) {
            return;
         }

         boolean foundEmptySlot = false;

         for(final int i = 44; i >= 36; --i) {
            ItemStack stack = this.mc.thePlayer.inventoryContainer.getSlot(i).getStack();
            if (stack == null) {
               if (this.lastRequest == null || this.lastRequest.isCompleted()) {
                  InventoryUtil.queueClickRequest(this.lastRequest = new WindowClickRequest() {
                     public void performRequest() {
                        InventoryUtil.windowClick(Scaffold.this.mc, bestStackInInv, i - 36, InventoryUtil.ClickType.SWAP_WITH_HOT_BAR_SLOT);
                     }
                  });
               }

               foundEmptySlot = true;
            }
         }

         if (!foundEmptySlot && (this.lastRequest == null || this.lastRequest.isCompleted())) {
            InventoryUtil.queueClickRequest(this.lastRequest = new WindowClickRequest() {
               public void performRequest() {
                  int overrideSlot = true;
                  InventoryUtil.windowClick(Scaffold.this.mc, bestStackInInv, 9, InventoryUtil.ClickType.SWAP_WITH_HOT_BAR_SLOT);
               }
            });
         }
      }

   }

   private void calculateTotalBlockCount() {
      this.totalBlockCount = 0;

      for(int i = 9; i < 45; ++i) {
         ItemStack stack = this.mc.thePlayer.inventoryContainer.getSlot(i).getStack();
         if (stack != null && stack.stackSize >= 1 && stack.getItem() instanceof ItemBlock && InventoryUtil.isStackValidToPlace(stack)) {
            this.totalBlockCount += stack.stackSize;
         }
      }

   }

   static {
      FACINGS = new EnumFacing[]{EnumFacing.EAST, EnumFacing.WEST, EnumFacing.SOUTH, EnumFacing.NORTH};
   }

   private static enum DrawOption {
      BREADCRUMBS("Breadcrumbs"),
      PLACEMENT("Draw Placement"),
      GLOW("Glow");

      private final String name;

      private DrawOption(String name) {
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

   private static class BlockData {
      private final BlockPos pos;
      private final EnumFacing face;
      private final Vec3 hitVec;

      public BlockData(BlockPos pos, EnumFacing face) {
         this.pos = pos;
         this.face = face;
         this.hitVec = this.calculateBlockData();
      }

      private Vec3 calculateBlockData() {
         Vec3i directionVec = this.face.getDirectionVec();
         Minecraft mc = Minecraft.getMinecraft();
         double x;
         double z;
         switch (this.face.getAxis()) {
            case Z:
               double absX = Math.abs(mc.thePlayer.posX);
               double xOffset = absX - (double)((int)absX);
               if (mc.thePlayer.posX < 0.0) {
                  xOffset = 1.0 - xOffset;
               }

               x = (double)directionVec.getX() * xOffset;
               z = (double)directionVec.getZ() * xOffset;
               break;
            case X:
               double absZ = Math.abs(mc.thePlayer.posZ);
               double zOffset = absZ - (double)((int)absZ);
               if (mc.thePlayer.posZ < 0.0) {
                  zOffset = 1.0 - zOffset;
               }

               x = (double)directionVec.getX() * zOffset;
               z = (double)directionVec.getZ() * zOffset;
               break;
            default:
               x = 0.25;
               z = 0.25;
         }

         if (this.face.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE) {
            x = -x;
            z = -z;
         }

         Vec3 hitVec = (new Vec3(this.pos)).addVector(x + z, (double)directionVec.getY() * 0.5, x + z);
         Vec3 src = mc.thePlayer.getPositionEyes(1.0F);
         MovingObjectPosition obj = mc.theWorld.rayTraceBlocks(src, hitVec, false, false, true);
         if (obj != null && obj.hitVec != null && obj.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            switch (this.face.getAxis()) {
               case Z:
                  obj.hitVec = new Vec3(obj.hitVec.xCoord, obj.hitVec.yCoord, (double)Math.round(obj.hitVec.zCoord));
                  break;
               case X:
                  obj.hitVec = new Vec3((double)Math.round(obj.hitVec.xCoord), obj.hitVec.yCoord, obj.hitVec.zCoord);
            }

            if (this.face != EnumFacing.DOWN && this.face != EnumFacing.UP) {
               IBlockState blockState = mc.theWorld.getBlockState(obj.getBlockPos());
               Block blockAtPos = blockState.getBlock();
               double blockFaceOffset = RandomUtils.nextDouble(0.1, 0.3);
               if (blockAtPos instanceof BlockSlab && !((BlockSlab)blockAtPos).isDouble()) {
                  BlockSlab.EnumBlockHalf half = (BlockSlab.EnumBlockHalf)blockState.getValue(BlockSlab.HALF);
                  if (half != BlockSlab.EnumBlockHalf.TOP) {
                     blockFaceOffset += 0.5;
                  }
               }

               obj.hitVec = obj.hitVec.addVector(0.0, -blockFaceOffset, 0.0);
            }

            return obj.hitVec;
         } else {
            return null;
         }
      }
   }
}
