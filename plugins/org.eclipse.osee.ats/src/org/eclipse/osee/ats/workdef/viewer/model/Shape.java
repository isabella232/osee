/*******************************************************************************
 * Copyright (c) 2004, 2005 Donald G. Dunne and others.
�* All rights reserved. This program and the accompanying materials
�* are made available under the terms of the Eclipse Public License v1.0
�* which accompanies this distribution, and is available at
�* http://www.eclipse.org/legal/epl-v10.html
�*
�* Contributors:
�*����Donald G. Dunne - initial API and implementation
�*******************************************************************************/
package org.eclipse.osee.ats.workdef.viewer.model;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

/**
 * Abstract prototype of a shape. Has a size (width and height), a location (x and y position) and a list of incoming
 * and outgoing connections. Use subclasses to instantiate a specific shape.
 * 
 * @see org.eclipse.osee.ats.workflow.StateDefShape.model.WorkPageShape
 * @see org.eclipse.osee.ats.workflow.editor.model.EllipticalShape
 * @author Donald G. Dunne
 */
public abstract class Shape extends ModelElement {

   /**
    * A static array of property descriptors. There is one IPropertyDescriptor entry per editable property.
    * 
    * @see #getPropertyDescriptors()
    * @see #getPropertyValue(Object)
    * @see #setPropertyValue(Object, Object)
    */
   private IPropertyDescriptor[] descriptors;
   private final List<IPropertyDescriptor> descriptorList = new ArrayList<>();
   /** ID for the Height property value (used for by the corresponding property descriptor). */
   private static final String HEIGHT_PROP = "Shape.Height";
   /** Property ID to use when the location of this shape is modified. */
   public static final String LOCATION_PROP = "Shape.Location";
   /** Property ID to use then the size of this shape is modified. */
   public static final String SIZE_PROP = "Shape.Size";
   /** Property ID to use when the list of outgoing connections is modified. */
   public static final String SOURCE_CONNECTIONS_PROP = "Shape.SourceConn";
   /** Property ID to use when the list of incoming connections is modified. */
   public static final String TARGET_CONNECTIONS_PROP = "Shape.TargetConn";
   /** ID for the Width property value (used for by the corresponding property descriptor). */
   private static final String WIDTH_PROP = "Shape.Width";

   /** ID for the X property value (used for by the corresponding property descriptor). */
   private static final String XPOS_PROP = "Shape.xPos";
   /** ID for the Y property value (used for by the corresponding property descriptor). */
   private static final String YPOS_PROP = "Shape.yPos";

   protected abstract String getName();

   protected abstract String getToolTip();

   /** Location of this shape. */
   private final Point location = new Point(0, 0);
   /** Size of this shape. */
   private final Dimension size = new Dimension(50, 50);
   /** List of outgoing Connections. */
   private final List<Connection> sourceConnections = new ArrayList<>();
   /** List of incoming Connections. */
   private final List<Connection> targetConnections = new ArrayList<>();
   private WorkDefinitionDiagram workflowDiagram;

   /**
    * Add an incoming or outgoing connection to this shape.
    * 
    * @param conn a non-null connection instance
    * @throws IllegalArgumentException if the connection is null or has not distinct endpoints
    */
   void addConnection(Connection conn) {
      if (conn == null || conn.getSource() == conn.getTarget()) {
         throw new IllegalArgumentException();
      }
      if (conn.getSource() == this) {
         sourceConnections.add(conn);
         firePropertyChange(SOURCE_CONNECTIONS_PROP, null, conn);
      } else if (conn.getTarget() == this) {
         targetConnections.add(conn);
         firePropertyChange(TARGET_CONNECTIONS_PROP, null, conn);
      }
   }

   /**
    * Return a pictogram (small icon) describing this model element. Children should override this method and return an
    * appropriate Image.
    * 
    * @return a 16x16 Image or null
    */
   public abstract Image getIcon();

   /**
    * Return the Location of this shape.
    * 
    * @return a non-null location instance
    */
   public Point getLocation() {
      return location.getCopy();
   }

   /**
    * Returns an array of IPropertyDescriptors for this shape.
    * <p>
    * The returned array is used to fill the property view, when the edit-part corresponding to this model element is
    * selected.
    * </p>
    * 
    * @see #descriptors
    * @see #getPropertyValue(Object)
    * @see #setPropertyValue(Object, Object)
    */
   @Override
   public IPropertyDescriptor[] getPropertyDescriptors() {
      if (descriptors == null) {
         initializePropertyDescriptors(descriptorList);
         descriptors = descriptorList.toArray(new IPropertyDescriptor[descriptorList.size()]);
      }
      return descriptors;
   }

