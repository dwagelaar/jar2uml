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

	private boolean create = false;
	private boolean created = false;
	private EClass metaClass = UMLPackage.eINSTANCE.getDataType();
	private String classifierName = null;
	private ReplaceByClassifierSwitch replaceByClassifierSwitch = new ReplaceByClassifierSwitch();

	/**
	 * @return Whether to create a new {@link Classifier} instance if none found.
	 */
	public boolean isCreate() {
		return create;
	}

	/**
	 * Sets whether to create a new {@link Classifier} instance if none found.
	 * @param create
	 */
	public void setCreate(boolean create) {
		this.create = create;
	}

	/**
	 * @return whether or not a new {@link Classifier} was actually created in the last {@link #doSwitch(org.eclipse.emf.ecore.EObject)}.
	 */
	public boolean isCreated() {
		return created;
	}

	/**
	 * Sets whether or not a new {@link Classifier} was actually created in the last {@link #doSwitch(org.eclipse.emf.ecore.EObject)}.
	 * @param created the created to set
	 */
	protected void setCreated(boolean created) {
		this.created = created;
	}

	/**
	 * @return The {@link EClass} of the instance to create if none found.
	 */
	public EClass getMetaClass() {
		return metaClass;
	}

	/**
	 * Sets the {@link EClass} of the instance to create if none found.
	 * @param metaClass
	 */
	public void setMetaClass(EClass metaClass) {
		assert metaClass != null;
		assert UMLPackage.eINSTANCE.getClassifier().isSuperTypeOf(metaClass);
		this.metaClass = metaClass;
	}

	/**
	 * @return The local name of the {@link Classifier} to be found.
	 */
	public String getClassifierName() {
		return classifierName;
	}

	/**
	 * Sets the local name of the {@link Classifier} to be found.
	 * @param classifierName
	 */
	public void setClassifierName(String classifierName) {
		this.classifierName = classifierName;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.uml2.uml.util.UMLSwitch#caseClass(org.eclipse.uml2.uml.Class)
	 */
	@Override
	public Classifier caseClass(Class parent) {
		setCreated(false);
		final String localClassName = getClassifierName();
		assert localClassName != null;
		for (Iterator<Classifier> it = parent.getNestedClassifiers().iterator(); it.hasNext();) {
			Classifier cl = it.next();
			if (localClassName.equals(cl.getName())) {
				return cl;
			}
		}
		if (isCreate()) {
			setCreated(true);
			Classifier child = parent.createNestedClassifier(localClassName, getMetaClass());
			child.setIsAbstract(true);
			child.setIsLeaf(true);
			return child;
		}
		return super.caseClass(parent);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.uml2.uml.util.UMLSwitch#caseInterface(org.eclipse.uml2.uml.Interface)
	 */
	@Override
	public Classifier caseInterface(Interface parent) {
		setCreated(false);
		final String localClassName = getClassifierName();
		assert localClassName != null;
		for (Iterator<Classifier> it = parent.getNestedClassifiers().iterator(); it.hasNext();) {
			Classifier cl = (Classifier) it.next();
			if (localClassName.equals(cl.getName())) {
				return cl;
			}
		}
		if (isCreate()) {
			setCreated(true);
			Classifier child = parent.createNestedClassifier(localClassName, getMetaClass());
			child.setIsAbstract(true);
			child.setIsLeaf(true);
			return child;
		}
		return super.caseInterface(parent);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.uml2.uml.util.UMLSwitch#casePackage(org.eclipse.uml2.uml.Package)
	 */
	@Override
	public Classifier casePackage(Package parent) {
		setCreated(false);
		final String localClassName = getClassifierName();
		assert localClassName != null;
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
			setCreated(true);
			Classifier child = (Classifier) parent.createPackagedElement(localClassName, getMetaClass());
			child.setIsAbstract(true);
			child.setIsLeaf(true);
			return child;
		}
		return super.casePackage(parent);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.uml2.uml.util.UMLSwitch#caseClassifier(org.eclipse.uml2.uml.Classifier)
	 */
	@Override
	public Classifier caseClassifier(Classifier parent) {
		setCreated(false);
		if (isCreate()) {
			replaceByClassifierSwitch.setClassifier(parent);
			replaceByClassifierSwitch.setMetaClass(UMLPackage.eINSTANCE.getClass_());
			JarToUML.logger.info(String.format(
					JarToUMLResources.getString("FindContainedClassifierSwitch.replacingByClass"), 
					JarToUML.qualifiedName(parent),
					parent.eClass().getName())); //$NON-NLS-1$
			Classifier newParent = (Classifier) replaceByClassifierSwitch.doSwitch(parent.getOwner());
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
		assert parent != null;
		assert localClassName != null;
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
		assert className != null;
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
		setCreated(false);
		assert packageName != null;
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
			setCreated(true);
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
		setCreated(false);
		assert typeName != null;
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
			setCreated(true);
			return parent.createOwnedPrimitiveType(localTypeName);
		}
		return null;
	}

}
