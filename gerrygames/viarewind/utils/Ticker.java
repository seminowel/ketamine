package de.gerrygames.viarewind.utils;

import com.viaversion.viaversion.api.Via;
import java.util.Objects;
import java.util.stream.Stream;

public class Ticker {
   private static boolean init = false;

   public static void init() {
      if (!init) {
         Class var0 = Ticker.class;
         synchronized(Ticker.class) {
            if (init) {
               return;
            }

            init = true;
         }

         Via.getPlatform().runRepeatingSync(() -> {
            Via.getManager().getConnectionManager().getConnections().forEach((user) -> {
               Stream var10000 = user.getStoredObjects().values().stream();
               Objects.requireNonNull(Tickable.class);
               var10000 = var10000.filter(Tickable.class::isInstance);
               Objects.requireNonNull(Tickable.class);
               var10000.map(Tickable.class::cast).forEach(Tickable::tick);
            });
         }, 1L);
      }
   }
}
