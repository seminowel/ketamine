package io.github.nevalackin.client.api.ui.framework;

import java.util.Iterator;
import java.util.List;

public interface Animated {
   Component getParent();

   List getChildren();

   void resetAnimationState();

   default boolean shouldPlayAnimation() {
      Component parent = this.getParent();
      if (parent instanceof Animated) {
         Animated animated = (Animated)parent;
         return animated.shouldPlayAnimation();
      } else {
         return true;
      }
   }

   default void resetChildrenAnimations() {
      Iterator var1 = this.getChildren().iterator();

      while(var1.hasNext()) {
         Component component = (Component)var1.next();
         if (component instanceof Animated) {
            Animated animated = (Animated)component;
            animated.resetAnimationState();
         }
      }

   }
}
