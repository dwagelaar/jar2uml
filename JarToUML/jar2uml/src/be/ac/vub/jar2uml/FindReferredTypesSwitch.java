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

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Dependency;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Generalization;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.TypedElement;
import org.eclipse.uml2.uml.util.UMLSwitch;

/**
 * Finds all {@link Type}s referred to
 * by the switched {@link Element} and returns them in a {@link Set}.
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public class FindReferredTypesSwitch extends UMLSwitch<Set<Type>> {

	protected static Logger logger = Logger.getLogger(JarToUML.LOGGER);

	private Set<Type> referencedTypes = new HashSet<Type>();

	/* (non-Javadoc)
	 * @see org.eclipse.uml2.uml.util.UMLSwitch#caseElement(org.eclipse.uml2.uml.Element)
	 */
	@Override
	public Set<Type> caseElement(Element object) {
		for (Element element : object.getOwnedElements()) {
			doSwitch(element);
		}
		return getReferencedTypes();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.uml2.uml.util.UMLSwitch#caseDependency(org.eclipse.uml2.uml.Dependency)
	 */
	@Override
	public Set<Type> caseDependency(Dependency object) {
		final Set<Type> refs = getReferencedTypes();
		for (NamedElement element : object.getSuppliers()) {
			if (element instanceof Type) {
				if (refs.add((Type) element)) {
					logger.finer(String.format(
							JarToUML.getString("FindReferredTypesSwitch.addedDepSupplier"),
							JarToUML.qualifiedName(element),
							JarToUML.getNameList(object.getClients()))); //$NON-NLS-1$
//					doSwitch(element); // transitive closure
				}
			}
		}
		return refs;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.uml2.uml.util.UMLSwitch#caseGeneralization(org.eclipse.uml2.uml.Generalization)
	 */
	@Override
	public Set<Type> caseGeneralization(Generalization object) {
		final Set<Type> refs = getReferencedTypes();
		final Classifier general = object.getGeneral();
		if (refs.add(general)) {
			logger.finer(String.format(
					JarToUML.getString("FindReferredTypesSwitch.addedGeneral"),
					JarToUML.qualifiedName(general),
					JarToUML.qualifiedName(object.getSpecific()))); //$NON-NLS-1$
//			doSwitch(general); // transitive closure
		}
		return refs;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.uml2.uml.util.UMLSwitch#caseTypedElement(org.eclipse.uml2.uml.TypedElement)
	 */
	@Override
	public Set<Type> caseTypedElement(TypedElement object) {
		final Set<Type> refs = getReferencedTypes();
		final Type type = object.getType();
		if (refs.add(type)) {
			logger.finer(String.format(
					JarToUML.getString("FindReferredTypesSwitch.addedElementType"),
					JarToUML.qualifiedName(type),
					JarToUML.qualifiedName(object))); //$NON-NLS-1$
//			doSwitch(type); // transitive closure
		}
		return refs;
	}

	/**
	 * @return the referencedTypes
	 */
	public Set<Type> getReferencedTypes() {
		return referencedTypes;
	}

	/**
	 * Resets the referenced types to an empty set.
	 */
	public void resetReferencedTypes() {
		getReferencedTypes().clear();
	}

}
