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

import org.apache.bcel.generic.IfInstruction;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.verifier.structurals.Frame;

import be.ac.vub.jar2uml.cflow.ControlFlow.InstructionFlow;

/**
 * Exception thrown when one or more targets of a conditional
 * branch {@link Instruction}, such as an {@link IfInstruction},
 * were unavailable within the given {@link Frame}.
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public class BranchTargetUnavailableException extends ControlFlowException {

	private static final long serialVersionUID = -2302822625940595508L;

	private transient final InstructionFlow[] unavailableTargets;
	private transient final InstructionFlow[] remainingTargets;

	/**
	 * Creates a new {@link BranchTargetUnavailableException}.
	 * @param unavailableTargets the branch targets that are not available
	 * @param remainingTargets the branch targets that are still available
	 * @param causingInstruction the instruction that caused the branch target to become unavailable
	 */
	public BranchTargetUnavailableException(InstructionFlow[] unavailableTargets, InstructionFlow[] remainingTargets, InstructionFlow causingInstruction) {
		super(causingInstruction);
		this.unavailableTargets = unavailableTargets;
		this.remainingTargets = remainingTargets;
	}

	/**
	 * Creates a new {@link BranchTargetUnavailableException}.
	 * @param unavailableTargets the branch targets that are not available
	 * @param remainingTargets the branch targets that are still available
	 * @param causingInstruction the instruction that caused the branch target to become unavailable
	 * @param message
	 */
	public BranchTargetUnavailableException(InstructionFlow[] unavailableTargets, InstructionFlow[] remainingTargets, InstructionFlow causingInstruction, String message) {
		super(causingInstruction, message);
		this.unavailableTargets = unavailableTargets;
		this.remainingTargets = remainingTargets;
	}

	/**
	 * Creates a new {@link BranchTargetUnavailableException}.
	 * @param unavailableTargets the branch targets that are not available
	 * @param remainingTargets the branch targets that are still available
	 * @param causingInstruction the instruction that caused the branch target to become unavailable
	 * @param cause
	 */
	public BranchTargetUnavailableException(InstructionFlow[] unavailableTargets, InstructionFlow[] remainingTargets, InstructionFlow causingInstruction, Throwable cause) {
		super(causingInstruction, cause);
		this.unavailableTargets = unavailableTargets;
		this.remainingTargets = remainingTargets;
	}

	/**
	 * Creates a new {@link BranchTargetUnavailableException}.
	 * @param unavailableTargets the branch targets that are not available
	 * @param remainingTargets the branch targets that are still available
	 * @param causingInstruction the instruction that caused the branch target to become unavailable
	 * @param message
	 * @param cause
	 */
	public BranchTargetUnavailableException(InstructionFlow[] unavailableTargets, InstructionFlow[] remainingTargets, InstructionFlow causingInstruction, String message, Throwable cause) {
		super(causingInstruction, message, cause);
		this.unavailableTargets = unavailableTargets;
		this.remainingTargets = remainingTargets;
	}

	/**
	 * @return the unavailableTargets
	 */
	public InstructionFlow[] getUnavailableTargets() {
		return unavailableTargets;
	}

	/**
	 * @return the remainingTargets
	 */
	public InstructionFlow[] getRemainingTargets() {
		return remainingTargets;
	}

}
