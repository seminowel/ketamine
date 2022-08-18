package io.github.nevalackin.client.impl.event.render.item;

import io.github.nevalackin.client.api.event.Event;

public class ApplyBlockTransformationEvent implements Event {
   private final TransformItem originalTransform;
   private TransformItem replacementTransform;

   public ApplyBlockTransformationEvent(TransformItem originalTransform) {
      this.originalTransform = originalTransform;
      this.replacementTransform = originalTransform;
   }

   public TransformItem getOriginalTransform() {
      return this.originalTransform;
   }

   public TransformItem getReplacementTransform() {
      return this.replacementTransform;
   }

   public void setReplacementTransform(TransformItem replacementTransform) {
      this.replacementTransform = replacementTransform;
   }

   @FunctionalInterface
   public interface TransformItem {
      void transform(float var1, float var2);
   }
}
