/*******************************************************************************
 * Copyright (c) 2007-2010 Dennis Wagelaar, Vrije Universiteit Brussel.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dennis Wagelaar, Vrije Universiteit Brussel
 *******************************************************************************/
package be.ac.vub.jar2uml;

import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.DataType;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.util.UMLSwitch;

/**
 * Adds a property to the switched element (class, interface or datatype). 
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public class AddClassifierPropertySwitch extends UMLSwitch<Property> {

	private String propertyName = null;
	private Type propertyType = null;
	private TypeToClassifierSwitch typeToClassifier = null;

	/**
	 * Creates a new {@link AddClassifierPropertySwitch}.
	 * @param typeToClassifier
	 */
	public AddClassifierPropertySwitch(TypeToClassifierSwitch typeToClassifier) {
		assert typeToClassifier != null;
		this.typeToClassifier = typeToClassifier;
	}

	/**
	 * @return Name of the {@link Property} to create.
	 */
	public String getPropertyName() {
		return propertyName;
	}

	/**
	 * Sets the name of the {@link Property} to create.
	 * @param propertyName
	 */
	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	/**
	 * @return {@link Type} of the {@link Property} to create.
	 */
	public Type getPropertyType() {
		return propertyType;
	}

	/**
	 * Sets the {@link Type} of the {@link Property} to create.
	 * @param propertyType
	 */
	public void setPropertyType(Type propertyType) {
		this.propertyType = propertyType;
	}

	/**
	 * Sets the {@link org.apache.bcel.generic.Type} of the {@link Property} to create.
	 * @param propertyType
	 */
	public void setBCELPropertyType(org.apache.bcel.generic.Type propertyType) {
		setPropertyType(typeToClassifier.doSwitch(propertyType));
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.uml2.uml.util.UMLSwitch#caseClass(org.eclipse.uml2.uml.Class)
	 */
	@Override
	public Property caseClass(Class umlClass) {
		String name = getPropertyName();
		Type type = getPropertyType();
		assert name != null;
		Property ownedAtt = umlClass.getOwnedAttribute(name, type);
		if (ownedAtt == null) {
			ownedAtt = umlClass.createOwnedAttribute(name, type);
			ownedAtt.setIsLeaf(true);		//final
			ownedAtt.setIsReadOnly(true);	//final
		}
		return ownedAtt;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.uml2.uml.util.UMLSwitch#caseInterface(org.eclipse.uml2.uml.Interface)
	 */
	@Override
	public Property caseInterface(Interface umlIface) {
		String name = getPropertyName();
		Type type = getPropertyType();
		assert name != null;
		Property ownedAtt = umlIface.getOwnedAttribute(name, type);
		if (ownedAtt == null) {
			ownedAtt = umlIface.createOwnedAttribute(name, type);
			ownedAtt.setIsLeaf(true);		//final
			ownedAtt.setIsReadOnly(true);	//final
		}
		return ownedAtt;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.uml2.uml.util.UMLSwitch#caseDataType(org.eclipse.uml2.uml.DataType)
	 */
	@Override
	public Property caseDataType(DataType umlDataType) {
		String name = getPropertyName();
		Type type = getPropertyType();
		assert name != null;
		Property ownedAtt = umlDataType.getOwnedAttribute(name, type);
		if (ownedAtt == null) {
			ownedAtt = umlDataType.createOwnedAttribute(name, type);
			ownedAtt.setIsLeaf(true);		//final
			ownedAtt.setIsReadOnly(true);	//final
		}
		return ownedAtt;
	}

}
