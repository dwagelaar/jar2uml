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

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.DataType;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.util.UMLSwitch;

/**
 * Replaces {@link #setClassifier(Classifier)} by a new {@link Interface}.
 * Switches on {@link Element#getOwner()} of {@link #getClassifier()}
 * and returns new {@link Interface}.
 * Copies {@link #getClassifier()} attributes, operations and nested classifiers.
 * @author dennis
 *
 */
public class ReplaceByClassifierSwitch extends UMLSwitch<Classifier> {

	/**
	 * Retrieves and stores all nested elements from the switched classifier. 
	 */
	public class PreSwitch extends UMLSwitch<Classifier> {
		public Classifier caseClass(Class umlClass) {
			nested = umlClass.getNestedClassifiers();
			atts = umlClass.getOwnedAttributes();
			ops = umlClass.getOwnedOperations();
			isAbstract = umlClass.isAbstract();
			isLeaf = umlClass.isLeaf();
			return umlClass;
		}

		public Classifier caseInterface(Interface umlIface) {
			nested = umlIface.getNestedClassifiers();
			atts = umlIface.getOwnedAttributes();
			ops = umlIface.getOwnedOperations();
			isAbstract = umlIface.isAbstract();
			isLeaf = umlIface.isLeaf();
			return umlIface;
		}

		public Classifier caseDataType(DataType dataType) {
			nested = null;
			atts = dataType.getOwnedAttributes();
			ops = dataType.getOwnedOperations();
			isAbstract = dataType.isAbstract();
			isLeaf = dataType.isLeaf();
			return dataType;
		}

		public Classifier caseClassifier(Classifier classifier) {
			nested = null;
			atts = null;
			ops = null;
			isAbstract = classifier.isAbstract();
			isLeaf = classifier.isLeaf();
			return classifier;
		}
	}

	/**
	 * Adds all prepared nested elements to the switched classifier. 
	 */
	public class PostSwitch extends UMLSwitch<Classifier> {
		public Classifier caseClass(Class umlClass) {
			if (nested != null) {
				umlClass.getNestedClassifiers().addAll(nested);
			}
			if (atts != null) {
				umlClass.getOwnedAttributes().addAll(atts);
			}
			if (ops != null) {
				umlClass.getOwnedOperations().addAll(ops);
			}
			return super.caseClass(umlClass);
		}

		public Classifier caseInterface(Interface umlIface) {
			if (nested != null) {
				umlIface.getNestedClassifiers().addAll(nested);
			}
			if (atts != null) {
				umlIface.getOwnedAttributes().addAll(atts);
			}
			if (ops != null) {
				umlIface.getOwnedOperations().addAll(ops);
			}
			return super.caseInterface(umlIface);
		}

		public Classifier caseDataType(DataType dataType) {
			if (atts != null) {
				dataType.getOwnedAttributes().addAll(atts);
			}
			if (ops != null) {
				dataType.getOwnedOperations().addAll(ops);
			}
			return super.caseDataType(dataType);
		}

		public Classifier caseClassifier(Classifier classifier) {
			classifier.setIsAbstract(isAbstract);
			return classifier;
		}
	}

	private Classifier classifier = null;
	private EClass metaClass = UMLPackage.eINSTANCE.getDataType();

	protected EList<Classifier> nested = null;
	protected EList<Property> atts = null;
	protected EList<Operation> ops = null;
	protected boolean isAbstract = false;
	protected boolean isLeaf = false;
	protected PreSwitch preSwitch = new PreSwitch();
	protected PostSwitch postSwitch = new PostSwitch();

	/**
	 * Logs the replacement of classifier.
	 * @param classifier
	 */
	private void logReplace(Classifier classifier) {
		JarToUML.logger.finer(String.format(
				JarToUMLResources.getString("ReplaceByClassifierSwitch.replacing"), 
				JarToUML.qualifiedName(classifier),
				classifier.eClass().getName(),
				getMetaClass().getName())); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.uml2.uml.util.UMLSwitch#caseClass(org.eclipse.uml2.uml.Class)
	 */
	@Override
	public Classifier caseClass(Class umlClass) {
		Classifier classifier = preSwitch.doSwitch(getClassifier());
		logReplace(classifier);
		umlClass.getNestedClassifiers().remove(classifier);
		classifier = umlClass.createNestedClassifier(classifier.getName(), getMetaClass());
		return postSwitch.doSwitch(classifier);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.uml2.uml.util.UMLSwitch#caseInterface(org.eclipse.uml2.uml.Interface)
	 */
	@Override
	public Classifier caseInterface(Interface umlIface) {
		Classifier classifier = preSwitch.doSwitch(getClassifier());
		logReplace(classifier);
		umlIface.getNestedClassifiers().remove(classifier);
		classifier = umlIface.createNestedClassifier(classifier.getName(), getMetaClass());
		return postSwitch.doSwitch(classifier);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.uml2.uml.util.UMLSwitch#casePackage(org.eclipse.uml2.uml.Package)
	 */
	@Override
	public Classifier casePackage(Package pack) {
		Classifier classifier = preSwitch.doSwitch(getClassifier());
		logReplace(classifier);
		pack.getPackagedElements().remove(classifier);
		classifier = (Classifier) pack.createPackagedElement(classifier.getName(), getMetaClass());
		return postSwitch.doSwitch(classifier);
	}

	/**
	 * Sets the classifier.
	 * @param classifier
	 */
	public void setClassifier(Classifier classifier) {
		this.classifier = classifier;
	}

	/**
	 * @return The classifier.
	 */
	public Classifier getClassifier() {
		return classifier;
	}

	/**
	 * @return The meta-class.
	 */
	public EClass getMetaClass() {
		return metaClass;
	}

	/**
	 * Sets the meta-class.
	 * @param metaClass
	 */
	public void setMetaClass(EClass metaClass) {
		Assert.assertNotNull(metaClass);
		Assert.assertEquals(true, UMLPackage.eINSTANCE.getClassifier().isSuperTypeOf(metaClass));
		this.metaClass = metaClass;
	}

}
