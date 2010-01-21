package be.ac.vub.jar2uml;

import junit.framework.Assert;

import org.apache.bcel.generic.ArrayType;
import org.apache.bcel.generic.BasicType;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.verifier.structurals.UninitializedObjectType;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.UMLPackage;

/**
 * Returns the corresponding UML type for a given BCEL type. 
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public class TypeToClassifierSwitch extends TypeSwitch<Classifier> {
	
	private Package root = null;
	private FindContainedClassifierSwitch findContainedClassifier = new FindContainedClassifierSwitch();
	
	public Classifier caseArrayType(ArrayType type) {
		Classifier inner = doSwitch(type.getElementType());
		Assert.assertNotNull(inner);
		return findContainedClassifier.findLocalClassifier(
				inner.getOwner(), 
				inner.getName() + "[]", 
				UMLPackage.eINSTANCE.getDataType());
	}

	public Classifier caseBasicType(BasicType type) {
		if (BasicType.BOOLEAN.equals(type)) {
			return findContainedClassifier.findPrimitiveType(root, "java.lang.boolean", true);
		} else if (BasicType.BYTE.equals(type)) {
			return findContainedClassifier.findPrimitiveType(root, "java.lang.byte", true);
		} else if (BasicType.CHAR.equals(type)) {
			return findContainedClassifier.findPrimitiveType(root, "java.lang.char", true);
		} else if (BasicType.DOUBLE.equals(type)) {
			return findContainedClassifier.findPrimitiveType(root, "java.lang.double", true);
		} else if (BasicType.FLOAT.equals(type)) {
			return findContainedClassifier.findPrimitiveType(root, "java.lang.float", true);
		} else if (BasicType.INT.equals(type)) {
			return findContainedClassifier.findPrimitiveType(root, "java.lang.int", true);
		} else if (BasicType.LONG.equals(type)) {
			return findContainedClassifier.findPrimitiveType(root, "java.lang.long", true);
		} else if (BasicType.SHORT.equals(type)) {
			return findContainedClassifier.findPrimitiveType(root, "java.lang.short", true);
		} else {
			return null;
		}
	}

	public Classifier caseObjectType(ObjectType type) {
		Assert.assertNotNull(root);
		return findContainedClassifier.findClassifier(root, type.getClassName(), UMLPackage.eINSTANCE.getDataType());
	}

	public Classifier caseUninitializedObjectType(UninitializedObjectType type) {
		logger.warning("What is an UninitializedObjectType?! " + type);
		return doSwitch(type.getInitialized());
	}

	public Package getRoot() {
		return root;
	}

	public void setRoot(Package root) {
		this.root = root;
	}
	
	public void reset() {
		setRoot(null);
	}
	
}
