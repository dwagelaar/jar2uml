package be.ac.vub.jar2uml;

import java.util.Iterator;
import java.util.logging.Logger;

import junit.framework.Assert;

import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.DataType;
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
public class RemoveClassifierSwitch extends UMLSwitch {
	
	public class RemoveNestedClassifierSwitch extends UMLSwitch {
		
		public Object caseClass(Class umlClass) {
			final Package pack = umlClass.getNearestPackage();
			Assert.assertNotNull(pack);
			for (Iterator it = umlClass.getNestedClassifiers().iterator(); it.hasNext();) {
				Classifier c = (Classifier) it.next();
				doSwitch(c);
				removeDerivedDataTypes(pack.getPackagedElements(), c.getName());
				it.remove();
			}
			return umlClass;
		}

		public Object caseInterface(Interface umlIface) {
			final Package pack = umlIface.getNearestPackage();
			Assert.assertNotNull(pack);
			for (Iterator it = umlIface.getNestedClassifiers().iterator(); it.hasNext();) {
				Classifier c = (Classifier) it.next();
				doSwitch(c);
				removeDerivedDataTypes(pack.getPackagedElements(), c.getName());
				it.remove();
			}
			return umlIface;
		}
	}
	
	protected static Logger logger = Logger.getLogger(JarToUML.LOGGER);
	
	private Classifier classifier = null;
	protected RemoveNestedClassifierSwitch removeNested = new RemoveNestedClassifierSwitch();

	public Object caseClass(Class umlClass) {
		final Classifier classifier = getClassifier();
		final Package pack = classifier.getNearestPackage();
		Assert.assertNotNull(classifier);
		removeNested.doSwitch(classifier);
		umlClass.getNestedClassifiers().remove(classifier);
		logger.fine("Removed " + classifier.getQualifiedName() + " : " + 
				classifier.eClass().getName());
		Assert.assertNotNull(pack);
		removeDerivedDataTypes(pack.getPackagedElements(), classifier.getName());
		return classifier;
	}

	public Object caseInterface(Interface umlIface) {
		final Classifier classifier = getClassifier();
		final Package pack = classifier.getNearestPackage();
		Assert.assertNotNull(classifier);
		removeNested.doSwitch(classifier);
		umlIface.getNestedClassifiers().remove(classifier);
		logger.fine("Removed " + classifier.getQualifiedName() + " : " + 
				classifier.eClass().getName());
		Assert.assertNotNull(pack);
		removeDerivedDataTypes(pack.getPackagedElements(), classifier.getName());
		return classifier;
	}

	public Object casePackage(Package pack) {
		final Classifier classifier = getClassifier();
		Assert.assertNotNull(classifier);
		removeNested.doSwitch(classifier);
		pack.getPackagedElements().remove(classifier);
		logger.fine("Removed " + classifier.getQualifiedName() + " : " + 
				classifier.eClass().getName());
		removeDerivedDataTypes(
				pack.getPackagedElements(), 
				classifier.getName());
//		while ((pack != null) && (pack.getPackagedElements().isEmpty())) {
//			Package child = pack;
//			pack = child.getNestingPackage();
//			pack.getNestedPackages().remove(child);
//			logger.fine("Removed " + child.getQualifiedName() + " : " + 
//					child.eClass().getName());
//		}
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
	
	/**
	 * Removes all {@link DataType} instances from fromList whose name
	 * starts with basename.
	 * @param fromList
	 * @param basename
	 */
	protected void removeDerivedDataTypes(EList fromList, String basename) {
		for (Iterator it = fromList.iterator(); it.hasNext();) {
			Object o = it.next();
			if (o instanceof DataType) {
				DataType dt = (DataType) o;
				if (dt.getName().startsWith(basename)) {
					it.remove();
					logger.fine("Removed " + dt.getQualifiedName() + " : " + 
							dt.eClass().getName());
				}
			}
		}
	}

}
