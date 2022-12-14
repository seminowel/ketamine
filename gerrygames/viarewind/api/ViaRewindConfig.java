package de.gerrygames.viarewind.api;

public interface ViaRewindConfig {
   CooldownIndicator getCooldownIndicator();

   boolean isReplaceAdventureMode();

   boolean isReplaceParticles();

   int getMaxBookPages();

   int getMaxBookPageSize();

   public static enum CooldownIndicator {
      TITLE,
      ACTION_BAR,
      BOSS_BAR,
      DISABLED;
   }
}
