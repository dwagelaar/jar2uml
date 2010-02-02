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

import org.apache.bcel.classfile.AccessFlags;
import org.apache.bcel.classfile.JavaClass;

/**
 * Includes only named public/protected elements.
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public class PublicAPIFilter implements Filter {

	public boolean filter(String expression) {
		return true;
	}

	public boolean filter(JavaClass javaClass) {
		return JarToUML.isNamedClass(javaClass) && filter((AccessFlags) javaClass);
	}

	public boolean filter(AccessFlags flags) {
		return (flags.isPublic() || flags.isProtected());
	}

}
