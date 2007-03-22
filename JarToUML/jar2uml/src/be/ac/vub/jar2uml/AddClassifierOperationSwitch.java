package be.ac.vub.jar2uml;

import junit.framework.Assert;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.util.UMLSwitch;

public class AddClassifierOperationSwitch extends UMLSwitch {
	
	private String operationName = null;
	private EList argumentTypes = null;

	public EList getArgumentTypes() {
		return argumentTypes;
	}

	public void setArgumentTypes(EList argumentTypes) {
		this.argumentTypes = argumentTypes;
	}

	public String getOperationName() {
		return operationName;
	}

	public void setOperationName(String operationName) {
		this.operationName = operationName;
	}

	public Object caseClass(Class object) {
		String name = getOperationName();
		EList args = getArgumentTypes();
		Assert.assertNotNull(name);
		Assert.assertNotNull(args);
		return object.createOwnedOperation(name, toUMLArgNames(args), args);
	}

	public Object caseInterface(Interface object) {
		String name = getOperationName();
		EList args = getArgumentTypes();
		Assert.assertNotNull(name);
		Assert.assertNotNull(args);
		return object.createOwnedOperation(name, toUMLArgNames(args), args);
	}

	private static EList toUMLArgNames(EList args) {
		EList umlArgNames = new BasicEList();
		for (int i = 0; i < args.size(); i++) {
			umlArgNames.add("arg" + i);
		}
		return umlArgNames;
	}
	
}
