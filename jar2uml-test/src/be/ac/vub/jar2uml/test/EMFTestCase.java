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
package be.ac.vub.jar2uml.test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import junit.framework.TestCase;

import org.eclipse.emf.compare.diff.metamodel.DiffGroup;
import org.eclipse.emf.compare.diff.metamodel.DiffModel;
import org.eclipse.emf.compare.diff.metamodel.DifferenceKind;
import org.eclipse.emf.compare.diff.metamodel.ModelElementChangeLeftTarget;
import org.eclipse.emf.compare.diff.metamodel.MoveModelElement;
import org.eclipse.emf.compare.diff.metamodel.ReferenceOrderChange;
import org.eclipse.emf.compare.diff.metamodel.UpdateAttribute;
import org.eclipse.emf.compare.diff.service.DiffService;
import org.eclipse.emf.compare.match.MatchOptions;
import org.eclipse.emf.compare.match.metamodel.MatchModel;
import org.eclipse.emf.compare.match.service.MatchService;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.BehavioralFeature;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.RedefinableElement;
import org.eclipse.uml2.uml.StructuralFeature;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.VisibilityKind;

/**
 * Test case functionality for EMF-based code.
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public abstract class EMFTestCase extends TestCase {

	/**
	 * Creates a new {@link EMFTestCase}.
	 */
	public EMFTestCase() {
		super();
	}

	/**
	 * Creates a new {@link EMFTestCase}.
	 * @param name
	 */
	public EMFTestCase(String name) {
		super(name);
	}

	/**
	 * Asserts that leftResource and rightResource are equal. Uses EMF Compare.
	 * @param leftObject
	 * @param rightObject
	 * @throws InterruptedException 
	 */
	public static void assertEquals(Resource leftResource, Resource rightResource)
	throws InterruptedException {
		final Map<String, Object> options = new HashMap<String, Object>();
		options.put(MatchOptions.OPTION_IGNORE_XMI_ID, Boolean.TRUE);
		final MatchModel match = MatchService.doResourceMatch(leftResource, rightResource, options);
		assertTrue(match.getUnmatchedElements().isEmpty());
		assertFalse(match.getMatchedElements().isEmpty());
		final DiffModel diff = DiffService.doDiff(match);
		assertTrue(diff.getOwnedElements().size() == 1);
		for (Iterator<EObject> allContents = diff.eAllContents(); allContents.hasNext();) {
			EObject de = allContents.next();
			//allow only certain kinds of diff elements
			assertTrue(de instanceof DiffGroup || de instanceof ReferenceOrderChange);
		}
	}

	/**
	 * Asserts that leftResource and rightResource are equal. Uses EMF Compare.
	 * @param leftObject
	 * @param rightObject
	 * @throws InterruptedException 
	 */
	public static void assertEquals(EObject leftObject, EObject rightObject)
	throws InterruptedException {
		assertEquals(leftObject, rightObject, false);
	}

	/**
	 * Asserts that leftResource and rightResource are equal. Uses EMF Compare.
	 * @param leftObject
	 * @param rightObject
	 * @param allowUnmatchedElements iff <code>true</code>, allows unmatched elements to exist, as long as there are matched elements as well
	 * @throws InterruptedException 
	 */
	public static void assertEquals(EObject leftObject, EObject rightObject, boolean allowUnmatchedElements)
	throws InterruptedException {
		final Map<String, Object> options = new HashMap<String, Object>();
		options.put(MatchOptions.OPTION_IGNORE_XMI_ID, Boolean.TRUE);
		final MatchModel match = MatchService.doContentMatch(leftObject, rightObject, options);
		if (!allowUnmatchedElements) {
			assertTrue(match.getUnmatchedElements().isEmpty());
		}
		assertFalse(match.getMatchedElements().isEmpty());
		final DiffModel diff = DiffService.doDiff(match);
		assertTrue(diff.getOwnedElements().size() == 1);
		for (Iterator<EObject> allContents = diff.eAllContents(); allContents.hasNext();) {
			EObject de = allContents.next();
			//allow only certain kinds of diff elements
			assertTrue(
					de.toString(), 
					de instanceof DiffGroup || de instanceof ReferenceOrderChange || de instanceof MoveModelElement);
		}
	}

	/**
	 * Asserts that leftResource is compatible with rightResource. Uses EMF Compare.
	 * @param leftObject
	 * @param rightObject
	 * @param allowUnmatchedElements iff <code>true</code>, allows unmatched elements to exist, as long as there are matched elements as well
	 * @throws InterruptedException 
	 */
	public static void assertCompatible(EObject leftObject, EObject rightObject)
	throws InterruptedException {
		final UMLPackage uml = UMLPackage.eINSTANCE;
		final Map<String, Object> options = new HashMap<String, Object>();
		options.put(MatchOptions.OPTION_IGNORE_XMI_ID, Boolean.TRUE);
		final MatchModel match = MatchService.doContentMatch(leftObject, rightObject, options);
		assertFalse(match.getMatchedElements().isEmpty());
		final DiffModel diff = DiffService.doDiff(match);
		assertTrue(diff.getOwnedElements().size() == 1);
		for (Iterator<EObject> allContents = diff.eAllContents(); allContents.hasNext();) {
			EObject de = allContents.next();
			if (de instanceof UpdateAttribute) {
				UpdateAttribute ua = (UpdateAttribute) de;
				EAttribute att = ua.getAttribute();
				if (att == uml.getBehavioralFeature_IsAbstract()) {
					BehavioralFeature left = (BehavioralFeature) ua.getLeftElement();
					BehavioralFeature right = (BehavioralFeature) ua.getRightElement();
					assertTrue(
							ua.toString(),
							implies(!right.isAbstract(), !left.isAbstract()));
				} else if (att == uml.getClassifier_IsAbstract()) {
					Classifier left = (Classifier) ua.getLeftElement();
					Classifier right = (Classifier) ua.getRightElement();
					assertTrue(
							ua.toString(),
							implies(!right.isAbstract(), !left.isAbstract()));
				} else if (att == uml.getRedefinableElement_IsLeaf()) {
					RedefinableElement left = (RedefinableElement) ua.getLeftElement();
					RedefinableElement right = (RedefinableElement) ua.getRightElement();
					assertTrue(
							ua.toString(),
							implies(!right.isLeaf(), !left.isLeaf()));
				} else if (att == uml.getStructuralFeature_IsReadOnly()) {
					StructuralFeature left = (StructuralFeature) ua.getLeftElement();
					StructuralFeature right = (StructuralFeature) ua.getRightElement();
					assertTrue(
							ua.toString(),
							implies(!right.isReadOnly(), !left.isReadOnly()));
				} else if (att == uml.getNamedElement_Visibility()) {
					NamedElement left = (NamedElement) ua.getLeftElement();
					NamedElement right = (NamedElement) ua.getRightElement();
					assertTrue(
							ua.toString(),
							implies(right.isSetVisibility(), left.isSetVisibility()));
					boolean visOk = true;
					int leftVis = left.getVisibility().ordinal();
					switch (right.getVisibility().ordinal()) {
					case VisibilityKind.PUBLIC:
						visOk = leftVis == VisibilityKind.PUBLIC;
					case VisibilityKind.PROTECTED:
						visOk = leftVis == VisibilityKind.PUBLIC
							|| leftVis == VisibilityKind.PROTECTED;
					case VisibilityKind.PACKAGE:
						visOk = leftVis == VisibilityKind.PUBLIC
							|| leftVis == VisibilityKind.PROTECTED
							|| leftVis == VisibilityKind.PACKAGE;
					}
					assertTrue(ua.toString(), visOk);
				} else {
					fail(ua.toString());
				}
			} else if (de instanceof ModelElementChangeLeftTarget) {
				ModelElementChangeLeftTarget mc = (ModelElementChangeLeftTarget) de;
				assertTrue(
						de.toString(), 
						mc.getKind() == DifferenceKind.ADDITION);
			} else {
				//allow only certain kinds of diff elements
				assertTrue(
						de.toString(), 
						de instanceof DiffGroup || 
						de instanceof ReferenceOrderChange || 
						de instanceof MoveModelElement);
			}
		}
	}

	/**
	 * @param first
	 * @param second
	 * @return <code>true</code> iff first => second
	 */
	public static final boolean implies(boolean first, boolean second) {
		return first ? second : true;
	}

}