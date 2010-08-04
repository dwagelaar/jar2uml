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

import java.util.BitSet;

import org.apache.bcel.verifier.structurals.Frame;

import be.ac.vub.jar2uml.cflow.ControlFlow.InstructionFlow;

/**
 * Represents the combination of current instruction, instruction history,
 * and current execution frame.
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public class ExecutionContext {

	private final InstructionFlow iflow;
	private final LocalHistoryTable history;
	private final BitSet pathHistory;
	private final Frame frame;

	/**
	 * Creates a new {@link ExecutionContext}.
	 * @param iflow
	 * @param history
	 * @param pathHistory
	 * @param frame
	 */
	public ExecutionContext(InstructionFlow iflow, LocalHistoryTable history, BitSet pathHistory, Frame frame) {
		super();
		this.iflow = iflow;
		this.history = history;
		this.pathHistory = pathHistory;
		this.frame = frame;
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
	 * @return the pathHistory
	 */
	public BitSet getPathHistory() {
		return pathHistory;
	}

	/**
	 * @return the frame
	 */
	public Frame getFrame() {
		return frame;
	}

}
