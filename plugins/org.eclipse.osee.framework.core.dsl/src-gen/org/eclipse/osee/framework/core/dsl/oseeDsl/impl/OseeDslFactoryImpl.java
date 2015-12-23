/**
 */
package org.eclipse.osee.framework.core.dsl.oseeDsl.impl;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.impl.EFactoryImpl;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.osee.framework.core.dsl.oseeDsl.AccessContext;
import org.eclipse.osee.framework.core.dsl.oseeDsl.AccessPermissionEnum;
import org.eclipse.osee.framework.core.dsl.oseeDsl.AddAttribute;
import org.eclipse.osee.framework.core.dsl.oseeDsl.AddEnum;
import org.eclipse.osee.framework.core.dsl.oseeDsl.ArtifactMatchRestriction;
import org.eclipse.osee.framework.core.dsl.oseeDsl.ArtifactTypeRestriction;
import org.eclipse.osee.framework.core.dsl.oseeDsl.AttributeOverrideOption;
import org.eclipse.osee.framework.core.dsl.oseeDsl.AttributeTypeRestriction;
import org.eclipse.osee.framework.core.dsl.oseeDsl.CompareOp;
import org.eclipse.osee.framework.core.dsl.oseeDsl.CompoundCondition;
import org.eclipse.osee.framework.core.dsl.oseeDsl.Condition;
import org.eclipse.osee.framework.core.dsl.oseeDsl.HierarchyRestriction;
import org.eclipse.osee.framework.core.dsl.oseeDsl.Import;
import org.eclipse.osee.framework.core.dsl.oseeDsl.LegacyRelationTypeRestriction;
import org.eclipse.osee.framework.core.dsl.oseeDsl.MatchField;
import org.eclipse.osee.framework.core.dsl.oseeDsl.ObjectRestriction;
import org.eclipse.osee.framework.core.dsl.oseeDsl.OseeDsl;
import org.eclipse.osee.framework.core.dsl.oseeDsl.OseeDslFactory;
import org.eclipse.osee.framework.core.dsl.oseeDsl.OseeDslPackage;
import org.eclipse.osee.framework.core.dsl.oseeDsl.OseeElement;
import org.eclipse.osee.framework.core.dsl.oseeDsl.OseeType;
import org.eclipse.osee.framework.core.dsl.oseeDsl.OverrideOption;
import org.eclipse.osee.framework.core.dsl.oseeDsl.ReferencedContext;
import org.eclipse.osee.framework.core.dsl.oseeDsl.RelationMultiplicityEnum;
import org.eclipse.osee.framework.core.dsl.oseeDsl.RelationTypeArtifactPredicate;
import org.eclipse.osee.framework.core.dsl.oseeDsl.RelationTypeArtifactTypePredicate;
import org.eclipse.osee.framework.core.dsl.oseeDsl.RelationTypeMatch;
import org.eclipse.osee.framework.core.dsl.oseeDsl.RelationTypePredicate;
import org.eclipse.osee.framework.core.dsl.oseeDsl.RelationTypeRestriction;
import org.eclipse.osee.framework.core.dsl.oseeDsl.RemoveAttribute;
import org.eclipse.osee.framework.core.dsl.oseeDsl.RemoveEnum;
import org.eclipse.osee.framework.core.dsl.oseeDsl.Role;
import org.eclipse.osee.framework.core.dsl.oseeDsl.SimpleCondition;
import org.eclipse.osee.framework.core.dsl.oseeDsl.UpdateAttribute;
import org.eclipse.osee.framework.core.dsl.oseeDsl.UsersAndGroups;
import org.eclipse.osee.framework.core.dsl.oseeDsl.XArtifactMatcher;
import org.eclipse.osee.framework.core.dsl.oseeDsl.XArtifactType;
import org.eclipse.osee.framework.core.dsl.oseeDsl.XAttributeType;
import org.eclipse.osee.framework.core.dsl.oseeDsl.XAttributeTypeRef;
import org.eclipse.osee.framework.core.dsl.oseeDsl.XLogicOperator;
import org.eclipse.osee.framework.core.dsl.oseeDsl.XOseeArtifactTypeOverride;
import org.eclipse.osee.framework.core.dsl.oseeDsl.XOseeEnumEntry;
import org.eclipse.osee.framework.core.dsl.oseeDsl.XOseeEnumOverride;
import org.eclipse.osee.framework.core.dsl.oseeDsl.XOseeEnumType;
import org.eclipse.osee.framework.core.dsl.oseeDsl.XRelationSideEnum;
import org.eclipse.osee.framework.core.dsl.oseeDsl.XRelationType;

