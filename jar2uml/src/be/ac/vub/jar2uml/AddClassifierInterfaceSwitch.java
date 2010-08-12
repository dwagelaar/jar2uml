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

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.uml2.uml.util.UMLSwitch#caseClass(org.eclipse.uml2.uml.Class)
	 */
	@Override
	public DirectedRelationship caseClass(Class umlClass) {
		Interface iface = getIface();
		assert iface != null;
		return umlClass.createInterfaceRealization(null, (Interface) iface);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.uml2.uml.util.UMLSwitch#caseInterface(org.eclipse.uml2.uml.Interface)
	 */
	@Override
	public DirectedRelationship caseInterface(Interface umlIface) {
		Interface iface = getIface();
		assert iface != null;
		return umlIface.createGeneralization(iface);
	}

}
