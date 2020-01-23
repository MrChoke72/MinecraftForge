package net.minecraft.client.renderer.entity;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import java.util.Map;
import javax.annotation.Nullable;

import com.mrchoke.client.renderer.entity.ZombieMeanRenderer;
import com.mrchoke.client.renderer.entity.ZombieNastyRenderer;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.Quaternion;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.culling.ClippingHelperImpl;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPartEntity;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EntityRendererManager {
   private static final RenderType field_229082_e_ = RenderType.func_228650_h_(new ResourceLocation("textures/misc/shadow.png"));
   public final Map<EntityType<?>, EntityRenderer<?>> renderers = Maps.newHashMap();
   private final Map<String, PlayerRenderer> skinMap = Maps.newHashMap();
   private final PlayerRenderer playerRenderer;
   private final FontRenderer textRenderer;
   public final TextureManager textureManager;
   private World world;
   public ActiveRenderInfo info;
   private Quaternion field_229083_k_;
   public Entity pointedEntity;
   public final GameSettings options;
   private boolean renderShadow = true;
   private boolean debugBoundingBox;

   public <E extends Entity> int func_229085_a_(E p_229085_1_, float p_229085_2_) {
      return this.getRenderer(p_229085_1_).func_229100_c_(p_229085_1_, p_229085_2_);
   }

   public <T extends Entity> void addRenderer(EntityType<T> entityType, EntityRenderer<? super T> renderer) {
      this.renderers.put(entityType, renderer);
   }

   private void func_229097_a_(net.minecraft.client.renderer.ItemRenderer p_229097_1_, IReloadableResourceManager p_229097_2_) {
      this.addRenderer(EntityType.AREA_EFFECT_CLOUD, new AreaEffectCloudRenderer(this));
      this.addRenderer(EntityType.ARMOR_STAND, new ArmorStandRenderer(this));
      this.addRenderer(EntityType.ARROW, new TippedArrowRenderer(this));
      this.addRenderer(EntityType.BAT, new BatRenderer(this));
      this.addRenderer(EntityType.field_226289_e_, new BeeRenderer(this));
      this.addRenderer(EntityType.BLAZE, new BlazeRenderer(this));
      this.addRenderer(EntityType.BOAT, new BoatRenderer(this));
      this.addRenderer(EntityType.CAT, new CatRenderer(this));
      this.addRenderer(EntityType.CAVE_SPIDER, new CaveSpiderRenderer(this));
      this.addRenderer(EntityType.CHEST_MINECART, new MinecartRenderer<>(this));
      this.addRenderer(EntityType.CHICKEN, new ChickenRenderer(this));
      this.addRenderer(EntityType.COD, new CodRenderer(this));
      this.addRenderer(EntityType.COMMAND_BLOCK_MINECART, new MinecartRenderer<>(this));
      this.addRenderer(EntityType.COW, new CowRenderer(this));
      this.addRenderer(EntityType.CREEPER, new CreeperRenderer(this));
      this.addRenderer(EntityType.DOLPHIN, new DolphinRenderer(this));
      this.addRenderer(EntityType.DONKEY, new ChestedHorseRenderer<>(this, 0.87F));
      this.addRenderer(EntityType.DRAGON_FIREBALL, new DragonFireballRenderer(this));
      this.addRenderer(EntityType.DROWNED, new DrownedRenderer(this));
      this.addRenderer(EntityType.EGG, new SpriteRenderer<>(this, p_229097_1_));
      this.addRenderer(EntityType.ELDER_GUARDIAN, new ElderGuardianRenderer(this));
      this.addRenderer(EntityType.END_CRYSTAL, new EnderCrystalRenderer(this));
      this.addRenderer(EntityType.ENDER_DRAGON, new EnderDragonRenderer(this));
      this.addRenderer(EntityType.ENDERMAN, new EndermanRenderer(this));
      this.addRenderer(EntityType.ENDERMITE, new EndermiteRenderer(this));
      this.addRenderer(EntityType.ENDER_PEARL, new SpriteRenderer<>(this, p_229097_1_));
      this.addRenderer(EntityType.EVOKER_FANGS, new EvokerFangsRenderer(this));
      this.addRenderer(EntityType.EVOKER, new EvokerRenderer<>(this));
      this.addRenderer(EntityType.EXPERIENCE_BOTTLE, new SpriteRenderer<>(this, p_229097_1_));
      this.addRenderer(EntityType.EXPERIENCE_ORB, new ExperienceOrbRenderer(this));
      this.addRenderer(EntityType.EYE_OF_ENDER, new SpriteRenderer<>(this, p_229097_1_, 1.0F, true));
      this.addRenderer(EntityType.FALLING_BLOCK, new FallingBlockRenderer(this));
      this.addRenderer(EntityType.FIREBALL, new SpriteRenderer<>(this, p_229097_1_, 3.0F, true));
      this.addRenderer(EntityType.FIREWORK_ROCKET, new FireworkRocketRenderer(this, p_229097_1_));
      this.addRenderer(EntityType.FISHING_BOBBER, new FishRenderer(this));
      this.addRenderer(EntityType.FOX, new FoxRenderer(this));
      this.addRenderer(EntityType.FURNACE_MINECART, new MinecartRenderer<>(this));
      this.addRenderer(EntityType.GHAST, new GhastRenderer(this));
      this.addRenderer(EntityType.GIANT, new GiantZombieRenderer(this, 6.0F));
      this.addRenderer(EntityType.GUARDIAN, new GuardianRenderer(this));
      this.addRenderer(EntityType.HOPPER_MINECART, new MinecartRenderer<>(this));
      this.addRenderer(EntityType.HORSE, new HorseRenderer(this));
      this.addRenderer(EntityType.HUSK, new HuskRenderer(this));
      this.addRenderer(EntityType.ILLUSIONER, new IllusionerRenderer(this));
      this.addRenderer(EntityType.IRON_GOLEM, new IronGolemRenderer(this));
      this.addRenderer(EntityType.ITEM_FRAME, new ItemFrameRenderer(this, p_229097_1_));
      this.addRenderer(EntityType.ITEM, new ItemRenderer(this, p_229097_1_));
      this.addRenderer(EntityType.LEASH_KNOT, new LeashKnotRenderer(this));
      this.addRenderer(EntityType.LIGHTNING_BOLT, new LightningBoltRenderer(this));
      this.addRenderer(EntityType.LLAMA, new LlamaRenderer(this));
      this.addRenderer(EntityType.LLAMA_SPIT, new LlamaSpitRenderer(this));
      this.addRenderer(EntityType.MAGMA_CUBE, new MagmaCubeRenderer(this));
      this.addRenderer(EntityType.MINECART, new MinecartRenderer<>(this));
      this.addRenderer(EntityType.MOOSHROOM, new MooshroomRenderer(this));
      this.addRenderer(EntityType.MULE, new ChestedHorseRenderer<>(this, 0.92F));
      this.addRenderer(EntityType.OCELOT, new OcelotRenderer(this));
      this.addRenderer(EntityType.PAINTING, new PaintingRenderer(this));
      this.addRenderer(EntityType.PANDA, new PandaRenderer(this));
      this.addRenderer(EntityType.PARROT, new ParrotRenderer(this));
      this.addRenderer(EntityType.PHANTOM, new PhantomRenderer(this));
      this.addRenderer(EntityType.PIG, new PigRenderer(this));
      this.addRenderer(EntityType.PILLAGER, new PillagerRenderer(this));
      this.addRenderer(EntityType.POLAR_BEAR, new PolarBearRenderer(this));
      this.addRenderer(EntityType.POTION, new SpriteRenderer<>(this, p_229097_1_));
      this.addRenderer(EntityType.PUFFERFISH, new PufferfishRenderer(this));
      this.addRenderer(EntityType.RABBIT, new RabbitRenderer(this));
      this.addRenderer(EntityType.RAVAGER, new RavagerRenderer(this));
      this.addRenderer(EntityType.SALMON, new SalmonRenderer(this));
      this.addRenderer(EntityType.SHEEP, new SheepRenderer(this));
      this.addRenderer(EntityType.SHULKER_BULLET, new ShulkerBulletRenderer(this));
      this.addRenderer(EntityType.SHULKER, new ShulkerRenderer(this));
      this.addRenderer(EntityType.SILVERFISH, new SilverfishRenderer(this));
      this.addRenderer(EntityType.SKELETON_HORSE, new UndeadHorseRenderer(this));
      this.addRenderer(EntityType.SKELETON, new SkeletonRenderer(this));
      this.addRenderer(EntityType.SLIME, new SlimeRenderer(this));
      this.addRenderer(EntityType.SMALL_FIREBALL, new SpriteRenderer<>(this, p_229097_1_, 0.75F, true));
      this.addRenderer(EntityType.SNOWBALL, new SpriteRenderer<>(this, p_229097_1_));
      this.addRenderer(EntityType.SNOW_GOLEM, new SnowManRenderer(this));
      this.addRenderer(EntityType.SPAWNER_MINECART, new MinecartRenderer<>(this));
      this.addRenderer(EntityType.SPECTRAL_ARROW, new SpectralArrowRenderer(this));
      this.addRenderer(EntityType.SPIDER, new SpiderRenderer<>(this));
      this.addRenderer(EntityType.SQUID, new SquidRenderer(this));
      this.addRenderer(EntityType.STRAY, new StrayRenderer(this));
      this.addRenderer(EntityType.TNT_MINECART, new TNTMinecartRenderer(this));
      this.addRenderer(EntityType.TNT, new TNTRenderer(this));
      this.addRenderer(EntityType.TRADER_LLAMA, new LlamaRenderer(this));
      this.addRenderer(EntityType.TRIDENT, new TridentRenderer(this));
      this.addRenderer(EntityType.TROPICAL_FISH, new TropicalFishRenderer(this));
      this.addRenderer(EntityType.TURTLE, new TurtleRenderer(this));
      this.addRenderer(EntityType.VEX, new VexRenderer(this));
      this.addRenderer(EntityType.VILLAGER, new VillagerRenderer(this, p_229097_2_));
      this.addRenderer(EntityType.VINDICATOR, new VindicatorRenderer(this));
      this.addRenderer(EntityType.WANDERING_TRADER, new WanderingTraderRenderer(this));
      this.addRenderer(EntityType.WITCH, new WitchRenderer(this));
      this.addRenderer(EntityType.WITHER, new WitherRenderer(this));
      this.addRenderer(EntityType.WITHER_SKELETON, new WitherSkeletonRenderer(this));
      this.addRenderer(EntityType.WITHER_SKULL, new WitherSkullRenderer(this));
      this.addRenderer(EntityType.WOLF, new WolfRenderer(this));
      this.addRenderer(EntityType.ZOMBIE_HORSE, new UndeadHorseRenderer(this));
      this.addRenderer(EntityType.ZOMBIE, new ZombieRenderer(this));
      this.addRenderer(EntityType.ZOMBIE_PIGMAN, new PigZombieRenderer(this));
      this.addRenderer(EntityType.ZOMBIE_VILLAGER, new ZombieVillagerRenderer(this, p_229097_2_));

      //AH ADD ****
      this.addRenderer(EntityType.ZOMBIE_NASTY, new ZombieNastyRenderer(this));
      this.addRenderer(EntityType.ZOMBIE_MEAN, new ZombieMeanRenderer(this));
      //AH ADD END
   }

   public EntityRendererManager(TextureManager p_i226034_1_, net.minecraft.client.renderer.ItemRenderer p_i226034_2_, IReloadableResourceManager p_i226034_3_, FontRenderer p_i226034_4_, GameSettings p_i226034_5_) {
      this.textureManager = p_i226034_1_;
      this.textRenderer = p_i226034_4_;
      this.options = p_i226034_5_;
      this.func_229097_a_(p_i226034_2_, p_i226034_3_);
      this.playerRenderer = new PlayerRenderer(this);
      this.skinMap.put("default", this.playerRenderer);
      this.skinMap.put("slim", new PlayerRenderer(this, true));

      for(EntityType<?> entitytype : Registry.ENTITY_TYPE) {
         if (entitytype != EntityType.PLAYER && !this.renderers.containsKey(entitytype)) {
            throw new IllegalStateException("No renderer registered for " + Registry.ENTITY_TYPE.getKey(entitytype));
         }
      }

   }

   public <T extends Entity> EntityRenderer<? super T> getRenderer(T entityIn) {
      if (entityIn instanceof AbstractClientPlayerEntity) {
         String s = ((AbstractClientPlayerEntity)entityIn).getSkinType();
         PlayerRenderer playerrenderer = this.skinMap.get(s);
         return (EntityRenderer<? super T>)(playerrenderer != null ? playerrenderer : this.playerRenderer);
      } else {
         return (EntityRenderer<? super T>)this.renderers.get(entityIn.getType());
      }
   }

   public void func_229088_a_(World p_229088_1_, ActiveRenderInfo p_229088_2_, Entity p_229088_3_) {
      this.world = p_229088_1_;
      this.info = p_229088_2_;
      this.field_229083_k_ = p_229088_2_.func_227995_f_();
      this.pointedEntity = p_229088_3_;
   }

   public void func_229089_a_(Quaternion p_229089_1_) {
      this.field_229083_k_ = p_229089_1_;
   }

   public void setRenderShadow(boolean renderShadowIn) {
      this.renderShadow = renderShadowIn;
   }

   public void setDebugBoundingBox(boolean debugBoundingBoxIn) {
      this.debugBoundingBox = debugBoundingBoxIn;
   }

   public boolean isDebugBoundingBox() {
      return this.debugBoundingBox;
   }

   public <E extends Entity> boolean func_229086_a_(E p_229086_1_, ClippingHelperImpl p_229086_2_, double p_229086_3_, double p_229086_5_, double p_229086_7_) {
      EntityRenderer<? super E> entityrenderer = this.getRenderer(p_229086_1_);
      return entityrenderer.func_225626_a_(p_229086_1_, p_229086_2_, p_229086_3_, p_229086_5_, p_229086_7_);
   }

   public <E extends Entity> void func_229084_a_(E p_229084_1_, double p_229084_2_, double p_229084_4_, double p_229084_6_, float p_229084_8_, float p_229084_9_, MatrixStack p_229084_10_, IRenderTypeBuffer p_229084_11_, int p_229084_12_) {
      EntityRenderer<? super E> entityrenderer = this.getRenderer(p_229084_1_);

      try {
         Vec3d vec3d = entityrenderer.func_225627_b_(p_229084_1_, p_229084_9_);
         double d2 = p_229084_2_ + vec3d.getX();
         double d3 = p_229084_4_ + vec3d.getY();
         double d0 = p_229084_6_ + vec3d.getZ();
         p_229084_10_.func_227860_a_();
         p_229084_10_.func_227861_a_(d2, d3, d0);
         entityrenderer.func_225623_a_(p_229084_1_, p_229084_8_, p_229084_9_, p_229084_10_, p_229084_11_, p_229084_12_);
         if (p_229084_1_.canRenderOnFire()) {
            this.func_229095_a_(p_229084_10_, p_229084_11_, p_229084_1_);
         }

         p_229084_10_.func_227861_a_(-vec3d.getX(), -vec3d.getY(), -vec3d.getZ());
         if (this.options.entityShadows && this.renderShadow && entityrenderer.shadowSize > 0.0F && !p_229084_1_.isInvisible()) {
            double d1 = this.getDistanceToCamera(p_229084_1_.getPosX(), p_229084_1_.getPosY(), p_229084_1_.getPosZ());
            float f = (float)((1.0D - d1 / 256.0D) * (double)entityrenderer.shadowOpaque);
            if (f > 0.0F) {
               func_229096_a_(p_229084_10_, p_229084_11_, p_229084_1_, f, p_229084_9_, this.world, entityrenderer.shadowSize);
            }
         }

         if (this.debugBoundingBox && !p_229084_1_.isInvisible() && !Minecraft.getInstance().isReducedDebug()) {
            this.func_229093_a_(p_229084_10_, p_229084_11_.getBuffer(RenderType.func_228659_m_()), p_229084_1_, p_229084_9_);
         }

         p_229084_10_.func_227865_b_();
      } catch (Throwable throwable) {
         CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Rendering entity in world");
         CrashReportCategory crashreportcategory = crashreport.makeCategory("Entity being rendered");
         p_229084_1_.fillCrashReport(crashreportcategory);
         CrashReportCategory crashreportcategory1 = crashreport.makeCategory("Renderer details");
         crashreportcategory1.addDetail("Assigned renderer", entityrenderer);
         crashreportcategory1.addDetail("Location", CrashReportCategory.getCoordinateInfo(p_229084_2_, p_229084_4_, p_229084_6_));
         crashreportcategory1.addDetail("Rotation", p_229084_8_);
         crashreportcategory1.addDetail("Delta", p_229084_9_);
         throw new ReportedException(crashreport);
      }
   }

   private void func_229093_a_(MatrixStack p_229093_1_, IVertexBuilder p_229093_2_, Entity p_229093_3_, float p_229093_4_) {
      float f = p_229093_3_.getWidth() / 2.0F;
      this.func_229094_a_(p_229093_1_, p_229093_2_, p_229093_3_, 1.0F, 1.0F, 1.0F);
      if (p_229093_3_ instanceof EnderDragonEntity) {
         double d0 = p_229093_3_.getPosX() - MathHelper.lerp((double)p_229093_4_, p_229093_3_.lastTickPosX, p_229093_3_.getPosX());
         double d1 = p_229093_3_.getPosY() - MathHelper.lerp((double)p_229093_4_, p_229093_3_.lastTickPosY, p_229093_3_.getPosY());
         double d2 = p_229093_3_.getPosZ() - MathHelper.lerp((double)p_229093_4_, p_229093_3_.lastTickPosZ, p_229093_3_.getPosZ());

         for(EnderDragonPartEntity enderdragonpartentity : ((EnderDragonEntity)p_229093_3_).func_213404_dT()) {
            p_229093_1_.func_227860_a_();
            double d3 = d0 + MathHelper.lerp((double)p_229093_4_, enderdragonpartentity.lastTickPosX, enderdragonpartentity.getPosX());
            double d4 = d1 + MathHelper.lerp((double)p_229093_4_, enderdragonpartentity.lastTickPosY, enderdragonpartentity.getPosY());
            double d5 = d2 + MathHelper.lerp((double)p_229093_4_, enderdragonpartentity.lastTickPosZ, enderdragonpartentity.getPosZ());
            p_229093_1_.func_227861_a_(d3, d4, d5);
            this.func_229094_a_(p_229093_1_, p_229093_2_, enderdragonpartentity, 0.25F, 1.0F, 0.0F);
            p_229093_1_.func_227865_b_();
         }
      }

      if (p_229093_3_ instanceof LivingEntity) {
         float f1 = 0.01F;
         WorldRenderer.func_228427_a_(p_229093_1_, p_229093_2_, (double)(-f), (double)(p_229093_3_.getEyeHeight() - 0.01F), (double)(-f), (double)f, (double)(p_229093_3_.getEyeHeight() + 0.01F), (double)f, 1.0F, 0.0F, 0.0F, 1.0F);
      }

      Vec3d vec3d = p_229093_3_.getLook(p_229093_4_);
      Matrix4f matrix4f = p_229093_1_.func_227866_c_().func_227870_a_();
      p_229093_2_.func_227888_a_(matrix4f, 0.0F, p_229093_3_.getEyeHeight(), 0.0F).func_225586_a_(0, 0, 255, 255).endVertex();
      p_229093_2_.func_227888_a_(matrix4f, (float)(vec3d.x * 2.0D), (float)((double)p_229093_3_.getEyeHeight() + vec3d.y * 2.0D), (float)(vec3d.z * 2.0D)).func_225586_a_(0, 0, 255, 255).endVertex();
   }

   private void func_229094_a_(MatrixStack p_229094_1_, IVertexBuilder p_229094_2_, Entity p_229094_3_, float p_229094_4_, float p_229094_5_, float p_229094_6_) {
      AxisAlignedBB axisalignedbb = p_229094_3_.getBoundingBox().offset(-p_229094_3_.getPosX(), -p_229094_3_.getPosY(), -p_229094_3_.getPosZ());
      WorldRenderer.func_228430_a_(p_229094_1_, p_229094_2_, axisalignedbb, p_229094_4_, p_229094_5_, p_229094_6_, 1.0F);
   }

   private void func_229095_a_(MatrixStack p_229095_1_, IRenderTypeBuffer p_229095_2_, Entity p_229095_3_) {
      TextureAtlasSprite textureatlassprite = ModelBakery.LOCATION_FIRE_0.func_229314_c_();
      TextureAtlasSprite textureatlassprite1 = ModelBakery.LOCATION_FIRE_1.func_229314_c_();
      p_229095_1_.func_227860_a_();
      float f = p_229095_3_.getWidth() * 1.4F;
      p_229095_1_.func_227862_a_(f, f, f);
      float f1 = 0.5F;
      float f2 = 0.0F;
      float f3 = p_229095_3_.getHeight() / f;
      float f4 = 0.0F;
      p_229095_1_.func_227863_a_(Vector3f.field_229181_d_.func_229187_a_(-this.info.getYaw()));
      p_229095_1_.func_227861_a_(0.0D, 0.0D, (double)(-0.3F + (float)((int)f3) * 0.02F));
      float f5 = 0.0F;
      int i = 0;
      IVertexBuilder ivertexbuilder = p_229095_2_.getBuffer(Atlases.func_228783_h_());

      for(MatrixStack.Entry matrixstack$entry = p_229095_1_.func_227866_c_(); f3 > 0.0F; ++i) {
         TextureAtlasSprite textureatlassprite2 = i % 2 == 0 ? textureatlassprite : textureatlassprite1;
         float f6 = textureatlassprite2.getMinU();
         float f7 = textureatlassprite2.getMinV();
         float f8 = textureatlassprite2.getMaxU();
         float f9 = textureatlassprite2.getMaxV();
         if (i / 2 % 2 == 0) {
            float f10 = f8;
            f8 = f6;
            f6 = f10;
         }

         func_229090_a_(matrixstack$entry, ivertexbuilder, f1 - 0.0F, 0.0F - f4, f5, f8, f9);
         func_229090_a_(matrixstack$entry, ivertexbuilder, -f1 - 0.0F, 0.0F - f4, f5, f6, f9);
         func_229090_a_(matrixstack$entry, ivertexbuilder, -f1 - 0.0F, 1.4F - f4, f5, f6, f7);
         func_229090_a_(matrixstack$entry, ivertexbuilder, f1 - 0.0F, 1.4F - f4, f5, f8, f7);
         f3 -= 0.45F;
         f4 -= 0.45F;
         f1 *= 0.9F;
         f5 += 0.03F;
      }

      p_229095_1_.func_227865_b_();
   }

   private static void func_229090_a_(MatrixStack.Entry p_229090_0_, IVertexBuilder p_229090_1_, float p_229090_2_, float p_229090_3_, float p_229090_4_, float p_229090_5_, float p_229090_6_) {
      p_229090_1_.func_227888_a_(p_229090_0_.func_227870_a_(), p_229090_2_, p_229090_3_, p_229090_4_).func_225586_a_(255, 255, 255, 255).func_225583_a_(p_229090_5_, p_229090_6_).func_225585_a_(0, 10).func_227886_a_(240).func_227887_a_(p_229090_0_.func_227872_b_(), 0.0F, 1.0F, 0.0F).endVertex();
   }

   private static void func_229096_a_(MatrixStack p_229096_0_, IRenderTypeBuffer p_229096_1_, Entity p_229096_2_, float p_229096_3_, float p_229096_4_, IWorldReader p_229096_5_, float p_229096_6_) {
      float f = p_229096_6_;
      if (p_229096_2_ instanceof MobEntity) {
         MobEntity mobentity = (MobEntity)p_229096_2_;
         if (mobentity.isChild()) {
            f = p_229096_6_ * 0.5F;
         }
      }

      double d2 = MathHelper.lerp((double)p_229096_4_, p_229096_2_.lastTickPosX, p_229096_2_.getPosX());
      double d0 = MathHelper.lerp((double)p_229096_4_, p_229096_2_.lastTickPosY, p_229096_2_.getPosY());
      double d1 = MathHelper.lerp((double)p_229096_4_, p_229096_2_.lastTickPosZ, p_229096_2_.getPosZ());
      int i = MathHelper.floor(d2 - (double)f);
      int j = MathHelper.floor(d2 + (double)f);
      int k = MathHelper.floor(d0 - (double)f);
      int l = MathHelper.floor(d0);
      int i1 = MathHelper.floor(d1 - (double)f);
      int j1 = MathHelper.floor(d1 + (double)f);
      MatrixStack.Entry matrixstack$entry = p_229096_0_.func_227866_c_();
      IVertexBuilder ivertexbuilder = p_229096_1_.getBuffer(field_229082_e_);

      for(BlockPos blockpos : BlockPos.getAllInBoxMutable(new BlockPos(i, k, i1), new BlockPos(j, l, j1))) {
         func_229092_a_(matrixstack$entry, ivertexbuilder, p_229096_5_, blockpos, d2, d0, d1, f, p_229096_3_);
      }

   }

   private static void func_229092_a_(MatrixStack.Entry p_229092_0_, IVertexBuilder p_229092_1_, IWorldReader p_229092_2_, BlockPos p_229092_3_, double p_229092_4_, double p_229092_6_, double p_229092_8_, float p_229092_10_, float p_229092_11_) {
      BlockPos blockpos = p_229092_3_.down();
      BlockState blockstate = p_229092_2_.getBlockState(blockpos);
      if (blockstate.getRenderType() != BlockRenderType.INVISIBLE && p_229092_2_.getLight(p_229092_3_) > 3) {
         if (blockstate.func_224756_o(p_229092_2_, blockpos)) {
            VoxelShape voxelshape = blockstate.getShape(p_229092_2_, p_229092_3_.down());
            if (!voxelshape.isEmpty()) {
               float f = (float)(((double)p_229092_11_ - (p_229092_6_ - (double)p_229092_3_.getY()) / 2.0D) * 0.5D * (double)p_229092_2_.getBrightness(p_229092_3_));
               if (f >= 0.0F) {
                  if (f > 1.0F) {
                     f = 1.0F;
                  }

                  AxisAlignedBB axisalignedbb = voxelshape.getBoundingBox();
                  double d0 = (double)p_229092_3_.getX() + axisalignedbb.minX;
                  double d1 = (double)p_229092_3_.getX() + axisalignedbb.maxX;
                  double d2 = (double)p_229092_3_.getY() + axisalignedbb.minY;
                  double d3 = (double)p_229092_3_.getZ() + axisalignedbb.minZ;
                  double d4 = (double)p_229092_3_.getZ() + axisalignedbb.maxZ;
                  float f1 = (float)(d0 - p_229092_4_);
                  float f2 = (float)(d1 - p_229092_4_);
                  float f3 = (float)(d2 - p_229092_6_ + 0.015625D);
                  float f4 = (float)(d3 - p_229092_8_);
                  float f5 = (float)(d4 - p_229092_8_);
                  float f6 = -f1 / 2.0F / p_229092_10_ + 0.5F;
                  float f7 = -f2 / 2.0F / p_229092_10_ + 0.5F;
                  float f8 = -f4 / 2.0F / p_229092_10_ + 0.5F;
                  float f9 = -f5 / 2.0F / p_229092_10_ + 0.5F;
                  func_229091_a_(p_229092_0_, p_229092_1_, f, f1, f3, f4, f6, f8);
                  func_229091_a_(p_229092_0_, p_229092_1_, f, f1, f3, f5, f6, f9);
                  func_229091_a_(p_229092_0_, p_229092_1_, f, f2, f3, f5, f7, f9);
                  func_229091_a_(p_229092_0_, p_229092_1_, f, f2, f3, f4, f7, f8);
               }

            }
         }
      }
   }

   private static void func_229091_a_(MatrixStack.Entry p_229091_0_, IVertexBuilder p_229091_1_, float p_229091_2_, float p_229091_3_, float p_229091_4_, float p_229091_5_, float p_229091_6_, float p_229091_7_) {
      p_229091_1_.func_227888_a_(p_229091_0_.func_227870_a_(), p_229091_3_, p_229091_4_, p_229091_5_).func_227885_a_(1.0F, 1.0F, 1.0F, p_229091_2_).func_225583_a_(p_229091_6_, p_229091_7_).func_227891_b_(OverlayTexture.field_229196_a_).func_227886_a_(15728880).func_227887_a_(p_229091_0_.func_227872_b_(), 0.0F, 1.0F, 0.0F).endVertex();
   }

   public void setWorld(@Nullable World worldIn) {
      this.world = worldIn;
      if (worldIn == null) {
         this.info = null;
      }

   }

   public double func_229099_b_(Entity p_229099_1_) {
      return this.info.getProjectedView().squareDistanceTo(p_229099_1_.getPositionVec());
   }

   public double getDistanceToCamera(double x, double y, double z) {
      return this.info.getProjectedView().squareDistanceTo(x, y, z);
   }

   public Quaternion func_229098_b_() {
      return this.field_229083_k_;
   }

   public FontRenderer getFontRenderer() {
      return this.textRenderer;
   }
}
