/*******************************************************************************
 * Copyright (c) 2014 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.orcs.script.dsl.fields;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.osee.orcs.script.dsl.IFieldResolver;
import org.eclipse.osee.orcs.script.dsl.OsCollectType;
import org.eclipse.osee.orcs.script.dsl.OsFieldEnum;
import org.eclipse.osee.orcs.script.dsl.OsFieldEnum.Family;
import org.eclipse.osee.orcs.script.dsl.orcsScriptDsl.OsBranchQueryStatement;
import org.eclipse.osee.orcs.script.dsl.orcsScriptDsl.OsCollectAllFieldsExpression;
import org.eclipse.osee.orcs.script.dsl.orcsScriptDsl.OsCollectClause;
import org.eclipse.osee.orcs.script.dsl.orcsScriptDsl.OsCollectExpression;
import org.eclipse.osee.orcs.script.dsl.orcsScriptDsl.OsCollectFieldExpression;
import org.eclipse.osee.orcs.script.dsl.orcsScriptDsl.OsCollectObjectExpression;
import org.eclipse.osee.orcs.script.dsl.orcsScriptDsl.OsFollowStatement;
import org.eclipse.osee.orcs.script.dsl.orcsScriptDsl.OsObjectQuery;
import org.eclipse.osee.orcs.script.dsl.orcsScriptDsl.OsQuery;
import org.eclipse.osee.orcs.script.dsl.orcsScriptDsl.OsTxQueryStatement;
import org.eclipse.xtext.EcoreUtil2;
import com.google.common.collect.Sets;
import com.google.inject.Singleton;

/**
 * @author Roberto E. Escobar
 */
@Singleton
public class OsFieldResolverImpl implements IFieldResolver {

   /**
    * Could be considered a scope provider function?
    * 
    * <pre>
    * 1. Determine what fields are allowed based on collect 
    *    clause position in the query expression
    *    
    * 2. Create a field description object that contains - field scope to 
    *    distinguish between field section
    *    
    * 3. Provide fields being used in a query - for instance
    *    if a collect clause has * after a tx query section should return
    *    all tx fields
    * 
    * 4. Provide fields for map variables when queries are assigned to 
    *    variables and then used in other queries. dot notation content assist
    * 
    * uses - 
    *    content assist, 
    *    validation, 
    *    field to column mapping
    *    available and allowed sort by fields section 
    *       a. can use alias or field name
    *       b. can use fields that are not being collected as long as 
    *          they are in scope
    * </pre>
    */
   //   public void checkOsQueryExpression(OsQueryExpression expression) {
   //      List<OsClause> clause = expression.getClause();
   //      // which collect goes with what statement ?
   //      // first collect before find or follow can only use above rule
   //      // after a find allow art,rel,attr,
   //      //      String name = field.getName();
   //      // check name is valid
   //      //Check that alias does not equal name;
   //      //      field.getAlias();
   //   }

   @Override
   public OsCollectType getCollectType(EObject object) {
      OsCollectType toReturn = null;
      OsCollectObjectExpression expression = null;
      if (object instanceof OsCollectObjectExpression) {
         expression = (OsCollectObjectExpression) object;
      } else {
         expression = EcoreUtil2.getContainerOfType(object, OsCollectObjectExpression.class);
      }
      if (expression != null) {
         String typeId = expression.getName();
         toReturn = OsCollectType.fromString(typeId);
      }
      return toReturn;
   }

   @Override
   public Set<? extends OsField> getAllowedFields(EObject object) {
      Set<? extends OsField> toReturn;
      OsCollectType type = getCollectType(object);
      if (type != null) {
         toReturn = getAllowedFieldsByType(type);
      } else {
         toReturn = Collections.<OsField> emptySet();
      }
      return toReturn;
   }

   private Set<? extends OsField> getAllowedFieldsByType(OsCollectType type) {
      Family family = getFamily(type);
      Set<? extends OsField> toReturn;
      if (family != null) {
         toReturn = OsFieldEnum.getFieldsFor(family);
      } else {
         toReturn = Collections.<OsField> emptySet();
      }
      return toReturn;
   }

