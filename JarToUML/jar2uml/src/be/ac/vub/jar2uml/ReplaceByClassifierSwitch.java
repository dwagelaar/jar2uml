package be.ac.vub.jar2uml;

import java.util.logging.Logger;

import junit.framework.Assert;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.DataType;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.util.UMLSwitch;

/**
 * Replaces {@link #setClassifier(Classifier)} by a new {@link Interface}.
 * Switches on {@link Element#getOwner()} of {@link #getClassifier()}
 * and returns new {@link Interface}.
 * Copies {@link #getClassifier()} attributes, operations and nested classifiers.
 * @author dennis
 *
 */
public class ReplaceByClassifierSwitch extends UMLSwitch {
	
	public class PreSwitch extends UMLSwitch {
		public Object caseClass(Class umlClass) {
			nested = umlClass.getNestedClassifiers();
			atts = umlClass.getOwnedAttributes();
			ops = umlClass.getOwnedOperations();
			isAbstract = umlClass.isAbstract();
			isLeaf = umlClass.isLeaf();
			return umlClass;
		}

		public Object caseInterface(Interface umlIface) {
			nested = umlIface.getNestedClassifiers();
			atts = umlIface.getOwnedAttributes();
			ops = umlIface.getOwnedOperations();
			isAbstract = umlIface.isAbstract();
			isLeaf = umlIface.isLeaf();
			return umlIface;
		}

		public Object caseDataType(DataType dataType) {
			nested = null;
			atts = dataType.getOwnedAttributes();
			ops = dataType.getOwnedOperations();
			isAbstract = dataType.isAbstract();
			isLeaf = dataType.isLeaf();
			return dataType;
		}

		public Object caseClassifier(Classifier classifier) {
			nested = null;
			atts = null;
			ops = null;
			isAbstract = classifier.isAbstract();
			isLeaf = classifier.isLeaf();
			return classifier;
		}
	}

	public class PostSwitch extends UMLSwitch {
		public Object caseClass(Class umlClass) {
			if (nested != null) {
				umlClass.getNestedClassifiers().addAll(nested);
			}
			if (atts != null) {
				umlClass.getOwnedAttributes().addAll(atts);
			}
			if (ops != null) {
				umlClass.getOwnedOperations().addAll(ops);
			}
			return super.caseClass(umlClass);
		}

		public Object caseInterface(Interface umlIface) {
			if (nested != null) {
				umlIface.getNestedClassifiers().addAll(nested);
			}
			if (atts != null) {
				umlIface.getOwnedAttributes().addAll(atts);
			}
			if (ops != null) {
				umlIface.getOwnedOperations().addAll(ops);
			}
			return super.caseInterface(umlIface);
		}

		public Object caseDataType(DataType dataType) {
			if (atts != null) {
				dataType.getOwnedAttributes().addAll(atts);
			}
			if (ops != null) {
				dataType.getOwnedOperations().addAll(ops);
			}
			return super.caseDataType(dataType);
		}

		public Object caseClassifier(Classifier classifier) {
			classifier.setIsAbstract(isAbstract);
			return classifier;
		}
	}

	protected static Logger logger = Logger.getLogger(JarToUML.LOGGER);
	
	private Classifier classifier = null;
	private EClass metaClass = UMLPackage.eINSTANCE.getDataType();

	protected EList nested = null;
	protected EList atts = null;
	protected EList ops = null;
	protected boolean isAbstract = false;
	protected boolean isLeaf = false;
	protected PreSwitch preSwitch = new PreSwitch();
	protected PostSwitch postSwitch = new PostSwitch();
	
	public Object caseClass(Class umlClass) {
		Classifier classifier = (Classifier) preSwitch.doSwitch(getClassifier());
		umlClass.getNestedClassifiers().remove(classifier);
		logger.fine("Replacing " + classifier.getQualifiedName() + " : " + 
				classifier.eClass().getName() + " by instance of " + getMetaClass().getName());
		classifier = umlClass.createNestedClassifier(classifier.getName(), getMetaClass());
		return (Classifier) postSwitch.doSwitch(classifier);
	}

	public Object caseInterface(Interface umlIface) {
		Classifier classifier = (Classifier) preSwitch.doSwitch(getClassifier());
		umlIface.getNestedClassifiers().remove(classifier);
		logger.fine("Replacing " + classifier.getQualifiedName() + " : " + 
				classifier.eClass().getName() + " by instance of " + getMetaClass().getName());
		classifier = umlIface.createNestedClassifier(classifier.getName(), getMetaClass());
		return (Classifier) postSwitch.doSwitch(classifier);
	}

	public Object casePackage(Package pack) {
		Classifier classifier = (Classifier) preSwitch.doSwitch(getClassifier());
		pack.getPackagedElements().remove(classifier);
		logger.fine("Replacing " + classifier.getQualifiedName() + " : " + 
				classifier.eClass().getName() + " by instance of " + getMetaClass().getName());
		classifier = (Classifier) pack.createPackagedElement(classifier.getName(), getMetaClass());
		return (Classifier) postSwitch.doSwitch(classifier);
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
	
	public void reset() {
		setClassifier(null);
		setMetaClass(UMLPackage.eINSTANCE.getDataType());
	}

}
