package be.ac.vub.jar2uml;

import org.apache.bcel.classfile.AccessFlags;
import org.apache.bcel.classfile.JavaClass;

/**
 * Includes only named public/protected elements.
 * @author dennis
 *
 */
public class PublicAPIFilter implements Filter {

	public boolean filter(String expression) {
		return true;
	}
	
	public boolean filter(JavaClass javaClass) {
		return JarToUML.isNamedClass(javaClass) && filter((AccessFlags) javaClass);
	}

	public boolean filter(AccessFlags flags) {
		return (flags.isPublic() || flags.isProtected());
	}

}
