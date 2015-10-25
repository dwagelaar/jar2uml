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
package org.eclipselabs.jar2uml.cflow;

import org.eclipselabs.jar2uml.cflow.ControlFlow.InstructionFlow;

/**
 * Represents the combination of current instruction, instruction history,
 * and current execution frame.
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public class ExecutionContext {

	private final InstructionFlow iflow;
	private final LocalHistoryTable history;
	private final SmartFrame frame;
	private final Trace trace;

	/**
	 * Creates a new {@link ExecutionContext}.
	 * @param iflow
	 * @param history
	 * @param frame
	 * @param trace
	 */
	public ExecutionContext(InstructionFlow iflow, LocalHistoryTable history, SmartFrame frame, Trace trace) {
		super();
		this.iflow = iflow;
		this.history = history;
		this.frame = frame;
		this.trace = trace;
	}

	/**
	 * @return the iflow
	 */
	public InstructionFlow getIflow() {
		return iflow;
	}

	/**
	 * @return the history
	 */
	public LocalHistoryTable getHistory() {
		return history;
	}

	/**
	 * @return the frame
	 */
	public SmartFrame getFrame() {
		return frame;
	}

	/**
	 * @return the trace
	 */
	public Trace getTrace() {
		return trace;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getClass().getSimpleName() + "{ " + getIflow() + " }"; //$NON-NLS-1$ //$NON-NLS-2$
	}

}
