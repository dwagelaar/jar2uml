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

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.Attribute;
import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.classfile.StackMap;
import org.apache.bcel.generic.ATHROW;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.Type;
import org.apache.bcel.verifier.exc.StructuralCodeConstraintException;
import org.apache.bcel.verifier.structurals.ControlFlowGraph;
import org.apache.bcel.verifier.structurals.ExceptionHandler;
import org.apache.bcel.verifier.structurals.Frame;
import org.apache.bcel.verifier.structurals.InstructionContext;
import org.apache.bcel.verifier.structurals.LocalVariables;
import org.apache.bcel.verifier.structurals.UninitializedObjectType;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Property;

import be.ac.vub.jar2uml.cflow.JarToUMLExecutionVisitor;

/**
 * Adds {@link Classifier} {@link Operation}s and {@link Property}s to the UML {@link Model}.
 * This represents the first pass in {@link JarToUML}.
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public class AddProperties extends AddToModel {

	/**
	 * @param code
	 * @return True if the code has been preverified for CLDC execution, i.e. it has a StackMap attribute
	 */
	public static boolean isPreverified(Code code) {
		if (code == null) {
			return false;
		}
		for (Attribute att : code.getAttributes()) {
			if (att instanceof StackMap) {
				return true;
			}
		}
		return false;
	}

	protected AddInstructionDependenciesVisitor addInstructionDependencies = 
		new AddInstructionDependenciesVisitor(
				typeToClassifier,
				addClassifierProperty,
				addClassifierOperation);
	protected JarToUMLExecutionVisitor execution = new JarToUMLExecutionVisitor();

	private boolean preverified;

	/**
	 * Creates a new {@link AddProperties}.
	 * @param filter A filter to apply to model operations.
	 * @param monitor A progress monitor to check for end user cancellation.
	 * @param model The UML model to store generated elements in.
	 * @param includeFeatures Whether to include fields and methods.
	 * @param includeInstructionReferences Whether or not to include Java elements that are
	 * referred to by bytecode instructions.
	 */
	public AddProperties(Filter filter, IProgressMonitor monitor, Model model,
			boolean includeFeatures, boolean includeInstructionReferences) {
		super(filter, monitor, model, includeFeatures, includeInstructionReferences);
	}

	/**
	 * Adds the properties of all classifiers in parsedClasses to the classifiers in the UML model.
	 * @param parsedClasses
	 * @throws IOException
	 * @throws JarToUMLException 
	 */
	public void addAllProperties(Collection<JavaClass> parsedClasses) throws IOException, JarToUMLException {
		if (isIncludeFeatures()) {
			for (JavaClass javaClass : parsedClasses) {
				addClassifierProperties(javaClass);
				checkCancelled();
			}
		}
	}

	/**
	 * Adds the properties of the javaClass to the corresponding classifier in the UML model.
	 * @param javaClass The BCEL class representation to convert.
	 * @throws JarToUMLException 
	 */
	public void addClassifierProperties(JavaClass javaClass) throws JarToUMLException {
		final String className = javaClass.getClassName();
		if (!filter(javaClass)) {
			logSkippedFiltered(javaClass);
			return;
		}
		JarToUML.logger.finest(className);
		final Classifier classifier = findContainedClassifier.findClassifier(
				getModel(), className, null);
		addProperties(classifier, javaClass);
		addOperations(classifier, javaClass);
	}

	/**
	 * Adds a property to classifier for each javaClass field.
	 * @param classifier The classifier representation of javaClass.
	 * @param javaClass The Java class file to convert.
	 * @throws JarToUMLException 
	 */
	public void addProperties(Classifier classifier, JavaClass javaClass) throws JarToUMLException {
		Assert.assertNotNull(classifier);
		Field[] fields = javaClass.getFields();
		for (int i = 0; i < fields.length; i++) {
			if (!filter(fields[i])) {
				continue;
			}
			JarToUML.logger.finest(fields[i].getSignature());
			addClassifierProperty.setPropertyName(fields[i].getName());
			addClassifierProperty.setBCELPropertyType(fields[i].getType());
			if (addClassifierProperty.getPropertyType() == null) {
				throw new JarToUMLException(String.format(
						JarToUMLResources.getString("typeNotFoundFor"), 
						javaClass.getClassName(),
						fields[i].getName(),
						fields[i].getType().getSignature())); //$NON-NLS-1$
			}
			Property prop = (Property) addClassifierProperty.doSwitch(classifier);
			prop.setVisibility(JarToUML.toUMLVisibility(fields[i]));
			prop.setIsStatic(fields[i].isStatic());
			prop.setIsReadOnly(fields[i].isFinal());
			prop.setIsLeaf(fields[i].isFinal());
		}
	}

	/**
	 * Adds an operation to classifier for each javaClass method.
	 * @param classifier The classifier representation of javaClass.
	 * @param javaClass The Java class file to convert.
	 * @throws JarToUMLException 
	 */
	public void addOperations(Classifier classifier, JavaClass javaClass) throws JarToUMLException {
		Assert.assertNotNull(classifier);
		Method[] methods = javaClass.getMethods();
		for (int i = 0; i < methods.length; i++) {
			if (getFilter() != null) {
				if (!getFilter().filter(methods[i])) {
					continue;
				}
			}
			JarToUML.logger.finest(methods[i].getSignature());
			org.apache.bcel.generic.Type[] types = methods[i].getArgumentTypes();
			addClassifierOperation.setOperationName(methods[i].getName());
			addClassifierOperation.setBCELArgumentTypes(types);
			addClassifierOperation.setBCELReturnType(methods[i].getReturnType());
			Operation op = (Operation) addClassifierOperation.doSwitch(classifier);
			op.setVisibility(JarToUML.toUMLVisibility(methods[i]));
			op.setIsAbstract(methods[i].isAbstract());
			op.setIsStatic(methods[i].isStatic());
			op.setIsLeaf(methods[i].isFinal());
			if (isIncludeInstructionReferences()) {
				addOpCode(classifier, javaClass, methods[i]);
			}
			if (isPreverified(methods[i].getCode())) {
				setPreverified(true);
			}
		}
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
		final Code code = method.getCode();
		if (code == null) {
			return;
		}
		addInstructionDependencies.setInstrContext(instrContext);
		addInstructionDependencies.setCp(method.getConstantPool());
		final MethodGen method_gen = new MethodGen(method, javaClass.getClassName(), addInstructionDependencies.getCpg());
		try {
			final ControlFlowGraph cfgraph = new ControlFlowGraph(method_gen);
			final InstructionContext[] succ = new InstructionContext[] {
					cfgraph.contextOf(method_gen.getInstructionList().getStart())
			};
			//Successfully determined control flow graph: simulate stack frame
			final Frame frame = new Frame(code.getMaxLocals(), code.getMaxStack());
			initLocalVariableTypes(frame, javaClass, method);
			addInstructionDependencies.setFrame(frame);
			execution.setConstantPoolGen(addInstructionDependencies.getCpg());
			execution.setFrame(frame);
			executeInstr(cfgraph, succ, new HashSet<InstructionContext>());
		} catch (StructuralCodeConstraintException e) {
			JarToUML.logger.warning(String.format(
					JarToUMLResources.getString("AddProperties.cannotCreateCFG"),
					method_gen, javaClass.getClassName(), e.getLocalizedMessage())); //$NON-NLS-1$
			//Fall back to naive instruction visiting, which cannot keep a correct stack frame
			addInstructionDependencies.setFrame(null);
			final InstructionList instrList = new InstructionList(code.getCode());
			final Instruction[] instr = instrList.getInstructions();
			for (int i = 0; i < instr.length; i++) {
				instr[i].accept(addInstructionDependencies);
				final Exception ve = addInstructionDependencies.getException();
				if (ve != null) {
					throw new JarToUMLException(ve);
				}
			}
		}
	}
	
	/**
	 * Executes all possible execution paths and records the inferred dependencies.
	 * @param cfg the control flow graph
	 * @param succ all possible successor instructions
	 * @param history already executed instructions
	 * @throws JarToUMLException
	 */
	private void executeInstr(final ControlFlowGraph cfg, final InstructionContext[] succ, final Set<InstructionContext> history) throws JarToUMLException {
		final Frame frame = addInstructionDependencies.getFrame();
		for (InstructionContext ic : succ) {
			//Skip already covered instructions
			if (history.contains(ic)) {
				continue;
			}
			//Use frame copy for each possible execution path
			Frame frameClone = frame.getClone();
			addInstructionDependencies.setFrame(frameClone);
			execution.setFrame(frameClone);
			//add dependencies
			InstructionHandle instr = ic.getInstruction();
			instr.accept(addInstructionDependencies);
			Exception e = addInstructionDependencies.getException();
			if (e != null) {
				throw new JarToUMLException(e);
			}
			//update stack
			instr.accept(execution);
			history.add(ic);
			executeInstr(cfg, ic.getSuccessors(), history);
			executeInstr(cfg, ic.getExceptionHandlers(), history);
		}
	}
	
	/**
	 * Executes all possible execution paths and records the inferred dependencies.
	 * @param cfg the control flow graph
	 * @param succ all possible successor instructions
	 * @param history already executed instructions
	 * @throws JarToUMLException
	 */
	private void executeInstr(final ControlFlowGraph cfg, final ExceptionHandler[] succ, final Set<InstructionContext> history) throws JarToUMLException {
		final Frame frame = addInstructionDependencies.getFrame();
		for (ExceptionHandler eh : succ) {
			//Use frame copy for each possible execution path
			Frame frameClone = frame.getClone();
			addInstructionDependencies.setFrame(frameClone);
			execution.setFrame(frameClone);
			//simulate throwing the exception, resulting in a correct stack
			frameClone.getStack().clear();
			final org.apache.bcel.generic.ObjectType exceptionType = eh.getExceptionType();
			if (exceptionType != null) {
				frameClone.getStack().push(exceptionType);
			} else {
				frameClone.getStack().push(Type.THROWABLE);
			}
			new ATHROW().accept(execution);
			//execute handler
			executeInstr(cfg, new InstructionContext[]{ cfg.contextOf(eh.getHandlerStart()) }, history);
		}
	}
	
	/**
	 * Initialises the local variable types in frame according to the method
	 * context (javaClass) and argument types.
	 * @param frame
	 * @param javaClass
	 * @param method
	 */
	private void initLocalVariableTypes(final Frame frame, final JavaClass javaClass, final Method method) {
		final LocalVariables localVars = frame.getLocals();
		int i = 0;
		if (!method.isStatic()) {
			final ObjectType objectType = new ObjectType(javaClass.getClassName());
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
			i++;
		}
	}

	/**
	 * Whether or not the bytecode has been preverified for execution on J2ME CLDC.
	 * @return the preverified
	 */
	public boolean isPreverified() {
		return preverified;
	}

	/**
	 * @param preverified the preverified to set
	 */
	protected void setPreverified(boolean preverified) {
		this.preverified = preverified;
	}

}
