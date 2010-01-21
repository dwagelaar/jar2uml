package be.ac.vub.jar2uml;

import junit.framework.Assert;

import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.DirectedRelationship;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.util.UMLSwitch;

/**
 * Adds a super-interface to the switched element (either class or interface).
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public class AddClassifierInterfaceSwitch extends UMLSwitch<DirectedRelationship> {
	
	private Interface iface = null;

	/**
	 * @return The interface to add as a super-interface.
	 */
	public Interface getIface() {
		return iface;
	}

	/**
	 * @param iface The interface to add as a super-interface.
	 */
	public void setIface(Interface iface) {
		this.iface = iface;
	}

	public DirectedRelationship caseClass(Class umlClass) {
		Interface iface = getIface();
		Assert.assertNotNull(iface);
		return umlClass.createInterfaceRealization(null, (Interface) iface);
	}

	public DirectedRelationship caseInterface(Interface umlIface) {
		Interface iface = getIface();
		Assert.assertNotNull(iface);
		return umlIface.createGeneralization(iface);
	}
	
}
