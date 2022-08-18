package de.gerrygames.viarewind.storage;

public class BlockState {
   public static int extractId(int raw) {
      return raw >> 4;
   }

   public static int extractData(int raw) {
      return raw & 15;
   }

   public static int stateToRaw(int id, int data) {
      return id << 4 | data & 15;
   }
}
