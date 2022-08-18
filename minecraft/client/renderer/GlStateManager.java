package net.minecraft.client.renderer;

import java.nio.FloatBuffer;
import org.lwjgl.opengl.GL11;

public class GlStateManager {
   private static AlphaState alphaState = new AlphaState();
   private static BooleanState[] lightState = new BooleanState[8];
   private static FogState fogState = new FogState();
   private static PolygonOffsetState polygonOffsetState = new PolygonOffsetState();
   private static ColorLogicState colorLogicState = new ColorLogicState();
   private static TexGenState texGenState = new TexGenState();
   private static BooleanState normalizeState = new BooleanState(2977);
   private static int activeTextureUnit = 0;
   private static TextureState[] textureState = new TextureState[8];
   private static BooleanState rescaleNormalState = new BooleanState(32826);

   public static void pushAttrib() {
      GL11.glPushAttrib(8256);
   }

   public static void popAttrib() {
      GL11.glPopAttrib();
   }

   public static void disableAlpha() {
      alphaState.field_179208_a.setDisabled();
   }

   public static void enableAlpha() {
      alphaState.field_179208_a.setEnabled();
   }

   public static void enableLight(int light) {
      lightState[light].setEnabled();
   }

   public static void disableLight(int light) {
      lightState[light].setDisabled();
   }

   public static void enableFog() {
      fogState.field_179049_a.setEnabled();
   }

   public static void disableFog() {
      fogState.field_179049_a.setDisabled();
   }

   public static void setFog(int param) {
      if (param != fogState.field_179047_b) {
         fogState.field_179047_b = param;
         GL11.glFogi(2917, param);
      }

   }

   public static void setFogDensity(float param) {
      if (param != fogState.field_179048_c) {
         fogState.field_179048_c = param;
         GL11.glFogf(2914, param);
      }

   }

   public static void setFogStart(float param) {
      if (param != fogState.field_179045_d) {
         fogState.field_179045_d = param;
         GL11.glFogf(2915, param);
      }

   }

   public static void setFogEnd(float param) {
      if (param != fogState.field_179046_e) {
         fogState.field_179046_e = param;
         GL11.glFogf(2916, param);
      }

   }

   public static void enablePolygonOffset() {
      polygonOffsetState.field_179044_a.setEnabled();
   }

   public static void disablePolygonOffset() {
      polygonOffsetState.field_179044_a.setDisabled();
   }

   public static void doPolygonOffset(float factor, float units) {
      if (factor != polygonOffsetState.field_179043_c || units != polygonOffsetState.field_179041_d) {
         polygonOffsetState.field_179043_c = factor;
         polygonOffsetState.field_179041_d = units;
         GL11.glPolygonOffset(factor, units);
      }

   }

   public static void enableColorLogic() {
      colorLogicState.field_179197_a.setEnabled();
   }

   public static void disableColorLogic() {
      colorLogicState.field_179197_a.setDisabled();
   }

   public static void colorLogicOp(int opcode) {
      if (opcode != colorLogicState.field_179196_b) {
         colorLogicState.field_179196_b = opcode;
         GL11.glLogicOp(opcode);
      }

   }

   public static void enableTexGenCoord(TexGen p_179087_0_) {
      texGenCoord(p_179087_0_).field_179067_a.setEnabled();
   }

   public static void disableTexGenCoord(TexGen p_179100_0_) {
      texGenCoord(p_179100_0_).field_179067_a.setDisabled();
   }

   public static void texGen(TexGen p_179149_0_, int p_179149_1_) {
      TexGenCoord glstatemanager$texgencoord = texGenCoord(p_179149_0_);
      if (p_179149_1_ != glstatemanager$texgencoord.field_179066_c) {
         glstatemanager$texgencoord.field_179066_c = p_179149_1_;
         GL11.glTexGeni(glstatemanager$texgencoord.field_179065_b, 9472, p_179149_1_);
      }

   }

   public static void func_179105_a(TexGen p_179105_0_, int pname, FloatBuffer params) {
      GL11.glTexGen(texGenCoord(p_179105_0_).field_179065_b, pname, params);
   }

   private static TexGenCoord texGenCoord(TexGen p_179125_0_) {
      switch (p_179125_0_) {
         case S:
            return texGenState.field_179064_a;
         case T:
            return texGenState.field_179062_b;
         case R:
            return texGenState.field_179063_c;
         case Q:
            return texGenState.field_179061_d;
         default:
            return texGenState.field_179064_a;
      }
   }

   public static void setActiveTexture(int texture) {
      if (activeTextureUnit != texture - OpenGlHelper.defaultTexUnit) {
         activeTextureUnit = texture - OpenGlHelper.defaultTexUnit;
         OpenGlHelper.setActiveTexture(texture);
      }

   }

