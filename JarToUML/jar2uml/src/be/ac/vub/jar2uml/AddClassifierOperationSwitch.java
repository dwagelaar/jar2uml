package be.ac.vub.jar2uml;

import java.util.logging.Logger;

import junit.framework.Assert;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.DataType;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.ParameterDirectionKind;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.util.UMLSwitch;

public class AddClassifierOperationSwitch extends UMLSwitch {
	
	protected static Logger logger = Logger.getLogger(JarToUML.LOGGER);

	private String operationName = null;
	private EList argumentTypes = null;
	private Type returnType = null;
	private TypeToClassifierSwitch typeToClassifier = null;
	
	protected EList argNames = null;
	protected EList allArgNames = null;
	protected EList allArgTypes = null;
	
	public AddClassifierOperationSwitch(TypeToClassifierSwitch typeToClassifier) {
		Assert.assertNotNull(typeToClassifier);
		this.typeToClassifier = typeToClassifier;
	}

	public EList getArgumentTypes() {
		return argumentTypes;
	}

	public void setArgumentTypes(EList argumentTypes) {
		this.argumentTypes = argumentTypes;
	}

	public void setBCELArgumentTypes(org.apache.bcel.generic.Type[] argumentTypes) {
		setArgumentTypes(toUMLTypes(argumentTypes));
	}

	private EList toUMLTypes(org.apache.bcel.generic.Type[] types) {
		EList umlTypes = new BasicEList();
		for (int i = 0; i < types.length; i++) {
			Type type = (Type) typeToClassifier.doSwitch(types[i]);
			if (type == null) {
				logger.warning("Type not found: " +	types[i].getSignature());
			}
			umlTypes.add(type);
		}
		return umlTypes;
	}
	
	public String getOperationName() {
		return operationName;
	}

	public void setOperationName(String operationName) {
		this.operationName = operationName;
	}

	protected void prepareArgs() {
		EList args = getArgumentTypes();
		Assert.assertNotNull(args);
		argNames = toUMLArgNames(args);
		allArgNames = new BasicEList(argNames);
		allArgTypes = new BasicEList(args);
		if (getReturnType() != null) {
			allArgNames.add("return");
			allArgTypes.add(getReturnType());
		}
	}
	
	public Object caseClass(Class object) {
		String name = getOperationName();
		Assert.assertNotNull(name);
		prepareArgs();
		// names may not be null in getOwnedOperation, but return parameter name is always null?!
		Operation op = object.getOwnedOperation(name, allArgNames, allArgTypes);
		if (op == null) {
			op = object.createOwnedOperation(name, argNames, getArgumentTypes());
			if (getReturnType() != null) {
				Parameter par = op.createOwnedParameter("return", getReturnType());
				par.setDirection(ParameterDirectionKind.RETURN_LITERAL);
			}
			op.setIsLeaf(true);		//final
			op.setIsAbstract(true); //abstract
		}
		return op;
	}

	public Object caseInterface(Interface object) {
		String name = getOperationName();
		Assert.assertNotNull(name);
		prepareArgs();
		Operation op = object.getOwnedOperation(name, allArgNames, allArgTypes);
		if (op == null) {
			op = object.createOwnedOperation(name, argNames, getArgumentTypes());
			if (getReturnType() != null) {
				Parameter par = op.createOwnedParameter("return", getReturnType());
				par.setDirection(ParameterDirectionKind.RETURN_LITERAL);
			}
			op.setIsLeaf(true);		//final
			op.setIsAbstract(true); //abstract
		}
		return op;
	}

	public Object caseDataType(DataType object) {
		String name = getOperationName();
		Assert.assertNotNull(name);
		prepareArgs();
		Operation op = object.getOwnedOperation(name, allArgNames, allArgTypes);
		if (op == null) {
			op = object.createOwnedOperation(name, argNames, getArgumentTypes());
			if (getReturnType() != null) {
				Parameter par = op.createOwnedParameter("return", getReturnType());
				par.setDirection(ParameterDirectionKind.RETURN_LITERAL);
			}
			op.setIsLeaf(true);		//final
			op.setIsAbstract(true); //abstract
		}
		return op;
	}

	private static EList toUMLArgNames(EList args) {
		EList umlArgNames = new BasicEList();
		for (int i = 0; i < args.size(); i++) {
			umlArgNames.add("arg" + i);
		}
		return umlArgNames;
	}
	
	public void reset() {
		setOperationName(null);
		setArgumentTypes(null);
		setReturnType(null);
	}

	public Type getReturnType() {
		return returnType;
	}

	public void setReturnType(Type returnType) {
		this.returnType = returnType;
	}
	
	public void setBCELReturnType(org.apache.bcel.generic.Type returnType) {
		setReturnType((Type) typeToClassifier.doSwitch(returnType));
	}
	
}
