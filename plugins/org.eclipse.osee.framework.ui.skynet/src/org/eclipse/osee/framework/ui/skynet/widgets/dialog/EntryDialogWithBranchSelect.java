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
package org.eclipse.osee.framework.ui.skynet.widgets.dialog;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.ui.plugin.util.IShellCloseEvent;
import org.eclipse.osee.framework.ui.skynet.branch.BranchSelectComposite;
import org.eclipse.osee.framework.ui.skynet.widgets.XText;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Donald G. Dunne
 */
public class EntryDialogWithBranchSelect extends MessageDialog {

   XText text;
   Composite c;
   String entryText = "";
   String validationRegularExpression = null;
   String validationErrorString = "";
   Button ok;
   MouseMoveListener listener;
   Label errorLabel;
   BranchSelectComposite branchSelect;
   boolean fillVertically = false;

   private final List<IShellCloseEvent> closeEventListeners = new ArrayList<>();

   public EntryDialogWithBranchSelect(String dialogTitle, String dialogMessage) {
      super(Displays.getActiveShell(), dialogTitle, null, dialogMessage, MessageDialog.QUESTION,
         new String[] {"OK", "Cancel"}, 0);
   }

   public EntryDialogWithBranchSelect(Shell parentShell, String dialogTitle, Image dialogTitleImage, String dialogMessage, int dialogImageType, String[] dialogButtonLabels, int defaultIndex) {
      super(parentShell, dialogTitle, dialogTitleImage, dialogMessage, dialogImageType, dialogButtonLabels,
         defaultIndex);
   }

   @Override
   protected Control createCustomArea(Composite parent) {

      c = new Composite(parent, SWT.NONE);
      c.setLayout(new GridLayout(2, false));
      c.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));

      listener = new MouseMoveListener() {

         @Override
         public void mouseMove(MouseEvent e) {
            setInitialButtonState();
         }
      };
      c.addMouseMoveListener(listener);

      // Create error label
      errorLabel = new Label(c, SWT.NONE);
      errorLabel.setSize(errorLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT));
      errorLabel.setText("");

      text = new XText();
      text.setFillHorizontally(true);
      text.setFocus();
      text.setDisplayLabel(false);
      if (!entryText.equals("")) {
         text.set(entryText);
      }
      if (fillVertically) {
         text.setFillVertically(true);
         text.setHeight(200);
      }
      text.createWidgets(c, 2);

      ModifyListener modifyListener = new ModifyListener() {

         @Override
         public void modifyText(ModifyEvent e) {
            handleModified();
         }
      };
      text.addModifyListener(modifyListener);

      branchSelect = new BranchSelectComposite(c, SWT.NONE, false);
      branchSelect.setDefaultSelectedBranch(BranchManager.getLastBranch());
      createExtendedArea(c);
      c.layout();
      parent.layout();
      return c;
   }

   /**
    * Override to provide other widgets
    */
   protected void createExtendedArea(Composite parent) {
      // provided for subclass implementation
   }

   public void setInitialButtonState() {
      if (ok == null) {
         ok = getButton(0);
         handleModified();
      }
      c.removeMouseMoveListener(listener);
   }

   public void handleModified() {
      if (text != null) {
         entryText = text.get();
         if (text.get().equals("") || !isEntryValid() || branchSelect.getSelectedBranch() == null) {
            getButton(getDefaultButtonIndex()).setEnabled(false);
            errorLabel.setText(validationErrorString);
            errorLabel.update();
            c.layout();
         } else {
            getButton(getDefaultButtonIndex()).setEnabled(true);
            errorLabel.setText("");
            errorLabel.update();
            c.layout();
         }
      }
   }

   public String getEntry() {
      return entryText;
   }

   public void setEntry(String entry) {
      if (text != null) {
         text.set(entry);
      }
      this.entryText = entry;
   }

   public BranchId getBranch() {
      return branchSelect.getSelectedBranch();
   }

   /**
    * override this method to make own checks on entry this will be called with every keystroke
    * 
    * @return true if entry is valid
    */
   public boolean isEntryValid() {
      if (validationRegularExpression == null) {
         return true;
      }
      // verify title is alpha-numeric with spaces and dashes
      Matcher m = Pattern.compile(validationRegularExpression).matcher(text.get());
      return m.find();
   }

   public void setValidationRegularExpression(String regExp) {
      validationRegularExpression = regExp;
   }

   public void setValidationErrorString(String errorText) {
      validationErrorString = errorText;
   }

   /**
    * Calling will enable dialog to loose focus
    */
   public void setModeless() {
      setShellStyle(SWT.DIALOG_TRIM | SWT.MODELESS);
      setBlockOnOpen(false);
   }

   public void setSelectionListener(SelectionListener listener) {
      for (int i = 0; i < getButtonLabels().length; i++) {
         Button button = getButton(i);
         button.addSelectionListener(listener);
      }
   }

   public boolean isFillVertically() {
      return fillVertically;
   }

   public void setFillVertically(boolean fillVertically) {
      this.fillVertically = fillVertically;
   }

   @Override
   protected void handleShellCloseEvent() {
      super.handleShellCloseEvent();
      for (IShellCloseEvent event : closeEventListeners) {
         event.onClose();
      }
   }

   public void addShellCloseEventListeners(IShellCloseEvent event) {
      closeEventListeners.add(event);
   }

}
