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
package be.ac.vub.jar2uml.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import be.ac.vub.jar2uml.AddProperties;
import be.ac.vub.jar2uml.ParseClasses;

/**
 * Test class for {@link AddProperties}.
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public final class AddPropertiesTest extends J2UTestCase {

	public static final String javatestProject = "AddPropertiesTest";

	/**
	 * Test method for {@link AddProperties#isPreverified(org.apache.bcel.classfile.Code)}
	 * @throws IOException 
	 * @throws CoreException 
	 */
	public void testIsPreverifiedCode() throws CoreException, IOException {
		//
		// Retrieve Java test project
		//
		final IProject project = getProject(javatestProject);
		//
		// Copy instant messenger jar to Java test project
		//
		IFile file = JarToUMLTest.copyFileToProject(instantmessengerJar, project);
		JarFile jar = JarToUMLTest.jarFile(file);
		//
		// Run with preverified code
		//
		ParseClasses parseClasses = new ParseClasses(null, null);
		List<JavaClass> parsedClasses = new ArrayList<JavaClass>();
		parseClasses.parseClasses(jar, parsedClasses, parsedClasses);
		boolean preverified = false;
		for (JavaClass javaClass : parsedClasses) {
			for (Method method : javaClass.getMethods()) {
				Code code = method.getCode();
				preverified |= AddProperties.isPreverified(code);
			}
		}
		assertTrue(preverified);
		//
		// Copy "AddPropertiesTest.class" into Java project
		//
		copyClassToJavaProject(AddPropertiesTest.class, project);
		//
		// Run without preverified code
		//
		parsedClasses.clear();
		parseClasses.parseClasses(project, parsedClasses);
		preverified = false;
		for (JavaClass javaClass : parsedClasses) {
			for (Method method : javaClass.getMethods()) {
				Code code = method.getCode();
				preverified |= AddProperties.isPreverified(code);
			}
		}
		assertFalse(preverified);
	}

	/* (non-Javadoc)
	 * @see be.ac.vub.jar2uml.test.J2UTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		createJavaProject(javatestProject);
	}

}
