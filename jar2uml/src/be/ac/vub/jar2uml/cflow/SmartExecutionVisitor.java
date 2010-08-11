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
import org.apache.bcel.classfile.ConstantDouble;
import org.apache.bcel.classfile.ConstantFloat;
import org.apache.bcel.classfile.ConstantInteger;
import org.apache.bcel.classfile.ConstantLong;
import org.apache.bcel.classfile.ConstantString;
import org.apache.bcel.generic.*;
import org.apache.bcel.verifier.structurals.ExecutionVisitor;
import org.apache.bcel.verifier.structurals.Frame;
import org.apache.bcel.verifier.structurals.OperandStack;

import be.ac.vub.jar2uml.JarToUMLResources;
import be.ac.vub.jar2uml.cflow.ControlFlow.InstructionFlow;

/**
 * Keeps track of {@link Type#NULL} origins in a {@link SmartFrame} 
 * and works around the bugs in {@link ExecutionVisitor}.
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public class SmartExecutionVisitor extends ExecutionVisitor {
	
	private ConstantPoolGen cpg;
	private SmartFrame frame;
	private InstructionFlow iflow;

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitAALOAD(org.apache.bcel.generic.AALOAD)
	 */
	@Override
	public void visitAALOAD(AALOAD o) {
		super.visitAALOAD(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitACONST_NULL(org.apache.bcel.generic.ACONST_NULL)
	 */
	@Override
	public void visitACONST_NULL(ACONST_NULL o) {
		super.visitACONST_NULL(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitANEWARRAY(org.apache.bcel.generic.ANEWARRAY)
	 */
	@Override
	public void visitANEWARRAY(ANEWARRAY o) {
		super.visitANEWARRAY(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitALOAD(org.apache.bcel.generic.ALOAD)
	 */
	@Override
	public void visitALOAD(ALOAD o) {
		super.visitALOAD(o);
		frame.setResponsibleForStackTop(frame.getResponsibleForLocalVariable(o.getIndex()));
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitARRAYLENGTH(org.apache.bcel.generic.ARRAYLENGTH)
	 */
	@Override
	public void visitARRAYLENGTH(ARRAYLENGTH o) {
		super.visitARRAYLENGTH(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitASTORE(org.apache.bcel.generic.ASTORE)
	 */
	@Override
	public void visitASTORE(ASTORE o) {
		final InstructionFlow iflow1 = frame.getResponsibleForStackTop();
		super.visitASTORE(o);
		frame.setResponsibleForLocalVariable(iflow1, o.getIndex());
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitATHROW(org.apache.bcel.generic.ATHROW)
	 */
	@Override
	public void visitATHROW(ATHROW o) {
		super.visitATHROW(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitBALOAD(org.apache.bcel.generic.BALOAD)
	 */
	@Override
	public void visitBALOAD(BALOAD o) {
		super.visitBALOAD(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitBIPUSH(org.apache.bcel.generic.BIPUSH)
	 */
	@Override
	public void visitBIPUSH(BIPUSH o) {
		super.visitBIPUSH(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitCALOAD(org.apache.bcel.generic.CALOAD)
	 */
	@Override
	public void visitCALOAD(CALOAD o) {
		super.visitCALOAD(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitCHECKCAST(org.apache.bcel.generic.CHECKCAST)
	 */
	@Override
	public void visitCHECKCAST(CHECKCAST o) {
		super.visitCHECKCAST(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitD2F(org.apache.bcel.generic.D2F)
	 */
	@Override
	public void visitD2F(D2F o) {
		super.visitD2F(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitD2I(org.apache.bcel.generic.D2I)
	 */
	@Override
	public void visitD2I(D2I o) {
		super.visitD2I(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitD2L(org.apache.bcel.generic.D2L)
	 */
	@Override
	public void visitD2L(D2L o) {
		super.visitD2L(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitDADD(org.apache.bcel.generic.DADD)
	 */
	@Override
	public void visitDADD(DADD o) {
		super.visitDADD(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitDALOAD(org.apache.bcel.generic.DALOAD)
	 */
	@Override
	public void visitDALOAD(DALOAD o) {
		super.visitDALOAD(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitDCMPG(org.apache.bcel.generic.DCMPG)
	 */
	@Override
	public void visitDCMPG(DCMPG o) {
		super.visitDCMPG(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitDCMPL(org.apache.bcel.generic.DCMPL)
	 */
	@Override
	public void visitDCMPL(DCMPL o) {
		super.visitDCMPL(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitDCONST(org.apache.bcel.generic.DCONST)
	 */
	@Override
	public void visitDCONST(DCONST o) {
		super.visitDCONST(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitDDIV(org.apache.bcel.generic.DDIV)
	 */
	@Override
	public void visitDDIV(DDIV o) {
		super.visitDDIV(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitDLOAD(org.apache.bcel.generic.DLOAD)
	 */
	@Override
	public void visitDLOAD(DLOAD o) {
		super.visitDLOAD(o);
		frame.setResponsibleForStackTop(frame.getResponsibleForLocalVariable(o.getIndex()));
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitDMUL(org.apache.bcel.generic.DMUL)
	 */
	@Override
	public void visitDMUL(DMUL o) {
		super.visitDMUL(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitDNEG(org.apache.bcel.generic.DNEG)
	 */
	@Override
	public void visitDNEG(DNEG o) {
		super.visitDNEG(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitDREM(org.apache.bcel.generic.DREM)
	 */
	@Override
	public void visitDREM(DREM o) {
		super.visitDREM(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitDSTORE(org.apache.bcel.generic.DSTORE)
	 */
	@Override
	public void visitDSTORE(DSTORE o) {
		final InstructionFlow iflow1 = frame.getResponsibleForStackTop();
		super.visitDSTORE(o);
		frame.setResponsibleForLocalVariable(iflow1, o.getIndex());
		frame.setResponsibleForLocalVariable(iflow, o.getIndex()+1);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitDSUB(org.apache.bcel.generic.DSUB)
	 */
	@Override
	public void visitDSUB(DSUB o) {
		super.visitDSUB(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitDUP_X1(org.apache.bcel.generic.DUP_X1)
	 */
	@Override
	public void visitDUP_X1(DUP_X1 o) {
		final InstructionFlow iflow1 = frame.getResponsibleForStackTop();
		final InstructionFlow iflow2 = frame.getResponsibleForStackEntry(1);
		super.visitDUP_X1(o);
		frame.setResponsibleForStackTop(iflow1);
		frame.setResponsibleForStackEntry(iflow2, 1);
		frame.setResponsibleForStackEntry(iflow1, 2);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitDUP_X2(org.apache.bcel.generic.DUP_X2)
	 */
	@Override
	public void visitDUP_X2(DUP_X2 o) {
		final InstructionFlow iflow1 = frame.getResponsibleForStackTop();
		final InstructionFlow iflow2 = frame.getResponsibleForStackEntry(1);
		InstructionFlow iflow3 = null;
		final Type w2 = frame.getStack().peek(1);
		if (w2.getSize() != 2) {
			iflow3 = frame.getResponsibleForStackEntry(2);
		}
		super.visitDUP_X2(o);
		if (w2.getSize() == 2) {
			frame.setResponsibleForStackTop(iflow1);
			frame.setResponsibleForStackEntry(iflow2, 1);
			frame.setResponsibleForStackEntry(iflow1, 2);
		} else {
			frame.setResponsibleForStackTop(iflow1);
			frame.setResponsibleForStackEntry(iflow2, 1);
			frame.setResponsibleForStackEntry(iflow3, 2);
			frame.setResponsibleForStackEntry(iflow1, 3);
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitDUP(org.apache.bcel.generic.DUP)
	 */
	@Override
	public void visitDUP(DUP o) {
		final InstructionFlow iflow1 = frame.getResponsibleForStackTop();
		super.visitDUP(o);
		frame.setResponsibleForStackTop(iflow1);
		frame.setResponsibleForStackTop(iflow1);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitDUP2_X1(org.apache.bcel.generic.DUP2_X1)
	 */
	@Override
	public void visitDUP2_X1(DUP2_X1 o) {
		final InstructionFlow iflow1 = frame.getResponsibleForStackTop();
		final InstructionFlow iflow2 = frame.getResponsibleForStackEntry(1);
		InstructionFlow iflow3 = null;
		final Type w1 = frame.getStack().peek();
		if (w1.getSize() != 2) {
			iflow3 = frame.getResponsibleForStackEntry(2);
		}
		super.visitDUP2_X1(o);
		if (w1.getSize() == 2) {
			frame.setResponsibleForStackTop(iflow1);
			frame.setResponsibleForStackEntry(iflow2, 1);
			frame.setResponsibleForStackEntry(iflow1, 2);
		} else {
			frame.setResponsibleForStackTop(iflow1);
			frame.setResponsibleForStackEntry(iflow2, 1);
			frame.setResponsibleForStackEntry(iflow3, 2);
			frame.setResponsibleForStackEntry(iflow1, 3);
			frame.setResponsibleForStackEntry(iflow2, 4);
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitDUP2_X2(org.apache.bcel.generic.DUP2_X2)
	 */
	@Override
	public void visitDUP2_X2(DUP2_X2 o) {
		final InstructionFlow iflow1 = frame.getResponsibleForStackTop();
		final InstructionFlow iflow2 = frame.getResponsibleForStackEntry(1);
		InstructionFlow iflow3 = null;
		InstructionFlow iflow4 = null;
		final Type w1 = frame.getStack().peek();
		Type w2 = null;
		Type w3 = null;
		if (w1.getSize() != 2) {
			iflow3 = frame.getResponsibleForStackEntry(2);
			w3 = frame.getStack().peek(2);
			if (w3.getSize() != 2) {
				iflow4 = frame.getResponsibleForStackEntry(3);
			}
		} else {
			w2 = frame.getStack().peek(1);
			if (w2.getSize() != 2) {
				iflow3 = frame.getResponsibleForStackEntry(2);
			}
		}
		super.visitDUP2_X2(o);
		if (w1.getSize() == 2) {
			if (w2.getSize() == 2) {
				frame.setResponsibleForStackTop(iflow1);
				frame.setResponsibleForStackEntry(iflow2, 1);
				frame.setResponsibleForStackEntry(iflow1, 2);
			} else {
				frame.setResponsibleForStackTop(iflow1);
				frame.setResponsibleForStackEntry(iflow2, 1);
				frame.setResponsibleForStackEntry(iflow3, 2);
				frame.setResponsibleForStackEntry(iflow1, 3);
			}
		} else {
			if (w3.getSize() == 2) {
				frame.setResponsibleForStackTop(iflow1);
				frame.setResponsibleForStackEntry(iflow2, 1);
				frame.setResponsibleForStackEntry(iflow3, 2);
				frame.setResponsibleForStackEntry(iflow1, 3);
				frame.setResponsibleForStackEntry(iflow2, 4);
			} else {
				frame.setResponsibleForStackTop(iflow1);
				frame.setResponsibleForStackEntry(iflow2, 1);
				frame.setResponsibleForStackEntry(iflow3, 2);
				frame.setResponsibleForStackEntry(iflow4, 3);
				frame.setResponsibleForStackEntry(iflow1, 4);
				frame.setResponsibleForStackEntry(iflow2, 5);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitDUP2(org.apache.bcel.generic.DUP2)
	 */
	@Override
	public void visitDUP2(DUP2 o) {
		final InstructionFlow iflow1 = frame.getResponsibleForStackTop();
		InstructionFlow iflow2 = null;
		final Type w1 = frame.getStack().peek();
		if (w1.getSize() != 2) {
			iflow2 = frame.getResponsibleForStackEntry(1);
		}
		super.visitDUP2(o);
		if (w1.getSize() == 2) {
			frame.setResponsibleForStackTop(iflow1);
			frame.setResponsibleForStackEntry(iflow1, 1);
		} else {
			frame.setResponsibleForStackTop(iflow1);
			frame.setResponsibleForStackEntry(iflow2, 1);
			frame.setResponsibleForStackEntry(iflow1, 2);
			frame.setResponsibleForStackEntry(iflow2, 3);
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitF2D(org.apache.bcel.generic.F2D)
	 */
	@Override
	public void visitF2D(F2D o) {
		super.visitF2D(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitF2I(org.apache.bcel.generic.F2I)
	 */
	@Override
	public void visitF2I(F2I o) {
		super.visitF2I(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitF2L(org.apache.bcel.generic.F2L)
	 */
	@Override
	public void visitF2L(F2L o) {
		super.visitF2L(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitFADD(org.apache.bcel.generic.FADD)
	 */
	@Override
	public void visitFADD(FADD o) {
		super.visitFADD(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitFALOAD(org.apache.bcel.generic.FALOAD)
	 */
	@Override
	public void visitFALOAD(FALOAD o) {
		super.visitFALOAD(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitFCMPG(org.apache.bcel.generic.FCMPG)
	 */
	@Override
	public void visitFCMPG(FCMPG o) {
		super.visitFCMPG(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitFCMPL(org.apache.bcel.generic.FCMPL)
	 */
	@Override
	public void visitFCMPL(FCMPL o) {
		super.visitFCMPL(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitFCONST(org.apache.bcel.generic.FCONST)
	 */
	@Override
	public void visitFCONST(FCONST o) {
		super.visitFCONST(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitFDIV(org.apache.bcel.generic.FDIV)
	 */
	@Override
	public void visitFDIV(FDIV o) {
		super.visitFDIV(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitFLOAD(org.apache.bcel.generic.FLOAD)
	 */
	@Override
	public void visitFLOAD(FLOAD o) {
		super.visitFLOAD(o);
		frame.setResponsibleForStackTop(frame.getResponsibleForLocalVariable(o.getIndex()));
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitFMUL(org.apache.bcel.generic.FMUL)
	 */
	@Override
	public void visitFMUL(FMUL o) {
		super.visitFMUL(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitFNEG(org.apache.bcel.generic.FNEG)
	 */
	@Override
	public void visitFNEG(FNEG o) {
		super.visitFNEG(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitFREM(org.apache.bcel.generic.FREM)
	 */
	@Override
	public void visitFREM(FREM o) {
		super.visitFREM(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitFSTORE(org.apache.bcel.generic.FSTORE)
	 */
	@Override
	public void visitFSTORE(FSTORE o) {
		final InstructionFlow iflow1 = frame.getResponsibleForStackTop();
		super.visitFSTORE(o);
		frame.setResponsibleForLocalVariable(iflow1, o.getIndex());
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitFSUB(org.apache.bcel.generic.FSUB)
	 */
	@Override
	public void visitFSUB(FSUB o) {
		super.visitFSUB(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitGETFIELD(org.apache.bcel.generic.GETFIELD)
	 */
	@Override
	public void visitGETFIELD(GETFIELD o) {
		super.visitGETFIELD(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitGETSTATIC(org.apache.bcel.generic.GETSTATIC)
	 */
	@Override
	public void visitGETSTATIC(GETSTATIC o) {
		super.visitGETSTATIC(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitI2B(org.apache.bcel.generic.I2B)
	 */
	@Override
	public void visitI2B(I2B o) {
		super.visitI2B(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitI2C(org.apache.bcel.generic.I2C)
	 */
	@Override
	public void visitI2C(I2C o) {
		super.visitI2C(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitI2D(org.apache.bcel.generic.I2D)
	 */
	@Override
	public void visitI2D(I2D o) {
		super.visitI2D(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitI2F(org.apache.bcel.generic.I2F)
	 */
	@Override
	public void visitI2F(I2F o) {
		super.visitI2F(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitI2L(org.apache.bcel.generic.I2L)
	 */
	@Override
	public void visitI2L(I2L o) {
		super.visitI2L(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitI2S(org.apache.bcel.generic.I2S)
	 */
	@Override
	public void visitI2S(I2S o) {
		super.visitI2S(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitIADD(org.apache.bcel.generic.IADD)
	 */
	@Override
	public void visitIADD(IADD o) {
		super.visitIADD(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitIALOAD(org.apache.bcel.generic.IALOAD)
	 */
	@Override
	public void visitIALOAD(IALOAD o) {
		super.visitIALOAD(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitIAND(org.apache.bcel.generic.IAND)
	 */
	@Override
	public void visitIAND(IAND o) {
		super.visitIAND(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitICONST(org.apache.bcel.generic.ICONST)
	 */
	@Override
	public void visitICONST(ICONST o) {
		super.visitICONST(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitIDIV(org.apache.bcel.generic.IDIV)
	 */
	@Override
	public void visitIDIV(IDIV o) {
		super.visitIDIV(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitILOAD(org.apache.bcel.generic.ILOAD)
	 */
	@Override
	public void visitILOAD(ILOAD o) {
		super.visitILOAD(o);
		frame.setResponsibleForStackTop(frame.getResponsibleForLocalVariable(o.getIndex()));
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitIMUL(org.apache.bcel.generic.IMUL)
	 */
	@Override
	public void visitIMUL(IMUL o) {
		super.visitIMUL(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitINEG(org.apache.bcel.generic.INEG)
	 */
	@Override
	public void visitINEG(INEG o) {
		super.visitINEG(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitINSTANCEOF(org.apache.bcel.generic.INSTANCEOF)
	 */
	@Override
	public void visitINSTANCEOF(INSTANCEOF o) {
		super.visitINSTANCEOF(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitINVOKEINTERFACE(org.apache.bcel.generic.INVOKEINTERFACE)
	 */
	@Override
	public void visitINVOKEINTERFACE(INVOKEINTERFACE o) {
		super.visitINVOKEINTERFACE(o);
		if (o.getReturnType(cpg) != Type.VOID){
			frame.setResponsibleForStackTop(iflow);
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitINVOKESPECIAL(org.apache.bcel.generic.INVOKESPECIAL)
	 */
	@Override
	public void visitINVOKESPECIAL(INVOKESPECIAL o) {
		super.visitINVOKESPECIAL(o);
		if (o.getReturnType(cpg) != Type.VOID){
			frame.setResponsibleForStackTop(iflow);
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitINVOKESTATIC(org.apache.bcel.generic.INVOKESTATIC)
	 */
	@Override
	public void visitINVOKESTATIC(INVOKESTATIC o) {
		super.visitINVOKESTATIC(o);
		if (o.getReturnType(cpg) != Type.VOID){
			frame.setResponsibleForStackTop(iflow);
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitINVOKEVIRTUAL(org.apache.bcel.generic.INVOKEVIRTUAL)
	 */
	@Override
	public void visitINVOKEVIRTUAL(INVOKEVIRTUAL o) {
		super.visitINVOKEVIRTUAL(o);
		if (o.getReturnType(cpg) != Type.VOID){
			frame.setResponsibleForStackTop(iflow);
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitIOR(org.apache.bcel.generic.IOR)
	 */
	@Override
	public void visitIOR(IOR o) {
		super.visitIOR(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitIREM(org.apache.bcel.generic.IREM)
	 */
	@Override
	public void visitIREM(IREM o) {
		super.visitIREM(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitISHL(org.apache.bcel.generic.ISHL)
	 */
	@Override
	public void visitISHL(ISHL o) {
		super.visitISHL(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitISHR(org.apache.bcel.generic.ISHR)
	 */
	@Override
	public void visitISHR(ISHR o) {
		super.visitISHR(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitISTORE(org.apache.bcel.generic.ISTORE)
	 */
	@Override
	public void visitISTORE(ISTORE o) {
		final InstructionFlow iflow1 = frame.getResponsibleForStackTop();
		super.visitISTORE(o);
		frame.setResponsibleForLocalVariable(iflow1, o.getIndex());
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitISUB(org.apache.bcel.generic.ISUB)
	 */
	@Override
	public void visitISUB(ISUB o) {
		super.visitISUB(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitIUSHR(org.apache.bcel.generic.IUSHR)
	 */
	@Override
	public void visitIUSHR(IUSHR o) {
		super.visitIUSHR(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitIXOR(org.apache.bcel.generic.IXOR)
	 */
	@Override
	public void visitIXOR(IXOR o) {
		super.visitIXOR(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitJSR_W(org.apache.bcel.generic.JSR_W)
	 */
	@Override
	public void visitJSR_W(JSR_W o) {
		super.visitJSR_W(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitJSR(org.apache.bcel.generic.JSR)
	 */
	@Override
	public void visitJSR(JSR o) {
		super.visitJSR(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitL2D(org.apache.bcel.generic.L2D)
	 */
	@Override
	public void visitL2D(L2D o) {
		super.visitL2D(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitL2F(org.apache.bcel.generic.L2F)
	 */
	@Override
	public void visitL2F(L2F o) {
		super.visitL2F(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitL2I(org.apache.bcel.generic.L2I)
	 */
	@Override
	public void visitL2I(L2I o) {
		super.visitL2I(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitLADD(org.apache.bcel.generic.LADD)
	 */
	@Override
	public void visitLADD(LADD o) {
		super.visitLADD(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitLALOAD(org.apache.bcel.generic.LALOAD)
	 */
	@Override
	public void visitLALOAD(LALOAD o) {
		super.visitLALOAD(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitLAND(org.apache.bcel.generic.LAND)
	 */
	@Override
	public void visitLAND(LAND o) {
		super.visitLAND(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitLCMP(org.apache.bcel.generic.LCMP)
	 */
	@Override
	public void visitLCMP(LCMP o) {
		super.visitLCMP(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitLCONST(org.apache.bcel.generic.LCONST)
	 */
	@Override
	public void visitLCONST(LCONST o) {
		super.visitLCONST(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/**
	 * {@inheritDoc}
	 * Fixes bug in {@link ExecutionVisitor}, which does not support LDC'ing a {@link ConstantClass}.
	 * @throws RuntimeException when an unexpected constant is encountered
	 */
	@Override
	public void visitLDC(LDC o) {
		final OperandStack stack = getFrame().getStack();
		final Constant c = cpg.getConstant(o.getIndex());
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
		frame.setResponsibleForStackTop(iflow);
	}

	/**
	 * Executes {@link #visitLDC(LDC)}.
	 */
	@Override
	public void visitLDC_W(LDC_W o) {
		//this method is never invoked because of a missing accept() method in LDC_W
		visitLDC(o);
	}

	/**
	 * {@inheritDoc}
	 * @throws RuntimeException when an unexpected constant is encountered
	 */
	@Override
	public void visitLDC2_W(LDC2_W o) {
		final OperandStack stack = getFrame().getStack();
		final Constant c = cpg.getConstant(o.getIndex());
		if (c instanceof ConstantLong){
			stack.push(Type.LONG);
		} else if (c instanceof ConstantDouble){
			stack.push(Type.DOUBLE);
		} else {
			throw new RuntimeException(String.format(
					JarToUMLResources.getString("JarToUMLExecutionVisitor.unexpectedConstant"),
					c));
		}
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitLDIV(org.apache.bcel.generic.LDIV)
	 */
	@Override
	public void visitLDIV(LDIV o) {
		super.visitLDIV(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitLLOAD(org.apache.bcel.generic.LLOAD)
	 */
	@Override
	public void visitLLOAD(LLOAD o) {
		super.visitLLOAD(o);
		frame.setResponsibleForStackTop(frame.getResponsibleForLocalVariable(o.getIndex()));
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitLMUL(org.apache.bcel.generic.LMUL)
	 */
	@Override
	public void visitLMUL(LMUL o) {
		super.visitLMUL(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitLNEG(org.apache.bcel.generic.LNEG)
	 */
	@Override
	public void visitLNEG(LNEG o) {
		super.visitLNEG(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitLOR(org.apache.bcel.generic.LOR)
	 */
	@Override
	public void visitLOR(LOR o) {
		super.visitLOR(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitLREM(org.apache.bcel.generic.LREM)
	 */
	@Override
	public void visitLREM(LREM o) {
		super.visitLREM(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitLSHL(org.apache.bcel.generic.LSHL)
	 */
	@Override
	public void visitLSHL(LSHL o) {
		super.visitLSHL(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitLSHR(org.apache.bcel.generic.LSHR)
	 */
	@Override
	public void visitLSHR(LSHR o) {
		super.visitLSHR(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitLSTORE(org.apache.bcel.generic.LSTORE)
	 */
	@Override
	public void visitLSTORE(LSTORE o) {
		final InstructionFlow iflow1 = frame.getResponsibleForStackTop();
		super.visitLSTORE(o);
		frame.setResponsibleForLocalVariable(iflow1, o.getIndex());
		frame.setResponsibleForLocalVariable(iflow, o.getIndex()+1);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitLSUB(org.apache.bcel.generic.LSUB)
	 */
	@Override
	public void visitLSUB(LSUB o) {
		super.visitLSUB(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitLUSHR(org.apache.bcel.generic.LUSHR)
	 */
	@Override
	public void visitLUSHR(LUSHR o) {
		super.visitLUSHR(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitLXOR(org.apache.bcel.generic.LXOR)
	 */
	@Override
	public void visitLXOR(LXOR o) {
		super.visitLXOR(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitMULTIANEWARRAY(org.apache.bcel.generic.MULTIANEWARRAY)
	 */
	@Override
	public void visitMULTIANEWARRAY(MULTIANEWARRAY o) {
		super.visitMULTIANEWARRAY(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitNEW(org.apache.bcel.generic.NEW)
	 */
	@Override
	public void visitNEW(NEW o) {
		super.visitNEW(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitNEWARRAY(org.apache.bcel.generic.NEWARRAY)
	 */
	@Override
	public void visitNEWARRAY(NEWARRAY o) {
		super.visitNEWARRAY(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitSALOAD(org.apache.bcel.generic.SALOAD)
	 */
	@Override
	public void visitSALOAD(SALOAD o) {
		super.visitSALOAD(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitSIPUSH(org.apache.bcel.generic.SIPUSH)
	 */
	@Override
	public void visitSIPUSH(SIPUSH o) {
		super.visitSIPUSH(o);
		frame.setResponsibleForStackTop(iflow);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#visitSWAP(org.apache.bcel.generic.SWAP)
	 */
	@Override
	public void visitSWAP(SWAP o) {
		final InstructionFlow iflow1 = frame.getResponsibleForStackTop();
		final InstructionFlow iflow2 = frame.getResponsibleForStackEntry(1);
		super.visitSWAP(o);
		frame.setResponsibleForStackTop(iflow2);
		frame.setResponsibleForStackEntry(iflow1, 1);
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.verifier.structurals.ExecutionVisitor#setConstantPoolGen(org.apache.bcel.generic.ConstantPoolGen)
	 */
	@Override
	public void setConstantPoolGen(ConstantPoolGen cpg) {
		super.setConstantPoolGen(cpg);
		this.cpg = cpg;
	}

	/**
	 * {@inheritDoc}
	 * @throws ClassCastException if f is not a {@link SmartFrame}
	 */
	@Override
	public void setFrame(Frame f) {
		super.setFrame(f);
		this.frame = (SmartFrame) f;
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

	/**
	 * @param iflow the iflow to set
	 */
	public void setIflow(InstructionFlow iflow) {
		this.iflow = iflow;
	}

	/**
	 * @return the iflow
	 */
	public InstructionFlow getIflow() {
		return iflow;
	}

}
