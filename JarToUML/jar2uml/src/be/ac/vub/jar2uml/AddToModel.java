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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.uml2.uml.Model;

/**
 * Abstract base class for operations that add elements to the UML {@link Model}.
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public abstract class AddToModel extends ChangeModel {

	protected FindContainedClassifierSwitch findContainedClassifier = new FindContainedClassifierSwitch();
	protected TypeToClassifierSwitch typeToClassifier = new TypeToClassifierSwitch();
	protected AddClassifierPropertySwitch addClassifierProperty = new AddClassifierPropertySwitch(typeToClassifier);
	protected AddClassifierOperationSwitch addClassifierOperation = new AddClassifierOperationSwitch(typeToClassifier);

	private boolean includeFeatures;
	private boolean includeInstructionReferences;

	/**
	 * Creates a new {@link AddToModel}.
	 * @param filter A filter to apply to model operations.
	 * @param monitor A progress monitor to check for end user cancellation.
	 * @param ticks amount of ticks this task will add to the progress monitor
	 * @param model The UML model to store generated elements in.
	 * @param includeFeatures Whether to include fields and methods.
	 * @param includeInstructionReferences Whether or not to include Java elements that are
	 * referred to by bytecode instructions.
	 */
	public AddToModel(Filter filter, IProgressMonitor monitor, int ticks, Model model, 
			boolean includeFeatures, boolean includeInstructionReferences) {
		super(filter, monitor, ticks, model);
		setIncludeFeatures(includeFeatures);
		setIncludeInstructionReferences(includeInstructionReferences);
		typeToClassifier.setRoot(getModel());
	}

	/**
	 * Whether or not to include classifier operations and attributes.
	 * @return the includeFeatures
	 */
	public boolean isIncludeFeatures() {
		return includeFeatures;
	}

	/**
	 * Whether or not to include classifier operations and attributes.
	 * @param includeFeatures the includeFeatures to set
	 */
	public void setIncludeFeatures(boolean includeFeatures) {
		this.includeFeatures = includeFeatures;
	}

	/**
	 * @return Whether or not to include Java elements that are
	 * referred to by bytecode instructions.
	 */
	public boolean isIncludeInstructionReferences() {
		return includeInstructionReferences;
	}

	/**
	 * Sets whether or not to include Java elements that are
	 * referred to by bytecode instructions.
	 * @param includeInstructionReferences
	 */
	public void setIncludeInstructionReferences(boolean includeInstructionReferences) {
		this.includeInstructionReferences = includeInstructionReferences;
	}

}