package io.github.nevalackin.client.impl.ui.nl.components;

import io.github.nevalackin.client.api.ui.framework.Animated;
import io.github.nevalackin.client.api.ui.framework.Component;

public class WindowComponent extends Component implements Animated {
   private boolean dragging;
   private double prevX;
   private double prevY;
   protected double fadeInProgress;
   private final Decoration[] decorations;
   private boolean draggable;
   private boolean resizable;

   public WindowComponent(Component parent, double x, double y, double width, double height, Decoration... decorations) {
      super(parent, x, y, width, height);
      this.decorations = decorations;
   }

   protected void onHandleDragging(double mouseX, double mouseY) {
      if (!this.isDraggable()) {
         this.dragging = false;
      } else {
         if (this.dragging) {
            this.setX(mouseX - this.prevX);
            this.setY(mouseY - this.prevY);
         }

      }
   }

   protected void onStopDragging() {
      if (this.dragging) {
         this.dragging = false;
      }

   }

   protected void onStartDragging(double mouseX, double mouseY) {
      if (this.isDraggable() && !this.dragging) {
         this.dragging = true;
         this.prevX = mouseX - this.getX();
         this.prevY = mouseY - this.getY();
      }

   }

   public Decoration[] getDecorations() {
      return this.decorations;
   }

   public boolean isDraggable() {
      return this.draggable;
   }

   public void setDraggable(boolean draggable) {
      this.draggable = draggable;
   }

   public boolean isResizable() {
      return this.resizable;
   }

   public void setResizable(boolean resizable) {
      this.resizable = resizable;
   }

   public void resetAnimationState() {
      this.fadeInProgress = 0.0;
   }

   public static enum Decoration {
      CLOSE,
      DOCK;
   }
}
