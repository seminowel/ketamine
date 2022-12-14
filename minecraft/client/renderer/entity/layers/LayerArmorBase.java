package net.minecraft.client.renderer.entity.layers;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public abstract class LayerArmorBase extends LayerRenderer {
   protected static final ResourceLocation ENCHANTED_ITEM_GLINT_RES = new ResourceLocation("textures/misc/enchanted_item_glint.png");
   protected ModelBase field_177189_c;
   protected ModelBase field_177186_d;
   private final RendererLivingEntity renderer;
   private float alpha = 1.0F;
   private float colorR = 1.0F;
   private float colorG = 1.0F;
   private float colorB = 1.0F;
   private boolean field_177193_i;
   private static final Map ARMOR_TEXTURE_RES_MAP = Maps.newHashMap();

   public LayerArmorBase(RendererLivingEntity rendererIn) {
      this.renderer = rendererIn;
      this.initArmor();
   }

   public void doRenderLayer(EntityLivingBase entitylivingbaseIn, float p_177141_2_, float p_177141_3_, float partialTicks, float p_177141_5_, float p_177141_6_, float p_177141_7_, float scale) {
      this.renderLayer(entitylivingbaseIn, p_177141_2_, p_177141_3_, partialTicks, p_177141_5_, p_177141_6_, p_177141_7_, scale, 4);
      this.renderLayer(entitylivingbaseIn, p_177141_2_, p_177141_3_, partialTicks, p_177141_5_, p_177141_6_, p_177141_7_, scale, 3);
      this.renderLayer(entitylivingbaseIn, p_177141_2_, p_177141_3_, partialTicks, p_177141_5_, p_177141_6_, p_177141_7_, scale, 2);
      this.renderLayer(entitylivingbaseIn, p_177141_2_, p_177141_3_, partialTicks, p_177141_5_, p_177141_6_, p_177141_7_, scale, 1);
   }

   public boolean shouldCombineTextures() {
      return false;
   }

   private void renderLayer(EntityLivingBase entitylivingbaseIn, float p_177182_2_, float p_177182_3_, float p_177182_4_, float p_177182_5_, float p_177182_6_, float p_177182_7_, float p_177182_8_, int armorSlot) {
      ItemStack itemstack = this.getCurrentArmor(entitylivingbaseIn, armorSlot);
      if (itemstack != null && itemstack.getItem() instanceof ItemArmor) {
         ItemArmor itemarmor = (ItemArmor)itemstack.getItem();
         ModelBase t = this.func_177175_a(armorSlot);
         t.setModelAttributes(this.renderer.getMainModel());
         t.setLivingAnimations(entitylivingbaseIn, p_177182_2_, p_177182_3_, p_177182_4_);
         this.func_177179_a(t, armorSlot);
         boolean flag = this.isSlotForLeggings(armorSlot);
         this.renderer.bindTexture(this.getArmorResource(itemarmor, flag));
         switch (itemarmor.getArmorMaterial()) {
            case LEATHER:
               int i = itemarmor.getColor(itemstack);
               float f = (float)(i >> 16 & 255) / 255.0F;
               float f1 = (float)(i >> 8 & 255) / 255.0F;
               float f2 = (float)(i & 255) / 255.0F;
               GL11.glColor4f(this.colorR * f, this.colorG * f1, this.colorB * f2, this.alpha);
               t.render(entitylivingbaseIn, p_177182_2_, p_177182_3_, p_177182_5_, p_177182_6_, p_177182_7_, p_177182_8_);
               this.renderer.bindTexture(this.getArmorResource(itemarmor, flag, "overlay"));
            case CHAIN:
            case IRON:
            case GOLD:
            case DIAMOND:
               GL11.glColor4f(this.colorR, this.colorG, this.colorB, this.alpha);
               t.render(entitylivingbaseIn, p_177182_2_, p_177182_3_, p_177182_5_, p_177182_6_, p_177182_7_, p_177182_8_);
            default:
               if (renderEnchantGlint && !this.field_177193_i && itemstack.isItemEnchanted()) {
                  this.func_177183_a(entitylivingbaseIn, t, p_177182_2_, p_177182_3_, p_177182_4_, p_177182_5_, p_177182_6_, p_177182_7_, p_177182_8_);
               }
         }
      }

   }

   public ItemStack getCurrentArmor(EntityLivingBase entitylivingbaseIn, int armorSlot) {
      return entitylivingbaseIn.getCurrentArmor(armorSlot - 1);
   }

   public ModelBase func_177175_a(int p_177175_1_) {
      return this.isSlotForLeggings(p_177175_1_) ? this.field_177189_c : this.field_177186_d;
   }

   private boolean isSlotForLeggings(int armorSlot) {
      return armorSlot == 2;
   }

   private void func_177183_a(EntityLivingBase entitylivingbaseIn, ModelBase modelbaseIn, float p_177183_3_, float p_177183_4_, float p_177183_5_, float p_177183_6_, float p_177183_7_, float p_177183_8_, float p_177183_9_) {
      float f = (float)entitylivingbaseIn.ticksExisted + p_177183_5_;
      this.renderer.bindTexture(ENCHANTED_ITEM_GLINT_RES);
      GL11.glEnable(3042);
      GL11.glDepthFunc(514);
      GL11.glDepthMask(false);
      float f1 = 0.5F;
      GL11.glColor4f(f1, f1, f1, 1.0F);

      for(int i = 0; i < 2; ++i) {
         GL11.glDisable(2896);
         GL11.glBlendFunc(768, 1);
         float f2 = 0.76F;
         GL11.glColor4f(0.5F * f2, 0.25F * f2, 0.8F * f2, 1.0F);
         GL11.glMatrixMode(5890);
         GL11.glLoadIdentity();
         float f3 = 0.33333334F;
         GL11.glScalef(f3, f3, f3);
         GL11.glRotatef(30.0F - (float)i * 60.0F, 0.0F, 0.0F, 1.0F);
         GL11.glTranslatef(0.0F, f * (0.001F + (float)i * 0.003F) * 20.0F, 0.0F);
         GL11.glMatrixMode(5888);
         modelbaseIn.render(entitylivingbaseIn, p_177183_3_, p_177183_4_, p_177183_6_, p_177183_7_, p_177183_8_, p_177183_9_);
      }

      GL11.glMatrixMode(5890);
      GL11.glLoadIdentity();
      GL11.glMatrixMode(5888);
      GL11.glEnable(2896);
      GL11.glDepthMask(true);
      GL11.glDepthFunc(515);
      GL11.glDisable(3042);
   }

   private ResourceLocation getArmorResource(ItemArmor p_177181_1_, boolean p_177181_2_) {
      return this.getArmorResource(p_177181_1_, p_177181_2_, (String)null);
   }

   private ResourceLocation getArmorResource(ItemArmor p_177178_1_, boolean p_177178_2_, String p_177178_3_) {
      String s = String.format("textures/models/armor/%s_layer_%d%s.png", p_177178_1_.getArmorMaterial().getName(), p_177178_2_ ? 2 : 1, p_177178_3_ == null ? "" : String.format("_%s", p_177178_3_));
      ResourceLocation resourcelocation = (ResourceLocation)ARMOR_TEXTURE_RES_MAP.get(s);
      if (resourcelocation == null) {
         resourcelocation = new ResourceLocation(s);
         ARMOR_TEXTURE_RES_MAP.put(s, resourcelocation);
      }

      return resourcelocation;
   }

   protected abstract void initArmor();

   protected abstract void func_177179_a(ModelBase var1, int var2);
}
