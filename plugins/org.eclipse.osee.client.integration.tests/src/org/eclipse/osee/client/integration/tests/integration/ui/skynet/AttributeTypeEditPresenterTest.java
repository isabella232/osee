/*******************************************************************************
 * Copyright (c) 2010 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.client.integration.tests.integration.ui.skynet;

import static org.eclipse.osee.client.demo.DemoChoice.OSEE_CLIENT_DEMO;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.eclipse.osee.client.test.framework.OseeClientIntegrationRule;
import org.eclipse.osee.client.test.framework.OseeLogMonitorRule;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.CoreBranches;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.type.Pair;
import org.eclipse.osee.framework.jdk.core.util.Compare;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactTypeManager;
import org.eclipse.osee.framework.ui.skynet.artifact.editor.sections.AttributeTypeEditPresenter;
import org.eclipse.osee.framework.ui.skynet.artifact.editor.sections.AttributeTypeEditPresenter.Display.OperationType;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * Test Case for {@link AttributeTypeEditPresenter}
 * 
 * @author Roberto E. Escobar
 */
public class AttributeTypeEditPresenterTest {

   @Rule
   public OseeClientIntegrationRule integration = new OseeClientIntegrationRule(OSEE_CLIENT_DEMO);

   @Rule
   public OseeLogMonitorRule monitorRule = new OseeLogMonitorRule();

   private static final IAttributeType[] selectableTypes = new IAttributeType[] {
      CoreAttributeTypes.RelationOrder,
      CoreAttributeTypes.Annotation,
      CoreAttributeTypes.StaticId};

   private AttributeTypeEditPresenter controller;
   private MockDisplay display;
   private Artifact artifact;
   private MockEditor editor;

   @Before
   public void setUp() throws OseeCoreException {
      display = new MockDisplay();
      editor = new MockEditor();
      controller = new AttributeTypeEditPresenter(editor, display);

      artifact =
         ArtifactTypeManager.addArtifact(CoreArtifactTypes.Artifact, CoreBranches.COMMON, "test attribute types");
      editor.setArtifact(artifact);
   }

   @After
   public void tearDown() throws OseeCoreException {
      if (artifact != null) {
         artifact.purgeFromBranch();
      }
   }

   @Test
   public void testAddRemoveItems() throws OseeCoreException {
      String expectedTitle = "Add Attribute Types";
      String expectedOpMessage = "Select items to add.";
      String expectedNoneMessage = "No attribute types available to add.";
      OperationType expectedType = OperationType.ADD_ITEM;

      testOperation(expectedType, expectedTitle, expectedOpMessage, expectedNoneMessage);

      expectedTitle = "Delete Attribute Types";
      expectedOpMessage = "Select items to remove.";
      expectedNoneMessage = "No attribute types available to remove.";
      expectedType = OperationType.REMOVE_ITEM;

      testOperation(expectedType, expectedTitle, expectedOpMessage, expectedNoneMessage);
   }

   private static void performOp(AttributeTypeEditPresenter controller, OperationType operationType) throws OseeCoreException {
      switch (operationType) {
         case ADD_ITEM:
            controller.onAddAttributeType();
            break;
         case REMOVE_ITEM:
            controller.onRemoveAttributeType();
            break;
         default:
            throw new UnsupportedOperationException();
      }
   }

   private void testOperation(OperationType operationType, String expectedTitle, String expectedOpMessage, String expectedNoneMessage) throws OseeCoreException {
      editor.setWasDirtyStateCalled(false);
      display.setAddWidgetsAttributeTypes(null);
      display.setRemoveWidgetsAttributeTypes(null);

      // None Selected
      display.setSelected();
      List<IAttributeType> selectable = new ArrayList<IAttributeType>(Arrays.asList(selectableTypes));
      performOp(controller, operationType);

      Assert.assertNull(display.getAddWidgetsAttributeTypes());
      Assert.assertNull(display.getRemoveWidgetsAttributeTypes());

      // Editor not saved unless artifact change
      Assert.assertFalse(editor.wasDirtyStateCalled());
      checkDisplay(display, operationType, expectedTitle, expectedOpMessage, selectable);

      // Add one at a time
      for (IAttributeType itemToSelect : selectableTypes) {
         editor.setWasDirtyStateCalled(false);
         Assert.assertFalse(editor.wasDirtyStateCalled());

         display.setAddWidgetsAttributeTypes(null);
         display.setRemoveWidgetsAttributeTypes(null);

         display.setSelected(itemToSelect);

         if (OperationType.ADD_ITEM == operationType) {
            Assert.assertTrue(artifact.getAttributes(itemToSelect).isEmpty());
         } else if (OperationType.REMOVE_ITEM == operationType) {
            Assert.assertFalse(artifact.getAttributes(itemToSelect).isEmpty());
         }

         Assert.assertNull(display.getAddWidgetsAttributeTypes());
         Assert.assertNull(display.getRemoveWidgetsAttributeTypes());
         performOp(controller, operationType);

         // Check Dirty State
         Assert.assertTrue(editor.wasDirtyStateCalled());

         checkDisplay(display, operationType, expectedTitle, expectedOpMessage, selectable);

         if (OperationType.ADD_ITEM == operationType) {
            Assert.assertFalse(artifact.getAttributes(itemToSelect).isEmpty());
            Assert.assertNull(display.getRemoveWidgetsAttributeTypes());

            Collection<IAttributeType> addedTypes = display.getAddWidgetsAttributeTypes();
            Assert.assertEquals(1, addedTypes.size());
            Assert.assertEquals(itemToSelect, addedTypes.iterator().next());

         } else if (OperationType.REMOVE_ITEM == operationType) {
            Assert.assertTrue(artifact.getAttributes(itemToSelect).isEmpty());
            Assert.assertNull(display.getAddWidgetsAttributeTypes());

            Collection<IAttributeType> removedTypes = display.getRemoveWidgetsAttributeTypes();
            Assert.assertEquals(1, removedTypes.size());
            Assert.assertEquals(itemToSelect, removedTypes.iterator().next());
         }
         selectable.remove(itemToSelect);
      }

      editor.setWasDirtyStateCalled(false);
      Assert.assertFalse(editor.wasDirtyStateCalled());

      display.setAddWidgetsAttributeTypes(null);
      display.setRemoveWidgetsAttributeTypes(null);

      //      artifact.deleteAttributes(CoreAttributeTypes.Name);

      // None Selectable
      display.setSelected();
      performOp(controller, operationType);
      Assert.assertNull(display.getAddWidgetsAttributeTypes());
      Assert.assertNull(display.getRemoveWidgetsAttributeTypes());

      Assert.assertFalse(editor.wasDirtyStateCalled());

      Pair<String, String> info = display.getShowInfo();
      Assert.assertEquals(expectedTitle, info.getFirst());
      Assert.assertEquals(expectedNoneMessage, info.getSecond());
   }

