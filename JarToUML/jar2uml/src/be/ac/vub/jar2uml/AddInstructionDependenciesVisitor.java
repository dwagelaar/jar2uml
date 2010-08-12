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
import org.apache.bcel.generic.FieldOrMethod;
import org.apache.bcel.generic.GETFIELD;
import org.apache.bcel.generic.GETSTATIC;
import org.apache.bcel.generic.INVOKEINTERFACE;
import org.apache.bcel.generic.INVOKESPECIAL;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.PUTFIELD;
import org.apache.bcel.generic.PUTSTATIC;
import org.apache.bcel.generic.ReferenceType;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.DataType;
import org.eclipse.uml2.uml.Feature;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.VisibilityKind;

import be.ac.vub.jar2uml.cflow.AccessContextUnavailableException;
import be.ac.vub.jar2uml.cflow.SmartFrame;

/**
 * Adds classifier fields/methods referenced by the switched bytecode instruction to the model.
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public class AddInstructionDependenciesVisitor extends EmptyVisitor {

	private Classifier instrContext;
	private ConstantPool cp;
	private ConstantPoolGen cpg;
	private SmartFrame frame;

	protected TypeToClassifierSwitch typeToClassifier = null;
	protected AddClassifierPropertySwitch addClassifierProperty = null;
	protected AddClassifierOperationSwitch addClassifierOperation = null;
	protected ReplaceByClassifierSwitch replaceByClassifier = new ReplaceByClassifierSwitch();
	protected Classifier owner = null;

	/**
	 * Creates a new {@link AddInstructionDependenciesVisitor}.
	 * @param typeToClassifierSwitch
	 * @param addClassifierPropertySwitch
	 * @param addClassifierOperationSwitch
	 */
	public AddInstructionDependenciesVisitor(
			TypeToClassifierSwitch typeToClassifierSwitch,
			AddClassifierPropertySwitch addClassifierPropertySwitch,
			AddClassifierOperationSwitch addClassifierOperationSwitch) {
		super();
		assert typeToClassifierSwitch != null;
		assert addClassifierPropertySwitch != null;
		assert addClassifierOperationSwitch != null;
		this.typeToClassifier = typeToClassifierSwitch;
		this.addClassifierProperty = addClassifierPropertySwitch;
		this.addClassifierOperation = addClassifierOperationSwitch;
	}

	/* (non-Javadoc)
	 * @see org.apache.bcel.generic.EmptyVisitor#visitAALOAD(org.apache.bcel.generic.AALOAD)
	 */
	@Override
	public void visitAALOAD(AALOAD obj) {
		typeToClassifier.doSwitch(getGetFieldAccessContext());
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.bcel.generic.EmptyVisitor#visitFieldOrMethod(org.apache.bcel.generic.FieldOrMethod)
	 */
	@Override
	public void visitFieldOrMethod(FieldOrMethod obj) {
		final ConstantPoolGen cpg = getCpg();
		assert cpg != null;
		final ReferenceType fieldOwner = obj.getReferenceType(cpg);
		owner = (Classifier) typeToClassifier.doSwitch(fieldOwner);
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.bcel.generic.EmptyVisitor#visitGETFIELD(org.apache.bcel.generic.GETFIELD)
	 */
	@Override
	public void visitGETFIELD(GETFIELD obj) {
		final Classifier accessContext = typeToClassifier.doSwitch(getGetFieldAccessContext());
		//Only classes have instance fields
		assert owner instanceof Class;
		final ConstantPoolGen cpg = getCpg();
		addClassifierProperty.setPropertyName(obj.getFieldName(cpg));
		addClassifierProperty.setBCELPropertyType(obj.getFieldType(cpg));
		final Property att = (Property) addClassifierProperty.doSwitch(owner);
		setVisibility(att, accessContext);
		att.setIsStatic(false);
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.bcel.generic.EmptyVisitor#visitGETSTATIC(org.apache.bcel.generic.GETSTATIC)
	 */
	@Override
	public void visitGETSTATIC(GETSTATIC obj) {
		//Can be invoked on interfaces as well as classes (static final) -> allow DataType
		assert (owner instanceof Class) || (owner instanceof Interface) || (owner instanceof DataType);
		final ConstantPoolGen cpg = getCpg();
		addClassifierProperty.setPropertyName(obj.getFieldName(cpg));
		addClassifierProperty.setBCELPropertyType(obj.getFieldType(cpg));
		final Property att = (Property) addClassifierProperty.doSwitch(owner);
		setVisibilityStatic(att);
		att.setIsStatic(true);
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.bcel.generic.EmptyVisitor#visitInvokeInstruction(org.apache.bcel.generic.InvokeInstruction)
	 */
	@Override
	public void visitInvokeInstruction(InvokeInstruction obj) {
		final ConstantPoolGen cpg = getCpg();
		final org.apache.bcel.generic.Type returnType = obj.getReturnType(cpg);
		try {
			addClassifierOperation.setOperationName(obj.getMethodName(cpg));
			addClassifierOperation.setBCELArgumentTypes(obj.getArgumentTypes(cpg));
			addClassifierOperation.setBCELReturnType(returnType);
		} catch (JarToUMLException e) {
			throw new RuntimeException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.bcel.generic.EmptyVisitor#visitINVOKEINTERFACE(org.apache.bcel.generic.INVOKEINTERFACE)
	 */
	@Override
	public void visitINVOKEINTERFACE(INVOKEINTERFACE obj) {
		final Classifier accessContext = typeToClassifier.doSwitch(getAccessContext(obj));
		//Can be invoked only on interfaces
		assert owner instanceof Interface;
		final Operation newOp = (Operation) addClassifierOperation.doSwitch(owner);
		setVisibility(newOp, accessContext);
		setIsLeaf(newOp, accessContext);
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.bcel.generic.EmptyVisitor#visitINVOKESPECIAL(org.apache.bcel.generic.INVOKESPECIAL)
	 */
	@Override
	public void visitINVOKESPECIAL(INVOKESPECIAL obj) {
		final Classifier accessContext = typeToClassifier.doSwitch(getAccessContext(obj));
		//Can be invoked only on classes (<init>, superclass methods and private methods)
		assert owner instanceof Class;
		Operation newOp = (Operation) addClassifierOperation.doSwitch(owner);
		setVisibility(newOp, accessContext);
		setIsLeaf(newOp, accessContext);
		newOp.setIsAbstract(false); //these methods are never abstract
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.bcel.generic.EmptyVisitor#visitINVOKESTATIC(org.apache.bcel.generic.INVOKESTATIC)
	 */
	@Override
	public void visitINVOKESTATIC(INVOKESTATIC obj) {
		//Can be invoked only on classes (interfaces cannot contain static method headers)
		assert owner instanceof Class;
		Operation newOp = (Operation) addClassifierOperation.doSwitch(owner);
		setVisibilityStatic(newOp);
		setIsLeafStatic(newOp);
		newOp.setIsAbstract(false); //these methods are never abstract
		newOp.setIsStatic(true);
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.bcel.generic.EmptyVisitor#visitINVOKEVIRTUAL(org.apache.bcel.generic.INVOKEVIRTUAL)
	 */
	@Override
	public void visitINVOKEVIRTUAL(INVOKEVIRTUAL obj) {
		final Classifier accessContext = typeToClassifier.doSwitch(getAccessContext(obj));
		//Can be invoked only on classes and array types (refers to all remaining non-interface methods)
		assert owner instanceof Class || TypeToClassifierSwitch.isArrayType(owner);
		Operation newOp = (Operation) addClassifierOperation.doSwitch(owner);
		setVisibility(newOp, accessContext);
		setIsLeaf(newOp, accessContext);
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.bcel.generic.EmptyVisitor#visitPUTFIELD(org.apache.bcel.generic.PUTFIELD)
	 */
	@Override
	public void visitPUTFIELD(PUTFIELD obj) {
		final Classifier accessContext = typeToClassifier.doSwitch(getPutFieldAccessContext());
		//Only classes have instance fields
		assert owner instanceof Class;
		addClassifierProperty.setPropertyName(obj.getFieldName(cpg));
		addClassifierProperty.setBCELPropertyType(obj.getFieldType(cpg));
		final Property att = (Property) addClassifierProperty.doSwitch(owner);
		setVisibility(att, accessContext);
		//fields cannot be redefined in Java, so not possible to check on 'isLeaf'
		//even 'final' (isReadOly) fields can be 'put' once
		att.setIsStatic(false);
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.bcel.generic.EmptyVisitor#visitPUTSTATIC(org.apache.bcel.generic.PUTSTATIC)
	 */
	@Override
	public void visitPUTSTATIC(PUTSTATIC obj) {
		//Can be invoked on interfaces as well as classes (static final) -> allow DataType
		assert (owner instanceof Class) || (owner instanceof Interface) || (owner instanceof DataType);
		addClassifierProperty.setPropertyName(obj.getFieldName(cpg));
		addClassifierProperty.setBCELPropertyType(obj.getFieldType(cpg));
		final Property att = (Property) addClassifierProperty.doSwitch(owner);
		setVisibilityStatic(att);
		//fields cannot be redefined in Java, so not possible to check on 'isLeaf'
		//even 'final' (isReadOnly) fields can be 'put' once
		att.setIsStatic(true);
	}

	/**
	 * @param instr
	 * @return The access context of the (dynamic) invoke instruction, if available.
	 * @throws AccessContextUnavailableException if the access context is not available
	 */
	private org.apache.bcel.generic.Type getAccessContext(final InvokeInstruction instr) {
		assert !(instr instanceof INVOKESTATIC);
		final SmartFrame frame = getFrame();
		assert frame != null;
		final int stackIndex = addClassifierOperation.getArgumentTypes().size();
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
	private org.apache.bcel.generic.Type getGetFieldAccessContext() {
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
	private org.apache.bcel.generic.Type getPutFieldAccessContext() {
		final SmartFrame frame = getFrame();
		assert frame != null;
		final org.apache.bcel.generic.Type accessContext = frame.getStack().peek(1);
		if (accessContext.equals(org.apache.bcel.generic.Type.NULL)) {
			throw new AccessContextUnavailableException(frame.getResponsibleForStackEntry(1));
		}
		return accessContext;
	}

	/**
	 * Sets the visibility of feature, given the access context.
	 * @param feature
	 * @param accessContext
	 */
	private void setVisibility(final Feature feature, final Classifier accessContext) {
		final Classifier instrContext = getInstrContext();
		if (owner.equals(instrContext)) {
			return; //This feature is part of the contained code, and does not need to be inferred
		}
		if (owner.getNearestPackage().equals(instrContext.getNearestPackage())) {
			//feature access within same package
			if (!feature.isSetVisibility()) {
				feature.setVisibility(VisibilityKind.PACKAGE_LITERAL);
			}
		} else if (instrContext.equals(accessContext)) {
			//feature access on instance of this class
			if (!feature.isSetVisibility() || feature.getVisibility().equals(VisibilityKind.PACKAGE_LITERAL)) {
				feature.setVisibility(VisibilityKind.PROTECTED_LITERAL);
			}
		} else {
			//other feature access
			if (TypeToClassifierSwitch.isArrayType(accessContext) && "java::lang::Object::clone".equals(JarToUML.qualifiedName(feature))) {
				/* 
				 * Array types need special treatment: see 10.7 in http://java.sun.com/docs/books/jls/second_edition/html/arrays.doc.html
				 * All array types have an implicit public clone() method that is inherited from java.lang.Object.
				 * Here, the owner shows up as java.lang.Object, which means that it is actually clone() from java.lang.Object that is invoked.
				 */
				if (!feature.isSetVisibility() || feature.getVisibility().equals(VisibilityKind.PACKAGE_LITERAL)) {
					feature.setVisibility(VisibilityKind.PROTECTED_LITERAL);
				}
			} else {
				feature.setVisibility(VisibilityKind.PUBLIC_LITERAL);
			}
		}
	}

	/**
	 * Sets the visibility of a static feature.
	 * WARNING: this only works correctly if the entire class hierarchy is known!
	 * @param feature
	 */
	private void setVisibilityStatic(final Feature feature) {
		final Classifier instrContext = getInstrContext();
		if (owner.equals(instrContext)) {
			return; //This feature is part of the contained code, and does not need to be inferred
		}
		if (owner.getNearestPackage().equals(instrContext.getNearestPackage())) {
			//feature access within same package
			if (!feature.isSetVisibility()) {
				feature.setVisibility(VisibilityKind.PACKAGE_LITERAL);
			}
		} else if (instrContext.conformsTo(owner)) {
			//feature access on known superclass
			//TODO WARNING: this only works correctly if the entire class hierarchy is known!
			//Inheritance links between inferred classes are typically missing!
			if (!feature.isSetVisibility() || feature.getVisibility().equals(VisibilityKind.PACKAGE_LITERAL)) {
				feature.setVisibility(VisibilityKind.PROTECTED_LITERAL);
			}
		} //other feature access (uncertain due to incomplete class hierarchy): leave visibility unset (implicit public)
	}

	/**
	 * Checks if op cannot be final, and sets it so if true. 
	 * @param op
	 * @param accessContext
	 */
	private void setIsLeaf(final Operation op, final Classifier accessContext) {
		final Classifier instrContext = getInstrContext();
		if (owner.equals(instrContext)) {
			return; //This feature is part of the contained code, and does not need to be inferred
		}
		if (instrContext.conformsTo(accessContext) || instrContext.conformsTo(owner)) {
			//feature access on instance of this class or known superclass
			final EList<Parameter> params = op.getOwnedParameters();
			final Operation childOp = instrContext.getOperation(op.getName(), getParameterNames(params), getParameterTypes(params));
			if (childOp != null) {
				op.setIsLeaf(false);
			}
		}
	}

	/**
	 * Checks if op cannot be final, and sets it so if true.
	 * WARNING: this only works correctly if the entire class hierarchy is known!
	 * @param op
	 */
	private void setIsLeafStatic(final Operation op) {
		final Classifier instrContext = getInstrContext();
		if (owner.equals(instrContext)) {
			return; //This feature is part of the contained code, and does not need to be inferred
		}
		if (instrContext.conformsTo(owner)) {
			//feature access on superclass
			//TODO WARNING: this only works correctly if the entire class hierarchy is known!
			//Inheritance links between inferred classes are typically missing!
			final EList<Parameter> params = op.getOwnedParameters();
			final Operation childOp = instrContext.getOperation(op.getName(), getParameterNames(params), getParameterTypes(params));
			if (childOp != null) {
				op.setIsLeaf(false);
			}
		}
	}

	/**
	 * @param parameters
	 * @return An {@link EList} of the names of each {@link Parameter} in parameters.
	 */
	private EList<String> getParameterNames(final EList<Parameter> parameters) {
		final EList<String> names = new BasicEList<String>();
		for (Parameter par : parameters) {
			names.add(par.getName());
		}
		return names;
	}

	/**
	 * @param parameters
	 * @return An {@link EList} of the {@link Type}s of each {@link Parameter} in parameters.
	 */
	private EList<Type> getParameterTypes(final EList<Parameter> parameters) {
		final EList<Type> types = new BasicEList<Type>();
		for (Parameter par : parameters) {
			types.add(par.getType());
		}
		return types;
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
	 * @return The context in which the instructions run.
	 */
	public Classifier getInstrContext() {
		return instrContext;
	}

	/**
	 * Sets the context in which the instructions run.
	 * @param instrContext
	 */
	public void setInstrContext(Classifier instrContext) {
		this.instrContext = instrContext;
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

}
