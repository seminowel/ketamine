package net.minecraft.client.renderer.entity.layers;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySkullRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.StringUtils;
import org.lwjgl.opengl.GL11;

public class LayerCustomHead extends LayerRenderer {
   private final ModelRenderer field_177209_a;

   public LayerCustomHead(ModelRenderer p_i46120_1_) {
      this.field_177209_a = p_i46120_1_;
   }

   public void doRenderLayer(EntityLivingBase entitylivingbaseIn, float p_177141_2_, float p_177141_3_, float partialTicks, float p_177141_5_, float p_177141_6_, float p_177141_7_, float scale) {
      ItemStack itemstack = entitylivingbaseIn.getCurrentArmor(3);
      if (itemstack != null && itemstack.getItem() != null) {
         Item item = itemstack.getItem();
         Minecraft minecraft = Minecraft.getMinecraft();
         GL11.glPushMatrix();
         if (entitylivingbaseIn.isSneaking()) {
            GL11.glTranslatef(0.0F, 0.2F, 0.0F);
         }

         boolean flag = entitylivingbaseIn instanceof EntityVillager || entitylivingbaseIn instanceof EntityZombie && ((EntityZombie)entitylivingbaseIn).isVillager();
         float f3;
         if (!flag && entitylivingbaseIn.isChild()) {
            f3 = 2.0F;
            float f1 = 1.4F;
            GL11.glScalef(f1 / f3, f1 / f3, f1 / f3);
            GL11.glTranslatef(0.0F, 16.0F * scale, 0.0F);
         }

         this.field_177209_a.postRender(0.0625F);
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         if (item instanceof ItemBlock) {
            f3 = 0.625F;
            GL11.glTranslatef(0.0F, -0.25F, 0.0F);
            GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
            GL11.glScalef(f3, -f3, -f3);
            if (flag) {
               GL11.glTranslatef(0.0F, 0.1875F, 0.0F);
            }

            minecraft.getItemRenderer().renderItem(entitylivingbaseIn, itemstack, ItemCameraTransforms.TransformType.HEAD);
         } else if (item == Items.skull) {
            f3 = 1.1875F;
            GL11.glScalef(f3, -f3, -f3);
            if (flag) {
               GL11.glTranslatef(0.0F, 0.0625F, 0.0F);
            }

            GameProfile gameprofile = null;
            if (itemstack.hasTagCompound()) {
               NBTTagCompound nbttagcompound = itemstack.getTagCompound();
               if (nbttagcompound.hasKey("SkullOwner", 10)) {
                  gameprofile = NBTUtil.readGameProfileFromNBT(nbttagcompound.getCompoundTag("SkullOwner"));
               } else if (nbttagcompound.hasKey("SkullOwner", 8)) {
                  String s = nbttagcompound.getString("SkullOwner");
                  if (!StringUtils.isNullOrEmpty(s)) {
                     gameprofile = TileEntitySkull.updateGameprofile(new GameProfile((UUID)null, s));
                     nbttagcompound.setTag("SkullOwner", NBTUtil.writeGameProfile(new NBTTagCompound(), gameprofile));
                  }
               }
            }

            TileEntitySkullRenderer.instance.renderSkull(-0.5F, 0.0F, -0.5F, EnumFacing.UP, 180.0F, itemstack.getMetadata(), gameprofile, -1);
         }

         GL11.glPopMatrix();
      }

   }

   public boolean shouldCombineTextures() {
      return true;
   }
}
