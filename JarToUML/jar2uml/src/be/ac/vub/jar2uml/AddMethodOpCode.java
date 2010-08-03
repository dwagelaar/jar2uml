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

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ATHROW;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;
import org.apache.bcel.verifier.structurals.ExceptionHandler;
import org.apache.bcel.verifier.structurals.Frame;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Model;

import be.ac.vub.jar2uml.cflow.AccessContextUnavailableException;
import be.ac.vub.jar2uml.cflow.ControlFlow;
import be.ac.vub.jar2uml.cflow.JarToUMLExecutionVisitor;
import be.ac.vub.jar2uml.cflow.LocalHistoryTable;
import be.ac.vub.jar2uml.cflow.ControlFlow.InstructionFlow;
import be.ac.vub.jar2uml.cflow.LocalHistoryTable.LocalHistorySet;

/**
 * Adds bytecode instruction dependencies for the given method.
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public class AddMethodOpCode extends AddToModel {

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
	public void addOpCode(final Classifier instrContext, final JavaClass javaClass, final Method method) throws JarToUMLException {
		if (!isIncludeInstructionReferences() || method.getCode() == null) {
			return;
		}

		reuseCount = 0;
		copyCount = 0;
		globalHistory.clear();
		noAccessContextAvailable.clear();
		addInstructionDependencies.setInstrContext(instrContext);
		addInstructionDependencies.setCp(method.getConstantPool());
		execution.setConstantPoolGen(addInstructionDependencies.getCpg());

		final MethodGen method_gen = new MethodGen(method, javaClass.getClassName(), addInstructionDependencies.getCpg());
		final ControlFlow cflow = new ControlFlow(method_gen);

		JarToUML.logger.finer(method_gen.toString());

		execute(cflow);

		JarToUML.logger.fine(String.format(
				JarToUMLResources.getString("AddMethodOpCode.instrCount"), 
				reuseCount, copyCount, method_gen.toString())); //$NON-NLS-1$

		if (method_gen.getInstructionList().size() != globalHistory.size()) {
			final InstructionHandle[] allInstr = method_gen.getInstructionList().getInstructionHandles();
			final Set<InstructionFlow> notcovered = new HashSet<InstructionFlow>(allInstr.length);
			for (InstructionHandle instr : allInstr) {
				notcovered.add(cflow.getFlowOf(instr));
			}
			notcovered.removeAll(globalHistory);
			JarToUML.logger.fine(String.format(
					JarToUMLResources.getString("AddMethodOpCode.notCovered"),
					javaClass.getClassName(),
					method_gen.toString(),
					notcovered.toString())); //$NON-NLS-1$
			final Set<InstructionFlow> naca = new HashSet<InstructionFlow>(notcovered);
			naca.retainAll(noAccessContextAvailable);
			JarToUML.logger.warning(String.format(
					JarToUMLResources.getString("AddMethodOpCode.guaranteedNPE"),
					javaClass.getClassName(),
					method_gen.toString(),
					notcovered.toString())); //$NON-NLS-1$
		}
	}

	/**
	 * Executes all instructions reachable from cflow and records the inferred dependencies.
	 * Guarantees that if there exists a valid access context for a (dynamic)
	 * field or method access, it will be found.
	 * @param cflow
	 */
	protected void execute(final ControlFlow cflow) {
		final Stack<Frame> frameStack = new Stack<Frame>();
		final Stack<LocalHistoryTable> historyStack = new Stack<LocalHistoryTable>();
		final Stack<InstructionFlow> iflowStack = new Stack<InstructionFlow>();
		final int instrCount = cflow.getMethod().getInstructionList().getLength();

		Frame frame = cflow.getStartFrame().getClone();
		InstructionFlow iflow = cflow.getStartInstruction();
		LocalHistoryTable history = new LocalHistoryTable(instrCount);

		while (iflow != null && globalHistory.size() < instrCount) {

			try {
				LocalHistorySet instrHistory = history.get(iflow);
				boolean terminated = true;
				//execute exception handlers in the context of before the covered instruction
				if (iflow.isExceptionThrower()) {
					for (ExceptionHandler eh : iflow.getExceptionHandlers()) {
						InstructionFlow succ = cflow.getFlowOf(eh.getHandlerStart());
						//create a new history for each alternative successor path
						LocalHistoryTable newHistory = history.getClone();
						copyCount++;
						//check if successor has ever been executed from this history by calculating new history
						if (newHistory.get(succ).addAll(instrHistory)) {
							//push execution context on stack
							iflowStack.push(succ);
							historyStack.push(newHistory);
							frameStack.push(prepareExceptionFrame(frame, eh));
							//if the covered instructions terminate successfully, this exception handler also should, so don't check
						}
					}
				}
				//execute instruction
				InstructionFlow[] succ = executeInstr(frame, iflow);
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
						//push execution context on stack
						iflowStack.push(succ[i]);
						historyStack.push(newHistory);
						frameStack.push(newFrame);
						terminated = false;
					}
				}
				if (terminated && !historyStack.isEmpty()) {
					//copy back history of successfully terminated execution paths
					historyStack.peek().union(history);
				}
			} catch (AccessContextUnavailableException e) {
				//cut execution path and remember the reason
				noAccessContextAvailable.add(iflow);
			}

			if (!iflowStack.isEmpty()) {
				//retrieve next execution context from stack
				iflow = iflowStack.pop();
				history = historyStack.pop();
				frame = frameStack.pop();
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
	 */
	private InstructionFlow[] executeInstr(final Frame frame, final InstructionFlow iflow) {
		//check if instr has already been successfully visited
		if (!globalHistory.contains(iflow)) {
			//add dependencies
			addInstructionDependencies.setFrame(frame);
			iflow.accept(addInstructionDependencies); // this will bail out with an AccessContextUnavailableException on null pointer access
			globalHistory.add(iflow);
		}
		final InstructionFlow[] succ = iflow.getSuccessors(frame);
		//update stack
		execution.setFrame(frame);
		iflow.accept(execution);
		return succ;
	}

	/**
	 * @param frame
	 * @param eh
	 * @return The prepared frame after throwing the exception type for eh
	 */
	private Frame prepareExceptionFrame(final Frame frame, final ExceptionHandler eh) {
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
	
}
