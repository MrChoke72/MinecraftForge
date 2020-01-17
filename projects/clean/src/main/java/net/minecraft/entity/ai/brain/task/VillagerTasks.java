package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.village.PointOfInterestType;

public class VillagerTasks {

   //AH REFACTOR
   public static ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntity>>> core(VillagerProfession profession, float moveSpeed) {
   //public static ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntity>>> core(VillagerProfession p_220638_0_, float p_220638_1_) {
      return ImmutableList.of(
              Pair.of(0, new SwimTask(0.4F, 0.8F)),
              Pair.of(0, new InteractWithDoorTask()),

              //AH ADDED
              Pair.of(0, new InteractWithTrapDoorTask()),

              Pair.of(0, new LookTask(45, 90)),
              Pair.of(0, new PanicTask()),
              Pair.of(0, new WakeUpTask()),
              Pair.of(0, new HideFromRaidOnBellRingTask()),
              Pair.of(0, new BeginRaidTask()),
              Pair.of(1, new WalkToTargetTask(200)),
              Pair.of(2, new TradeTask(moveSpeed)),
              Pair.of(5, new PickupFoodTask()),
              Pair.of(10, new GatherPOITask(profession.getPointOfInterest(), MemoryModuleType.JOB_SITE, true)),
              Pair.of(10, new GatherPOITask(PointOfInterestType.HOME, MemoryModuleType.HOME, false)),
              Pair.of(10, new GatherPOITask(PointOfInterestType.MEETING, MemoryModuleType.MEETING_POINT, true)),
              Pair.of(10, new AssignProfessionTask()), Pair.of(10, new ChangeJobTask()));
   }

   public static ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntity>>> work(VillagerProfession profession, float moveSpeed) {
      return ImmutableList.of(
              lookTasks(),
              Pair.of(5, new FirstShuffledTask<>(ImmutableList.of(
                      Pair.of(new SpawnGolemTask(), 7),
                      Pair.of(new WorkTask(MemoryModuleType.JOB_SITE, 4), 2),
                      Pair.of(new WalkTowardsPosTask(MemoryModuleType.JOB_SITE, 1, 10), 5),
                      Pair.of(new WalkTowardsRandomSecondaryPosTask(MemoryModuleType.SECONDARY_JOB_SITE, 0.4F, 1, 6, MemoryModuleType.JOB_SITE), 5),
                      Pair.of(new FarmTask(), profession == VillagerProfession.FARMER ? 2 : 5)))
              ),
              Pair.of(10, new ShowWaresTask(400, 1600)),
              Pair.of(10, new FindInteractionAndLookTargetTask(EntityType.PLAYER, 4)),
              Pair.of(2, new StayNearPointTask(MemoryModuleType.JOB_SITE, moveSpeed, 9, 100, 1200)),
              Pair.of(3, new GiveHeroGiftsTask(100)),
              Pair.of(3, new ExpirePOITask(profession.getPointOfInterest(), MemoryModuleType.JOB_SITE)),
              Pair.of(99, new UpdateActivityTask()));
   }

   public static ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntity>>> play(float p_220645_0_) {
      return ImmutableList.of(
              Pair.of(0, new WalkToTargetTask(100)),
              lookAtEntityTasks(),
              Pair.of(5, new WalkToVillagerBabiesTask()),
              Pair.of(5, new FirstShuffledTask<>(
                      ImmutableMap.of(
                              MemoryModuleType.VISIBLE_VILLAGER_BABIES, MemoryModuleStatus.VALUE_ABSENT),
                      ImmutableList.of(
                              Pair.of(InteractWithEntityTask.func_220445_a(EntityType.VILLAGER, 8, MemoryModuleType.INTERACTION_TARGET, p_220645_0_, 2), 2),
                              Pair.of(InteractWithEntityTask.func_220445_a(EntityType.CAT, 8, MemoryModuleType.INTERACTION_TARGET, p_220645_0_, 2), 1),
                              Pair.of(new FindWalkTargetTask(p_220645_0_), 1),
                              Pair.of(new WalkTowardsLookTargetTask(p_220645_0_, 2), 1),
                              Pair.of(new JumpOnBedTask(p_220645_0_), 2),
                              Pair.of(new DummyTask(20, 40), 2)))),
              Pair.of(99, new UpdateActivityTask()));
   }

