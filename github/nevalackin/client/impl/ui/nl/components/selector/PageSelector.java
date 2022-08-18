package io.github.nevalackin.client.impl.ui.nl.components.selector;

import io.github.nevalackin.client.api.ui.framework.Component;

public interface PageSelector {
   Component getParent();

   default PageSelector getSelectorParent() {
      return (PageSelector)this.getParent();
   }

   default int getSelectedIdx() {
      return this.getSelectorParent().getSelectedIdx();
   }

   default void onPageSelect(int idx, double y) {
      this.getSelectorParent().onPageSelect(idx, y);
   }
}