   public static void deleteTexture(int texture) {
      GL11.glDeleteTextures(texture);
      TextureState[] var1 = textureState;
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         TextureState glstatemanager$texturestate = var1[var3];
         if (glstatemanager$texturestate.textureName == texture) {
            glstatemanager$texturestate.textureName = -1;
         }
      }

   }

   public static void enableNormalize() {
      normalizeState.setEnabled();
   }

   public static void disableNormalize() {
      normalizeState.setDisabled();
   }

   public static void enableRescaleNormal() {
      rescaleNormalState.setEnabled();
   }

   public static void disableRescaleNormal() {
      rescaleNormalState.setDisabled();
   }

   static {
      int j;
      for(j = 0; j < 8; ++j) {
         lightState[j] = new BooleanState(16384 + j);
      }

      for(j = 0; j < 8; ++j) {
         textureState[j] = new TextureState();
      }

   }

   static class TextureState {
      public BooleanState texture2DState;
      public int textureName;

      private TextureState() {
         this.texture2DState = new BooleanState(3553);
         this.textureName = 0;
      }

      // $FF: synthetic method
      TextureState(Object x0) {
         this();
      }
   }

   static class TexGenState {
      public TexGenCoord field_179064_a;
      public TexGenCoord field_179062_b;
      public TexGenCoord field_179063_c;
      public TexGenCoord field_179061_d;

      private TexGenState() {
         this.field_179064_a = new TexGenCoord(8192, 3168);
         this.field_179062_b = new TexGenCoord(8193, 3169);
         this.field_179063_c = new TexGenCoord(8194, 3170);
         this.field_179061_d = new TexGenCoord(8195, 3171);
      }

      // $FF: synthetic method
      TexGenState(Object x0) {
         this();
      }
   }

   static class TexGenCoord {
      public BooleanState field_179067_a;
      public int field_179065_b;
      public int field_179066_c = -1;

      public TexGenCoord(int p_i46254_1_, int p_i46254_2_) {
         this.field_179065_b = p_i46254_1_;
         this.field_179067_a = new BooleanState(p_i46254_2_);
      }
   }

   public static enum TexGen {
      S,
      T,
      R,
      Q;
   }

   static class PolygonOffsetState {
      public BooleanState field_179044_a;
      public BooleanState field_179042_b;
      public float field_179043_c;
      public float field_179041_d;

      private PolygonOffsetState() {
         this.field_179044_a = new BooleanState(32823);
         this.field_179042_b = new BooleanState(10754);
         this.field_179043_c = 0.0F;
         this.field_179041_d = 0.0F;
      }

      // $FF: synthetic method
      PolygonOffsetState(Object x0) {
         this();
      }
   }

   static class FogState {
      public BooleanState field_179049_a;
      public int field_179047_b;
      public float field_179048_c;
      public float field_179045_d;
      public float field_179046_e;

      private FogState() {
         this.field_179049_a = new BooleanState(2912);
         this.field_179047_b = 2048;
         this.field_179048_c = 1.0F;
         this.field_179045_d = 0.0F;
         this.field_179046_e = 1.0F;
      }

      // $FF: synthetic method
      FogState(Object x0) {
         this();
      }
   }

   static class DepthState {
      public BooleanState depthTest = new BooleanState(2929);
      public boolean maskEnabled = true;
      public int depthFunc = 513;

      private DepthState() {
      }
   }

   static class ColorLogicState {
      public BooleanState field_179197_a;
      public int field_179196_b;

      private ColorLogicState() {
         this.field_179197_a = new BooleanState(3058);
         this.field_179196_b = 5379;
      }

      // $FF: synthetic method
      ColorLogicState(Object x0) {
         this();
      }
   }

   static class BooleanState {
      private final int capability;
      private boolean currentState = false;

      public BooleanState(int capabilityIn) {
         this.capability = capabilityIn;
      }

      public void setDisabled() {
         this.setState(false);
      }

      public void setEnabled() {
         this.setState(true);
      }

      public void setState(boolean state) {
         if (state != this.currentState) {
            this.currentState = state;
            if (state) {
               GL11.glEnable(this.capability);
            } else {
               GL11.glDisable(this.capability);
            }
         }

      }
   }

   static class BlendState {
      public BooleanState field_179213_a = new BooleanState(3042);
      public int srcFactor = 1;
      public int dstFactor = 0;
      public int srcFactorAlpha = 1;
      public int dstFactorAlpha = 0;

      private BlendState() {
      }
   }

   static class AlphaState {
      public BooleanState field_179208_a;
      public int func;
      public float ref;

      private AlphaState() {
         this.field_179208_a = new BooleanState(3008);
         this.func = 519;
         this.ref = -1.0F;
      }

      // $FF: synthetic method
      AlphaState(Object x0) {
         this();
      }
   }
}
