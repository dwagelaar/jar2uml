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
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.MethodGen;

import be.ac.vub.jar2uml.AccessContextVisitor;
import be.ac.vub.jar2uml.JarToUML;
import be.ac.vub.jar2uml.cflow.ControlFlow;
import be.ac.vub.jar2uml.cflow.FrameSimulator;
import be.ac.vub.jar2uml.cflow.SmartExecutionVisitor;
import be.ac.vub.jar2uml.test.data.B;

/**
 * Test class for {@link FrameSimulator}.
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public class FrameSimulatorTest extends J2UTestCase {

	/**
	 * Test method for {@link FrameSimulator#execute(be.ac.vub.jar2uml.cflow.ControlFlow)}
	 * @throws IOException 
	 * @throws ClassFormatException 
	 */
	public void testExecute() throws ClassFormatException, IOException {
		final JavaClass testClass = getTestClass(B.class);
		final AccessContextVisitor acv = new AccessContextVisitor();
		final FrameSimulator simulator = new FrameSimulator(new SmartExecutionVisitor(), acv);
		for (Method m : testClass.getMethods()) {
			testMethod(simulator, acv, new MethodGen(
					m, testClass.getClassName(), new ConstantPoolGen(m.getConstantPool())));
		}
	}

	/**
	 * Tests a method from the test class.
	 * @param simulator
	 * @param acv
	 * @param m
	 */
	public void testMethod(final FrameSimulator simulator, final AccessContextVisitor acv, final MethodGen m) {
		acv.setCp(m.getConstantPool().getConstantPool());
		final ControlFlow cflow = new ControlFlow(m);
		JarToUML.logger.info(cflow.toString());
		//execute once in default mode
		simulator.getExecution().setTrackNull(false);
		simulator.execute(cflow);
		Assert.assertTrue(simulator.getNotCovered().isEmpty() || simulator.isCutOff());
		//execute once in find-dead-code mode
		simulator.getExecution().setTrackNull(true);
		simulator.execute(cflow);
		Assert.assertTrue(simulator.getNotCovered().isEmpty() || simulator.isCutOff());
	}

}
