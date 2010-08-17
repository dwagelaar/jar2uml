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

import java.io.IOException;
import java.util.Collection;

import org.apache.bcel.classfile.Attribute;
import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.classfile.StackMap;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Property;

/**
 * Adds {@link Classifier} {@link Operation}s and {@link Property}s to the UML {@link Model}.
 * This represents the first pass in {@link JarToUML}.
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public class AddProperties extends AddToModel {

	/**
	 * @param code
	 * @return True if the code has been preverified for CLDC execution, i.e. it has a StackMap attribute
	 */
	public static boolean isPreverified(Code code) {
		if (code == null) {
			return false;
		}
		for (Attribute att : code.getAttributes()) {
			if (att instanceof StackMap) {
				return true;
			}
		}
		return false;
	}

	protected final AddMethodOpCode addMethodOpCode;

	private boolean preverified;

	/**
	 * Creates a new {@link AddProperties}.
	 * @param filter A filter to apply to model operations.
	 * @param monitor A progress monitor to check for end user cancellation.
	 * @param model The UML model to store generated elements in.
	 * @param includeFeatures Whether to include fields and methods.
	 * @param includeInstructionReferences Whether or not to include Java elements that are
	 * referred to by bytecode instructions.
	 */
	public AddProperties(Filter filter, IProgressMonitor monitor, Model model,
			boolean includeFeatures, boolean includeInstructionReferences) {
		super(filter, monitor, model, includeFeatures, includeInstructionReferences);
		addMethodOpCode = new AddMethodOpCode(filter, monitor, model,
				includeFeatures, includeInstructionReferences);
	}

	/**
	 * Adds the properties of all classifiers in parsedClasses to the classifiers in the UML model.
	 * @param parsedClasses
	 * @throws IOException
	 * @throws JarToUMLException 
	 */
	public void addAllProperties(Collection<JavaClass> parsedClasses) throws IOException, JarToUMLException {
		if (isIncludeFeatures()) {
			for (JavaClass javaClass : parsedClasses) {
				addClassifierProperties(javaClass);
				checkCancelled();
			}
		}
	}

	/**
	 * Adds the properties of the javaClass to the corresponding classifier in the UML model.
	 * @param javaClass The BCEL class representation to convert.
	 * @throws JarToUMLException 
	 */
	public void addClassifierProperties(JavaClass javaClass) throws JarToUMLException {
		final String className = javaClass.getClassName();
		if (!filter(javaClass)) {
			logSkippedFiltered(javaClass);
			return;
		}
		JarToUML.logger.finer(className);
		final Classifier classifier = findContainedClassifier.findClassifier(
				getModel(), className, null);
		addProperties(classifier, javaClass);
		addOperations(classifier, javaClass);
	}

	/**
	 * Adds a property to classifier for each javaClass field.
	 * @param classifier The classifier representation of javaClass.
	 * @param javaClass The Java class file to convert.
	 * @throws JarToUMLException 
	 */
	public void addProperties(Classifier classifier, JavaClass javaClass) throws JarToUMLException {
		assert classifier != null;
		Field[] fields = javaClass.getFields();
		for (int i = 0; i < fields.length; i++) {
			if (!filter(fields[i])) {
				continue;
			}
			JarToUML.logger.finest(fields[i].getSignature());
			addClassifierProperty.setPropertyName(fields[i].getName());
			addClassifierProperty.setBCELPropertyType(fields[i].getType());
			if (addClassifierProperty.getPropertyType() == null) {
				throw new JarToUMLException(String.format(
						JarToUMLResources.getString("typeNotFoundFor"), 
						javaClass.getClassName(),
						fields[i].getName(),
						fields[i].getType().getSignature())); //$NON-NLS-1$
			}
			Property prop = (Property) addClassifierProperty.doSwitch(classifier);
			prop.setVisibility(JarToUML.toUMLVisibility(fields[i]));
			prop.setIsStatic(fields[i].isStatic());
			prop.setIsReadOnly(fields[i].isFinal());
			prop.setIsLeaf(fields[i].isFinal());
		}
	}

	/**
	 * Adds an operation to classifier for each javaClass method.
	 * @param classifier The classifier representation of javaClass.
	 * @param javaClass The Java class file to convert.
	 * @throws JarToUMLException 
	 */
	public void addOperations(Classifier classifier, JavaClass javaClass) throws JarToUMLException {
		assert classifier != null;
		Method[] methods = javaClass.getMethods();
		for (int i = 0; i < methods.length; i++) {
			if (getFilter() != null) {
				if (!getFilter().filter(methods[i])) {
					continue;
				}
			}
			JarToUML.logger.finest(methods[i].getSignature());
			addClassifierOperation.setAll(methods[i]);
			Operation op = (Operation) addClassifierOperation.doSwitch(classifier);
			op.setVisibility(JarToUML.toUMLVisibility(methods[i]));
			op.setIsAbstract(methods[i].isAbstract());
			op.setIsStatic(methods[i].isStatic());
			op.setIsLeaf(methods[i].isFinal());
			addMethodOpCode.addOpCode(classifier, javaClass, methods[i]);
			if (isPreverified(methods[i].getCode())) {
				setPreverified(true);
			}
		}
	}

	/**
	 * Whether or not the bytecode has been preverified for execution on J2ME CLDC.
	 * @return the preverified
	 */
	public boolean isPreverified() {
		return preverified;
	}

	/**
	 * @param preverified the preverified to set
	 */
	protected void setPreverified(boolean preverified) {
		this.preverified = preverified;
	}

}
