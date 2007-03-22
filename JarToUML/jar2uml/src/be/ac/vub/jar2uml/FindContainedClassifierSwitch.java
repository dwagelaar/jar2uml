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
 * @author dennis
 *
 */
public class FindContainedClassifierSwitch extends UMLSwitch {

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

	public Object caseClass(Class parent) {
		String localClassName = getClassifierName();
		Assert.assertNotNull(localClassName);
		for (Iterator it = parent.getNestedClassifiers().iterator(); it.hasNext();) {
			Classifier cl = (Classifier) it.next();
			if (localClassName.equals(cl.getName())) {
				return cl;
			}
		}
		if (isCreate()) {
			return parent.createNestedClassifier(localClassName, getMetaClass());
		}
		return super.caseClass(parent);
	}

	public Object caseInterface(Interface parent) {
		String localClassName = getClassifierName();
		Assert.assertNotNull(localClassName);
		for (Iterator it = parent.getNestedClassifiers().iterator(); it.hasNext();) {
			Classifier cl = (Classifier) it.next();
			if (localClassName.equals(cl.getName())) {
				return cl;
			}
		}
		if (isCreate()) {
			return parent.createNestedClassifier(localClassName, getMetaClass());
		}
		return super.caseInterface(parent);
	}

	public Object casePackage(Package parent) {
		String localClassName = getClassifierName();
		Assert.assertNotNull(localClassName);
		for (Iterator it = parent.getPackagedElements().iterator(); it.hasNext();) {
			Object element = it.next();
			if (element instanceof Classifier) {
				Classifier cl = (Classifier) element;
				if (localClassName.equals(cl.getName())) {
					return cl;
				}
			}
		}
		if (create) {
			return parent.createPackagedElement(localClassName, getMetaClass());
		}
		return super.casePackage(parent);
	}

	public Object caseClassifier(Classifier parent) {
		if (isCreate()) {
			replaceByClassifierSwitch.setClassifier(parent);
			replaceByClassifierSwitch.setMetaClass(UMLPackage.eINSTANCE.getClass_());
			Classifier newParent = (Classifier) replaceByClassifierSwitch.doSwitch(parent);
			logger.warning("Classifier " + parent + " replaced by Class " + newParent + " to support nested Classifiers");
			return doSwitch(newParent);
		}
		return super.caseClassifier(parent);
	}

}
