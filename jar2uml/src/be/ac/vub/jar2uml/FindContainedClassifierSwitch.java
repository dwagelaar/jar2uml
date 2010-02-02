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

import java.util.Iterator;
import java.util.logging.Logger;

import junit.framework.Assert;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.DataType;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.PackageableElement;
import org.eclipse.uml2.uml.PrimitiveType;
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
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public class FindContainedClassifierSwitch extends UMLSwitch<Classifier> {

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

	public Classifier caseClass(Class parent) {
		String localClassName = getClassifierName();
		Assert.assertNotNull(localClassName);
		for (Iterator<Classifier> it = parent.getNestedClassifiers().iterator(); it.hasNext();) {
			Classifier cl = it.next();
			if (localClassName.equals(cl.getName())) {
				return cl;
			}
		}
		if (isCreate()) {
			Classifier child = parent.createNestedClassifier(localClassName, getMetaClass());
			child.setIsAbstract(true);
			child.setIsLeaf(true);
			return child;
		}
		return super.caseClass(parent);
	}

	public Classifier caseInterface(Interface parent) {
		String localClassName = getClassifierName();
		Assert.assertNotNull(localClassName);
		for (Iterator<Classifier> it = parent.getNestedClassifiers().iterator(); it.hasNext();) {
			Classifier cl = (Classifier) it.next();
			if (localClassName.equals(cl.getName())) {
				return cl;
			}
		}
		if (isCreate()) {
			Classifier child = parent.createNestedClassifier(localClassName, getMetaClass());
			child.setIsAbstract(true);
			child.setIsLeaf(true);
			return child;
		}
		return super.caseInterface(parent);
	}

	public Classifier casePackage(Package parent) {
		String localClassName = getClassifierName();
		Assert.assertNotNull(localClassName);
		for (Iterator<PackageableElement> it = parent.getPackagedElements().iterator(); it.hasNext();) {
			PackageableElement element = it.next();
			if (element instanceof Classifier) {
				Classifier cl = (Classifier) element;
				if (localClassName.equals(cl.getName())) {
					return cl;
				}
			}
		}
		if (isCreate()) {
			Classifier child = (Classifier) parent.createPackagedElement(localClassName, getMetaClass());
			child.setIsAbstract(true);
			child.setIsLeaf(true);
			return child;
		}
		return super.casePackage(parent);
	}

	public Classifier caseClassifier(Classifier parent) {
		if (isCreate()) {
			replaceByClassifierSwitch.setClassifier(parent);
			replaceByClassifierSwitch.setMetaClass(UMLPackage.eINSTANCE.getClass_());
			Classifier newParent = (Classifier) replaceByClassifierSwitch.doSwitch(parent.getOwner());
			logger.info(String.format(
					JarToUML.getString("FindContainedClassifierSwitch.replacedByClass"), 
					parent,
					newParent)); //$NON-NLS-1$
			return doSwitch(newParent);
		}
		return super.caseClassifier(parent);
	}

	/**
	 * @param parent The parent element to search at, e.g. "java.lang.Class".
	 * @param localClassName The local classifier name (e.g. "Inner")
	 * @param createAs If not null and classifier is not found, an instance of this meta-class is created.
	 * @return A {@link Classifier} with name localClassName. If createAs is not null
	 * and the classifier was not found, a new instance of the createAs meta-class is
	 * created and returned.
	 */
	public Classifier findLocalClassifier(Element parent, String localClassName, EClass createAs) {
		Assert.assertNotNull(parent);
		Assert.assertNotNull(localClassName);
		setClassifierName(localClassName);
		if (createAs != null) {
			setMetaClass(createAs);
			setCreate(true);
		} else {
			setCreate(false);
		}
		return doSwitch(parent);
	}

	/**
	 * @param root The model root to start searching at.
	 * @param className The fully qualified classifier name (e.g. "java.lang.Class$Inner")
	 * @param createAs If not null and classifier is not found, an instance of this meta-class is created.
	 * @return A {@link Classifier} with qualified name className. If createAs is not null
	 * and the classifier was not found, a new instance of the createAs meta-class is
	 * created and returned.
	 */
	public Classifier findClassifier(Package root, String className, EClass createAs) {
		Assert.assertNotNull(className);
		String localClassName = className;
		Package parent = root;
		String tail = className.substring(className.lastIndexOf('.') + 1);
		if (tail.length() < className.length()) {
			String parentName = className.substring(0, className.length() - tail.length() - 1);
			parent = findPackage(root, parentName, createAs != null);
			localClassName = tail;
		}
		if (parent == null) {
			return null;
		}
		Classifier parentClass = null;
		tail = className.substring(className.lastIndexOf('$') + 1);
		if (tail.length() < className.length()) {
			String parentName = className.substring(0, className.length() - tail.length() - 1);
			parentClass = findClassifier(root, parentName, createAs);
			localClassName = tail;
		}
		if (parentClass != null) {
			return findLocalClassifier(parentClass, localClassName, createAs);
		} else {
			return findLocalClassifier(parent, localClassName, createAs);
		}
	}

	/**
	 * Finds a package in the UML model, starting from root.
	 * @param root The root node in the UML model to search under.
	 * @param packageName The qualified Java package name relative to root.
	 * @param create If true, a new package is created if the package is not found.
	 * @return The requested package or null if create is false and no package is found.
	 */
	public Package findPackage(Package root, String packageName, boolean create) {
		Assert.assertNotNull(packageName);
		Package parent = root;
		final String tail = packageName.substring(packageName.lastIndexOf('.') + 1);
		if (tail.length() < packageName.length()) {
			String parentName = packageName.substring(0, packageName.length() - tail.length() - 1);
			parent = findPackage(root, parentName, create);
			packageName = tail;
		}
		if (parent == null) {
			return null;
		}
		for (Iterator<Package> it = parent.getNestedPackages().iterator(); it.hasNext();) {
			Package pack = it.next();
			if (packageName.equals(pack.getName())) {
				return pack;
			}
		}
		if (create) {
			return parent.createNestedPackage(packageName);
		} else {
			return null;
		}
	}

	/**
	 * Finds the primitive type with name typeName in the UML model.
	 * @param root The model root to start searching at.
	 * @param typeName The fully qualified type name (e.g. "java.lang.int").
	 * @param create If true, create a new primitive type if not found.
	 * @return the {@link PrimitiveType} with name typeName. If create is true and
	 * the primitive type is not found, a new instance of {@link PrimitiveType} is
	 * created and returned.
	 */
	public PrimitiveType findPrimitiveType(Package root, String typeName, boolean create) {
		Assert.assertNotNull(typeName);
		String localTypeName = typeName;
		Package parent = root;
		String tail = typeName.substring(typeName.lastIndexOf('.') + 1);
		if (tail.length() < typeName.length()) {
			String parentName = typeName.substring(0, typeName.length() - tail.length() - 1);
			parent = findPackage(root, parentName, create);
			localTypeName = tail;
		}
		if (parent == null) {
			return null;
		}
		for (Iterator<PackageableElement> it = parent.getPackagedElements().iterator(); it.hasNext();) {
			PackageableElement element = it.next();
			if (element instanceof PrimitiveType) {
				PrimitiveType type = (PrimitiveType) element;
				if (localTypeName.equals(type.getName())) {
					return type;
				}
			}
		}
		if (create) {
			return parent.createOwnedPrimitiveType(localTypeName);
		}
		return null;
	}

}
