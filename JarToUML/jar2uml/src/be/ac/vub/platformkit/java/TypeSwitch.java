/**
 * 
 */
package be.ac.vub.platformkit.java;

import java.util.logging.Logger;

import org.apache.bcel.generic.ArrayType;
import org.apache.bcel.generic.BasicType;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.ReferenceType;
import org.apache.bcel.generic.Type;
import org.apache.bcel.verifier.structurals.UninitializedObjectType;

/**
 * Switches between various {@link Type} subclasses.
 * @author dennis
 *
 */
public class TypeSwitch {
	
	protected static Logger logger = Logger.getLogger(JarToUML.LOGGER);
	
	public Object doSwitch(Type type) {
		if (type instanceof BasicType) {
			Object result = caseBasicType((BasicType) type);
			if (result != null) return result;
		}
		if (type instanceof ArrayType) {
			Object result = caseArrayType((ArrayType) type);
			if (result != null) return result;
		}
		if (type instanceof ObjectType) {
			Object result = caseObjectType((ObjectType) type);
			if (result != null) return result;
		}
		if (type instanceof UninitializedObjectType) {
			Object result = caseUninitializedObjectType((UninitializedObjectType) type);
			if (result != null) return result;
		}
		if (type instanceof ReferenceType) {
			Object result = caseReferenceType((ReferenceType) type);
			if (result != null) return result;
		}
		return defaultCase(type);
	}
	
	public Object caseBasicType(BasicType type) {
		return null;
	}
	
	public Object caseArrayType(ArrayType type) {
		return null;
	}
	
	public Object caseObjectType(ObjectType type) {
		return null;
	}
	
	public Object caseUninitializedObjectType(UninitializedObjectType type) {
		return null;
	}
	
	public Object caseReferenceType(ReferenceType type) {
		return null;
	}
	
	public Object defaultCase(Type type) {
		return null;
	}
}