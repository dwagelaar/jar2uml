package be.ac.vub.jar2uml;

import java.util.Iterator;

import junit.framework.Assert;

import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.EmptyVisitor;
import org.apache.bcel.generic.FieldOrMethod;
import org.apache.bcel.generic.GETFIELD;
import org.apache.bcel.generic.GETSTATIC;
import org.apache.bcel.generic.INVOKEINTERFACE;
import org.apache.bcel.generic.INVOKESPECIAL;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.PUTFIELD;
import org.apache.bcel.generic.PUTSTATIC;
import org.apache.bcel.generic.ReferenceType;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.DataType;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.VisibilityKind;

/**
 * Adds classifier fields/methods referenced by the switched bytecode instruction to the model.
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public class AddInstructionDependenciesVisitor extends EmptyVisitor {
	
	private Classifier instrContext = null;
	private ConstantPool cp = null;
	private ConstantPoolGen cpg = null;

	protected TypeToClassifierSwitch typeToClassifier = null;
	protected AddClassifierPropertySwitch addClassifierProperty = null;
	protected AddClassifierOperationSwitch addClassifierOperation = null;
	protected ReplaceByClassifierSwitch replaceByClassifier = new ReplaceByClassifierSwitch();
	protected Classifier owner = null;
	
	public AddInstructionDependenciesVisitor(
			TypeToClassifierSwitch typeToClassifierSwitch,
			AddClassifierPropertySwitch addClassifierPropertySwitch,
			AddClassifierOperationSwitch addClassifierOperationSwitch) {
		Assert.assertNotNull(typeToClassifierSwitch);
		Assert.assertNotNull(addClassifierPropertySwitch);
		Assert.assertNotNull(addClassifierOperationSwitch);
		this.typeToClassifier = typeToClassifierSwitch;
		this.addClassifierProperty = addClassifierPropertySwitch;
		this.addClassifierOperation = addClassifierOperationSwitch;
	}

	public void visitFieldOrMethod(FieldOrMethod obj) {
		Assert.assertNotNull(cpg);
		Assert.assertNotNull(typeToClassifier);
		ReferenceType fieldOwner = obj.getReferenceType(cpg);
		owner = (Classifier) typeToClassifier.doSwitch(fieldOwner);
	}

	public void visitGETFIELD(GETFIELD obj) {
		//Only classes have instance fields
		Assert.assertTrue(owner instanceof Class);
		addClassifierProperty.setPropertyName(obj.getFieldName(cpg));
		addClassifierProperty.setBCELPropertyType(obj.getFieldType(cpg));
		Property att = (Property) addClassifierProperty.doSwitch(owner);
		if (getInstrContext().conformsTo(owner)) {
			att.setVisibility(VisibilityKind.PROTECTED_LITERAL);
			//fields cannot be redefined in Java, so not possible to check on 'isLeaf'
		}
		att.setIsStatic(false);
	}

	public void visitGETSTATIC(GETSTATIC obj) {
		//Can be invoked on interfaces as well as classes (static final) -> allow DataType
		Assert.assertTrue((owner instanceof Class) || (owner instanceof Interface) || (owner instanceof DataType));
		addClassifierProperty.setPropertyName(obj.getFieldName(cpg));
		addClassifierProperty.setBCELPropertyType(obj.getFieldType(cpg));
		Property att = (Property) addClassifierProperty.doSwitch(owner);
		if (getInstrContext().conformsTo(owner)) {
			att.setVisibility(VisibilityKind.PROTECTED_LITERAL);
			//fields cannot be redefined in Java, so not possible to check on 'isLeaf'
		}
		att.setIsStatic(true);
	}

	public void visitInvokeInstruction(InvokeInstruction obj) {
		addClassifierOperation.setOperationName(obj.getMethodName(cpg));
		addClassifierOperation.setBCELArgumentTypes(obj.getArgumentTypes(cpg));
		addClassifierOperation.setBCELReturnType(obj.getReturnType(cpg));
	}

	public void visitINVOKEINTERFACE(INVOKEINTERFACE obj) {
		//Can be invoked only on interfaces
		Assert.assertTrue(owner instanceof Interface);
		Operation newOp = (Operation) addClassifierOperation.doSwitch(owner);
		Assert.assertNotNull(getInstrContext());
		if (getInstrContext().conformsTo(owner)) {
			newOp.setVisibility(VisibilityKind.PROTECTED_LITERAL);
			EList<Parameter> params = newOp.getOwnedParameters();
			Operation childOp = getInstrContext().getOperation(newOp.getName(), getParameterNames(params), getParameterTypes(params));
			if (childOp != null) {
				newOp.setIsLeaf(false);
			}
		}
		newOp.setIsAbstract(true);
	}
	
	private EList<String> getParameterNames(EList<Parameter> parameters) {
		EList<String> names = new BasicEList<String>();
		for (Iterator<Parameter> i = parameters.iterator(); i.hasNext();) {
			Parameter par = i.next();
			names.add(par.getName());
		}
		return names;
	}

	private EList<Type> getParameterTypes(EList<Parameter> parameters) {
		EList<Type> types = new BasicEList<Type>();
		for (Iterator<Parameter> i = parameters.iterator(); i.hasNext();) {
			Parameter par = i.next();
			types.add(par.getType());
		}
		return types;
	}

	public void visitINVOKESPECIAL(INVOKESPECIAL obj) {
		//Can be invoked only on classes (<init>, superclass methods and private methods)
		Assert.assertTrue(owner instanceof Class);
		Operation newOp = (Operation) addClassifierOperation.doSwitch(owner);
		Assert.assertNotNull(getInstrContext());
		if (getInstrContext().conformsTo(owner)) {
			newOp.setVisibility(VisibilityKind.PROTECTED_LITERAL);
			EList<Parameter> params = newOp.getOwnedParameters();
			Operation childOp = getInstrContext().getOperation(newOp.getName(), getParameterNames(params), getParameterTypes(params));
			if (childOp != null) {
				newOp.setIsLeaf(false);
			}
		}
		newOp.setIsAbstract(false); //these methods are never abstract
	}

	public void visitINVOKESTATIC(INVOKESTATIC obj) {
		//Can be invoked only on classes (interfaces cannot contain static method headers)
		Assert.assertTrue(owner instanceof Class);
		Operation newOp = (Operation) addClassifierOperation.doSwitch(owner);
		Assert.assertNotNull(getInstrContext());
		if (getInstrContext().conformsTo(owner)) {
			newOp.setVisibility(VisibilityKind.PROTECTED_LITERAL);
			EList<Parameter> params = newOp.getOwnedParameters();
			Operation childOp = getInstrContext().getOperation(newOp.getName(), getParameterNames(params), getParameterTypes(params));
			if (childOp != null) {
				newOp.setIsLeaf(false);
			}
		}
		newOp.setIsAbstract(false); //these methods are never abstract
		newOp.setIsStatic(true);
	}

	public void visitINVOKEVIRTUAL(INVOKEVIRTUAL obj) {
		//Can be invoked only on classes (refers to all remaining non-interface methods)
		Assert.assertTrue(owner instanceof Class);
		Operation newOp = (Operation) addClassifierOperation.doSwitch(owner);
		Assert.assertNotNull(getInstrContext());
		if (getInstrContext().conformsTo(owner)) {
			newOp.setVisibility(VisibilityKind.PROTECTED_LITERAL);
		}
		newOp.setIsAbstract(true);
	}

	public void visitPUTFIELD(PUTFIELD obj) {
		//Only classes have instance fields
		Assert.assertTrue(owner instanceof Class);
		addClassifierProperty.setPropertyName(obj.getFieldName(cpg));
		addClassifierProperty.setBCELPropertyType(obj.getFieldType(cpg));
		Property att = (Property) addClassifierProperty.doSwitch(owner);
		Assert.assertNotNull(getInstrContext());
		if (getInstrContext().conformsTo(owner)) {
			att.setVisibility(VisibilityKind.PROTECTED_LITERAL);
			//fields cannot be redefined in Java, so not possible to check on 'isLeaf'
			//even 'final' (isReadOly) fields can be 'put' once
		}
		att.setIsStatic(false);
	}

	public void visitPUTSTATIC(PUTSTATIC obj) {
		//Can be invoked on interfaces as well as classes (static final) -> allow DataType
		Assert.assertTrue((owner instanceof Class) || (owner instanceof Interface) || (owner instanceof DataType));
		addClassifierProperty.setPropertyName(obj.getFieldName(cpg));
		addClassifierProperty.setBCELPropertyType(obj.getFieldType(cpg));
		Property att = (Property) addClassifierProperty.doSwitch(owner);
		Assert.assertNotNull(getInstrContext());
		if (getInstrContext().conformsTo(owner)) {
			att.setVisibility(VisibilityKind.PROTECTED_LITERAL);
			//fields cannot be redefined in Java, so not possible to check on 'isLeaf'
			//even 'final' (isReadOnly) fields can be 'put' once
		}
		att.setIsStatic(true);
	}

	public ConstantPool getCp() {
		return cp;
	}

	public void setCp(ConstantPool cp) {
		this.cp = cp;
		if (cp == null) {
			this.cpg = null;
		} else {
			this.cpg = new ConstantPoolGen(cp);
		}
	}

	public ConstantPoolGen getCpg() {
		return cpg;
	}

	public Classifier getInstrContext() {
		return instrContext;
	}

	public void setInstrContext(Classifier instrContext) {
		this.instrContext = instrContext;
	}

}
