package com.mrchoke.client.renderer.entity;

import com.mrchoke.entity.monster.ZombieMean;
import net.minecraft.client.renderer.entity.AbstractZombieRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.model.ZombieModel;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ZombieMeanRenderer extends AbstractZombieRenderer<ZombieMean, ZombieModel<ZombieMean>> {

    private static final ResourceLocation ZOMBIE_MEAN_TEXTURES = new ResourceLocation("choke", "textures/entity/zombiemean/zombie_mean.png");

    public ZombieMeanRenderer(EntityRendererManager renderManagerIn)
    {
        super(renderManagerIn, new ZombieModel<>(0.0F, false), new ZombieModel<>(0.5F, true), new ZombieModel<>(1.0F, true));
    }

    @Override
    public ResourceLocation getEntityTexture(ZombieMean entity)
    {
        return ZOMBIE_MEAN_TEXTURES;
    }
}
