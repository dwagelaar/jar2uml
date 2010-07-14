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
 * Includes only named public/protected elements from the java.*, javax.*,
 * org.omg.*, org.w3c.*, org.xml.* and org.ietf.* packages.
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public class JavaAPIFilter extends PublicAPIFilter {

	/*
	 * (non-Javadoc)
	 * @see be.ac.vub.jar2uml.PublicAPIFilter#filter(java.lang.String)
	 */
	public boolean filter(String expression) {
		return (expression.startsWith("java/")
				|| expression.startsWith("javax/")
				|| expression.startsWith("org/omg/")
				|| expression.startsWith("org/w3c/")
				|| expression.startsWith("org/xml/")
				|| expression.startsWith("org/ietf/"));
	}

	/*
	 * (non-Javadoc)
	 * @see be.ac.vub.jar2uml.PublicAPIFilter#filter(org.apache.bcel.classfile.JavaClass)
	 */
	public boolean filter(JavaClass javaClass) {
		return super.filter(javaClass);
	}

	/*
	 * (non-Javadoc)
	 * @see be.ac.vub.jar2uml.PublicAPIFilter#filter(org.apache.bcel.classfile.AccessFlags)
	 */
	public boolean filter(AccessFlags flags) {
		return super.filter(flags);
	}

}
