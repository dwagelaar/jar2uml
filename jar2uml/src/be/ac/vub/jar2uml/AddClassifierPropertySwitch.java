package be.ac.vub.jar2uml;

import junit.framework.Assert;

import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.util.UMLSwitch;

public class AddClassifierPropertySwitch extends UMLSwitch {

	private String propertyName = null;
	private Type propertyType = null;

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

	public Object caseClass(Class umlClass) {
		String name = getPropertyName();
		Type type = getPropertyType();
		Assert.assertNotNull(name);
		return umlClass.createOwnedAttribute(name, type);
	}

	public Object caseInterface(Interface umlIface) {
		String name = getPropertyName();
		Type type = getPropertyType();
		Assert.assertNotNull(name);
		return umlIface.createOwnedAttribute(name, type);
	}

}
