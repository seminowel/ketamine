package net.minecraft.client.model;

import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;

public class ModelEnderCrystal extends ModelBase {
   private ModelRenderer cube;
   private ModelRenderer glass = new ModelRenderer(this, "glass");
   private ModelRenderer base;

   public ModelEnderCrystal(float p_i1170_1_, boolean p_i1170_2_) {
      this.glass.setTextureOffset(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8, 8, 8);
      this.cube = new ModelRenderer(this, "cube");
      this.cube.setTextureOffset(32, 0).addBox(-4.0F, -4.0F, -4.0F, 8, 8, 8);
      if (p_i1170_2_) {
         this.base = new ModelRenderer(this, "base");
         this.base.setTextureOffset(0, 16).addBox(-6.0F, 0.0F, -6.0F, 12, 4, 12);
      }

   }

   public void render(Entity entityIn, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_, float p_78088_6_, float scale) {
      GL11.glPushMatrix();
      GL11.glScalef(2.0F, 2.0F, 2.0F);
      GL11.glTranslatef(0.0F, -0.5F, 0.0F);
      if (this.base != null) {
         this.base.render(scale);
      }

      GL11.glRotatef(p_78088_3_, 0.0F, 1.0F, 0.0F);
      GL11.glTranslatef(0.0F, 0.8F + p_78088_4_, 0.0F);
      GL11.glRotatef(60.0F, 0.7071F, 0.0F, 0.7071F);
      this.glass.render(scale);
      float f = 0.875F;
      GL11.glScalef(f, f, f);
      GL11.glRotatef(60.0F, 0.7071F, 0.0F, 0.7071F);
      GL11.glRotatef(p_78088_3_, 0.0F, 1.0F, 0.0F);
      this.glass.render(scale);
      GL11.glScalef(f, f, f);
      GL11.glRotatef(60.0F, 0.7071F, 0.0F, 0.7071F);
      GL11.glRotatef(p_78088_3_, 0.0F, 1.0F, 0.0F);
      this.cube.render(scale);
      GL11.glPopMatrix();
   }
}
