package net.minecraft.client.renderer.entity;

import java.util.Random;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class RenderLightningBolt extends Render {
   public RenderLightningBolt(RenderManager renderManagerIn) {
      super(renderManagerIn);
   }

   public void doRender(EntityLightningBolt entity, double x, double y, double z, float entityYaw, float partialTicks) {
      Tessellator tessellator = Tessellator.getInstance();
      WorldRenderer worldrenderer = tessellator.getWorldRenderer();
      GL11.glDisable(3553);
      GL11.glDisable(2896);
      GL11.glEnable(3042);
      GL11.glBlendFunc(770, 1);
      double[] adouble = new double[8];
      double[] adouble1 = new double[8];
      double d0 = 0.0;
      double d1 = 0.0;
      Random random = new Random(entity.boltVertex);

      int k1;
      for(k1 = 7; k1 >= 0; --k1) {
         adouble[k1] = d0;
         adouble1[k1] = d1;
         d0 += (double)(random.nextInt(11) - 5);
         d1 += (double)(random.nextInt(11) - 5);
      }

      for(k1 = 0; k1 < 4; ++k1) {
         Random random1 = new Random(entity.boltVertex);

         for(int j = 0; j < 3; ++j) {
            int k = 7;
            int l = 0;
            if (j > 0) {
               k = 7 - j;
            }

            if (j > 0) {
               l = k - 2;
            }

            double d2 = adouble[k] - d0;
            double d3 = adouble1[k] - d1;

            for(int i1 = k; i1 >= l; --i1) {
               double d4 = d2;
               double d5 = d3;
               if (j == 0) {
                  d2 += (double)(random1.nextInt(11) - 5);
                  d3 += (double)(random1.nextInt(11) - 5);
               } else {
                  d2 += (double)(random1.nextInt(31) - 15);
                  d3 += (double)(random1.nextInt(31) - 15);
               }

               worldrenderer.begin(5, DefaultVertexFormats.POSITION_COLOR);
               float f = 0.5F;
               float f1 = 0.45F;
               float f2 = 0.45F;
               float f3 = 0.5F;
               double d6 = 0.1 + (double)k1 * 0.2;
               if (j == 0) {
                  d6 *= (double)i1 * 0.1 + 1.0;
               }

               double d7 = 0.1 + (double)k1 * 0.2;
               if (j == 0) {
                  d7 *= (double)(i1 - 1) * 0.1 + 1.0;
               }

               for(int j1 = 0; j1 < 5; ++j1) {
                  double d8 = x + 0.5 - d6;
                  double d9 = z + 0.5 - d6;
                  if (j1 == 1 || j1 == 2) {
                     d8 += d6 * 2.0;
                  }

                  if (j1 == 2 || j1 == 3) {
                     d9 += d6 * 2.0;
                  }

                  double d10 = x + 0.5 - d7;
                  double d11 = z + 0.5 - d7;
                  if (j1 == 1 || j1 == 2) {
                     d10 += d7 * 2.0;
                  }

                  if (j1 == 2 || j1 == 3) {
                     d11 += d7 * 2.0;
                  }

                  worldrenderer.pos(d10 + d2, y + (double)(i1 * 16), d11 + d3).color(0.45F, 0.45F, 0.5F, 0.3F).endVertex();
                  worldrenderer.pos(d8 + d4, y + (double)((i1 + 1) * 16), d9 + d5).color(0.45F, 0.45F, 0.5F, 0.3F).endVertex();
               }

               tessellator.draw();
            }
         }
      }

      GL11.glDisable(3042);
      GL11.glEnable(2896);
      GL11.glEnable(3553);
   }

   protected ResourceLocation getEntityTexture(EntityLightningBolt entity) {
      return null;
   }
}