   private Family getFamily(OsCollectType type) {
      Family toReturn;
      switch (type) {
         case BRANCHES:
            toReturn = Family.BRANCH;
            break;
         case TXS:
            toReturn = Family.TX;
            break;
         case ARTIFACTS:
            toReturn = Family.ARTIFACT;
            break;
         case ATTRIBUTES:
            toReturn = Family.ATTRIBUTE;
            break;
         case RELATIONS:
            toReturn = Family.RELATION;
            break;
         default:
            toReturn = Family.UNDEFINED;
            break;
      }
      return toReturn;
   }

   private EObject getCollectContainer(EObject object) {
      EObject parent = object.eContainer();
      if (parent != null) {
         OsCollectObjectExpression expression = EcoreUtil2.getContainerOfType(parent, OsCollectObjectExpression.class);
         if (expression == null) {
            EObject container = EcoreUtil2.getContainerOfType(parent, OsQuery.class);
            if (container == null) {
               container = EcoreUtil2.getContainerOfType(parent, OsObjectQuery.class);
               if (container == null) {
                  container = EcoreUtil2.getContainerOfType(parent, OsFollowStatement.class);
               }
            }
            parent = container;
         } else {
            parent = expression;
         }
      }
      return parent;
   }

   @Override
   public Set<OsCollectType> getAllowedCollectTypes(EObject object) {
      Set<OsCollectType> toReturn = new TreeSet<OsCollectType>();
      if (object instanceof OsCollectClause) {
         EObject container = getCollectContainer(object);
         if (container instanceof OsBranchQueryStatement) {
            toReturn.add(OsCollectType.BRANCHES);
         } else if (container instanceof OsTxQueryStatement) {
            toReturn.add(OsCollectType.TXS);
         } else {
            toReturn.add(OsCollectType.ARTIFACTS);
         }
      }
      return toReturn;
   }

   @Override
   public Set<? extends OsField> getDeclaredFields(EObject object) {
      Set<OsField> toReturn = new LinkedHashSet<OsField>();
      OsCollectObjectExpression container = EcoreUtil2.getContainerOfType(object, OsCollectObjectExpression.class);
      if (container != null) {
         String collectName = container.getName();
         OsCollectType type = OsCollectType.fromString(collectName);

         Set<? extends OsField> allowed = getAllowedFieldsByType(type);
         for (OsCollectExpression expression : container.getExpressions()) {
            if (expression instanceof OsCollectAllFieldsExpression) {
               toReturn.addAll(allowed);
            } else if (expression instanceof OsCollectObjectExpression) {
               String fieldName = ((OsCollectObjectExpression) expression).getName();
               OsField field = getField(type, fieldName);
               toReturn.add(field);
            } else if (expression instanceof OsCollectFieldExpression) {
               OsCollectFieldExpression fieldExp = (OsCollectFieldExpression) expression;
               String fieldName = fieldExp.getName();
               OsField field = getField(type, fieldName);
               toReturn.add(field);
            }
         }
      }
      return toReturn;
   };

   @Override
   public Set<? extends OsField> getRemainingAllowedFields(EObject object) {
      Set<? extends OsField> allowedFields = getAllowedFields(object);
      Set<OsField> declaredFields = Sets.newTreeSet(OsFieldEnum.getComparator());
      declaredFields.addAll(getDeclaredFields(object));
      return Sets.difference(allowedFields, declaredFields);
   }

   @Override
   public Set<? extends OsField> getNotAllowedDeclaredFields(EObject object) {
      Set<? extends OsField> allowedFields = getAllowedFields(object);
      Set<? extends OsField> declaredFields = getDeclaredFields(object);
      return Sets.difference(declaredFields, allowedFields);
   }

   private OsField getField(OsCollectType type, String fieldName) {
      Family family = getFamily(type);
      OsField toReturn = OsFieldEnum.getField(family, fieldName);
      if (toReturn == null) {
         toReturn = OsFieldEnum.newField(fieldName);
      }
      return toReturn;
   }

}