   //AH REFACTOR
   public static ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntity>>> rest(VillagerProfession profession, float moveSpeed) {
   //public static ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntity>>> rest(VillagerProfession p_220635_0_, float p_220635_1_) {
      return ImmutableList.of(
              Pair.of(2, new StayNearPointTask(MemoryModuleType.HOME, moveSpeed, 1, 150, 1200)),
              Pair.of(3, new ExpirePOITask(PointOfInterestType.HOME, MemoryModuleType.HOME)),
              Pair.of(3, new SleepAtHomeTask()),
              Pair.of(5, new FirstShuffledTask<>(
                      ImmutableMap.of(MemoryModuleType.HOME, MemoryModuleStatus.VALUE_ABSENT),
                      ImmutableList.of(
                           Pair.of(new WalkToHouseTask(moveSpeed), 1),
                           Pair.of(new WalkRandomlyTask(moveSpeed), 4),
                           Pair.of(new WalkToPOITask(moveSpeed, 4), 2),
                           Pair.of(new DummyTask(20, 40), 2)))
              ),
              lookTasks(),
              Pair.of(99, new UpdateActivityTask()));
   }

   public static ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntity>>> meet(VillagerProfession p_220637_0_, float moveSpeed) {
      return ImmutableList.of(
              Pair.of(2, new FirstShuffledTask<>(ImmutableList.of(
                      Pair.of(new WorkTask(MemoryModuleType.MEETING_POINT, 40), 2),
                      Pair.of(new CongregateTask(), 2)))),
              Pair.of(10, new ShowWaresTask(400, 1600)),
              Pair.of(10, new FindInteractionAndLookTargetTask(EntityType.PLAYER, 4)),
              Pair.of(2, new StayNearPointTask(MemoryModuleType.MEETING_POINT, moveSpeed, 6, 100, 200)),
              Pair.of(3, new GiveHeroGiftsTask(100)),
              Pair.of(3, new ExpirePOITask(PointOfInterestType.MEETING, MemoryModuleType.MEETING_POINT)),
              Pair.of(3, new MultiTask<>(
                      ImmutableMap.of(),
                      ImmutableSet.of(MemoryModuleType.INTERACTION_TARGET),
                      MultiTask.Ordering.ORDERED,
                      MultiTask.RunType.RUN_ONE,
                      ImmutableList.of(
                              Pair.of(new ShareItemsTask(), 1)))),
              lookAtEntityTasks(),
              Pair.of(99, new UpdateActivityTask()));
   }

   public static ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntity>>> idle(VillagerProfession profession, float moveSpeed) {
      return ImmutableList.of(
              Pair.of(2, new FirstShuffledTask<>(
                      ImmutableList.of(
                              Pair.of(InteractWithEntityTask.func_220445_a(EntityType.VILLAGER, 8, MemoryModuleType.INTERACTION_TARGET, moveSpeed, 2), 2),
                              Pair.of(new InteractWithEntityTask<>(EntityType.VILLAGER, 8, VillagerEntity::canBreed, VillagerEntity::canBreed, MemoryModuleType.BREED_TARGET, moveSpeed, 2), 1),
                              Pair.of(InteractWithEntityTask.func_220445_a(EntityType.CAT, 8, MemoryModuleType.INTERACTION_TARGET, moveSpeed, 2), 1),
                              Pair.of(new FindWalkTargetTask(moveSpeed), 1), Pair.of(new WalkTowardsLookTargetTask(moveSpeed, 2), 1),
                              Pair.of(new JumpOnBedTask(moveSpeed), 1),
                              Pair.of(new DummyTask(30, 60), 1)))),
              Pair.of(3, new GiveHeroGiftsTask(100)),
              Pair.of(3, new FindInteractionAndLookTargetTask(EntityType.PLAYER, 4)),
              Pair.of(3, new ShowWaresTask(400, 1600)),
              Pair.of(3, new MultiTask<>(
                      ImmutableMap.of(),
                      ImmutableSet.of(MemoryModuleType.INTERACTION_TARGET),
                      MultiTask.Ordering.ORDERED,
                      MultiTask.RunType.RUN_ONE,
                      ImmutableList.of(
                              Pair.of(new ShareItemsTask(), 1))
                      )),
              Pair.of(3, new MultiTask<>(
                      ImmutableMap.of(),
                      ImmutableSet.of(MemoryModuleType.BREED_TARGET),
                      MultiTask.Ordering.ORDERED,
                      MultiTask.RunType.RUN_ONE,
                      ImmutableList.of(
                              Pair.of(new CreateBabyVillagerTask(), 1)
                      ))),
              lookAtEntityTasks(),
              Pair.of(99, new UpdateActivityTask())
      );
   }

