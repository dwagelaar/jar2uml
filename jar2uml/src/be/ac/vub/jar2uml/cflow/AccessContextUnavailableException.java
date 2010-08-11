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

import org.apache.bcel.classfile.FieldOrMethod;

import be.ac.vub.jar2uml.cflow.ControlFlow.InstructionFlow;

/**
 * Exception thrown when the access context for a {@link FieldOrMethod}
 * instruction is not available (e.g. it is <code>null</code>).
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public class AccessContextUnavailableException extends ControlFlowException {

	private static final long serialVersionUID = -6442255567464649457L;

	/**
	 * Creates a new {@link AccessContextUnavailableException}.
	 * @param causingInstruction the instruction that caused the access context to be unavailable
	 */
	public AccessContextUnavailableException(InstructionFlow causingInstruction) {
		super(causingInstruction);
	}

	/**
	 * Creates a new {@link AccessContextUnavailableException}.
	 * @param causingInstruction the instruction that caused the access context to be unavailable
	 * @param message
	 * @param cause
	 */
	public AccessContextUnavailableException(InstructionFlow causingInstruction, String message, Throwable cause) {
		super(causingInstruction, message, cause);
	}

	/**
	 * Creates a new {@link AccessContextUnavailableException}.
	 * @param causingInstruction the instruction that caused the access context to be unavailable
	 * @param message
	 */
	public AccessContextUnavailableException(InstructionFlow causingInstruction, String message) {
		super(causingInstruction, message);
	}

	/**
	 * Creates a new {@link AccessContextUnavailableException}.
	 * @param causingInstruction the instruction that caused the access context to be unavailable
	 * @param cause
	 */
	public AccessContextUnavailableException(InstructionFlow causingInstruction, Throwable cause) {
		super(causingInstruction, cause);
	}

}
