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

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.MethodGen;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Model;

import be.ac.vub.jar2uml.cflow.ControlFlow;
import be.ac.vub.jar2uml.cflow.FrameSimulator;
import be.ac.vub.jar2uml.cflow.SmartExecutionVisitor;

/**
 * Adds bytecode instruction dependencies for the given method.
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public class AddMethodOpCode extends AddToModel {

	protected final AddInstructionDependenciesVisitor addInstructionDependencies = 
		new AddInstructionDependenciesVisitor(
				typeToClassifier,
				addClassifierProperty,
				addClassifierOperation);
	protected final SmartExecutionVisitor execution = new SmartExecutionVisitor();
	protected final FrameSimulator simulator = 
		new FrameSimulator(execution, addInstructionDependencies);

	/**
	 * Creates a new {@link AddMethodOpCode}.
	 * @param filter A filter to apply to model operations.
	 * @param monitor A progress monitor to check for end user cancellation.
	 * @param ticks amount of ticks this task will add to the progress monitor
	 * @param model The UML model to store generated elements in.
	 * @param includeFeatures Whether to include fields and methods.
	 * @param includeInstructionReferences Whether or not to include Java elements that are
	 * referred to by bytecode instructions.
	 */
	public AddMethodOpCode(Filter filter, IProgressMonitor monitor, int ticks,
			Model model, boolean includeFeatures,
			boolean includeInstructionReferences) {
		super(filter, monitor, ticks, model, includeFeatures,
				includeInstructionReferences);
	}

	/**
	 * Adds fields/methods referenced by the bytecode instructions of method
	 * to the UML model. Used in 2nd pass.
	 * @param instrContext The classifier on which the method is defined.
	 * @param javaClass The {@link JavaClass} representation of instrContext.
	 * @param method The method for which to convert the references.
	 * @throws JarToUMLException 
	 */
	public void addOpCode(final Classifier instrContext, final JavaClass javaClass, final Method method) throws JarToUMLException {
		if (!isIncludeInstructionReferences() || method.getCode() == null) {
			return;
		}

		addInstructionDependencies.setInstrContext(instrContext);
		addInstructionDependencies.setCp(method.getConstantPool());

		final MethodGen method_gen = new MethodGen(method, javaClass.getClassName(), addInstructionDependencies.getCpg());
		final ControlFlow cflow = new ControlFlow(method_gen);

		simulator.execute(cflow);
	}

}
