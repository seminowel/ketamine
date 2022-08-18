package net.minecraft.entity.ai;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EntityAITasks {
   private static final Logger logger = LogManager.getLogger();
   private List taskEntries = Lists.newArrayList();
   private List executingTaskEntries = Lists.newArrayList();
   private int tickCount;
   private int tickRate = 3;

   public void addTask(int priority, EntityAIBase task) {
      this.taskEntries.add(new EntityAITaskEntry(priority, task));
   }

   public void removeTask(EntityAIBase task) {
      Iterator iterator = this.taskEntries.iterator();

      while(iterator.hasNext()) {
         EntityAITaskEntry entityaitasks$entityaitaskentry = (EntityAITaskEntry)iterator.next();
         EntityAIBase entityaibase = entityaitasks$entityaitaskentry.action;
         if (entityaibase == task) {
            if (this.executingTaskEntries.contains(entityaitasks$entityaitaskentry)) {
               entityaibase.resetTask();
               this.executingTaskEntries.remove(entityaitasks$entityaitaskentry);
            }

            iterator.remove();
         }
      }

   }

   public void onUpdateTasks() {
      Iterator iterator1;
      EntityAITaskEntry entityaitasks$entityaitaskentry;
      if (this.tickCount++ % this.tickRate == 0) {
         iterator1 = this.taskEntries.iterator();

         label53:
         while(true) {
            while(true) {
               if (!iterator1.hasNext()) {
                  break label53;
               }

               entityaitasks$entityaitaskentry = (EntityAITaskEntry)iterator1.next();
               boolean flag = this.executingTaskEntries.contains(entityaitasks$entityaitaskentry);
               if (!flag) {
                  break;
               }

               if (!this.canUse(entityaitasks$entityaitaskentry) || !this.canContinue(entityaitasks$entityaitaskentry)) {
                  entityaitasks$entityaitaskentry.action.resetTask();
                  this.executingTaskEntries.remove(entityaitasks$entityaitaskentry);
                  break;
               }
            }

            if (this.canUse(entityaitasks$entityaitaskentry) && entityaitasks$entityaitaskentry.action.shouldExecute()) {
               entityaitasks$entityaitaskentry.action.startExecuting();
               this.executingTaskEntries.add(entityaitasks$entityaitaskentry);
            }
         }
      } else {
         iterator1 = this.executingTaskEntries.iterator();

         while(iterator1.hasNext()) {
            entityaitasks$entityaitaskentry = (EntityAITaskEntry)iterator1.next();
            if (!this.canContinue(entityaitasks$entityaitaskentry)) {
               entityaitasks$entityaitaskentry.action.resetTask();
               iterator1.remove();
            }
         }
      }

      iterator1 = this.executingTaskEntries.iterator();

      while(iterator1.hasNext()) {
         entityaitasks$entityaitaskentry = (EntityAITaskEntry)iterator1.next();
         entityaitasks$entityaitaskentry.action.updateTask();
      }

   }

   private boolean canContinue(EntityAITaskEntry taskEntry) {
      boolean flag = taskEntry.action.continueExecuting();
      return flag;
   }

   private boolean canUse(EntityAITaskEntry taskEntry) {
      Iterator var2 = this.taskEntries.iterator();

      while(var2.hasNext()) {
         EntityAITaskEntry entityaitasks$entityaitaskentry = (EntityAITaskEntry)var2.next();
         if (entityaitasks$entityaitaskentry != taskEntry) {
            if (taskEntry.priority >= entityaitasks$entityaitaskentry.priority) {
               if (!this.areTasksCompatible(taskEntry, entityaitasks$entityaitaskentry) && this.executingTaskEntries.contains(entityaitasks$entityaitaskentry)) {
                  return false;
               }
            } else if (!entityaitasks$entityaitaskentry.action.isInterruptible() && this.executingTaskEntries.contains(entityaitasks$entityaitaskentry)) {
               return false;
            }
         }
      }

      return true;
   }

   private boolean areTasksCompatible(EntityAITaskEntry taskEntry1, EntityAITaskEntry taskEntry2) {
      return (taskEntry1.action.getMutexBits() & taskEntry2.action.getMutexBits()) == 0;
   }

   class EntityAITaskEntry {
      public EntityAIBase action;
      public int priority;

      public EntityAITaskEntry(int priorityIn, EntityAIBase task) {
         this.priority = priorityIn;
         this.action = task;
      }
   }
}
