package org.eclipselabs.jar2uml.test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipselabs.jar2uml.AddInferredTagSwitch;
import org.eclipselabs.jar2uml.FindContainedClassifierSwitch;
import org.eclipselabs.jar2uml.MergeModel;

public class MergeModelTest extends J2UTestCase {

	public static final Pattern javaNamePattern = Pattern.compile("^\\w+(\\[\\])*(\\.\\w+)*(\\$\\w+)*(\\[\\])*(\\#(\\w+|<init>)(\\.\\w+)?)?$");

	public static final String bug78Uri = PLUGIN_URI + "/resources/bug78.uml";

	/**
	 * Test method for {@link MergeModel#getJavaName(NamedElement)}.
	 */
	public void testGetJavaName() {
		final Model jaxbOsgiRefDepsModel = loadModelFromUri(jaxbOsgiDepsUri);
		final String modelName = jaxbOsgiRefDepsModel.getName();
		for (Element e : jaxbOsgiRefDepsModel.allOwnedElements()) {
			if (e instanceof NamedElement) {
				String javaName = MergeModel.getJavaName((NamedElement) e);
				assertFalse(javaName.startsWith(modelName));
				assertTrue(javaNamePattern.matcher(javaName).matches());
			}
		}
	}

	/**
	 * Test method for {@link MergeModel#findClassifiers(org.eclipse.uml2.uml.Namespace, java.util.Collection)}.
	 */
	public void testFindClassifiers() {
		final Model jaxbOsgiRefDepsModel = loadModelFromUri(jaxbOsgiDepsUri);
		final List<Classifier> classifiers = new ArrayList<Classifier>();
		final List<Element> elements = jaxbOsgiRefDepsModel.allOwnedElements();
		MergeModel.findClassifiers(jaxbOsgiRefDepsModel, classifiers);
		assertFalse(classifiers.isEmpty());
		assertFalse(classifiers.size() > elements.size());
		final Set<Classifier> uniqueClassifiers = new HashSet<Classifier>(classifiers);
		assertEquals(uniqueClassifiers.size(), classifiers.size()); // no duplicates
		for (Element e : elements) {
			if (e instanceof Classifier) {
				assertTrue(uniqueClassifiers.contains(e));
			}
		}
	}

	/**
	 * Test method for {@link MergeModel#run()}.
	 * @throws InterruptedException 
	 */
	public void testRun() throws InterruptedException {
		final Model baseModel = loadModelFromUri(jaxbOsgiDepsUri);
		final Model jaxbOsgiRefDepsModel = loadModelFromUri(jaxbOsgiDepsUri);
		final Model j2eeRefDepsModel = loadModelFromUri(j2eeDepsUri);
		testRunMerge(baseModel, j2eeRefDepsModel, jaxbOsgiRefDepsModel);
	}

	/**
	 * Test method for <a href="https://soft.vub.ac.be/bugzilla/show_bug.cgi?id=78">bug 78</a>.
	 * @throws InterruptedException 
	 */
	public void testBug78() throws InterruptedException {
		final Model baseModel = loadModelFromUri(jaxbOsgiDepsUri);
		final Model refModel = loadModelFromUri(jaxbOsgiDepsUri);
		final Model mergeModel = loadModelFromUri(bug78Uri);
		testRunMerge(baseModel, mergeModel, refModel);
	}

	/**
	 * Tests {@link MergeModel#run()} with given models.
	 * @param base the base model
	 * @param merge the model to merge
	 * @param ref the reference model - the merged model must be compatible with this model
	 * @throws InterruptedException 
	 */
	private void testRunMerge(final Model base, final Model merge, final Model ref) throws InterruptedException {
		final MergeModel mergeModel = new MergeModel();
		mergeModel.setBaseModel(base);
		mergeModel.setMergeModel(merge);
		mergeModel.run();
		validateModel(base);
		validateInferredTags(base);
		final List<Classifier> mergeClassifiers = new ArrayList<Classifier>();
		MergeModel.findClassifiers(merge, mergeClassifiers);
		FindContainedClassifierSwitch findClassifierSwitch = new FindContainedClassifierSwitch();
		for (Classifier c : mergeClassifiers) {
			String className = MergeModel.getJavaName(c);
			Classifier cim = findClassifierSwitch.findClassifier(base, className, null);
			assertNotNull(cim);
			Classifier orig = findClassifierSwitch.findClassifier(ref, className, null);
			boolean inferred = (orig != null ? AddInferredTagSwitch.isInferred(orig) : true) && AddInferredTagSwitch.isInferred(c);
			assertEquals(inferred, AddInferredTagSwitch.isInferred(cim));
		}
		final List<Classifier> refClassifiers = new ArrayList<Classifier>();
		MergeModel.findClassifiers(ref, refClassifiers);
		for (Classifier c : refClassifiers) {
			String className = MergeModel.getJavaName(c);
			Classifier cim = findClassifierSwitch.findClassifier(base, className, null);
			assertNotNull(cim);
			if (!AddInferredTagSwitch.isInferred(c)) {
				assertFalse(AddInferredTagSwitch.isInferred(cim));
				assertCompatible(cim, c);
			}
		}
	}

}
