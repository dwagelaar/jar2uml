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
 * Exception related to {@link ControlFlow} problems.
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public abstract class ControlFlowException extends RuntimeException {

	private static final long serialVersionUID = 878088619000410006L;

	private final transient InstructionFlow causingInstruction;

	/**
	 * Creates a new {@link ControlFlowException}.
	 */
	public ControlFlowException() {
		super();
		this.causingInstruction = null;
	}

	/**
	 * Creates a new {@link ControlFlowException}.
	 * @param causingInstruction the instruction that caused the problem
	 */
	public ControlFlowException(InstructionFlow causingInstruction) {
		super();
		this.causingInstruction = causingInstruction;
	}

	/**
	 * Creates a new {@link ControlFlowException}.
	 * @param causingInstruction the instruction that caused the problem
	 * @param message
	 */
	public ControlFlowException(InstructionFlow causingInstruction,String message) {
		super(message);
		this.causingInstruction = causingInstruction;
	}

	/**
	 * Creates a new {@link ControlFlowException}.
	 * @param causingInstruction the instruction that caused the problem
	 * @param cause
	 */
	public ControlFlowException(InstructionFlow causingInstruction, Throwable cause) {
		super(cause);
		this.causingInstruction = causingInstruction;
	}

	/**
	 * Creates a new {@link ControlFlowException}.
	 * @param causingInstruction the instruction that caused the problem
	 * @param message
	 * @param cause
	 */
	public ControlFlowException(InstructionFlow causingInstruction, String message, Throwable cause) {
		super(message, cause);
		this.causingInstruction = causingInstruction;
	}

	/**
	 * @return the causingInstruction
	 */
	public InstructionFlow getCausingInstruction() {
		return causingInstruction;
	}

}