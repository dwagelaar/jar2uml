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

import junit.framework.Assert;

import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.Type;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.UMLFactory;

import be.ac.vub.jar2uml.AddClassifierOperationSwitch;
import be.ac.vub.jar2uml.JarToUMLException;
import be.ac.vub.jar2uml.TypeToClassifierSwitch;
import be.ac.vub.jar2uml.test.data.B;

/**
 * Test class for {@link AddClassifierOperationSwitch}.
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public class AddClassifierOperationSwitchTest extends J2UTestCase {

	/**
	 * Test method for {@link AddClassifierOperationSwitch#toUMLArgNames(org.eclipse.emf.common.util.EList)}.
	 * @throws IOException 
	 * @throws ClassFormatException 
	 */
	public void testToUMLArgNames() throws ClassFormatException, IOException {
		try {
			AddClassifierOperationSwitch.toUMLArgNames(null);
			fail("expected NullPointerException");
		} catch (NullPointerException e) {
			//good...
		}
		final EList<Object> args = new BasicEList<Object>();
		Assert.assertEquals(0, AddClassifierOperationSwitch.toUMLArgNames(args).size());
		args.add(new Object());
		EList<String> argNames = AddClassifierOperationSwitch.toUMLArgNames(args);
		Assert.assertEquals(1, argNames.size());
		Assert.assertEquals("arg0", argNames.get(0));
		args.add(new Object());
		argNames = AddClassifierOperationSwitch.toUMLArgNames(args);
		Assert.assertEquals(2, argNames.size());
		Assert.assertEquals("arg0", argNames.get(0));
		Assert.assertEquals("arg1", argNames.get(1));
	}

	/**
	 * Test method for {@link AddClassifierOperationSwitch#getArgumentNames(org.apache.bcel.classfile.Method)}.
	 * @throws IOException 
	 * @throws ClassFormatException 
	 */
	public void testGetArgumentNamesMethod() throws ClassFormatException, IOException {
		final JavaClass bClass = getTestClass(B.class);
		testGetArgumentNamesMethod(bClass);
		final JavaClass bbClass = getTestClass(B.BB.class);
		testGetArgumentNamesMethod(bbClass);
	}

	protected void testGetArgumentNamesMethod(final JavaClass testClass) {
		for (Method m : testClass.getMethods()) {
			//this works for all methods with a local variable table
			if (m.getLocalVariableTable() != null) {
				Assert.assertNotNull(AddClassifierOperationSwitch.getArgumentNames(m));
			}
		}
	}

	/**
	 * @return a new {@link AddClassifierOperationSwitch}.
	 */
	protected AddClassifierOperationSwitch createAddClassifierOperationSwitch() {
		final Model model = UMLFactory.eINSTANCE.createModel();
		model.setName(getClass().getSimpleName()); //model must have a name
		final TypeToClassifierSwitch ttc = new TypeToClassifierSwitch();
		ttc.setRoot(model);
		return new AddClassifierOperationSwitch(ttc);
	}

	/**
	 * Test method for {@link AddClassifierOperationSwitch#setAll(org.apache.bcel.classfile.Method)}.
	 * @throws IOException 
	 * @throws ClassFormatException 
	 * @throws JarToUMLException 
	 */
	public void testSetAll() throws ClassFormatException, IOException, JarToUMLException {
		final AddClassifierOperationSwitch aco = createAddClassifierOperationSwitch();
		final JavaClass testClass = getTestClass(B.class);
		for (Method m : testClass.getMethods()) {
			aco.setAll(m);
			Assert.assertEquals(m.getName(), aco.getOperationName());
			EList<String> argNames = AddClassifierOperationSwitch.getArgumentNames(m);
			if (argNames != null) {
				Assert.assertEquals(argNames, aco.getArgumentNames());
			} else {
				Assert.assertEquals(m.getArgumentTypes().length, aco.getArgumentNames().size());
			}
			Assert.assertEquals(m.getArgumentTypes().length, aco.getArgumentTypes().size());
			Assert.assertEquals(m.getReturnType() != Type.VOID, aco.getReturnType() != null);
		}
	}

	/**
	 * Test method for {@link AddClassifierOperationSwitch#setBCELArgumentTypes(org.apache.bcel.generic.Type[])}.
	 * @throws IOException 
	 * @throws ClassFormatException 
	 * @throws JarToUMLException 
	 */
	public void testSetBCELArgumentTypes() throws ClassFormatException, IOException, JarToUMLException {
		final AddClassifierOperationSwitch aco = createAddClassifierOperationSwitch();
		final JavaClass testClass = getTestClass(B.class);
		for (Method m : testClass.getMethods()) {
			aco.setBCELArgumentTypes(m.getArgumentTypes());
			Assert.assertEquals(m.getArgumentTypes().length, aco.getArgumentTypes().size());
		}
	}

	/**
	 * Test method for {@link AddClassifierOperationSwitch#setBCELReturnType(org.apache.bcel.generic.Type)}.
	 * @throws IOException 
	 * @throws ClassFormatException 
	 * @throws JarToUMLException 
	 */
	public void testSetBCELReturnType() throws ClassFormatException, IOException, JarToUMLException {
		final AddClassifierOperationSwitch aco = createAddClassifierOperationSwitch();
		final JavaClass testClass = getTestClass(B.class);
		for (Method m : testClass.getMethods()) {
			aco.setBCELReturnType(m.getReturnType());
			Assert.assertEquals(m.getReturnType() != Type.VOID, aco.getReturnType() != null);
		}
	}

}
