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
import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.PackageableElement;

/**
 * Operations that remove elements from the UML {@link Model}.
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public class RemoveFromModel extends ChangeModel {

	protected RemoveClassifierSwitch removeClassifier = new RemoveClassifierSwitch();
	protected RemoveClassifierPropertiesSwitch removeClassifierProperties = new RemoveClassifierPropertiesSwitch();

	/**
	 * Creates a new {@link RemoveFromModel}.
	 * @param filter A filter to apply to model operations.
	 * @param monitor A progress monitor to check for end user cancellation.
	 * @param model The UML model to store generated elements in.
	 */
	public RemoveFromModel(Filter filter, IProgressMonitor monitor, Model model) {
		super(filter, monitor, model);
	}

	/**
	 * Removes all classifiers in removeClassifiers from the UML model.
	 * @param removeClassifiers
	 * @throws IOException
	 */
	public void removeAllClassifiers(Collection<? extends Classifier> removeClassifiers) throws IOException {
		for (Classifier classifier : removeClassifiers) {
			removeClassifier(classifier);
			checkCancelled();
		}
	}

	/**
	 * Removes all properties of classifiers in removeClassifiers from the UML model.
	 * @param removeClassifiers
	 * @throws IOException
	 */
	public void removeAllProperties(Collection<? extends Classifier> removeClassifiers) throws IOException {
		for (Classifier classifier : removeClassifiers) {
			removeProperties(classifier);
			checkCancelled();
		}
	}

	/**
	 * Remove classifier from the UML model. Also removes contained classifiers.
	 * @param classifier
	 */
	public void removeClassifier(Classifier classifier) {
		assert classifier != null;
		final Element owner = classifier.getOwner();
		if (owner != null) {
			// null owner means already removed
			removeClassifier.setClassifier(classifier);
			removeClassifier.doSwitch(owner);
		}
	}

	/**
	 * Remove features of classifier from the UML model.
	 * @param classifier
	 */
	public void removeProperties(Classifier classifier) {
		assert classifier != null;
		removeClassifierProperties.doSwitch(classifier);
	}

	/**
	 * Recursively removes empty packages from fromPackage.
	 * @param fromPackage
	 */
	public void removeEmptyPackages(Package fromPackage) {
		for (Iterator<PackageableElement> it = fromPackage.getPackagedElements().iterator(); it.hasNext();) {
			PackageableElement o = it.next();
			if (o instanceof Package) {
				Package pack = (Package) o;
				removeEmptyPackages(pack);
				if (pack.getPackagedElements().isEmpty()) {
					JarToUML.logger.finer(String.format(
							JarToUMLResources.getString("RemoveFromModel.removing"), 
							JarToUML.qualifiedName(pack), 
							pack.eClass().getName())); //$NON-NLS-1$
					it.remove();
				}
			}
			checkCancelled();
		}
	}

}
