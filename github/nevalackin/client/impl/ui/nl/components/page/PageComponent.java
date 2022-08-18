package io.github.nevalackin.client.impl.ui.nl.components.page;

import io.github.nevalackin.client.api.ui.framework.Animated;
import io.github.nevalackin.client.api.ui.framework.Component;
import io.github.nevalackin.client.impl.ui.nl.components.page.groupBox.GroupBoxComponent;
import io.github.nevalackin.client.util.render.DrawUtil;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;

public abstract class PageComponent extends Component implements Animated {
   private double fadeInProgress;
   public double pageSelectorButtonY;

   public PageComponent(Component parent) {
      super(parent, 0.0, 0.0, 0.0, 0.0);
      this.onInit();
      this.getChildren().sort(Comparator.comparingDouble(Component::getHeight).reversed());
   }

   public void onDraw(ScaledResolution scaledResolution, int mouseX, int mouseY) {
      double x = this.getX();
      double y = this.getY();
      double width = this.getWidth();
      double height = this.getHeight();
      this.fadeInProgress = DrawUtil.animateProgress(this.fadeInProgress, 1.0, 3.0);
      double smooth = DrawUtil.bezierBlendAnimation(this.fadeInProgress);
      GL11.glTranslated(x, this.pageSelectorButtonY, 0.0);
      GL11.glScaled(smooth, smooth, 1.0);
      GL11.glTranslated(-x, -this.pageSelectorButtonY, 0.0);
      double xMargin = width / 30.0;
      double yMargin = height / 30.0;
      double xGap = xMargin / 2.0;
      double yGap = yMargin / 2.0;
      double canvasWidth = width - xMargin * 2.0;
      double canvasHeight = height - yMargin;
      double minGroupBoxSize = 150.0;
      int groupBoxesPerRow = 1;

      for(int i = 5; i >= 1; --i) {
         double groupBoxSize = (canvasWidth - xGap * (double)(i - 1)) / (double)i;
         if (groupBoxSize > 150.0) {
            if (i < groupBoxesPerRow) {
               break;
            }

            groupBoxesPerRow = i;
         }
      }

      List children = this.getChildren();
      int size = children.size();
      groupBoxesPerRow = Math.min(size, groupBoxesPerRow);
      double groupBoxWidth = (canvasWidth - xGap * (double)(groupBoxesPerRow - 1)) / (double)groupBoxesPerRow;
      double[] columns = new double[groupBoxesPerRow];
      Arrays.fill(columns, yMargin);

      for(int j = 0; j < size; ++j) {
         Component child = (Component)children.get(j);
         if (child instanceof GroupBoxComponent) {
            GroupBoxComponent groupBox;
            double groupBoxHeight;
            label42: {
               groupBox = (GroupBoxComponent)child;
               child.setWidth(groupBoxWidth);
               groupBoxHeight = child.getHeight();
               int currColumn = j % groupBoxesPerRow;
               if (j >= groupBoxesPerRow) {
                  double smallest = Double.MAX_VALUE;
                  int smallestColumn = -1;

                  for(int column = 0; column < groupBoxesPerRow; ++column) {
                     double columnHeight = columns[column] + groupBoxHeight;
                     if (columnHeight < smallest) {
                        smallest = columnHeight;
                        smallestColumn = column;
                     }
                  }

                  if (smallestColumn != -1) {
                     child.setX(xMargin + (groupBoxWidth + xGap) * (double)smallestColumn);
                     child.setY(smallest - groupBoxHeight);
                     groupBox.pageColumn = smallestColumn;
                     break label42;
                  }
               }

               child.setX(xMargin + (groupBoxWidth + xGap) * (double)currColumn);
               child.setY(yMargin);
               groupBox.pageColumn = currColumn;
            }

            groupBox.maxHeight = canvasHeight - columns[groupBox.pageColumn];
            int var10001 = groupBox.pageColumn;
            columns[var10001] += groupBoxHeight + yGap;
            child.onDraw(scaledResolution, mouseX, mouseY);
         }
      }

      GL11.glScaled(-smooth, -smooth, 1.0);
   }

   public void resetAnimationState() {
      this.fadeInProgress = 0.0;
      this.resetChildrenAnimations();
   }

   public boolean shouldPlayAnimation() {
      return this.fadeInProgress >= 1.0;
   }

   public void onMouseClick(int mouseX, int mouseY, int button) {
   }

   public abstract void onInit();
}
