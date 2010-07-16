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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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

	/**
	 * @param forTypes
	 * @return The {@link Type}s directly or indirectly containing any of forTypes.
	 */
	public static Set<Type> findContainerTypes(Collection<Type> forTypes) {
		final Set<Type> containerTypes = new HashSet<Type>();
		for (Type type : forTypes) {
			addContainerTypes(type, containerTypes);
		}
		return containerTypes;
	}

	/**
	 * Adds the {@link Type}s directly or indirectly containing forType to containerTypes.
	 * @param forType
	 * @param containerTypes
	 */
	private static void addContainerTypes(Type forType, Collection<Type> containerTypes) {
		final Element owner = forType.getOwner();
		if (owner instanceof Type) {
			containerTypes.add((Type) owner);
			addContainerTypes((Type) owner, containerTypes); 
		}
	}

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
					JarToUML.logger.finer(String.format(
							JarToUMLResources.getString("FindReferredTypesSwitch.addedDepSupplier"),
							JarToUML.qualifiedName(element),
							JarToUML.getNameList(object.getClients()))); //$NON-NLS-1$
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
			JarToUML.logger.finer(String.format(
					JarToUMLResources.getString("FindReferredTypesSwitch.addedGeneral"),
					JarToUML.qualifiedName(general),
					JarToUML.qualifiedName(object.getSpecific()))); //$NON-NLS-1$
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
			JarToUML.logger.finer(String.format(
					JarToUMLResources.getString("FindReferredTypesSwitch.addedElementType"),
					JarToUML.qualifiedName(type),
					JarToUML.qualifiedName(object))); //$NON-NLS-1$
		}
		return refs;
	}

	/**
	 * @param referredFrom
	 * @return All {@link Type}s referenced from elements in referredFrom.
	 */
	public Set<Type> findAllReferredTypes(Collection<? extends Element> referredFrom) {
		resetReferencedTypes();
		for (Element element : referredFrom) {
			doSwitch(element);
		}
		JarToUML.logger.fine(JarToUMLResources.getString("FindReferredTypesSwitch.foundReferredTypes")); //$NON-NLS-1$
		return getReferencedTypes();
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
