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

import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantClass;
import org.apache.bcel.classfile.ConstantFloat;
import org.apache.bcel.classfile.ConstantInteger;
import org.apache.bcel.classfile.ConstantString;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.LDC;
import org.apache.bcel.generic.LDC_W;
import org.apache.bcel.generic.Type;
import org.apache.bcel.verifier.structurals.ExecutionVisitor;
import org.apache.bcel.verifier.structurals.Frame;
import org.apache.bcel.verifier.structurals.OperandStack;

import be.ac.vub.jar2uml.JarToUMLResources;

/**
 * Work around the bugs in {@link ExecutionVisitor}.
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public class JarToUMLExecutionVisitor extends ExecutionVisitor {
	
	private ConstantPoolGen cpg;
	private Frame frame;

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitLDC_W(org.apache.bcel.generic.LDC_W)
	 */
	@Override
	public void visitLDC_W(LDC_W o) {
		//this method is never invoked because of a missing accept() method in LDC_W
		visitLDC(o);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitLDC(org.apache.bcel.generic.LDC)
	 */
	@Override
	public void visitLDC(LDC o) {
		final OperandStack stack = getFrame().getStack();
		final Constant c = getConstantPoolGen().getConstant(o.getIndex());
		if (c instanceof ConstantInteger){
			stack.push(Type.INT);
		} else if (c instanceof ConstantFloat){
			stack.push(Type.FLOAT);
		} else if (c instanceof ConstantString){
			stack.push(Type.STRING);
		} else if (c instanceof ConstantClass) {
			final StringBuffer className = new StringBuffer();
			className.append('L');
			className.append(((ConstantClass) c).getBytes(getConstantPoolGen().getConstantPool()));
			className.append(';');
			stack.push(Type.getType(className.toString()));
		} else {
			throw new RuntimeException(String.format(
					JarToUMLResources.getString("JarToUMLExecutionVisitor.unexpectedConstant"),
					c));
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#setConstantPoolGen(org.apache.bcel.generic.ConstantPoolGen)
	 */
	@Override
	public void setConstantPoolGen(ConstantPoolGen cpg) {
		super.setConstantPoolGen(cpg);
		this.cpg = cpg;
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#setFrame(org.apache.bcel.verifier.structurals.Frame)
	 */
	@Override
	public void setFrame(Frame f) {
		super.setFrame(f);
		this.frame = f;
	}

	/**
	 * @return the cpg
	 */
	public ConstantPoolGen getConstantPoolGen() {
		return cpg;
	}

	/**
	 * @return the frame
	 */
	public Frame getFrame() {
		return frame;
	}

}
