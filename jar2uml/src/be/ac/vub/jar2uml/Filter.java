package be.ac.vub.jar2uml;

import org.apache.bcel.classfile.AccessFlags;
import org.apache.bcel.classfile.JavaClass;

/**
 * Filter for Java elements that should be included in the generated UML model.
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public interface Filter {
	
	/**
	 * @param expression The Java Jar file entry expression.
	 * @return True if the Jar entry should be included in the UML model.
	 */
	boolean filter(String expression);

	/**
	 * @param javaClass The parsed Java class of interface.
	 * @return True if the class/interface should be included in the UML model.
	 */
	boolean filter(JavaClass javaClass);

	/**
	 * @param flags The access modifier flags (public/protected/private) of a Java element.
	 * @return True if the given access modifier level should be included in the UML model.
	 */
	boolean filter(AccessFlags flags);

}
