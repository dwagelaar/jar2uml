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

/**
 * Adds an operation to the switched element (class, interface or datatype).
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public class AddClassifierOperationSwitch extends UMLSwitch<Operation> {
	
	protected static Logger logger = Logger.getLogger(JarToUML.LOGGER);

	private String operationName = null;
	private EList<Type> argumentTypes = null;
	private Type returnType = null;
	private TypeToClassifierSwitch typeToClassifier = null;
	
	protected EList<String> argNames = null;
	protected EList<String> allArgNames = null;
	protected EList<Type> allArgTypes = null;
	
	public AddClassifierOperationSwitch(TypeToClassifierSwitch typeToClassifier) {
		Assert.assertNotNull(typeToClassifier);
		this.typeToClassifier = typeToClassifier;
	}

	public EList<Type> getArgumentTypes() {
		return argumentTypes;
	}

	public void setArgumentTypes(EList<Type> argumentTypes) {
		this.argumentTypes = argumentTypes;
	}

	public void setBCELArgumentTypes(org.apache.bcel.generic.Type[] argumentTypes) {
		setArgumentTypes(toUMLTypes(argumentTypes));
	}

	private EList<Type> toUMLTypes(org.apache.bcel.generic.Type[] types) {
		EList<Type> umlTypes = new BasicEList<Type>();
		for (int i = 0; i < types.length; i++) {
			Type type = typeToClassifier.doSwitch(types[i]);
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
		EList<Type> args = getArgumentTypes();
		Assert.assertNotNull(args);
		argNames = toUMLArgNames(args);
		allArgNames = new BasicEList<String>(argNames);
		allArgTypes = new BasicEList<Type>(args);
		if (getReturnType() != null) {
			allArgNames.add("return");
			allArgTypes.add(getReturnType());
		}
	}
	
	public Operation caseClass(Class object) {
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

	public Operation caseInterface(Interface object) {
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

	public Operation caseDataType(DataType object) {
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

	private static EList<String> toUMLArgNames(EList<?> args) {
		EList<String> umlArgNames = new BasicEList<String>();
		for (int i = 0; i < args.size(); i++) {
			umlArgNames.add("arg" + i);
		}
		return umlArgNames;
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
