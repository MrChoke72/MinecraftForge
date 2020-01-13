package net.minecraft.entity.ai.goal;

import javax.annotation.Nullable;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.brain.BrainUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.SectionPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.server.ServerWorld;

public class MoveTowardsVillageGoal extends RandomWalkingGoal {
   public MoveTowardsVillageGoal(CreatureEntity entity, double speedIn) {
      super(entity, speedIn, 10);
   }

   public boolean shouldExecute() {
      ServerWorld serverworld = (ServerWorld)this.creature.world;
      BlockPos blockpos = new BlockPos(this.creature);
      return serverworld.isPosBelowEQSecLevel1(blockpos) ? false : super.shouldExecute();
   }

   @Nullable
   protected Vec3d getPosition() {
      ServerWorld serverworld = (ServerWorld)this.creature.world;
      BlockPos blockpos = new BlockPos(this.creature);
      SectionPos sectionpos = SectionPos.from(blockpos);

      //AH CHANGE DEBUG OFF
      /*
      if(this.creature.getCustomName() != null && this.creature.getCustomName().getString().equals("Chuck"))
      {
         System.out.println("In MoveTowardsVillageGoal:  secPos=" + sectionpos.toString());
      }
       */

      SectionPos sectionpos1 = BrainUtil.getSecPosLowerInRadius(serverworld, sectionpos, 2);

      //AH CHANGE DEBUG OFF
      /*
      if(this.creature.getCustomName() != null && this.creature.getCustomName().getString().equals("Chuck"))
      {
         System.out.println("In MoveTowardsVillageGoal:  secPos1=" + sectionpos1.toString());
      }
       */
      
      return sectionpos1 != sectionpos ? RandomPositionGenerator.findRandomTargetBlockTowards(this.creature, 10, 7, new Vec3d(sectionpos1.getCenter())) : null;
   }
}
