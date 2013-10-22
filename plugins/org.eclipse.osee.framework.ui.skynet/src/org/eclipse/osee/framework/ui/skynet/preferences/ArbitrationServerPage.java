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
package org.eclipse.osee.framework.ui.skynet.preferences;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.osee.framework.core.client.OseeClientProperties;
import org.eclipse.osee.framework.core.client.server.HttpUrlBuilderClient;
import org.eclipse.osee.framework.core.data.OseeCodeVersion;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.ui.skynet.internal.Activator;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * @author Roberto E. Escobar
 */
public class ArbitrationServerPage extends PreferencePage implements IWorkbenchPreferencePage {

   public ArbitrationServerPage() {
      super();
   }

   @Override
   protected Control createContents(Composite parent) {
      Composite content = new Composite(parent, SWT.NONE);
      GridLayout layout = new GridLayout();
      layout.marginHeight = 0;
      layout.marginWidth = 0;
      content.setLayout(layout);
      content.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

      Group resourceGroup = new Group(content, SWT.NONE);
      resourceGroup.setLayout(new GridLayout());
      resourceGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
      resourceGroup.setText("OSEE Arbitration");

      Composite resourceComposite = new Composite(resourceGroup, SWT.NONE);
      resourceComposite.setLayout(new GridLayout(2, false));
      resourceComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

      boolean wasArbitrationOverriden = Strings.isValid(OseeClientProperties.getOseeApplicationServer());
      String defaultArbitrationServer = null;
      if (!wasArbitrationOverriden) {
         defaultArbitrationServer = HttpUrlBuilderClient.getInstance().getArbitrationServerPrefix();
      }

      String defaultApplicationServer = null;
      try {
         defaultApplicationServer = HttpUrlBuilderClient.getInstance().getApplicationServerPrefix();
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }

      List<Data> entries = new ArrayList<Data>();

      if (wasArbitrationOverriden) {
         entries.add(new Data("Arbitration", "Disabled", Displays.getSystemColor(SWT.COLOR_RED)));
         entries.add(new Data("Application Server: ", defaultApplicationServer != null ? defaultApplicationServer : ""));
      } else {
         entries.add(new Data("Arbitration Server: ", defaultArbitrationServer != null ? defaultArbitrationServer : ""));
         entries.add(new Data("Version To Match: ", OseeCodeVersion.getVersion()));
         entries.add(new Data("Resolved To Server: ", defaultApplicationServer != null ? defaultApplicationServer : ""));
      }
      for (Data entry : entries) {
         Label label1 = new Label(resourceComposite, SWT.NONE);
         label1.setForeground(entry.getLabelColor());
         label1.setText(entry.getLabelText());

         Label label2 = new Label(resourceComposite, SWT.NONE);
         label2.setForeground(entry.getDataColor());
         label2.setText(entry.getDataText());
      }
      return content;
   }

   @Override
   public void init(IWorkbench workbench) {
      setPreferenceStore(Activator.getInstance().getPreferenceStore());
      setDescription("See default Arbitration Server below.");
   }

   private final class Data {
      private final String label;
      private final Color labelColor;
      private final String value;
      private final Color valueColor;

      public Data(String label, Color labelColor, String value, Color valueColor) {
         super();
         this.label = label;
         this.labelColor = labelColor;
         this.value = value;
         this.valueColor = valueColor;
      }

      public Data(String label, String value, Color valueColor) {
         this(label, Displays.getSystemColor(SWT.COLOR_BLUE), value, valueColor);
      }

      public Data(String label, String value) {
         this(label, Displays.getSystemColor(SWT.COLOR_BLUE), value, Displays.getSystemColor(SWT.COLOR_BLACK));
      }

      public String getLabelText() {
         return label;
      }

      public Color getLabelColor() {
         return labelColor;
      }

      public String getDataText() {
         return value;
      }

      public Color getDataColor() {
         return valueColor;
      }

   }
}
