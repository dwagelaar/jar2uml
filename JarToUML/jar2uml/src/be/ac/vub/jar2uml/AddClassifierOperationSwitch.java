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

import java.util.logging.Logger;

import junit.framework.Assert;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.DataType;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.ParameterDirectionKind;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.util.UMLSwitch;

/**
 * Adds an operation to the switched element (class, interface or datatype).
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public class AddClassifierOperationSwitch extends UMLSwitch<Operation> {

	protected static Logger logger = Logger.getLogger(JarToUML.LOGGER);

	private String operationName = null;
	private EList<Type> argumentTypes = null;
	private Type returnType = null;
	private TypeToClassifierSwitch typeToClassifier = null;

	protected EList<String> argNames = null;
	protected EList<String> allArgNames = null;
	protected EList<Type> allArgTypes = null;

	/**
	 * Creates a new {@link AddClassifierOperationSwitch}.
	 * @param typeToClassifier
	 */
	public AddClassifierOperationSwitch(TypeToClassifierSwitch typeToClassifier) {
		Assert.assertNotNull(typeToClassifier);
		this.typeToClassifier = typeToClassifier;
	}

	/**
	 * @return The {@link EList} of argument {@link Type}s for the {@link Operation} to create.
	 */
	public EList<Type> getArgumentTypes() {
		return argumentTypes;
	}

	/**
	 * Sets the {@link EList} of argument {@link Type}s for the {@link Operation} to create.
	 * @param argumentTypes
	 */
	public void setArgumentTypes(EList<Type> argumentTypes) {
		this.argumentTypes = argumentTypes;
	}

	/**
	 * Sets the array of argument {@link org.apache.bcel.generic.Type}s for the {@link Operation} to create.
	 * @param argumentTypes
	 * @throws JarToUMLException If one or more types cannot be found.
	 */
	public void setBCELArgumentTypes(org.apache.bcel.generic.Type[] argumentTypes) throws JarToUMLException {
		setArgumentTypes(toUMLTypes(argumentTypes));
	}

	/**
	 * Converts an array of {@link org.apache.bcel.generic.Type}s to an {@link EList} of {@link Type}s.
	 * @param types
	 * @return an {@link EList} of {@link Type}s.
	 * @throws JarToUMLException If one or more types cannot be found.
	 */
	private EList<Type> toUMLTypes(org.apache.bcel.generic.Type[] types) throws JarToUMLException {
		EList<Type> umlTypes = new BasicEList<Type>();
		for (int i = 0; i < types.length; i++) {
			Type type = typeToClassifier.doSwitch(types[i]);
			if (type == null) {
				throw new JarToUMLException(String.format(
						JarToUMLResources.getString("AddClassifierOperationSwitch.typeNotFound"), 
						types[i].getSignature())); //$NON-NLS-1$
			}
			umlTypes.add(type);
		}
		return umlTypes;
	}

	/**
	 * @return The name of the {@link Operation} to create.
	 */
	public String getOperationName() {
		return operationName;
	}

	/**
	 * Sets the name of the {@link Operation} to create.
	 * @param operationName
	 */
	public void setOperationName(String operationName) {
		this.operationName = operationName;
	}

	/**
	 * Prepares {@link #argNames}, {@link #allArgNames}, and {@link #allArgTypes} for creation of {@link Operation}.
	 */
	protected void prepareArgs() {
		EList<Type> args = getArgumentTypes();
		Assert.assertNotNull(args);
		argNames = toUMLArgNames(args);
		allArgNames = new BasicEList<String>(argNames);
		allArgTypes = new BasicEList<Type>(args);
		if (getReturnType() != null) {
			allArgNames.add("return");
			allArgTypes.add(getReturnType());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.uml2.uml.util.UMLSwitch#caseClass(org.eclipse.uml2.uml.Class)
	 */
	@Override
	public Operation caseClass(Class object) {
		String name = getOperationName();
		Assert.assertNotNull(name);
		prepareArgs();
		// names may not be null in getOwnedOperation, but return parameter name is always null?!
		Operation op = object.getOwnedOperation(name, allArgNames, allArgTypes);
		if (op == null) {
			op = object.createOwnedOperation(name, argNames, getArgumentTypes());
			if (getReturnType() != null) {
				Parameter par = op.createOwnedParameter("return", getReturnType()); //$NON-NLS-1$
				par.setDirection(ParameterDirectionKind.RETURN_LITERAL);
			}
			op.setIsLeaf(true);		//final
			op.setIsAbstract(true); //abstract
		}
		return op;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.uml2.uml.util.UMLSwitch#caseInterface(org.eclipse.uml2.uml.Interface)
	 */
	@Override
	public Operation caseInterface(Interface object) {
		String name = getOperationName();
		Assert.assertNotNull(name);
		prepareArgs();
		Operation op = object.getOwnedOperation(name, allArgNames, allArgTypes);
		if (op == null) {
			op = object.createOwnedOperation(name, argNames, getArgumentTypes());
			if (getReturnType() != null) {
				Parameter par = op.createOwnedParameter("return", getReturnType()); //$NON-NLS-1$
				par.setDirection(ParameterDirectionKind.RETURN_LITERAL);
			}
			op.setIsLeaf(true);		//final
			op.setIsAbstract(true); //abstract
		}
		return op;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.uml2.uml.util.UMLSwitch#caseDataType(org.eclipse.uml2.uml.DataType)
	 */
	@Override
	public Operation caseDataType(DataType object) {
		String name = getOperationName();
		Assert.assertNotNull(name);
		prepareArgs();
		Operation op = object.getOwnedOperation(name, allArgNames, allArgTypes);
		if (op == null) {
			op = object.createOwnedOperation(name, argNames, getArgumentTypes());
			if (getReturnType() != null) {
				Parameter par = op.createOwnedParameter("return", getReturnType()); //$NON-NLS-1$
				par.setDirection(ParameterDirectionKind.RETURN_LITERAL);
			}
			op.setIsLeaf(true);		//final
			op.setIsAbstract(true); //abstract
		}
		return op;
	}

	/**
	 * @param args
	 * @return A list of generated argument names, the size of args.
	 */
	private static EList<String> toUMLArgNames(EList<?> args) {
		EList<String> umlArgNames = new BasicEList<String>();
		for (int i = 0; i < args.size(); i++) {
			umlArgNames.add("arg" + i);
		}
		return umlArgNames;
	}

	/**
	 * @return The return {@link Type} for the {@link Operation} to create.
	 */
	public Type getReturnType() {
		return returnType;
	}

	/**
	 * Sets the return {@link Type} for the {@link Operation} to create.
	 * @param returnType
	 */
	public void setReturnType(Type returnType) {
		this.returnType = returnType;
	}

	/**
	 * Sets the return {@link org.apache.bcel.generic.Type} for the {@link Operation} to create.
	 * @param returnType
	 */
	public void setBCELReturnType(org.apache.bcel.generic.Type returnType) {
		setReturnType((Type) typeToClassifier.doSwitch(returnType));
	}

}
