/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package ch.psi.sics.hipadaba.impl;

import ch.psi.sics.hipadaba.Component;
import ch.psi.sics.hipadaba.DataType;
import ch.psi.sics.hipadaba.DocumentRoot;
import ch.psi.sics.hipadaba.HipadabaFactory;
import ch.psi.sics.hipadaba.Property;

import ch.psi.sics.hipadaba.Value;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import org.eclipse.emf.ecore.xml.type.XMLTypePackage;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see ch.psi.sics.hipadaba.HipadabaFactory
 * @model kind="package"
 * @generated
 */
public class HipadabaPackageImpl extends EPackageImpl {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final String eNAME = "hipadaba";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final String eNS_URI = "http://www.psi.ch/sics/hipadaba";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final String eNS_PREFIX = "hipadaba";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final HipadabaPackageImpl eINSTANCE = ch.psi.sics.hipadaba.impl.HipadabaPackageImpl.init();

	/**
	 * The meta object id for the '{@link ch.psi.sics.hipadaba.impl.ComponentImpl <em>Component</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.psi.sics.hipadaba.impl.ComponentImpl
	 * @see ch.psi.sics.hipadaba.impl.HipadabaPackageImpl#getComponent()
	 * @generated
	 */
	public static final int COMPONENT = 0;

	/**
	 * The feature id for the '<em><b>Value</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int COMPONENT__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Property</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int COMPONENT__PROPERTY = 1;

	/**
	 * The feature id for the '<em><b>Component</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int COMPONENT__COMPONENT = 2;

	/**
	 * The feature id for the '<em><b>Data Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int COMPONENT__DATA_TYPE = 3;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int COMPONENT__ID = 4;

	/**
	 * The number of structural features of the '<em>Component</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int COMPONENT_FEATURE_COUNT = 5;

	/**
	 * The meta object id for the '{@link ch.psi.sics.hipadaba.impl.DocumentRootImpl <em>Document Root</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.psi.sics.hipadaba.impl.DocumentRootImpl
	 * @see ch.psi.sics.hipadaba.impl.HipadabaPackageImpl#getDocumentRoot()
	 * @generated
	 */
	public static final int DOCUMENT_ROOT = 1;

	/**
	 * The feature id for the '<em><b>Mixed</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int DOCUMENT_ROOT__MIXED = 0;

	/**
	 * The feature id for the '<em><b>XMLNS Prefix Map</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int DOCUMENT_ROOT__XMLNS_PREFIX_MAP = 1;

	/**
	 * The feature id for the '<em><b>XSI Schema Location</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int DOCUMENT_ROOT__XSI_SCHEMA_LOCATION = 2;

	/**
	 * The feature id for the '<em><b>SICS</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int DOCUMENT_ROOT__SICS = 3;

	/**
	 * The number of structural features of the '<em>Document Root</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int DOCUMENT_ROOT_FEATURE_COUNT = 4;

	/**
	 * The meta object id for the '{@link ch.psi.sics.hipadaba.impl.PropertyImpl <em>Property</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.psi.sics.hipadaba.impl.PropertyImpl
	 * @see ch.psi.sics.hipadaba.impl.HipadabaPackageImpl#getProperty()
	 * @generated
	 */
	public static final int PROPERTY = 2;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int PROPERTY__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int PROPERTY__ID = 1;

	/**
	 * The number of structural features of the '<em>Property</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int PROPERTY_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link ch.psi.sics.hipadaba.impl.SICSImpl <em>SICS</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.psi.sics.hipadaba.impl.SICSImpl
	 * @see ch.psi.sics.hipadaba.impl.HipadabaPackageImpl#getSICS()
	 * @generated
	 */
	public static final int SICS = 3;

	/**
	 * The feature id for the '<em><b>Component</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int SICS__COMPONENT = 0;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int SICS__ID = 1;

	/**
	 * The number of structural features of the '<em>SICS</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int SICS_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link ch.psi.sics.hipadaba.impl.ValueImpl <em>Value</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.psi.sics.hipadaba.impl.ValueImpl
	 * @see ch.psi.sics.hipadaba.impl.HipadabaPackageImpl#getValue()
	 * @generated
	 */
	public static final int VALUE = 4;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int VALUE__VALUE = 0;