   public static ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntity>>> panic(VillagerProfession profession, float moveSpeed) {

      //AH CHANGE, make villagers a little harder to catch!.  Default is 1.5 x speed
      float f = moveSpeed * 1.75F;
      //float f = moveSpeed * 1.5F;

      return ImmutableList.of(
              Pair.of(0, new ClearHurtTask()),
              Pair.of(1, new FleeTask(MemoryModuleType.NEAREST_HOSTILE, f)),
              Pair.of(1, new FleeTask(MemoryModuleType.HURT_BY_ENTITY, f)),
              Pair.of(3, new FindWalkTargetTask(f, 2, 2)),
              lookTasks());
   }

   public static ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntity>>> preRaid(VillagerProfession profession, float moveSpeed) {
      return ImmutableList.of(
              Pair.of(0, new RingBellTask()),
              Pair.of(0, new FirstShuffledTask<>(
                      ImmutableList.of(
                              Pair.of(new StayNearPointTask(MemoryModuleType.MEETING_POINT, moveSpeed * 1.5F, 2, 150, 200), 6),
                              Pair.of(new FindWalkTargetTask(moveSpeed * 1.5F), 2)))),
              lookTasks(), Pair.of(99, new ForgetRaidTask()));
   }

   public static ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntity>>> raid(VillagerProfession profession, float moveSpeed) {
      return ImmutableList.of(Pair.of(0, new FirstShuffledTask<>(ImmutableList.of(Pair.of(new GoOutsideAfterRaidTask(moveSpeed), 5), Pair.of(new FindWalkTargetAfterRaidVictoryTask(moveSpeed * 1.1F), 2)))), Pair.of(0, new CelebrateRaidVictoryTask(600, 600)), Pair.of(2, new FindHidingPlaceDuringRaidTask(24, moveSpeed * 1.4F)), lookTasks(), Pair.of(99, new ForgetRaidTask()));
   }

   public static ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntity>>> hide(VillagerProfession profession, float moveSpeed) {
      return ImmutableList.of(
              Pair.of(0, new ExpireHidingTask(15, 2)),
              Pair.of(1, new FindHidingPlaceTask(32, moveSpeed * 1.25F, 2)),
              lookTasks());
   }

   private static Pair<Integer, Task<LivingEntity>> lookAtEntityTasks() {
      return Pair.of(5, new FirstShuffledTask<>(
              ImmutableList.of(
                      Pair.of(new LookAtEntityTask(EntityType.CAT, 8.0F), 8),
                      Pair.of(new LookAtEntityTask(EntityType.VILLAGER, 8.0F), 2),
                      Pair.of(new LookAtEntityTask(EntityType.PLAYER, 8.0F), 2),
                      Pair.of(new LookAtEntityTask(EntityClassification.CREATURE, 8.0F), 1),
                      Pair.of(new LookAtEntityTask(EntityClassification.WATER_CREATURE, 8.0F), 1),
                      Pair.of(new LookAtEntityTask(EntityClassification.MONSTER, 8.0F), 1),
                      Pair.of(new DummyTask(30, 60), 2))));
   }

   //AH REFACTOR
   private static Pair<Integer, Task<LivingEntity>> lookTasks() {
   //private static Pair<Integer, Task<LivingEntity>> func_220646_b() {
      return Pair.of(5, new FirstShuffledTask<>(ImmutableList.of(
                  Pair.of(new LookAtEntityTask(EntityType.VILLAGER, 8.0F), 2),
                  Pair.of(new LookAtEntityTask(EntityType.PLAYER, 8.0F), 2),
                  Pair.of(new DummyTask(30, 60), 8))));
   }
}
