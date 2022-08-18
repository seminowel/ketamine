package io.github.nevalackin.client.impl.ui.hud.rendered.graphicbox;

import java.util.function.Function;

public class GraphicBoxField {
   public final String title;
   public final Function valueFunc;
   public final Function valueColorFunc;

   public GraphicBoxField(String title, Function valueFunc, Function valueColorFunc) {
      this.title = title;
      this.valueFunc = valueFunc;
      this.valueColorFunc = valueColorFunc;
   }
}
