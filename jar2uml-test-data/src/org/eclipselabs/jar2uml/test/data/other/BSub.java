package org.eclipselabs.jar2uml.test.data.other;
import org.eclipselabs.jar2uml.test.data.B;


public class BSub extends B {
	
	public BSub() {
		super();
		B.testStaticProtected();
		this.testProtected();
//		new B().testProtected(); // this is illegal
	}

}
