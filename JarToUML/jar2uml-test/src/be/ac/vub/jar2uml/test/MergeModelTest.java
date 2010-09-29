package be.ac.vub.jar2uml.test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.NamedElement;

import be.ac.vub.jar2uml.AddInferredTagSwitch;
import be.ac.vub.jar2uml.FindContainedClassifierSwitch;
import be.ac.vub.jar2uml.MergeModel;

public class MergeModelTest extends J2UTestCase {

	public static final Pattern javaNamePattern = Pattern.compile("^\\w+(\\[\\])*(\\.\\w+)*(\\$\\w+)*(\\[\\])*(\\#(\\w+|<init>)(\\.\\w+)?)?$");

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
		final MergeModel mergeModel = new MergeModel();
		mergeModel.setBaseModel(baseModel);
		mergeModel.setMergeModel(j2eeRefDepsModel);
		mergeModel.run();
		validateModel(baseModel);
		validateInferredTags(baseModel);
		final List<Classifier> j2eeClassifiers = new ArrayList<Classifier>();
		MergeModel.findClassifiers(j2eeRefDepsModel, j2eeClassifiers);
		FindContainedClassifierSwitch findClassifierSwitch = new FindContainedClassifierSwitch();
		for (Classifier c : j2eeClassifiers) {
			String className = MergeModel.getJavaName(c);
			Classifier cim = findClassifierSwitch.findClassifier(baseModel, className, null);
			assertNotNull(cim);
			Classifier orig = findClassifierSwitch.findClassifier(jaxbOsgiRefDepsModel, className, null);
			boolean inferred = (orig != null ? AddInferredTagSwitch.isInferred(orig) : true) && AddInferredTagSwitch.isInferred(c);
			assertEquals(inferred, AddInferredTagSwitch.isInferred(cim));
		}
		final List<Classifier> jaxbClassifiers = new ArrayList<Classifier>();
		MergeModel.findClassifiers(jaxbOsgiRefDepsModel, jaxbClassifiers);
		for (Classifier c : jaxbClassifiers) {
			String className = MergeModel.getJavaName(c);
			Classifier cim = findClassifierSwitch.findClassifier(baseModel, className, null);
			assertNotNull(cim);
			if (!AddInferredTagSwitch.isInferred(c)) {
				assertFalse(AddInferredTagSwitch.isInferred(cim));
				assertCompatible(cim, c);
			}
		}
	}

}