	/**
	 * The number of structural features of the '<em>Value</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int VALUE_FEATURE_COUNT = 1;

	/**
	 * The meta object id for the '{@link ch.psi.sics.hipadaba.DataType <em>Data Type</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.psi.sics.hipadaba.DataType
	 * @see ch.psi.sics.hipadaba.impl.HipadabaPackageImpl#getDataType()
	 * @generated
	 */
	public static final int DATA_TYPE = 5;

	/**
	 * The meta object id for the '<em>Data Type Object</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.psi.sics.hipadaba.DataType
	 * @see ch.psi.sics.hipadaba.impl.HipadabaPackageImpl#getDataTypeObject()
	 * @generated
	 */
	public static final int DATA_TYPE_OBJECT = 6;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass componentEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass documentRootEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass propertyEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass sicsEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass valueEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum dataTypeEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType dataTypeObjectEDataType = null;

	/**
	 * Creates an instance of the model <b>Package</b>, registered with
	 * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the package
	 * package URI value.
	 * <p>Note: the correct way to create the package is via the static
	 * factory method {@link #init init()}, which also performs
	 * initialization of the package, or returns the registered package,
	 * if one already exists.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.emf.ecore.EPackage.Registry
	 * @see ch.psi.sics.hipadaba.impl.HipadabaPackageImpl#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private HipadabaPackageImpl() {
		super(eNS_URI, ((EFactory)HipadabaFactory.INSTANCE));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static boolean isInited = false;

	/**
	 * Creates, registers, and initializes the <b>Package</b> for this model, and for any others upon which it depends.
	 * 
	 * <p>This method is used to initialize {@link HipadabaPackageImpl#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static HipadabaPackageImpl init() {
		if (isInited) return (HipadabaPackageImpl)EPackage.Registry.INSTANCE.getEPackage(HipadabaPackageImpl.eNS_URI);

		// Obtain or create and register package
		HipadabaPackageImpl theHipadabaPackage = (HipadabaPackageImpl)(EPackage.Registry.INSTANCE.get(eNS_URI) instanceof HipadabaPackageImpl ? EPackage.Registry.INSTANCE.get(eNS_URI) : new HipadabaPackageImpl());

		isInited = true;

		// Initialize simple dependencies
		XMLTypePackage.eINSTANCE.eClass();

		// Create package meta-data objects
		theHipadabaPackage.createPackageContents();

		// Initialize created meta-data
		theHipadabaPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theHipadabaPackage.freeze();

  
		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(HipadabaPackageImpl.eNS_URI, theHipadabaPackage);
		return theHipadabaPackage;
	}


	/**
	 * Returns the meta object for class '{@link ch.psi.sics.hipadaba.Component <em>Component</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Component</em>'.
	 * @see ch.psi.sics.hipadaba.Component
	 * @generated
	 */
	public EClass getComponent() {
		return componentEClass;
	}

	/**
	 * Returns the meta object for the containment reference '{@link ch.psi.sics.hipadaba.Component#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Value</em>'.
	 * @see ch.psi.sics.hipadaba.Component#getValue()
	 * @see #getComponent()
	 * @generated
	 */
	public EReference getComponent_Value() {
		return (EReference)componentEClass.getEStructuralFeatures().get(0);
	}


