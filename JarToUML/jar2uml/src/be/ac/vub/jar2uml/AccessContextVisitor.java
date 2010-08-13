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
package be.ac.vub.jar2uml;

import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.generic.AALOAD;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.EmptyVisitor;
import org.apache.bcel.generic.GETFIELD;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.PUTFIELD;

import be.ac.vub.jar2uml.cflow.AccessContextUnavailableException;
import be.ac.vub.jar2uml.cflow.SmartFrame;
import be.ac.vub.jar2uml.cflow.VisitorWithFrame;

/**
 * Instruction visitor that checks for valid access context for relevant instructions.
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public class AccessContextVisitor extends EmptyVisitor implements VisitorWithFrame {

	private ConstantPool cp;
	protected ConstantPoolGen cpg;
	private SmartFrame frame;

	/**
	 * Creates a new {@link AccessContextVisitor}.
	 */
	public AccessContextVisitor() {
		super();
	}

	/**
	 * @param instr
	 * @return The access context of the (dynamic) invoke instruction, if available.
	 * @throws AccessContextUnavailableException if the access context is not available
	 */
	protected org.apache.bcel.generic.Type getAccessContext(final InvokeInstruction instr) {
		assert !(instr instanceof INVOKESTATIC);
		final SmartFrame frame = getFrame();
		assert frame != null;
		final int stackIndex = instr.getArgumentTypes(getCpg()).length;
		final org.apache.bcel.generic.Type accessContext = frame.getStack().peek(stackIndex);
		if (accessContext.equals(org.apache.bcel.generic.Type.NULL)) {
			throw new AccessContextUnavailableException(frame.getResponsibleForStackEntry(stackIndex));
		}
		return accessContext;
	}

	/**
	 * @return The access context of a GETFIELD instruction, if available.
	 * @throws AccessContextUnavailableException if the access context is not available
	 */
	protected org.apache.bcel.generic.Type getGetFieldAccessContext() {
		final SmartFrame frame = getFrame();
		assert frame != null;
		final org.apache.bcel.generic.Type accessContext = frame.getStack().peek();
		if (accessContext.equals(org.apache.bcel.generic.Type.NULL)) {
			throw new AccessContextUnavailableException(frame.getResponsibleForStackTop());
		}
		return accessContext;
	}

	/**
	 * @return The access context of a PUTFIELD instruction, if available.
	 * @throws AccessContextUnavailableException if the access context is not available
	 */
	protected org.apache.bcel.generic.Type getPutFieldAccessContext() {
		final SmartFrame frame = getFrame();
		assert frame != null;
		final org.apache.bcel.generic.Type accessContext = frame.getStack().peek(1);
		if (accessContext.equals(org.apache.bcel.generic.Type.NULL)) {
			throw new AccessContextUnavailableException(frame.getResponsibleForStackEntry(1));
		}
		return accessContext;
	}

	/**
	 * @return The {@link ConstantPool} to use for the instructions.
	 */
	public ConstantPool getCp() {
		return cp;
	}

	/**
	 * Sets the {@link ConstantPool} to use for the instructions.
	 * @param cp
	 */
	public void setCp(ConstantPool cp) {
		this.cp = cp;
		if (cp == null) {
			this.cpg = null;
		} else {
			this.cpg = new ConstantPoolGen(cp);
		}
	}

	/**
	 * @return The {@link ConstantPoolGen} for {@link #getCp()}.
	 */
	public ConstantPoolGen getCpg() {
		return cpg;
	}

	/**
	 * @return the frame
	 */
	public SmartFrame getFrame() {
		return frame;
	}

	/**
	 * @param frame the frame to set
	 */
	public void setFrame(SmartFrame frame) {
		this.frame = frame;
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.generic.EmptyVisitor#visitAALOAD(org.apache.bcel.generic.AALOAD)
	 */
	@Override
	public void visitAALOAD(AALOAD obj) {
		getGetFieldAccessContext();
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.bcel.generic.EmptyVisitor#visitGETFIELD(org.apache.bcel.generic.GETFIELD)
	 */
	@Override
	public void visitGETFIELD(GETFIELD obj) {
		getGetFieldAccessContext();
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.bcel.generic.EmptyVisitor#visitInvokeInstruction(org.apache.bcel.generic.InvokeInstruction)
	 */
	@Override
	public void visitInvokeInstruction(InvokeInstruction obj) {
		if (!(obj instanceof INVOKESTATIC)) {
			getAccessContext(obj);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.bcel.generic.EmptyVisitor#visitPUTFIELD(org.apache.bcel.generic.PUTFIELD)
	 */
	@Override
	public void visitPUTFIELD(PUTFIELD obj) {
		getPutFieldAccessContext();
	}

}