   protected void initializePropertyDescriptors(List<IPropertyDescriptor> descriptorList) {
      descriptorList.add(new TextPropertyDescriptor(XPOS_PROP, "X")); // id and description pair
      descriptorList.add(new TextPropertyDescriptor(YPOS_PROP, "Y"));
      descriptorList.add(new TextPropertyDescriptor(WIDTH_PROP, "Width"));
      descriptorList.add(new TextPropertyDescriptor(HEIGHT_PROP, "Height"));
      // use a custom cell editor validator for all four array entries
      for (IPropertyDescriptor descriptor : descriptorList) {
         ((PropertyDescriptor) descriptor).setValidator(new ICellEditorValidator() {
            @Override
            public String isValid(Object value) {
               int intValue = -1;
               try {
                  intValue = Integer.parseInt((String) value);
               } catch (NumberFormatException exc) {
                  return "Not a number";
               }
               return intValue >= 0 ? null : "Value must be >=  0";
            }
         });
      }
   }

   /**
    * Return the property value for the given propertyId, or null.
    * <p>
    * The property view uses the IDs from the IPropertyDescriptors array to obtain the value of the corresponding
    * properties.
    * </p>
    * 
    * @see #descriptors
    * @see #getPropertyDescriptors()
    */
   @Override
   public Object getPropertyValue(Object propertyId) {
      if (XPOS_PROP.equals(propertyId)) {
         return Integer.toString(location.x);
      }
      if (YPOS_PROP.equals(propertyId)) {
         return Integer.toString(location.y);
      }
      if (HEIGHT_PROP.equals(propertyId)) {
         return Integer.toString(size.height);
      }
      if (WIDTH_PROP.equals(propertyId)) {
         return Integer.toString(size.width);
      }
      return super.getPropertyValue(propertyId);
   }

   /**
    * Return the Size of this shape.
    * 
    * @return a non-null Dimension instance
    */
   public Dimension getSize() {
      return size.getCopy();
   }

   /**
    * Return a List of outgoing Connections.
    */
   public List<Connection> getSourceConnections() {
      return new ArrayList<Connection>(sourceConnections);
   }

   /**
    * Return a List of incoming Connections.
    */
   public List<Connection> getTargetConnections() {
      return new ArrayList<Connection>(targetConnections);
   }

   /**
    * Remove an incoming or outgoing connection from this shape.
    * 
    * @param conn a non-null connection instance
    * @throws IllegalArgumentException if the parameter is null
    */
   void removeConnection(Connection conn) {
      if (conn == null) {
         throw new IllegalArgumentException();
      }
      if (conn.getSource() == this) {
         sourceConnections.remove(conn);
         firePropertyChange(SOURCE_CONNECTIONS_PROP, null, conn);
      } else if (conn.getTarget() == this) {
         targetConnections.remove(conn);
         firePropertyChange(TARGET_CONNECTIONS_PROP, null, conn);
      }
   }

   /**
    * Set the Location of this shape.
    * 
    * @param newLocation a non-null Point instance
    * @throws IllegalArgumentException if the parameter is null
    */
   public void setLocation(Point newLocation) {
      if (newLocation == null) {
         throw new IllegalArgumentException();
      }
      location.setLocation(newLocation);
      firePropertyChange(LOCATION_PROP, null, location);
   }

   /**
    * Set the property value for the given property id. If no matching id is found, the call is forwarded to the
    * superclass.
    * <p>
    * The property view uses the IDs from the IPropertyDescriptors array to set the values of the corresponding
    * properties.
    * </p>
    * 
    * @see #descriptors
    * @see #getPropertyDescriptors()
    */
   @Override
   public void setPropertyValue(Object propertyId, Object value) {
      if (XPOS_PROP.equals(propertyId)) {
         int x = Integer.parseInt((String) value);
         setLocation(new Point(x, location.y));
      } else if (YPOS_PROP.equals(propertyId)) {
         int y = Integer.parseInt((String) value);
         setLocation(new Point(location.x, y));
      } else if (HEIGHT_PROP.equals(propertyId)) {
         int height = Integer.parseInt((String) value);
         setSize(new Dimension(size.width, height));
      } else if (WIDTH_PROP.equals(propertyId)) {
         int width = Integer.parseInt((String) value);
         setSize(new Dimension(width, size.height));
      } else {
         super.setPropertyValue(propertyId, value);
      }
   }

   /**
    * Set the Size of this shape. Will not modify the size if newSize is null.
    * 
    * @param newSize a non-null Dimension instance or null
    */
   public void setSize(Dimension newSize) {
      if (newSize != null) {
         size.setSize(newSize);
         firePropertyChange(SIZE_PROP, null, size);
      }
   }

   /**
    * @return the workflowDiagram
    */
   public WorkDefinitionDiagram getWorkflowDiagram() {
      return workflowDiagram;
   }

   /**
    * @param workflowDiagram the workflowDiagram to set
    */
   public void setWorkflowDiagram(WorkDefinitionDiagram workflowDiagram) {
      this.workflowDiagram = workflowDiagram;
   }
}