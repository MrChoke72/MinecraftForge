//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.mrchoke.client.renderer.entity;

import com.mrchoke.entity.monster.ZombieNasty;
import net.minecraft.client.renderer.entity.AbstractZombieRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.model.ZombieModel;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ZombieNastyRenderer extends AbstractZombieRenderer<ZombieNasty, ZombieModel<ZombieNasty>> {

    private static final ResourceLocation ZOMBIE_NASTY_TEXTURES = new ResourceLocation("choke", "textures/entity/zombienasty/zombie_nasty.png");

    public ZombieNastyRenderer(EntityRendererManager renderManagerIn)
    {
        super(renderManagerIn, new ZombieModel<>(0.0F, false), new ZombieModel<>(0.5F, true), new ZombieModel<>(1.0F, true));
    }

    @Override
    public ResourceLocation getEntityTexture(ZombieNasty entity)
    {
        return ZOMBIE_NASTY_TEXTURES;
    }

}