package io.github.nevalackin.client.impl.ui.nl.components;

public enum Theme {
   BLUE(-16734222, -1442640614, -16501939, -16250867, -16377563, -12040120, -16250867, -16377563, -1, -16774378, -16770516, -6640462, -1, -16250867, -14540254, -16734222, -8680548, -16776437, -16181459, -16704725);

   private final int mainColour;
   private final int pageSelectorBackgroundColour;
   private final int pageSelectorSelectedPageColour;
   private final int pageBackgroundColour;
   private final int pageSelectorPageSeparatorColour;
   private final int pageSelectorLabelColour;
   private final int headerBackgroundColour;
   private final int headerPageSeparatorColour;
   private final int watermarkTextColour;
   private final int groupBoxBackgroundColour;
   private final int groupBoxHeaderSeparatorColour;
   private final int textColour;
   private final int highlightedTextColour;
   private final int componentBackgroundColour;
   private final int componentOutlineColour;
   private final int checkBoxEnabledColour;
   private final int checkBoxDisabledColour;
   private final int checkBoxBackgroundDisabledColour;
   private final int checkBoxBackgroundEnabledColour;
   private final int sliderBackgroundColour;

   private Theme(int mainColour, int pageSelectorBackgroundColour, int pageSelectorSelectedPageColour, int pageBackgroundColour, int pageSelectorPageSeparatorColour, int pageSelectorLabelColour, int headerBackgroundColour, int headerPageSeparatorColour, int watermarkTextColour, int groupBoxBackgroundColour, int groupBoxHeaderSeparatorColour, int textColour, int highlightedTextColour, int componentBackgroundColour, int componentOutlineColour, int checkBoxEnabledColour, int checkBoxDisabledColour, int checkBoxBackgroundDisabledColour, int checkBoxBackgroundEnabledColour, int sliderBackgroundColour) {
      this.mainColour = mainColour;
      this.pageSelectorBackgroundColour = pageSelectorBackgroundColour;
      this.pageSelectorSelectedPageColour = pageSelectorSelectedPageColour;
      this.pageBackgroundColour = pageBackgroundColour;
      this.pageSelectorPageSeparatorColour = pageSelectorPageSeparatorColour;
      this.pageSelectorLabelColour = pageSelectorLabelColour;
      this.headerBackgroundColour = headerBackgroundColour;
      this.headerPageSeparatorColour = headerPageSeparatorColour;
      this.watermarkTextColour = watermarkTextColour;
      this.groupBoxBackgroundColour = groupBoxBackgroundColour;
      this.groupBoxHeaderSeparatorColour = groupBoxHeaderSeparatorColour;
      this.textColour = textColour;
      this.highlightedTextColour = highlightedTextColour;
      this.componentBackgroundColour = componentBackgroundColour;
      this.componentOutlineColour = componentOutlineColour;
      this.checkBoxEnabledColour = checkBoxEnabledColour;
      this.checkBoxDisabledColour = checkBoxDisabledColour;
      this.checkBoxBackgroundDisabledColour = checkBoxBackgroundDisabledColour;
      this.checkBoxBackgroundEnabledColour = checkBoxBackgroundEnabledColour;
      this.sliderBackgroundColour = sliderBackgroundColour;
   }

   public int getSliderBackgroundColour() {
      return this.sliderBackgroundColour;
   }

   public int getMainColour() {
      return this.mainColour;
   }

   public int getPageSelectorBackgroundColour() {
      return this.pageSelectorBackgroundColour;
   }

   public int getPageSelectorSelectedPageColour() {
      return this.pageSelectorSelectedPageColour;
   }

   public int getPageBackgroundColour() {
      return this.pageBackgroundColour;
   }

   public int getPageSelectorLabelColour() {
      return this.pageSelectorLabelColour;
   }

   public int getPageSelectorPageSeparatorColour() {
      return this.pageSelectorPageSeparatorColour;
   }

   public int getHeaderBackgroundColour() {
      return this.headerBackgroundColour;
   }

   public int getHeaderPageSeparatorColour() {
      return this.headerPageSeparatorColour;
   }

   public int getWatermarkTextColour() {
      return this.watermarkTextColour;
   }

   public int getGroupBoxBackgroundColour() {
      return this.groupBoxBackgroundColour;
   }

   public int getGroupBoxHeaderSeparatorColour() {
      return this.groupBoxHeaderSeparatorColour;
   }

   public int getTextColour() {
      return this.textColour;
   }

   public int getHighlightedTextColour() {
      return this.highlightedTextColour;
   }

   public int getComponentBackgroundColour() {
      return this.componentBackgroundColour;
   }

   public int getComponentOutlineColour() {
      return this.componentOutlineColour;
   }

   public int getCheckBoxEnabledColour() {
      return this.checkBoxEnabledColour;
   }

   public int getCheckBoxDisabledColour() {
      return this.checkBoxDisabledColour;
   }

   public int getCheckBoxBackgroundDisabledColour() {
      return this.checkBoxBackgroundDisabledColour;
   }

   public int getCheckBoxBackgroundEnabledColour() {
      return this.checkBoxBackgroundEnabledColour;
   }
}
