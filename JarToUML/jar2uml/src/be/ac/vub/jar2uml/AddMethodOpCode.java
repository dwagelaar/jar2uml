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
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ATHROW;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;
import org.apache.bcel.verifier.structurals.ExceptionHandler;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Model;

import be.ac.vub.jar2uml.cflow.AccessContextUnavailableException;
import be.ac.vub.jar2uml.cflow.BranchTargetUnavailableException;
import be.ac.vub.jar2uml.cflow.ControlFlow;
import be.ac.vub.jar2uml.cflow.ControlFlowException;
import be.ac.vub.jar2uml.cflow.ExecutionContext;
import be.ac.vub.jar2uml.cflow.LocalHistoryTable;
import be.ac.vub.jar2uml.cflow.SmartExecutionVisitor;
import be.ac.vub.jar2uml.cflow.SmartFrame;
import be.ac.vub.jar2uml.cflow.Trace;
import be.ac.vub.jar2uml.cflow.ControlFlow.InstructionFlow;
import be.ac.vub.jar2uml.cflow.LocalHistoryTable.LocalHistorySet;

/**
 * Adds bytecode instruction dependencies for the given method.
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public class AddMethodOpCode extends AddToModel {

	private static final int CUT_OFF = 0x8000;

	protected final AddInstructionDependenciesVisitor addInstructionDependencies = 
		new AddInstructionDependenciesVisitor(
				typeToClassifier,
				addClassifierProperty,
				addClassifierOperation);
	protected final SmartExecutionVisitor execution = new SmartExecutionVisitor();
	/**
	 * The global history of all successfully executed instructions.
	 */
	protected final Set<InstructionFlow> globalHistory = new HashSet<InstructionFlow>();
	/**
	 * All instructions which have terminated at least one with an {@link AccessContextUnavailableException}.
	 */
	protected final Set<InstructionFlow> noAccessContextAvailable = new HashSet<InstructionFlow>();
	/**
	 * All instructions which are guaranteed by the stack simulation algorithm to be unreachable.
	 */
	protected final Set<InstructionFlow> deadCode = new HashSet<InstructionFlow>();
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
		final ControlFlow cflow = new ControlFlow(method_gen);
		final int instrCount = cflow.getFlowCount();
		final int liveInstrCount = instrCount - cflow.getDeadCode().size();

		JarToUML.logger.finer(method_gen.toString());

		//try first with simplified algorithm
		executeSimple(cflow);
		JarToUML.logger.fine(String.format(
				JarToUMLResources.getString("AddMethodOpCode.instrCount"), 
				reuseCount, copyCount, excCopyCount, maxQueueSize, method_gen)); //$NON-NLS-1$

		if (liveInstrCount > globalHistory.size() + deadCode.size()) {
			//fall back to full algorithm if instructions were skipped without being proved unreachable
			JarToUML.logger.fine(String.format(
					JarToUMLResources.getString("AddMethodOpCode.fallback"), 
					method_gen.toString())); //$NON-NLS-1$
			execute(cflow);
			JarToUML.logger.fine(String.format(
					JarToUMLResources.getString("AddMethodOpCode.instrCount"), 
					reuseCount, copyCount, excCopyCount, maxQueueSize, method_gen)); //$NON-NLS-1$
		}

		assert noAccessContextAvailable.isEmpty() || instrCount > globalHistory.size();

		if (instrCount > globalHistory.size()) {
			//no all instructions were covered -> report why
			if (!noAccessContextAvailable.isEmpty()) {
				final SortedSet<InstructionFlow> naca = new TreeSet<InstructionFlow>(noAccessContextAvailable);
				JarToUML.logger.warning(String.format(
						JarToUMLResources.getString("AddMethodOpCode.guaranteedNPE"),
						javaClass.getClassName(),
						method_gen,
						naca,
						ControlFlow.getLineNumbers(naca))); //$NON-NLS-1$
			}
			final SortedSet<InstructionFlow> allDeadCode = new TreeSet<InstructionFlow>(cflow.getDeadCode());
			allDeadCode.addAll(deadCode);
			if (!allDeadCode.isEmpty()) {
				JarToUML.logger.warning(String.format(
						JarToUMLResources.getString("AddMethodOpCode.guaranteedDead"),
						javaClass.getClassName(),
						method_gen,
						allDeadCode,
						ControlFlow.getLineNumbers(allDeadCode))); //$NON-NLS-1$
			}
			final InstructionHandle[] allInstr = method_gen.getInstructionList().getInstructionHandles();
			final Set<InstructionFlow> notcovered = new HashSet<InstructionFlow>(allInstr.length);
			for (InstructionHandle instr : allInstr) {
				notcovered.add(cflow.getFlowOf(instr));
			}
			notcovered.removeAll(globalHistory);
			notcovered.removeAll(allDeadCode);
			notcovered.removeAll(noAccessContextAvailable);
			if (!notcovered.isEmpty()) {
				final SortedSet<InstructionFlow> nc = new TreeSet<InstructionFlow>(notcovered);
				JarToUML.logger.warning(String.format(
						JarToUMLResources.getString("AddMethodOpCode.notCovered"),
						javaClass.getClassName(),
						method_gen,
						nc,
						ControlFlow.getLineNumbers(nc))); //$NON-NLS-1$
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
		deadCode.clear();
	}

	/**
	 * Executes all instructions reachable from cflow and records the inferred dependencies.
	 * Guarantees that if there exists a valid access context for an instance
	 * field or method access, it will be found.
	 * @param cflow
	 */
	protected void execute(final ControlFlow cflow) {
		final LinkedList<ExecutionContext> ecQueue = new LinkedList<ExecutionContext>();
		final int instrCount = cflow.getFlowCount();
		final int liveInstrCount = instrCount - cflow.getDeadCode().size();

		SmartFrame frame = (SmartFrame) cflow.getStartFrame().clone();
		InstructionFlow iflow = cflow.getStartInstruction();
		LocalHistoryTable history = new LocalHistoryTable(instrCount);
		Trace trace = new Trace();
		ExecutionContext ec = new ExecutionContext(iflow, history, frame, trace);

		resetGlobals();

		while (iflow != null //done searching
				&& globalHistory.size() + deadCode.size() < liveInstrCount) { //all live instructions covered 

			if (copyCount + excCopyCount > CUT_OFF) {
				JarToUML.logger.fine(String.format(
						JarToUMLResources.getString("AddMethodOpCode.cutoff"),
						cflow)); //$NON-NLS-1$
				break;
			}

			//represents a successful termination of the current execution path
			boolean terminated = true;

			try {
				LocalHistorySet instrHistory = history.get(iflow);
				//execute exception handlers in the context of before the covered instruction
				if (iflow.isExceptionThrower()) {
					for (ExceptionHandler eh : iflow.getExceptionHandlers()) {
						InstructionFlow succ = cflow.getFlowOf(eh.getHandlerStart());
						//create a new history for each alternative successor path
						LocalHistoryTable newHistory = (LocalHistoryTable) history.clone();
						excCopyCount++;
						//check if successor has ever been executed from this history by calculating new history
						if (newHistory.get(succ).addAll(instrHistory)) {
							//add execution context to back of queue
							ecQueue.addLast(new ExecutionContext(
									succ, 
									newHistory, 
									prepareExceptionFrame(frame, eh),
									trace));
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
					if (!checkForDeadCode(iflow, e)) {
						history.setUnmergeable();
					}
				}

				//update history
				instrHistory.add(iflow);
				trace = trace.addEntry(iflow, succ.length);
				//prepare successor execution context
				for (int i = succ.length-1; i >= 0; i--) {
					//create a new trace/history/frame for each alternative successor path, except first path
					LocalHistoryTable newHistory;
					SmartFrame newFrame;
					if (i == 0) {
						newHistory = history;
						newFrame = frame;
						reuseCount++;
					} else {
						newHistory = (LocalHistoryTable) history.clone();
						newFrame = (SmartFrame) frame.clone();
						copyCount++;
					}
					//check if successor has ever been executed from this history by calculating new history
					if (newHistory.get(succ[i]).addAll(instrHistory)) {
						//add execution context to front of queue
						ecQueue.addFirst(new ExecutionContext(
								succ[i], 
								newHistory, 
								newFrame,
								trace));
						terminated = false;
					}
				}
			} catch (AccessContextUnavailableException e) {
				//cut execution path
				if (!globalHistory.contains(iflow)) {
					noAccessContextAvailable.add(iflow); //no previous valid access context
				}
				if (!checkForDeadCode(iflow, e)) {
					history.setUnmergeable();
				}
			}

			if (!ecQueue.isEmpty()) {
				maxQueueSize = Math.max(maxQueueSize, ecQueue.size());
				assert maxQueueSize <= copyCount + excCopyCount + 1;
				if (terminated && !history.isUnmergeable()) {
					//merge back history of successfully terminated and fully covered execution paths
					ec = ecQueue.removeFirst(); //first in queue is last branch in current search path
					assert history != ec.getHistory();
					ec.getHistory().merge(history);
				} else {
					ec = pickBestFromQueue(ec, ecQueue);
				}
				iflow = ec.getIflow();
				history = ec.getHistory();
				frame = ec.getFrame();
				trace = ec.getTrace();
			} else {
				iflow = null;
				//any code not covered now is guaranteed to be dead
				final Set<InstructionFlow> notcovered = new HashSet<InstructionFlow>(cflow.getFlows());
				notcovered.removeAll(globalHistory);
				notcovered.removeAll(noAccessContextAvailable);
				deadCode.addAll(notcovered);
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
		final int instrCount = cflow.getFlowCount();
		final int liveInstrCount = instrCount - cflow.getDeadCode().size();

		SmartFrame frame = (SmartFrame) cflow.getStartFrame().clone();
		InstructionFlow iflow = cflow.getStartInstruction();
		LocalHistoryTable history = new LocalHistoryTable(instrCount);
		Trace trace = new Trace();
		ExecutionContext ec = new ExecutionContext(iflow, history, frame, trace);

		resetGlobals();

		while (iflow != null && globalHistory.size() + deadCode.size() < liveInstrCount) {

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
									prepareExceptionFrame(frame, eh),
									trace));
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
					checkForDeadCode(iflow, e);
				}

				//update history
				instrHistory.add(iflow);
				trace = trace.addEntry(iflow, succ.length);
				//prepare successor execution context
				for (int i = succ.length-1; i >= 0; i--) {
					//create a new trace/frame for each alternative successor path, except first path
					SmartFrame newFrame;
					if (i == 0) {
						newFrame = frame;
						reuseCount++;
					} else {
						newFrame = (SmartFrame) frame.clone();
						copyCount++;
					}
					//check if successor has ever been executed from this history by calculating new history
					if (history.get(succ[i]).addAll(instrHistory)) {
						//add execution context to front of queue
						ecQueue.addFirst(new ExecutionContext(
								succ[i], 
								history, 
								newFrame,
								trace));
					}
				}
			} catch (AccessContextUnavailableException e) {
				//cut execution path
				if (!globalHistory.contains(iflow)) {
					noAccessContextAvailable.add(iflow); //no previous valid access context
				}
				checkForDeadCode(iflow, e);
			}

			if (!ecQueue.isEmpty()) {
				maxQueueSize = Math.max(maxQueueSize, ecQueue.size());
				assert maxQueueSize <= copyCount + excCopyCount + 1;
				ec = ecQueue.removeFirst();
				iflow = ec.getIflow();
				history = ec.getHistory();
				frame = ec.getFrame();
				trace = ec.getTrace();
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
	protected InstructionFlow[] executeInstr(final SmartFrame frame, final InstructionFlow iflow) {
		//TODO only one valid access context is covered, which may lead to optimistic conclusions w.r.t. protected/public
		//check if instr has already been successfully visited
		if (!globalHistory.contains(iflow)) {
			//add dependencies
			addInstructionDependencies.setFrame(frame);
			iflow.accept(addInstructionDependencies); //this will bail out with an AccessContextUnavailableException on null pointer access
			final Set<InstructionFlow> sameLineSet = iflow.getSameLineSet();
			globalHistory.addAll(sameLineSet); //add all instructions that were inlined from the same source code 
			noAccessContextAvailable.removeAll(sameLineSet);
			deadCode.removeAll(sameLineSet);
		}
		try {
			//Expect BranchTargetUnavailableException
			return iflow.getSuccessors(frame);
		} finally {
			//update stack
			execution.setFrame(frame);
			execution.setIflow(iflow);
			iflow.accept(execution);
		}
	}

	/**
	 * @param frame
	 * @param eh
	 * @return The prepared frame after throwing the exception type for eh
	 */
	protected SmartFrame prepareExceptionFrame(final SmartFrame frame, final ExceptionHandler eh) {
		//Use frame copy for each exception handler
		final SmartFrame frameClone = (SmartFrame) frame.clone();
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
		assert !ecQueue.isEmpty();
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

		assert ec != null;
		return ec;
	}

	/**
	 * Checks if iflow and its successor set are dead (unreachable) code, and updates {@link #deadCode}.
	 * @param iflow
	 * @param e
	 * @return <code>true</code> iff iflow and its successor set are dead code
	 */
	protected final boolean checkForDeadCode(final InstructionFlow iflow, final ControlFlowException e) {
		if (globalHistory.contains(iflow)) {
			return false; //we managed to get here before - or to one of its inlined siblings
		}
		if (iflow.getPredecessorRun().contains(e.getCausingInstruction())) {
			//the root cause instruction is contained within the run guaranteed to precede this instruction -> dead code
			if (e instanceof BranchTargetUnavailableException) {
				//instructions in the unavailable branches are dead
				for (InstructionFlow unavailable : ((BranchTargetUnavailableException) e).getUnavailableTargets()) {
					unavailable.findSuccessorSet(deadCode);
				}
			} else {
				//instructions after this instruction - without other incoming paths - are dead
				iflow.findSuccessorSet(deadCode);
				deadCode.remove(iflow);
			}
			return true;
		}
		return false;
	}

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
