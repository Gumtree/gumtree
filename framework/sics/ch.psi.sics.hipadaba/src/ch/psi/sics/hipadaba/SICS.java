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
 * A representation of the model object '<em><b>SICS</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link ch.psi.sics.hipadaba.SICS#getComponent <em>Component</em>}</li>
 *   <li>{@link ch.psi.sics.hipadaba.SICS#getId <em>Id</em>}</li>
 * </ul>
 * </p>
 *
 * @model extendedMetaData="name='SICS' kind='elementOnly'"
 * @generated
 */
public interface SICS {
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
	 * Returns the value of the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Id</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Id</em>' attribute.
	 * @see #setId(String)
	 * @model dataType="org.eclipse.emf.ecore.xml.type.String"
	 *        extendedMetaData="kind='attribute' name='id'"
	 * @generated
	 */
	String getId();

	/**
	 * Sets the value of the '{@link ch.psi.sics.hipadaba.SICS#getId <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Id</em>' attribute.
	 * @see #getId()
	 * @generated
	 */
	void setId(String value);

} // SICS
