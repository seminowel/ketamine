package io.github.nevalackin.homoBus.bus.impl;

import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import io.github.nevalackin.homoBus.bus.Bus;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class EventBus implements Bus {
   private final Map callSiteMap = new HashMap();
   private final Map listenerCache = new HashMap();
   private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

   public void subscribe(Object subscriber) {
      Field[] var2 = subscriber.getClass().getDeclaredFields();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         Field field = var2[var4];
         EventLink annotation = (EventLink)field.getAnnotation(EventLink.class);
         if (annotation != null) {
            Type eventType = ((ParameterizedType)((ParameterizedType)field.getGenericType())).getActualTypeArguments()[0];
            if (!field.isAccessible()) {
               field.setAccessible(true);
            }

            try {
               Listener listener = (Listener)LOOKUP.unreflectGetter(field).invokeWithArguments(subscriber);
               byte priority = annotation.value();
               CallSite callSite = new CallSite(subscriber, listener, priority);
               if (this.callSiteMap.containsKey(eventType)) {
                  List callSites = (List)this.callSiteMap.get(eventType);
                  callSites.add(callSite);
                  callSites.sort(Comparator.comparingInt((o) -> {
                     return o.priority;
                  }));
               } else {
                  List callSites = new ArrayList(1);
                  callSites.add(callSite);
                  this.callSiteMap.put(eventType, callSites);
               }
            } catch (Throwable var12) {
            }
         }
      }

      this.populateListenerCache();
   }

   private void populateListenerCache() {
      Map callSiteMap = this.callSiteMap;
      Map listenerCache = this.listenerCache;
      Iterator var3 = callSiteMap.keySet().iterator();

      while(var3.hasNext()) {
         Type type = (Type)var3.next();
         List callSites = (List)callSiteMap.get(type);
         int size = callSites.size();
         List listeners = new ArrayList(size);
         Iterator var8 = callSites.iterator();

         while(var8.hasNext()) {
            CallSite callSite = (CallSite)var8.next();
            listeners.add(callSite.listener);
         }

         listenerCache.put(type, listeners);
      }

   }

   public void unsubscribe(Object subscriber) {
      Iterator var2 = this.callSiteMap.values().iterator();

      while(var2.hasNext()) {
         List callSites = (List)var2.next();
         callSites.removeIf((eventCallSite) -> {
            return eventCallSite.owner == subscriber;
         });
      }

      this.populateListenerCache();
   }

   public void post(Object event) {
      List listeners = (List)this.listenerCache.get(event.getClass());
      if (listeners != null) {
         int listenersSize = listeners.size();

         while(listenersSize > 0) {
            --listenersSize;
            ((Listener)listeners.get(listenersSize)).call(event);
         }

      }
   }

   private static class CallSite {
      private final Object owner;
      private final Listener listener;
      private final byte priority;

      public CallSite(Object owner, Listener listener, byte priority) {
         this.owner = owner;
         this.listener = listener;
         this.priority = priority;
      }
   }
}
