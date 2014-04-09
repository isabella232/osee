/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.eclipse.osee.framework.core.dsl.oseeDsl.provider;

import java.util.Collection;
import java.util.List;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.ResourceLocator;
import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;
import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
import org.eclipse.emf.edit.provider.IItemLabelProvider;
import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.IItemPropertySource;
import org.eclipse.emf.edit.provider.IStructuredItemContentProvider;
import org.eclipse.emf.edit.provider.ITreeItemContentProvider;
import org.eclipse.emf.edit.provider.ItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.ItemProviderAdapter;
import org.eclipse.emf.edit.provider.ViewerNotification;
import org.eclipse.osee.framework.core.dsl.oseeDsl.OseeDslPackage;
import org.eclipse.osee.framework.core.dsl.oseeDsl.XAttributeTypeRef;

/**
 * This is the item provider adapter for a {@link org.eclipse.osee.framework.core.dsl.oseeDsl.XAttributeTypeRef} object.
 * <!-- begin-user-doc --> <!-- end-user-doc -->
 * 
 * @generated
 */
public class XAttributeTypeRefItemProvider extends ItemProviderAdapter implements IEditingDomainItemProvider, IStructuredItemContentProvider, ITreeItemContentProvider, IItemLabelProvider, IItemPropertySource {
   /**
    * This constructs an instance from a factory and a notifier. <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   public XAttributeTypeRefItemProvider(AdapterFactory adapterFactory) {
      super(adapterFactory);
   }

   /**
    * This returns the property descriptors for the adapted class. <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public List<IItemPropertyDescriptor> getPropertyDescriptors(Object object) {
      if (itemPropertyDescriptors == null) {
         super.getPropertyDescriptors(object);

         addValidAttributeTypePropertyDescriptor(object);
         addBranchGuidPropertyDescriptor(object);
      }
      return itemPropertyDescriptors;
   }

   /**
    * This adds a property descriptor for the Valid Attribute Type feature. <!-- begin-user-doc --> <!-- end-user-doc
    * -->
    * 
    * @generated
    */
   protected void addValidAttributeTypePropertyDescriptor(Object object) {
      itemPropertyDescriptors.add(createItemPropertyDescriptor(
         ((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(),
         getResourceLocator(),
         getString("_UI_XAttributeTypeRef_validAttributeType_feature"),
         getString("_UI_PropertyDescriptor_description", "_UI_XAttributeTypeRef_validAttributeType_feature",
            "_UI_XAttributeTypeRef_type"), OseeDslPackage.Literals.XATTRIBUTE_TYPE_REF__VALID_ATTRIBUTE_TYPE, true,
         false, true, null, null, null));
   }

   /**
    * This adds a property descriptor for the Branch Guid feature. <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   protected void addBranchGuidPropertyDescriptor(Object object) {
      itemPropertyDescriptors.add(createItemPropertyDescriptor(
         ((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(),
         getResourceLocator(),
         getString("_UI_XAttributeTypeRef_branchGuid_feature"),
         getString("_UI_PropertyDescriptor_description", "_UI_XAttributeTypeRef_branchGuid_feature",
            "_UI_XAttributeTypeRef_type"), OseeDslPackage.Literals.XATTRIBUTE_TYPE_REF__BRANCH_UUID, true, false,
         false, ItemPropertyDescriptor.GENERIC_VALUE_IMAGE, null, null));
   }

   /**
    * This returns XAttributeTypeRef.gif. <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public Object getImage(Object object) {
      return overlayImage(object, getResourceLocator().getImage("full/obj16/XAttributeTypeRef"));
   }

   /**
    * This returns the label text for the adapted class. <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public String getText(Object object) {
      String label = String.valueOf(((XAttributeTypeRef) object).getBranchUuid());
      return label == null || label.length() == 0 ? getString("_UI_XAttributeTypeRef_type") : getString("_UI_XAttributeTypeRef_type") + " " + label;
   }

   /**
    * This handles model notifications by calling {@link #updateChildren} to update any cached children and by creating
    * a viewer notification, which it passes to {@link #fireNotifyChanged}. <!-- begin-user-doc --> <!-- end-user-doc
    * -->
    * 
    * @generated
    */
   @Override
   public void notifyChanged(Notification notification) {
      updateChildren(notification);

      switch (notification.getFeatureID(XAttributeTypeRef.class)) {
         case OseeDslPackage.XATTRIBUTE_TYPE_REF__BRANCH_UUID:
            fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), false, true));
            return;
      }
      super.notifyChanged(notification);
   }

   /**
    * This adds {@link org.eclipse.emf.edit.command.CommandParameter}s describing the children that can be created under
    * this object. <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   protected void collectNewChildDescriptors(Collection<Object> newChildDescriptors, Object object) {
      super.collectNewChildDescriptors(newChildDescriptors, object);
   }

   /**
    * Return the resource locator for this item provider's resources. <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public ResourceLocator getResourceLocator() {
      return OseeDslEditPlugin.INSTANCE;
   }

}
