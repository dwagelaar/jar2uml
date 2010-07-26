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
package be.ac.vub.jar2uml.test.data;

import be.ac.vub.jar2uml.JarToUML;
import be.ac.vub.jar2uml.test.JarToUMLTest;

/**
 * {@link JarToUMLTest} test class. This class is imported,
 * and its references are to be inferred by {@link JarToUML}.
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public class B {
	
	public class BB {
		
	}
	
	private A.AA aaField;
	private String[] arrayField = new String[1];

	/**
	 * @return the aaField
	 */
	public A.AA getAaField() {
		return aaField;
	}

	/**
	 * @param aaField the aaField to set
	 */
	public void setAaField(A.AA aaField) {
		this.aaField = aaField;
	}
	
	public B() {
		super();
		A a = new A();
		a.setBbField(new BB());
		System.out.println(arrayField.length);
	}
}
