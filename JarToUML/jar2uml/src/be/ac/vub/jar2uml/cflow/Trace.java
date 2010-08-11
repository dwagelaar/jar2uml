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

import java.util.Iterator;

import be.ac.vub.jar2uml.AddMethodOpCode;
import be.ac.vub.jar2uml.cflow.ControlFlow.InstructionFlow;

/**
 * Execution trace for a search path of {@link AddMethodOpCode}.
 * Supports branching off alternative traces.
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public class Trace implements Iterable<Trace> {

	private class TraceIterator implements Iterator<Trace> {

		Trace cursor = new Trace(null, 0, Trace.this); //put the cursor one ahead

		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#hasNext()
		 */
		public boolean hasNext() {
			return cursor.getBranchedFrom() != null && cursor.getBranchedFrom().getInstruction() != null;
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#next()
		 */
		public Trace next() {
			cursor = cursor.getBranchedFrom();
			return cursor;
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#remove()
		 */
		public void remove() {
			throw new UnsupportedOperationException();			
		}
		
	}

	private final InstructionFlow iflow;
	private final int successors;
	private final Trace branchedFrom;

	/**
	 * Creates a new {@link Trace}.
	 */
	public Trace() {
		this(null, 0, null);
	}

	/**
	 * Creates a new {@link Trace} branched from another {@link Trace}.
	 * @param entry the trace entry that represents the last added entry to this trace
	 * @param branchedFrom the previous trace object
	 */
	protected Trace(final InstructionFlow entry, final int successors, final Trace branchedFrom) {
		super();
		this.iflow = entry;
		this.successors = successors;
		this.branchedFrom = branchedFrom;
	}

	/**
	 * @return the instruction
	 */
	public InstructionFlow getInstruction() {
		return iflow;
	}

	/**
	 * @return the successors
	 */
	public int getSuccessors() {
		return successors;
	}

	/**
	 * @return the branchedFrom
	 */
	public Trace getBranchedFrom() {
		return branchedFrom;
	}

	/**
	 * Adds a new entry to the trace.
	 * @param iflow the instruction
	 * @param successors the amount of potential successors this instruction has
	 * @return the trace object including the added entry
	 */
	public Trace addEntry(InstructionFlow iflow, int successors) {
		return new Trace(iflow, successors, this);
	}

	/**
	 * {@inheritDoc}
	 * Runs from the last added entry to the first.
	 */
	public Iterator<Trace> iterator() {
		return new TraceIterator();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append('[');
		for (Trace entry : this) {
			sb.append(entry.getInstruction());
			sb.append("(succ="); //$NON-NLS-1$
			sb.append(entry.getSuccessors());
			sb.append("),\n"); //$NON-NLS-1$
		}
		if (sb.length() > 1) {
			sb.deleteCharAt(sb.length()-1);
			sb.setCharAt(sb.length()-1, ']');
		} else {
			sb.append(']');
		}
		return sb.toString();
	}
}
