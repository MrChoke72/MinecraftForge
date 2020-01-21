package net.minecraft.util;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Stream;

public class WeightedList<U> {
   protected final List<WeightedList<U>.Entry<? extends U>> entryList = Lists.newArrayList();
   private final Random random;

   public WeightedList(Random random) {
      this.random = random;
   }

   public WeightedList() {
      this(new Random());
   }

   public <T> WeightedList(Dynamic<T> p_i225709_1_, Function<Dynamic<T>, U> p_i225709_2_) {
      this();
      p_i225709_1_.asStream().forEach((p_226316_2_) -> {
         p_226316_2_.get("data").map((p_226317_3_) -> {
            U u = p_i225709_2_.apply(p_226317_3_);
            int i = p_226316_2_.get("weight").asInt(1);
            return (U)this.addToList(u, i);
         });
      });
   }

   public <T> T func_226310_a_(DynamicOps<T> p_226310_1_, Function<U, Dynamic<T>> p_226310_2_) {
      return p_226310_1_.createList(this.entryStream().map((p_226311_2_) -> {
         return p_226310_1_.createMap(ImmutableMap.<T, T>builder().put(p_226310_1_.createString("data"), p_226310_2_.apply((U)p_226311_2_.getEnt()).getValue()).put(p_226310_1_.createString("weight"), p_226310_1_.createInt(p_226311_2_.getPower())).build());
      }));
   }

   public WeightedList<U> addToList(U u, int power) {
      this.entryList.add(new WeightedList.Entry(u, power));
      return this;
   }

   public WeightedList<U> getWeighedList() {
      return this.getWeighedList(this.random);
   }

   public WeightedList<U> getWeighedList(Random random) {
      this.entryList.forEach((entry) -> {
         entry.setWeight(random.nextFloat());
      });
      this.entryList.sort(Comparator.comparingDouble((entry) -> {
         return entry.getWeight();
      }));
      return this;
   }

   public Stream<? extends U> entStream() {
      return this.entryList.stream().map(WeightedList.Entry::getEnt);
   }

   public Stream<WeightedList<U>.Entry<? extends U>> entryStream() {
      return this.entryList.stream();
   }

   public U func_226318_b_(Random random) {
      return (U)this.getWeighedList(random).entStream().findFirst().orElseThrow(RuntimeException::new);
   }

   public String toString() {
      return "WeightedList[" + this.entryList + "]";
   }

   public class Entry<T> {
      private final T ent;
      private final int power;
      private double weight;

      private Entry(T t, int power) {
         this.power = power;
         this.ent = t;
      }

      private double getWeight() {
         return this.weight;
      }

      private void setWeight(float base) {
         this.weight = -Math.pow((double)base, (double)(1.0F / (float)this.power));
      }

      public T getEnt() {
         return this.ent;
      }

      public int getPower() {
         return this.power;
      }

      public String toString() {
         return "" + this.power + ":" + this.ent;
      }
   }
}
