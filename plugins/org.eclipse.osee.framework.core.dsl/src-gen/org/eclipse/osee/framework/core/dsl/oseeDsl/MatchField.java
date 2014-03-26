/**
 */
package org.eclipse.osee.framework.core.dsl.oseeDsl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.Enumerator;

/**
 * <!-- begin-user-doc -->
 * A representation of the literals of the enumeration '<em><b>Match Field</b></em>',
 * and utility methods for working with them.
 * <!-- end-user-doc -->
 * @see org.eclipse.osee.framework.core.dsl.oseeDsl.OseeDslPackage#getMatchField()
 * @model
 * @generated
 */
public enum MatchField implements Enumerator
{
  /**
   * The '<em><b>Artifact Name</b></em>' literal object.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #ARTIFACT_NAME_VALUE
   * @generated
   * @ordered
   */
  ARTIFACT_NAME(0, "artifactName", "artifactName"),

  /**
   * The '<em><b>Artifact Guid</b></em>' literal object.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #ARTIFACT_GUID_VALUE
   * @generated
   * @ordered
   */
  ARTIFACT_GUID(1, "artifactGuid", "artifactGuid"),

  /**
   * The '<em><b>Branch Name</b></em>' literal object.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #BRANCH_NAME_VALUE
   * @generated
   * @ordered
   */
  BRANCH_NAME(2, "branchName", "branchName"),

  /**
   * The '<em><b>Branch Guid</b></em>' literal object.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #BRANCH_GUID_VALUE
   * @generated
   * @ordered
   */
  BRANCH_GUID(3, "branchGuid", "branchGuid");

  /**
   * The '<em><b>Artifact Name</b></em>' literal value.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of '<em><b>Artifact Name</b></em>' literal object isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @see #ARTIFACT_NAME
   * @model name="artifactName"
   * @generated
   * @ordered
   */
  public static final int ARTIFACT_NAME_VALUE = 0;

  /**
   * The '<em><b>Artifact Guid</b></em>' literal value.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of '<em><b>Artifact Guid</b></em>' literal object isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @see #ARTIFACT_GUID
   * @model name="artifactGuid"
   * @generated
   * @ordered
   */
  public static final int ARTIFACT_GUID_VALUE = 1;

  /**
   * The '<em><b>Branch Name</b></em>' literal value.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of '<em><b>Branch Name</b></em>' literal object isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @see #BRANCH_NAME
   * @model name="branchName"
   * @generated
   * @ordered
   */
  public static final int BRANCH_NAME_VALUE = 2;

  /**
   * The '<em><b>Branch Guid</b></em>' literal value.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of '<em><b>Branch Guid</b></em>' literal object isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @see #BRANCH_GUID
   * @model name="branchGuid"
   * @generated
   * @ordered
   */
  public static final int BRANCH_GUID_VALUE = 3;

  /**
   * An array of all the '<em><b>Match Field</b></em>' enumerators.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private static final MatchField[] VALUES_ARRAY =
    new MatchField[]
    {
      ARTIFACT_NAME,
      ARTIFACT_GUID,
      BRANCH_NAME,
      BRANCH_GUID,
    };

  /**
   * A public read-only list of all the '<em><b>Match Field</b></em>' enumerators.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public static final List<MatchField> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

  /**
   * Returns the '<em><b>Match Field</b></em>' literal with the specified literal value.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public static MatchField get(String literal)
  {
    for (int i = 0; i < VALUES_ARRAY.length; ++i)
    {
      MatchField result = VALUES_ARRAY[i];
      if (result.toString().equals(literal))
      {
        return result;
      }
    }
    return null;
  }

  /**
   * Returns the '<em><b>Match Field</b></em>' literal with the specified name.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public static MatchField getByName(String name)
  {
    for (int i = 0; i < VALUES_ARRAY.length; ++i)
    {
      MatchField result = VALUES_ARRAY[i];
      if (result.getName().equals(name))
      {
        return result;
      }
    }
    return null;
  }

  /**
   * Returns the '<em><b>Match Field</b></em>' literal with the specified integer value.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public static MatchField get(int value)
  {
    switch (value)
    {
      case ARTIFACT_NAME_VALUE: return ARTIFACT_NAME;
      case ARTIFACT_GUID_VALUE: return ARTIFACT_GUID;
      case BRANCH_NAME_VALUE: return BRANCH_NAME;
      case BRANCH_GUID_VALUE: return BRANCH_GUID;
    }
    return null;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private final int value;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private final String name;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private final String literal;

  /**
   * Only this class can construct instances.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private MatchField(int value, String name, String literal)
  {
    this.value = value;
    this.name = name;
    this.literal = literal;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public int getValue()
  {
    return value;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getName()
  {
    return name;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getLiteral()
  {
    return literal;
  }

  /**
   * Returns the literal value of the enumerator, which is its string representation.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public String toString()
  {
    return literal;
  }
  
} //MatchField
