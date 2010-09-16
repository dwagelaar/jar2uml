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

import java.util.Set;

import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.DataType;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.PackageableElement;
import org.eclipse.uml2.uml.util.UMLSwitch;

/**
 * Adds inferred {@link EAnnotation}s to elements that are not in {@link #getContainedClassifiers()}.
 * Switch returns <code>false</code> for each inferred element.
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public class AddInferredTagSwitch extends UMLSwitch<Boolean> {

	public static final String INFERRED = "inferred"; //$NON-NLS-1$

	private Set<? extends Classifier> containedClassifiers;

	/**
	 * @return the containedClassifiers
	 */
	public Set<? extends Classifier> getContainedClassifiers() {
		return containedClassifiers;
	}

	/**
	 * @param containedClassifiers the containedClassifiers to set
	 */
	public void setContainedClassifiers(Set<? extends Classifier> containedClassifiers) {
		this.containedClassifiers = containedClassifiers;
	}

	/**
	 * Adds a tag to indicate it has been inferred
	 * from class file references.
	 * @param element The element to add the tag to.
	 */
	public static final void addInferredTag(Element element) {
		JarToUML.annotate(element, INFERRED, Boolean.TRUE.toString());
	}

	/**
	 * Removes the tag to indicate it has been inferred
	 * from class file references.
	 * @param element The element to remove the tag from.
	 */
	public static final void removeInferredTag(Element element) {
		JarToUML.deannotate(element, INFERRED);
	}

	/**
	 * @param element
	 * @return <code>true</code> iff this element or one of its containers is marked as inferred
	 */
	public static final boolean isInferred(Element element) {
		final Element owner = element.getOwner();
		final String inferred = JarToUML.getAnnotationValue(element, INFERRED);
		if (Boolean.parseBoolean(inferred)) {
			return true;
		} else if (owner != null) {
			return isInferred(owner);
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.uml2.uml.util.UMLSwitch#casePackage(org.eclipse.uml2.uml.Package)
	 */
	@Override
	public Boolean casePackage(Package object) {
		boolean isContained = false;
		for (PackageableElement element : object.getPackagedElements()) {
			isContained |= doSwitch(element);
		}
		if (!isContained) {
			addInferredTag(object);
			// remove unnecessary tags
			for (PackageableElement element : object.getPackagedElements()) {
				removeInferredTag(element);
			}
		}
		return isContained;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.uml2.uml.util.UMLSwitch#caseClassifier(org.eclipse.uml2.uml.Classifier)
	 */
	@Override
	public Boolean caseClassifier(Classifier object) {
		// Always mark as inferred if array type (name ends with "[]")
		return getContainedClassifiers().contains(object) && (!object.getName().endsWith("[]")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.uml2.uml.util.UMLSwitch#caseClass(org.eclipse.uml2.uml.Class)
	 */
	@Override
	public Boolean caseClass(Class object) {
		boolean isContained = false;
		for (Classifier nested : object.getNestedClassifiers()) {
			isContained |= doSwitch(nested);
		}
		isContained |= caseClassifier(object);
		if (!isContained) {
			addInferredTag(object);
			// remove unnecessary tags
			for (Classifier nested : object.getNestedClassifiers()) {
				removeInferredTag(nested);
			}
		}
		return isContained;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.uml2.uml.util.UMLSwitch#caseDataType(org.eclipse.uml2.uml.DataType)
	 */
	@Override
	public Boolean caseDataType(DataType object) {
		boolean isContained = caseClassifier(object);
		if (!isContained) {
			addInferredTag(object);
		}
		return isContained;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.uml2.uml.util.UMLSwitch#caseInterface(org.eclipse.uml2.uml.Interface)
	 */
	@Override
	public Boolean caseInterface(Interface object) {
		boolean isContained = false;
		for (Classifier nested : object.getNestedClassifiers()) {
			isContained |= doSwitch(nested);
		}
		isContained |= caseClassifier(object);
		if (!isContained) {
			addInferredTag(object);
			// remove unnecessary tags
			for (Classifier nested : object.getNestedClassifiers()) {
				removeInferredTag(nested);
			}
		}
		return isContained;
	}

}
