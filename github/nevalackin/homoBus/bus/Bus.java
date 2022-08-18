package io.github.nevalackin.homoBus.bus;

public interface Bus {
   void subscribe(Object var1);

   void unsubscribe(Object var1);

   void post(Object var1);
}
