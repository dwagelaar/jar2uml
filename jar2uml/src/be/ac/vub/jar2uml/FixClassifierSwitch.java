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

import org.apache.bcel.classfile.JavaClass;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.DataType;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.util.UMLSwitch;

/**
 * {@link #doSwitch(org.eclipse.emf.ecore.EObject)} fixes the switched {@link Classifier}
 * to be of the right class, given {@link #getJavaClass()}.
 * Also initialises the new {@link Classifier} attributes.
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public class FixClassifierSwitch extends UMLSwitch<Classifier> {

	protected static Logger logger = Logger.getLogger(JarToUML.LOGGER);

	private JavaClass javaClass = null;
	private ReplaceByClassifierSwitch replaceByClassifierSwitch = new ReplaceByClassifierSwitch();

	public JavaClass getJavaClass() {
		return javaClass;
	}

	public void setJavaClass(JavaClass javaClass) {
		this.javaClass = javaClass;
	}

	public Classifier caseClass(Class umlClass) {
		Assert.assertNotNull(umlClass);
		JavaClass javaClass = getJavaClass();
		Assert.assertNotNull(javaClass);
		if (javaClass.isInterface()) {
			replaceByClassifierSwitch.setMetaClass(UMLPackage.eINSTANCE.getInterface());
			replaceByClassifierSwitch.setClassifier(umlClass);
			Interface umlIface = (Interface) replaceByClassifierSwitch.doSwitch(umlClass.getOwner());
			umlIface.getNestedClassifiers().addAll(umlClass.getNestedClassifiers());
			return doSwitch(umlIface);
		}
		return super.caseClass(umlClass);
	}

	public Classifier caseInterface(Interface umlIface) {
		Assert.assertNotNull(umlIface);
		JavaClass javaClass = getJavaClass();
		Assert.assertNotNull(javaClass);
		if (!javaClass.isInterface()) {
			replaceByClassifierSwitch.setMetaClass(UMLPackage.eINSTANCE.getClass_());
			replaceByClassifierSwitch.setClassifier(umlIface);
			Class umlClass = (Class) replaceByClassifierSwitch.doSwitch(umlIface.getOwner());
			umlClass.getNestedClassifiers().addAll(umlIface.getNestedClassifiers());
			return doSwitch(umlClass);
		}
		return super.caseInterface(umlIface);
	}

	public Classifier caseDataType(DataType datatype) {
		Assert.assertNotNull(datatype);
		JavaClass javaClass = getJavaClass();
		Assert.assertNotNull(javaClass);
		if (javaClass.isInterface()) {
			replaceByClassifierSwitch.setMetaClass(UMLPackage.eINSTANCE.getInterface());
			replaceByClassifierSwitch.setClassifier(datatype);
			Interface umlIface = (Interface) replaceByClassifierSwitch.doSwitch(datatype.getOwner());
			return doSwitch(umlIface);
		} else {
			replaceByClassifierSwitch.setMetaClass(UMLPackage.eINSTANCE.getClass_());
			replaceByClassifierSwitch.setClassifier(datatype);
			Class umlClass = (Class) replaceByClassifierSwitch.doSwitch(datatype.getOwner());
			return doSwitch(umlClass);
		}
	}

	public Classifier caseClassifier(Classifier classifier) {
		Assert.assertNotNull(classifier);
		JavaClass javaClass = getJavaClass();
		Assert.assertNotNull(javaClass);
		classifier.setIsAbstract(javaClass.isAbstract());
		classifier.setVisibility(JarToUML.toUMLVisibility(javaClass));
		classifier.setIsLeaf(javaClass.isFinal());
		return classifier;
	}

}