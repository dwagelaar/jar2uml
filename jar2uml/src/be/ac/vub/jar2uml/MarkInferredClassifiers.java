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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.bcel.classfile.JavaClass;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Package;

/**
 * Finds and marks inferred {@link Classifier}s in the {@link Model}.
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public class MarkInferredClassifiers extends ChangeModel {

	/**
	 * @param classifier
	 * @return All classifiers that are derivatives (i.e. array types) of classifier.
	 */
	public static Collection<Classifier> findDerivedClassifiers(Classifier classifier) {
		assert classifier != null;
		final String name = classifier.getName();
		assert name != null;
		final List<Classifier> derived = new ArrayList<Classifier>();
		final Element owner = classifier.getOwner();
		assert owner != null;
		for (Element e : owner.getOwnedElements()) {
			if ((e instanceof Classifier) && (e != classifier)) {
				Classifier c = (Classifier) e;
				String cname = c.getName();
				assert cname != null;
				if (cname.startsWith(name)) {
					cname = cname.substring(name.length());
					cname = cname.replace('[', ' ');
					cname = cname.replace(']', ' ');
					cname = cname.trim();
					if (cname.length() == 0) {
						derived.add(c);
					}
				}
			}
		}
		return derived;
	}

	protected FindContainedClassifierSwitch findContainedClassifier = new FindContainedClassifierSwitch();
	protected AddInferredTagSwitch addInferredTags = new AddInferredTagSwitch();

	/**
	 * Creates a new {@link MarkInferredClassifiers}.
	 * @param filter A filter to apply to model operations.
	 * @param monitor A progress monitor to check for end user cancellation.
	 * @param model The UML model to store generated elements in.
	 */
	public MarkInferredClassifiers(Filter filter, IProgressMonitor monitor,
			Model model) {
		super(filter, monitor, model);
	}

	/**
	 * @return All {@link Classifier}s corresponding to elements contained in parsedClasses, including derived classifiers.
	 * @param parsedClasses
	 */
	public Set<Classifier> findContainedClassifiers(Collection<JavaClass> parsedClasses) {
		final Set<Classifier> containedClassifiers = new HashSet<Classifier>();
		for (JavaClass javaClass : parsedClasses) {
			addContainedClassifier(javaClass, containedClassifiers);
		}
		JarToUML.logger.fine(JarToUMLResources.getString("MarkInferredClassifiers.foundContainedClassifiers")); //$NON-NLS-1$
		return containedClassifiers;
	}

	/**
	 * Adds all {@link Classifier}s for javaClass to containedClassifiers, including derived classifiers.
	 * @param javaClass
	 * @param containedClassifiers
	 */
	public void addContainedClassifier(JavaClass javaClass, Collection<Classifier> containedClassifiers) {
		if (!filter(javaClass)) {
			logSkippedFiltered(javaClass);
			return;
		}
		Classifier classifier = findContainedClassifier.findClassifier(
				getModel(), javaClass.getClassName(), null);
		containedClassifiers.add(classifier);
		JarToUML.logger.finer(String.format(
				JarToUMLResources.getString("MarkInferredClassifiers.addedContainedClassifier"), 
				JarToUML.qualifiedName(classifier))); //$NON-NLS-1$
		Collection<Classifier> derived = findDerivedClassifiers(classifier);
		containedClassifiers.addAll(derived);
	}

	/**
	 * @return All {@link Classifier}s not in containedClassifiers.
	 * @param containedClassifiers
	 */
	public Set<Classifier> findInferredClassifiers(Collection<? extends Classifier> containedClassifiers) {
		final Set<Classifier> inferredClassifiers = new HashSet<Classifier>();
		addInferredClassifiers(getModel(), containedClassifiers, inferredClassifiers);
		JarToUML.logger.fine(JarToUMLResources.getString("MarkInferredClassifiers.foundInferredClassifiers")); //$NON-NLS-1$
		return inferredClassifiers;
	}

	/**
	 * Adds all {@link Classifier}s under container not in containedClassifiers to inferredClassifiers.
	 * @param container
	 * @param containedClassifiers
	 * @param inferredClassifiers
	 */
	public void addInferredClassifiers(Element container, Collection<? extends Classifier> containedClassifiers, Set<Classifier> inferredClassifiers) {
		for (Element e : container.getOwnedElements()) {
			if (e instanceof Classifier) {
				if (!containedClassifiers.contains(e)) {
					inferredClassifiers.add((Classifier) e);
					JarToUML.logger.finer(String.format(
							JarToUMLResources.getString("MarkInferredClassifiers.addedInferredClassifier"), 
							JarToUML.qualifiedName((Classifier) e))); //$NON-NLS-1$
				}
				addInferredClassifiers(e, containedClassifiers, inferredClassifiers);
			} else if (e instanceof Package) {
				addInferredClassifiers(e, containedClassifiers, inferredClassifiers);
			}
		}
	}

	/**
	 * Adds inferred tags to all elements not contained in containedClassifiers.
	 * @param containedClassifiers
	 */
	public void addAllInferredTags(Set<? extends Classifier> containedClassifiers) {
		addInferredTags.setContainedClassifiers(containedClassifiers);
		addInferredTags.doSwitch(getModel());
	}

}
