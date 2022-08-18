package net.minecraft.client.renderer;

import java.nio.FloatBuffer;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

public class RenderHelper {
   private static FloatBuffer colorBuffer = GLAllocation.createDirectFloatBuffer(16);
   private static final Vec3 LIGHT0_POS = (new Vec3(0.20000000298023224, 1.0, -0.699999988079071)).normalize();
   private static final Vec3 LIGHT1_POS = (new Vec3(-0.20000000298023224, 1.0, 0.699999988079071)).normalize();

   public static void disableStandardItemLighting() {
      GL11.glDisable(2896);
      GlStateManager.disableLight(0);
      GlStateManager.disableLight(1);
      GL11.glDisable(2903);
   }

   public static void enableStandardItemLighting() {
      GL11.glEnable(2896);
      GlStateManager.enableLight(0);
      GlStateManager.enableLight(1);
      GL11.glEnable(2903);
      GL11.glColorMaterial(1032, 5634);
      float f = 0.4F;
      float f1 = 0.6F;
      float f2 = 0.0F;
      GL11.glLight(16384, 4611, setColorBuffer(LIGHT0_POS.xCoord, LIGHT0_POS.yCoord, LIGHT0_POS.zCoord, 0.0));
      GL11.glLight(16384, 4609, setColorBuffer(f1, f1, f1, 1.0F));
      GL11.glLight(16384, 4608, setColorBuffer(0.0F, 0.0F, 0.0F, 1.0F));
      GL11.glLight(16384, 4610, setColorBuffer(f2, f2, f2, 1.0F));
      GL11.glLight(16385, 4611, setColorBuffer(LIGHT1_POS.xCoord, LIGHT1_POS.yCoord, LIGHT1_POS.zCoord, 0.0));
      GL11.glLight(16385, 4609, setColorBuffer(f1, f1, f1, 1.0F));
      GL11.glLight(16385, 4608, setColorBuffer(0.0F, 0.0F, 0.0F, 1.0F));
      GL11.glLight(16385, 4610, setColorBuffer(f2, f2, f2, 1.0F));
      GL11.glShadeModel(7424);
      GL11.glLightModel(2899, setColorBuffer(f, f, f, 1.0F));
   }

   private static FloatBuffer setColorBuffer(double p_74517_0_, double p_74517_2_, double p_74517_4_, double p_74517_6_) {
      return setColorBuffer((float)p_74517_0_, (float)p_74517_2_, (float)p_74517_4_, (float)p_74517_6_);
   }

   private static FloatBuffer setColorBuffer(float p_74521_0_, float p_74521_1_, float p_74521_2_, float p_74521_3_) {
      colorBuffer.clear();
      colorBuffer.put(p_74521_0_).put(p_74521_1_).put(p_74521_2_).put(p_74521_3_);
      colorBuffer.flip();
      return colorBuffer;
   }

   public static void enableGUIStandardItemLighting() {
      GL11.glPushMatrix();
      GL11.glRotatef(-30.0F, 0.0F, 1.0F, 0.0F);
      GL11.glRotatef(165.0F, 1.0F, 0.0F, 0.0F);
      enableStandardItemLighting();
      GL11.glPopMatrix();
   }
}
