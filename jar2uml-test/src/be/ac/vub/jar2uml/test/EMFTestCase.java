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

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.emf.compare.AttributeChange;
import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.compare.Diff;
import org.eclipse.emf.compare.DifferenceKind;
import org.eclipse.emf.compare.ReferenceChange;
import org.eclipse.emf.compare.diff.DefaultDiffEngine;
import org.eclipse.emf.compare.diff.DiffBuilder;
import org.eclipse.emf.compare.match.DefaultMatchEngine;
import org.eclipse.emf.compare.scope.DefaultComparisonScope;
import org.eclipse.emf.compare.utils.UseIdentifiers;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.BehavioralFeature;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.RedefinableElement;
import org.eclipse.uml2.uml.StructuralFeature;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.VisibilityKind;

import junit.framework.TestCase;

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
	 * 
	 * @param leftResource
	 *            the left-hand resource to compare
	 * @param rightResource
	 *            the right-hand resource to compare
	 */
	public static void assertEquals(Resource leftResource, Resource rightResource) {
		final DefaultComparisonScope scope = new DefaultComparisonScope(leftResource, rightResource, null);
		final Comparison match = DefaultMatchEngine.create(UseIdentifiers.NEVER).match(scope, null);
		if (!leftResource.getContents().isEmpty()) {
			assertFalse("Match model is empty: " + match.getMatches(), match.getMatches().isEmpty());
		}
		new DefaultDiffEngine(new DiffBuilder()).diff(match, null);
		for (Diff diff : match.getDifferences()) {
			// allow only certain kinds of diff elements
			if (diff instanceof ReferenceChange && ((ReferenceChange) diff).getKind() == DifferenceKind.CHANGE) {
				assertEquals(diff.getMatch().getLeft(), diff.getMatch().getRight(), ((ReferenceChange) diff).getReference());
			} else {
				fail("Difference found: " + diff);
			}
		}
	}

	/**
	 * Asserts that <code>left.ref</code> and <code>right.ref</code> point to equal values.
	 * 
	 * @param left
	 *            the left-hand object to compare
	 * @param right
	 *            the right-hand object to compare
	 * @param ref
	 *            the {@link EReference} of which to compare the values
	 */
	public static void assertEquals(final EObject left, final EObject right, final EReference ref) {
		if (ref.isMany()) {
			final Collection<?> leftValue = (Collection<?>) left.eGet(ref);
			final Collection<?> rightValue = (Collection<?>) right.eGet(ref);
			final String errorMsg = String.format("Different value found on %s.%s (%s) and %s.%s (%s)", left, ref.getName(), leftValue,
					right, ref.getName(), rightValue);

			assertEquals(errorMsg, leftValue.size(), rightValue.size());
			final Iterator<?> leftVs = leftValue.iterator();
			final Iterator<?> rightVs = rightValue.iterator();
			while (leftVs.hasNext()) {
				// Reference to same object by URI - only target objects are different instances
				assertSameURI(errorMsg, (EObject) leftVs.next(), (EObject) rightVs.next());
			}
		} else {
			final EObject leftValue = left == null ? null : (EObject) left.eGet(ref);
			final EObject rightValue = right == null ? null : (EObject) right.eGet(ref);
			final String errorMsg = String.format("Different value found on %s.%s (%s) and %s.%s (%s)", left, ref.getName(), leftValue,
					right, ref.getName(), rightValue);
			// Reference to same object by URI - only target objects are different instances
			assertSameURI(errorMsg, leftValue, rightValue);
		}
	}

	/**
	 * Asserts that <code>leftValue</code> and <code>rightValue</code> have the same EMF URI.
	 * 
	 * @param errorMsg
	 *            the error message to display on assertion failure
	 * @param leftValue
	 *            the left-hand value to compare
	 * @param rightValue
	 *            the right-hand value to compare
	 */
	private static void assertSameURI(final String errorMsg, final EObject leftValue, final EObject rightValue) {
		assertEquals(errorMsg, leftValue == null ? null : leftValue.eResource().getURI(),
				rightValue == null ? null : rightValue.eResource().getURI());
		assertEquals(errorMsg, leftValue == null ? null : leftValue.eResource().getURIFragment(leftValue),
				rightValue == null ? null : rightValue.eResource().getURIFragment(rightValue));
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
		final DefaultComparisonScope scope = new DefaultComparisonScope(leftObject, rightObject, null);
		final Comparison match = DefaultMatchEngine.create(UseIdentifiers.NEVER).match(scope, null);
		assertFalse("Match model is empty: " + match.getMatches(), match.getMatches().isEmpty());
		new DefaultDiffEngine(new DiffBuilder()).diff(match, null);
		for (Diff diff : match.getDifferences()) {
			// allow only certain kinds of diff elements
			if (diff instanceof ReferenceChange && ((ReferenceChange) diff).getKind() == DifferenceKind.CHANGE) {
				ReferenceChange refChg = (ReferenceChange) diff;
				switch (refChg.getKind()) {
				case ADD:
				case MOVE:
					// Allowed
					break;
				case CHANGE:
					assertEquals(diff.getMatch().getLeft(), diff.getMatch().getRight(), ((ReferenceChange) diff).getReference());
					break;
				default:
					fail("Difference found: " + diff);
					break;
				}
			} else if (diff instanceof AttributeChange) {
				AttributeChange attChg = (AttributeChange) diff;
				EAttribute att = attChg.getAttribute();
				if (att == uml.getBehavioralFeature_IsAbstract()) {
					BehavioralFeature left = (BehavioralFeature) attChg.getMatch().getLeft();
					BehavioralFeature right = (BehavioralFeature) attChg.getMatch().getRight();
					assertTrue(attChg.toString(), implies(!right.isAbstract(), !left.isAbstract()));
				} else if (att == uml.getClassifier_IsAbstract()) {
					Classifier left = (Classifier) attChg.getMatch().getLeft();
					Classifier right = (Classifier) attChg.getMatch().getRight();
					assertTrue(attChg.toString(), implies(!right.isAbstract(), !left.isAbstract()));
				} else if (att == uml.getRedefinableElement_IsLeaf()) {
					RedefinableElement left = (RedefinableElement) attChg.getMatch().getLeft();
					RedefinableElement right = (RedefinableElement) attChg.getMatch().getRight();
					assertTrue(attChg.toString(), implies(!right.isLeaf(), !left.isLeaf()));
				} else if (att == uml.getStructuralFeature_IsReadOnly()) {
					StructuralFeature left = (StructuralFeature) attChg.getMatch().getLeft();
					StructuralFeature right = (StructuralFeature) attChg.getMatch().getRight();
					assertTrue(attChg.toString(), implies(!right.isReadOnly(), !left.isReadOnly()));
				} else if (att == uml.getNamedElement_Visibility()) {
					NamedElement left = (NamedElement) attChg.getMatch().getLeft();
					NamedElement right = (NamedElement) attChg.getMatch().getRight();
					assertTrue(attChg.toString(), implies(right.isSetVisibility(), left.isSetVisibility()));
					boolean visOk = true;
					int leftVis = left.getVisibility().ordinal();
					switch (right.getVisibility().ordinal()) {
					case VisibilityKind.PUBLIC:
						visOk = leftVis == VisibilityKind.PUBLIC;
					case VisibilityKind.PROTECTED:
						visOk = leftVis == VisibilityKind.PUBLIC || leftVis == VisibilityKind.PROTECTED;
					case VisibilityKind.PACKAGE:
						visOk = leftVis == VisibilityKind.PUBLIC || leftVis == VisibilityKind.PROTECTED
								|| leftVis == VisibilityKind.PACKAGE;
					}
					assertTrue(attChg.toString(), visOk);
				} else {
					fail(attChg.toString());
				}
			} else {
				fail("Difference found: " + diff);
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