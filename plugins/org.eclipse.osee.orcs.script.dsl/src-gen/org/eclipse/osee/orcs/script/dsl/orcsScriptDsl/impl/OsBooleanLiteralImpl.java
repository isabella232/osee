/**
 */
package org.eclipse.osee.orcs.script.dsl.orcsScriptDsl.impl;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.osee.orcs.script.dsl.orcsScriptDsl.OrcsScriptDslPackage;
import org.eclipse.osee.orcs.script.dsl.orcsScriptDsl.OsBooleanLiteral;

/**
 * <!-- begin-user-doc --> An implementation of the model object '<em><b>Os Boolean Literal</b></em>'. <!-- end-user-doc
 * -->
 * <p>
 * The following features are implemented:
 * <ul>
 * <li>{@link org.eclipse.osee.orcs.script.dsl.orcsScriptDsl.impl.OsBooleanLiteralImpl#isIsTrue <em>Is True</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class OsBooleanLiteralImpl extends OsExpressionImpl implements OsBooleanLiteral {
   /**
    * The default value of the '{@link #isIsTrue() <em>Is True</em>}' attribute. <!-- begin-user-doc --> <!--
    * end-user-doc -->
    * 
    * @see #isIsTrue()
    * @generated
    * @ordered
    */
   protected static final boolean IS_TRUE_EDEFAULT = false;

   /**
    * The cached value of the '{@link #isIsTrue() <em>Is True</em>}' attribute. <!-- begin-user-doc --> <!--
    * end-user-doc -->
    * 
    * @see #isIsTrue()
    * @generated
    * @ordered
    */
   protected boolean isTrue = IS_TRUE_EDEFAULT;

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   protected OsBooleanLiteralImpl() {
      super();
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   protected EClass eStaticClass() {
      return OrcsScriptDslPackage.Literals.OS_BOOLEAN_LITERAL;
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public boolean isIsTrue() {
      return isTrue;
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public void setIsTrue(boolean newIsTrue) {
      boolean oldIsTrue = isTrue;
      isTrue = newIsTrue;
      if (eNotificationRequired()) {
         eNotify(new ENotificationImpl(this, Notification.SET,
            OrcsScriptDslPackage.OS_BOOLEAN_LITERAL__IS_TRUE, oldIsTrue, isTrue));
      }
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public Object eGet(int featureID, boolean resolve, boolean coreType) {
      switch (featureID) {
         case OrcsScriptDslPackage.OS_BOOLEAN_LITERAL__IS_TRUE:
            return isIsTrue();
      }
      return super.eGet(featureID, resolve, coreType);
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public void eSet(int featureID, Object newValue) {
      switch (featureID) {
         case OrcsScriptDslPackage.OS_BOOLEAN_LITERAL__IS_TRUE:
            setIsTrue((Boolean) newValue);
            return;
      }
      super.eSet(featureID, newValue);
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public void eUnset(int featureID) {
      switch (featureID) {
         case OrcsScriptDslPackage.OS_BOOLEAN_LITERAL__IS_TRUE:
            setIsTrue(IS_TRUE_EDEFAULT);
            return;
      }
      super.eUnset(featureID);
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public boolean eIsSet(int featureID) {
      switch (featureID) {
         case OrcsScriptDslPackage.OS_BOOLEAN_LITERAL__IS_TRUE:
            return isTrue != IS_TRUE_EDEFAULT;
      }
      return super.eIsSet(featureID);
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public String toString() {
      if (eIsProxy()) {
         return super.toString();
      }

      StringBuffer result = new StringBuffer(super.toString());
      result.append(" (isTrue: ");
      result.append(isTrue);
      result.append(')');
      return result.toString();
   }

} //OsBooleanLiteralImpl
