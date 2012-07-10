/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package ch.psi.sics.hipadaba.util;

import ch.psi.sics.hipadaba.impl.HipadabaPackageImpl;

import java.util.Map;

import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.resource.Resource;

import org.eclipse.emf.ecore.xmi.util.XMLProcessor;

/**
 * This class contains helper methods to serialize and deserialize XML documents
 * <!-- begin-user-doc -->
 * <!-- end-user-doc -->
 * @generated
 */
public class HipadabaXMLProcessor extends XMLProcessor {

	/**
	 * Public constructor to instantiate the helper.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public HipadabaXMLProcessor() {
		super((EPackage.Registry.INSTANCE));
		HipadabaPackageImpl.eINSTANCE.eClass();
	}
	
	/**
	 * Register for "*" and "xml" file extensions the HipadabaResourceFactoryImpl factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected Map<String, Resource.Factory> getRegistrations() {
		if (registrations == null) {
			super.getRegistrations();
			registrations.put(XML_EXTENSION, new HipadabaResourceFactoryImpl());
			registrations.put(STAR_EXTENSION, new HipadabaResourceFactoryImpl());
		}
		return registrations;
	}

} //HipadabaXMLProcessor
