package io.github.nevalackin.client.impl.ui.nl.components;

import io.github.nevalackin.client.api.ui.framework.Component;

public final class PopupWindowComponent extends WindowComponent {
   private final String title;
   private final String body;

   public PopupWindowComponent(Component parent, String title, String body, PopupType type, double x, double y, double width, double height, WindowComponent.Decoration... decorations) {
      super(parent, x, y, width, height, decorations);
      this.title = title;
      this.body = body;
   }

   public static enum PopupType {
      OK,
      YES_NO;
   }
}
