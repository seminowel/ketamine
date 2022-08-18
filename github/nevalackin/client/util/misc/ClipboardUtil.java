package io.github.nevalackin.client.util.misc;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;

public final class ClipboardUtil {
   private ClipboardUtil() {
   }

   public static void setClipboardContents(String data) {
      StringSelection selection = new StringSelection(data);
      Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      clipboard.setContents(selection, selection);
   }

   public static String getClipboardContents() {
      Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

      try {
         return (String)clipboard.getData(DataFlavor.stringFlavor);
      } catch (Exception var2) {
         return null;
      }
   }
}
