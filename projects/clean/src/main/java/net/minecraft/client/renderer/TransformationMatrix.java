package net.minecraft.client.renderer;

import com.mojang.datafixers.util.Pair;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.util.Util;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.tuple.Triple;

@OnlyIn(Dist.CLIENT)
public final class TransformationMatrix {
   private final Matrix4f matrix;
   private boolean decomposed;
   @Nullable
   private Vector3f translation;
   @Nullable
   private Quaternion rotationLeft;
   @Nullable
   private Vector3f scale;
   @Nullable
   private Quaternion rotationRight;
   private static final TransformationMatrix IDENTITY = Util.make(() -> {
      Matrix4f matrix4f = new Matrix4f();
      matrix4f.identity();
      TransformationMatrix transformationmatrix = new TransformationMatrix(matrix4f);
      transformationmatrix.getRotationLeft();
      return transformationmatrix;
   });

   public TransformationMatrix(@Nullable Matrix4f matrixIn) {
      if (matrixIn == null) {
         this.matrix = IDENTITY.matrix;
      } else {
         this.matrix = matrixIn;
      }

   }

   public TransformationMatrix(@Nullable Vector3f translationIn, @Nullable Quaternion rotationLeftIn, @Nullable Vector3f scaleIn, @Nullable Quaternion rotationRightIn) {
      this.matrix = compose(translationIn, rotationLeftIn, scaleIn, rotationRightIn);
      this.translation = translationIn != null ? translationIn : new Vector3f();
      this.rotationLeft = rotationLeftIn != null ? rotationLeftIn : Quaternion.ONE.copy();
      this.scale = scaleIn != null ? scaleIn : new Vector3f(1.0F, 1.0F, 1.0F);
      this.rotationRight = rotationRightIn != null ? rotationRightIn : Quaternion.ONE.copy();
      this.decomposed = true;
   }

   public static TransformationMatrix identity() {
      return IDENTITY;
   }

   public TransformationMatrix compose(TransformationMatrix matrixIn) {
      Matrix4f matrix4f = this.getMatrix();
      matrix4f.multiply(matrixIn.getMatrix());
      return new TransformationMatrix(matrix4f);
   }

   @Nullable
   public TransformationMatrix inverse() {
      if (this == IDENTITY) {
         return this;
      } else {
         Matrix4f matrix4f = this.getMatrix();
         return matrix4f.invert() ? new TransformationMatrix(matrix4f) : null;
      }
   }

   private void decompose() {
      if (!this.decomposed) {
         Pair<Matrix3f, Vector3f> pair = affine(this.matrix);
         Triple<Quaternion, Vector3f, Quaternion> triple = pair.getFirst().svdDecompose();
         this.translation = pair.getSecond();
         this.rotationLeft = triple.getLeft();
         this.scale = triple.getMiddle();
         this.rotationRight = triple.getRight();
         this.decomposed = true;
      }

   }

   private static Matrix4f compose(@Nullable Vector3f p_227986_0_, @Nullable Quaternion p_227986_1_, @Nullable Vector3f p_227986_2_, @Nullable Quaternion p_227986_3_) {
      Matrix4f matrix4f = new Matrix4f();
      matrix4f.identity();
      if (p_227986_1_ != null) {
         matrix4f.multiply(new Matrix4f(p_227986_1_));
      }

      if (p_227986_2_ != null) {
         matrix4f.multiply(Matrix4f.makeScale(p_227986_2_.getX(), p_227986_2_.getY(), p_227986_2_.getZ()));
      }

      if (p_227986_3_ != null) {
         matrix4f.multiply(new Matrix4f(p_227986_3_));
      }

      if (p_227986_0_ != null) {
         matrix4f.m30 = p_227986_0_.getX();
         matrix4f.m31 = p_227986_0_.getY();
         matrix4f.m32 = p_227986_0_.getZ();
      }

      return matrix4f;
   }

   public static Pair<Matrix3f, Vector3f> affine(Matrix4f matrixIn) {
      matrixIn.mul(1.0F / matrixIn.m33);
      Vector3f vector3f = new Vector3f(matrixIn.m30, matrixIn.m31, matrixIn.m32);
      Matrix3f matrix3f = new Matrix3f(matrixIn);
      return Pair.of(matrix3f, vector3f);
   }

   public Matrix4f getMatrix() {
      return this.matrix.copy();
   }

   public Quaternion getRotationLeft() {
      this.decompose();
      return this.rotationLeft.copy();
   }

   public boolean equals(Object p_equals_1_) {
      if (this == p_equals_1_) {
         return true;
      } else if (p_equals_1_ != null && this.getClass() == p_equals_1_.getClass()) {
         TransformationMatrix transformationmatrix = (TransformationMatrix)p_equals_1_;
         return Objects.equals(this.matrix, transformationmatrix.matrix);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(this.matrix);
   }
}
