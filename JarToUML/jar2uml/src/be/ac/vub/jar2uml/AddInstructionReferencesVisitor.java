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

import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.EmptyVisitor;
import org.apache.bcel.generic.FieldOrMethod;
import org.apache.bcel.generic.GETFIELD;
import org.apache.bcel.generic.INVOKEINTERFACE;
import org.apache.bcel.generic.INVOKESPECIAL;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.Instruction;
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

	protected static Logger logger = Logger.getLogger(JarToUML.LOGGER);

	private ConstantPool cp = null;
	private ConstantPoolGen cpg = null;

	protected TypeToClassifierSwitch typeToClassifier = null;
	protected ReplaceByClassifierSwitch replaceByClassifier = new ReplaceByClassifierSwitch();
	protected Classifier owner = null;

	public AddInstructionReferencesVisitor(
			TypeToClassifierSwitch typeToClassifierSwitch) {
		Assert.assertNotNull(typeToClassifierSwitch);
		this.typeToClassifier = typeToClassifierSwitch;
	}

	public void visitFieldOrMethod(FieldOrMethod obj) {
		Assert.assertNotNull(cpg);
		Assert.assertNotNull(typeToClassifier);
		ReferenceType fieldOwner = obj.getReferenceType(cpg);
		owner = (Classifier) typeToClassifier.doSwitch(fieldOwner);
	}

	private void changeOwnerToClass(Instruction obj) {
		if (!(owner instanceof Class)) {
			if (!(owner instanceof DataType)) {
				logger.warning(String.format(
						JarToUML.getString("AddInstructionReferencesVisitor.changingOwnerToClass"), 
						owner.getQualifiedName(),
						owner)); //$NON-NLS-1$
			}
			replaceByClassifier.setMetaClass(UMLPackage.eINSTANCE.getClass_());
			replaceByClassifier.setClassifier(owner);
			owner = (Classifier) replaceByClassifier.doSwitch(owner.getOwner());
		}
		Assert.assertTrue(owner instanceof Class);
	}

	private void changeOwnerToInterface(Instruction obj) {
		if (!(owner instanceof Interface)) {
			if (!(owner instanceof DataType)) {
				logger.warning(String.format(
						JarToUML.getString("AddInstructionReferencesVisitor.changingOwnerToInterface"), 
						owner.getQualifiedName(),
						owner)); //$NON-NLS-1$
			}
			replaceByClassifier.setMetaClass(UMLPackage.eINSTANCE.getInterface());
			replaceByClassifier.setClassifier(owner);
			owner = (Classifier) replaceByClassifier.doSwitch(owner.getOwner());
		}
		Assert.assertTrue(owner instanceof Interface);
	}

	public void visitGETFIELD(GETFIELD obj) {
		//Only classes have instance fields
		changeOwnerToClass(obj);
	}

	public void visitINVOKEINTERFACE(INVOKEINTERFACE obj) {
		//Can be invoked only on interfaces
		changeOwnerToInterface(obj);
	}

	public void visitINVOKESPECIAL(INVOKESPECIAL obj) {
		//Can be invoked only on classes (<init>, superclass methods and private methods)
		changeOwnerToClass(obj);
	}

	public void visitINVOKESTATIC(INVOKESTATIC obj) {
		//Can be invoked only on classes (interfaces cannot contain static method headers)
		changeOwnerToClass(obj);
	}

	public void visitINVOKEVIRTUAL(INVOKEVIRTUAL obj) {
		//Can be invoked only on classes (refers to all remaining non-interface methods)
		changeOwnerToClass(obj);
	}

	public void visitPUTFIELD(PUTFIELD obj) {
		//Only classes have instance fields
		changeOwnerToClass(obj);
	}

	public void visitNEW(NEW obj) {
		Assert.assertNotNull(cpg);
		Assert.assertNotNull(typeToClassifier);
		ObjectType fieldOwner = obj.getLoadClassType(cpg);;
		owner = (Classifier) typeToClassifier.doSwitch(fieldOwner);

		changeOwnerToClass(obj);

		owner.setIsAbstract(false);
	}

	public ConstantPool getCp() {
		return cp;
	}

	public void setCp(ConstantPool cp) {
		this.cp = cp;
		if (cp == null) {
			this.cpg = null;
		} else {
			this.cpg = new ConstantPoolGen(cp);
		}
	}

	public ConstantPoolGen getCpg() {
		return cpg;
	}

}
