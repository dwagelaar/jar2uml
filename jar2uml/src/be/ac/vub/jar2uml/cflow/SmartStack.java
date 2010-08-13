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

import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.Type;
import org.apache.bcel.verifier.structurals.OperandStack;

/**
 * {@link OperandStack} for use with a {@link SmartFrame}.
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 *
 */
public class SmartStack extends OperandStack {

	private SmartFrame frame;

	/**
	 * Creates an otherwise empty stack with a maximum of
	 * maxStack slots and the ObjectType 'obj' at the top.
	 * @param maxStack
	 * @param obj
	 */
	public SmartStack(int maxStack, ObjectType obj) {
		super(maxStack, obj);
	}

	/**
	 * Creates an empty stack with a maximum of maxStack slots.
	 * @param maxStack
	 */
	public SmartStack(int maxStack) {
		super(maxStack);
	}

	/**
	 * @param frame the frame to set
	 */
	protected void setFrame(SmartFrame frame) {
		this.frame = frame;
	}

	/**
	 * @return the frame
	 */
	public SmartFrame getFrame() {
		return frame;
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.OperandStack#pop()
	 */
	@Override
	public Type pop() {
		frame.stackEntryResponsibles.remove(frame.stackEntryResponsibles.size()-1);
		return super.pop();
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.OperandStack#push(org.apache.bcel.generic.Type)
	 */
	@Override
	public void push(Type type) {
		super.push(type);
		frame.stackEntryResponsibles.add(null);
	}

	/**
	 * Clone operation not supported.
	 * @throws UnsupportedOperationException
	 */
	@Override
	protected Object clone() {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.OperandStack#toString()
	 */
	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer();
		sb.append("Slots used: ");
		sb.append(slotsUsed());
		sb.append(" MaxStack: ");
		sb.append(maxStack());
		sb.append(".\n");
		for (int i=0; i<size(); i++){
			sb.append(peek(i));
			sb.append(" (Size: ");
			sb.append(peek(i).getSize());
			sb.append(") <= ");
			sb.append(getFrame().getResponsibleForStackEntry(i));
			sb.append("\n");
		}
		return sb.toString();
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.OperandStack#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof SmartStack)) {
            return false;
        }
		SmartStack s = (SmartStack) o;
		return frame.equals(s.frame) && super.equals(o);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.OperandStack#hashCode()
	 */
	@Override
	public int hashCode() {
		return super.hashCode() ^ frame.hashCode();
	}

}