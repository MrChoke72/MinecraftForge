package net.minecraft.client.renderer;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.Hash.Strategy;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.tileentity.EndPortalTileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class RenderType extends RenderState {
   private static final RenderType SOLID = get("solid", DefaultVertexFormats.BLOCK, 7, 2097152, true, false, RenderType.State.builder().shadeModel(SHADE_ENABLED).lightmap(LIGHTMAP_ENABLED).texture(BLOCK_SHEET_MIPPED).build(true));
   private static final RenderType CUTOUT_MIPPED = get("cutout_mipped", DefaultVertexFormats.BLOCK, 7, 131072, true, false, RenderType.State.builder().shadeModel(SHADE_ENABLED).lightmap(LIGHTMAP_ENABLED).texture(BLOCK_SHEET_MIPPED).alpha(HALF_ALPHA).build(true));
   private static final RenderType CUTOUT = get("cutout", DefaultVertexFormats.BLOCK, 7, 131072, true, false, RenderType.State.builder().shadeModel(SHADE_ENABLED).lightmap(LIGHTMAP_ENABLED).texture(BLOCK_SHEET).alpha(HALF_ALPHA).build(true));
   private static final RenderType TRANSLUCENT = get("translucent", DefaultVertexFormats.BLOCK, 7, 262144, true, true, translucentBase());
   private static final RenderType TRANSLUCENT_NO_CRUMBLING = get("translucent_no_crumbling", DefaultVertexFormats.BLOCK, 7, 262144, false, true, translucentBase());
   private static final RenderType LEASH = get("leash", DefaultVertexFormats.POSITION_COLOR_LIGHTMAP, 7, 256, RenderType.State.builder().texture(NO_TEXTURE).cull(CULL_DISABLED).lightmap(LIGHTMAP_ENABLED).build(false));
   private static final RenderType WATER_MASK = get("water_mask", DefaultVertexFormats.POSITION, 7, 256, RenderType.State.builder().texture(NO_TEXTURE).writeMask(DEPTH_WRITE).build(false));
   private static final RenderType GLINT = get("glint", DefaultVertexFormats.POSITION_TEX, 7, 256, RenderType.State.builder().texture(new RenderState.TextureState(ItemRenderer.RES_ITEM_GLINT, true, false)).writeMask(COLOR_WRITE).cull(CULL_DISABLED).depthTest(DEPTH_EQUAL).transparency(GLINT_TRANSPARENCY).texturing(GLINT_TEXTURING).build(false));
   private static final RenderType ENTITY_GLINT = get("entity_glint", DefaultVertexFormats.POSITION_TEX, 7, 256, RenderType.State.builder().texture(new RenderState.TextureState(ItemRenderer.RES_ITEM_GLINT, true, false)).writeMask(COLOR_WRITE).cull(CULL_DISABLED).depthTest(DEPTH_EQUAL).transparency(GLINT_TRANSPARENCY).texturing(ENTITY_GLINT_TEXTURING).build(false));
   private static final RenderType LIGHTNING = get("lightning", DefaultVertexFormats.POSITION_COLOR, 7, 256, false, true, RenderType.State.builder().writeMask(COLOR_WRITE).transparency(LIGHTNING_TRANSPARENCY).shadeModel(SHADE_ENABLED).build(false));
   public static final RenderType.Type LINES = get("lines", DefaultVertexFormats.POSITION_COLOR, 1, 256, RenderType.State.builder().line(new RenderState.LineState(OptionalDouble.empty())).layer(PROJECTION_LAYERING).transparency(TRANSLUCENT_TRANSPARENCY).writeMask(COLOR_WRITE).build(false));
   private final VertexFormat vertexFormat;
   private final int glMode;
   private final int bufferSize;
   private final boolean useDelegate;
   private final boolean needsSorting;
   private final Optional<RenderType> field_230166_ag_;

   public static RenderType solid() {
      return SOLID;
   }

   public static RenderType cutoutMipped() {
      return CUTOUT_MIPPED;
   }

   public static RenderType cutout() {
      return CUTOUT;
   }

   private static RenderType.State translucentBase() {
      return RenderType.State.builder().shadeModel(SHADE_ENABLED).lightmap(LIGHTMAP_ENABLED).texture(BLOCK_SHEET_MIPPED).transparency(TRANSLUCENT_TRANSPARENCY).build(true);
   }

   public static RenderType translucent() {
      return TRANSLUCENT;
   }

   public static RenderType translucentNoCrumbling() {
      return TRANSLUCENT_NO_CRUMBLING;
   }

   public static RenderType entitySolid(ResourceLocation p_228634_0_) {
      RenderType.State rendertype$state = RenderType.State.builder().texture(new RenderState.TextureState(p_228634_0_, false, false)).transparency(NO_TRANSPARENCY).diffuseLighting(DIFFUSE_LIGHTING_ENABLED).lightmap(LIGHTMAP_ENABLED).overlay(OVERLAY_ENABLED).build(true);
      return get("entity_solid", DefaultVertexFormats.ITEM, 7, 256, true, false, rendertype$state);
   }

   public static RenderType entityCutout(ResourceLocation p_228638_0_) {
      RenderType.State rendertype$state = RenderType.State.builder().texture(new RenderState.TextureState(p_228638_0_, false, false)).transparency(NO_TRANSPARENCY).diffuseLighting(DIFFUSE_LIGHTING_ENABLED).alpha(DEFAULT_ALPHA).lightmap(LIGHTMAP_ENABLED).overlay(OVERLAY_ENABLED).build(true);
      return get("entity_cutout", DefaultVertexFormats.ITEM, 7, 256, true, false, rendertype$state);
   }

   public static RenderType func_230167_a_(ResourceLocation p_230167_0_, boolean p_230167_1_) {
      RenderType.State rendertype$state = RenderType.State.builder().texture(new RenderState.TextureState(p_230167_0_, false, false)).transparency(NO_TRANSPARENCY).diffuseLighting(DIFFUSE_LIGHTING_ENABLED).alpha(DEFAULT_ALPHA).cull(CULL_DISABLED).lightmap(LIGHTMAP_ENABLED).overlay(OVERLAY_ENABLED).build(p_230167_1_);
      return get("entity_cutout_no_cull", DefaultVertexFormats.ITEM, 7, 256, true, false, rendertype$state);
   }

   public static RenderType entityCutoutNoCull(ResourceLocation p_228640_0_) {
      return func_230167_a_(p_228640_0_, true);
   }

   public static RenderType entityTranslucentCull(ResourceLocation p_228642_0_) {
      RenderType.State rendertype$state = RenderType.State.builder().texture(new RenderState.TextureState(p_228642_0_, false, false)).transparency(TRANSLUCENT_TRANSPARENCY).diffuseLighting(DIFFUSE_LIGHTING_ENABLED).alpha(DEFAULT_ALPHA).lightmap(LIGHTMAP_ENABLED).overlay(OVERLAY_ENABLED).build(true);
      return get("entity_translucent_cull", DefaultVertexFormats.ITEM, 7, 256, true, true, rendertype$state);
   }

   public static RenderType func_230168_b_(ResourceLocation p_230168_0_, boolean p_230168_1_) {
      RenderType.State rendertype$state = RenderType.State.builder().texture(new RenderState.TextureState(p_230168_0_, false, false)).transparency(TRANSLUCENT_TRANSPARENCY).diffuseLighting(DIFFUSE_LIGHTING_ENABLED).alpha(DEFAULT_ALPHA).cull(CULL_DISABLED).lightmap(LIGHTMAP_ENABLED).overlay(OVERLAY_ENABLED).build(p_230168_1_);
      return get("entity_translucent", DefaultVertexFormats.ITEM, 7, 256, true, true, rendertype$state);
   }

   public static RenderType entityTranslucent(ResourceLocation p_228644_0_) {
      return func_230168_b_(p_228644_0_, true);
   }

   public static RenderType entitySmoothCutout(ResourceLocation p_228646_0_) {
      RenderType.State rendertype$state = RenderType.State.builder().texture(new RenderState.TextureState(p_228646_0_, false, false)).alpha(HALF_ALPHA).diffuseLighting(DIFFUSE_LIGHTING_ENABLED).shadeModel(SHADE_ENABLED).cull(CULL_DISABLED).lightmap(LIGHTMAP_ENABLED).build(true);
      return get("entity_smooth_cutout", DefaultVertexFormats.ITEM, 7, 256, rendertype$state);
   }

   public static RenderType beaconBeam(ResourceLocation p_228637_0_, boolean p_228637_1_) {
      RenderType.State rendertype$state = RenderType.State.builder().texture(new RenderState.TextureState(p_228637_0_, false, false)).transparency(p_228637_1_ ? TRANSLUCENT_TRANSPARENCY : NO_TRANSPARENCY).writeMask(p_228637_1_ ? COLOR_WRITE : COLOR_DEPTH_WRITE).fog(NO_FOG).build(false);
      return get("beacon_beam", DefaultVertexFormats.BLOCK, 7, 256, false, true, rendertype$state);
   }

   public static RenderType entityDecal(ResourceLocation p_228648_0_) {
      RenderType.State rendertype$state = RenderType.State.builder().texture(new RenderState.TextureState(p_228648_0_, false, false)).diffuseLighting(DIFFUSE_LIGHTING_ENABLED).alpha(DEFAULT_ALPHA).depthTest(DEPTH_EQUAL).cull(CULL_DISABLED).lightmap(LIGHTMAP_ENABLED).overlay(OVERLAY_ENABLED).build(false);
      return get("entity_decal", DefaultVertexFormats.ITEM, 7, 256, rendertype$state);
   }

   public static RenderType entityNoOutline(ResourceLocation p_228650_0_) {
      RenderType.State rendertype$state = RenderType.State.builder().texture(new RenderState.TextureState(p_228650_0_, false, false)).transparency(TRANSLUCENT_TRANSPARENCY).diffuseLighting(DIFFUSE_LIGHTING_ENABLED).alpha(DEFAULT_ALPHA).cull(CULL_DISABLED).lightmap(LIGHTMAP_ENABLED).overlay(OVERLAY_ENABLED).writeMask(COLOR_WRITE).build(false);
      return get("entity_no_outline", DefaultVertexFormats.ITEM, 7, 256, false, true, rendertype$state);
   }

   public static RenderType entityAlpha(ResourceLocation p_228635_0_, float p_228635_1_) {
      RenderType.State rendertype$state = RenderType.State.builder().texture(new RenderState.TextureState(p_228635_0_, false, false)).alpha(new RenderState.AlphaState(p_228635_1_)).cull(CULL_DISABLED).build(true);
      return get("entity_alpha", DefaultVertexFormats.ITEM, 7, 256, rendertype$state);
   }

   public static RenderType eyes(ResourceLocation p_228652_0_) {
      RenderState.TextureState renderstate$texturestate = new RenderState.TextureState(p_228652_0_, false, false);
      return get("eyes", DefaultVertexFormats.ITEM, 7, 256, false, true, RenderType.State.builder().texture(renderstate$texturestate).transparency(ADDITIVE_TRANSPARENCY).writeMask(COLOR_WRITE).fog(BLACK_FOG).build(false));
   }

   public static RenderType energySwirl(ResourceLocation p_228636_0_, float p_228636_1_, float p_228636_2_) {
      return get("energy_swirl", DefaultVertexFormats.ITEM, 7, 256, false, true, RenderType.State.builder().texture(new RenderState.TextureState(p_228636_0_, false, false)).texturing(new RenderState.OffsetTexturingState(p_228636_1_, p_228636_2_)).fog(BLACK_FOG).transparency(ADDITIVE_TRANSPARENCY).diffuseLighting(DIFFUSE_LIGHTING_ENABLED).alpha(DEFAULT_ALPHA).cull(CULL_DISABLED).lightmap(LIGHTMAP_ENABLED).overlay(OVERLAY_ENABLED).build(false));
   }

   public static RenderType leash() {
      return LEASH;
   }

   public static RenderType waterMask() {
      return WATER_MASK;
   }

   public static RenderType outline(ResourceLocation p_228654_0_) {
      return get("outline", DefaultVertexFormats.POSITION_COLOR_TEX, 7, 256, RenderType.State.builder().texture(new RenderState.TextureState(p_228654_0_, false, false)).cull(CULL_DISABLED).depthTest(DEPTH_ALWAYS).alpha(DEFAULT_ALPHA).texturing(OUTLINE_TEXTURING).fog(NO_FOG).target(OUTLINE_TARGET).func_230173_a_(RenderType.OutlineState.IS_OUTLINE));
   }

   public static RenderType glint() {
      return GLINT;
   }

   public static RenderType entityGlint() {
      return ENTITY_GLINT;
   }

   public static RenderType crumbling(ResourceLocation p_228656_0_) {
      RenderState.TextureState renderstate$texturestate = new RenderState.TextureState(p_228656_0_, false, false);
      return get("crumbling", DefaultVertexFormats.BLOCK, 7, 256, false, true, RenderType.State.builder().texture(renderstate$texturestate).alpha(DEFAULT_ALPHA).transparency(CRUMBLING_TRANSPARENCY).writeMask(COLOR_WRITE).layer(POLYGON_OFFSET_LAYERING).build(false));
   }

   public static RenderType text(ResourceLocation p_228658_0_) {
      return get("text", DefaultVertexFormats.POSITION_COLOR_TEX_LIGHTMAP, 7, 256, false, true, RenderType.State.builder().texture(new RenderState.TextureState(p_228658_0_, false, false)).alpha(DEFAULT_ALPHA).transparency(TRANSLUCENT_TRANSPARENCY).lightmap(LIGHTMAP_ENABLED).build(false));
   }

   public static RenderType textSeeThrough(ResourceLocation p_228660_0_) {
      return get("text_see_through", DefaultVertexFormats.POSITION_COLOR_TEX_LIGHTMAP, 7, 256, false, true, RenderType.State.builder().texture(new RenderState.TextureState(p_228660_0_, false, false)).alpha(DEFAULT_ALPHA).transparency(TRANSLUCENT_TRANSPARENCY).lightmap(LIGHTMAP_ENABLED).depthTest(DEPTH_ALWAYS).writeMask(COLOR_WRITE).build(false));
   }

   public static RenderType lightning() {
      return LIGHTNING;
   }

   public static RenderType endPortal(int p_228630_0_) {
      RenderState.TransparencyState renderstate$transparencystate;
      RenderState.TextureState renderstate$texturestate;
      if (p_228630_0_ <= 1) {
         renderstate$transparencystate = TRANSLUCENT_TRANSPARENCY;
         renderstate$texturestate = new RenderState.TextureState(EndPortalTileEntityRenderer.END_SKY_TEXTURE, false, false);
      } else {
         renderstate$transparencystate = ADDITIVE_TRANSPARENCY;
         renderstate$texturestate = new RenderState.TextureState(EndPortalTileEntityRenderer.END_PORTAL_TEXTURE, false, false);
      }

      return get("end_portal", DefaultVertexFormats.POSITION_COLOR, 7, 256, false, true, RenderType.State.builder().transparency(renderstate$transparencystate).texture(renderstate$texturestate).texturing(new RenderState.PortalTexturingState(p_228630_0_)).fog(BLACK_FOG).build(false));
   }

   public static RenderType lines() {
      return LINES;
   }

   public RenderType(String p_i225992_1_, VertexFormat p_i225992_2_, int p_i225992_3_, int p_i225992_4_, boolean p_i225992_5_, boolean p_i225992_6_, Runnable p_i225992_7_, Runnable p_i225992_8_) {
      super(p_i225992_1_, p_i225992_7_, p_i225992_8_);
      this.vertexFormat = p_i225992_2_;
      this.glMode = p_i225992_3_;
      this.bufferSize = p_i225992_4_;
      this.useDelegate = p_i225992_5_;
      this.needsSorting = p_i225992_6_;
      this.field_230166_ag_ = Optional.of(this);
   }

   public static RenderType.Type get(String p_228632_0_, VertexFormat p_228632_1_, int p_228632_2_, int p_228632_3_, RenderType.State p_228632_4_) {
      return get(p_228632_0_, p_228632_1_, p_228632_2_, p_228632_3_, false, false, p_228632_4_);
   }

   public static RenderType.Type get(String name, VertexFormat vertexFormatIn, int glMode, int p_228633_3_, boolean p_228633_4_, boolean p_228633_5_, RenderType.State p_228633_6_) {
      return RenderType.Type.getOrCreate(name, vertexFormatIn, glMode, p_228633_3_, p_228633_4_, p_228633_5_, p_228633_6_);
   }

   public void finish(BufferBuilder p_228631_1_, int p_228631_2_, int p_228631_3_, int p_228631_4_) {
      if (p_228631_1_.isDrawing()) {
         if (this.needsSorting) {
            p_228631_1_.sortVertexData((float)p_228631_2_, (float)p_228631_3_, (float)p_228631_4_);
         }

         p_228631_1_.finishDrawing();
         this.enable();
         WorldVertexBufferUploader.draw(p_228631_1_);
         this.disable();
      }
   }

   public String toString() {
      return this.name;
   }

   public static List<RenderType> getBlockRenderTypes() {
      return ImmutableList.of(solid(), cutoutMipped(), cutout(), translucent());
   }

   public int defaultBufferSize() {
      return this.bufferSize;
   }

   public VertexFormat getVertexFormat() {
      return this.vertexFormat;
   }

   public int getGlMode() {
      return this.glMode;
   }

   public Optional<RenderType> getOutline() {
      return Optional.empty();
   }

   public boolean func_230041_s_() {
      return false;
   }

   public boolean getUseDelegate() {
      return this.useDelegate;
   }

   public Optional<RenderType> func_230169_u_() {
      return this.field_230166_ag_;
   }

   @OnlyIn(Dist.CLIENT)
   static enum OutlineState {
      NONE,
      IS_OUTLINE,
      AFFECTS_OUTLINE;
   }

   @OnlyIn(Dist.CLIENT)
   public static final class State {
      private final RenderState.TextureState texture;
      private final RenderState.TransparencyState transparency;
      private final RenderState.DiffuseLightingState diffuseLighting;
      private final RenderState.ShadeModelState shadowModel;
      private final RenderState.AlphaState alpha;
      private final RenderState.DepthTestState depthTest;
      private final RenderState.CullState cull;
      private final RenderState.LightmapState lightmap;
      private final RenderState.OverlayState overlay;
      private final RenderState.FogState fog;
      private final RenderState.LayerState layer;
      private final RenderState.TargetState target;
      private final RenderState.TexturingState texturing;
      private final RenderState.WriteMaskState writeMask;
      private final RenderState.LineState line;
      private final RenderType.OutlineState field_230171_p_;
      private final ImmutableList<RenderState> renderStates;

      private State(RenderState.TextureState p_i230053_1_, RenderState.TransparencyState p_i230053_2_, RenderState.DiffuseLightingState p_i230053_3_, RenderState.ShadeModelState p_i230053_4_, RenderState.AlphaState p_i230053_5_, RenderState.DepthTestState p_i230053_6_, RenderState.CullState p_i230053_7_, RenderState.LightmapState p_i230053_8_, RenderState.OverlayState p_i230053_9_, RenderState.FogState p_i230053_10_, RenderState.LayerState p_i230053_11_, RenderState.TargetState p_i230053_12_, RenderState.TexturingState p_i230053_13_, RenderState.WriteMaskState p_i230053_14_, RenderState.LineState p_i230053_15_, RenderType.OutlineState p_i230053_16_) {
         this.texture = p_i230053_1_;
         this.transparency = p_i230053_2_;
         this.diffuseLighting = p_i230053_3_;
         this.shadowModel = p_i230053_4_;
         this.alpha = p_i230053_5_;
         this.depthTest = p_i230053_6_;
         this.cull = p_i230053_7_;
         this.lightmap = p_i230053_8_;
         this.overlay = p_i230053_9_;
         this.fog = p_i230053_10_;
         this.layer = p_i230053_11_;
         this.target = p_i230053_12_;
         this.texturing = p_i230053_13_;
         this.writeMask = p_i230053_14_;
         this.line = p_i230053_15_;
         this.field_230171_p_ = p_i230053_16_;
         this.renderStates = ImmutableList.of(this.texture, this.transparency, this.diffuseLighting, this.shadowModel, this.alpha, this.depthTest, this.cull, this.lightmap, this.overlay, this.fog, this.layer, this.target, this.texturing, this.writeMask, this.line);
      }

      public boolean equals(Object p_equals_1_) {
         if (this == p_equals_1_) {
            return true;
         } else if (p_equals_1_ != null && this.getClass() == p_equals_1_.getClass()) {
            RenderType.State rendertype$state = (RenderType.State)p_equals_1_;
            return this.field_230171_p_ == rendertype$state.field_230171_p_ && this.renderStates.equals(rendertype$state.renderStates);
         } else {
            return false;
         }
      }

      public int hashCode() {
         return Objects.hash(this.renderStates, this.field_230171_p_);
      }

      public static RenderType.State.Builder builder() {
         return new RenderType.State.Builder();
      }

      @OnlyIn(Dist.CLIENT)
      public static class Builder {
         private RenderState.TextureState texture = RenderState.NO_TEXTURE;
         private RenderState.TransparencyState transparency = RenderState.NO_TRANSPARENCY;
         private RenderState.DiffuseLightingState diffuseLighting = RenderState.DIFFUSE_LIGHTING_DISABLED;
         private RenderState.ShadeModelState shadeModel = RenderState.SHADE_DISABLED;
         private RenderState.AlphaState alpha = RenderState.ZERO_ALPHA;
         private RenderState.DepthTestState depthTest = RenderState.DEPTH_LEQUAL;
         private RenderState.CullState cull = RenderState.CULL_ENABLED;
         private RenderState.LightmapState lightmap = RenderState.LIGHTMAP_DISABLED;
         private RenderState.OverlayState overlay = RenderState.OVERLAY_DISABLED;
         private RenderState.FogState fog = RenderState.FOG;
         private RenderState.LayerState layer = RenderState.NO_LAYERING;
         private RenderState.TargetState target = RenderState.MAIN_TARGET;
         private RenderState.TexturingState texturing = RenderState.DEFAULT_TEXTURING;
         private RenderState.WriteMaskState writeMask = RenderState.COLOR_DEPTH_WRITE;
         private RenderState.LineState line = RenderState.DEFAULT_LINE;

         private Builder() {
         }

         public RenderType.State.Builder texture(RenderState.TextureState p_228724_1_) {
            this.texture = p_228724_1_;
            return this;
         }

         public RenderType.State.Builder transparency(RenderState.TransparencyState p_228726_1_) {
            this.transparency = p_228726_1_;
            return this;
         }

         public RenderType.State.Builder diffuseLighting(RenderState.DiffuseLightingState p_228716_1_) {
            this.diffuseLighting = p_228716_1_;
            return this;
         }

         public RenderType.State.Builder shadeModel(RenderState.ShadeModelState p_228723_1_) {
            this.shadeModel = p_228723_1_;
            return this;
         }

         public RenderType.State.Builder alpha(RenderState.AlphaState p_228713_1_) {
            this.alpha = p_228713_1_;
            return this;
         }

         public RenderType.State.Builder depthTest(RenderState.DepthTestState p_228715_1_) {
            this.depthTest = p_228715_1_;
            return this;
         }

         public RenderType.State.Builder cull(RenderState.CullState p_228714_1_) {
            this.cull = p_228714_1_;
            return this;
         }

         public RenderType.State.Builder lightmap(RenderState.LightmapState p_228719_1_) {
            this.lightmap = p_228719_1_;
            return this;
         }

         public RenderType.State.Builder overlay(RenderState.OverlayState p_228722_1_) {
            this.overlay = p_228722_1_;
            return this;
         }

         public RenderType.State.Builder fog(RenderState.FogState p_228717_1_) {
            this.fog = p_228717_1_;
            return this;
         }

         public RenderType.State.Builder layer(RenderState.LayerState p_228718_1_) {
            this.layer = p_228718_1_;
            return this;
         }

         public RenderType.State.Builder target(RenderState.TargetState p_228721_1_) {
            this.target = p_228721_1_;
            return this;
         }

         public RenderType.State.Builder texturing(RenderState.TexturingState p_228725_1_) {
            this.texturing = p_228725_1_;
            return this;
         }

         public RenderType.State.Builder writeMask(RenderState.WriteMaskState p_228727_1_) {
            this.writeMask = p_228727_1_;
            return this;
         }

         public RenderType.State.Builder line(RenderState.LineState p_228720_1_) {
            this.line = p_228720_1_;
            return this;
         }

         public RenderType.State build(boolean outlineIn) {
            return this.func_230173_a_(outlineIn ? RenderType.OutlineState.AFFECTS_OUTLINE : RenderType.OutlineState.NONE);
         }

         public RenderType.State func_230173_a_(RenderType.OutlineState p_230173_1_) {
            return new RenderType.State(this.texture, this.transparency, this.diffuseLighting, this.shadeModel, this.alpha, this.depthTest, this.cull, this.lightmap, this.overlay, this.fog, this.layer, this.target, this.texturing, this.writeMask, this.line, p_230173_1_);
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   static final class Type extends RenderType {
      private static final ObjectOpenCustomHashSet<RenderType.Type> TYPES = new ObjectOpenCustomHashSet<>(RenderType.Type.EqualityStrategy.INSTANCE);
      private final RenderType.State renderState;
      private final int hashCode;
      private final Optional<RenderType> outline;
      private final boolean field_230170_V_;

      private Type(String p_i225993_1_, VertexFormat p_i225993_2_, int p_i225993_3_, int p_i225993_4_, boolean p_i225993_5_, boolean p_i225993_6_, RenderType.State p_i225993_7_) {
         super(p_i225993_1_, p_i225993_2_, p_i225993_3_, p_i225993_4_, p_i225993_5_, p_i225993_6_, () -> {
            p_i225993_7_.renderStates.forEach(RenderState::enable);
         }, () -> {
            p_i225993_7_.renderStates.forEach(RenderState::disable);
         });
         this.renderState = p_i225993_7_;
         this.outline = p_i225993_7_.field_230171_p_ == RenderType.OutlineState.AFFECTS_OUTLINE ? p_i225993_7_.texture.func_228606_c_().map(RenderType::outline) : Optional.empty();
         this.field_230170_V_ = p_i225993_7_.field_230171_p_ == RenderType.OutlineState.IS_OUTLINE;
         this.hashCode = Objects.hash(super.hashCode(), p_i225993_7_);
      }

      private static RenderType.Type getOrCreate(String p_228676_0_, VertexFormat p_228676_1_, int p_228676_2_, int p_228676_3_, boolean p_228676_4_, boolean p_228676_5_, RenderType.State p_228676_6_) {
         return TYPES.addOrGet(new RenderType.Type(p_228676_0_, p_228676_1_, p_228676_2_, p_228676_3_, p_228676_4_, p_228676_5_, p_228676_6_));
      }

      public Optional<RenderType> getOutline() {
         return this.outline;
      }

      public boolean func_230041_s_() {
         return this.field_230170_V_;
      }

      public boolean equals(@Nullable Object p_equals_1_) {
         return this == p_equals_1_;
      }

      public int hashCode() {
         return this.hashCode;
      }

      @OnlyIn(Dist.CLIENT)
      static enum EqualityStrategy implements Strategy<RenderType.Type> {
         INSTANCE;

         public int hashCode(@Nullable RenderType.Type p_hashCode_1_) {
            return p_hashCode_1_ == null ? 0 : p_hashCode_1_.hashCode;
         }

         public boolean equals(@Nullable RenderType.Type p_equals_1_, @Nullable RenderType.Type p_equals_2_) {
            if (p_equals_1_ == p_equals_2_) {
               return true;
            } else {
               return p_equals_1_ != null && p_equals_2_ != null ? Objects.equals(p_equals_1_.renderState, p_equals_2_.renderState) : false;
            }
         }
      }
   }
}
