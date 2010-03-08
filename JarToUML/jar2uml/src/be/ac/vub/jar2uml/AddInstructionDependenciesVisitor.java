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

import junit.framework.Assert;

import org.apache.bcel.classfile.ConstantPool;
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
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.VisibilityKind;

/**
 * Adds classifier fields/methods referenced by the switched bytecode instruction to the model.
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public class AddInstructionDependenciesVisitor extends EmptyVisitor {

	private Classifier instrContext = null;
	private ConstantPool cp = null;
	private ConstantPoolGen cpg = null;
	private Exception exception = null;

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
		Assert.assertNotNull(typeToClassifierSwitch);
		Assert.assertNotNull(addClassifierPropertySwitch);
		Assert.assertNotNull(addClassifierOperationSwitch);
		this.typeToClassifier = typeToClassifierSwitch;
		this.addClassifierProperty = addClassifierPropertySwitch;
		this.addClassifierOperation = addClassifierOperationSwitch;
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.bcel.generic.EmptyVisitor#visitFieldOrMethod(org.apache.bcel.generic.FieldOrMethod)
	 */
	@Override
	public void visitFieldOrMethod(FieldOrMethod obj) {
		Assert.assertNotNull(cpg);
		Assert.assertNotNull(typeToClassifier);
		ReferenceType fieldOwner = obj.getReferenceType(cpg);
		owner = (Classifier) typeToClassifier.doSwitch(fieldOwner);
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.bcel.generic.EmptyVisitor#visitGETFIELD(org.apache.bcel.generic.GETFIELD)
	 */
	@Override
	public void visitGETFIELD(GETFIELD obj) {
		//Only classes have instance fields
		Assert.assertTrue(owner instanceof Class);
		addClassifierProperty.setPropertyName(obj.getFieldName(cpg));
		addClassifierProperty.setBCELPropertyType(obj.getFieldType(cpg));
		Property att = (Property) addClassifierProperty.doSwitch(owner);
		if (getInstrContext().conformsTo(owner)) {
			att.setVisibility(VisibilityKind.PROTECTED_LITERAL);
			//fields cannot be redefined in Java, so not possible to check on 'isLeaf'
		}
		att.setIsStatic(false);
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.bcel.generic.EmptyVisitor#visitGETSTATIC(org.apache.bcel.generic.GETSTATIC)
	 */
	@Override
	public void visitGETSTATIC(GETSTATIC obj) {
		//Can be invoked on interfaces as well as classes (static final) -> allow DataType
		Assert.assertTrue((owner instanceof Class) || (owner instanceof Interface) || (owner instanceof DataType));
		addClassifierProperty.setPropertyName(obj.getFieldName(cpg));
		addClassifierProperty.setBCELPropertyType(obj.getFieldType(cpg));
		Property att = (Property) addClassifierProperty.doSwitch(owner);
		if (getInstrContext().conformsTo(owner)) {
			att.setVisibility(VisibilityKind.PROTECTED_LITERAL);
			//fields cannot be redefined in Java, so not possible to check on 'isLeaf'
		}
		att.setIsStatic(true);
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.bcel.generic.EmptyVisitor#visitInvokeInstruction(org.apache.bcel.generic.InvokeInstruction)
	 */
	@Override
	public void visitInvokeInstruction(InvokeInstruction obj) {
		try {
			addClassifierOperation.setOperationName(obj.getMethodName(cpg));
			addClassifierOperation.setBCELArgumentTypes(obj.getArgumentTypes(cpg));
			addClassifierOperation.setBCELReturnType(obj.getReturnType(cpg));
		} catch (JarToUMLException e) {
			setException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.bcel.generic.EmptyVisitor#visitINVOKEINTERFACE(org.apache.bcel.generic.INVOKEINTERFACE)
	 */
	@Override
	public void visitINVOKEINTERFACE(INVOKEINTERFACE obj) {
		//Can be invoked only on interfaces
		Assert.assertTrue(owner instanceof Interface);
		Operation newOp = (Operation) addClassifierOperation.doSwitch(owner);
		Assert.assertNotNull(getInstrContext());
		if (getInstrContext().conformsTo(owner)) {
			newOp.setVisibility(VisibilityKind.PROTECTED_LITERAL);
			EList<Parameter> params = newOp.getOwnedParameters();
			Operation childOp = getInstrContext().getOperation(newOp.getName(), getParameterNames(params), getParameterTypes(params));
			if (childOp != null) {
				newOp.setIsLeaf(false);
			}
		}
		newOp.setIsAbstract(true);
	}

	/**
	 * @param parameters
	 * @return An {@link EList} of the names of each {@link Parameter} in parameters.
	 */
	private EList<String> getParameterNames(EList<Parameter> parameters) {
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
	private EList<Type> getParameterTypes(EList<Parameter> parameters) {
		final EList<Type> types = new BasicEList<Type>();
		for (Parameter par : parameters) {
			types.add(par.getType());
		}
		return types;
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.bcel.generic.EmptyVisitor#visitINVOKESPECIAL(org.apache.bcel.generic.INVOKESPECIAL)
	 */
	@Override
	public void visitINVOKESPECIAL(INVOKESPECIAL obj) {
		//Can be invoked only on classes (<init>, superclass methods and private methods)
		Assert.assertTrue(owner instanceof Class);
		Operation newOp = (Operation) addClassifierOperation.doSwitch(owner);
		Assert.assertNotNull(getInstrContext());
		if (getInstrContext().conformsTo(owner)) {
			newOp.setVisibility(VisibilityKind.PROTECTED_LITERAL);
			EList<Parameter> params = newOp.getOwnedParameters();
			Operation childOp = getInstrContext().getOperation(newOp.getName(), getParameterNames(params), getParameterTypes(params));
			if (childOp != null) {
				newOp.setIsLeaf(false);
			}
		}
		newOp.setIsAbstract(false); //these methods are never abstract
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.bcel.generic.EmptyVisitor#visitINVOKESTATIC(org.apache.bcel.generic.INVOKESTATIC)
	 */
	@Override
	public void visitINVOKESTATIC(INVOKESTATIC obj) {
		//Can be invoked only on classes (interfaces cannot contain static method headers)
		Assert.assertTrue(owner instanceof Class);
		Operation newOp = (Operation) addClassifierOperation.doSwitch(owner);
		Assert.assertNotNull(getInstrContext());
		if (getInstrContext().conformsTo(owner)) {
			newOp.setVisibility(VisibilityKind.PROTECTED_LITERAL);
			EList<Parameter> params = newOp.getOwnedParameters();
			Operation childOp = getInstrContext().getOperation(newOp.getName(), getParameterNames(params), getParameterTypes(params));
			if (childOp != null) {
				newOp.setIsLeaf(false);
			}
		}
		newOp.setIsAbstract(false); //these methods are never abstract
		newOp.setIsStatic(true);
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.bcel.generic.EmptyVisitor#visitINVOKEVIRTUAL(org.apache.bcel.generic.INVOKEVIRTUAL)
	 */
	@Override
	public void visitINVOKEVIRTUAL(INVOKEVIRTUAL obj) {
		//Can be invoked only on classes (refers to all remaining non-interface methods)
		Assert.assertTrue(owner instanceof Class);
		Operation newOp = (Operation) addClassifierOperation.doSwitch(owner);
		Assert.assertNotNull(getInstrContext());
		if (getInstrContext().conformsTo(owner)) {
			newOp.setVisibility(VisibilityKind.PROTECTED_LITERAL);
		}
		newOp.setIsAbstract(true);
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.bcel.generic.EmptyVisitor#visitPUTFIELD(org.apache.bcel.generic.PUTFIELD)
	 */
	@Override
	public void visitPUTFIELD(PUTFIELD obj) {
		//Only classes have instance fields
		Assert.assertTrue(owner instanceof Class);
		addClassifierProperty.setPropertyName(obj.getFieldName(cpg));
		addClassifierProperty.setBCELPropertyType(obj.getFieldType(cpg));
		Property att = (Property) addClassifierProperty.doSwitch(owner);
		Assert.assertNotNull(getInstrContext());
		if (getInstrContext().conformsTo(owner)) {
			att.setVisibility(VisibilityKind.PROTECTED_LITERAL);
			//fields cannot be redefined in Java, so not possible to check on 'isLeaf'
			//even 'final' (isReadOly) fields can be 'put' once
		}
		att.setIsStatic(false);
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.bcel.generic.EmptyVisitor#visitPUTSTATIC(org.apache.bcel.generic.PUTSTATIC)
	 */
	@Override
	public void visitPUTSTATIC(PUTSTATIC obj) {
		//Can be invoked on interfaces as well as classes (static final) -> allow DataType
		Assert.assertTrue((owner instanceof Class) || (owner instanceof Interface) || (owner instanceof DataType));
		addClassifierProperty.setPropertyName(obj.getFieldName(cpg));
		addClassifierProperty.setBCELPropertyType(obj.getFieldType(cpg));
		Property att = (Property) addClassifierProperty.doSwitch(owner);
		Assert.assertNotNull(getInstrContext());
		if (getInstrContext().conformsTo(owner)) {
			att.setVisibility(VisibilityKind.PROTECTED_LITERAL);
			//fields cannot be redefined in Java, so not possible to check on 'isLeaf'
			//even 'final' (isReadOnly) fields can be 'put' once
		}
		att.setIsStatic(true);
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
	 * @return the exception that was thrown, or <code>null</code>.
	 */
	public Exception getException() {
		return exception;
	}

	/**
	 * @param exception the exception to set
	 */
	protected void setException(Exception exception) {
		this.exception = exception;
	}

}
