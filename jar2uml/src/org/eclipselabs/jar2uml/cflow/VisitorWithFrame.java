package org.eclipselabs.jar2uml.cflow;

import org.apache.bcel.generic.Visitor;

public interface VisitorWithFrame extends Visitor {

	public void setFrame(SmartFrame frame);

}
