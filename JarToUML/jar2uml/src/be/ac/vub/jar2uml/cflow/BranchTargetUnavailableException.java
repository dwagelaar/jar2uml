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
public class BranchTargetUnavailableException extends RuntimeException {

	private static final long serialVersionUID = -2302822625940595508L;

	private final InstructionFlow[] remainingTargets;

	/**
	 * Creates a new {@link BranchTargetUnavailableException}.
	 * @param remainingTargets the branch targets that are still available
	 */
	public BranchTargetUnavailableException(InstructionFlow[] remainingTargets) {
		super();
		this.remainingTargets = remainingTargets;
	}

	/**
	 * Creates a new {@link BranchTargetUnavailableException}.
	 * @param remainingTargets the branch targets that are still available
	 * @param message
	 */
	public BranchTargetUnavailableException(InstructionFlow[] remainingTargets, String message) {
		super(message);
		this.remainingTargets = remainingTargets;
	}

	/**
	 * Creates a new {@link BranchTargetUnavailableException}.
	 * @param remainingTargets the branch targets that are still available
	 * @param cause
	 */
	public BranchTargetUnavailableException(InstructionFlow[] remainingTargets, Throwable cause) {
		super(cause);
		this.remainingTargets = remainingTargets;
	}

	/**
	 * Creates a new {@link BranchTargetUnavailableException}.
	 * @param remainingTargets the branch targets that are still available
	 * @param message
	 * @param cause
	 */
	public BranchTargetUnavailableException(InstructionFlow[] remainingTargets, String message, Throwable cause) {
		super(message, cause);
		this.remainingTargets = remainingTargets;
	}

	/**
	 * @return the remainingTargets
	 */
	public InstructionFlow[] getRemainingTargets() {
		return remainingTargets;
	}

}
