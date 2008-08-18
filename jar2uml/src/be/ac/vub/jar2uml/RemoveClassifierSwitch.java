package be.ac.vub.jar2uml;

import java.util.Iterator;
import java.util.logging.Logger;

import junit.framework.Assert;

import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.util.UMLSwitch;

/**
 * Removes {@link #setClassifier(Classifier)} from its parent.
 * Switches on {@link Element#getOwner()} of {@link #getClassifier()}
 * and returns itself. Also removes derived datatypes (arrays), nested
 * classifiers and container packages if they become empty.
 * @author dennis
 *
 */
public class RemoveClassifierSwitch extends UMLSwitch<Classifier> {
	
	public class RemoveNestedClassifierSwitch extends UMLSwitch<Classifier> {
		
		public Classifier caseClass(Class umlClass) {
			for (Iterator<Classifier> it = umlClass.getNestedClassifiers().iterator(); it.hasNext();) {
				Classifier c = it.next();
				doSwitch(c);
				it.remove();
			}
			return umlClass;
		}

		public Classifier caseInterface(Interface umlIface) {
			for (Iterator<Classifier> it = umlIface.getNestedClassifiers().iterator(); it.hasNext();) {
				Classifier c = it.next();
				doSwitch(c);
				it.remove();
			}
			return umlIface;
		}
	}
	
	protected static Logger logger = Logger.getLogger(JarToUML.LOGGER);
	
	private Classifier classifier = null;
	protected RemoveNestedClassifierSwitch removeNested = new RemoveNestedClassifierSwitch();

	public Classifier caseClass(Class umlClass) {
		final Classifier classifier = getClassifier();
		Assert.assertNotNull(classifier);
		removeNested.doSwitch(classifier);
		umlClass.getNestedClassifiers().remove(classifier);
		logger.fine("Removed " + classifier.getQualifiedName() + " : " + 
				classifier.eClass().getName());
		return classifier;
	}

	public Classifier caseInterface(Interface umlIface) {
		final Classifier classifier = getClassifier();
		Assert.assertNotNull(classifier);
		removeNested.doSwitch(classifier);
		umlIface.getNestedClassifiers().remove(classifier);
		logger.fine("Removed " + classifier.getQualifiedName() + " : " + 
				classifier.eClass().getName());
		return classifier;
	}

	public Classifier casePackage(Package pack) {
		final Classifier classifier = getClassifier();
		Assert.assertNotNull(classifier);
		removeNested.doSwitch(classifier);
		pack.getPackagedElements().remove(classifier);
		logger.fine("Removed " + classifier.getQualifiedName() + " : " + 
				classifier.eClass().getName());
		return classifier;
	}

	public void setClassifier(Classifier classifier) {
		this.classifier = classifier;
	}

	public Classifier getClassifier() {
		return classifier;
	}

	public void reset() {
		setClassifier(null);
	}
	
}
