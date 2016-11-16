/*******************************************************************************
 * Copyright (c) 2015 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.api.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import org.eclipse.osee.ats.api.config.JaxAtsObject;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.data.RelationTypeSide;

/**
 * @author Donald G. Dunne
 */
@XmlRootElement
public class JaxAtsTask extends JaxAtsObject {

   private List<String> assigneeUserIds = new LinkedList<>();
   private Date createdDate;
   private String createdByUserId;
   private String relatedToState;
   private String taskWorkDef;
   List<JaxAttribute> attributes;
   List<JaxRelation> relations;

   public JaxAtsTask() {
      attributes = new ArrayList<>();
   }

   public Date getCreatedDate() {
      return createdDate;
   }

   public void setCreatedDate(Date createdDate) {
      this.createdDate = createdDate;
   }

   public String getCreatedByUserId() {
      return createdByUserId;
   }

   public void setCreatedByUserId(String createdByUserId) {
      this.createdByUserId = createdByUserId;
   }

   public String getRelatedToState() {
      return relatedToState;
   }

   public void setRelatedToState(String relatedToState) {
      this.relatedToState = relatedToState;
   }

   @Override
   public String toString() {
      return "JaxAtsTask [title=" + getName() + ", createdDate=" + createdDate + ", createdByUuid=" + createdByUserId + ", assigneeUuids=" + assigneeUserIds + ", relatedToState=" + relatedToState + "]";
   }

   public List<String> getAssigneeUserIds() {
      return assigneeUserIds;
   }

   public void setAssigneeUserIds(List<String> assigneeUserIds) {
      this.assigneeUserIds = assigneeUserIds;
   }

   public String getTaskWorkDef() {
      return taskWorkDef;
   }

   public void setTaskWorkDef(String taskWorkDef) {
      this.taskWorkDef = taskWorkDef;
   }

   public List<JaxAttribute> getAttributes() {
      return attributes;
   }

   public void setAttributes(List<JaxAttribute> attributes) {
      this.attributes = attributes;
   }

   public void addAttributes(String attrTypeName, List<Object> values) {
      JaxAttribute attr = new JaxAttribute();
      attr.setAttrTypeName(attrTypeName);
      attr.getValues().addAll(values);
      attributes.add(attr);
   }

   public void addAttribute(String attrTypeName, Object value) {
      JaxAttribute attr = new JaxAttribute();
      attr.setAttrTypeName(attrTypeName);
      attr.getValues().add(value);
      attributes.add(attr);
   }

   public void addAttribute(IAttributeType attrType, Object value) {
      addAttribute(attrType.getName(), value);
   }

   public void addRelation(RelationTypeSide relationSide, long... relatedUuid) {
      JaxRelation relation = new JaxRelation();
      relation.setRelationTypeName(relationSide.getName());
      relation.setSideA(relationSide.getSide().isSideA());
      for (long relationUuid : relatedUuid) {
         relation.getRelatedUuids().add(relationUuid);
      }
      getRelations().add(relation);
   }

   public List<JaxRelation> getRelations() {
      if (relations == null) {
         relations = new LinkedList<>();
      }
      return relations;
   }

   public void setRelations(List<JaxRelation> relations) {
      this.relations = relations;
   }

}
