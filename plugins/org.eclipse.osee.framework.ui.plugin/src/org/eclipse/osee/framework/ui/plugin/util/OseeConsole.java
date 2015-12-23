/*******************************************************************************
 * Copyright (c) 2004, 2007 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.ui.plugin.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import org.eclipse.osee.framework.jdk.core.util.IConsoleInputListener;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.ui.plugin.internal.UiPluginConstants;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.swt.SWT;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleOutputStream;

/**
 * Creates an Eclipse Console instance and allows writing output, errors and prompt messages There should be at one
 * instance of a ConsoleWriter per plugin that needs to output messages and errors. *
 * 
 * @author Donald G. Dunne
 */
public class OseeConsole {
   private IOConsoleOutputStream streamOut = null;

   private IOConsoleOutputStream streamErr = null;

   private IOConsoleOutputStream streamPrompt = null;

   private final IOConsole console;

   private final HandleInput inputHandler;

   private final boolean time;

   private final Thread thread;

   private final boolean newline;

   public OseeConsole(String title) {
      this(title, true, true);
   }

   public OseeConsole(String title, boolean time, boolean newline) {
      console = new IOConsole(title, null);
      this.time = time;
      this.newline = newline;
      this.inputHandler = new HandleInput();

      thread = new Thread(inputHandler);
      thread.setName("Osee console input handler");
      ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] {console});
      Displays.ensureInDisplayThread(new Runnable() {
         @Override
         public void run() {
            streamOut = console.newOutputStream();// newMessageStream();
            streamOut.setColor(Displays.getSystemColor(SWT.COLOR_BLACK));
            streamOut.setActivateOnWrite(false);
            streamErr = console.newOutputStream();
            streamErr.setColor(Displays.getSystemColor(SWT.COLOR_RED));
            streamErr.setActivateOnWrite(false);
            streamPrompt = console.newOutputStream();
            streamPrompt.setColor(Displays.getSystemColor(SWT.COLOR_BLUE));
            streamPrompt.setActivateOnWrite(false);
         }
      });
      thread.start();
   }

   public PrintStream getPrintStream() {
      return new PrintStream(streamOut);
   }

   public void shutdown() {
      thread.interrupt();
      try {
         thread.join(5000);
      } catch (InterruptedException e) {
         e.printStackTrace();
      } finally {
         ConsolePlugin.getDefault().getConsoleManager().removeConsoles(new IConsole[] {console});
      }
   }

   public static final int CONSOLE_ERROR = 0;

   public static final int CONSOLE_OUT = 1;

   public static final int CONSOLE_PROMPT = 2;

   /**
    * Writes string to console without popping console forward
    */
   public void write(String str) {
      write(str, false);
   }

   /**
    * Writes string to console without popping console forward
    */
   public void writeError(String str) {
      write(str, CONSOLE_ERROR, true);
   }

   /**
    * Writes string to console
    * 
    * @param popup bring console window forward
    */
   public void write(String str, boolean popup) {
      write(str, CONSOLE_OUT, popup);
   }

   /**
    * Write string to console
    * 
    * @param type CONSOLE_ERROR, CONSOLE_OUT, CONSOLE_PROMPT
    */
   public void write(String str, int type) {
      write(str, type, false);
   }

   /**
    * Write string to console
    * 
    * @param type CONSOLE_ERROR, CONSOLE_OUT, CONSOLE_PROMPT
    * @param popup bring console window forward
    */
   public void write(String str, int type, boolean popup) {
      String time = "";
      if (this.time) {
         Calendar cal = Calendar.getInstance();
         cal.setTime(new Date());

         if (cal.get(Calendar.HOUR) == 0) {
            time = "12";
         } else {
            time = "" + cal.get(Calendar.HOUR);
         }
         time = Lib.padLeading(time, '0', 2);
         String minute = "" + cal.get(Calendar.MINUTE);
         minute = Lib.padLeading(minute, '0', 2);
         time += ":" + minute + " => ";
      }
      try {
         sendToStreams(type, time);
         if (str.length() > 100000) {
            int i = 0;

            while (i < str.length()) {
               int endIndex = i + 100000;
               endIndex = endIndex > str.length() ? str.length() : endIndex;
               String chunk = str.substring(i, endIndex);
               sendToStreams(type, chunk);
               i = endIndex;
            }
         } else {
            sendToStreams(type, str);
         }
         if (newline) {
            sendToStreams(type, "\n");
         }
         if (popup) {
            popup();
         }
      } catch (IOException ex) {
         ex.printStackTrace();
      }
   }

   public void prompt(String str) throws IOException {
      sendToStreams(CONSOLE_PROMPT, str);
   }

   private void sendToStreams(int type, String str) throws IOException {
      if (type == CONSOLE_ERROR && streamErr != null) {
         streamErr.write(str);
      }
      if (type == CONSOLE_PROMPT && streamPrompt != null) {
         streamPrompt.write(str);
      }
      if (type == CONSOLE_OUT && streamOut != null) {
         streamOut.write(str);
      }
   }

   public void popup() {
      ConsolePlugin.getDefault().getConsoleManager().showConsoleView(console);
   }

   public void addInputListener(IConsoleInputListener listener) {
      inputHandler.addListener(listener);
   }

   public void removeInputListener(IConsoleInputListener listener) {
      inputHandler.removeListener(listener);
   }

   private class HandleInput implements Runnable {

      private final CopyOnWriteArrayList<IConsoleInputListener> listeners;

      public HandleInput() {
         listeners = new CopyOnWriteArrayList<>();
      }

      public void addListener(IConsoleInputListener listener) {
         listeners.add(listener);
      }

      public void removeListener(IConsoleInputListener listener) {
         listeners.remove(listener);
      }

      @Override
      public void run() {
         BufferedReader input = new BufferedReader(new InputStreamReader(console.getInputStream()));
         try {
            String line = null;
            while ((line = input.readLine()) != null) {
               for (IConsoleInputListener listener : listeners) {
                  listener.lineRead(line);
               }
            }
         } catch (IOException e) {
            e.printStackTrace();
         }
         OseeLog.log(UiPluginConstants.class, Level.INFO, "done with the handling of input");
      }

   }
}