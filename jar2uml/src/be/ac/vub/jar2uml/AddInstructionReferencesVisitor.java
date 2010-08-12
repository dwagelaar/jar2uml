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
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.EmptyVisitor;
import org.apache.bcel.generic.FieldOrMethod;
import org.apache.bcel.generic.GETFIELD;
import org.apache.bcel.generic.INVOKEINTERFACE;
import org.apache.bcel.generic.INVOKESPECIAL;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.NEW;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.PUTFIELD;
import org.apache.bcel.generic.ReferenceType;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.DataType;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.UMLPackage;

/**
 * Adds any classifiers referenced by the switched bytecode instruction and
 * changes it to the right classifier subclass depending on the specific instruction.
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public class AddInstructionReferencesVisitor extends EmptyVisitor {

	private ConstantPool cp = null;
	private ConstantPoolGen cpg = null;

	protected TypeToClassifierSwitch typeToClassifier = null;
	protected ReplaceByClassifierSwitch replaceByClassifier = new ReplaceByClassifierSwitch();
	protected Classifier owner = null;

	/**
	 * Creates a new {@link AddInstructionReferencesVisitor}.
	 * @param typeToClassifierSwitch
	 */
	public AddInstructionReferencesVisitor(
			TypeToClassifierSwitch typeToClassifierSwitch) {
		assert typeToClassifierSwitch != null;
		this.typeToClassifier = typeToClassifierSwitch;
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.bcel.generic.EmptyVisitor#visitFieldOrMethod(org.apache.bcel.generic.FieldOrMethod)
	 */
	@Override
	public void visitFieldOrMethod(FieldOrMethod obj) {
		assert cpg != null;
		assert typeToClassifier != null;
		ReferenceType fieldOwner = obj.getReferenceType(cpg);
		owner = (Classifier) typeToClassifier.doSwitch(fieldOwner);
	}

	/**
	 * Changes {@link #owner} to an instance of {@link Class}.
	 */
	private void changeOwnerToClass() {
		if (!(owner instanceof Class)) {
			if (!(owner instanceof DataType)) {
				JarToUML.logger.fine(String.format(
						JarToUMLResources.getString("AddInstructionReferencesVisitor.changingOwnerToClass"), 
						JarToUML.qualifiedName(owner),
						owner.eClass().getName())); //$NON-NLS-1$
			}
			replaceByClassifier.setMetaClass(UMLPackage.eINSTANCE.getClass_());
			replaceByClassifier.setClassifier(owner);
			owner = (Classifier) replaceByClassifier.doSwitch(owner.getOwner());
		}
		assert owner instanceof Class;
	}

	/**
	 * Changes {@link #owner} to an instance of {@link Interface}.
	 */
	private void changeOwnerToInterface() {
		if (!(owner instanceof Interface)) {
			if (!(owner instanceof DataType)) {
				JarToUML.logger.fine(String.format(
						JarToUMLResources.getString("AddInstructionReferencesVisitor.changingOwnerToInterface"), 
						JarToUML.qualifiedName(owner),
						owner.eClass().getName())); //$NON-NLS-1$
			}
			replaceByClassifier.setMetaClass(UMLPackage.eINSTANCE.getInterface());
			replaceByClassifier.setClassifier(owner);
			owner = (Classifier) replaceByClassifier.doSwitch(owner.getOwner());
		}
		assert owner instanceof Interface;
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.bcel.generic.EmptyVisitor#visitGETFIELD(org.apache.bcel.generic.GETFIELD)
	 */
	@Override
	public void visitGETFIELD(GETFIELD obj) {
		//Only classes have instance fields
		changeOwnerToClass();
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.bcel.generic.EmptyVisitor#visitINVOKEINTERFACE(org.apache.bcel.generic.INVOKEINTERFACE)
	 */
	@Override
	public void visitINVOKEINTERFACE(INVOKEINTERFACE obj) {
		//Can be invoked only on interfaces
		changeOwnerToInterface();
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.bcel.generic.EmptyVisitor#visitINVOKESPECIAL(org.apache.bcel.generic.INVOKESPECIAL)
	 */
	@Override
	public void visitINVOKESPECIAL(INVOKESPECIAL obj) {
		//Can be invoked only on classes (<init>, superclass methods and private methods)
		changeOwnerToClass();
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.bcel.generic.EmptyVisitor#visitINVOKESTATIC(org.apache.bcel.generic.INVOKESTATIC)
	 */
	@Override
	public void visitINVOKESTATIC(INVOKESTATIC obj) {
		//Can be invoked only on classes (interfaces cannot contain static method headers)
		changeOwnerToClass();
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.bcel.generic.EmptyVisitor#visitINVOKEVIRTUAL(org.apache.bcel.generic.INVOKEVIRTUAL)
	 */
	@Override
	public void visitINVOKEVIRTUAL(INVOKEVIRTUAL obj) {
		//Can be invoked only on classes and array types (refers to all remaining non-interface methods)
		if (!TypeToClassifierSwitch.isArrayType(owner)) {
			changeOwnerToClass();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.bcel.generic.EmptyVisitor#visitPUTFIELD(org.apache.bcel.generic.PUTFIELD)
	 */
	@Override
	public void visitPUTFIELD(PUTFIELD obj) {
		//Only classes have instance fields
		changeOwnerToClass();
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.bcel.generic.EmptyVisitor#visitNEW(org.apache.bcel.generic.NEW)
	 */
	@Override
	public void visitNEW(NEW obj) {
		assert cpg != null;
		assert typeToClassifier != null;
		ObjectType fieldOwner = obj.getLoadClassType(cpg);;
		owner = (Classifier) typeToClassifier.doSwitch(fieldOwner);
		changeOwnerToClass();
		owner.setIsAbstract(false);
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

}
