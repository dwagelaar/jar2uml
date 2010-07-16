package be.ac.vub.jar2uml.test.data;

public class B {
	
	public class BB {
		
	}
	
	private A.AA aaField;

	/**
	 * @return the aaField
	 */
	public A.AA getAaField() {
		return aaField;
	}

	/**
	 * @param aaField the aaField to set
	 */
	public void setAaField(A.AA aaField) {
		this.aaField = aaField;
	}
	
	public B() {
		super();
		A a = new A();
		a.setBbField(new BB());
	}
}
