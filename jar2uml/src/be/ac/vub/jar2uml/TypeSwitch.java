/*******************************************************************************
 * Copyright (c) 2007-2010 Dennis Wagelaar, Vrije Universiteit Brussel.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dennis Wagelaar, Vrije Universiteit Brussel
 *******************************************************************************/
package be.ac.vub.jar2uml;

import java.util.logging.Logger;

import org.apache.bcel.generic.ArrayType;
import org.apache.bcel.generic.BasicType;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.ReferenceType;
import org.apache.bcel.generic.Type;
import org.apache.bcel.verifier.structurals.UninitializedObjectType;

/**
 * Switches between various {@link Type} subclasses.
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public class TypeSwitch<T> {

	protected static Logger logger = Logger.getLogger(JarToUML.LOGGER);

	public T doSwitch(Type type) {
		if (type instanceof BasicType) {
			T result = caseBasicType((BasicType) type);
			if (result != null) return result;
		}
		if (type instanceof ArrayType) {
			T result = caseArrayType((ArrayType) type);
			if (result != null) return result;
		}
		if (type instanceof ObjectType) {
			T result = caseObjectType((ObjectType) type);
			if (result != null) return result;
		}
		if (type instanceof UninitializedObjectType) {
			T result = caseUninitializedObjectType((UninitializedObjectType) type);
			if (result != null) return result;
		}
		if (type instanceof ReferenceType) {
			T result = caseReferenceType((ReferenceType) type);
			if (result != null) return result;
		}
		return defaultCase(type);
	}

	public T caseBasicType(BasicType type) {
		return null;
	}

	public T caseArrayType(ArrayType type) {
		return null;
	}

	public T caseObjectType(ObjectType type) {
		return null;
	}

	public T caseUninitializedObjectType(UninitializedObjectType type) {
		return null;
	}

	public T caseReferenceType(ReferenceType type) {
		return null;
	}

	public T defaultCase(Type type) {
		return null;
	}
}