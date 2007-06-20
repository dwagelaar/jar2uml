package be.ac.vub.jar2uml;

import junit.framework.Assert;

import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.DataType;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.util.UMLSwitch;

public class AddClassifierPropertySwitch extends UMLSwitch {

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

	public Object caseClass(Class umlClass) {
		String name = getPropertyName();
		Type type = getPropertyType();
		Assert.assertNotNull(name);
		Property ownedAtt = umlClass.getOwnedAttribute(name, type);
		if (ownedAtt == null) {
			return umlClass.createOwnedAttribute(name, type);
		} else {
			return ownedAtt;
		}
	}

	public Object caseInterface(Interface umlIface) {
		String name = getPropertyName();
		Type type = getPropertyType();
		Assert.assertNotNull(name);
		Property ownedAtt = umlIface.getOwnedAttribute(name, type);
		if (ownedAtt == null) {
			return umlIface.createOwnedAttribute(name, type);
		} else {
			return ownedAtt;
		}
	}
	
	public Object caseDataType(DataType umlDataType) {
		String name = getPropertyName();
		Type type = getPropertyType();
		Assert.assertNotNull(name);
		Property ownedAtt = umlDataType.getOwnedAttribute(name, type);
		if (ownedAtt == null) {
			return umlDataType.createOwnedAttribute(name, type);
		} else {
			return ownedAtt;
		}
	}
	
	public void reset() {
		setPropertyName(null);
		setPropertyType(null);
	}

}
