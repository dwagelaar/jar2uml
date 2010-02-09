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

import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.uml2.uml.Classifier;

/**
 * Adds inferred {@link EAnnotation}s to elements that are in {@link #getContainedClassifiers()}.
 * Switch returns <code>false</code> for each inferred element.
 * Use this to mark all contained elements as inferred.
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public class InverseAddInferredTagSwitch extends AddInferredTagSwitch {

	/* (non-Javadoc)
	 * @see be.ac.vub.jar2uml.AddInferredTagSwitch#caseClassifier(org.eclipse.uml2.uml.Classifier)
	 */
	@Override
	public Boolean caseClassifier(Classifier object) {
		return !super.caseClassifier(object);
	}

}
