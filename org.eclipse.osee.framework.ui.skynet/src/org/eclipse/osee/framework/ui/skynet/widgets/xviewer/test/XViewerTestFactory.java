/*
 * Created on Jun 28, 2008
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.framework.ui.skynet.widgets.xviewer.test;

import java.util.Arrays;
import java.util.List;
import org.eclipse.osee.framework.ui.skynet.widgets.xviewer.XViewerColumn;
import org.eclipse.osee.framework.ui.skynet.widgets.xviewer.XViewerFactory;
import org.eclipse.osee.framework.ui.skynet.widgets.xviewer.XViewerColumn.SortDataType;
import org.eclipse.osee.framework.ui.skynet.widgets.xviewer.customize.CustomizeData;
import org.eclipse.osee.framework.ui.skynet.widgets.xviewer.customize.IXViewerCustomizations;
import org.eclipse.swt.SWT;

/**
 * @author Donald G. Dunne
 */
public class XViewerTestFactory extends XViewerFactory {
   /**
    * @param namespace
    */
   public XViewerTestFactory() {
      super("xviewer.test");
   }

   private static String COLUMN_NAMESPACE = "xviewer.test";
   public static XViewerColumn Run_Col =
         new XViewerColumn(COLUMN_NAMESPACE + ".run", "Run", 50, SWT.LEFT, true, SortDataType.String);
   public static XViewerColumn Name_Col =
         new XViewerColumn(COLUMN_NAMESPACE + ".name", "Name", 150, SWT.LEFT, true, SortDataType.String);
   public static XViewerColumn Schedule_Time =
         new XViewerColumn(COLUMN_NAMESPACE + ".startTime", "Start Time", 40, SWT.CENTER, true, SortDataType.String);
   public static XViewerColumn Run_Db =
         new XViewerColumn(COLUMN_NAMESPACE + ".runDb", "Run DB", 80, SWT.LEFT, true, SortDataType.String);
   public static XViewerColumn Task_Type =
         new XViewerColumn(COLUMN_NAMESPACE + ".taskType", "Task Type", 80, SWT.LEFT, true, SortDataType.String);
   public static XViewerColumn Category =
         new XViewerColumn(COLUMN_NAMESPACE + ".category", "Category", 80, SWT.LEFT, false, SortDataType.String);
   public static XViewerColumn Notification =
         new XViewerColumn(COLUMN_NAMESPACE + ".emailResults", "Email Results To", 150, SWT.LEFT, true,
               SortDataType.String);
   public static XViewerColumn Description =
         new XViewerColumn(COLUMN_NAMESPACE + ".description", "Description", 75, SWT.LEFT, true, SortDataType.String);
   public static XViewerColumn Other_Description =
         new XViewerColumn(COLUMN_NAMESPACE + ".otherDescription", "Other Description", 75, SWT.LEFT, false,
               SortDataType.String);
   public List<XViewerColumn> columns =
         Arrays.asList(Run_Col, Name_Col, Schedule_Time, Run_Db, Task_Type, Category, Notification, Description,
               Other_Description);

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.ui.skynet.widgets.xviewer.IXViewerFactory#getDefaultTableCustomizeData()
    */
   @Override
   public CustomizeData getDefaultTableCustomizeData() {
      CustomizeData custData = new CustomizeData();
      custData.getColumnData().setColumns(columns);
      return custData;
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.ui.skynet.widgets.xviewer.IXViewerFactory#getDefaultXViewerColumn(java.lang.String)
    */
   @Override
   public XViewerColumn getDefaultXViewerColumn(String id) {
      for (XViewerColumn xCol : columns) {
         if (xCol.getId().equals(id)) {
            return xCol;
         }
      }
      return null;
   }

   @Override
   public IXViewerCustomizations getXViewerCustomizations() {
      return new XViewerTestCustomizations();
   }

}
