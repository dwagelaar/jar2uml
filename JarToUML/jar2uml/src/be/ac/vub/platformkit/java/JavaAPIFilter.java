package be.ac.vub.platformkit.java;

public class JavaAPIFilter implements Filter {

	public boolean filter(String expression) {
		return (expression.startsWith("java")
				|| expression.startsWith("org/omg")
				|| expression.startsWith("org/w3c")
				|| expression.startsWith("org/xml")
				|| expression.startsWith("org/ietf"));
	}

}
