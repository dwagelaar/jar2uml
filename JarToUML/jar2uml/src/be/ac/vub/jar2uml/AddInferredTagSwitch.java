package be.ac.vub.jar2uml;

import java.util.Set;

import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.DataType;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.PackageableElement;
import org.eclipse.uml2.uml.util.UMLSwitch;

/**
 * Adds inferred {@link EAnnotation}s to elements that are not in {@link #getContainedClassifiers()}.
 * Switch returns <code>false</code> for each inferred element.
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public class AddInferredTagSwitch extends UMLSwitch<Boolean> {

	private Set<Classifier> containedClassifiers;
	
	/**
	 * @return the containedClassifiers
	 */
	public Set<Classifier> getContainedClassifiers() {
		return containedClassifiers;
	}

	/**
	 * @param containedClassifiers the containedClassifiers to set
	 */
	public void setContainedClassifiers(Set<Classifier> containedClassifiers) {
		this.containedClassifiers = containedClassifiers;
	}

	/**
	 * Adds a tag to indicate it has been inferred
	 * from class file references.
	 * @param element The element to add the tag to.
	 */
	protected void addInferredTag(Element element) {
		final EAnnotation ann = element.createEAnnotation("Jar2UML");
		final EMap<String, String> details = ann.getDetails();
		details.put("inferred", "true");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.uml2.uml.util.UMLSwitch#casePackage(org.eclipse.uml2.uml.Package)
	 */
	@Override
	public Boolean casePackage(Package object) {
		boolean isContained = false;
		for (PackageableElement element : object.getPackagedElements()) {
			isContained |= doSwitch(element);
		}
		if (!isContained) {
			addInferredTag(object);
		}
		return isContained;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.uml2.uml.util.UMLSwitch#caseClassifier(org.eclipse.uml2.uml.Classifier)
	 */
	@Override
	public Boolean caseClassifier(Classifier object) {
		return getContainedClassifiers().contains(object);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.uml2.uml.util.UMLSwitch#caseClass(org.eclipse.uml2.uml.Class)
	 */
	@Override
	public Boolean caseClass(Class object) {
		boolean isContained = false;
		for (Classifier nested : object.getNestedClassifiers()) {
			isContained |= doSwitch(nested);
		}
		isContained |= caseClassifier(object);
		if (!isContained) {
			addInferredTag(object);
		}
		return isContained;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.uml2.uml.util.UMLSwitch#caseDataType(org.eclipse.uml2.uml.DataType)
	 */
	@Override
	public Boolean caseDataType(DataType object) {
		boolean isContained = caseClassifier(object);
		if (!isContained) {
			addInferredTag(object);
		}
		return isContained;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.uml2.uml.util.UMLSwitch#caseInterface(org.eclipse.uml2.uml.Interface)
	 */
	@Override
	public Boolean caseInterface(Interface object) {
		boolean isContained = false;
		for (Classifier nested : object.getNestedClassifiers()) {
			isContained |= doSwitch(nested);
		}
		isContained |= caseClassifier(object);
		if (!isContained) {
			addInferredTag(object);
		}
		return isContained;
	}
	
}
