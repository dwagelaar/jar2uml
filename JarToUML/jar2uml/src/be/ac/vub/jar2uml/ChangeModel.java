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
 * Introduces functionality for changing the UML {@link Model}. 
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public abstract class ChangeModel extends JarToUMLOperation {

	private Model model;

	/**
	 * Creates a new {@link ChangeModel}.
	 * @param filter A filter to apply to model operations.
	 * @param monitor A progress monitor to check for end user cancellation.
	 * @param ticks amount of ticks this task will add to the progress monitor
	 * @param model The UML model to store generated elements in.
	 */
	public ChangeModel(Filter filter, IProgressMonitor monitor, int ticks, Model model) {
		super(filter, monitor, ticks);
		setModel(model);
	}

	/**
	 * @return The generated UML model.
	 */
	public Model getModel() {
		return model;
	}

	/**
	 * Sets the UML model to store generated elements in.
	 * @param model
	 */
	public void setModel(Model model) {
		this.model = model;
	}

}
