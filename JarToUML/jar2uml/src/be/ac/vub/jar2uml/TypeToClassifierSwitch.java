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

import org.apache.bcel.generic.ArrayType;
import org.apache.bcel.generic.BasicType;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.verifier.structurals.UninitializedObjectType;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.DataType;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.UMLPackage;

/**
 * Returns the corresponding UML type for a given BCEL type. 
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public class TypeToClassifierSwitch extends TypeSwitch<Classifier> {

	/**
	 * @param classifier
	 * @return <code>true</code> iff classifier represents a Java {@link ArrayType}.
	 */
	public static final boolean isArrayType(Classifier classifier) {
		return (classifier instanceof DataType) && classifier.getName().endsWith("[]");
	}

	private Package root = null;
	private FindContainedClassifierSwitch findContainedClassifier = new FindContainedClassifierSwitch();

	/*
	 * (non-Javadoc)
	 * @see be.ac.vub.jar2uml.TypeSwitch#caseArrayType(org.apache.bcel.generic.ArrayType)
	 */
	@Override
	public Classifier caseArrayType(ArrayType type) {
		final Classifier inner = doSwitch(type.getElementType());
		assert inner != null;
		return findContainedClassifier.findLocalClassifier(
				inner.getOwner(), 
				inner.getName() + "[]", 
				UMLPackage.eINSTANCE.getDataType());
	}

	/*
	 * (non-Javadoc)
	 * @see be.ac.vub.jar2uml.TypeSwitch#caseBasicType(org.apache.bcel.generic.BasicType)
	 */
	@Override
	public Classifier caseBasicType(BasicType type) {
		final Package root = getRoot();
		assert root != null;
		if (BasicType.BOOLEAN.equals(type)) {
			return findContainedClassifier.findPrimitiveType(root, "java.lang.boolean", true); //$NON-NLS-1$
		} else if (BasicType.BYTE.equals(type)) {
			return findContainedClassifier.findPrimitiveType(root, "java.lang.byte", true); //$NON-NLS-1$
		} else if (BasicType.CHAR.equals(type)) {
			return findContainedClassifier.findPrimitiveType(root, "java.lang.char", true); //$NON-NLS-1$
		} else if (BasicType.DOUBLE.equals(type)) {
			return findContainedClassifier.findPrimitiveType(root, "java.lang.double", true); //$NON-NLS-1$
		} else if (BasicType.FLOAT.equals(type)) {
			return findContainedClassifier.findPrimitiveType(root, "java.lang.float", true); //$NON-NLS-1$
		} else if (BasicType.INT.equals(type)) {
			return findContainedClassifier.findPrimitiveType(root, "java.lang.int", true); //$NON-NLS-1$
		} else if (BasicType.LONG.equals(type)) {
			return findContainedClassifier.findPrimitiveType(root, "java.lang.long", true); //$NON-NLS-1$
		} else if (BasicType.SHORT.equals(type)) {
			return findContainedClassifier.findPrimitiveType(root, "java.lang.short", true); //$NON-NLS-1$
		} else if (BasicType.VOID.equals(type)) {
			return null; //$NON-NLS-1$
		} else {
			throw new IllegalArgumentException(String.format(
					JarToUMLResources.getString("TypeToClassifierSwitch.unsupportedType"), 
					type.getSignature())); //$NON-NLS-1$
		}
	}

	/*
	 * (non-Javadoc)
	 * @see be.ac.vub.jar2uml.TypeSwitch#caseObjectType(org.apache.bcel.generic.ObjectType)
	 */
	@Override
	public Classifier caseObjectType(ObjectType type) {
		final Package root = getRoot();
		assert root != null;
		return findContainedClassifier.findClassifier(root, type.getClassName(), UMLPackage.eINSTANCE.getDataType());
	}

	/*
	 * (non-Javadoc)
	 * @see be.ac.vub.jar2uml.TypeSwitch#caseUninitializedObjectType(org.apache.bcel.verifier.structurals.UninitializedObjectType)
	 */
	@Override
	public Classifier caseUninitializedObjectType(UninitializedObjectType type) {
		//Special type for constructor methods; changes into object type after construction
		return doSwitch(type.getInitialized());
	}

	/**
	 * @return The root {@link Package} under which to find {@link Classifier}s.
	 */
	public Package getRoot() {
		return root;
	}

	/**
	 * Sets the root {@link Package} under which to find {@link Classifier}s.
	 * @param root
	 */
	public void setRoot(Package root) {
		this.root = root;
	}

}
