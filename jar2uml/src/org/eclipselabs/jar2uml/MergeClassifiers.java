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
package org.eclipselabs.jar2uml;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Dependency;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.InterfaceRealization;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.VisibilityKind;

/**
 * Merges classifiers into the model.
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public class MergeClassifiers extends AddToModel {

	/**
	 * Merges the visibility of base and merge into base.
	 * @param base
	 * @param merge
	 */
	public static final void mergeVisibility(final NamedElement base, final NamedElement merge) {
		if (base.isSetVisibility() && merge.isSetVisibility()) {
			final VisibilityKind cv = merge.getVisibility();
			switch (base.getVisibility().ordinal()) {
			case VisibilityKind.PRIVATE:
				if (cv == VisibilityKind.PACKAGE_LITERAL) {
					base.setVisibility(cv);
				}
			case VisibilityKind.PACKAGE:
				if (cv == VisibilityKind.PROTECTED_LITERAL) {
					base.setVisibility(cv);
				}
			case VisibilityKind.PROTECTED:
				if (cv == VisibilityKind.PUBLIC_LITERAL) {
					base.setVisibility(cv);
				}
			}
		} else if (merge.isSetVisibility()) {
			base.setVisibility(merge.getVisibility());
		}
	}

	private final Set<Classifier> containedClassifiers = new HashSet<Classifier>();

	/**
	 * Creates a {@link MergeClassifiers}.
	 * @param filter ignored
	 * @param monitor A progress monitor to check for end user cancellation.
	 * @param ticks amount of ticks this task will add to the progress monitor
	 * @param model The UML model to store generated elements in.
	 * @param includeFeatures Whether to include fields and methods.
	 * @param includeInstructionReferences ignored
	 */
	public MergeClassifiers(Filter filter, IProgressMonitor monitor, int ticks,
			Model model, boolean includeFeatures,
			boolean includeInstructionReferences) {
		super(filter, monitor, ticks, model, includeFeatures,
				includeInstructionReferences);
	}

	/**
	 * Merges all classifiers into the model.
	 * @param elements the classifiers to merge
	 */
	public void mergeAllClassifiers(final List<Classifier> elements) {
		for (Classifier c : elements) {
			mergeClassifier(c);
			worked();
		}
	}

	/**
	 * Merges c into the model.
	 * @param c
	 */
	public void mergeClassifier(final Classifier c) {
		JarToUMLResources.logger.finest(c.getQualifiedName());
		final Classifier classifier = findClassifierInModel(c);
		assert classifier != null;
		if (!AddInferredTagSwitch.isInferred(c)) {
			getContainedClassifiers().add(classifier);
		}
		if (findContainedClassifier.isCreated()) {
			//copy values
			classifier.setIsAbstract(c.isAbstract());
			classifier.setIsLeaf(c.isLeaf());
			if (c.isSetVisibility()) {
				classifier.setVisibility(c.getVisibility());
			}
		} else {
			//merge values
			mergeVisibility(classifier, c);
			classifier.setIsAbstract(classifier.isAbstract() && c.isAbstract());
			classifier.setIsLeaf(classifier.isLeaf() && c.isLeaf());
		}
	}

	/**
	 * Merges properties of all classifiers into the model.
	 * @param elements the classifiers to merge
	 */
	public void mergeAllClassifierProperties(final List<Classifier> elements) {
		for (Classifier c : elements) {
			mergeClassifierProperties(c);
			worked();
		}
	}

	/**
	 * Merges properties of c into the model.
	 * @param c
	 */
	public void mergeClassifierProperties(final Classifier c) {
		JarToUMLResources.logger.finest(c.getQualifiedName());
		final Classifier classifier = findClassifierInModel(c);
		assert classifier != null;
		addInterfaceRealizations(classifier, c);
		addGeneralizations(classifier, c);
		if (isIncludeFeatures()) {
			addProperties(classifier, c);
			addOperations(classifier, c);
		}
	}

	/**
	 * Adds interface realizations to classifier for each interface implemented
	 * by c.
	 * @param classifier The classifier representation of c.
	 * @param c The classifier to merge.
	 */
	public void addInterfaceRealizations(final Classifier classifier, final Classifier c) {
		assert classifier != null;
		for (Dependency d : c.getClientDependencies()) {
			if (d instanceof InterfaceRealization) {
				Interface ci = ((InterfaceRealization) d).getContract();
				Classifier iface = findClassifierInModel(ci);
				assert iface instanceof Interface;
				addClassifierInterface.setIface((Interface) iface);
				addClassifierInterface.doSwitch(classifier);
			}
		}
	}

	/**
	 * Adds generalizations to classifier for each superclass
	 * of c.
	 * @param classifier The classifier representation of c.
	 * @param c The classifier to merge.
	 */
	public void addGeneralizations(final Classifier classifier, final Classifier c) {
		assert classifier != null;
		for (Classifier general : c.getGenerals()) {
			Classifier superClass = findClassifierInModel(general);
			classifier.getGeneralization(superClass, true);
		}
	}

	/**
	 * Adds a property to classifier for each property of c.
	 * @param classifier The classifier representation of c.
	 * @param c The classifier to merge.
	 */
	public void addProperties(final Classifier classifier, final Classifier c) {
		assert classifier != null;
		for (Property p : c.getAttributes()) {
			JarToUMLResources.logger.finest(p.toString());
			addClassifierProperty.setPropertyName(p.getName());
			Type ptype = p.getType();
			assert ptype instanceof Classifier;
			Classifier tim = findClassifierInModel((Classifier) p.getType());
			addClassifierProperty.setPropertyType(tim);
			Property prop = (Property) addClassifierProperty.doSwitch(classifier);
			if (addClassifierProperty.isPropertyCreated()) {
				//copy value
				prop.setIsStatic(p.isStatic());
				prop.setIsReadOnly(p.isReadOnly());
				prop.setIsLeaf(p.isLeaf());
				if (p.isSetVisibility()) {
					prop.setVisibility(p.getVisibility());
				}
			} else {
				//merge values
				mergeVisibility(prop, p);
				if (prop.isStatic() != p.isStatic()) {
					throw new JarToUMLException(String.format(
							JarToUMLResources.getString("MergeClassifiers.cannotMergeStatic"), 
							prop.getQualifiedName(), p.getQualifiedName()));
				}
				prop.setIsReadOnly(prop.isReadOnly() && p.isReadOnly());
				prop.setIsLeaf(prop.isLeaf() && p.isLeaf());
			}
		}
	}

	/**
	 * Adds an operation to classifier for each javaClass method.
	 * @param classifier The classifier representation of javaClass.
	 * @param c The classifier to merge.
	 */
	public void addOperations(final Classifier classifier, final Classifier c) {
		assert classifier != null;
		for (Operation o : c.getOperations()) {
			JarToUMLResources.logger.finest(o.toString());
			addClassifierOperation.setOperationName(o.getName());
			EList<Parameter> pars = o.getOwnedParameters();
			addClassifierOperation.setArgumentNames(
					AddClassifierOperationSwitch.getParameterNames(pars));
			addClassifierOperation.setArgumentTypes(
					findTypesInModel(AddClassifierOperationSwitch.getParameterTypes(pars)));
			Parameter retPar = o.getReturnResult();
			if (retPar != null) {
				Type retType = retPar.getType();
				assert retType instanceof Classifier;
				addClassifierOperation.setReturnType(
						findClassifierInModel((Classifier) retType));
			} else {
				addClassifierOperation.setReturnType(null);
			}
			Operation op = (Operation) addClassifierOperation.doSwitch(classifier);
			if (addClassifierOperation.isOperationCreated()) {
				//copy values
				op.setIsStatic(o.isStatic());
				op.setIsAbstract(o.isAbstract());
				op.setIsLeaf(o.isLeaf());
				if (o.isSetVisibility()) {
					op.setVisibility(o.getVisibility());
				}
			} else {
				//merge values
				mergeVisibility(op, o);
				op.setIsAbstract(op.isAbstract() && o.isAbstract());
				if (op.isStatic() != o.isStatic()) {
					throw new JarToUMLException(String.format(
							JarToUMLResources.getString("MergeClassifiers.cannotMergeStatic"), 
							op.getQualifiedName(), o.getQualifiedName()));
				}
				op.setIsLeaf(op.isLeaf() && o.isLeaf());
			}
		}
	}

	/**
	 * @param c
	 * @return the representation of c inside the model
	 */
	protected Classifier findClassifierInModel(Classifier c) {
		final Classifier cim = findContainedClassifier.findClassifier(
				getModel(), MergeModel.getJavaName(c), c.eClass());
		return cim;
	}

	/**
	 * @param types
	 * @return the representations of types inside the model
	 * Asserts all types to be {@link Classifier}s.
	 */
	protected EList<Type> findTypesInModel(List<Type> types) {
		final EList<Type> tim = new BasicEList<Type>();
		for (Type type : types) {
			assert type instanceof Classifier;
			tim.add(findClassifierInModel((Classifier) type));
		}
		return tim;
	}

	/**
	 * @return the containedClassifiers
	 */
	public Set<Classifier> getContainedClassifiers() {
		return containedClassifiers;
	}

}
