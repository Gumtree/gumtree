/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package ch.psi.sics.hipadaba;

import java.util.List;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Component</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link ch.psi.sics.hipadaba.Component#getValue <em>Value</em>}</li>
 *   <li>{@link ch.psi.sics.hipadaba.Component#getProperty <em>Property</em>}</li>
 *   <li>{@link ch.psi.sics.hipadaba.Component#getComponent <em>Component</em>}</li>
 *   <li>{@link ch.psi.sics.hipadaba.Component#getDataType <em>Data Type</em>}</li>
 *   <li>{@link ch.psi.sics.hipadaba.Component#getId <em>Id</em>}</li>
 * </ul>
 * </p>
 *
 * @model extendedMetaData="name='Component' kind='elementOnly'"
 * @generated
 */
public interface Component {
	/**
	 * Returns the value of the '<em><b>Value</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Value</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Value</em>' containment reference.
	 * @see #setValue(Value)
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='value'"
	 * @generated
	 */
	Value getValue();

	/**
	 * Sets the value of the '{@link ch.psi.sics.hipadaba.Component#getValue <em>Value</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Value</em>' containment reference.
	 * @see #getValue()
	 * @generated
	 */
	void setValue(Value value);

	/**
	 * Returns the value of the '<em><b>Component</b></em>' containment reference list.
	 * The list contents are of type {@link ch.psi.sics.hipadaba.Component}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Component</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Component</em>' containment reference list.
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='component'"
	 * @generated
	 */
	List<Component> getComponent();

	/**
	 * Returns the value of the '<em><b>Property</b></em>' containment reference list.
	 * The list contents are of type {@link ch.psi.sics.hipadaba.Property}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Property</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Property</em>' containment reference list.
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='property'"
	 * @generated
	 */
	List<Property> getProperty();

	/**
	 * Returns the value of the '<em><b>Data Type</b></em>' attribute.
	 * The literals are from the enumeration {@link ch.psi.sics.hipadaba.DataType}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Data Type</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Data Type</em>' attribute.
	 * @see ch.psi.sics.hipadaba.DataType
	 * @see #isSetDataType()
	 * @see #unsetDataType()
	 * @see #setDataType(DataType)
	 * @model unsettable="true"
	 *        extendedMetaData="kind='attribute' name='dataType'"
	 * @generated
	 */
	DataType getDataType();

	/**
	 * Sets the value of the '{@link ch.psi.sics.hipadaba.Component#getDataType <em>Data Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Data Type</em>' attribute.
	 * @see ch.psi.sics.hipadaba.DataType
	 * @see #isSetDataType()
	 * @see #unsetDataType()
	 * @see #getDataType()
	 * @generated
	 */
	void setDataType(DataType value);

	/**
	 * Unsets the value of the '{@link ch.psi.sics.hipadaba.Component#getDataType <em>Data Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetDataType()
	 * @see #getDataType()
	 * @see #setDataType(DataType)
	 * @generated
	 */
	void unsetDataType();

	/**
	 * Returns whether the value of the '{@link ch.psi.sics.hipadaba.Component#getDataType <em>Data Type</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Data Type</em>' attribute is set.
	 * @see #unsetDataType()
	 * @see #getDataType()
	 * @see #setDataType(DataType)
	 * @generated
	 */
	boolean isSetDataType();

	/**
	 * Returns the value of the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Id</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Id</em>' attribute.
	 * @see #setId(String)
	 * @model dataType="org.eclipse.emf.ecore.xml.type.String" required="true"
	 *        extendedMetaData="kind='attribute' name='id'"
	 * @generated
	 */
	String getId();

	/**
	 * Sets the value of the '{@link ch.psi.sics.hipadaba.Component#getId <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Id</em>' attribute.
	 * @see #getId()
	 * @generated
	 */
	void setId(String value);

} // Component
