/**
 */
package org.eclipse.osee.framework.core.dsl.oseeDsl;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Add Attribute</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.osee.framework.core.dsl.oseeDsl.AddAttribute#getAttribute <em>Attribute</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.osee.framework.core.dsl.oseeDsl.OseeDslPackage#getAddAttribute()
 * @model
 * @generated
 */
public interface AddAttribute extends AttributeOverrideOption
{
  /**
   * Returns the value of the '<em><b>Attribute</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Attribute</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Attribute</em>' containment reference.
   * @see #setAttribute(XAttributeTypeRef)
   * @see org.eclipse.osee.framework.core.dsl.oseeDsl.OseeDslPackage#getAddAttribute_Attribute()
   * @model containment="true"
   * @generated
   */
  XAttributeTypeRef getAttribute();

  /**
   * Sets the value of the '{@link org.eclipse.osee.framework.core.dsl.oseeDsl.AddAttribute#getAttribute <em>Attribute</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Attribute</em>' containment reference.
   * @see #getAttribute()
   * @generated
   */
  void setAttribute(XAttributeTypeRef value);

} // AddAttribute
