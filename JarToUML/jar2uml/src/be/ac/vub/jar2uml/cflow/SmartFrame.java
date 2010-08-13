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

import java.util.ArrayList;

import org.apache.bcel.generic.Type;
import org.apache.bcel.verifier.structurals.Frame;
import org.apache.bcel.verifier.structurals.LocalVariables;
import org.apache.bcel.verifier.structurals.OperandStack;

import be.ac.vub.jar2uml.cflow.ControlFlow.InstructionFlow;

/**
 * Execution {@link Frame} that keeps track of extra metadata.
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public class SmartFrame extends Frame {

	protected final InstructionFlow[] localVariableResponsibles;
	protected final ArrayList<InstructionFlow> stackEntryResponsibles = new ArrayList<InstructionFlow>();

	/**
	 * Creates a new {@link SmartFrame}.
	 * @param maxLocals
	 * @param maxStack
	 */
	public SmartFrame(int maxLocals, int maxStack) {
		super(new LocalVariables(maxLocals), new SmartStack(maxStack));
		this.localVariableResponsibles = new InstructionFlow[maxLocals];
		((SmartStack) getStack()).setFrame(this);
	}

	/**
	 * Creates a new {@link SmartFrame}.
	 * @param locals
	 * @param stack
	 */
	protected SmartFrame(LocalVariables locals, SmartStack stack) {
		super(locals, stack);
		this.localVariableResponsibles = new InstructionFlow[locals.maxLocals()];
		((SmartStack) getStack()).setFrame(this);
	}

	/**
	 * @param index
	 * @return the instruction responsible for the value of local variable with slot index
	 */
	public InstructionFlow getResponsibleForLocalVariable(int index) {
		return localVariableResponsibles[index];
	}

	/**
	 * Sets the instruction responsible for the value of local variable with slot index.
	 * @param iflow
	 * @param index
	 */
	public void setResponsibleForLocalVariable(InstructionFlow iflow, int index) {
		localVariableResponsibles[index] = iflow;
	}

	/**
	 * @param index
	 * @return the instruction responsible for the value of {@link OperandStack#peek(int)} at index.
	 */
	public InstructionFlow getResponsibleForStackEntry(int index) {
		return stackEntryResponsibles.get(stackEntryResponsibles.size()-index-1);
	}

	/**
	 * @return the instruction responsible for the value of {@link OperandStack#peek()}
	 */
	public InstructionFlow getResponsibleForStackTop() {
		return stackEntryResponsibles.get(stackEntryResponsibles.size()-1);
	}

	/**
	 * Sets the instruction responsible for the value of {@link OperandStack#peek(int)} at index.
	 * @param iflow
	 * @param index
	 */
	public void setResponsibleForStackEntry(InstructionFlow iflow, int index) {
		stackEntryResponsibles.set(stackEntryResponsibles.size()-index-1, iflow);
	}

	/**
	 * Sets the instruction responsible for the value of {@link OperandStack#peek()}.
	 * @param iflow
	 */
	public void setResponsibleForStackTop(InstructionFlow iflow) {
		stackEntryResponsibles.set(stackEntryResponsibles.size()-1, iflow);
	}

	/**
	 * Clone operation not supported.
	 * @throws UnsupportedOperationException
	 */
	@Override
	protected Object clone() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @return a deep copy of this frame, with {@link InstructionFlow} and {@link Type} references left original
	 */
	public final SmartFrame getCopy() {
		final SmartStack stack = (SmartStack) getStack();
		final SmartStack newstack = new SmartStack(stack.maxStack());
		final SmartFrame frame = new SmartFrame(getLocals().getClone(), newstack);
		//auto-update stackEntryResponsibles
		for (int i = stack.size()-1; i >= 0; i--) {
			newstack.push(stack.peek(i));
		}
		System.arraycopy(localVariableResponsibles, 0, frame.localVariableResponsibles, 0, localVariableResponsibles.length);
		return frame;
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.Frame#toString()
	 */
	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer();
		final LocalVariables locals = getLocals();

		sb.append(locals.getClass().getSimpleName());
		sb.append(":\n");
		for (int i = 0; i < locals.maxLocals(); i++) {
			sb.append(i);
			sb.append(": ");
			sb.append(locals.get(i));
			sb.append(" <= ");
			sb.append(getResponsibleForLocalVariable(i));
			sb.append("\n");
		}

		sb.append(getStack().getClass().getSimpleName());
		sb.append(":\n");
		sb.append(getStack());
		return sb.toString();
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.Frame#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof SmartFrame)) {
            return false; // implies "null" is non-equal.
        }
		SmartFrame f = (SmartFrame) o;
		return localVariableResponsibles == f.localVariableResponsibles
			&& stackEntryResponsibles.equals(f.stackEntryResponsibles)
			&& super.equals(o);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.Frame#hashCode()
	 * @see java.util.List#hashCode()
	 */
	@Override
	public int hashCode() {
		int lvrHashCode = 1;
		for (InstructionFlow iflow : localVariableResponsibles) {
			lvrHashCode = 31 * lvrHashCode + (iflow == null ? 0 : iflow.hashCode());
		}
		return super.hashCode() ^ lvrHashCode ^ stackEntryResponsibles.hashCode();
	}

}
