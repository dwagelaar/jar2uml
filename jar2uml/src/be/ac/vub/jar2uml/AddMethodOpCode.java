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

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import junit.framework.Assert;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ATHROW;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.LineNumberGen;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;
import org.apache.bcel.verifier.structurals.ExceptionHandler;
import org.apache.bcel.verifier.structurals.Frame;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Model;

import be.ac.vub.jar2uml.cflow.AccessContextUnavailableException;
import be.ac.vub.jar2uml.cflow.BranchTargetUnavailableException;
import be.ac.vub.jar2uml.cflow.ControlFlow;
import be.ac.vub.jar2uml.cflow.ExecutionContext;
import be.ac.vub.jar2uml.cflow.JarToUMLExecutionVisitor;
import be.ac.vub.jar2uml.cflow.LocalHistoryTable;
import be.ac.vub.jar2uml.cflow.ControlFlow.InstructionFlow;
import be.ac.vub.jar2uml.cflow.LocalHistoryTable.LocalHistorySet;

/**
 * Adds bytecode instruction dependencies for the given method.
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public class AddMethodOpCode extends AddToModel {

	/**
	 * @param lines
	 * @return The sets of {@link LineNumberGen} that have the same source line number
	 */
	public static Set<Set<LineNumberGen>> findSameLineSets(final LineNumberGen[] lines) {
		final Set<Set<LineNumberGen>> inlineSets = new HashSet<Set<LineNumberGen>>();
	
		outer:
		for (LineNumberGen line1 : lines) {
			for (Set<LineNumberGen> inlineSet : inlineSets) {
				if (inlineSet.contains(line1)) {
					continue outer; //line1 is already part of a complete inlineSet
				}
			}
			Set<LineNumberGen> inlineSet = new HashSet<LineNumberGen>();
			inlineSet.add(line1);
			for (LineNumberGen line2 : lines) {
				if (line2 != line1 && line2.getSourceLine() == line1.getSourceLine()) {
					//found a reference to the same source line number
					inlineSet.add(line2);
				}
			}
			if (inlineSet.size() > 1) {
				//we are only interested in sets of duplicated line numbers
				inlineSets.add(inlineSet);
			}
		}
	
		return inlineSets;
	}

	protected final AddInstructionDependenciesVisitor addInstructionDependencies = 
		new AddInstructionDependenciesVisitor(
				typeToClassifier,
				addClassifierProperty,
				addClassifierOperation);
	protected final JarToUMLExecutionVisitor execution = new JarToUMLExecutionVisitor();
	/**
	 * The global history of all successfully executed instructions.
	 */
	protected final Set<InstructionFlow> globalHistory = new HashSet<InstructionFlow>();
	/**
	 * All instructions which have terminated at least one with an {@link AccessContextUnavailableException}.
	 */
	protected final Set<InstructionFlow> noAccessContextAvailable = new HashSet<InstructionFlow>();
	protected int reuseCount = 0;
	protected int copyCount = 0;
	protected int excCopyCount = 0;
	protected int maxQueueSize = 0;

	/**
	 * @param filter
	 * @param monitor
	 * @param model
	 * @param includeFeatures
	 * @param includeInstructionReferences
	 */
	public AddMethodOpCode(Filter filter, IProgressMonitor monitor,
			Model model, boolean includeFeatures,
			boolean includeInstructionReferences) {
		super(filter, monitor, model, includeFeatures,
				includeInstructionReferences);
	}

	/**
	 * Adds fields/methods referenced by the bytecode instructions of method
	 * to the UML model. Used in 2nd pass.
	 * @param instrContext The classifier on which the method is defined.
	 * @param javaClass The {@link JavaClass} representation of instrContext.
	 * @param method The method for which to convert the references.
	 * @throws JarToUMLException 
	 */
	public synchronized void addOpCode(final Classifier instrContext, final JavaClass javaClass, final Method method) throws JarToUMLException {
		if (!isIncludeInstructionReferences() || method.getCode() == null) {
			return;
		}

		addInstructionDependencies.setInstrContext(instrContext);
		addInstructionDependencies.setCp(method.getConstantPool());
		execution.setConstantPoolGen(addInstructionDependencies.getCpg());

		final MethodGen method_gen = new MethodGen(method, javaClass.getClassName(), addInstructionDependencies.getCpg());
		final int instrCount = method_gen.getInstructionList().size();
		final ControlFlow cflow = new ControlFlow(method_gen);

		JarToUML.logger.finer(method_gen.toString());

		//try first with simplified algorithm
		executeSimple(cflow);
		if (!noAccessContextAvailable.isEmpty() || instrCount != globalHistory.size()) {
			//fall back to full algorithm if not all access context could be found or instructions were skipped
			JarToUML.logger.fine(String.format(
					JarToUMLResources.getString("AddMethodOpCode.fallback"), 
					method_gen.toString())); //$NON-NLS-1$
			execute(cflow);
		}

		JarToUML.logger.fine(String.format(
				JarToUMLResources.getString("AddMethodOpCode.instrCount"), 
				reuseCount, copyCount, excCopyCount, maxQueueSize, method_gen.toString())); //$NON-NLS-1$

		if (instrCount != globalHistory.size()) {
			final InstructionHandle[] allInstr = method_gen.getInstructionList().getInstructionHandles();
			final Set<InstructionFlow> notcovered = new HashSet<InstructionFlow>(allInstr.length);
			for (InstructionHandle instr : allInstr) {
				notcovered.add(cflow.getFlowOf(instr));
			}
			notcovered.removeAll(globalHistory);
			JarToUML.logger.warning(String.format(
					JarToUMLResources.getString("AddMethodOpCode.notCovered"),
					javaClass.getClassName(),
					method_gen.toString(),
					notcovered.toString())); //$NON-NLS-1$
			if (!noAccessContextAvailable.isEmpty()) {
				JarToUML.logger.warning(String.format(
						JarToUMLResources.getString("AddMethodOpCode.guaranteedNPE"),
						javaClass.getClassName(),
						method_gen.toString(),
						noAccessContextAvailable.toString())); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Resets object-wide fields.
	 */
	protected void resetGlobals() {
		reuseCount = 0;
		copyCount = 0;
		excCopyCount = 0;
		maxQueueSize = 0;
		globalHistory.clear();
		noAccessContextAvailable.clear();
	}

	/**
	 * Executes all instructions reachable from cflow and records the inferred dependencies.
	 * Guarantees that if there exists a valid access context for an instance
	 * field or method access, it will be found.
	 * @param cflow
	 */
	protected void execute(final ControlFlow cflow) {
		//TODO this algorithm does not scale sufficiently when unreachable code exists
		final LinkedList<ExecutionContext> ecQueue = new LinkedList<ExecutionContext>();
		final int instrCount = cflow.getMethod().getInstructionList().getLength();

		Frame frame = cflow.getStartFrame().getClone();
		InstructionFlow iflow = cflow.getStartInstruction();
		LocalHistoryTable history = new LocalHistoryTable(instrCount);
		ExecutionContext ec = new ExecutionContext(iflow, history, frame);

		resetGlobals();

		while (iflow != null && globalHistory.size() < instrCount) {

			//represents a successful termination of the current execution path
			boolean terminated = true;

			try {
				LocalHistorySet instrHistory = history.get(iflow);
				//execute exception handlers in the context of before the covered instruction
				if (iflow.isExceptionThrower()) {
					for (ExceptionHandler eh : iflow.getExceptionHandlers()) {
						InstructionFlow succ = cflow.getFlowOf(eh.getHandlerStart());
						//create a new history for each alternative successor path
						LocalHistoryTable newHistory = history.getClone();
						excCopyCount++;
						//check if successor has ever been executed from this history by calculating new history
						if (newHistory.get(succ).addAll(instrHistory)) {
							//add execution context to back of queue
							ecQueue.addLast(new ExecutionContext(
									succ, 
									newHistory, 
									prepareExceptionFrame(frame, eh)));
							//if the covered instructions terminate successfully, this exception handler also should, so don't check
						}
					}
				}

				InstructionFlow[] succ;
				try {
					//execute instruction
					succ = executeInstr(frame, iflow);
				} catch (BranchTargetUnavailableException e) {
					succ = e.getRemainingTargets();
					history.setBranchTargetUnavailable();
				}

				//update history
				instrHistory.add(iflow);
				//prepare successor execution context
				for (int i = succ.length-1; i >= 0; i--) {
					//create a new history/frame for each alternative successor path, except first path
					LocalHistoryTable newHistory;
					Frame newFrame;
					if (i == 0) {
						newHistory = history;
						newFrame = frame;
						reuseCount++;
					} else {
						newHistory = history.getClone();
						newFrame = frame.getClone();
						copyCount++;
					}
					//check if successor has ever been executed from this history by calculating new history
					if (newHistory.get(succ[i]).addAll(instrHistory)) {
						//add execution context to front of queue
						ecQueue.addFirst(new ExecutionContext(
								succ[i], 
								newHistory, 
								newFrame));
						terminated = false;
					}
				}
			} catch (AccessContextUnavailableException e) {
				//cut execution path and remember the reason
				noAccessContextAvailable.add(iflow);
				terminated = false;
			}

			if (!ecQueue.isEmpty()) {
				maxQueueSize = Math.max(maxQueueSize, ecQueue.size());
				Assert.assertTrue(maxQueueSize <= copyCount + excCopyCount + 1);
				if (terminated && !history.hasBranchTargetUnavailable()) {
					//merge back history of successfully terminated and fully covered execution paths
					ec = ecQueue.removeFirst(); //first in queue is last branch in current search path
					Assert.assertNotSame(history, ec.getHistory());
					ec.getHistory().merge(history);
				} else {
					ec = pickBestFromQueue(ec, ecQueue);
				}
				iflow = ec.getIflow();
				history = ec.getHistory();
				frame = ec.getFrame();
			} else {
				iflow = null;
			}

			checkCancelled();

		}
	}

	/**
	 * Executes all instructions reachable from cflow and records the inferred dependencies.
	 * Does not guarantee finding a valid access context for an instance
	 * field or method access. It may also fail to cover instructions that are
	 * masked out because a branch target is unavailable from a certain history.
	 * 
	 * Consider this algorithm has failed when not all instructions in cflow are covered.
	 * @param cflow
	 */
	protected void executeSimple(final ControlFlow cflow) {
		final LinkedList<ExecutionContext> ecQueue = new LinkedList<ExecutionContext>();
		final int instrCount = cflow.getMethod().getInstructionList().getLength();

		Frame frame = cflow.getStartFrame().getClone();
		InstructionFlow iflow = cflow.getStartInstruction();
		LocalHistoryTable history = new LocalHistoryTable(instrCount);
		ExecutionContext ec = new ExecutionContext(iflow, history, frame);

		resetGlobals();

		while (iflow != null && globalHistory.size() < instrCount) {

			try {
				LocalHistorySet instrHistory = history.get(iflow);
				//execute exception handlers in the context of before the covered instruction
				if (iflow.isExceptionThrower()) {
					for (ExceptionHandler eh : iflow.getExceptionHandlers()) {
						InstructionFlow succ = cflow.getFlowOf(eh.getHandlerStart());
						//check if successor has ever been executed from this history by calculating new history
						if (history.get(succ).addAll(instrHistory)) {
							excCopyCount++;
							//add execution context to back of queue
							ecQueue.addLast(new ExecutionContext(
									succ, 
									history, 
									prepareExceptionFrame(frame, eh)));
							//if the covered instructions terminate successfully, this exception handler also should, so don't check
						}
					}
				}

				InstructionFlow[] succ;
				try {
					//execute instruction
					succ = executeInstr(frame, iflow);
				} catch (BranchTargetUnavailableException e) {
					succ = e.getRemainingTargets();
					history.setBranchTargetUnavailable();
					//we can't do anything with this, using this algorithm
				}

				//update history
				instrHistory.add(iflow);
				//prepare successor execution context
				for (int i = succ.length-1; i >= 0; i--) {
					//create a new history/frame for each alternative successor path, except first path
					Frame newFrame;
					if (i == 0) {
						newFrame = frame;
						reuseCount++;
					} else {
						newFrame = frame.getClone();
						copyCount++;
					}
					//check if successor has ever been executed from this history by calculating new history
					if (history.get(succ[i]).addAll(instrHistory)) {
						//add execution context to front of queue
						ecQueue.addFirst(new ExecutionContext(
								succ[i], 
								history, 
								newFrame));
					}
				}
			} catch (AccessContextUnavailableException e) {
				//cut execution path and remember the reason
				noAccessContextAvailable.add(iflow);
			}

			if (!ecQueue.isEmpty()) {
				maxQueueSize = Math.max(maxQueueSize, ecQueue.size());
				Assert.assertTrue(maxQueueSize <= copyCount + excCopyCount + 1);
				ec = ecQueue.removeFirst();
				iflow = ec.getIflow();
				history = ec.getHistory();
				frame = ec.getFrame();
			} else {
				iflow = null;
			}

			checkCancelled();

		}
	}

	/**
	 * Executes iflow and records the inferred dependencies.
	 * @param frame the execution frame
	 * @param iflow the instruction for which to execute the exception handlers
	 * @return the possible successor instructions of iflow
	 * @throws AccessContextUnavailableException if iflow retrieves a <code>null</code> access context
	 * @throws BranchTargetUnavailableException if a branch target was cut off due to the given execution frame
	 */
	protected InstructionFlow[] executeInstr(final Frame frame, final InstructionFlow iflow) {
		//TODO only one valid access context is covered, which may lead to optimistic conclusions w.r.t. protected/public
		//check if instr has already been successfully visited
		if (!globalHistory.contains(iflow)) {
			//add dependencies
			addInstructionDependencies.setFrame(frame);
			iflow.accept(addInstructionDependencies); // this will bail out with an AccessContextUnavailableException on null pointer access
			globalHistory.add(iflow);
			noAccessContextAvailable.remove(iflow);
		}
		try {
			//Expect BranchTargetUnavailableException
			return iflow.getSuccessors(frame);
		} finally {
			//update stack
			execution.setFrame(frame);
			iflow.accept(execution);
		}
	}

	/**
	 * @param frame
	 * @param eh
	 * @return The prepared frame after throwing the exception type for eh
	 */
	protected Frame prepareExceptionFrame(final Frame frame, final ExceptionHandler eh) {
		//Use frame copy for each exception handler
		final Frame frameClone = frame.getClone();
		//clear the stack, as there may not be enough room for the exception type
		frameClone.getStack().clear();
		//simulate throwing the exception, resulting in a correct stack
		final org.apache.bcel.generic.ObjectType exceptionType = eh.getExceptionType();
		if (exceptionType != null) {
			frameClone.getStack().push(exceptionType);
		} else {
			frameClone.getStack().push(Type.THROWABLE);
		}
		execution.setFrame(frameClone);
		new ATHROW().accept(execution);
		//return the prepared frame
		return frameClone;
	}

	/**
	 * @param current the current execution contex
	 * @param ecQueue the queue of pending execution contexts
	 * @return the best execution context candidate to process next
	 */
	protected ExecutionContext pickBestFromQueue(final ExecutionContext current, final LinkedList<ExecutionContext> ecQueue) {
		Assert.assertFalse(ecQueue.isEmpty());
		ExecutionContext ec = null;

		//cherry-pick next execution context from queue: one that preferably has not been visited before
		for (ExecutionContext next : ecQueue) {
			InstructionFlow iflow = next.getIflow();
			if (!globalHistory.contains(iflow)) {
				//instruction has never been visited before - 1st choice
				ec = next;
				break; //optimal solution - stop looking
			} else if (ec == null && !next.getHistory().get(iflow).contains(iflow)) {
				//instruction has never been visited before in this search path - 2nd choice
				ec = next;
			}
		}

		if (ec == null) {
			//only previously visited instructions left
			ec = ecQueue.removeFirst();
		} else {
			ecQueue.remove(ec);
		}

		Assert.assertNotNull(ec);
		return ec;
	}

	//TODO detect inlined code
//	protected InstructionFlow[] findInlinedDuplicates(ControlFlow cflow, InstructionFlow iflow) {
//		final LineNumberGen[] lines = cflow.getMethod().getLineNumbers();
//		final Set<Set<LineNumberGen>> inlineSets = findSameLineSets(lines);
//
//		for (Set<LineNumberGen> inlineSet : inlineSets) {
//			
//		}
//
//		iflow.getInstruction().getPosition();
//	}

	/**
	 * @return the globalHistory
	 */
	public Set<InstructionFlow> getGlobalHistory() {
		return Collections.unmodifiableSet(globalHistory);
	}

	/**
	 * @return the noAccessContextAvailable
	 */
	public Set<InstructionFlow> getNoAccessContextAvailable() {
		return Collections.unmodifiableSet(noAccessContextAvailable);
	}

	/**
	 * @return the reuseCount
	 */
	public int getReuseCount() {
		return reuseCount;
	}

	/**
	 * @return the copyCount
	 */
	public int getCopyCount() {
		return copyCount;
	}

	/**
	 * @return the excCopyCount
	 */
	public int getExcCopyCount() {
		return excCopyCount;
	}

	/**
	 * @return the maxQueueSize
	 */
	public int getMaxQueueSize() {
		return maxQueueSize;
	}

}
