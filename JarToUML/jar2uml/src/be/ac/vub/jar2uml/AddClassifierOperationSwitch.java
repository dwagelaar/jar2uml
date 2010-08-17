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

import java.util.Iterator;

import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.classfile.LocalVariableTable;
import org.apache.bcel.classfile.Method;
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

	/**
	 * @param args
	 * @return A list of generated argument names, the size of args.
	 */
	public static EList<String> toUMLArgNames(EList<?> args) {
		final EList<String> umlArgNames = new BasicEList<String>();
		for (int i = 0; i < args.size(); i++) {
			umlArgNames.add("arg" + i);
		}
		return umlArgNames;
	}

	/**
	 * @param m
	 * @return the argument names of m, if available, otherwise <code>null</code>
	 */
	public static EList<String> getArgumentNames(Method m) {
		final LocalVariableTable lvt = m.getLocalVariableTable();
		if (lvt == null) {
			return null;
		}
		final EList<String> argNames = new BasicEList<String>();
		final org.apache.bcel.generic.Type[] argTypes = m.getArgumentTypes();
		int offset = m.isStatic() ? 0 : 1;
		int argIndex = 0;
		for (org.apache.bcel.generic.Type argType : argTypes) {
			LocalVariable lv = lvt.getLocalVariable(offset, 0);
			if (lv == null) {
				argNames.add("arg" + argIndex);
			} else {
				argNames.add(lv.getName());
				assert org.apache.bcel.generic.Type.getType(lv.getSignature()).equals(argType);
			}
			offset += argType.getSize();
			argIndex++;
		}
		return argNames;
	}

	/**
	 * @param operations the operations to select from
	 * @param name the operation name
	 * @param argumentTypes the argument types
	 * @param returnType the return type or <code>null</code>
	 * @return the first operation from operations with the given name, argument types and return type, or <code>null</code>
	 */
	public static Operation getOperation(EList<Operation> operations, String name, EList<Type> argumentTypes, Type returnType) {
		for (Operation op : operations) {
			if (op.getType() != returnType) {
				continue;
			}
			if (!name.equals(op.getName())) {
				continue;
			}
			if (compareParameterTypes(op.getOwnedParameters(), argumentTypes)) {
				return op;
			}
		}
		return null;
	}

	/**
	 * Compares the types of pars to the given types. Return parameters are ignored.
	 * @param pars
	 * @param types
	 * @return <code>true</code> iff all types match
	 */
	public static boolean compareParameterTypes(final EList<Parameter> pars, final EList<Type> types) {
		if (pars.size() < types.size()) {
			return false;
		}
		final Iterator<Type> t = types.iterator();
		final Iterator<Parameter> p = pars.iterator();
		while (p.hasNext()) {
			Parameter par = p.next();
			if (par.getDirection() != ParameterDirectionKind.RETURN_LITERAL) {
				if (!t.hasNext() || t.next() != par.getType()) {
					return false;
				}
			}
		}
		if (t.hasNext()) {
			return false;
		}
		return true;
	}

	private String operationName = null;
	private EList<String> argumentNames = null;
	private EList<Type> argumentTypes = null;
	private Type returnType = null;
	private TypeToClassifierSwitch typeToClassifier = null;

	/**
	 * Creates a new {@link AddClassifierOperationSwitch}.
	 * @param typeToClassifier
	 */
	public AddClassifierOperationSwitch(TypeToClassifierSwitch typeToClassifier) {
		assert typeToClassifier != null;
		this.typeToClassifier = typeToClassifier;
	}

	/**
	 * Sets all input data from method.
	 * @param method
	 * @throws JarToUMLException if a type cannot be found
	 */
	public void setAll(Method method) throws JarToUMLException {
		setOperationName(method.getName());
		setArgumentNames(getArgumentNames(method));
		setBCELArgumentTypes(method.getArgumentTypes());
		setBCELReturnType(method.getReturnType());
	}

	/**
	 * @param argumentNames the argumentNames to set
	 */
	public void setArgumentNames(EList<String> argumentNames) {
		this.argumentNames = argumentNames;
	}

	/**
	 * @return the argument names, either set or generated
	 */
	public EList<String> getArgumentNames() {
		if (argumentNames == null) {
			return toUMLArgNames(getArgumentTypes());
		}
		return argumentNames;
	}

	/**
	 * @return <code>true</code> iff the argument names are set
	 */
	public boolean isArgumentNamesSet() {
		return argumentNames != null;
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
	protected EList<Type> toUMLTypes(org.apache.bcel.generic.Type[] types) throws JarToUMLException {
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
	 * Updates the parameter names of op with the set parameter names.
	 * Asserts the amount of set parameter names to match the amount of non-return op parameters.
	 * @param op
	 */
	protected void updateOperationParameterNames(final Operation op) {
		final Iterator<Parameter> p = op.getOwnedParameters().iterator();
		final Iterator<String> n = getArgumentNames().iterator();
		while (p.hasNext()) {
			Parameter par = p.next();
			if (par.getDirection() == ParameterDirectionKind.RETURN_LITERAL) {
				continue;
			}
			assert n.hasNext();
			par.setName(n.next());
		}
		assert !n.hasNext();
	}

	@Override
	public Operation caseClass(Class object) {
		final String name = getOperationName();
		assert name != null;
		Operation op = getOperation(object.getOwnedOperations(), name, getArgumentTypes(), getReturnType());
		if (op == null) {
			op = object.createOwnedOperation(name, getArgumentNames(), getArgumentTypes());
			if (getReturnType() != null) {
				Parameter par = op.createOwnedParameter("return", getReturnType()); //$NON-NLS-1$
				par.setDirection(ParameterDirectionKind.RETURN_LITERAL);
			}
			op.setIsLeaf(true);		//final
			op.setIsAbstract(true); //abstract
		} else if (isArgumentNamesSet()) {
			updateOperationParameterNames(op);
		}
		return op;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.uml2.uml.util.UMLSwitch#caseInterface(org.eclipse.uml2.uml.Interface)
	 */
	@Override
	public Operation caseInterface(Interface object) {
		final String name = getOperationName();
		assert name != null;
		Operation op = getOperation(object.getOwnedOperations(), name, getArgumentTypes(), getReturnType());
		if (op == null) {
			op = object.createOwnedOperation(name, getArgumentNames(), getArgumentTypes());
			if (getReturnType() != null) {
				Parameter par = op.createOwnedParameter("return", getReturnType()); //$NON-NLS-1$
				par.setDirection(ParameterDirectionKind.RETURN_LITERAL);
			}
			op.setIsLeaf(true);		//final
			op.setIsAbstract(true); //abstract
		} else if (isArgumentNamesSet()) {
			updateOperationParameterNames(op);
		}
		return op;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.uml2.uml.util.UMLSwitch#caseDataType(org.eclipse.uml2.uml.DataType)
	 */
	@Override
	public Operation caseDataType(DataType object) {
		final String name = getOperationName();
		assert name != null;
		Operation op = getOperation(object.getOwnedOperations(), name, getArgumentTypes(), getReturnType());
		if (op == null) {
			op = object.createOwnedOperation(name, getArgumentNames(), getArgumentTypes());
			if (getReturnType() != null) {
				Parameter par = op.createOwnedParameter("return", getReturnType()); //$NON-NLS-1$
				par.setDirection(ParameterDirectionKind.RETURN_LITERAL);
			}
			op.setIsLeaf(true);		//final
			op.setIsAbstract(true); //abstract
		} else if (isArgumentNamesSet()) {
			updateOperationParameterNames(op);
		}
		return op;
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
		setReturnType(typeToClassifier.doSwitch(returnType));
	}

}
