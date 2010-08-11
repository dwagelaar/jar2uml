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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.LineNumber;
import org.apache.bcel.classfile.LineNumberTable;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ATHROW;
import org.apache.bcel.generic.EmptyVisitor;
import org.apache.bcel.generic.ExceptionThrower;
import org.apache.bcel.generic.GotoInstruction;
import org.apache.bcel.generic.IFNONNULL;
import org.apache.bcel.generic.IFNULL;
import org.apache.bcel.generic.IfInstruction;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.JsrInstruction;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.RET;
import org.apache.bcel.generic.ReturnInstruction;
import org.apache.bcel.generic.ReturnaddressType;
import org.apache.bcel.generic.Select;
import org.apache.bcel.generic.Type;
import org.apache.bcel.generic.Visitor;
import org.apache.bcel.verifier.structurals.ExceptionHandler;
import org.apache.bcel.verifier.structurals.ExceptionHandlers;
import org.apache.bcel.verifier.structurals.Frame;
import org.apache.bcel.verifier.structurals.LocalVariables;
import org.apache.bcel.verifier.structurals.UninitializedObjectType;

import be.ac.vub.jar2uml.JarToUMLResources;
import be.ac.vub.jar2uml.cflow.LocalHistoryTable.OrderedItem;

/**
 * A control flow simulator for BCEL {@link MethodGen} code. 
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public class ControlFlow {

	private static final InstructionFlow[] EMPTY = new InstructionFlow[0];

	/**
	 * @param instr
	 * @return The index of instr in the sequence of instructions
	 */
	protected static int getIndexOf(InstructionHandle instr) {
		int counter = 0;
		while (instr.getPrev() != null) {
			instr = instr.getPrev();
			counter++;
		}
		return counter;
	}

	/**
	 * @param iflows
	 * @return the line numbers for all iflows
	 */
	public static Set<Integer> getLineNumbers(Collection<InstructionFlow> iflows) {
		final Set<Integer> lines = new TreeSet<Integer>();
		for (InstructionFlow iflow : iflows) {
			lines.add(iflow.getLineNumber());
		}
		return lines;
	}

	/**
	 * Represents the control flow information of an {@link InstructionHandle}. 
	 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
	 */
	public class InstructionFlow implements OrderedItem, Comparable<InstructionFlow> {

		/**
		 * Simplified instruction successor determination algorithm,
		 * which does not take the execution frame into account.
		 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
		 */
		protected class SimpleInstructionSuccessorVisitor extends EmptyVisitor {
			
			private InstructionFlow[] successors = getInstruction().getNext() == null ? EMPTY :
				new InstructionFlow[] {
					getFlowOf(getInstruction().getNext())
			};

			/**
			 * @param successors the successors to set
			 */
			protected void setSuccessors(InstructionFlow[] successors) {
				this.successors = successors;
			}

			/**
			 * @return the successors
			 */
			public InstructionFlow[] getSuccessors() {
				return successors;
			}

			/**
			 * Terminates method abnormally: no successors.
			 * @param obj
			 */
			@Override
			public void visitATHROW(ATHROW obj) {
				setSuccessors(EMPTY);
			}

			/**
			 * Goto target is set as successor.
			 * @param obj
			 */
			@Override
			public void visitGotoInstruction(GotoInstruction obj) {
				setSuccessors(new InstructionFlow[] { getFlowOf(obj.getTarget()) });
			}

			/**
			 * Two alternative branch targets are set as successors.
			 * @param obj
			 */
			@Override
			public void visitIfInstruction(IfInstruction obj) {
				final InstructionFlow[] ret = new InstructionFlow[2];
				ret[0] = getFlowOf(getInstruction().getNext());
				ret[1] = getFlowOf(obj.getTarget());
				setSuccessors(ret);
			}

			/**
			 * Jump target and next instruction are set as successor.
			 * This approximates returning from RET.
			 * @param obj
			 */
			@Override
			public void visitJsrInstruction(JsrInstruction obj) {
				final InstructionFlow[] ret = new InstructionFlow[2];
				ret[0] = getFlowOf(getInstruction().getNext());
				ret[1] = getFlowOf(obj.getTarget());
				setSuccessors(ret);
			}

			/**
			 * No successors: return target is approximated by letting JSR continue to next instruction
			 * @param obj
			 */
			@Override
			public void visitRET(RET obj) {
				//approximate by letting JSR continue to next instruction
				setSuccessors(EMPTY);
			}

			/**
			 * Terminates method normally: no successors.
			 * @param obj
			 */
			@Override
			public void visitReturnInstruction(ReturnInstruction obj) {
				setSuccessors(EMPTY);
			}

			/**
			 * Sets the (unique) collection of switch targets as successors.
			 * @param obj
			 */
			@Override
			public void visitSelect(Select obj) {
				final LinkedHashSet<InstructionFlow> uniqueTargets = new LinkedHashSet<InstructionFlow>();
				//default target
				uniqueTargets.add(getFlowOf(obj.getTarget()));
				//switch targets
				for (InstructionHandle target : obj.getTargets()) {
					uniqueTargets.add(getFlowOf(target));
				}
				final InstructionFlow[] ret = new InstructionFlow[uniqueTargets.size()];
				uniqueTargets.toArray(ret);
				setSuccessors(ret);
			}

		}

		/**
		 * Extended instruction successor determination algorithm,
		 * which does take the execution frame into account.
		 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
		 */
		protected class InstructionSuccessorVisitor extends SimpleInstructionSuccessorVisitor {
			
			private SmartFrame frame;

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

			/*
			 * We may know beforehand which way a IFNULL or IFNONNULL jump goes,
			 * but the Java compiler does not check for this. As a result, there
			 * may be dead code WITHOUT any warnings generated by the compiler!
			 * 
			 * It's ok to be smarter than the compiler here, as dependencies of
			 * dead code can be ignored.
			 * 
			 * If a target of a jump is cut off, this may confuse the search
			 * algorithm, as it doesn't know about the cut off branch! The search
			 * algorithm may decide not to try this instruction anymore, as there
			 * were "no problems" with it! Hence, an exception must be thrown to
			 * indicate the situation!
			 */

			/**
			 * @see #visitIfInstruction(IfInstruction)
			 * @throws BranchTargetUnavailableException if a branch target was cut off due to the given execution frame
			 */
			@Override
			public void visitIFNONNULL(IFNONNULL obj) {
				if (getFrame().getStack().peek().equals(Type.NULL)) {
					//We already know which way the jump goes
					setSuccessors(new InstructionFlow[] { getFlowOf(getInstruction().getNext()) });
					throw new BranchTargetUnavailableException(
							new InstructionFlow[] { getFlowOf(obj.getTarget()) }, 
							getSuccessors(),
							getFrame().getResponsibleForStackTop());
				} //else jump may still go either way
			}

			/**
			 * @see #visitIfInstruction(IfInstruction)
			 * @throws BranchTargetUnavailableException if a branch target was cut off due to the given execution frame
			 */
			@Override
			public void visitIFNULL(IFNULL obj) {
				if (getFrame().getStack().peek().equals(Type.NULL)) {
					//We already know which way the jump goes
					setSuccessors(new InstructionFlow[] { getFlowOf(obj.getTarget()) });
					throw new BranchTargetUnavailableException(
							new InstructionFlow[] { getFlowOf(getInstruction().getNext()) }, 
							getSuccessors(),
							getFrame().getResponsibleForStackTop());
				} //else jump may still go either way
			}

			/**
			 * Jump target is set as successor.
			 * @param obj
			 */
			@Override
			public void visitJsrInstruction(JsrInstruction obj) {
				setSuccessors(new InstructionFlow[] { getFlowOf(obj.getTarget()) });
			}

			/**
			 * Return target is set as successor.
			 * @param obj
			 * @throws ClassCastException if the given execution frame does not contain a valid return address
			 */
			@Override
			public void visitRET(RET obj) {
				final ReturnaddressType address = (ReturnaddressType) getFrame().getLocals().get(obj.getIndex());
				setSuccessors(new InstructionFlow[]{ getFlowOf(address.getTarget()) });
			}

		}

		private final InstructionHandle instr;
		private final int index;
		private final Set<InstructionFlow> predecessors = new LinkedHashSet<InstructionFlow>();
		private final Set<InstructionFlow> successors = new LinkedHashSet<InstructionFlow>();
		private final Set<InstructionFlow> sameLineSet = new HashSet<InstructionFlow>();

		protected InstructionSuccessorVisitor successorVisitor;

		/**
		 * Creates a new {@link InstructionFlow}.
		 * @param instr
		 */
		protected InstructionFlow(InstructionHandle instr, int index) {
			this.instr = instr;
			this.index = index;
			flows[index] = this;
			flowOf.put(instr, this);
			addSameLineEntry(this);
		}

		/**
		 * Creates a new {@link InstructionFlow}.
		 * @param instr
		 */
		protected InstructionFlow(InstructionHandle instr) {
			this(instr, getIndexOf(instr));
		}

		/**
		 * Initialises potential successors/predecessors
		 */
		protected void initSuccessors() {
			final SimpleInstructionSuccessorVisitor successors = new SimpleInstructionSuccessorVisitor();
			accept(successors);
			addAllSuccessors(successors.getSuccessors());
		}

		/**
		 * @return The exception handlers that protect this instruction
		 * @see ExceptionHandlers#getExceptionHandlers(InstructionHandle)
		 */
		public final ExceptionHandler[] getExceptionHandlers() {
			return getExceptionHandlersObject().getExceptionHandlers(getInstruction());
		}

		/**
		 * @return The represented instruction
		 */
		public final InstructionHandle getInstruction() {
			return instr;
		}

		/**
		 * @param frame the execution frame BEFORE execution if this instruction
		 * @return The possible successor instructions for this instruction
		 * @throws BranchTargetUnavailableException if a branch target was cut off due to the given execution frame
		 */
		public final InstructionFlow[] getSuccessors(final SmartFrame frame) {
			if (successorVisitor == null) {
				successorVisitor = new InstructionSuccessorVisitor();
			}
			successorVisitor.setFrame(frame);
			accept(successorVisitor);
			return successorVisitor.getSuccessors();
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			final StringBuffer sb = new StringBuffer();
			sb.append("["); //$NON-NLS-1$
			sb.append(getIndex());
			sb.append("] "); //$NON-NLS-1$
			sb.append(getInstruction().toString().trim());
			sb.append(" (p="); //$NON-NLS-1$
			sb.append(getPredecessors().size());
			sb.append(",s="); //$NON-NLS-1$
			sb.append(getSuccessors().size());
			sb.append(",l#="); //$NON-NLS-1$
			sb.append(getLineNumber());
			sb.append(")"); //$NON-NLS-1$
			return sb.toString();
		}
		
	    /** 
	     * Convenience method, simply calls accept() on the contained instruction.
	     * @param v Visitor object
	     */
		public final void accept(Visitor v) {
			getInstruction().accept(v);
		}

		/**
		 * @return <code>true</code> iff the contained instruction is an {@link ExceptionThrower}.
		 */
		public final boolean isExceptionThrower() {
			return getInstruction().getInstruction() instanceof ExceptionThrower;
		}

		/**
		 * @return <code>true</code> iff the code terminates after this instruction
		 */
		public final boolean isFinalInstruction() {
			final Instruction instr = getInstruction().getInstruction();
			return instr instanceof ReturnInstruction || instr instanceof RET || instr instanceof ATHROW;
		}

		/**
		 * Convenience method that calls getPosition() on the contained instruction.
		 * @return the byte offset position of the contained instruction
		 */
		public final int getPosition() {
			return getInstruction().getPosition();
		}

		/**
		 * @return the list position of the contained instruction
		 */
		public final int getIndex() {
			return index;
		}

		/**
		 * @return the predecessors
		 */
		public Set<InstructionFlow> getPredecessors() {
			return Collections.unmodifiableSet(predecessors);
		}

		/**
		 * Adds iflow as a predecessor of this.
		 * @param iflow
		 */
		protected void addPredecessor(InstructionFlow iflow) {
			if (predecessors.add(iflow)) {
				iflow.addSuccessor(this);
			}
		}

		/**
		 * Adds all iflows as predecessors of this.
		 * @param iflows
		 */
		protected void addAllPredecessors(InstructionFlow[] iflows) {
			for (InstructionFlow iflow : iflows) {
				addPredecessor(iflow);
			}
		}

		/**
		 * @return the successors
		 */
		public Set<InstructionFlow> getSuccessors() {
			return Collections.unmodifiableSet(successors);
		}

		/**
		 * Adds iflow as a successor of this.
		 * @param iflow
		 */
		protected void addSuccessor(InstructionFlow iflow) {
			if (successors.add(iflow)) {
				iflow.addPredecessor(this);
			}
		}

		/**
		 * Adds all iflows as successors of this.
		 * @param iflows
		 */
		protected void addAllSuccessors(InstructionFlow[] iflows) {
			for (InstructionFlow iflow : iflows) {
				addSuccessor(iflow);
			}
		}

		/**
		 * @return the linear run of instructions that is guaranteed to be executed prior to this instruction
		 */
		public List<InstructionFlow> getPredecessorRun() {
			final ArrayList<InstructionFlow> run = new ArrayList<InstructionFlow>();
			InstructionFlow iflow = this;
			while (iflow.getPredecessors().size() == 1) {
				iflow = iflow.getPredecessors().iterator().next();
				run.add(iflow);
			}
			return run;
		}

		/**
		 * Finds the non-linear set of instructions for which this instruction is guaranteed to be executed prior to it, including this instruction
		 * @param succset the set of instructions found so far
		 */
		public void findSuccessorSet(final Set<InstructionFlow> succset) {
			succset.add(this);
			Succ:
			for (InstructionFlow succ : getSuccessors()) {
				for (InstructionFlow pre : succ.getPredecessors()) {
					if (!succset.contains(pre)) {
						continue Succ;
					}
				}
				if (!succset.contains(succ)) {
					succ.findSuccessorSet(succset);
				}
			}
		}

		/**
		 * @return the set of instructions that map back to the same source code and correspond to this instruction, including this instruction
		 */
		public Set<InstructionFlow> getSameLineSet() {
			return Collections.unmodifiableSet(sameLineSet);
		}

		/**
		 * Adds iflow to the same line set.
		 * @param iflow
		 * @return <code>true</code> iff the set changed as a result
		 */
		protected boolean addSameLineEntry(InstructionFlow iflow) {
			return sameLineSet.add(iflow);
		}

		/**
		 * @return the source code line number of this instruction, if available, -1 otherwise
		 */
		public int getLineNumber() {
			if (getLines() != null) {
				return getLines().getSourceLine(getInstruction().getPosition());
			}
			return -1;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		public int compareTo(InstructionFlow o) {
			if (!getFlows().contains(o)) {
				throw new IllegalArgumentException(String.format(
						JarToUMLResources.getString("ControlFlow.illegalInstr"),
						o));
			}
			return Integer.valueOf(getIndex()).compareTo(Integer.valueOf(o.getIndex()));
		}

	}

	private final MethodGen method;
	private final LineNumberTable lines;
	private final SmartFrame startFrame;
	private final ExceptionHandlers exceptionHandlers;
	private final int flowCount;

	protected final Map<InstructionHandle, InstructionFlow> flowOf = new HashMap<InstructionHandle, InstructionFlow>();
	protected final InstructionFlow[] flows;
	protected final Set<InstructionFlow> deadCode = new LinkedHashSet<InstructionFlow>();

	/**
	 * Creates a new {@link ControlFlow}.
	 * @param method
	 */
	public ControlFlow(MethodGen method) {
		super();
		this.method = method;
		final Method m = method.getMethod();
		this.lines = m.getLineNumberTable();
		final Code code = m.getCode();
		this.startFrame = new SmartFrame(code.getMaxLocals(), code.getMaxStack());
		initLocalVariableTypes(startFrame);
		exceptionHandlers = new ExceptionHandlers(method);
		this.flowCount = method.getInstructionList().getLength();
		flows = new InstructionFlow[flowCount];
		createInstructionFlows();
		addAllSuccessors();
		findDeadCode();
		if (lines != null) {
			findSameLineInstr();
		}
	}
	
	/**
	 * Initialises the local variable types in frame according to the method
	 * context (java class) and argument types.
	 * @param frame
	 */
	private void initLocalVariableTypes(final Frame frame) {
		final MethodGen method = getMethod();
		final LocalVariables localVars = frame.getLocals();
		int i = 0;
		if (!method.isStatic()) {
			final ObjectType objectType = new ObjectType(method.getClassName());
			if (Constants.CONSTRUCTOR_NAME.equals(method.getName())) {
				localVars.set(i, new UninitializedObjectType(objectType));
			} else {
				localVars.set(i, objectType);
			}
			i++;
		}
		for (Type argType : method.getArgumentTypes()) {
			if (argType == Type.BYTE || argType == Type.SHORT || argType == Type.BOOLEAN || argType == Type.CHAR){
				argType = Type.INT;
			}
			localVars.set(i, argType);
			i += argType.getSize();
		}
	}

	/**
	 * Creates the elements of the control flow graph.
	 */
	private void createInstructionFlows() {
		final InstructionHandle[] instr = getMethod().getInstructionList().getInstructionHandles();
		for (int i = 0; i < instr.length; i++) {
			new InstructionFlow(instr[i], i);
		}
	}

	/**
	 * Adds the successor information to the control flow graph.
	 */
	private void addAllSuccessors() {
		final ExceptionHandlers ehs = getExceptionHandlersObject();
		for (InstructionFlow iflow : flows) {
			iflow.initSuccessors();
			for (ExceptionHandler eh : ehs.getExceptionHandlers(iflow.getInstruction())) {
				iflow.addSuccessor(getFlowOf(eh.getHandlerStart()));
			}
		}
	}

	/**
	 * Finds unreachable code in the control flow graph and adds it to {@link ControlFlow#deadCode}.
	 */
	private void findDeadCode() {
		for (int i = 1; i < flows.length; i++) {
			boolean noLivePredecessors = true;
			Set<InstructionFlow> pres = flows[i].getPredecessors();
			for (InstructionFlow pre : pres) {
				if (!deadCode.contains(pre)) {
					noLivePredecessors = false;
					break;
				}
			}
			if (noLivePredecessors) {
				deadCode.add(flows[i]);
			}
		}
	}

	/**
	 * Finds instructions with the same line number in the line number table and adds it to {@link ControlFlow#sameLineInstr}.
	 */
	private void findSameLineInstr() {
		final LineNumber[] lines = getLines().getLineNumberTable();

		for (int i = 0; i < lines.length; i++) {
			List<InstructionFlow> iflows = findFlowsFor(i);

			nextj:
			for (int j = 0; j < lines.length; j++) {
				if (lines[j] != lines[i] && lines[j].getLineNumber() == lines[i].getLineNumber()) {
					List<InstructionFlow> jflows = findFlowsFor(j);
					if (iflows.size() == jflows.size()) {
						for (int k = 0; k < iflows.size(); k++) {
							if (iflows.get(k).getInstruction().getInstruction().getOpcode() != jflows.get(k).getInstruction().getInstruction().getOpcode()) {
								continue nextj;
							}
						}
						for (int k = 0; k < iflows.size(); k++) {
							iflows.get(k).addSameLineEntry(jflows.get(k));
						}
					}
				}
			}
	
		}
	}

	/**
	 * @return the flowCount
	 */
	public int getFlowCount() {
		return flowCount;
	}

	/**
	 * @param instr
	 * @return the control flow information for instr
	 * @throws IllegalArgumentException if instr is not part of this control flow
	 */
	public InstructionFlow getFlowOf(InstructionHandle instr) {
		final InstructionFlow flow = flowOf.get(instr);
		if (flow == null) {
			throw new IllegalArgumentException(String.format(
					JarToUMLResources.getString("ControlFlow.illegalInstr"),
					instr));
		}
		return flow;
	}

	/**
	 * @param instr
	 * @return the control flow information for instr
	 * @see #getFlowOf(InstructionHandle)
	 */
	public InstructionFlow[] getFlowOf(InstructionHandle[] instr) {
		final InstructionFlow[] flowOf = new InstructionFlow[instr.length];
		for (int i = 0; i < instr.length; i++) {
			flowOf[i] = getFlowOf(instr[i]);
		}
		return flowOf;
	}

	/**
	 * @param index
	 * @return the instruction flow with the given index, or <code>null</code>
     * @throws IndexOutOfBoundsException {@inheritDoc}
	 */
	public InstructionFlow getFlow(int index) {
		return flows[index];
	}

	/**
	 * @return all instruction flows in this control flow
	 */
	public List<InstructionFlow> getFlows() {
		return Collections.unmodifiableList(Arrays.asList(flows));
	}

	/**
	 * @return the method
	 */
	public MethodGen getMethod() {
		return method;
	}

	/**
	 * @return the lines
	 */
	public LineNumberTable getLines() {
		return lines;
	}

	/**
	 * @param line
	 * @return the index of line in the line number table, or -1 if line is not contained in the line number table
	 */
	public int getIndexOfLine(final LineNumber line) {
		final LineNumber[] lines = getLines().getLineNumberTable();
		for (int i = 0; i < lines.length; i++) {
			if (lines[i] == line) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * @return the startFrame
	 */
	public SmartFrame getStartFrame() {
		return startFrame;
	}

	/**
	 * @return the starting instruction
	 */
	public InstructionFlow getStartInstruction() {
		return getFlowOf(getMethod().getInstructionList().getStart());
	}

	/**
	 * @return the exceptionHandlers
	 */
	public ExceptionHandlers getExceptionHandlersObject() {
		return exceptionHandlers;
	}

	/**
	 * Returns instructions that have no potential predecessor.
	 * @return the dead code
	 */
	public Set<InstructionFlow> getDeadCode() {
		return Collections.unmodifiableSet(deadCode);
	}

	/**
	 * @param lineIndex the index of the line number table entry
	 * @return the instruction flows that map to the line number table entry
	 * @see InstructionList#findHandle(int)
	 * @throws IllegalArgumentException if the given line index is not contained in the line number table
	 */
	public List<InstructionFlow> findFlowsFor(int lineIndex) {
		final LineNumber[] lines = getLines().getLineNumberTable();
		if (lineIndex > lines.length-1) {
			throw new IllegalArgumentException();
		}

		final InstructionList il = getMethod().getInstructionList();
		final int beforePos = lineIndex < lines.length-1 ? lines[lineIndex+1].getStartPC() : il.getByteCode().length;
		final List<InstructionFlow> run = new ArrayList<InstructionFlow>();

		InstructionHandle handle = il.findHandle(lines[lineIndex].getStartPC());
		while (handle != null && handle.getPosition() < beforePos) {
			run.add(getFlowOf(handle));
			handle = handle.getNext();
		}
		return run;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + flows.length + "]{ " + getMethod().toString() + " }"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

}