/**
 * <!-- begin-user-doc --> An implementation of the model <b>Factory</b>. <!-- end-user-doc -->
 * 
 * @generated
 */
public class OseeDslFactoryImpl extends EFactoryImpl implements OseeDslFactory {
   /**
    * Creates the default factory implementation. <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   public static OseeDslFactory init() {
      try {
         OseeDslFactory theOseeDslFactory =
            (OseeDslFactory) EPackage.Registry.INSTANCE.getEFactory(OseeDslPackage.eNS_URI);
         if (theOseeDslFactory != null) {
            return theOseeDslFactory;
         }
      } catch (Exception exception) {
         EcorePlugin.INSTANCE.log(exception);
      }
      return new OseeDslFactoryImpl();
   }

   /**
    * Creates an instance of the factory. <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   public OseeDslFactoryImpl() {
      super();
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public EObject create(EClass eClass) {
      switch (eClass.getClassifierID()) {
         case OseeDslPackage.OSEE_DSL:
            return createOseeDsl();
         case OseeDslPackage.IMPORT:
            return createImport();
         case OseeDslPackage.OSEE_ELEMENT:
            return createOseeElement();
         case OseeDslPackage.OSEE_TYPE:
            return createOseeType();
         case OseeDslPackage.XARTIFACT_TYPE:
            return createXArtifactType();
         case OseeDslPackage.XATTRIBUTE_TYPE_REF:
            return createXAttributeTypeRef();
         case OseeDslPackage.XATTRIBUTE_TYPE:
            return createXAttributeType();
         case OseeDslPackage.XOSEE_ENUM_TYPE:
            return createXOseeEnumType();
         case OseeDslPackage.XOSEE_ENUM_ENTRY:
            return createXOseeEnumEntry();
         case OseeDslPackage.XOSEE_ENUM_OVERRIDE:
            return createXOseeEnumOverride();
         case OseeDslPackage.OVERRIDE_OPTION:
            return createOverrideOption();
         case OseeDslPackage.ADD_ENUM:
            return createAddEnum();
         case OseeDslPackage.REMOVE_ENUM:
            return createRemoveEnum();
         case OseeDslPackage.XOSEE_ARTIFACT_TYPE_OVERRIDE:
            return createXOseeArtifactTypeOverride();
         case OseeDslPackage.ATTRIBUTE_OVERRIDE_OPTION:
            return createAttributeOverrideOption();
         case OseeDslPackage.ADD_ATTRIBUTE:
            return createAddAttribute();
         case OseeDslPackage.REMOVE_ATTRIBUTE:
            return createRemoveAttribute();
         case OseeDslPackage.UPDATE_ATTRIBUTE:
            return createUpdateAttribute();
         case OseeDslPackage.XRELATION_TYPE:
            return createXRelationType();
         case OseeDslPackage.CONDITION:
            return createCondition();
         case OseeDslPackage.SIMPLE_CONDITION:
            return createSimpleCondition();
         case OseeDslPackage.COMPOUND_CONDITION:
            return createCompoundCondition();
         case OseeDslPackage.XARTIFACT_MATCHER:
            return createXArtifactMatcher();
         case OseeDslPackage.ROLE:
            return createRole();
         case OseeDslPackage.REFERENCED_CONTEXT:
            return createReferencedContext();
         case OseeDslPackage.USERS_AND_GROUPS:
            return createUsersAndGroups();
         case OseeDslPackage.ACCESS_CONTEXT:
            return createAccessContext();
         case OseeDslPackage.HIERARCHY_RESTRICTION:
            return createHierarchyRestriction();
         case OseeDslPackage.RELATION_TYPE_ARTIFACT_TYPE_PREDICATE:
            return createRelationTypeArtifactTypePredicate();
         case OseeDslPackage.RELATION_TYPE_ARTIFACT_PREDICATE:
            return createRelationTypeArtifactPredicate();
         case OseeDslPackage.RELATION_TYPE_PREDICATE:
            return createRelationTypePredicate();
         case OseeDslPackage.OBJECT_RESTRICTION:
            return createObjectRestriction();
         case OseeDslPackage.ARTIFACT_MATCH_RESTRICTION:
            return createArtifactMatchRestriction();
         case OseeDslPackage.ARTIFACT_TYPE_RESTRICTION:
            return createArtifactTypeRestriction();
         case OseeDslPackage.ATTRIBUTE_TYPE_RESTRICTION:
            return createAttributeTypeRestriction();
         case OseeDslPackage.LEGACY_RELATION_TYPE_RESTRICTION:
            return createLegacyRelationTypeRestriction();
         case OseeDslPackage.RELATION_TYPE_RESTRICTION:
            return createRelationTypeRestriction();
         default:
            throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
      }
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public Object createFromString(EDataType eDataType, String initialValue) {
      switch (eDataType.getClassifierID()) {
         case OseeDslPackage.RELATION_MULTIPLICITY_ENUM:
            return createRelationMultiplicityEnumFromString(eDataType, initialValue);
         case OseeDslPackage.COMPARE_OP:
            return createCompareOpFromString(eDataType, initialValue);
         case OseeDslPackage.XLOGIC_OPERATOR:
            return createXLogicOperatorFromString(eDataType, initialValue);
         case OseeDslPackage.MATCH_FIELD:
            return createMatchFieldFromString(eDataType, initialValue);
         case OseeDslPackage.ACCESS_PERMISSION_ENUM:
            return createAccessPermissionEnumFromString(eDataType, initialValue);
         case OseeDslPackage.RELATION_TYPE_MATCH:
            return createRelationTypeMatchFromString(eDataType, initialValue);
         case OseeDslPackage.XRELATION_SIDE_ENUM:
            return createXRelationSideEnumFromString(eDataType, initialValue);
         default:
            throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier");
      }
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public String convertToString(EDataType eDataType, Object instanceValue) {
      switch (eDataType.getClassifierID()) {
         case OseeDslPackage.RELATION_MULTIPLICITY_ENUM:
            return convertRelationMultiplicityEnumToString(eDataType, instanceValue);
         case OseeDslPackage.COMPARE_OP:
            return convertCompareOpToString(eDataType, instanceValue);
         case OseeDslPackage.XLOGIC_OPERATOR:
            return convertXLogicOperatorToString(eDataType, instanceValue);
         case OseeDslPackage.MATCH_FIELD:
            return convertMatchFieldToString(eDataType, instanceValue);
         case OseeDslPackage.ACCESS_PERMISSION_ENUM:
            return convertAccessPermissionEnumToString(eDataType, instanceValue);
         case OseeDslPackage.RELATION_TYPE_MATCH:
            return convertRelationTypeMatchToString(eDataType, instanceValue);
         case OseeDslPackage.XRELATION_SIDE_ENUM:
            return convertXRelationSideEnumToString(eDataType, instanceValue);
         default:
            throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier");
      }
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public OseeDsl createOseeDsl() {
      OseeDslImpl oseeDsl = new OseeDslImpl();
      return oseeDsl;
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public Import createImport() {
      ImportImpl import_ = new ImportImpl();
      return import_;
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public OseeElement createOseeElement() {
      OseeElementImpl oseeElement = new OseeElementImpl();
      return oseeElement;
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public OseeType createOseeType() {
      OseeTypeImpl oseeType = new OseeTypeImpl();
      return oseeType;
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public XArtifactType createXArtifactType() {
      XArtifactTypeImpl xArtifactType = new XArtifactTypeImpl();
      return xArtifactType;
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public XAttributeTypeRef createXAttributeTypeRef() {
      XAttributeTypeRefImpl xAttributeTypeRef = new XAttributeTypeRefImpl();
      return xAttributeTypeRef;
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public XAttributeType createXAttributeType() {
      XAttributeTypeImpl xAttributeType = new XAttributeTypeImpl();
      return xAttributeType;
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public XOseeEnumType createXOseeEnumType() {
      XOseeEnumTypeImpl xOseeEnumType = new XOseeEnumTypeImpl();
      return xOseeEnumType;
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public XOseeEnumEntry createXOseeEnumEntry() {
      XOseeEnumEntryImpl xOseeEnumEntry = new XOseeEnumEntryImpl();
      return xOseeEnumEntry;
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public XOseeEnumOverride createXOseeEnumOverride() {
      XOseeEnumOverrideImpl xOseeEnumOverride = new XOseeEnumOverrideImpl();
      return xOseeEnumOverride;
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public OverrideOption createOverrideOption() {
      OverrideOptionImpl overrideOption = new OverrideOptionImpl();
      return overrideOption;
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public AddEnum createAddEnum() {
      AddEnumImpl addEnum = new AddEnumImpl();
      return addEnum;
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public RemoveEnum createRemoveEnum() {
      RemoveEnumImpl removeEnum = new RemoveEnumImpl();
      return removeEnum;
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public XOseeArtifactTypeOverride createXOseeArtifactTypeOverride() {
      XOseeArtifactTypeOverrideImpl xOseeArtifactTypeOverride = new XOseeArtifactTypeOverrideImpl();
      return xOseeArtifactTypeOverride;
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public AttributeOverrideOption createAttributeOverrideOption() {
      AttributeOverrideOptionImpl attributeOverrideOption = new AttributeOverrideOptionImpl();
      return attributeOverrideOption;
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public AddAttribute createAddAttribute() {
      AddAttributeImpl addAttribute = new AddAttributeImpl();
      return addAttribute;
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public RemoveAttribute createRemoveAttribute() {
      RemoveAttributeImpl removeAttribute = new RemoveAttributeImpl();
      return removeAttribute;
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public UpdateAttribute createUpdateAttribute() {
      UpdateAttributeImpl updateAttribute = new UpdateAttributeImpl();
      return updateAttribute;
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public XRelationType createXRelationType() {
      XRelationTypeImpl xRelationType = new XRelationTypeImpl();
      return xRelationType;
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public Condition createCondition() {
      ConditionImpl condition = new ConditionImpl();
      return condition;
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public SimpleCondition createSimpleCondition() {
      SimpleConditionImpl simpleCondition = new SimpleConditionImpl();
      return simpleCondition;
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public CompoundCondition createCompoundCondition() {
      CompoundConditionImpl compoundCondition = new CompoundConditionImpl();
      return compoundCondition;
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public XArtifactMatcher createXArtifactMatcher() {
      XArtifactMatcherImpl xArtifactMatcher = new XArtifactMatcherImpl();
      return xArtifactMatcher;
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public Role createRole() {
      RoleImpl role = new RoleImpl();
      return role;
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public ReferencedContext createReferencedContext() {
      ReferencedContextImpl referencedContext = new ReferencedContextImpl();
      return referencedContext;
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public UsersAndGroups createUsersAndGroups() {
      UsersAndGroupsImpl usersAndGroups = new UsersAndGroupsImpl();
      return usersAndGroups;
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public AccessContext createAccessContext() {
      AccessContextImpl accessContext = new AccessContextImpl();
      return accessContext;
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public HierarchyRestriction createHierarchyRestriction() {
      HierarchyRestrictionImpl hierarchyRestriction = new HierarchyRestrictionImpl();
      return hierarchyRestriction;
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public RelationTypeArtifactTypePredicate createRelationTypeArtifactTypePredicate() {
      RelationTypeArtifactTypePredicateImpl relationTypeArtifactTypePredicate =
         new RelationTypeArtifactTypePredicateImpl();
      return relationTypeArtifactTypePredicate;
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public RelationTypeArtifactPredicate createRelationTypeArtifactPredicate() {
      RelationTypeArtifactPredicateImpl relationTypeArtifactPredicate = new RelationTypeArtifactPredicateImpl();
      return relationTypeArtifactPredicate;
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public RelationTypePredicate createRelationTypePredicate() {
      RelationTypePredicateImpl relationTypePredicate = new RelationTypePredicateImpl();
      return relationTypePredicate;
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public ObjectRestriction createObjectRestriction() {
      ObjectRestrictionImpl objectRestriction = new ObjectRestrictionImpl();
      return objectRestriction;
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public ArtifactMatchRestriction createArtifactMatchRestriction() {
      ArtifactMatchRestrictionImpl artifactMatchRestriction = new ArtifactMatchRestrictionImpl();
      return artifactMatchRestriction;
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public ArtifactTypeRestriction createArtifactTypeRestriction() {
      ArtifactTypeRestrictionImpl artifactTypeRestriction = new ArtifactTypeRestrictionImpl();
      return artifactTypeRestriction;
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public AttributeTypeRestriction createAttributeTypeRestriction() {
      AttributeTypeRestrictionImpl attributeTypeRestriction = new AttributeTypeRestrictionImpl();
      return attributeTypeRestriction;
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public LegacyRelationTypeRestriction createLegacyRelationTypeRestriction() {
      LegacyRelationTypeRestrictionImpl legacyRelationTypeRestriction = new LegacyRelationTypeRestrictionImpl();
      return legacyRelationTypeRestriction;
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public RelationTypeRestriction createRelationTypeRestriction() {
      RelationTypeRestrictionImpl relationTypeRestriction = new RelationTypeRestrictionImpl();
      return relationTypeRestriction;
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   public RelationMultiplicityEnum createRelationMultiplicityEnumFromString(EDataType eDataType, String initialValue) {
      RelationMultiplicityEnum result = RelationMultiplicityEnum.get(initialValue);
      if (result == null) {
         throw new IllegalArgumentException(
            "The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
      }
      return result;
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   public String convertRelationMultiplicityEnumToString(EDataType eDataType, Object instanceValue) {
      return instanceValue == null ? null : instanceValue.toString();
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   public CompareOp createCompareOpFromString(EDataType eDataType, String initialValue) {
      CompareOp result = CompareOp.get(initialValue);
      if (result == null) {
         throw new IllegalArgumentException(
            "The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
      }
      return result;
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   public String convertCompareOpToString(EDataType eDataType, Object instanceValue) {
      return instanceValue == null ? null : instanceValue.toString();
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   public XLogicOperator createXLogicOperatorFromString(EDataType eDataType, String initialValue) {
      XLogicOperator result = XLogicOperator.get(initialValue);
      if (result == null) {
         throw new IllegalArgumentException(
            "The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
      }
      return result;
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   public String convertXLogicOperatorToString(EDataType eDataType, Object instanceValue) {
      return instanceValue == null ? null : instanceValue.toString();
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   public MatchField createMatchFieldFromString(EDataType eDataType, String initialValue) {
      MatchField result = MatchField.get(initialValue);
      if (result == null) {
         throw new IllegalArgumentException(
            "The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
      }
      return result;
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   public String convertMatchFieldToString(EDataType eDataType, Object instanceValue) {
      return instanceValue == null ? null : instanceValue.toString();
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   public AccessPermissionEnum createAccessPermissionEnumFromString(EDataType eDataType, String initialValue) {
      AccessPermissionEnum result = AccessPermissionEnum.get(initialValue);
      if (result == null) {
         throw new IllegalArgumentException(
            "The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
      }
      return result;
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   public String convertAccessPermissionEnumToString(EDataType eDataType, Object instanceValue) {
      return instanceValue == null ? null : instanceValue.toString();
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   public RelationTypeMatch createRelationTypeMatchFromString(EDataType eDataType, String initialValue) {
      RelationTypeMatch result = RelationTypeMatch.get(initialValue);
      if (result == null) {
         throw new IllegalArgumentException(
            "The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
      }
      return result;
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   public String convertRelationTypeMatchToString(EDataType eDataType, Object instanceValue) {
      return instanceValue == null ? null : instanceValue.toString();
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   public XRelationSideEnum createXRelationSideEnumFromString(EDataType eDataType, String initialValue) {
      XRelationSideEnum result = XRelationSideEnum.get(initialValue);
      if (result == null) {
         throw new IllegalArgumentException(
            "The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
      }
      return result;
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   public String convertXRelationSideEnumToString(EDataType eDataType, Object instanceValue) {
      return instanceValue == null ? null : instanceValue.toString();
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public OseeDslPackage getOseeDslPackage() {
      return (OseeDslPackage) getEPackage();
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @deprecated
    * @generated
    */
   @Deprecated
   public static OseeDslPackage getPackage() {
      return OseeDslPackage.eINSTANCE;
   }

} //OseeDslFactoryImpl
