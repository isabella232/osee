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
package org.eclipse.osee.framework.ui.skynet.widgets;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Donald G. Dunne
 */
public class XOptionHandler {

   private final Set<XOption> xOptions = new HashSet<>();

   public XOptionHandler(XOption... xOption) {
      set(xOption);
   }

   public void add(XOption xOption) {
      switch (xOption) {
         case ALIGN_CENTER:
         case ALIGN_LEFT:
         case ALIGN_RIGHT:
            xOptions.remove(XOption.ALIGN_CENTER);
            xOptions.remove(XOption.ALIGN_LEFT);
            xOptions.remove(XOption.ALIGN_RIGHT);
            break;
         case HORIZONTAL_LABEL:
            xOptions.remove(XOption.VERTICAL_LABEL);
            break;
         case EDITABLE:
            xOptions.remove(XOption.NOT_EDITABLE);
            break;
         case NOT_EDITABLE:
            xOptions.remove(XOption.EDITABLE);
            break;
         case NOT_REQUIRED:
            xOptions.remove(XOption.REQUIRED);
            break;
         case REQUIRED:
            xOptions.remove(XOption.NOT_REQUIRED);
            break;
         case NOT_REQUIRED_FOR_COMPLETION:
            xOptions.remove(XOption.REQUIRED_FOR_COMPLETION);
            break;
         case REQUIRED_FOR_COMPLETION:
            xOptions.remove(XOption.NOT_REQUIRED_FOR_COMPLETION);
            break;
         case NOT_ENABLED:
            xOptions.remove(XOption.ENABLED);
            break;
         case ENABLED:
            xOptions.remove(XOption.NOT_ENABLED);
            break;
         case FILL_NONE:
            xOptions.remove(XOption.FILL_HORIZONTALLY);
            xOptions.remove(XOption.FILL_VERTICALLY);
            break;
         case VERTICAL_LABEL:
            xOptions.remove(XOption.HORIZONTAL_LABEL);
            break;
         default:
            break;
      }

      xOptions.add(xOption);
   }

   public void add(XOption... xOption) {
      for (XOption xOpt : xOption) {
         add(xOpt);
      }
   }

   public void add(Collection<XOption> xOption) {
      add(xOption.toArray(new XOption[0]));
   }

   public boolean contains(XOption xOption) {
      return xOptions.contains(xOption);
   }

   /**
    * @return the xOptions
    */
   public Set<XOption> getXOptions() {
      return xOptions;
   }

   /**
    * Must go through the add method to ensure values set properly
    * 
    * @param options the xOptions to set
    */
   public void set(Set<XOption> options) {
      xOptions.clear();
      add(options);
   }

   /**
    * Must go through the add method to ensure values set properly
    * 
    * @param options the xOptions to set
    */
   public void set(XOption[] options) {
      xOptions.clear();
      add(options);
   }

   @Override
   public String toString() {
      return String.valueOf(xOptions);
   }
}
