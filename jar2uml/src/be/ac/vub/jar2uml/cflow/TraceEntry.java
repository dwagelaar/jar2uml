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
package be.ac.vub.jar2uml.cflow;

import be.ac.vub.jar2uml.cflow.ControlFlow.InstructionFlow;

/**
 * Entry from a trace path.
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public class TraceEntry {

	private final InstructionFlow iflow;
	private final int successors;

	/**
	 * Creates a new {@link TraceEntry}.
	 * @param trace
	 * @param iflow
	 * @param successors the amount of successors for iflow
	 */
	protected TraceEntry(InstructionFlow iflow, int successors) {
		super();
		this.iflow = iflow;
		this.successors = successors;
	}

	/**
	 * @return the iflow
	 */
	public InstructionFlow getIflow() {
		return iflow;
	}

	/**
	 * @return the successors
	 */
	public int getSuccessors() {
		return successors;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getIflow() + " : " + getSuccessors(); //$NON-NLS-1$
	}

}