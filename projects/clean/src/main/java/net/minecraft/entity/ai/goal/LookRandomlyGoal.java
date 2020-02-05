package net.minecraft.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.entity.MobEntity;

public class LookRandomlyGoal extends Goal {
   //AH CHANGE REFACTOR
   private final MobEntity idleEntity;
   //private final MobEntity field_75258_a;

   private double lookX;
   private double lookZ;
   private int idleTime;

   public LookRandomlyGoal(MobEntity entitylivingIn) {
      this.idleEntity = entitylivingIn;
      this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
   }

   public boolean shouldExecute() {
      return this.idleEntity.getRNG().nextFloat() < 0.02F;
   }

   public boolean shouldContinueExecuting() {
      return this.idleTime >= 0;
   }

   public void startExecuting() {
      double d0 = (Math.PI * 2D) * this.idleEntity.getRNG().nextDouble();
      this.lookX = Math.cos(d0);
      this.lookZ = Math.sin(d0);
      this.idleTime = 20 + this.idleEntity.getRNG().nextInt(20);
   }

   public void tick() {

      //AH CHANGE DEBUG OFF
      /*
      if(this.entity instanceof HuskEntity && this.entity.getCustomName() != null) // && this.entity.getCustomName().getString().equals("Chuck"))
      {
         System.out.println("in LookRandomlyGoal tick.  idleTime=" + idleTime);
      }
       */

      --this.idleTime;
      this.idleEntity.getLookController().setLookPosition(this.idleEntity.getPosX() + this.lookX, this.idleEntity.getPosYEye(), this.idleEntity.getPosZ() + this.lookZ);
   }
}
