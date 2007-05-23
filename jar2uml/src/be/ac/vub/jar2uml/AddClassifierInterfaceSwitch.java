package be.ac.vub.jar2uml;

import junit.framework.Assert;

import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.util.UMLSwitch;

public class AddClassifierInterfaceSwitch extends UMLSwitch {
	
	private Interface iface = null;

	public Interface getIface() {
		return iface;
	}

	public void setIface(Interface iface) {
		this.iface = iface;
	}

	public Object caseClass(Class umlClass) {
		Interface iface = getIface();
		Assert.assertNotNull(iface);
		return umlClass.createInterfaceRealization(null, (Interface) iface);
	}

	public Object caseInterface(Interface umlIface) {
		Interface iface = getIface();
		Assert.assertNotNull(iface);
		return umlIface.createGeneralization(iface);
	}
	
	public void reset() {
		setIface(null);
	}

}
