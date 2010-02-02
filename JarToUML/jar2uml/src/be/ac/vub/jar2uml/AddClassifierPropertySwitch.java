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

import junit.framework.Assert;

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

	public AddClassifierPropertySwitch(TypeToClassifierSwitch typeToClassifier) {
		Assert.assertNotNull(typeToClassifier);
		this.typeToClassifier = typeToClassifier;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	public Type getPropertyType() {
		return propertyType;
	}

	public void setPropertyType(Type propertyType) {
		this.propertyType = propertyType;
	}

	public void setBCELPropertyType(org.apache.bcel.generic.Type propertyType) {
		setPropertyType((Type) typeToClassifier.doSwitch(propertyType));
	}

	public Property caseClass(Class umlClass) {
		String name = getPropertyName();
		Type type = getPropertyType();
		Assert.assertNotNull(name);
		Property ownedAtt = umlClass.getOwnedAttribute(name, type);
		if (ownedAtt == null) {
			ownedAtt = umlClass.createOwnedAttribute(name, type);
			ownedAtt.setIsLeaf(true);		//final
			ownedAtt.setIsReadOnly(true);	//final
		}
		return ownedAtt;
	}

	public Property caseInterface(Interface umlIface) {
		String name = getPropertyName();
		Type type = getPropertyType();
		Assert.assertNotNull(name);
		Property ownedAtt = umlIface.getOwnedAttribute(name, type);
		if (ownedAtt == null) {
			ownedAtt = umlIface.createOwnedAttribute(name, type);
			ownedAtt.setIsLeaf(true);		//final
			ownedAtt.setIsReadOnly(true);	//final
		}
		return ownedAtt;
	}

	public Property caseDataType(DataType umlDataType) {
		String name = getPropertyName();
		Type type = getPropertyType();
		Assert.assertNotNull(name);
		Property ownedAtt = umlDataType.getOwnedAttribute(name, type);
		if (ownedAtt == null) {
			ownedAtt = umlDataType.createOwnedAttribute(name, type);
			ownedAtt.setIsLeaf(true);		//final
			ownedAtt.setIsReadOnly(true);	//final
		}
		return ownedAtt;
	}

}
