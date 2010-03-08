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

import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.DataType;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.util.UMLSwitch;

/**
 * Removes the properties of the switched element.
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public class RemoveClassifierPropertiesSwitch extends UMLSwitch<Classifier> {

	/**
	 * Logs the removal of classifier
	 * @param classifier
	 */
	private void logRemoving(Classifier classifier) {
		JarToUML.logger.finer(String.format(
				JarToUMLResources.getString("RemoveClassifierPropertiesSwitch.removing"), 
				JarToUML.qualifiedName(classifier),
				classifier.eClass().getName())); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.uml2.uml.util.UMLSwitch#caseClassifier(org.eclipse.uml2.uml.Classifier)
	 */
	@Override
	public Classifier caseClassifier(Classifier classifier) {
		logRemoving(classifier);
		classifier.getClientDependencies().clear();
		classifier.getGeneralizations().clear();
		return classifier;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.uml2.uml.util.UMLSwitch#caseClass(org.eclipse.uml2.uml.Class)
	 */
	@Override
	public Classifier caseClass(Class umlClass) {
		caseClassifier(umlClass);
		umlClass.getOwnedAttributes().clear();
		umlClass.getOwnedBehaviors().clear();
		umlClass.getOwnedOperations().clear();
		return umlClass;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.uml2.uml.util.UMLSwitch#caseInterface(org.eclipse.uml2.uml.Interface)
	 */
	@Override
	public Classifier caseInterface(Interface umlIface) {
		caseClassifier(umlIface);
		umlIface.getOwnedAttributes().clear();
		umlIface.getOwnedOperations().clear();
		return umlIface;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.uml2.uml.util.UMLSwitch#caseDataType(org.eclipse.uml2.uml.DataType)
	 */
	@Override
	public Classifier caseDataType(DataType dataType) {
		caseClassifier(dataType);
		dataType.getOwnedAttributes().clear();
		dataType.getOwnedOperations().clear();
		return dataType;
	}

}