	/**
	 * Returns the meta object for the containment reference list '{@link ch.psi.sics.hipadaba.Component#getComponent <em>Component</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Component</em>'.
	 * @see ch.psi.sics.hipadaba.Component#getComponent()
	 * @see #getComponent()
	 * @generated
	 */
	public EReference getComponent_Component() {
		return (EReference)componentEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * Returns the meta object for the containment reference list '{@link ch.psi.sics.hipadaba.Component#getProperty <em>Property</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Property</em>'.
	 * @see ch.psi.sics.hipadaba.Component#getProperty()
	 * @see #getComponent()
	 * @generated
	 */
	public EReference getComponent_Property() {
		return (EReference)componentEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * Returns the meta object for the attribute '{@link ch.psi.sics.hipadaba.Component#getDataType <em>Data Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Data Type</em>'.
	 * @see ch.psi.sics.hipadaba.Component#getDataType()
	 * @see #getComponent()
	 * @generated
	 */
	public EAttribute getComponent_DataType() {
		return (EAttribute)componentEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * Returns the meta object for the attribute '{@link ch.psi.sics.hipadaba.Component#getId <em>Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Id</em>'.
	 * @see ch.psi.sics.hipadaba.Component#getId()
	 * @see #getComponent()
	 * @generated
	 */
	public EAttribute getComponent_Id() {
		return (EAttribute)componentEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * Returns the meta object for class '{@link ch.psi.sics.hipadaba.DocumentRoot <em>Document Root</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Document Root</em>'.
	 * @see ch.psi.sics.hipadaba.DocumentRoot
	 * @generated
	 */
	public EClass getDocumentRoot() {
		return documentRootEClass;
	}

	/**
	 * Returns the meta object for the attribute list '{@link ch.psi.sics.hipadaba.DocumentRoot#getMixed <em>Mixed</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Mixed</em>'.
	 * @see ch.psi.sics.hipadaba.DocumentRoot#getMixed()
	 * @see #getDocumentRoot()
	 * @generated
	 */
	public EAttribute getDocumentRoot_Mixed() {
		return (EAttribute)documentRootEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * Returns the meta object for the map '{@link ch.psi.sics.hipadaba.DocumentRoot#getXMLNSPrefixMap <em>XMLNS Prefix Map</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the map '<em>XMLNS Prefix Map</em>'.
	 * @see ch.psi.sics.hipadaba.DocumentRoot#getXMLNSPrefixMap()
	 * @see #getDocumentRoot()
	 * @generated
	 */
	public EReference getDocumentRoot_XMLNSPrefixMap() {
		return (EReference)documentRootEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * Returns the meta object for the map '{@link ch.psi.sics.hipadaba.DocumentRoot#getXSISchemaLocation <em>XSI Schema Location</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the map '<em>XSI Schema Location</em>'.
	 * @see ch.psi.sics.hipadaba.DocumentRoot#getXSISchemaLocation()
	 * @see #getDocumentRoot()
	 * @generated
	 */
	public EReference getDocumentRoot_XSISchemaLocation() {
		return (EReference)documentRootEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * Returns the meta object for the containment reference '{@link ch.psi.sics.hipadaba.DocumentRoot#getSICS <em>SICS</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>SICS</em>'.
	 * @see ch.psi.sics.hipadaba.DocumentRoot#getSICS()
	 * @see #getDocumentRoot()
	 * @generated
	 */
	public EReference getDocumentRoot_SICS() {
		return (EReference)documentRootEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * Returns the meta object for class '{@link ch.psi.sics.hipadaba.Property <em>Property</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Property</em>'.
	 * @see ch.psi.sics.hipadaba.Property
	 * @generated
	 */
	public EClass getProperty() {
		return propertyEClass;
	}

	/**
	 * Returns the meta object for the attribute list '{@link ch.psi.sics.hipadaba.Property#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Value</em>'.
	 * @see ch.psi.sics.hipadaba.Property#getValue()
	 * @see #getProperty()
	 * @generated
	 */
	public EAttribute getProperty_Value() {
		return (EAttribute)propertyEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * Returns the meta object for the attribute '{@link ch.psi.sics.hipadaba.Property#getId <em>Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Id</em>'.
	 * @see ch.psi.sics.hipadaba.Property#getId()
	 * @see #getProperty()
	 * @generated
	 */
	public EAttribute getProperty_Id() {
		return (EAttribute)propertyEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * Returns the meta object for class '{@link ch.psi.sics.hipadaba.SICS <em>SICS</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>SICS</em>'.
	 * @see ch.psi.sics.hipadaba.SICS
	 * @generated
	 */
	public EClass getSICS() {
		return sicsEClass;
	}

	/**
	 * Returns the meta object for the containment reference list '{@link ch.psi.sics.hipadaba.SICS#getComponent <em>Component</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Component</em>'.
	 * @see ch.psi.sics.hipadaba.SICS#getComponent()
	 * @see #getSICS()
	 * @generated
	 */
	public EReference getSICS_Component() {
		return (EReference)sicsEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * Returns the meta object for the attribute '{@link ch.psi.sics.hipadaba.SICS#getId <em>Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Id</em>'.
	 * @see ch.psi.sics.hipadaba.SICS#getId()
	 * @see #getSICS()
	 * @generated
	 */
	public EAttribute getSICS_Id() {
		return (EAttribute)sicsEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * Returns the meta object for class '{@link ch.psi.sics.hipadaba.Value <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Value</em>'.
	 * @see ch.psi.sics.hipadaba.Value
	 * @generated
	 */
	public EClass getValue() {
		return valueEClass;
	}


	/**
	 * Returns the meta object for the attribute '{@link ch.psi.sics.hipadaba.Value#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see ch.psi.sics.hipadaba.Value#getValue()
	 * @see #getValue()
	 * @generated
	 */
	public EAttribute getValue_Value() {
		return (EAttribute)valueEClass.getEStructuralFeatures().get(0);
	}


	/**
	 * Returns the meta object for enum '{@link ch.psi.sics.hipadaba.DataType <em>Data Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Data Type</em>'.
	 * @see ch.psi.sics.hipadaba.DataType
	 * @generated
	 */
	public EEnum getDataType() {
		return dataTypeEEnum;
	}

	/**
	 * Returns the meta object for data type '{@link ch.psi.sics.hipadaba.DataType <em>Data Type Object</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>Data Type Object</em>'.
	 * @see ch.psi.sics.hipadaba.DataType
	 * @model instanceClass="ch.psi.sics.hipadaba.DataType"
	 *        extendedMetaData="name='DataType:Object' baseType='DataType'"
	 * @generated
	 */
	public EDataType getDataTypeObject() {
		return dataTypeObjectEDataType;
	}

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	public HipadabaFactory getHipadabaFactory() {
		return (HipadabaFactory)getEFactoryInstance();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isCreated = false;

	/**
	 * Creates the meta-model objects for the package.  This method is
	 * guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void createPackageContents() {
		if (isCreated) return;
		isCreated = true;

		// Create classes and their features
		componentEClass = createEClass(COMPONENT);
		createEReference(componentEClass, COMPONENT__VALUE);
		createEReference(componentEClass, COMPONENT__PROPERTY);
		createEReference(componentEClass, COMPONENT__COMPONENT);
		createEAttribute(componentEClass, COMPONENT__DATA_TYPE);
		createEAttribute(componentEClass, COMPONENT__ID);

		documentRootEClass = createEClass(DOCUMENT_ROOT);
		createEAttribute(documentRootEClass, DOCUMENT_ROOT__MIXED);
		createEReference(documentRootEClass, DOCUMENT_ROOT__XMLNS_PREFIX_MAP);
		createEReference(documentRootEClass, DOCUMENT_ROOT__XSI_SCHEMA_LOCATION);
		createEReference(documentRootEClass, DOCUMENT_ROOT__SICS);

		propertyEClass = createEClass(PROPERTY);
		createEAttribute(propertyEClass, PROPERTY__VALUE);
		createEAttribute(propertyEClass, PROPERTY__ID);

		sicsEClass = createEClass(SICS);
		createEReference(sicsEClass, SICS__COMPONENT);
		createEAttribute(sicsEClass, SICS__ID);

		valueEClass = createEClass(VALUE);
		createEAttribute(valueEClass, VALUE__VALUE);

		// Create enums
		dataTypeEEnum = createEEnum(DATA_TYPE);

		// Create data types
		dataTypeObjectEDataType = createEDataType(DATA_TYPE_OBJECT);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isInitialized = false;

	/**
	 * Complete the initialization of the package and its meta-model.  This
	 * method is guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void initializePackageContents() {
		if (isInitialized) return;
		isInitialized = true;

		// Initialize package
		setName(eNAME);
		setNsPrefix(eNS_PREFIX);
		setNsURI(eNS_URI);

		// Obtain other dependent packages
		XMLTypePackage theXMLTypePackage = (XMLTypePackage)EPackage.Registry.INSTANCE.getEPackage(XMLTypePackage.eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes

		// Initialize classes and features; add operations and parameters
		initEClass(componentEClass, Component.class, "Component", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getComponent_Value(), this.getValue(), null, "value", null, 0, 1, Component.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getComponent_Property(), this.getProperty(), null, "property", null, 0, -1, Component.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getComponent_Component(), this.getComponent(), null, "component", null, 0, -1, Component.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getComponent_DataType(), this.getDataType(), "dataType", null, 0, 1, Component.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getComponent_Id(), theXMLTypePackage.getString(), "id", null, 1, 1, Component.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(documentRootEClass, DocumentRoot.class, "DocumentRoot", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getDocumentRoot_Mixed(), ecorePackage.getEFeatureMapEntry(), "mixed", null, 0, -1, null, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getDocumentRoot_XMLNSPrefixMap(), ecorePackage.getEStringToStringMapEntry(), null, "xMLNSPrefixMap", null, 0, -1, null, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getDocumentRoot_XSISchemaLocation(), ecorePackage.getEStringToStringMapEntry(), null, "xSISchemaLocation", null, 0, -1, null, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getDocumentRoot_SICS(), this.getSICS(), null, "sICS", null, 0, -2, null, IS_TRANSIENT, IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, IS_DERIVED, IS_ORDERED);

		initEClass(propertyEClass, Property.class, "Property", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getProperty_Value(), theXMLTypePackage.getString(), "value", null, 1, -1, Property.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getProperty_Id(), theXMLTypePackage.getString(), "id", null, 0, 1, Property.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(sicsEClass, ch.psi.sics.hipadaba.SICS.class, "SICS", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getSICS_Component(), this.getComponent(), null, "component", null, 0, -1, ch.psi.sics.hipadaba.SICS.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSICS_Id(), theXMLTypePackage.getString(), "id", null, 0, 1, ch.psi.sics.hipadaba.SICS.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(valueEClass, Value.class, "Value", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getValue_Value(), theXMLTypePackage.getString(), "value", null, 0, 1, Value.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		// Initialize enums and add enum literals
		initEEnum(dataTypeEEnum, DataType.class, "DataType");
		addEEnumLiteral(dataTypeEEnum, DataType.INT_LITERAL);
		addEEnumLiteral(dataTypeEEnum, DataType.FLOAT_LITERAL);
		addEEnumLiteral(dataTypeEEnum, DataType.TEXT_LITERAL);
		addEEnumLiteral(dataTypeEEnum, DataType.INTAR_LITERAL);
		addEEnumLiteral(dataTypeEEnum, DataType.FLOATAR_LITERAL);
		addEEnumLiteral(dataTypeEEnum, DataType.INTVARAR_LITERAL);
		addEEnumLiteral(dataTypeEEnum, DataType.FLOATVARAR_LITERAL);
		addEEnumLiteral(dataTypeEEnum, DataType.NONE_LITERAL);
		addEEnumLiteral(dataTypeEEnum, DataType.FUNC_LITERAL);

		// Initialize data types
		initEDataType(dataTypeObjectEDataType, DataType.class, "DataTypeObject", IS_SERIALIZABLE, IS_GENERATED_INSTANCE_CLASS);

		// Create resource
		createResource(eNS_URI);

		// Create annotations
		// http:///org/eclipse/emf/ecore/util/ExtendedMetaData
		createExtendedMetaDataAnnotations();
	}

	/**
	 * Initializes the annotations for <b>http:///org/eclipse/emf/ecore/util/ExtendedMetaData</b>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void createExtendedMetaDataAnnotations() {
		String source = "http:///org/eclipse/emf/ecore/util/ExtendedMetaData";		
		addAnnotation
		  (componentEClass, 
		   source, 
		   new String[] {
			 "name", "Component",
			 "kind", "elementOnly"
		   });		
		addAnnotation
		  (getComponent_Value(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "value"
		   });		
		addAnnotation
		  (getComponent_Property(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "property"
		   });		
		addAnnotation
		  (getComponent_Component(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "component"
		   });		
		addAnnotation
		  (getComponent_DataType(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "dataType"
		   });		
		addAnnotation
		  (getComponent_Id(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "id"
		   });		
		addAnnotation
		  (dataTypeEEnum, 
		   source, 
		   new String[] {
			 "name", "DataType"
		   });		
		addAnnotation
		  (dataTypeObjectEDataType, 
		   source, 
		   new String[] {
			 "name", "DataType:Object",
			 "baseType", "DataType"
		   });		
		addAnnotation
		  (documentRootEClass, 
		   source, 
		   new String[] {
			 "name", "",
			 "kind", "mixed"
		   });		
		addAnnotation
		  (getDocumentRoot_Mixed(), 
		   source, 
		   new String[] {
			 "kind", "elementWildcard",
			 "name", ":mixed"
		   });		
		addAnnotation
		  (getDocumentRoot_XMLNSPrefixMap(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "xmlns:prefix"
		   });		
		addAnnotation
		  (getDocumentRoot_XSISchemaLocation(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "xsi:schemaLocation"
		   });		
		addAnnotation
		  (getDocumentRoot_SICS(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "SICS",
			 "namespace", "##targetNamespace"
		   });		
		addAnnotation
		  (propertyEClass, 
		   source, 
		   new String[] {
			 "name", "Property",
			 "kind", "elementOnly"
		   });		
		addAnnotation
		  (getProperty_Value(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "value"
		   });		
		addAnnotation
		  (getProperty_Id(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "id"
		   });		
		addAnnotation
		  (sicsEClass, 
		   source, 
		   new String[] {
			 "name", "SICS",
			 "kind", "elementOnly"
		   });		
		addAnnotation
		  (getSICS_Component(), 
		   source, 
		   new String[] {
			 "kind", "element",
			 "name", "component"
		   });		
		addAnnotation
		  (getSICS_Id(), 
		   source, 
		   new String[] {
			 "kind", "attribute",
			 "name", "id"
		   });		
		addAnnotation
		  (valueEClass, 
		   source, 
		   new String[] {
			 "name", "Value",
			 "kind", "simple"
		   });		
		addAnnotation
		  (getValue_Value(), 
		   source, 
		   new String[] {
			 "name", ":0",
			 "kind", "simple"
		   });
	}

	/**
	 * <!-- begin-user-doc -->
	 * Defines literals for the meta objects that represent
	 * <ul>
	 *   <li>each class,</li>
	 *   <li>each feature of each class,</li>
	 *   <li>each enum,</li>
	 *   <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public interface Literals {
		/**
		 * The meta object literal for the '{@link ch.psi.sics.hipadaba.impl.ComponentImpl <em>Component</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.psi.sics.hipadaba.impl.ComponentImpl
		 * @see ch.psi.sics.hipadaba.impl.HipadabaPackageImpl#getComponent()
		 * @generated
		 */
		public static final EClass COMPONENT = eINSTANCE.getComponent();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EReference COMPONENT__VALUE = eINSTANCE.getComponent_Value();

		/**
		 * The meta object literal for the '<em><b>Component</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EReference COMPONENT__COMPONENT = eINSTANCE.getComponent_Component();

		/**
		 * The meta object literal for the '<em><b>Property</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EReference COMPONENT__PROPERTY = eINSTANCE.getComponent_Property();

		/**
		 * The meta object literal for the '<em><b>Data Type</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EAttribute COMPONENT__DATA_TYPE = eINSTANCE.getComponent_DataType();

		/**
		 * The meta object literal for the '<em><b>Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EAttribute COMPONENT__ID = eINSTANCE.getComponent_Id();

		/**
		 * The meta object literal for the '{@link ch.psi.sics.hipadaba.impl.DocumentRootImpl <em>Document Root</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.psi.sics.hipadaba.impl.DocumentRootImpl
		 * @see ch.psi.sics.hipadaba.impl.HipadabaPackageImpl#getDocumentRoot()
		 * @generated
		 */
		public static final EClass DOCUMENT_ROOT = eINSTANCE.getDocumentRoot();

		/**
		 * The meta object literal for the '<em><b>Mixed</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EAttribute DOCUMENT_ROOT__MIXED = eINSTANCE.getDocumentRoot_Mixed();

		/**
		 * The meta object literal for the '<em><b>XMLNS Prefix Map</b></em>' map feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EReference DOCUMENT_ROOT__XMLNS_PREFIX_MAP = eINSTANCE.getDocumentRoot_XMLNSPrefixMap();

		/**
		 * The meta object literal for the '<em><b>XSI Schema Location</b></em>' map feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EReference DOCUMENT_ROOT__XSI_SCHEMA_LOCATION = eINSTANCE.getDocumentRoot_XSISchemaLocation();

		/**
		 * The meta object literal for the '<em><b>SICS</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EReference DOCUMENT_ROOT__SICS = eINSTANCE.getDocumentRoot_SICS();

		/**
		 * The meta object literal for the '{@link ch.psi.sics.hipadaba.impl.PropertyImpl <em>Property</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.psi.sics.hipadaba.impl.PropertyImpl
		 * @see ch.psi.sics.hipadaba.impl.HipadabaPackageImpl#getProperty()
		 * @generated
		 */
		public static final EClass PROPERTY = eINSTANCE.getProperty();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EAttribute PROPERTY__VALUE = eINSTANCE.getProperty_Value();

		/**
		 * The meta object literal for the '<em><b>Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EAttribute PROPERTY__ID = eINSTANCE.getProperty_Id();

		/**
		 * The meta object literal for the '{@link ch.psi.sics.hipadaba.impl.SICSImpl <em>SICS</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.psi.sics.hipadaba.impl.SICSImpl
		 * @see ch.psi.sics.hipadaba.impl.HipadabaPackageImpl#getSICS()
		 * @generated
		 */
		public static final EClass SICS = eINSTANCE.getSICS();

		/**
		 * The meta object literal for the '<em><b>Component</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EReference SICS__COMPONENT = eINSTANCE.getSICS_Component();

		/**
		 * The meta object literal for the '<em><b>Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EAttribute SICS__ID = eINSTANCE.getSICS_Id();

		/**
		 * The meta object literal for the '{@link ch.psi.sics.hipadaba.impl.ValueImpl <em>Value</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.psi.sics.hipadaba.impl.ValueImpl
		 * @see ch.psi.sics.hipadaba.impl.HipadabaPackageImpl#getValue()
		 * @generated
		 */
		public static final EClass VALUE = eINSTANCE.getValue();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EAttribute VALUE__VALUE = eINSTANCE.getValue_Value();

		/**
		 * The meta object literal for the '{@link ch.psi.sics.hipadaba.DataType <em>Data Type</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.psi.sics.hipadaba.DataType
		 * @see ch.psi.sics.hipadaba.impl.HipadabaPackageImpl#getDataType()
		 * @generated
		 */
		public static final EEnum DATA_TYPE = eINSTANCE.getDataType();

		/**
		 * The meta object literal for the '<em>Data Type Object</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.psi.sics.hipadaba.DataType
		 * @see ch.psi.sics.hipadaba.impl.HipadabaPackageImpl#getDataTypeObject()
		 * @generated
		 */
		public static final EDataType DATA_TYPE_OBJECT = eINSTANCE.getDataTypeObject();

	}

} //HipadabaPackageImpl
