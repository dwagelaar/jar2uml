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

import junit.framework.Assert;

import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.util.UMLSwitch;

/**
 * Removes {@link #getClassifier()} from its parent.
 * Switches on {@link Element#getOwner()} of {@link #getClassifier()}
 * and returns itself. Also removes derived datatypes (arrays), nested
 * classifiers and container packages if they become empty.
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public class RemoveClassifierSwitch extends UMLSwitch<Classifier> {

	/**
	 * Removes nested classifiers of {@link #doSwitch(org.eclipse.emf.ecore.EObject)}.
	 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
	 */
	public class RemoveNestedClassifierSwitch extends UMLSwitch<Classifier> {

		public Classifier caseClass(Class umlClass) {
			for (Iterator<Classifier> it = umlClass.getNestedClassifiers().iterator(); it.hasNext();) {
				Classifier c = it.next();
				doSwitch(c);
				it.remove();
			}
			return umlClass;
		}

		public Classifier caseInterface(Interface umlIface) {
			for (Iterator<Classifier> it = umlIface.getNestedClassifiers().iterator(); it.hasNext();) {
				Classifier c = it.next();
				doSwitch(c);
				it.remove();
			}
			return umlIface;
		}
	}

	private Classifier classifier = null;

	protected RemoveNestedClassifierSwitch removeNested = new RemoveNestedClassifierSwitch();

	/**
	 * Logs the removal of classifier
	 * @param classifier
	 */
	private void logRemoving(Classifier classifier) {
		JarToUML.logger.finer(String.format(
				JarToUMLResources.getString("RemoveClassifierSwitch.removing"), 
				JarToUML.qualifiedName(classifier),
				classifier.eClass().getName())); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.uml2.uml.util.UMLSwitch#caseClass(org.eclipse.uml2.uml.Class)
	 */
	@Override
	public Classifier caseClass(Class umlClass) {
		final Classifier classifier = getClassifier();
		Assert.assertNotNull(classifier);
		logRemoving(classifier);
		removeNested.doSwitch(classifier);
		umlClass.getNestedClassifiers().remove(classifier);
		return classifier;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.uml2.uml.util.UMLSwitch#caseInterface(org.eclipse.uml2.uml.Interface)
	 */
	@Override
	public Classifier caseInterface(Interface umlIface) {
		final Classifier classifier = getClassifier();
		Assert.assertNotNull(classifier);
		logRemoving(classifier);
		removeNested.doSwitch(classifier);
		umlIface.getNestedClassifiers().remove(classifier);
		return classifier;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.uml2.uml.util.UMLSwitch#casePackage(org.eclipse.uml2.uml.Package)
	 */
	@Override
	public Classifier casePackage(Package pack) {
		final Classifier classifier = getClassifier();
		Assert.assertNotNull(classifier);
		logRemoving(classifier);
		removeNested.doSwitch(classifier);
		pack.getPackagedElements().remove(classifier);
		return classifier;
	}

	/**
	 * Sets the classifier
	 * @param classifier
	 */
	public void setClassifier(Classifier classifier) {
		this.classifier = classifier;
	}

	/**
	 * @return the classifier
	 */
	public Classifier getClassifier() {
		return classifier;
	}

}
