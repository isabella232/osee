/**
 */
package org.eclipse.osee.framework.core.dsl.oseeDsl.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.osee.framework.core.dsl.oseeDsl.OseeDslPackage;
import org.eclipse.osee.framework.core.dsl.oseeDsl.XAttributeType;
import org.eclipse.osee.framework.core.dsl.oseeDsl.XOseeEnumType;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>XAttribute Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.eclipse.osee.framework.core.dsl.oseeDsl.impl.XAttributeTypeImpl#getBaseAttributeType <em>Base Attribute Type</em>}</li>
 *   <li>{@link org.eclipse.osee.framework.core.dsl.oseeDsl.impl.XAttributeTypeImpl#getOverride <em>Override</em>}</li>
 *   <li>{@link org.eclipse.osee.framework.core.dsl.oseeDsl.impl.XAttributeTypeImpl#getDataProvider <em>Data Provider</em>}</li>
 *   <li>{@link org.eclipse.osee.framework.core.dsl.oseeDsl.impl.XAttributeTypeImpl#getMin <em>Min</em>}</li>
 *   <li>{@link org.eclipse.osee.framework.core.dsl.oseeDsl.impl.XAttributeTypeImpl#getMax <em>Max</em>}</li>
 *   <li>{@link org.eclipse.osee.framework.core.dsl.oseeDsl.impl.XAttributeTypeImpl#getTaggerId <em>Tagger Id</em>}</li>
 *   <li>{@link org.eclipse.osee.framework.core.dsl.oseeDsl.impl.XAttributeTypeImpl#getEnumType <em>Enum Type</em>}</li>
 *   <li>{@link org.eclipse.osee.framework.core.dsl.oseeDsl.impl.XAttributeTypeImpl#getDescription <em>Description</em>}</li>
 *   <li>{@link org.eclipse.osee.framework.core.dsl.oseeDsl.impl.XAttributeTypeImpl#getDefaultValue <em>Default Value</em>}</li>
 *   <li>{@link org.eclipse.osee.framework.core.dsl.oseeDsl.impl.XAttributeTypeImpl#getFileExtension <em>File Extension</em>}</li>
 *   <li>{@link org.eclipse.osee.framework.core.dsl.oseeDsl.impl.XAttributeTypeImpl#getMediaType <em>Media Type</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class XAttributeTypeImpl extends OseeTypeImpl implements XAttributeType
{
  /**
   * The default value of the '{@link #getBaseAttributeType() <em>Base Attribute Type</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getBaseAttributeType()
   * @generated
   * @ordered
   */
  protected static final String BASE_ATTRIBUTE_TYPE_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getBaseAttributeType() <em>Base Attribute Type</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getBaseAttributeType()
   * @generated
   * @ordered
   */
  protected String baseAttributeType = BASE_ATTRIBUTE_TYPE_EDEFAULT;

  /**
   * The cached value of the '{@link #getOverride() <em>Override</em>}' reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getOverride()
   * @generated
   * @ordered
   */
  protected XAttributeType override;

  /**
   * The default value of the '{@link #getDataProvider() <em>Data Provider</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getDataProvider()
   * @generated
   * @ordered
   */
  protected static final String DATA_PROVIDER_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getDataProvider() <em>Data Provider</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getDataProvider()
   * @generated
   * @ordered
   */
  protected String dataProvider = DATA_PROVIDER_EDEFAULT;

  /**
   * The default value of the '{@link #getMin() <em>Min</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getMin()
   * @generated
   * @ordered
   */
  protected static final String MIN_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getMin() <em>Min</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getMin()
   * @generated
   * @ordered
   */
  protected String min = MIN_EDEFAULT;

  /**
   * The default value of the '{@link #getMax() <em>Max</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getMax()
   * @generated
   * @ordered
   */
  protected static final String MAX_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getMax() <em>Max</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getMax()
   * @generated
   * @ordered
   */
  protected String max = MAX_EDEFAULT;

  /**
   * The default value of the '{@link #getTaggerId() <em>Tagger Id</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getTaggerId()
   * @generated
   * @ordered
   */
  protected static final String TAGGER_ID_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getTaggerId() <em>Tagger Id</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getTaggerId()
   * @generated
   * @ordered
   */
  protected String taggerId = TAGGER_ID_EDEFAULT;

  /**
   * The cached value of the '{@link #getEnumType() <em>Enum Type</em>}' reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getEnumType()
   * @generated
   * @ordered
   */
  protected XOseeEnumType enumType;

  /**
   * The default value of the '{@link #getDescription() <em>Description</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getDescription()
   * @generated
   * @ordered
   */
  protected static final String DESCRIPTION_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getDescription() <em>Description</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getDescription()
   * @generated
   * @ordered
   */
  protected String description = DESCRIPTION_EDEFAULT;

  /**
   * The default value of the '{@link #getDefaultValue() <em>Default Value</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getDefaultValue()
   * @generated
   * @ordered
   */
  protected static final String DEFAULT_VALUE_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getDefaultValue() <em>Default Value</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getDefaultValue()
   * @generated
   * @ordered
   */
  protected String defaultValue = DEFAULT_VALUE_EDEFAULT;

  /**
   * The default value of the '{@link #getFileExtension() <em>File Extension</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getFileExtension()
   * @generated
   * @ordered
   */
  protected static final String FILE_EXTENSION_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getFileExtension() <em>File Extension</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getFileExtension()
   * @generated
   * @ordered
   */
  protected String fileExtension = FILE_EXTENSION_EDEFAULT;

  /**
   * The default value of the '{@link #getMediaType() <em>Media Type</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getMediaType()
   * @generated
   * @ordered
   */
  protected static final String MEDIA_TYPE_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getMediaType() <em>Media Type</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getMediaType()
   * @generated
   * @ordered
   */
  protected String mediaType = MEDIA_TYPE_EDEFAULT;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected XAttributeTypeImpl()
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
    return OseeDslPackage.Literals.XATTRIBUTE_TYPE;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getBaseAttributeType()
  {
    return baseAttributeType;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setBaseAttributeType(String newBaseAttributeType)
  {
    String oldBaseAttributeType = baseAttributeType;
    baseAttributeType = newBaseAttributeType;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, OseeDslPackage.XATTRIBUTE_TYPE__BASE_ATTRIBUTE_TYPE, oldBaseAttributeType, baseAttributeType));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public XAttributeType getOverride()
  {
    if (override != null && override.eIsProxy())
    {
      InternalEObject oldOverride = (InternalEObject)override;
      override = (XAttributeType)eResolveProxy(oldOverride);
      if (override != oldOverride)
      {
        if (eNotificationRequired())
          eNotify(new ENotificationImpl(this, Notification.RESOLVE, OseeDslPackage.XATTRIBUTE_TYPE__OVERRIDE, oldOverride, override));
      }
    }
    return override;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public XAttributeType basicGetOverride()
  {
    return override;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setOverride(XAttributeType newOverride)
  {
    XAttributeType oldOverride = override;
    override = newOverride;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, OseeDslPackage.XATTRIBUTE_TYPE__OVERRIDE, oldOverride, override));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getDataProvider()
  {
    return dataProvider;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setDataProvider(String newDataProvider)
  {
    String oldDataProvider = dataProvider;
    dataProvider = newDataProvider;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, OseeDslPackage.XATTRIBUTE_TYPE__DATA_PROVIDER, oldDataProvider, dataProvider));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getMin()
  {
    return min;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setMin(String newMin)
  {
    String oldMin = min;
    min = newMin;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, OseeDslPackage.XATTRIBUTE_TYPE__MIN, oldMin, min));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getMax()
  {
    return max;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setMax(String newMax)
  {
    String oldMax = max;
    max = newMax;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, OseeDslPackage.XATTRIBUTE_TYPE__MAX, oldMax, max));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getTaggerId()
  {
    return taggerId;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setTaggerId(String newTaggerId)
  {
    String oldTaggerId = taggerId;
    taggerId = newTaggerId;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, OseeDslPackage.XATTRIBUTE_TYPE__TAGGER_ID, oldTaggerId, taggerId));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public XOseeEnumType getEnumType()
  {
    if (enumType != null && enumType.eIsProxy())
    {
      InternalEObject oldEnumType = (InternalEObject)enumType;
      enumType = (XOseeEnumType)eResolveProxy(oldEnumType);
      if (enumType != oldEnumType)
      {
        if (eNotificationRequired())
          eNotify(new ENotificationImpl(this, Notification.RESOLVE, OseeDslPackage.XATTRIBUTE_TYPE__ENUM_TYPE, oldEnumType, enumType));
      }
    }
    return enumType;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public XOseeEnumType basicGetEnumType()
  {
    return enumType;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setEnumType(XOseeEnumType newEnumType)
  {
    XOseeEnumType oldEnumType = enumType;
    enumType = newEnumType;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, OseeDslPackage.XATTRIBUTE_TYPE__ENUM_TYPE, oldEnumType, enumType));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getDescription()
  {
    return description;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setDescription(String newDescription)
  {
    String oldDescription = description;
    description = newDescription;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, OseeDslPackage.XATTRIBUTE_TYPE__DESCRIPTION, oldDescription, description));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getDefaultValue()
  {
    return defaultValue;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setDefaultValue(String newDefaultValue)
  {
    String oldDefaultValue = defaultValue;
    defaultValue = newDefaultValue;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, OseeDslPackage.XATTRIBUTE_TYPE__DEFAULT_VALUE, oldDefaultValue, defaultValue));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getFileExtension()
  {
    return fileExtension;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setFileExtension(String newFileExtension)
  {
    String oldFileExtension = fileExtension;
    fileExtension = newFileExtension;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, OseeDslPackage.XATTRIBUTE_TYPE__FILE_EXTENSION, oldFileExtension, fileExtension));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getMediaType()
  {
    return mediaType;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setMediaType(String newMediaType)
  {
    String oldMediaType = mediaType;
    mediaType = newMediaType;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, OseeDslPackage.XATTRIBUTE_TYPE__MEDIA_TYPE, oldMediaType, mediaType));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public Object eGet(int featureID, boolean resolve, boolean coreType)
  {
    switch (featureID)
    {
      case OseeDslPackage.XATTRIBUTE_TYPE__BASE_ATTRIBUTE_TYPE:
        return getBaseAttributeType();
      case OseeDslPackage.XATTRIBUTE_TYPE__OVERRIDE:
        if (resolve) return getOverride();
        return basicGetOverride();
      case OseeDslPackage.XATTRIBUTE_TYPE__DATA_PROVIDER:
        return getDataProvider();
      case OseeDslPackage.XATTRIBUTE_TYPE__MIN:
        return getMin();
      case OseeDslPackage.XATTRIBUTE_TYPE__MAX:
        return getMax();
      case OseeDslPackage.XATTRIBUTE_TYPE__TAGGER_ID:
        return getTaggerId();
      case OseeDslPackage.XATTRIBUTE_TYPE__ENUM_TYPE:
        if (resolve) return getEnumType();
        return basicGetEnumType();
      case OseeDslPackage.XATTRIBUTE_TYPE__DESCRIPTION:
        return getDescription();
      case OseeDslPackage.XATTRIBUTE_TYPE__DEFAULT_VALUE:
        return getDefaultValue();
      case OseeDslPackage.XATTRIBUTE_TYPE__FILE_EXTENSION:
        return getFileExtension();
      case OseeDslPackage.XATTRIBUTE_TYPE__MEDIA_TYPE:
        return getMediaType();
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
    switch (featureID)
    {
      case OseeDslPackage.XATTRIBUTE_TYPE__BASE_ATTRIBUTE_TYPE:
        setBaseAttributeType((String)newValue);
        return;
      case OseeDslPackage.XATTRIBUTE_TYPE__OVERRIDE:
        setOverride((XAttributeType)newValue);
        return;
      case OseeDslPackage.XATTRIBUTE_TYPE__DATA_PROVIDER:
        setDataProvider((String)newValue);
        return;
      case OseeDslPackage.XATTRIBUTE_TYPE__MIN:
        setMin((String)newValue);
        return;
      case OseeDslPackage.XATTRIBUTE_TYPE__MAX:
        setMax((String)newValue);
        return;
      case OseeDslPackage.XATTRIBUTE_TYPE__TAGGER_ID:
        setTaggerId((String)newValue);
        return;
      case OseeDslPackage.XATTRIBUTE_TYPE__ENUM_TYPE:
        setEnumType((XOseeEnumType)newValue);
        return;
      case OseeDslPackage.XATTRIBUTE_TYPE__DESCRIPTION:
        setDescription((String)newValue);
        return;
      case OseeDslPackage.XATTRIBUTE_TYPE__DEFAULT_VALUE:
        setDefaultValue((String)newValue);
        return;
      case OseeDslPackage.XATTRIBUTE_TYPE__FILE_EXTENSION:
        setFileExtension((String)newValue);
        return;
      case OseeDslPackage.XATTRIBUTE_TYPE__MEDIA_TYPE:
        setMediaType((String)newValue);
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
    switch (featureID)
    {
      case OseeDslPackage.XATTRIBUTE_TYPE__BASE_ATTRIBUTE_TYPE:
        setBaseAttributeType(BASE_ATTRIBUTE_TYPE_EDEFAULT);
        return;
      case OseeDslPackage.XATTRIBUTE_TYPE__OVERRIDE:
        setOverride((XAttributeType)null);
        return;
      case OseeDslPackage.XATTRIBUTE_TYPE__DATA_PROVIDER:
        setDataProvider(DATA_PROVIDER_EDEFAULT);
        return;
      case OseeDslPackage.XATTRIBUTE_TYPE__MIN:
        setMin(MIN_EDEFAULT);
        return;
      case OseeDslPackage.XATTRIBUTE_TYPE__MAX:
        setMax(MAX_EDEFAULT);
        return;
      case OseeDslPackage.XATTRIBUTE_TYPE__TAGGER_ID:
        setTaggerId(TAGGER_ID_EDEFAULT);
        return;
      case OseeDslPackage.XATTRIBUTE_TYPE__ENUM_TYPE:
        setEnumType((XOseeEnumType)null);
        return;
      case OseeDslPackage.XATTRIBUTE_TYPE__DESCRIPTION:
        setDescription(DESCRIPTION_EDEFAULT);
        return;
      case OseeDslPackage.XATTRIBUTE_TYPE__DEFAULT_VALUE:
        setDefaultValue(DEFAULT_VALUE_EDEFAULT);
        return;
      case OseeDslPackage.XATTRIBUTE_TYPE__FILE_EXTENSION:
        setFileExtension(FILE_EXTENSION_EDEFAULT);
        return;
      case OseeDslPackage.XATTRIBUTE_TYPE__MEDIA_TYPE:
        setMediaType(MEDIA_TYPE_EDEFAULT);
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
    switch (featureID)
    {
      case OseeDslPackage.XATTRIBUTE_TYPE__BASE_ATTRIBUTE_TYPE:
        return BASE_ATTRIBUTE_TYPE_EDEFAULT == null ? baseAttributeType != null : !BASE_ATTRIBUTE_TYPE_EDEFAULT.equals(baseAttributeType);
      case OseeDslPackage.XATTRIBUTE_TYPE__OVERRIDE:
        return override != null;
      case OseeDslPackage.XATTRIBUTE_TYPE__DATA_PROVIDER:
        return DATA_PROVIDER_EDEFAULT == null ? dataProvider != null : !DATA_PROVIDER_EDEFAULT.equals(dataProvider);
      case OseeDslPackage.XATTRIBUTE_TYPE__MIN:
        return MIN_EDEFAULT == null ? min != null : !MIN_EDEFAULT.equals(min);
      case OseeDslPackage.XATTRIBUTE_TYPE__MAX:
        return MAX_EDEFAULT == null ? max != null : !MAX_EDEFAULT.equals(max);
      case OseeDslPackage.XATTRIBUTE_TYPE__TAGGER_ID:
        return TAGGER_ID_EDEFAULT == null ? taggerId != null : !TAGGER_ID_EDEFAULT.equals(taggerId);
      case OseeDslPackage.XATTRIBUTE_TYPE__ENUM_TYPE:
        return enumType != null;
      case OseeDslPackage.XATTRIBUTE_TYPE__DESCRIPTION:
        return DESCRIPTION_EDEFAULT == null ? description != null : !DESCRIPTION_EDEFAULT.equals(description);
      case OseeDslPackage.XATTRIBUTE_TYPE__DEFAULT_VALUE:
        return DEFAULT_VALUE_EDEFAULT == null ? defaultValue != null : !DEFAULT_VALUE_EDEFAULT.equals(defaultValue);
      case OseeDslPackage.XATTRIBUTE_TYPE__FILE_EXTENSION:
        return FILE_EXTENSION_EDEFAULT == null ? fileExtension != null : !FILE_EXTENSION_EDEFAULT.equals(fileExtension);
      case OseeDslPackage.XATTRIBUTE_TYPE__MEDIA_TYPE:
        return MEDIA_TYPE_EDEFAULT == null ? mediaType != null : !MEDIA_TYPE_EDEFAULT.equals(mediaType);
    }
    return super.eIsSet(featureID);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public String toString()
  {
    if (eIsProxy()) return super.toString();

    StringBuffer result = new StringBuffer(super.toString());
    result.append(" (baseAttributeType: ");
    result.append(baseAttributeType);
    result.append(", dataProvider: ");
    result.append(dataProvider);
    result.append(", min: ");
    result.append(min);
    result.append(", max: ");
    result.append(max);
    result.append(", taggerId: ");
    result.append(taggerId);
    result.append(", description: ");
    result.append(description);
    result.append(", defaultValue: ");
    result.append(defaultValue);
    result.append(", fileExtension: ");
    result.append(fileExtension);
    result.append(", mediaType: ");
    result.append(mediaType);
    result.append(')');
    return result.toString();
  }

} //XAttributeTypeImpl
