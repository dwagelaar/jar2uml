package be.ac.vub.jar2uml;

import org.apache.bcel.classfile.AccessFlags;
import org.apache.bcel.classfile.JavaClass;

/**
 * Includes only named public/protected elements from the java.*, javax.*,
 * org.omg.*, org.w3c.*, org.xml.* and org.ietf.* packages.
 * @author dennis
 *
 */
public class JavaAPIFilter extends PublicAPIFilter {

	public boolean filter(String expression) {
		return (expression.startsWith("java")
				|| expression.startsWith("org/omg")
				|| expression.startsWith("org/w3c")
				|| expression.startsWith("org/xml")
				|| expression.startsWith("org/ietf"));
	}
	
	public boolean filter(JavaClass javaClass) {
		return super.filter(javaClass);
	}

	public boolean filter(AccessFlags flags) {
		return super.filter(flags);
	}

}
