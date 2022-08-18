package net.minecraft.client.renderer;

import java.nio.ByteBuffer;
import java.util.List;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import org.lwjgl.opengl.GL11;

public class WorldVertexBufferUploader {
   public void func_181679_a(WorldRenderer p_181679_1_) {
      if (p_181679_1_.getVertexCount() > 0) {
         VertexFormat vertexformat = p_181679_1_.getVertexFormat();
         int i = vertexformat.getNextOffset();
         ByteBuffer bytebuffer = p_181679_1_.getByteBuffer();
         List list = vertexformat.getElements();

         int i1;
         int k1;
         for(i1 = 0; i1 < list.size(); ++i1) {
            VertexFormatElement vertexformatelement = (VertexFormatElement)list.get(i1);
            VertexFormatElement.EnumUsage vertexformatelement$enumusage = vertexformatelement.getUsage();
            int k = vertexformatelement.getType().getGlConstant();
            k1 = vertexformatelement.getIndex();
            bytebuffer.position(vertexformat.func_181720_d(i1));
            switch (vertexformatelement$enumusage) {
               case POSITION:
                  GL11.glVertexPointer(vertexformatelement.getElementCount(), k, i, bytebuffer);
                  GL11.glEnableClientState(32884);
                  break;
               case UV:
                  OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit + k1);
                  GL11.glTexCoordPointer(vertexformatelement.getElementCount(), k, i, bytebuffer);
                  GL11.glEnableClientState(32888);
                  OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
                  break;
               case COLOR:
                  GL11.glColorPointer(vertexformatelement.getElementCount(), k, i, bytebuffer);
                  GL11.glEnableClientState(32886);
                  break;
               case NORMAL:
                  GL11.glNormalPointer(k, i, bytebuffer);
                  GL11.glEnableClientState(32885);
            }
         }

         GL11.glDrawArrays(p_181679_1_.getDrawMode(), 0, p_181679_1_.getVertexCount());
         i1 = 0;

         for(int j1 = list.size(); i1 < j1; ++i1) {
            VertexFormatElement vertexformatelement1 = (VertexFormatElement)list.get(i1);
            VertexFormatElement.EnumUsage vertexformatelement$enumusage1 = vertexformatelement1.getUsage();
            k1 = vertexformatelement1.getIndex();
            switch (vertexformatelement$enumusage1) {
               case POSITION:
                  GL11.glDisableClientState(32884);
                  break;
               case UV:
                  OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit + k1);
                  GL11.glDisableClientState(32888);
                  OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
                  break;
               case COLOR:
                  GL11.glDisableClientState(32886);
                  break;
               case NORMAL:
                  GL11.glDisableClientState(32885);
            }
         }
      }

      p_181679_1_.reset();
   }
}
