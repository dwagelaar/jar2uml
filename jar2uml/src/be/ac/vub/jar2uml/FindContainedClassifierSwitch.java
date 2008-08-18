package be.ac.vub.jar2uml;

import java.util.Iterator;
import java.util.logging.Logger;

import junit.framework.Assert;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.DataType;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.PackageableElement;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.util.UMLSwitch;

/**
 * Searches for a contained {@link Classifier} with name {@link #getClassifierName()}
 * in the switched object. If a {@link Classifier} named {@link #getClassifierName()}
 * is not found and {@link #isCreate()} is true, a contained instance of
 * {@link #getMetaClass()} will be added to the switched object.
 * {@link #getMetaClass()} defaults to {@link DataType}.
 * If the switched object is a {@link Classifier} that cannot contain nested instances of
 * {@link Classifier} and {@link #isCreate()} is true, the switched object will be turned
 * into an instance of {@link Class}. This will generate a warning in the log.
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public class FindContainedClassifierSwitch extends UMLSwitch<Classifier> {

	protected static Logger logger = Logger.getLogger(JarToUML.LOGGER);
	
	private boolean create = false;
	private EClass metaClass = UMLPackage.eINSTANCE.getDataType();
	private String classifierName = null;
	private ReplaceByClassifierSwitch replaceByClassifierSwitch = new ReplaceByClassifierSwitch();

	public boolean isCreate() {
		return create;
	}

	public void setCreate(boolean create) {
		this.create = create;
	}

	public EClass getMetaClass() {
		return metaClass;
	}

	public void setMetaClass(EClass metaClass) {
		Assert.assertNotNull(metaClass);
		Assert.assertEquals(true, UMLPackage.eINSTANCE.getClassifier().isSuperTypeOf(metaClass));
		this.metaClass = metaClass;
	}

	public String getClassifierName() {
		return classifierName;
	}

	public void setClassifierName(String classifierName) {
		this.classifierName = classifierName;
	}

	public Classifier caseClass(Class parent) {
		String localClassName = getClassifierName();
		Assert.assertNotNull(localClassName);
		for (Iterator<Classifier> it = parent.getNestedClassifiers().iterator(); it.hasNext();) {
			Classifier cl = it.next();
			if (localClassName.equals(cl.getName())) {
				return cl;
			}
		}
		if (isCreate()) {
			Classifier child = parent.createNestedClassifier(localClassName, getMetaClass());
			child.setIsAbstract(true);
			child.setIsLeaf(true);
			return child;
		}
		return super.caseClass(parent);
	}

	public Classifier caseInterface(Interface parent) {
		String localClassName = getClassifierName();
		Assert.assertNotNull(localClassName);
		for (Iterator<Classifier> it = parent.getNestedClassifiers().iterator(); it.hasNext();) {
			Classifier cl = (Classifier) it.next();
			if (localClassName.equals(cl.getName())) {
				return cl;
			}
		}
		if (isCreate()) {
			Classifier child = parent.createNestedClassifier(localClassName, getMetaClass());
			child.setIsAbstract(true);
			child.setIsLeaf(true);
			return child;
		}
		return super.caseInterface(parent);
	}

	public Classifier casePackage(Package parent) {
		String localClassName = getClassifierName();
		Assert.assertNotNull(localClassName);
		for (Iterator<PackageableElement> it = parent.getPackagedElements().iterator(); it.hasNext();) {
			PackageableElement element = it.next();
			if (element instanceof Classifier) {
				Classifier cl = (Classifier) element;
				if (localClassName.equals(cl.getName())) {
					return cl;
				}
			}
		}
		if (isCreate()) {
			Classifier child = (Classifier) parent.createPackagedElement(localClassName, getMetaClass());
			child.setIsAbstract(true);
			child.setIsLeaf(true);
			return child;
		}
		return super.casePackage(parent);
	}

	public Classifier caseClassifier(Classifier parent) {
		if (isCreate()) {
			replaceByClassifierSwitch.setClassifier(parent);
			replaceByClassifierSwitch.setMetaClass(UMLPackage.eINSTANCE.getClass_());
			Classifier newParent = (Classifier) replaceByClassifierSwitch.doSwitch(parent.getOwner());
			replaceByClassifierSwitch.reset();
			logger.info("Classifier " + parent + " replaced by Class " + newParent + " to support nested Classifiers");
			return doSwitch(newParent);
		}
		return super.caseClassifier(parent);
	}

}
