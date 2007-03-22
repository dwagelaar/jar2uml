package be.ac.vub.platformkit.java;

import junit.framework.Assert;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.util.UMLSwitch;

/**
 * Replaces {@link #setClassifier(Classifier)} by a new {@link Interface}.
 * Switches on {@link Element#getOwner()} of {@link #getClassifier()}
 * and returns new {@link Interface}.
 * Does not copy nested {@link #getClassifier()} elements.
 * @author dennis
 *
 */
public class ReplaceByClassifierSwitch extends UMLSwitch {

	private Classifier classifier = null;
	private EClass metaClass = UMLPackage.eINSTANCE.getDataType();

	public Object caseClass(Class umlClass) {
		Classifier classifier = getClassifier();
		Assert.assertNotNull(classifier);
		umlClass.getNestedClassifiers().remove(classifier);
		return umlClass.createNestedClassifier(classifier.getName(), getMetaClass());
	}

	public Object caseInterface(Interface umlIface) {
		Classifier classifier = getClassifier();
		Assert.assertNotNull(classifier);
		umlIface.getNestedClassifiers().remove(classifier);
		return umlIface.createNestedClassifier(classifier.getName(), getMetaClass());
	}

	public Object casePackage(Package pack) {
		Classifier classifier = getClassifier();
		Assert.assertNotNull(classifier);
		pack.getPackagedElements().remove(classifier);
		return pack.createPackagedElement(classifier.getName(), getMetaClass());
	}

	public void setClassifier(Classifier classifier) {
		this.classifier = classifier;
	}

	public Classifier getClassifier() {
		return classifier;
	}

	public EClass getMetaClass() {
		return metaClass;
	}

	public void setMetaClass(EClass metaClass) {
		Assert.assertNotNull(metaClass);
		Assert.assertEquals(true, UMLPackage.eINSTANCE.getClassifier().isSuperTypeOf(metaClass));
		this.metaClass = metaClass;
	}

}