   private static void checkDisplay(MockDisplay display, OperationType expectedType, String title, String message, List<IAttributeType> expectedSelectable) {
      List<IAttributeType> selectableItems = display.getInput();

      Assert.assertFalse(
         String.format("Selectable Types - expected:[%s] actual:[%s]", expectedSelectable, selectableItems),
         Compare.isDifferent(expectedSelectable, selectableItems));
      Assert.assertEquals(expectedType, display.getOperationType());

      Pair<String, String> selectionInfo = display.getSelectionInfo();
      Assert.assertEquals(title, selectionInfo.getFirst());
      Assert.assertEquals(message, selectionInfo.getSecond());
      Assert.assertNull(display.getShowInfo());

      //      display.getSelections(operationType, title, message, input)
   }

   private final static class MockEditor implements AttributeTypeEditPresenter.Model {
      private boolean wasDirtyStateCalled;
      private Artifact artifact;

      public void setWasDirtyStateCalled(boolean wasDirtyStateCalled) {
         this.wasDirtyStateCalled = wasDirtyStateCalled;
      }

      public boolean wasDirtyStateCalled() {
         return wasDirtyStateCalled;
      }

      @Override
      public Artifact getArtifact() {
         return artifact;
      }

      public void setArtifact(Artifact artifact) {
         this.artifact = artifact;
      }

      @Override
      public void refreshDirtyArtifact() {
         // do nothing
      }

      @Override
      public void dirtyStateChanged() {
         this.wasDirtyStateCalled = true;
      }

   }

   private final static class MockDisplay implements AttributeTypeEditPresenter.Display {

      private Pair<String, String> showInfo;
      private Pair<String, String> selectionInfo;
      private OperationType operationType;
      private List<IAttributeType> input;
      private Collection<IAttributeType> selected;
      private Collection<IAttributeType> addWidgetsAttributeTypes;
      private Collection<IAttributeType> removeWidgetsAttributeTypes;

      private MockDisplay() {
         this.selected = Collections.emptyList();
      }

      @Override
      public Collection<IAttributeType> getSelections(OperationType operationType, String title, String message, List<IAttributeType> input) {
         setSelectionInfo(new Pair<String, String>(title, message));
         setInput(input);
         setOperationType(operationType);
         setShowInfo(null);
         return selected;
      }

      @Override
      public void showInformation(String title, String message) {
         showInfo = new Pair<String, String>(title, message);
      }

      public Pair<String, String> getShowInfo() {
         return showInfo;
      }

      public Pair<String, String> getSelectionInfo() {
         return selectionInfo;
      }

      public OperationType getOperationType() {
         return operationType;
      }

      public List<IAttributeType> getInput() {
         return input;
      }

      public void setSelected(IAttributeType... selected) {
         this.selected = Arrays.asList(selected);
      }

      public void setShowInfo(Pair<String, String> showInfo) {
         this.showInfo = showInfo;
      }

      public void setSelectionInfo(Pair<String, String> selectionInfo) {
         this.selectionInfo = selectionInfo;
      }

      public void setOperationType(OperationType operationType) {
         this.operationType = operationType;
      }

      public void setInput(List<IAttributeType> input) {
         this.input = input;
      }

      public Collection<IAttributeType> getAddWidgetsAttributeTypes() {
         return addWidgetsAttributeTypes;
      }

      public Collection<IAttributeType> getRemoveWidgetsAttributeTypes() {
         return removeWidgetsAttributeTypes;
      }

      public void setAddWidgetsAttributeTypes(Collection<IAttributeType> addWidgetsAttributeTypes) {
         this.addWidgetsAttributeTypes = addWidgetsAttributeTypes;
      }

      public void setRemoveWidgetsAttributeTypes(Collection<IAttributeType> removeWidgetsAttributeTypes) {
         this.removeWidgetsAttributeTypes = removeWidgetsAttributeTypes;
      }

      @Override
      public void addWidgetFor(Collection<IAttributeType> attributeTypes) {
         addWidgetsAttributeTypes = attributeTypes;
      }

      @Override
      public void removeWidgetFor(Collection<IAttributeType> attributeTypes) {
         removeWidgetsAttributeTypes = attributeTypes;
      }
   }
}
