/**
 */
package org.eclipse.osee.framework.core.dsl.oseeDsl.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.osee.framework.core.dsl.oseeDsl.ArtifactMatchRestriction;
import org.eclipse.osee.framework.core.dsl.oseeDsl.OseeDslPackage;
import org.eclipse.osee.framework.core.dsl.oseeDsl.XArtifactMatcher;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Artifact Match Restriction</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.eclipse.osee.framework.core.dsl.oseeDsl.impl.ArtifactMatchRestrictionImpl#getArtifactMatcherRef <em>Artifact Matcher Ref</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ArtifactMatchRestrictionImpl extends ObjectRestrictionImpl implements ArtifactMatchRestriction
{
  /**
    * The cached value of the '{@link #getArtifactMatcherRef() <em>Artifact Matcher Ref</em>}' reference.
    * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
    * @see #getArtifactMatcherRef()
    * @generated
    * @ordered
    */
  protected XArtifactMatcher artifactMatcherRef;

  /**
    * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
    * @generated
    */
  protected ArtifactMatchRestrictionImpl()
  {
      super();
   }

  /**
    * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
    * @generated
    */
  @Override
  protected EClass eStaticClass()
  {
      return OseeDslPackage.Literals.ARTIFACT_MATCH_RESTRICTION;
   }

  /**
    * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
    * @generated
    */
  public XArtifactMatcher getArtifactMatcherRef()
  {
      if (artifactMatcherRef != null && artifactMatcherRef.eIsProxy()) {
         InternalEObject oldArtifactMatcherRef = (InternalEObject)artifactMatcherRef;
         artifactMatcherRef = (XArtifactMatcher)eResolveProxy(oldArtifactMatcherRef);
         if (artifactMatcherRef != oldArtifactMatcherRef) {
            if (eNotificationRequired())
               eNotify(new ENotificationImpl(this, Notification.RESOLVE, OseeDslPackage.ARTIFACT_MATCH_RESTRICTION__ARTIFACT_MATCHER_REF, oldArtifactMatcherRef, artifactMatcherRef));
         }
      }
      return artifactMatcherRef;
   }

  /**
    * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
    * @generated
    */
  public XArtifactMatcher basicGetArtifactMatcherRef()
  {
      return artifactMatcherRef;
   }

  /**
    * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
    * @generated
    */
  public void setArtifactMatcherRef(XArtifactMatcher newArtifactMatcherRef)
  {
      XArtifactMatcher oldArtifactMatcherRef = artifactMatcherRef;
      artifactMatcherRef = newArtifactMatcherRef;
      if (eNotificationRequired())
         eNotify(new ENotificationImpl(this, Notification.SET, OseeDslPackage.ARTIFACT_MATCH_RESTRICTION__ARTIFACT_MATCHER_REF, oldArtifactMatcherRef, artifactMatcherRef));
   }

  /**
    * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
    * @generated
    */
  @Override
  public Object eGet(int featureID, boolean resolve, boolean coreType)
  {
      switch (featureID) {
         case OseeDslPackage.ARTIFACT_MATCH_RESTRICTION__ARTIFACT_MATCHER_REF:
            if (resolve) return getArtifactMatcherRef();
            return basicGetArtifactMatcherRef();
      }
      return super.eGet(featureID, resolve, coreType);
   }

  /**
    * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
    * @generated
    */
  @Override
  public void eSet(int featureID, Object newValue)
  {
      switch (featureID) {
         case OseeDslPackage.ARTIFACT_MATCH_RESTRICTION__ARTIFACT_MATCHER_REF:
            setArtifactMatcherRef((XArtifactMatcher)newValue);
            return;
      }
      super.eSet(featureID, newValue);
   }

  /**
    * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
    * @generated
    */
  @Override
  public void eUnset(int featureID)
  {
      switch (featureID) {
         case OseeDslPackage.ARTIFACT_MATCH_RESTRICTION__ARTIFACT_MATCHER_REF:
            setArtifactMatcherRef((XArtifactMatcher)null);
            return;
      }
      super.eUnset(featureID);
   }

  /**
    * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
    * @generated
    */
  @Override
  public boolean eIsSet(int featureID)
  {
      switch (featureID) {
         case OseeDslPackage.ARTIFACT_MATCH_RESTRICTION__ARTIFACT_MATCHER_REF:
            return artifactMatcherRef != null;
      }
      return super.eIsSet(featureID);
   }

} //ArtifactMatchRestrictionImpl
