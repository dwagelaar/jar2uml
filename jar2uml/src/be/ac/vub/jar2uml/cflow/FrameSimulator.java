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

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.bcel.generic.ATHROW;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;

import be.ac.vub.jar2uml.Cancellable;
import be.ac.vub.jar2uml.JarToUML;
import be.ac.vub.jar2uml.JarToUMLResources;
import be.ac.vub.jar2uml.cflow.ControlFlow.InstructionFlow;
import be.ac.vub.jar2uml.cflow.LocalHistoryTable.LocalHistorySet;

/**
 * Simulates JVM execution frames by going through possible
 * execution paths of a {@link ControlFlow}. Guarantees that
 * the given visitor will have exactly one valid frame context
 * for each instruction, if it exists.
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public class FrameSimulator {

	/**
	 * Algorithm cut-off point, where execution stops.
	 */
	private static final int CUT_OFF = 0x8000;

	protected final SmartExecutionVisitor execution;
	protected final VisitorWithFrame visitor;
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
	/**
	 * All instructions which are not covered (skipped?) by the stack simulation algorithm.
	 */
	protected final Set<InstructionFlow> notCovered = new HashSet<InstructionFlow>();

	private boolean cutOff;
	private int reuseCount = 0;
	private int copyCount = 0;
	private int excCopyCount = 0;
	private int maxQueueSize = 0;
	private Cancellable cancellable;

	/**
	 * Creates a new {@link FrameSimulator}.
	 * @param execution the execution visitor responsible for updating the frame after every instruction
	 * @param visitor the visitor that needs a valid frame for its execution
	 */
	public FrameSimulator(SmartExecutionVisitor execution, VisitorWithFrame visitor) {
		super();
		assert execution != null;
		assert visitor != null;
		this.execution = execution;
		this.visitor = visitor;
	}

	/**
	 * Executes this {@link FrameSimulator} for he given {@link ControlFlow}.
	 * @param cflow
	 */
	public synchronized void execute(final ControlFlow cflow) {
		final MethodGen method = cflow.getMethod();
		final int instrCount = cflow.getFlowCount();
		final int liveInstrCount = instrCount - cflow.getDeadCode().size();

		execution.setConstantPoolGen(method.getConstantPool());
		execution.setLocalVarTable(method.getLocalVariableTable(method.getConstantPool()));

		JarToUML.logger.finest(method.toString());

		//try first with simplified algorithm
		executeSimple(cflow);
		JarToUML.logger.finer(String.format(
				JarToUMLResources.getString("AddMethodOpCode.instrCount"), 
				reuseCount, copyCount, excCopyCount, maxQueueSize, method)); //$NON-NLS-1$

		if (liveInstrCount > globalHistory.size() + deadCode.size()) {
			/*
			 * Fall back to full algorithm if instructions were skipped without being proved unreachable.
			 * 
			 * Since we check the types in the local variable table, we can avoid most unreachable code
			 * situations. Some methods do not have a local variable table, however, so we must retain
			 * the full algorithm.
			 */
			JarToUML.logger.fine(String.format(
					JarToUMLResources.getString("AddMethodOpCode.fallback"), 
					method.toString())); //$NON-NLS-1$
			executeFull(cflow);
			JarToUML.logger.finer(String.format(
					JarToUMLResources.getString("AddMethodOpCode.instrCount"), 
					reuseCount, copyCount, excCopyCount, maxQueueSize, method)); //$NON-NLS-1$
		}

		assert noAccessContextAvailable.isEmpty() || instrCount > globalHistory.size();

		if (instrCount > globalHistory.size()) {
			//no all instructions were covered -> report why
			if (!noAccessContextAvailable.isEmpty()) {
				final SortedSet<InstructionFlow> naca = new TreeSet<InstructionFlow>(noAccessContextAvailable);
				JarToUML.logger.info(String.format(
						JarToUMLResources.getString("AddMethodOpCode.guaranteedNPE"),
						method.getClassName(),
						method,
						naca,
						ControlFlow.getLineNumbers(naca))); //$NON-NLS-1$
			}
			final SortedSet<InstructionFlow> allDeadCode = new TreeSet<InstructionFlow>(cflow.getDeadCode());
			allDeadCode.addAll(deadCode);
			if (!allDeadCode.isEmpty()) {
				JarToUML.logger.info(String.format(
						JarToUMLResources.getString("AddMethodOpCode.guaranteedDead"),
						method.getClassName(),
						method,
						allDeadCode,
						ControlFlow.getLineNumbers(allDeadCode))); //$NON-NLS-1$
			}
			notCovered.addAll(cflow.getFlows());
			notCovered.removeAll(globalHistory);
			notCovered.removeAll(allDeadCode);
			notCovered.removeAll(noAccessContextAvailable);
			if (!notCovered.isEmpty()) {
				final SortedSet<InstructionFlow> nc = new TreeSet<InstructionFlow>(notCovered);
				JarToUML.logger.warning(String.format(
						JarToUMLResources.getString("AddMethodOpCode.notCovered"),
						method.getClassName(),
						method,
						nc,
						ControlFlow.getLineNumbers(nc))); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Executes all instructions reachable from cflow and records the inferred dependencies.
	 * Guarantees that if there exists a valid access context for an instance
	 * field or method access, it will be found.
	 * @param cflow
	 */
	protected void executeFull(final ControlFlow cflow) {
		final LinkedList<ExecutionContext> ecQueue = new LinkedList<ExecutionContext>();
		final int instrCount = cflow.getFlowCount();
		final int liveInstrCount = instrCount - cflow.getDeadCode().size();

		SmartFrame frame = (SmartFrame) cflow.getStartFrame().clone();
		InstructionFlow iflow = cflow.getStartInstruction();
		LocalHistoryTable history = new LocalHistoryTable(instrCount);
		Trace trace = new Trace();
		ExecutionContext ec = new ExecutionContext(iflow, history, frame, trace);
		int reuseCount = 0;
		int copyCount = 0;
		int excCopyCount = 0;
		int maxQueueSize = 0;

		resetGlobals();

		while (iflow != null //done searching
				&& globalHistory.size() + deadCode.size() < liveInstrCount) { //all live instructions covered 

			if (copyCount + excCopyCount > CUT_OFF) {
				setCutOff(true);
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
						InstructionFlow succ = eh.getHandlerStart();
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
					if (i == 0) {
						newHistory = history;
						reuseCount++;
					} else {
						newHistory = (LocalHistoryTable) history.clone();
						copyCount++;
					}
					//check if successor has ever been executed from this history by calculating new history
					if (newHistory.get(succ[i]).addAll(instrHistory)) {
						//add execution context to front of queue
						ecQueue.addFirst(new ExecutionContext(
								succ[i], 
								newHistory, 
								i == 0 ? frame : (SmartFrame) frame.clone(),
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

		//store stats
		setReuseCount(reuseCount);
		setCopyCount(copyCount);
		setExcCopyCount(excCopyCount);
		setMaxQueueSize(maxQueueSize);
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
		int reuseCount = 0;
		int copyCount = 0;
		int excCopyCount = 0;
		int maxQueueSize = 0;

		resetGlobals();

		while (iflow != null && globalHistory.size() + deadCode.size() < liveInstrCount) {

			try {
				LocalHistorySet instrHistory = history.get(iflow);
				//execute exception handlers in the context of before the covered instruction
				if (iflow.isExceptionThrower()) {
					for (ExceptionHandler eh : iflow.getExceptionHandlers()) {
						InstructionFlow succ = eh.getHandlerStart();
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

		//store stats
		setReuseCount(reuseCount);
		setCopyCount(copyCount);
		setExcCopyCount(excCopyCount);
		setMaxQueueSize(maxQueueSize);
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
		//check if instr has already been successfully visited
		if (!globalHistory.contains(iflow)) {
			//add dependencies
			visitor.setFrame(frame);
			iflow.accept(visitor); //this can bail out with an AccessContextUnavailableException on null pointer access
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
	 * Resets object-wide fields.
	 */
	protected void resetGlobals() {
		globalHistory.clear();
		noAccessContextAvailable.clear();
		deadCode.clear();
		notCovered.clear();
		setCutOff(false);
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
		execution.setIflow(null);
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
	 * Checks if the running execution needs to be cancelled.
	 */
	protected final void checkCancelled() {
		final Cancellable cancellable = getCancellable();
		if (cancellable != null) {
			cancellable.checkCancelled();
		}
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
	 * @return the deadCode
	 */
	public Set<InstructionFlow> getDeadCode() {
		return Collections.unmodifiableSet(deadCode);
	}

	/**
	 * @return the notCovered
	 */
	public Set<InstructionFlow> getNotCovered() {
		return Collections.unmodifiableSet(notCovered);
	}

	/**
	 * @return the cutOff
	 */
	public boolean isCutOff() {
		return cutOff;
	}

	/**
	 * @param cutOff the cutOff to set
	 */
	protected void setCutOff(boolean cutOff) {
		this.cutOff = cutOff;
	}

	/**
	 * @return the history table reuse statistics.
	 */
	public int getReuseCount() {
		return reuseCount;
	}

	/**
	 * @param reuseCount the reuseCount to set
	 */
	protected void setReuseCount(int reuseCount) {
		this.reuseCount = reuseCount;
	}

	/**
	 * @return the history table copy statistics.
	 */
	public int getCopyCount() {
		return copyCount;
	}

	/**
	 * @param copyCount the copyCount to set
	 */
	protected void setCopyCount(int copyCount) {
		this.copyCount = copyCount;
	}

	/**
	 * @return the history table copy statistics for exception handlers.
	 */
	public int getExcCopyCount() {
		return excCopyCount;
	}

	/**
	 * @param excCopyCount the excCopyCount to set
	 */
	protected void setExcCopyCount(int excCopyCount) {
		this.excCopyCount = excCopyCount;
	}

	/**
	 * @return the maximum execution context queue size statistics (simultaneous search paths).
	 */
	public int getMaxQueueSize() {
		return maxQueueSize;
	}

	/**
	 * @param maxQueueSize the maxQueueSize to set
	 */
	protected void setMaxQueueSize(int maxQueueSize) {
		this.maxQueueSize = maxQueueSize;
	}

	/**
	 * @param cancellable the cancellable to set
	 */
	public void setCancellable(Cancellable cancellable) {
		this.cancellable = cancellable;
	}

	/**
	 * @return the cancellable
	 */
	public Cancellable getCancellable() {
		return cancellable;
	}

	/**
	 * @return the execution
	 */
	public SmartExecutionVisitor getExecution() {
		return execution;
	}

	/**
	 * @return the visitor
	 */
	public VisitorWithFrame getVisitor() {
		return visitor;
	}

}
