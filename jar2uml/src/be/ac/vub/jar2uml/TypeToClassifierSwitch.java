package be.ac.vub.jar2uml;

import junit.framework.Assert;

import org.apache.bcel.generic.ArrayType;
import org.apache.bcel.generic.BasicType;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.verifier.structurals.UninitializedObjectType;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.UMLPackage;

public class TypeToClassifierSwitch extends TypeSwitch {
	
	private Package root = null;
	
	public Object caseArrayType(ArrayType type) {
		Classifier inner = (Classifier) doSwitch(type.getElementType());
		Assert.assertNotNull(inner);
		return JarToUML.findClassifier(
				inner.getNearestPackage(), 
				inner.getName() + "[]", 
				UMLPackage.eINSTANCE.getDataType());
	}

	public Object caseBasicType(BasicType type) {
		if (BasicType.BOOLEAN.equals(type)) {
			return JarToUML.findPrimitiveType(root, "java.lang.boolean", true);
		} else if (BasicType.BYTE.equals(type)) {
			return JarToUML.findPrimitiveType(root, "java.lang.byte", true);
		} else if (BasicType.CHAR.equals(type)) {
			return JarToUML.findPrimitiveType(root, "java.lang.char", true);
		} else if (BasicType.DOUBLE.equals(type)) {
			return JarToUML.findPrimitiveType(root, "java.lang.double", true);
		} else if (BasicType.FLOAT.equals(type)) {
			return JarToUML.findPrimitiveType(root, "java.lang.float", true);
		} else if (BasicType.INT.equals(type)) {
			return JarToUML.findPrimitiveType(root, "java.lang.int", true);
		} else if (BasicType.LONG.equals(type)) {
			return JarToUML.findPrimitiveType(root, "java.lang.long", true);
		} else if (BasicType.SHORT.equals(type)) {
			return JarToUML.findPrimitiveType(root, "java.lang.short", true);
		} else {
			return null;
		}
	}

	public Object caseObjectType(ObjectType type) {
		Assert.assertNotNull(root);
		return JarToUML.findClassifier(root, type.getClassName(), UMLPackage.eINSTANCE.getDataType());
	}

	public Object caseUninitializedObjectType(UninitializedObjectType type) {
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
